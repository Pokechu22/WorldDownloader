/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.handler.block;

import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import wdl.WDL;
import wdl.WDLMessageTypes;
import wdl.api.IWDLMessageType;

public abstract class BlockHandler<B extends TileEntity, C extends Container> {
	@SuppressWarnings("serial")
	public static class HandlerException extends Exception {
		public HandlerException(@Nonnull String translationKey, @Nonnull Object... args) {
			this(WDLMessageTypes.ON_GUI_CLOSED_WARNING, translationKey);
		}
		public HandlerException(@Nonnull IWDLMessageType messageType, @Nonnull String translationKey, @Nonnull Object... args) {
			this.translationKey = translationKey;
			this.messageType = messageType;
			this.args = args;
		}
		public final @Nonnull String translationKey;
		public final @Nonnull IWDLMessageType messageType;
		public final @Nonnull Object[] args;
	}

	protected BlockHandler(@Nonnull Class<B> blockEntityClass, @Nonnull Class<C> containerClass) {
		this.blockEntityClass = blockEntityClass;
		this.containerClass = containerClass;
	}
	protected final @Nonnull Class<B> blockEntityClass;
	protected final @Nonnull Class<C> containerClass;
	/** Gets the type of block entity handled by this. */
	public final @Nonnull Class<B> getBlockEntityClass() {
		return blockEntityClass;
	}
	/** Gets the type of container handled by this. */
	public final @Nonnull Class<C> getContainerClass() {
		return containerClass;
	}

	/**
	 * Saves the contents of a block entity from the container.
	 *
	 * @param clickedPos
	 *            The position that the clicked block is at.  It is assumed that
	 *            blockEntity is at that position.
	 * @param container
	 *            The container to grab items from.
	 * @param blockEntity
	 *            The block entity at the given position.
	 * @param world
	 *            The world to query if more information is needed.
	 * @param saveMethod
	 *            The method to call to save block entities.
	 * @return A translation key to put into chat describing what was saved.
	 * @throws HandlerException
	 *             When something is handled wrong.
	 */
	public abstract @Nonnull String handle(@Nonnull BlockPos clickedPos, @Nonnull C container,
			@Nonnull B blockEntity, @Nonnull IBlockAccess world,
			@Nonnull BiConsumer<BlockPos, B> saveMethod) throws HandlerException;

	/**
	 * Saves the items of a container to the given TileEntity.
	 *
	 * @param container
	 *            The container to save from, usually {@link WDL#windowContainer} .
	 * @param tileEntity
	 *            The TileEntity to save to.
	 * @param containerStartIndex
	 *            The index in the container to start copying items from.
	 */
	protected static void saveContainerItems(@Nonnull Container container,
			@Nonnull IInventory tileEntity, int containerStartIndex) {
		int containerSize = container.inventorySlots.size();
		int inventorySize = tileEntity.getSizeInventory();
		int containerIndex = containerStartIndex;
		int inventoryIndex = 0;

		while ((containerIndex < containerSize) && (inventoryIndex < inventorySize)) {
			Slot slot = container.getSlot(containerIndex);
			if (slot.getHasStack()) {
				tileEntity.setInventorySlotContents(inventoryIndex, slot.getStack());
			}
			inventoryIndex++;
			containerIndex++;
		}
	}

	/**
	 * Saves the fields of an inventory.
	 * Fields are pieces of data such as furnace smelt time and
	 * beacon effects.
	 *
	 * @param inventory The inventory to save from.
	 * @param tileEntity The inventory to save to.
	 */
	protected static void saveInventoryFields(@Nonnull IInventory inventory,
			@Nonnull IInventory tileEntity) {
		for (int i = 0; i < inventory.getFieldCount(); i++) {
			tileEntity.setField(i, inventory.getField(i));
		}
	}
}
