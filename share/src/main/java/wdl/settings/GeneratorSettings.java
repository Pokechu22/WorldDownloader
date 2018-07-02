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
package wdl.settings;

import static wdl.settings.Utils.*;

import java.util.Map;

import net.minecraft.util.IStringSerializable;

/**
 * Contains various settings and enums for the world generator.
 */
public final class GeneratorSettings {
	private GeneratorSettings() { throw new AssertionError(); }

	public static final StringSetting SEED =
			new StringSetting("RandomSeed", "");
	public static final BooleanSetting GENERATE_STRUCTURES =
			new BooleanSetting("MapFeatures", true, "wdl.gui.generator.generateStructures");
	// Used to control what generator is exposed in the UI.
	public static final EnumSetting<Generator> GENERATOR =
			new EnumSetting<>("MapGenerator", Generator.VOID, "wdl.gui.generator.generator", Generator.values(), Generator::fromString);
	// Actual generator properties
	public static final StringSetting GENERATOR_NAME =
			new StringSetting("GeneratorName", "flat");
	public static final IntSetting GENERATOR_VERSION =
			new IntSetting("GeneratorVersion", 0);
	public static final StringSetting GENERATOR_OPTIONS =
			new StringSetting("GeneratorOptions", ";0");

	public enum Generator implements IStringSerializable {
		VOID("void"),
		DEFAULT("default"),
		FLAT("flat"),
		LARGE_BIOMES("largeBiomes"),
		AMPLIFIED("amplified"),
		CUSTOMIZED("custom"),
		LEGACY("legacy"); // XXX do we really need this?

		private final String confName;
		private static final Map<String, Generator> FROM_STRING = makeFromString(values(), t -> t.confName);

		private Generator(String confName) {
			this.confName = confName;
		}

		public static Generator fromString(String confName) {
			return FROM_STRING.get(confName);
		}

		@Override
		public String getName() {
			return confName;
		}
	}
}
