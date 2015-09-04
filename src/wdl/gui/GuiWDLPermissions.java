package wdl.gui;

import java.io.IOException;

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
	 * List of permissions.
	 */
	private class PermissionsList extends GuiListExtended {
		public PermissionsList() {
			super(GuiWDLPermissions.this.mc, GuiWDLPermissions.this.width,
					GuiWDLPermissions.this.height, TOP_MARGIN,
					GuiWDLPermissions.this.height - BOTTOM_MARGIN, 20);
		}
		
		@Override
		public IGuiListEntry getListEntry(int index) {
			return null;
		}
		
		@Override
		protected int getSize() {
			return 0;
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
