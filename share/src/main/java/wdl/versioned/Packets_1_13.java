/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.versioned;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.ResourceLocation;

/**
 * Contains functions related to packets. This version is used in Minecraft 1.13
 * and newer.
 */
final class PacketFunctions {
	private PacketFunctions() { throw new AssertionError(); }

	/* (non-javadoc)
	 * {@see VersionedFunctions#makePluginMessagePacket
	 */
	static CPacketCustomPayload makePluginMessagePacket(String channel, byte[] bytes) {
		return new CPacketCustomPayload(new ResourceLocation(channel), new PacketBuffer(Unpooled.copiedBuffer(bytes)));
	}
}
