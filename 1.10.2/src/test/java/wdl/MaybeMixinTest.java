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
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.minecraft.init.Bootstrap;

/**
 * This is a more or less empty class that is used to specify the runner that
 * JUnit should use, for tests that rely upon mixins or base changes.
 *
 * The only purpose is to make use of the {@link RunWith @RunWith} annotation,
 * which is inherited into subclasses.
 */
@RunWith(JUnit4.class)
public abstract class MaybeMixinTest {
	private static final Logger LOGGER = LogManager.getLogger();
	private static boolean ran = false;

	@BeforeClass
	public static void init() {
		if (ran) {
			return;
		}
		if (Bootstrap.isRegistered()) {
			LOGGER.warn("Bootstrap already initialized.");
			return;
		}
		ran = true;
		LOGGER.debug("Initializing bootstrap...");
		Bootstrap.register();
		LOGGER.debug("Initialized bootstrap.");
		// Note: not checking Bootstrap.hasErrored as that didn't exist in this version
	}
}
