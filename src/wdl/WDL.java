package wdl;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBeacon;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockNote;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.inventory.Container;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
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
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.ThreadedFileIOBase;

/**
 * This is the main class that does most of the work.
 */
public class WDL {
	// TODO: This class needs to be split into smaller classes. There is way too
	// much different stuff in here.

	/**
	 * Whether or not debug is enabled. Setting to false will suppress debug
	 * output in chat console.
	 */
	public static boolean DEBUG = true;

	/**
	 * Reference to the Minecraft object.
	 */
	public static Minecraft minecraft;
	/**
	 * Reference to the World object that WDL uses.
	 */
	public static WorldClient worldClient;
	/**
	 * Reference to a connection specific object. Used to detect a new
	 * connection.
	 */
	public static NetworkManager networkManager = null;
	/**
	 * The current player. <br/>
	 * In 1.7.10, a net.minecraft.client.entity.EntityClientPlayerMP was used
	 * here, but now that does not exist, and it appears that the SinglePlayer
	 * type is what is supposed to be used instead.
	 */
	public static EntityPlayerSP thePlayer;

	/**
	 * Reference to the place where all the item stacks end up after receiving
	 * them.
	 */
	public static Container windowContainer;
	/**
	 * The block position clicked most recently.
	 *
	 * Needed for TileEntity creation.
	 */
	public static BlockPos lastClickedBlock;
	/**
	 * Last entity clicked (used for non-block tiles like minecarts with chests)
	 */
	public static Entity lastEntity;

	/**
	 * For player files and the level.dat file.
	 */
	public static SaveHandler saveHandler;
	/**
	 * For the chunks (despite the name it does also SAVE chunks)
	 */
	public static IChunkLoader chunkLoader;

	/**
	 * Positions of newly created TileEntities that will overwrite the imported
	 * ones when saving:
	 */
	public static HashSet<BlockPos> newTileEntities = new HashSet<BlockPos>();

	// State variables:
	/**
	 * Whether the world is currently downloading.
	 *
	 * Don't modify this outside of WDL.java. TODO See above -- getters?
	 */
	public static boolean downloading = false;
	/**
	 * Is this a multiworld server?
	 */
	public static boolean isMultiworld = false;
	/**
	 * Are there saved properties available?
	 */
	public static boolean propsFound = false;
	/**
	 * Automatically restart after world changes?
	 */
	public static boolean startOnChange = false;

	/**
	 * Is the world currently being saved?
	 */
	public static boolean saving = false;
	/**
	 * (Unknown) Has loading the world been delayed? TODO Seems to be unused.
	 */
	public static boolean worldLoadingDeferred = false;

	// Names:
	/**
	 * The current world name.
	 */
	public static String worldName = "WorldDownloaderERROR";
	/**
	 * The folder in which worlds are being saved.
	 */
	public static String baseFolderName = "WorldDownloaderERROR";

	// Properties:
	public static Properties baseProps;
	public static Properties worldProps;
	public static Properties defaultProps;

	// Initialization:
	static {
		minecraft = Minecraft.getMinecraft();
		// Initialize the Properties template:
		defaultProps = new Properties();
		defaultProps.setProperty("ServerName", "");
		defaultProps.setProperty("WorldName", "");
		defaultProps.setProperty("LinkedWorlds", "");
		defaultProps.setProperty("AutoStart", "false");
		defaultProps.setProperty("GameType", "keep");
		defaultProps.setProperty("Time", "keep");
		defaultProps.setProperty("Weather", "keep");
		defaultProps.setProperty("MapFeatures", "false");
		defaultProps.setProperty("RandomSeed", "");
		defaultProps.setProperty("GeneratorName", "flat");
		defaultProps.setProperty("GeneratorVersion", "0");
		defaultProps.setProperty("Spawn", "player");
		defaultProps.setProperty("SpawnX", "8");
		defaultProps.setProperty("SpawnY", "127");
		defaultProps.setProperty("SpawnZ", "8");
		defaultProps.setProperty("PlayerPos", "keep");
		defaultProps.setProperty("PlayerX", "8");
		defaultProps.setProperty("PlayerY", "127");
		defaultProps.setProperty("PlayerZ", "8");
		defaultProps.setProperty("PlayerHealth", "20");
		defaultProps.setProperty("PlayerFood", "20");
		baseProps = new Properties(defaultProps);
		worldProps = new Properties(baseProps);
	}

	/** Starts the download */
	public static void start() {
		worldClient = minecraft.theWorld;

		if (isMultiworld && worldName.isEmpty()) {
			// Ask the user which world is loaded
			minecraft.displayGuiScreen(new GuiWDLMultiworldSelect(null));
			return;
		}

		if (!propsFound) {
			// Never seen this world before. Ask user about multiworlds:
			minecraft.displayGuiScreen(new GuiWDLMultiworld(null));
			return;
		}

		WDL.minecraft.displayGuiScreen((GuiScreen) null);
		WDL.minecraft.setIngameFocus();
		worldProps = loadWorldProps(worldName);
		saveHandler = (SaveHandler) minecraft.getSaveLoader().getSaveLoader(
				getWorldFolderName(worldName), true);
		chunkLoader = saveHandler.getChunkLoader(worldClient.provider);
		newTileEntities = new HashSet<BlockPos>();

		if (baseProps.getProperty("ServerName").isEmpty()) {
			baseProps.setProperty("ServerName", getServerName());
		}

		startOnChange = true;
		downloading = true;
		chatMsg("Download started");
	}

	/** Stops the download */
	public static void stop() {
		if (downloading) {
			// Indicate that downloading has stopped
			downloading = false;
			startOnChange = false;
			chatMsg("Download stopped");
			startSaveThread();
		}
	}

	private static void startSaveThread() {
		// Indicate that we are saving
		WDL.chatMsg("Save started.");
		WDL.saving = true;
		WDLSaveAsync saver = new WDLSaveAsync();
		Thread thread = new Thread(saver, "WDL Save Thread");
		thread.start();
	}

