package wdl.gui.widget;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import wdl.gui.Utils;

/**
 * {@link GuiListExtended} that provides scrollable lines of text, and support
 * for embedding links in it.
 */
public class TextList extends GuiListExtended {
	public final int topMargin;
	public final int bottomMargin;

	/**
	 * Creates a new TextList with no text.
	 */
	public TextList(Minecraft mc, int width, int height, int topMargin,
			int bottomMargin) {
		super(mc, width, height, topMargin, height - bottomMargin,
				mc.fontRenderer.FONT_HEIGHT + 1);

		this.topMargin = topMargin;
		this.bottomMargin = bottomMargin;

		this.entries = new ArrayList<>();
	}

	private List<IGuiListEntry> entries;

	@Override
	public IGuiListEntry getListEntry(int index) {
		return entries.get(index);
	}

	@Override
	protected int getSize() {
		return entries.size();
	}

	@Override
	protected int getScrollBarX() {
		return width - 10;
	}

	@Override
	public int getListWidth() {
		return width - 18;
	}

	public void addLine(String text) {
		List<String> lines = Utils.wordWrap(text, getListWidth());
		for (String line : lines) {
			entries.add(new TextEntry(mc, line, 0xFFFFFF));
		}
	}

	public void addBlankLine() {
		entries.add(new TextEntry(mc, "", 0xFFFFFF));
	}

	public void addLinkLine(String text, String URL) {
		List<String> lines = Utils.wordWrap(text, getListWidth());
		for (String line : lines) {
			entries.add(new LinkEntry(mc, line, URL));
		}
	}

	public void clearLines() {
		entries.clear();
	}
}