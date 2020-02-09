/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2020 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;

import java.util.UUID;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import wdl.MapDataHandler.MapDataResult;
import wdl.api.IWorldLoadListener;
import wdl.api.WDLApi;
import wdl.api.WDLApi.ModInfo;
import wdl.config.settings.GeneratorSettings;
import wdl.handler.HandlerException;
import wdl.handler.block.BlockHandler;
import wdl.handler.blockaction.BlockActionHandler;
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
	private static final IProfiler PROFILER = ENABLE_PROFILER ? Minecraft.getInstance().getProfiler() : null;

	// XXX this shoudln't be static
	private static WDL wdl = WDL.getInstance();

	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Must be called after the static World object in Minecraft has been
	 * replaced.
	 */
	public static void onWorldLoad(ClientWorld world) {
		if (ENABLE_PROFILER) PROFILER.startSection("Core");

		if (wdl.minecraft.isIntegratedServerRunning()) {
			// Don't do anything else in single player

			if (ENABLE_PROFILER) PROFILER.endSection();  // "Core"
			return;
		}

		// If already downloading
		if (WDL.downloading) {
			// If not currently saving, stop the current download and start
			// saving now
			if (!WDL.saving) {
				wdl.saveForWorldChange();
			}

			if (ENABLE_PROFILER) PROFILER.endSection();  // "Core"
			return;
		}

		boolean sameServer = wdl.loadWorld();

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
					WDL.serverProps,
					WDLMessageTypes.ON_CHUNK_NO_LONGER_NEEDED,
					"wdl.messages.onChunkNoLongerNeeded.saved", unneededChunk.getPos().x, unneededChunk.getPos().z);
			wdl.saveChunk(unneededChunk);
		} else {
			WDLMessages.chatMessageTranslated(
					WDL.serverProps,
					WDLMessageTypes.ON_CHUNK_NO_LONGER_NEEDED,
					"wdl.messages.onChunkNoLongerNeeded.didNotSave", unneededChunk.getPos().x, unneededChunk.getPos().z);
		}
	}

	/**
	 * Must be called when a GUI that receives item stacks from the server is
	 * shown.
	 */
	public static void onItemGuiOpened() {
		if (!WDL.downloading) { return; }

		// NOTE: https://bugs.mojang.com/browse/MC-79925 was fixed in 1.14, but when backporting
		// will possibly cause issues.
		RayTraceResult result = wdl.minecraft.objectMouseOver;
		if (result.getType() == RayTraceResult.Type.MISS) {
			return;
		}

		if (result.getType() == RayTraceResult.Type.ENTITY) {
			wdl.lastEntity = ((EntityRayTraceResult)result).getEntity();
		} else {
			wdl.lastEntity = null;
			wdl.lastClickedBlock = ((BlockRayTraceResult)result).getPos();
		}
	}

	/**
	 * Must be called when a GUI that triggered an onItemGuiOpened is no longer
	 * shown.
	 */
	public static boolean onItemGuiClosed() {
		if (!WDL.downloading) { return true; }

		Container windowContainer = wdl.windowContainer;

		if (windowContainer == null ||
				ReflectionUtils.isCreativeContainer(windowContainer.getClass())) {
			// Can't do anything with null containers or the creative inventory
			return true;
		}

		Entity ridingEntity = wdl.player.getRidingEntity();
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
						WDLMessages.chatMessageTranslated(WDL.serverProps,
								WDLMessageTypes.ON_GUI_CLOSED_INFO, "wdl.messages.onGuiClosedInfo.cannotSaveEntities");
						return true;
					}

					try {
						ITextComponent msg = handler.copyDataCasting(windowContainer, ridingEntity, true);
						WDLMessages.chatMessage(WDL.serverProps, WDLMessageTypes.ON_GUI_CLOSED_INFO, msg);
						return true;
					} catch (HandlerException e) {
						WDLMessages.chatMessageTranslated(WDL.serverProps, e.messageType, e.translationKey, e.args);
						return false;
					}
				}
			} else {
				// A null handler is perfectly normal -- consider a player
				// riding a pig and then opening a chest
			}
		}

		// If the last thing clicked was an ENTITY
		Entity entity = wdl.lastEntity;
		if (entity != null) {
			if (!WDLPluginChannels.canSaveEntities(entity.chunkCoordX, entity.chunkCoordZ)) {
				WDLMessages.chatMessageTranslated(WDL.serverProps,
						WDLMessageTypes.ON_GUI_CLOSED_INFO, "wdl.messages.onGuiClosedInfo.cannotSaveEntities");
				return true;
			}

			EntityHandler<?, ?> handler = EntityHandler.getHandler(entity.getClass(), windowContainer.getClass());
			if (handler != null) {
				try {
					ITextComponent msg = handler.copyDataCasting(windowContainer, entity, true);
					WDLMessages.chatMessage(WDL.serverProps, WDLMessageTypes.ON_GUI_CLOSED_INFO, msg);
					return true;
				} catch (HandlerException e) {
					WDLMessages.chatMessageTranslated(WDL.serverProps, e.messageType, e.translationKey, e.args);
					return false;
				}
			} else {
				return false;
			}
		}

		// Else, the last thing clicked was a TILE ENTITY

		// Get the tile entity which we are going to update the inventory for
		TileEntity te = wdl.worldClient.getTileEntity(wdl.lastClickedBlock);

		if (te == null) {
			//TODO: Is this a good way to stop?  Is the event truely handled here?
			WDLMessages.chatMessageTranslated(
					WDL.serverProps,
					WDLMessageTypes.ON_GUI_CLOSED_WARNING,
					"wdl.messages.onGuiClosedWarning.couldNotGetTE", wdl.lastClickedBlock);
			return true;
		}

		//Permissions check.
		if (!WDLPluginChannels.canSaveContainers(te.getPos().getX() >> 4, te
				.getPos().getZ() >> 4)) {
			WDLMessages.chatMessageTranslated(WDL.serverProps,
					WDLMessageTypes.ON_GUI_CLOSED_INFO, "wdl.messages.onGuiClosedInfo.cannotSaveTileEntities");
			return true;
		}

		BlockHandler<? extends TileEntity, ? extends Container> handler =
				BlockHandler.getHandler(te.getClass(), wdl.windowContainer.getClass());
		if (handler != null) {
			try {
				ITextComponent msg = handler.handleCasting(wdl.lastClickedBlock, wdl.windowContainer,
						te, wdl.worldClient, wdl::saveTileEntity);
				WDLMessages.chatMessage(WDL.serverProps, WDLMessageTypes.ON_GUI_CLOSED_INFO, msg);
				return true;
			} catch (HandlerException e) {
				WDLMessages.chatMessageTranslated(WDL.serverProps, e.messageType, e.translationKey, e.args);
				return false;
			}
		} else if (wdl.windowContainer instanceof ChestContainer
				&& te instanceof EnderChestTileEntity) {
			EnderChestInventory inventoryEnderChest = wdl.player
					.getInventoryEnderChest();
			int inventorySize = inventoryEnderChest.getSizeInventory();
			int containerSize = wdl.windowContainer.inventorySlots.size();

			for (int i = 0; i < containerSize && i < inventorySize; i++) {
				Slot slot = wdl.windowContainer.getSlot(i);
				if (slot.getHasStack()) {
					inventoryEnderChest.setInventorySlotContents(i, slot.getStack());
				}
			}

			WDLMessages.chatMessageTranslated(WDL.serverProps,
					WDLMessageTypes.ON_GUI_CLOSED_INFO, "wdl.messages.onGuiClosedInfo.savedTileEntity.enderChest");
		} else {
			return false;
		}

		return true;
	}

	/**
	 * Must be called when a block event/block action packet is received.
	 */
	public static void onBlockEvent(BlockPos pos, Block block, int data1, int data2) {
		if (!WDL.downloading) { return; }

		if (!WDLPluginChannels.canSaveTileEntities(pos.getX() >> 4,
				pos.getZ() >> 4)) {
			return;
		}

		TileEntity blockEntity = wdl.worldClient.getTileEntity(pos);
		if (blockEntity == null) {
			return;
		}

		BlockActionHandler<? extends Block, ? extends TileEntity> handler =
				BlockActionHandler.getHandler(block.getClass(), blockEntity.getClass());
		if (handler != null) {
			try {
				ITextComponent msg = handler.handleCasting(pos, block, blockEntity,
						data1, data2, wdl.worldClient, wdl::saveTileEntity);
				WDLMessages.chatMessage(WDL.serverProps, WDLMessageTypes.ON_GUI_CLOSED_INFO, msg);
			} catch (HandlerException e) {
				WDLMessages.chatMessageTranslated(WDL.serverProps, e.messageType, e.translationKey, e.args);
			}
		}
	}

	/**
	 * Must be called when a Map Data packet is received, to store the image on
	 * the map item.
	 */
	public static void onMapDataLoaded(int mapID, @Nonnull MapData mapData) {
		if (!WDL.downloading) { return; }

		if (!WDLPluginChannels.canSaveMaps()) {
			return;
		}

		// Assume that the current dimension is the right one
		ClientPlayerEntity player = wdl.player;
		assert player != null;
		MapDataResult result = MapDataHandler.repairMapData(mapID, mapData, wdl.player);

		wdl.newMapDatas.put(mapID, result.map);

		WDLMessages.chatMessageTranslated(WDL.serverProps,
				WDLMessageTypes.ON_MAP_SAVED, "wdl.messages.onMapSaved", mapID, result.toComponent());
	}

	/**
	 * Must be called whenever a plugin channel message / custom payload packet
	 * is received.
	 */
	public static void onPluginChannelPacket(ClientPlayNetHandler sender,
			String channel, byte[] bytes) {
		WDLPluginChannels.onPluginChannelPacket(sender, channel, bytes);
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
						WDL.serverProps,
						WDLMessageTypes.REMOVE_ENTITY,
						"wdl.messages.removeEntity.allowingRemoveUserPref", entity);
				return;
			}

			int threshold = EntityUtils.getEntityTrackDistance(entity);

			if (threshold < 0) {
				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.REMOVE_ENTITY,
						"wdl.messages.removeEntity.allowingRemoveUnrecognizedDistance", entity);
				return;
			}

			int serverViewDistance = 10; // XXX hardcoded for now

			if (EntityUtils.isWithinSavingDistance(entity, wdl.player,
					threshold, serverViewDistance)) {
				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.REMOVE_ENTITY,
						"wdl.messages.removeEntity.savingDistance", entity,
						entity.getPositionVector().toString(), wdl.player.getPositionVector(), threshold, serverViewDistance);
				ChunkPos pos = new ChunkPos(entity.chunkCoordX, entity.chunkCoordZ);
				UUID uuid = entity.getUniqueID();
				if (wdl.entityPositions.containsKey(uuid)) {
					// Remove previous entity, to avoid saving the same one in multiple chunks.
					ChunkPos prevPos = wdl.entityPositions.get(uuid);
					boolean removedSome = wdl.newEntities.get(pos).removeIf(e -> e.getUniqueID().equals(uuid));
					LOGGER.info("Replacing entity with UUID {} previously located at {} with new position {}.  There was an entity at old position (should be true): {}", uuid, prevPos, pos, removedSome);
				}
				wdl.newEntities.put(pos, entity);
				wdl.entityPositions.put(uuid, pos);
			} else {
				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.REMOVE_ENTITY,
						"wdl.messages.removeEntity.allowingRemoveDistance", entity,
						entity.getPositionVector().toString(), wdl.player.getPositionVector(), threshold, serverViewDistance);
			}
		}
	}

	/**
	 * Called upon any chat message.  Used for getting the seed.
	 */
	public static void onChatMessage(String msg) {
		if (WDL.downloading && msg.startsWith("Seed: ")) {
			String seed = msg.substring(6);
			if (seed.startsWith("[") && seed.endsWith("]")) {
				// In 1.13, the seed is enclosed by brackets (and is also selectable on click)
				// We don't want those brackets.
				seed = seed.substring(1, seed.length() - 1);
			}
			wdl.worldProps.setValue(GeneratorSettings.SEED, seed);

			if (wdl.worldProps.getValue(GeneratorSettings.GENERATOR) ==
					GeneratorSettings.Generator.VOID) {

				wdl.worldProps.setValue(GeneratorSettings.GENERATOR,
						GeneratorSettings.Generator.DEFAULT);

				WDLMessages.chatMessageTranslated(WDL.serverProps,
						WDLMessageTypes.INFO, "wdl.messages.generalInfo.seedAndGenSet", seed);
			} else {
				WDLMessages.chatMessageTranslated(WDL.serverProps,
						WDLMessageTypes.INFO, "wdl.messages.generalInfo.seedSet", seed);
			}
		}
	}
}
