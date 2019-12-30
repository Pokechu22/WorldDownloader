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
package wdl.config;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class IntSettingTest {

	@Test
	public void testConfig() {
		IntSetting setting = new IntSetting("test", 42);
		Configuration config = new Configuration(new DefaultConfiguration());
		assertThat(config.getValue(setting), is(42));  // default value
		config.setValue(setting, 5);
		assertThat(config.getValue(setting), is(5));
	}

	@Test
	public void testParse() {
		IntSetting setting = new IntSetting("test", 42);
		assertThat(setting.deserializeFromString("4"), is(4));
		assertThat(setting.deserializeFromString("0"), is(0));
		assertThat(setting.deserializeFromString("-42"), is(-42));
		assertThat(setting.serializeToString(4), is("4"));
		assertThat(setting.serializeToString(0), is("0"));
		assertThat(setting.serializeToString(-42), is("-42"));
	}
}
