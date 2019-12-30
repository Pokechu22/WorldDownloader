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

public class BooleanSettingTest {

	@Test
	public void testCycle() {
		BooleanSetting setting = new BooleanSetting("test", false, "test");
		assertThat(setting.cycle(true), is(false));
		assertThat(setting.cycle(false), is(true));
	}

	@Test
	public void testConfig() {
		BooleanSetting setting = new BooleanSetting("test", false, "test");
		Configuration config = new Configuration(new DefaultConfiguration());
		assertThat(config.getValue(setting), is(false));  // default value
		config.cycle(setting);
		assertThat(config.getValue(setting), is(true));
		config.cycle(setting);
		assertThat(config.getValue(setting), is(false));
		config.setValue(setting, true);
		assertThat(config.getValue(setting), is(true));
	}

	@Test
	public void testParse() {
		BooleanSetting setting = new BooleanSetting("test", false, "test");
		assertThat(setting.deserializeFromString("true"), is(true));
		assertThat(setting.deserializeFromString("false"), is(false));
		assertThat(setting.serializeToString(true), is("true"));
		assertThat(setting.serializeToString(false), is("false"));
	}
}
