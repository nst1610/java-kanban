package ru.yandex.javacourse.schedule.api.handler;

import static java.util.Objects.nonNull;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;

public class TaskHttpHandler extends BaseHttpHandler {

    public TaskHttpHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        if (splitPath(exchange).length == 2) {
            sendText(exchange, gson.toJson(taskManager.getTasks()), 200);
        }
        int taskId = Integer.parseInt(splitPath(exchange)[2]);
        sendText(exchange, gson.toJson(taskManager.getTask(taskId)), 200);
    }

    @Override
    protected void handlePost(HttpExchange exchange) throws IOException {
        Task task = gson.fromJson(
            new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8),
            Task.class
        );
        if (nonNull(task.getId())) {
            taskManager.updateTask(task);
        } else {
            taskManager.addNewTask(task);
        }
        sendEmpty(exchange, 201);
    }

    @Override
    protected void handleDelete(HttpExchange exchange) throws IOException {
        int taskId = Integer.parseInt(splitPath(exchange)[2]);
        taskManager.deleteTask(taskId);
        sendEmpty(exchange, 200);
    }
}
