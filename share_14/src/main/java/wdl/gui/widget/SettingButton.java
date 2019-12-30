/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui.widget;

import wdl.config.CyclableSetting;
import wdl.config.IConfiguration;

/**
 * A button that controls a setting.
 */
public class SettingButton extends WDLButton {
	private final CyclableSetting<?> setting;
	private final IConfiguration config;

	public SettingButton(CyclableSetting<?> setting, IConfiguration config, int x, int y) {
		this(setting, config, x, y, 200, 20);
	}

	public SettingButton(CyclableSetting<?> setting, IConfiguration config, int x, int y, int width, int height) {
		super(x, y, width, height, "");
		this.setting = setting;
		this.config = config;
		this.updateDisplayString();
	}

	private void updateDisplayString() {
		setMessage(config.getButtonText(setting).getFormattedText());
	}

	@Override
	public void performAction() {
		config.cycle(setting);
		this.updateDisplayString();
	}

	/**
	 * Gets a translated tooltip for this button.
	 */
	public String getTooltip() {
		return setting.getDescription().getFormattedText();
	}
}
