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
 *
 * @param <T> Type for this setting.  Currently unused (!)
 */
public class Setting<T> {

	public final String name;
	@Nullable
	private String value;
	private final Configuration owner;
	private final Function<Configuration, Setting<T>> fieldGetter;

	public Setting(String name, String value, Configuration owner, Function<Configuration, Setting<T>> fieldGetter) {
		this.name = name;
		this.value = value;
		this.owner = owner;
		this.fieldGetter = fieldGetter;
	}

	/**
	 * Gets the value matched by this setting.
	 */
	@Nonnull
	public String get() {
		if (owner.containsKey(name)) {
			return owner.getProperty(name);
		} else {
			if (owner.parent != null) {
				return fieldGetter.apply(owner.parent).get();
			} else {
				throw new IllegalStateException("No value was set even on the default value");
			}
		}
	}

	/**
	 * Sets the value matched by this setting.
	 */
	public void set(String value) {
		if (owner.containsKey(name)) {
			owner.setProperty(name, value);
		} else {
			if (owner.parent != null) {
				fieldGetter.apply(owner.parent).set(value);
			} else {
				throw new IllegalStateException("No value was set even on the default value");
			}
		}
	}
}
