/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.CheckForSigned;
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.world.chunk.Chunk;
import wdl.versioned.VersionedFunctions;
import wdl.versioned.VersionedFunctions.ChannelName;

/**
 * World Downloader permission system implemented with Plugin Channels.
 *
 * This system is used to configure the mod, and disable certain features,
 * at a server's decision.  I've made this system because there already were
 * other (more esoteric) methods of finding the mod, based off of forge and
 * lightloader handshakes.  I think that a system like this, where there are
 * <em>degrees of control</em>, is better than one where the player is
 * indiscriminately kicked.  For instance, this system allows for permission
 * requests, which would be hard to do with another mechanism.
 *
 * This system makes use of plugin channels (hence the class name).  If you
 * haven't read <a href="https://wiki.vg/Plugin_channels">their info</a>,
 * they're a vanilla minecraft packet intended for mods.  But they <em>do</em>
 * need each channel to be REGISTERed before use, so the server does know
 * when the mod is installed.  As such, I actually did a second step and
 * instead send a second packet when the data is ready to be received by the
 * client, since mid-download permission changes can be problematic.
 *
 * Theoretically, these could be implemented with chat-based codes or even
 * MOTD-based codes.  However, I <em>really</em> do not like that system, as
 * it is really rigid.  So I chose this one, which is also highly expandable.
 *
 * This system is also used to fetch a few things from willing servers, mainly
 * entity track distances so that things can be saved correctly.
 *
 * And yes, this is the fabled "backdoor" / "back door"; I don't like that term
 * but people will call it that.  I think it's the best possible system out
 * of the available options (and doing nothing wouldn't work - as I said,
 * there <em>are</em> weird ways of finding mods).
 *
 * <a href="https://wiki.vg/Plugin_channels/World_downloader">Packet
 * documentation is on wiki.vg</a>, if you're interested.
 */
public class WDLPluginChannels {
	private static final Logger LOGGER = LogManager.getLogger();
	/**
	 * Packets that have been received.
	 */
	private static HashSet<Integer> receivedPackets = new HashSet<>();

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
			new HashMap<>();

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
	private static Map<String, Multimap<String, ChunkRange>> chunkOverrides = new HashMap<>();

	/**
	 * Active permission requests.
	 */
	private static Map<String, String> requests = new HashMap<>();

	/**
	 * Permission request fields that take boolean parameters.
	 */
	public static final List<String> BOOLEAN_REQUEST_FIELDS = Arrays.asList(
			"downloadInGeneral", "cacheChunks", "saveEntities",
			"saveTileEntities", "saveContainers", "getEntityRanges");
	/**
	 * Permission request fields that take integer parameters.
	 */
	public static final List<String> INTEGER_REQUEST_FIELDS = Arrays.asList(
			"saveRadius");

