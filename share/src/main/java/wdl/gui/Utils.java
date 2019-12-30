/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import wdl.versioned.VersionedFunctions;

public class Utils {
	private static final Minecraft mc = Minecraft.getInstance();

	/**
	 * Draws a semitransparent description box.
	 *
	 * @param text
	 *            Text to display. Takes \n into consideration.
	 * @param guiWidth
	 *            Width of the GUI.
	 * @param guiHeight
	 *            Height of the GUI.
	 * @param bottomPadding
	 *            The amount of space to put below the bottom of the info box.
	 */
	public static void drawGuiInfoBox(String text, int guiWidth, int guiHeight,
			int bottomPadding) {
		drawGuiInfoBox(text, 300, 100, guiWidth, guiHeight, bottomPadding);
	}

	/**
	 * Draws a semitransparent description box.
	 *
	 * @param text
	 *            Text to display. Takes \n into consideration.
	 * @param infoBoxWidth
	 *            The width of the info box.
	 * @param infoBoxHeight
	 *            The height of the info box.
	 * @param guiWidth
	 *            Width of the GUI.
	 * @param guiHeight
	 *            Height of the GUI.
	 * @param bottomPadding
	 *            The amount of space to put below the bottom of the info box.
	 */
	public static void drawGuiInfoBox(String text, int infoBoxWidth,
			int infoBoxHeight, int guiWidth, int guiHeight, int bottomPadding) {
		if (text == null) {
			return;
		}

		int infoX = guiWidth / 2 - infoBoxWidth / 2;
		int infoY = guiHeight - bottomPadding - infoBoxHeight;
		int y = infoY + 5;

		Gui.fill(infoX, infoY, infoX + infoBoxWidth, infoY
				+ infoBoxHeight, 0x7F000000);

		List<String> lines = wordWrap(text, infoBoxWidth - 10);

		for (String s : lines) {
			mc.fontRenderer.drawString(s, infoX + 5, y, 0xFFFFFF);
			y += mc.fontRenderer.FONT_HEIGHT;
		}
	}

	/**
	 * Converts a string into a list of lines that are each shorter than the
	 * given width.  Takes \n into consideration.
	 *
	 * @param s The string to word wrap.
	 * @param width The width to use.
	 * @return A list of lines.
	 */
	public static List<String> wordWrap(String s, int width) {
		s = s.replace("\\n", "\n");

		List<String> lines = mc.fontRenderer.listFormattedStringToWidth(s, width);

		return lines;
	}

	/**
	 * Draws the background/border used by list GUIs.
	 * <br/>
	 * Based off of
	 * {@link net.minecraft.client.gui.GuiSlot#drawScreen(int, int, float)}.
	 *
	 * Note that there is an additional 4-pixel padding on the margins for the gradient.
	 *
	 * @param topMargin Amount of space to give for the upper box.
	 * @param bottomMargin Amount of space to give for the lower box.
	 * @param top Where to start drawing (usually, 0)
	 * @param left Where to start drawing (usually, 0)
	 * @param bottom Where to stop drawing (usually, height).
	 * @param right Where to stop drawing (usually, width)
	 */
	public static void drawListBackground(int topMargin, int bottomMargin, int top, int left, int bottom, int right) {
		VersionedFunctions.drawDarkBackground(top, left, bottom, right);
		drawBorder(topMargin, bottomMargin, top, left, bottom, right);
	}

	/**
	 * Draws the top and bottom borders found on gui lists (but no background).
	 * <br/>
	 * Based off of
	 * {@link net.minecraft.client.gui.GuiSlot#overlayBackground(int, int, int, int)}.
	 *
	 * Note that there is an additional 4-pixel padding on the margins for the gradient.
	 *
	 * @param topMargin Amount of space to give for the upper box.
	 * @param bottomMargin Amount of space to give for the lower box.
	 * @param top Where to start drawing (usually, 0)
	 * @param left Where to start drawing (usually, 0)
	 * @param bottom Where to stop drawing (usually, height).
	 * @param right Where to stop drawing (usually, width)
	 */
	public static void drawBorder(int topMargin, int bottomMargin, int top, int left, int bottom, int right) {
		VersionedFunctions.drawBorder(topMargin, bottomMargin, top, left, bottom, right);
	}

	/**
	 * Is the mouse over the given text box?
	 * @param mouseX The mouse's current (scaled) x.
	 * @param mouseY The mouse's current (scaled) y.
	 * @param textBox The text box.
	 * @return Whether the mouse is over the given text box.
	 */
	public static boolean isHoveredTextBox(int mouseX, int mouseY,
			GuiTextField textBox) {
		if (!textBox.getVisible()) {
			return false;
		}

		int scaledX = mouseX - textBox.x;
		int scaledY = mouseY - textBox.y;

		// Standard text box height -- there is no actual getter for the real
		// one.
		final int height = 20;

		return scaledX >= 0 && scaledX < textBox.getAdjustedWidth() && scaledY >= 0
				&& scaledY < height;
	}
}
