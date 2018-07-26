/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemMap;
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import wdl.api.IBlockEventListener;
import wdl.api.IChatMessageListener;
import wdl.api.IGuiHooksListener;
import wdl.api.IPluginChannelListener;
import wdl.api.WDLApi;
import wdl.api.WDLApi.ModInfo;
import wdl.gui.GuiTurningCameraBase;
import wdl.gui.GuiWDL;
import wdl.gui.GuiWDLAbout;
import wdl.gui.GuiWDLChunkOverrides;
import wdl.gui.GuiWDLPermissions;
import wdl.gui.widget.Button;

/**
 * The various hooks for WDL. <br/>
 * All of these should be called regardless of any WDL state variables.
 */
public class WDLHooks {
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * If set, enables the profiler.  For unknown reasons, the profiler seems
	 * to use up some memory even when not enabled; see
	 * <a href="https://github.com/Pokechu22/WorldDownloader/pull/77">pull request 77</a>
	 * for more information.
	 *
	 * The compiler should eliminate all references to the profiler when set to false,
	 * as per <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-13.html#jls-13.1-110-C">JLS §13.1</a>
	 * constants must be inlined.  It is not guaranteed that the compiler eliminates
	 * code in an <code>if (false)</code> condition (per JLS §14.9.1) but javac does
	 * optimize it out, as may be verified by javap.
	 */
	static final boolean ENABLE_PROFILER = false;
	private static final Profiler PROFILER = ENABLE_PROFILER ? Minecraft.getMinecraft().profiler : null;

	/**
	 * Called when {@link WorldClient#tick()} is called.
	 * <br/>
	 * Should be at end of the method.
	 */
	public static void onWorldClientTick(WorldClient sender) {
		try {
			if (ENABLE_PROFILER) PROFILER.startSection("wdl");

			if (sender != WDL.worldClient) {
				if (ENABLE_PROFILER) PROFILER.startSection("onWorldLoad");
				if (WDL.worldLoadingDeferred) {
					return;
				}

				WDLEvents.onWorldLoad(sender);
				if (ENABLE_PROFILER) PROFILER.endSection();  // "onWorldLoad"
			} else {
				if (ENABLE_PROFILER) PROFILER.startSection("inventoryCheck");
				if (WDL.downloading && WDL.thePlayer != null) {
					if (WDL.thePlayer.openContainer != WDL.windowContainer) {
						if (WDL.thePlayer.openContainer == WDL.thePlayer.inventoryContainer) {
							boolean handled;

							if (ENABLE_PROFILER) PROFILER.startSection("onItemGuiClosed");
							if (ENABLE_PROFILER) PROFILER.startSection("Core");
							handled = WDLEvents.onItemGuiClosed();
							if (ENABLE_PROFILER) PROFILER.endSection();  // "Core"

							Container container = WDL.thePlayer.openContainer;
							if (WDL.lastEntity != null) {
								Entity entity = WDL.lastEntity;

								for (ModInfo<IGuiHooksListener> info : WDLApi
										.getImplementingExtensions(IGuiHooksListener.class)) {
									if (handled) {
										break;
									}

									if (ENABLE_PROFILER) PROFILER.startSection(info.id);
									handled = info.mod.onEntityGuiClosed(
											sender, entity, container);
									if (ENABLE_PROFILER) PROFILER.endSection();  // info.id
								}

								if (!handled) {
									WDLMessages.chatMessageTranslated(
											WDL.baseProps,
											WDLMessageTypes.ON_GUI_CLOSED_WARNING,
											"wdl.messages.onGuiClosedWarning.unhandledEntity", entity);
								}
							} else {
								BlockPos pos = WDL.lastClickedBlock;
								for (ModInfo<IGuiHooksListener> info : WDLApi
										.getImplementingExtensions(IGuiHooksListener.class)) {
									if (handled) {
										break;
									}

									if (ENABLE_PROFILER) PROFILER.startSection(info.id);
									handled = info.mod.onBlockGuiClosed(
											sender, pos, container);
									if (ENABLE_PROFILER) PROFILER.endSection();  // info.id
								}

								if (!handled) {
									WDLMessages.chatMessageTranslated(
											WDL.baseProps,
											WDLMessageTypes.ON_GUI_CLOSED_WARNING,
											"wdl.messages.onGuiClosedWarning.unhandledTileEntity", pos, sender.getTileEntity(pos));
								}
							}

							if (ENABLE_PROFILER) PROFILER.endSection();  // onItemGuiClosed
						} else {
							if (ENABLE_PROFILER) PROFILER.startSection("onItemGuiOpened");
							if (ENABLE_PROFILER) PROFILER.startSection("Core");
							WDLEvents.onItemGuiOpened();
							if (ENABLE_PROFILER) PROFILER.endSection();  // "Core"
							if (ENABLE_PROFILER) PROFILER.endSection();  // "onItemGuiOpened"
						}

						WDL.windowContainer = WDL.thePlayer.openContainer;
					}
				}
				if (ENABLE_PROFILER) PROFILER.endSection();  // "inventoryCheck"
			}

			if (ENABLE_PROFILER) PROFILER.startSection("camera");
			GuiTurningCameraBase.onWorldTick();
			if (ENABLE_PROFILER) PROFILER.endSection();  // "camera"
			if (ENABLE_PROFILER) PROFILER.endSection();  // "wdl"
		} catch (Throwable e) {
			WDL.crashed(e, "WDL mod: exception in onWorldClientTick event");
		}
	}

