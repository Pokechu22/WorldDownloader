/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2020 Pokechu22, julialy
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
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import wdl.WDL;
import wdl.config.BooleanSetting;
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
	private SettingButton difficultyBtn;
	private SettingButton lockDifficultyBtn;
	private SettingButton spawnBtn;
	private WDLButton pickSpawnBtn;
	private boolean showSpawnFields = false;
	private GuiNumericTextField spawnX;
	private GuiNumericTextField spawnY;
	private GuiNumericTextField spawnZ;
	private int spawnTextY;

	/**
	 * @see net.minecraft.client.gui.widget.button.LockIconButton
	 */
	private class LockSettingButton extends SettingButton {
		private final BooleanSetting setting;

		public LockSettingButton(BooleanSetting setting, IConfiguration config, int x, int y) {
			super(setting, config, x, y, 20, 20);
			this.setting = setting;
			setMessage(new StringTextComponent(""));
		}

		@Override
		public void performAction() {
			config.cycle(setting);
			//TODO: this.queueNarration(250);
		}

		@Override
		public ITextComponent getNarratorMessage() {
			return config.getButtonText(setting);
		}

		@Override
		public void afterDraw() {
			int textureX = config.getValue((BooleanSetting)this.setting) ? 0 : 20; 
			int textureY = this.isHovered() ? 166 : 146;
			minecraft.getTextureManager().bindTexture(Button.WIDGETS_LOCATION);
			blit(this.x, this.y, textureX, textureY, this.width, this.height);
		}
	}

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
		this.difficultyBtn = this.addButton(new SettingButton(
				WorldSettings.DIFFICULTY, this.config, this.width / 2 - 100, y, 180, 20));
		this.lockDifficultyBtn = this.addButton(new LockSettingButton(
				WorldSettings.LOCK_DIFFICULTY, this.config, this.width / 2 + 80, y));
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
		this.spawnX = this.addTextField(new GuiNumericTextField(this.font,
				this.width / 2 - 87, y, 50, 16,
				new TranslationTextComponent("wdl.gui.world.spawn.coord", "X")));
		this.spawnY = this.addTextField(new GuiNumericTextField(this.font,
				this.width / 2 - 19, y, 50, 16,
				new TranslationTextComponent("wdl.gui.world.spawn.coord", "Y")));
		this.spawnZ = this.addTextField(new GuiNumericTextField(this.font,
				this.width / 2 + 48, y, 50, 16,
				new TranslationTextComponent("wdl.gui.world.spawn.coord", "Z")));
		spawnX.setValue(config.getValue(WorldSettings.SPAWN_X));
		spawnY.setValue(config.getValue(WorldSettings.SPAWN_Y));
		spawnZ.setValue(config.getValue(WorldSettings.SPAWN_Z));
		this.spawnX.setMaxStringLength(7);
		this.spawnY.setMaxStringLength(7);
		this.spawnZ.setMaxStringLength(7);
		y += 18;
		this.pickSpawnBtn = this.addButton(new WDLButton(this.width / 2, y, 100, 20,
				new TranslationTextComponent("wdl.gui.world.setSpawnToCurrentPosition")) {
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
		this.drawListBackground(23, 32, 0, 0, height, width);

		if (this.showSpawnFields) {
			this.drawString(this.font, "X:", this.width / 2 - 99,
					this.spawnTextY, 0xFFFFFF);
			this.drawString(this.font, "Y:", this.width / 2 - 31,
					this.spawnTextY, 0xFFFFFF);
			this.drawString(this.font, "Z:", this.width / 2 + 37,
					this.spawnTextY, 0xFFFFFF);
		}

		super.render(mouseX, mouseY, partialTicks);

		ITextComponent tooltip = null;

		if (allowCheatsBtn.isHovered()) {
			tooltip = allowCheatsBtn.getTooltip();
		} else if (gamemodeBtn.isHovered()) {
			tooltip = gamemodeBtn.getTooltip();
		} else if (timeBtn.isHovered()) {
			tooltip = timeBtn.getTooltip();
		} else if (weatherBtn.isHovered()) {
			tooltip = weatherBtn.getTooltip();
		} else if (this.difficultyBtn.isHovered()) {
			tooltip = difficultyBtn.getTooltip();
		} else if (this.lockDifficultyBtn.isHovered()) {
			tooltip = lockDifficultyBtn.getTooltip();
		} else if (spawnBtn.isHovered()) {
			tooltip = spawnBtn.getTooltip();
		} else if (pickSpawnBtn.isHovered()) {
			tooltip = new TranslationTextComponent("wdl.gui.world.setSpawnToCurrentPosition.description");
		} else if (showSpawnFields) {
			if (spawnX.isHovered()) {
				tooltip = new TranslationTextComponent("wdl.gui.world.spawnPos.description", "X");
			} else if (spawnY.isHovered()) {
				tooltip = new TranslationTextComponent("wdl.gui.world.spawnPos.description", "Y");
			} else if (spawnZ.isHovered()) {
				tooltip = new TranslationTextComponent("wdl.gui.world.spawnPos.description", "Z");
			}
		}

		this.drawGuiInfoBox(tooltip, width, height, 48);
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
