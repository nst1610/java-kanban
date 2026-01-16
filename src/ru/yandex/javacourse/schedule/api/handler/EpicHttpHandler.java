package ru.yandex.javacourse.schedule.api.handler;

import static java.util.Objects.nonNull;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;

public class EpicHttpHandler extends BaseHttpHandler {
    public EpicHttpHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        String[] splitPath = splitPath(exchange);
        if (splitPath.length == 2) {
            sendText(exchange, gson.toJson(taskManager.getEpics()), 200);
        }
        int epicId = Integer.parseInt(splitPath[2]);
        if (splitPath.length == 4) {
            sendText(exchange, gson.toJson(taskManager.getEpicSubtasks(epicId)), 200);
        } else {
            sendText(exchange, gson.toJson(taskManager.getEpic(epicId)), 200);
        }
    }

    @Override
    protected void handlePost(HttpExchange exchange) throws IOException {
        Epic epic = gson.fromJson(
            new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8),
            Epic.class
        );
        if (nonNull(epic.getId())) {
            taskManager.updateEpic(epic);
        } else {
            taskManager.addNewEpic(epic);
        }
        sendEmpty(exchange, 201);
    }

    @Override
    protected void handleDelete(HttpExchange exchange) throws IOException {
        int epicId = Integer.parseInt(splitPath(exchange)[2]);
        taskManager.deleteEpic(epicId);
        sendEmpty(exchange, 200);
    }
}
