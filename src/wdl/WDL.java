package wdl;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBeacon;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockNote;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
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
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ClassInheratanceMultiMap;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.ThreadedFileIOBase;
import wdl.WorldBackup.WorldBackupType;
import wdl.api.IWDLMessageType;
import wdl.api.IWDLMod;
import wdl.api.WDLApi;
import wdl.gui.GuiWDLMultiworld;
import wdl.gui.GuiWDLMultiworldSelect;
import wdl.gui.GuiWDLOverwriteChanges;
import wdl.gui.GuiWDLSaveProgress;

/**
 * This is the main class that does most of the work.
 */
public class WDL {
	public static final String VERSION = "1.8d";
	
	// TODO: This class needs to be split into smaller classes. There is way too
	// much different stuff in here.

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
	 * Positions and contents of all of the new tileentities, to be overwritten
	 * from the old version when {@linkplain #importTileEntities(Chunk) saving}.
	 */
	public static HashMap<BlockPos, TileEntity> newTileEntities = new HashMap<BlockPos, TileEntity>();
	
	/**
	 * All entities that were downloaded.  The key is the entity's EID.
	 */
	public static HashMap<Integer, Entity> newEntities = new HashMap<Integer, Entity>();
	
	/**
	 * All of the {@link MapData}s that were sent to the client in the current
	 * world.
	 */
	public static HashMap<Integer, MapData> newMapDatas = new HashMap<Integer, MapData>();
	
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
	 * Whether to ignore the check as to whether a player
	 * previously modified the world before downloading it.
	 */
	public static boolean overrideLastModifiedCheck = false;

	/**
	 * Is the world currently being saved?
	 */
	public static boolean saving = false;
	/**
	 * Has loading the world been delayed while the old one is being saved?
	 * 
	 * Used when going thru portals or otherwise saving data.
	 */
	public static boolean worldLoadingDeferred = false;

	// Names:
	/**
	 * The current world name, if the world is multiworld.
	 */
	public static String worldName = "WorldDownloaderERROR";
	/**
	 * The folder in which worlds are being saved.
	 */
	public static String baseFolderName = "WorldDownloaderERROR";

	// Properties:
	/**
	 * Base properties, shared between each world on a multiworld server.
	 */
	public static Properties baseProps;
	/**
	 * Properties for a single world on a multiworld server, or all worlds
	 * on a single world server.
	 */
	public static Properties worldProps;
	/**
	 * Default properties used for creating baseProps.  Can be changed
	 * manually.
	 */
	public static Properties defaultProps;
	/**
	 * "Super" default properties that are used if defaultProps cannot be
	 * loaded.
	 */
	public static Properties superDefaultProps;
	
	/**
	 * Check to see if the API handlers have been added yet.
	 * Used for loading purposes.
	 */
	private static boolean addedAPIHandlers = false;
	
	// Initialization:
	static {
		minecraft = Minecraft.getMinecraft();
		// Initialize the Properties template:
		superDefaultProps = new Properties();
		superDefaultProps.setProperty("ServerName", "");
		superDefaultProps.setProperty("WorldName", "");
		superDefaultProps.setProperty("LinkedWorlds", "");
		superDefaultProps.setProperty("AutoStart", "false");
		superDefaultProps.setProperty("Backup", "ZIP");
		superDefaultProps.setProperty("AllowCheats", "true");
		superDefaultProps.setProperty("GameType", "keep");
		superDefaultProps.setProperty("Time", "keep");
		superDefaultProps.setProperty("Weather", "keep");
		superDefaultProps.setProperty("MapFeatures", "false");
		superDefaultProps.setProperty("RandomSeed", "");
		superDefaultProps.setProperty("GeneratorName", "flat");
		superDefaultProps.setProperty("GeneratorVersion", "0");
		superDefaultProps.setProperty("Spawn", "player");
		superDefaultProps.setProperty("SpawnX", "8");
		superDefaultProps.setProperty("SpawnY", "127");
		superDefaultProps.setProperty("SpawnZ", "8");
		superDefaultProps.setProperty("PlayerPos", "keep");
		superDefaultProps.setProperty("PlayerX", "8");
		superDefaultProps.setProperty("PlayerY", "127");
		superDefaultProps.setProperty("PlayerZ", "8");
		superDefaultProps.setProperty("PlayerHealth", "20");
		superDefaultProps.setProperty("PlayerFood", "20");
		
		superDefaultProps.setProperty("Messages.enableAll", "true");
		
		//Set up entities.
		superDefaultProps.setProperty("Entity.TrackDistanceMode", "server");
		
		List<String> entityTypes = EntityUtils.getEntityTypes();
		for (String entity : entityTypes) {
			superDefaultProps.setProperty("Entity." + entity + ".Enabled", "true");
			superDefaultProps.setProperty("Entity." + entity + ".TrackDistance", 
					Integer.toString(EntityUtils.getDefaultEntityRange(entity)));
		}
		
		//Don't save these entities by default -- they're problematic.
		superDefaultProps.setProperty("Entity.FireworksRocketEntity.Enabled", "false");
		superDefaultProps.setProperty("Entity.EnderDragon.Enabled", "false");
		superDefaultProps.setProperty("Entity.WitherBoss.Enabled", "false");
		superDefaultProps.setProperty("Entity.PrimedTnt.Enabled", "false");
		superDefaultProps.setProperty("Entity.null.Enabled", "false"); // :(
		
		//Groups
		superDefaultProps.setProperty("EntityGroup.Other.Enabled", "true");
		superDefaultProps.setProperty("EntityGroup.Hostile.Enabled", "true");
		superDefaultProps.setProperty("EntityGroup.Passive.Enabled", "true");
		
		//Last saved time, so that you can tell if the world was modified.
		superDefaultProps.setProperty("LastSaved", "-1");
		
		defaultProps = new Properties(superDefaultProps);
		try {
			defaultProps.load(new FileReader(new File(minecraft.mcDataDir,
					"WorldDownloader.txt")));
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		
		worldProps = loadWorldProps(worldName);
		saveHandler = (SaveHandler) minecraft.getSaveLoader().getSaveLoader(
				getWorldFolderName(worldName), true);
		
		FileInputStream worldDat = null;
		try {
			long lastSaved = Long.parseLong(worldProps.getProperty("LastSaved"));
			//Can't directly use worldClient.getWorldInfo, as that doesn't use
			//the saved version.
			worldDat = new FileInputStream(new File(
					saveHandler.getWorldDirectory(), "level.dat"));
			long lastPlayed = CompressedStreamTools.readCompressed(worldDat)
					.getCompoundTag("Data").getLong("LastPlayed");
			if (!overrideLastModifiedCheck && lastSaved != -1 &&
					lastPlayed > lastSaved) {
				// The world was played later than it was saved; confirm that the
				// user is willing for possible changes they made to be overwritten.
				minecraft.displayGuiScreen(new GuiWDLOverwriteChanges(
						lastSaved, lastPlayed));
				return;
			}
		} catch (Exception e) {
			//TODO: handle this in a useful way -- will always happen
			//on new worlds.
			e.printStackTrace();
		} finally {
			if (worldDat != null) {
				try {
					worldDat.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		WDL.minecraft.displayGuiScreen((GuiScreen) null);
		WDL.minecraft.setIngameFocus();
		chunkLoader = saveHandler.getChunkLoader(worldClient.provider);
		newTileEntities = new HashMap<BlockPos, TileEntity>();
		newEntities = new HashMap<Integer, Entity>();
		newMapDatas = new HashMap<Integer, MapData>();

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

	static void startSaveThread() {
		// Indicate that we are saving
		WDL.chatMsg("Save started.");
		WDL.saving = true;
		WDLSaveAsync saver = new WDLSaveAsync();
		Thread thread = new Thread(saver, "WDL Save Thread");
		thread.start();
	}

	private static void initPluginChannels() {
		
	}

	/**
	 * Called when the world has loaded.
	 * 
	 * @return Whether on the same server.
	 */
	public static boolean loadWorld() {
		if (!addedAPIHandlers) {
			WDLApi.addWDLMod(new HologramHandler());
			addedAPIHandlers = true;
		}
		
		WDLPluginChannels.onWorldLoad();
		
		worldName = ""; // The new (multi-)world name is unknown at the moment
		worldClient = minecraft.theWorld;
		thePlayer = minecraft.thePlayer;
		windowContainer = thePlayer.openContainer;
		overrideLastModifiedCheck = false;
		// Is this a different server?
		NetworkManager newNM = thePlayer.sendQueue.getNetworkManager();

		if (networkManager != newNM) {
			loadBaseProps();
			WDLMessages.onNewServer();
			
			// Different server, different world!
			chatDebug(WDLMessageTypes.ON_WORLD_LOAD,
					"onWorldLoad: different server!");
			
			networkManager = newNM;
			
			chatDebug(WDLMessageTypes.ON_WORLD_LOAD,
					"Server brand=" + thePlayer.getClientBrand() +
					".  Using " + (isSpigot() ? "Spigot" : "Vanilla") +
							" track distances.");
			
			if (baseProps.getProperty("AutoStart").equals("true")) {
				start();
			} else {
				startOnChange = false;
			}
			
			return true;
		} else {
			// Same server, different world!
			chatDebug(WDLMessageTypes.ON_WORLD_LOAD,
					"onWorldLoad: same server!");
			
			if (startOnChange) {
				start();
			}
			
			return false;
		}
	}

	public static void onSaveComplete() {
		WDL.minecraft.getSaveLoader().flushCache();
		WDL.saveHandler.flush();
		WDL.worldClient = null;
		
		// Force the world to redraw as if the player pressed F3+A.
		// This fixes the world going invisible issue.
		WDL.minecraft.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				WDL.minecraft.renderGlobal.loadRenderers();	
			}
		});
		
		worldLoadingDeferred = false;

		// If still downloading, load the current world and keep on downloading
		if (downloading) {
			WDL.chatMsg("Save complete. Starting download again.");
			WDL.loadWorld();
			return;
		}

		WDL.chatMsg("Save complete. Your single player file is ready to play!");
	}

	/** Load the previously saved TileEntities and add them to the Chunk **/
	public static void importTileEntities(Chunk chunk) {
		File chunkSaveLocation = ReflectionUtils.stealAndGetField(chunkLoader,
				File.class);
		DataInputStream dis = RegionFileCache.getChunkInputStream(
				chunkSaveLocation, chunk.xPosition, chunk.zPosition);

		if (dis == null) {
			// This happens whenever the chunk hasn't been saved before.
			// It's a normal case.
			return;
		}
		
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
					
					te.setWorldObj(worldClient);
					
					String entityType = tileEntityNBT.getString("id") +
							" (" + te.getClass().getCanonicalName() +")";

					if (shouldImportTileEntity(te)) {
						if (!newTileEntities.containsKey(te.getPos())) {
							//The player didn't save this tile entity in
							//this download session.  So we use the old one.
							//Note that this doesn't mean that the old one's
							//a valid one; it could be empty.
							worldClient.setTileEntity(te.getPos(), te);
							chatDebug(
									WDLMessageTypes.LOAD_TILE_ENTITY,
									"Using old TE from saved file: " +
											entityType + " at " + te.getPos());
						} else {
							worldClient.setTileEntity(te.getPos(), 
									newTileEntities.get(te.getPos()));
							chatDebug(WDLMessageTypes.LOAD_TILE_ENTITY,
									"Using new TE: " + entityType + " at "
											+ te.getPos());
						}
					} else {
						chatDebug(WDLMessageTypes.LOAD_TILE_ENTITY,
								"Old TE does not need importing: "
										+ entityType + " at " + te.getPos());
					}
				}
			}
		} catch (Exception e) {
			chatError("Failed to import tile entities for chunk at " + 
					chunk.xPosition + ", " + chunk.zPosition + ": " + e);
			e.printStackTrace();
		}
	}

