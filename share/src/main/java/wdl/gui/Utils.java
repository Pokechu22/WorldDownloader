/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class Utils {
	private static final Minecraft mc = Minecraft.getMinecraft();
	private static final Logger LOGGER = LogManager.getLogger();

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

		Gui.drawRect(infoX, infoY, infoX + infoBoxWidth, infoY
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
		s = s.replace("\r", ""); // If we got a \r\n in the text somehow, remove it.

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
		LocalUtils.drawDarkBackground(top, left, bottom, right);
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
		LocalUtils.drawBorder(topMargin, bottomMargin, top, left, bottom, right);
	}

	/**
	 * Is the mouse over the given text box?
	 * @param mouseX The mouse's current (scaled) x.
	 * @param mouseY The mouse's current (scaled) y.
	 * @param textBox The text box.
	 * @return Whether the mouse is over the given text box.
	 */
	public static boolean isMouseOverTextBox(int mouseX, int mouseY,
			GuiTextField textBox) {
		int scaledX = mouseX - textBox.x;
		int scaledY = mouseY - textBox.y;

		// Standard text box height -- there is no actual getter for the real
		// one.
		final int height = 20;

		return scaledX >= 0 && scaledX < textBox.getWidth() && scaledY >= 0
				&& scaledY < height;
	}

	/**
	 * Attempts to open a link.
	 * @param path the URL to open.
	 */
	public static void openLink(String path) {
		try {
			Class<?> desktopClass = Class.forName("java.awt.Desktop");
			Object desktop = desktopClass.getMethod("getDesktop").invoke(
					null);
			desktopClass.getMethod("browse", URI.class).invoke(desktop,
					new URI(path));
		} catch (Throwable e) {
			LOGGER.error("Couldn\'t open link", e);
		}
	}

	/**
	 * Draws a string with a shadow.
	 *
	 * Needed because of obfuscation.
	 */
	public static void drawStringWithShadow(String s, int x, int y, int color) {
		//TODO: No longer obfuscated; should I care enough to inline this?
		mc.fontRenderer.drawStringWithShadow(s, x, y, color);
	}
}

/**
 * A slider that doesn't require a bunch of interfaces to work.
 *
 * Based off of {@link net.minecraft.client.gui.GuiOptionSlider}.
 */
class GuiSlider extends GuiButton {
	private float sliderValue;
	private boolean dragging;
	/**
	 * I18n key for this slider.
	 */
	private final String text;
	/**
	 * Maximum value for the slider.
	 */
	private final int max;

	public GuiSlider(int id, int x, int y, int width, int height,
			String text, int value, int max) {
		super(id, x, y, width, height, text);

		this.text = text;
		this.max = max;

		setValue(value);
	}

	/**
	 * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over
	 * this button and 2 if it IS hovering over this button.
	 */
	@Override
	protected int getHoverState(boolean mouseOver) {
		return 0;
	}

	/**
	 * Fired when the mouse button is dragged. Equivalent of
	 * MouseListener.mouseDragged(MouseEvent e).
	 */
	@Override
	protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible) {
			if (this.dragging) {
				this.sliderValue = (float)(mouseX - (this.x + 4))
						/ (float)(this.width - 8);
				this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F,
						1.0F);
				this.dragging = true;

				this.displayString = I18n.format(text, getValue());
			}

			mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			if (this.enabled) {
				this.drawTexturedModalRect(this.x
						+ (int) (this.sliderValue * (this.width - 8)),
						this.y, 0, 66, 4, 20);
				this.drawTexturedModalRect(this.x
						+ (int) (this.sliderValue * (this.width - 8))
						+ 4, this.y, 196, 66, 4, 20);
			} else {
				this.drawTexturedModalRect(this.x
						+ (int) (this.sliderValue * (this.width - 8)),
						this.y, 0, 46, 4, 20);
				this.drawTexturedModalRect(this.x
						+ (int) (this.sliderValue * (this.width - 8))
						+ 4, this.y, 196, 46, 4, 20);
			}
		}
	}

	/**
	 * Returns true if the mouse has been pressed on this control. Equivalent of
	 * MouseListener.mousePressed(MouseEvent e).
	 */
	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			this.sliderValue = (float)(mouseX - (this.x + 4))
					/ (float)(this.width - 8);
			this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F,
					1.0F);
			this.displayString = I18n.format(text, getValue());

			this.dragging = true;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Gets the current value of the slider.
	 * @return
	 */
	public int getValue() {
		return (int)(sliderValue * max);
	}

	/**
	 * Gets the current value of the slider.
	 * @return
	 */
	public void setValue(int value) {
		this.sliderValue = value / (float)max;

		this.displayString = I18n.format(text, getValue());
	}

	/**
	 * Fired when the mouse button is released. Equivalent of
	 * MouseListener.mouseReleased(MouseEvent e).
	 */
	@Override
	public void mouseReleased(int mouseX, int mouseY) {
		this.dragging = false;
	}
}

