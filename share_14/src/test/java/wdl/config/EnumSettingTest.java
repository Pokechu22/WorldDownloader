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

import net.minecraft.util.IStringSerializable;

public class EnumSettingTest {

	public enum TestEnum implements IStringSerializable {
		FOO("foo"),
		BAR("bar"),
		BAZ("baz");

		private final String confName;

		private TestEnum(String confName) {
			this.confName = confName;
		}

		public static TestEnum fromString(String confName) {
			for (TestEnum t : values()) {
				if (t.confName.equals(confName)) {
					return t;
				}
			}
			return null;
		}

		@Override
		public String getName() {
			return confName;
		}
	}

	@Test
	public void testCycle() {
		EnumSetting<TestEnum> setting = new EnumSetting<>("test", TestEnum.FOO, "test", TestEnum.values(), TestEnum::fromString);
		assertThat(setting.cycle(TestEnum.FOO), is(TestEnum.BAR));
		assertThat(setting.cycle(TestEnum.BAR), is(TestEnum.BAZ));
		assertThat(setting.cycle(TestEnum.BAZ), is(TestEnum.FOO));
	}

	@Test
	public void testConfig() {
		EnumSetting<TestEnum> setting = new EnumSetting<>("test", TestEnum.FOO, "test", TestEnum.values(), TestEnum::fromString);
		Configuration config = new Configuration(new DefaultConfiguration());
		assertThat(config.getValue(setting), is(TestEnum.FOO));  // default value
		config.cycle(setting);
		assertThat(config.getValue(setting), is(TestEnum.BAR));
		config.cycle(setting);
		assertThat(config.getValue(setting), is(TestEnum.BAZ));
		config.cycle(setting);
		assertThat(config.getValue(setting), is(TestEnum.FOO));
		config.setValue(setting, TestEnum.BAZ);
		assertThat(config.getValue(setting), is(TestEnum.BAZ));
	}

	@Test
	public void testParse() {
		EnumSetting<TestEnum> setting = new EnumSetting<>("test", TestEnum.FOO, "test", TestEnum.values(), TestEnum::fromString);
		assertThat(setting.deserializeFromString("foo"), is(TestEnum.FOO));
		assertThat(setting.deserializeFromString("bar"), is(TestEnum.BAR));
		assertThat(setting.deserializeFromString("baz"), is(TestEnum.BAZ));
		assertThat(setting.serializeToString(TestEnum.FOO), is("foo"));
		assertThat(setting.serializeToString(TestEnum.BAR), is("bar"));
		assertThat(setting.serializeToString(TestEnum.BAZ), is("baz"));
	}
}
