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

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;

/**
 * Shared interface for a GuiScreen variant that can also store buttons and GuiLists.
 */
public interface IExtGuiScreen {
	/**
	 * Adds a button to the collection of buttons to process.
	 *
	 * @param button The button to add.
	 */
	public abstract <T extends GuiButton> T addButton(T button);

	/**
	 * Adds a list to the collection of lists to process.
	 *
	 * @param list The list to add.
	 */
	public abstract <T extends GuiList<E>, E extends GuiList.GuiListEntry<E>> T addList(T list);

	/**
	 * Adds a text field to the collection of fields to process.
	 *
	 * @param field The field to add.
	 */
	public abstract <T extends GuiTextField> T addTextField(T field);

	/**
	 * Called on click when the mouse is over the button.
	 * @param mouseX Mouse position on press.
	 * @param mouseY Mouse position on press.
	 */
	public abstract void mouseDown(int mouseX, int mouseY);
	/**
	 * Called on mouse release after this button was clicked on.
	 * @param mouseX Mouse position on release, which might not be over the button.
	 * @param mouseY Mouse position on release, which might not be over the button.
	 */
	public abstract void mouseUp(int mouseX, int mouseY);
	/**
	 * Called when the mouse is dragged after this button was clicked on.
	 * @param mouseX New mouse position.
	 * @param mouseY New mouse position.
	 */
	public abstract void mouseDragged(int mouseX, int mouseY);

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
	 * Called when escape is pressed, which normally closes the GUI.
	 *
	 * @return True if the attempt should go through.
	 */
	public abstract boolean onCloseAttempt();
}
