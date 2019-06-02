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
package wdl.config.settings;

import org.junit.Test;
import wdl.MaybeMixinTest;


import org.junit.Test;

import wdl.MaybeMixinTest;

public class MessageSettingsTest extends MaybeMixinTest {

	@Test
	public void testEnableAll() {
		checkAllText(MessageSettings.ENABLE_ALL_MESSAGES);
	}

	// MessageCategorySetting and MessageTypeSetting are handled by WDLMessageTypesTest

}
