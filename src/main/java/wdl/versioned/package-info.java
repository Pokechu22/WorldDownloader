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
/**
 * Version-specific code. {@link VersionedFunctions} contains a number of
 * version-specific methods, and the various interfaces declared in this package
 * are inherited by it to load the right methods. The wrong versions are
 * excluded from compilation in the buildscript.
 *
 * Note that this is not the only location that uses version-specific code; for instance,
 * {@link wdl.WDLChunkLoader} also uses it.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
package wdl.versioned;

import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
