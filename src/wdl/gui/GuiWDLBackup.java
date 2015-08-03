package wdl.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import wdl.WDL;
import wdl.WDLDebugMessageCause;
import wdl.WorldBackup;
import wdl.WorldBackup.WorldBackupType;

/**
 * GUI allowing control over the way the world is backed up.
 */
public class GuiWDLBackup extends GuiScreen {
	private GuiScreen parent;
	
	private WorldBackupType backupType;
	
	public GuiWDLBackup(GuiScreen parent) {
		this.parent = parent;
	}
	
	@Override
	public void initGui() {
		backupType = WorldBackupType.match(
				WDL.baseProps.getProperty("Backup", "ZIP"));
		
		int x = (this.width / 2) - 100;
		int y = (this.height / 4) - 15;
		
		this.buttonList.add(new GuiButton(0, x, y, 
				"Backup mode: " + backupType.description));
		
		WDLDebugMessageCause[] causes = WDLDebugMessageCause.values();
		
		y += 28;
		
		this.buttonList.add(new GuiButton(100, x, y, "Done"));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (!button.enabled) {
			return;
		}
		
		if (button.id == 0) { //Backup mode
			switch (backupType) {
			case NONE: backupType = WorldBackupType.FOLDER; break;
			case FOLDER: backupType = WorldBackupType.ZIP; break;
			case ZIP: backupType = WorldBackupType.NONE; break;
			}
			
			button.displayString = "Backup mode: " + backupType.description;
		} else if (button.id == 100) { //Done
			this.mc.displayGuiScreen(this.parent);
		}
	}
	
	@Override
	public void onGuiClosed() {
		WDL.baseProps.setProperty("Backup", backupType.name());
		
		WDL.saveProps();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRendererObj, "Backup options",
				this.width / 2, this.height / 4 - 40, 0xFFFFFF);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
