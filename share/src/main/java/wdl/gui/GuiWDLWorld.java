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
import wdl.config.settings.WorldSettings;
import wdl.gui.widget.GuiNumericTextField;
import wdl.gui.widget.SettingButton;

public class GuiWDLWorld extends GuiScreen {
	private String title;
	private final GuiScreen parent;
	private final IConfiguration config;
	private SettingButton allowCheatsBtn;
	private SettingButton gamemodeBtn;
	private SettingButton timeBtn;
	private SettingButton weatherBtn;
	private SettingButton spawnBtn;
	private GuiButton pickSpawnBtn;
	private boolean showSpawnFields = false;
	private GuiNumericTextField spawnX;
	private GuiNumericTextField spawnY;
	private GuiNumericTextField spawnZ;
	private int spawnTextY;

	public GuiWDLWorld(GuiScreen parent) {
		this.parent = parent;
		this.config = WDL.worldProps;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui() {
		this.buttonList.clear();
		this.title = I18n.format("wdl.gui.world.title",
				WDL.baseFolderName.replace('@', ':'));

		int y = this.height / 4 - 15;

		this.gamemodeBtn = new SettingButton(1, WorldSettings.GAME_MODE, this.config, this.width / 2 - 100, y);
		this.buttonList.add(this.gamemodeBtn);
		y += 22;
		this.allowCheatsBtn = new SettingButton(6, WorldSettings.ALLOW_CHEATS, this.config, this.width / 2 - 100, y);
		this.buttonList.add(this.allowCheatsBtn);
		y += 22;
		this.timeBtn = new SettingButton(2, WorldSettings.TIME, this.config, this.width / 2 - 100, y);
		this.buttonList.add(this.timeBtn);
		y += 22;
		this.weatherBtn = new SettingButton(3, WorldSettings.WEATHER, this.config, this.width / 2 - 100, y);
		this.buttonList.add(this.weatherBtn);
		y += 22;
		this.spawnBtn = new SettingButton(4, WorldSettings.SPAWN, this.config, this.width / 2 - 100, y);
		this.buttonList.add(this.spawnBtn);
		y += 22;
		this.spawnTextY = y + 4;
		this.spawnX = new GuiNumericTextField(40, this.fontRenderer, this.width / 2 - 87,
				y, 50, 16);
		this.spawnY = new GuiNumericTextField(41, this.fontRenderer, this.width / 2 - 19,
				y, 50, 16);
		this.spawnZ = new GuiNumericTextField(42, this.fontRenderer, this.width / 2 + 48,
				y, 50, 16);
		spawnX.setValue(config.getValue(WorldSettings.SPAWN_X));
		spawnY.setValue(config.getValue(WorldSettings.SPAWN_Y));
		spawnZ.setValue(config.getValue(WorldSettings.SPAWN_Z));
		this.spawnX.setMaxStringLength(7);
		this.spawnY.setMaxStringLength(7);
		this.spawnZ.setMaxStringLength(7);
		y += 18;
		this.pickSpawnBtn = new GuiButton(5, this.width / 2, y, 100, 20,
				I18n.format("wdl.gui.world.setSpawnToCurrentPosition"));
		this.buttonList.add(this.pickSpawnBtn);

		updateSpawnTextBoxVisibility();

		this.buttonList.add(new GuiButton(100, this.width / 2 - 100,
				this.height - 29, I18n.format("gui.done")));
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id == 4) {
				this.updateSpawnTextBoxVisibility();
			} else if (button.id == 5) {
				this.setSpawnToPlayerPosition();
			} else if (button.id == 100) {
				this.mc.displayGuiScreen(this.parent);
			}
		}
	}

	@Override
	public void onGuiClosed() {
		if (this.showSpawnFields) {
			this.config.setValue(WorldSettings.SPAWN_X, spawnX.getValue());
			this.config.setValue(WorldSettings.SPAWN_Y, spawnY.getValue());
			this.config.setValue(WorldSettings.SPAWN_Z, spawnZ.getValue());
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

		if (this.showSpawnFields) {
			this.spawnX.mouseClicked(mouseX, mouseY, mouseButton);
			this.spawnY.mouseClicked(mouseX, mouseY, mouseButton);
			this.spawnZ.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	/**
	 * Fired when a key is typed. This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e).
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		this.spawnX.textboxKeyTyped(typedChar, keyCode);
		this.spawnY.textboxKeyTyped(typedChar, keyCode);
		this.spawnZ.textboxKeyTyped(typedChar, keyCode);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	@Override
	public void updateScreen() {
		this.spawnX.updateCursorCounter();
		this.spawnY.updateCursorCounter();
		this.spawnZ.updateCursorCounter();
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

		if (this.showSpawnFields) {
			this.drawString(this.fontRenderer, "X:", this.width / 2 - 99,
					this.spawnTextY, 0xFFFFFF);
			this.drawString(this.fontRenderer, "Y:", this.width / 2 - 31,
					this.spawnTextY, 0xFFFFFF);
			this.drawString(this.fontRenderer, "Z:", this.width / 2 + 37,
					this.spawnTextY, 0xFFFFFF);
			this.spawnX.drawTextBox();
			this.spawnY.drawTextBox();
			this.spawnZ.drawTextBox();
		}

		super.drawScreen(mouseX, mouseY, partialTicks);

		String tooltip = null;

		if (allowCheatsBtn.isMouseOver()) {
			tooltip = allowCheatsBtn.getTooltip();
		} else if (gamemodeBtn.isMouseOver()) {
			tooltip = gamemodeBtn.getTooltip();
		} else if (timeBtn.isMouseOver()) {
			tooltip = timeBtn.getTooltip();
		} else if (weatherBtn.isMouseOver()) {
			tooltip = weatherBtn.getTooltip();
		} else if (spawnBtn.isMouseOver()) {
			tooltip = spawnBtn.getTooltip();
		} else if (pickSpawnBtn.isMouseOver()) {
			tooltip = I18n.format("wdl.gui.world.setSpawnToCurrentPosition.description");
		} else if (showSpawnFields) {
			if (Utils.isMouseOverTextBox(mouseX, mouseY, spawnX)) {
				tooltip = I18n.format("wdl.gui.world.spawnPos.description", "X");
			} else if (Utils.isMouseOverTextBox(mouseX, mouseY, spawnY)) {
				tooltip = I18n.format("wdl.gui.world.spawnPos.description", "Y");
			} else if (Utils.isMouseOverTextBox(mouseX, mouseY, spawnZ)) {
				tooltip = I18n.format("wdl.gui.world.spawnPos.description", "Z");
			}
		}

		Utils.drawGuiInfoBox(tooltip, width, height, 48);
	}

	/**
	 * Recalculates whether the spawn text boxes should be displayed.
	 */
	private void updateSpawnTextBoxVisibility() {
		boolean show = config.getValue(WorldSettings.SPAWN) == WorldSettings.SpawnMode.XYZ;

		this.showSpawnFields = show;
		this.pickSpawnBtn.visible = show;
	}

	private void setSpawnToPlayerPosition() {
		this.spawnX.setValue((int)WDL.thePlayer.posX);
		this.spawnY.setValue((int)WDL.thePlayer.posY);
		this.spawnZ.setValue((int)WDL.thePlayer.posZ);
	}
}
