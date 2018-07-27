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

import java.util.List;
import java.util.function.Function;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import wdl.WDL;
import wdl.WDLPluginChannels;
import wdl.config.IConfiguration;
import wdl.config.settings.MiscSettings;
import wdl.gui.widget.Button;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.GuiList;
import wdl.gui.widget.Screen;
import wdl.update.WDLUpdateChecker;

public class GuiWDL extends Screen {
	/**
	 * Tooltip to display on the given frame.
	 */
	private String displayedTooltip = null;

	private class GuiWDLButtonList extends GuiList<GuiWDLButtonList.ButtonEntry> {
		public GuiWDLButtonList() {
			super(GuiWDL.this.mc, GuiWDL.this.width, GuiWDL.this.height, 39,
					GuiWDL.this.height - 32, 20);
		}

		private class ButtonEntry extends GuiList.GuiListEntry<ButtonEntry> {
			private final Button button;

			private final String tooltip;

			/**
			 * Constructor.
			 *
			 * @param key
			 *            The I18n key, which will have the base for this GUI
			 *            prepended.
			 * @param openFunc
			 *            Supplier that constructs a GuiScreen to open based off
			 *            of this screen (the one to open when that screen is
			 *            closed)
			 * @param needsPerms
			 *            Whether the player needs download permission to use
			 *            this button.
			 */
			public ButtonEntry(String key, Function<GuiScreen, GuiScreen> openFunc, boolean needsPerms) {
				this.button = new ButtonDisplayGui(0, 0, 200, 20,
						I18n.format("wdl.gui.wdl." + key + ".name"),
						() -> openFunc.apply(GuiWDL.this));
				if (needsPerms) {
					button.enabled = WDLPluginChannels.canDownloadAtAll();
				}
				this.addButton(this.button, -100, 0);

				this.tooltip = I18n.format("wdl.gui.wdl." + key + ".description");
			}

			@Override
			public void drawEntry(int x, int y, int width, int height, int mouseX, int mouseY) {
				super.drawEntry(x, y, width, height, mouseX, mouseY);
				if (button.isMouseOver()) {
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

	private String title = "";

	private final GuiScreen parent;
	private final IConfiguration config;

	private GuiTextField worldname;

	public GuiWDL(GuiScreen parent) {
		this.parent = parent;
		this.config = WDL.baseProps;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui() {
		if (WDL.isMultiworld && WDL.worldName.isEmpty()) {
			this.mc.displayGuiScreen(new GuiWDLMultiworldSelect(I18n
					.format("wdl.gui.multiworldSelect.title.changeOptions"),
					new GuiWDLMultiworldSelect.WorldSelectionCallback() {
				@Override
				public void onWorldSelected(String selectedWorld) {
					WDL.worldName = selectedWorld;
					WDL.isMultiworld = true;
					WDL.propsFound = true;

					WDL.worldProps = WDL.loadWorldProps(selectedWorld);
					mc.displayGuiScreen(GuiWDL.this);
				}

				@Override
				public void onCancel() {
					mc.displayGuiScreen(null);
				}
			}));
			return;
		}

		if (!WDL.propsFound) {
			mc.displayGuiScreen(new GuiWDLMultiworld(new GuiWDLMultiworld.MultiworldCallback() {
				@Override
				public void onSelect(boolean enableMutliworld) {
					WDL.isMultiworld = enableMutliworld;

					if (WDL.isMultiworld) {
						// Ask the user which world is loaded
						// TODO: Copy-pasted code from above -- suboptimal.
						mc.displayGuiScreen(new GuiWDLMultiworldSelect(I18n
								.format("wdl.gui.multiworldSelect.title.changeOptions"),
								new GuiWDLMultiworldSelect.WorldSelectionCallback() {
							@Override
							public void onWorldSelected(String selectedWorld) {
								WDL.worldName = selectedWorld;
								WDL.isMultiworld = true;
								WDL.propsFound = true;

								WDL.worldProps = WDL.loadWorldProps(selectedWorld);
								mc.displayGuiScreen(GuiWDL.this);
							}

							@Override
							public void onCancel() {
								mc.displayGuiScreen(null);
							}
						}));
					} else {
						WDL.baseProps.setValue(MiscSettings.LINKED_WORLDS, "");
						WDL.saveProps();
						WDL.propsFound = true;

						mc.displayGuiScreen(GuiWDL.this);
					}
				}

				@Override
				public void onCancel() {
					mc.displayGuiScreen(null);
				}
			}));
			return;
		}

		this.title = I18n.format("wdl.gui.wdl.title",
				WDL.baseFolderName.replace('@', ':'));

		this.worldname = new GuiTextField(42, this.fontRenderer,
				this.width / 2 - 155, 19, 150, 18);
		this.worldname.setText(this.config.getValue(MiscSettings.SERVER_NAME));
		this.addTextField(this.worldname);

		this.addButton(new ButtonDisplayGui(this.width / 2 - 100, this.height - 29,
				200, 20, parent));

		this.addList(new GuiWDLButtonList());
	}

	@Override
	public void onGuiClosed() {
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
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();

		displayedTooltip = null;

		super.drawScreen(mouseX, mouseY, partialTicks);

		this.drawCenteredString(this.fontRenderer, this.title,
				this.width / 2, 8, 0xFFFFFF);
		String name = I18n.format("wdl.gui.wdl.worldname");
		this.drawString(this.fontRenderer, name, this.worldname.x
				- this.fontRenderer.getStringWidth(name + " "), 26, 0xFFFFFF);

		Utils.drawGuiInfoBox(displayedTooltip, width, height, 48);
	}
}
