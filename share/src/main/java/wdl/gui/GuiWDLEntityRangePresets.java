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
import java.util.Set;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import wdl.EntityUtils;
import wdl.EntityUtils.SpigotEntityType;
import wdl.StandardEntityManagers;
import wdl.WDL;
import wdl.WDLPluginChannels;

/**
 * Provides fast setting for various entity options.
 */
public class GuiWDLEntityRangePresets extends GuiScreen implements GuiYesNoCallback {
	private final GuiScreen parent;

	private GuiButton vanillaButton;
	private GuiButton spigotButton;
	private GuiButton serverButton;
	private GuiButton cancelButton;

	public GuiWDLEntityRangePresets(GuiScreen parent) {
		this.parent = parent;
	}

	@Override
	public void initGui() {
		int y = this.height / 4;

		this.vanillaButton = new GuiButton(0, this.width / 2 - 100, y,
				I18n.format("wdl.gui.rangePresets.vanilla"));
		y += 22;
		this.spigotButton = new GuiButton(1, this.width / 2 - 100, y,
				I18n.format("wdl.gui.rangePresets.spigot"));
		y += 22;
		this.serverButton = new GuiButton(2, this.width / 2 - 100, y,
				I18n.format("wdl.gui.rangePresets.server"));

		serverButton.enabled = WDLPluginChannels.hasServerEntityRange();

		this.buttonList.add(vanillaButton);
		this.buttonList.add(spigotButton);
		this.buttonList.add(serverButton);

		y += 28;

		this.cancelButton = new GuiButton(100, this.width / 2 - 100,
				this.height - 29, I18n.format("gui.cancel"));
		this.buttonList.add(cancelButton);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (!button.enabled) {
			return;
		}

		if (button.id < 3) {
			String upper;
			String lower;

			upper = I18n.format("wdl.gui.rangePresets.upperWarning");

			if (button.id == 0) {
				lower = I18n.format("wdl.gui.rangePresets.vanilla.warning");
			} else if (button.id == 1) {
				lower = I18n.format("wdl.gui.rangePresets.spigot.warning");
			} else if (button.id == 2) {
				lower = I18n.format("wdl.gui.rangePresets.server.warning");
			} else {
				//Should not happen.
				throw new Error("Button.id should never be negative.");
			}

			mc.displayGuiScreen(new GuiYesNo(this, upper, lower, button.id));
		}

		if (button.id == 100) {
			mc.displayGuiScreen(parent);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		Utils.drawListBackground(23, 32, 0, 0, height, width);

		this.drawCenteredString(this.fontRenderer,
				I18n.format("wdl.gui.rangePresets.title"), this.width / 2, 8,
				0xFFFFFF);

		String infoText = null;

		if (vanillaButton.isMouseOver()) {
			infoText = I18n.format("wdl.gui.rangePresets.vanilla.description");
		} else if (spigotButton.isMouseOver()) {
			infoText = I18n.format("wdl.gui.rangePresets.spigot.description");
		} else if (serverButton.isMouseOver()) {
			infoText = I18n.format("wdl.gui.rangePresets.server.description") + "\n\n";

			if (serverButton.enabled) {
				infoText += I18n.format("wdl.gui.rangePresets.server.installed");
			} else {
				infoText += I18n.format("wdl.gui.rangePresets.server.notInstalled");
			}
		} else if (cancelButton.isMouseOver()) {
			infoText = I18n.format("wdl.gui.rangePresets.cancel.description");
		}

		if (infoText != null) {
			Utils.drawGuiInfoBox(infoText, width, height, 48);
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		if (result) {
			Set<String> entities = EntityUtils.getEntityTypes();

			if (id == 0) {
				for (String entity : StandardEntityManagers.VANILLA.getProvidedEntities()) {
					WDL.worldProps.setProperty("Entity." + entity
							+ ".TrackDistance", Integer.toString(
									StandardEntityManagers.VANILLA.getTrackDistance(entity, null)));
				}
			} else if (id == 1) {
				for (String entity : StandardEntityManagers.SPIGOT.getProvidedEntities()) {
					SpigotEntityType type = StandardEntityManagers.SPIGOT.getSpigotType(entity);
					// XXX Allow specifying the range for each type instead of the default
					WDL.worldProps.setProperty("Entity." + entity
							+ ".TrackDistance", Integer.toString(
									type.getDefaultRange()));
				}
			} else if (id == 2) {
				for (String entity : entities) {
					WDL.worldProps.setProperty("Entity." + entity
							+ ".TrackDistance", Integer.toString(
									WDLPluginChannels.getEntityRange(entity)));
				}
			}
		}

		mc.displayGuiScreen(parent);
	}

	@Override
	public void onGuiClosed() {
		WDL.saveProps();
	}
}
