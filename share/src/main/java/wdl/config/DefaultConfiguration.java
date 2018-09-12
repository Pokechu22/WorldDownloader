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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckForSigned;

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
		return setting.getDefault(config);
	}

	@Override
	@Deprecated
	public <T> void clearValue(Setting<T> setting) {
		throw new UnsupportedOperationException("Cannot clear settings on the default configuration (" + setting + ")");
	}

	@Override
	public void addToCrashReport(CrashReportCategory category, String name) {
		category.addDetail("-", "Default config (" + name + ")");
	}

	@Override
	@Deprecated
	public void load(File file) throws IOException {
		throw new UnsupportedOperationException("Cannot load the default configuration!");
	}

	@Override
	@Deprecated
	public void store(File file, String comments) throws IOException {
		throw new UnsupportedOperationException("Cannot save the default configuration!");
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
	@Deprecated
	public void setGameRule(String name, String value) {
		throw new UnsupportedOperationException("Cannot change gamerule settings on the default configuration (" + name + "->" + value + ")");
	}

	@Override
	@Deprecated
	public void clearGameRule(String name) {
		// This is allowed, as clearGameRule is recursive, but it shouldn't generally be used directly.
	}

	@Override
	public boolean hasGameRule(String name) {
		return false;
	}

	@Override
	@CheckForSigned
	public int getUserEntityTrackDistance(String entityType) {
		return -1;
	}

	@Override
	@Deprecated
	public void setUserEntityTrackDistance(String entityType, int value) {
		throw new UnsupportedOperationException("Cannot change entity track settings on the default configuration (" + entityType + "->" + value + ")");
	}

	// XXX this is a bit of an ugly hack.  Set in WDL.<clinit>.
	public static final Set<String> DANGEROUS_ENTITY_TYPES = new HashSet<>();

	@Override
	public boolean isEntityTypeEnabled(String entityType) {
		return !DANGEROUS_ENTITY_TYPES.contains(entityType);
	}

	@Override
	@Deprecated
	public void setEntityTypeEnabled(String entityType, boolean value) {
		throw new UnsupportedOperationException("Cannot change entity type settings on the default configuration (" + entityType + "->" + value + ")");
	}

	@Override
	public boolean isEntityGroupEnabled(String entityGroup) {
		return true;
	}

	@Override
	@Deprecated
	public void setEntityGroupEnabled(String entityGroup, boolean value) {
		throw new UnsupportedOperationException("Cannot change entity group settings on the default configuration (" + entityGroup + "->" + value + ")");
	}
}
