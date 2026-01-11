package ru.yandex.javacourse.schedule.tasks;

public enum Fields {
    ID(0, "id"),
    TYPE(1, "type"),
    NAME(2, "name"),
    STATUS(3, "status"),
    DESCRIPTION(4, "description"),
    START_TIME(5, "startTime"),
    DURATION(6, "duration"),
    END_TIME(7, "endTime"),
    EPIC(8, "epic");

    private final Integer id;
    private final String name;

    Fields(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }
}
