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
import net.minecraft.client.gui.GuiButton;

/**
 * Extendable button, to deal with changing method names between versions
 * @author Pokechu22
 *
 */
public abstract class ExtButton extends GuiButton {
	public ExtButton(int buttonId, int x, int y, int widthIn, int heightIn,
			String buttonText) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
	}

	public abstract void beforeDraw();
	public abstract void afterDraw();

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY,
			float partialTicks) {
		beforeDraw();
		super.drawButton(mc, mouseX, mouseY, partialTicks);
		afterDraw();
	}
}