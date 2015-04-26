package wdl;

import java.util.List;

import net.minecraft.crash.CrashReport;
import net.minecraft.world.storage.ThreadedFileIOBase;

public class WDLSaveProgressReporter implements Runnable {
	public static volatile int totalChunks = 0;
	public static volatile int currentChunk = 0;
	
	/**
	 * All of the items that are queued in {@link ThreadedFileIOBase}.
	 * 
	 * This is a reference to {@link ThreadedFileIOBase#threadedIOQueue}.
	 */
	private List threadedFileIOBaseQueue;
	
	private int initialQueueSize;
	
	@Override
	public void run() {
		try {
			threadedFileIOBaseQueue = (List)WDL.stealAndGetField(
					ThreadedFileIOBase.func_178779_a(), List.class);
			
			initialQueueSize = threadedFileIOBaseQueue.size();
			
			while (WDL.saving) {
				WDL.chatMsg("Saving... " + makeProgressBar(currentChunk + 
						threadedFileIOBaseQueue.size(),
						totalChunks + initialQueueSize));
	
				Thread.sleep(1000L);
			}
		} catch (Throwable t) {
			WDL.minecraft.crashed(CrashReport.makeCrashReport(t,
					"Displaying save progress bar"));
		}
	}
	
	private String makeProgressBar(int current, int max) {
		if (max == 0) { return "0 / 0"; }
		
		//GTL: Greatest to least.
		final String[] blocksGTL = new String[] {
				"", //Don't want a full block in that case.
				"\u2589",
				"\u258A",
				"\u258B",
				"\u258C",
				"\u258D",
				"\u258E",
				"\u258F",
				""
		};
		final String[] blocksLTG = new String[] {
				"",
				"\u258F",
				"\u258E",
				"\u258D",
				"\u258C",
				"\u258B",
				"\u258A",
				"\u2589",
				"\u2588"
		};
		
		StringBuilder builder = new StringBuilder();
		
		int percent = (current * 100) / max;
		int progress = (current * 128) / max;
		
		builder.append(percent).append("% ");
		
		int full = progress / 8;
		int partial = progress % 8;
		
		for (int i = 0; i < full; i++) {
			builder.append("\u2588"); //Full block
		}
		builder.append(blocksLTG[partial]);
		//Switch color to black for the other side of the bar.
		builder.append("§0");
		builder.append(blocksGTL[partial]);
		for (int i = 0; i < (16 - full - (partial > 0 ? 1 : 0)); i++) {
			builder.append("\u2588");
		}
		
		//Switch back to orange.
		builder.append("§6");
		
		builder.append(" (").append(current).append(" of ")
				.append(max).append(")");
		
		return builder.toString();
	}

	public void start() {
		Thread var1 = new Thread(this);
		var1.start();
	}
}
