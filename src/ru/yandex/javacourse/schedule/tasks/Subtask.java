package ru.yandex.javacourse.schedule.tasks;

import java.util.Objects;

public class Subtask extends Task {
	protected Integer epicId;

	public Subtask(Integer id, String name, String description, TaskStatus status, Integer epicId) {
		super(id, name, description, status);
		checkEpicId(epicId);
		this.epicId = epicId;
	}

	public Subtask(String name, String description, TaskStatus status, Integer epicId) {
		super(name, description, status);
		this.epicId = epicId;
	}

	public Subtask(Subtask subtask) {
		super(subtask);
		this.epicId = subtask.epicId;
	}

	public Integer getEpicId() {
		return epicId;
	}

	@Override
	public TaskType getType() {
		return TaskType.SUB_TASK;
	}

	@Override
	public String toString() {
		return "Subtask{" +
				"id=" + id +
				", epicId=" + epicId +
				", name='" + name + '\'' +
				", status=" + status +
				", description='" + description + '\'' +
				'}';
	}

	private void checkEpicId(Integer epicId) {
		if (Objects.equals(this.id, epicId)) {
			throw new IllegalArgumentException("Subtask cannot be attached to itself");
		}
	}

	@Override
	public String toCsvString() {
		return String.format("%d,%s,%s,%s,%s,%d", id, TaskType.SUB_TASK, name,status, description, epicId);
	}
}
