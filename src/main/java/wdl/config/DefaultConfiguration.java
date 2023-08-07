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

import java.io.File;
import java.io.IOException;

import javax.annotation.CheckForSigned;

import net.minecraft.crash.CrashReportCategory;
import wdl.EntityUtils;

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
	@CheckForSigned
	public int getUserEntityTrackDistance(String entityType) {
		return -1;
	}

	@Override
	@Deprecated
	public void setUserEntityTrackDistance(String entityType, int value) {
		throw new UnsupportedOperationException("Cannot change entity track settings on the default configuration (" + entityType + "->" + value + ")");
	}

	@Override
	public boolean isEntityTypeEnabled(String entityType) {
		return EntityUtils.isEntityEnabledByDefault(entityType);
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
