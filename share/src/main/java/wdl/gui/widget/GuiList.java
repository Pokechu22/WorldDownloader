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

import net.minecraft.client.Minecraft;
import wdl.gui.widget.GuiList.GuiListEntry;

public abstract class GuiList<T extends GuiListEntry<T>> extends ExtGuiList<T> {
	public GuiList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int entryHeight) {
		super(mcIn, widthIn, heightIn, topIn, bottomIn, entryHeight);
	}

	public static class GuiListEntry<T extends GuiListEntry<T>> extends ExtGuiListEntry<T> {
		@Override
		public boolean mouseDown(int mouseX, int mouseY, int mouseButton) {
			return false;
		}

		@Override
		public void mouseUp(int mouseX, int mouseY, int mouseButton) { }

		@Override
		public void drawEntry(int x, int y, int width, int height, int mouseX, int mouseY) { }

		@Override
		public void charTyped(char keyChar) { }

		@Override
		public boolean isSelected() {
			return false;
		}
	}

	//private int verticalOffset;

	@Override
	public void setVerticalOffset(int offset) {
//		this.verticalOffset = offset;
	}

	@Override
	public int getEntryWidth() {
		return 220;
	}

	@Override
	public int getScrollBarX() {
		return (getWidth() / 2) + (getEntryWidth() / 2) + 4;
	}
}
