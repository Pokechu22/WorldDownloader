package wdl;

import java.lang.reflect.Field;
import java.util.function.LongConsumer;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;

import net.minecraft.world.storage.ThreadedFileIOBase;

/**
 * Utilities relating to {@link ThreadedFileIOBase}.
 */
public class IOUtils {
	private static final Logger LOGGER = LogManager.getLogger();

	private static final ThreadedFileIOBase INSTANCE = ThreadedFileIOBase.getThreadedIOInstance();
	/**
	 * True if accessing the private fields of {@link ThreadedFileIOBase} succeeded.
	 * If false, the {@link Field}s below will be null.
	 */
	@VisibleForTesting
	static final boolean SETUP_SUCCEEDED;
	/** {@link ThreadedFileIOBase#writeQueuedCounter} */
	@Nullable private static final Field IO_QUEUED_COUNTER;
	/** {@link ThreadedFileIOBase#savedIOCounter} */
	@Nullable private static final Field IO_SAVED_COUNTER;
	/** {@link ThreadedFileIOBase#isThreadWaiting} */
	@Nullable private static final Field IO_THREAD_WAITING;

	static {
		boolean successful;
		Field writeQueuedCounter = null;
		Field savedIOCounter = null;
		Field isThreadWaiting = null;
		try {
			for (Field field : ThreadedFileIOBase.class.getDeclaredFields()) {
				if (field.getType().equals(long.class)) {
					if (writeQueuedCounter == null) {
						writeQueuedCounter = field;
					} else if (savedIOCounter == null) {
						savedIOCounter = field;
					} else {
						throw new RuntimeException("Too many long fields: already have "
								+ writeQueuedCounter + " and " + savedIOCounter
								+ "; don't know what to do about " + field);
					}
				} else if (field.getType().equals(boolean.class)) {
					if (isThreadWaiting == null) {
						isThreadWaiting = field;
					} else {
						throw new RuntimeException("Too many boolean fields: already have "
								+ isThreadWaiting + "; don't know what to do about " + field);
					}
				}
			}

			LOGGER.debug("IOUtils: writeQueuedCounter={}, savedIOCounter={}, isThreadWaiting={}", writeQueuedCounter, savedIOCounter, isThreadWaiting);

			if (writeQueuedCounter == null) throw new RuntimeException("Failed to get writeQueuedCounter");
			if (savedIOCounter == null) throw new RuntimeException("Failed to get savedIOCounter");
			if (isThreadWaiting == null) throw new RuntimeException("Failed to get isThreadWaiting");

			writeQueuedCounter.setAccessible(true);
			savedIOCounter.setAccessible(true);
			isThreadWaiting.setAccessible(true);

			successful = true;
		} catch (Exception ex) {
			LOGGER.warn("Failed to set up IOUtils; using fallback implementation", ex);
			successful = false;
		}
		SETUP_SUCCEEDED = successful;

		if (successful) {
			IO_QUEUED_COUNTER = writeQueuedCounter;
			IO_SAVED_COUNTER = savedIOCounter;
			IO_THREAD_WAITING = isThreadWaiting;
		} else {
			IO_QUEUED_COUNTER = null;
			IO_SAVED_COUNTER = null;
			IO_THREAD_WAITING = null;
		}
	}

	/**
	 * Behaves the same as {@link ThreadedFileIOBase#waitForFinish()} except that it
	 * also updates the given listener.
	 */
	public static void waitForFinishAndUpdateProgress(LongConsumer remainingTasksListener) throws InterruptedException {
		if (!SETUP_SUCCEEDED) {
			// Can't accurately report information
			INSTANCE.waitForFinish();
		} else {
			try {
				IO_THREAD_WAITING.setBoolean(INSTANCE, true);

				long writeQueuedCounter = IO_QUEUED_COUNTER.getLong(INSTANCE);
				long savedIOCounter = IO_SAVED_COUNTER.getLong(INSTANCE);

				// This call to Math.abs ensures that no negative numbers are returned due to
				// reversal of field order. Iteration order over class fields is undefined -
				// so we can't be 100% sure that either field is the one in question.  However,
				// for actually comparing the values this does not matter.
				long difference = Math.abs(writeQueuedCounter - savedIOCounter);
				remainingTasksListener.accept(difference);

				while (writeQueuedCounter != savedIOCounter) {
					Thread.sleep(10L);

					writeQueuedCounter = IO_QUEUED_COUNTER.getLong(INSTANCE);
					savedIOCounter = IO_SAVED_COUNTER.getLong(INSTANCE);

					long newDiff = Math.abs(writeQueuedCounter - savedIOCounter);
					if (newDiff != difference) {
						difference = newDiff;
						remainingTasksListener.accept(difference);
					}
				}

				IO_THREAD_WAITING.setBoolean(INSTANCE, false);
			} catch (Exception ex) {
				LOGGER.warn("Failed to manually process IO; reverting to fallback", ex);

				INSTANCE.waitForFinish();
			}
		}
	}
}
