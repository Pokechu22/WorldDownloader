package wdl;

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
			WDL.saveContainerItems(WDL.windowContainer, (TileEntityShulkerBox) te, 0);
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
}
