package wdl.api;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.util.BlockPos;

public interface IGuiHooksListener extends IWDLMod {
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
}
