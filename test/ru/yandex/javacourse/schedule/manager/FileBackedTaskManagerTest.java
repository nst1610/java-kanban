package ru.yandex.javacourse.schedule.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.manager.impl.FileBackedTaskManager;
import ru.yandex.javacourse.schedule.manager.impl.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

public class FileBackedTaskManagerTest {
    FileBackedTaskManager fileManager;
    File tempFile, historyFile;

    @BeforeEach
    public void initManager() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        historyFile = File.createTempFile("test_history", ".csv");
        fileManager = Managers.getFileBackedTaskManager(tempFile.toPath(), historyFile.toPath());
    }

    @Test
    void testLoadEmptyFile() {
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile, historyFile);
        assertTrue(loaded.getTasks().isEmpty());
        assertTrue(loaded.getEpics().isEmpty());
        assertTrue(loaded.getSubtasks().isEmpty());
        assertTrue(loaded.getHistory().isEmpty());
    }

    @Test
    void testSaveTasks() throws IOException {
        Task task1 = new Task("Task1", "Task1 description", TaskStatus.NEW);
        Task task2 = new Task("Task2", "Task2 description", TaskStatus.DONE);
        fileManager.addNewTask(task1);
        fileManager.addNewTask(task2);
        List<String> lines = Files.readAllLines(tempFile.toPath());
        assertEquals(3, lines.size());
        assertTrue(lines.get(1).contains("Task1"));
        assertTrue(lines.get(2).contains("Task2"));
    }

    @Test
    void testLoadTasks() {
        Task task1 = new Task("Task1", "Task1 description", TaskStatus.NEW);
        Task task2 = new Task("Task2", "Task2 description", TaskStatus.IN_PROGRESS);
        int id1 = fileManager.addNewTask(task1);
        int id2 = fileManager.addNewTask(task2);
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile, historyFile);
        assertEquals(2, loaded.getTasks().size());
        assertEquals("Task1", loaded.getTask(id1).getName());
        assertEquals("Task2", loaded.getTask(id2).getName());
        assertEquals(TaskStatus.IN_PROGRESS, loaded.getTask(id2).getStatus());
    }

    @Test
    void testSaveAndLoadEpicsAndSubtasks() {
        Epic epic = new Epic("Epic1", "Epic1 description");
        int epicId = fileManager.addNewEpic(epic);
        Subtask subtask1 = new Subtask("Subtask1", "Subtask1 description", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask2", "Subtask2 description", TaskStatus.DONE, epicId);
        int s1Id = fileManager.addNewSubtask(subtask1);
        int s2Id = fileManager.addNewSubtask(subtask2);
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile, historyFile);
        Epic loadedEpic = loaded.getEpic(epicId);
        assertEquals(2, loadedEpic.getSubtaskIds().size());
        assertTrue(loadedEpic.getSubtaskIds().contains(s1Id));
        assertTrue(loadedEpic.getSubtaskIds().contains(s2Id));
        Subtask loadedS1 = loaded.getSubtask(s1Id);
        assertEquals(TaskStatus.NEW, loadedS1.getStatus());
    }

    @Test
    void testBehaveSameAsInMemoryManager() {
        InMemoryTaskManager inMemory = new InMemoryTaskManager();
        Task task1 = new Task("Task1", "Task1 description", TaskStatus.NEW);
        Task task2 = new Task("Task2", "Task2 description", TaskStatus.NEW);
        int id1Memory = inMemory.addNewTask(task1);
        int id2Memory = inMemory.addNewTask(task2);
        int id1File = fileManager.addNewTask(task1);
        int id2File = fileManager.addNewTask(task2);
        assertEquals(inMemory.getTasks().size(), fileManager.getTasks().size());
        assertEquals(inMemory.getTask(id1Memory).getName(), fileManager.getTask(id1File).getName());
        assertEquals(inMemory.getTask(id2Memory).getName(), fileManager.getTask(id2File).getName());
    }

    @Test
    void testIdGenerator() {
        InMemoryTaskManager inMemory = new InMemoryTaskManager();
        Task task1 = new Task("Task1", "Task1 description", TaskStatus.NEW);
        fileManager.addNewTask(task1);
        inMemory.addNewTask(task1);
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile, historyFile);
        int newId = loaded.addNewTask(new Task("Task2", "Task2 description", TaskStatus.NEW));
        assertEquals(2, newId);
    }

    @Test
    void testDeleteTask() {
        Task task1 = new Task("Task1", "Task1 description", TaskStatus.NEW);
        int id1 = fileManager.addNewTask(task1);
        fileManager.deleteTask(id1);
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile, historyFile);
        assertTrue(loaded.getTasks().isEmpty());
    }

    @Test
    void testSaveHistory() throws IOException {
        Task task1 = new Task("Task1", "Task1 description", TaskStatus.NEW);
        Task task2 = new Task("Task2", "Task2 description", TaskStatus.DONE);
        int id1 = fileManager.addNewTask(task1);
        int id2 = fileManager.addNewTask(task2);
        fileManager.getTask(id1);
        fileManager.getTask(id2);
        fileManager.getTask(id1);
        List<String> historyLines = Files.readAllLines(historyFile.toPath());
        assertEquals(1, historyLines.size());
        String[] ids = historyLines.getFirst().split(",");
        assertEquals(2, ids.length);
        assertEquals(String.valueOf(id2), ids[0]);
        assertEquals(String.valueOf(id1), ids[1]);
    }

    @Test
    void testLoadHistory() {
        Task task1 = new Task("Task1", "Task1 description", TaskStatus.NEW);
        Task task2 = new Task("Task2", "Task2 description", TaskStatus.DONE);
        int id1 = fileManager.addNewTask(task1);
        int id2 = fileManager.addNewTask(task2);
        fileManager.getTask(id1);
        fileManager.getTask(id2);
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile, historyFile);
        List<Task> history = loaded.getHistory();
        assertEquals(2, history.size());
        assertEquals(id1, history.get(0).getId());
        assertEquals(id2, history.get(1).getId());
    }

    @Test
    void testHistoryAfterDelete() {
        Epic epic = new Epic("Epic1", "Epic1 description");
        int epicId = fileManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Subtask1 description", TaskStatus.NEW, epicId);
        int subId = fileManager.addNewSubtask(subtask);
        fileManager.getEpic(epicId);
        fileManager.getSubtask(subId);
        fileManager.deleteEpic(epicId);
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile, historyFile);
        assertTrue(loaded.getHistory().isEmpty());
    }
}
