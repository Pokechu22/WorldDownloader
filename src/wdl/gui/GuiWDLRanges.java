package wdl.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import wdl.WDLPluginChannels;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;

/**
 * A GUI that Lists... well, will list, the current ChunkRanges.  Currently
 * a work in progress.
 * 
 * Also, expect a possible minimap integration in the future.
 */
public class GuiWDLRanges extends GuiScreen {
	private static final int TOP_MARGIN = 61, BOTTOM_MARGIN = 32;
	
	private class RangesList extends GuiListExtended {
		public RangesList(String text) {
			super(GuiWDLRanges.this.mc, GuiWDLRanges.this.width,
					GuiWDLRanges.this.height, TOP_MARGIN,
					GuiWDLRanges.this.height - BOTTOM_MARGIN, 
					fontRendererObj.FONT_HEIGHT + 1);
			
			this.entries = new ArrayList<TextEntry>();
			
			List<String> lines = Utils.wordWrap(text, getListWidth());
			
			for (String s : lines) {
				entries.add(new TextEntry(s));
			}
		}
		
		private final List<TextEntry> entries;
		
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
			return GuiWDLRanges.this.width - 10;
		}
		
		@Override
		public int getListWidth() {
			return GuiWDLRanges.this.width - 18;
		}
	}
	
	private class TextEntry implements IGuiListEntry {
		private final String line;
		
		/**
		 * Creates a TextEntry.
		 */
		public TextEntry(String line) {
			this.line = line;
		}
		
		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth,
				int slotHeight, int mouseX, int mouseY, boolean isSelected) {
			fontRendererObj.drawString(line, x, y + 1, 0xFFFFFF);
		}

		@Override
		public boolean mousePressed(int slotIndex, int x, int y,
				int mouseEvent, int relativeX, int relativeY) {
			return false;
		}

		@Override
		public void mouseReleased(int slotIndex, int x, int y,
				int mouseEvent, int relativeX, int relativeY) {
			
		}

		@Override
		public void setSelected(int slotIndex, int p_178011_2_,
				int p_178011_3_) {
			
		}
	}
	
	private RangesList list;
	
	public GuiWDLRanges(GuiScreen parent) {
		// TODO
	}
	
	@Override
	public void initGui() {
		String text = "§c§lThis is a work in progress.§r\nYou can download " +
				"in overriden chunks even if you are not allowed to download " +
				"elsewhere on the server.\nHere is a list of the current " +
				"chunk overrides; in the future, a map will appear here.  " +
				"Maybe also there will be a minimap mod integration.\n\n";
		text += WDLPluginChannels.getChunkOverrides().toString();
		this.list = new RangesList(text);
	}
	
	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
	throws IOException {
		list.func_148179_a(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.list.func_178039_p();
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if (list.func_148181_b(mouseX, mouseY, state)) {
			return;
		}
		super.mouseReleased(mouseX, mouseY, state);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.list == null) {
			return;
		}
		
		this.list.drawScreen(mouseX, mouseY, partialTicks);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
