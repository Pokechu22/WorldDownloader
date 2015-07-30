package wdl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.lwjgl.input.Keyboard;

import wdl.WorldBackup.WorldBackupType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/**
 * GUI shown before possibly overwriting data in the world.
 */
public class GuiWDLOverwriteChanges extends GuiScreen {
	private static final DateFormat displayDateFormat = 
			new SimpleDateFormat();
	
	private class BackupThread extends Thread {
		private final DateFormat folderDateFormat = 
				new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		
		@Override
		public void run() {
			try {
				backingUp = true;
				
				String backupName = WDL.worldName + "_"
						+ folderDateFormat.format(new Date());
				
				boolean folderBackup = WDL.baseProps.getProperty("Backup",
						"ZIP").equals("FOLDER");
				
				backupData = "Backing up to " + (folderBackup ? "zip " : " ")
						+ "folder " + backupName + ".";
				
				WorldBackupType type = folderBackup ? WorldBackupType.FOLDER
						: WorldBackupType.ZIP; 
				WorldBackup.backupWorld(WDL.saveHandler.getWorldDirectory(),
						WDL.getWorldFolderName(WDL.worldName), type);
			} catch (Exception e) {
				WDL.chatError("Exception while backing up world: " + e);
				e.printStackTrace();
			} finally {
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
		int y = (this.height / 4) - 15;
		
		this.buttonList.add(new GuiButton(0, x, y,
				"Backup first (then start download)"));
		y += 22;
		this.buttonList.add(new GuiButton(1, x, y,
				"Allow overwriting (start download)"));
		y += 22;
		this.buttonList.add(new GuiButton(2, x, y,
				"Cancel (don't start download)"));
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (backingUp && keyCode == Keyboard.KEY_ESCAPE) {
			//Don't allow closing while a backup is underway. 
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
			new BackupThread().run();
		}
		if (button.id == 1) {
			WDL.overrideLastModifiedCheck = true;
			mc.displayGuiScreen(null);
			
			WDL.start();
		}
		if (button.id == 2) {
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
			//TODO: Better description (it's last played, not last modified).
			drawCenteredString(fontRendererObj, 
					"The world appears to have been changed.  Overwrite?",
					width / 2, height / 4 - 40, 0xFFFFFF);
			drawCenteredString(fontRendererObj, "Saved on " + savedText
					+ ", last played " + playedText,
					width / 2, height / 4 - 10, 0xFFFFFF);
			
			super.drawScreen(mouseX, mouseY, partialTicks);
		}
	}
}
