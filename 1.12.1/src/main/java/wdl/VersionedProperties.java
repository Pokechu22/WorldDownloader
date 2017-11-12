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
package wdl;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.inventory.ContainerShulkerBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * Helper that determines version-specific information about things, such
 * as whether a world has skylight.
 */
public class VersionedProperties {
	/**
	 * Returns true if the given world has skylight data.
	 *
	 * @return a boolean
	 */
	public static boolean hasSkyLight(World world) {
		// 1.11+: use hasSkyLight
		return world.provider.hasSkyLight();
	}

	/**
	 * Returns the ID used for block entities with the given class in the given version
	 *
	 * @return The ID, or an empty string if the given TE is not registered.
	 */
	public static String getBlockEntityID(Class<? extends TileEntity> clazz) {
		// 1.11+: use new IDs, and getKey exists.
		ResourceLocation loc = TileEntity.getKey(clazz);
		return (loc != null) ? loc.toString() : "";
	}

	/**
	 * Called when any GUI is closed, to handle shulker boxes.
	 *
	 * XXX This is not a good approach to version specific block entities.
	 */
	public static boolean handleShulkerGuiClosed(TileEntity te) {
		if (WDL.windowContainer instanceof ContainerShulkerBox
				&& te instanceof TileEntityShulkerBox) {
			WDLEvents.saveContainerItems(WDL.windowContainer, (TileEntityShulkerBox) te, 0);
			WDL.saveTileEntity(WDL.lastClickedBlock, te);
			return true;
		}
		return false;
	}
	/**
	 * Checks if the given block is a shulker box, and the block entity ID matches.
	 */
	public static boolean isImportableShulkerBox(String entityID, Block block) {
		return block instanceof BlockShulkerBox && entityID.equals(getBlockEntityID(TileEntityShulkerBox.class));
	}

	/**
	 * Gets the class used to store the list of chunks in ChunkProviderClient.
	 */
	@SuppressWarnings("rawtypes")
	public static Class<Long2ObjectMap> getChunkListClass() {
		return Long2ObjectMap.class;
	}
}
