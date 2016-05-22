package wdl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.ThreadedFileIOBase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wdl.WorldBackup.WorldBackupType;
import wdl.api.IPlayerInfoEditor;
import wdl.api.ISaveListener;
import wdl.api.IWorldInfoEditor;
import wdl.api.WDLApi;
import wdl.api.WDLApi.ModInfo;
import wdl.gui.GuiWDLMultiworld;
import wdl.gui.GuiWDLMultiworldSelect;
import wdl.gui.GuiWDLOverwriteChanges;
import wdl.gui.GuiWDLSaveProgress;
import wdl.update.GithubInfoGrabber;

import com.google.common.collect.HashMultimap;

/**
 * This is the main class that does most of the work.
 */
public class WDL {
	// VERSION INFO - IF YOU ARE MAKING A CUSTOM VERSION, **PLEASE** CHANGE THIS
	/**
	 * Current version.  This should match the git tag for the current release.
	 */
	public static final String VERSION = "1.8.9a-beta2";
	/**
	 * The version of minecraft that this mod is installed on.
	 */
	public static final String EXPECTED_MINECRAFT_VERSION = "1.8.9";
	/**
	 * Owning username for the github repository to check for updates against.
	 * 
	 * For <code>https://github.com/Pokechu22/WorldDownloader</code>, this would
	 * be <code>Pokechu22/WorldDownloader</code>.
	 * 
	 * Note that WDL is licensed under the MMPLv2, which requires modified
	 * versions to be open source if they are released (plus requires permission
	 * for that - <a href="http://www.minecraftforum.net/private-messages/send?recipient=Pokechu22">
	 * send Pokechu22 a message on the Minecraft Forums to get it</a>).
	 * 
	 * @see GithubInfoGrabber
	 */
	public static final String GITHUB_REPO = "Pokechu22/WorldDownloader";
	
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
	 * All tile entities that were saved manually, by chunk and then position.
	 */
	public static HashMap<ChunkCoordIntPair, Map<BlockPos, TileEntity>> newTileEntities = new HashMap<ChunkCoordIntPair, Map<BlockPos, TileEntity>>();
	
	/**
	 * All entities that were downloaded, by chunk.
	 */
	public static HashMultimap<ChunkCoordIntPair, Entity> newEntities = HashMultimap.create();
	
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
	public static final Properties globalProps;
	/**
	 * Default properties that are used to create the global properites.
	 */
	public static final Properties defaultProps;
	
	private static Logger logger = LogManager.getLogger();
	
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
		defaultProps.setProperty("MapGenerator", "void");
		defaultProps.setProperty("GeneratorName", "flat");
		defaultProps.setProperty("GeneratorVersion", "0");
		defaultProps.setProperty("GeneratorOptions", ";0");
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
		
		// Updates
		defaultProps.setProperty("UpdateMinecraftVersion", "client");
		//XXX change this based off of whether the current build is beta or not
		defaultProps.setProperty("UpdateAllowBetas", "true");
		
