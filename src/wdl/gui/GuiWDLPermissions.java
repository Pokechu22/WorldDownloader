package wdl.gui;

import java.io.IOException;

import wdl.WDL;
import wdl.WDLPluginChannels;
import wdl.WorldBackup.WorldBackupType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/**
 * GUI that shows the current permissions for the user.
 */
public class GuiWDLPermissions extends GuiScreen {
	private final GuiScreen parent;
	
	public GuiWDLPermissions(GuiScreen parent) {
		this.parent = parent;
	}
	
	@Override
	public void initGui() {
		this.buttonList.add(new GuiButton(100, width / 2 - 100, height - 29,
				"Done"));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 100) {
			this.mc.displayGuiScreen(this.parent);
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		Utils.drawBorder(23, 32, 0, 0, height, width);
		
		this.drawCenteredString(this.fontRendererObj, "Permission info",
				this.width / 2, 8, 0xFFFFFF);
		
		if (!WDLPluginChannels.hasPermissions()) {
			this.drawCenteredString(this.fontRendererObj,
					"No permissions loaded.", this.width / 2, this.height / 2,
					0xFFFFFF);
		} else {
			this.drawCenteredString(this.fontRendererObj,
					"Some permissions loaded.", this.width / 2,
					this.height / 2, 0xFFFFFF);
		}
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
