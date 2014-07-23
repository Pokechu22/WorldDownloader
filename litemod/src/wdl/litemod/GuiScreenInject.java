package wdl.litemod;

import wdl.WDL;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;

public class GuiScreenInject extends GuiIngameMenu {
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		// Your own code //
        if (!Minecraft.getMinecraft().isIntegratedServerRunning()) // If connected to a real server
        {
        	byte var1 = -16;
            GuiButton wdlDownload = new GuiButton(50, this.width / 2 - 100, this.height / 4 + 72 + var1, 170, 20, "WDL bug!");
            GuiButton wdlOptions = new GuiButton(51, this.width / 2 + 71, this.height / 4 + 72 + var1, 28, 20, "...");
            wdlDownload.displayString = (WDL.downloading ? (WDL.saving ? "Still saving..." : "Stop download") : "Download this world");
            wdlDownload.enabled = (!WDL.downloading || (WDL.downloading && !WDL.saving));
            wdlOptions.enabled = (!WDL.downloading || (WDL.downloading && !WDL.saving));
            buttonList.add(wdlDownload);
            buttonList.add(wdlOptions);
            ((GuiButton)buttonList.get(0)).yPosition = this.height / 4 + 144 + var1;
            ((GuiButton)buttonList.get(2)).yPosition = this.height / 4 + 120 + var1;
            ((GuiButton)buttonList.get(3)).yPosition = this.height / 4 + 120 + var1;
        }
	}
	
}
