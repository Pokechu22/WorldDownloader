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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReferenceArray;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import net.minecraft.block.AirBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.SessionLockException;
import net.minecraft.world.storage.WorldInfo;
import wdl.WorldBackup.WorldBackupType;
import wdl.api.APIImpl;
import wdl.api.IPlayerInfoEditor;
import wdl.api.ISaveListener;
import wdl.api.IWorldInfoEditor;
import wdl.api.WDLApi;
import wdl.api.WDLApi.ModInfo;
import wdl.config.Configuration;
import wdl.config.DefaultConfiguration;
import wdl.config.IConfiguration;
import wdl.config.settings.GeneratorSettings;
import wdl.config.settings.MiscSettings;
import wdl.config.settings.PlayerSettings;
import wdl.config.settings.WorldSettings;
import wdl.gui.GuiWDLMultiworld;
import wdl.gui.GuiWDLMultiworldSelect;
import wdl.gui.GuiWDLOverwriteChanges;
import wdl.gui.GuiWDLSaveProgress;
import wdl.update.GithubInfoGrabber;
import wdl.versioned.VersionedFunctions;

/**
 * This is the main class that does most of the work.
 */
public class WDL {
	/**
	 * Owning username for the github repository to check for updates against.
	 *
	 * For <code>https://github.com/Pokechu22/WorldDownloader</code>, this would
	 * be <code>Pokechu22/WorldDownloader</code>.
	 *
	 * Note that WDL is licensed under the MMPLv2, which requires modified
	 * versions to be open source if they are released (plus requires permission
	 * for that - <a href="https://www.minecraftforum.net/private-messages/send?recipient=Pokechu22">
	 * send Pokechu22 a message on the Minecraft Forums to get it</a>).
	 *
	 * @see GithubInfoGrabber
	 */
	public static final String GITHUB_REPO = "Pokechu22/WorldDownloader";

	// TODO: This class needs to be split into smaller classes. There is way too
	// much different stuff in here.

