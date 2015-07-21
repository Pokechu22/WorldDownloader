package wdl.api;

import net.minecraft.block.Block;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.BlockPos;

/**
 * Represents a mod that listens to WDL's events.
 * 
 * To use this, it MUST be added to the list of WDL mods via
 * {@link WDLApi#addWDLMod(IWDLMod)}.
 * 
 * Also, implement the subinterfaces for this to be useful.
 */
public interface IWDLMod {
	/**
	 * Gets the name of the mod.
	 * 
	 * @return The name of the mod.
	 */
	public abstract String getName();

	/**
	 * Called when a world loads. (Occurs after the previous world has saved).
	 * 
	 * @param world
	 */
	public abstract void onWorldLoad(WorldClient world);

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

	/**
	 * Called when a Plugin channel packet is received.
	 * 
	 * @param world
	 *            The current world.
	 * @param channel
	 *            The channel the packet was sent on.
	 * @param packetData
	 *            The payload of the packet.
	 */
	public abstract void onPluginChannelPacket(WorldClient world,
			String channel, byte[] packetData);

	/**
	 * Called when a chat message occurs. <br/>
	 * I really hope no mods are using chat to send data anymore, but some
	 * might.
	 * 
	 * @param world
	 *            The current world.
	 * @param message
	 *            The received message, in raw format.
	 */
	public abstract void onChat(WorldClient world, String message);
}
