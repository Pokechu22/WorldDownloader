/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;

import java.io.DataInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.RegionFile;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.SaveHandler;
import wdl.api.IEntityEditor;
import wdl.api.ITileEntityEditor;
import wdl.api.ITileEntityEditor.TileEntityCreationMode;
import wdl.api.ITileEntityImportationIdentifier;
import wdl.api.WDLApi;
import wdl.api.WDLApi.ModInfo;
import wdl.versioned.VersionedFunctions;

/**
 * Alternative implementation of {@link AnvilChunkLoader} that handles editing
 * WDL-specific properties of chunks as they are being saved.
 *
 * Extends the class in either WDLChunkLoader12.java or WDLChunkLoader13.java,
 * depending on the Minecraft version.
 */
public class WDLChunkLoader extends WDLChunkLoaderBase {
	private static final Logger LOGGER = LogManager.getLogger();

	public static WDLChunkLoader create(WDL wdl,
			SaveHandler handler, Dimension dimension) {
		return new WDLChunkLoader(wdl, getWorldSaveFolder(handler, dimension));
	}

	public WDLChunkLoader(WDL wdl, File file) {
		super(wdl, file);
	}

	/**
	 * Creates an NBT list of all entities in this chunk, adding in custom entities.
	 * @param chunk
	 * @return
	 */
	@Override
	protected NBTTagList getEntityList(Chunk chunk) {
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
		for (Entity e : wdl.newEntities.get(chunk.getPos())) {
			assert chunk.getPos().equals(wdl.entityPositions.get(e.getUniqueID())) :
					"Mismatch between position of " + e + " in "
					+ chunk.getPos() + " and position recorded in entityPositions of "
					+ wdl.entityPositions.get(e.getUniqueID());
			// "Unkill" the entity, since it is killed when it is unloaded.
			e.removed = false;
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
				if (entity.writeUnlessPassenger(entityData)) {
					chunk.setHasEntities(true);
					entityList.add(entityData);
				}
			} catch (Exception e) {
				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.ERROR,
						"wdl.messages.generalError.failedToSaveEntity", entity, chunk.getPos().x, chunk.getPos().z, e);
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
	protected static boolean shouldSaveEntity(Entity e) {
		if (e instanceof EntityPlayer) {
			// Players shouldn't be saved, and it's dangerous to mess with them.
			return false;
		}

		if (!EntityUtils.isEntityEnabled(e)) {
			WDLMessages.chatMessageTranslated(
					WDL.serverProps,
					WDLMessageTypes.REMOVE_ENTITY,
					"wdl.messages.removeEntity.notSavingUserPreference", e);
			return false;
		}

		return true;
	}

	/**
	 * Creates an NBT list of all tile entities in this chunk, importing tile
	 * entities as needed.
	 */
	@Override
	protected NBTTagList getTileEntityList(Chunk chunk) {
		NBTTagList tileEntityList = new NBTTagList();

		if (!WDLPluginChannels.canSaveTileEntities(chunk)) {
			return tileEntityList;
		}

		Map<BlockPos, TileEntity> chunkTEMap = chunk.getTileEntityMap();
		Map<BlockPos, NBTTagCompound> oldTEMap = getOldTileEntities(chunk);
		Map<BlockPos, TileEntity> newTEMap = wdl.newTileEntities.get(chunk.getPos());
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
					te.write(compound);
				} catch (Exception e) {
					WDLMessages.chatMessageTranslated(
							WDL.serverProps,
							WDLMessageTypes.ERROR,
							"wdl.messages.generalError.failedToSaveTE", te, pos, chunk.getPos().x, chunk.getPos().z, e);
					LOGGER.warn("Compound: " + compound);
					continue;
				}

				String entityType = compound.getString("id") +
						" (" + te.getClass().getCanonicalName() +")";
				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.LOAD_TILE_ENTITY,
						"wdl.messages.tileEntity.usingNew", entityType, pos);

				editTileEntity(pos, compound, TileEntityCreationMode.NEW);

