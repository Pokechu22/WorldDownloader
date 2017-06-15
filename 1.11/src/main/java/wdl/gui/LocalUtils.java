package wdl.gui;

import wdl.WDL;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;

class LocalUtils {
	private LocalUtils() { throw new AssertionError(); }

	/**
	 * Draws the given button (for multi-version compatability, this is needed for lists)
	 * @param button The button to draw.  Should already have been positioned.
	 */
	public static void drawButton(GuiButton button, Minecraft mc, int mouseX, int mouseY) {
		button.func_146112_a(mc, mouseX, mouseY);
	}

	/**
	 * Creates a new instance of {@link EntityPlayerSP}.
	 */
	public static EntityPlayerSP makePlayer() {
		return new EntityPlayerSP(WDL.minecraft, WDL.worldClient,
				WDL.thePlayer.connection, WDL.thePlayer.getStatFileWriter());
	}
}

/**
 * Version-agnostic implementation of IGuiListEntry.
 */
interface GuiListEntry extends IGuiListEntry {
	@Override
	public default void func_178011_a(int p_178011_1_, int p_178011_2_, int p_178011_3_) {
		setSelected(p_178011_1_, p_178011_2_, p_178011_3_);
	}

	@Override
	public default void func_180790_a(int p_180790_1_, int p_180790_2_,
			int p_180790_3_, int p_180790_4_, int p_180790_5_, int p_180790_6_,
			int p_180790_7_, boolean p_180790_8_) {
		drawEntry(p_180790_1_, p_180790_2_, p_180790_3_, p_180790_4_, p_180790_5_, p_180790_6_, p_180790_7_, p_180790_8_);
	}

	public default void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_) { }
	public abstract void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected);

	@Override
	public abstract boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY);
	@Override
	public abstract void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY);
}

/**
 * Extendable button, to deal with changing method names between versions
 * @author Pokechu22
 *
 */
abstract class ExtButton extends GuiButton {
	public ExtButton(int buttonId, int x, int y, int widthIn, int heightIn,
			String buttonText) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
	}

	public abstract void beforeDraw();
	public abstract void afterDraw();

	@Override
	public void func_146112_a(Minecraft mc, int mouseX, int mouseY) {
		beforeDraw();
		super.func_146112_a(mc, mouseX, mouseY);
		afterDraw();
	}
}

