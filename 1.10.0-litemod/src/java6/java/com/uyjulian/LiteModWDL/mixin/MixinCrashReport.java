/*
 * This file is part of LiteModWDL.  LiteModWDL contains the liteloader-specific
 * code for World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package com.uyjulian.LiteModWDL.mixin;

import net.minecraft.crash.CrashReport;
import wdl.ducks.IBaseChangesApplied;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReport.class)
public abstract class MixinCrashReport implements IBaseChangesApplied {
	@Inject(method="populateEnvironment", at=@At("RETURN"))
	private void onCrashReportPopulateEnvironment(CallbackInfo ci) {
		try {
			wdl.WDLHooks.onCrashReportPopulateEnvironment((CrashReport)(Object)this);
		} catch (Throwable t) {
			try {
				final Logger LOGGER = LogManager.getLogger();
				LOGGER.fatal("World Downloader: Failed to add crash info", t);
				((CrashReport)(Object)this).getCategory().addCrashSectionThrowable("World Downloader - Fatal error in crash handler (see log)", t);
			} catch (Throwable t2) {
				System.err.println("WDL: Double failure adding info to crash report!");
				t.printStackTrace();
				t2.printStackTrace();
			}
		}
	}
}
