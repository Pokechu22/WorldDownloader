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

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * A cyclable setting is a setting that can be cycled via a button through a
 * fixed set of values.
 *
 * This implementation assumes a fixed default, and fixed parameter-less translation strings
 * based on key.value and key.description.
 */
public abstract class BaseCyclableSetting<T> extends BaseSetting<T> implements CyclableSetting<T> {
	private final String key;

	/**
	 * Constructor.
	 *
	 * @param name The name as used in a properties file.
	 * @param def The default value.
	 * @param key The translation key.
	 * @param fromString A function that converts a String into a T.
	 */
	public BaseCyclableSetting(String name, T def, String key, Function<String, T> fromString) {
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
	public BaseCyclableSetting(String name, T def, String key, Function<String, T> fromString, Function<T, String> toString) {
		super(name, def, fromString, toString);
		this.key = key;
	}

	@Override
	public abstract T cycle(T value);

	@Override
	public ITextComponent getDescription() {
		return new TextComponentTranslation(key + ".description");
	}

	@Override
	public ITextComponent getButtonText(T curValue) {
		return new TextComponentTranslation(key + "." + serializeToString(curValue));
	}
}
