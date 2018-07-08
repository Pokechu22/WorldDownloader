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

import static wdl.config.settings.Utils.*;

import java.util.Map;
import java.util.function.Function;

import net.minecraft.util.IStringSerializable;
import wdl.config.BooleanSetting;
import wdl.config.EnumSetting;
import wdl.config.IConfiguration;
import wdl.config.Setting;
import wdl.config.StringSetting;

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
	public static final Setting<String> GENERATOR_NAME = new NameSetting("GeneratorName");
	public static final Setting<Integer> GENERATOR_VERSION = new VersionSetting("GeneratorVersion");
	public static final Setting<String> GENERATOR_OPTIONS = new OptionSetting("GeneratorOptions");

	public enum Generator implements IStringSerializable {
		VOID("void", "flat", 0, ";0"), // XXX ;0 for air format will break in 1.13
		DEFAULT("default", "default", 1, ""),
		FLAT("flat", "flat", 0, ""),
		LARGE_BIOMES("largeBiomes", "largeBiomes", 0, ""),
		AMPLIFIED("amplified", "amplified", 0, ""),
		CUSTOMIZED("custom", "custom", 0, ""),
		LEGACY("legacy", "default_1_1", 0, ""); // XXX do we really need this?

		private final String confName;
		private static final Map<String, Generator> FROM_STRING = makeFromString(values(), t -> t.confName);

		final String generatorName;
		final int generatorVersion;
		final String defaultOption;

		private Generator(String confName, String generatorName, int generatorVersion, String defaultOption) {
			this.confName = confName;
			this.generatorName = generatorName;
			this.generatorVersion = generatorVersion;
			this.defaultOption = defaultOption;
		}

		public static Generator fromString(String confName) {
			return FROM_STRING.get(confName);
		}

		@Override
		public String getName() {
			return confName;
		}
	}

	// Note: this is an abstract class instead of taking a parameter, such that
	// the class name for each subclass is identified in stack traces
	private static abstract class GeneratorSetting<T> implements Setting<T> {
		private final String name;
		private final Function<String, T> fromString;
		private final Function<T, String> toString;

		protected GeneratorSetting(String name, Function<String, T> fromString, Function<T, String> toString) {
			this.name = name;
			this.fromString = fromString;
			this.toString = toString;
		}

		@Override
		public T deserializeFromString(String text) {
			return fromString.apply(text);
		}

		@Override
		public String serializeToString(T value) {
			return toString.apply(value);
		}

		@Override
		public String getConfigurationKey() {
			return name;
		}

		@Override
		public T getDefault(IConfiguration context) {
			return getDefault(context.getValue(GENERATOR));
		}

		protected abstract T getDefault(Generator generator);
	}

	// These take a name value for clarity above
	private static class NameSetting extends GeneratorSetting<String> {
		public NameSetting(String name) {
			super(name, Function.identity(), Function.identity());
		}

		@Override
		protected String getDefault(Generator generator) {
			return generator.generatorName;
		}
	}

	private static class VersionSetting extends GeneratorSetting<Integer> {
		public VersionSetting(String name) {
			super(name, Integer::parseInt, Object::toString);
		}

		@Override
		protected Integer getDefault(Generator generator) {
			return generator.generatorVersion;
		}
	}

	private static class OptionSetting extends GeneratorSetting<String> {
		public OptionSetting(String name) {
			super(name, Function.identity(), Function.identity());
		}

		@Override
		protected String getDefault(Generator generator) {
			return generator.defaultOption;
		}
	}
}
