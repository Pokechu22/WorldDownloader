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

/**
 * A wrapper from a setting to the property represented by it.
 *
 * @param <T> The type of value.
 */
public interface Setting<T> {
	/**
	 * Deserializes a String back into a T instance.
	 */
	public abstract T deserializeFromString(String text);

	/**
	 * Serializes a T instance into a String.
	 */
	public abstract String serializeToString(T value);

	/**
	 * Gets the key as used in a Properties file.
	 */
	public abstract String getConfigurationKey();

	/**
	 * Gets the default value of this setting, given the configuration file as
	 * context.
	 */
	public abstract T getDefault(IConfiguration context);
}
