/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui.widget;

/**
 * A specialty screen that can store text fields and lists.
 */
public class Screen extends ExtGuiScreen {

	@Override
	public void mouseDown(int mouseX, int mouseY) { }

	@Override
	public void mouseDragged(int mouseX, int mouseY) { }

	@Override
	public void mouseUp(int mouseX, int mouseY) { }

	@Override
	public void charTyped(char keyChar) { }

	@Override
	public void anyKeyPressed() { }

	@Override
	public boolean onCloseAttempt() {
		return true;
	}
}
