/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018-2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;


import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ServerTickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimerTickList;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.EndDimension;
import net.minecraft.world.dimension.NetherDimension;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.SessionLockException;
import wdl.versioned.VersionedFunctions;

/**
 * Alternative implementation of {@link ChunkManager} that handles editing
 * WDL-specific properties of chunks as they are being saved.
 *
 * This variant is used for chunks from 1.13 and later.
 */
abstract class WDLChunkLoaderBase extends ChunkManager {

	private static final Logger LOGGER = LogManager.getLogger();
	/**
	 * Gets the save folder for the given WorldProvider, respecting Forge's
	 * dimension names if forge is present.
	 */
	protected static File getWorldSaveFolder(SaveHandler handler,
			Dimension dimension) {
		File baseFolder = handler.getWorldDirectory();
		// XXX No forge support at this time

		if (dimension instanceof NetherDimension) {
			File file = new File(baseFolder, "DIM-1");
			file.mkdirs();
			return file;
		} else if (dimension instanceof EndDimension) {
			File file = new File(baseFolder, "DIM1");
			file.mkdirs();
			return file;
		}

		return baseFolder;
	}

	protected final WDL wdl;
	protected final Map<ChunkPos, CompoundNBT> chunksToSave;
	protected final File chunkSaveLocation;

	protected WDLChunkLoaderBase(WDL wdl, File file) {
		super(file, null);
		this.wdl = wdl;
		@SuppressWarnings("unchecked")
		Map<ChunkPos, CompoundNBT> chunksToSave =
				ReflectionUtils.findAndGetPrivateField(this, AnvilChunkLoader.class, VersionedFunctions.getChunksToSaveClass());
		this.chunksToSave = chunksToSave;
		this.chunkSaveLocation = file;
	}

	/**
	 * Saves the given chunk.
	 *
	 * Note that while the normal implementation swallows Exceptions, this
	 * version does not.
	 */
	@Override
	public synchronized void saveChunk(World world, IChunk chunk) throws SessionLockException, IOException {
		world.checkSessionLock();

		CompoundNBT levelTag = writeChunkToNBT((Chunk)chunk, world);

		CompoundNBT rootTag = new CompoundNBT();
		rootTag.func_218657_a("Level", levelTag);
		rootTag.putInt("DataVersion", VersionConstants.getDataVersion());

		addChunkToPending(chunk.getPos(), rootTag);

		wdl.unloadChunk(chunk.getPos());
	}

