package ru.yandex.javacourse.schedule;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;

public class Main {
	public static void main(String[] args) {

		TaskManager manager = Managers.getDefault();

		// Создание
		Task task1 = new Task("Task1", "Task1 description", NEW);
		Task task2 = new Task("Task2", "Task2 description", NEW);
		final int task1Id = manager.addNewTask(task1);
		final int task2Id = manager.addNewTask(task2);

		Epic epic1 = new Epic("Epic1", "Epic1 description");
		final int epic1Id = manager.addNewEpic(epic1);
		Subtask subtask1 = new Subtask("Subtask1-1", "Subtask1 description", NEW, epic1Id);
		Subtask subtask2 = new Subtask("Subtask1-2", "Subtask1 description", NEW, epic1Id);
		Subtask subtask3 = new Subtask("Subtask1-3", "Subtask1 description", NEW, epic1Id);
		final int subtask1Id = manager.addNewSubtask(subtask1);
		final int subtask2Id = manager.addNewSubtask(subtask2);
		final int subtask3Id = manager.addNewSubtask(subtask3);

		Epic epic2 = new Epic("Epic2", "Epic2 description");
		final int epic2Id = manager.addNewEpic(epic2);

		// Просмотр истории
		manager.getTask(task1Id);
		manager.getEpic(epic1Id);
		manager.getSubtask(subtask2Id);
		printHistory(manager);

		manager.getTask(task1Id);
		manager.getTask(task2Id);
		manager.getSubtask(subtask1Id);
		manager.getSubtask(subtask2Id);
		printHistory(manager);

		// Удаление задачи
		manager.deleteTask(task1Id);
		printHistory(manager);

		// Удаление эпика
		manager.deleteEpic(epic1Id);
		printHistory(manager);

		printAllTasks(manager);
	}

	private static void printAllTasks(TaskManager manager) {
		System.out.println("Задачи:");
		for (Task task : manager.getTasks()) {
			System.out.println(task);
		}
		System.out.println("Эпики:");
		for (Task epic : manager.getEpics()) {
			System.out.println(epic);
//			System.out.println("--> Подзадачи эпика:");
			for (Task task : manager.getEpicSubtasks(epic.getId())) {
				System.out.println("--> " + task);
			}
		}
		System.out.println("Подзадачи:");
		for (Task subtask : manager.getSubtasks()) {
			System.out.println(subtask);
		}

		System.out.println("История:");
		for (Task task : manager.getHistory()) {
			System.out.println(task);
		}
	}

	private static void printHistory(TaskManager manager) {
		System.out.println("История:");
		for (Task task : manager.getHistory()) {
			System.out.println(task);
		}
	}
}
