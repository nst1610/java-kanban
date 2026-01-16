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
import ru.yandex.javacourse.schedule.api.token.TaskListTypeToken;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.manager.impl.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

public class HttpTaskManagerTasksTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);

    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Duration.class, new DurationAdapter())
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    public HttpTaskManagerTasksTest() throws IOException {
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
    public void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("Task1", "Task1 description", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task))).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        List<Task> tasksFromManager = manager.getTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(task.getName(), tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task1", "Task1 description", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        Task task2 = new Task("Task2", "Task2 description", TaskStatus.NEW, Duration.ofMinutes(5),
            LocalDateTime.now().minusMinutes(15));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request1 = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task1))).build();
        client.send(request1, HttpResponse.BodyHandlers.ofString());
        HttpRequest request2 = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task2))).build();
        client.send(request2, HttpResponse.BodyHandlers.ofString());

        HttpRequest getRequest = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        List<Task> tasks = gson.fromJson(getResponse.body(), new TaskListTypeToken().getType());

        assertEquals(200, getResponse.statusCode());
        assertEquals(2, tasks.size(), "Некорректное количество задач");
    }

    @Test
    public void testGetTask() throws IOException, InterruptedException {
        Task task1 = new Task("Task1", "Task1 description", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request1 = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task1))).build();
        client.send(request1, HttpResponse.BodyHandlers.ofString());

        URI getUrl = URI.create("http://localhost:8080/tasks/1");
        HttpRequest getRequest = HttpRequest.newBuilder().uri(getUrl).GET().build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, getResponse.statusCode());
        assertEquals(task1.getName(), gson.fromJson(getResponse.body(), Task.class).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testDeleteTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task1", "Task1 description", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        Task task2 = new Task("Task2", "Task2 description", TaskStatus.NEW, Duration.ofMinutes(5),
            LocalDateTime.now().minusMinutes(15));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request1 = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task1))).build();
        client.send(request1, HttpResponse.BodyHandlers.ofString());
        HttpRequest request2 = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task2))).build();
        client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(2, manager.getTasks().size(), "Некорректное количество задач");

        URI deleteUrl = URI.create("http://localhost:8080/tasks/1");
        HttpRequest deleteRequest = HttpRequest.newBuilder().uri(deleteUrl).DELETE().build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode());
        assertEquals(1, manager.getTasks().size(), "Некорректное количество задач");
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Task1", "Task1 description", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task))).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Task updated = new Task(1, "Task1Updated", "Task1 description",
            TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now().minusMinutes(30));
        HttpRequest updateRequest = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(updated))).build();
        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, updateResponse.statusCode());
        assertEquals(updated.getName(), manager.getTasks().get(0).getName(), "Некорректное имя задачи");
    }
}
