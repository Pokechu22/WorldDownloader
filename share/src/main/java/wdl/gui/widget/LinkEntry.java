package wdl.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import wdl.versioned.VersionedFunctions;

/**
 * {@link IGuiListEntry} that displays a single clickable link.
 */
public class LinkEntry extends TextEntry {
	private final String link;
	private final int textWidth;
	private final int linkWidth;

	private int x;

	public LinkEntry(Minecraft mc, String text, String link) {
		super(mc, text, 0x5555FF);

		this.link = link;
		this.textWidth = mc.fontRenderer.getStringWidth(text);
		this.linkWidth = mc.fontRenderer.getStringWidth(link);
	}

	@Override
	public void drawEntry(int x, int y, int width, int height, int mouseX, int mouseY) {
		if (y < 0) {
			return;
		}

		this.x = x;

		super.drawEntry(x, y, width, height, mouseX, mouseY);

		int relativeX = mouseX - x;
		int relativeY = mouseY - y;
		if (relativeX >= 0 && relativeX <= textWidth &&
				relativeY >= 0 && relativeY <= height) {
			int drawX = mouseX - 2;
			if (drawX + linkWidth + 4 > width + x) {
				drawX = width + x - (4 + linkWidth);
			}
			Gui.fill(drawX, mouseY - 2, drawX + linkWidth + 4,
					mouseY + mc.fontRenderer.FONT_HEIGHT + 2, 0x80000000);

			mc.fontRenderer.drawStringWithShadow(link, drawX + 2, mouseY, 0xFFFFFF);
		}
	}

	@Override
	public boolean mouseDown(int mouseX, int mouseY, int mouseButton) {
		if (mouseX >= x && mouseX <= x + textWidth) {
			VersionedFunctions.openLink(link);
			return true;
		}
		return false;
	}
}