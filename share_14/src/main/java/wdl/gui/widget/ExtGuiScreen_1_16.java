/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018-2020 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui.widget;


import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import wdl.gui.widget.GuiList.GuiListEntry;

abstract class ExtGuiScreen extends net.minecraft.client.gui.screen.Screen implements IExtGuiScreen {

	/**
	 * Hide the buttonList field from subclasses.
	 * @deprecated Do not use; use {@link #addButton} instead.
	 */
	@Deprecated
	protected static final Void buttonList = null;

	private final List<GuiList<?>> listList = new ArrayList<>();
	private final List<TextFieldWidget> textFieldList = new ArrayList<>();
	@Nullable
	private MatrixStack matrixStack = null;

	protected ExtGuiScreen(ITextComponent title) {
		super(title);
	}

	// Called before init
	@Override
	public final void init(Minecraft mc, int width, int height) {
		this.listList.clear();
		this.textFieldList.clear();
		super.init(mc, width, height);
	}

	@Override
	public final <T extends Widget> T addButton(T buttonIn) {
		return super.addButton(buttonIn);
	}

	@Override
	public final <T extends GuiList<E>, E extends GuiListEntry<E>> T addList(T list) {
		this.listList.add(list);
		this.children.add(list);
		return list;
	}

	@Override
	public final <T extends TextFieldWidget> T addTextField(T field) {
		this.textFieldList.add(field);
		this.children.add(field);
		return field;
	}

	@Override
	public final boolean shouldCloseOnEsc() {
		return this.onCloseAttempt();
	}

	@Override
	public final boolean mouseClicked(double mouseX, double mouseY, int state) {
		boolean result = super.mouseClicked(mouseX, mouseY, state);
		if (state == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			this.mouseDown((int)mouseX, (int)mouseY);
		}
		return result;
	}

	@Override
	public final boolean mouseReleased(double mouseX, double mouseY, int state) {
		boolean result = super.mouseReleased(mouseX, mouseY, state);
		if (state == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			this.mouseUp((int)mouseX, (int)mouseY);
		}
		return result;
	}

	@Override
	public final boolean mouseDragged(double mouseX, double mouseY, int state, double dx, double dy) {
		boolean result = super.mouseDragged(mouseX, mouseY, state, dx, dy);
		if (state == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			this.mouseDragged((int)mouseX, (int)mouseY);
		}
		return result;
	}

	@Override
	public final boolean keyPressed(int key, int scanCode, int modifiers) {
		boolean result = super.keyPressed(key, scanCode, modifiers);
		if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
			this.charTyped('\n');
		}
		this.anyKeyPressed();
		return result;
	}

	@Override
	public final boolean charTyped(char c, int modifiers) {
		boolean result = super.charTyped(c, modifiers);
		this.charTyped(c);
		this.anyKeyPressed();
		return result;
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void tick() {
		super.tick();
		for (GuiList<?> list : this.listList) {
			list.tick();
		}
		for (TextFieldWidget field : this.textFieldList) {
			field.tick();
		}
	}

	@Override
	public final void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.matrixStack = matrixStack;
		this.render(mouseX, mouseY, partialTicks);
		this.matrixStack = null;
	}

	public void renderBackground() {
		super.renderBackground(matrixStack);
	}

	public void renderBackground(int p_238651_2_) {
		super.renderBackground(matrixStack, p_238651_2_);
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void render(int mouseX, int mouseY, float partialTicks) {
		for (GuiList<?> list : this.listList) {
			list.render(matrixStack, mouseX, mouseY, partialTicks);
		}
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		for (TextFieldWidget field : this.textFieldList) {
			field.render(matrixStack, mouseX, mouseY, partialTicks);
		}
		this.renderTitle(mouseX, mouseY, partialTicks);
	}

	protected void renderTitle(int mouseX, int mouseY, float partialTicks) {
		this.drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 8, 0xFFFFFF);
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
