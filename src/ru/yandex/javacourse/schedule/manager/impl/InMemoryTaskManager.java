package ru.yandex.javacourse.schedule.manager.impl;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.IN_PROGRESS;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import ru.yandex.javacourse.schedule.exceptions.IntersectionException;
import ru.yandex.javacourse.schedule.manager.HistoryManager;
import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskComparator;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

public class InMemoryTaskManager implements TaskManager {

	protected final Map<Integer, Task> tasks = new HashMap<>();
	protected final Map<Integer, Epic> epics = new HashMap<>();
	protected final Map<Integer, Subtask> subtasks = new HashMap<>();
	protected int generatorId = 0;
	protected final HistoryManager historyManager = Managers.getDefaultHistory();
	protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(new TaskComparator());

	@Override
	public List<Task> getTasks() {
		return new ArrayList<>(tasks.values());
	}

	@Override
	public List<Subtask> getSubtasks() {
		return new ArrayList<>(subtasks.values());
	}

	@Override
	public List<Epic> getEpics() {
		return new ArrayList<>(epics.values());
	}

	@Override
	public List<Subtask> getEpicSubtasks(int epicId) {
		List<Subtask> tasks = new ArrayList<>();
		Epic epic = epics.get(epicId);
		if (epic == null) {
			return null;
		}
		for (int id : epic.getSubtaskIds()) {
			tasks.add(subtasks.get(id));
		}
		return tasks;
	}

	@Override
	public Task getTask(int id) {
		final Task task = tasks.get(id);
		if (task == null) {
			return null;
		}
		historyManager.addTask(task);
		return task;
	}

	@Override
	public Subtask getSubtask(int id) {
		final Subtask subtask = subtasks.get(id);
		if (subtask == null) {
			return null;
		}
		historyManager.addTask(subtask);
		return subtask;
	}

	@Override
	public Epic getEpic(int id) {
		final Epic epic = epics.get(id);
		if (epic == null) {
			return null;
		}
		historyManager.addTask(epic);
		return epic;
	}

	@Override
	public int addNewTask(Task task) {
		if (task.getId() != null && tasks.containsKey(task.getId())) {
			throw new IllegalArgumentException("Task id " + task.getId() + " already exists.");
		} else {
			setCorrectId(task);
		}
		if (!checkIntersections(task)) {
			throw new IntersectionException("Task intersect with an existing one.");
		}
		tasks.put(task.getId(), task);
		addToPrioritizedSet(task);
		return task.getId();
	}

	@Override
	public int addNewEpic(Epic epic) {
		if (epic.getId() != null && epics.containsKey(epic.getId())) {
			throw new IllegalArgumentException("Epic id " + epic.getId() + " already exists.");
		} else {
			setCorrectId(epic);
		}
		epics.put(epic.getId(), epic);
		return epic.getId();
	}

	@Override
	public Integer addNewSubtask(Subtask subtask) {
		final int epicId = subtask.getEpicId();
		Epic epic = epics.get(epicId);
		if (epic == null) {
			return null;
		}
		if (subtask.getId() != null && subtasks.containsKey(subtask.getId())) {
			throw new IllegalArgumentException("Subtask id " + epic.getId() + " already exists.");
		} else {
			setCorrectId(subtask);
		}
		if (!checkIntersections(subtask)) {
			throw new IntersectionException("Task intersect with an existing one.");
		}
		subtasks.put(subtask.getId(), subtask);
		epic.addSubtaskId(subtask.getId());
		updateEpicStatus(epicId);
		updateEpicDuration(epicId);
		addToPrioritizedSet(subtask);
		return subtask.getId();
	}

	@Override
	public void updateTask(Task task) {
		if (task == null) {
			return;
		}
		if (tasks.containsKey(task.getId())) {
			if (!checkIntersections(task)) {
				throw new IntersectionException("Task intersect with an existing one.");
			}
			tasks.put(task.getId(), task);
			addToPrioritizedSet(task);
		}
	}

	@Override
	public void updateEpic(Epic epic) {
		if (epic == null) {
			return;
		}
		if (epics.containsKey(epic.getId())) {
			epics.get(epic.getId()).setName(epic.getName());
			epics.get(epic.getId()).setDescription(epic.getDescription());
		}
	}

	@Override
	public void updateSubtask(Subtask subtask) {
		if (subtask == null) {
			return;
		}
		if (subtasks.containsKey(subtask.getId())) {
			final int epicId = subtask.getEpicId();
			if (!epics.containsKey(epicId)) {
				return;
			}
			if (!checkIntersections(subtask)) {
				throw new IntersectionException("Task intersect with an existing one.");
			}
			subtasks.put(subtask.getId(), subtask);
			updateEpicStatus(epicId);
			updateEpicDuration(epicId);
			addToPrioritizedSet(subtask);
		}
	}

