package wdl.gui;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

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
	
	@Override
	public void initGui() {
		backingUp = false;
		
		int x = (this.width / 2) - 100;
		int y = (this.height / 4) + 15;
		
		this.buttonList.add(new GuiButton(0, x, y,
				"Backup as zip (then start download)"));
		y += 22;
		this.buttonList.add(new GuiButton(1, x, y,
				"Backup folder (then start download)"));
		y += 22;
		this.buttonList.add(new GuiButton(2, x, y,
				"Allow overwriting (start download)"));
		y += 22;
		this.buttonList.add(new GuiButton(3, x, y,
				"Cancel (don't start download)"));
		
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
			
			drawCenteredString(fontRendererObj, 
					"The world may have been changed.  Overwrite changes?",
					width / 2, height / 4 - 40, 0xFFFFFF);
			drawCenteredString(fontRendererObj, "Last downloaded on " + savedText
					+ ", last played " + playedText,
					width / 2, height / 4 - 10, 0xFFFFFF);
			
			super.drawScreen(mouseX, mouseY, partialTicks);
		}
	}
}
