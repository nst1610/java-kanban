package ru.yandex.javacourse.schedule.manager;

import java.nio.file.Path;
import ru.yandex.javacourse.schedule.manager.impl.FileBackedTaskManager;
import ru.yandex.javacourse.schedule.manager.impl.InMemoryHistoryManager;
import ru.yandex.javacourse.schedule.manager.impl.InMemoryTaskManager;

/**
 * Default managers.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public class Managers {
	public static TaskManager getDefault() {
		return new InMemoryTaskManager();
	}

	public static HistoryManager getDefaultHistory() {
		return new InMemoryHistoryManager();
	}

	public static FileBackedTaskManager getFileMBackedTaskManager(Path filePath) {
		return new FileBackedTaskManager(filePath);
	}
}
