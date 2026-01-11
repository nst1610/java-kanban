package ru.yandex.javacourse.schedule.tasks;

import java.util.Comparator;

public class TaskComparator implements Comparator<Task> {
    @Override
    public int compare(Task o1, Task o2) {
        if (o1.equals(o2)) {
            return 0;
        }
        return o1.getStartTime().compareTo(o2.getStartTime());
    }
}
