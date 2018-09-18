/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.handler;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

/**
 * A partial implementation of World that can be extended from all versions.
 */
abstract class SimpleWorld extends World {
	protected SimpleWorld(ISaveHandler saveHandlerIn, WorldInfo info, Dimension dimension, Profiler profilerIn,
			boolean client) {
		super(saveHandlerIn, info, dimension, profilerIn, client);
		this.chunkProvider = this.createChunkProvider();
	}

	/** Places a block, creating the proper state. */
	public void placeBlockAt(BlockPos pos, Block block, EntityPlayer player, EnumFacing direction) {
		player.rotationYaw = direction.getHorizontalAngle();
		IBlockState state = block.getStateForPlacement(this, pos, direction, 0, 0, 0, 0, player);
		this.setBlockState(pos, state);
	}

	@Override
	protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
		return true;
	}

	@Override
	protected IChunkProvider createChunkProvider() {
		return new SimpleChunkProvider();
	}

	/**
	 * A simple chunk provider implementation that just caches chunks in a map.
	 */
	private final class SimpleChunkProvider implements IChunkProvider {
		private final Long2ObjectMap<Chunk> map = new Long2ObjectOpenHashMap<>();

		@Override
		public Chunk provideChunk(int x, int z) {
			return map.computeIfAbsent(ChunkPos.asLong(x, z), k -> new Chunk(SimpleWorld.this, x, z));
		}

		@Override
		public Chunk getLoadedChunk(int x, int z) {
			return provideChunk(x, z);
		}

		@Override
		public boolean tick() {
			return false;
		}

		@Override
		public String makeString() {
			return this.toString();
		}
	}
}
