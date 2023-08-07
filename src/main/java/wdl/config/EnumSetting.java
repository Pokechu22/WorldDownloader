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

import net.minecraft.util.IStringSerializable;

/**
 * A setting that uses an enum.
 */
public final class EnumSetting<T extends Enum<T> & IStringSerializable> extends BaseCyclableSetting<T> {

	private T[] values;

	/**
	 * Constructor.
	 *
	 * @param name The name as used in a properties file.
	 * @param def The default value.
	 * @param key The translation key.
	 * @param values The values() method for the given enum.
	 * @param fromString A function taking a string and returning an enum instance (e.g. valueOf)
	 */
	public EnumSetting(String name, T def, String key, T[] values, Function<String, T> fromString) {
		super(name, def, key, fromString, t -> t.getName());
		this.values = values;
	}

	@Override
	public T cycle(T value) {
		return values[(value.ordinal() + 1) % values.length];
	}
}
