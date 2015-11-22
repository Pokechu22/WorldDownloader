package wdl;

import io.netty.buffer.Unpooled;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.world.chunk.Chunk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
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
	 * Whether players can request permissions.
	 * 
	 * With the default implementation, this is always <i>sent</i> as
	 * <code>true</code>.  However, this needs to be sent for it to be useful -
	 * if the plugin does NOT send it, it does not support permission requests.
	 */
	private static boolean canRequestPermissions = false;
	
	/**
	 * Message to display when requesting.  If empty, nothing
	 * is displayed.
	 */
	private static String requestMessage = "";
	
	/**
	 * Chunk overrides. Any chunk within a range is allowed to be downloaded in.
	 */
	private static Map<String, Multimap<String, ChunkRange>> chunkOverrides = new HashMap<String, Multimap<String, ChunkRange>>();
	
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
		if (isChunkOverridden(chunk)) {
			return true;
		}
		
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
	 * Checks whether entities are allowed to be saved in the given chunk.
	 */
	public static boolean canSaveEntities(Chunk chunk) {
		if (isChunkOverridden(chunk)) {
			return true;
		}
		
		return canSaveEntities();
	}
	
	/**
	 * Checks whether entities are allowed to be saved in the given chunk.
	 */
	public static boolean canSaveEntities(int chunkX, int chunkZ) {
		if (isChunkOverridden(chunkX, chunkZ)) {
			return true;
		}
		
		return canSaveEntities();
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
	 * Checks whether a player can save tile entities in the given chunk.
	 */
	public static boolean canSaveTileEntities(Chunk chunk) {
		if (isChunkOverridden(chunk)) {
			return true;
		}
		
		return canSaveTileEntities();
	}
	
	/**
	 * Checks whether a player can save tile entities in the given chunk.
	 */
	public static boolean canSaveTileEntities(int chunkX, int chunkZ) {
		if (isChunkOverridden(chunkX, chunkZ)) {
			return true;
		}
		
		return canSaveTileEntities();
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
	 * Checks whether containers (such as chests) can be saved.
	 */
	public static boolean canSaveContainers(Chunk chunk) {
		if (isChunkOverridden(chunk)) {
			return true;
		}
		
		return canSaveContainers();
	}
	
	/**
	 * Checks whether containers (such as chests) can be saved.
	 */
	public static boolean canSaveContainers(int chunkX, int chunkZ) {
		if (isChunkOverridden(chunkX, chunkZ)) {
			return true;
		}
		
		return canSaveContainers();
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
		if (!canSaveEntities(null)) {
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
	 * Gets the save radius.
	 * 
	 * Note that using {@link #canSaveChunk(Chunk)} is generally better
	 * as it handles most of the radius logic.
	 * 
	 * @return {@link #saveRadius}.
	 */
	public static int getSaveRadius() {
		return saveRadius;
	}
	
	/**
	 * Gets whether chunks can be cached.
	 * 
	 * Note that using {@link #canSaveChunk(Chunk)} is generally better
	 * as it handles most of the radius logic.
	 * 
	 * @return {@link #canCacheChunks}.
	 */
	public static boolean canCacheChunks() {
		return canCacheChunks;
	}
	
	/**
	 * Checks if the server-set entity range is configured.
	 */
	public static boolean hasServerEntityRange() {
		return receivedPackets.contains(2) && entityRanges.size() > 0;
	}
	
	public static Map<String, Integer> getEntityRanges() {
		return new HashMap<String, Integer>(entityRanges);
	}
	
	/**
	 * Gets whether permissions are available.
	 */
	public static boolean hasPermissions() {
		return receivedPackets != null && !receivedPackets.isEmpty();
	}
	
	/**
	 * Gets whether permissions are available.
	 */
	public static boolean canRequestPermissions() {
		return receivedPackets.contains(3) && canRequestPermissions;
	}
	
	/**
	 * Gets the request message.
	 * @return The {@link #requestMessage}.
	 */
	public static String getRequestMessage() {
		if (receivedPackets.contains(3)) {
			return requestMessage;
		} else {
			return null;
		}
	}
	
	/**
	 * Is the given chunk part of a chunk override?
	 */
	public static boolean isChunkOverridden(Chunk chunk) {
		if (chunk == null) {
			return false;
		}
		
		return isChunkOverridden(chunk.xPosition, chunk.zPosition);
	}
	/**
	 * Is the given chunk location part of a chunk override?
	 */
	public static boolean isChunkOverridden(int x, int z) {
		for (Multimap<String, ChunkRange> map : chunkOverrides.values()) {
			for (ChunkRange range : map.values()) {
				if (x >= range.x1 &&
						x <= range.x2 &&
						z >= range.z1 &&
						z <= range.z2) {
					return true;
				}
			}
		}
		
		return false;
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
		
		WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE,
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
		C17PacketCustomPayload initPacket;
		try {
			initPacket = new C17PacketCustomPayload("WDL|INIT",
					new PacketBuffer(Unpooled.copiedBuffer(WDL.VERSION
							.getBytes("UTF-8"))));
		} catch (UnsupportedEncodingException e) {
			WDL.chatError("Your computer doesn't support the UTF-8 charset."
					+ "You should feel bad.  " + (e.toString()));
			e.printStackTrace();

			initPacket = new C17PacketCustomPayload("WDL|INIT",
					new PacketBuffer(Unpooled.buffer()));
		}
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
				
				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE, 
						"Loaded settings packet #0 from the server!");
				
				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE, 
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

				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE, 
						"Loaded settings packet #1 from the server!");

				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE, 
						"canDownloadInGeneral: " + canDownloadInGeneral);
				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE, 
						"saveRadius: " + saveRadius);
				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE, 
						"canCacheChunks: " + canCacheChunks);
				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE, 
						"canSaveEntities: " + canSaveEntities);
				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE, 
						"canSaveTileEntities: " + canSaveTileEntities);
				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE, 
						"canSaveContainers: " + canSaveContainers);
				
				//Cancel a download if it is occurring.
				if (!canDownloadInGeneral) {
					if (WDL.downloading) {
						WDL.chatError("The server forbids downloading this world!");
						WDL.cancelDownload();
					}
				}
				break;
			case 2:
				entityRanges.clear();
				
				int count = input.readInt();
				for (int i = 0; i < count; i++) {
					String name = input.readUTF();
					int range = input.readInt();
					
					entityRanges.put(name, range);
				}
				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE, 
						"Loaded settings packet #2 from the server!");
				
				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE, 
						"entityRanges: total of " + entityRanges.size());
				break;
			case 3: 
				canRequestPermissions = input.readBoolean();
				requestMessage = input.readUTF();
				
				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE, 
						"Loaded settings packet #3 from the server!");
				//Don't bother printing out any exact info.
				//The user will only need this in the perm UI, and
				//it'll be true all of the time as of the current plugin.
				break;
			case 4:
				chunkOverrides.clear();
				
				int numRangeGroups = input.readInt();
				int totalRanges = 0;
				for (int i = 0; i < numRangeGroups; i++) {
					String groupName = input.readUTF();
					int groupSize = input.readInt();
					
					Multimap<String, ChunkRange> ranges = HashMultimap
							.<String, ChunkRange> create();
					
					for (int j = 0; j < groupSize; j++) {
						ChunkRange range = ChunkRange.readFromInput(input);
						ranges.put(range.tag, range);
					}
					
					chunkOverrides.put(groupName, ranges);
					
					totalRanges += groupSize;
				}
				
				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE, 
						"Loaded settings packet #4 from the server!");
				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE, 
						"ChunkRanges: " + numRangeGroups + " groups, "
								+ totalRanges + " in total");
				break;
			case 5:
				
				String groupToEdit = input.readUTF();
				boolean replaceGroups = input.readBoolean();
				int numNewGroups = input.readInt();
				
				Multimap<String, ChunkRange> newRanges = HashMultimap
						.<String, ChunkRange> create();
				if (!replaceGroups) {
					newRanges.putAll(chunkOverrides.get(groupToEdit));
				}
				
				for (int i = 0; i < numNewGroups; i++) {
					ChunkRange range = ChunkRange.readFromInput(input);
					
					newRanges.put(range.tag, range);
				}
				chunkOverrides.put(groupToEdit, newRanges);
				
				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE, 
						"Loaded settings packet #5 from the server!");
				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE,
						"Edited ChunkRanges for " + groupToEdit + ": "
								+ (replaceGroups ? "Now contains " : "Added ")
								+ " " + numNewGroups + " ranges.");
				break;
			case 6:
				String groupToChangeTagsFor = input.readUTF();
				int numTags = input.readInt();
				String[] tags = new String[numTags];
				
				for (int i = 0; i < numTags; i++) {
					tags[i] = input.readUTF();
				}
				
				int oldCount = 0;
				for (String tag : tags) {
					oldCount += chunkOverrides.get(groupToChangeTagsFor)
							.get(tag).size();
					chunkOverrides.get(groupToChangeTagsFor).removeAll(tag);
				}
				
				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE, 
						"Loaded settings packet #6 from the server!");
				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE,
						"Removed " + oldCount + " ChunkRanges from "
								+ groupToChangeTagsFor + " with tags of "
								+ Arrays.toString(tags));
				break;
			case 7:
				String groupToSetTagFor = input.readUTF();
				String tag = input.readUTF();
				int numNewRanges = input.readInt();
				
				chunkOverrides.get(groupToSetTagFor).removeAll(tag);
				
				for (int i = 0; i < numNewRanges; i++) {
					//TODO: Ensure that the range has the right tag.
					
					chunkOverrides.get(groupToSetTagFor).put(tag,
							ChunkRange.readFromInput(input));
				}
				
				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE, 
						"Loaded settings packet #7 from the server!");
				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE,
						"Replaced ChunkRanges for " + groupToSetTagFor
								+ " with tag " + tag + " with " + numNewRanges
								+ " ranges.");
				break;
			default:
				StringBuilder messageBuilder = new StringBuilder();
				for (byte b : bytes) {
					messageBuilder.append(b).append(' ');
				}

				WDL.chatMessage(WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE,
						"Received unkown plugin channel message #" + 
								section + ".");
				logger.info(messageBuilder.toString());
			}
		}
	}
	
	/**
	 * A range of chunks.
	 */
	private static class ChunkRange {
		public ChunkRange(String tag, int x1, int z1, int x2, int z2) {
			this.tag = tag;
			this.x1 = x1;
			this.z1 = z1;
			this.x2 = x2;
			this.z2 = z2;
		}
		
		/**
		 * The tag of this chunk range/
		 */
		public final String tag;
		/**
		 * Range of coordinates.  x1 will never be higher than x2, as will z1
		 * with z2.
		 */
		public final int x1, z1, x2, z2;
		
		public static ChunkRange readFromInput(ByteArrayDataInput input) {
			String tag = input.readUTF();
			int x1 = input.readInt();
			int z1 = input.readInt();
			int x2 = input.readInt();
			int z2 = input.readInt();
			
			return new ChunkRange(tag, x1, z1, x2, z2);
		}

		@Override
		public String toString() {
			return "ChunkRange [tag=" + tag + ", x1=" + x1 + ", z1=" + z1
					+ ", x2=" + x2 + ", z2=" + z2 + "]";
		}
	}
}
