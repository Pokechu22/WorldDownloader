package wdl;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.ContainerHorseInventory;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
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
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;

import wdl.api.IWorldLoadListener;
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
	 * All WDLMods that implement {@link IWorldLoadListener}.
	 */
	public static Map<String, IWorldLoadListener> worldLoadListeners =
			new HashMap<String, IWorldLoadListener>();
	
	/**
	 * Must be called after the static World object in Minecraft has been
	 * replaced.
	 */
	public static void onWorldLoad(WorldClient world) {
		profiler.startSection("Core");
		
		if (WDL.minecraft.isIntegratedServerRunning()) {
			return;
		}
		
		WDLUpdateChecker.startIfNeeded(WDLMessageTypes.UPDATES,
				WDLMessageTypes.UPDATE_DEBUG);

		// If already downloading
		if (WDL.downloading) {
			// If not currently saving, stop the current download and start
			// saving now
			if (!WDL.saving) {
				WDL.chatInfo("World change detected. Download will start once current save completes.");
				WDL.worldLoadingDeferred = true;
				WDL.startSaveThread();
			}
			
			profiler.endSection();
			return;
		}

		boolean sameServer = WDL.loadWorld();
		profiler.endSection();
		
		for (Map.Entry<String, IWorldLoadListener> e : worldLoadListeners.entrySet()) {
			profiler.startSection(e.getKey());
			e.getValue().onWorldLoad(world, sameServer);
			profiler.endSection();
		}
	}

	/** Must be called when a chunk is no longer needed and should be removed */
	public static void onChunkNoLongerNeeded(Chunk unneededChunk) {
		if (!WDL.downloading) { return; }
		
		if (!WDLPluginChannels.canDownloadInGeneral()) { return; }

		if (unneededChunk == null) {
			return;
		}

		if (WDLPluginChannels.canSaveChunk(unneededChunk)) {
			WDL.chatMessage(WDLMessageTypes.ON_CHUNK_NO_LONGER_NEEDED,
					"onChunkNoLongerNeeded: " + unneededChunk.xPosition + ", "
							+ unneededChunk.zPosition);
			WDL.saveChunk(unneededChunk);
		} else {
			WDL.chatMessage(WDLMessageTypes.ON_CHUNK_NO_LONGER_NEEDED,
					"onChunkNoLongerNeeded cannot save chunk at " +
							unneededChunk.xPosition + ", " + unneededChunk.zPosition
							+ " due to server restrictions!");
		}
	}

	/**
	 * Must be called when a GUI that receives item stacks from the server is
	 * shown
	 */
	public static void onItemGuiOpened() {
		if (!WDL.downloading) { return; }
		
		if (WDL.minecraft.objectMouseOver == null) {
			return;
		}

		if (WDL.minecraft.objectMouseOver.typeOfHit == MovingObjectType.ENTITY) {
			WDL.lastEntity = WDL.minecraft.objectMouseOver.entityHit;
		} else {
			WDL.lastEntity = null;
			// func_178782_a returns a BlockPos; find another one
			// if it is reobfuscated.
			WDL.lastClickedBlock = WDL.minecraft.objectMouseOver.func_178782_a();
		}
	}

	/**
	 * Must be called when a GUI that triggered an onItemGuiOpened is no longer
	 * shown
	 */
	public static boolean onItemGuiClosed() {
		if (!WDL.downloading) { return true; }
		
		if (!WDLPluginChannels.canDownloadInGeneral()) { return true; }

		String saveName = "";

		if (WDL.thePlayer.ridingEntity != null &&
				WDL.thePlayer.ridingEntity instanceof EntityHorse) {
			//If the player is on a horse, check if they are opening the
			//inventory of the horse they are on.  If so, use that,
			//rather than the entity being looked at.
			if (WDL.windowContainer instanceof ContainerHorseInventory) {
				EntityHorse horseInContainer = ReflectionUtils
						.stealAndGetField(WDL.windowContainer,
								EntityHorse.class);

				//Intentional reference equals
				if (horseInContainer == WDL.thePlayer.ridingEntity) {
					if (!WDLPluginChannels.canSaveEntities(
							horseInContainer.chunkCoordX,
							horseInContainer.chunkCoordZ)) {
						//I'm not 100% sure the chunkCoord stuff will have been
						//set up at this point.  Might cause bugs.
						WDL.chatMessage(WDLMessageTypes.ON_GUI_CLOSED_INFO,
								"Server configuration forbids saving of Entities!");
						return true;
					}

					EntityHorse entityHorse = (EntityHorse)
							WDL.thePlayer.ridingEntity;
					//Resize the horse's chest.  Needed because... reasons.
					//Apparently the saved horse has the wrong size by
					//default.
					//Based off of EntityHorse.func_110226_cD (in 1.8).
					AnimalChest horseChest = new AnimalChest("HorseChest",
							(entityHorse.isChested() &&
									(entityHorse.getHorseType() == 1 ||
									entityHorse.getHorseType() == 2)) ? 17 : 2);
					//func_110133_a sets the custom name -- if changed look
					//for one that sets hasCustomName to true and gives
					//inventoryTitle the value of the parameter.
					horseChest.func_110133_a(entityHorse.getName());
					WDL.saveContainerItems(WDL.windowContainer, horseChest, 0);
					//I don't even know what this does, but it's part of the
					//other method...
					horseChest.func_110134_a(entityHorse);
					//Save the actual data value to the other horse.
					ReflectionUtils.stealAndSetField(entityHorse, AnimalChest.class, horseChest);
					WDL.chatMessage(WDLMessageTypes.ON_GUI_CLOSED_INFO,
							"Saved ridden horse inventory.");
					return true;
				}
			}
		}

		// If the last thing clicked was an ENTITY
		if (WDL.lastEntity != null) {
			if (!WDLPluginChannels.canSaveEntities(WDL.lastEntity.chunkCoordX,
					WDL.lastEntity.chunkCoordZ)) {
				WDL.chatMessage(WDLMessageTypes.ON_GUI_CLOSED_INFO,
						"Server configuration forbids saving of Entities!");
				return true;
			}

			if (WDL.lastEntity instanceof EntityMinecartChest
					&& WDL.windowContainer instanceof ContainerChest) {
				EntityMinecartChest emcc = (EntityMinecartChest) WDL.lastEntity;

				for (int i = 0; i < emcc.getSizeInventory(); i++) {
					emcc.setInventorySlotContents(i, WDL.windowContainer
							.getSlot(i).getStack());
					saveName = "Storage Minecart contents";
				}
			} else if (WDL.lastEntity instanceof EntityMinecartHopper
					&& WDL.windowContainer instanceof ContainerHopper) {
				EntityMinecartHopper emch = (EntityMinecartHopper) WDL.lastEntity;

				for (int i = 0; i < emch.getSizeInventory(); i++) {
					emch.setInventorySlotContents(i, WDL.windowContainer
							.getSlot(i).getStack());
					saveName = "Hopper Minecart contents";
				}
			} else if (WDL.lastEntity instanceof EntityVillager
					&& WDL.windowContainer instanceof ContainerMerchant) {
				EntityVillager ev = (EntityVillager) WDL.lastEntity;
				MerchantRecipeList list = (ReflectionUtils.stealAndGetField(
						WDL.windowContainer, IMerchant.class)).getRecipes(
								WDL.thePlayer);
				ReflectionUtils.stealAndSetField(ev, MerchantRecipeList.class, list);
				saveName = "Villager offers";
			} else if (WDL.lastEntity instanceof EntityHorse
					&& WDL.windowContainer instanceof ContainerHorseInventory) {
				EntityHorse entityHorse = (EntityHorse)WDL.lastEntity;
				//Resize the horse's chest.  Needed because... reasons.
				//Apparently the saved horse has the wrong size by
				//default.
				//Based off of EntityHorse.func_110226_cD (in 1.8).
				AnimalChest horseChest = new AnimalChest("HorseChest",
						(entityHorse.isChested() &&
								(entityHorse.getHorseType() == 1 ||
								entityHorse.getHorseType() == 2)) ? 17 : 2);
				//func_110133_a sets the custom name -- if changed look
				//for one that sets hasCustomName to true and gives
				//inventoryTitle the value of the parameter.
				horseChest.func_110133_a(entityHorse.getName());
				WDL.saveContainerItems(WDL.windowContainer, horseChest, 0);
				//I don't even know what this does, but it's part of the
				//other method...
				horseChest.func_110134_a(entityHorse);
				//Save the actual data value to the other horse.
				ReflectionUtils.stealAndSetField(entityHorse, AnimalChest.class, horseChest);
				saveName = "Horse Chest";
			} else {
				return false;
			}

			WDL.chatMessage(WDLMessageTypes.ON_GUI_CLOSED_INFO, "Saved "
					+ saveName + ".");
			return true;
		}

		// Else, the last thing clicked was a TILE ENTITY
		
		// Get the tile entity which we are going to update the inventory for
		TileEntity te = WDL.worldClient.getTileEntity(WDL.lastClickedBlock);
		
		if (te == null) {
			//TODO: Is this a good way to stop?  Is the event truely handled here?
			WDL.chatMessage(WDLMessageTypes.ON_GUI_CLOSED_WARNING,
					"onItemGuiClosed could not get TE at " + WDL.lastClickedBlock);
			return true;
		}
		
		//Permissions check.
		if (!WDLPluginChannels.canSaveContainers(te.getPos().getX() << 4, te
				.getPos().getZ() << 4)) {
			WDL.chatMessage(WDLMessageTypes.ON_GUI_CLOSED_INFO,
					"Server configuration forbids saving of TileEntities!");
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
					WDL.chatError("Could not save this double chest!");
					WDL.chatError("Not all chest blocks were found!");
					return true;
				}

				WDL.saveContainerItems(WDL.windowContainer, chest1, 0);
				WDL.saveContainerItems(WDL.windowContainer, chest2, 27);
				WDL.newTileEntities.put(chestPos1, chest1);
				WDL.newTileEntities.put(chestPos2, chest2);
				
				saveName = "Double Chest contents";
			}
			// basic chest
			else {
				WDL.saveContainerItems(WDL.windowContainer, (TileEntityChest) te, 0);
				WDL.newTileEntities.put(WDL.lastClickedBlock, te);
				saveName = "Chest contents";
			}
		} else if (WDL.windowContainer instanceof ContainerChest
				&& te instanceof TileEntityEnderChest) {
			InventoryEnderChest inventoryEnderChest = WDL.thePlayer
					.getInventoryEnderChest();
			int inventorySize = inventoryEnderChest.getSizeInventory();
			int containerSize = WDL.windowContainer.inventorySlots.size();

			for (int i = 0; i < containerSize && i < inventorySize; i++) {
				inventoryEnderChest.setInventorySlotContents(i, WDL.windowContainer
						.getSlot(i).getStack());
			}

			saveName = "Ender Chest contents";
		} else if (WDL.windowContainer instanceof ContainerBrewingStand) {
			IInventory brewingInventory = ReflectionUtils.stealAndGetField(
					WDL.windowContainer, IInventory.class);
			WDL.saveContainerItems(WDL.windowContainer, (TileEntityBrewingStand) te, 0);
			WDL.saveInventoryFields(brewingInventory, (TileEntityBrewingStand) te);
			WDL.newTileEntities.put(WDL.lastClickedBlock, te);
			saveName = "Brewing Stand contents";
		} else if (WDL.windowContainer instanceof ContainerDispenser) {
			WDL.saveContainerItems(WDL.windowContainer, (TileEntityDispenser) te, 0);
			WDL.newTileEntities.put(WDL.lastClickedBlock, te);
			saveName = "Dispenser contents";
		} else if (WDL.windowContainer instanceof ContainerFurnace) {
			IInventory furnaceInventory = ReflectionUtils.stealAndGetField(
					WDL.windowContainer, IInventory.class);
			WDL.saveContainerItems(WDL.windowContainer, (TileEntityFurnace) te, 0);
			WDL.saveInventoryFields(furnaceInventory, (TileEntityFurnace) te);
			WDL.newTileEntities.put(WDL.lastClickedBlock, te);
			saveName = "Furnace contents";
		} else if (WDL.windowContainer instanceof ContainerHopper) {
			WDL.saveContainerItems(WDL.windowContainer, (TileEntityHopper) te, 0);
			WDL.newTileEntities.put(WDL.lastClickedBlock, te);
			saveName = "Hopper contents";
		} else if (WDL.windowContainer instanceof ContainerBeacon) {
			//func_180611_e returns the beacon's IInventory tileBeacon.
			IInventory beaconInventory =
					((ContainerBeacon)WDL.windowContainer).func_180611_e();
			TileEntityBeacon savedBeacon = (TileEntityBeacon)te;
			WDL.saveContainerItems(WDL.windowContainer, savedBeacon, 0);
			WDL.saveInventoryFields(beaconInventory, savedBeacon);
			WDL.newTileEntities.put(WDL.lastClickedBlock, te);
			saveName = "Beacon effects";
		} else {
			return false;
		}

		WDL.chatMessage(WDLMessageTypes.ON_GUI_CLOSED_INFO, "Saved "
				+ saveName + ".");
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
		if (block == Blocks.noteblock) {
			TileEntityNote newTE = new TileEntityNote();
			newTE.note = (byte)(param % 25);
			WDL.worldClient.setTileEntity(pos, newTE);
			WDL.newTileEntities.put(pos, newTE);
			WDL.chatMessage(WDLMessageTypes.ON_BLOCK_EVENT,
					"onBlockEvent: Note Block: " + pos + " pitch: " + param
					+ " - " + newTE);
		}
	}

	/**
	 * Must be called when Packet 0x34 (map data) is received.
	 */
	public static void onMapDataLoaded(int mapID, 
			MapData mapData) {
		if (!WDL.downloading) { return; }
		
		if (!WDLPluginChannels.canSaveMaps()) {
			return;
		}

		WDL.newMapDatas.put(mapID, mapData);

		WDL.chatMessage(WDLMessageTypes.ON_MAP_SAVED,
				"onMapDataLoaded: Saved map " + mapID + ".");
	}

	/**
	 * Must be called whenever a {@link S3FPacketCustomPayload} is
	 * received by the client.
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

		// Proper tracking ranges can be found in EntityTracker#trackEntity
		// (the one that takes an Entity as a paremeter) -- it's the 2nd arg
		// given to addEntityToTracker.
		if (WDL.downloading
				&& WDLPluginChannels.canSaveEntities(entity.chunkCoordX,
						entity.chunkCoordZ)) {
			if (entity != null) {
				if (!EntityUtils.isEntityEnabled(entity)) {
					WDLMessages.chatMessageTranslated(
							WDLMessageTypes.REMOVE_ENTITY,
							"wdl.messages.removeEntity.allowingRemoveUserPref",
							entity);
					return;
				}
				
				IChatComponent unsafeMessage = EntityUtils.isUnsafeToSaveEntity(entity);
				if (unsafeMessage != null) {
					WDLMessages.chatMessageTranslated(
							WDLMessageTypes.REMOVE_ENTITY,
							"wdl.messages.removeEntity.allowingRemoveUnsafe",
							entity, unsafeMessage);
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
							.floor_double(entity.posX / 16.0D);
					entity.chunkCoordZ = MathHelper
							.floor_double(entity.posZ / 16.0D);

					WDL.newEntities.put(entity.getEntityId(), entity);
					return;
				}

				WDLMessages.chatMessageTranslated(
						WDLMessageTypes.REMOVE_ENTITY,
						"wdl.messages.removeEntity.allowingRemoveDistance",
						entity, distance, threshold);
			}
		}
	}

	/**
	 * Called upon any chat message.  Used for getting the seed.
	 */
	public static void onChatMessage(String msg) {
		if (WDL.downloading && msg.startsWith("Seed: ")) {
			String seed = msg.substring(6);
			WDL.worldProps.setProperty("RandomSeed", seed);
			WDL.chatInfo("Setting single-player world seed to " + seed);
		}
	}
}
