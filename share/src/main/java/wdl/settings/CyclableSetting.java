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
 * A cyclable setting is a setting that can be cycled via a button through a
 * fixed set of values.
 */
public abstract class CyclableSetting<T> extends BaseSetting<T> {
	private String key;

	/**
	 * Constructor.
	 *
	 * @param name The name as used in a properties file.
	 * @param def The default value.
	 * @param key The translation key.
	 * @param fromString A function that converts a String into a T.
	 */
	public CyclableSetting(String name, T def, String key, Function<String, T> fromString) {
		this(name, def, key, fromString, T::toString);
	}

	/**
	 * Constructor.
	 *
	 * @param name The name as used in a properties file.
	 * @param def The default value.
	 * @param key The translation key.
	 * @param fromString A function that converts a String into a T.
	 * @param toString A function that converts a T back into a String.
	 */
	public CyclableSetting(String name, T def, String key, Function<String, T> fromString, Function<T, String> toString) {
		super(name, def, fromString, toString);
	}

	/**
	 * Cycles into the next value.
	 *
	 * @param value Current value
	 * @return Next value
	 */
	public abstract T cycle(T value);

	/**
	 * Gets a translation string that describes this setting.
	 *
	 * @return A translation string.
	 */
	public String getDescription() {
		return key + ".description";
	}
	/**
	 * Gets a translation string that describes this setting's current value.
	 *
	 * @return A translation string.
	 */
	public String getButtonText(T curValue) {
		return key + "." + serializeToString(curValue);
	}
}
