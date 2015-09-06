package wdl.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import wdl.WDL;
import wdl.WDLPluginChannels;
import wdl.WorldBackup.WorldBackupType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;

/**
 * GUI that shows the current permissions for the user.
 */
public class GuiWDLPermissions extends GuiScreen {
	/**
	 * Margins for the top and the bottom of the list.
	 */
	private static final int TOP_MARGIN = 23, BOTTOM_MARGIN = 32;
	
	/**
	 * Whether the UI is in the request mode.  True: requesting new
	 * permissions; false, displaying current permissions.
	 */
	private boolean requestMode;
	
	/**
	 * List of permissions.
	 */
	private class PermissionsList extends GuiListExtended {
		public PermissionsList() {
			super(GuiWDLPermissions.this.mc, GuiWDLPermissions.this.width,
					GuiWDLPermissions.this.height, TOP_MARGIN,
					GuiWDLPermissions.this.height - BOTTOM_MARGIN, 
					fontRendererObj.FONT_HEIGHT * 2 + 2);
		}
		
		private class TextEntry implements IGuiListEntry {
			private final String line1;
			private final String line2;
			
			public TextEntry(String line1, String line2) {
				this.line1 = line1;
				this.line2 = "§7" + line2;
			}
			
			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				fontRendererObj.drawString(line1, x, y + 1, 0xFFFFFF);
				fontRendererObj.drawString(line2, x, y + 2
						+ fontRendererObj.FONT_HEIGHT, 0xFFFFFF);
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
		
		private List<IGuiListEntry> entries = new ArrayList<IGuiListEntry>() {{
			if (WDLPluginChannels.hasPermissions()) {
				add(new TextEntry("Can download: " + 
						WDLPluginChannels.canDownloadInGeneral(), 
						"Controls whether you are able to download"));
				//TODO canCacheChunks & saveradius
				add(new TextEntry("Can save entities: " + 
						WDLPluginChannels.canSaveEntities(),
						"Controls whether you can save entities"));
				add(new TextEntry("Can save tile entities: " + 
						WDLPluginChannels.canSaveTileEntities(),
						"Controls whether you can save tile entities"));
				add(new TextEntry("Can save containers: " + 
						WDLPluginChannels.canSaveContainers(),
						"Controls whether you can save containers"));
				add(new TextEntry("Can use functions unknown to the server: " + 
						WDLPluginChannels.canUseFunctionsUnknownToServer(),
						"Controls whether you can use newer functions of WDL."));
				//TODO: Entity ranges.
			}
		}};
		
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
			return GuiWDLPermissions.this.width - 10;
		}
		
		@Override
		public int getListWidth() {
			return GuiWDLPermissions.this.width - 18;
		}
	}
	
	private final GuiScreen parent;
	
	private PermissionsList list;
	
	public GuiWDLPermissions(GuiScreen parent) {
		this.parent = parent;
	}
	
	@Override
	public void initGui() {
		this.buttonList.add(new GuiButton(100, width / 2 - 100, height - 29,
				"Done"));
		this.list = new PermissionsList();
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
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 100) {
			this.mc.displayGuiScreen(this.parent);
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.list.drawScreen(mouseX, mouseY, partialTicks);
		
		this.drawCenteredString(this.fontRendererObj, "Permission info",
				this.width / 2, 8, 0xFFFFFF);
		
		if (!WDLPluginChannels.hasPermissions()) {
			this.drawCenteredString(this.fontRendererObj,
					"No permissions sent; defaulting to everything enabled.",
					this.width / 2, (this.height - 32 - 23) / 2 + 23
							- fontRendererObj.FONT_HEIGHT / 2, 0xFFFFFF);
		}
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
