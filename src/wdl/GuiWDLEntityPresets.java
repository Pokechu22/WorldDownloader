package wdl;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;

/**
 * Allows setting the current user range for each entity.
 */
public class GuiWDLEntityPresets extends GuiScreen implements GuiYesNoCallback {
	private final GuiScreen parent;
	
	private GuiButton vanillaButton;
	private GuiButton spigotButton;
	private GuiButton serverButton;
	private GuiButton okButton;
	
	public GuiWDLEntityPresets(GuiScreen parent) {
		this.parent = parent;
	}
	
	@Override
	public void initGui() {
		int y = this.height / 4 - 15;
		
		this.vanillaButton = new GuiButton(0, this.width / 2 - 155, y, 150, 20,
				"Vanilla minecraft ranges");
		this.buttonList.add(vanillaButton);
		
		this.spigotButton = new GuiButton(1, this.width / 2 + 5, y, 150, 20,
				"Default spigot ranges");
		this.buttonList.add(spigotButton);
		
		y += 22;
		
		this.serverButton = new GuiButton(2, this.width / 2 - 155, y, 150, 20,
				"Server-configured ranges");
		serverButton.enabled = WDLPluginChannels.hasServerEntityRange();
		this.buttonList.add(serverButton);
		
		y += 28;
		this.okButton = new GuiButton(100, this.width / 2 - 100, y, "OK");
		this.buttonList.add(okButton);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (!button.enabled) {
			return;
		}
		
		if (button.id < 100) {
			mc.displayGuiScreen(new GuiYesNo(this,
					"Are you sure you want to change your entity ranges?",
					"(Setting to " + button.displayString + ")", button.id));
		}
		if (button.id == 100) {
			mc.displayGuiScreen(parent);
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawBackground(0);
		
		this.drawCenteredString(this.fontRendererObj, "Entity range presets",
				this.width / 2, this.height / 4 - 40, 0xFFFFFF);
		
		int infoX = this.width / 2 - 150;
		int infoY = 2 * this.height / 3;
		int y = infoY + 5;
		
		if (vanillaButton.isMouseOver()) {
			drawRect(infoX, infoY, infoX + 300, infoY + 100, 0x7F000000);
			
			drawString(fontRendererObj, "Set the ranges to those used by " +
					"an unmodified (vanilla)", infoX + 5, y, 0xFFFFFF);
			y += fontRendererObj.FONT_HEIGHT;
			drawString(fontRendererObj, "Minecraft server, and " +
					"for some Bukkit servers.", infoX + 5, y, 0xFFFFFF);
		} else if (spigotButton.isMouseOver()) {
			drawRect(infoX, infoY, infoX + 300, infoY + 100, 0x7F000000);
			
			drawString(fontRendererObj, "Set the ranges to the default " +
					"values for spigot servers.", infoX + 5, y, 0xFFFFFF);
			y += fontRendererObj.FONT_HEIGHT;
			drawString(fontRendererObj, "Spigot allows these values to be " +
					"customized, but these",
					infoX + 5, y, 0xFFFFFF);
			y += fontRendererObj.FONT_HEIGHT;
			drawString(fontRendererObj, "are the defaults.",
					infoX + 5, y, 0xFFFFFF);
		} else if (serverButton.isMouseOver()) {
			drawRect(infoX, infoY, infoX + 300, infoY + 100, 0x7F000000);
			
			drawString(fontRendererObj, "Set the ranges to the values " +
					"that are actually used on", infoX + 5, y, 0xFFFFFF);
			y += fontRendererObj.FONT_HEIGHT;
			drawString(fontRendererObj, "the server.  This option is only " +
					"available if the server", infoX + 5, y, 0xFFFFFF);
			y += fontRendererObj.FONT_HEIGHT;
			drawString(fontRendererObj, "has the WDLCompanion " +
					"plugin installed.", infoX + 5, y, 0xFFFFFF);
			y += fontRendererObj.FONT_HEIGHT;
			y += fontRendererObj.FONT_HEIGHT;
			if (serverButton.enabled) {
				drawString(fontRendererObj, "§aAs it is installed, this may " +
						"be used.", infoX + 5, y, 0xFFFFFF);
			} else {
				drawString(fontRendererObj, "§cAs it is not installed, this " +
						"cannot be used.", infoX + 5, y, 0xFFFFFF);
			}
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
							EntityUtils.getVanillaEntityRange(entity)));
				}
			}
			if (id == 1) {
				for (String entity : entities) {
					WDL.worldProps.setProperty("Entity." + entity
							+ ".TrackDistance", Integer.toString(
							EntityUtils.getSpigotEntityRange(entity)));
				}
			}
			if (id == 2) { 
				for (String entity : entities) {
					WDL.worldProps.setProperty("Entity." + entity
							+ ".TrackDistance", Integer.toString(
							WDLPluginChannels.getEntityRange(entity)));
				}
			}
		}
		
		mc.displayGuiScreen(this);
	}
}
