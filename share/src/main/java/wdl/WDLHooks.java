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

import java.util.Collection;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;

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
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
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
import wdl.gui.widget.WDLButton;
import wdl.versioned.VersionedFunctions;

/**
 * The various hooks for wdl. <br/>
 * All of these should be called regardless of any WDL state variables.
 */
public class WDLHooks {
	/**
	 * Instance which should receive event calls.
	 * Only exists (and is mutable) for the purpose of testing.
	 */
	@VisibleForTesting
	@Nonnull
	static WDLHooks INSTANCE = new WDLHooks();

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
	private static final Profiler PROFILER = ENABLE_PROFILER ? Minecraft.getInstance().profiler : null;

	private final WDL wdl = WDL.getInstance();

	/**
	 * Called when {@link WorldClient#tick()} is called.
	 * <br/>
	 * Should be at end of the method.
	 */
	public static void onWorldClientTick(WorldClient sender) {
		INSTANCE.onWorldClientTick0(sender);
	}
	protected void onWorldClientTick0(WorldClient sender) {
		try {
			if (ENABLE_PROFILER) PROFILER.startSection("wdl");

			if (sender != wdl.worldClient) {
				if (ENABLE_PROFILER) PROFILER.startSection("onWorldLoad");
				if (WDL.worldLoadingDeferred) {
					return;
				}

				WDLEvents.onWorldLoad(sender);
				if (ENABLE_PROFILER) PROFILER.endSection();  // "onWorldLoad"
			} else {
				if (ENABLE_PROFILER) PROFILER.startSection("inventoryCheck");
				if (WDL.downloading && wdl.player != null) {
					if (wdl.player.openContainer != wdl.windowContainer) {
						if (wdl.player.openContainer == wdl.player.container) {
							boolean handled;

							if (ENABLE_PROFILER) PROFILER.startSection("onItemGuiClosed");
							if (ENABLE_PROFILER) PROFILER.startSection("Core");
							handled = WDLEvents.onItemGuiClosed();
							if (ENABLE_PROFILER) PROFILER.endSection();  // "Core"

							Container container = wdl.player.openContainer;
							if (wdl.lastEntity != null) {
								Entity entity = wdl.lastEntity;

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
											WDL.serverProps,
											WDLMessageTypes.ON_GUI_CLOSED_WARNING,
											"wdl.messages.onGuiClosedWarning.unhandledEntity", entity);
								}
							} else if (wdl.lastClickedBlock != null) {
								BlockPos pos = wdl.lastClickedBlock;
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
											WDL.serverProps,
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

						wdl.windowContainer = wdl.player.openContainer;
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
	 * Called when {@link WorldClient#removeEntityFromWorld(int)} is called.
	 * <br/>
	 * Should be at the start of the method.
	 *
	 * @param eid
	 *            The entity's unique ID.
	 */
	public static void onWorldClientRemoveEntityFromWorld(WorldClient sender,
			int eid) {
		INSTANCE.onWorldClientRemoveEntityFromWorld0(sender, eid);
	}
	protected void onWorldClientRemoveEntityFromWorld0(WorldClient sender,
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
	 * Called when {@link NetHandlerPlayClient#processChunkUnload(SPacketUnloadChunk)} is called.
	 * <br/>
	 * Should be at the start of the method.
	 */
	public static void onNHPCHandleChunkUnload(NetHandlerPlayClient sender,
			WorldClient world, SPacketUnloadChunk packet) {
		INSTANCE.onNHPCHandleChunkUnload0(sender, world, packet);
	}
	protected void onNHPCHandleChunkUnload0(NetHandlerPlayClient sender,
			WorldClient world, SPacketUnloadChunk packet) {
		try {
			if (!Minecraft.getInstance().isOnExecutionThread()) {
				return;
			}

			if (!WDL.downloading) { return; }

			if (ENABLE_PROFILER) PROFILER.startSection("wdl.onChunkNoLongerNeeded");
			Chunk chunk = world.getChunk(packet.getX(), packet.getZ());

			if (ENABLE_PROFILER) PROFILER.startSection("Core");
			WDLEvents.onChunkNoLongerNeeded(chunk);
			if (ENABLE_PROFILER) PROFILER.endSection();  // "Core"

			if (ENABLE_PROFILER) PROFILER.endSection();  // "wdl.onChunkNoLongerNeeded"
		} catch (Throwable e) {
			WDL.crashed(e, "WDL mod: exception in onNHPCHandleChunkUnload event");
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
		INSTANCE.onNHPCHandleChat0(sender, packet);
	}
	protected void onNHPCHandleChat0(NetHandlerPlayClient sender,
			SPacketChat packet) {
		try {
			if (!Minecraft.getInstance().isOnExecutionThread()) {
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
				info.mod.onChat(wdl.worldClient, chatMessage);
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
		INSTANCE.onNHPCHandleMaps0(sender, packet);
	}
	protected void onNHPCHandleMaps0(NetHandlerPlayClient sender,
			SPacketMaps packet) {
		try {
			if (!Minecraft.getInstance().isOnExecutionThread()) {
				return;
			}

			if (!WDL.downloading) { return; }

			if (ENABLE_PROFILER) PROFILER.startSection("wdl.onMapDataLoaded");

			MapData mapData = VersionedFunctions.getMapData(wdl.worldClient, packet);

			if (mapData != null) {
				if (ENABLE_PROFILER) PROFILER.startSection("Core");
				WDLEvents.onMapDataLoaded(packet.getMapId(), mapData);
				if (ENABLE_PROFILER) PROFILER.endSection();  // "Core"
			} else {
				LOGGER.warn("Received a null map data: " + packet.getMapId());
			}

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
		INSTANCE.onNHPCHandleCustomPayload0(sender, packet);
	}
	protected void onNHPCHandleCustomPayload0(NetHandlerPlayClient sender,
			SPacketCustomPayload packet) {
		try {
			if (!Minecraft.getInstance().isOnExecutionThread()) {
				return;
			}
			if (ENABLE_PROFILER) PROFILER.startSection("wdl.onPluginMessage");

			if (ENABLE_PROFILER) PROFILER.startSection("Parse");
			String channel = packet.getChannelName().toString(); // 1.13: ResourceLocation -> String; otherwise no-op
			ByteBuf buf = packet.getBufferData();
			int refCnt = buf.refCnt();
			if (refCnt <= 0) {
				// The buffer has already been released.  Just break out now.
				// This happens with e.g. the MC|TrList packet (villager trade list),
				// which closes the buffer after reading it.
				if (ENABLE_PROFILER) PROFILER.endSection();  // "Parse"
				if (ENABLE_PROFILER) PROFILER.endSection();  // "wdl.onPluginMessage"
				return;
			}

			// Something else may have already read the payload; return to the start
			buf.markReaderIndex();
			buf.readerIndex(0);
			byte[] payload = new byte[buf.readableBytes()];
			buf.readBytes(payload);
			// OK, now that we've done our reading, return to where it was before
			// (which could be the end, or other code might not have read it yet)
			buf.resetReaderIndex();
			// buf will be released by the packet handler, eventually.
			// It definitely is NOT our responsibility to release it, as
			// doing so would probably break other code outside of wdl.
			// Perhaps we might want to call retain once at the start of this method
			// and then release at the end, but that feels excessive (since there
			// _shouldn't_ be multiple threads at play at this point, and if there
			// were we'd be in trouble anyways).

			if (ENABLE_PROFILER) PROFILER.endSection();  // "Parse"

			if (ENABLE_PROFILER) PROFILER.startSection("Core");
			WDLEvents.onPluginChannelPacket(sender, channel, payload);
			if (ENABLE_PROFILER) PROFILER.endSection();  // "Core"

			for (ModInfo<IPluginChannelListener> info : WDLApi
					.getImplementingExtensions(IPluginChannelListener.class)) {
				if (ENABLE_PROFILER) PROFILER.startSection(info.id);
				info.mod.onPluginChannelPacket(wdl.worldClient, channel,
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
		INSTANCE.onNHPCHandleBlockAction0(sender, packet);
	}
	protected void onNHPCHandleBlockAction0(NetHandlerPlayClient sender,
			SPacketBlockAction packet) {
		try {
			if (!Minecraft.getInstance().isOnExecutionThread()) {
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
				info.mod.onBlockEvent(wdl.worldClient, pos, block,
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
	 * Called when {@link NetHandlerPlayClient#onDisconnect(ITextComponent)} is called.
	 * <br/>
	 * Should be at the start of the method.
	 *
	 * @param reason The reason for the disconnect, as passed to onDisconnect.
	 */
	public static void onNHPCDisconnect(NetHandlerPlayClient sender, ITextComponent reason) {
		INSTANCE.onNHPCDisconnect0(sender, reason);
	}
	protected void onNHPCDisconnect0(NetHandlerPlayClient sender, ITextComponent reason) {
		if (WDL.downloading) {
			// This is likely to be called from an unexpected thread, so queue a task
			Minecraft.getInstance().enqueue(wdl::stopDownload);

			// This code was present on older versions of WDL which weren't missing
			// the onDisconnect handler before.
			// It presumably makes sure that the disconnect doesn't propagate to other state variables,
			// but I don't completely trust it
			try {
				Thread.sleep(2000L);
			} catch (InterruptedException e) { }
		}
	}

	/**
	 * Injects WDL information into a crash report.
	 *
	 * Called at the end of {@link CrashReport#populateEnvironment()}.
	 * @param report
	 */
	public static void onCrashReportPopulateEnvironment(CrashReport report) {
		INSTANCE.onCrashReportPopulateEnvironment0(report);
	}
	protected void onCrashReportPopulateEnvironment0(CrashReport report) {
		wdl.addInfoToCrash(report);
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

	private class StartDownloadButton extends WDLButton {
		@SuppressWarnings("deprecation")
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
			if (wdl.minecraft.isIntegratedServerRunning()) {
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
			setEnabled(enabled);
			setMessage(displayString);
		}

		@Override
		public void performAction() {
			if (wdl.minecraft.isIntegratedServerRunning()) {
				return; // WDL not available if in singleplayer or LAN server mode
			}

			if (WDL.downloading) {
				wdl.stopDownload();
				setEnabled(false); // Disable to stop double-clicks
			} else {
				if (!WDLPluginChannels.canDownloadAtAll()) {
					// If they don't have any permissions, let the player
					// request some.
					if (WDLPluginChannels.canRequestPermissions()) {
						wdl.minecraft.displayGuiScreen(new GuiWDLPermissions(menu, wdl));
					} else {
						// Should never happen
					}
				} else if (WDLPluginChannels.hasChunkOverrides()
						&& !WDLPluginChannels.canDownloadInGeneral()) {
					// Handle the "only has chunk overrides" state - notify
					// the player of limited areas.
					wdl.minecraft.displayGuiScreen(new GuiWDLChunkOverrides(menu, wdl));
				} else {
					wdl.startDownload();
					setEnabled(false); // Disable to stop double-clicks
				}
			}
		}
	}

	private class SettingsButton extends WDLButton {
		@SuppressWarnings("deprecation")
		public SettingsButton(GuiScreen menu, int x, int y, int width, int height, String displayString) {
			super(x, y, width, height, displayString);
			this.menu = menu;
			this.id = WDLo; // keep unique, even though this isn't used by WDL
		}

		// The GuiScreen containing this button, as a parent for other GUIs
		private final GuiScreen menu;

		@Override
		public void performAction() {
			if (wdl.minecraft.isIntegratedServerRunning()) {
				wdl.minecraft.displayGuiScreen(new GuiWDLAbout(menu, wdl));
			} else {
				if (wdl.promptForInfoForSettings("changeOptions", false, this::performAction, () -> wdl.minecraft.displayGuiScreen(null))) {
					return;
				}
				wdl.minecraft.displayGuiScreen(new GuiWDL(menu, wdl));
			}
		}
	}

	/**
	 * Adds WDL's buttons to the pause menu GUI.
	 *
	 * @param gui        The GUI
	 * @param buttonList The list of buttons in the GUI. This list should not be
	 *                   modified directly.
	 * @param addButton  Method to add a button to the GUI.
	 */
	public static void injectWDLButtons(GuiIngameMenu gui, Collection<? super GuiButton> buttonList,
			Consumer<GuiButton> addButton) {
		INSTANCE.injectWDLButtons0(gui, buttonList, addButton);
	}
	@SuppressWarnings("deprecation")
	protected void injectWDLButtons0(GuiIngameMenu gui, Collection<? super GuiButton> buttonList,
			Consumer<GuiButton> addButton) {
		int insertAtYPos = 0;

		for (Object o : buttonList) {
			if (!(o instanceof GuiButton)) {
				continue;
			}
			GuiButton btn = (GuiButton)o;
			if (btn.id == 5) { // Button "Achievements"
				insertAtYPos = btn.y + 24;
				break;
			}
		}

		// Move other buttons down one slot (= 24 height units)
		for (Object o : buttonList) {
			if (!(o instanceof GuiButton)) {
				continue;
			}
			GuiButton btn = (GuiButton)o;
			if (btn.y >= insertAtYPos) {
				btn.y += 24;
			}
		}

		// Insert wdl buttons.
		addButton.accept(new StartDownloadButton(gui,
				gui.width / 2 - 100, insertAtYPos, 170, 20));

		addButton.accept(new SettingsButton(gui,
				gui.width / 2 + 72, insertAtYPos, 28, 20,
				I18n.format("wdl.gui.ingameMenu.settings")));
	}

	/**
	 * Handle clicks to the ingame pause GUI, specifically for the disconnect
	 * button.
	 *
	 * @param gui    The GUI
	 * @param button The button that was clicked.
	 */
	public static void handleWDLButtonClick(GuiIngameMenu gui, GuiButton button) {
		INSTANCE.handleWDLButtonClick0(gui, button);
	}
	@SuppressWarnings("deprecation")
	protected void handleWDLButtonClick0(GuiIngameMenu gui, GuiButton button) {
		if (button.id == 1) { // "Disconnect", from vanilla
			wdl.stopDownload();
			// Disable the button to prevent double-clicks
			button.active = false;
		}
	}
}
