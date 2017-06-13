package wdl.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;

class LocalUtils {
	private LocalUtils() { throw new AssertionError(); }

	/**
	 * Draws the given button (for multi-version compatability, this is needed for lists)
	 * @param button The button to draw.  Should already have been positioned.
	 */
	public static void drawButton(GuiButton button, Minecraft mc, int mouseX, int mouseY) {
		button.drawButton(mc, mouseX, mouseY);
	}
}

/**
 * Version-agnostic implementation of IGuiListEntry.
 */
interface GuiListEntry extends IGuiListEntry {
	@Override
	public default void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_) { }
	@Override
	public abstract void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected);

	@Override
	public abstract boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY);
	@Override
	public abstract void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY);
}
