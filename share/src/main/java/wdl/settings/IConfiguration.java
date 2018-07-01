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

import java.util.Map;

import net.minecraft.crash.CrashReportCategory;

public interface IConfiguration {

	/**
	 * Sets the value for a setting.
	 *
	 * @param setting The setting to change.
	 * @param value The new value.
	 */
	public abstract <T> void setValue(Setting<T> setting, T value);

	/**
	 * Gets the value for a setting.
	 *
	 * @param setting The setting to change.
	 * @param value The new value.
	 */
	public abstract <T> T getValue(Setting<T> setting);

	// These methods exist partially because they can change a Setting<?> to a
	// Setting<T> so that the type from getValue is still the type of the setting
	// (useful for e.g. SettingButton)
	/**
	 * Cycles the value of a setting.
	 *
	 * @param setting The setting to cycle
	 * @see CyclableSetting#cycle
	 */
	public default <T> void cycle(CyclableSetting<T> setting) {
		this.setValue(setting, setting.cycle(this.getValue(setting)));
	}

	/**
	 * Gets the text for a setting.
	 *
	 * @param setting The setting to use
	 * @see CyclableSetting#getButtonText
	 */
	public default <T> String getButtonText(CyclableSetting<T> setting) {
		return setting.getButtonText(this.getValue(setting));
	}

	/**
	 * Gets a map of gamerules to values set in this configuration. This includes ones
	 * inherited from the parent.
	 */
	public abstract Map<String, String> getGameRules();

	/**
	 * Puts the contents of this configuration into the given crash report category.
	 */
	public abstract void addToCrashReport(CrashReportCategory category, String name);

}