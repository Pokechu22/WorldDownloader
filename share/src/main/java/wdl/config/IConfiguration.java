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
package wdl.config;

import java.util.Set;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnegative;

import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.text.ITextComponent;

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
	 * @param setting The setting to query
	 */
	public default <T> T getValue(Setting<T> setting) {
		return getValue(setting, this);
	}

	/**
	 * Gets the value for a setting.
	 *
	 * Note: This method is mainly intended for internal use (it would be protected,
	 * if that were possible on interfaces). Generally, {@link #getValue(Setting)}
	 * should be used.
	 *
	 * @param setting The setting to query.
	 * @param config  The configuration that this check was initiated on, which may
	 *                be passed to {@link Setting#getDefault}.
	 */
	public abstract <T> T getValue(Setting<T> setting, IConfiguration config);

	/**
	 * Clears the value of the given setting, at the current configuration level only.
	 * Does nothing if no value is set for that setting.
	 *
	 * @param setting The setting to clear.
	 */
	public abstract <T> void clearValue(Setting<T> setting);

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
	public default <T> ITextComponent getButtonText(CyclableSetting<T> setting) {
		return setting.getButtonText(this.getValue(setting));
	}

	/**
	 * Puts the contents of this configuration into the given crash report category.
	 */
	public abstract void addToCrashReport(CrashReportCategory category, String name);

	// XXX Older things that should be moved to the Setting system eventually, but
	// I don't have time to do for this release
	/**
	 * Gets a set of defined gamerules.  The resultant set is not backed by this collection.
	 * This includes rules defined by the parent.
	 */
	public abstract Set<String> getGameRules();

	/**
	 * Queries the value of a gamerule.
	 */
	public abstract String getGameRule(String name);

	/**
	 * Sets the value of a gamerule.
	 */
	public abstract void setGameRule(String name, String value);

	/**
	 * Removes the value for the given gamerule, even if it's set in a parent.
	 */
	public abstract void clearGameRule(String name);

	/**
	 * True if the given rule is present in this config or a parent of it.
	 */
	public abstract boolean hasGameRule(String name);

	/**
	 * Queries the track distance for the given entity type.
	 * @return The distance if configured, or else -1.
	 */
	@CheckForSigned
	public abstract int getUserEntityTrackDistance(String entityType);

	/**
	 * Sets the track distance for the given entity type.
	 */
	public abstract void setUserEntityTrackDistance(String entityType, @Nonnegative int value);

	/**
	 * Checks if the given entity type is enabled.
	 */
	public abstract boolean isEntityTypeEnabled(String entityType);

	/**
	 * Sets whether or not the given entity type is enabled.
	 */
	public abstract void setEntityTypeEnabled(String entityType, boolean value);

	/**
	 * Checks if the given entity group is enabled.
	 */
	public abstract boolean isEntityGroupEnabled(String entityGroup);

	/**
	 * Sets whether or not the given entity group is enabled.
	 */
	public abstract void setEntityGroupEnabled(String entityGroup, boolean value);
}