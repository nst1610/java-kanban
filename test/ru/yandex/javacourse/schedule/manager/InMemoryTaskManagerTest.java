package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import ru.yandex.javacourse.schedule.manager.impl.InMemoryTaskManager;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    public void initManager(){
        taskManager = new InMemoryTaskManager();
    }

}