	/**
	 * Must be called after the static World object in Minecraft has been
	 * replaced
	 */
	public static void onWorldLoad() {
		if (minecraft.isIntegratedServerRunning()) {
			return;
		}

		// If already downloading
		if (downloading) {
			// If not currently saving, stop the current download and start
			// saving now
			if (!saving) {
				WDL.chatMsg("World change detected. Download will start once current save completes.");
				// worldLoadingDeferred = true;
				startSaveThread();
			}

			return;
		}

		loadWorld();
	}

	public static void loadWorld() {
		worldName = ""; // The new (multi-)world name is unknown at the moment
		worldClient = minecraft.theWorld;
		thePlayer = minecraft.thePlayer;
		windowContainer = thePlayer.openContainer;
		// Is this a different server?
		NetworkManager newNM = thePlayer.sendQueue.getNetworkManager(); // tp.sendQueue.getNetManager()

		if (networkManager != newNM) {
			// Different server, different world!
			chatDebug("onWorldLoad: different server!");
			networkManager = newNM;
			loadBaseProps();

			if (baseProps.getProperty("AutoStart").equals("true")) {
				start();
			} else {
				startOnChange = false;
			}
		} else {
			// Same server, different world!
			chatDebug("onWorldLoad: same server!");

			if (startOnChange) {
				start();
			}
		}
	}

	/** Must be called when the world is no longer used */
	public static void onWorldUnload() {
	}

	public static void onSaveComplete() {
		WDL.minecraft.getSaveLoader().flushCache();
		WDL.saveHandler.flush();
		WDL.worldClient = null;

		// If still downloading, load the current world and keep on downloading
		if (downloading) {
			WDL.chatMsg("Save complete. Starting download again.");
			WDL.loadWorld();
			return;
		}

		WDL.chatMsg("Save complete. Your single player file is ready to play!");
	}

	/** Must be called when a chunk is no longer needed and should be removed */
	public static void onChunkNoLongerNeeded(Chunk unneededChunk) {
		if (unneededChunk == null) {
			return;
		}

		chatDebug("onChunkNoLongerNeeded: " + unneededChunk.xPosition + ", "
				+ unneededChunk.zPosition);
		saveChunk(unneededChunk);
	}

	/**
	 * Must be called when a GUI that receives item stacks from the server is
	 * shown
	 */
	public static void onItemGuiOpened() {
		if (minecraft.objectMouseOver == null) {
			return;
		}

		if (minecraft.objectMouseOver.typeOfHit == MovingObjectType.ENTITY) {
			lastEntity = minecraft.objectMouseOver.entityHit;
		} else {
			lastEntity = null;
			// func_178782_a returns a BlockPos; find another one
			// if it is reobfuscated.
			lastClickedBlock = minecraft.objectMouseOver.func_178782_a();
		}
	}

