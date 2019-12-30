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
package wdl.config.settings;

import static wdl.config.settings.SettingTestUtils.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import wdl.MaybeMixinTest;
import wdl.config.Configuration;
import wdl.config.DefaultConfiguration;
import wdl.config.IConfiguration;

public class GeneratorSettingsTest extends MaybeMixinTest {

	@Test
	public void testGenerateStructures() {
		checkAllText(GeneratorSettings.GENERATE_STRUCTURES);
	}

	@Test
	public void testGenerator() {
		checkAllText(GeneratorSettings.GENERATOR);
		checkParsability(GeneratorSettings.GENERATOR);
	}

	@Test
	public void testProperties() {
		IConfiguration config = new Configuration(new DefaultConfiguration());
		forEachValue(GeneratorSettings.GENERATOR, (generator) -> {
			config.setValue(GeneratorSettings.GENERATOR, generator);
			assertThat(config.getValue(GeneratorSettings.GENERATOR_NAME), is(generator.generatorName));
			assertThat(config.getValue(GeneratorSettings.GENERATOR_VERSION), is(generator.generatorVersion));
			assertThat(config.getValue(GeneratorSettings.GENERATOR_OPTIONS), is(generator.defaultOption));

			assertReserializesCorrectly(GeneratorSettings.GENERATOR_NAME, generator.generatorName);
			assertReserializesCorrectly(GeneratorSettings.GENERATOR_VERSION, generator.generatorVersion);
			assertReserializesCorrectly(GeneratorSettings.GENERATOR_OPTIONS, generator.defaultOption);
		});
	}
}
