/*
 * This file is part of WDL Forge.  WDL Forge contains the forge-specific
 * code for World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.forge;

import net.minecraftforge.fml.common.Mod;
import wdl.api.APIImpl;

@Mod("wdl")
public class WDLForgeMod {
	public WDLForgeMod() {
		// Since Forge generates a crash report on startup, which will also include
		// WDL crash info, we want to make sure the API is initialized.
		APIImpl.ensureInitialized();
	}
}
