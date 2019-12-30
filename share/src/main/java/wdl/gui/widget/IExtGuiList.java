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

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;

/**
 * Methods that should be provided or implemented in the implementation-specific ExtGuiList.
 */
interface IExtGuiList<T extends IExtGuiList.IExtGuiListEntry<T>> {
	static interface IExtGuiListEntry<T> {
		/**
		 * Adds a button.  Intended for internal use.
		 *
		 * @param button The button.
		 * @param x      x coordinate relative to the center of the screen (may be
		 *               negative).
		 * @param y      y coordinate relative to the top of the entry.
		 */
		public abstract <B extends GuiButton> B addButton(B button, int x, int y);

		/**
		 * Adds a text field.  Intended for internal use.
		 *
		 * @param field  The text field.
		 * @param x      x coordinate relative to the center of the screen (may be
		 *               negative).
		 * @param y      y coordinate relative to the top of the entry.
		 */
		public abstract <B extends GuiTextField> B addTextField(B field, int x, int y);

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
		 * Called when a character is typed.  Will not be fired for special characters,
		 * such as the arrow keys, but will be fixed when pressing enter.
		 *
		 * @param keyChar The character that was typed.
		 */
		public abstract void charTyped(char keyChar);

		/**
		 * Called when any key is pressed, even keys that don't correspond to a char.
		 * Will be called after {@link #charTyped}, if charTyped is called at all.
		 */
		public abstract void anyKeyPressed();

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

	/**
	 * Can be used to visually offset the top of this list.
	 * All controls will receive input as if y were 0.
	 *
	 * @param pos The new y position.
	 */
	public abstract void setY(int pos);
	/**
	 * Gets the previously set y position.
	 *
	 * @return The y offset
	 */
	public abstract int getY();

	public abstract int getEntryWidth();
	public abstract int getScrollBarX();

	public abstract int getWidth();
}
