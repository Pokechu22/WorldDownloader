/*
 * This file is part of WDL Forge.  WDL Forge contains the forge-specific
 * code for World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.forge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import wdl.ducks.IBaseChangesApplied;

@Mixin(ClientWorld.class)
public abstract class MixinWorldClient extends World implements IBaseChangesApplied {

	protected MixinWorldClient() {
		super(null, null, null, null, true);
	}

	@Inject(method="tick", at=@At("RETURN"))
	private void onTick(CallbackInfo ci) {
		//more up here
		/* WDL >>> */
		wdl.WDLHooks.onWorldClientTick((ClientWorld)(Object)this);
		/* <<< WDL */
	}

	@Inject(method="removeEntityFromWorld", at=@At("HEAD"))
	private void onRemoveEntityFromWorld(int p_73028_1_, CallbackInfo ci) {
		/* WDL >>> */
		wdl.WDLHooks.onWorldClientRemoveEntityFromWorld((ClientWorld)(Object)this, p_73028_1_);
		/* <<< WDL */
		//more down here
	}
}
