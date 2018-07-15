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

import java.util.Collections;
import java.util.Set;

import net.minecraft.crash.CrashReportCategory;

/**
 * An immutable default configuration.
 */
public class DefaultConfiguration implements IConfiguration {

	@Override
	@Deprecated
	public <T> void setValue(Setting<T> setting, T value) {
		throw new UnsupportedOperationException("Cannot change settings on the default configuration (" + setting + "->" + value + ")");
	}

	@Override
	public <T> T getValue(Setting<T> setting, IConfiguration config) {
		return setting.getDefault(this);
	}

	@Override
	@Deprecated
	public <T> void clearValue(Setting<T> setting) {
		throw new UnsupportedOperationException("Cannot clear settings on the default configuration (" + setting + ")");
	}

	@Override
	public void addToCrashReport(CrashReportCategory category, String name) {
		category.addCrashSection("-", "Default config (" + name + ")");
	}

	@Override
	public Set<String> getGameRules() {
		return Collections.emptySet();
	}

	@Override
	public String getGameRule(String name) {
		throw new IllegalArgumentException("No custom value specified for gamerule " + name); // XXX Is this the best behavior?
	}

	@Override
	public void setGameRule(String name, String value) {
		throw new UnsupportedOperationException("Cannot change gamerule settings on the default configuration (" + name + "->" + value + ")");
	}

	@Override
	public void clearGameRule(String name) {
		// This is allowed, as clearGameRule is recursive.
	}

	@Override
	public boolean hasGameRule(String name) {
		return false;
	}
}