	/**
	 * Called when {@link WorldClient#doPreChunk(int, int, boolean)} is called.
	 * <br/>
	 * Should be at the start of the method.
	 */
	public static void onWorldClientDoPreChunk(WorldClient sender, int x,
			int z, boolean loading) {
		try {
			if (!WDL.downloading) { return; }

			if (ENABLE_PROFILER) PROFILER.startSection("wdl");

			if (!loading) {
				if (ENABLE_PROFILER) PROFILER.startSection("onChunkNoLongerNeeded");
				Chunk c = sender.getChunk(x, z);

				if (ENABLE_PROFILER) PROFILER.startSection("Core");
				wdl.WDLEvents.onChunkNoLongerNeeded(c);
				if (ENABLE_PROFILER) PROFILER.endSection();  // "Core"

				if (ENABLE_PROFILER) PROFILER.endSection();  // "onChunkNoLongerNeeded"
			} else {
				LOGGER.debug("Adding new empty chunk at " + x + ", " + z + " (already has: " + (sender.getChunkProvider().getLoadedChunk(x, z) != null) + ")");
			}

			if (ENABLE_PROFILER) PROFILER.endSection();
		} catch (Throwable e) {
			WDL.crashed(e, "WDL mod: exception in onWorldDoPreChunk event");
		}
	}

	/**
	 * Called when {@link WorldClient#removeEntityFromWorld(int)} is called.
	 * <br/>
	 * Should be at the start of the method.
	 *
	 * @param eid
	 *            The entity's unique ID.
	 */
	public static void onWorldClientRemoveEntityFromWorld(WorldClient sender,
			int eid) {
		try {
			if (!WDL.downloading) { return; }

			if (ENABLE_PROFILER) PROFILER.startSection("wdl.onRemoveEntityFromWorld");

			Entity entity = sender.getEntityByID(eid);

			if (ENABLE_PROFILER) PROFILER.startSection("Core");
			WDLEvents.onRemoveEntityFromWorld(entity);
			if (ENABLE_PROFILER) PROFILER.endSection();  // "Core"

			if (ENABLE_PROFILER) PROFILER.endSection();  // "wdl.onRemoveEntityFromWorld"
		} catch (Throwable e) {
			WDL.crashed(e,
					"WDL mod: exception in onWorldRemoveEntityFromWorld event");
		}
	}

