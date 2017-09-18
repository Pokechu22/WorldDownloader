package wdl;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import net.minecraft.world.storage.IThreadedFileIO;
import net.minecraft.world.storage.ThreadedFileIOBase;

public class IOUtilsTest {
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Makes sure that setup succeeded on this MC version.
	 *
	 * Runtime results may be different, but that doesn't matter and things will gracefully fall back.
	 */
	@Test
	public void testSetup() {
		assertTrue(IOUtils.SETUP_SUCCEEDED);
	}

	private static class IOTask implements IThreadedFileIO {
		// Actually used by the task
		volatile boolean started = false;
		volatile boolean ready = false;
		volatile boolean timedOut = false;
		// Ensure the task doesn't get stuck
		private long start = System.currentTimeMillis();
		@Override
		public boolean writeNextIO() {
			started = true;

			while (true) {
				if (ready) {
					// Ensure that things still take longer than the sleep between tries
					try {
						Thread.sleep(20L);
					} catch (InterruptedException ex) {
						LOGGER.warn("Unexpected InterruptedException", ex);
						throw new RuntimeException(ex);
					}
					return false;
				}
				if (System.currentTimeMillis() - start > 1000) {
					// Ensure the task doesn't get stuck
					timedOut = true;
					return false;
				}
				Thread.yield();
			}
		}
	}

	@Test
	public void testWaitForFinish() throws InterruptedException {
		class State {
			volatile boolean received1 = false;
			volatile boolean received0 = false;
		}

		IOTask task = new IOTask();

		ThreadedFileIOBase.getThreadedIOInstance().queueIO(task);

		// Wait for the queue processor to have started this task
		while (!task.started) {
			Thread.yield();
		}

		State state = new State();

		IOUtils.waitForFinishAndUpdateProgress((i) -> {
			LOGGER.debug("{} tasks remain", i);
			if (i == 0) {
				if (state.received0) {
					fail("Unexpected duplicate 0 task");
				}
				state.received0 = true;
				if (!state.received1) {
					fail("Received no tasks without having received a task - initial number not reported!");
				}
			} else if (i == 1) {
				if (state.received1) {
					fail("Unexpected duplicate 1 task");
				}
				state.received1 = true;
				if (state.received0) {
					fail("Received 1 task after 0 tasks - incorrect number reported");
				}
				task.ready = true;
			} else {
				fail("Unexpected count " + i);
			}
		});

		if (task.timedOut) {
			fail("Task timed out");
		}
	}

	@Test
	public void testAddTask() throws InterruptedException {
		class State {
			volatile boolean received2 = false;
			volatile boolean queuedTask2 = false;
		}

		IOTask task1 = new IOTask();
		IOTask task2 = new IOTask();

		ThreadedFileIOBase.getThreadedIOInstance().queueIO(task1);

		// Wait for the queue processor to have started this task
		while (!task1.started) {
			Thread.yield();
		}

		State state = new State();

		IOUtils.waitForFinishAndUpdateProgress((i) -> {
			LOGGER.debug("{} tasks remain", i);
			if (i == 1) {
				if (!state.queuedTask2) {
					state.queuedTask2 = true;
					ThreadedFileIOBase.getThreadedIOInstance().queueIO(task2);
				} else if (state.received2) {
					task2.ready = true;
				} else {
					fail("Unexcepted 1 task");
				}
			} else if (i == 2) {
				if (state.received2) {
					fail("Unexcepted duplicate 2 tasks");
				}
				state.received2 = true;
				task1.ready = true;
			} else if (i != 0) {
				fail("Unexpected count " + i);
			}
		});

		if (task1.timedOut) {
			fail("Task1 timed out");
		}
		if (task2.timedOut) {
			fail("Task2 timed out");
		}
	}
}
