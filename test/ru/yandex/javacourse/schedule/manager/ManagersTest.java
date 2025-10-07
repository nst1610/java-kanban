package ru.yandex.javacourse.schedule.manager;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ManagersTest {

    @Test
    public void testDefaultManagersNotNull() {
        assertNotNull(Managers.getDefault(), "default manager should not be null");
        assertNotNull(Managers.getDefaultHistory(), "default history managers should not be null");
    }

}
