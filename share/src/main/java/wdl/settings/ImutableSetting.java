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
 * A setting that takes one value that cannot be changed.  Intended for defaults
 */
public final class ImutableSetting<T> implements ISetting<T>, ICyclableSetting<T> /* falsely */ {
	private final T value;

	public ImutableSetting(T value) {
		this.value = value;
	}

	@Override
	public T get() {
		return value;
	}

	@Override
	public void set(T value) {
		throw new UnsupportedOperationException("Cannot mutate imutable settings");
	}

	@Override
	public void cycle() {
		throw new UnsupportedOperationException("Cannot cycle imutable settings");
	}

	@Override
	public String getDescription() {
		return null; // uhh, not sure what to do here
	}

	@Override
	public String getValueText() {
		return null; // uhh, not sure what to do here
	}
}
