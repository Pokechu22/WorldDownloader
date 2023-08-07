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

import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
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
			Block block, CompoundNBT tileEntityNBT, Chunk chunk);
}
