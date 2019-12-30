/*
 * This file is part of LiteModWDL.  LiteModWDL contains the liteloader-specific
 * code for World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package com.uyjulian.LiteModWDL;

import java.io.File;

import wdl.VersionConstants;

import com.mumfrey.liteloader.LiteMod;

public class LiteModWDL implements LiteMod {

	@Override
	public String getName() {
		return "LiteModWDL";
	}

	@Override
	public String getVersion() {
		return VersionConstants.getModVersion() + "-" + VersionConstants.getExpectedVersion();
	}

	@Override
	public void init(File configPath) {

	}

	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath) {

	}


}