	/**
	 * Instance of WDL, currently a singleton but this will change in the future.
	 */
	@Nullable
	private static WDL INSTANCE = null;
	/**
	 * Returns the active instance of WDL.
	 */
	public static WDL getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("WDL has not been bootstraped, yet getInstance() called");
		}
		return INSTANCE;
	}
	/**
	 * Reference to the Minecraft object.
	 */
	public final Minecraft minecraft;
	/**
	 * Reference to the World object that WDL uses.
	 */
	public ClientWorld worldClient;
	/**
	 * Reference to a connection specific object. Used to detect a new
	 * connection.
	 */
	public NetworkManager networkManager = null;
	/**
	 * The current player.
	 */
	public ClientPlayerEntity player;

	/**
	 * Reference to the place where all the item stacks end up after receiving
	 * them.
	 */
	public Container windowContainer;
	/**
	 * The block position clicked at the time of the most recent window opening.
	 *
	 * Needed for block entity creation.
	 */
	@Nullable
	public BlockPos lastClickedBlock;
	/**
	 * Last entity clicked (used for non-block containers like minecarts with chests).
	 */
	@Nullable
	public Entity lastEntity;

	/**
	 * For player files and the level.dat file.
	 */
	public SaveHandler saveHandler;
	/**
	 * For the chunks (despite the name it does also SAVE chunks)
	 */
	public WDLChunkLoader chunkLoader;

	/**
	 * All tile entities that were saved manually, by chunk and then position.
	 */
	public Map<ChunkPos, Map<BlockPos, TileEntity>> newTileEntities = new HashMap<>();

	/**
	 * All entities that were downloaded, by chunk.
	 */
	public Multimap<ChunkPos, Entity> newEntities = HashMultimap.create();

	/**
	 * All of the {@link MapData}s that were sent to the client in the current
	 * world.
	 */
	public Map<Integer, MapData> newMapDatas = new HashMap<>();

	/**
	 * The chunk position of each entity that has been saved, by UUID.
	 * Cleared after that chunk has been written to disk.  Used to avoid
	 * writing the same entity into multiple chunks.
	 */
	public Map<UUID, ChunkPos> entityPositions = new HashMap<>();

	/**
	 * All chunks that have been saved, for use in a UI.
	 */
	public Set<ChunkPos> savedChunks = new HashSet<>();

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
	public static String worldName;
	/**
	 * The folder in which worlds are being saved.
	 */
	public static String baseFolderName;

	// Properties:
	/**
	 * Base properties, shared between each world on a multiworld server.
	 */
	public static IConfiguration serverProps;
	/**
	 * Properties for a single world on a multiworld server, or all worlds
	 * on a single world server.
	 */
	public IConfiguration worldProps;
	/**
	 * Default properties used for creating baseProps.  Saved and loaded;
	 * shared between all servers.
	 */
	public static final IConfiguration globalProps;
	/**
	 * Default properties that are used to create the global properites.
	 */
	public static final IConfiguration defaultProps;
	/**
	 * Gamerules associated with the current world.  Loaded at the same time as worldProps.
	 */
	public GameRules gameRules;

	private static final Logger LOGGER = LogManager.getLogger();

	static {
		// Initialize the Properties template:
		defaultProps = new DefaultConfiguration();

		globalProps = new Configuration(defaultProps);
	}
	/**
	 * Sets up everything used by this class; can be called multiple times if needed.
	 */
	public static void bootstrap(Minecraft minecraft) {
		if (minecraft == null) {
			throw new AssertionError("Minecraft instance is null at WDL bootstrap!");
		}
		if (INSTANCE == null) {
			INSTANCE = new WDL(Minecraft.getInstance());
		}
	}
	private WDL(Minecraft minecraft) {
		this.minecraft = minecraft;

		try {
			File dataFile = new File(minecraft.gameDir, "WorldDownloader.txt");
			globalProps.load(dataFile);
		} catch (FileNotFoundException e) {
			LOGGER.debug("Failed to load global properties as they do not exist", e);
		} catch (Exception e) {
			LOGGER.warn("Failed to load global properties", e);
		}
		serverProps = new Configuration(globalProps);
		this.worldProps = serverProps;
		this.gameRules = new GameRules();

		WDLEvents.createListener(this);

		// Now that all configuration is loaded, it should be safe to access this
		// (and it shouldn't have issues with depending back on this class)
		APIImpl.ensureInitialized();
	}

	/**
	 * Prompts for specific input that is needed (world name and multiworld status).
	 *
	 * @param context "startDownload" or "changeOptions"
	 * @param checkLastModified True if the last modified check should be performed.
	 * @param callback Callback when information is entered.  Should call this method again.  Should also change GUIs.
	 * @param cancel Callback when canceling.
	 * @return true when the calling method should exit (this is prompting), false if it should continue
	 */
	public boolean promptForInfoForSettings(String context, boolean checkLastModified, Runnable callback, Runnable cancel) {
		// NOTE: This is checked first, even though this condition will fail initially while
		// the !propsFound one will be hit first.
		// That's because this one will trigger on the next call, but the properties
		// will not have yet been loaded.  We want to ask this question next, and
		// then the properties will finally be loaded.
		// This code really needs to be redone.
		if (isMultiworld && worldName.isEmpty()) {
			minecraft.displayGuiScreen(new GuiWDLMultiworldSelect(this,
					new TranslationTextComponent("wdl.gui.multiworldSelect.title." + context),
					new GuiWDLMultiworldSelect.WorldSelectionCallback() {
				@Override
				public void onWorldSelected(String selectedWorld) {
					worldName = selectedWorld;

					worldProps = loadWorldProps(selectedWorld);
					propsFound = true; // Successfully loaded, even if the file doesn't exist.
					gameRules = loadGameRules(selectedWorld);
					callback.run();
				}

				@Override
				public void onCancel() {
					cancel.run();
				}
			}));
			return true;
		}

		if (!propsFound) {
			minecraft.displayGuiScreen(new GuiWDLMultiworld(new GuiWDLMultiworld.MultiworldCallback() {
				@Override
				public void onSelect(boolean enableMutliworld) {
					isMultiworld = enableMutliworld;

					if (!isMultiworld) {
						serverProps.setValue(MiscSettings.LINKED_WORLDS, "");
						saveProps();
						propsFound = true;
					}

					callback.run();
				}

				@Override
				public void onCancel() {
					cancel.run();
				}
			}));
			return true;
		}

		if (checkLastModified && !overrideLastModifiedCheck) {
			long lastSaved = worldProps.getValue(MiscSettings.LAST_SAVED);
			long lastPlayed;
			// Can't directly use worldClient.getWorldInfo, as that doesn't use
			// the saved version.
			File savesDir = new File(minecraft.gameDir, "saves");
			String folder = getWorldFolderName(worldName);
			File worldFolder = new File(savesDir, folder);
			File levelDatFile = new File(worldFolder, "level.dat");
			if (levelDatFile.exists()) {
				try (FileInputStream stream = new FileInputStream(levelDatFile)) {
					CompoundNBT compound = CompressedStreamTools.readCompressed(stream);
					lastPlayed = compound.getCompound("Data").getLong("LastPlayed");
				} catch (Exception e) {
					LOGGER.warn("Error while checking if the map has been played and " +
							"needs to be backed up: ", e);
					lastPlayed = -1;
				}
			} else {
				lastPlayed = -1;
			}
			if (lastPlayed > lastSaved) {
				// The world was played later than it was saved; confirm that the
				// user is willing for possible changes they made to be overwritten.
				minecraft.displayGuiScreen(new GuiWDLOverwriteChanges(this,
						lastSaved, lastPlayed, () -> {
							overrideLastModifiedCheck = true;
							callback.run();
						}, cancel));
				return true;
			}
		}

		return false;
	}

	/**
	 * Starts the download.
	 */
	public void startDownload() {
		minecraft.displayGuiScreen(null);
		worldClient = minecraft.world;

		if (!WDLPluginChannels.canDownloadAtAll()) {
			return;
		}

		if (promptForInfoForSettings("startDownload", true, this::startDownload, () -> minecraft.displayGuiScreen(null))) {
			return;
		}

		saveHandler = VersionedFunctions.getSaveHandler(minecraft, getWorldFolderName(worldName));

		runSanityCheck(false);

		minecraft.displayGuiScreen(null);

		chunkLoader = WDLChunkLoader.create(this, saveHandler, worldClient.dimension);
		newTileEntities.values().forEach((m) -> {
			m.clear();
		});
		newTileEntities.clear();
		newEntities.clear();
		newMapDatas.clear();
		savedChunks.clear();

		startOnChange = true;
		downloading = true;
		WDLMessages.chatMessageTranslated(WDL.serverProps,
				WDLMessageTypes.INFO, "wdl.messages.generalInfo.downloadStarted");
	}

	/**
	 * Stops the download, and saves.
	 */
	public void stopDownload() {
		if (downloading) {
			// Indicate that downloading has stopped
			downloading = false;
			startOnChange = false;
			WDLMessages.chatMessageTranslated(WDL.serverProps,
					WDLMessageTypes.INFO, "wdl.messages.generalInfo.downloadStopped");
			startSaveThread();
		}
	}

	/**
	 * Cancels the download.
	 */
	public void cancelDownload() {
		boolean wasDownloading = downloading;

		if (wasDownloading) {
			try {
				chunkLoader.close();
			} catch (IOException ex) {
				LOGGER.warn("Failed to close chunkLoader", ex);
			}
			chunkLoader = null;
			startOnChange = false;
			saving = false;
			downloading = false;
			worldLoadingDeferred = false;

			WDLMessages.chatMessageTranslated(WDL.serverProps,
					WDLMessageTypes.INFO, "wdl.messages.generalInfo.downloadCanceled");
		}
	}

	/**
	 * Saves the current world and defers world loading until saving is finished.
	 * Used for world/dimension changes.
	 */
	public void saveForWorldChange() {
		WDLMessages.chatMessageTranslated(WDL.serverProps,
				WDLMessageTypes.INFO, "wdl.messages.generalInfo.worldChanged");
		WDL.worldLoadingDeferred = true;
		startSaveThread();
	}

	/**
	 * Starts the asynchronous save thread.
	 */
	private void startSaveThread() {
		// Indicate that we are saving
		WDLMessages.chatMessageTranslated(WDL.serverProps,
				WDLMessageTypes.INFO, "wdl.messages.generalInfo.saveStarted");
		WDL.saving = true;
		Thread thread = new Thread(() -> {
			try {
				saveEverything();
				minecraft.execute(() -> {
					WDL.saving = false;
					onSaveComplete();
				});
			} catch (Throwable e) {
				crashed(e, "World Downloader Mod: Saving world");
			}
		}, "WDL Save Thread");
		thread.start();
	}

	/**
	 * Called when the world has loaded.
	 *
	 * @return Whether on the same server.
	 */
	public boolean loadWorld() {
		worldName = ""; // The new (multi-)world name is unknown at the moment
		worldClient = minecraft.world;
		player = minecraft.player;
		windowContainer = player.openContainer;
		overrideLastModifiedCheck = false;

		NetworkManager newNM = player.connection.getNetworkManager();

		// Handle checking if the server changes here so that
		// messages are loaded FIRST.
		if (networkManager != newNM) {
			loadServerProps();
		}

		WDLPluginChannels.onWorldLoad();

		// Is this a different server?
		if (networkManager != newNM) {
			// Different server, different world!
			WDLMessages.chatMessageTranslated(WDL.serverProps,
					WDLMessageTypes.ON_WORLD_LOAD, "wdl.messages.onWorldLoad.differentServer");

			networkManager = newNM;

			if (isSpigot()) {
				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.ON_WORLD_LOAD,
						"wdl.messages.onWorldLoad.spigot", player.getServerBrand());
			} else {
				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.ON_WORLD_LOAD,
						"wdl.messages.onWorldLoad.vanilla", player.getServerBrand());
			}

			startOnChange = false;

			return true;
		} else {
			// Same server, different world!
			WDLMessages.chatMessageTranslated(WDL.serverProps,
					WDLMessageTypes.ON_WORLD_LOAD, "wdl.messages.onWorldLoad.sameServer");

			if (isSpigot()) {
				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.ON_WORLD_LOAD,
						"wdl.messages.onWorldLoad.spigot", player.getServerBrand());
			} else {
				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.ON_WORLD_LOAD,
						"wdl.messages.onWorldLoad.vanilla", player.getServerBrand());
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
	public void onSaveComplete() {
		worldClient = null;

		worldLoadingDeferred = false;

		// If still downloading, load the current world and keep on downloading
		if (downloading) {
			WDLMessages.chatMessageTranslated(WDL.serverProps,
					WDLMessageTypes.INFO, "wdl.messages.generalInfo.saveComplete.startingAgain");
			loadWorld();
			return;
		}

		WDLMessages.chatMessageTranslated(WDL.serverProps,
				WDLMessageTypes.INFO, "wdl.messages.generalInfo.saveComplete.done");
	}

	/**
	 * Saves all remaining chunks, world info and player info. Usually called
	 * when stopping.
	 */
	public void saveEverything() throws Exception {
		if (!WDLPluginChannels.canDownloadAtAll()) {
			WDLMessages.chatMessageTranslated(WDL.serverProps,
					WDLMessageTypes.ERROR, "wdl.messages.generalError.forbidden");
			return;
		}

		WorldBackupType backupType = serverProps.getValue(MiscSettings.BACKUP_TYPE);

		final GuiWDLSaveProgress progressScreen = new GuiWDLSaveProgress(this,
				new TranslationTextComponent("wdl.saveProgress.title"),
				(backupType != WorldBackupType.NONE ? 6 : 5)
				+ WDLApi.getImplementingExtensions(ISaveListener.class).size());

		// Schedule this as a task to avoid threading issues.
		// If directly displayed, in some rare cases the GUI will be drawn before it has been
		// initialized, causing a crash.  Using a task stops that.
		minecraft.enqueue(() -> { minecraft.displayGuiScreen(progressScreen); });

		saveProps();

		try {
			saveHandler.checkSessionLock();
		} catch (SessionLockException e) {
			throw new RuntimeException(
					"WorldDownloader: Couldn't get session lock for saving the world!", e);
		}

		// Player NBT is stored both in a separate file and level.dat.
		CompoundNBT playerNBT = savePlayer(progressScreen);
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
			WDLMessages.chatMessageTranslated(WDL.serverProps,
					WDLMessageTypes.SAVING, "wdl.messages.saving.flushingIO");

			progressScreen.startMajorTask(
					I18n.format("wdl.saveProgress.flushingIO.title"), 1);
			progressScreen.setMinorTaskProgress(() -> {
				WDLChunkLoader chunkLoader = WDL.this.chunkLoader;
				if (chunkLoader != null) {
					return I18n.format("wdl.saveProgress.flushingIO.subtitle", chunkLoader.getNumPendingChunks());
				} else {
					return "";
				}
			}, 1);

			// XXX Still needed?
			//ThreadedFileIOBase.getThreadedIOInstance().waitForFinish();
		} catch (Exception e) {
			throw new RuntimeException("Threw exception waiting for asynchronous IO to finish. Hmmm.", e);
		}
		chunkLoader.close();
		chunkLoader = null;

		if (backupType != WorldBackupType.NONE) {
			WDLMessages.chatMessageTranslated(WDL.serverProps,
					WDLMessageTypes.SAVING, "wdl.messages.saving.backingUp");
			progressScreen.startMajorTask(
					backupType.getTitle(), 1);
			progressScreen.setMinorTaskProgress(
					I18n.format("wdl.saveProgress.backingUp.preparing"), 1);

			class BackupState implements WorldBackup.ICustomBackupProgressMonitor {
				int curFile = 0;
				@Override
				public void setDenominator(int value, boolean show) {
					progressScreen.setMinorTaskCount(value, show);
				}
				@Override
				public void incrementNumerator() {
					curFile++;
					progressScreen.setMinorTaskProgress(curFile);
				}
				@Override
				public void setNumerator(int value) {
					curFile = value;
					progressScreen.setMinorTaskProgress(value);
				}
				@Override
				public void onTextUpdate(String text) {
					progressScreen.setMinorTaskProgress(text, curFile);
				}
				@Override
				public boolean shouldCancel() {
					return progressScreen.cancelAttempted();
				}
			}

			try {
				WorldBackup.backupWorld(saveHandler.getWorldDirectory(),
						getWorldFolderName(worldName), backupType, new BackupState(),
						serverProps.getValue(MiscSettings.BACKUP_COMMAND_TEMPLATE),
						serverProps.getValue(MiscSettings.BACKUP_EXTENSION));
			} catch (IOException ex) {
				WDLMessages.chatMessageTranslated(WDL.serverProps,
						WDLMessageTypes.ERROR, "wdl.messages.generalError.failedToBackUp", ex);
				VersionedFunctions.makeBackupFailedToast(ex);
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
	private CompoundNBT savePlayer(GuiWDLSaveProgress progressScreen) {
		if (!WDLPluginChannels.canDownloadAtAll()) { return new CompoundNBT(); }

		progressScreen.startMajorTask(
				I18n.format("wdl.saveProgress.playerData.title"),
				3 + WDLApi.getImplementingExtensions(IPlayerInfoEditor.class).size());
		WDLMessages.chatMessageTranslated(WDL.serverProps,
				WDLMessageTypes.SAVING, "wdl.messages.saving.savingPlayer");

		progressScreen.setMinorTaskProgress(
				I18n.format("wdl.saveProgress.playerData.creatingNBT"), 1);

		CompoundNBT playerNBT = new CompoundNBT();
		player.writeWithoutTypeId(playerNBT);

		progressScreen.setMinorTaskProgress(
				I18n.format("wdl.saveProgress.playerData.editingNBT"), 2);
		applyOverridesToPlayer(playerNBT);

		int taskNum = 3;
		for (ModInfo<IPlayerInfoEditor> info : WDLApi
				.getImplementingExtensions(IPlayerInfoEditor.class)) {
			progressScreen.setMinorTaskProgress(
					I18n.format("wdl.saveProgress.playerData.extension",
							info.getDisplayName()), taskNum);

			info.mod.editPlayerInfo(player, saveHandler, playerNBT);

			taskNum++;
		}

		progressScreen.setMinorTaskProgress(
				I18n.format("wdl.saveProgress.playerData.writingNBT"), taskNum);

		File playersDirectory = new File(saveHandler.getWorldDirectory(),
				"playerdata");
		playersDirectory.mkdirs();
		File playerFileTmp = new File(playersDirectory, player
				.getUniqueID().toString() + ".dat.tmp");
		File playerFile = new File(playersDirectory, player
				.getUniqueID().toString() + ".dat");

		try (FileOutputStream stream = new FileOutputStream(playerFileTmp)) {

			CompressedStreamTools.writeCompressed(playerNBT, stream);

			// Remove the old player file to make space for the new one.
			Files.deleteIfExists(playerFile.toPath());
			Files.move(playerFileTmp.toPath(), playerFile.toPath());
		} catch (Exception e) {
			throw new RuntimeException("Couldn't save the player!", e);
		}

		WDLMessages.chatMessageTranslated(WDL.serverProps,
				WDLMessageTypes.SAVING, "wdl.messages.saving.playerSaved");

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
	private void saveWorldInfo(GuiWDLSaveProgress progressScreen,
			CompoundNBT playerInfoNBT) {
		if (!WDLPluginChannels.canDownloadAtAll()) { return; }

		progressScreen.startMajorTask(
				I18n.format("wdl.saveProgress.worldMetadata.title"),
				3 + WDLApi.getImplementingExtensions(IWorldInfoEditor.class).size());
		WDLMessages.chatMessageTranslated(WDL.serverProps,
				WDLMessageTypes.SAVING, "wdl.messages.saving.savingWorld");

		progressScreen.setMinorTaskProgress(
				I18n.format("wdl.saveProgress.worldMetadata.creatingNBT"), 1);

		// Set the save version, which isn't done automatically for some
		// strange reason.
		worldClient.getWorldInfo().setSaveVersion(ANVIL_SAVE_VERSION);

		// cloneNBTCompound takes the PLAYER's nbt file, and puts it in the
		// right place.
		// This is needed because single player uses that data.
		CompoundNBT worldInfoNBT = worldClient.getWorldInfo()
				.cloneNBTCompound(playerInfoNBT);

		// There's a root tag that stores the above one.
		CompoundNBT rootWorldInfoNBT = new CompoundNBT();
		rootWorldInfoNBT.put("Data", worldInfoNBT);

		progressScreen.setMinorTaskProgress(
				I18n.format("wdl.saveProgress.worldMetadata.editingNBT"), 2);
		applyOverridesToWorldInfo(worldInfoNBT, rootWorldInfoNBT);

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

		worldProps.setValue(MiscSettings.LAST_SAVED, worldInfoNBT.getLong("LastPlayed"));

		File dataFileTmp = new File(saveDirectory, "level.dat_new");
		File dataFileBackup = new File(saveDirectory, "level.dat_old");
		File dataFile = new File(saveDirectory, "level.dat");

		try (FileOutputStream stream = new FileOutputStream(dataFileTmp)) {
			// Make temporary level.dat_new
			CompressedStreamTools.writeCompressed(rootWorldInfoNBT, stream);

			if (dataFile.exists()) {
				// level.dat becomes level.dat_old
				// (however it won't exist the first time the world is saved)
				Files.deleteIfExists(dataFileBackup.toPath());
				Files.move(dataFile.toPath(), dataFileBackup.toPath());
			}

			// level.dat_new becomes level.dat
			Files.deleteIfExists(dataFile.toPath());
			Files.move(dataFileTmp.toPath(), dataFile.toPath());
		} catch (Exception e) {
			throw new RuntimeException("Couldn't save the world metadata!", e);
		}

		WDLMessages.chatMessageTranslated(WDL.serverProps,
				WDLMessageTypes.SAVING, "wdl.messages.saving.worldSaved");
	}

	/**
	 * Calls saveChunk for all currently loaded chunks
	 *
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private void saveChunks(GuiWDLSaveProgress progressScreen)
			throws IllegalArgumentException, IllegalAccessException {
		if (!WDLPluginChannels.canDownloadAtAll()) { return; }

		WDLMessages.chatMessageTranslated(WDL.serverProps,
				WDLMessageTypes.SAVING, "wdl.messages.saving.savingChunks");

		// Get the list of loaded chunks
		Class<?> chunkArrayClass = ClientChunkProvider.class.getDeclaredClasses()[0];
		Object obj = ReflectionUtils.findAndGetPrivateField(worldClient.getChunkProvider(),
				ClientChunkProvider.class,
				chunkArrayClass);
		@SuppressWarnings("unchecked")
		AtomicReferenceArray<Chunk> chunks = ReflectionUtils.findAndGetPrivateField(obj,
				AtomicReferenceArray.class);

		progressScreen.startMajorTask(I18n.format("wdl.saveProgress.chunk.title"),
				chunks.length());

		for (int currentChunk = 0; currentChunk < chunks.length(); currentChunk++) {
			Chunk c = chunks.get(currentChunk);
			if (c != null) {
				//Serverside restrictions check
				if (!WDLPluginChannels.canSaveChunk(c)) {
					continue;
				}

				progressScreen.setMinorTaskProgress(I18n.format(
						"wdl.saveProgress.chunk.saving", c.getPos().x,
						c.getPos().z), currentChunk);

				saveChunk(c);
			}
		}
		WDLMessages.chatMessageTranslated(WDL.serverProps,
				WDLMessageTypes.SAVING, "wdl.messages.saving.chunksSaved");
	}

	/**
	 * Import all non-overwritten TileEntities, then save the chunk
	 */
	public void saveChunk(Chunk c) {
		if (!WDLPluginChannels.canDownloadAtAll()) { return; }

		if (!WDLPluginChannels.canSaveChunk(c)) { return; }

		try {
			savedChunks.add(c.getPos());
			if (isEmpty(c)) {
				//LOGGER.warn("[WDL] Tried to save empty chunk! (" + c + "@" + c.x + "," + c.z + ")");
				return;
			}
			chunkLoader.saveChunk(worldClient, c);
		} catch (Exception e) {
			// Better tell the player that something didn't work:
			WDLMessages.chatMessageTranslated(WDL.serverProps,
					WDLMessageTypes.ERROR,
					"wdl.messages.generalError.failedToSaveChunk", c.getPos().x, c.getPos().z, e);
		}
	}

	private boolean isEmpty(Chunk c) {
		if (c.isEmpty() || c instanceof EmptyChunk) {
			return true;
		}
		ChunkSection[] array = c.getSections();
		for (int i = 1; i < array.length; i++) {
			if (array[i] != Chunk.EMPTY_SECTION) {
				return false;
			}
		}
		if (array[0] != Chunk.EMPTY_SECTION) {
			// All-air empty chunks sometimes are sent with a bottom section;
			// handle that and a few other special cases.
			for (int y = 0; y < 16; y++) {
				for (int z = 0; z < 16; z++) {
					for (int x = 0; x < 16; x++) {
						Block block = array[0].getBlockState(x, y, z).getBlock(); 
						if (!(block instanceof AirBlock || block instanceof BedBlock)) {
							// Contains a non-airoid; stop
							return false;
						}
					}
				}
			}
			// Only composed of airoids; treat as empty
			LOGGER.warn("[WDL] Skipping airoid empty chunk at " + c.getPos().x + ", " + c.getPos().z);
		} else {
			// Definitely empty
			LOGGER.warn("[WDL] Skipping chunk with all null sections at " + c.getPos().x + ", " + c.getPos().z);
		}
		return true;
	}

	/**
	 * Loads the sever-shared properties, which act as a default
	 * for the properties of each individual world in a multiworld server.
	 */
	private void loadServerProps() {
		baseFolderName = getBaseFolderName();
		serverProps = new Configuration(globalProps);

		File savesFolder = new File(minecraft.gameDir, "saves");
		File baseFolder = new File(savesFolder, baseFolderName);
		File dataFile = new File(baseFolder, "WorldDownloader.txt");
		try {
			serverProps.load(dataFile);
			propsFound = true;
		} catch (FileNotFoundException e) {
			propsFound = false;
			LOGGER.debug("Failed to load server properties as they do not exist", e);
		} catch (Exception e) {
			propsFound = false;
			LOGGER.warn("Failed to load server properties", e);
		}

		if (serverProps.getValue(MiscSettings.LINKED_WORLDS).isEmpty()) {
			isMultiworld = false;
			worldProps = serverProps;
			gameRules = loadGameRules("");
		} else {
			isMultiworld = true;
		}
	}

	/**
	 * Loads the properties for the given world, and returns it.
	 *
	 * Returns an empty Configuration that inherits from serverProps if the specific
	 * world cannot be found.
	 *
	 * Returns baseProps if there is no multiworld (i.e. the world name is empty).
	 */
	public IConfiguration loadWorldProps(String worldName) {
		if (worldName.isEmpty()) {
			return serverProps;
		}

		IConfiguration ret = new Configuration(serverProps);

		File savesDir = new File(minecraft.gameDir, "saves");

		String folder = getWorldFolderName(worldName);
		File worldFolder = new File(savesDir, folder);
		File dataFile = new File(worldFolder, "WorldDownloader.txt");

		try {
			ret.load(dataFile);
		} catch (FileNotFoundException e) {
			LOGGER.debug("Failed to load world props for " + worldName + " as they do not exist", e);
		} catch (Exception e) {
			LOGGER.warn("Failed to load world props for " + worldName, e);
		}

		return ret;
	}

	/**
	 * Gets the gamerules associated with the given world.
	 *
	 * @param worldName The name of the world, for multiworld.  Otherwise empty.
	 * @return The rules for that world, or a new default rules instance if the
	 *         rules couldn't be found (e.g. a new world)
	 */
	public GameRules loadGameRules(String worldName) {
		File savesDir = new File(minecraft.gameDir, "saves");

		String folder = getWorldFolderName(worldName);
		File worldFolder = new File(savesDir, folder);
		File levelDatFile = new File(worldFolder, "level.dat");

		GameRules rules = new GameRules();

		if (!levelDatFile.exists()) {
			return rules;
		}

		CompoundNBT gameRules;
		try (FileInputStream stream = new FileInputStream(levelDatFile)) {
			CompoundNBT compound = CompressedStreamTools.readCompressed(stream);
			gameRules = compound.getCompound("Data").getCompound("GameRules");
		} catch (Exception e) {
			LOGGER.warn("[WDL] Error while loading existing gamerules; the defaults will be used instead: ", e);

			return rules;
		}

		rules.read(gameRules);
		return rules;
	}

	/**
	 * Saves the currently used base and world properties in their corresponding
	 * folders.
	 */
	public void saveProps() {
		saveProps(worldName, worldProps);
	}

	/**
	 * Saves the specified world properties, and the server properties, in their
	 * corresponding folders.
	 */
	public void saveProps(String worldName, IConfiguration worldProps) {
		File savesDir = new File(minecraft.gameDir, "saves");

		if (worldName.length() > 0) {
			String folder = getWorldFolderName(worldName);

			File worldFolder = new File(savesDir, folder);
			worldFolder.mkdirs();
			File worldPropsFile = new File(worldFolder, "WorldDownloader.txt");
			try {
				worldProps.store(worldPropsFile, I18n.format("wdl.props.world.title"));
			} catch (Exception e) {
				LOGGER.warn("Failed to write world props!", e);
			}
		}

		File baseFolder = new File(savesDir, baseFolderName);
		baseFolder.mkdirs();

		File serverPropsFile = new File(baseFolder, "WorldDownloader.txt");
		try {
			serverProps.store(serverPropsFile, I18n.format("wdl.props.base.title"));
		} catch (Exception e) {
			LOGGER.warn("Failed to write server props!", e);
		}

		saveGlobalProps();
	}

	/**
	 * Saves the global properties, which are used for all servers.
	 */
	public static void saveGlobalProps() {
		File globalPropsFile = new File(Minecraft.getInstance().gameDir, "WorldDownloader.txt");
		try {
			globalProps.store(globalPropsFile, I18n.format("wdl.props.global.title"));
		} catch (Exception e) {
			LOGGER.warn("Failed to write globalprops!", e);
		}
	}

	/**
	 * Change player specific fields according to the overrides found in the
	 * properties file.
	 */
	private void applyOverridesToPlayer(CompoundNBT playerNBT) {
		// Health
		PlayerSettings.Health health = worldProps.getValue(PlayerSettings.HEALTH);

		if (health != PlayerSettings.Health.KEEP) {
			playerNBT.putShort("Health", health.healthValue);
		}

		// foodLevel, foodTimer, foodSaturationLevel, foodExhaustionLevel
		PlayerSettings.Hunger food = worldProps.getValue(PlayerSettings.HUNGER);

		if (food != PlayerSettings.Hunger.KEEP) {
			playerNBT.putInt("foodLevel", food.foodLevel);
			playerNBT.putInt("foodTickTimer", food.foodTickTimer);
			playerNBT.putFloat("foodSaturationLevel", food.foodSaturationLevel);
			playerNBT.putFloat("foodExhaustionLevel", food.foodExhaustionLevel);
		}

		// Player Position
		PlayerSettings.PlayerPos playerPos = worldProps.getValue(PlayerSettings.PLAYER_POSITION);

		if (playerPos == PlayerSettings.PlayerPos.XYZ) {
			int x = worldProps.getValue(PlayerSettings.PLAYER_X);
			int y = worldProps.getValue(PlayerSettings.PLAYER_Y);
			int z = worldProps.getValue(PlayerSettings.PLAYER_Z);
			// Positions are offset to center of block,
			// or player height.
			ListNBT pos = VersionedFunctions.createDoubleListTag(x + 0.5D, y + 0.621D, z + 0.5D);
			playerNBT.put("Pos", pos);
			// Force them to land on the ground?
			ListNBT motion = VersionedFunctions.createDoubleListTag(0.0D, -0.0001D, 0.0D);
			playerNBT.put("Motion", motion);
			ListNBT rotation = VersionedFunctions.createFloatListTag(0.0f, 0.0f);
			playerNBT.put("Rotation", rotation);
		}

		// If the player is able to fly, spawn them flying.
		// Helps ensure they don't fall out of the world.
		if (playerNBT.getCompound("abilities").getBoolean("mayfly")) {
			playerNBT.getCompound("abilities").putBoolean("flying", true);
		}

		if (serverProps.getValue(MiscSettings.FORCE_DIMENSION_TO_OVERWORLD)) {
			playerNBT.putInt("Dimension", 0);
		}
	}

	/**
	 * Change world and generator specific fields according to the overrides
	 * found in the properties file.
	 *
	 * @param worldInfoNBT The main world info, generated by {@link WorldInfo#cloneNBTCompound}.
	 * @param rootWorldInfoNBT The root tag containing worldInfoNBT as "<code>Data</code>"
	 */
	private void applyOverridesToWorldInfo(CompoundNBT worldInfoNBT, CompoundNBT rootWorldInfoNBT) {
		// LevelName
		String baseName = serverProps.getValue(MiscSettings.SERVER_NAME);
		String worldName = worldProps.getValue(MiscSettings.WORLD_NAME);

		if (worldName.isEmpty()) {
			worldInfoNBT.putString("LevelName", baseName);
		} else {
			worldInfoNBT.putString("LevelName", baseName + " - " + worldName);
		}

		// Cheats
		boolean allowCommands = worldProps.getValue(WorldSettings.ALLOW_CHEATS);
		worldInfoNBT.putBoolean("allowCommands", allowCommands);

		// GameType
		WorldSettings.GameMode gametypeOption = worldProps.getValue(WorldSettings.GAME_MODE);

		if (gametypeOption == WorldSettings.GameMode.KEEP) {
			// XXX Do we want this?  Or should it just use the actual mode without overriding?
			if (player.abilities.isCreativeMode) { // capabilities
				worldInfoNBT.putInt("GameType", 1); // Creative
			} else {
				worldInfoNBT.putInt("GameType", 0); // Survival
			}
		} else {
			worldInfoNBT.putInt("GameType", gametypeOption.gamemodeID);
			worldInfoNBT.putBoolean("hardcore", gametypeOption.hardcore);
		}

		// Time
		WorldSettings.Time timeOption = worldProps.getValue(WorldSettings.TIME);

		if (timeOption != WorldSettings.Time.KEEP) {
			worldInfoNBT.putLong("Time", timeOption.timeValue);
		}

		// RandomSeed
		String randomSeed = worldProps.getValue(GeneratorSettings.SEED);
		long seed = 0;

		// As per GuiCreateWorld.actionPerformed's done button handler
		if (!randomSeed.isEmpty()) {
			try {
				seed = Long.parseLong(randomSeed);
			} catch (NumberFormatException numberformatexception) {
				seed = randomSeed.hashCode();
			}
		}

		worldInfoNBT.putLong("RandomSeed", seed);

		// MapFeatures
		boolean mapFeatures = worldProps.getValue(GeneratorSettings.GENERATE_STRUCTURES);
		worldInfoNBT.putBoolean("MapFeatures", mapFeatures);
		// generatorName
		String generatorName = worldProps.getValue(GeneratorSettings.GENERATOR_NAME);
		worldInfoNBT.putString("generatorName", generatorName);
		// generatorOptions
		String generatorOptions = worldProps.getValue(GeneratorSettings.GENERATOR_OPTIONS);
		// NOTE: The type varies between versions; in 1.12.2 it's a string tag and in 1.13 it's a compound.
		worldInfoNBT.put("generatorOptions", VersionedFunctions.createGeneratorOptionsTag(generatorOptions));
		// generatorVersion
		int generatorVersion = worldProps.getValue(GeneratorSettings.GENERATOR_VERSION);
		worldInfoNBT.putInt("generatorVersion", generatorVersion);

		// Weather
		WorldSettings.Weather weather = worldProps.getValue(WorldSettings.WEATHER);

		if (weather != WorldSettings.Weather.KEEP) {
			worldInfoNBT.putBoolean("raining", weather.raining);
			worldInfoNBT.putInt("rainTime", weather.rainTime);
			worldInfoNBT.putBoolean("thundering", weather.thundering);
			worldInfoNBT.putInt("thunderTime", weather.thunderTime);
		}

		// Spawn
		WorldSettings.SpawnMode spawn = worldProps.getValue(WorldSettings.SPAWN);

		if (spawn != WorldSettings.SpawnMode.AUTO) {
			int x = spawn.getX(player, worldProps);
			int y = spawn.getY(player, worldProps);
			int z = spawn.getZ(player, worldProps);
			worldInfoNBT.putInt("SpawnX", x);
			worldInfoNBT.putInt("SpawnY", y);
			worldInfoNBT.putInt("SpawnZ", z);
			worldInfoNBT.putBoolean("initialized", true);
		}

		// Compute an entire new set of gamerules
		// (based on what we loaded from level.dat earlier)
		CompoundNBT vanillaRules = worldInfoNBT.getCompound("GameRules");
		Map<String, String> ourRules = VersionedFunctions.getGameRules(gameRules);
		if (!vanillaRules.keySet().equals(ourRules.keySet())) {
			LOGGER.warn("[WDL] Mismatched custom/vanilla game rule list!  We have " + ourRules +
					" and vanilla has " + VersionedFunctions.nbtString(vanillaRules) + ".  " +
					"(only differences in keys matter; values are expected to differ)");
		}
		CompoundNBT gamerules = new CompoundNBT();
		for (Map.Entry<String, String> e : ourRules.entrySet()) {
			gamerules.putString(e.getKey(), e.getValue());
		}
		worldInfoNBT.put("GameRules", gamerules);

		addForgeDataToWorldInfo(rootWorldInfoNBT, worldInfoNBT);
	}

	private void addForgeDataToWorldInfo(CompoundNBT rootWorldInfoNBT, CompoundNBT worldInfoNBT) {
		try {
			CompoundNBT versionInfo = worldInfoNBT.getCompound("Version");

			Class<?> fmlCommonHandler = Class.forName("net.minecraftforge.fml.common.FMLCommonHandler");
			Object instance = fmlCommonHandler.getMethod("instance").invoke(null);
			Object dataFixer = fmlCommonHandler.getMethod("getDataFixer").invoke(instance);
			Method writeVersionData = dataFixer.getClass()
					.getMethod("writeVersionData", CompoundNBT.class);
			writeVersionData.invoke(dataFixer, versionInfo);
		} catch (Throwable ex) {
			LOGGER.info("Failed to call FML writeVersionData", ex);
		}

		try {
			Class<?> fmlCommonHandler = Class.forName("net.minecraftforge.fml.common.FMLCommonHandler");
			Object instance = fmlCommonHandler.getMethod("instance").invoke(null);
			Method handleWorldDataSave = fmlCommonHandler.getMethod("handleWorldDataSave",
					SaveHandler.class, WorldInfo.class, CompoundNBT.class);
			handleWorldDataSave.invoke(instance, saveHandler, worldClient.getWorldInfo(), rootWorldInfoNBT);
		} catch (Throwable ex) {
			LOGGER.info("Failed to call FML handleWorldDataSave", ex);
		}
	}

	/**
	 * Saves existing map data.  Map data referring to the items
	 * that contain pictures.
	 */
	private void saveMapData(GuiWDLSaveProgress progressScreen) {
		if (!WDLPluginChannels.canSaveMaps()) { return; }

		File dataDirectory = new File(saveHandler.getWorldDirectory(),
				"data");
		dataDirectory.mkdirs();

		progressScreen.startMajorTask(
				I18n.format("wdl.saveProgress.map.title"), newMapDatas.size());

		WDLMessages.chatMessageTranslated(WDL.serverProps,
				WDLMessageTypes.SAVING, "wdl.messages.saving.savingMapItemData");

		Optional<Integer> highestCurrent = newMapDatas.keySet().stream().max(Integer::compare);

		highestCurrent.ifPresent(current -> {
			progressScreen.setMinorTaskProgress(I18n.format("wdl.saveProgress.map.idcounts"), 0);

			// In 1.13, idcounts.dat changed from storing shorts to ints.
			// Note that older versions are VERY particular about it being a short.
			boolean isInt = (VersionConstants.getDataVersion() >= 1451); // 17w47a

			// Compute the highest known map data ID, taking into account the highest
			// one we saved and any existing version idcounts data.
			int overallCount = current;
			File idcountsFile = new File(dataDirectory, "idcounts.dat");
			CompoundNBT tag = new CompoundNBT();
			if (idcountsFile.exists()) {
				try (DataInputStream stream = new DataInputStream(new FileInputStream(idcountsFile))) {
					tag = CompressedStreamTools.read(stream);
					int currentCount = (isInt ? tag.getInt("map") : tag.getShort("map"));
					overallCount = Math.max(currentCount, current);
				} catch (Exception ex) {
					LOGGER.warn("[WDL] Failed to load existing idcounts.dat; this shouldn't happen", ex);
					return;
				}
			}
			if (isInt) {
				tag.putInt("map", overallCount);
			} else {
				tag.putShort("map", (short)overallCount);
			}
			try (DataOutputStream stream = new DataOutputStream(new FileOutputStream(idcountsFile))) {
				CompressedStreamTools.write(tag, stream);
			} catch (Exception ex) {
				throw new RuntimeException("[WDL] Failed to save idcounts.dat with count " + overallCount + "!");
			}
		});

		int count = 0;
		for (Map.Entry<Integer, MapData> e : newMapDatas.entrySet()) {
			count++;

			progressScreen.setMinorTaskProgress(
					I18n.format("wdl.saveProgress.map.saving", e.getKey()),
					count);

			File mapFile = new File(dataDirectory, "map_" + e.getKey() + ".dat");

			CompoundNBT mapNBT = new CompoundNBT();
			CompoundNBT data = new CompoundNBT();

			e.getValue().write(data);

			mapNBT.put("data", data);
			if (VersionConstants.getDataVersion() >= 1484) { // 18w19a
				// MapData has a data version in 1.13+
				mapNBT.putInt("DataVersion", VersionConstants.getDataVersion());
			}

			try (FileOutputStream stream = new FileOutputStream(mapFile)) {
				CompressedStreamTools.writeCompressed(mapNBT, stream);
			} catch (IOException ex) {
				throw new RuntimeException("WDL: Exception while writing " +
						"map data for map " + e.getKey() + "!", ex);
			}
		}

		WDLMessages.chatMessageTranslated(WDL.serverProps,
				WDLMessageTypes.SAVING, "wdl.messages.saving.mapItemDataSaved");
	}

	/**
	 * Gets the name of the server, either from the name in the server list,
	 * or using the server's IP.
	 */
	public String getServerName() {
		try {
			if (minecraft.getCurrentServerData() != null) {
				String name = minecraft.getCurrentServerData().serverName;

				if (name.equals(I18n.format("selectServer.defaultName"))) {
					// Direct connection using domain name or IP (and port)
					name = minecraft.getCurrentServerData().serverIP;
				}

				return name;
			} else if (minecraft.isConnectedToRealms()) {
				String realmName = getRealmName();
				if (realmName != null) {
					return realmName;
				} else {
					LOGGER.warn("getServerName: getRealmName returned null!");
				}
			} else {
				LOGGER.warn("getServerName: Not connected to either a real server or realms!");
			}
		} catch (Exception e) {
			LOGGER.warn("Exception while getting server name: ", e);
		}

		return "Unidentified Server";
	}

	/**
	 * Gets the name of the realm that the player is currently connected to, or <code>null</code> if they are not connected to a realm.
	 *
	 * @return The name of the connected realm, or null.
	 */
	@Nullable
	private String getRealmName() {
		return null; // TODO
		/*if (!minecraft.isConnectedToRealms()) {
			LOGGER.warn("getRealmName: Not currently connected to realms!");
		}
		// Is this the only way to get the name of the Realms server? Really Mojang?
		// If this function turns out to be a pain to update, just remove Realms support completely.
		// I doubt anyone will need this anyway since Realms support downloading the world out of the box.

		// Try to get the value of NetHandlerPlayClient.guiScreenServer:
		Screen screen = ReflectionUtils.findAndGetPrivateField(minecraft.getConnection(), Screen.class);

		// If it is not a GuiScreenRealmsProxy we are not using a Realms server
		if (!(screen instanceof GuiScreenRealmsProxy)) {
			LOGGER.warn("getRealmName: screen {} is not an instance of GuiScreenRealmsProxy", screen);
			return null;
		}

		// Get the proxy's RealmsScreen object
		GuiScreenRealmsProxy screenProxy = (GuiScreenRealmsProxy) screen;
		RealmsScreen rs = screenProxy.getProxy();

		// It needs to be of type RealmsMainScreen (this should always be the case)
		if (!(rs instanceof RealmsMainScreen)) {
			LOGGER.warn("getRealmName: realms screen {} (instance of {}) not an instance of RealmsMainScreen!", rs, (rs != null ? rs.getClass() : null));
			return null;
		}

		RealmsMainScreen rms = (RealmsMainScreen) rs;
		RealmsServer mcos = null;
		try {
			// Find the ID of the selected Realms server. Fortunately unobfuscated names!
			Field selectedServerId = rms.getClass().getDeclaredField("selectedServerId");
			selectedServerId.setAccessible(true);
			if (!selectedServerId.getType().equals(long.class)) {
				LOGGER.warn("getRealmName: RealmsMainScreen selectedServerId field ({}) is not of type `long` ({})!", selectedServerId, selectedServerId.getType());
				return null;
			}
			long id = selectedServerId.getLong(rms);

			// Get the McoServer instance that was selected
			Method findServer = rms.getClass().getDeclaredMethod("findServer", long.class);
			findServer.setAccessible(true);
			Object obj = findServer.invoke(rms, id);
			if (!(obj instanceof RealmsServer)) {
				LOGGER.warn("getRealmName: RealmsMainScreen findServer method ({}) returned something other than a RealmsServer! ({})", findServer, obj);
				return null;
			}
			mcos = (RealmsServer) obj;
		} catch (Exception e) {
			LOGGER.warn("getRealmName: Unexpected exception!", e);
			return null;
		}

		// Return its name. Not sure if this is the best naming scheme...
		return mcos.name;*/
	}



	/**
	 * Get the base folder name for the server we are connected to,
	 * which is the server's name with special characters escaped.
	 */
	private String getBaseFolderName() {
		return getServerName().replaceAll("\\W+", "_");
	}

	/**
	 * Get the folder name for the specified world.
	 */
	public static String getWorldFolderName(String worldName) {
		if (worldName.isEmpty()) {
			return baseFolderName;
		} else {
			return baseFolderName + " - " + worldName;
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
	public void saveTileEntity(BlockPos pos, TileEntity te) {
		int chunkX = pos.getX() >> 4;
		int chunkZ = pos.getZ() >> 4;

		ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

		if (!newTileEntities.containsKey(chunkPos)) {
			newTileEntities.put(chunkPos, new HashMap<BlockPos, TileEntity>());
		}
		newTileEntities.get(chunkPos).put(pos, te);
	}

	/**
	 * Called when the given chunk is unloaded, to free memory.
	 *
	 * @param pos Location of the chunk
	 */
	public void unloadChunk(ChunkPos pos) {
		Map<BlockPos, TileEntity> m = newTileEntities.get(pos);
		if (m != null) {
			m.clear();
		}
		newTileEntities.remove(pos);
		// The entity has been saved, so there's no easy way to remove it from the already saved file.
		// TODO: In the future it might be worth rewriting existing chunks for this, but that seems
		// like a fair bit of work for the moment.
		newEntities.get(pos).forEach(e -> entityPositions.remove(e.getUniqueID()));
		newEntities.removeAll(pos);
	}

	/**
	 * Runs a sanity check. Even if the check fails, processing continues, but
	 * the user is warned in chat.
	 *
	 * @see SanityCheck
	 * @param stopOnError True if checking should stop on the first error.
	 * @return false if any sanity checks failed; true otherwise.
	 */
	public boolean runSanityCheck(boolean stopOnError) {
		Map<SanityCheck, Exception> failures = Maps.newEnumMap(SanityCheck.class);

		for (SanityCheck check : SanityCheck.values()) {
			try {
				if (check.canRun()) {
					LOGGER.trace("Running {}", check);
					check.run();
				} else {
					LOGGER.trace("Skipping {}", check);
				}
			} catch (Exception ex) {
				LOGGER.trace("{} failed", check, ex);
				failures.put(check, ex);
				if (stopOnError) {
					break;
				}
			}
		}
		if (!failures.isEmpty()) {
			WDLMessages.chatMessageTranslated(WDL.serverProps, WDLMessageTypes.ERROR, "wdl.sanity.failed");
			for (Map.Entry<SanityCheck, Exception> failure : failures.entrySet()) {
				WDLMessages.chatMessageTranslated(WDL.serverProps, WDLMessageTypes.ERROR, failure.getKey().errorMessage, failure.getValue());
			}
			if (failures.containsKey(SanityCheck.TRANSLATION)) {
				// Err, we can't put translated stuff into chat.  So redo those messages, without translation.
				// For obvious reasons these messages aren't translated.
				WDLMessages.chatMessage(WDL.serverProps, WDLMessageTypes.ERROR, "----- SANITY CHECKS FAILED! -----");
				for (Map.Entry<SanityCheck, Exception> failure : failures.entrySet()) {
					WDLMessages.chatMessage(WDL.serverProps, WDLMessageTypes.ERROR, failure.getKey() + ": " + failure.getValue());
				}
				WDLMessages.chatMessage(WDL.serverProps, WDLMessageTypes.ERROR, "Please check the log for more info.");
			}
			return false;
		}
		return true;
	}

	/**
	 * Is the current server running spigot?
	 *
	 * This is detected based off of the server brand.
	 */
	public boolean isSpigot() {
		if (player != null && player.getServerBrand() != null) {
			String brand = player.getServerBrand().toLowerCase();
			return brand.contains("spigot") || brand.contains("paper");
		}
		return false;
	}

	/**
	 * Gets the current setup information.
	 */
	public String getDebugInfo() {
		Exception ex = new Exception();
		ex.setStackTrace(new StackTraceElement[0]);
		CrashReport report = new CrashReport("Wrapper crash report", ex);
		addInfoToCrash(report);
		StringBuilder sb = new StringBuilder();
		report.getSectionsInStringBuilder(sb);
		return sb.toString();
	}
	/**
	 * Adds information to the given crash report.
	 * @param report The report to add sections to.
	 */
	public void addInfoToCrash(CrashReport report) {
		// Trick the crash report handler into not storing a stack trace
		// (we don't want it)
		int stSize;
		try {
			stSize = Thread.currentThread().getStackTrace().length - 1;
		} catch (Exception e) {
			// Ignore
			stSize = 0;
		}
		CrashReportCategory core = report.makeCategoryDepth(
				"World Downloader Mod - Core", stSize);
		core.addDetail("WDL version", VersionConstants.getModVersion());
		core.addDetail("Minecraft version", VersionConstants.getMinecraftVersionInfo());
		core.addDetail("Expected version", VersionConstants.getExpectedVersion());
		core.addDetail("Protocol version", VersionConstants.getProtocolVersion());
		core.addDetail("Data version", VersionConstants.getDataVersion());
		core.addDetail("File location", () -> {
			//https://stackoverflow.com/q/320542/3991344
			String path = new File(WDL.class.getProtectionDomain()
					.getCodeSource().getLocation().toURI()).getPath();

			//Censor username.
			String username = System.getProperty("user.name");
			path = path.replace(username, "<USERNAME>");

			return path;
		});

		CrashReportCategory ext = report.makeCategoryDepth(
				"World Downloader Mod - Extensions", stSize);
		Map<String, ModInfo<?>> extensions = WDLApi.getWDLMods();
		ext.addDetail("Number loaded", extensions.size());
		for (Map.Entry<String, ModInfo<?>> e : extensions.entrySet()) {
			ext.addDetail(e.getKey(), e.getValue()::getInfo);
		}

		CrashReportCategory state = report.makeCategoryDepth(
				"World Downloader Mod - State", stSize);
		state.addDetail("minecraft", minecraft);
		state.addDetail("worldClient", worldClient);
		state.addDetail("networkManager", networkManager);
		state.addDetail("player", player);
		state.addDetail("windowContainer", windowContainer);
		state.addDetail("lastClickedBlock", lastClickedBlock);
		state.addDetail("lastEntity", lastEntity);
		state.addDetail("saveHandler", saveHandler);
		state.addDetail("chunkLoader", chunkLoader);
		state.addDetail("newTileEntities", newTileEntities);
		state.addDetail("newEntities", newEntities);
		state.addDetail("entityPositions", entityPositions);
		state.addDetail("newMapDatas", newMapDatas);
		state.addDetail("downloading", downloading);
		state.addDetail("isMultiworld", isMultiworld);
		state.addDetail("propsFound", propsFound);
		state.addDetail("startOnChange", startOnChange);
		state.addDetail("overrideLastModifiedCheck", overrideLastModifiedCheck);
		state.addDetail("saving", saving);
		state.addDetail("worldLoadingDeferred", worldLoadingDeferred);
		state.addDetail("worldName", worldName);
		state.addDetail("baseFolderName", baseFolderName);
		state.addDetail("serverProps", serverProps);
		state.addDetail("worldProps", worldProps);
		state.addDetail("globalProps", globalProps);
		state.addDetail("defaultProps", defaultProps);
		state.addDetail("gameRules", gameRules);

		CrashReportCategory base = report.makeCategoryDepth(
				"World Downloader Mod - Server properties", stSize);
		if (serverProps != null) {
			serverProps.addToCrashReport(base, "serverProps");
		} else {
			base.addDetail("-", "null");
		}
		CrashReportCategory world = report.makeCategoryDepth(
				"World Downloader Mod - World properties", stSize);
		if (worldProps != null) {
			worldProps.addToCrashReport(world, "worldProps");
		} else {
			world.addDetail("-", "null");
		}
		CrashReportCategory global = report.makeCategoryDepth(
				"World Downloader Mod - Global properties", stSize);
		if (globalProps != null) {
			globalProps.addToCrashReport(global, "globalProps");
		} else {
			global.addDetail("-", "null");
		}

		CrashReportCategory sanity = report.makeCategoryDepth("Sanity checks", stSize);
		for (SanityCheck check : SanityCheck.values()) {
			sanity.addDetail(check.name(), () -> {
				if (!check.canRun()) {
					return "Skipped";
				}
				check.run();
				return "Passed";
			});
		}
	}

	/**
	 * Call to properly crash the game when an exception is caught in WDL code.
	 *
	 * @param category
	 */
	public void crashed(Throwable t, String category) {
		CrashReport report;

		if (t instanceof ReportedException) {
			CrashReport oldReport = ((ReportedException) t).getCrashReport();

			report = CrashReport.makeCrashReport(oldReport.getCrashCause(),
					category + " (" + oldReport.getCauseStackTraceOrString() + ")");

			try {
				//Steal crashReportSections, and replace it.
				@SuppressWarnings("unchecked")
				List<CrashReportCategory> crashReportSectionsOld = ReflectionUtils
						.findAndGetPrivateField(oldReport, List.class);
				@SuppressWarnings("unchecked")
				List<CrashReportCategory> crashReportSectionsNew = ReflectionUtils
						.findAndGetPrivateField(report, List.class);

				crashReportSectionsNew.addAll(crashReportSectionsOld);
			} catch (Exception e) {
				// Well... some kind of reflection error.
				// No use trying to do anything else.
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
