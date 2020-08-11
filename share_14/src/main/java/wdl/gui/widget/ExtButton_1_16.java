/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
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
package wdl.gui.widget;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

/**
 * Extendible button, to deal with changing method signatures between versions.
 * The actual implementation is {@link WDLButton}, and methods are declared in {@link IExtButton}.
 */
abstract class ExtButton extends AbstractButton implements IExtButton {
	/**
	 * @deprecated Do not use; use {@link #setMessage} instead.
	 */
	@Deprecated
	protected static final Void message = null;
	/**
	 * @deprecated Do not use; use {@link #setEnabled} instead.
	 */
	@Deprecated
	protected static final Void active = null;
	@Nullable
	private MatrixStack matrixStack = null;

	public ExtButton(int x, int y, int widthIn, int heightIn, ITextComponent buttonText) {
		super(x, y, widthIn, heightIn, buttonText);
	}

	@Override
	public void onPress() {
		this.performAction();
	}

	// Renamed
	public abstract void performAction();

	@Override
	public boolean mouseClicked(double arg0, double arg1, int arg2) {
		boolean result = super.mouseClicked(arg0, arg1, arg2);
		if (result && arg2 == 0) {
			this.mouseDown((int)arg0, (int)arg1);
		}
		return result;
	}

	@Override
	public boolean mouseReleased(double arg0, double arg1, int arg2) {
		boolean result = super.mouseReleased(arg0, arg1, arg2);
		if (arg2 == 0) {
			this.mouseUp((int)arg0, (int)arg1);
		}
		return result;
	}

	@Override
	public final boolean mouseDragged(double arg0, double arg1, int arg2, double arg3, double arg4) {
		boolean result = super.mouseDragged(arg0, arg1, arg2, arg3, arg4);
		if (arg2 == 0) {
			this.mouseDragged((int)arg0, (int)arg1);
		}
		return result;
	}

	@Override
	public final void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.matrixStack = matrixStack;
		this.beforeDraw();
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.afterDraw();
		this.matrixStack = null;
	}

	@Override
	protected final void renderBg(MatrixStack matrixStack, Minecraft mc, int mouseX, int mouseY) {
		super.renderBg(matrixStack, mc, mouseX, mouseY);
		this.midDraw();
	}

	@Override
	public void setMessage(ITextComponent message) {
		super.setMessage(message);
	}

	@Override
	protected final IFormattableTextComponent getNarrationMessage() {
		ITextComponent component = this.getNarratorMessage();
		if (component instanceof IFormattableTextComponent) {
			return (IFormattableTextComponent)component;
		} else {
			// XXX This is kinda hacky... is there a better solution?
			return new StringTextComponent("").appendSibling(component);
		}
	}

	@Override
	public ITextComponent getNarratorMessage() {
		return super.getNarrationMessage();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.active = enabled;
	}

	@Override
	public boolean isEnabled() {
		return super.active;
	}

	public void hLine(int startX, int endX, int y, int color) {
		super.hLine(matrixStack, startX, endX, y, color);
	}
	public void vLine(int x, int startY, int endY, int color) {
		super.vLine(matrixStack, x, startY, endY, color);
	}
	public void fill(int left, int top, int right, int bottom, int color) {
		super.fill(matrixStack, left, top, right, bottom, color);
	}
	public void drawCenteredString(FontRenderer font, String str, int x, int y, int color) {
		super.drawCenteredString(matrixStack, font, new StringTextComponent(str), x, y, color);
	}
	public void drawRightAlignedString(FontRenderer font, String str, int x, int y, int color) {
		super.drawRightAlignedString(matrixStack, font, str, x, y, color);
	}
	public void drawString(FontRenderer font, String str, int x, int y, int color) {
		super.drawString(matrixStack, font, new StringTextComponent(str), x, y, color);
	}
	public void blit(int x, int y, int textureX, int textureY, int width, int height) {
		super.blit(matrixStack, x, y, textureX, textureY, width, height);
	}
}
