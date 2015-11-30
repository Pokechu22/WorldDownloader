package wdl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemMap;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S24PacketBlockAction;
import net.minecraft.network.play.server.S34PacketMaps;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.chunk.Chunk;

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
			
			if (sender != WDL.worldClient) {
				profiler.startSection("onWorldLoad");
				WDLEvents.onWorldLoad();
				profiler.endSection();
			} else {
				profiler.startSection("inventoryCheck");
				if (WDL.downloading && WDL.thePlayer != null) {
					if (WDL.thePlayer.openContainer != WDL.windowContainer) {
						if (WDL.thePlayer.openContainer == WDL.thePlayer.inventoryContainer) {
							profiler.startSection("onItemGuiClosed");
							WDLEvents.onItemGuiClosed();
							profiler.endSection();
						} else {
							profiler.startSection("onItemGuiOpened");
							WDLEvents.onItemGuiOpened();
							profiler.endSection();
						}
	
						WDL.windowContainer = WDL.thePlayer.openContainer;
					}
				}
				profiler.endSection();
			}
			
			profiler.endSection();
		} catch (Throwable e) {
			WDL.minecraft.crashed(CrashReport.makeCrashReport(e,
					"WDL mod: exception in onWorldClientTick event"));
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
				
				wdl.WDLEvents.onChunkNoLongerNeeded(c);
				profiler.endSection();
			}
			
			profiler.endSection();
		} catch (Throwable e) {
			WDL.minecraft.crashed(CrashReport.makeCrashReport(e,
					"WDL mod: exception in onWorldDoPreChunk event"));
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
			
			WDLEvents.onRemoveEntityFromWorld(entity);
			profiler.endSection();
		} catch (Throwable e) {
			WDL.minecraft.crashed(CrashReport.makeCrashReport(e,
					"WDL mod: exception in onWorldRemoveEntityFromWorld event"));
		}
	}

	/**
	 * Called when {@link NetHandlerPlayClient#handleChat(S02PacketChat)} is
	 * called.
	 * <br/>
	 * Should be at the end of the method.
	 */
	public static void onNHPCHandleChat(NetHandlerPlayClient sender,
			S02PacketChat packet) {
		try {
			if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
				return;
			}
			
			if (!WDL.downloading) { return; }
			
			profiler.startSection("wdl.onChatMessage");
			
			WDLEvents.onChatMessage(packet.getChatComponent().getUnformattedText());
			
			profiler.endSection();
		} catch (Throwable e) {
			WDL.minecraft.crashed(CrashReport.makeCrashReport(e,
					"WDL mod: exception in onNHPCHandleChat event"));
		}
	}

	/**
	 * Called when {@link NetHandlerPlayClient#handleMaps(S34PacketMaps)} is
	 * called.
	 * <br/>
	 * Should be at the end of the method.
	 */
	public static void onNHPCHandleMaps(NetHandlerPlayClient sender,
			S34PacketMaps packet) {
		try {
			if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
				return;
			}
			
			if (!WDL.downloading) { return; }
			
			profiler.startSection("wdl.onMapDataLoaded");
			
			WDLEvents.onMapDataLoaded(packet.getMapId(),
					ItemMap.loadMapData(packet.getMapId(), WDL.worldClient));
			
			profiler.endSection();
		} catch (Throwable e) {
			WDL.minecraft.crashed(CrashReport.makeCrashReport(e,
					"WDL mod: exception in onNHPCHandleMaps event"));
		}
	}

	/**
	 * Called when
	 * {@link NetHandlerPlayClient#handleCustomPayload(S3FPacketCustomPayload)}
	 * is called.
	 * <br/>
	 * Should be at the end of the method.
	 */
	public static void onNHPCHandleCustomPayload(NetHandlerPlayClient sender,
			S3FPacketCustomPayload packet) {
		try {
			if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
				return;
			}
			
			profiler.startSection("wdl.onPluginChannelPacket");
			
			WDLEvents.onPluginChannelPacket(packet.getChannelName(), packet
					.getBufferData().array());
			
			profiler.endSection();
		} catch (Throwable e) {
			WDL.minecraft.crashed(CrashReport.makeCrashReport(e,
					"WDL mod: exception in onNHPCHandleCustomPayload event"));
		}
	}

	/**
	 * Called when
	 * {@link NetHandlerPlayClient#handleBlockAction(S24PacketBlockAction)} is
	 * called.
	 * <br/>
	 * Should be at the end of the method.
	 */
	public static void onNHPCHandleBlockAction(NetHandlerPlayClient sender,
			S24PacketBlockAction packet) {
		try {
			if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
				return;
			}
			
			if (!WDL.downloading) { return; }
			
			profiler.startSection("wdl.onBlockEvent");
			
			WDLEvents.onBlockEvent(packet.getBlockPosition(), packet.getBlockType(),
					packet.getData1(), packet.getData2());
			
			profiler.endSection();
		} catch (Throwable e) {
			WDL.minecraft.crashed(CrashReport.makeCrashReport(e,
					"WDL mod: exception in onNHPCHandleBlockAction event"));
		}
	}
}