	/**
	 * Checks if the TileEntity should be imported. Only "problematic" (IE,
	 * those that require manual interaction such as chests) TileEntities
	 * will be imported.
	 */
	public static boolean shouldImportTileEntity(TileEntity te) {
		Block block = te.getBlockType();

		if (block instanceof BlockChest && te instanceof TileEntityChest) {
			return true;
		} else if (block instanceof BlockDispenser
				&& te instanceof TileEntityDispenser) {
			return true;
		} else if (block instanceof BlockFurnace
				&& te instanceof TileEntityFurnace) {
			return true;
		} else if (block instanceof BlockNote && te instanceof TileEntityNote) {
			return true;
		} else if (block instanceof BlockBrewingStand
				&& te instanceof TileEntityBrewingStand) {
			return true;
		} else if (block instanceof BlockHopper
				&& te instanceof TileEntityHopper) {
			return true;
		} else if (block instanceof BlockBeacon
				&& te instanceof TileEntityBeacon) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Saves all remaining chunks, world info and player info. Usually called
	 * when stopping.
	 */
	public static void saveEverything() throws Exception {
		if (!WDLPluginChannels.canDownloadInGeneral()) {
			chatError("The server forbids downloading!");
		}
		
		WorldBackupType backupType = 
				WorldBackupType.match(baseProps.getProperty("Backup", "ZIP"));
		
		GuiWDLSaveProgress progressScreen = new GuiWDLSaveProgress(
				"Saving downloaded world", 
				(backupType != WorldBackupType.NONE ? 5 : 4));
		minecraft.displayGuiScreen(progressScreen);
		
		saveProps();

		try {
			saveHandler.checkSessionLock();
		} catch (MinecraftException e) {
			throw new RuntimeException(
				"WorldDownloader: Couldn't get session lock for saving the world!", e);
		}

		progressScreen.startMajorTask("Saving player and map info", 3);
		
		progressScreen.setMinorTaskProgress("Creating NBTs", 1);
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
		savePlayer(playerNBT, progressScreen);
		saveWorldInfo(worldInfoNBT, progressScreen);
		saveMapData(progressScreen);
		saveChunks(progressScreen);
		
		saveProps();
		
		try {
			chatDebug(WDLMessageTypes.SAVING, "Waiting for ThreadedFileIOBase to finish...");
			
			progressScreen.startMajorTask("Procrastinating...", 1);
			progressScreen.setMinorTaskProgress(
					"(waiting for ThreadedFileIOBase to finish)", 1);
			
			// func_178779_a is a getter for the instance.
			// Look inside of ThreadedFileIOBase.java for
			// such a getter.
			ThreadedFileIOBase.func_178779_a().waitForFinish();
		} catch (Exception e) {
			throw new RuntimeException("Threw exception waiting for asynchronous IO to finish. Hmmm.", e);
		}
		
		if (backupType != WorldBackupType.NONE) {
			chatDebug(WDLMessageTypes.SAVING, "Backing up the world...");
			progressScreen.startMajorTask("Backing up world...", 1);
			progressScreen.setMinorTaskProgress(
					backupType.description, 1);
			
			try {
				WorldBackup.backupWorld(saveHandler.getWorldDirectory(), 
						getWorldFolderName(worldName), backupType);
			} catch (IOException e) {
				chatError("Error while backing up world: " + e);
				e.printStackTrace();
			}
		}
		
		progressScreen.setDoneWorking();
	}

	/**
	 * Save the player (position, health, inventory, ...) into its own file in
	 * the players directory
	 */
	public static void savePlayer(NBTTagCompound playerNBT, 
			GuiWDLSaveProgress progressScreen) {
		if (!WDLPluginChannels.canDownloadInGeneral()) { return; }
		
		chatDebug(WDLMessageTypes.SAVING, "Saving player data...");
		progressScreen.setMinorTaskProgress("Writing player data", 2);
		
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

		chatDebug(WDLMessageTypes.SAVING, "Player data saved.");
	}

	/**
	 * Save the world metadata (time, gamemode, seed, ...) into the level.dat
	 * file
	 */
	public static void saveWorldInfo(NBTTagCompound worldInfoNBT,
			GuiWDLSaveProgress progressScreen) {
		if (!WDLPluginChannels.canDownloadInGeneral()) { return; }
		
		chatDebug(WDLMessageTypes.SAVING, "Saving world metadata...");
		progressScreen.setMinorTaskProgress("Writing world data", 3);
		
		File saveDirectory = saveHandler.getWorldDirectory();
		NBTTagCompound dataNBT = new NBTTagCompound();
		dataNBT.setTag("Data", worldInfoNBT);
		
		worldProps.setProperty("LastSaved",
				Long.toString(worldInfoNBT.getLong("LastPlayed")));

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

		chatDebug(WDLMessageTypes.SAVING, "World data saved.");
	}

	/**
	 * Calls saveChunk for all currently loaded chunks
	 *
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static void saveChunks(GuiWDLSaveProgress progressScreen)
			throws IllegalArgumentException, IllegalAccessException {
		if (!WDLPluginChannels.canDownloadInGeneral()) { return; }
		
		chatDebug(WDLMessageTypes.SAVING, "Saving chunks...");
		
		// Get the ChunkProviderClient from WorldClient
		ChunkProviderClient chunkProvider = (ChunkProviderClient) worldClient
				.getChunkProvider();
		// Get the hashArray field and set it accessible
		Field hashArrayField = null;
		Field[] lhmFields = LongHashMap.class.getDeclaredFields();

		for (Field f : lhmFields) {
			if (f.getType().isArray()) {
				hashArrayField = f;
				break;
			}
		}

		if (hashArrayField == null) {
			chatMsg("Could not save chunks. Reflection error.");
			return;
		}

		hashArrayField.setAccessible(true);
		// Steal the instance of LongHashMap from our chunk provider
		LongHashMap lhm = ReflectionUtils.stealAndGetField(chunkProvider,
				LongHashMap.class);
		
		progressScreen.startMajorTask("Saving chunks", 
				lhm.getNumHashElements());
		
		// Get the LongHashMap.Entry[] through the now accessible field using a
		// LongHashMap we steal from our chunkProvider.
		Object[] hashArray = (Object[]) hashArrayField.get(lhm);

		if (lhm.getNumHashElements() == 0 || hashArray.length == 0) {
			chatError("ChunkProviderClient has no chunk data!");
			chatError("(If you changed dimensions just now, this is normal)");
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
			
			int currentChunk = 0;
			
			for (int i = 0; i < hashArray.length; ++i) {
				for (Object lhme = hashArray[i]; lhme != null; 
						lhme = nextEntryField.get(lhme)) {
					Chunk c = (Chunk) valueField.get(lhme);

					if (c != null) {
						currentChunk++;
						
						//Serverside restrictions check
						if (!WDLPluginChannels.canSaveChunk(c)) {
							continue;
						}
						
						progressScreen.setMinorTaskProgress(
								"Saving chunk at " + c.xPosition + ", " +
										c.zPosition, currentChunk);
						
						saveChunk(c);
					}
				}
			}

			chatDebug(WDLMessageTypes.SAVING, "Chunk data saved.");
		}
	}

	/**
	 * Import all non-overwritten TileEntities, then save the chunk
	 */
	public static void saveChunk(Chunk c) {
		if (!WDLPluginChannels.canDownloadInGeneral()) { return; }
		
		if (!WDLPluginChannels.canDownloadInGeneral()) {
			c.getTileEntityMap().clear();
		}
		
		importTileEntities(c);
		c.setTerrainPopulated(true);

		try {
			ClassInheratanceMultiMap[] oldMaps = c.getEntityLists().clone();
			ClassInheratanceMultiMap[] maps = c.getEntityLists();
			
			if (!WDLPluginChannels.canSaveEntities()) {
				// Temporarily delete entities if saving them is disabled.
				for (int i = 0; i < maps.length; i++) {
					WrappedClassInheratanceMultiMap map = WrappedClassInheratanceMultiMap
							.copyOf(maps[i]);
					maps[i] = map;
					for (Entity e : (Iterable<Entity>)map) {
						if (e instanceof EntityPlayer) {
							//Skip players, as otherwise bad things happen, 
							//such as deleting the current player and causing
							//the screen to flicker.
							continue;
						}
						
						map.removeWDL(e);
					}
				}
			} else {
				// Remove entities of unwanted types.
				for (int i = 0; i < maps.length; i++) {
					WrappedClassInheratanceMultiMap map = WrappedClassInheratanceMultiMap
							.copyOf(maps[i]);
					maps[i] = map;
					for (Entity e : (Iterable<Entity>)map) {
						if (e instanceof EntityPlayer) {
							//Skip players, as otherwise bad things happen, 
							//such as deleting the current player and causing
							//the screen to flicker.
							continue;
						}
						
						if (!EntityUtils.isEntityEnabled(e)) {
							WDL.chatDebug(
									WDLMessageTypes.REMOVE_ENTITY,
									"saveChunk: Not saving "
											+ EntityUtils.getEntityType(e)
											+ " (User preference)");
							
							map.removeWDL(e);
						} else {
							String unsafeReason = EntityUtils.isUnsafeToSaveEntity(e);
							if (unsafeReason != null) {
								WDL.chatDebug(
										WDLMessageTypes.REMOVE_ENTITY,
										"saveChunk: Not saving "
												+ EntityUtils.getEntityType(e)
												+ " (not safe to save - "
												+ unsafeReason + ")");
								
								map.removeWDL(e);
							}
						}
					}
				}
				
				// Add in new entities now.
				// TODO: This is probably inefficient (as we go through ALL
				// entities that were loaded.
				for (Entity e : newEntities.values()) {
					if (e.chunkCoordX == c.xPosition &&
							e.chunkCoordZ == c.zPosition) {
						// Unkill the entity so that it doesn't despawn on
						// world load.  Note that 'isDead' is a bad name, as
						// it actually means "Delete this entity next tick",
						// not "this entitiy was killed by a player".
						e.isDead = false;
						e.posX = convertServerPos(e.serverPosX);
						e.posY = convertServerPos(e.serverPosY);
						e.posZ = convertServerPos(e.serverPosZ);
						((WrappedClassInheratanceMultiMap) maps[e.chunkCoordY])
								.addWDL(e);
					}
				}
			}
			
			// Force the entity into its serverside location.
			// Needed for certain things that move clientside,
			// such as boats (http://imgur.com/3QQchZL)
			for (Iterable<Entity> entityList : c.getEntityLists()) {
				for (Entity e : entityList) {
					e.posX = convertServerPos(e.serverPosX);
					e.posY = convertServerPos(e.serverPosY);
					e.posZ = convertServerPos(e.serverPosZ);
				}
			}
			
			chunkLoader.saveChunk(worldClient, c);
			
			// Return the entity maps to the previous state.
			for (int i = 0; i < oldMaps.length; i++) {
				maps[i] = oldMaps[i];
			}
		} catch (Exception e) {
			// Better tell the player that something didn't work:
			chatError("Chunk at chunk position " + c.xPosition + ","
					+ c.zPosition + " can't be saved!");
			chatError(e.toString());
			
			e.printStackTrace();
		}
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
		
		try {
			defaultProps.store(new FileWriter(new File(minecraft.mcDataDir,
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

		// Cheats
		if (worldProps.getProperty("AllowCheats").equals("true")) {
			worldInfoNBT.setBoolean("allowCommands", true);
		} else {
			worldInfoNBT.setBoolean("allowCommands", false);
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
	
	/**
	 * Saves existing map data.
	 * 
	 * TODO: Overwrite / create IDCounts.dat.
	 */
	public static void saveMapData(GuiWDLSaveProgress progressScreen) {
		if (!WDLPluginChannels.canSaveMaps()) { return; }
		
		File dataDirectory = new File(saveHandler.getWorldDirectory(),
				"data");
		dataDirectory.mkdirs();
		
		progressScreen.startMajorTask("Saving map item data", newMapDatas.size());
		
		chatDebug(WDLMessageTypes.SAVING, "Saving map data...");
		
		int count = 0;
		for (Map.Entry<Integer, MapData> e : newMapDatas.entrySet()) {
			count++;
			
			progressScreen.setMinorTaskProgress("Writing map #" + e.getKey(),
					count);
			
			File mapFile = new File(dataDirectory, "map_" + e.getKey() + ".dat");
			
			NBTTagCompound mapNBT = new NBTTagCompound();
			NBTTagCompound data = new NBTTagCompound();
			
			e.getValue().writeToNBT(data);
			
			mapNBT.setTag("data", data);
			
			try {
				CompressedStreamTools.writeCompressed(mapNBT,
						new FileOutputStream(mapFile));
			} catch (IOException ex) {
				throw new RuntimeException("WDL: Exception while writing " +
						"map data for map " + e.getKey() + "!", ex);
			}
		}
		
		chatDebug(WDLMessageTypes.SAVING, "Map data saved.");
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
		WDLMessages.chatMessage(WDLMessageTypes.INFO, msg);
	}

	/** Adds a chat message with a World Downloader prefix */
	public static void chatDebug(IWDLMessageType type, String msg) {
		WDLMessages.chatMessage(type, msg);
	}

	/** Adds a chat message with a World Downloader prefix */
	public static void chatError(String msg) {
		WDLMessages.chatMessage(WDLMessageTypes.ERROR, msg);
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
			//Default version for 1.8
			saveVersion = 19133;
		}

		return saveVersion;
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
	
	/**
	 * Is the current server running spigot?
	 */
	public static boolean isSpigot() {
		//getClientBrand() returns the server brand; blame MCP.
		if (thePlayer != null && thePlayer.getClientBrand() != null) {
			return thePlayer.getClientBrand().toLowerCase().contains("spigot");
		}
		return false;
	}
	
	/**
	 * Gets the current setup information.
	 */
	public static String getDebugInfo() {
		StringBuilder info = new StringBuilder();
		info.append("### CORE INFO\n\n");
		info.append("WDL version: ").append(VERSION).append('\n');
		info.append("Launched version: ")
				.append(Minecraft.getMinecraft().func_175600_c()).append('\n');
		info.append("Client brand: ")
				.append(ClientBrandRetriever.getClientModName()).append('\n');
		info.append("File location: ");
		try {
			//http://stackoverflow.com/q/320542/3991344
			String path = new File(WDL.class.getProtectionDomain()
					.getCodeSource().getLocation().toURI()).getPath();
			
			//Censor username.
			String username = System.getProperty("user.name");
			path = path.replace(username, "<USERNAME>");
			
			info.append(path);
		} catch (Exception e) {
			info.append("Unknown (").append(e.toString()).append(')');
		}
		info.append("\n\n### EXTENSIONS\n\n");
		Map<String, IWDLMod> extensions = WDLApi.getWDLMods();
		info.append(extensions.size()).append(" loaded\n");
		for (Map.Entry<String, IWDLMod> e : extensions.entrySet()) {
			info.append("\n#### ").append(e.getKey()).append("\n\n");
			try {
				info.append(WDLApi.getModInfo(e.getValue()));
			} catch (Exception ex) {
				info.append("ERROR: ").append(ex).append('\n');
				for (StackTraceElement elm : ex.getStackTrace()) {
					info.append(elm).append('\n');
				}
			}
		}
		info.append("\n### STATE\n\n");
		info.append("minecraft: ").append(minecraft).append('\n');
		info.append("worldClient: ").append(worldClient).append('\n');
		info.append("networkManager: ").append(networkManager).append('\n');
		info.append("thePlayer: ").append(thePlayer).append('\n');
		info.append("windowContainer: ").append(windowContainer).append('\n');
		info.append("lastClickedBlock: ").append(lastClickedBlock).append('\n');
		info.append("lastEntity: ").append(lastEntity).append('\n');
		info.append("saveHandler: ").append(saveHandler).append('\n');
		info.append("chunkLoader: ").append(chunkLoader).append('\n');
		info.append("newTileEntities: ").append(newTileEntities).append('\n');
		info.append("newEntities: ").append(newEntities).append('\n');
		info.append("newMapDatas: ").append(newMapDatas).append('\n');
		info.append("downloading: ").append(downloading).append('\n');
		info.append("isMultiworld: ").append(isMultiworld).append('\n');
		info.append("propsFound: ").append(propsFound).append('\n');
		info.append("startOnChange: ").append(startOnChange).append('\n');
		info.append("overrideLastModifiedCheck: ")
				.append(overrideLastModifiedCheck).append('\n');
		info.append("saving: ").append(saving).append('\n');
		info.append("worldLoadingDeferred: ").append(worldLoadingDeferred)
				.append('\n');
		info.append("worldName: ").append(worldName).append('\n');
		info.append("baseFolderName: ").append(baseFolderName).append('\n');
		info.append("addedAPIHandlers: ").append(addedAPIHandlers).append('\n');
		
		info.append("### CONNECTED SERVER\n\n");
		ServerData data = Minecraft.getMinecraft().getCurrentServerData();
		if (data == null) {
			info.append("No data\n");
		} else {
			info.append("Name: ").append(data.serverName).append('\n');
			info.append("IP: ").append(data.serverIP).append('\n');
		}
		
		info.append("\n### PROPERTIES\n\n");
		info.append("\n#### BASE\n\n");
		if (baseProps != null) {
			if (!baseProps.isEmpty()) {
				for (Map.Entry<Object, Object> e : baseProps.entrySet()) {
					info.append(e.getKey()).append(": ").append(e.getValue());
					info.append('\n');
				}
			} else {
				info.append("empty\n");
			}
		} else {
			info.append("null\n");
		}
		info.append("\n#### WORLD\n\n");
		if (worldProps != null) {
			if (!worldProps.isEmpty()) {
				for (Map.Entry<Object, Object> e : worldProps.entrySet()) {
					info.append(e.getKey()).append(": ").append(e.getValue());
					info.append('\n');
				}
			} else {
				info.append("empty\n");
			}
		} else {
			info.append("null\n");
		}
		info.append("\n#### DEFAULT\n\n");
		if (defaultProps != null) {
			if (!defaultProps.isEmpty()) {
				for (Map.Entry<Object, Object> e : defaultProps.entrySet()) {
					info.append(e.getKey()).append(": ").append(e.getValue());
					info.append('\n');
				}
			} else {
				info.append("empty\n");
			}
		} else {
			info.append("null\n");
		}
		
		return info.toString();
	}
	
	/**
	 * Call to properly crash the game when an exception is caught.
	 * 
	 * @param category
	 */
	public static void crashed(Throwable t, String category) {
		CrashReport report;
		
		if (t instanceof ReportedException) {
			CrashReport oldReport = 
					((ReportedException) t).getCrashReport();
			
			report = CrashReport.makeCrashReport(oldReport.getCrashCause(),
					category + " (" + oldReport.getCauseStackTraceOrString());
			
			try {
				//Steal crashReportSections, and replace it.
				List crashReportSectionsOld = ReflectionUtils.stealAndGetField(
						oldReport, List.class);
				List crashReportSectionsNew = ReflectionUtils.stealAndGetField(
						report, List.class);
				
				crashReportSectionsNew.addAll(crashReportSectionsOld);
			} catch (Exception e) {
				//Well... some kind of reflection error.
				//No use trying to do anything else.
				report.makeCategory(
						"An exception occured while trying to copy " +
						"the origional categories.")
						.addCrashSectionThrowable(":(", e);
			}
		} else {
			report = CrashReport.makeCrashReport(t, category);
		}
		minecraft.crashed(report);
	}
}
