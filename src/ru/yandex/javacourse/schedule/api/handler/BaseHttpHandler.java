package ru.yandex.javacourse.schedule.api.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import ru.yandex.javacourse.schedule.api.adapter.DurationAdapter;
import ru.yandex.javacourse.schedule.api.adapter.LocalDateTimeAdapter;
import ru.yandex.javacourse.schedule.exceptions.IntersectionException;
import ru.yandex.javacourse.schedule.exceptions.NotFoundException;
import ru.yandex.javacourse.schedule.manager.TaskManager;

public class BaseHttpHandler implements HttpHandler {
    protected final TaskManager taskManager;
    protected final Gson gson;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (exchange.getRequestMethod()) {
                case "GET": {
                    handleGet(exchange);
                    break;
                }
                case "POST": {
                    handlePost(exchange);
                    break;
                }
                case "DELETE": {
                    handleDelete(exchange);
                    break;
                }
                default: {
                    sendNotRealized(exchange);
                }
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (IntersectionException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void handleGet(HttpExchange exchange) throws IOException {
        sendNotRealized(exchange);
    }

    protected void handlePost(HttpExchange exchange) throws IOException {
        sendNotRealized(exchange);
    }

    protected void handleDelete(HttpExchange exchange) throws IOException {
        sendNotRealized(exchange);
    }

    protected String[] splitPath(HttpExchange exchange) {
        return exchange.getRequestURI().getPath().split("/");
    }

    protected void sendText(HttpExchange exchange, String text, int status) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(status, response.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response);
        }
    }

    protected void sendNotFound(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, 404);
    }

    protected void sendHasInteractions(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, 406);
    }

    protected void sendEmpty(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, 0);
        exchange.close();
    }

    protected void sendNotRealized(HttpExchange exchange) throws IOException {
        sendText(exchange, String.format("Метод %s не поддерживается.", exchange.getRequestMethod()), 400);
    }
}
