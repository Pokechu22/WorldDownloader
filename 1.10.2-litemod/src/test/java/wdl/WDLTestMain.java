/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
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
		// Note: not checking Bootstrap.hasErrored as that didn't exist in this version
	}

}
