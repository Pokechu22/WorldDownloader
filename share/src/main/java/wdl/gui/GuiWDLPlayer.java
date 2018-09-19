/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import wdl.WDL;
import wdl.config.IConfiguration;
import wdl.config.settings.PlayerSettings;
import wdl.gui.widget.Button;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.GuiNumericTextField;
import wdl.gui.widget.Screen;
import wdl.gui.widget.SettingButton;

public class GuiWDLPlayer extends Screen {
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
		this.title = I18n.format("wdl.gui.player.title",
				WDL.baseFolderName.replace('@', ':'));
		int y = this.height / 4 - 15;
		this.healthBtn = this.addButton(new SettingButton(
				PlayerSettings.HEALTH, this.config, this.width / 2 - 100, y));
		y += 22;
		this.hungerBtn = this.addButton(new SettingButton(
				PlayerSettings.HUNGER, this.config, this.width / 2 - 100, y));
		y += 22;
		this.playerPosBtn = this.addButton(new SettingButton(
				PlayerSettings.PLAYER_POSITION, this.config, this.width / 2 - 100, y) {
			public @Override void performAction() {
				super.performAction();
				upadatePlayerPosVisibility();
			}
		});
		y += 22;
		this.posTextY = y + 4;
		this.posX = this.addTextField(new GuiNumericTextField(40, this.fontRenderer,
				this.width / 2 - 87, y, 50, 16));
		this.posY = this.addTextField(new GuiNumericTextField(41, this.fontRenderer,
				this.width / 2 - 19, y, 50, 16));
		this.posZ = this.addTextField(new GuiNumericTextField(42, this.fontRenderer,
				this.width / 2 + 48, y, 50, 16));
		this.posX.setValue(config.getValue(PlayerSettings.PLAYER_X));
		this.posY.setValue(config.getValue(PlayerSettings.PLAYER_Y));
		this.posZ.setValue(config.getValue(PlayerSettings.PLAYER_Z));
		this.posX.setMaxStringLength(7);
		this.posY.setMaxStringLength(7);
		this.posZ.setMaxStringLength(7);
		y += 18;
		this.pickPosBtn = this.addButton(new Button(
				this.width / 2 - 0, y, 100, 20,
				I18n.format("wdl.gui.player.setPositionToCurrentPosition")) {
			public @Override void performAction() {
				setPlayerPosToPlayerPosition();
			}
		});

		upadatePlayerPosVisibility();

		this.addButton(new ButtonDisplayGui(this.width / 2 - 100, this.height - 29,
				200, 20, this.parent));
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
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
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

		super.render(mouseX, mouseY, partialTicks);

		if (tooltip != null) {
			Utils.drawGuiInfoBox(tooltip, width, height, 48);
		}
	}

	private void upadatePlayerPosVisibility() {
		boolean show = config.getValue(PlayerSettings.PLAYER_POSITION) == PlayerSettings.PlayerPos.XYZ;;

		showPosFields = show;
		posX.setVisible(show);
		posY.setVisible(show);
		posZ.setVisible(show);
		pickPosBtn.visible = showPosFields;
	}

	private void setPlayerPosToPlayerPosition() {
		this.posX.setValue((int)WDL.player.posX);
		this.posY.setValue((int)WDL.player.posY);
		this.posZ.setValue((int)WDL.player.posZ);
	}
}
