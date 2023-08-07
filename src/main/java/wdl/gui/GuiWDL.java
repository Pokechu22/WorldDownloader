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

import java.util.List;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import wdl.WDL;
import wdl.WDLPluginChannels;
import wdl.config.IConfiguration;
import wdl.config.settings.MiscSettings;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.GuiList;
import wdl.gui.widget.WDLButton;
import wdl.gui.widget.WDLScreen;
import wdl.gui.widget.WDLTextField;
import wdl.update.WDLUpdateChecker;

public class GuiWDL extends WDLScreen {
	/**
	 * Tooltip to display on the given frame.
	 */
	private ITextComponent displayedTooltip = null;

	private class GuiWDLButtonList extends GuiList<GuiWDLButtonList.ButtonEntry> {
		public GuiWDLButtonList() {
			super(GuiWDL.this, GuiWDL.this.width, GuiWDL.this.height, 39,
					GuiWDL.this.height - 32, 20);
		}

		private class ButtonEntry extends GuiList.GuiListEntry<ButtonEntry> {
			private final WDLButton button;

			private final ITextComponent tooltip;

			/**
			 * Constructor.
			 *
			 * @param key
			 *            The I18n key, which will have the base for this GUI
			 *            prepended.
			 * @param openFunc
			 *            Supplier that constructs a GuiScreen to open based off
			 *            of this screen (the one to open when that screen is
			 *            closed) and the WDL instance
			 * @param needsPerms
			 *            Whether the player needs download permission to use
			 *            this button.
			 */
			public ButtonEntry(String key, BiFunction<Screen, WDL, Screen> openFunc, boolean needsPerms) {
				this.button = this.addButton(new ButtonDisplayGui(0, 0, 200, 20,
						new TranslationTextComponent("wdl.gui.wdl." + key + ".name"),
						() -> openFunc.apply(GuiWDL.this, GuiWDL.this.wdl)), -100, 0);
				if (needsPerms) {
					button.setEnabled(WDLPluginChannels.canDownloadAtAll());
				}

				this.tooltip = new TranslationTextComponent("wdl.gui.wdl." + key + ".description");
			}

			@Override
			public void drawEntry(int x, int y, int width, int height, int mouseX, int mouseY) {
				super.drawEntry(x, y, width, height, mouseX, mouseY);
				if (button.isHovered()) {
					displayedTooltip = tooltip;
				}
			}
		}

		{
			List<ButtonEntry> entries = getEntries();

			entries.add(new ButtonEntry("worldOverrides", GuiWDLWorld::new, true));
			entries.add(new ButtonEntry("generatorOverrides", GuiWDLGenerator::new, true));
			entries.add(new ButtonEntry("playerOverrides", GuiWDLPlayer::new, true));
			entries.add(new ButtonEntry("entityOptions", GuiWDLEntities::new, true));
			entries.add(new ButtonEntry("gameruleOptions", GuiWDLGameRules::new, true));
			entries.add(new ButtonEntry("backupOptions", GuiWDLBackup::new, true));
			entries.add(new ButtonEntry("messageOptions", GuiWDLMessages::new, false));
			entries.add(new ButtonEntry("savedChunks", GuiSavedChunks::new, true));
			entries.add(new ButtonEntry("permissionsInfo", GuiWDLPermissions::new, false));
			entries.add(new ButtonEntry("about", GuiWDLAbout::new, false));
			if (WDLUpdateChecker.hasNewVersion()) {
				// Put at start
				entries.add(0, new ButtonEntry("updates.hasNew", GuiWDLUpdates::new, false));
			} else {
				entries.add(new ButtonEntry("updates", GuiWDLUpdates::new, false));
			}
		}
	}

	@Nullable
	private final Screen parent;
	private final WDL wdl;
	private final IConfiguration config;

	private WDLTextField worldname;

	public GuiWDL(@Nullable Screen parent, WDL wdl) {
		super(new TranslationTextComponent("wdl.gui.wdl.title", WDL.baseFolderName));
		this.parent = parent;
		this.wdl = wdl;
		this.config = WDL.serverProps;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void init() {
		this.worldname = this.addTextField(new WDLTextField(this.font,
				this.width / 2 - 155, 19, 150, 18, new TranslationTextComponent("wdl.gui.wdl.worldname")));
		this.worldname.setText(this.config.getValue(MiscSettings.SERVER_NAME));

		this.addButton(new ButtonDisplayGui(this.width / 2 - 100, this.height - 29,
				200, 20, parent));

		this.addList(new GuiWDLButtonList());
	}

	@Override
	public void removed() {
		if (this.worldname != null) {
			// Check to see if the server name matches the default, and clear the
			// setting if so, such that changing the name of the server will be
			// reflected in it.
			if (this.worldname.getText().equals(MiscSettings.SERVER_NAME.getDefault(this.config))) {
				this.config.clearValue(MiscSettings.SERVER_NAME);
			} else {
				this.config.setValue(MiscSettings.SERVER_NAME, this.worldname.getText());
			}
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		this.renderBackground();

		displayedTooltip = null;

		super.render(mouseX, mouseY, partialTicks);

		String name = I18n.format("wdl.gui.wdl.worldname");
		this.drawString(this.font, name, this.worldname.x
				- this.font.getStringWidth(name + " "), 26, 0xFFFFFF);

		this.drawGuiInfoBox(displayedTooltip, width, height, 48);
	}
}
