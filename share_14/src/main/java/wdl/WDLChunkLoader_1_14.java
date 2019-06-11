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
import java.util.Arrays;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.SerializableTickList;
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
import net.minecraft.world.chunk.storage.ChunkLoader;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.EndDimension;
import net.minecraft.world.dimension.NetherDimension;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.SessionLockException;
import wdl.versioned.VersionedFunctions;

/**
 * Alternative implementation of {@link ChunkLoader} that handles editing
 * WDL-specific properties of chunks as they are being saved.
 *
 * This variant is used for chunks from 1.13 and later.
 */
abstract class WDLChunkLoaderBase extends ChunkLoader {

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

		return new File(baseFolder, "region");
	}

	protected final WDL wdl;
	/**
	 * Location where chunks are saved.
	 *
	 * In this version, this directly is the region folder for the given dimension;
	 * for the overworld it is world/region and others it is world/DIM#/region.
	 */
	protected final File chunkSaveLocation;

	protected WDLChunkLoaderBase(WDL wdl, File file) {
		super(file, null);
		this.wdl = wdl;
		this.chunkSaveLocation = file;
	}

	/**
	 * Saves the given chunk.
	 *
	 * Note that while the normal implementation swallows Exceptions, this
	 * version does not.
	 */
	public synchronized void saveChunk(World world, IChunk chunk) throws SessionLockException, IOException {
		wdl.saveHandler.checkSessionLock();

		CompoundNBT levelTag = writeChunkToNBT((Chunk)chunk, world);

		CompoundNBT rootTag = new CompoundNBT();
		rootTag.func_218657_a("Level", levelTag);
		rootTag.putInt("DataVersion", VersionConstants.getDataVersion());

		func_219100_a(chunk.getPos(), rootTag);

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

		ChunkPos chunkpos = chunk.getPos();
		compound.putInt("xPos", chunkpos.x);
		compound.putInt("zPos", chunkpos.z);
		compound.putLong("LastUpdate", world.getGameTime());
		compound.putLong("InhabitedTime", chunk.getInhabitedTime());
		compound.putString("Status", ChunkStatus.field_222617_m/*FULL*/.func_222596_d()); // Make sure that the chunk is considered fully generated
		UpgradeData upgradedata = chunk.getUpgradeData();

		if (!upgradedata.isEmpty()) {
			compound.func_218657_a("UpgradeData", upgradedata.write());
		}

		ChunkSection[] chunkSections = chunk.getSections();
		ListNBT chunkSectionList = new ListNBT();
		WorldLightManager worldlightmanager = world.getChunkProvider().func_212863_j_();

		// XXX: VersionedFunctions.hasSkyLight is inapplicable here presumably, but it might still need to be used somehow
		for (int y = -1; y < 17; ++y) {
			final int f_y = y; // Compiler food
			ChunkSection chunkSection = Arrays.stream(chunkSections)
					.filter(section -> section != null && section.func_222632_g() >> 4 == f_y)
					.findFirst()
					.orElse(Chunk.EMPTY_SECTION);
			NibbleArray blocklightArray = worldlightmanager.func_215569_a(LightType.BLOCK)
					.func_215612_a(SectionPos.func_218156_a(chunkpos, y));
			NibbleArray skylightArray = worldlightmanager.func_215569_a(LightType.SKY)
					.func_215612_a(SectionPos.func_218156_a(chunkpos, y));
			if (chunkSection != Chunk.EMPTY_SECTION || blocklightArray != null || skylightArray != null) {
				CompoundNBT sectionNBT = new CompoundNBT();
				sectionNBT.putByte("Y", (byte) (y & 255));
				if (chunkSection != Chunk.EMPTY_SECTION) {
					chunkSection.getData().writeChunkPalette(sectionNBT, "Palette", "BlockStates");
				}

				if (blocklightArray != null && !blocklightArray.func_215655_c()) {
					sectionNBT.putByteArray("BlockLight", blocklightArray.getData());
				}

				if (skylightArray != null && !skylightArray.func_215655_c()) {
					sectionNBT.putByteArray("SkyLight", skylightArray.getData());
				}

				chunkSectionList.add(sectionNBT);
			}
		}

		compound.func_218657_a("Sections", chunkSectionList);

		if (chunk.func_217310_r()) {
			compound.putBoolean("isLightOn", true);
		}

		Biome[] biomes = chunk.getBiomes();
		int[] biomeData = biomes != null ? new int[biomes.length] : new int[0];
		if (biomes != null) {
			for (int j = 0; j < biomes.length; ++j) {
				biomeData[j] = VersionedFunctions.getBiomeId(biomes[j]);
			}
		}

		compound.putIntArray("Biomes", biomeData);

		chunk.setHasEntities(false);
		ListNBT entityList = getEntityList(chunk);
		compound.func_218657_a("Entities", entityList);

		ListNBT tileEntityList = getTileEntityList(chunk);
		compound.func_218657_a("TileEntities", tileEntityList);

		// XXX: Note: This was re-sorted on mojang's end; I've undone that.
		if (world.getPendingBlockTicks() instanceof ServerTickList) {
			compound.func_218657_a("TileTicks", ((ServerTickList<?>) world.getPendingBlockTicks()).func_219503_a(chunkpos));
		}
		if (world.getPendingFluidTicks() instanceof ServerTickList) {
			compound.func_218657_a("LiquidTicks", ((ServerTickList<?>) world.getPendingFluidTicks()).func_219503_a(chunkpos));
		}

		compound.func_218657_a("PostProcessing", listArrayToTag(chunk.getPackedPositions()));

		if (chunk.getBlocksToBeTicked() instanceof ChunkPrimerTickList) {
			compound.func_218657_a("ToBeTicked", ((ChunkPrimerTickList<?>) chunk.getBlocksToBeTicked()).write());
		}

		// XXX: These are new, and they might conflict with the other one.  Not sure which should be used.
		if (chunk.getBlocksToBeTicked() instanceof SerializableTickList) {
			compound.func_218657_a("TileTicks", ((SerializableTickList<?>) chunk.getBlocksToBeTicked())
					.func_219498_a(world.getGameTime()));
		}

		if (chunk.getFluidsToBeTicked() instanceof ChunkPrimerTickList) {
			compound.func_218657_a("LiquidsToBeTicked", ((ChunkPrimerTickList<?>) chunk.getFluidsToBeTicked()).write());
		}

		if (chunk.getFluidsToBeTicked() instanceof SerializableTickList) {
			compound.func_218657_a("LiquidTicks", ((SerializableTickList<?>) chunk.getFluidsToBeTicked())
					.func_219498_a(world.getGameTime()));
		}

		CompoundNBT heightMaps = new CompoundNBT();

		for (Entry<Heightmap.Type, Heightmap> entry : chunk.func_217311_f()) {
			if (chunk.getStatus().func_222595_h().contains(entry.getKey())) {
				heightMaps.func_218657_a(entry.getKey().getId(), new LongArrayNBT(entry.getValue().getDataArray()));
			}
		}

		compound.func_218657_a("Heightmaps", heightMaps);
		// TODO
		//compound.func_218657_a("Structures",
		//		func_222649_a(chunkpos, chunk.getStructureStarts(), chunk.getStructureReferences()));

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
		return this.field_219102_c.size(); // XXX This is actually the number of regions
	}

	private ListNBT listArrayToTag(ShortList[] list) {
		ListNBT listnbt = new ListNBT();

		for (ShortList shortlist : list) {
			ListNBT sublist = new ListNBT();

			if (shortlist != null) {
				for (Short val : shortlist) {
					sublist.add(new ShortNBT(val));
				}
			}

			listnbt.add(sublist);
		}

		return listnbt;
	}
}
