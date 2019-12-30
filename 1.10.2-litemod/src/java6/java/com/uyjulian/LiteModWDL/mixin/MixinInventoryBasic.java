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
package com.uyjulian.LiteModWDL.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import wdl.ducks.INetworkNameable;

@Mixin(InventoryBasic.class)
public abstract class MixinInventoryBasic implements INetworkNameable {
	@Inject(method = "<init>(Lnet/minecraft/util/text/ITextComponent;I)V", at = @At("RETURN"))
	private void onConstructed(ITextComponent title, int slotCount, CallbackInfo ci) {
		if (title instanceof TextComponentString) {
			this.networkCustomName = title.getString();
		}
	}

	@Nullable
	private String networkCustomName;

	@Nullable
	@Override
	public String getCustomDisplayName() {
		return networkCustomName;
	}
}
