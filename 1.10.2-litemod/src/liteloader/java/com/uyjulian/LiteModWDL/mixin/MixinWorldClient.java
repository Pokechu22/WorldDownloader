package com.uyjulian.LiteModWDL.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

@Mixin(WorldClient.class)
public abstract class MixinWorldClient extends World {

	protected MixinWorldClient(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn,
			Profiler profilerIn, boolean client) {
		super(saveHandlerIn, info, providerIn, profilerIn, client);
		// TODO Auto-generated constructor stub
	}

	@Inject(method="tick", at=@At("RETURN"))
	private void onTick(CallbackInfo ci) {
		//more up here
		/* WDL >>> */
		wdl.WDLHooks.onWorldClientTick((WorldClient)(Object)this);
		/* <<< WDL */
	}
	
	@Inject(method="doPreChunk", at=@At("HEAD"))
	private void onDoPreChunk(int p_73025_1_, int p_73025_2_, boolean p_73025_3_, CallbackInfo ci) {
		/* WDL >>> */
        wdl.WDLHooks.onWorldClientDoPreChunk((WorldClient)(Object)this, p_73025_1_, p_73025_2_, p_73025_3_);
		/* <<< WDL */
        //more down here
	}
	
	@Inject(method="removeEntityFromWorld", at=@At("HEAD"))
	private void onRemoveEntityFromWorld(int p_73028_1_, CallbackInfoReturnable<Entity> ci) {
		/* WDL >>> */
		wdl.WDLHooks.onWorldClientRemoveEntityFromWorld((WorldClient)(Object)this, p_73028_1_);
		/* <<< WDL */
        //more down here
	}
}
