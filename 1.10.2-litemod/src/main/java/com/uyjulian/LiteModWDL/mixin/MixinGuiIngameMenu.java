package com.uyjulian.LiteModWDL.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;

@Mixin(GuiIngameMenu.class)
public abstract class MixinGuiIngameMenu extends GuiScreen {

	@Inject(method="initGui", at=@At("RETURN"))
	private void onInitGui(CallbackInfo ci) {
		wdl.WDLHooks.injectWDLButtons((GuiIngameMenu)(Object)this, buttonList);
	}
	@Inject(method="actionPerformed", at=@At("HEAD"))
	private void onActionPerformed(GuiButton guibutton, CallbackInfo ci) {
		wdl.WDLHooks.handleWDLButtonClick((GuiIngameMenu)(Object)this, guibutton);
	}
}
