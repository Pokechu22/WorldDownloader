package wdl.api;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;

/**
 * {@link IWDLMod} that helps identify "problematic" tile entities that need to
 * be imported from previously saved chunks (IE, tile entities that do not have
 * their data sent unless opened, such as chests).
 */
public interface ITileEntityImportationIdentifier extends IWDLMod {
	/**
	 * Checks if the TileEntity should be imported. Only "problematic" (IE,
	 * those that require manual interaction such as chests) TileEntities will
	 * be imported. Additionally, the block at the tile entity's coordinates
	 * must be one that would normally be used with that tile entity.
	 * 
	 * @param entityID
	 *            The tile entity's ID, as found in the 'id' tag.
	 * @param pos
	 *            The location of the tile entity, as created by its 'x', 'y',
	 *            and 'z' tags.
	 * @param block
	 *            The block at the given position.
	 * @param tileEntityNBT
	 *            The full NBT tag of the existing tile entity. May be used if
	 *            further identification is needed.
	 * @param chunk
	 *            The chunk for which entities are being imported. May be used
	 *            if further identification is needed (eg nearby blocks).
	 * @return <code>true</code> if that tile entity should be imported.
	 */
	public boolean shouldImportTileEntity(String entityID, BlockPos pos,
			Block block, NBTTagCompound tileEntityNBT, Chunk chunk);
}