	/**
	 * Must be called when a GUI that triggered an onItemGuiOpened is no longer
	 * shown
	 */
	public static void onItemGuiClosed() {
		String saveName = "";

		if (thePlayer.ridingEntity != null &&
				thePlayer.ridingEntity instanceof EntityHorse) {
			//If the player is on a horse, check if they are opening the
			//inventory of the horse they are on.  If so, use that,
			//rather than the entity being looked at.
			if (windowContainer instanceof ContainerHorseInventory) {
				EntityHorse horseInContainer = (EntityHorse)
						stealAndGetField(windowContainer, EntityHorse.class);

				//Intentional reference equals
				if (horseInContainer == thePlayer.ridingEntity) {
					EntityHorse entityHorse = (EntityHorse)
							thePlayer.ridingEntity;
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
					saveContainerItems(windowContainer, horseChest, 0);
					//I don't even know what this does, but it's part of the
					//other method...
					horseChest.func_110134_a(entityHorse);
					//Save the actual data value to the other horse.
					stealAndSetField(entityHorse, AnimalChest.class, horseChest);
					WDL.chatDebug("Saved ridden horse inventory.");
					return;
				}
			}
		}

		// If the last thing clicked was an ENTITY
		if (lastEntity != null) {
			if (lastEntity instanceof EntityMinecartChest
					&& windowContainer instanceof ContainerChest) {
				EntityMinecartChest emcc = (EntityMinecartChest) lastEntity;

				for (int i = 0; i < emcc.getSizeInventory(); i++) {
					emcc.setInventorySlotContents(i, windowContainer
							.getSlot(i).getStack());
					saveName = "Storage Minecart contents";
				}
			} else if (lastEntity instanceof EntityMinecartHopper
					&& windowContainer instanceof ContainerHopper) {
				EntityMinecartHopper emch = (EntityMinecartHopper) lastEntity;

				for (int i = 0; i < emch.getSizeInventory(); i++) {
					emch.setInventorySlotContents(i, windowContainer
							.getSlot(i).getStack());
					saveName = "Hopper Minecart contents";
				}
			} else if (lastEntity instanceof EntityVillager
					&& windowContainer instanceof ContainerMerchant) {
				EntityVillager ev = (EntityVillager) lastEntity;
				MerchantRecipeList list = ((IMerchant)stealAndGetField(
						windowContainer, IMerchant.class)).getRecipes(
								thePlayer);
				stealAndSetField(ev, MerchantRecipeList.class, list);
				saveName = "Villager offers";
			} else if (lastEntity instanceof EntityHorse
					&& windowContainer instanceof ContainerHorseInventory) {
				EntityHorse entityHorse = (EntityHorse)lastEntity;
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
				saveContainerItems(windowContainer, horseChest, 0);
				//I don't even know what this does, but it's part of the
				//other method...
				horseChest.func_110134_a(entityHorse);
				//Save the actual data value to the other horse.
				stealAndSetField(entityHorse, AnimalChest.class, horseChest);
				saveName = "Horse Chest";
			} else {
				WDL.chatMsg("Unsupported entity cannot be saved:"
						+ EntityList.getEntityString(lastEntity));
			}

			WDL.chatDebug("Saved " + saveName + ".");
			return;
		}

		// Else, the last thing clicked was a TILE ENTITY
		// Get the tile entity which we are going to update the inventory for
		TileEntity te = worldClient.getTileEntity(lastClickedBlock);

		if (te == null) {
			WDL.chatDebug("onItemGuiClosed could not get TE at "
					+ lastClickedBlock);
			return;
		}

		if (windowContainer instanceof ContainerChest
				&& te instanceof TileEntityChest) {
			if (windowContainer.inventorySlots.size() > 63) {
				TileEntity te2;
				BlockPos chestPos1 = lastClickedBlock;
				BlockPos chestPos2;
				TileEntityChest tec1, tec2;

				if ((te2 = worldClient.getTileEntity(chestPos1.add(0, 0, 1))) instanceof TileEntityChest
						&& ((TileEntityChest) te2).getChestType() == ((TileEntityChest) te)
						.getChestType()) {
					tec1 = (TileEntityChest) te;
					tec2 = (TileEntityChest) te2;
					chestPos2 = chestPos1.add(0, 0, 1);
				} else if ((te2 = worldClient.getTileEntity(chestPos1.add(0, 0,
						-1))) instanceof TileEntityChest
						&& ((TileEntityChest) te2).getChestType() == ((TileEntityChest) te)
						.getChestType()) {
					tec1 = (TileEntityChest) te2;
					tec2 = (TileEntityChest) te;
					chestPos2 = chestPos1.add(0, 0, -1);
				} else if ((te2 = worldClient.getTileEntity(chestPos1.add(1, 0,
						0))) instanceof TileEntityChest
						&& ((TileEntityChest) te2).getChestType() == ((TileEntityChest) te)
						.getChestType()) {
					tec1 = (TileEntityChest) te;
					tec2 = (TileEntityChest) te2;
					chestPos2 = chestPos1.add(-1, 0, 0);
				} else if ((te2 = worldClient.getTileEntity(chestPos1.add(-1,
						0, 0))) instanceof TileEntityChest
						&& ((TileEntityChest) te2).getChestType() == ((TileEntityChest) te)
						.getChestType()) {
					tec1 = (TileEntityChest) te2;
					tec2 = (TileEntityChest) te;
					chestPos2 = chestPos1.add(-1, 0, 0);
				} else {
					WDL.chatMsg("Could not save this chest!");
					return;
				}

				saveContainerItems(windowContainer, tec1, 0);
				saveContainerItems(windowContainer, tec2, 27);
				newTileEntities.add(chestPos1);
				newTileEntities.add(chestPos2);
				saveName = "Double Chest contents";
			}
			// basic chest
			else {
				saveContainerItems(windowContainer, (TileEntityChest) te, 0);
				newTileEntities.add(lastClickedBlock);
				saveName = "Chest contents";
			}
		} else if (windowContainer instanceof ContainerChest
				&& te instanceof TileEntityEnderChest) {
			InventoryEnderChest inventoryEnderChest = thePlayer
					.getInventoryEnderChest();
			int inventorySize = inventoryEnderChest.getSizeInventory();
			int containerSize = windowContainer.inventorySlots.size();

			for (int i = 0; i < containerSize && i < inventorySize; i++) {
				inventoryEnderChest.setInventorySlotContents(i, windowContainer
						.getSlot(i).getStack());
			}

			saveName = "Ender Chest contents";
		} else if (windowContainer instanceof ContainerBrewingStand) {
			IInventory brewingInventory = (IInventory) stealAndGetField(
					windowContainer, IInventory.class);
			saveContainerItems(windowContainer, (TileEntityBrewingStand) te, 0);
			saveInventoryFields(brewingInventory, (TileEntityBrewingStand) te);
			newTileEntities.add(lastClickedBlock);
			saveName = "Brewing Stand contents";
		} else if (windowContainer instanceof ContainerDispenser) {
			saveContainerItems(windowContainer, (TileEntityDispenser) te, 0);
			newTileEntities.add(lastClickedBlock);
			saveName = "Dispenser contents";
		} else if (windowContainer instanceof ContainerFurnace) {
			IInventory furnaceInventory = (IInventory) stealAndGetField(
					windowContainer, IInventory.class);
			saveContainerItems(windowContainer, (TileEntityFurnace) te, 0);
			saveInventoryFields(furnaceInventory, (TileEntityFurnace) te);
			newTileEntities.add(lastClickedBlock);
			saveName = "Furnace contents";
		} else if (windowContainer instanceof ContainerHopper) {
			saveContainerItems(windowContainer, (TileEntityHopper) te, 0);
			newTileEntities.add(lastClickedBlock);
			saveName = "Hopper contents";
		} else if (windowContainer instanceof ContainerBeacon) {
			//func_180611_e returns the beacon's IInventory tileBeacon.
			IInventory beaconInventory =
				((ContainerBeacon)windowContainer).func_180611_e();
			TileEntityBeacon savedBeacon = (TileEntityBeacon)te;
			saveContainerItems(windowContainer, savedBeacon, 0);
			saveInventoryFields(beaconInventory, savedBeacon);
			newTileEntities.add(lastClickedBlock);
			saveName = "Beacon effects";
		} else {
			WDL.chatDebug("onItemGuiClosed unhandled TE: " + te);
			return;
		}

		WDL.chatDebug("Saved " + saveName + ".");
		return;
	}

	/**
	 * Must be called when a block event is scheduled for the next tick. The
	 * caller has to check if WDL.downloading is true!
	 */
	public static void onBlockEvent(BlockPos pos, Block block, int event,
			int param) {
		// if( blockID == Block.music.blockID )
		if (block == Blocks.noteblock) {
			TileEntityNote newTE = new TileEntityNote();
			newTE.note = (byte)(param % 25);
			worldClient.setTileEntity(pos, newTE);
			newTileEntities.add(pos);
			chatDebug("onBlockEvent: Note Block: " + pos + " pitch: " + param
					+ " - " + newTE);
		}

		// Pistons, Chests (open, close), EnderChests, ... (see references to
		// WorldServer.addBlockEvent)
	}

