package wdl;

import io.netty.buffer.Unpooled;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.world.chunk.Chunk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

/**
 * Plugin channel handler.
 * 
 * This is used in combination with the "WDL Serverside Companion" plugin
 * to control which functions of WDL are enabled, without using crappy
 * color codes in the MOTD.
 * 
 * The structure of the packets is documented on the WDLCompanion source: 
 * https://github.com/Pokechu22/WorldDownloader-Serverside-Companion
 * 
 * @author Pokechu22
 */
public class WDLPluginChannels {
	private static Logger logger = LogManager.getLogger();
	
	/**
	 * Packets that have been received.
	 */
	private static HashSet<Integer> receivedPackets = new HashSet<Integer>();
	
	/**
	 * Whether functions that the server is not aware of can be used.
	 * (Packet #0)
	 */
	private static boolean canUseFunctionsUnknownToServer = true;
	/**
	 * Whether all players are allowed to download the world in general.
	 * If false, they aren't allowed, regardless of the other values below.
	 */
	private static boolean canDownloadInGeneral = true;
	/**
	 * The distance from a player that WDL can save chunks.
	 * 
	 * This is only used when {@link #canCacheChunks} is false. 
	 */
	private static int saveRadius = -1;
	/**
	 * Whether a player can cache chunks as they move.  In essence, this means
	 * that if the value is true, the player can download the entire map while
	 * moving about, but if false, the player will only save the nearby chunks
	 * when they stop download.
	 */
	private static boolean canCacheChunks = true;
	/**
	 * Whether or not a player can save entities in the map.
	 */
	private static boolean canSaveEntities = true;
	/**
	 * Whether or not a player can save TileEntities in general.
	 * <br/>
	 * Chests and other containers also require {@link #canSaveContainers}. 
	 */
	private static boolean canSaveTileEntities = true;
	/**
	 * Whether a player can save containers that require opening to save their
	 * contents, such as chests.  For this value to have meaning, the value of
	 * {@link #canSaveTileEntities} must also be true.  
	 */
	private static boolean canSaveContainers = true;
	/**
	 * Map of entity ranges.
	 * 
	 * Key is the entity string, int is the range.
	 */
	private static Map<String, Integer> entityRanges =
			new HashMap<String, Integer>();
	
	/**
	 * Checks whether players can use functions unknown to the server.
	 */
	public static boolean canUseFunctionsUnknownToServer() {
		if (receivedPackets.contains(0)) {
			return canUseFunctionsUnknownToServer;
		} else {
			return true;
		}
	}
	
	/**
	 * Checks whether players are allowed to download in general.
	 */
	public static boolean canDownloadInGeneral() {
		if (receivedPackets.contains(1)) {
			return canDownloadInGeneral;
		} else {
			return canUseFunctionsUnknownToServer();
		}
	}
	/**
	 * Checks if a chunk is within the saveRadius 
	 * (and chunk caching is disabled).
	 */
	public static boolean canSaveChunk(Chunk chunk) {
		if (!canDownloadInGeneral()) {
			return false;
		}
		
		if (receivedPackets.contains(1)) {
			if (!canCacheChunks && saveRadius >= 0) {
				int distanceX = chunk.xPosition - WDL.thePlayer.chunkCoordX;
				int distanceZ = chunk.zPosition - WDL.thePlayer.chunkCoordZ;
				
				if (Math.abs(distanceX) > saveRadius ||
						Math.abs(distanceZ) > saveRadius) {
					return false;
				}
			}
			
			return true;
		} else {
			return canUseFunctionsUnknownToServer();
		}
	}
	/**
	 * Checks whether entities are allowed to be saved.
	 */
	public static boolean canSaveEntities() {
		if (!canDownloadInGeneral()) {
			return false;
		}
		
		if (receivedPackets.contains(1)) {
			return canSaveEntities;
		} else {
			return canUseFunctionsUnknownToServer();
		}
	}
	/**
	 * Checks whether a player can save tile entities.
	 */
	public static boolean canSaveTileEntities() {
		if (!canDownloadInGeneral()) {
			return false;
		}
		
		if (receivedPackets.contains(1)) {
			return canSaveTileEntities;
		} else {
			return canUseFunctionsUnknownToServer();
		}
	}
	/**
	 * Checks whether containers (such as chests) can be saved.
	 */
	public static boolean canSaveContainers() {
		if (!canDownloadInGeneral()) {
			return false; 
		}
		if (!canSaveTileEntities()) {
			return false;
		}
		if (receivedPackets.contains(1)) {
			return canSaveContainers;
		} else {
			return canUseFunctionsUnknownToServer();
		}
	}
	/**
	 * Checks whether maps (the map item, not the world itself) can be saved.
	 */
	public static boolean canSaveMaps() {
		if (!canDownloadInGeneral()) {
			return false; 
		}
		//TODO: Better value than 'canSaveTileEntities'.
		if (receivedPackets.contains(1)) {
			return canSaveTileEntities;
		} else {
			return canUseFunctionsUnknownToServer();
		}
	}
	
