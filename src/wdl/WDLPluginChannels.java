package wdl;

import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.world.chunk.Chunk;

/**
 * Plugin channel handler.
 * 
 * This is used in combination with the "WDL Serverside Companion" plugin
 * to control which functions of WDL are enabled, without using crappy
 * color codes in the MOTD.
 * 
 * <hr/>
 * <h1>Current packets</h1>
 * <table>
 * <tr>
 * <th>Channel</th>
 * <th>Direction</th>
 * <th>Purpose</th>
 * <th>Contents</th>
 * </tr>
 * <tr>
 * <td><code>WDL|INIT</code></td>
 * <td>Client&nbsp;&#8594;&nbsp;Server</td>
 * <td>Tells the server that the client runs WDL and that it should send
 * WDL-specific settings. Server should respond with a
 * <code>WDL|CONTROL</code> packet. Until a response, assume the most
 * permissive options.</td>
 * <td>There is no payload -- should be 0 bytes.</td>
 * </tr>
 * <tr>
 * <td><code>WDL|CONTROL</code></td>
 * <td>Server&nbsp;&#8594;&nbsp;Client</td>
 * <td>Updates the client's settings.</td>
 * <td>
 * The following sequence (in {@link ByteArrayDataOutput} format):
 * <ol>
 * <li>an <code>int</code>: The version of the packet -- should be
 * <code>1</code> for now. If it's higher, tell the player to update and
 * disable all features.</li>
 * <li>a <code>boolean</code>: If false, all of WDL should be disabled.</li>
 * <li>an <code>int</code>: The "save distance".</li>
 * <li>a <code>boolean</code>: Whether or not the client can cache chunks as
 * they move. If false, only the nearby chunks can be saved when download
 * stops.</li>
 * <li>a <code>boolean</code>: Whether or not WDL can save entities.</li>
 * <li>a <code>boolean</code>: Whether or not WDL can save tile entities in
 * general -- both noncontainers such as signs and containers such as
 * chests.</li>
 * <li>a <code>boolean</code>: Whether or not WDL can save containers. The
 * previous value must be true as well.
 * </ol>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Pokechu22
 */
public class WDLPluginChannels {
	private static Logger logger = LogManager.getLogger();
	
	/**
	 * Packets that have been received.
	 */
	private static HashSet<Integer> receivedPackets;
	
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
	
	static void onPluginChannelPacket(String channel,
			S3FPacketCustomPayload packet) {
		if ("WDL|CONTROL".equals(channel)) {
			ByteArrayDataInput input = ByteStreams.newDataInput(packet
					.getBufferData().array());

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
			default:
				byte[] data = packet.getBufferData().array();

				StringBuilder messageBuilder = new StringBuilder();
				for (byte b : data) {
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
