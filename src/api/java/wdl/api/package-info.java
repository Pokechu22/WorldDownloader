/*
 * This file is part of the World Downloader API.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * You are free to include the World Downloader API within your own mods, as
 * permitted via the MMPLv2.
 */
/**
 * <h1>World Downloader API</h1>
 *
 * You can create extensions for this mod using the API.  Simply implement
 * some of the WDL interfaces, and then call {@link WDLApi#addWDLMod(IWDLMod)}.
 *
 * Note that you do need to handle loading the mod yourself - use liteloader
 * or Minecraft Forge for that.
 */
@javax.annotation.ParametersAreNonnullByDefault
package wdl.api;
