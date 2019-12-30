/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
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
	public void testMixinInventoryBasic() throws Exception {
		assumeTrue(SanityCheck.MIXIN_INVENTORYBASIC.canRun());
		SanityCheck.MIXIN_INVENTORYBASIC.run();
	}

	@Test
	public void testMixinGuiIngameMenu() throws Exception {
		assumeTrue(SanityCheck.MIXIN_GUIINGAMEMENU.canRun());
		SanityCheck.MIXIN_GUIINGAMEMENU.run();
	}

	@Test
	public void testMixinWorldClient() throws Exception {
		assumeTrue(SanityCheck.MIXIN_WORLDCLIENT.canRun());
		SanityCheck.MIXIN_WORLDCLIENT.run();
	}

	@Test
	public void testMixinNHPC() throws Exception {
		assumeTrue(SanityCheck.MIXIN_NHPC.canRun());
		SanityCheck.MIXIN_NHPC.run();
	}

	@Test
	public void testMixinCrashReport() throws Exception {
		assumeTrue(SanityCheck.MIXIN_CRASHREPORT.canRun());
		SanityCheck.MIXIN_CRASHREPORT.run();
	}

	@Test
	public void testEncoding() throws Exception {
		assumeTrue(SanityCheck.ENCODING.canRun());
		SanityCheck.ENCODING.run();
	}
}
