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
import wdl.settings.GeneratorSettings;
import wdl.settings.IConfiguration;

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
		this.seedField.setText(WDL.worldProps.getProperty("RandomSeed"));
		y += 22;
		this.generatorBtn = new SettingButton(1, GeneratorSettings.GENERATOR, this.config, this.width / 2 - 100, y);
		this.buttonList.add(this.generatorBtn);
		y += 22;
		this.generateStructuresBtn = new SettingButton(2, GeneratorSettings.GENERATE_STRUCTURES, this.config, this.width / 2 - 100, y);
		this.buttonList.add(this.generateStructuresBtn);
		y += 22;
		this.settingsPageBtn = new GuiButton(3, this.width / 2 - 100, y,
				"");
		updateSettingsButtonVisibility();
		this.buttonList.add(this.settingsPageBtn);

		this.buttonList.add(new GuiButton(100, this.width / 2 - 100, height - 29,
				I18n.format("gui.done")));
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id == 1) {
				this.cycleGenerator();
			} else if (button.id == 3) {
				GeneratorSettings.Generator generator = config.getValue(GeneratorSettings.GENERATOR);
				switch (generator) {
				case FLAT:
					this.mc.displayGuiScreen(new GuiFlatPresets(
							new GuiCreateFlatWorldProxy()));
					break;
				case CUSTOMIZED:
					this.mc.displayGuiScreen(new GuiCustomizeWorldScreen(
							new GuiCreateWorldProxy(), WDL.worldProps
							.getProperty("GeneratorOptions", "")));
					break;
				default:
					LOGGER.warn("Generator lacks extra settings; this button should not be usable: " + generator);
				}
			} else if (button.id == 100) {
				this.mc.displayGuiScreen(this.parent);
			}
		}
	}

	@Override
	public void onGuiClosed() {
		WDL.worldProps.setProperty("RandomSeed", this.seedField.getText());

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

	// XXX This method probably shouldn't still exist
	private void cycleGenerator() {
		switch (config.getValue(GeneratorSettings.GENERATOR)) {
		case VOID:
			WDL.worldProps.setProperty("GeneratorName", "flat");
			WDL.worldProps.setProperty("GeneratorVersion", "0");
			WDL.worldProps.setProperty("GeneratorOptions", ";0"); //Single layer of air
			break;
		case DEFAULT:
			WDL.worldProps.setProperty("GeneratorName", "default");
			WDL.worldProps.setProperty("GeneratorVersion", "1");
			WDL.worldProps.setProperty("GeneratorOptions", "");
			break;
		case FLAT:
			WDL.worldProps.setProperty("GeneratorName", "flat");
			WDL.worldProps.setProperty("GeneratorVersion", "0");
			//Empty options for superflat gives the default superflat.
			WDL.worldProps.setProperty("GeneratorOptions", "");
			break;
		case LARGE_BIOMES:
			WDL.worldProps.setProperty("GeneratorName", "largeBiomes");
			WDL.worldProps.setProperty("GeneratorVersion", "0");
			WDL.worldProps.setProperty("GeneratorOptions", "");
			break;
		case AMPLIFIED:
			WDL.worldProps.setProperty("GeneratorName", "amplified");
			WDL.worldProps.setProperty("GeneratorVersion", "0");
			WDL.worldProps.setProperty("GeneratorOptions", "");
			break;
		case CUSTOMIZED:
			WDL.worldProps.setProperty("GeneratorName", "custom");
			WDL.worldProps.setProperty("GeneratorVersion", "0");
			WDL.worldProps.setProperty("GeneratorOptions", "");
			break;
		case LEGACY:
			// Legacy (1.1) world generator
			WDL.worldProps.setProperty("GeneratorName", "default_1_1");
			WDL.worldProps.setProperty("GeneratorVersion", "0");
			WDL.worldProps.setProperty("GeneratorOptions", "");
			break;
		}

		updateSettingsButtonVisibility();
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
			super(null, WDL.worldProps.getProperty("GeneratorOptions", ""));
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
			return WDL.worldProps.getProperty("GeneratorOptions", "");
		}

		/**
		 * Sets the current flat preset.
		 */
		@Override
		public void setPreset(String preset) {
			if (preset == null) {
				preset = "";
			}
			WDL.worldProps.setProperty("GeneratorOptions", preset);
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

			this.chunkProviderSettingsJson = WDL.worldProps.getProperty("GeneratorOptions", "");
		}

		@Override
		public void initGui() {
			mc.displayGuiScreen(GuiWDLGenerator.this);
			WDL.worldProps.setProperty("GeneratorOptions", this.chunkProviderSettingsJson);
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