	@Override
	public void deleteTask(int id) {
		Task task = tasks.get(id);
		tasks.remove(id);
		historyManager.remove(id);
		prioritizedTasks.remove(task);
	}

	@Override
	public void deleteEpic(int id) {
		final Epic epic = epics.remove(id);
		if (epic != null) {
			for (Integer subtaskId : epic.getSubtaskIds()) {
				Subtask subtask = subtasks.get(subtaskId);
				subtasks.remove(subtaskId);
				historyManager.remove(subtaskId);
				prioritizedTasks.remove(subtask);
			}
			historyManager.remove(id);
		}
	}

	@Override
	public void deleteSubtask(int id) {
		Subtask subtask = subtasks.remove(id);
		if (subtask != null) {
			historyManager.remove(id);
			Epic epic = epics.get(subtask.getEpicId());
			epic.removeSubtask(id);
			updateEpicStatus(epic.getId());
			updateEpicDuration(epic.getId());
			prioritizedTasks.remove(subtask);
		}
	}

	@Override
	public void deleteTasks() {
		for (Task task : tasks.values()) {
			historyManager.remove(task.getId());
			prioritizedTasks.remove(task);
		}
		tasks.clear();
	}

	@Override
	public void deleteSubtasks() {
		for (Epic epic : epics.values()) {
			epic.cleanSubtaskIds();
			updateEpicStatus(epic.getId());
			updateEpicDuration(epic.getId());
		}
		for (Subtask subtask : subtasks.values()) {
			historyManager.remove(subtask.getId());
			prioritizedTasks.remove(subtask);
		}
		subtasks.clear();
	}

	@Override
	public void deleteEpics() {
		for (Epic epic : epics.values()) {
			historyManager.remove(epic.getId());
		}
		epics.clear();
		for (Subtask subtask : subtasks.values()) {
			historyManager.remove(subtask.getId());
			prioritizedTasks.remove(subtask);
		}
		subtasks.clear();
	}

	@Override
	public List<Task> getHistory() {
		return historyManager.getHistory();
	}

	@Override
	public List<Task> getPrioritizedTasks() {
		return new ArrayList<>(prioritizedTasks);
	}

	private void updateEpicStatus(int epicId) {
		Epic epic = epics.get(epicId);
		List<Integer> subs = epic.getSubtaskIds();
		if (subs.isEmpty()) {
			epic.setStatus(NEW);
			return;
		}
		TaskStatus status = null;
		for (int id : subs) {
			final Subtask subtask = subtasks.get(id);
			if (status == null) {
				status = subtask.getStatus();
				continue;
			}

			if (status == subtask.getStatus()
					&& status != IN_PROGRESS) {
				continue;
			}
			epic.setStatus(IN_PROGRESS);
			return;
		}
		epic.setStatus(status);
	}

	private void updateEpicDuration(int epicId) {
		Epic epic = epics.get(epicId);
		List<Integer> subs = epic.getSubtaskIds();
		if (subs.isEmpty()) {
			epic.setDuration(Duration.ZERO);
			epic.setStartTime(null);
			epic.setEndTime(null);
			return;
		}
		Duration duration = subs.stream()
			.map(subtasks::get)
			.map(Subtask::getDuration)
			.filter(Objects::nonNull)
			.reduce(Duration.ZERO, Duration::plus);
		LocalDateTime startTime = subs.stream()
			.map(subtasks::get)
			.map(Subtask::getStartTime)
			.filter(Objects::nonNull)
			.min(LocalDateTime::compareTo)
			.orElse(null);
		LocalDateTime endTime = subs.stream()
			.map(subtasks::get)
			.map(Subtask::getEndTime)
			.filter(Objects::nonNull)
			.max(LocalDateTime::compareTo)
			.orElse(null);
		epic.setDuration(duration);
		epic.setStartTime(startTime);
		epic.setEndTime(endTime);
	}

	private void addToPrioritizedSet(Task task) {
		if (task.getStartTime() != null) {
			prioritizedTasks.remove(task);
			prioritizedTasks.add(task);
		}
	}

	private void setCorrectId(Task task) {
		if (task.getId() == null) {
			task.setId(generateId());
		} else if (task.getId() <= generatorId) {
			throw new IllegalArgumentException("Id cannot be less or equals than the generator value. The generator value is " +
				generatorId);
		} else if (task.getId() > generatorId) {
			generatorId = task.getId();
		}
	}

	private int generateId() {
		generatorId++;
		return generatorId;
	}

	private boolean checkIntersections(Task task) {
		if (Objects.isNull(task.getStartTime())) {
			return true;
		}
		return getPrioritizedTasks().stream().allMatch(existing -> task.getStartTime().isAfter(existing.getEndTime())
			|| task.getEndTime().isBefore(existing.getStartTime()));
	}
}
