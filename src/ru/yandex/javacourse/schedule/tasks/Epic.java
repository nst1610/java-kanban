package ru.yandex.javacourse.schedule.tasks;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
	protected ArrayList<Integer> subtaskIds = new ArrayList<>();
	private LocalDateTime endTime;

	public Epic(Integer id, String name, String description) {
		super(id, name, description, NEW);
	}

	public Epic(String name, String description) {
		super(name, description, NEW);
	}

	public Epic(Epic epic) {
		super(epic);
		this.subtaskIds = new ArrayList<>(epic.getSubtaskIds());
		this.endTime = epic.endTime;
	}

	public void addSubtaskId(int id) {
		if (subtaskIds.contains(id) || this.id == id)
			return;
		subtaskIds.add(id);
	}

	public List<Integer> getSubtaskIds() {
		return subtaskIds;
	}

	public void cleanSubtaskIds() {
		subtaskIds.clear();
	}

	public void removeSubtask(int id) {
		subtaskIds.remove(Integer.valueOf(id));
	}

	@Override
	public TaskType getType() {
		return TaskType.EPIC;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	@Override
	public String toString() {
		return "Epic{" +
				"id=" + id +
				", name='" + name + '\'' +
				", status=" + status +
				", description='" + description + '\'' +
				", subtaskIds=" + subtaskIds +
				'}';
	}

	@Override
	public String toCsvString() {
		return String.format("%d,%s,%s,%s,%s,%s,%s,%s,", id, TaskType.EPIC, name, status, description,
			startTime == null ? "" : startTime, duration == null ? 0 : duration.toMinutes(),
			getEndTime() == null ? "" : getEndTime());
	}
}
