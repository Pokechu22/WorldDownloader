/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui.widget;

import net.minecraft.client.Minecraft;
import wdl.gui.Utils;

/**
 * {@link GuiListEntry} that displays a single line of text.
 */
public class TextEntry extends GuiListEntry {
	private final String text;
	private final int color;
	protected final Minecraft mc;

	/**
	 * Creates a new TextEntry with the default color.
	 */
	public TextEntry(Minecraft mc, String text) {
		this(mc, text, 0xFFFFFF);
	}

	/**
	 * Creates a new TextEntry.
	 */
	public TextEntry(Minecraft mc, String text, int color) {
		this.mc = mc;
		this.text = text;
		this.color = color;
	}

	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth,
			int slotHeight, int mouseX, int mouseY, boolean isSelected) {
		if (y < 0) {
			return;
		}
		Utils.drawStringWithShadow(text, x, y + 1, color);
	}

	@Override
	public boolean mousePressed(int slotIndex, int x, int y, int mouseEvent,
			int relativeX, int relativeY) {
		return false;
	}

	@Override
	public void mouseReleased(int slotIndex, int x, int y, int mouseEvent,
			int relativeX, int relativeY) {

	}

	@Override
	public void setSelected(int slotIndex, int p_178011_2_, int p_178011_3_) {

	}
}