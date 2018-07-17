package wdl.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import wdl.gui.Utils;

/**
 * {@link IGuiListEntry} that displays a single clickable link.
 */
public class LinkEntry extends TextEntry {
	private final String link;
	private final int textWidth;
	private final int linkWidth;

	public LinkEntry(Minecraft mc, String text, String link) {
		super(mc, text, 0x5555FF);

		this.link = link;
		this.textWidth = mc.fontRenderer.getStringWidth(text);
		this.linkWidth = mc.fontRenderer.getStringWidth(link);
	}

	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth,
			int slotHeight, int mouseX, int mouseY, boolean isSelected) {
		if (y < 0) {
			return;
		}

		super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX,
				mouseY, isSelected);

		int relativeX = mouseX - x;
		int relativeY = mouseY - y;
		if (relativeX >= 0 && relativeX <= textWidth &&
				relativeY >= 0 && relativeY <= slotHeight) {
			int drawX = mouseX - 2;
			if (drawX + linkWidth + 4 > listWidth + x) {
				drawX = listWidth + x - (4 + linkWidth);
			}
			Gui.drawRect(drawX, mouseY - 2, drawX + linkWidth + 4,
					mouseY + mc.fontRenderer.FONT_HEIGHT + 2, 0x80000000);

			Utils.drawStringWithShadow(link, drawX + 2, mouseY, 0xFFFFFF);
		}
	}

	@Override
	public boolean mousePressed(int slotIndex, int x, int y, int mouseEvent,
			int relativeX, int relativeY) {
		if (relativeX >= 0 && relativeX <= textWidth) {
			Utils.openLink(link);
			return true;
		}
		return false;
	}
}