	/**
	 * Writes the given chunk, creating an NBT compound tag.
	 *
	 * Note that this does <b>not</b> override the private method
	 * {@link AnvilChunkLoader#writeChunkToNBT(Chunk, World, NBTCompoundNBT)}.
	 * That method is private and cannot be overridden; plus, this version
	 * returns a tag rather than modifying the one passed as an argument.
	 *
	 * @param chunk
	 *            The chunk to write
	 * @param world
	 *            The world the chunk is in, used to determine the modified
	 *            time.
	 * @return A new CompoundNBT
	 */
	private CompoundNBT writeChunkToNBT(Chunk chunk, World world) {
		CompoundNBT compound = new CompoundNBT();

		compound.putInt("xPos", chunk.x);
		compound.putInt("zPos", chunk.z);
		compound.putLong("LastUpdate", world.getGameTime());
		compound.putLong("InhabitedTime", chunk.getInhabitedTime());
		compound.putString("Status", ChunkStatus.POSTPROCESSED.getName()); // Make sure that the chunk is considered fully generated
		UpgradeData upgradedata = chunk.getUpgradeData();

		if (!upgradedata.isEmpty()) {
			compound.func_218657_a("UpgradeData", upgradedata.write());
		}

		ChunkSection[] chunkSections = chunk.getSections();
		ListNBT chunkSectionList = new ListNBT();
		boolean hasSky = VersionedFunctions.hasSkyLight(world);

		for (ChunkSection chunkSection : chunkSections) {
			if (chunkSection != Chunk.EMPTY_SECTION) {
				CompoundNBT sectionNBT = new CompoundNBT();
				sectionNBT.putByte("Y",
						(byte) (chunkSection.getYLocation() >> 4 & 255));
				chunkSection.getData().writeChunkPalette(sectionNBT, "Palette", "BlockStates");

				NibbleArray blocklightArray = chunkSection.getBlockLight();
				int lightArrayLen = blocklightArray.getData().length;
				sectionNBT.putByteArray("BlockLight", blocklightArray.getData());

				if (hasSky) {
					NibbleArray skylightArray = chunkSection.getSkyLight();
					if (skylightArray != null) {
						sectionNBT.putByteArray("SkyLight", chunkSection.getSkyLight().getData());
					} else {
						// Shouldn't happen, but if it does, handle it smoothly.
						LOGGER.error("[WDL] Skylight array for chunk at " +
								chunk.x + ", " + chunk.z +
								" is null despite VersionedProperties " +
								"saying it shouldn't be!");
						sectionNBT.putByteArray("SkyLight", new byte[lightArrayLen]);
					}
				} else {
					sectionNBT.putByteArray("SkyLight", new byte[chunkSection.getBlockLight().getData().length]);
				}

				chunkSectionList.add((INBT) sectionNBT);
			}
		}

		compound.func_218657_a("Sections", chunkSectionList);

		Biome[] biomes = chunk.getBiomes();
		int[] biomeData = new int[biomes.length];
		for (int i = 0; i < biomes.length; ++i) {
			biomeData[i] = VersionedFunctions.getBiomeId(biomes[i]);
		}

		compound.putIntArray("Biomes", biomeData);

		chunk.setHasEntities(false);
		ListNBT entityList = getEntityList(chunk);
		compound.func_218657_a("Entities", entityList);

		ListNBT tileEntityList = getTileEntityList(chunk);
		compound.func_218657_a("TileEntities", tileEntityList);

		if (world.getPendingBlockTicks() instanceof ServerTickList) {
			compound.func_218657_a("TileTicks", ((ServerTickList<?>) world.getPendingBlockTicks()).write(chunk));
		}
		if (world.getPendingFluidTicks() instanceof ServerTickList) {
			compound.func_218657_a("LiquidTicks", ((ServerTickList<?>) world.getPendingFluidTicks()).write(chunk));
		}

		compound.func_218657_a("PostProcessing", listArrayToTag(chunk.getPackedPositions()));

		if (chunk.getBlocksToBeTicked() instanceof ChunkPrimerTickList) {
			compound.func_218657_a("ToBeTicked", ((ChunkPrimerTickList<?>) chunk.getBlocksToBeTicked()).write());
		}
		if (chunk.getFluidsToBeTicked() instanceof ChunkPrimerTickList) {
			compound.func_218657_a("LiquidsToBeTicked", ((ChunkPrimerTickList<?>) chunk.getFluidsToBeTicked()).write());
		}

		CompoundNBT heightMaps = new CompoundNBT();

		for (Heightmap.Type type : chunk.getHeightmaps()) {
			if (type.getUsage() == Heightmap.Usage.LIVE_WORLD) {
				heightMaps.put(type.getId(),
						new LongArrayNBT(chunk.getHeightmap(type).getDataArray()));
			}
		}

		compound.func_218657_a("Heightmaps", heightMaps);
		// TODO
		//compound.func_218657_a("Structures",
		//		this.func_202160_a(chunk.x, chunk.z, chunk.func_201609_c(), chunk.func_201604_d()));

		return compound;
	}

	protected abstract ListNBT getEntityList(Chunk chunk);
	protected abstract ListNBT getTileEntityList(Chunk chunk);

	/**
	 * Gets a count of how many chunks there are that still need to be written to
	 * disk. (Does not include any chunk that is currently being written to disk)
	 *
	 * @return The number of chunks that still need to be written to disk
	 */
	public synchronized int getNumPendingChunks() {
		return this.chunksToSave.size();
	}
}
