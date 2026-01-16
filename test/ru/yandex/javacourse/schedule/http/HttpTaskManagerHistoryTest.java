package ru.yandex.javacourse.schedule.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.api.HttpTaskServer;
import ru.yandex.javacourse.schedule.api.adapter.DurationAdapter;
import ru.yandex.javacourse.schedule.api.adapter.LocalDateTimeAdapter;
import ru.yandex.javacourse.schedule.api.token.TaskListTypeToken;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.manager.impl.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

public class HttpTaskManagerHistoryTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);

    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Duration.class, new DurationAdapter())
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    public HttpTaskManagerHistoryTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteTasks();
        manager.deleteSubtasks();
        manager.deleteEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testGetHistory() throws IOException, InterruptedException {
        Task t1 = new Task("Task1", "Task1 description", TaskStatus.NEW, Duration.ofMinutes(10), LocalDateTime.now());
        Task t2 = new Task("Task2", "Task2 description", TaskStatus.NEW, Duration.ofMinutes(10),
            LocalDateTime.now().minusMinutes(40));
        Task t3 = new Task("Task3", "Task3 description", TaskStatus.NEW, Duration.ofMinutes(10),
            LocalDateTime.now().minusMinutes(60));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request1 = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(t1))).build();
        client.send(request1, HttpResponse.BodyHandlers.ofString());
        HttpRequest request2 = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(t2))).build();
        client.send(request2, HttpResponse.BodyHandlers.ofString());
        HttpRequest request3 = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(t3))).build();
        client.send(request3, HttpResponse.BodyHandlers.ofString());

        manager.getTask(2);
        manager.getTask(1);

        URI historyUrl = URI.create("http://localhost:8080/history");
        HttpRequest historyRequest = HttpRequest.newBuilder().uri(historyUrl).GET().build();
        HttpResponse<String> historyResponse = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, historyResponse.statusCode());

        List<Task> tasks = gson.fromJson(historyResponse.body(), new TaskListTypeToken().getType());
        assertEquals(2, tasks.size());
        assertEquals(t1.getName(), tasks.get(1).getName());
        assertEquals(t2.getName(), tasks.get(0).getName());
    }
}
