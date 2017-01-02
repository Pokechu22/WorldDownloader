package wdl.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import wdl.WDL;
import wdl.WorldBackup.WorldBackupType;

/**
 * GUI allowing control over the way the world is backed up.
 */
public class GuiWDLBackup extends GuiScreen {
	private GuiScreen parent;
	
	private String description;
	
	private WorldBackupType backupType;
	
	public GuiWDLBackup(GuiScreen parent) {
		this.parent = parent;
		
		this.description = I18n.format("wdl.gui.backup.description1") + "\n\n"
				+ I18n.format("wdl.gui.backup.description2") + "\n\n"
				+ I18n.format("wdl.gui.backup.description3");
	}
	
	@Override
	public void initGui() {
		backupType = WorldBackupType.match(
				WDL.baseProps.getProperty("Backup", "ZIP"));
		
		this.buttonList.add(new GuiButton(0, this.width / 2 - 100, 32,
				getBackupButtonText()));
		
		this.buttonList.add(new GuiButton(100, this.width / 2 - 100,
				height - 29, I18n.format("gui.done")));
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
			
			button.displayString = getBackupButtonText();
		} else if (button.id == 100) { //Done
			this.mc.displayGuiScreen(this.parent);
		}
	}
	
	private String getBackupButtonText() {
		return I18n.format("wdl.gui.backup.backupMode",
				backupType.getDescription());
	}
	
	@Override
	public void onGuiClosed() {
		WDL.baseProps.setProperty("Backup", backupType.name());
		
		WDL.saveProps();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		Utils.drawListBackground(23, 32, 0, 0, height, width);
		
		this.drawCenteredString(this.fontRendererObj,
				I18n.format("wdl.gui.backup.title"), this.width / 2, 8,
				0xFFFFFF);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		Utils.drawGuiInfoBox(description, width - 50, 3 * this.height / 5, width,
				height, 48);
	}
}
