package com.uyjulian.LiteModWDL.mixin;

import net.minecraft.crash.CrashReport;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReport.class)
public abstract class MixinCrashReport {
	@Inject(method="populateEnvironment", at=@At("RETURN"))
	private void onCrashReportPopulateEnvironment(CallbackInfo ci) {
		wdl.WDLHooks.onCrashReportPopulateEnvironment((CrashReport)(Object)this);
	}
}
