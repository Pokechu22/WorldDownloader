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

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import wdl.WDL;
import wdl.WorldBackup.WorldBackupType;
import wdl.config.IConfiguration;
import wdl.config.settings.MiscSettings;
import wdl.gui.widget.ButtonDisplayGui;

/**
 * GUI allowing control over the way the world is backed up.
 */
public class GuiWDLBackup extends GuiScreen {
	private final GuiScreen parent;
	private final IConfiguration config;

	private String description;

	private WorldBackupType backupType;

	public GuiWDLBackup(GuiScreen parent) {
		this.parent = parent;
		this.config = WDL.baseProps;

		this.description = I18n.format("wdl.gui.backup.description1") + "\n\n"
				+ I18n.format("wdl.gui.backup.description2") + "\n\n"
				+ I18n.format("wdl.gui.backup.description3");
	}

	@Override
	public void initGui() {
		backupType = config.getValue(MiscSettings.BACKUP_TYPE);

		this.buttonList.add(new GuiButton(0, this.width / 2 - 100, 32,
				getBackupButtonText()));

		this.buttonList.add(new ButtonDisplayGui(this.width / 2 - 100, height - 29,
				200, 20, this.parent));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) { //Backup mode
			switch (backupType) {
			case NONE: backupType = WorldBackupType.FOLDER; break;
			case FOLDER: backupType = WorldBackupType.ZIP; break;
			case ZIP: backupType = WorldBackupType.NONE; break;
			}

			button.displayString = getBackupButtonText();
		}
	}

	private String getBackupButtonText() {
		return I18n.format("wdl.gui.backup.backupMode",
				backupType.getDescription());
	}

	@Override
	public void onGuiClosed() {
		config.setValue(MiscSettings.BACKUP_TYPE, backupType);

		WDL.saveProps();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		Utils.drawListBackground(23, 32, 0, 0, height, width);

		this.drawCenteredString(this.fontRenderer,
				I18n.format("wdl.gui.backup.title"), this.width / 2, 8,
				0xFFFFFF);

		super.drawScreen(mouseX, mouseY, partialTicks);

		Utils.drawGuiInfoBox(description, width - 50, 3 * this.height / 5, width,
				height, 48);
	}
}
