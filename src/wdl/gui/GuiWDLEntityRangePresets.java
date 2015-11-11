package wdl.gui;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import wdl.EntityUtils;
import wdl.WDL;
import wdl.WDLPluginChannels;

/**
 * Provides fast setting for various entity options.
 */
public class GuiWDLEntityRangePresets extends GuiScreen implements GuiYesNoCallback {
	private final GuiScreen parent;
	
	private GuiButton vanillaButton;
	private GuiButton spigotButton;
	private GuiButton serverButton;
	private GuiButton cancelButton;
	
	public GuiWDLEntityRangePresets(GuiScreen parent) {
		this.parent = parent;
	}
	
	@Override
	public void initGui() {
		int y = this.height / 4;
		
		this.vanillaButton = new GuiButton(0, this.width / 2 - 100, y,
				"Vanilla minecraft ranges");
		y += 22;
		this.spigotButton = new GuiButton(1, this.width / 2 - 100, y,
				"Default spigot ranges");
		y += 22;
		this.serverButton = new GuiButton(2, this.width / 2 - 100, y,
				"Server-configured ranges");
		
		serverButton.enabled = WDLPluginChannels.hasServerEntityRange();
		
		this.buttonList.add(vanillaButton);
		this.buttonList.add(spigotButton);
		this.buttonList.add(serverButton);
		
		y += 28;
		
		this.cancelButton = new GuiButton(100, this.width / 2 - 100, y, "Cancel");
		this.buttonList.add(cancelButton);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (!button.enabled) {
			return;
		}
		
		if (button.id < 3) {
			String upper;
			String lower;
			
			upper = "Are you sure you want to reset your entity ranges?";
			
			if (button.id == 0) {
				lower = "§rSetting to vanilla Minecraft ranges -- " +
						"§c§n§lThis cannot be undone.§r";
			} else if (button.id == 1) {
				lower = "§rSetting to default spigot ranges -- " +
						"§c§n§lThis cannot be undone.§r";
			} else if (button.id == 2) {
				lower = "§rSetting to server configured ranges -- " +
						"§c§n§lThis cannot be undone.§r";
			} else {
				//Should not happen.
				throw new Error("Button.id should never be negative.");
			}
			
			mc.displayGuiScreen(new GuiYesNo(this, upper, lower, button.id));
		}
		
		if (button.id == 100) {
			mc.displayGuiScreen(parent);
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawBackground(0);
		
		this.drawCenteredString(this.fontRendererObj, "Range presets",
				this.width / 2, this.height / 4 - fontRendererObj.FONT_HEIGHT
						- 2, 0xFFFFFF);
		
		String infoText = null;
		
		if (vanillaButton.isMouseOver()) {
			infoText = "Set the ranges to those used by " +
					"an unmodified (vanilla) Minecraft server, and " +
					"for some Bukkit servers.";
		} else if (spigotButton.isMouseOver()) {
			infoText = "Set the ranges to the default values for spigot " +
					"servers.\nSpigot allows these values to be " +
					"customized, but these are the defaults.";
		} else if (serverButton.isMouseOver()) {
			infoText = "Set the ranges to the values that are actually used " +
					"on the server.  This option is only available if the " +
					"server has the WDLCompanion plugin installed.\n\n";
			
			if (serverButton.enabled) {
				infoText += "§aAs it is installed, this may be used.";
			} else {
				infoText += "§cAs it is not installed, this cannot be used.";
			}
		} else if (cancelButton.isMouseOver()) {
			infoText = "Cancel without making changes.";
		}
		
		if (infoText != null) {
			Utils.drawGuiInfoBox(infoText, width, height);
		}
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void confirmClicked(boolean result, int id) {
		if (result) {
			List<String> entities = EntityUtils.getEntityTypes();
			
			if (id == 0) {
				for (String entity : entities) {
					WDL.worldProps.setProperty("Entity." + entity
							+ ".TrackDistance", Integer.toString(
							EntityUtils.getDefaultEntityRange(entity)));
				}
			} else if (id == 1) {
				for (String entity : entities) {
					Class<?> c = EntityUtils.stringToClassMapping.get(entity);
					if (c == null) {
						continue;
					}
					
					WDL.worldProps.setProperty("Entity." + entity
							+ ".TrackDistance", Integer.toString(
							EntityUtils.getDefaultSpigotEntityRange(c)));
				}
			} else if (id == 2) { 
				for (String entity : entities) {
					WDL.worldProps.setProperty("Entity." + entity
							+ ".TrackDistance", Integer.toString(
							WDLPluginChannels.getEntityRange(entity)));
				}
			}
		}
		
		mc.displayGuiScreen(parent);
	}
	
	@Override
	public void onGuiClosed() {
		WDL.saveProps();
	}
}
