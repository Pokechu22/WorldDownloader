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
 * Handlers for block actions. This is only used for note blocks, and is a
 * rather silly special case (that doesn't even apply in 1.13).
 *
 * Block actions are also known as block events in MCP. See
 * <a href="https://wiki.vg/index.php?title=Block_Actions&oldid=12934">this
 * article</a> for info about them in 1.12.2 and before, and
 * <a href="https://wiki.vg/Block_Actions">this article</a> for info in 1.13.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
package wdl.handler.blockaction;

import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
