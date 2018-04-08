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

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.inventory.Slot;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import wdl.api.IWorldLoadListener;
import wdl.api.WDLApi;
import wdl.api.WDLApi.ModInfo;
import wdl.handler.HandlerException;
import wdl.handler.block.BlockHandler;
import wdl.handler.entity.EntityHandler;
import wdl.update.WDLUpdateChecker;

/**
 * Handles all of the events for WDL.
 *
 * These should be called regardless of whether downloading is
 * active; they handle that logic themselves.
 * <br/>
 * The difference between this class and {@link WDLHooks} is that WDLEvents
 * should be called directly from the source and does a bit of processing, while
 */
public class WDLEvents {
	/** @see WDLHooks#ENABLE_PROFILER */
	private static final boolean ENABLE_PROFILER = WDLHooks.ENABLE_PROFILER;
	private static final Profiler PROFILER = ENABLE_PROFILER ? Minecraft.getMinecraft().mcProfiler : null;

	/**
	 * Must be called after the static World object in Minecraft has been
	 * replaced.
	 */
	public static void onWorldLoad(WorldClient world) {
		if (ENABLE_PROFILER) PROFILER.startSection("Core");

		if (WDL.minecraft.isIntegratedServerRunning()) {
			// Don't do anything else in single player

			if (ENABLE_PROFILER) PROFILER.endSection();  // "Core"
			return;
		}

		// If already downloading
		if (WDL.downloading) {
			// If not currently saving, stop the current download and start
			// saving now
			if (!WDL.saving) {
				WDLMessages.chatMessageTranslated(WDLMessageTypes.INFO,
						"wdl.messages.generalInfo.worldChanged");
				WDL.worldLoadingDeferred = true;
				WDL.startSaveThread();
			}

			if (ENABLE_PROFILER) PROFILER.endSection();  // "Core"
			return;
		}

		boolean sameServer = WDL.loadWorld();

		WDLUpdateChecker.startIfNeeded();  // TODO: Always check for updates, even in single player

		if (ENABLE_PROFILER) PROFILER.endSection();  // "Core"

		for (ModInfo<IWorldLoadListener> info : WDLApi
				.getImplementingExtensions(IWorldLoadListener.class)) {
			if (ENABLE_PROFILER) PROFILER.startSection(info.id);
			info.mod.onWorldLoad(world, sameServer);
			if (ENABLE_PROFILER) PROFILER.endSection();  // info.id
		}
	}

	/**
	 * Must be called when a chunk is no longer needed and is about to be removed.
	 */
	public static void onChunkNoLongerNeeded(Chunk unneededChunk) {
		if (!WDL.downloading) { return; }

		if (unneededChunk == null) {
			return;
		}

		if (WDLPluginChannels.canSaveChunk(unneededChunk)) {
			WDLMessages.chatMessageTranslated(
					WDLMessageTypes.ON_CHUNK_NO_LONGER_NEEDED,
					"wdl.messages.onChunkNoLongerNeeded.saved",
					unneededChunk.x, unneededChunk.z);
			WDL.saveChunk(unneededChunk);
		} else {
			WDLMessages.chatMessageTranslated(
					WDLMessageTypes.ON_CHUNK_NO_LONGER_NEEDED,
					"wdl.messages.onChunkNoLongerNeeded.didNotSave",
					unneededChunk.x, unneededChunk.z);
		}
	}

	/**
	 * Must be called when a GUI that receives item stacks from the server is
	 * shown.
	 */
	public static void onItemGuiOpened() {
		if (!WDL.downloading) { return; }

		if (WDL.minecraft.objectMouseOver == null) {
			return;
		}

		if (WDL.minecraft.objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY) {
			WDL.lastEntity = WDL.minecraft.objectMouseOver.entityHit;
		} else {
			WDL.lastEntity = null;
			WDL.lastClickedBlock = WDL.minecraft.objectMouseOver.getBlockPos();
		}
	}

