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

import static org.mockito.Mockito.*;

import java.util.Arrays;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.init.Biomes;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.EmptyTickList;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

/**
 * A minimal implementation of World.
 */
abstract class SimpleWorld extends World {
	protected SimpleWorld(ISaveHandler saveHandlerIn, WorldInfo info, Dimension dimension, Profiler profilerIn,
			boolean client) {
		super(saveHandlerIn, info, dimension, profilerIn, client);
		this.chunkProvider = createChunkProvider();
	}

	private final ITickList<Block> pendingBlockTicks = EmptyTickList.get();
	private final ITickList<Fluid> pendingFluidTicks = EmptyTickList.get();
	private final Scoreboard scoreboard = mock(Scoreboard.class);
	private final RecipeManager recipeManager = mock(RecipeManager.class);
	private final NetworkTagManager tags = mock(NetworkTagManager.class);

	@Override
	public ITickList<Block> getPendingBlockTicks() {
		return pendingBlockTicks;
	}

	@Override
	public ITickList<Fluid> getPendingFluidTicks() {
		return pendingFluidTicks;
	}

	@Override
	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	@Override
	public RecipeManager getRecipeManager() {
		return recipeManager;
	}

	@Override
	public NetworkTagManager getTags() {
		return tags;
	}

	@Override
	public boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
		return true;
	}

	@Override
	protected IChunkProvider createChunkProvider() {
		return new SimpleChunkProvider();
	}

	private static final Biome[] NO_BIOMES = new Biome[16*16];
	static {
		Arrays.fill(NO_BIOMES, Biomes.THE_VOID);
	}

	/**
	 * A simple chunk provider implementation that just caches chunks in a map.
	 */
	private final class SimpleChunkProvider implements IChunkProvider {
		private final Long2ObjectMap<Chunk> map = new Long2ObjectOpenHashMap<>();

		@Override
		public Chunk provideChunk(int x, int z) {
			return map.computeIfAbsent(ChunkPos.asLong(x, z), k -> new Chunk(SimpleWorld.this, x, z, NO_BIOMES));
		}

		@Override
		public Chunk getLoadedChunk(int x, int z) {
			return provideChunk(x, z);
		}

		@Override
		public IChunk provideChunkOrPrimer(int x, int z) {
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

		@Override
		public boolean isChunkGeneratedAt(int x, int z) {
			return true;
		}

		@Override
		public IChunkGenerator<?> getChunkGenerator() {
			return null; // This is allowed, albeit awkward; see ChunkProviderClient
		}
	}
}
