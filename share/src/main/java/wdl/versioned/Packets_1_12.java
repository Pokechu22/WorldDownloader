/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.versioned;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomPayload;
import wdl.versioned.VersionedFunctions.ChannelName;

/**
 * Contains functions related to packets. This version is used between Minecraft
 * 1.9 and Minecraft 1.12.2.
 */
final class PacketFunctions {
	private PacketFunctions() { throw new AssertionError(); }

	/* (non-javadoc)
	 * @see VersionedFunctions#PLUGIN_CHANNEL_REGEX
	 *
	 * Note: the max length was shorter in some earlier version (before 1.9).
	 */
	static final String CHANNEL_NAME_REGEX = ".{1,20}";

	/* (non-javadoc)
	 * @see VersionedFunctions#makePluginMessagePacket
	 */
	static CPacketCustomPayload makePluginMessagePacket(@ChannelName String channel, byte[] bytes) {
		return new CPacketCustomPayload(channel, new PacketBuffer(Unpooled.copiedBuffer(bytes)));
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#makeServerPluginMessagePacket
	 */
	static SPacketCustomPayload makeServerPluginMessagePacket(@ChannelName String channel, byte[] bytes) {
		return new SPacketCustomPayload(channel, new PacketBuffer(Unpooled.copiedBuffer(bytes)));
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getRegisterChannel
	 */
	@ChannelName
	static String getRegisterChannel() {
		return "REGISTER";
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getUnregisterChannel
	 */
	@ChannelName
	static String getUnregisterChannel() {
		return "UNREGISTER";
	}
}
