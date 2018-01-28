package wdl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.init.Bootstrap;

public class WDLTestMain {

	private static final Logger LOGGER = LogManager.getLogger();

	public static void main(String[] args) {
		if (Bootstrap.isRegistered()) {
			LOGGER.warn("Bootstrap already initialized.");
			return;
		}
		LOGGER.debug("Initializing bootstrap...");
		Bootstrap.register();
		LOGGER.debug("Initialized bootstrap.");
		// Note: not checking Bootstrap.hasErrored as that didn't exist in older
		// versions
	}

}