		globalProps = new Properties(defaultProps);
		FileReader reader = null;
		try {
			reader = new FileReader(new File(minecraft.mcDataDir,
					"WorldDownloader.txt"));
			globalProps.load(reader);
		} catch (Exception e) {
			logger.debug("Failed to load global properties", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					logger.warn("Failed to close global properties reader", e);
				}
			}
		}
		baseProps = new Properties(globalProps);
		worldProps = new Properties(baseProps);
	}

	/**
	 * Starts the download.
	 */
	public static void startDownload() {
		worldClient = minecraft.theWorld;

		if (!WDLPluginChannels.canDownloadAtAll()) {
			return;
		}

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
			long lastSaved = Long.parseLong(worldProps.getProperty("LastSaved",
					"-1"));
			//Can't directly use worldClient.getWorldInfo, as that doesn't use
			//the saved version.
			worldDat = new FileInputStream(new File(
					saveHandler.getWorldDirectory(), "level.dat"));
			long lastPlayed = CompressedStreamTools.readCompressed(worldDat)
					.getCompoundTag("Data").getLong("LastPlayed");
			if (!overrideLastModifiedCheck && lastPlayed > lastSaved) {
				// The world was played later than it was saved; confirm that the
				// user is willing for possible changes they made to be overwritten.
				minecraft.displayGuiScreen(new GuiWDLOverwriteChanges(
						lastSaved, lastPlayed));
				return;
			}
		} catch (Exception e) {
			logger.warn("Error while checking if the map has been played and" +
					"needs to be backed up (this is normal if this world " +
					"has not been saved before): ", e);
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
		chunkLoader = WDLChunkLoader.create(saveHandler, worldClient.provider);
		newTileEntities = new HashMap<ChunkCoordIntPair,Map<BlockPos,TileEntity>>();
		newEntities = HashMultimap.create();
		newMapDatas = new HashMap<Integer, MapData>();

		if (baseProps.getProperty("ServerName").isEmpty()) {
			baseProps.setProperty("ServerName", getServerName());
		}

		startOnChange = true;
		downloading = true;
		WDLMessages.chatMessageTranslated(WDLMessageTypes.INFO,
				"wdl.messages.generalInfo.downloadStarted");
	}

	/**
	 * Stops the download, and saves.
	 */
	public static void stopDownload() {
		if (downloading) {
			// Indicate that downloading has stopped
			downloading = false;
			startOnChange = false;
			WDLMessages.chatMessageTranslated(WDLMessageTypes.INFO,
					"wdl.messages.generalInfo.downloadStopped");
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
			startOnChange = false;
			saving = false;
			downloading = false;
			worldLoadingDeferred = false;

			WDLMessages.chatMessageTranslated(WDLMessageTypes.INFO,
					"wdl.messages.generalInfo.downloadCanceled");
		}
	}

	/**
	 * Starts the asnchronous save thread.
	 */
	static void startSaveThread() {
		// Indicate that we are saving
		WDLMessages.chatMessageTranslated(WDLMessageTypes.INFO,
				"wdl.messages.generalInfo.saveStarted");
		WDL.saving = true;
		Thread thread = new Thread("WDL Save Thread") {
			@Override
			public void run() {
				try {
					WDL.saveEverything();
					WDL.saving = false;
					WDL.onSaveComplete();
				} catch (Throwable e) {
					WDL.crashed(e, "World Downloader Mod: Saving world");
				}
			}
		};
		thread.start();
	}

	/**
	 * Called when the world has loaded.
	 * 
	 * @return Whether on the same server.
	 */
	public static boolean loadWorld() {
		worldName = ""; // The new (multi-)world name is unknown at the moment
		worldClient = minecraft.theWorld;
		thePlayer = minecraft.thePlayer;
		windowContainer = thePlayer.openContainer;
		overrideLastModifiedCheck = false;
		
		NetworkManager newNM = thePlayer.sendQueue.getNetworkManager();
		
		// Handle checking if the server changes here so that
		// messages are loaded FIRST.
		if (networkManager != newNM) {
			loadBaseProps();
			WDLMessages.onNewServer();
		}
		
		WDLPluginChannels.onWorldLoad();
		
		// Is this a different server?
		if (networkManager != newNM) {
			// Different server, different world!
			WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_WORLD_LOAD,
					"wdl.messages.onWorldLoad.differentServer");
			
			networkManager = newNM;
			
			if (isSpigot()) {
				WDLMessages.chatMessageTranslated(
						WDLMessageTypes.ON_WORLD_LOAD,
						"wdl.messages.onWorldLoad.spigot",
						thePlayer.getClientBrand());
			} else {
				WDLMessages.chatMessageTranslated(
						WDLMessageTypes.ON_WORLD_LOAD,
						"wdl.messages.onWorldLoad.vanilla",
						thePlayer.getClientBrand());
			}
			
			startOnChange = false;
			
			return true;
		} else {
			// Same server, different world!
			WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_WORLD_LOAD,
					"wdl.messages.onWorldLoad.sameServer");
			
			if (isSpigot()) {
				WDLMessages.chatMessageTranslated(
						WDLMessageTypes.ON_WORLD_LOAD,
						"wdl.messages.onWorldLoad.spigot",
						thePlayer.getClientBrand());
			} else {
				WDLMessages.chatMessageTranslated(
						WDLMessageTypes.ON_WORLD_LOAD,
						"wdl.messages.onWorldLoad.vanilla",
						thePlayer.getClientBrand());
			}
			
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

		worldLoadingDeferred = false;

		// If still downloading, load the current world and keep on downloading
		if (downloading) {
			WDLMessages.chatMessageTranslated(WDLMessageTypes.INFO,
					"wdl.messages.generalInfo.saveComplete.startingAgain");
			WDL.loadWorld();
			return;
		}

		WDLMessages.chatMessageTranslated(WDLMessageTypes.INFO,
				"wdl.messages.generalInfo.saveComplete.done");
	}

	/**
	 * Saves all remaining chunks, world info and player info. Usually called
	 * when stopping.
	 */
	public static void saveEverything() throws Exception {
		if (!WDLPluginChannels.canDownloadAtAll()) {
			WDLMessages.chatMessageTranslated(WDLMessageTypes.ERROR,
					"wdl.messages.generalError.forbidden");
			return;
		}
		
		WorldBackupType backupType = 
				WorldBackupType.match(baseProps.getProperty("Backup", "ZIP"));
		
		final GuiWDLSaveProgress progressScreen = new GuiWDLSaveProgress(
				I18n.format("wdl.saveProgress.title"), 
				(backupType != WorldBackupType.NONE ? 6 : 5)
						+ WDLApi.getImplementingExtensions(ISaveListener.class).size());
		
		minecraft.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				minecraft.displayGuiScreen(progressScreen);
			}
		});
		
		saveProps();

		try {
			saveHandler.checkSessionLock();
		} catch (MinecraftException e) {
			throw new RuntimeException(
				"WorldDownloader: Couldn't get session lock for saving the world!", e);
		}

		// Player NBT is stored both in a separate file and level.dat.
		NBTTagCompound playerNBT = savePlayer(progressScreen);
		saveWorldInfo(progressScreen, playerNBT);
		
		saveMapData(progressScreen);
		saveChunks(progressScreen);
		
		saveProps();
		
		for (ModInfo<ISaveListener> info : WDLApi
				.getImplementingExtensions(ISaveListener.class)) {
			progressScreen.startMajorTask(
					I18n.format("wdl.saveProgress.extension.title",
							info.getDisplayName()), 1);
			info.mod.afterChunksSaved(saveHandler.getWorldDirectory());
		}
		
		try {
			WDLMessages.chatMessageTranslated(WDLMessageTypes.SAVING,
					"wdl.messages.saving.flushingIO");
			
			progressScreen.startMajorTask(
					I18n.format("wdl.saveProgress.flushingIO.title"), 1);
			progressScreen.setMinorTaskProgress(
					I18n.format("wdl.saveProgress.flushingIO.subtitle"), 1);
			
			// func_178779_a is a getter for the instance.
			// Look inside of ThreadedFileIOBase.java for
			// such a getter.
			ThreadedFileIOBase.getThreadedIOInstance().waitForFinish();
		} catch (Exception e) {
			throw new RuntimeException("Threw exception waiting for asynchronous IO to finish. Hmmm.", e);
		}
		
		if (backupType != WorldBackupType.NONE) {
			WDLMessages.chatMessageTranslated(WDLMessageTypes.SAVING,
					"wdl.messages.saving.backingUp");
			progressScreen.startMajorTask(
					backupType.getTitle(), 1);
			progressScreen.setMinorTaskProgress(
					I18n.format("wdl.saveProgress.backingUp.preparing"), 1);
			
			try {
				WorldBackup.backupWorld(saveHandler.getWorldDirectory(), 
						getWorldFolderName(worldName), backupType, progressScreen);
			} catch (IOException e) {
				WDLMessages.chatMessageTranslated(WDLMessageTypes.ERROR,
						"wdl.messages.generalError.failedToBackUp");
			}
		}
		
		progressScreen.setDoneWorking();
	}

	/**
	 * Save the player (position, health, inventory, ...) into its own file in
	 * the players directory, and applies needed overrides to the player info.
	 * 
	 * @return The player NBT tag.  Needed for later use in the world info.
	 */
	public static NBTTagCompound savePlayer(GuiWDLSaveProgress progressScreen) {
		if (!WDLPluginChannels.canDownloadAtAll()) { return new NBTTagCompound(); }
		
		progressScreen.startMajorTask(
				I18n.format("wdl.saveProgress.playerData.title"),
				3 + WDLApi.getImplementingExtensions(IPlayerInfoEditor.class).size());
		WDLMessages.chatMessageTranslated(WDLMessageTypes.SAVING,
				"wdl.messages.saving.savingPlayer");
		
		progressScreen.setMinorTaskProgress(
				I18n.format("wdl.saveProgress.playerData.creatingNBT"), 1);
		
		NBTTagCompound playerNBT = new NBTTagCompound();
		thePlayer.writeToNBT(playerNBT);
		
		progressScreen.setMinorTaskProgress(
				I18n.format("wdl.saveProgress.playerData.editingNBT"), 2);
		applyOverridesToPlayer(playerNBT);
		
		int taskNum = 3;
		for (ModInfo<IPlayerInfoEditor> info : WDLApi
				.getImplementingExtensions(IPlayerInfoEditor.class)) {
			progressScreen.setMinorTaskProgress(
					I18n.format("wdl.saveProgress.playerData.extension",
							info.getDisplayName()), taskNum);
			
			info.mod.editPlayerInfo(thePlayer, saveHandler, playerNBT);
			
			taskNum++;
		}
		
		progressScreen.setMinorTaskProgress(
				I18n.format("wdl.saveProgress.playerData.writingNBT"), taskNum);
		
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

		WDLMessages.chatMessageTranslated(WDLMessageTypes.SAVING,
				"wdl.messages.saving.playerSaved");
		
		return playerNBT;
	}

	/**
	 * Hardcoded, unchanging anvil save version ID.
	 * 
	 * 19132: McRegion; 19133: Anvil.  If it's necessary to specify a new
	 * version, many other parts of the mod will be broken anyways.
	 */
	private static final int ANVIL_SAVE_VERSION = 19133;

	/**
	 * Save the world metadata (time, gamemode, seed, ...) into the level.dat
	 * file.
	 */
	public static void saveWorldInfo(GuiWDLSaveProgress progressScreen,
			NBTTagCompound playerInfoNBT) {
		if (!WDLPluginChannels.canDownloadAtAll()) { return; }
		
		progressScreen.startMajorTask(
				I18n.format("wdl.saveProgress.worldMetadata.title"),
				3 + WDLApi.getImplementingExtensions(IWorldInfoEditor.class).size());
		WDLMessages.chatMessageTranslated(WDLMessageTypes.SAVING,
				"wdl.messages.saving.savingWorld");
		
		progressScreen.setMinorTaskProgress(
				I18n.format("wdl.saveProgress.worldMetadata.creatingNBT"), 1);
		
		// Set the save version, which isn't done automatically for some
		// strange reason.
		worldClient.getWorldInfo().setSaveVersion(ANVIL_SAVE_VERSION);

		// cloneNBTCompound takes the PLAYER's nbt file, and puts it in the
		// right place.
		// This is needed because single player uses that data.
		NBTTagCompound worldInfoNBT = worldClient.getWorldInfo()
				.cloneNBTCompound(playerInfoNBT);
		
		progressScreen.setMinorTaskProgress(
				I18n.format("wdl.saveProgress.worldMetadata.editingNBT"), 2);
		applyOverridesToWorldInfo(worldInfoNBT);
		
		int taskNum = 3;
		for (ModInfo<IWorldInfoEditor> info : WDLApi
				.getImplementingExtensions(IWorldInfoEditor.class)) {
			progressScreen.setMinorTaskProgress(
					I18n.format("wdl.saveProgress.worldMetadata.extension",
							info.getDisplayName()), taskNum);
			
			info.mod.editWorldInfo(worldClient, worldClient.getWorldInfo(),
					saveHandler, worldInfoNBT);
			
			taskNum++;
		}
		
		progressScreen.setMinorTaskProgress(
				I18n.format("wdl.saveProgress.worldMetadata.writingNBT"), taskNum);
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

		WDLMessages.chatMessageTranslated(WDLMessageTypes.SAVING,
				"wdl.messages.saving.worldSaved");
	}

	/**
	 * Calls saveChunk for all currently loaded chunks
	 *
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static void saveChunks(GuiWDLSaveProgress progressScreen)
			throws IllegalArgumentException, IllegalAccessException {
		if (!WDLPluginChannels.canDownloadAtAll()) { return; }
		
		WDLMessages.chatMessageTranslated(WDLMessageTypes.SAVING,
				"wdl.messages.saving.savingChunks");
		
		// Get the ChunkProviderClient from WorldClient
		ChunkProviderClient chunkProvider = (ChunkProviderClient) worldClient
				.getChunkProvider();
		// Get the list of loaded chunks
		List<?> chunks = ReflectionUtils.stealAndGetField(chunkProvider,
				List.class);
		
		progressScreen.startMajorTask(I18n.format("wdl.saveProgress.chunk.title"), 
				chunks.size());
		
		for (int currentChunk = 0; currentChunk < chunks.size(); currentChunk++) {
			Chunk c = (Chunk) chunks.get(currentChunk);
			if (c != null) {
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
		WDLMessages.chatMessageTranslated(WDLMessageTypes.SAVING,
				"wdl.messages.saving.chunksSaved");
	}

	/**
	 * Import all non-overwritten TileEntities, then save the chunk
	 */
	public static void saveChunk(Chunk c) {
		if (!WDLPluginChannels.canDownloadAtAll()) { return; }
		
		if (!WDLPluginChannels.canSaveChunk(c)) { return; }
		
		c.setTerrainPopulated(true);

		try {
			chunkLoader.saveChunk(worldClient, c);
		} catch (Exception e) {
			// Better tell the player that something didn't work:
			WDLMessages.chatMessageTranslated(WDLMessageTypes.ERROR,
					"wdl.messages.generalError.failedToSaveChunk",
					c.xPosition, c.zPosition, e);
		}
	}

	/**
	 * Loads the sever-shared properties, which act as a default
	 * for the properties of each individual world in a multiworld server.
	 */
	public static void loadBaseProps() {
		baseFolderName = getBaseFolderName();
		baseProps = new Properties(globalProps);

		FileReader reader = null;
		try {
			File savesFolder = new File(minecraft.mcDataDir, "saves");
			File baseFolder = new File(savesFolder, baseFolderName);
			reader = new FileReader(new File(baseFolder,
					"WorldDownloader.txt"));
			baseProps.load(reader);
			propsFound = true;
		} catch (Exception e) {
			propsFound = false;
			logger.debug("Failed to load base properties", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					logger.warn("Failed to close base properties reader", e);
				}
			}
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
	 * 
	 * Returns an empty Properties that inherits from baseProps if the specific
	 * world cannot be found.
	 */
	public static Properties loadWorldProps(String theWorldName) {
		Properties ret = new Properties(baseProps);
		
		if (theWorldName.isEmpty()) {
			return ret;
		}
		
		File savesDir = new File(minecraft.mcDataDir, "saves");
		
		String folder = getWorldFolderName(theWorldName);
		File worldFolder = new File(savesDir, folder);

		FileReader reader = null;
		try {
			ret.load(new FileReader(new File(worldFolder,
					"WorldDownloader.txt")));
			
			return ret;
		} catch (Exception e) {
			logger.debug("Failed to load world props for " + worldName, e);
			return ret;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					logger.warn("Failed to close world props reader for "
							+ worldName, e);						
				}
			}
		}
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
		
		// If the player is able to fly, spawn them flying.
		// Helps ensure they don't fall out of the world.
		if (thePlayer.capabilities.allowFlying) {
			playerNBT.getCompoundTag("abilities").setBoolean("flying", true);
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
		// generatorOptions
		String generatorOptions = worldProps.getProperty("GeneratorOptions");
		worldInfoNBT.setString("generatorOptions", generatorOptions);
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
		} else if (weather.equals("rain")) {
			worldInfoNBT.setBoolean("raining", true);
			worldInfoNBT.setInteger("rainTime", 24000);
			worldInfoNBT.setBoolean("thundering", false);
			worldInfoNBT.setInteger("thunderTime", 0);
		} else if (weather.equals("thunderstorm")) {
			worldInfoNBT.setBoolean("raining", true);
			worldInfoNBT.setInteger("rainTime", 24000);
			worldInfoNBT.setBoolean("thundering", true);
			worldInfoNBT.setInteger("thunderTime", 24000);
		}

		// Spawn
		String spawn = worldProps.getProperty("Spawn");

		if (spawn.equals("player")) {
			int x = MathHelper.floor_double(thePlayer.posX);
			int y = MathHelper.floor_double(thePlayer.posY);
			int z = MathHelper.floor_double(thePlayer.posZ);
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
		
		WDLMessages.chatMessageTranslated(WDLMessageTypes.SAVING,
				"wdl.messages.saving.savingMapItemData");
		
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
		
		WDLMessages.chatMessageTranslated(WDLMessageTypes.SAVING,
				"wdl.messages.saving.mapItemDataSaved");
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
			logger.warn("Exception while getting server name: ", e);
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
	 * @param container
	 *            The container to save from, usually {@link #windowContainer} .
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
			ItemStack item = container.getSlot(containerIndex).getStack();
			tileEntity.setInventorySlotContents(inventoryIndex, item);
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
	
	/**
	 * Adds the given tile entity to {@link #newTileEntities}.
	 * 
	 * @param pos
	 *            The position of the tile entity
	 * @param te
	 *            The tile entity to add
	 */
	public static void saveTileEntity(BlockPos pos, TileEntity te) {
		int chunkX = pos.getX() / 16;
		int chunkZ = pos.getZ() / 16;
		
		ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(chunkX, chunkZ);
		
		if (!newTileEntities.containsKey(chunkPos)) {
			newTileEntities.put(chunkPos, new HashMap<BlockPos, TileEntity>());
		}
		newTileEntities.get(chunkPos).put(pos, te);
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
				.append(Minecraft.getMinecraft().getVersion()).append('\n');
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
		Map<String, ModInfo<?>> extensions = WDLApi.getWDLMods();
		info.append(extensions.size()).append(" loaded\n");
		for (Map.Entry<String, ModInfo<?>> e : extensions.entrySet()) {
			info.append("\n#### ").append(e.getKey()).append("\n\n");
			try {
				info.append(e.getValue().getInfo());
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
	
	/**
	 * Gets the current minecraft version. This is different from the launched
	 * version; it is constant between profile names.
	 */
	public static String getMinecraftVersion() {
		//Returns some session info used when making a HTTP request for resource packs.
		//Only matters because X-Minecraft-Version is included.
		Map<?, ?> map = Minecraft.getSessionInfo();
		if (map.containsKey("X-Minecraft-Version")) {
			return (String) map.get("X-Minecraft-Version");
		} else {
			return EXPECTED_MINECRAFT_VERSION;
		}
	}

	/**
	 * Gets version info similar to the info that appears at the top of F3.
	 */
	public static String getMinecraftVersionInfo() {
		String version = getMinecraftVersion();
		// Gets the launched version (appears in F3)
		String launchedVersion = Minecraft.getMinecraft().getVersion();
		String brand = ClientBrandRetriever.getClientModName();
		
		return String.format("Minecraft %s (%s/%s)", version,
				launchedVersion, brand);
	}
}