	/**
	 * Called when {@link NetHandlerPlayClient#handleChat(SPacketChat)} is
	 * called.
	 * <br/>
	 * Should be at the end of the method.
	 */
	public static void onNHPCHandleChat(NetHandlerPlayClient sender,
			SPacketChat packet) {
		try {
			if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
				return;
			}

			if (!WDL.downloading) { return; }

			if (ENABLE_PROFILER) PROFILER.startSection("wdl.onChatMessage");

			String chatMessage = packet.getChatComponent().getString();

			if (ENABLE_PROFILER) PROFILER.startSection("Core");
			WDLEvents.onChatMessage(chatMessage);
			if (ENABLE_PROFILER) PROFILER.endSection();  // "Core"

			for (ModInfo<IChatMessageListener> info : WDLApi
					.getImplementingExtensions(IChatMessageListener.class)) {
				if (ENABLE_PROFILER) PROFILER.startSection(info.id);
				info.mod.onChat(WDL.worldClient, chatMessage);
				if (ENABLE_PROFILER) PROFILER.endSection();  // info.id
			}

			if (ENABLE_PROFILER) PROFILER.endSection();  // "wdl.onChatMessage"
		} catch (Throwable e) {
			WDL.crashed(e, "WDL mod: exception in onNHPCHandleChat event");
		}
	}

	/**
	 * Called when {@link NetHandlerPlayClient#handleMaps(SPacketMaps)} is
	 * called.
	 * <br/>
	 * Should be at the end of the method.
	 */
	public static void onNHPCHandleMaps(NetHandlerPlayClient sender,
			SPacketMaps packet) {
		try {
			if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
				return;
			}

			if (!WDL.downloading) { return; }

			if (ENABLE_PROFILER) PROFILER.startSection("wdl.onMapDataLoaded");

			int id = packet.getMapId();
			MapData mapData = ItemMap.loadMapData(packet.getMapId(),
					WDL.worldClient);

			if (ENABLE_PROFILER) PROFILER.startSection("Core");
			WDLEvents.onMapDataLoaded(id, mapData);
			if (ENABLE_PROFILER) PROFILER.endSection();  // "Core"

			if (ENABLE_PROFILER) PROFILER.endSection();  // "wdl.onMapDataLoaded"
		} catch (Throwable e) {
			WDL.crashed(e, "WDL mod: exception in onNHPCHandleMaps event");
		}
	}

	/**
	 * Called when
	 * {@link NetHandlerPlayClient#handleCustomPayload(SPacketCustomPayload)}
	 * is called.
	 * <br/>
	 * Should be at the end of the method.
	 */
	public static void onNHPCHandleCustomPayload(NetHandlerPlayClient sender,
			SPacketCustomPayload packet) {
		try {
			if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
				return;
			}
			if (!packet.getBufferData().isReadable()) {
				return;
			}
			if (ENABLE_PROFILER) PROFILER.startSection("wdl.onPluginMessage");

			if (ENABLE_PROFILER) PROFILER.startSection("Parse");
			String channel = packet.getChannelName().toString(); // 1.13: ResourceLocation -> String; otherwise no-op
			ByteBuf buf = packet.getBufferData();
			byte[] payload = new byte[buf.readableBytes()];
			buf.readBytes(payload);
			if (ENABLE_PROFILER) PROFILER.endSection();  // "Parse"

			if (ENABLE_PROFILER) PROFILER.startSection("Core");
			WDLEvents.onPluginChannelPacket(channel, payload);
			if (ENABLE_PROFILER) PROFILER.endSection();  // "Core"

			for (ModInfo<IPluginChannelListener> info : WDLApi
					.getImplementingExtensions(IPluginChannelListener.class)) {
				if (ENABLE_PROFILER) PROFILER.startSection(info.id);
				info.mod.onPluginChannelPacket(WDL.worldClient, channel,
						payload);
				if (ENABLE_PROFILER) PROFILER.endSection();  // info.id
			}

			if (ENABLE_PROFILER) PROFILER.endSection();  // "wdl.onPluginMessage"
		} catch (Throwable e) {
			WDL.crashed(e,
					"WDL mod: exception in onNHPCHandleCustomPayload event");
		}
	}

	/**
	 * Called when
	 * {@link NetHandlerPlayClient#handleBlockAction(SPacketBlockAction)} is
	 * called.
	 * <br/>
	 * Should be at the end of the method.
	 */
	public static void onNHPCHandleBlockAction(NetHandlerPlayClient sender,
			SPacketBlockAction packet) {
		try {
			if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
				return;
			}

			if (!WDL.downloading) { return; }

			if (ENABLE_PROFILER) PROFILER.startSection("wdl.onBlockEvent");

			BlockPos pos = packet.getBlockPosition();
			Block block = packet.getBlockType();
			int data1 = packet.getData1();
			int data2 = packet.getData2();

			if (ENABLE_PROFILER) PROFILER.startSection("Core");
			WDLEvents.onBlockEvent(pos, block, data1, data2);
			if (ENABLE_PROFILER) PROFILER.endSection();  // "Core"

			for (ModInfo<IBlockEventListener> info : WDLApi
					.getImplementingExtensions(IBlockEventListener.class)) {
				if (ENABLE_PROFILER) PROFILER.startSection(info.id);
				info.mod.onBlockEvent(WDL.worldClient, pos, block,
						data1, data2);
				if (ENABLE_PROFILER) PROFILER.endSection();  // info.id
			}

			if (ENABLE_PROFILER) PROFILER.endSection();  // "wdl.onBlockEvent"
		} catch (Throwable e) {
			WDL.crashed(e,
					"WDL mod: exception in onNHPCHandleBlockAction event");
		}
	}

	/**
	 * Injects WDL information into a crash report.
	 *
	 * Called at the end of {@link CrashReport#populateEnvironment()}.
	 * @param report
	 */
	public static void onCrashReportPopulateEnvironment(CrashReport report) {
		WDL.addInfoToCrash(report);
	}

	/**
	 * Start button ID. Ascii-encoded 'WDLs' (World Downloader Start).
	 * Chosen to be unique.
	 */
	private static final int WDLs = ('W' << 24) | ('D' << 16) | ('L' << 8) | ('s');
	/**
	 * Options button ID. Ascii-encoded 'WDLo' (World Downloader Options).
	 * Chosen to be unique.
	 */
	private static final int WDLo = ('W' << 24) | ('D' << 16) | ('L' << 8) | ('o');

	private static class StartDownloadButton extends Button {
		public StartDownloadButton(GuiScreen menu, int x, int y, int width, int height) {
			super(x, y, width, height, null);
			this.menu = menu;
			this.id = WDLs; // keep unique, even though this isn't used by WDL
		}

		// The GuiScreen containing this button, as a parent for other GUIs
		private final GuiScreen menu;

		@Override
		public void beforeDraw() {
			final String displayString;
			final boolean enabled;
			if (WDL.minecraft.isIntegratedServerRunning()) {
				// Singleplayer
				displayString = I18n
						.format("wdl.gui.ingameMenu.downloadStatus.singlePlayer");
				enabled = false;
			} else if (!WDLPluginChannels.canDownloadAtAll()) {
				if (WDLPluginChannels.canRequestPermissions()) {
					// Allow requesting permissions.
					displayString = I18n
							.format("wdl.gui.ingameMenu.downloadStatus.request");
					enabled = true;
				} else {
					// Out of date plugin :/
					displayString = I18n
							.format("wdl.gui.ingameMenu.downloadStatus.disabled");
					enabled = false;
				}
			} else if (WDL.saving) {
				// Normally not accessible; only happens as a major fallback...
				displayString = I18n
						.format("wdl.gui.ingameMenu.downloadStatus.saving");
				enabled = false;
			} else if (WDL.downloading) {
				displayString = I18n
						.format("wdl.gui.ingameMenu.downloadStatus.stop");
				enabled = true;
			} else {
				displayString = I18n
						.format("wdl.gui.ingameMenu.downloadStatus.start");
				enabled = true;
			}
			this.enabled = enabled;
			this.displayString = displayString;
		}

		@Override
		public void performAction() {
			if (WDL.minecraft.isIntegratedServerRunning()) {
				return; // WDL not available if in singleplayer or LAN server mode
			}

			if (WDL.downloading) {
				WDL.stopDownload();
				enabled = false; // Disable to stop double-clicks
			} else {
				if (!WDLPluginChannels.canDownloadAtAll()) {
					// If they don't have any permissions, let the player
					// request some.
					if (WDLPluginChannels.canRequestPermissions()) {
						WDL.minecraft.displayGuiScreen(new GuiWDLPermissions(menu));
					} else {
						// Should never happen
					}
				} else if (WDLPluginChannels.hasChunkOverrides()
						&& !WDLPluginChannels.canDownloadInGeneral()) {
					// Handle the "only has chunk overrides" state - notify
					// the player of limited areas.
					WDL.minecraft.displayGuiScreen(new GuiWDLChunkOverrides(menu));
				} else {
					WDL.startDownload();
					enabled = false; // Disable to stop double-clicks
				}
			}
		}
	}

	private static class SettingsButton extends Button {
		public SettingsButton(GuiScreen menu, int x, int y, int width, int height, String displayString) {
			super(x, y, width, height, displayString);
			this.menu = menu;
			this.id = WDLo; // keep unique, even though this isn't used by WDL
		}

		// The GuiScreen containing this button, as a parent for other GUIs
		private final GuiScreen menu;

		@Override
		public void performAction() {
			if (WDL.minecraft.isIntegratedServerRunning()) {
				WDL.minecraft.displayGuiScreen(new GuiWDLAbout(menu));
			} else {
				WDL.minecraft.displayGuiScreen(new GuiWDL(menu));
			}
		}
	}

	/**
	 * Adds the "Download this world" button to the ingame pause GUI.
	 *
	 * @param gui
	 * @param buttonList
	 */
	public static void injectWDLButtons(GuiIngameMenu gui, List<GuiButton> buttonList) {
		int insertAtYPos = 0;

		for (GuiButton btn : buttonList) {
			if (btn.id == 5) { // Button "Achievements"
				insertAtYPos = btn.y + 24;
				break;
			}
		}

		// Move other buttons down one slot (= 24 height units)
		for (GuiButton btn : buttonList) {
			if (btn.y >= insertAtYPos) {
				btn.y += 24;
			}
		}

		// Insert wdl buttons.
		buttonList.add(new StartDownloadButton(gui,
				gui.width / 2 - 100, insertAtYPos, 170, 20));

		buttonList.add(new SettingsButton(gui,
				gui.width / 2 + 71, insertAtYPos, 28, 20,
				I18n.format("wdl.gui.ingameMenu.settings")));
	}

	/**
	 * Handle clicks in the ingame pause GUI.
	 *
	 * @param gui
	 * @param button
	 */
	public static void handleWDLButtonClick(GuiIngameMenu gui, GuiButton button) {
		if (!button.enabled) {
			return;
		}

		if (button.id == 1) { // "Disconnect", from vanilla
			WDL.stopDownload();
			// Disable the button to prevent double-clicks
			button.enabled = false;
		}
	}
}
