/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018-2020 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui.widget;

import net.minecraft.util.text.ITextComponent;

/**
 * A Button class that works across versions.
 */
public abstract class WDLButton extends ExtButton {
	public WDLButton(int x, int y, int widthIn, int heightIn, ITextComponent buttonText) {
		super(x, y, widthIn, heightIn, buttonText);
	}

	/**
	 * Performs the action of this button when it has been clicked.
	 */
	@Override
	public abstract void performAction();

	@Override
	public void beforeDraw() { }

	@Override
	public void midDraw() { }

	@Override
	public void afterDraw() { }

	@Override
	public void mouseDown(int mouseX, int mouseY) { }

	@Override
	public void mouseDragged(int mouseX, int mouseY) { }

	@Override
	public void mouseUp(int mouseX, int mouseY) { }
}
