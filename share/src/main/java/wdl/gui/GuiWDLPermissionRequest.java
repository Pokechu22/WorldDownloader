package wdl.gui;

import java.io.IOException;
import java.util.Map;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

import org.lwjgl.input.Keyboard;

import wdl.WDLPluginChannels;

/**
 * GUI for requesting permissions.  Again, this is a work in progress.
 */
public class GuiWDLPermissionRequest extends GuiScreen {
	private static final int TOP_MARGIN = 61, BOTTOM_MARGIN = 32;
	
	private TextList list;
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
	
	@Override
	public void initGui() {
		this.list = new TextList(mc, width, height, TOP_MARGIN, BOTTOM_MARGIN);
		
		list.addLine("\u00A7c\u00A7lThis is a work in progress.");
		list.addLine("You can request permissions in this GUI, although " +
				"it currently requires manually specifying the names.");
		list.addBlankLine();
		list.addLine("Boolean fields: " + WDLPluginChannels.BOOLEAN_REQUEST_FIELDS);
		list.addLine("Integer fields: " + WDLPluginChannels.INTEGER_REQUEST_FIELDS);
		list.addBlankLine();
		
		
		//Get the existing requests.
		for (Map.Entry<String, String> request : WDLPluginChannels
				.getRequests().entrySet()) {
			list.addLine("Requesting '" + request.getKey() + "' to be '"
					+ request.getValue() + "'.");
		}
		
		this.requestField = new GuiTextField(0, fontRendererObj,
				width / 2 - 155, 18, 150, 20);
		
		this.submitButton = new GuiButton(1, width / 2 + 5, 18, 150,
				20, "Submit request");
		this.submitButton.enabled = !(WDLPluginChannels.getRequests().isEmpty());
		this.buttonList.add(this.submitButton);
		
		this.buttonList.add(new GuiButton(100, width / 2 - 100, height - 29,
				I18n.format("gui.done")));
		
		this.buttonList.add(new GuiButton(200, this.width / 2 - 155, 39, 100, 20,
				I18n.format("wdl.gui.permissions.current")));
		this.buttonList.add(new GuiButton(201, this.width / 2 - 50, 39, 100, 20,
				I18n.format("wdl.gui.permissions.request")));
		this.buttonList.add(new GuiButton(202, this.width / 2 + 55, 39, 100, 20,
				I18n.format("wdl.gui.permissions.overrides")));
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
			this.mc.displayGuiScreen(new GuiWDLChunkOverrides(this.parent));
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
		list.mouseClicked(mouseX, mouseY, mouseButton);
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
						list.addLine("Requesting '" + key + "' to be '"
								+ value + "'.");
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
		this.list.handleMouseInput();
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if (list.mouseReleased(mouseX, mouseY, state)) {
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
