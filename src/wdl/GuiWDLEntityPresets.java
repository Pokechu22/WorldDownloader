package wdl;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;

/**
 * Provides fast setting for various entity options.
 */
public class GuiWDLEntityPresets extends GuiScreen implements GuiYesNoCallback {
	private final GuiScreen parent;
	
	private GuiButton vanillaButton;
	private GuiButton spigotButton;
	private GuiButton serverButton;
	private GuiButton allOtherButton;
	private GuiButton noOtherButton;
	private GuiButton allPassiveButton;
	private GuiButton noPassiveButton;
	private GuiButton allHostileButton;
	private GuiButton noHostileButton;
	private GuiButton okButton;
	
	public GuiWDLEntityPresets(GuiScreen parent) {
		this.parent = parent;
	}
	
	@Override
	public void initGui() {
		int y = this.height / 4;
		
		this.vanillaButton = new GuiButton(0, this.width / 2 - 230, y, 150, 20,
				"Vanilla minecraft ranges");
		this.spigotButton = new GuiButton(1, this.width / 2 - 75, y, 150, 20,
				"Default spigot ranges");
		this.serverButton = new GuiButton(2, this.width / 2 + 80, y, 150, 20,
				"Server-configured ranges");
		
		serverButton.enabled = WDLPluginChannels.hasServerEntityRange();
		
		this.buttonList.add(vanillaButton);
		this.buttonList.add(spigotButton);
		this.buttonList.add(serverButton);
		
		y += 28 + 2 + mc.fontRendererObj.FONT_HEIGHT;
		
		this.allOtherButton = new GuiButton(3, this.width / 2 - 230, y, 150, 20,
				"Enable all other entities");
		this.allPassiveButton = new GuiButton(4, this.width / 2 - 75, y, 150, 20,
				"Enable all passive entities");
		this.allHostileButton = new GuiButton(5, this.width / 2 + 80, y, 150, 20,
				"Enable all hostile entities");
		
		y += 22;
		
		this.noOtherButton = new GuiButton(6, this.width / 2 - 230, y, 150, 20,
				"Disable all other entities");
		this.noPassiveButton = new GuiButton(7, this.width / 2 - 75, y, 150, 20,
				"Disable all passive entities");
		this.noHostileButton = new GuiButton(8, this.width / 2 + 80, y, 150, 20,
				"Disable all hostile entities");
		
		this.buttonList.add(allOtherButton);
		this.buttonList.add(allPassiveButton);
		this.buttonList.add(allHostileButton);
		this.buttonList.add(noOtherButton);
		this.buttonList.add(noPassiveButton);
		this.buttonList.add(noHostileButton);
		
		y += 28 + 2 + mc.fontRendererObj.FONT_HEIGHT;
		
		this.okButton = new GuiButton(100, this.width / 2 - 100, y, "OK");
		this.buttonList.add(okButton);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (!button.enabled) {
			return;
		}
		
		if (button.id < 3) {
			mc.displayGuiScreen(new GuiYesNo(this,
					"Are you sure you want to reset your entity ranges?",
					"(Setting to " + button.displayString + ")", button.id));
		}
		if (button.id == 100) {
			mc.displayGuiScreen(parent);
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawBackground(0);
		
		this.drawCenteredString(this.fontRendererObj, "§n§lEntity presets",
				this.width / 2, this.height / 4 - 40, 0xFFFFFF);
		this.drawCenteredString(this.fontRendererObj, "Ranges", this.width / 2,
				this.height / 4 - fontRendererObj.FONT_HEIGHT - 2, 0xFFFFFF);
		this.drawCenteredString(this.fontRendererObj, "Enable / Disable",
				this.width / 2, this.height / 4 + 28, 0xFFFFFF);
		
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
		//TODO: Entity button mouse overs.
		
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
