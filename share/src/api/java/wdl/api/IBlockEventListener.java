/*
 * This file is part of the World Downloader API.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * You are free to include the World Downloader API within your own mods, as
 * permitted via the MMPLv2.
 */
package wdl.api;

import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.BlockPos;

public interface IBlockEventListener extends IWDLMod {
	/**
	 * Called when a "Block Event" occurs. Some mods use this to send data. (In
	 * vanilla, noteblocks use this to play a note).
	 *
	 * @param world
	 *            The world in which the event occured.
	 * @param pos
	 *            The position the event occured at.
	 * @param block
	 *            The type of block.
	 * @param data1
	 *            Data1 of the event.
	 * @param data2
	 *            Data2 of the event.
	 */
	public abstract void onBlockEvent(WorldClient world, BlockPos pos,
			Block block, int data1, int data2);
}
