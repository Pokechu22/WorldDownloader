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

import java.util.function.Function;

/**
 * A wrapper from a setting to the property represented by it.
 *
 * @param <T> The type of value.
 */
public abstract class Setting<T> {
	/**
	 * The default value.
	 */
	public final T def;

	// Configuration stuff
	/**
	 * The name of the setting as used in a configuration.
	 */
	public final String name;
	/**
	 * A function that converts a String into a T.
	 */
	public final Function<String, T> fromString;
	/**
	 * A function that converts a T back into a String.
	 */
	public final Function<T, String> toString;

	/**
	 * Constructor.
	 *
	 * @param name The name as used in the configuration.
	 * @param def The default value.
	 * @param fromString A function that converts a String into a T.
	 * @param toString A function that converts a T back into a String.
	 */
	public Setting(String name, T def, Function<String, T> fromString,
			Function<T, String> toString) {
		this.name = name;
		this.def = def;
		this.fromString = fromString;
		this.toString = toString;
	}
}
