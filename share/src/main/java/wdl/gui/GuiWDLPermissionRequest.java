/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui;

import java.util.Map;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import wdl.WDLPluginChannels;
import wdl.gui.widget.Button;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.Screen;
import wdl.gui.widget.TextList;

/**
 * GUI for requesting permissions.  Again, this is a work in progress.
 */
public class GuiWDLPermissionRequest extends Screen {
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
		this.addList(this.list);

		this.requestField = new GuiTextField(0, fontRenderer,
				width / 2 - 155, 18, 150, 20);
		this.addTextField(requestField);

		this.submitButton = new Button(width / 2 + 5, 18,
				150, 20, "Submit request") {
			public @Override void performAction() {
				WDLPluginChannels.sendRequests();
				displayString = "Submitted!";
			}
		};
		this.submitButton.enabled = !(WDLPluginChannels.getRequests().isEmpty());
		this.buttonList.add(this.submitButton);

		this.buttonList.add(new ButtonDisplayGui(width / 2 - 100, height - 29,
				200, 20, this.parent));

		this.buttonList.add(new ButtonDisplayGui(this.width / 2 - 155, 39, 100, 20,
				I18n.format("wdl.gui.permissions.current"), () -> new GuiWDLPermissions(this.parent)));
		this.buttonList.add(new Button(this.width / 2 - 50, 39, 100, 20,
				I18n.format("wdl.gui.permissions.request")) {
			public @Override void performAction() {
				// Would open this GUI; do nothing.
			}
		});
		this.buttonList.add(new ButtonDisplayGui(this.width / 2 + 55, 39, 100, 20,
				I18n.format("wdl.gui.permissions.overrides"), () -> new GuiWDLChunkOverrides(this.parent)));
	}

	@Override
	public void charTyped(char keyChar) {
		String request = requestField.getText();
		boolean isValid = false;

		if (request.contains("=")) {
			String[] requestData = request.split("=", 2);
			if (requestData.length == 2) {
				String key = requestData[0];
				String value = requestData[1];

				isValid = WDLPluginChannels.isValidRequest(key, value);

				if (isValid && keyChar == '\n') {
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

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);

		this.drawCenteredString(this.fontRenderer, "Permission request",
				this.width / 2, 8, 0xFFFFFF);
	}
}