	/**
	 * Must be called when an entity is about to be removed from the world.
	 *
	 * @return true if the entity should not be removed, false if it can be
	 */
	public static boolean shouldKeepEntity(Entity entity) {
		// If the entity is being removed and it's outside the default tracking
		// range,
		// go ahead and remember it until the chunk is saved.
		if (WDL.downloading) {
			if (entity != null) {
				int threshold = 0;

				if ((entity instanceof EntityFishHook)
						||
						// (entity instanceof EntityArrow) ||
						// (entity instanceof EntitySmallFireball) ||
						// (entity instanceof EntitySnowball) ||
						(entity instanceof EntityEnderPearl)
						|| (entity instanceof EntityEnderEye)
						|| (entity instanceof EntityEgg)
						|| (entity instanceof EntityPotion)
						|| (entity instanceof EntityExpBottle)
						|| (entity instanceof EntityItem)
						|| (entity instanceof EntitySquid)) {
					threshold = 64;
				} else if ((entity instanceof EntityMinecart)
						|| (entity instanceof EntityBoat)
						|| (entity instanceof IAnimals)) {
					threshold = 80;
				} else if ((entity instanceof EntityDragon)
						|| (entity instanceof EntityTNTPrimed)
						|| (entity instanceof EntityFallingBlock)
						|| (entity instanceof EntityPainting)
						|| (entity instanceof EntityXPOrb)) {
					threshold = 160;
				}

				double distance = entity.getDistance(WDL.thePlayer.posX,
						entity.posY, WDL.thePlayer.posZ);

				if (distance > threshold) {
					WDL.chatDebug("removeEntityFromWorld: Refusing to remove "
							+ EntityList.getEntityString(entity)
							+ " at distance " + distance);
					return true;
				}

				WDL.chatDebug("removeEntityFromWorld: Removing "
						+ EntityList.getEntityString(entity) + " at distance "
						+ distance);
			}
		}

		return false;
	}

	/** Load the previously saved TileEntities and add them to the Chunk **/
	public static void importTileEntities(Chunk chunk) {
		File chunkSaveLocation = (File) stealAndGetField(chunkLoader,
				File.class);
		DataInputStream dis = RegionFileCache.getChunkInputStream(
				chunkSaveLocation, chunk.xPosition, chunk.zPosition);

		try {
			NBTTagCompound chunkNBT = CompressedStreamTools.read(dis);
			// NBTTagCompound levelNBT = chunkNBT.getCompoundTag( "Level" );
			NBTTagCompound levelNBT = chunkNBT.getCompoundTag("Level");
			// The official code checks if the chunk is in the right location.
			// Should I too?.
			NBTTagList tileEntitiesNBT = levelNBT
					.getTagList("TileEntities", 10);

			if (tileEntitiesNBT != null) {
				for (int i = 0; i < tileEntitiesNBT.tagCount(); i++) {
					NBTTagCompound tileEntityNBT = tileEntitiesNBT
							.getCompoundTagAt(i);
					TileEntity te = TileEntity
							.createAndLoadEntity(tileEntityNBT);
					String entityType = null;

					if ((entityType = isImportableTileEntity(te)) != null) {
						if (!newTileEntities.contains(te.getPos())) {
							worldClient.setTileEntity(te.getPos(), te);
							chatDebug("Loaded TE: " + entityType + " at "
									+ te.getPos());
						} else {
							chatDebug("Dropping old TE: " + entityType + " at "
									+ te.getPos());
						}
					} else {
						chatDebug("Old TE is not importable: " + entityType
								+ " at " + te.getPos());
					}
				}
			}
		} catch (Exception e) {
		} // Couldn't load the old chunk. Nothing unusual. Happens with every

		// not downloaded chunk.
	}

	/**
	 * Checks if the TileEntity should be imported. Only "problematic" (IE,
	 * those that require manual interaction such as chests) TileEntities
	 * will be imported.
	 */
	public static String isImportableTileEntity(TileEntity te) {
		Block block = te.getBlockType();

		if (block instanceof BlockChest && te instanceof TileEntityChest) {
			return "TileEntityChest";
		} else if (block instanceof BlockDispenser
				&& te instanceof TileEntityDispenser) {
			return "TileEntityDispenser";
		} else if (block instanceof BlockFurnace
				&& te instanceof TileEntityFurnace) {
			return "TileEntityFurnace";
		} else if (block instanceof BlockNote && te instanceof TileEntityNote) {
			return "TileEntityNote";
		} else if (block instanceof BlockBrewingStand
				&& te instanceof TileEntityBrewingStand) {
			return "TileEntityBrewingStand";
		} else if (block instanceof BlockHopper
				&& te instanceof TileEntityHopper) {
			return "TileEntityHopper";
		} else if (block instanceof BlockBeacon
				&& te instanceof TileEntityBeacon) {
			return "TileEntityBeacon";
		} else {
			return null;
		}
	}

	/**
	 * Saves all remaining chunks, world info and player info. Usually called
	 * when stopping.
	 */
	public static void saveEverything() throws Exception {
		saveProps();

		try {
			saveHandler.checkSessionLock();
		} catch (MinecraftException e) {
			throw new RuntimeException(
				"WorldDownloader: Couldn't get session lock for saving the world!", e);
		}

		NBTTagCompound playerNBT = new NBTTagCompound();
		thePlayer.writeToNBT(playerNBT);
		applyOverridesToPlayer(playerNBT);
		ISaveHandler saveHAndler = worldClient.getSaveHandler();
		AnvilSaveConverter saveConverter = (AnvilSaveConverter) minecraft
				.getSaveLoader();
		worldClient.getWorldInfo()
		.setSaveVersion(getSaveVersion(saveConverter));
		NBTTagCompound worldInfoNBT = worldClient.getWorldInfo()
				.cloneNBTCompound(playerNBT);
		applyOverridesToWorldInfo(worldInfoNBT);
		savePlayer(playerNBT);
		saveWorldInfo(worldInfoNBT);
		saveChunks();
	}

