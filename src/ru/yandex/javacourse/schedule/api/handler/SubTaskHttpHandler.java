package ru.yandex.javacourse.schedule.api.handler;

import static java.util.Objects.nonNull;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Subtask;

public class SubTaskHttpHandler extends BaseHttpHandler {
    public SubTaskHttpHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        if (splitPath(exchange).length == 2) {
            sendText(exchange, gson.toJson(taskManager.getSubtasks()), 200);
        }
        int subtaskId = Integer.parseInt(splitPath(exchange)[2]);
        sendText(exchange, gson.toJson(taskManager.getSubtask(subtaskId)), 200);
    }

    @Override
    protected void handlePost(HttpExchange exchange) throws IOException {
        Subtask subtask = gson.fromJson(
            new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8),
            Subtask.class
        );
        if (nonNull(subtask.getId())) {
            taskManager.updateSubtask(subtask);
        } else {
            taskManager.addNewSubtask(subtask);
        }
        sendEmpty(exchange, 201);
    }

    @Override
    protected void handleDelete(HttpExchange exchange) throws IOException {
        int subtaskId = Integer.parseInt(splitPath(exchange)[2]);
        taskManager.deleteSubtask(subtaskId);
        sendEmpty(exchange, 200);
    }
}
