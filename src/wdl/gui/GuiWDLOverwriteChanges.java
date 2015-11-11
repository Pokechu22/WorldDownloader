package wdl.gui;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.minecraft.client.gui.GuiButton;

import org.lwjgl.input.Keyboard;

import wdl.WDL;
import wdl.WorldBackup;

/**
 * GUI shown before possibly overwriting data in the world.
 */
public class GuiWDLOverwriteChanges extends GuiTurningCameraBase {
	private static final DateFormat displayDateFormat = 
			new SimpleDateFormat();
	
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
				
				backupData = "Backing up to " + (zip ? "zip " : " ")
						+ "folder " + backupName + ".";
				
				File fromFolder = WDL.saveHandler.getWorldDirectory();
				File backupFile = new File(fromFolder.getParentFile(),
						backupName);
				
				if (backupFile.exists()) {
					throw new IOException("Backup target (" + backupFile
							+ ") already exists!");
				}
				
				if (zip) {
					WorldBackup.zipDirectory(fromFolder, backupFile);
				} else {
					WorldBackup.copyDirectory(fromFolder, backupFile);
				}
			} catch (Exception e) {
				WDL.chatError("Exception while backing up world: " + e);
				e.printStackTrace();
			} finally {
				backingUp = false;
				
				WDL.overrideLastModifiedCheck = true;
				mc.displayGuiScreen(null);
				
				WDL.start();
			}
		}
	}
	
	public GuiWDLOverwriteChanges(long lastSaved, long lastPlayed) {
		savedText = displayDateFormat.format(new Date(lastSaved));
		playedText = displayDateFormat.format(new Date(lastPlayed));
	}
	
	/**
	 * Whether a backup is actively occuring.
	 */
	private volatile boolean backingUp = false;
	/**
	 * Data about the current backup process.
	 */
	private volatile String backupData = "";
	
	private String savedText = "";
	private String playedText = "";
	
	private int infoBoxX, infoBoxY;
	private int infoBoxWidth, infoBoxHeight;
	private GuiButton backupAsZipButton;
	private GuiButton backupAsFolderButton;
	private GuiButton downloadNowButton;
	private GuiButton cancelButton;
	
	private static final String TITLE =
			"The saved copy of the world may have been changed.";
	
	@Override
	public void initGui() {
		backingUp = false;
		
		infoBoxWidth = fontRendererObj.getStringWidth(TITLE);
		infoBoxHeight = 22 * 5;
		
		// Future compatibility -- TITLE may be far shorter
		// if i18n is ever setup.
		if (infoBoxWidth < 200) {
			infoBoxWidth = 200;
		}
		
		infoBoxY = 48;
		infoBoxX = (this.width / 2) - (infoBoxWidth / 2);
		
		int x = (this.width / 2) - 100;
		int y = infoBoxY + 22;
		
		backupAsZipButton = new GuiButton(0, x, y,
				"Backup as zip (then start download)");
		this.buttonList.add(backupAsZipButton);
		y += 22;
		backupAsFolderButton = new GuiButton(1, x, y,
				"Backup folder (then start download)");
		this.buttonList.add(backupAsFolderButton);
		y += 22;
		downloadNowButton = new GuiButton(2, x, y,
				"Allow overwriting (start download now)");
		this.buttonList.add(downloadNowButton);
		y += 22;
		cancelButton = new GuiButton(3, x, y,
				"Cancel (don't start download)");
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
			
			WDL.start();
		}
		if (button.id == 3) {
			mc.displayGuiScreen(null);
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.backingUp) {
			drawBackground(0);
			
			drawCenteredString(fontRendererObj, "Backing up the world...",
					width / 2, height / 4 - 40, 0xFFFFFF);
			drawCenteredString(fontRendererObj, backupData,
					width / 2, height / 4 - 10, 0xFFFFFF); 
		} else {
			drawDefaultBackground();
			Utils.drawBorder(32, 22, 0, 0, height, width);
			
			drawCenteredString(fontRendererObj,
					"WorldDownloader: Overwrite changes?", width / 2, 8,
					0xFFFFFF);
			drawCenteredString(fontRendererObj, "Last downloaded at " + savedText
					+ ", last played at " + playedText,
					width / 2, height - 8 - fontRendererObj.FONT_HEIGHT, 0xFFFFFF);
			
			drawRect(infoBoxX - 5, infoBoxY - 5, infoBoxX + infoBoxWidth + 5,
					infoBoxY + infoBoxHeight + 5, 0xB0000000);
			
			drawCenteredString(fontRendererObj, TITLE, width / 2, infoBoxY,
					0xFFFFFF);
			drawCenteredString(fontRendererObj,
					"Changes may be lost if not backed up.", width / 2,
					infoBoxY + fontRendererObj.FONT_HEIGHT, 0xFFFFFF);
			
			super.drawScreen(mouseX, mouseY, partialTicks);
			
			String tooltip = null;
			if (backupAsZipButton.isMouseOver()) {
				tooltip = "Creates a .zip folder in the saves folder.\n\n" +
						"This backup can't be played in game unless extracted, " +
						"but is useful if you just want to archive the changes.";
			} else if (backupAsFolderButton.isMouseOver()) {
				tooltip = "Creates a copy of the existing world folder.\n\n" +
						"This backup can be loaded in-game and will appear " +
						"in the world list.";
			} else if (downloadNowButton.isMouseOver()) {
				tooltip = "Don't create any backup at all.\n\n" +
						"§cAny changes made to the world will be overwritten.§r\n" +
						"If you haven't made any changes you want to keep, " +
						"this is the right option.";
			} else if (cancelButton.isMouseOver()) {
				tooltip = "Don't start any download.\n\n" +
						"Return to the sever.  You can manually make a " +
						"backup or just not download for the moment.";
			}
			
			Utils.drawGuiInfoBox(tooltip, width, height);
		}
	}
}
