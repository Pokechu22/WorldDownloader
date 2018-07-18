/*
 * This file is part of LiteModWDL.  LiteModWDL contains the liteloader-specific
 * code for World Downloader: A mod to make backups of your multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package com.uyjulian.LiteModWDL.mixin;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldClient.class)
public abstract class MixinWorldClient extends World {

	protected MixinWorldClient(ISaveHandler saveHandlerIn, WorldInfo info, Dimension providerIn,
			Profiler profilerIn, boolean client) {
		super(saveHandlerIn, info, providerIn, profilerIn, client);
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
