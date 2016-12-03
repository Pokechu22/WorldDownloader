package wdl;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.ContainerHorseChest;
import net.minecraft.inventory.ContainerHorseInventory;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.inventory.Slot;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import wdl.api.IWorldLoadListener;
import wdl.api.WDLApi;
import wdl.api.WDLApi.ModInfo;
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
	private static final Profiler profiler = Minecraft.getMinecraft().mcProfiler;

	/**
	 * Must be called after the static World object in Minecraft has been
	 * replaced.
	 */
	public static void onWorldLoad(WorldClient world) {
		profiler.startSection("Core");
		
		if (WDL.minecraft.isIntegratedServerRunning()) {
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
			
			profiler.endSection();  // "Core"
			return;
		}

		boolean sameServer = WDL.loadWorld();
		
		WDLUpdateChecker.startIfNeeded();
		
		profiler.endSection();  // "Core"
		
		for (ModInfo<IWorldLoadListener> info : WDLApi
				.getImplementingExtensions(IWorldLoadListener.class)) {
			profiler.startSection(info.id);
			info.mod.onWorldLoad(world, sameServer);
			profiler.endSection();  // info.id
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
					unneededChunk.xPosition, unneededChunk.zPosition);
			WDL.saveChunk(unneededChunk);
		} else {
			WDLMessages.chatMessageTranslated(
					WDLMessageTypes.ON_CHUNK_NO_LONGER_NEEDED,
					"wdl.messages.onChunkNoLongerNeeded.didNotSave",
					unneededChunk.xPosition, unneededChunk.zPosition);
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
			// func_178782_a returns a BlockPos; find another one
			// if it is reobfuscated.
			WDL.lastClickedBlock = WDL.minecraft.objectMouseOver.getBlockPos();
		}
	}

	/**
	 * Must be called when a GUI that triggered an onItemGuiOpened is no longer
	 * shown.
	 */
	public static boolean onItemGuiClosed() {
		if (!WDL.downloading) { return true; }
		
		String saveName = "";

		if (WDL.thePlayer.getRidingEntity() instanceof AbstractHorse) {
			//If the player is on a horse, check if they are opening the
			//inventory of the horse they are on.  If so, use that,
			//rather than the entity being looked at.
			if (WDL.windowContainer instanceof ContainerHorseInventory) {
				AbstractHorse horseInContainer = ReflectionUtils
						.stealAndGetField(WDL.windowContainer,
								AbstractHorse.class);

				//Intentional reference equals
				if (horseInContainer == WDL.thePlayer.getRidingEntity()) {
					if (!WDLPluginChannels.canSaveEntities(
							horseInContainer.chunkCoordX,
							horseInContainer.chunkCoordZ)) {
						//I'm not 100% sure the chunkCoord stuff will have been
						//set up at this point.  Might cause bugs.
						WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_GUI_CLOSED_INFO,
								"wdl.messages.onGuiClosedInfo.cannotSaveEntities");
						return true;
					}

					AbstractHorse entityHorse = (AbstractHorse)
							WDL.thePlayer.getRidingEntity();
					saveHorse((ContainerHorseInventory)WDL.windowContainer, entityHorse);

					WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_GUI_CLOSED_INFO,
							"wdl.messages.onGuiClosedInfo.savedRiddenHorse");
					return true;
				}
			}
		}

		// If the last thing clicked was an ENTITY
		if (WDL.lastEntity != null) {
			if (!WDLPluginChannels.canSaveEntities(WDL.lastEntity.chunkCoordX,
					WDL.lastEntity.chunkCoordZ)) {
				WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_GUI_CLOSED_INFO,
						"wdl.messages.onGuiClosedInfo.cannotSaveEntities");
				return true;
			}

			if (WDL.lastEntity instanceof EntityMinecartChest
					&& WDL.windowContainer instanceof ContainerChest) {
				EntityMinecartChest emcc = (EntityMinecartChest) WDL.lastEntity;

				for (int i = 0; i < emcc.getSizeInventory(); i++) {
					Slot slot = WDL.windowContainer.getSlot(i);
					if (slot.getHasStack()) {
						emcc.setInventorySlotContents(i, slot.getStack());
					}
				}
				
				saveName = "storageMinecart";
			} else if (WDL.lastEntity instanceof EntityMinecartHopper
					&& WDL.windowContainer instanceof ContainerHopper) {
				EntityMinecartHopper emch = (EntityMinecartHopper) WDL.lastEntity;

				for (int i = 0; i < emch.getSizeInventory(); i++) {
					Slot slot = WDL.windowContainer.getSlot(i);
					if (slot.getHasStack()) {
						emch.setInventorySlotContents(i, slot.getStack());
					}
				}
				
				saveName = "hopperMinecart";
			} else if (WDL.lastEntity instanceof EntityVillager
					&& WDL.windowContainer instanceof ContainerMerchant) {
				EntityVillager ev = (EntityVillager) WDL.lastEntity;
				MerchantRecipeList list = (ReflectionUtils.stealAndGetField(
						WDL.windowContainer, IMerchant.class)).getRecipes(
								WDL.thePlayer);
				ReflectionUtils.stealAndSetField(ev, MerchantRecipeList.class, list);
				
				saveName = "villager";
			} else if (WDL.lastEntity instanceof AbstractHorse
					&& WDL.windowContainer instanceof ContainerHorseInventory) {
				saveHorse((ContainerHorseInventory) WDL.windowContainer,
						(AbstractHorse) WDL.lastEntity);
				
				saveName = "horse";
			} else {
				return false;
			}

			WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_GUI_CLOSED_INFO,
					"wdl.messages.onGuiClosedInfo.savedEntity." + saveName);
			return true;
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
		if (!WDLPluginChannels.canSaveContainers(te.getPos().getX() << 4, te
				.getPos().getZ() << 4)) {
			WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_GUI_CLOSED_INFO,
					"wdl.messages.onGuiClosedInfo.cannotSaveTileEntities");
			return true;
		}

		if (WDL.windowContainer instanceof ContainerChest
				&& te instanceof TileEntityChest) {
			if (WDL.windowContainer.inventorySlots.size() > 63) {
				// This is messy, but it needs to be like this because
				// the left and right chests must be in the right positions.
				
				BlockPos pos1, pos2;
				TileEntity te1, te2;
				
				pos1 = WDL.lastClickedBlock;
				te1 = te;
				
				// We need seperate variables for the above reason -- 
				// pos1 isn't always the same as chestPos1 (and thus
				// chest1 isn't always te1).
				BlockPos chestPos1 = null, chestPos2 = null;
				TileEntityChest chest1 = null, chest2 = null;
				
				pos2 = pos1.add(0, 0, 1);
				te2 = WDL.worldClient.getTileEntity(pos2);
				if (te2 instanceof TileEntityChest && 
						((TileEntityChest) te2).getChestType() == 
						((TileEntityChest) te1).getChestType()) {
					
					chest1 = (TileEntityChest) te1;
					chest2 = (TileEntityChest) te2;
					
					chestPos1 = pos1;
					chestPos2 = pos2;
				}
				
				pos2 = pos1.add(0, 0, -1);
				te2 = WDL.worldClient.getTileEntity(pos2);
				if (te2 instanceof TileEntityChest && 
						((TileEntityChest) te2).getChestType() == 
						((TileEntityChest) te1).getChestType()) {
					
					chest1 = (TileEntityChest) te2;
					chest2 = (TileEntityChest) te1;
					
					chestPos1 = pos2;
					chestPos2 = pos1;
				}

				pos2 = pos1.add(1, 0, 0);
				te2 = WDL.worldClient.getTileEntity(pos2);
				if (te2 instanceof TileEntityChest && 
						((TileEntityChest) te2).getChestType() == 
						((TileEntityChest) te1).getChestType()) {
					chest1 = (TileEntityChest) te1;
					chest2 = (TileEntityChest) te2;
					
					chestPos1 = pos1;
					chestPos2 = pos2;
				}
				
				pos2 = pos1.add(-1, 0, 0);
				te2 = WDL.worldClient.getTileEntity(pos2);
				if (te2 instanceof TileEntityChest && 
						((TileEntityChest) te2).getChestType() == 
						((TileEntityChest) te1).getChestType()) {
					chest1 = (TileEntityChest) te2;
					chest2 = (TileEntityChest) te1;
					
					chestPos1 = pos2;
					chestPos2 = pos1;
				}
				
				if (chest1 == null || chest2 == null || 
						chestPos1 == null || chestPos2 == null) {
					WDLMessages.chatMessageTranslated(WDLMessageTypes.ERROR,
							"wdl.messages.onGuiClosedWarning.failedToFindDoubleChest");
					return true;
				}

				WDL.saveContainerItems(WDL.windowContainer, chest1, 0);
				WDL.saveContainerItems(WDL.windowContainer, chest2, 27);
				WDL.saveTileEntity(chestPos1, chest1);
				WDL.saveTileEntity(chestPos2, chest2);
				
				saveName = "doubleChest";
			}
			// basic chest
			else {
				WDL.saveContainerItems(WDL.windowContainer, (TileEntityChest) te, 0);
				WDL.saveTileEntity(WDL.lastClickedBlock, te);
				saveName = "singleChest";
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

			saveName = "enderChest";
		} else if (WDL.windowContainer instanceof ContainerBrewingStand
				&& te instanceof TileEntityBrewingStand) {
			IInventory brewingInventory = ReflectionUtils.stealAndGetField(
					WDL.windowContainer, IInventory.class);
			WDL.saveContainerItems(WDL.windowContainer, (TileEntityBrewingStand) te, 0);
			WDL.saveInventoryFields(brewingInventory, (TileEntityBrewingStand) te);
			WDL.saveTileEntity(WDL.lastClickedBlock, te);
			saveName = "brewingStand";
		} else if (WDL.windowContainer instanceof ContainerDispenser
				&& te instanceof TileEntityDispenser) {
			WDL.saveContainerItems(WDL.windowContainer, (TileEntityDispenser) te, 0);
			WDL.saveTileEntity(WDL.lastClickedBlock, te);
			saveName = "dispenser";
		} else if (WDL.windowContainer instanceof ContainerFurnace
				&& te instanceof TileEntityFurnace) {
			IInventory furnaceInventory = ReflectionUtils.stealAndGetField(
					WDL.windowContainer, IInventory.class);
			WDL.saveContainerItems(WDL.windowContainer, (TileEntityFurnace) te, 0);
			WDL.saveInventoryFields(furnaceInventory, (TileEntityFurnace) te);
			WDL.saveTileEntity(WDL.lastClickedBlock, te);
			saveName = "furnace";
		} else if (WDL.windowContainer instanceof ContainerHopper
				&& te instanceof TileEntityHopper) {
			WDL.saveContainerItems(WDL.windowContainer, (TileEntityHopper) te, 0);
			WDL.saveTileEntity(WDL.lastClickedBlock, te);
			saveName = "hopper";
		} else if (WDL.windowContainer instanceof ContainerBeacon
				&& te instanceof TileEntityBeacon) {
			IInventory beaconInventory =
					((ContainerBeacon)WDL.windowContainer).getTileEntity();
			TileEntityBeacon savedBeacon = (TileEntityBeacon)te;
			WDL.saveContainerItems(WDL.windowContainer, savedBeacon, 0);
			WDL.saveInventoryFields(beaconInventory, savedBeacon);
			WDL.saveTileEntity(WDL.lastClickedBlock, te);
			saveName = "beacon";
		} else {
			return false;
		}

		WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_GUI_CLOSED_INFO,
				"wdl.messages.onGuiClosedInfo.savedTileEntity." + saveName);
		return true;
	}

	/**
	 * Must be called when a block event is scheduled for the next tick. The
	 * caller has to check if WDL.downloading is true!
	 */
	public static void onBlockEvent(BlockPos pos, Block block, int event,
			int param) {
		if (!WDL.downloading) { return; }
		
		if (!WDLPluginChannels.canSaveTileEntities(pos.getX() << 4,
				pos.getZ() << 4)) {
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
		/*
		// If the entity is being removed and it's outside the default tracking
		// range, go ahead and remember it until the chunk is saved.

		// Proper tracking ranges can be found in EntityTracker#trackEntity
		// (the one that takes an Entity as a paremeter) -- it's the 2nd arg
		// given to addEntityToTracker.
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

			double distance = entity.getDistance(WDL.thePlayer.posX,
					entity.posY, WDL.thePlayer.posZ);

			if (distance > threshold) {
				WDLMessages.chatMessageTranslated(
						WDLMessageTypes.REMOVE_ENTITY,
						"wdl.messages.removeEntity.savingDistance",
						entity, distance, threshold);
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
					entity, distance, threshold);
		}*/
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

	/**
	 * Saves all data for a horse into its inventory.
	 *
	 * @param container
	 * @param horse
	 */
	private static void saveHorse(ContainerHorseInventory container, AbstractHorse horse) {
		final int PLAYER_INVENTORY_SLOTS = 4 * 9;
		ContainerHorseChest horseInventory = new ContainerHorseChest(
				"HorseChest", container.inventorySlots.size()
						- PLAYER_INVENTORY_SLOTS);
		for (int i = 0; i < horseInventory.getSizeInventory(); i++) {
			Slot slot = container.getSlot(i);
			if (slot.getHasStack()) {
				horseInventory.setInventorySlotContents(i, slot.getStack());
			}
		}

		ReflectionUtils.stealAndSetField(horse, ContainerHorseChest.class, horseInventory);
	}
}
