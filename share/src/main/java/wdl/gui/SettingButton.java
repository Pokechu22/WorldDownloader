/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import wdl.settings.IConfiguration;
import wdl.settings.CyclableSetting;

/**
 * A button that controls a setting.
 */
public class SettingButton extends GuiButton {
	private final CyclableSetting<?> setting;
	private final IConfiguration config;

	public SettingButton(int id, CyclableSetting<?> setting, IConfiguration config, int x, int y) {
		this(id, setting, config, x, y, 200, 20);
	}

	public SettingButton(int id, CyclableSetting<?> setting, IConfiguration config, int x, int y, int width, int height) {
		super(id, x, y, width, height, "");
		this.setting = setting;
		this.config = config;
		this.updateDisplayString();
	}

	private void updateDisplayString() {
		this.displayString = I18n.format(config.getButtonText(setting));
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			config.cycle(setting);
			this.updateDisplayString();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Gets a translated tooltip for this button.
	 */
	public String getTooltip() {
		return I18n.format(setting.getDescription());
	}
}
