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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import wdl.gui.widget.ExtGuiList.ExtGuiListEntry;

abstract class ExtGuiList<T extends ExtGuiListEntry<T>> extends GuiListExtended implements IExtGuiList<T> {

	static abstract class ExtGuiListEntry<T extends ExtGuiListEntry<T>> implements IExtGuiListEntry<T>, IGuiListEntry {

		@Override
		public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_) { }

		@Override
		public final void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
				boolean isSelected) {
			this.drawEntry(x, y, listWidth, slotHeight, mouseX, mouseY);
		}

		@Override
		public final boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX,
				int relativeY) {
			return this.mouseDown(mouseX, mouseY, mouseEvent);
		}

		@Override
		public final void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			this.mouseUp(x, y, mouseEvent);
		}
	}

	public ExtGuiList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
		super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
	}

	private final List<T> entries = new ArrayList<>();

	@Override
	public List<T> getEntries() {
		return entries;
	}

	@Override
	protected final boolean isSelected(int slotIndex) {
		return entries.get(slotIndex).isSelected();
	}

	@Override
	public final IGuiListEntry getListEntry(int index) {
		return entries.get(index);
	}

	@Override
	protected final int getSize() {
		return entries.size();
	}

	@Override
	public final int getListWidth() {
		return this.getEntryWidth();
	}

	@Override
	public abstract int getScrollBarX();

	@Override
	public void scroll(double by) {
		this.scrollBy((int)by);
	}

	@Override
	public final int getWidth() {
		return this.width;
	}
}
