package wdl;

import net.minecraft.crash.CrashReport;

public class WDLSaveAsync implements Runnable {
	@Override
	public void run() {
		try {
			WDL.saveEverything();
			WDL.saving = false;
			WDL.onSaveComplete();
		} catch (Throwable e) {
			WDL.minecraft.crashed(CrashReport.makeCrashReport(e,
					"Saving downloaded world"));
		}
	}
}
