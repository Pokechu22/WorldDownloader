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

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiWDLMultiworld extends GuiScreen {
	private final MultiworldCallback callback;
	private GuiButton multiworldEnabledBtn;
	private boolean enableMultiworld = false;

	//TODO: Some of these things can be constants, but for consistancy aren't.
	//Maybe refactor it?
	private int infoBoxWidth;
	private int infoBoxHeight;
	private int infoBoxX;
	private int infoBoxY;
	private List<String> infoBoxLines;

	public static interface MultiworldCallback {
		public abstract void onCancel();
		public abstract void onSelect(boolean enableMutliworld);
	}

	public GuiWDLMultiworld(MultiworldCallback callback) {
		this.callback = callback;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui() {
		this.buttonList.clear();

		String multiworldMessage = I18n
				.format("wdl.gui.multiworld.descirption.requiredWhen")
				+ "\n\n"
				+ I18n.format("wdl.gui.multiworld.descirption.whatIs");

		infoBoxWidth = 320;
		infoBoxLines = Utils.wordWrap(multiworldMessage, infoBoxWidth - 20);
		infoBoxHeight = (fontRenderer.FONT_HEIGHT * (infoBoxLines.size() + 1)) + 40;

		infoBoxX = this.width / 2 - infoBoxWidth / 2;
		infoBoxY = this.height / 2 - infoBoxHeight / 2;

		this.multiworldEnabledBtn = new GuiButton(1, this.width / 2 - 100,
				infoBoxY + infoBoxHeight - 30,
				this.getMultiworldEnabledText());
		this.buttonList.add(this.multiworldEnabledBtn);

		this.buttonList.add(new GuiButton(100, this.width / 2 - 155,
				this.height - 29, 150, 20, I18n.format("gui.cancel")));

		this.buttonList.add(new GuiButton(101, this.width / 2 + 5,
				this.height - 29, 150, 20, I18n.format("gui.done")));
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 1) {
			this.toggleMultiworldEnabled();
		} else if (button.id == 100) {
			callback.onCancel();
		} else if (button.id == 101) {
			callback.onSelect(this.enableMultiworld);
		}
	}

	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
			throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Fired when a key is typed. This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e).
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		Utils.drawBorder(32, 32, 0, 0, height, width);

		this.drawCenteredString(this.fontRenderer,
				I18n.format("wdl.gui.multiworld.title"),
				this.width / 2, 8, 0xFFFFFF);

		drawRect(infoBoxX, infoBoxY, infoBoxX + infoBoxWidth, infoBoxY
				+ infoBoxHeight, 0xB0000000);

		int x = infoBoxX + 10;
		int y = infoBoxY + 10;

		for (String s : infoBoxLines) {
			this.drawString(fontRenderer, s, x, y, 0xFFFFFF);
			y += fontRenderer.FONT_HEIGHT;
		}

		//Red box around "multiworld support" button.
		drawRect(
				multiworldEnabledBtn.x - 2,
				multiworldEnabledBtn.y - 2,
				multiworldEnabledBtn.x
				+ multiworldEnabledBtn.getButtonWidth() + 2,
				multiworldEnabledBtn.y + 20 + 2, 0xFFFF0000);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	/**
	 * Toggles whether multiworld support is enabled.
	 */
	private void toggleMultiworldEnabled() {
		if (this.enableMultiworld) {
			this.enableMultiworld = false;
		} else {
			this.enableMultiworld = true;
		}

		this.multiworldEnabledBtn.displayString = getMultiworldEnabledText();
	}

	/**
	 * Gets the text to display on the multiworld enabled button.
	 */
	private String getMultiworldEnabledText() {
		return I18n.format("wdl.gui.multiworld." + enableMultiworld);
	}
}
