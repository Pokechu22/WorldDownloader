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

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateFlatWorld;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiCustomizeWorldScreen;
import net.minecraft.client.gui.GuiFlatPresets;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import wdl.WDL;
import wdl.config.IConfiguration;
import wdl.config.settings.GeneratorSettings;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.SettingButton;

public class GuiWDLGenerator extends GuiScreen {
	private static final Logger LOGGER = LogManager.getLogger();
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

		this.buttonList.clear();
		this.title = I18n.format("wdl.gui.generator.title",
				WDL.baseFolderName.replace('@', ':'));
		int y = this.height / 4 - 15;
		this.seedField = new GuiTextField(40, this.fontRenderer,
				this.width / 2 - (100 - seedWidth), y, 200 - seedWidth, 18);
		this.seedField.setText(config.getValue(GeneratorSettings.SEED));
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
				200, 20, "", this::getSettingsGui);
		updateSettingsButtonVisibility();
		this.buttonList.add(this.settingsPageBtn);

		this.buttonList.add(new ButtonDisplayGui(this.width / 2 - 100, height - 29,
				200, 20, this.parent));
	}

	/**
	 * Gets the proxy GUI to use for the current settings.
	 */
	@Nullable
	private GuiScreen getSettingsGui() {
		GeneratorSettings.Generator generator = config.getValue(GeneratorSettings.GENERATOR);
		switch (generator) {
		case FLAT:
			return new GuiFlatPresets(new GuiCreateFlatWorldProxy());
		case CUSTOMIZED:
			return new GuiCustomizeWorldScreen(new GuiCreateWorldProxy(),
					config.getValue(GeneratorSettings.GENERATOR_OPTIONS));
		default:
			LOGGER.warn("Generator lacks extra settings; this button should not be usable: " + generator);
		}
		return null;
	}

	@Override
	public void onGuiClosed() {
		config.setValue(GeneratorSettings.SEED, this.seedField.getText());

		WDL.saveProps();
	}

	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
			throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.seedField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Fired when a key is typed. This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e).
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		this.seedField.textboxKeyTyped(typedChar, keyCode);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	@Override
	public void updateScreen() {
		this.seedField.updateCursorCounter();
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

		this.drawString(this.fontRenderer, seedText, this.width / 2 - 100,
				this.height / 4 - 10, 0xFFFFFF);
		this.seedField.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);

		String tooltip = null;

		if (Utils.isMouseOverTextBox(mouseX, mouseY, seedField)) {
			tooltip = I18n.format("wdl.gui.generator.seed.description");
		} else if (generatorBtn.isMouseOver()) {
			tooltip = generatorBtn.getTooltip();
		} else if (generateStructuresBtn.isMouseOver()) {
			tooltip = generatorBtn.getTooltip();
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
		default:
			settingsPageBtn.visible = false;
		}
	}

	/**
	 * Fake implementation of {@link GuiCreateFlatWorld} that allows use of
	 * {@link GuiFlatPresets}.  Doesn't actually do anything; just passed in
	 * to the constructor to forward the information we need and to switch
	 * back to the main GUI afterwards.
	 */
	private class GuiCreateFlatWorldProxy extends GuiCreateFlatWorld {
		public GuiCreateFlatWorldProxy() {
			super(null, config.getValue(GeneratorSettings.GENERATOR_OPTIONS));
		}

		@Override
		public void initGui() {
			mc.displayGuiScreen(GuiWDLGenerator.this);
		}

		@Override
		protected void actionPerformed(GuiButton button) throws IOException {
			// Do nothing
		}

		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			// Do nothing
		}

		/**
		 * Gets the current flat preset.
		 */
		@Override
		public String getPreset() {
			return config.getValue(GeneratorSettings.GENERATOR_OPTIONS);
		}

		/**
		 * Sets the current flat preset.
		 */
		@Override
		public void setPreset(String preset) {
			if (preset == null) {
				preset = "";
			}
			config.setValue(GeneratorSettings.GENERATOR_OPTIONS, preset);
		}
	}

	/**
	 * Fake implementation of {@link GuiCreateWorld} that allows use of
	 * {@link GuiCustomizeWorldScreen}.  Doesn't actually do anything; just passed in
	 * to the constructor to forward the information we need and to switch
	 * back to the main GUI afterwards.
	 */
	private class GuiCreateWorldProxy extends GuiCreateWorld {
		public GuiCreateWorldProxy() {
			super(GuiWDLGenerator.this);

			this.chunkProviderSettingsJson = config.getValue(GeneratorSettings.GENERATOR_OPTIONS);
		}

		@Override
		public void initGui() {
			mc.displayGuiScreen(GuiWDLGenerator.this);
			config.setValue(GeneratorSettings.GENERATOR_OPTIONS, this.chunkProviderSettingsJson);
		}

		@Override
		protected void actionPerformed(GuiButton button) throws IOException {
			// Do nothing
		}

		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			// Do nothing
		}
	}
}
