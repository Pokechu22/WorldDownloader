package com.uyjulian.LiteModWDL.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.util.text.ITextComponent;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient implements INetHandlerPlayClient {
	@Inject(method="handleDisconnect", at=@At("HEAD"))
	private void onHandleDisconnect(SPacketDisconnect p_147253_1_, CallbackInfo ci) {
		/* WDL >>> */
		if (wdl.WDL.downloading) {
			wdl.WDL.stopDownload();

			try {
				Thread.sleep(2000L);
			} catch (Exception var3) {
				;
			}
		}

		/* <<< WDL */
        //more down here
	}
	@Inject(method="onDisconnect", at=@At("HEAD"))
	private void onOnDisconnect(ITextComponent p_147231_1_, CallbackInfo ci) {
		/* WDL >>> */
		if (wdl.WDL.downloading) {
			wdl.WDL.stopDownload();

			try {
				Thread.sleep(2000L);
			} catch (Exception var3) {
				;
			}
		}

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
