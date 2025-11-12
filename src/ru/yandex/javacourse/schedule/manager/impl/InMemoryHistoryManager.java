package ru.yandex.javacourse.schedule.manager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.yandex.javacourse.schedule.manager.HistoryManager;
import ru.yandex.javacourse.schedule.tasks.Task;

/**
 * In memory history manager.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public class InMemoryHistoryManager implements HistoryManager {
	private Node first;

	private Node last;

	private final Map<Integer, Node> history = new HashMap<>();

	@Override
	public List<Task> getHistory() {
		List<Task> historyList = new ArrayList<>();
		Node current = first;
		while (current != null) {
			historyList.add(current.item);
			current = current.next;
		}
		return historyList;
	}

	@Override
	public void addTask(Task task) {
		if (task == null) {
			return;
		}
		Task taskForHistory = new Task(task);
		if (history.containsKey(taskForHistory.getId())) {
			remove(taskForHistory.getId());
		}
		Node node = linkLast(taskForHistory);
		history.put(taskForHistory.getId(), node);
	}

	@Override
	public void remove(int id) {
		Node node = history.remove(id);
		if (node != null) {
			removeNode(node);
		}
	}

	private Node linkLast(Task task) {
		Node newNode = new Node(last, task, null);
		if (last == null) {
			first = newNode;
        } else {
			last.next = newNode;
        }
        last = newNode;
		return newNode;
    }

	private void removeNode(Node node) {
		if (node.prev == null) {
			first = node.next;
		} else {
			node.prev.next = node.next;
		}
		if (node.next == null) {
			last = node.prev;
		} else {
			node.next.prev = node.prev;
		}
	}

	private static class Node {
		Task item;
		Node next;
		Node prev;

		Node(Node prev, Task item, Node next) {
			this.item = item;
			this.next = next;
			 this.prev = prev;
		}
	}
}
