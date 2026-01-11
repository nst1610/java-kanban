package ru.yandex.javacourse.schedule.manager.impl;

import static ru.yandex.javacourse.schedule.tasks.Fields.DESCRIPTION;
import static ru.yandex.javacourse.schedule.tasks.Fields.DURATION;
import static ru.yandex.javacourse.schedule.tasks.Fields.END_TIME;
import static ru.yandex.javacourse.schedule.tasks.Fields.EPIC;
import static ru.yandex.javacourse.schedule.tasks.Fields.ID;
import static ru.yandex.javacourse.schedule.tasks.Fields.NAME;
import static ru.yandex.javacourse.schedule.tasks.Fields.START_TIME;
import static ru.yandex.javacourse.schedule.tasks.Fields.STATUS;
import static ru.yandex.javacourse.schedule.tasks.Fields.TYPE;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import ru.yandex.javacourse.schedule.exceptions.ManagerSaveException;
import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Fields;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;
import ru.yandex.javacourse.schedule.tasks.TaskType;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path filePath;
    private final Path historyPath;
    private static final String NAMES = Arrays.stream(Fields.values()).map(Fields::getName).collect(Collectors.joining(","));

    public FileBackedTaskManager(Path filePath, Path historyPath) {
        this.filePath = filePath;
        this.historyPath = historyPath;
        try {
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Task getTask(int id) {
        final Task task = super.getTask(id);
        saveHistory();
        return task;
    }

    @Override
    public Subtask getSubtask(int id) {
        final Subtask subtask = super.getSubtask(id);
        saveHistory();
        return subtask;
    }

    @Override
    public Epic getEpic(int id) {
        final Epic epic = super.getEpic(id);
        saveHistory();
        return epic;
    }

    @Override
    public int addNewTask(Task task) {
        int id = super.addNewTask(task);
        save();
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int id = super.addNewEpic(epic);
        save();
        return id;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        Integer id = super.addNewSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
        saveHistory();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
        saveHistory();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
        saveHistory();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
        saveHistory();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
        saveHistory();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
        saveHistory();
    }

    private void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath.toString()))) {
            bw.write(NAMES);
            bw.newLine();
            List<Task> allTasks = new ArrayList<>();
            allTasks.addAll(getTasks());
            allTasks.addAll(getEpics());
            allTasks.addAll(getSubtasks());

            for (Task task : allTasks) {
                bw.write(task.toCsvString());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Failed to save to file.");
        }
    }

    private void saveHistory() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(historyPath.toString()))) {
            bw.write(historyToString());
        } catch (IOException e) {
            throw new ManagerSaveException("Failed to save history to file.");
        }
    }

    public static FileBackedTaskManager loadFromFile(File file, File historyFile) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.toPath(), historyFile.toPath());
        Map<Integer, Task> allTasks = new HashMap<>();
        int maxTaskId = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String names = br.readLine();
            while (br.ready()) {
                String line = br.readLine();
                Task currentTask = fromString(line);
                switch (currentTask.getType()) {
                    case TaskType.TASK -> {
                        manager.tasks.put(currentTask.getId(), currentTask);
                        if (Objects.nonNull(currentTask.getStartTime())) {
                            manager.prioritizedTasks.add(currentTask);
                        }
                    }
                    case TaskType.EPIC -> manager.epics.put(currentTask.getId(), (Epic) currentTask);
                    case TaskType.SUB_TASK -> {
                        manager.subtasks.put(currentTask.getId(), (Subtask) currentTask);
                        Integer epicId = ((Subtask) currentTask).getEpicId();
                        manager.epics.get(epicId).getSubtaskIds().add(currentTask.getId());
                        if (Objects.nonNull(currentTask.getStartTime())) {
                            manager.prioritizedTasks.add(currentTask);
                        }
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + currentTask.getType());
                }
                maxTaskId = Math.max(maxTaskId, currentTask.getId());
                allTasks.put(currentTask.getId(), currentTask);
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Failed to load from file.");
        } finally {
            manager.generatorId = maxTaskId;
        }
        for (Integer id : loadHistoryFromFile(historyFile)) {
            manager.historyManager.addTask(allTasks.get(id));
        }
        return manager;
    }

    private static List<Integer> loadHistoryFromFile(File historyFile) {
        List<Integer> taskIds = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(historyFile))) {
            String line = br.readLine();
            if (line != null && !line.isBlank()) {
                for (String id : line.split(",")) {
                    taskIds.add(Integer.valueOf(id));
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Failed to load history from file.");
        }
        return taskIds;
    }

    private static Task fromString(String line) {
        String[] fields = line.split(",", -1);
        Integer id = Integer.valueOf(fields[ID.getId()]);
        TaskType type = TaskType.valueOf(fields[TYPE.getId()]);
        String name = fields[NAME.getId()];
        TaskStatus status = TaskStatus.valueOf(fields[STATUS.getId()]);
        String description = fields[DESCRIPTION.getId()];
        LocalDateTime startTime = fields[START_TIME.getId()].isBlank() ? null : LocalDateTime.parse(fields[START_TIME.getId()]);
        Duration duration = Duration.ofMinutes(Long.parseLong(fields[DURATION.getId()]));
        LocalDateTime endTime = fields[END_TIME.getId()].isBlank() ? null : LocalDateTime.parse(fields[END_TIME.getId()]);
        Integer epicId = fields[EPIC.getId()].isBlank() ? null : Integer.valueOf(fields[EPIC.getId()]);
        switch (type) {
            case TaskType.TASK -> {
                return new Task(id, name, description, status, duration, startTime);
            }
            case TaskType.EPIC -> {
                Epic epic = new Epic(id, name, description);
                epic.setStatus(status);
                epic.setStartTime(startTime);
                epic.setDuration(duration);
                epic.setEndTime(endTime);
                return epic;
            }
            case TaskType.SUB_TASK -> {
                return new Subtask(id, name, description, status, epicId, duration, startTime);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    private String historyToString() {
        return super.getHistory().stream()
            .map(task -> String.valueOf(task.getId())).collect(Collectors.joining(","));
    }

    public static void main(String[] args) {
        File tempFile, historyFile;
        try {
            tempFile = File.createTempFile("task", ".csv");
            historyFile = File.createTempFile("history", ".csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TaskManager manager = Managers.getFileBackedTaskManager(tempFile.toPath(), historyFile.toPath());

        Task task1 = new Task("Task1", "Task1 description", NEW);
        Task task2 = new Task("Task2", "Task2 description", NEW);
        manager.addNewTask(task1);
        manager.addNewTask(task2);

        Epic epic1 = new Epic("Epic1", "Epic1 description");
        int epic1Id = manager.addNewEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask1-1", "Subtask1 description", NEW, epic1Id);
        Subtask subtask2 = new Subtask("Subtask1-2", "Subtask1 description", NEW, epic1Id);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        TaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile, historyFile);

        System.out.printf("Count tasks in first manager: %d. Count tasks in new manager: %d.\n",
            manager.getTasks().size(), loadedManager.getTasks().size());
        System.out.printf("Count subtasks in first manager: %d. Count subtasks in new manager: %d.\n",
            manager.getSubtasks().size(), loadedManager.getSubtasks().size());
        System.out.printf("Count epics in first manager: %d. Count epics in new manager: %d.\n",
            manager.getEpics().size(), loadedManager.getEpics().size());
        System.out.println();
        System.out.println("First manager tasks: ");
        for (Task task : manager.getTasks()) {
            System.out.println(task);
        }
        System.out.println("New manager tasks: ");
        for (Task task : loadedManager.getTasks()) {
            System.out.println(task);
        }
        System.out.println();
        System.out.println("First manager epics: ");
        for (Epic epic : manager.getEpics()) {
            System.out.println(epic);
        }
        System.out.println("New manager epics: ");
        for (Epic epic : loadedManager.getEpics()) {
            System.out.println(epic);
        }
        System.out.println();
        System.out.println("First manager subtasks: ");
        for (Subtask subtask : manager.getSubtasks()) {
            System.out.println(subtask);
        }
        System.out.println("New manager subtasks: ");
        for (Subtask subtask : loadedManager.getSubtasks()) {
            System.out.println(subtask);
        }

    }
}
