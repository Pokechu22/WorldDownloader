package wdl;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemMap;
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.profiler.Profiler;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import wdl.api.IBlockEventListener;
import wdl.api.IChatMessageListener;
import wdl.api.IGuiHooksListener;
import wdl.api.IPluginChannelListener;
import wdl.api.WDLApi;
import wdl.api.WDLApi.ModInfo;
import wdl.gui.GuiWDL;
import wdl.gui.GuiWDLAbout;
import wdl.gui.GuiWDLChunkOverrides;
import wdl.gui.GuiWDLPermissions;

import com.google.common.collect.ImmutableList;

/**
 * The various hooks for WDL. <br/>
 * All of these should be called regardless of any WDL state variables.
 */
public class WDLHooks {
	private static final Profiler profiler = Minecraft.getMinecraft().mcProfiler;
	
	/**
	 * Called when {@link WorldClient#tick()} is called.
	 * <br/>
	 * Should be at end of the method.
	 */
	public static void onWorldClientTick(WorldClient sender) {
		try {
			profiler.startSection("wdl");
			
			List<EntityPlayer> players = ImmutableList.copyOf(sender.playerEntities);
			
			if (sender != WDL.worldClient) {
				profiler.startSection("onWorldLoad");
				if (WDL.worldLoadingDeferred) {
					return;
				}
				
				WDLEvents.onWorldLoad(sender);
				profiler.endSection();  // "onWorldLoad"
			} else {
				profiler.startSection("inventoryCheck");
				if (WDL.downloading && WDL.thePlayer != null) {
					if (WDL.thePlayer.openContainer != WDL.windowContainer) {
						if (WDL.thePlayer.openContainer == WDL.thePlayer.inventoryContainer) {
							boolean handled;
							
							profiler.startSection("onItemGuiClosed");
							profiler.startSection("Core");
							handled = WDLEvents.onItemGuiClosed();
							profiler.endSection();  // "Core"
							
							Container container = WDL.thePlayer.openContainer;
							if (WDL.lastEntity != null) {
								Entity entity = WDL.lastEntity;

								for (ModInfo<IGuiHooksListener> info : WDLApi
										.getImplementingExtensions(IGuiHooksListener.class)) {
									if (handled) {
										break;
									}

									profiler.startSection(info.id);
									handled = info.mod.onEntityGuiClosed(
											sender, entity, container);
									profiler.endSection();  // info.id
								}
								
								if (!handled) {
									WDLMessages.chatMessageTranslated(
											WDLMessageTypes.ON_GUI_CLOSED_WARNING,
											"wdl.messages.onGuiClosedWarning.unhandledEntity",
											entity);
								}
							} else {
								BlockPos pos = WDL.lastClickedBlock;
								for (ModInfo<IGuiHooksListener> info : WDLApi
										.getImplementingExtensions(IGuiHooksListener.class)) {
									if (handled) {
										break;
									}

									profiler.startSection(info.id);
									handled = info.mod.onBlockGuiClosed(
											sender, pos, container);
									profiler.endSection();  // info.id
								}
								
								if (!handled) {
									WDLMessages.chatMessageTranslated(
											WDLMessageTypes.ON_GUI_CLOSED_WARNING,
											"wdl.messages.onGuiClosedWarning.unhandledTileEntity",
											pos, sender.getTileEntity(pos));
								}
							}
							
							profiler.endSection();  // onItemGuiClosed
						} else {
							profiler.startSection("onItemGuiOpened");
							profiler.startSection("Core");
							WDLEvents.onItemGuiOpened();
							profiler.endSection();  // "Core"
							profiler.endSection();  // "onItemGuiOpened"
						}
	
						WDL.windowContainer = WDL.thePlayer.openContainer;
					}
				}
				profiler.endSection();  // "inventoryCheck"
			}
			
			profiler.startSection("capes");
			CapeHandler.onWorldTick(players);
			profiler.endSection();  // "capes"
			profiler.endSection();  // "wdl"
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
			
			profiler.startSection("wdl");
			
			if (!loading) {
				profiler.startSection("onChunkNoLongerNeeded");
				Chunk c = sender.getChunkFromChunkCoords(x, z); 
				
				profiler.startSection("Core");
				wdl.WDLEvents.onChunkNoLongerNeeded(c);
				profiler.endSection();  // "Core"
				
				profiler.endSection();  // "onChunkNoLongerNeeded"
			}
			
			profiler.endSection();
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
			
			profiler.startSection("wdl.onRemoveEntityFromWorld");
			
			Entity entity = sender.getEntityByID(eid);
			
			profiler.startSection("Core");
			WDLEvents.onRemoveEntityFromWorld(entity);
			profiler.endSection();  // "Core"
			
			profiler.endSection();  // "wdl.onRemoveEntityFromWorld"
		} catch (Throwable e) {
			WDL.crashed(e,
					"WDL mod: exception in onWorldRemoveEntityFromWorld event");
		}
	}

