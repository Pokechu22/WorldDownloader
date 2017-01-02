package wdl.gui;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

import org.lwjgl.input.Keyboard;

import wdl.WDL;
import wdl.WDLMessageTypes;
import wdl.WDLMessages;
import wdl.WorldBackup;
import wdl.WorldBackup.IBackupProgressMonitor;

/**
 * GUI shown before possibly overwriting data in the world.
 */
public class GuiWDLOverwriteChanges extends GuiTurningCameraBase implements
		IBackupProgressMonitor {
	private class BackupThread extends Thread {
		private final DateFormat folderDateFormat = 
				new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		
		public BackupThread(boolean zip) {
			this.zip = zip;
		}
		
		private final boolean zip;
		
		@Override
		public void run() {
			try {
				String backupName = WDL.getWorldFolderName(WDL.worldName) + "_"
						+ folderDateFormat.format(new Date())
						+ "_user" + (zip ? ".zip" : "");
				
				if (zip) {
					backupData = I18n
							.format("wdl.gui.overwriteChanges.backingUp.zip", backupName);
				} else {
					backupData = I18n
							.format("wdl.gui.overwriteChanges.backingUp.folder", backupName);
				}
				
				File fromFolder = WDL.saveHandler.getWorldDirectory();
				File backupFile = new File(fromFolder.getParentFile(),
						backupName);
				
				if (backupFile.exists()) {
					throw new IOException("Backup target (" + backupFile
							+ ") already exists!");
				}
				
				if (zip) {
					WorldBackup.zipDirectory(fromFolder, backupFile,
							GuiWDLOverwriteChanges.this);
				} else {
					WorldBackup.copyDirectory(fromFolder, backupFile,
							GuiWDLOverwriteChanges.this);
				}
			} catch (Exception e) {
				WDLMessages.chatMessageTranslated(WDLMessageTypes.ERROR,
						"wdl.messages.generalError.failedToSetUpEntityUI");
			} finally {
				backingUp = false;
				
				WDL.overrideLastModifiedCheck = true;
				mc.displayGuiScreen(null);
				
				WDL.startDownload();
			}
		}
	}
	
	public GuiWDLOverwriteChanges(long lastSaved, long lastPlayed) {
		this.lastSaved = lastSaved;
		this.lastPlayed = lastPlayed;
	}
	
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
		infoBoxWidth = fontRendererObj.getStringWidth(overwriteWarning1);
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
		
		backupAsZipButton = new GuiButton(0, x, y,
				I18n.format("wdl.gui.overwriteChanges.asZip.name"));
		this.buttonList.add(backupAsZipButton);
		y += 22;
		backupAsFolderButton = new GuiButton(1, x, y,
				I18n.format("wdl.gui.overwriteChanges.asFolder.name"));
		this.buttonList.add(backupAsFolderButton);
		y += 22;
		downloadNowButton = new GuiButton(2, x, y,
				I18n.format("wdl.gui.overwriteChanges.startNow.name"));
		this.buttonList.add(downloadNowButton);
		y += 22;
		cancelButton = new GuiButton(3, x, y,
				I18n.format("wdl.gui.overwriteChanges.cancel.name"));
		this.buttonList.add(cancelButton);
		
		super.initGui();
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			//Don't allow closing at all. 
			return;
		}
		super.keyTyped(typedChar, keyCode);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (this.backingUp) {
			return;
		}
		
		if (button.id == 0) {
			backingUp = true;
			
			new BackupThread(true).start();
		}
		if (button.id == 1) {
			backingUp = true;
			
			new BackupThread(false).start();
		}
		if (button.id == 2) {
			WDL.overrideLastModifiedCheck = true;
			mc.displayGuiScreen(null);
			
			WDL.startDownload();
		}
		if (button.id == 3) {
			mc.displayGuiScreen(null);
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.backingUp) {
			drawBackground(0);
			
			drawCenteredString(fontRendererObj, backingUpTitle,
					width / 2, height / 4 - 40, 0xFFFFFF);
			drawCenteredString(fontRendererObj, backupData,
					width / 2, height / 4 - 10, 0xFFFFFF);
			if (backupFile != null) {
				String text = I18n.format(
						"wdl.gui.overwriteChanges.backingUp.progress",
						backupCurrent, backupCount, backupFile);
				drawCenteredString(fontRendererObj, text, width / 2,
						height / 4 + 10, 0xFFFFFF);
			}
		} else {
			drawDefaultBackground();
			Utils.drawBorder(32, 22, 0, 0, height, width);
			
			drawCenteredString(fontRendererObj, title, width / 2, 8, 0xFFFFFF);
			drawCenteredString(fontRendererObj, footer, width / 2, height - 8
					- fontRendererObj.FONT_HEIGHT, 0xFFFFFF);
			
			drawRect(infoBoxX - 5, infoBoxY - 5, infoBoxX + infoBoxWidth + 5,
					infoBoxY + infoBoxHeight + 5, 0xB0000000);
			
			drawCenteredString(fontRendererObj, captionTitle, width / 2,
					infoBoxY, 0xFFFFFF);
			drawCenteredString(fontRendererObj, captionSubtitle, width / 2,
					infoBoxY + fontRendererObj.FONT_HEIGHT, 0xFFFFFF);

			drawCenteredString(fontRendererObj, overwriteWarning1, width / 2,
					infoBoxY + 115, 0xFFFFFF);
			drawCenteredString(fontRendererObj, overwriteWarning2, width / 2,
					infoBoxY + 115 + fontRendererObj.FONT_HEIGHT, 0xFFFFFF);
			
			super.drawScreen(mouseX, mouseY, partialTicks);
			
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
}