	/**
	 * Gets the server-set range for the given entity.
	 * 
	 * @param entity The entity's name (via {@link EntityUtils#getEntityType}).
	 * @return The entity's range, or -1 if no data was recieved.
	 */
	public static int getEntityRange(String entity) {
		if (!canSaveEntities()) {
			return -1;
		}
		if (receivedPackets.contains(2)) {
			if (entityRanges.containsKey(entity)) {
				return entityRanges.get(entity);
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}
	
	/**
	 * Checks if the server-set entity range is configured.
	 */
	public static boolean hasServerEntityRange() {
		return receivedPackets.contains(2) && entityRanges.size() > 0;
	}
	
	//TODO: Getter for entityRanges.
	
	/**
	 * Event that is called when the world is loaded.
	 * Sets the default values, and then asks the server to give the
	 * correct ones.
	 */
	static void onWorldLoad() {
		Minecraft minecraft = Minecraft.getMinecraft();
		
		receivedPackets = new HashSet<Integer>();
		
		canUseFunctionsUnknownToServer = true;
		
		WDL.chatDebug(WDLDebugMessageCause.PLUGIN_CHANNEL_MESSAGE,
				"Sending plugin channels registration to the server.");
		
		// Register the WDL messages.
		PacketBuffer registerPacketBuffer = new PacketBuffer(Unpooled.buffer());
		// Done like this because "buffer.writeString" doesn't do the propper
		// null-terminated strings.
		registerPacketBuffer.writeBytes(new byte[] {
				'W', 'D', 'L', '|', 'I', 'N', 'I', 'T', '\0',
				'W', 'D', 'L', '|', 'C', 'O', 'N', 'T', 'R', 'O', 'L', '\0' });
		C17PacketCustomPayload registerPacket = new C17PacketCustomPayload(
				"REGISTER", registerPacketBuffer);
		minecraft.getNetHandler().addToSendQueue(registerPacket);

		// Send the init message.
		C17PacketCustomPayload initPacket = new C17PacketCustomPayload(
				"WDL|INIT", new PacketBuffer(Unpooled.EMPTY_BUFFER));
		minecraft.getNetHandler().addToSendQueue(initPacket);
	}
	
	static void onPluginChannelPacket(String channel, byte[] bytes) {
		if ("WDL|CONTROL".equals(channel)) {
			ByteArrayDataInput input = ByteStreams.newDataInput(bytes);

			int section = input.readInt();

			receivedPackets.add(section);
			
			switch (section) {
			case 0:
				canUseFunctionsUnknownToServer = input.readBoolean();
				
				WDL.chatDebug(WDLDebugMessageCause.PLUGIN_CHANNEL_MESSAGE, 
						"Loaded settings packet #0 from the server!");
				
				WDL.chatDebug(WDLDebugMessageCause.PLUGIN_CHANNEL_MESSAGE, 
						"canUseFunctionsUnknownToServer: " +
								canUseFunctionsUnknownToServer);
				
				break;
			case 1: 
				canDownloadInGeneral = input.readBoolean();
				saveRadius = input.readInt();
				canCacheChunks = input.readBoolean();
				canSaveEntities = input.readBoolean();
				canSaveTileEntities = input.readBoolean();
				canSaveContainers = input.readBoolean();

				WDL.chatDebug(WDLDebugMessageCause.PLUGIN_CHANNEL_MESSAGE, 
						"Loaded settings packet #1 from the server!");

				WDL.chatDebug(WDLDebugMessageCause.PLUGIN_CHANNEL_MESSAGE, 
						"canDownloadInGeneral: " + canDownloadInGeneral);
				WDL.chatDebug(WDLDebugMessageCause.PLUGIN_CHANNEL_MESSAGE, 
						"saveRadius: " + saveRadius);
				WDL.chatDebug(WDLDebugMessageCause.PLUGIN_CHANNEL_MESSAGE, 
						"canCacheChunks: " + canCacheChunks);
				WDL.chatDebug(WDLDebugMessageCause.PLUGIN_CHANNEL_MESSAGE, 
						"canSaveEntities: " + canSaveEntities);
				WDL.chatDebug(WDLDebugMessageCause.PLUGIN_CHANNEL_MESSAGE, 
						"canSaveTileEntities: " + canSaveTileEntities);
				WDL.chatDebug(WDLDebugMessageCause.PLUGIN_CHANNEL_MESSAGE, 
						"canSaveContainers: " + canSaveContainers);
				break;
			case 2:
				entityRanges.clear();
				
				int count = input.readInt();
				for (int i = 0; i < count; i++) {
					String name = input.readUTF();
					int range = input.readInt();
					
					entityRanges.put(name, range);
				}
				WDL.chatDebug(WDLDebugMessageCause.PLUGIN_CHANNEL_MESSAGE, 
						"Loaded settings packet #2 from the server!");
				
				WDL.chatDebug(WDLDebugMessageCause.PLUGIN_CHANNEL_MESSAGE, 
						"entityRanges: total of " + entityRanges.size());
				break;
			default:
				StringBuilder messageBuilder = new StringBuilder();
				for (byte b : bytes) {
					messageBuilder.append(b).append(' ');
				}

				WDL.chatDebug(WDLDebugMessageCause.PLUGIN_CHANNEL_MESSAGE,
						"Received unkown plugin channel message #" + 
								section + ".");
				logger.info(messageBuilder.toString());
			}
		}
	}
}
