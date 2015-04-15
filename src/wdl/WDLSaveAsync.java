package wdl;

public class WDLSaveAsync implements Runnable {
	@Override
	public void run() {
		WDL.saveEverything();
		WDL.saving = false;
		WDL.onSaveComplete();
	}
}
