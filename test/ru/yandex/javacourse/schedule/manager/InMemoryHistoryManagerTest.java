package ru.yandex.javacourse.schedule.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

public class InMemoryHistoryManagerTest {

    HistoryManager historyManager;

    @BeforeEach
    public void initHistoryManager(){
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void testAddTaskAndReturnHistory() {
        Task task1 = new Task(1, "Task1", "Task1 description", TaskStatus.NEW);
        Task task2 = new Task(2, "Task2", "Task2 description", TaskStatus.NEW);
        historyManager.addTask(task1);
        historyManager.addTask(task2);
        List<Task> list = historyManager.getHistory();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
    }

    @Test
    void testMoveExistingTaskToEnd() {
        Task task1 = new Task(1, "Task1", "Task1 description", TaskStatus.NEW);
        Task task2 = new Task(2, "Task2", "Task2 description", TaskStatus.NEW);
        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.addTask(task1);
        List<Task> list = historyManager.getHistory();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(1).getId());
    }

    @Test
    public void testHistoricVersions(){
        Task task = new Task(1, "Task", "Task1 description", TaskStatus.NEW);
        historyManager.addTask(task);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be added");
        task.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.addTask(task);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be added");
    }

    @Test
    public void testHistoricVersionsByPointer(){
        Task task = new Task(1, "Task", "Task1 description", TaskStatus.NEW);
        historyManager.addTask(task);
        assertEquals(task.getStatus(), historyManager.getHistory().get(0).getStatus(), "historic task should be stored");
        task.setStatus(TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.NEW, historyManager.getHistory().get(0).getStatus(), "historic task should not be changed");
    }

    @Test
    void testRemoveFromHistory() {
        Task task1 = new Task(1, "Task1", "Task1 description", TaskStatus.NEW);
        Task task2 = new Task(2, "Task2", "Task2 description", TaskStatus.NEW);
        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.remove(1);
        List<Task> list = historyManager.getHistory();
        assertEquals(1, list.size());
        assertEquals(2, list.get(0).getId());
    }

}
