package ru.yandex.javacourse.schedule.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import ru.yandex.javacourse.schedule.api.token.SubtaskListTypeToken;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.manager.impl.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

public class HttpTaskMangerSubTasksTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);

    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Duration.class, new DurationAdapter())
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    public HttpTaskMangerSubTasksTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteTasks();
        manager.deleteSubtasks();
        manager.deleteEpics();
        taskServer.start();

        Epic epic = new Epic("Epic1", "Epic1 description");
        manager.addNewEpic(epic);
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Subtask1", "Subtask1 description",
            TaskStatus.NEW, 1, Duration.ofMinutes(5), LocalDateTime.now());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask))).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals(subtask.getName(), subtasksFromManager.get(0).getName(), "Некорректное имя подзадачи");
    }

    @Test
    public void testGetSubtasks() throws IOException, InterruptedException {
        Subtask subtask1 = new Subtask("Subtask1", "Subtask1 description",
            TaskStatus.NEW, 1, Duration.ofMinutes(5), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Subtask2", "Subtask2 description",
            TaskStatus.NEW, 1, Duration.ofMinutes(5), LocalDateTime.now().minusMinutes(15));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request1 = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask1))).build();
        client.send(request1, HttpResponse.BodyHandlers.ofString());
        HttpRequest request2 = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask2))).build();
        client.send(request2, HttpResponse.BodyHandlers.ofString());

        HttpRequest getRequest = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        List<Subtask> subtasks = gson.fromJson(getResponse.body(), new SubtaskListTypeToken().getType());

        assertEquals(200, getResponse.statusCode());
        assertEquals(2, subtasks.size(), "Некорректное количество подзадач");
    }

    @Test
    public void testGetSubtask() throws IOException, InterruptedException {
        Subtask subtask1 = new Subtask("Subtask1", "Subtask1 description",
            TaskStatus.NEW, 1, Duration.ofMinutes(5), LocalDateTime.now());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request1 = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask1))).build();
        client.send(request1, HttpResponse.BodyHandlers.ofString());

        URI getUrl = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest getRequest = HttpRequest.newBuilder().uri(getUrl).GET().build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, getResponse.statusCode());
        assertEquals(subtask1.getName(), gson.fromJson(getResponse.body(),
            Subtask.class).getName(), "Некорректное имя подзадачи");
    }

    @Test
    public void testDeleteSubtasks() throws IOException, InterruptedException {
        Subtask subtask1 = new Subtask("Subtask1", "Subtask1 description",
            TaskStatus.NEW, 1, Duration.ofMinutes(5), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Subtask2", "Subtask2 description",
            TaskStatus.NEW, 1, Duration.ofMinutes(5), LocalDateTime.now().minusMinutes(15));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request1 = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask1))).build();
        client.send(request1, HttpResponse.BodyHandlers.ofString());
        HttpRequest request2 = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask2))).build();
        client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(2, manager.getSubtasks().size(), "Некорректное количество подзадач");

        URI deleteUrl = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest deleteRequest = HttpRequest.newBuilder().uri(deleteUrl).DELETE().build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode());
        assertEquals(1, manager.getSubtasks().size(), "Некорректное количество подзадач");
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Subtask1", "Subtask1 description",
            TaskStatus.NEW, 1, Duration.ofMinutes(5), LocalDateTime.now());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask))).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Subtask updated = new Subtask(2, "Subtask1Updated", "Subtask1 description",
            TaskStatus.NEW, 1, Duration.ofMinutes(5), LocalDateTime.now().minusMinutes(30));
        HttpRequest updateRequest = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(updated))).build();
        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, updateResponse.statusCode());
        assertEquals(updated.getName(), manager.getSubtasks().get(0).getName(), "Некорректное имя подзадачи");
    }
}
