package wdl.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import wdl.WDLPluginChannels;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;

/**
 * A GUI that Lists... well, will list, the current chunk overrides.  Currently
 * a work in progress.
 * 
 * Also, expect a possible minimap integration in the future.
 */
public class GuiWDLChunkOverrides extends GuiScreen {
	private static final int TOP_MARGIN = 61, BOTTOM_MARGIN = 32;
	
	private class OverridesList extends GuiListExtended {
		public OverridesList(String text) {
			super(GuiWDLChunkOverrides.this.mc, GuiWDLChunkOverrides.this.width,
					GuiWDLChunkOverrides.this.height, TOP_MARGIN,
					GuiWDLChunkOverrides.this.height - BOTTOM_MARGIN, 
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
			return GuiWDLChunkOverrides.this.width - 10;
		}
		
		@Override
		public int getListWidth() {
			return GuiWDLChunkOverrides.this.width - 18;
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
	
	private OverridesList list;
	/**
	 * Parent GUI screen; displayed when this GUI is closed.
	 */
	private final GuiScreen parent;
	
	public GuiWDLChunkOverrides(GuiScreen parent) {
		this.parent = parent;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		String text = "§c§lThis is a work in progress.§r\nYou can download " +
				"in overriden chunks even if you are not allowed to download " +
				"elsewhere on the server.\nHere is a list of the current " +
				"chunk overrides; in the future, a map will appear here.  " +
				"Maybe also there will be a minimap mod integration.\n\n";
		text += WDLPluginChannels.getChunkOverrides().toString();
		this.list = new OverridesList(text);
		
		this.buttonList.add(new GuiButton(100, width / 2 - 100, height - 29,
				"Done"));
		
		this.buttonList.add(new GuiButton(200, this.width / 2 - 155, 39, 100, 20,
				"Current perms"));
		this.buttonList.add(new GuiButton(201, this.width / 2 - 50, 39, 100, 20,
				"Request perms"));
		this.buttonList.add(new GuiButton(202, this.width / 2 + 55, 39, 100, 20,
				"Chunk Overrides"));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 100) {
			this.mc.displayGuiScreen(this.parent);
		}
		
		if (button.id == 200) {
			this.mc.displayGuiScreen(new GuiWDLPermissions(this.parent));
		}
		if (button.id == 201) {
			this.mc.displayGuiScreen(new GuiWDLPermissionRequest(this.parent));
		}
		if (button.id == 202) {
			// Would open this GUI; do nothing.
		}
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
		
		this.drawCenteredString(this.fontRendererObj, "Chunk overrides",
				this.width / 2, 8, 0xFFFFFF);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
