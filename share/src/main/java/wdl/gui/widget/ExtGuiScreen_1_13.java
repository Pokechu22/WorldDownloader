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

	@Override
	public void initGui() {
		this.listList.clear();
		this.textFieldList.clear();
		super.initGui();
	}

	@Override
	public final <T extends GuiButton> T addButton(T buttonIn) {
		return super.addButton(buttonIn);
	}

	@Override
	public final <T extends GuiList<E>, E extends GuiListEntry<E>> T addList(T list) {
		this.listList.add(list);
		this.eventListeners.add(list);
		return list;
	}

	@Override
	public final <T extends GuiTextField> T addTextField(T field) {
		this.textFieldList.add(field);
		this.eventListeners.add(field);
		return field;
	}

	@Override
	public final boolean allowCloseWithEscape() {
		return this.onCloseAttempt();
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void updateScreen() {
		super.updateScreen();
		for (GuiList<?> list : this.listList) {
			list.updateScreen();
		}
		for (GuiTextField field : this.textFieldList) {
			field.updateCursorCounter();
		}
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		for (GuiList<?> list : this.listList) {
			list.drawScreen(mouseX, mouseY, partialTicks);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		for (GuiTextField field : this.textFieldList) {
			field.drawTextField(mouseX, mouseY, partialTicks);
		}
	}
}
