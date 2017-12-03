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

import java.lang.reflect.Field;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.command.CommandException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EquineEntity;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
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
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import wdl.api.IWorldLoadListener;
import wdl.api.WDLApi;
import wdl.api.WDLApi.ModInfo;
import wdl.handler.block.BlockHandler;
import wdl.handler.block.BlockHandler.HandlerException;
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
	private static final Profiler PROFILER;
	static {
		// XXX This is a hack, probably should move tested code out of here
		Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft != null) {
			PROFILER = minecraft.mcProfiler;
		} else {
			PROFILER = new Profiler();
		}
	}

	/**
	 * Must be called after the static World object in Minecraft has been
	 * replaced.
	 */
	public static void onWorldLoad(WorldClient world) {
		PROFILER.startSection("Core");

		if (WDL.minecraft.isIntegratedServerRunning()) {
			// Don't do anything else in single player

			PROFILER.endSection();  // "Core"
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

			PROFILER.endSection();  // "Core"
			return;
		}

		boolean sameServer = WDL.loadWorld();

		WDLUpdateChecker.startIfNeeded();  // TODO: Always check for updates, even in single player

		PROFILER.endSection();  // "Core"

		for (ModInfo<IWorldLoadListener> info : WDLApi
				.getImplementingExtensions(IWorldLoadListener.class)) {
			PROFILER.startSection(info.id);
			info.mod.onWorldLoad(world, sameServer);
			PROFILER.endSection();  // info.id
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

		if (WDL.windowContainer == null ||
				ReflectionUtils.isCreativeContainer(WDL.windowContainer.getClass())) {
			// Can't do anything with null containers or the creative inventory
			return true;
		}

		String saveName = "";

		if (WDL.thePlayer.getRidingEntity() instanceof EquineEntity) {
			//If the player is on a horse, check if they are opening the
			//inventory of the horse they are on.  If so, use that,
			//rather than the entity being looked at.
			if (WDL.windowContainer instanceof ContainerHorseInventory) {
				EquineEntity horseInContainer = ReflectionUtils
						.findAndGetPrivateField(WDL.windowContainer,
								EquineEntity.class);

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

					EquineEntity entityHorse = (EquineEntity)
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

				IMerchant merchant = ReflectionUtils.findAndGetPrivateField(
						WDL.windowContainer, IMerchant.class);
				MerchantRecipeList list = merchant.getRecipes(WDL.thePlayer);
				ReflectionUtils.findAndSetPrivateField(ev, MerchantRecipeList.class, list);

				try {
					ITextComponent displayName = merchant.getDisplayName();
					if (!(displayName instanceof TextComponentTranslation)) {
						// Taking the toString to reflect JSON structure
						String componentDesc= String.valueOf(displayName);
						throw new CommandException("wdl.messages.onGuiClosedWarning.villagerCareer.notAComponent", componentDesc);
					}

					TextComponentTranslation displayNameTranslation = ((TextComponentTranslation) displayName);
					String key = displayNameTranslation.getKey();

					int career = EntityUtils.getCareer(key, ev.getProfession());

					// XXX Iteration order of fields is undefined, and this is generally sloppy
					// careerId is the 4th field
					int fieldIndex = 0;
					Field careerIdField = null;
					for (Field field : EntityVillager.class.getDeclaredFields()) {
						if (field.getType().equals(int.class)) {
							fieldIndex++;
							if (fieldIndex == 4) {
								careerIdField = field;
								break;
							}
						}
					}
					if (careerIdField == null) {
						throw new CommandException("wdl.messages.onGuiClosedWarning.villagerCareer.professionField");
					}

					careerIdField.setAccessible(true);
					careerIdField.setInt(ev, career);

					// Re-create this component rather than modifying the old one
					ITextComponent dispCareer = new TextComponentTranslation(key, displayNameTranslation.getFormatArgs());
					dispCareer.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(key)));

					WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_GUI_CLOSED_INFO, "wdl.messages.onGuiClosedInfo.savedEntity.villager.career", dispCareer, career);
				} catch (CommandException ex) {
					WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_GUI_CLOSED_WARNING, ex.getMessage(), ex.getErrorObjects());
				} catch (Throwable ex) {
					WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_GUI_CLOSED_WARNING, "wdl.messages.onGuiClosedWarning.villagerCareer.exception", ex);
				}

				saveName = "villager";
			} else if (WDL.lastEntity instanceof EquineEntity
					&& WDL.windowContainer instanceof ContainerHorseInventory) {
				saveHorse((ContainerHorseInventory) WDL.windowContainer,
						(EquineEntity) WDL.lastEntity);

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
		if (!WDLPluginChannels.canSaveContainers(te.getPos().getX() >> 4, te
				.getPos().getZ() >> 4)) {
			WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_GUI_CLOSED_INFO,
					"wdl.messages.onGuiClosedInfo.cannotSaveTileEntities");
			return true;
		}

		BlockHandler<? extends TileEntity, ? extends Container> handler =
				VersionedProperties.getHandler(te.getClass(), WDL.windowContainer.getClass());
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

			saveName = "enderChest";
		} else if (WDL.windowContainer instanceof ContainerBrewingStand
				&& te instanceof TileEntityBrewingStand) {
			IInventory brewingInventory = ReflectionUtils.findAndGetPrivateField(
					WDL.windowContainer, IInventory.class);
			saveContainerItems(WDL.windowContainer, (TileEntityBrewingStand) te, 0);
			saveInventoryFields(brewingInventory, (TileEntityBrewingStand) te);
			WDL.saveTileEntity(WDL.lastClickedBlock, te);
			saveName = "brewingStand";
		} else if (WDL.windowContainer instanceof ContainerDispenser
				&& te instanceof TileEntityDispenser) {
			saveContainerItems(WDL.windowContainer, (TileEntityDispenser) te, 0);
			WDL.saveTileEntity(WDL.lastClickedBlock, te);
			saveName = "dispenser";
		} else if (WDL.windowContainer instanceof ContainerFurnace
				&& te instanceof TileEntityFurnace) {
			IInventory furnaceInventory = ReflectionUtils.findAndGetPrivateField(
					WDL.windowContainer, IInventory.class);
			saveContainerItems(WDL.windowContainer, (TileEntityFurnace) te, 0);
			saveInventoryFields(furnaceInventory, (TileEntityFurnace) te);
			WDL.saveTileEntity(WDL.lastClickedBlock, te);
			saveName = "furnace";
		} else if (WDL.windowContainer instanceof ContainerHopper
				&& te instanceof TileEntityHopper) {
			saveContainerItems(WDL.windowContainer, (TileEntityHopper) te, 0);
			WDL.saveTileEntity(WDL.lastClickedBlock, te);
			saveName = "hopper";
		} else if (WDL.windowContainer instanceof ContainerBeacon
				&& te instanceof TileEntityBeacon) {
			IInventory beaconInventory =
					((ContainerBeacon)WDL.windowContainer).getTileEntity();
			TileEntityBeacon savedBeacon = (TileEntityBeacon)te;
			saveContainerItems(WDL.windowContainer, savedBeacon, 0);
			saveInventoryFields(beaconInventory, savedBeacon);
			WDL.saveTileEntity(WDL.lastClickedBlock, te);
			saveName = "beacon";
		} else if (VersionedProperties.handleShulkerGuiClosed(te)) {
			saveName = "shulkerBox";
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

	/**
	 * Saves all data for a horse into its inventory.
	 *
	 * @param container
	 * @param horse
	 */
	private static void saveHorse(ContainerHorseInventory container, EquineEntity horse) {
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

		ReflectionUtils.findAndSetPrivateField(horse, EquineEntity.class, ContainerHorseChest.class, horseInventory);
	}

	/**
	 * Saves the items of a container to the given TileEntity.
	 *
	 * @param container
	 *            The container to save from, usually {@link WDL#windowContainer} .
	 * @param tileEntity
	 *            The TileEntity to save to.
	 * @param containerStartIndex
	 *            The index in the container to start copying items from.
	 */
	public static void saveContainerItems(Container container,
			IInventory tileEntity, int containerStartIndex) {
		int containerSize = container.inventorySlots.size();
		int inventorySize = tileEntity.getSizeInventory();
		int containerIndex = containerStartIndex;
		int inventoryIndex = 0;

		while ((containerIndex < containerSize) && (inventoryIndex < inventorySize)) {
			Slot slot = container.getSlot(containerIndex);
			if (slot.getHasStack()) {
				tileEntity.setInventorySlotContents(inventoryIndex, slot.getStack());
			}
			inventoryIndex++;
			containerIndex++;
		}
	}

	/**
	 * Saves the fields of an inventory.
	 * Fields are pieces of data such as furnace smelt time and
	 * beacon effects.
	 *
	 * @param inventory The inventory to save from.
	 * @param tileEntity The inventory to save to.
	 */
	public static void saveInventoryFields(IInventory inventory,
			IInventory tileEntity) {
		for (int i = 0; i < inventory.getFieldCount(); i++) {
			tileEntity.setField(i, inventory.getField(i));
		}
	}
}
