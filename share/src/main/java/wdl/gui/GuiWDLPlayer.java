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

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import wdl.WDL;
import wdl.config.IConfiguration;
import wdl.config.settings.PlayerSettings;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.GuiNumericTextField;
import wdl.gui.widget.SettingButton;

public class GuiWDLPlayer extends GuiScreen {
	private String title;
	private final GuiScreen parent;
	private final IConfiguration config;
	private SettingButton healthBtn;
	private SettingButton hungerBtn;
	private SettingButton playerPosBtn;
	private GuiButton pickPosBtn;
	private boolean showPosFields = false;
	private GuiNumericTextField posX;
	private GuiNumericTextField posY;
	private GuiNumericTextField posZ;
	private int posTextY;

	public GuiWDLPlayer(GuiScreen parent) {
		this.parent = parent;
		this.config = WDL.worldProps;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui() {
		this.buttonList.clear();
		this.title = I18n.format("wdl.gui.player.title",
				WDL.baseFolderName.replace('@', ':'));
		int y = this.height / 4 - 15;
		this.healthBtn = new SettingButton(1, PlayerSettings.HEALTH, this.config, this.width / 2 - 100, y);
		this.buttonList.add(this.healthBtn);
		y += 22;
		this.hungerBtn = new SettingButton(2, PlayerSettings.HUNGER, this.config, this.width / 2 - 100, y);
		this.buttonList.add(this.hungerBtn);
		y += 22;
		this.playerPosBtn = new SettingButton(3, PlayerSettings.PLAYER_POSITION, this.config, this.width / 2 - 100, y);
		this.buttonList.add(this.playerPosBtn);
		y += 22;
		this.posTextY = y + 4;
		this.posX = new GuiNumericTextField(40, this.fontRenderer,
				this.width / 2 - 87, y, 50, 16);
		this.posY = new GuiNumericTextField(41, this.fontRenderer,
				this.width / 2 - 19, y, 50, 16);
		this.posZ = new GuiNumericTextField(42, this.fontRenderer,
				this.width / 2 + 48, y, 50, 16);
		this.posX.setValue(config.getValue(PlayerSettings.PLAYER_X));
		this.posY.setValue(config.getValue(PlayerSettings.PLAYER_Y));
		this.posZ.setValue(config.getValue(PlayerSettings.PLAYER_Z));
		this.posX.setMaxStringLength(7);
		this.posY.setMaxStringLength(7);
		this.posZ.setMaxStringLength(7);
		y += 18;
		this.pickPosBtn = new GuiButton(4, this.width / 2 - 0, y, 100, 20,
				I18n.format("wdl.gui.player.setPositionToCurrentPosition"));
		this.buttonList.add(this.pickPosBtn);

		upadatePlayerPosVisibility();

		this.buttonList.add(new ButtonDisplayGui(this.width / 2 - 100, this.height - 29,
				200, 20, this.parent));
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 3) {
			upadatePlayerPosVisibility();
		} else if (button.id == 4) {
			this.setPlayerPosToPlayerPosition();
		}
	}

	@Override
	public void onGuiClosed() {
		if (this.showPosFields) {
			this.config.setValue(PlayerSettings.PLAYER_X, posX.getValue());
			this.config.setValue(PlayerSettings.PLAYER_Y, posY.getValue());
			this.config.setValue(PlayerSettings.PLAYER_Z, posZ.getValue());
		}

		WDL.saveProps();
	}

	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
			throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (this.showPosFields) {
			this.posX.mouseClicked(mouseX, mouseY, mouseButton);
			this.posY.mouseClicked(mouseX, mouseY, mouseButton);
			this.posZ.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	/**
	 * Fired when a key is typed. This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e).
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		this.posX.textboxKeyTyped(typedChar, keyCode);
		this.posY.textboxKeyTyped(typedChar, keyCode);
		this.posZ.textboxKeyTyped(typedChar, keyCode);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	@Override
	public void updateScreen() {
		this.posX.updateCursorCounter();
		this.posY.updateCursorCounter();
		this.posZ.updateCursorCounter();
		super.updateScreen();
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		Utils.drawListBackground(23, 32, 0, 0, height, width);

		this.drawCenteredString(this.fontRenderer, this.title,
				this.width / 2, 8, 0xFFFFFF);

		String tooltip = null;

		if (this.showPosFields) {
			this.drawString(this.fontRenderer, "X:", this.width / 2 - 99,
					this.posTextY, 0xFFFFFF);
			this.drawString(this.fontRenderer, "Y:", this.width / 2 - 31,
					this.posTextY, 0xFFFFFF);
			this.drawString(this.fontRenderer, "Z:", this.width / 2 + 37,
					this.posTextY, 0xFFFFFF);
			this.posX.drawTextBox();
			this.posY.drawTextBox();
			this.posZ.drawTextBox();

			if (Utils.isMouseOverTextBox(mouseX, mouseY, posX)) {
				tooltip = I18n.format("wdl.gui.player.positionTextBox.description", "X");
			} else if (Utils.isMouseOverTextBox(mouseX, mouseY, posY)) {
				tooltip = I18n.format("wdl.gui.player.positionTextBox.description", "Y");
			} else if (Utils.isMouseOverTextBox(mouseX, mouseY, posZ)) {
				tooltip = I18n.format("wdl.gui.player.positionTextBox.description", "Z");
			}

			if (pickPosBtn.isMouseOver()) {
				tooltip = I18n.format("wdl.gui.player.setPositionToCurrentPosition.description");
			}
		}

		if (healthBtn.isMouseOver()) {
			tooltip = healthBtn.getTooltip();
		}
		if (hungerBtn.isMouseOver()) {
			tooltip = hungerBtn.getTooltip();
		}
		if (playerPosBtn.isMouseOver()) {
			tooltip = playerPosBtn.getTooltip();
		}

		super.drawScreen(mouseX, mouseY, partialTicks);

		if (tooltip != null) {
			Utils.drawGuiInfoBox(tooltip, width, height, 48);
		}
	}

	private void upadatePlayerPosVisibility() {
		showPosFields = config.getValue(PlayerSettings.PLAYER_POSITION) == PlayerSettings.PlayerPos.XYZ;
		pickPosBtn.visible = showPosFields;
	}

	private void setPlayerPosToPlayerPosition() {
		this.posX.setValue((int)WDL.thePlayer.posX);
		this.posY.setValue((int)WDL.thePlayer.posY);
		this.posZ.setValue((int)WDL.thePlayer.posZ);
	}
}
