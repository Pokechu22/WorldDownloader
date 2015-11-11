package wdl;


public class WDLSaveAsync implements Runnable {
	@Override
	public void run() {
		try {
			WDL.saveEverything();
			WDL.saving = false;
			WDL.onSaveComplete();
		} catch (Throwable e) {
			WDL.crashed(e, "World Downloader Mod: Saving world");
		}
	}
}