	/**
	 * Must be called when a GUI that triggered an onItemGuiOpened is no longer
	 * shown.
	 */
	public static boolean onItemGuiClosed() {
		if (!WDL.downloading) { return true; }

		Container windowContainer = WDL.windowContainer;

		if (windowContainer == null ||
				ReflectionUtils.isCreativeContainer(windowContainer.getClass())) {
			// Can't do anything with null containers or the creative inventory
			return true;
		}

		Entity ridingEntity = WDL.thePlayer.getRidingEntity();
		if (ridingEntity != null) {
			// Check for ridden entities.  See EntityHandler.checkRiding for
			// more info about why this is useful.
			EntityHandler<?, ?> handler = EntityHandler.getHandler(ridingEntity.getClass(), windowContainer.getClass());
			if (handler != null) {
				if (handler.checkRidingCasting(windowContainer, ridingEntity)) {
					if (!WDLPluginChannels.canSaveEntities(
							ridingEntity.chunkCoordX,
							ridingEntity.chunkCoordZ)) {
						// Run this check now that we've confirmed that we're saving
						// the entity being ridden. If we're riding a pig but opening
						// a chest in another chunk, that should go to the other check.
						WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_GUI_CLOSED_INFO,
								"wdl.messages.onGuiClosedInfo.cannotSaveEntities");
						return true;
					}

					try {
						String msg = handler.copyDataCasting(windowContainer, ridingEntity, true);
						WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_GUI_CLOSED_INFO, msg);
						return true;
					} catch (HandlerException e) {
						WDLMessages.chatMessageTranslated(e.messageType, e.translationKey, e.args);
						return false;
					}
				}
			} else {
				// A null handler is perfectly normal -- consider a player
				// riding a pig and then opening a chest
			}
		}

		// If the last thing clicked was an ENTITY
		Entity entity = WDL.lastEntity;
		if (entity != null) {
			if (!WDLPluginChannels.canSaveEntities(entity.chunkCoordX, entity.chunkCoordZ)) {
				WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_GUI_CLOSED_INFO,
						"wdl.messages.onGuiClosedInfo.cannotSaveEntities");
				return true;
			}

			EntityHandler<?, ?> handler = EntityHandler.getHandler(entity.getClass(), windowContainer.getClass());
			if (handler != null) {
				try {
					String msg = handler.copyDataCasting(windowContainer, entity, true);
					WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_GUI_CLOSED_INFO, msg);
					return true;
				} catch (HandlerException e) {
					WDLMessages.chatMessageTranslated(e.messageType, e.translationKey, e.args);
					return false;
				}
			} else {
				return false;
			}
		}

		// Else, the last thing clicked was a TILE ENTITY

		// Get the tile entity which we are going to update the inventory for
		TileEntity te = WDL.worldClient.getTileEntity(WDL.lastClickedBlock);

		if (te == null) {
			//TODO: Is this a good way to stop?  Is the event truely handled here?
			WDLMessages.chatMessageTranslated(
					WDLMessageTypes.ON_GUI_CLOSED_WARNING,
					"wdl.messages.onGuiClosedWarning.couldNotGetTE",
					WDL.lastClickedBlock);
			return true;
		}

		//Permissions check.
		if (!WDLPluginChannels.canSaveContainers(te.getPos().getX() >> 4, te
				.getPos().getZ() >> 4)) {
			WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_GUI_CLOSED_INFO,
					"wdl.messages.onGuiClosedInfo.cannotSaveTileEntities");
			return true;
		}

		BlockHandler<? extends TileEntity, ? extends Container> handler =
				BlockHandler.getHandler(te.getClass(), WDL.windowContainer.getClass());
		if (handler != null) {
			try {
				String msg = handler.handleCasting(WDL.lastClickedBlock, WDL.windowContainer,
						te, WDL.worldClient, WDL::saveTileEntity);
				WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_GUI_CLOSED_INFO, msg);
				return true;
			} catch (HandlerException e) {
				WDLMessages.chatMessageTranslated(e.messageType, e.translationKey, e.args);
				return false;
			}
		} else if (WDL.windowContainer instanceof ContainerChest
				&& te instanceof TileEntityEnderChest) {
			InventoryEnderChest inventoryEnderChest = WDL.thePlayer
					.getInventoryEnderChest();
			int inventorySize = inventoryEnderChest.getSizeInventory();
			int containerSize = WDL.windowContainer.inventorySlots.size();

			for (int i = 0; i < containerSize && i < inventorySize; i++) {
				Slot slot = WDL.windowContainer.getSlot(i);
				if (slot.getHasStack()) {
					inventoryEnderChest.setInventorySlotContents(i, slot.getStack());
				}
			}

			WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_GUI_CLOSED_INFO,
					"wdl.messages.onGuiClosedInfo.savedTileEntity.enderChest");
		} else {
			return false;
		}

		return true;
	}

	/**
	 * Must be called when a block event is scheduled for the next tick. The
	 * caller has to check if WDL.downloading is true!
	 */
	public static void onBlockEvent(BlockPos pos, Block block, int event,
			int param) {
		if (!WDL.downloading) { return; }

		if (!WDLPluginChannels.canSaveTileEntities(pos.getX() >> 4,
				pos.getZ() >> 4)) {
			return;
		}
		if (block == Blocks.NOTEBLOCK) {
			TileEntityNote newTE = new TileEntityNote();
			newTE.note = (byte)(param % 25);
			WDL.worldClient.setTileEntity(pos, newTE);
			WDL.saveTileEntity(pos, newTE);
			WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_BLOCK_EVENT,
					"wdl.messages.onBlockEvent.noteblock", pos, param, newTE);
		}
	}

	/**
	 * Must be called when a Map Data packet is received, to store the image on
	 * the map item.
	 */
	public static void onMapDataLoaded(int mapID,
			MapData mapData) {
		if (!WDL.downloading) { return; }

		if (!WDLPluginChannels.canSaveMaps()) {
			return;
		}

		WDL.newMapDatas.put(mapID, mapData);

		WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_MAP_SAVED,
				"wdl.messages.onMapSaved", mapID);
	}

	/**
	 * Must be called whenever a plugin channel message / custom payload packet
	 * is received.
	 */
	public static void onPluginChannelPacket(String channel,
			byte[] bytes) {
		WDLPluginChannels.onPluginChannelPacket(channel, bytes);
	}

	/**
	 * Must be called when an entity is about to be removed from the world.
	 */
	public static void onRemoveEntityFromWorld(Entity entity) {
		// If the entity is being removed and it's outside the default tracking
		// range, go ahead and remember it until the chunk is saved.
		if (WDL.downloading && entity != null
				&& WDLPluginChannels.canSaveEntities(entity.chunkCoordX,
						entity.chunkCoordZ)) {
			if (!EntityUtils.isEntityEnabled(entity)) {
				WDLMessages.chatMessageTranslated(
						WDLMessageTypes.REMOVE_ENTITY,
						"wdl.messages.removeEntity.allowingRemoveUserPref",
						entity);
				return;
			}

			int threshold = EntityUtils.getEntityTrackDistance(entity);

			if (threshold < 0) {
				WDLMessages.chatMessageTranslated(
						WDLMessageTypes.REMOVE_ENTITY,
						"wdl.messages.removeEntity.allowingRemoveUnrecognizedDistance",
						entity);
				return;
			}

			int serverViewDistance = 10; // XXX hardcoded for now
			// Ref EntityTracker.setViewDistance and PlayerList.getFurthestViewableBlock
			// (note that PlayerChunkMap.getFurthestViewableBlock is a misleading name)
			int maxThreshold = (serverViewDistance - 1) * 16;

			int range = Math.min(threshold, maxThreshold);

			// Entity track distance is a square, see EntityTrackerEntry.isVisibleTo
			double dx = Math.abs(entity.posX - WDL.thePlayer.posX);
			double dz = Math.abs(entity.posZ - WDL.thePlayer.posZ);

			double distance = Math.max(dx, dz);

			if (distance > range) {
				WDLMessages.chatMessageTranslated(
						WDLMessageTypes.REMOVE_ENTITY,
						"wdl.messages.removeEntity.savingDistance",
						entity, distance, range);
				entity.chunkCoordX = MathHelper
						.floor(entity.posX / 16.0D);
				entity.chunkCoordZ = MathHelper
						.floor(entity.posZ / 16.0D);

				WDL.newEntities.put(new ChunkPos(entity.chunkCoordX,
						entity.chunkCoordZ), entity);
				return;
			}

			WDLMessages.chatMessageTranslated(
					WDLMessageTypes.REMOVE_ENTITY,
					"wdl.messages.removeEntity.allowingRemoveDistance",
					entity, distance, range);
		}
	}

	/**
	 * Called upon any chat message.  Used for getting the seed.
	 */
	public static void onChatMessage(String msg) {
		if (WDL.downloading && msg.startsWith("Seed: ")) {
			String seed = msg.substring(6);
			WDL.worldProps.setProperty("RandomSeed", seed);

			if (WDL.worldProps.getProperty("MapGenerator", "void").equals("void")) {

				WDL.worldProps.setProperty("MapGenerator", "default");
				WDL.worldProps.setProperty("GeneratorName", "default");
				WDL.worldProps.setProperty("GeneratorVersion", "1");
				WDL.worldProps.setProperty("GeneratorOptions", "");

				WDLMessages.chatMessageTranslated(WDLMessageTypes.INFO,
						"wdl.messages.generalInfo.seedAndGenSet", seed);
			} else {
				WDLMessages.chatMessageTranslated(WDLMessageTypes.INFO,
						"wdl.messages.generalInfo.seedSet", seed);
			}
		}
	}
}
