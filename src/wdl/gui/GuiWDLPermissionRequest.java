package wdl.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.GuiScreen;

/**
 * GUI for requesting permissions.  Again, this is a work in progress.
 */
public class GuiWDLPermissionRequest extends GuiScreen {
	private static final int TOP_MARGIN = 61, BOTTOM_MARGIN = 32;
	
	private class PermissionsList extends GuiListExtended {
		public PermissionsList(String text) {
			super(GuiWDLPermissionRequest.this.mc, GuiWDLPermissionRequest.this.width,
					GuiWDLPermissionRequest.this.height, TOP_MARGIN,
					GuiWDLPermissionRequest.this.height - BOTTOM_MARGIN, 
					fontRendererObj.FONT_HEIGHT + 1);
			
			this.entries = new ArrayList<TextEntry>();
			
			List<String> lines = Utils.wordWrap(text, getListWidth());
			
			for (String s : lines) {
				entries.add(new TextEntry(s));
			}
		}
		
		public final List<TextEntry> entries;
		
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
			return GuiWDLPermissionRequest.this.width - 10;
		}
		
		@Override
		public int getListWidth() {
			return GuiWDLPermissionRequest.this.width - 18;
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
	
	private PermissionsList list;
	/**
	 * Parent GUI screen; displayed when this GUI is closed.
	 */
	private final GuiScreen parent;
	/**
	 * Field in which the wanted request is entered.
	 */
	private GuiTextField requestField;
	
	public GuiWDLPermissionRequest(GuiScreen parent) {
		this.parent = parent;
	}
	
	/**
	 * TEMPORARY options.  Obviously, more things WOULD be allowed, but right
	 * now they aren't.
	 */
	private static final List<String> OPTIONS = Arrays.asList(
			"downloadInGeneral=true", "cacheChunks=true", "saveRadius=10",
			"saveRadius=1", "saveEntities=true", "saveTileEntities=true",
			"saveContainers=true", "getEntityRanges=true");
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		String text = "§c§lThis is a work in progress.§r\nYou can request " +
				"permissions in this GUI, although it currently requires " +
				"manually specifying the names.  Valid values: \n" +
				OPTIONS + "\n\n";
		this.list = new PermissionsList(text);
		
		this.requestField = new GuiTextField(0, fontRendererObj,
				width / 2 - 155, 18, 150, 20);
		
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
			// Do nothing; on that GUI.
		}
		if (button.id == 202) {
			this.mc.displayGuiScreen(new GuiWDLRanges(this.parent));
		}
	}
	
	@Override
	public void updateScreen() {
		requestField.updateCursorCounter();
		super.updateScreen();
	}
	
	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
	throws IOException {
		requestField.mouseClicked(mouseX, mouseY, mouseButton);
		list.func_148179_a(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		requestField.textboxKeyTyped(typedChar, keyCode);
		
		if (keyCode == Keyboard.KEY_RETURN) {
			String request = requestField.getText();
			if (OPTIONS.contains(request)) {
				requestField.setText("");
				//Convert the request.
				String[] requestData = request.split("=");
				String key = requestData[0];
				String value = requestData[1];
				
				list.entries.add(new TextEntry("Requesting '" + key
						+ "' to be '" + value + "'."));
				
				//WDLPluginChannels.doSomething()
			}
		}
		if (OPTIONS.contains(requestField.getText())) {
			requestField.setTextColor(0x40E040);
		} else {
			requestField.setTextColor(0xE04040);
		}
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
		
		requestField.drawTextBox();
		
		this.drawCenteredString(this.fontRendererObj, "Permission request",
				this.width / 2, 8, 0xFFFFFF);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
