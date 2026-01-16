package ru.yandex.javacourse.schedule.api;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import ru.yandex.javacourse.schedule.api.handler.EpicHttpHandler;
import ru.yandex.javacourse.schedule.api.handler.HistoryHttpHandler;
import ru.yandex.javacourse.schedule.api.handler.PrioritizedHttpHandler;
import ru.yandex.javacourse.schedule.api.handler.SubTaskHttpHandler;
import ru.yandex.javacourse.schedule.api.handler.TaskHttpHandler;
import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer httpServer;
    private final TaskManager taskManager;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        this.taskManager = taskManager;
    }

    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefault();
        Task task1 = new Task("Task1", "Task1 description", NEW);
        Task task2 = new Task("Task2", "Task2 description", NEW);
        manager.addNewTask(task1);
        manager.addNewTask(task2);
        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();
    }

    public void start() {
        httpServer.createContext("/tasks", new TaskHttpHandler(taskManager));
        httpServer.createContext("/subtasks", new SubTaskHttpHandler(taskManager));
        httpServer.createContext("/epics", new EpicHttpHandler(taskManager));
        httpServer.createContext("/history", new HistoryHttpHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHttpHandler(taskManager));
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }
}
