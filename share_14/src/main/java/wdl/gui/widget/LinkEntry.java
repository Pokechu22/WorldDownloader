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
import wdl.versioned.VersionedFunctions;

/**
 * {@link IGuiListEntry} that displays a single clickable link.
 */
public class LinkEntry extends TextEntry {
	private final String link;
	private final int textWidth;
	private final int linkWidth;

	private int x;

	public LinkEntry(WDLScreen screen, FontRenderer font, String text, String link) {
		super(screen, font, text, 0x5555FF);

		this.link = link;
		this.textWidth = font.getStringWidth(text);
		this.linkWidth = font.getStringWidth(link);
	}

	@Override
	public void drawEntry(int x, int y, int width, int height, int mouseX, int mouseY) {
		if (y < 0) {
			return;
		}

		this.x = x;

		super.drawEntry(x, y, width, height, mouseX, mouseY);

		int relativeX = mouseX - x;
		int relativeY = mouseY - y;
		if (relativeX >= 0 && relativeX <= textWidth &&
				relativeY >= 0 && relativeY <= height) {
			int drawX = mouseX - 2;
			if (drawX + linkWidth + 4 > width + x) {
				drawX = width + x - (4 + linkWidth);
			}
			screen.fill(drawX, mouseY - 2, drawX + linkWidth + 4,
					mouseY + font.FONT_HEIGHT + 2, 0x80000000);

			screen.drawString(font, link, drawX + 2, mouseY, 0xFFFFFF);
		}
	}

	@Override
	public boolean mouseDown(int mouseX, int mouseY, int mouseButton) {
		if (mouseX >= x && mouseX <= x + textWidth) {
			VersionedFunctions.openLink(link);
			return true;
		}
		return false;
	}
}
