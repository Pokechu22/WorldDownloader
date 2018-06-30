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
 * A setting that can be represented directly as a button that cycles through
 * several values.
 */
public interface ICyclableSetting<T> extends ISetting<T> {
	/**
	 * Gets a description of the setting, to be used when hovering over the button.
	 *
	 * @return A translation string
	 */
	public abstract String getDescription();

	/**
	 * Gets a description of the current value.
	 *
	 * @return A translation string
	 */
	public abstract String getValueText();

	/**
	 * Cycles through to the next value.
	 */
	public abstract void cycle();
}
