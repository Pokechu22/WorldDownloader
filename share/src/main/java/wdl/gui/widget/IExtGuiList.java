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

import java.util.List;

/**
 * Methods that should be provided or implemented in the implementation-specific ExtGuiList.
 */
interface IExtGuiList<T extends IExtGuiList.IExtGuiListEntry<T>> {
	static interface IExtGuiListEntry<T> {
		/**
		 * Draw this entry, and also perform various update logic.
		 *
		 * @param x      X coordinate of the top left of the entry, based on centering
		 *               logic.
		 * @param y      Y coordinate of the top left of the entry.
		 * @param width  The usable width of the entry.
		 * @param height The usable height of the entry.
		 * @param mouseX The mouse's x position.
		 * @param mouseY The mouse's y position.
		 */
		public abstract void drawEntry(int x, int y, int width, int height, int mouseX, int mouseY);

		/**
		 * Called when the mouse is pressed.
		 *
		 * @param mouseX      Mouse x position in screen coordinates.
		 * @param mouseY      Mouse y position in screen coordinates.
		 * @param mouseButton The button that was pressed.
		 * @return True if the press hit a control (i.e. the list shouldn't drag with
		 *         mouse motions)
		 */
		public abstract boolean mouseDown(int mouseX, int mouseY, int mouseButton);

		/**
		 * Called when the mouse is released.
		 *
		 * @param mouseX      Mouse x position in screen coordinates.
		 * @param mouseY      Mouse y position in screen coordinates.
		 * @param mouseButton The button that was pressed.
		 */
		public abstract void mouseUp(int mouseX, int mouseY, int mouseButton);

		/**
		 * Checks if this entry is selected.
		 *
		 * @return True if the entry is selected and a black box should be drawn around it.
		 */
		public abstract boolean isSelected();
	}

	/**
	 * Gets a mutable list of entries.
	 */
	public abstract List<T> getEntries();

	public abstract void scroll(double by);

	/**
	 * Sets a special vertical offset, which only applies visually.
	 * @param offset The offset.
	 */
	public abstract void setVerticalOffset(int offset);
	public abstract int getEntryWidth();
	public abstract int getScrollBarX();

	public abstract int getWidth();
}
