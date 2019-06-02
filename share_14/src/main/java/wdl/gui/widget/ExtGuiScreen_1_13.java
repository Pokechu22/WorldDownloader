/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui.widget;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import wdl.gui.widget.GuiList.GuiListEntry;

abstract class ExtGuiScreen extends Screen implements IExtGuiScreen {

	/**
	 * Hide the buttonList field from subclasses.
	 * @deprecated Do not use; use {@link #addButton(GuiButton)} instead.
	 */
	@Deprecated
	protected static final Void buttonList = null;

	private final List<GuiList<?>> listList = new ArrayList<>();
	private final List<GuiTextField> textFieldList = new ArrayList<>();

	// Called before initGui
	@Override
	public final void setWorldAndResolution(Minecraft mc, int width, int height) {
		this.listList.clear();
		this.textFieldList.clear();
		super.setWorldAndResolution(mc, width, height);
	}

	@Override
	public final <T extends GuiButton> T addButton(T buttonIn) {
		return super.addButton(buttonIn);
	}

	@Override
	public final <T extends GuiList<E>, E extends GuiListEntry<E>> T addList(T list) {
		this.listList.add(list);
		this.children.add(list);
		return list;
	}

	@Override
	public final <T extends GuiTextField> T addTextField(T field) {
		this.textFieldList.add(field);
		this.children.add(field);
		return field;
	}

	@Override
	public final boolean allowCloseWithEscape() {
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
		for (GuiTextField field : this.textFieldList) {
			field.tick();
		}
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void render(int mouseX, int mouseY, float partialTicks) {
		for (GuiList<?> list : this.listList) {
			list.drawScreen(mouseX, mouseY, partialTicks);
		}
		super.render(mouseX, mouseY, partialTicks);
		for (GuiTextField field : this.textFieldList) {
			field.drawTextField(mouseX, mouseY, partialTicks);
		}
	}
}