	/**
	 * List of new chunk override requests.
	 */
	private static List<ChunkRange> chunkOverrideRequests = new ArrayList<>();

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
	 * Checks whether the player should be able to start download at all: Either
	 * {@link #canDownloadInGeneral()} is true, or the player has some chunks
	 * overridden.
	 */
	public static boolean canDownloadAtAll() {
		if (hasChunkOverrides()) {
			return true;
		} else {
			return canDownloadInGeneral();
		}
	}

	/**
	 * Checks whether players are allowed to download in general (outside of
	 * overridden chunks).
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
				int distanceX = chunk.getPos().x - WDL.INSTANCE.player.chunkCoordX;
				int distanceZ = chunk.getPos().z - WDL.INSTANCE.player.chunkCoordZ;

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
	@CheckForSigned
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
		return new HashMap<>(entityRanges);
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

		return isChunkOverridden(chunk.getPos().x, chunk.getPos().z);
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
	 * Are there any chunk overrides present?
	 */
	public static boolean hasChunkOverrides() {
		if (!receivedPackets.contains(4)) {
			// XXX It's possible that some implementations may not send
			// packet 4, but still send ranges. If so, that may lead to issues.
			// But right now, I'm not checking that.
			return false;
		}
		if (chunkOverrides == null || chunkOverrides.isEmpty()) {
			return false;
		}
		for (Multimap<String, ChunkRange> m : chunkOverrides.values()) {
			if (!m.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets an immutable copy of the {@link #chunkOverrides} map.
	 */
	public static Map<String, Multimap<String, ChunkRange>> getChunkOverrides() {
		Map<String, Multimap<String, ChunkRange>> returned = new
				HashMap<>();

		for (Map.Entry<String, Multimap<String, ChunkRange>> e : chunkOverrides
				.entrySet()) {
			// Create a copy of the given map.
			Multimap<String, ChunkRange> map = ImmutableMultimap.copyOf(e.getValue());

			returned.put(e.getKey(), map);
		}

		return ImmutableMap.copyOf(returned);
	}

	/**
	 * Create a new permission request.
	 * @param key The key for the request.
	 * @param value The wanted value.
	 */
	public static void addRequest(String key, String value) {
		if (!isValidRequest(key, value)) {
			return;
		}

		requests.put(key, value);
	}

	/**
	 * Gets an immutable copy of the current requests.
	 */
	public static Map<String, String> getRequests() {
		return ImmutableMap.copyOf(requests);
	}

	/**
	 * Is the given set of values valid for the given request?
	 *
	 * Handles checking if the key exists and if the value is valid.
	 *
	 * @param key The key for the request.
	 * @param value The wanted value.
	 */
	public static boolean isValidRequest(String key, String value) {
		if (key == null || value == null) {
			return false;
		}

		if (BOOLEAN_REQUEST_FIELDS.contains(key)) {
			return value.equals("true") || value.equals("false");
		} else if (INTEGER_REQUEST_FIELDS.contains(key)) {
			try {
				Integer.parseInt(value);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}

		return false;
	}

	/**
	 * Gets the current list of chunk override requests.
	 */
	public static List<ChunkRange> getChunkOverrideRequests() {
		return ImmutableList.copyOf(chunkOverrideRequests);
	}
	/**
	 * Adds a new chunk override request for the given range.
	 */
	public static void addChunkOverrideRequest(ChunkRange range) {
		chunkOverrideRequests.add(range);
	}

	/**
	 * Sends the current requests to the server.
	 */
	public static void sendRequests() {
		if (requests.isEmpty() && chunkOverrideRequests.isEmpty()) {
			return;
		}

		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		output.writeUTF("REQUEST REASON WILL GO HERE"); //TODO
		output.writeInt(requests.size());
		for (Map.Entry<String, String> request : requests.entrySet()) {
			output.writeUTF(request.getKey());
			output.writeUTF(request.getValue());
		}

		output.writeInt(chunkOverrideRequests.size());
		for (ChunkRange range : chunkOverrideRequests) {
			range.writeToOutput(output);
		}

		ClientPlayNetHandler nhpc = Minecraft.getInstance().getConnection();
		final String channel;
		if (isRegistered(nhpc, REQUEST_CHANNEL_NEW)) {
			channel = REQUEST_CHANNEL_NEW;
		} else if (isRegistered(nhpc, REQUEST_CHANNEL_OLD)) {
			channel = REQUEST_CHANNEL_OLD;
		} else {
			throw new RuntimeException("No request channel has been registered :("); // XXX
		}
		CCustomPayloadPacket requestPacket = VersionedFunctions.makePluginMessagePacket(channel, output.toByteArray());
		nhpc.sendPacket(requestPacket);
	}

	/**
	 * Channels that the server has registered/unregistered.
	 *
	 * A map from a NetworkManager instance to a String, so that data is kept per-server.
	 *
	 * Unfortunately, clearing in onWorldLoad -> newServer doesn't work right, as that happens
	 * after (possibly far after) plugin messages are handled.
	 *
	 * XXX Equally unfortunately, the server never bothers to tell the client what channels it will send on...
	 */
	private static final Map<NetworkManager, Set<@ChannelName String>> REGISTERED_CHANNELS = new WeakHashMap<>();

	/** Channels for the init packet */
	private static final String INIT_CHANNEL_OLD = "WDL|INIT", INIT_CHANNEL_NEW = "wdl:init";
	/** Channels for the control packet */
	private static final String CONTROL_CHANNEL_OLD = "WDL|CONTROL", CONTROL_CHANNEL_NEW = "wdl:control";
	/** Channels for the request packet */
	private static final String REQUEST_CHANNEL_OLD = "WDL|REQUEST", REQUEST_CHANNEL_NEW = "wdl:request";

	/** All known channels */
	private static final List<@ChannelName String> WDL_CHANNELS = VersionedFunctions.removeInvalidChannelNames(
			INIT_CHANNEL_NEW, CONTROL_CHANNEL_NEW, REQUEST_CHANNEL_NEW,
			INIT_CHANNEL_OLD, CONTROL_CHANNEL_OLD, REQUEST_CHANNEL_OLD
			);

	/**
	 * Gets the current set of registered channels for this server.
	 */
	private static Set<@ChannelName String> getRegisteredChannels(ClientPlayNetHandler nhpc) {
		return REGISTERED_CHANNELS.computeIfAbsent(
				nhpc.getNetworkManager(),
				key -> new HashSet<>());
	}

	/**
	 * Checks if the given channel is registered on this server.
	 */
	private static boolean isRegistered(ClientPlayNetHandler nhpc, String channelName) {
		return getRegisteredChannels(nhpc).contains(channelName);
	}

	private static final String UPDATE_NOTE = "For 1.13 compatibility, please update your plugin as channel names have changed.";

	/**
	 * The state for {@link #sendInitPacket(String)} if it was called when no channels were registered.
	 */
	@Nullable
	private static String deferredInitState = null;

	public static void sendInitPacket(String state) {
		sendInitPacket(Minecraft.getInstance().getConnection(), state);
	}
	private static void sendInitPacket(ClientPlayNetHandler nhpc, String state) {
		assert nhpc != null : "Unexpected null nhpc: state=" + state + ", chans=" + REGISTERED_CHANNELS;

		final String channel;
		if (isRegistered(nhpc, INIT_CHANNEL_NEW)) {
			channel = INIT_CHANNEL_NEW;
		} else if (isRegistered(nhpc, INIT_CHANNEL_OLD)) {
			channel = INIT_CHANNEL_OLD;
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[WDL] Deferring init packet for state {} (existing deferred state is {} -- should be null), as no registered channel: {}", state, deferredInitState, REGISTERED_CHANNELS);
			}
			deferredInitState = state;
			return;
		}

		LOGGER.debug("[WDL] Sending init packet for state {} on {}", state, channel);

		JsonObject object = new JsonObject();
		object.addProperty("X-RTFM", "https://wiki.vg/Plugin_channels/World_downloader");
		object.addProperty("X-UpdateNote", UPDATE_NOTE);
		object.addProperty("Version", VersionConstants.getModVersion());
		object.addProperty("State", state);
		byte[] bytes = object.toString().getBytes(StandardCharsets.UTF_8);

		CCustomPayloadPacket initPacket = VersionedFunctions.makePluginMessagePacket(channel, bytes);

		nhpc.sendPacket(initPacket);

		deferredInitState = null;
	}

	/**
	 * Event that is called when the world is loaded.
	 * Sets the default values, and then asks the server to give the
	 * correct ones.
	 */
	static void onWorldLoad() {
		@SuppressWarnings("resource")
		Minecraft minecraft = Minecraft.getInstance();

		receivedPackets = new HashSet<>();
		requests = new HashMap<>();
		chunkOverrideRequests = new ArrayList<>();

		canUseFunctionsUnknownToServer = true;

		WDLMessages.chatMessageTranslated(
				WDL.serverProps,
				WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE, "wdl.messages.permissions.init");

		// Register the WDL messages.
		byte[] registerBytes = String.join("\0", WDL_CHANNELS).getBytes();

		CCustomPayloadPacket registerPacket = VersionedFunctions.makePluginMessagePacket(VersionedFunctions.getRegisterChannel(), registerBytes);
		minecraft.getConnection().sendPacket(registerPacket);

		// Send the init message.
		sendInitPacket("Init?");
	}

	static void onPluginChannelPacket(ClientPlayNetHandler sender, @ChannelName String channel, byte[] bytes) {
		if ("REGISTER".equals(channel) || "minecraft:register".equals(channel)) {
			registerChannels(sender, bytes);
		} else if ("UNREGISTER".equals(channel) || "minecraft:unregister".equals(channel)) {
			unregisterChannels(sender, bytes);
		} else if (CONTROL_CHANNEL_NEW.equals(channel) || CONTROL_CHANNEL_OLD.equals(channel)) {
			handleControlPacket(bytes);
		}
	}

	private static void registerChannels(ClientPlayNetHandler nhpc, byte[] bytes) {
		String existing = LOGGER.isDebugEnabled() ? REGISTERED_CHANNELS.toString() : null;

		String str = new String(bytes, StandardCharsets.UTF_8);

		List<String> channels = Arrays.asList(str.split("\0"));
		channels.stream()
				.filter(WDL_CHANNELS::contains)
				.forEach(getRegisteredChannels(nhpc)::add);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[WDL] REGISTER: " + str + "/" + channels + ": " + existing + " => " + REGISTERED_CHANNELS);
		}

		if (deferredInitState != null) {
			LOGGER.debug("[WDL] REGISTER: Trying to resolve deferred {}", deferredInitState);
			sendInitPacket(nhpc, deferredInitState);
		}
	}

	private static void unregisterChannels(ClientPlayNetHandler nhpc, byte[] bytes) {
		String existing = LOGGER.isDebugEnabled() ? REGISTERED_CHANNELS.toString() : null;

		String str = new String(bytes, StandardCharsets.UTF_8);
		List<String> channels = Arrays.asList(str.split("\0"));
		channels.stream()
				.filter(WDL_CHANNELS::contains)
				.forEach(getRegisteredChannels(nhpc)::remove);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[WDL] UNREGISTER: " + str + "/" + channels + ": " + existing + " => " + REGISTERED_CHANNELS);
		}
	}

	private static void handleControlPacket(byte[] bytes) {
		try {
			ByteArrayDataInput input = ByteStreams.newDataInput(bytes);

			int section = input.readInt();

			receivedPackets.add(section);

			switch (section) {
			case 0:
				canUseFunctionsUnknownToServer = input.readBoolean();

				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE,
						"wdl.messages.permissions.packet0", canUseFunctionsUnknownToServer);

				break;
			case 1:
				canDownloadInGeneral = input.readBoolean();
				saveRadius = input.readInt();
				canCacheChunks = input.readBoolean();
				canSaveEntities = input.readBoolean();
				canSaveTileEntities = input.readBoolean();
				canSaveContainers = input.readBoolean();

				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE,
						"wdl.messages.permissions.packet1", canDownloadInGeneral,
						saveRadius, canCacheChunks,
						canSaveEntities, canSaveTileEntities, canSaveContainers);

				//Cancel a download if it is occurring.
				if (!canDownloadInGeneral) {
					if (WDL.downloading) {
						WDLMessages.chatMessageTranslated(
								WDL.serverProps,
								WDLMessageTypes.ERROR, "wdl.messages.generalError.forbidden");
						WDL.INSTANCE.cancelDownload();
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

				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE,
						"wdl.messages.permissions.packet2", entityRanges.size());
				break;
			case 3:
				canRequestPermissions = input.readBoolean();
				requestMessage = input.readUTF();

				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE,
						"wdl.messages.permissions.packet3", canRequestPermissions,
						requestMessage.length(), Integer.toHexString(requestMessage.hashCode()));
				// Don't include the exact message because it's too long and would be spammy.
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

				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE,
						"wdl.messages.permissions.packet4", numRangeGroups, totalRanges);
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

				if (replaceGroups) {
					WDLMessages.chatMessageTranslated(
							WDL.serverProps,
							WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE,
							"wdl.messages.permissions.packet5.set", numNewGroups, groupToEdit);
				} else {
					WDLMessages.chatMessageTranslated(
							WDL.serverProps,
							WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE,
							"wdl.messages.permissions.packet5.added", numNewGroups, groupToEdit);
				}
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

				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE,
						"wdl.messages.permissions.packet6", oldCount, groupToChangeTagsFor, Arrays.toString(tags));
				break;
			case 7:
				String groupToSetTagFor = input.readUTF();
				String tag = input.readUTF();
				int numNewRanges = input.readInt();

				Collection<ChunkRange> oldRanges = chunkOverrides.get(
						groupToSetTagFor).removeAll(tag);
				int numRangesRemoved = oldRanges.size();

				for (int i = 0; i < numNewRanges; i++) {
					//TODO: Ensure that the range has the right tag.

					chunkOverrides.get(groupToSetTagFor).put(tag,
							ChunkRange.readFromInput(input));
				}

				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE,
						"wdl.messages.permissions.packet7", numRangesRemoved, groupToSetTagFor, tag, numNewRanges);
				break;
			default:
				WDLMessages.chatMessageTranslated(
						WDL.serverProps,
						WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE, "wdl.messages.permissions.unknownPacket", section);
				dump(bytes);
			}
		} catch (Exception ex) {
			WDLMessages.chatMessageTranslated(WDL.serverProps, WDLMessageTypes.PLUGIN_CHANNEL_MESSAGE,
					"wdl.messages.permissions.badPacket", ex);
		}
	}

	private static void dump(byte[] bytes) {
		StringBuilder messageBuilder = new StringBuilder();
		for (byte b : bytes) {
			messageBuilder.append(b).append(' ');
		}

		LOGGER.info(messageBuilder.toString());
	}

	/**
	 * A range of chunks.
	 */
	public static class ChunkRange {
		public ChunkRange(String tag, int x1, int z1, int x2, int z2) {
			this.tag = tag;

			// Ensure that the order is correct
			if (x1 > x2) {
				this.x1 = x2;
				this.x2 = x1;
			} else {
				this.x1 = x1;
				this.x2 = x2;
			}
			if (z1 > z2) {
				this.z1 = z2;
				this.z2 = z1;
			} else {
				this.z1 = z1;
				this.z2 = z2;
			}
		}

		/**
		 * The tag of this chunk range.
		 */
		public final String tag;
		/**
		 * Range of coordinates.  x1 will never be higher than x2, as will z1
		 * with z2.
		 */
		public final int x1, z1, x2, z2;

		/**
		 * Reads and creates a new ChunkRange from the given
		 * {@link ByteArrayDataInput}.
		 */
		public static ChunkRange readFromInput(ByteArrayDataInput input) {
			String tag = input.readUTF();
			int x1 = input.readInt();
			int z1 = input.readInt();
			int x2 = input.readInt();
			int z2 = input.readInt();

			return new ChunkRange(tag, x1, z1, x2, z2);
		}

		/**
		 * Writes this ChunkRange to the given {@link ByteArrayDataOutput}.
		 *
		 * Note that I expect most serverside implementations will ignore the
		 * tag, but it still is included for clarity.  The value in it can be
		 * anything so long as it is not null - an empty string will do.
		 */
		public void writeToOutput(ByteArrayDataOutput output) {
			output.writeUTF(this.tag);

			output.writeInt(this.x1);
			output.writeInt(this.z1);
			output.writeInt(this.x2);
			output.writeInt(this.z2);
		}

		@Override
		public String toString() {
			return "ChunkRange [tag=" + tag + ", x1=" + x1 + ", z1=" + z1
					+ ", x2=" + x2 + ", z2=" + z2 + "]";
		}
	}
}