				tileEntityList.add(compound);
			} else if (oldTEMap.containsKey(pos)) {
				NBTTagCompound compound = oldTEMap.get(pos);
				String entityType = compound.getString("id");
				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.LOAD_TILE_ENTITY,
						"wdl.messages.tileEntity.usingOld", entityType, pos);

				editTileEntity(pos, compound, TileEntityCreationMode.IMPORTED);

				tileEntityList.add(compound);
			} else if (chunkTEMap.containsKey(pos)) {
				// TODO: Do we want a chat message for this?
				// It seems unnecessary.
				TileEntity te = chunkTEMap.get(pos);
				NBTTagCompound compound = new NBTTagCompound();
				try {
					te.write(compound);
				} catch (Exception e) {
					WDLMessages.chatMessageTranslated(
							WDL.serverProps,
							WDLMessageTypes.ERROR,
							"wdl.messages.generalError.failedToSaveTE", te, pos, chunk.getPos().x, chunk.getPos().z, e);
					LOGGER.warn("Compound: " + compound);
					continue;
				}

				editTileEntity(pos, compound, TileEntityCreationMode.EXISTING);

				tileEntityList.add(compound);
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
	 * {@link #shouldImportBlockEntity(String, BlockPos)} for details.
	 *
	 * @param chunk
	 *            The chunk that currently exists in that location
	 * @return A map of positions to tile entities.
	 */
	protected Map<BlockPos, NBTTagCompound> getOldTileEntities(Chunk chunk) {
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
					chunkSaveLocation, chunk.getPos().x, chunk.getPos().z)) {
				if (dis == null) {
					// This happens whenever the chunk hasn't been saved before.
					// It's a normal case.
					return returned;
				}

				chunkNBT = CompressedStreamTools.read(dis);
			}

			NBTTagCompound levelNBT = chunkNBT.getCompound("Level");
			NBTTagList oldList = levelNBT.getList("TileEntities", 10);

			if (oldList != null) {
				for (int i = 0; i < oldList.size(); i++) {
					NBTTagCompound oldNBT = oldList.getCompound(i);

					String entityID = oldNBT.getString("id");
					BlockPos pos = new BlockPos(oldNBT.getInt("x"),
							oldNBT.getInt("y"), oldNBT.getInt("z"));
					Block block = chunk.getBlockState(pos).getBlock();

					if (shouldImportBlockEntity(entityID, pos, block, oldNBT, chunk)) {
						returned.put(pos, oldNBT);
					} else {
						// Even if this tile entity is saved in another way
						// later, we still want the player to know we did not
						// import something in that chunk.
						WDLMessages.chatMessageTranslated(
								WDL.serverProps,
								WDLMessageTypes.LOAD_TILE_ENTITY,
								"wdl.messages.tileEntity.notImporting", entityID, pos);
					}
				}
			}
		} catch (Exception e) {
			WDLMessages.chatMessageTranslated(WDL.serverProps,
					WDLMessageTypes.ERROR,
					"wdl.messages.generalError.failedToImportTE", chunk.getPos().x, chunk.getPos().z, e);
		}
		return returned;
	}

	/**
	 * Checks if the block entity should be imported. Only "problematic" (IE,
	 * those that require manual interaction such as chests) block entities will
	 * be imported. Additionally, the block at the block entity's coordinates
	 * must be one that would normally be used with that block entity.
	 *
	 * @param entityID
	 *            The block entity's ID, as found in the 'id' tag.
	 * @param pos
	 *            The location of the block entity, as created by its 'x', 'y',
	 *            and 'z' tags.
	 * @param block
	 *            The block in the current world at the given position.
	 * @param blockEntityNBT
	 *            The full NBT tag of the existing block entity. May be used if
	 *            further identification is needed.
	 * @param chunk
	 *            The (current) chunk for which entities are being imported. May be used
	 *            if further identification is needed (e.g. nearby blocks).
	 * @return <code>true</code> if that block entity should be imported.
	 */
	protected boolean shouldImportBlockEntity(String entityID, BlockPos pos,
			Block block, NBTTagCompound blockEntityNBT, Chunk chunk) {
		if (VersionedFunctions.shouldImportBlockEntity(entityID, pos, block, blockEntityNBT, chunk)) {
			return true;
		}

		for (ModInfo<ITileEntityImportationIdentifier> info : WDLApi
				.getImplementingExtensions(ITileEntityImportationIdentifier.class)) {
			if (info.mod.shouldImportTileEntity(entityID, pos, block,
					blockEntityNBT, chunk)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Applies all registered {@link ITileEntityEditor}s to the given tile entity.
	 */
	protected static void editTileEntity(BlockPos pos, NBTTagCompound compound,
			TileEntityCreationMode creationMode) {
		for (ModInfo<ITileEntityEditor> info : WDLApi
				.getImplementingExtensions(ITileEntityEditor.class)) {
			try {
				if (info.mod.shouldEdit(pos, compound, creationMode)) {
					info.mod.editTileEntity(pos, compound, creationMode);

					WDLMessages.chatMessageTranslated(
							WDL.serverProps,
							WDLMessageTypes.LOAD_TILE_ENTITY,
							"wdl.messages.tileEntity.edited", pos, info.getDisplayName());
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

	public RegionFile getRegionFileIfExists(int regionX, int regionZ) {
		// 1.13.0 and earlier have func_191065_b or getRegionFileIfExists, but 1.13 doesn't,
		// so we get this...
		File regionFolder = new File(this.chunkSaveLocation, "region");
		File region = new File(regionFolder, "r." + regionX + "." + regionZ + ".mca");
		if (region.exists()) {
			// This takes chunk coordinates and shifts them back into region ones, so convert
			// our region coordinates to chunk ones to appease it
			return RegionFileCache.createOrLoadRegionFile(this.chunkSaveLocation, regionX << 5, regionZ << 5);
		} else {
			return null;
		}
	}
}