	/**
	 * Save the player (position, health, inventory, ...) into its own file in
	 * the players directory
	 */
	public static void savePlayer(NBTTagCompound playerNBT) {
		chatDebug("Saving player data...");

		try {
			File playersDirectory = new File(saveHandler.getWorldDirectory(),
					"playerdata");
			File playerFile = new File(playersDirectory, thePlayer
					.getUniqueID().toString() + ".dat.tmp");
			File playerFileOld = new File(playersDirectory, thePlayer
					.getUniqueID().toString() + ".dat");
			CompressedStreamTools.writeCompressed(playerNBT,
					new FileOutputStream(playerFile));

			if (playerFileOld.exists()) {
				playerFileOld.delete();
			}

			playerFile.renameTo(playerFileOld);
		} catch (Exception e) {
			throw new RuntimeException("Couldn't save the player!", e);
		}

		chatDebug("Player data saved.");
	}

	/**
	 * Save the world metadata (time, gamemode, seed, ...) into the level.dat
	 * file
	 */
	public static void saveWorldInfo(NBTTagCompound worldInfoNBT) {
		chatDebug("Saving world metadata...");
		File saveDirectory = saveHandler.getWorldDirectory();
		NBTTagCompound dataNBT = new NBTTagCompound();
		dataNBT.setTag("Data", worldInfoNBT);

		try {
			File dataFile = new File(saveDirectory, "level.dat_new");
			File dataFileBackup = new File(saveDirectory, "level.dat_old");
			File dataFileOld = new File(saveDirectory, "level.dat");
			CompressedStreamTools.writeCompressed(dataNBT,
					new FileOutputStream(dataFile));

			if (dataFileBackup.exists()) {
				dataFileBackup.delete();
			}

			dataFileOld.renameTo(dataFileBackup);

			if (dataFileOld.exists()) {
				dataFileOld.delete();
			}

			dataFile.renameTo(dataFileOld);

			if (dataFile.exists()) {
				dataFile.delete();
			}
		} catch (Exception e) {
			throw new RuntimeException("Couldn't save the world metadata!", e);
		}

		chatDebug("World data saved.");
	}

	/**
	 * Calls saveChunk for all currently loaded chunks
	 *
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static void saveChunks() throws IllegalArgumentException,
		IllegalAccessException {
		chatDebug("Saving chunks...");
		// Get the ChunkProviderClient from WorldClient
		ChunkProviderClient chunkProvider = (ChunkProviderClient) worldClient
				.getChunkProvider();
		// Get the hashArray field and set it accessible
		Field hashArrayField = null;
		Field[] lhmFields = LongHashMap.class.getDeclaredFields();

		// System.out.println("Looking for hashArray field...");
		for (Field f : lhmFields) {
			// System.out.println("Found field " + f.getName() + " of type " +
			// f.getType().getName());
			if (f.getType().isArray()) {
				hashArrayField = f;
				break;
			}
		}

		if (hashArrayField == null) {
			chatMsg("Could not save chunks. Reflection error.");
			return;
		}

		// System.out.println("Setting hashArrayField of type " +
		// hashArrayField.getType().getName() + " accessible.");
		hashArrayField.setAccessible(true);
		// Steal the instance of LongHashMap from our chunk provider
		// System.out.println("Stealing field from chunkProvider (type=" +
		// chunkProvider.getClass().getName() + ") of type " +
		// LongHashMap.class.getName());
		LongHashMap lhm = (LongHashMap) stealAndGetField(chunkProvider,
				LongHashMap.class);
		/*
		 * if (lhm != null) { System.out.println("Successfully got lhm of type"
		 * + lhm.getClass().getName()); }
		 */
		// Get the LongHashMap.Entry[] through the now accessible field using a
		// LongHashMap we steal from our chunkProvider.
		Object[] hashArray = (Object[]) hashArrayField.get(lhm);
		// System.out.println("hashArray is of type " +
		// hashArray.getClass().getName());

