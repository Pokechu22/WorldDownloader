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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A wrapper from a setting to the property represented by it.
 */
public abstract class BaseSetting<T> implements ISetting<T> {

	public final String name;
	@Nullable
	private T value;
	@Nullable
	private final Configuration owner;
	private final Function<Configuration, ISetting<T>> fieldGetter;

	/**
	 * Constructor.
	 *
	 * @param name The name as used in the configuration.
	 * @param value The value to currently use.
	 * @param owner The parent configuration.  May be null.
	 * @param fieldGetter A function to access the field for this setting.
	 */
	public BaseSetting(String name, @Nullable T value, @Nullable Configuration owner,
			Function<Configuration, ISetting<T>> fieldGetter) {
		this.name = name;
		this.value = value;
		this.owner = owner;
		this.fieldGetter = fieldGetter;
	}

	/**
	 * Gets a string value for this setting.
	 */
	@Nonnull
	protected String getString() {
		if (owner.containsKey(name)) {
			return owner.getProperty(name);
		} else {
			if (owner.parent != null) {
				return ((BaseSetting<T>)fieldGetter.apply(owner.parent)).getString(); // XXX cast is a hack
			} else {
				throw new IllegalStateException("No value was set even on the default value");
			}
		}
	}

	/**
	 * Sets a string value for this setting.
	 */
	protected void set(String value) {
		owner.setProperty(name, value);
	}
}
