package ru.yandex.javacourse.schedule.api.handler;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import ru.yandex.javacourse.schedule.manager.TaskManager;

public class PrioritizedHttpHandler extends BaseHttpHandler {

    public PrioritizedHttpHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(taskManager.getPrioritizedTasks()), 200);
    }
}
