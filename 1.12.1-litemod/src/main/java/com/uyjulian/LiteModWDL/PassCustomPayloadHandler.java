/*
 * This file is part of LiteModWDL.  LiteModWDL contains the liteloader-specific
 * code for World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018-2020 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package com.uyjulian.LiteModWDL;

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

	private final Minecraft mc;
	private final NetHandlerPlayClient nhpc;
	/**
	 * If true, on the next packet we will check whether the FML handler is present,
	 * and if not, remove ourselves.
	 *
	 * If we already know the FML handler is present, then this check is not needed.
	 */
	private boolean checkIfRemovalNeeded;

	public PassCustomPayloadHandler(Minecraft mc, NetHandlerPlayClient nhpc, boolean injectedBeforeFML) {
		super(false);
		this.mc = mc;
		this.nhpc = nhpc;
		checkIfRemovalNeeded = !injectedBeforeFML;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Packet<?> msg) throws Exception {
		if (checkIfRemovalNeeded) {
			checkIfRemovalNeeded = false;

			boolean hasFML = ctx.pipeline().names().contains("fml:packet_handler");
			if (!hasFML) {
				// Make sure to always pass the packet on to later handlers
				ctx.fireChannelRead(msg);
				// This handler isn't needed, as forge isn't present and isn't intercepting these packets.
				ctx.pipeline().remove(this);
				return;
			}
		}

		if (msg instanceof SPacketCustomPayload) {
			SPacketCustomPayload packet = (SPacketCustomPayload)msg;
			if (packet.getChannelName().equals("REGISTER") || packet.getChannelName().equals("UNREGISTER")) {
				// Make a copy of the packet contents, because otherwise, it'll be read on multiple threads.
				final PacketBuffer copiedBuffer = new PacketBuffer(packet.getBufferData().copy());
				final SPacketCustomPayload copiedPacket = new SPacketCustomPayload(packet.getChannelName(), copiedBuffer);

				mc.execute(new Runnable() {
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

		// Make sure to always pass the packet on to later handlers
		ctx.fireChannelRead(msg);
	}
}
