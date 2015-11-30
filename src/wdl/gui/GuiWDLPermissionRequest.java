package wdl.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import wdl.WDLPluginChannels;
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
			return GuiWDLPermissionRequest.this.width - 10;
		}
		
		@Override
		public int getListWidth() {
			return GuiWDLPermissionRequest.this.width - 18;
		}
		
		public void addRequest(String key, String value) {
			entries.add(new TextEntry("Requesting '" + key + "' to be '" + value + "'."));
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
	/**
	 * GUIButton for submitting the request.
	 */
	private GuiButton submitButton;
	
	public GuiWDLPermissionRequest(GuiScreen parent) {
		this.parent = parent;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		String text = "§c§lThis is a work in progress.§r\nYou can request " +
				"permissions in this GUI, although it currently requires " +
				"manually specifying the names.\n" +
				"Boolean fields: " + WDLPluginChannels.BOOLEAN_REQUEST_FIELDS + "\n" +
				"Integer fields: " + WDLPluginChannels.INTEGER_REQUEST_FIELDS + "\n\n";
		
		this.list = new PermissionsList(text);
		
		//Get the existing requests.
		for (Map.Entry<String, String> request : WDLPluginChannels
				.getRequests().entrySet()) {
			list.addRequest(request.getKey(), request.getValue());
		}
		
		this.requestField = new GuiTextField(0, fontRendererObj,
				width / 2 - 155, 18, 150, 20);
		
		this.submitButton = new GuiButton(1, width / 2 + 5, 18, 150,
				20, "Submit request");
		this.submitButton.enabled = !(WDLPluginChannels.getRequests().isEmpty());
		this.buttonList.add(this.submitButton);
		
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
		if (button.id == 1) {
			WDLPluginChannels.sendRequests();
			button.displayString = "Submitted!";
		}
		
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
		
		if (requestField.isFocused()) {
			String request = requestField.getText();
			
			boolean isValid = false;
			
			if (request.contains("=")) {
				String[] requestData = request.split("=", 2);
				if (requestData.length == 2) {
					String key = requestData[0];
					String value = requestData[1];
					
					isValid = WDLPluginChannels.isValidRequest(key, value);
					
					if (isValid && keyCode == Keyboard.KEY_RETURN) {
						requestField.setText("");
						isValid = false;
						
						WDLPluginChannels.addRequest(key, value);
						list.addRequest(key, value);
						submitButton.enabled = true;
					}
				}
			}
			
			requestField.setTextColor(isValid ? 0x40E040 : 0xE04040);
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
