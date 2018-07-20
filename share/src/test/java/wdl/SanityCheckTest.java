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

import static org.junit.Assume.*;

import org.junit.Test;

/**
 * Runs each {@link SanityCheck}.
 */
public class SanityCheckTest extends MaybeMixinTest {
	// XXX This probably should be parameterized, but MaybeMixinTest doesn't support that
	@Test
	public void testTripwire() throws Exception {
		assumeTrue(SanityCheck.TRIPWIRE.canRun());
		SanityCheck.TRIPWIRE.run();
	}

	@Test
	public void testVersion() throws Exception {
		assumeTrue(SanityCheck.VERSION.canRun());
		SanityCheck.VERSION.run();
	}

	@Test
	public void testTranslation() throws Exception {
		assumeTrue(SanityCheck.TRANSLATION.canRun());
		SanityCheck.TRANSLATION.run();
	}

	@Test
	public void testMixin() throws Exception {
		assumeTrue(SanityCheck.MIXIN.canRun());
		SanityCheck.MIXIN.run();
	}
}
