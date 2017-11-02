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

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBeacon;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDropper;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockNote;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.storage.SaveHandler;

import wdl.api.IEntityEditor;
import wdl.api.ITileEntityEditor;
import wdl.api.ITileEntityEditor.TileEntityCreationMode;
import wdl.api.ITileEntityImportationIdentifier;
import wdl.api.WDLApi;
import wdl.api.WDLApi.ModInfo;

/**
 * Alternative implementation of {@link AnvilChunkLoader} that handles editing
 * WDL-specific properties of chunks as they are being saved.
 */
public class WDLChunkLoader extends AnvilChunkLoader {
	private static final Logger LOGGER = LogManager.getLogger();

	public static WDLChunkLoader create(SaveHandler handler,
			WorldProvider provider) {
		return new WDLChunkLoader(getWorldSaveFolder(handler, provider));
	}

	/**
	 * Gets the save folder for the given WorldProvider, respecting Forge's
	 * dimension names if forge is present.
	 */
	private static File getWorldSaveFolder(SaveHandler handler,
			WorldProvider provider) {
		File baseFolder = handler.getWorldDirectory();

		// Based off of AnvilSaveHandler.getChunkLoader, but also accounts
		// for forge changes.
		try {
			// See forge changes here:
			// https://github.com/MinecraftForge/MinecraftForge/blob/250a77b35936e7ac68006dfd28a9e93c6def9128/patches/minecraft/net/minecraft/world/WorldProvider.java.patch#L85-L93
			// https://github.com/MinecraftForge/MinecraftForge/blob/250a77b35936e7ac68006dfd28a9e93c6def9128/patches/minecraft/net/minecraft/world/chunk/storage/AnvilSaveHandler.java.patch
			Method forgeGetSaveFolderMethod = provider.getClass().getMethod(
					"getSaveFolder");

			String name = (String) forgeGetSaveFolderMethod.invoke(provider);
			if (name != null) {
				File file = new File(baseFolder, name);
				file.mkdirs();
				return file;
			}
			return baseFolder;
		} catch (Exception e) {
			// Not a forge setup - emulate the vanilla method in
			// AnvilSaveHandler.getChunkLoader.

			if (provider instanceof WorldProviderHell) {
				File file = new File(baseFolder, "DIM-1");
				file.mkdirs();
				return file;
			} else if (provider instanceof WorldProviderEnd) {
				File file = new File(baseFolder, "DIM1");
				file.mkdirs();
				return file;
			}

			return baseFolder;
		}
	}

	private final Map<ChunkPos, NBTTagCompound> chunksToSave;
	private final File chunkSaveLocation;