	/**
	 * Called when {@link NetHandlerPlayClient#handleChat(SPacketChat)} is
	 * called.
	 * <br/>
	 * Should be at the start of the method.
	 */
	public static void onNHPCHandleChat(NetHandlerPlayClient sender,
			SPacketChat packet) {
		try {
			if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
				return;
			}
			
			if (!WDL.downloading) { return; }
			
			profiler.startSection("wdl.onChatMessage");
			
			//func_148915_c returns the ITextComponent.
			String chatMessage = packet.getChatComponent().getUnformattedText();
			
			profiler.startSection("Core");
			WDLEvents.onChatMessage(chatMessage);
			profiler.endSection();  // "Core"
			
			for (ModInfo<IChatMessageListener> info : WDLApi
					.getImplementingExtensions(IChatMessageListener.class)) {
				profiler.startSection(info.id);
				info.mod.onChat(WDL.worldClient, chatMessage);
				profiler.endSection();  // info.id
			}
			
			profiler.endSection();  // "wdl.onChatMessage"
		} catch (Throwable e) {
			WDL.crashed(e, "WDL mod: exception in onNHPCHandleChat event");
		}
	}

	/**
	 * Called when {@link NetHandlerPlayClient#handleMaps(SPacketMaps)} is
	 * called.
	 * <br/>
	 * Should be at the start of the method.
	 */
	public static void onNHPCHandleMaps(NetHandlerPlayClient sender,
			SPacketMaps packet) {
		try {
			if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
				return;
			}
			
			if (!WDL.downloading) { return; }
			
			profiler.startSection("wdl.onMapDataLoaded");
			
			int id = packet.getMapId();
			MapData mapData = ItemMap.loadMapData(packet.getMapId(),
					WDL.worldClient);
			
			profiler.startSection("Core");
			WDLEvents.onMapDataLoaded(id, mapData);
			profiler.endSection();  // "Core"
			
			profiler.endSection();  // "wdl.onMapDataLoaded"
		} catch (Throwable e) {
			WDL.crashed(e, "WDL mod: exception in onNHPCHandleMaps event");
		}
	}

	/**
	 * Called when
	 * {@link NetHandlerPlayClient#handleCustomPayload(SPacketCustomPayload)}
	 * is called.
	 * <br/>
	 * Should be at the start of the method.
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
			String channel = packet.getChannelName();
			byte[] payload = packet.getBufferData().array();
			
			profiler.startSection("wdl.onPluginMessage");
			profiler.startSection("Core");
			WDLEvents.onPluginChannelPacket(channel, payload);
			profiler.endSection();  // "Core"
			
			for (ModInfo<IPluginChannelListener> info : WDLApi
					.getImplementingExtensions(IPluginChannelListener.class)) {
				profiler.startSection(info.id);
				info.mod.onPluginChannelPacket(WDL.worldClient, channel,
						payload);
				profiler.endSection();  // info.id
			}
			
			profiler.endSection();  // "wdl.onPluginMessage"
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
	 * Should be at the start of the method.
	 */
	public static void onNHPCHandleBlockAction(NetHandlerPlayClient sender,
			SPacketBlockAction packet) {
		try {
			if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
				return;
			}
			
			if (!WDL.downloading) { return; }
			
			profiler.startSection("wdl.onBlockEvent");
			
			BlockPos pos = packet.getBlockPosition();
			Block block = packet.getBlockType();
			int data1 = packet.getData1();
			int data2 = packet.getData2();
			
			profiler.startSection("Core");
			WDLEvents.onBlockEvent(pos, block, data1, data2);
			profiler.endSection();  // "Core"
			
			for (ModInfo<IBlockEventListener> info : WDLApi
					.getImplementingExtensions(IBlockEventListener.class)) {
				profiler.startSection(info.id);
				info.mod.onBlockEvent(WDL.worldClient, pos, block, 
						data1, data2);
				profiler.endSection();  // info.id
			}
			
			profiler.endSection();  // "wdl.onBlockEvent"
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
		report.makeCategory("World Downloader Mod").setDetail("Info",
			new ICrashReportDetail<String>() {
				public String call() {
					return WDL.getDebugInfo();
				}
			});
	}

	/**
	 * Start button ID. Ascii-encoded 'WDLs' (World Downloader Start).
	 * Chosen to be unique.
	 */
	private static final int WDLs = 0x57444C73;
	/**
	 * Options button ID. Ascii-encoded 'WDLo' (World Downloader Options).
	 * Chosen to be unique.
	 */
	private static final int WDLo = 0x57444C6F;
	
	/**
	 * Adds the "Download this world" button to the ingame pause GUI.
	 * 
	 * @param gui
	 * @param buttonList
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void injectWDLButtons(GuiIngameMenu gui, List buttonList) {
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
	
		// Insert wdl buttons.
		GuiButton wdlDownload = new GuiButton(WDLs, gui.width / 2 - 100,
				insertAtYPos, 170, 20, null);
		
		GuiButton wdlOptions = new GuiButton(WDLo, gui.width / 2 + 71,
				insertAtYPos, 28, 20,
				I18n.format("wdl.gui.ingameMenu.settings"));
		
		if (WDL.minecraft.isIntegratedServerRunning()) {
			wdlDownload.displayString = I18n
					.format("wdl.gui.ingameMenu.downloadStatus.singlePlayer");
			wdlDownload.enabled = false;
		} else if (!WDLPluginChannels.canDownloadAtAll()) {
			if (WDLPluginChannels.canRequestPermissions()) {
				// Allow requesting permissions.
				wdlDownload.displayString = I18n
						.format("wdl.gui.ingameMenu.downloadStatus.request");
			} else {
				// Out of date plugin :/
				wdlDownload.displayString = I18n
						.format("wdl.gui.ingameMenu.downloadStatus.disabled");
				wdlDownload.enabled = false;
			}
		} else if (WDL.saving) {
			wdlDownload.displayString = I18n
					.format("wdl.gui.ingameMenu.downloadStatus.saving");
			wdlDownload.enabled = false;
			wdlOptions.enabled = false;
		} else if (WDL.downloading) {
			wdlDownload.displayString = I18n
					.format("wdl.gui.ingameMenu.downloadStatus.stop");
		} else {
			wdlDownload.displayString = I18n
					.format("wdl.gui.ingameMenu.downloadStatus.start");
		}
		buttonList.add(wdlDownload);
		buttonList.add(wdlOptions);
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
	
		if (button.id == WDLs) { // "Start/Stop Download"
			if (WDL.minecraft.isIntegratedServerRunning()) {
				return; // WDL not available if in singleplayer or LAN server mode
			}
			
			if (WDL.downloading) {
				WDL.stopDownload();
			} else {
				if (!WDLPluginChannels.canDownloadAtAll()) {
					// If they don't have any permissions, let the player
					// request some.
					if (WDLPluginChannels.canRequestPermissions()) {
						WDL.minecraft.displayGuiScreen(new GuiWDLPermissions(gui));
					} else {
						button.enabled = false;
					}
					
					return;
				} else if (WDLPluginChannels.hasChunkOverrides()
						&& !WDLPluginChannels.canDownloadInGeneral()) {
					// Handle the "only has chunk overrides" state - notify
					// the player of limited areas.
					WDL.minecraft.displayGuiScreen(new GuiWDLChunkOverrides(gui));
				} else {
					WDL.startDownload();
				}
			}
		} else if (button.id == WDLo) { // "..." (options)
			if (WDL.minecraft.isIntegratedServerRunning()) {
				WDL.minecraft.displayGuiScreen(new GuiWDLAbout(gui));
			} else {
				WDL.minecraft.displayGuiScreen(new GuiWDL(gui));
			}
		} else if (button.id == 1) { // "Disconnect"
			WDL.stopDownload();
		}
	}
}
