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

import net.minecraft.client.gui.FontRenderer;
import wdl.gui.widget.GuiList.GuiListEntry;

/**
 * {@link GuiListEntry} that displays a single line of text.
 */
public class TextEntry extends GuiListEntry<TextEntry> {
	private final String text;
	private final int color;
	protected final WDLScreen screen;
	protected final FontRenderer font;

	/**
	 * Creates a new TextEntry with the default color.
	 */
	public TextEntry(WDLScreen screen, FontRenderer font, String text) {
		this(screen, font, text, 0xFFFFFF);
	}

	/**
	 * Creates a new TextEntry.
	 */
	public TextEntry(WDLScreen screen, FontRenderer font, String text, int color) {
		this.screen = screen;
		this.font = font;
		this.text = text;
		this.color = color;
	}

	@Override
	public void drawEntry(int x, int y, int width, int height, int mouseX, int mouseY) {
		if (y < 0) {
			return;
		}
		screen.drawString(font, text, x, y + 1, color);
	}
}
