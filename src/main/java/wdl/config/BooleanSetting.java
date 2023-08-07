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

/**
 * A setting that is simply true or false.
 */
public final class BooleanSetting extends BaseCyclableSetting<Boolean> {
	/**
	 * Constructor.
	 *
	 * @param name The name as used in a properties file.
	 * @param def The default value.
	 * @param key The translation key.
	 */
	public BooleanSetting(String name, boolean def, String key) {
		super(name, def, key, Boolean::parseBoolean);
	}

	@Override
	public Boolean cycle(Boolean value) {
		return !value;
	}
}
