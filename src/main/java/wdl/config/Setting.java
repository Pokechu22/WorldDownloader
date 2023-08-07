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

import java.util.Optional;

/**
 * A wrapper from a setting to the property represented by it.
 *
 * @param <T> The type of value.
 */
public interface Setting<T> {
	/**
	 * Deserializes a String back into a T instance.
	 *
	 * This method may return null values or throw an exception on incorrect input.
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
	 *
	 * WARNING: Use caution when referencing the parameter, to avoid infinite loops.
	 */
	public abstract T getDefault(IConfiguration context);

	/**
	 * Overrides the value of this setting, based on the configuration context.
	 *
	 * WARNING: Use caution when referencing the parameter, to avoid infinite loops.
	 *
	 * @return A value that, if present, is used instead of the value set directly
	 *         in the config.
	 */
	public default Optional<T> overrideFromContext(IConfiguration context) {
		return Optional.empty();
	}
}
