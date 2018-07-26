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
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import wdl.WDL;
import wdl.config.IConfiguration;
import wdl.config.settings.GeneratorSettings;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.Screen;
import wdl.gui.widget.SettingButton;
import wdl.versioned.VersionedFunctions;

public class GuiWDLGenerator extends Screen {
	private String title;
	private final GuiScreen parent;
	private final IConfiguration config;
	private GuiTextField seedField;
	private SettingButton generatorBtn;
	private SettingButton generateStructuresBtn;
	private GuiButton settingsPageBtn;

	private String seedText;

	public GuiWDLGenerator(GuiScreen parent) {
		this.parent = parent;
		this.config = WDL.worldProps;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui() {
		this.seedText = I18n.format("wdl.gui.generator.seed");
		int seedWidth = fontRenderer.getStringWidth(seedText + " ");

		this.title = I18n.format("wdl.gui.generator.title",
				WDL.baseFolderName.replace('@', ':'));
		int y = this.height / 4 - 15;
		this.seedField = new GuiTextField(40, this.fontRenderer,
				this.width / 2 - (100 - seedWidth), y, 200 - seedWidth, 18);
		this.seedField.setText(config.getValue(GeneratorSettings.SEED));
		this.addTextField(seedField);
		y += 22;
		this.generatorBtn = new SettingButton(GeneratorSettings.GENERATOR, this.config, this.width / 2 - 100, y) {
			public @Override void performAction() {
				super.performAction();
				updateSettingsButtonVisibility();
				// Clear any existing custom values, as they don't apply to another generator.
				config.clearValue(GeneratorSettings.GENERATOR_NAME);
				config.clearValue(GeneratorSettings.GENERATOR_VERSION);
				config.clearValue(GeneratorSettings.GENERATOR_OPTIONS);
			}
		};
		this.buttonList.add(this.generatorBtn);
		y += 22;
		this.generateStructuresBtn = new SettingButton(GeneratorSettings.GENERATE_STRUCTURES, this.config, this.width / 2 - 100, y);
		this.buttonList.add(this.generateStructuresBtn);
		y += 22;
		this.settingsPageBtn = new ButtonDisplayGui(this.width / 2 - 100, y,
				200, 20, "", this::makeGeneratorSettingsGui);
		updateSettingsButtonVisibility();
		this.buttonList.add(this.settingsPageBtn);

		this.buttonList.add(new ButtonDisplayGui(this.width / 2 - 100, height - 29,
				200, 20, this.parent));
	}

	/**
	 * Gets the proxy GUI to use for the current settings.
	 */
	private GuiScreen makeGeneratorSettingsGui() {
		GeneratorSettings.Generator generator = config.getValue(GeneratorSettings.GENERATOR);
		String generatorConfig = config.getValue(GeneratorSettings.GENERATOR_OPTIONS);
		return VersionedFunctions.makeGeneratorSettingsGui(generator, this, generatorConfig,
				value -> config.setValue(GeneratorSettings.GENERATOR_OPTIONS, value));
	}

	@Override
	public void onGuiClosed() {
		config.setValue(GeneratorSettings.SEED, this.seedField.getText());

		WDL.saveProps();
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		Utils.drawListBackground(23, 32, 0, 0, height, width);

		this.drawCenteredString(this.fontRenderer, this.title,
				this.width / 2, 8, 0xFFFFFF);

		this.drawString(this.fontRenderer, seedText, this.width / 2 - 100,
				this.height / 4 - 10, 0xFFFFFF);

		super.drawScreen(mouseX, mouseY, partialTicks);

		String tooltip = null;

		if (Utils.isMouseOverTextBox(mouseX, mouseY, seedField)) {
			tooltip = I18n.format("wdl.gui.generator.seed.description");
		} else if (generatorBtn.isMouseOver()) {
			tooltip = generatorBtn.getTooltip();
		} else if (generateStructuresBtn.isMouseOver()) {
			tooltip = generateStructuresBtn.getTooltip();
		}
		Utils.drawGuiInfoBox(tooltip, width, height, 48);
	}

	/**
	 * Updates whether the {@link #settingsPageBtn} is shown or hidden, and
	 * the text on it.
	 */
	private void updateSettingsButtonVisibility() {
		switch (this.config.getValue(GeneratorSettings.GENERATOR)) {
		case FLAT:
			settingsPageBtn.visible = true;
			settingsPageBtn.displayString = I18n.format("wdl.gui.generator.flatSettings");
			break;
		case CUSTOMIZED:
			settingsPageBtn.visible = true;
			settingsPageBtn.displayString = I18n.format("wdl.gui.generator.customSettings");
			break;
		case BUFFET:
			settingsPageBtn.visible = true;
			settingsPageBtn.displayString = I18n.format("wdl.gui.generator.buffetSettings");
			break;
		default:
			settingsPageBtn.visible = false;
		}
	}
}
