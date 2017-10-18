/*
 * This file is part of the World Downloader API.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * You are free to include the World Downloader API within your own mods, as
 * permitted via the MMPLv2.
 */
package wdl.api;

import net.minecraft.client.gui.GuiScreen;

/**
 * {@link IWDLMod} that has an associated GUI that should be opened.
 */
public interface IWDLModWithGui extends IWDLMod {
	/**
	 * Gets the text to use on a button to open the given GUI.
	 *
	 * EG "Settings" or "About".  If <code>null</code> or an empty string
	 * is returned, "Settings..." will be used (translated to the active language).
	 */
	public abstract String getButtonName();

	/**
	 * Open the GUI after the coresponding button has been clicked.
	 *
	 * @param currentGui
	 *            The GUI that is currently open (and is calling this method).
	 */
	public abstract void openGui(GuiScreen currentGui);
}
