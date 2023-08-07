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
package wdl.api;

import net.minecraft.block.Block;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public interface IBlockEventListener extends IWDLMod {
	/**
	 * Called when a "Block Event" occurs. Some mods use this to send data. (In
	 * vanilla, noteblocks used this to play a note until 1.13).
	 *
	 * @param world
	 *            The world in which the event occurred.
	 * @param pos
	 *            The position the event occurred at.
	 * @param block
	 *            The type of block.
	 * @param data1
	 *            Data1 of the event.
	 * @param data2
	 *            Data2 of the event.
	 */
	public abstract void onBlockEvent(ClientWorld world, BlockPos pos,
			Block block, int data1, int data2);
}
