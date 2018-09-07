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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import wdl.gui.widget.GuiList.GuiListEntry;

abstract class ExtGuiScreen extends GuiScreen implements IExtGuiScreen {

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
		super.buttons.add(buttonIn);
		return buttonIn;
	}

	@Override
	public final <T extends GuiList<E>, E extends GuiListEntry<E>> T addList(T list) {
		this.listList.add(list);
		return list;
	}

	@Override
	public final <T extends GuiTextField> T addTextField(T field) {
		this.textFieldList.add(field);
		return field;
	}

	@Override
	protected final void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (mouseButton == 0) {
			this.mouseDown(mouseX, mouseY);
		}
		for (GuiList<?> list : this.listList) {
			list.mouseClicked(mouseX, mouseY, mouseButton);
		}
		for (GuiTextField field : this.textFieldList) {
			if (field.getVisible()) {
				field.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
	}

	@Override
	protected final void mouseReleased(int mouseX, int mouseY, int state) {
		if (state == 0) {
			this.mouseUp(mouseX, mouseY);
		}
		for (GuiList<?> list : this.listList) {
			list.mouseReleased(mouseX, mouseY, state);
		}
	}

	@Override
	protected final void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (clickedMouseButton == 0) {
			this.mouseDragged(mouseX, mouseY);
		}
	}

	@Override
	protected final void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			if (!this.onCloseAttempt()) {
				// Don't call super, which would close the GUI
				return;
			}
		}
		super.keyTyped(typedChar, keyCode);
		for (GuiList<?> list : this.listList) {
			list.keyTyped(typedChar, keyCode);
		}
		for (GuiTextField field : this.textFieldList) {
			if (field.getVisible()) {
				field.textboxKeyTyped(typedChar, keyCode);
			}
		}
		if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
			typedChar = '\n'; // instead of \r
		}
		if (typedChar != 0) {
			this.charTyped(typedChar);
		}
	}

	@Override
	public final void handleMouseInput() throws IOException {
		super.handleMouseInput();
		for (GuiList<?> list : this.listList) {
			list.handleMouseInput();
		}
	}

	// Not supported
	@Override
	@Deprecated
	protected final void actionPerformed(GuiButton button) { }

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
			field.drawTextBox();
		}
	}
}
