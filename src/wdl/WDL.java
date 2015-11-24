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
import net.minecraft.crash.CrashReportCategory;
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
import net.minecraft.util.IChatComponent;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.ThreadedFileIOBase;
import wdl.WorldBackup.WorldBackupType;
import wdl.api.IEntityEditor;
import wdl.api.ISaveListener;
import wdl.api.ITileEntityEditor;
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
	 * Default properties used for creating baseProps.  Saved and loaded;
	 * shared between all servers.
	 */
	public static Properties globalProps;
	/**
	 * Default properties that are used to create the global properites.
	 */
	public static Properties defaultProps;
	
	/**
	 * Check to see if the API handlers have been added yet.
	 * Used for loading purposes.
	 */
	private static boolean addedAPIHandlers = false;
	
	/**
	 * All IWDLMods that implement {@link ISaveListener}.
	 */
	public static Map<String, ISaveListener> saveListeners =
			new HashMap<String, ISaveListener>();
	/**
	 * All IWDLMods that implement {@link ITileEntityEditor}.
	 */
	public static Map<String, ITileEntityEditor> tileEntityEditors =
			new HashMap<String, ITileEntityEditor>();
	/**
	 * All IWDLMods that implement {@link IEntityEditor}.
	 */
	public static Map<String, IEntityEditor> entityEditors =
			new HashMap<String, IEntityEditor>();
	
	// Initialization:
	static {
		minecraft = Minecraft.getMinecraft();
		// Initialize the Properties template:
		defaultProps = new Properties();
		defaultProps.setProperty("ServerName", "");
		defaultProps.setProperty("WorldName", "");
		defaultProps.setProperty("LinkedWorlds", "");
		defaultProps.setProperty("Backup", "ZIP");
		defaultProps.setProperty("AllowCheats", "true");
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
		
		defaultProps.setProperty("Messages.enableAll", "true");
		
		//Set up entities.
		defaultProps.setProperty("Entity.TrackDistanceMode", "server");
		
		List<String> entityTypes = EntityUtils.getEntityTypes();
		for (String entity : entityTypes) {
			defaultProps.setProperty("Entity." + entity + ".Enabled", "true");
			defaultProps.setProperty("Entity." + entity + ".TrackDistance", 
					Integer.toString(EntityUtils.getDefaultEntityRange(entity)));
		}
		
		//Don't save these entities by default -- they're problematic.
		defaultProps.setProperty("Entity.FireworksRocketEntity.Enabled", "false");
		defaultProps.setProperty("Entity.EnderDragon.Enabled", "false");
		defaultProps.setProperty("Entity.WitherBoss.Enabled", "false");
		defaultProps.setProperty("Entity.PrimedTnt.Enabled", "false");
		defaultProps.setProperty("Entity.null.Enabled", "false"); // :(
		
		//Groups
		defaultProps.setProperty("EntityGroup.Other.Enabled", "true");
		defaultProps.setProperty("EntityGroup.Hostile.Enabled", "true");
		defaultProps.setProperty("EntityGroup.Passive.Enabled", "true");
		
		//Last saved time, so that you can tell if the world was modified.
		defaultProps.setProperty("LastSaved", "-1");
		
		// Whether the 1-time tutorial has been shown.
		defaultProps.setProperty("TutorialShown", "false");
		
		globalProps = new Properties(defaultProps);
		try {
			globalProps.load(new FileReader(new File(minecraft.mcDataDir,
					"WorldDownloader.txt")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		baseProps = new Properties(globalProps);
		worldProps = new Properties(baseProps);
	}

	/**
	 * Starts the download.
	 */
	public static void startDownload() {
		worldClient = minecraft.theWorld;

		if (isMultiworld && worldName.isEmpty()) {
			// Ask the user which world is loaded
			minecraft.displayGuiScreen(new GuiWDLMultiworldSelect(I18n
					.format("wdl.gui.multiworldSelect.title.startDownload"),
					new GuiWDLMultiworldSelect.WorldSelectionCallback() {
						@Override
						public void onWorldSelected(String selectedWorld) {
							WDL.worldName = selectedWorld;
							WDL.isMultiworld = true;
							WDL.propsFound = true;
							
							minecraft.displayGuiScreen(null);
							startDownload();
						}

						@Override
						public void onCancel() {
							minecraft.displayGuiScreen(null);
							cancelDownload();
						}
					}));
			return;
		}

		if (!propsFound) {
			// Never seen this server before. Ask user about multiworlds:
			minecraft.displayGuiScreen(new GuiWDLMultiworld(new GuiWDLMultiworld.MultiworldCallback() {
				@Override
				public void onSelect(boolean enableMutliworld) {
					isMultiworld = enableMutliworld;
					
					if (isMultiworld) {
						// Ask the user which world is loaded
						// TODO: Copy-pasted code from above -- suboptimal.
						minecraft.displayGuiScreen(new GuiWDLMultiworldSelect(I18n
								.format("wdl.gui.multiworld.title.startDownload"),
								new GuiWDLMultiworldSelect.WorldSelectionCallback() {
									@Override
									public void onWorldSelected(String selectedWorld) {
										WDL.worldName = selectedWorld;
										WDL.isMultiworld = true;
										WDL.propsFound = true;
										
										minecraft.displayGuiScreen(null);
										startDownload();
									}
	
									@Override
									public void onCancel() {
										minecraft.displayGuiScreen(null);
										cancelDownload();
									}
								}));
					} else {
						baseProps.setProperty("LinkedWorlds", "");
						saveProps();
						propsFound = true;

						minecraft.displayGuiScreen(null);
						WDL.startDownload();
					}
				}
				
				@Override
				public void onCancel() {
					minecraft.displayGuiScreen(null);
					cancelDownload();
				}
			}));
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
		chatInfo("Download started");
	}

	/**
	 * Stops the download, and saves.
	 */
	public static void stopDownload() {
		if (downloading) {
			// Indicate that downloading has stopped
			downloading = false;
			startOnChange = false;
			chatInfo("Download stopped");
			startSaveThread();
		}
	}

	/**
	 * Cancels the download.
	 */
	public static void cancelDownload() {
		boolean wasDownloading = downloading;
		
		if (wasDownloading) {
			minecraft.getSaveLoader().flushCache();
			saveHandler.flush();
			worldClient = null;
			saving = false;
			downloading = false;
			worldLoadingDeferred = false;
			
			// Force the world to redraw as if the player pressed F3+A.
			// This fixes the world going invisible issue.
			minecraft.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					WDL.minecraft.renderGlobal.loadRenderers();	
				}
			});
		
			WDL.chatInfo("Download canceled.");
		}
	}

	/**
	 * Starts the asnchronous save thread.
	 */
	static void startSaveThread() {
		// Indicate that we are saving
		WDL.chatInfo("Save started.");
		WDL.saving = true;
		WDLSaveAsync saver = new WDLSaveAsync();
		Thread thread = new Thread(saver, "WDL Save Thread");
		thread.start();
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
			chatMessage(WDLMessageTypes.ON_WORLD_LOAD,
					"onWorldLoad: different server!");
			
			networkManager = newNM;
			
			chatMessage(WDLMessageTypes.ON_WORLD_LOAD,
					"Server brand=" + thePlayer.getClientBrand() +
					".  Using " + (isSpigot() ? "Spigot" : "Vanilla") +
							" track distances.");
			
			startOnChange = false;
			
			return true;
		} else {
			// Same server, different world!
			chatMessage(WDLMessageTypes.ON_WORLD_LOAD,
					"onWorldLoad: same server!");
			
			if (startOnChange) {
				startDownload();
			}
			
			return false;
		}
	}

	/**
	 * Called after saving has finished.
	 */
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
			WDL.chatInfo("Save complete. Starting download again.");
			WDL.loadWorld();
			return;
		}

		WDL.chatInfo("Save complete. Your single player file is ready to play!");
	}

	/**
	 * Load the previous version of the chunk, and copy in any previous tile
	 * entities.
	 * 
	 * Needed so that the old tile entities aren't lost.
	 */
	public static void importTileEntities(Chunk chunk) {
		File chunkSaveLocation = ReflectionUtils.stealAndGetField(chunkLoader,
				File.class);
		DataInputStream dis = null;
		
		try {
			dis = RegionFileCache.getChunkInputStream(
					chunkSaveLocation, chunk.xPosition, chunk.zPosition);

			if (dis == null) {
				// This happens whenever the chunk hasn't been saved before.
				// It's a normal case.
				return;
			}
			
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
							chatMessage(
									WDLMessageTypes.LOAD_TILE_ENTITY,
									"Using old TE from saved file: " +
											entityType + " at " + te.getPos());
						} else {
							worldClient.setTileEntity(te.getPos(), 
									newTileEntities.get(te.getPos()));
							chatMessage(WDLMessageTypes.LOAD_TILE_ENTITY,
									"Using new TE: " + entityType + " at "
											+ te.getPos());
						}
					} else {
						chatMessage(WDLMessageTypes.LOAD_TILE_ENTITY,
								"Old TE does not need importing: "
										+ entityType + " at " + te.getPos());
					}
				}
			}
		} catch (Exception e) {
			chatError("Failed to import tile entities for chunk at " + 
					chunk.xPosition + ", " + chunk.zPosition + ": " + e);
			e.printStackTrace();
		} finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
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
	 * Applies all registered {@link ITileEntityEditor}s to the given chunk.
	 * 
	 * Note: {@link #importTileEntities(Chunk)} must be called before this method.
	 */
	public static void editTileEntities(Chunk chunk) {
		for (Map.Entry<String, ITileEntityEditor> editor : tileEntityEditors
				.entrySet()) {
			try {
				@SuppressWarnings("unchecked")
				Map<BlockPos, TileEntity> tileEntityMap = (Map<BlockPos, TileEntity>) chunk
						.getTileEntityMap();
				
				for (Map.Entry<BlockPos, TileEntity> entry : tileEntityMap
						.entrySet()) {
					boolean imported = !newTileEntities.containsKey(entry.getKey());
					if (editor.getValue().shouldEdit(entry.getValue(), imported)) {
						TileEntity newTE = editor.getValue().editTileEntity(
								entry.getValue(), imported);
						entry.setValue(newTE);
					}
				}
			} catch (Exception ex) {
				String chunkInfo;
				if (chunk == null) {
					chunkInfo = "null";
				} else {
					chunkInfo = "at " + chunk.xPosition + ", " + chunk.zPosition;
				}
				throw new RuntimeException("Failed to update tile entities "
						+ "for chunk " + chunkInfo + " with extension "
						+ editor.getKey(), ex);
			}
		}
	}

	/**
	 * Saves all remaining chunks, world info and player info. Usually called
	 * when stopping.
	 */
	public static void saveEverything() throws Exception {
		if (!WDLPluginChannels.canDownloadInGeneral()) {
			chatError("The server forbids downloading!");
			return;
		}
		
		WorldBackupType backupType = 
				WorldBackupType.match(baseProps.getProperty("Backup", "ZIP"));
		
		GuiWDLSaveProgress progressScreen = new GuiWDLSaveProgress(
				"Saving downloaded world", 
				(backupType != WorldBackupType.NONE ? 5 : 4)
						+ saveListeners.size());
		minecraft.displayGuiScreen(progressScreen);
		
		saveProps();

		try {
			saveHandler.checkSessionLock();
		} catch (MinecraftException e) {
			throw new RuntimeException(
				"WorldDownloader: Couldn't get session lock for saving the world!", e);
		}

		progressScreen.startMajorTask(
				I18n.format("wdl.saveProgress.metadata.title"), 3);
		
		progressScreen.setMinorTaskProgress(
				I18n.format("wdl.saveProgress.metadata.creatingNBTs"), 1);
		NBTTagCompound playerNBT = new NBTTagCompound();
		thePlayer.writeToNBT(playerNBT);
		applyOverridesToPlayer(playerNBT);
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
		
		for (Map.Entry<String, ISaveListener> e : saveListeners.entrySet()) {
			progressScreen.startMajorTask(I18n.format(
					"wdl.saveProgress.extension.title",	e.getKey()), 1);
			e.getValue().afterChunksSaved(saveHandler.getWorldDirectory());
		}
		
		try {
			chatMessage(WDLMessageTypes.SAVING, "Waiting for ThreadedFileIOBase to finish...");
			
			progressScreen.startMajorTask(
					I18n.format("wdl.saveProgress.flushingIO.title"), 1);
			progressScreen.setMinorTaskProgress(
					I18n.format("wdl.saveProgress.flushingIO.subtitle"), 1);
			
			// func_178779_a is a getter for the instance.
			// Look inside of ThreadedFileIOBase.java for
			// such a getter.
			ThreadedFileIOBase.func_178779_a().waitForFinish();
		} catch (Exception e) {
			throw new RuntimeException("Threw exception waiting for asynchronous IO to finish. Hmmm.", e);
		}
		
		if (backupType != WorldBackupType.NONE) {
			chatMessage(WDLMessageTypes.SAVING, "Backing up the world...");
			progressScreen.startMajorTask(
					backupType.getTitle(), 1);
			progressScreen.setMinorTaskProgress(
					I18n.format("wdl.saveProgress.backingUp.preparing"), 1);
			
			try {
				WorldBackup.backupWorld(saveHandler.getWorldDirectory(), 
						getWorldFolderName(worldName), backupType, progressScreen);
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
		
		chatMessage(WDLMessageTypes.SAVING, "Saving player data...");
		progressScreen.setMinorTaskProgress(
				I18n.format("wdl.saveProgress.metadata.writingPlayer"), 2);
		
		FileOutputStream stream = null;
		try {
			File playersDirectory = new File(saveHandler.getWorldDirectory(),
					"playerdata");
			File playerFileTmp = new File(playersDirectory, thePlayer
					.getUniqueID().toString() + ".dat.tmp");
			File playerFile = new File(playersDirectory, thePlayer
					.getUniqueID().toString() + ".dat");
			
			stream = new FileOutputStream(playerFileTmp);
			
			CompressedStreamTools.writeCompressed(playerNBT, stream);

			// Remove the old player file to make space for the new one.
			if (playerFile.exists()) {
				playerFile.delete();
			}

			playerFileTmp.renameTo(playerFile);
		} catch (Exception e) {
			throw new RuntimeException("Couldn't save the player!", e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		chatMessage(WDLMessageTypes.SAVING, "Player data saved.");
	}

	/**
	 * Save the world metadata (time, gamemode, seed, ...) into the level.dat
	 * file
	 */
	public static void saveWorldInfo(NBTTagCompound worldInfoNBT,
			GuiWDLSaveProgress progressScreen) {
		if (!WDLPluginChannels.canDownloadInGeneral()) { return; }
		
		chatMessage(WDLMessageTypes.SAVING, "Saving world metadata...");
		progressScreen.setMinorTaskProgress(
				I18n.format("wdl.saveProgress.metadata.writingWorld"), 3);
		
		File saveDirectory = saveHandler.getWorldDirectory();
		NBTTagCompound dataNBT = new NBTTagCompound();
		dataNBT.setTag("Data", worldInfoNBT);
		
		worldProps.setProperty("LastSaved",
				Long.toString(worldInfoNBT.getLong("LastPlayed")));

		FileOutputStream stream = null;
		try {
			File dataFile = new File(saveDirectory, "level.dat_new");
			File dataFileBackup = new File(saveDirectory, "level.dat_old");
			File dataFileOld = new File(saveDirectory, "level.dat");
			stream = new FileOutputStream(dataFile);
			
			CompressedStreamTools.writeCompressed(dataNBT, stream);

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
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		chatMessage(WDLMessageTypes.SAVING, "World data saved.");
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
		
		chatMessage(WDLMessageTypes.SAVING, "Saving chunks...");
		
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
			chatInfo("Could not save chunks. Reflection error.");
			return;
		}

		hashArrayField.setAccessible(true);
		// Steal the instance of LongHashMap from our chunk provider
		LongHashMap lhm = ReflectionUtils.stealAndGetField(chunkProvider,
				LongHashMap.class);
		
		progressScreen.startMajorTask(I18n.format("wdl.saveProgress.chunk.title"), 
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
						
						progressScreen.setMinorTaskProgress(I18n.format(
								"wdl.saveProgress.chunk.saving", c.xPosition,
								c.zPosition), currentChunk);
						
						saveChunk(c);
					}
				}
			}

			chatMessage(WDLMessageTypes.SAVING, "Chunk data saved.");
		}
	}

	/**
	 * Import all non-overwritten TileEntities, then save the chunk
	 */
	public static void saveChunk(Chunk c) {
		if (!WDLPluginChannels.canDownloadInGeneral()) { return; }
		
		if (!WDLPluginChannels.canSaveTileEntities()) {
			c.getTileEntityMap().clear();
		}
		
		importTileEntities(c);
		
		if (WDLPluginChannels.canSaveTileEntities()) {
			editTileEntities(c);
		}
		
		c.setTerrainPopulated(true);

		try {
			ClassInheratanceMultiMap[] oldMaps = c.getEntityLists().clone();
			ClassInheratanceMultiMap[] maps = c.getEntityLists();
			
			if (!WDLPluginChannels.canSaveEntities(c)) {
				// Temporarily delete entities if saving them is disabled.
				for (int i = 0; i < maps.length; i++) {
					WrappedClassInheratanceMultiMap<Entity> map =
							WrappedClassInheratanceMultiMap
							.<Entity> copyOf(maps[i]);
					maps[i] = map;
					for (Entity e : map.asIterable()) {
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
					WrappedClassInheratanceMultiMap<Entity> map =
							WrappedClassInheratanceMultiMap
							.<Entity> copyOf(maps[i]);
					maps[i] = map;
					for (Entity e : map.asIterable()) {
						if (e instanceof EntityPlayer) {
							//Skip players, as otherwise bad things happen, 
							//such as deleting the current player and causing
							//the screen to flicker.
							continue;
						}
						
						if (!EntityUtils.isEntityEnabled(e)) {
							WDLMessages.chatMessageTranslated(
									WDLMessageTypes.REMOVE_ENTITY,
									"wdl.messages.removeEntity.notSavingUserPreference",
									e);
							
							map.removeWDL(e);
						} else {
							IChatComponent unsafeReason = EntityUtils
									.isUnsafeToSaveEntity(e);
							if (unsafeReason != null) {
								WDLMessages.chatMessageTranslated(
										WDLMessageTypes.REMOVE_ENTITY,
										"wdl.messages.removeEntity.notSavingUnsafe",
										e, unsafeReason);
								
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
						@SuppressWarnings("unchecked")
						WrappedClassInheratanceMultiMap<Entity> map = 
								(WrappedClassInheratanceMultiMap<Entity>) maps[e.chunkCoordY];
						map.addWDL(e);
					}
				}
			}
			
			// Force the entity into its serverside location.
			// Needed for certain things that move clientside,
			// such as boats (http://imgur.com/3QQchZL)
			@SuppressWarnings("unchecked")
			Iterable<Entity>[] iterableMaps = c.getEntityLists();
			for (Iterable<Entity> entityList :  iterableMaps) {
				for (Entity e : entityList) {
					e.posX = convertServerPos(e.serverPosX);
					e.posY = convertServerPos(e.serverPosY);
					e.posZ = convertServerPos(e.serverPosZ);
					
					for (Map.Entry<String, IEntityEditor> editor : entityEditors
							.entrySet()) {
						try {
							if (editor.getValue().shouldEdit(e)) {
								editor.getValue().editEntity(e);
							}
						} catch (Exception ex) {
							String chunkInfo;
							if (c == null) {
								chunkInfo = "null";
							} else {
								chunkInfo = "at " + c.xPosition + ", " + c.zPosition;
							}
							throw new RuntimeException("Failed to edit entity " + e
									+ " for chunk " + chunkInfo + " with extension "
									+ editor.getKey(), ex);
						}
					}
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

	/**
	 * Loads the sever-shared properties, which act as a default
	 * for the properties of each individual world in a multiworld server.
	 */
	public static void loadBaseProps() {
		baseFolderName = getBaseFolderName();
		baseProps = new Properties(globalProps);

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

	/**
	 * Loads the properties for the given world, and returns it.
	 */
	public static Properties loadWorldProps(String theWorldName) {
		Properties ret = new Properties(baseProps);
		
		File savesDir = new File(minecraft.mcDataDir, "saves");

		if (!theWorldName.isEmpty()) {
			String folder = getWorldFolderName(theWorldName);
			File worldFolder = new File(savesDir, folder);

			try {
				ret.load(new FileReader(new File(worldFolder,
						"WorldDownloader.txt")));
			} catch (Exception e) {
				return null;
			}
		}

		return ret;
	}

	/**
	 * Saves the currently used base and world properties in their corresponding
	 * folders.
	 */
	public static void saveProps() {
		saveProps(worldName, worldProps);
	}

	/**
	 * Saves the specified world properties, and the base properties, in their
	 * corresponding folders.
	 */
	public static void saveProps(String theWorldName, Properties theWorldProps) {
		File savesDir = new File(minecraft.mcDataDir, "saves");
		
		if (theWorldName.length() > 0) {
			String folder = getWorldFolderName(theWorldName);

			File worldFolder = new File(savesDir, folder);
			worldFolder.mkdirs();
			try {
				theWorldProps.store(new FileWriter(new File(worldFolder,
						"WorldDownloader.txt")), I18n.format("wdl.props.world.title"));
			} catch (Exception e) {
			}
		} else if (!isMultiworld) {
			baseProps.putAll(theWorldProps);
		}

		File baseFolder = new File(savesDir, baseFolderName);
		baseFolder.mkdirs();

		try {
			baseProps.store(new FileWriter(new File(baseFolder,
					"WorldDownloader.txt")), I18n.format("wdl.props.base.title"));
		} catch (Exception e) {
		}
		
		saveGlobalProps();
	}
	
	/**
	 * Saves the global properties, which are used for all servers.
	 */
	public static void saveGlobalProps() {
		try {
			globalProps.store(new FileWriter(new File(minecraft.mcDataDir,
					"WorldDownloader.txt")), I18n.format("wdl.props.global.title"));
		} catch (Exception e) {
			
		}
	}

	/**
	 * Change player specific fields according to the overrides found in the
	 * properties file.
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
	 * found in the properties file.
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
	 * Saves existing map data.  Map data refering to the items
	 * that contain pictures.
	 * 
	 * TODO: Overwrite / create IDCounts.dat.
	 */
	public static void saveMapData(GuiWDLSaveProgress progressScreen) {
		if (!WDLPluginChannels.canSaveMaps()) { return; }
		
		File dataDirectory = new File(saveHandler.getWorldDirectory(),
				"data");
		dataDirectory.mkdirs();
		
		progressScreen.startMajorTask(
				I18n.format("wdl.saveProgress.map.title"), newMapDatas.size());
		
		chatMessage(WDLMessageTypes.SAVING, "Saving map data...");
		
		int count = 0;
		for (Map.Entry<Integer, MapData> e : newMapDatas.entrySet()) {
			count++;
			
			progressScreen.setMinorTaskProgress(
					I18n.format("wdl.saveProgress.map.saving", e.getKey()),
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
		
		chatMessage(WDLMessageTypes.SAVING, "Map data saved.");
	}

	/**
	 * Gets the name of the server, either from the name in the server list,
	 * or using the server's IP.
	 */
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

	/**
	 * Get the base folder name for the server we are connected to.
	 */
	public static String getBaseFolderName() {
		return getServerName().replaceAll("\\W+", "_");
	}

	/**
	 * Get the folder name for the specified world.
	 */
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

	/**
	 * Prints a message to the chat, with the given type. 
	 */
	public static void chatMessage(IWDLMessageType type, String msg) {
		WDLMessages.chatMessage(type, msg);
	}
	
	/**
	 * Prints a message to the chat, with a type of {@link WDLMessageTypes#INFO}.
	 */
	public static void chatInfo(String msg) {
		WDLMessages.chatMessage(WDLMessageTypes.INFO, msg);
	}

	/**
	 * Prints a message to the chat, with a type of {@link WDLMessageTypes#ERROR}.
	 */
	public static void chatError(String msg) {
		WDLMessages.chatMessage(WDLMessageTypes.ERROR, msg);
	}

	/**
	 * Gets the save version.
	 * 
	 * TODO: This seems mostly unnecessary -- this is just the NBT
	 * version, which is always 19133, yes?  This seems like it
	 * may be overkill (the number has not changed in years).
	 */
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
	 * 
	 * This is detected based off of the server brand.
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
		if (globalProps != null) {
			if (!globalProps.isEmpty()) {
				for (Map.Entry<Object, Object> e : globalProps.entrySet()) {
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
	 * Call to properly crash the game when an exception is caught in WDL code.
	 * 
	 * @param category
	 */
	public static void crashed(Throwable t, String category) {
		CrashReport report;
		
		if (t instanceof ReportedException) {
			CrashReport oldReport = 
					((ReportedException) t).getCrashReport();
			
			report = CrashReport.makeCrashReport(oldReport.getCrashCause(),
					category + " (" + oldReport.getCauseStackTraceOrString() + ")");
			
			try {
				//Steal crashReportSections, and replace it.
				@SuppressWarnings("unchecked")
				List<CrashReportCategory> crashReportSectionsOld = ReflectionUtils
						.stealAndGetField(oldReport, List.class);
				@SuppressWarnings("unchecked")
				List<CrashReportCategory> crashReportSectionsNew = ReflectionUtils
						.stealAndGetField(report, List.class);
				
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
