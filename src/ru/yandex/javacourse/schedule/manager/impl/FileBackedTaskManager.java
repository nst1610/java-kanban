package ru.yandex.javacourse.schedule.manager.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import ru.yandex.javacourse.schedule.exceptions.ManagerSaveException;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;
import ru.yandex.javacourse.schedule.tasks.TaskType;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path filePath;
    private static final String NAMES = "id,type,name,status,description,epic";

    public FileBackedTaskManager(Path filePath) {
        this.filePath = filePath;
        try {
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        int id = super.addNewSubtask(subtask);
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
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
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

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.toPath());
        int maxTaskId = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String names = br.readLine();
            while (br.ready()) {
                String line = br.readLine();
                Task currentTask = fromString(line);
                switch (currentTask.getType()) {
                    case TaskType.TASK -> manager.tasks.put(currentTask.getId(), currentTask);
                    case TaskType.EPIC -> manager.epics.put(currentTask.getId(), (Epic) currentTask);
                    case TaskType.SUB_TASK -> {
                        manager.subtasks.put(currentTask.getId(), (Subtask) currentTask);
                        Integer epicId = ((Subtask) currentTask).getEpicId();
                        manager.epics.get(epicId).getSubtaskIds().add(currentTask.getId());
                    }
                }
                maxTaskId = Math.max(maxTaskId, currentTask.getId());
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Failed to load from file.");
        }
        finally {
            manager.generatorId = maxTaskId;
        }
        return manager;
    }

    private static Task fromString(String line) {
        String[] fields = line.split(",", -1);
        Integer id = Integer.valueOf(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];
        Integer epicId = fields[5].isBlank() ? null : Integer.valueOf(fields[5]);
        switch (type) {
            case TaskType.TASK -> {
                return new Task(id, name, description, status);
            }
            case TaskType.EPIC -> {
                Epic epic = new Epic(id, name, description);
                epic.setStatus(status);
                return epic;
            }
            case TaskType.SUB_TASK -> {
                return new Subtask(id, name, description, status, epicId);
            }
            default -> throw new IllegalArgumentException();
        }
    }
}
