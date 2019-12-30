/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui;

import javax.annotation.Nullable;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;
import wdl.WDL;
import wdl.config.IConfiguration;
import wdl.config.settings.WorldSettings;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.GuiNumericTextField;
import wdl.gui.widget.SettingButton;
import wdl.gui.widget.WDLButton;
import wdl.gui.widget.WDLScreen;
import wdl.versioned.VersionedFunctions;

public class GuiWDLWorld extends WDLScreen {
	@Nullable
	private final Screen parent;
	private final WDL wdl;
	private final IConfiguration config;
	private SettingButton allowCheatsBtn;
	private SettingButton gamemodeBtn;
	private SettingButton timeBtn;
	private SettingButton weatherBtn;
	private SettingButton spawnBtn;
	private WDLButton pickSpawnBtn;
	private boolean showSpawnFields = false;
	private GuiNumericTextField spawnX;
	private GuiNumericTextField spawnY;
	private GuiNumericTextField spawnZ;
	private int spawnTextY;

	public GuiWDLWorld(@Nullable Screen parent, WDL wdl) {
		super(new TranslationTextComponent("wdl.gui.world.title", WDL.baseFolderName));
		this.parent = parent;
		this.wdl = wdl;
		this.config = wdl.worldProps;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void init() {
		int y = this.height / 4 - 15;

		this.gamemodeBtn = this.addButton(new SettingButton(
				WorldSettings.GAME_MODE, this.config, this.width / 2 - 100, y));
		y += 22;
		this.allowCheatsBtn = this.addButton(new SettingButton(
				WorldSettings.ALLOW_CHEATS, this.config, this.width / 2 - 100, y));
		y += 22;
		this.timeBtn = this.addButton(new SettingButton(
				WorldSettings.TIME, this.config, this.width / 2 - 100, y));
		y += 22;
		this.weatherBtn = this.addButton(new SettingButton(
				WorldSettings.WEATHER, this.config, this.width / 2 - 100, y));
		y += 22;
		this.spawnBtn = this.addButton(new SettingButton(
				WorldSettings.SPAWN, this.config, this.width / 2 - 100, y) {
			public @Override void performAction() {
				super.performAction();
				updateSpawnTextBoxVisibility();
			}
		});
		y += 22;
		this.spawnTextY = y + 4;
		this.spawnX = this.addTextField(new GuiNumericTextField(40, this.font,
				this.width / 2 - 87, y, 50, 16));
		this.spawnY = this.addTextField(new GuiNumericTextField(41, this.font,
				this.width / 2 - 19, y, 50, 16));
		this.spawnZ = this.addTextField(new GuiNumericTextField(42, this.font,
				this.width / 2 + 48, y, 50, 16));
		spawnX.setValue(config.getValue(WorldSettings.SPAWN_X));
		spawnY.setValue(config.getValue(WorldSettings.SPAWN_Y));
		spawnZ.setValue(config.getValue(WorldSettings.SPAWN_Z));
		this.spawnX.setMaxStringLength(7);
		this.spawnY.setMaxStringLength(7);
		this.spawnZ.setMaxStringLength(7);
		y += 18;
		this.pickSpawnBtn = this.addButton(new WDLButton(this.width / 2, y, 100, 20,
				I18n.format("wdl.gui.world.setSpawnToCurrentPosition")) {
			public @Override void performAction() {
				setSpawnToPlayerPosition();
			}
		});

		updateSpawnTextBoxVisibility();

		this.addButton(new ButtonDisplayGui(this.width / 2 - 100, this.height - 29,
				200, 20, this.parent));
	}

	@Override
	public void removed() {
		if (this.showSpawnFields) {
			this.config.setValue(WorldSettings.SPAWN_X, spawnX.getValue());
			this.config.setValue(WorldSettings.SPAWN_Y, spawnY.getValue());
			this.config.setValue(WorldSettings.SPAWN_Z, spawnZ.getValue());
		}

		wdl.saveProps();
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		Utils.drawListBackground(23, 32, 0, 0, height, width);

		if (this.showSpawnFields) {
			this.drawString(this.font, "X:", this.width / 2 - 99,
					this.spawnTextY, 0xFFFFFF);
			this.drawString(this.font, "Y:", this.width / 2 - 31,
					this.spawnTextY, 0xFFFFFF);
			this.drawString(this.font, "Z:", this.width / 2 + 37,
					this.spawnTextY, 0xFFFFFF);
		}

		super.render(mouseX, mouseY, partialTicks);

		String tooltip = null;

		if (allowCheatsBtn.isHovered()) {
			tooltip = allowCheatsBtn.getTooltip();
		} else if (gamemodeBtn.isHovered()) {
			tooltip = gamemodeBtn.getTooltip();
		} else if (timeBtn.isHovered()) {
			tooltip = timeBtn.getTooltip();
		} else if (weatherBtn.isHovered()) {
			tooltip = weatherBtn.getTooltip();
		} else if (spawnBtn.isHovered()) {
			tooltip = spawnBtn.getTooltip();
		} else if (pickSpawnBtn.isHovered()) {
			tooltip = I18n.format("wdl.gui.world.setSpawnToCurrentPosition.description");
		} else if (showSpawnFields) {
			if (Utils.isHoveredTextBox(mouseX, mouseY, spawnX)) {
				tooltip = I18n.format("wdl.gui.world.spawnPos.description", "X");
			} else if (Utils.isHoveredTextBox(mouseX, mouseY, spawnY)) {
				tooltip = I18n.format("wdl.gui.world.spawnPos.description", "Y");
			} else if (Utils.isHoveredTextBox(mouseX, mouseY, spawnZ)) {
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
		this.spawnX.setVisible(show);
		this.spawnY.setVisible(show);
		this.spawnZ.setVisible(show);
		this.pickSpawnBtn.visible = show;
	}

	private void setSpawnToPlayerPosition() {
		this.spawnX.setValue((int)VersionedFunctions.getEntityX(wdl.player));
		this.spawnY.setValue((int)VersionedFunctions.getEntityY(wdl.player));
		this.spawnZ.setValue((int)VersionedFunctions.getEntityZ(wdl.player));;
	}
}
