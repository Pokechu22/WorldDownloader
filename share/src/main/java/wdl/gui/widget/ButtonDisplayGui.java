/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui.widget;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

/**
 * A button that displays another GUI when it is clicked.
 */
public class ButtonDisplayGui extends WDLButton {

	/**
	 * Supplier for the GUI to display.  Allowed to return null.
	 */
	private final Supplier<? extends GuiScreen> screenSupplier;

	/**
	 * Creates a new button that displays the given screen, with "done" as its text.
	 */
	public ButtonDisplayGui(int x, int y, int widthIn, int heightIn, @Nullable GuiScreen screen) {
		this(x, y, widthIn, heightIn, I18n.format("gui.done"), screen);
	}

	/**
	 * Creates a new button that displays the given screen, with "done" as its text.
	 */
	public ButtonDisplayGui(int x, int y, int widthIn, int heightIn, Supplier<? extends GuiScreen> supplier) {
		this(x, y, widthIn, heightIn, I18n.format("gui.done"), supplier);
	}

	/**
	 * Creates a new button that displays the given screen, with the specified text.
	 */
	public ButtonDisplayGui(int x, int y, int widthIn, int heightIn, String buttonText, @Nullable GuiScreen screen) {
		this(x, y, widthIn, heightIn, buttonText, () -> screen);
	}

	/**
	 * Creates a new button that displays the given screen, with the specified text.
	 */
	public ButtonDisplayGui(int x, int y, int widthIn, int heightIn, String buttonText, Supplier<? extends GuiScreen> supplier) {
		super(x, y, widthIn, heightIn, buttonText);
		this.screenSupplier = supplier;
	}

	@Override
	public void performAction() {
		Minecraft.getInstance().displayGuiScreen(this.screenSupplier.get());
	}
}
