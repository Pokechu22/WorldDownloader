/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2019 Pokechu22, julialy
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
import net.minecraft.client.resources.I18n;
import wdl.WDL;
import wdl.WDLMessageTypes;
import wdl.WDLMessages;
import wdl.WorldBackup;
import wdl.WorldBackup.IBackupProgressMonitor;
import wdl.WorldBackup.WorldBackupType;
import wdl.gui.widget.Button;
import wdl.versioned.VersionedFunctions;

/**
 * GUI shown before possibly overwriting data in the world.
 */
public class GuiWDLOverwriteChanges extends GuiTurningCameraBase implements IBackupProgressMonitor {
	private class BackupThread extends Thread {
		public BackupThread(boolean zip) {
			this.zip = zip;
		}

		private final boolean zip;

		@Override
		public void run() {
			try {
				WorldBackupType type = zip ? WorldBackupType.ZIP : WorldBackupType.FOLDER;

				String name = WDL.getWorldFolderName(WDL.worldName);
				if (zip) {
					backupData = I18n
							.format("wdl.gui.overwriteChanges.backingUp.zip", name);
				} else {
					backupData = I18n
							.format("wdl.gui.overwriteChanges.backingUp.folder", name);
				}

				WorldBackup.backupWorld(wdl.saveHandler.getWorldDirectory(),
						WDL.getWorldFolderName(WDL.worldName) + "_user", type,
						GuiWDLOverwriteChanges.this);
			} catch (IOException ex) {
				WDLMessages.chatMessageTranslated(WDL.serverProps,
						WDLMessageTypes.ERROR, "wdl.messages.generalError.failedToBackUp", ex);
				VersionedFunctions.makeBackupFailedToast(ex);
			} finally {
				backingUp = false;

				mc.addScheduledTask(() -> {
					WDL.overrideLastModifiedCheck = true;
					mc.displayGuiScreen(null);

					wdl.startDownload();
				});
			}
		}
	}

	public GuiWDLOverwriteChanges(WDL wdl, long lastSaved, long lastPlayed) {
		super(wdl);
		this.wdl = wdl;
		this.lastSaved = lastSaved;
		this.lastPlayed = lastPlayed;
	}

	private final WDL wdl;

	/**
	 * Whether a backup is actively occuring.
	 */
	private volatile boolean backingUp = false;
	/**
	 * Data about the current backup process.
	 */
	private volatile String backupData = "";
	/**
	 * Number of files to backup.
	 */
	private volatile int backupCount;
	/**
	 * Current file being backed up.
	 */
	private volatile int backupCurrent;
	/**
	 * Name of the current file being backed up.
	 */
	private volatile String backupFile = "";

	private int infoBoxX, infoBoxY;
	private int infoBoxWidth, infoBoxHeight;
	private GuiButton backupAsZipButton;
	private GuiButton backupAsFolderButton;
	private GuiButton downloadNowButton;
	private GuiButton cancelButton;

	/**
	 * Time when the world was last saved / last played.
	 */
	private final long lastSaved, lastPlayed;

	private String title;
	private String footer;
	private String captionTitle;
	private String captionSubtitle;
	private String overwriteWarning1, overwriteWarning2;

	private String backingUpTitle;

