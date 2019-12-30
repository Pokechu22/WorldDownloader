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
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;
import wdl.versioned.VersionedFunctions.ChannelName;

/**
 * Contains functions related to packets. This version is used in Minecraft 1.13
 * and newer.
 */
final class PacketFunctions {
	private PacketFunctions() { throw new AssertionError(); }

	/* (non-javadoc)
	 * @see VersionedFunctions#CHANNEL_NAME_REGEX
	 */
	static final String CHANNEL_NAME_REGEX = "([a-z0-9_.-]+:)?[a-z0-9/._-]+";

	/* (non-javadoc)
	 * @see VersionedFunctions#makePluginMessagePacket
	 */
	static CCustomPayloadPacket makePluginMessagePacket(@ChannelName String channel, byte[] bytes) {
		return new CCustomPayloadPacket(new ResourceLocation(channel), new PacketBuffer(Unpooled.copiedBuffer(bytes)));
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#makeServerPluginMessagePacket
	 */
	static SCustomPayloadPlayPacket makeServerPluginMessagePacket(@ChannelName String channel, byte[] bytes) {
		return new SCustomPayloadPlayPacket(new ResourceLocation(channel), new PacketBuffer(Unpooled.copiedBuffer(bytes)));
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getRegisterChannel
	 */
	@ChannelName
	static String getRegisterChannel() {
		return "minecraft:register";
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getUnregisterChannel
	 */
	@ChannelName
	static String getUnregisterChannel() {
		return "minecraft:unregister";
	}
}
