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
		button.func_191745_a(mc, mouseX, mouseY, 0 /* partialTicks */);
	}
}
/**
 * Version-agnostic implementation of IGuiListEntry.
 */
interface GuiListEntry extends IGuiListEntry {
	@Override
	public default void func_192633_a(int p_192633_1_, int p_192633_2_,
			int p_192633_3_, float p_192633_4_) {
		setSelected(p_192633_1_, p_192633_2_, p_192633_3_);
	}

	@Override
	public default void func_192634_a(int p_192634_1_, int p_192634_2_,
			int p_192634_3_, int p_192634_4_, int p_192634_5_, int p_192634_6_,
			int p_192634_7_, boolean p_192634_8_, float p_192634_9_) {
		drawEntry(p_192634_1_, p_192634_2_, p_192634_3_, p_192634_4_, p_192634_5_, p_192634_6_, p_192634_7_, p_192634_8_);
	}

	public default void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_) { }
	public abstract void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected);

	@Override
	public abstract boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY);
	@Override
	public abstract void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY);
}