		// System.out.println("hashArray.length = " + hashArray.length);
		if (hashArray.length == 0) {
			chatError("ChunkProviderClient has no chunk data!");
			return;
		} else {
			// Get the actual class for LongHashMap.Entry
			Class<?> Entry = null;

			for (Object o : hashArray) {
				if (o != null) {
					Entry = o.getClass();
					break;
				}
			}

			if (Entry == null) {
				chatError("Could not get class for LongHashMap.Entry.");
				return;
			}

			// Find the private fields for 'value' and 'nextEntry' in
			// LongHashMap.Entry and make them accessible
			Field valueField = Entry.getDeclaredFields()[1]; // value
			valueField.setAccessible(true);
			Field nextEntryField = Entry.getDeclaredFields()[2]; // nextEntry
			nextEntryField.setAccessible(true);
			WDLSaveProgressReporter progressReporter = new WDLSaveProgressReporter();
			progressReporter.currentChunk = 0;
			//TODO hashArray.length probably isn't the right field.
			progressReporter.totalChunks = lhm.getNumHashElements();
			progressReporter.start();

			for (int i = 0; i < hashArray.length; ++i) {
				// for (LongHashMap.Entry entry = hashArray[i]; entry != null;
				// entry = entry.nextEntry)
				for (Object lhme = hashArray[i]; lhme != null; lhme = nextEntryField
						.get(lhme)) {
					// Chunk c = (Chunk)lhme.getValue();
					Chunk c = (Chunk) valueField.get(lhme);

					if (c != null) {
						saveChunk(c);
					}
				}
			}

			try {
				// func_178779_a is a getter for the intsance.
				// Look inside of ThreadedFileIOBase.java for
				// such a getter.
				ThreadedFileIOBase.func_178779_a().waitForFinish();
			} catch (Exception e) {
				throw new RuntimeException("Threw exception waiting for asynchronous IO to finish. Hmmm.", e);
			}

			chatDebug("Chunk data saved.");
		}
	}

	/**
	 * Import all non-overwritten TileEntities, then save the chunk
	 */
	public static void saveChunk(Chunk c) {
		//Force the entity into its serverside location.
		//Needed for certain things that move clientside,
		//such as my glitchboats (http://imgur.com/3QQchZL)
		for (Iterable<Entity> entityList : c.getEntityLists()) {
			for (Entity e : entityList) {
				e.posX = convertServerPos(e.serverPosX);
				e.posY = convertServerPos(e.serverPosY);
				e.posZ = convertServerPos(e.serverPosZ);
			}
		}

		// chatMsg( "saveChunk at " + c.xPosition + " " + c.zPosition);
		importTileEntities(c);
		c.setTerrainPopulated(true);

		try {
			chunkLoader.saveChunk(worldClient, c);
		} catch (Exception e) {
			// Better tell the player that something didn't work:
			chatMsg("Chunk at chunk position " + c.xPosition + ","
					+ c.zPosition + " can't be saved!");
		}

		WDLSaveProgressReporter.currentChunk++;
	}

	/** Loads the server specific set of properties */
	public static void loadBaseProps() {
		baseFolderName = getBaseFolderName();
		baseProps = new Properties(defaultProps);

		try {
			baseProps.load(new FileReader(new File(minecraft.mcDataDir,
					"saves/" + baseFolderName + "/WorldDownloader.txt")));
			propsFound = true;
		} catch (FileNotFoundException e) {
			propsFound = false;
		} catch (Exception e) {
		}

		if (baseProps.getProperty("LinkedWorlds").isEmpty()) {
			isMultiworld = false;
			worldProps = new Properties(baseProps);
		} else {
			isMultiworld = true;
		}
	}

	/** Loads the world specific set of properties */
	public static Properties loadWorldProps(String theWorldName) {
		Properties ret = new Properties(baseProps);

		if (!theWorldName.isEmpty()) {
			String folder = getWorldFolderName(theWorldName);

			try {
				ret.load(new FileReader(new File(minecraft.mcDataDir, "saves/"
						+ folder + "/WorldDownloader.txt")));
			} catch (Exception e) {
				return null;
			}
		}

		return ret;
	}

	/**
	 * Saves the currently used base and world properties in the corresponding
	 * folders
	 */
	public static void saveProps() {
		saveProps(worldName, worldProps);
	}

	/**
	 * Saves the specified world properties and the base properties in the
	 * corresponding folders
	 */
	public static void saveProps(String theWorldName, Properties theWorldProps) {
		if (theWorldName.length() > 0) {
			String folder = getWorldFolderName(theWorldName);

			try {
				theWorldProps.store(new FileWriter(new File(
						minecraft.mcDataDir, "saves/" + folder
						+ "/WorldDownloader.txt")), "");
			} catch (Exception e) {
			}
		} else if (!isMultiworld) {
			baseProps.putAll(theWorldProps);
		}

		File baseFolder = new File(minecraft.mcDataDir, "saves/"
				+ baseFolderName);
		baseFolder.mkdirs();

		try {
			baseProps.store(new FileWriter(new File(baseFolder,
					"WorldDownloader.txt")), "");
		} catch (Exception e) {
		}
	}

	/**
	 * Change player specific fields according to the overrides found in the
	 * properties file
	 */
	public static void applyOverridesToPlayer(NBTTagCompound playerNBT) {
		// Health
		String health = worldProps.getProperty("PlayerHealth");

		if (!health.equals("keep")) {
			short h = Short.parseShort(health);
			playerNBT.setShort("Health", h);
		}

		// foodLevel, foodTimer, foodSaturationLevel, foodExhaustionLevel
		String food = worldProps.getProperty("PlayerFood");

		if (!food.equals("keep")) {
			int f = Integer.parseInt(food);
			playerNBT.setInteger("foodLevel", f);
			playerNBT.setInteger("foodTickTimer", 0);

			if (f == 20) {
				playerNBT.setFloat("foodSaturationLevel", 5.0f);
			} else {
				playerNBT.setFloat("foodSaturationLevel", 0.0f);
			}

			playerNBT.setFloat("foodExhaustionLevel", 0.0f);
		}

		// Player Position
		String playerPos = worldProps.getProperty("PlayerPos");

		if (playerPos.equals("xyz")) {
			int x = Integer.parseInt(worldProps.getProperty("PlayerX"));
			int y = Integer.parseInt(worldProps.getProperty("PlayerY"));
			int z = Integer.parseInt(worldProps.getProperty("PlayerZ"));
			//Positions are offset to center of block,
			//or player height.
			NBTTagList pos = new NBTTagList();
			pos.appendTag(new NBTTagDouble(x + 0.5D));
			pos.appendTag(new NBTTagDouble(y + 0.621D));
			pos.appendTag(new NBTTagDouble(z + 0.5D));
			playerNBT.setTag("Pos", pos);
			NBTTagList motion = new NBTTagList();
			motion.appendTag(new NBTTagDouble(0.0D));
			//Force them to land on the ground?
			motion.appendTag(new NBTTagDouble(-0.0001D));
			motion.appendTag(new NBTTagDouble(0.0D));
			playerNBT.setTag("Motion", motion);
			NBTTagList rotation = new NBTTagList();
			rotation.appendTag(new NBTTagFloat(0.0f));
			rotation.appendTag(new NBTTagFloat(0.0f));
			playerNBT.setTag("Rotation", rotation);
		}
	}

	/**
	 * Change world and generator specific fields according to the overrides
	 * found in the properties file
	 */
	public static void applyOverridesToWorldInfo(NBTTagCompound worldInfoNBT) {
		// LevelName
		String baseName = baseProps.getProperty("ServerName");
		String worldName = worldProps.getProperty("WorldName");

		if (worldName.isEmpty()) {
			worldInfoNBT.setString("LevelName", baseName);
		} else {
			worldInfoNBT.setString("LevelName", baseName + " - " + worldName);
		}

		// GameType
		String gametypeOption = worldProps.getProperty("GameType");

		if (gametypeOption.equals("keep")) {
			if (thePlayer.capabilities.isCreativeMode) { // capabilities
				worldInfoNBT.setInteger("GameType", 1); // Creative
			} else {
				worldInfoNBT.setInteger("GameType", 0); // Survival
			}
		} else if (gametypeOption.equals("survival")) {
			worldInfoNBT.setInteger("GameType", 0);
		} else if (gametypeOption.equals("creative")) {
			worldInfoNBT.setInteger("GameType", 1);
		} else if (gametypeOption.equals("hardcore")) {
			worldInfoNBT.setInteger("GameType", 0);
			worldInfoNBT.setBoolean("hardcore", true);
		}

		// Time
		String timeOption = worldProps.getProperty("Time");

		if (!timeOption.equals("keep")) {
			long t = Integer.parseInt(timeOption);
			worldInfoNBT.setLong("Time", t);
		}

		// RandomSeed
		String randomSeed = worldProps.getProperty("RandomSeed");
		long seed = 0;

		if (!randomSeed.isEmpty()) {
			try {
				seed = Long.parseLong(randomSeed);
			} catch (NumberFormatException numberformatexception) {
				seed = randomSeed.hashCode();
			}
		}

		worldInfoNBT.setLong("RandomSeed", seed);
		// MapFeatures
		boolean mapFeatures = Boolean.parseBoolean(worldProps
				.getProperty("MapFeatures"));
		worldInfoNBT.setBoolean("MapFeatures", mapFeatures);
		// generatorName
		String generatorName = worldProps.getProperty("GeneratorName");
		worldInfoNBT.setString("generatorName", generatorName);
		// generatorVersion
		int generatorVersion = Integer.parseInt(worldProps
				.getProperty("GeneratorVersion"));
		worldInfoNBT.setInteger("generatorVersion", generatorVersion);
		// Weather
		String weather = worldProps.getProperty("Weather");

		if (weather.equals("sunny")) {
			worldInfoNBT.setBoolean("raining", false);
			worldInfoNBT.setInteger("rainTime", 0);
			worldInfoNBT.setBoolean("thundering", false);
			worldInfoNBT.setInteger("thunderTime", 0);
		}

		if (weather.equals("rain")) {
			worldInfoNBT.setBoolean("raining", true);
			worldInfoNBT.setInteger("rainTime", 24000);
			worldInfoNBT.setBoolean("thundering", false);
			worldInfoNBT.setInteger("thunderTime", 0);
		}

		if (weather.equals("thunderstorm")) {
			worldInfoNBT.setBoolean("raining", true);
			worldInfoNBT.setInteger("rainTime", 24000);
			worldInfoNBT.setBoolean("thundering", true);
			worldInfoNBT.setInteger("thunderTime", 24000);
		}

		// Spawn
		String spawn = worldProps.getProperty("Spawn");

		if (spawn.equals("player")) {
			int x = (int) Math.floor(thePlayer.posX);
			int y = (int) Math.floor(thePlayer.posY);
			int z = (int) Math.floor(thePlayer.posZ);
			worldInfoNBT.setInteger("SpawnX", x);
			worldInfoNBT.setInteger("SpawnY", y);
			worldInfoNBT.setInteger("SpawnZ", z);
			worldInfoNBT.setBoolean("initialized", true);
		} else if (spawn.equals("xyz")) {
			int x = Integer.parseInt(worldProps.getProperty("SpawnX"));
			int y = Integer.parseInt(worldProps.getProperty("SpawnY"));
			int z = Integer.parseInt(worldProps.getProperty("SpawnZ"));
			worldInfoNBT.setInteger("SpawnX", x);
			worldInfoNBT.setInteger("SpawnY", y);
			worldInfoNBT.setInteger("SpawnZ", z);
			worldInfoNBT.setBoolean("initialized", true);
		}
	}

	/** Get the name of the server the user specified it in the server list */
	public static String getServerName() {
		try {
			if (minecraft.getCurrentServerData() != null) {
				String name = minecraft.getCurrentServerData().serverName;

				if (name.equals(I18n.format("selectServer.defaultName"))) {
					// Direct connection using domain name or IP (and port)
					name = minecraft.getCurrentServerData().serverIP;
				}

				return name;
			}
		} catch (Exception e) {
		}

		return "Unidentified Server";
	}

	/** Get the base folder name for the server we are connected to */
	public static String getBaseFolderName() {
		return getServerName().replaceAll("\\W+", "_");
	}

	/** Get the folder name for the specified world */
	public static String getWorldFolderName(String theWorldName) {
		if (theWorldName.isEmpty()) {
			return baseFolderName;
		} else {
			return baseFolderName + " - " + theWorldName;
		}
	}

	/**
	 * Saves the items of a container to the given TileEntity.
	 *
	 * @param contaioner The container to save from -- usually
	 *                   {@link #windowContainer}.
	 * @param tileEntity The TileEntity to save to.
	 * @param startInContainerAt The position to start at in the
	 *                           container, for saving.
	 */
	public static void saveContainerItems(Container contaioner,
			IInventory tileEntity, int startInContainerAt) {
		int containerSize = contaioner.inventorySlots.size();
		int inventorySize = tileEntity.getSizeInventory();
		int nc = startInContainerAt;
		int ni = 0;

		while ((nc < containerSize) && (ni < inventorySize)) {
			ItemStack is = contaioner.getSlot(nc).getStack();
			tileEntity.setInventorySlotContents(ni, is);
			ni++;
			nc++;
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

	/** Adds a chat message with a World Downloader prefix */
	public static void chatMsg(String msg) {
		minecraft.ingameGUI.getChatGUI().printChatMessage(
			new ChatComponentText("§c[WorldDL]§6 " + msg));
	}

	/** Adds a chat message with a World Downloader prefix */
	public static void chatDebug(String msg) {
		if (!WDL.DEBUG) {
			return;
		}

		minecraft.ingameGUI.getChatGUI().printChatMessage(
			new ChatComponentText("§2[WorldDL]§6 " + msg));
	}

	/** Adds a chat message with a World Downloader prefix */
	public static void chatError(String msg) {
		minecraft.ingameGUI.getChatGUI().printChatMessage(
			new ChatComponentText("§2[WorldDL]§4 " + msg));
	}

	private static int getSaveVersion(AnvilSaveConverter asc) {
		int saveVersion = 0;

		try {
			Method[] anvilMethods = AnvilSaveConverter.class
					.getDeclaredMethods();

			for (Method m : anvilMethods) {
				if (m.getParameterTypes().length == 0
						&& m.getReturnType().equals(int.class)) {
					m.setAccessible(true);
					saveVersion = (Integer) m.invoke(asc);
					break;
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}

		if (saveVersion == 0) {
			saveVersion = 19133; // Version for 1.7.2 just in case we can't get
			// it
		}

		return saveVersion;
	}

	/**
	 * Uses Java's reflection API to get access to an unaccessible field
	 *
	 * @param typeOfClass
	 *            Class that the field should be read from
	 * @param typeOfField
	 *            The type of the field
	 * @return An Object of type Field
	 */
	public static Field stealField(Class typeOfClass, Class typeOfField) {
		// System.out.println("stealField: typeOfClass = " +
		// typeOfClass.getName());
		// System.out.println("stealField: typeOfField = " +
		// typeOfField.getName());
		Field[] fields = typeOfClass.getDeclaredFields();

		for (Field f : fields) {
			// System.out.println("stealField: Found field " + f.getName() +
			// " of type " + f.getType());
			if (f.getType().equals(typeOfField)) {
				try {
					f.setAccessible(true);
					return f;
				} catch (Exception e) {
					throw new RuntimeException(
						"WorldDownloader: Couldn't steal Field of type \""
						+ typeOfField + "\" from class \"" + typeOfClass
						+ "\" !", e);
				}
			}
		}

		throw new RuntimeException(
			"WorldDownloader: Couldn't steal Field of type \""
			+ typeOfField + "\" from class \"" + typeOfClass
			+ "\" !");
	}

	/**
	 * Uses Java's reflection API to get access to an unaccessible field
	 *
	 * @param object
	 *            Object that the field should be read from or the type of the
	 *            object if the field is static
	 * @param typeOfField
	 *            The type of the field
	 * @return The value of the field
	 */
	public static Object stealAndGetField(Object object, Class typeOfField) {
		Class typeOfObject;

		if (object instanceof Class) { // User asked for static field:
			typeOfObject = (Class) object;
			object = null;
		} else {
			typeOfObject = object.getClass();
		}

		try {
			Field f = stealField(typeOfObject, typeOfField);
			return f.get(object);
		} catch (Exception e) {
			throw new RuntimeException(
				"WorldDownloader: Couldn't get Field of type \""
				+ typeOfField + "\" from object \"" + object
				+ "\" !", e);
		}
	}

	/**
	 * Uses Java's reflection API to set the value of an unaccessible field
	 *
	 * @param object
	 *            Object that the field should be read from or the type of the
	 *            object if the field is static
	 * @param typeOfField
	 *            The type of the field
	 * @param value
	 *            The value to set the field to.
	 */
	public static void stealAndSetField(Object object, Class typeOfField,
			Object value) {
		Class typeOfObject;

		if (object instanceof Class) { // User asked for static field:
			typeOfObject = (Class) object;
			object = null;
		} else {
			typeOfObject = object.getClass();
		}

		try {
			Field f = stealField(typeOfObject, typeOfField);
			f.set(object, value);
		} catch (Exception e) {
			throw new RuntimeException(
				"WorldDownloader: Couldn't set Field of type \""
				+ typeOfField + "\" from object \"" + object
				+ "\" to " + value + "!", e);
		}
	}

	public static void handleServerSeedMessage(String msg) {
		if (downloading && msg.startsWith("Seed: ")) {
			String seed = msg.substring(6);
			worldProps.setProperty("RandomSeed", seed);
			WDL.chatMsg("Setting single-player world seed to " + seed);
		}

		/*
		 * else { WDL.chatMsg("Could not retrieve server seed"); }
		 */
	}

	// Add World Downloader buttons to GuiIngameMenu
	public static void injectWDLButtons(GuiIngameMenu gui, List buttonList) {
		if (minecraft.isIntegratedServerRunning()) {
			return; // WDL not available if in singleplayer or LAN server mode
		}

		int insertAtYPos = 0;

		for (Object obj : buttonList) {
			GuiButton btn = (GuiButton) obj;

			if (btn.id == 5) { // Button "Achievements"
				insertAtYPos = btn.yPosition + 24;
				break;
			}
		}

		// Move other buttons down one slot (= 24 height units)
		for (Object obj : buttonList) {
			GuiButton btn = (GuiButton) obj;

			if (btn.yPosition >= insertAtYPos) {
				btn.yPosition += 24;
			}
		}

		// Insert buttons... The IDs are chosen to be unique (hopefully). They
		// are ASCII encoded strings: "WDLs" and "WDLo"
		GuiButton wdlDownload = new GuiButton(0x57444C73, gui.width / 2 - 100,
				insertAtYPos, 170, 20, "WDL bug!");
		GuiButton wdlOptions = new GuiButton(0x57444C6F, gui.width / 2 + 71,
				insertAtYPos, 28, 20, "...");
		wdlDownload.displayString = (WDL.downloading ? (WDL.saving ? "Still saving..."
				: "Stop download")
				: "Download this world");
		wdlDownload.enabled = (!WDL.downloading || (WDL.downloading && !WDL.saving));
		wdlOptions.enabled = (!WDL.downloading || (WDL.downloading && !WDL.saving));
		buttonList.add(wdlDownload);
		buttonList.add(wdlOptions);
	}

	public static void handleWDLButtonClick(GuiIngameMenu gui, GuiButton button) {
		if (minecraft.isIntegratedServerRunning()) {
			return; // WDL not available if in singleplayer or LAN server mode
		}

		if (button.id == 0x57444C73) { // "Start/Stop Download"
			if (WDL.downloading) {
				WDL.stop();
				WDL.minecraft.displayGuiScreen((GuiScreen) null);
				WDL.minecraft.setIngameFocus();
			} else {
				WDL.start();
			}
		} else if (button.id == 0x57444C6F) { // "..." (options)
			WDL.minecraft.displayGuiScreen(new GuiWDL(gui));
		} else if (button.id == 1) { // "Disconnect"
			WDL.stop();
		}
	}

	/**
	 * Converts a position from the fixed-point version that a packet
	 * (or {@link Entity#serverPosX} and the like use) into a double.
	 *
	 * @see
	 *      <a href="http://wiki.vg/Protocol#Fixed-point_numbers">
	 *      wiki.vg on Fixed-point numbers</a>
	 *
	 * @param serverPos
	 * @return The double version of the position.
	 */
	public static double convertServerPos(int serverPos) {
		return serverPos / 32.0;
	}
}
