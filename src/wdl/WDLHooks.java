package wdl;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.item.ItemMap;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S24PacketBlockAction;
import net.minecraft.network.play.server.S34PacketMaps;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

/**
 * The various hooks for WDL. <br/>
 * All of these should be called regardless of any WDL state variables.
 */
public class WDLHooks {
	/**
	 * Called when {@link WorldClient#tick()} is called.
	 * Should be at end of the method.
	 */
	public static void onWorldClientTick(WorldClient sender) {
		if (sender != WDL.worldClient) {
			WDLEvents.onWorldLoad();
		} else {
			if (WDL.thePlayer != null) {
				if (WDL.thePlayer.openContainer != WDL.windowContainer) {
					if (WDL.thePlayer.openContainer == WDL.thePlayer.inventoryContainer) {
						WDLEvents.onItemGuiClosed();
					} else {
						WDLEvents.onItemGuiOpened();
					}

					WDL.windowContainer = WDL.thePlayer.openContainer;
				}
			}
		}
	}

	/**
	 * Called when {@link WorldClient#doPreChunk(int, int, boolean)} is called.
	 * Should be at the start of the method.
	 */
	public static void onWorldClientDoPreChunk(WorldClient sender, int x,
			int z, boolean loading) {
		if (!loading) {
			wdl.WDLEvents.onChunkNoLongerNeeded(WDL.worldClient
					.getChunkFromChunkCoords(x, z));
		}
	}

	/**
	 * Called when {@link WorldClient#removeEntityFromWorld(int)} is called.
	 * Should be at the start of the method.
	 * 
	 * @param eid
	 *            The entity's unique ID.
	 */
	public static void onWorldClientRemoveEntityFromWorld(WorldClient sender,
			int eid) {
		WDLEvents.onRemoveEntityFromWorld(WDL.worldClient.getEntityByID(eid));
	}

	/**
	 * Called when {@link NetHandlerPlayClient#handleChat(S02PacketChat)} is
	 * called.  Should be at the end of the method.
	 */
	public static void onNHPCHandleChat(NetHandlerPlayClient sender,
			S02PacketChat packet) {
		WDLEvents.onChatMessage(packet.func_148915_c().toString());
	}

	/**
	 * Called when {@link NetHandlerPlayClient#handleMaps(S34PacketMaps)} is
	 * called.  Should be at the end of the method.
	 */
	public static void onNHPCHandleMaps(NetHandlerPlayClient sender,
			S34PacketMaps packet) {
		
		WDLEvents.onMapDataLoaded(packet.getMapId(),
				ItemMap.loadMapData(packet.getMapId(), WDL.worldClient));
	}

	/**
	 * Called when
	 * {@link NetHandlerPlayClient#handleCustomPayload(S3FPacketCustomPayload)}
	 * is called.  Should be at the end of the method.
	 */
	public static void onNHPCHandleCustomPayload(NetHandlerPlayClient sender,
			S3FPacketCustomPayload packet) {
		WDLEvents.onPluginChannelPacket(packet.getChannelName(), packet
				.getBufferData().array());
	}

	/**
	 * Called when
	 * {@link NetHandlerPlayClient#handleBlockAction(S24PacketBlockAction)} is
	 * called.  Should be at the end of the method.
	 */
	public static void onNHPCHandleBlockAction(NetHandlerPlayClient sender,
			S24PacketBlockAction packet) {
		WDLEvents.onBlockEvent(packet.func_179825_a(), packet.getBlockType(),
				packet.getData1(), packet.getData2());
	}
}
