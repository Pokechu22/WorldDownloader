/*
 * This file is part of LiteModWDL.  LiteModWDL contains the liteloader-specific
 * code for World Downloader: A mod to make backups of your multiplayer worlds.
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
package com.uyjulian.LiteModWDL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import wdl.WDLHooks;

/**
 * Forge injects a separate packet handler, which doesn't pass some of the
 * packets we need on to the NetHandlerPlayClient instance.
 */
public class PassCustomPayloadHandler extends SimpleChannelInboundHandler<Packet<?>> {

	private static final Logger LOGGER = LogManager.getLogger();
	private final Minecraft mc;
	private final NetHandlerPlayClient nhpc;
	private boolean firstRead = true;

	public PassCustomPayloadHandler(Minecraft mc, NetHandlerPlayClient nhpc, boolean knownBeforeFML) {
		super(false);
		LOGGER.info("[WDL] Created custom payload handler for "+ nhpc + ":" + knownBeforeFML);
		this.mc = mc;
		this.nhpc = nhpc;
		if (knownBeforeFML) {
			firstRead = false;
		}
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("[WDL] handlerAdded: " + ctx.pipeline().names());
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("[WDL] handlerRemoved: " + ctx.pipeline().names());
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Packet<?> msg) throws Exception {
		if (firstRead) {
			firstRead = false;

			LOGGER.info("[WDL] First read: " + msg + " " + ctx.pipeline().names());
			boolean hasFML = ctx.pipeline().names().contains("fml:packet_handler");
			if (!hasFML) {
				LOGGER.info("[WDL] No need for this");
				ctx.fireChannelRead(msg);
				ctx.pipeline().remove(this);
				return;
			}
			LOGGER.info("[WDL] We do have FML: " + ctx.pipeline().get("fml:packet_handler"));
		}

		if (msg instanceof SPacketCustomPayload) {
			SPacketCustomPayload packet = (SPacketCustomPayload)msg;
			LOGGER.info("[WDL EARLY]: " + packet.getChannelName());
			if (packet.getChannelName().equals("REGISTER") || packet.getChannelName().equals("UNREGISTER")) {
				// Make a copy of the packet contents, because otherwise, it'll be read on multiple threads.
				final PacketBuffer copiedBuffer = new PacketBuffer(packet.getBufferData().copy());
				final SPacketCustomPayload copiedPacket = new SPacketCustomPayload(packet.getChannelName(), copiedBuffer);

				mc.enqueue(new Runnable() {
					@Override
					public void run() {
						try {
							WDLHooks.onNHPCHandleCustomPayload(nhpc, copiedPacket);
						} finally {
							copiedBuffer.release();
						}
					}
				});
			}
		}
		ctx.fireChannelRead(msg);
	}
}