/**
 * {@link GuiTextField} that only accepts numbers.
 */
class GuiNumericTextField extends GuiTextField {
	public GuiNumericTextField(int id, FontRenderer fontRenderer,
			int x, int y, int width, int height) {
		super(id, fontRenderer, x, y, width,
				height);
		setText("0");
	}

	/**
	 * Last text that was successfully entered.
	 */
	private String lastSafeText = "0";

	@Override
	public void drawTextBox() {
		// Save last safe text.
		try {
			Integer.parseInt("0" + getText());
			lastSafeText = getText();
		} catch (NumberFormatException e) {
			setText(lastSafeText);
		}

		super.drawTextBox();
	}

	/**
	 * Gets the current value.
	 * @return
	 */
	public int getValue() {
		try {
			return Integer.parseInt("0" + getText());
		} catch (NumberFormatException e) {
			// Should not happen, hopefully.
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * Sets the value.
	 * @param value
	 * @return
	 */
	public void setValue(int value) {
		String text = String.valueOf(value);
		lastSafeText = text;
		setText(text);
	}

	@Override
	public String getText() {
		String text = super.getText();

		try {
			int value = Integer.parseInt("0" + text);
			return String.valueOf(value);
		} catch (NumberFormatException e) {
			setText(lastSafeText);
			return lastSafeText;
		}
	}

	@Override
	public void setText(String text) {
		String value;

		try {
			value = String.valueOf(Integer.parseInt("0" + text));
		} catch (NumberFormatException e) {
			value = lastSafeText;
		}

		super.setText(value);
		lastSafeText = value;
	}
}

/**
 * {@link GuiListExtended} that provides scrollable lines of text, and support
 * for embedding links in it.
 */
class TextList extends GuiListExtended {
	public final int topMargin;
	public final int bottomMargin;

	/**
	 * Creates a new TextList with no text.
	 */
	public TextList(Minecraft mc, int width, int height, int topMargin,
			int bottomMargin) {
		super(mc, width, height, topMargin, height - bottomMargin,
				mc.fontRenderer.FONT_HEIGHT + 1);

		this.topMargin = topMargin;
		this.bottomMargin = bottomMargin;

		this.entries = new ArrayList<>();
	}

	private List<IGuiListEntry> entries;

	@Override
	public IGuiListEntry getListEntry(int index) {
		return entries.get(index);
	}

	@Override
	protected int getSize() {
		return entries.size();
	}

	@Override
	protected int getScrollBarX() {
		return width - 10;
	}

	@Override
	public int getListWidth() {
		return width - 18;
	}

	public void addLine(String text) {
		List<String> lines = Utils.wordWrap(text, getListWidth());
		for (String line : lines) {
			entries.add(new TextEntry(mc, line, 0xFFFFFF));
		}
	}

	public void addBlankLine() {
		entries.add(new TextEntry(mc, "", 0xFFFFFF));
	}

	public void addLinkLine(String text, String URL) {
		List<String> lines = Utils.wordWrap(text, getListWidth());
		for (String line : lines) {
			entries.add(new LinkEntry(mc, line, URL));
		}
	}

	public void clearLines() {
		entries.clear();
	}
}

/**
 * {@link GuiListEntry} that displays a single line of text.
 */
class TextEntry extends GuiListEntry {
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

/**
 * {@link IGuiListEntry} that displays a single clickable link.
 */
class LinkEntry extends TextEntry {
	private final String link;
	private final int textWidth;
	private final int linkWidth;

	public LinkEntry(Minecraft mc, String text, String link) {
		super(mc, text, 0x5555FF);

		this.link = link;
		this.textWidth = mc.fontRenderer.getStringWidth(text);
		this.linkWidth = mc.fontRenderer.getStringWidth(link);
	}

	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth,
			int slotHeight, int mouseX, int mouseY, boolean isSelected) {
		if (y < 0) {
			return;
		}

		super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX,
				mouseY, isSelected);

		int relativeX = mouseX - x;
		int relativeY = mouseY - y;
		if (relativeX >= 0 && relativeX <= textWidth &&
				relativeY >= 0 && relativeY <= slotHeight) {
			int drawX = mouseX - 2;
			if (drawX + linkWidth + 4 > listWidth + x) {
				drawX = listWidth + x - (4 + linkWidth);
			}
			Gui.drawRect(drawX, mouseY - 2, drawX + linkWidth + 4,
					mouseY + mc.fontRenderer.FONT_HEIGHT + 2, 0x80000000);

			Utils.drawStringWithShadow(link, drawX + 2, mouseY, 0xFFFFFF);
		}
	}

	@Override
	public boolean mousePressed(int slotIndex, int x, int y, int mouseEvent,
			int relativeX, int relativeY) {
		if (relativeX >= 0 && relativeX <= textWidth) {
			Utils.openLink(link);
			return true;
		}
		return false;
	}
}
