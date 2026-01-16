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
import ru.yandex.javacourse.schedule.api.token.EpicListTypeToken;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.manager.impl.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;

public class HttpTaskManagerEpicsTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);

    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Duration.class, new DurationAdapter())
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    public HttpTaskManagerEpicsTest() throws IOException {
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
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic1", "Epic1 description");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic))).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        List<Epic> epicsFromManager = manager.getEpics();
        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals(epic.getName(), epicsFromManager.get(0).getName(), "Некорректное имя эпика");
    }

    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic1", "Epic1 description");
        Epic epic2 = new Epic("Epic2", "Epic2 description");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request1 = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic1))).build();
        client.send(request1, HttpResponse.BodyHandlers.ofString());
        HttpRequest request2 = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic2))).build();
        client.send(request2, HttpResponse.BodyHandlers.ofString());

        HttpRequest getRequest = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        List<Epic> epics = gson.fromJson(getResponse.body(), new EpicListTypeToken().getType());

        assertEquals(200, getResponse.statusCode());
        assertEquals(2, epics.size(), "Некорректное количество эпиков");
    }

    @Test
    public void testGetEpic() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic1", "Epic1 description");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request1 = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic1))).build();
        client.send(request1, HttpResponse.BodyHandlers.ofString());

        URI getUrl = URI.create("http://localhost:8080/epics/1");
        HttpRequest getRequest = HttpRequest.newBuilder().uri(getUrl).GET().build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, getResponse.statusCode());
        assertEquals(epic1.getName(), gson.fromJson(getResponse.body(), Epic.class).getName(), "Некорректное имя эпика");
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic1", "Epic1 description");
        Epic epic2 = new Epic("Epic2", "Epic2 description");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request1 = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic1))).build();
        client.send(request1, HttpResponse.BodyHandlers.ofString());
        HttpRequest request2 = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic2))).build();
        client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(2, manager.getEpics().size(), "Некорректное количество эпиков");

        URI deleteUrl = URI.create("http://localhost:8080/epics/1");
        HttpRequest deleteRequest = HttpRequest.newBuilder().uri(deleteUrl).DELETE().build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode());
        assertEquals(1, manager.getEpics().size(), "Некорректное количество эпиков");
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic1", "Epic1 description");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic1))).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Epic updated = new Epic(1, "Epic1Updated", "Epic1 description");
        HttpRequest updateRequest = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(updated))).build();
        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, updateResponse.statusCode());
        assertEquals(updated.getName(), manager.getEpics().get(0).getName(), "Некорректное имя эпика");
    }
}
