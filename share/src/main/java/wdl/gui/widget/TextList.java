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

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import wdl.gui.Utils;

/**
 * {@link GuiListExtended} that provides scrollable lines of text, and support
 * for embedding links in it.
 */
public class TextList extends GuiList<TextEntry> {

	/**
	 * Creates a new TextList with no text.
	 */
	public TextList(Minecraft mc, int width, int height, int topMargin,
			int bottomMargin) {
		super(mc, width, height, topMargin, height - bottomMargin,
				mc.fontRenderer.FONT_HEIGHT + 1);
	}

	@Override
	public int getScrollBarX() {
		return getWidth() - 10;
	}

	@Override
	public int getEntryWidth() {
		return getWidth() - 18;
	}

	public void addLine(String text) {
		List<String> lines = Utils.wordWrap(text, getEntryWidth());
		lines.stream()
				.map(line -> new TextEntry(mc, line, 0xFFFFFF))
				.forEach(getEntries()::add);
	}

	public void addBlankLine() {
		getEntries().add(new TextEntry(mc, "", 0xFFFFFF));
	}

	public void addLinkLine(String text, String URL) {
		List<String> lines = Utils.wordWrap(text, getEntryWidth());
		lines.stream()
				.map(line -> new LinkEntry(mc, line, URL))
				.forEach(getEntries()::add);
	}

	public void clearLines() {
		getEntries().clear();
	}
}