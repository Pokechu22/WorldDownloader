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
	 * Called when a block's GUI closes. <br/>
	 * Note that the given position may not always be a block of the type for
	 * the container. Double-check that it is. This can happen if there is lag
	 * and the player looks away from the block before the GUI opens. It may
	 * also be an entity's GUI that the player looked away from.
	 * 
	 * @param world
	 *            The world the block is in.
	 * @param pos
	 *            The position of the block. (Actually, the position that the
	 *            player was looking at when the GUI was opened, and may not be
	 *            the actual location of the block.)
	 * @param container
	 *            The container that the closing player had open.
	 * 
	 * @return Whether the given mod handled the event (if <code>false</code> is
	 *         returned, it'll be passed on to the next mod).
	 */
	public abstract boolean onBlockGuiClosed(WorldClient world, BlockPos pos,
			Container container);

	/**
	 * Called when an entity's GUI closes. <br/>
	 * Note that the given entity may not have been the one coresponding to the
	 * entity. Double-check that it is. This can happen if there is lag and the
	 * player looks at an entity before the GUI opens (or if an entity walks in
	 * the way).
	 * 
	 * @param world
	 *            The world the block is in.
	 * @param entity
	 *            The entity whose GUI was closed.
	 * @param container
	 *            The container that the closing player had open.
	 * 
	 * @return Whether the given mod handled the event (if <code>false</code> is
	 *         returned, it'll be passed on to the next mod).
	 */
	public abstract boolean onEntityGuiClosed(WorldClient world, Entity entity,
			Container container);

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
