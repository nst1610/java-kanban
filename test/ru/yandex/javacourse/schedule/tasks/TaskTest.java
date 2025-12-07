package ru.yandex.javacourse.schedule.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class TaskTest {

    @Test
    public void testCompareById(){
        Task t0 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        Task t1 = new Task(1, "Test 2", "Testing task 2", TaskStatus.IN_PROGRESS);
        assertEquals(t0, t1, "task entities should be compared by id");
    }

    @Test
    void testTaskEndTimeCalculation() {
        LocalDateTime start = LocalDateTime.of(2025, 10, 1, 15, 0);
        Task t = new Task("Test 1", "Testing task 1", TaskStatus.NEW, Duration.ofMinutes(10), start);
        assertEquals(LocalDateTime.of(2025, 10, 1, 15, 10), t.getEndTime());
    }

}
