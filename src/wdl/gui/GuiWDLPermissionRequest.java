package wdl.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
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
	private static final int TOP_MARGIN = 61;

	private static final int BOTTOM_MARGIN = 32;

	private List<BoolFieldButton> fields;
	
	/**
	 * Next y coord to use for a BoolFieldButton.
	 */
	private int nextFieldButtonY;
	
	/**
	 * Single field.
	 * TODO: This should be a list element...
	 */
	private class BoolFieldButton extends GuiButton {
		/**
		 * Current value for this field.
		 * If null, then the default value is used.
		 */
		private Boolean value = null;
		private final String field;
		
		/**
		 * Constructs a new button and increments {@link #nextFieldButtonY}.
		 * @param field Name of the field.
		 */
		public BoolFieldButton(String field) {
			super(-1, 0, nextFieldButtonY, null);
			String currentValue = WDLPluginChannels.getRequests().get(value);
			if (currentValue == null) {
				this.value = null;
			} else {
				this.value = Boolean.parseBoolean(currentValue);
			}
			this.displayString = field + ": " + value;
			nextFieldButtonY += 22;
			this.field = field;
		}
		
		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY) {
			this.xPosition = GuiWDLPermissionRequest.this.width / 2 - this.width / 2;
			
			if (this.value == null) {
				// TODO: Display default value (caching?)
			}
			
			super.drawButton(mc, mouseX, mouseY);
		}
		
		@Override
		public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
			if (super.mousePressed(mc, mouseX, mouseY)) {
				if (this.value == null) {
					this.value = true;
				} else if (this.value == true) {
					this.value = false;
				} else if (this.value == false) {
					this.value = null;
				}
				this.displayString = field + ": " + value; // TODO: default value when null?
				// TODO: reset to default (right now invalid?)?
				WDLPluginChannels.addRequest(field, "" + value);
				list.addLine("Requesting '" + field + "' to be '" + value + "'.");
				return true;
			}
			return false;
		}
	}
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
		nextFieldButtonY = TOP_MARGIN + 6;
		this.fields = new ArrayList<BoolFieldButton>();
		for (String field : WDLPluginChannels.BOOLEAN_REQUEST_FIELDS) {
			this.fields.add(new BoolFieldButton(field));
		}
	}
	
	@SuppressWarnings("unchecked")
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
		
		this.buttonList.addAll(this.fields);
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
