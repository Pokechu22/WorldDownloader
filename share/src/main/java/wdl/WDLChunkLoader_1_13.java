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
package wdl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
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
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.EndDimension;
import net.minecraft.world.dimension.NetherDimension;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.SessionLockException;
import wdl.versioned.VersionedFunctions;

/**
 * Alternative implementation of {@link AnvilChunkLoader} that handles editing
 * WDL-specific properties of chunks as they are being saved.
 *
 * This variant is used for chunks from 1.13 and later.
 */
abstract class WDLChunkLoaderBase extends AnvilChunkLoader {

	/**
	 * The class that is used for the {@linkplain AnvilChunkLoader#chunksToSave} field.
	 */
	@SuppressWarnings("rawtypes")
	static final Class<Object2ObjectMap> CHUNKS_TO_SAVE_CLASS = Object2ObjectMap.class;

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

	protected final Map<ChunkPos, NBTTagCompound> chunksToSave;
	protected final File chunkSaveLocation;

	protected WDLChunkLoaderBase(File file) {
		super(file, null);
		@SuppressWarnings("unchecked")
		Object2ObjectMap<ChunkPos, NBTTagCompound> chunksToSave =
				ReflectionUtils.findAndGetPrivateField(this, AnvilChunkLoader.class, CHUNKS_TO_SAVE_CLASS);
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

		NBTTagCompound levelTag = writeChunkToNBT((Chunk)chunk, world);

		NBTTagCompound rootTag = new NBTTagCompound();
		rootTag.setTag("Level", levelTag);
		rootTag.setInt("DataVersion", VersionConstants.getDataVersion());

		addChunkToPending(chunk.getPos(), rootTag);

		WDL.unloadChunk(chunk.getPos());
	}

	/**
	 * Writes the given chunk, creating an NBT compound tag.
	 *
	 * Note that this does <b>not</b> override the private method
	 * {@link AnvilChunkLoader#writeChunkToNBT(Chunk, World, NBTCompoundTag)}.
	 * That method is private and cannot be overridden; plus, this version
	 * returns a tag rather than modifying the one passed as an argument.
	 *
	 * @param chunk
	 *            The chunk to write
	 * @param world
	 *            The world the chunk is in, used to determine the modified
	 *            time.
	 * @return A new NBTTagCompound
	 */
	private NBTTagCompound writeChunkToNBT(Chunk chunk, World world) {
		NBTTagCompound compound = new NBTTagCompound();

		compound.setInt("xPos", chunk.x);
		compound.setInt("zPos", chunk.z);
		compound.setLong("LastUpdate", world.getGameTime());
		compound.setLong("InhabitedTime", chunk.getInhabitedTime());
		compound.setString("Status", ChunkStatus.POSTPROCESSED.getName()); // Make sure that the chunk is considered fully generated
		UpgradeData upgradedata = chunk.getUpgradeData();

		if (!upgradedata.isEmpty()) {
			compound.setTag("UpgradeData", upgradedata.write());
		}

		ChunkSection[] chunkSections = chunk.getSections();
		NBTTagList chunkSectionList = new NBTTagList();
		boolean hasSky = VersionedFunctions.hasSkyLight(world);

		for (ChunkSection chunkSection : chunkSections) {
			if (chunkSection != Chunk.EMPTY_SECTION) {
				NBTTagCompound sectionNBT = new NBTTagCompound();
				sectionNBT.setByte("Y",
						(byte) (chunkSection.getYLocation() >> 4 & 255));
				chunkSection.getData().writeChunkPalette(sectionNBT, "Palette", "BlockStates");

				NibbleArray blocklightArray = chunkSection.getBlockLight();
				int lightArrayLen = blocklightArray.getData().length;
				sectionNBT.setByteArray("BlockLight", blocklightArray.getData());

				if (hasSky) {
					NibbleArray skylightArray = chunkSection.getSkyLight();
					if (skylightArray != null) {
						sectionNBT.setByteArray("SkyLight", chunkSection.getSkyLight().getData());
					} else {
						// Shouldn't happen, but if it does, handle it smoothly.
						LOGGER.error("[WDL] Skylight array for chunk at " +
								chunk.x + ", " + chunk.z +
								" is null despite VersionedProperties " +
								"saying it shouldn't be!");
						sectionNBT.setByteArray("SkyLight", new byte[lightArrayLen]);
					}
				} else {
					sectionNBT.setByteArray("SkyLight", new byte[chunkSection.getBlockLight().getData().length]);
				}

				chunkSectionList.add((INBTBase) sectionNBT);
			}
		}

		compound.setTag("Sections", chunkSectionList);

		Biome[] biomes = chunk.getBiomes();
		int[] biomeData = new int[biomes.length];
		for (int i = 0; i < biomes.length; ++i) {
			biomeData[i] = Biome.REGISTRY.getId(biomes[i]);
		}

		compound.setIntArray("Biomes", biomeData);

		chunk.setHasEntities(false);
		NBTTagList entityList = getEntityList(chunk);
		compound.setTag("Entities", entityList);

		NBTTagList tileEntityList = getTileEntityList(chunk);
		compound.setTag("TileEntities", tileEntityList);

		if (world.getPendingBlockTicks() instanceof ServerTickList) {
			compound.setTag("TileTicks", ((ServerTickList<?>) world.getPendingBlockTicks()).write(chunk));
		}
		if (world.getPendingFluidTicks() instanceof ServerTickList) {
			compound.setTag("LiquidTicks", ((ServerTickList<?>) world.getPendingFluidTicks()).write(chunk));
		}

		compound.setTag("PostProcessing", listArrayToTag(chunk.getPackedPositions()));

		if (chunk.getBlocksToBeTicked() instanceof ChunkPrimerTickList) {
			compound.setTag("ToBeTicked", ((ChunkPrimerTickList<?>) chunk.getBlocksToBeTicked()).write());
		}
		if (chunk.getFluidsToBeTicked() instanceof ChunkPrimerTickList) {
			compound.setTag("LiquidsToBeTicked", ((ChunkPrimerTickList<?>) chunk.getFluidsToBeTicked()).write());
		}

		NBTTagCompound heightMaps = new NBTTagCompound();

		for (Heightmap.Type type : chunk.getHeightmaps()) {
			if (type.getUsage() == Heightmap.Usage.LIVE_WORLD) {
				heightMaps.setTag(type.getId(),
						new NBTTagLongArray(chunk.getHeightmap(type).getDataArray()));
			}
		}

		compound.setTag("Heightmaps", heightMaps);
		// TODO
		//compound.setTag("Structures",
		//		this.func_202160_a(chunk.x, chunk.z, chunk.func_201609_c(), chunk.func_201604_d()));

		return compound;
	}

	protected abstract NBTTagList getEntityList(Chunk chunk);
	protected abstract NBTTagList getTileEntityList(Chunk chunk);

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
