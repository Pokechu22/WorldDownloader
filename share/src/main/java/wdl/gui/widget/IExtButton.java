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

/**
 * Methods that should be provided on the implementation-specific ExtButton.
 */
interface IExtButton {
	/**
	 * Called on click when the mouse is over the button.
	 * @param mouseX Mouse position on press.
	 * @param mouseY Mouse position on press.
	 */
	abstract void mouseDown(int mouseX, int mouseY);
	/**
	 * Called on mouse release after this button was clicked on.
	 * @param mouseX Mouse position on release, which might not be over the button.
	 * @param mouseY Mouse position on release, which might not be over the button.
	 */
	abstract void mouseUp(int mouseX, int mouseY);
	/**
	 * Called when the mouse is dragged after this button was clicked on.
	 * @param mouseX New mouse position.
	 * @param mouseY New mouse position.
	 */
	abstract void mouseDragged(int mouseX, int mouseY);
	/**
	 * Called before the button is drawn.  Called even if the button is not visible.
	 */
	abstract void beforeDraw();
	/**
	 * Called after the button background has been drawn, but before the text.  Called even if the button is not visible.
	 */
	abstract void midDraw();
	/**
	 * Called after the button is drawn.
	 */
	abstract void afterDraw();
	/**
	 * Sets the message associated with this button.
	 */
	abstract void setMessage(String message);
	/**
	 * Sets whether or not this button is enabled/active.
	 */
	abstract void setEnabled(boolean enabled);
	/**
	 * Checks whether or not this button is enabled/active.
	 */
	abstract boolean isEnabled();
}
