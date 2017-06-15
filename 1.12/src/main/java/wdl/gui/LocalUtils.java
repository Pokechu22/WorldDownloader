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
		button.drawButton(mc, mouseX, mouseY, 0 /* partialTicks */);
	}

	/**
	 * Creates a new instance of {@link EntityPlayerSP}.
	 */
	public static EntityPlayerSP makePlayer() {
		return new EntityPlayerSP(WDL.minecraft, WDL.worldClient,
				WDL.thePlayer.connection, WDL.thePlayer.getStatFileWriter(),
				WDL.thePlayer.getRecipeBook());
	}
}
/**
 * Version-agnostic implementation of IGuiListEntry.
 */
abstract class GuiListEntry implements IGuiListEntry {
	@Override
	public void func_192633_a(int p_192633_1_, int p_192633_2_,
			int p_192633_3_, float p_192633_4_) {
		setSelected(p_192633_1_, p_192633_2_, p_192633_3_);
	}

	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth,
			int slotHeight, int mouseX, int mouseY, boolean isSelected,
			float partialTicks) {
		drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected);
	}

	public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_) { }
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
	public void drawButton(Minecraft mc, int mouseX, int mouseY,
			float partialTicks) {
		beforeDraw();
		super.drawButton(mc, mouseX, mouseY, partialTicks);
		afterDraw();
	}
}