	public WDLChunkLoader(File file) {
		super(file, null);
		@SuppressWarnings("unchecked")
		Map<ChunkPos, NBTTagCompound> chunksToSave = (Map<ChunkPos, NBTTagCompound>)
				ReflectionUtils.findAndGetPrivateField(this, AnvilChunkLoader.class, Map.class);
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
	public void saveChunk(World world, Chunk chunk) throws MinecraftException,
	IOException {
		world.checkSessionLock();

		NBTTagCompound levelTag = writeChunkToNBT(chunk, world);

		NBTTagCompound rootTag = new NBTTagCompound();
		rootTag.setTag("Level", levelTag);
		rootTag.setInteger("DataVersion", VersionConstants.getDataVersion());

		addChunkToPending(chunk.getPos(), rootTag);
	}

	/**
	 * Writes the given chunk, creating an NBT compound tag.
	 *
	 * Note that this does <b>not</b> override the private method
	 * {@link AnvilChunkLoader#writeChunkToNBT(Chunk, World, NBTCompoundTag)}.
	 * That method is private and cannot be overridden; plus, this version
	 * returns a tag rather than modifying the one passed as an argument.
	 * However, that method
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

		compound.setByte("V", (byte) 1);
		compound.setInteger("xPos", chunk.x);
		compound.setInteger("zPos", chunk.z);
		compound.setLong("LastUpdate", world.getTotalWorldTime());
		compound.setIntArray("HeightMap", chunk.getHeightMap());
		compound.setBoolean("TerrainPopulated", true);  // We always want this
		compound.setBoolean("LightPopulated", chunk.isLightPopulated());
		compound.setLong("InhabitedTime", chunk.getInhabitedTime());
		ExtendedBlockStorage[] blockStorageArray = chunk.getBlockStorageArray();
		NBTTagList blockStorageList = new NBTTagList();
		boolean hasSky = VersionedProperties.hasSkyLight(world);

		for (ExtendedBlockStorage blockStorage : blockStorageArray) {
			if (blockStorage != null) {
				NBTTagCompound blockData = new NBTTagCompound();
				blockData.setByte("Y",
						(byte) (blockStorage.getYLocation() >> 4 & 255));
				byte[] buffer = new byte[4096];
				NibbleArray nibblearray = new NibbleArray();
				NibbleArray nibblearray1 = blockStorage.getData()
						.getDataForNBT(buffer, nibblearray);
				blockData.setByteArray("Blocks", buffer);
				blockData.setByteArray("Data", nibblearray.getData());

				if (nibblearray1 != null) {
					blockData.setByteArray("Add", nibblearray1.getData());
				}

				NibbleArray blocklightArray = blockStorage.getBlockLight();
				int lightArrayLen = blocklightArray.getData().length;
				blockData.setByteArray("BlockLight", blocklightArray.getData());

				if (hasSky) {
					NibbleArray skylightArray = blockStorage.getSkyLight();
					if (skylightArray != null) {
						blockData.setByteArray("SkyLight", skylightArray.getData());
					} else {
						// Shouldn't happen, but if it does, handle it smoothly.
						LOGGER.error("[WDL] Skylight array for chunk at " +
								chunk.x + ", " + chunk.z +
								" is null despite VersionedProperties " +
								"saying it shouldn't be!");
						blockData.setByteArray("SkyLight", new byte[lightArrayLen]);
					}
				} else {
					blockData.setByteArray("SkyLight", new byte[lightArrayLen]);
				}

				blockStorageList.appendTag(blockData);
			}
		}

		compound.setTag("Sections", blockStorageList);
		compound.setByteArray("Biomes", chunk.getBiomeArray());
		chunk.setHasEntities(false);

		NBTTagList entityList = getEntityList(chunk);
		compound.setTag("Entities", entityList);

		NBTTagList tileEntityList = getTileEntityList(chunk);
		compound.setTag("TileEntities", tileEntityList);
		List<NextTickListEntry> updateList = world.getPendingBlockUpdates(
				chunk, false);

		if (updateList != null) {
			long worldTime = world.getTotalWorldTime();
			NBTTagList entries = new NBTTagList();

			for (NextTickListEntry entry : updateList) {
				NBTTagCompound entryTag = new NBTTagCompound();
				ResourceLocation location = Block.REGISTRY
						.getNameForObject(entry.getBlock());
				entryTag.setString("i",
						location == null ? "" : location.toString());
				entryTag.setInteger("x", entry.position.getX());
				entryTag.setInteger("y", entry.position.getY());
				entryTag.setInteger("z", entry.position.getZ());
				entryTag.setInteger("t",
						(int) (entry.scheduledTime - worldTime));
				entryTag.setInteger("p", entry.priority);
				entries.appendTag(entryTag);
			}

			compound.setTag("TileTicks", entries);
		}

		return compound;
	}

	/**
	 * Creates an NBT list of all entities in this chunk, adding in custom entities.
	 * @param chunk
	 * @return
	 */
	public NBTTagList getEntityList(Chunk chunk) {
		NBTTagList entityList = new NBTTagList();

		if (!WDLPluginChannels.canSaveEntities(chunk)) {
			return entityList;
		}

		// Build a list of all entities in the chunk.
		List<Entity> entities = new ArrayList<>();
		// Add the entities already in the chunk.
		for (ClassInheritanceMultiMap<Entity> map : chunk.getEntityLists()) {
			entities.addAll(map);
		}
		// Add the manually saved entities.
		for (Entity e : WDL.newEntities.get(chunk.getPos())) {
			// "Unkill" the entity, since it is killed when it is unloaded.
			e.isDead = false;
			entities.add(e);
		}

		for (Entity entity : entities) {
			if (entity == null) {
				LOGGER.warn("[WDL] Null entity in chunk at "
						+ chunk.getPos());
				continue;
			}

			if (!shouldSaveEntity(entity)) {
				continue;
			}

			// Apply any editors.
			for (ModInfo<IEntityEditor> info : WDLApi
					.getImplementingExtensions(IEntityEditor.class)) {
				try {
					if (info.mod.shouldEdit(entity)) {
						info.mod.editEntity(entity);
					}
				} catch (Exception ex) {
					throw new RuntimeException("Failed to edit entity "
							+ entity + " for chunk at "
							+ chunk.getPos() + " with extension "
							+ info, ex);
				}
			}

			NBTTagCompound entityData = new NBTTagCompound();

			try {
				if (entity.writeToNBTOptional(entityData)) {
					chunk.setHasEntities(true);
					entityList.appendTag(entityData);
				}
			} catch (Exception e) {
				WDLMessages.chatMessageTranslated(
						WDLMessageTypes.ERROR,
						"wdl.messages.generalError.failedToSaveEntity",
						entity, chunk.x, chunk.z, e);
				LOGGER.warn("Compound: " + entityData);
				LOGGER.warn("Entity metadata dump:");
				try {
					List<EntityDataManager.DataEntry<?>> objects = entity
							.getDataManager().getAll();
					if (objects == null) {
						LOGGER.warn("No entries (getAllWatched() returned null)");
					} else {
						LOGGER.warn(objects);
						for (EntityDataManager.DataEntry<?> obj : objects) {
							if (obj != null) {
								LOGGER.warn("DataEntry [getValue()="
										+ obj.getValue()
										+ ", isDirty()="
										+ obj.isDirty()
										+ ", getKey()="
										+ "DataParameter ["
										+ "getId()="
										+ obj.getKey().getId()
										+ ", getSerializer()="
										+ obj.getKey().getSerializer() + "]]");
							}
						}
					}
				} catch (Exception e2) {
					LOGGER.warn("Failed to complete dump: ", e);
				}
				LOGGER.warn("End entity metadata dump");
				continue;
			}
		}

		return entityList;
	}

	/**
	 * Checks if the given entity should be saved, putting a message into the
	 * chat if it can't.
	 *
	 * @param e
	 *            The entity to check
	 * @return True if the entity should be saved.
	 */
	public static boolean shouldSaveEntity(Entity e) {
		if (e instanceof EntityPlayer) {
			// Players shouldn't be saved, and it's dangerous to mess with them.
			return false;
		}

		if (!EntityUtils.isEntityEnabled(e)) {
			WDLMessages.chatMessageTranslated(
					WDLMessageTypes.REMOVE_ENTITY,
					"wdl.messages.removeEntity.notSavingUserPreference",
					e);
			return false;
		}

		return true;
	}

	/**
	 * Creates an NBT list of all tile entities in this chunk, importing tile
	 * entities as needed.
	 */
	public NBTTagList getTileEntityList(Chunk chunk) {
		NBTTagList tileEntityList = new NBTTagList();

		if (!WDLPluginChannels.canSaveTileEntities(chunk)) {
			return tileEntityList;
		}

		Map<BlockPos, TileEntity> chunkTEMap = chunk.getTileEntityMap();
		Map<BlockPos, NBTTagCompound> oldTEMap = getOldTileEntities(chunk);
		Map<BlockPos, TileEntity> newTEMap = WDL.newTileEntities.get(chunk.getPos());
		if (newTEMap == null) {
			newTEMap = new HashMap<>();
		}

		// All of the locations of tile entities in the chunk.
		Set<BlockPos> allTELocations = new HashSet<>();
		allTELocations.addAll(chunkTEMap.keySet());
		allTELocations.addAll(oldTEMap.keySet());
		allTELocations.addAll(newTEMap.keySet());

		for (BlockPos pos : allTELocations) {
			// Now, add all of the tile entities, using the "best" map
			// if it's in multiple.
			if (newTEMap.containsKey(pos)) {
				NBTTagCompound compound = new NBTTagCompound();

				TileEntity te = newTEMap.get(pos);
				try {
					te.writeToNBT(compound);
				} catch (Exception e) {
					WDLMessages.chatMessageTranslated(
							WDLMessageTypes.ERROR,
							"wdl.messages.generalError.failedToSaveTE",
							te, pos, chunk.x, chunk.z, e);
					LOGGER.warn("Compound: " + compound);
					continue;
				}

				String entityType = compound.getString("id") +
						" (" + te.getClass().getCanonicalName() +")";
				WDLMessages.chatMessageTranslated(
						WDLMessageTypes.LOAD_TILE_ENTITY,
						"wdl.messages.tileEntity.usingNew",
						entityType, pos);

				editTileEntity(pos, compound, TileEntityCreationMode.NEW);

				tileEntityList.appendTag(compound);
			} else if (oldTEMap.containsKey(pos)) {
				NBTTagCompound compound = oldTEMap.get(pos);
				String entityType = compound.getString("id");
				WDLMessages.chatMessageTranslated(
						WDLMessageTypes.LOAD_TILE_ENTITY,
						"wdl.messages.tileEntity.usingOld",
						entityType, pos);

				editTileEntity(pos, compound, TileEntityCreationMode.IMPORTED);

				tileEntityList.appendTag(compound);
			} else if (chunkTEMap.containsKey(pos)) {
				// TODO: Do we want a chat message for this?
				// It seems unnecessary.
				TileEntity te = chunkTEMap.get(pos);
				NBTTagCompound compound = new NBTTagCompound();
				try {
					te.writeToNBT(compound);
				} catch (Exception e) {
					WDLMessages.chatMessageTranslated(
							WDLMessageTypes.ERROR,
							"wdl.messages.generalError.failedToSaveTE",
							te, pos, chunk.x, chunk.z, e);
					LOGGER.warn("Compound: " + compound);
					continue;
				}

				editTileEntity(pos, compound, TileEntityCreationMode.EXISTING);

				tileEntityList.appendTag(compound);
			}
		}

		return tileEntityList;
	}

	/**
	 * Gets a map of all tile entities in the previous version of that chunk.
	 * Only "problematic" tile entities (those that require manual opening) will
	 * be imported, and the tile entity must be in the correct position (IE, the
	 * block at the tile entity's position must match the block normally used
	 * with that tile entity). See
	 * {@link #shouldImportTileEntity(String, BlockPos)} for details.
	 *
	 * @param chunk
	 *            The chunk to import tile entities from.
	 * @return A map of positions to tile entities.
	 */
	public Map<BlockPos, NBTTagCompound> getOldTileEntities(Chunk chunk) {
		Map<BlockPos, NBTTagCompound> returned = new HashMap<>();

		try {
			NBTTagCompound chunkNBT;

			// The reason for the weird syntax here rather than containsKey is because
			// chunksToSave can be accessed from multiple threads.  Note that this still
			// doesn't handle the MC-119971-like case of the chunk being in chunksBeingSaved
			// (but that should be rare, and this condition should not happen in the first place)
			if ((chunkNBT = chunksToSave.get(chunk.getPos())) != null) {
				LOGGER.warn("getOldTileEntities (and thus saveChunk) was called while a chunk was already in chunksToSave!  (location: {})", chunk.getPos(), new Exception());
			} else try (DataInputStream dis = RegionFileCache.getChunkInputStream(
					chunkSaveLocation, chunk.x, chunk.z)) {
				if (dis == null) {
					// This happens whenever the chunk hasn't been saved before.
					// It's a normal case.
					return returned;
				}

				chunkNBT = CompressedStreamTools.read(dis);
			}

			NBTTagCompound levelNBT = chunkNBT.getCompoundTag("Level");
			NBTTagList oldList = levelNBT.getTagList("TileEntities", 10);

			if (oldList != null) {
				for (int i = 0; i < oldList.tagCount(); i++) {
					NBTTagCompound oldNBT = oldList.getCompoundTagAt(i);

					String entityID = oldNBT.getString("id");
					BlockPos pos = new BlockPos(oldNBT.getInteger("x"),
							oldNBT.getInteger("y"), oldNBT.getInteger("z"));
					Block block = chunk.getBlockState(pos).getBlock();

					if (shouldImportTileEntity(entityID, pos, block, oldNBT, chunk)) {
						returned.put(pos, oldNBT);
					} else {
						// Even if this tile entity is saved in another way
						// later, we still want the player to know we did not
						// import something in that chunk.
						WDLMessages.chatMessageTranslated(
								WDLMessageTypes.LOAD_TILE_ENTITY,
								"wdl.messages.tileEntity.notImporting",
								entityID, pos);
					}
				}
			}
		} catch (Exception e) {
			WDLMessages.chatMessageTranslated(WDLMessageTypes.ERROR,
					"wdl.messages.generalError.failedToImportTE",
					chunk.x, chunk.z, e);
		}
		return returned;
	}

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
			Block block, NBTTagCompound tileEntityNBT, Chunk chunk) {
		if (block instanceof BlockChest && entityID.equals(VersionedProperties.getBlockEntityID(TileEntityChest.class))) {
			return true;
		} else if (block instanceof BlockDispenser && entityID.equals(VersionedProperties.getBlockEntityID(TileEntityDispenser.class))) {
			return true;
		} else if (block instanceof BlockDropper && entityID.equals(VersionedProperties.getBlockEntityID(TileEntityDropper.class))) {
			return true;
		} else if (block instanceof BlockFurnace && entityID.equals(VersionedProperties.getBlockEntityID(TileEntityFurnace.class))) {
			return true;
		} else if (block instanceof BlockNote && entityID.equals(VersionedProperties.getBlockEntityID(TileEntityNote.class))) {
			return true;
		} else if (block instanceof BlockBrewingStand && entityID.equals(VersionedProperties.getBlockEntityID(TileEntityBrewingStand.class))) {
			return true;
		} else if (block instanceof BlockHopper && entityID.equals(VersionedProperties.getBlockEntityID(TileEntityHopper.class))) {
			return true;
		} else if (block instanceof BlockBeacon && entityID.equals(VersionedProperties.getBlockEntityID(TileEntityBeacon.class))) {
			return true;
		} else if (VersionedProperties.isImportableShulkerBox(entityID, block)) {
			return true;
		}

		for (ModInfo<ITileEntityImportationIdentifier> info : WDLApi
				.getImplementingExtensions(ITileEntityImportationIdentifier.class)) {
			if (info.mod.shouldImportTileEntity(entityID, pos, block,
					tileEntityNBT, chunk)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Applies all registered {@link ITileEntityEditor}s to the given tile entity.
	 */
	public static void editTileEntity(BlockPos pos, NBTTagCompound compound,
			TileEntityCreationMode creationMode) {
		for (ModInfo<ITileEntityEditor> info : WDLApi
				.getImplementingExtensions(ITileEntityEditor.class)) {
			try {
				if (info.mod.shouldEdit(pos, compound, creationMode)) {
					info.mod.editTileEntity(pos, compound, creationMode);

					WDLMessages.chatMessageTranslated(
							WDLMessageTypes.LOAD_TILE_ENTITY,
							"wdl.messages.tileEntity.edited",
							pos, info.getDisplayName());
				}
			} catch (Exception ex) {
				throw new RuntimeException("Failed to edit tile entity at "
						+ pos + " with extension " + info
						+ "; NBT is now " + compound + " (this may be the "
						+ "initial value, an edited value, or a partially "
						+ "edited value)", ex);
			}
		}
	}

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
