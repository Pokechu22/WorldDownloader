package wdl.api;

import net.minecraft.client.gui.GuiScreen;;

/**
 * {@link IWDLMod} that has an associated GUI that should be opened.
 */
public interface IWDLModWithGui {
	/**
	 * Gets the text to use on a button to open the given GUI.
	 * 
	 * EG "Settings" or "About".
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
