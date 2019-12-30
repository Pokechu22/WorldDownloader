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

import java.util.function.Function;

/**
 * A wrapper from a setting to the property represented by it.
 * This implementation assumes a constant default.
 *
 * @param <T> The type of value.
 */
public class BaseSetting<T> implements Setting<T> {
	/**
	 * The default value.
	 */
	private final T defaultValue;

	// Configuration stuff
	/**
	 * The name of the setting as used in a configuration.
	 */
	private final String name;
	/**
	 * A function that converts a String into a T.
	 */
	private final Function<String, T> fromString;
	/**
	 * A function that converts a T back into a String.
	 */
	private final Function<T, String> toString;

	/**
	 * Constructor.
	 *
	 * @param name The name as used in the configuration.
	 * @param def The default value.
	 * @param fromString A function that converts a String into a T.
	 * @param toString A function that converts a T back into a String.
	 */
	public BaseSetting(String name, T def, Function<String, T> fromString,
			Function<T, String> toString) {
		this.name = name;
		this.defaultValue = def;
		this.fromString = fromString;
		this.toString = toString;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + this.name + " (defaults to " + defaultValue + ")";
	}

	@Override
	public final T deserializeFromString(String text) {
		return fromString.apply(text);
	}

	@Override
	public final String serializeToString(T value) {
		return toString.apply(value);
	}

	@Override
	public final String getConfigurationKey() {
		return this.name;
	}

	@Override
	public final T getDefault(IConfiguration context) {
		return defaultValue;
	}
}