	@Override
	public void initGui() {
		backingUp = false;

		title = I18n.format("wdl.gui.overwriteChanges.title");
		if (lastSaved != -1) {
			footer = I18n.format("wdl.gui.overwriteChanges.footer", lastSaved, lastPlayed);
		} else {
			footer = I18n.format("wdl.gui.overwriteChanges.footerNeverSaved", lastPlayed);
		}
		captionTitle = I18n.format("wdl.gui.overwriteChanges.captionTitle");
		captionSubtitle = I18n.format("wdl.gui.overwriteChanges.captionSubtitle");
		overwriteWarning1 = I18n.format("wdl.gui.overwriteChanges.overwriteWarning1");
		overwriteWarning2 = I18n.format("wdl.gui.overwriteChanges.overwriteWarning2");

		backingUpTitle = I18n.format("wdl.gui.overwriteChanges.backingUp.title");

		// TODO: Figure out the widest between captionTitle, captionSubtitle,
		// overwriteWarning1, and overwriteWarning2.
		infoBoxWidth = fontRenderer.getStringWidth(overwriteWarning1);
		infoBoxHeight = 22 * 6;

		// Ensure that the infobox is wide enough for the buttons.
		// While the default caption title is short enough, a translation may
		// make it too short (Chinese, for example).
		if (infoBoxWidth < 200) {
			infoBoxWidth = 200;
		}

		infoBoxY = 48;
		infoBoxX = (this.width / 2) - (infoBoxWidth / 2);

		int x = (this.width / 2) - 100;
		int y = infoBoxY + 22;

		backupAsZipButton = this.addButton(new Button(x, y, 200, 20,
				I18n.format("wdl.gui.overwriteChanges.asZip.name")) {
			public @Override void performAction() {
				if (backingUp) return;
				backingUp = true;
				new BackupThread(true).start();
			}
		});
		y += 22;
		backupAsFolderButton = this.addButton(new Button(x, y, 200, 20,
				I18n.format("wdl.gui.overwriteChanges.asFolder.name")) {
			public @Override void performAction() {
				if (backingUp) return;
				backingUp = true;
				new BackupThread(false).start();
			}
		});
		y += 22;
		downloadNowButton = this.addButton(new Button(x, y, 200, 20,
				I18n.format("wdl.gui.overwriteChanges.startNow.name")) {
			public @Override void performAction() {
				WDL.overrideLastModifiedCheck = true;
				mc.displayGuiScreen(null);
				wdl.startDownload();
			}
		});
		y += 22;
		cancelButton = this.addButton(new Button(x, y, 200, 20,
				I18n.format("wdl.gui.overwriteChanges.cancel.name")) {
			public @Override void performAction() {
				mc.displayGuiScreen(null);
			}
		});

		super.initGui();
	}

	@Override
	public boolean onCloseAttempt() {
		// Don't allow closing with escape.  The user has to read it!
		return false;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();

		if (this.backingUp) {
			drawBackground(0);

			drawCenteredString(fontRenderer, backingUpTitle,
					width / 2, height / 4 - 40, 0xFFFFFF);
			drawCenteredString(fontRenderer, backupData,
					width / 2, height / 4 - 10, 0xFFFFFF);
			if (backupFile != null) {
				String text = I18n.format(
						"wdl.gui.overwriteChanges.backingUp.progress",
						backupCurrent, backupCount, backupFile);
				drawCenteredString(fontRenderer, text, width / 2,
						height / 4 + 10, 0xFFFFFF);
			}
		} else {
			drawDefaultBackground();
			Utils.drawBorder(32, 22, 0, 0, height, width);

			drawCenteredString(fontRenderer, title, width / 2, 8, 0xFFFFFF);
			drawCenteredString(fontRenderer, footer, width / 2, height - 8
					- fontRenderer.FONT_HEIGHT, 0xFFFFFF);

			drawRect(infoBoxX - 5, infoBoxY - 5, infoBoxX + infoBoxWidth + 5,
					infoBoxY + infoBoxHeight + 5, 0xB0000000);

			drawCenteredString(fontRenderer, captionTitle, width / 2,
					infoBoxY, 0xFFFFFF);
			drawCenteredString(fontRenderer, captionSubtitle, width / 2,
					infoBoxY + fontRenderer.FONT_HEIGHT, 0xFFFFFF);

			drawCenteredString(fontRenderer, overwriteWarning1, width / 2,
					infoBoxY + 115, 0xFFFFFF);
			drawCenteredString(fontRenderer, overwriteWarning2, width / 2,
					infoBoxY + 115 + fontRenderer.FONT_HEIGHT, 0xFFFFFF);

			super.render(mouseX, mouseY, partialTicks);

			String tooltip = null;
			if (backupAsZipButton.isMouseOver()) {
				tooltip = I18n.format("wdl.gui.overwriteChanges.asZip.description");
			} else if (backupAsFolderButton.isMouseOver()) {
				tooltip = I18n.format("wdl.gui.overwriteChanges.asFolder.description");
			} else if (downloadNowButton.isMouseOver()) {
				tooltip = I18n.format("wdl.gui.overwriteChanges.startNow.description");
			} else if (cancelButton.isMouseOver()) {
				tooltip = I18n.format("wdl.gui.overwriteChanges.cancel.description");
			}

			Utils.drawGuiInfoBox(tooltip, width, height, 48);
		}
	}

	@Override
	public void setNumberOfFiles(int num) {
		backupCount = num;
		backupCurrent = 0;
	}

	@Override
	public void onNextFile(String name) {
		backupCurrent++;
		backupFile = name;
	}

	@Override
	public boolean shouldCancel() {
		return false;
	}
}
