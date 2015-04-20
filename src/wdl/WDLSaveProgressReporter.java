package wdl;

public class WDLSaveProgressReporter implements Runnable {
	public static volatile int totalChunks = 0;
	public static volatile int currentChunk = 0;
	
	@Override
	public void run() {
		while (WDL.saving) {
			WDL.chatMsg("Saving... " + makeProgressBar(currentChunk,
					totalChunks));

			try {
				Thread.sleep(100L);
			} catch (InterruptedException var2) {
				var2.printStackTrace();
			}
		}
	}
	
	private String makeProgressBar(int current, int max) {
		if (max == 0) { return "0 / 0"; }
		
		//GTL: Greatest to least.
		final String[] blocksGTL = new String[] {
				"\u2588",
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
		
		int percent = (current * 128) / max;
		
		builder.append(percent).append("% ");
		
		int full = percent / 8;
		int partial = percent % 8;
		
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
