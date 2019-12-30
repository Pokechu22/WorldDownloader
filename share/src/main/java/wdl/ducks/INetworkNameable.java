/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.ducks;

import javax.annotation.Nullable;

/**
 * Interface to get the actual name of a thing as sent over a network.
 */
public interface INetworkNameable extends IBaseChangesApplied {
	/**
	 * Gets the "true" custom display name of this item. For instance, a furnace
	 * that has not been renamed will return null, but a furnace that has been named
	 * "smelter" will return "smelter", and a furnace that has been renamed to
	 * "Furnace" will return "Furnace" (and not null).
	 *
	 * @return The actual name from the network, or null if no custom name was set.
	 */
	@Nullable
	String getCustomDisplayName();
}
