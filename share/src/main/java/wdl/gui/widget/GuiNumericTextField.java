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
package wdl.gui.widget;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

/**
 * {@link GuiTextField} that only accepts numbers.
 */
public class GuiNumericTextField extends GuiTextField {
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
	public void tick() {
		// Save last safe text.
		try {
			Integer.parseInt("0" + getText());
			lastSafeText = getText();
		} catch (NumberFormatException e) {
			setText(lastSafeText);
		}
		super.tick();
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