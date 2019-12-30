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

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.math.BlockPos;

/**
 * Something that can edit tile entities as they are being saved.
 */
public interface ITileEntityEditor extends IWDLMod {
	/**
	 * Should the given tile entity be edited by this {@link ITileEntityEditor}?
	 *
	 * To get the type of the entity, look at the "id" String tag. See
	 * {@link TileEntity}'s static initializer block for a list of tile entity
	 * ids.
	 *
	 * Example:
	 *
	 * <pre>
	 * String type = compound.{@link NBTTagCompound#getString(String) getString("id")}
	 *
	 * // "Trap" is dispenser
	 * if (type.equals("Trap") || type.equals("Dropper")) {
	 *     return true;
	 * }
	 *
	 * return false;
	 * </pre>
	 *
	 * @param pos
	 *            The location of the tile entity in the world.
	 * @param compound
	 *            The tile entity to check.
	 * @param creationMode
	 *            How the tile entity was created.
	 * @return Whether it should be edited.
	 */
	public abstract boolean shouldEdit(BlockPos pos, NBTTagCompound compound,
			TileEntityCreationMode creationMode);

	/**
	 * Edit the given tile entity. Will only be called if
	 * {@link #shouldEdit(NBTTagCompound, TileEntityCreationMode)} returned
	 * true.
	 *
	 * The given NBT tag must be edited in-place.
	 *
	 * If you want to work with a TileEntity object instead of a
	 * {@link NBTTagCompound}, you can serialize and deserialize it:
	 *
	 * <pre>
	 * {@link TileEntity} te = {@link TileEntity#readFromNBT(NBTTagCompound) TileEntity.readFromNBT(compound)};
	 *
	 * if (te instanceof {@link TileEntityDispenser}) {
	 *     TileEntityDispenser dispenser = (TileEntityDispenser)te;
	 *
	 *     for (int i = 0; i < dispenser.{@link TileEntityDispenser#getSizeInventory() getSizeInventory()}; i++) {
	 *         dispenser.{@link TileEntityDispenser#setInventorySlotContents(int, net.minecraft.item.ItemStack) setInventorySlotContents}(i, new {@link ItemStack}({@link Blocks#tnt}, 64));
	 *     }
	 *
	 *     dispenser.{@link TileEntity#writeToNBT(NBTTagCompound) writeToNBT(compound)};
	 * }
	 * </pre>
	 *
	 * @param pos
	 *            The location of the tile entity in the world
	 * @param compound
	 *            The tile entity to edit.
	 * @param creationMode
	 *            How the tile entity was created.
	 */
	public abstract void editTileEntity(BlockPos pos, NBTTagCompound compound,
			TileEntityCreationMode creationMode);

	/**
	 * Identifies how the tile entity was created/loaded.
	 */
	public static enum TileEntityCreationMode {
		/**
		 * The tile entity was imported from an older version of the chunk.
		 */
		IMPORTED,
		/**
		 * The tile entity already existed in the chunk (for instance, signs and
		 * player skulls).
		 */
		EXISTING,
		/**
		 * The tile entity was manually saved, generally by having its GUI
		 * manually opened.
		 */
		NEW
	}
}
