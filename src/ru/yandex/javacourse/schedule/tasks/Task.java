package ru.yandex.javacourse.schedule.tasks;

import java.util.Objects;

public class Task {
	protected Integer id;
	protected String name;
	protected TaskStatus status;
	protected String description;

	public Task(Integer id, String name, String description, TaskStatus status) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.status = status;
	}

	public Task(String name, String description, TaskStatus status) {
		this.name = name;
		this.description = description;
		this.status = status;
	}

	public Task(Task task) {
		this.id = task.getId();
		this.name = task.getName();
		this.status = task.getStatus();
		this.description = task.getDescription();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TaskType getType() {
		return TaskType.TASK;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Task task = (Task) o;
		return Objects.equals(id, task.id);
	}

	@Override
	public String toString() {
		return "Task{" +
				"id=" + id +
				", name='" + name + '\'' +
				", status='" + status + '\'' +
				", description='" + description + '\'' +
				'}';
	}

	public String toCsvString() {
		return String.format("%d,%s,%s,%s,%s,", id, TaskType.TASK, name,status, description);
	}
}
