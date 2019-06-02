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

import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import wdl.EntityUtils;
import wdl.EntityUtils.SpigotEntityType;
import wdl.WDL;
import wdl.WDLPluginChannels;
import wdl.config.IConfiguration;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.WDLScreen;

/**
 * Provides fast setting for various entity options.
 */
public class GuiWDLEntityRangePresets extends WDLScreen implements GuiYesNoCallback {
	@Nullable
	private final GuiScreen parent;
	private final WDL wdl;
	private final IConfiguration config;

	private GuiButton vanillaButton;
	private GuiButton spigotButton;
	private GuiButton serverButton;
	private GuiButton cancelButton;

	private static final int ID_VANILLA = 0, ID_SPIGOT = 1, ID_SERVER = 2;

	public GuiWDLEntityRangePresets(@Nullable GuiScreen parent, WDL wdl, IConfiguration config) {
		this.parent = parent;
		this.wdl = wdl;
		this.config = config;
	}

	@Override
	public void initGui() {
		int y = this.height / 4;

		this.vanillaButton = this.addButton(new ButtonDisplayGui(
				this.width / 2 - 100, y, 200, 20,
				I18n.format("wdl.gui.rangePresets.vanilla"),
				makeYesNoGui("wdl.gui.rangePresets.vanilla.warning", ID_VANILLA)));
		y += 22;
		this.spigotButton = this.addButton(new ButtonDisplayGui(
				this.width / 2 - 100, y, 200, 20,
				I18n.format("wdl.gui.rangePresets.spigot"),
				makeYesNoGui("wdl.gui.rangePresets.spigot.warning", ID_SPIGOT)));
		y += 22;
		this.serverButton = this.addButton(new ButtonDisplayGui(
				this.width / 2 - 100, y, 200, 20,
				I18n.format("wdl.gui.rangePresets.server"),
				makeYesNoGui("wdl.gui.rangePresets.spigot.warning", ID_SERVER)));

		serverButton.enabled = WDLPluginChannels.hasServerEntityRange();

		y += 28;

		this.cancelButton = this.addButton(new ButtonDisplayGui(
				this.width / 2 - 100, this.height - 29, 200, 20,
				I18n.format("gui.cancel"), this.parent));
	}

	private Supplier<GuiYesNo> makeYesNoGui(String message, int id) {
		String upper = I18n.format("wdl.gui.rangePresets.upperWarning");
		String lower = I18n.format(message);

		return () -> new GuiYesNo(this, upper, lower, id);
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
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

		super.render(mouseX, mouseY, partialTicks);
	}

	@Override
	public void confirmResult(boolean result, int id) {
		if (result) {
			Set<String> entities = EntityUtils.getEntityTypes();

			if (id == ID_VANILLA) {
				for (String entity : EntityUtils.STANDARD_VANILLA_MANAGER.getProvidedEntities()) {
					config.setUserEntityTrackDistance(entity,
							EntityUtils.STANDARD_VANILLA_MANAGER.getTrackDistance(entity, null));
				}
			} else if (id == ID_SPIGOT) {
				for (String entity : EntityUtils.STANDARD_SPIGOT_MANAGER.getProvidedEntities()) {
					SpigotEntityType type = EntityUtils.STANDARD_SPIGOT_MANAGER.getSpigotType(entity);
					// XXX Allow specifying the range for each type instead of the default
					config.setUserEntityTrackDistance(entity,
							type.getDefaultRange());
				}
			} else if (id == ID_SERVER) {
				for (String entity : entities) {
					config.setUserEntityTrackDistance(entity,
							WDLPluginChannels.getEntityRange(entity));
				}
			}
		}

		mc.displayGuiScreen(parent);
	}

	@Override
	public void onGuiClosed() {
		wdl.saveProps();
	}
}
