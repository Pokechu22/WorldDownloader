package wdl.api;

import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.WorldClient;
import wdl.BlockPos;

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
