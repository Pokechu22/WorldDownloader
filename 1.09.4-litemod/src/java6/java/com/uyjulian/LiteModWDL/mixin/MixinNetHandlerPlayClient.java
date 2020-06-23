/*
 * This file is part of LiteModWDL.  LiteModWDL contains the liteloader-specific
 * code for World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2020 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package com.uyjulian.LiteModWDL.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;
import com.uyjulian.LiteModWDL.PassCustomPayloadHandler;

import io.netty.channel.Channel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.util.text.ITextComponent;
import wdl.ReflectionUtils;
import wdl.ducks.IBaseChangesApplied;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient implements INetHandlerPlayClient, IBaseChangesApplied {
	@Inject(method="<init>", at=@At("RETURN"))
	private void init(Minecraft mcIn, GuiScreen p_i46300_2_, NetworkManager networkManagerIn, GameProfile profileIn, CallbackInfo ci) {
		if (networkManagerIn == null) return; // Happens during unit tests

		// Litemod-only: work around forge issue
		Channel channel = ReflectionUtils.findAndGetPrivateField(networkManagerIn, NetworkManager.class, Channel.class);
		if (channel.pipeline().names().contains("wdl:packet_handler")) {
			// Already registered, do nothing.
			// Shoudln't happen in normal cases, but some mods do strange stuff like
			// creating/allowing creation of multiple NetHandlerPlayClient instances.
		} else if (channel.pipeline().names().contains("fml:packet_handler")) {
			channel.pipeline().addBefore("fml:packet_handler", "wdl:packet_handler",
					new PassCustomPayloadHandler(mcIn, (NetHandlerPlayClient)(Object)this, true));
		} else {
			channel.pipeline().addBefore("packet_handler", "wdl:packet_handler",
					new PassCustomPayloadHandler(mcIn, (NetHandlerPlayClient)(Object)this, false));
		}
	}

	// Automatic remapping sometimes fails; see
	// https://github.com/Pokechu22/WorldDownloader/issues/175
	@Shadow(remap = false, aliases = { "g", "field_147300_g" })
	private WorldClient world;

	@Inject(method="processChunkUnload", at=@At("HEAD"))
	private void onProcessChunkUnload(SPacketUnloadChunk packetIn, CallbackInfo ci) {
		/* WDL >>> */
		wdl.WDLHooks.onNHPCHandleChunkUnload((NetHandlerPlayClient)(Object)this, this.world, packetIn);
		/* <<< WDL */
		//more down here
	}
	@Inject(method="onDisconnect", at=@At("HEAD"))
	private void onDisconnect(ITextComponent reason, CallbackInfo ci) {
		/* WDL >>> */
		wdl.WDLHooks.onNHPCDisconnect((NetHandlerPlayClient)(Object)this, reason);
		/* <<< WDL */
		//more down here
	}
	@Inject(method="handleChat", at=@At("RETURN"))
	private void onHandleChat(SPacketChat p_147251_1_, CallbackInfo ci) {
		//more up here
		/* WDL >>> */
		wdl.WDLHooks.onNHPCHandleChat((NetHandlerPlayClient)(Object)this, p_147251_1_);
		/* <<< WDL */
	}
	@Inject(method="handleBlockAction", at=@At("RETURN"))
	private void onHandleBlockAction(SPacketBlockAction packetIn, CallbackInfo ci) {
		//more up here
		/* WDL >>> */
		wdl.WDLHooks.onNHPCHandleBlockAction((NetHandlerPlayClient)(Object)this, packetIn);
		/* <<< WDL */
	}
	@Inject(method="handleMaps", at=@At("RETURN"))
	private void onHandleMaps(SPacketMaps packetIn, CallbackInfo ci) {
		//more up here
		/* WDL >>> */
		wdl.WDLHooks.onNHPCHandleMaps((NetHandlerPlayClient)(Object)this, packetIn);
		/* <<< WDL */
	}
	@Inject(method="handleCustomPayload", at=@At("RETURN"))
	private void onHandleCustomPayload(SPacketCustomPayload packetIn, CallbackInfo ci) {
		//more up here
		/* WDL >>> */
		wdl.WDLHooks.onNHPCHandleCustomPayload((NetHandlerPlayClient)(Object)this, packetIn);
		/* <<< WDL */
	}
}
