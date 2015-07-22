package wdl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.util.ChatComponentText;

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
		
		if (button.id < 9) {
			String upper;
			String lower = "§c§n§lThis cannot be undone.§r";
			if (button.id < 3) {
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
				}
			} else {
				if (button.id < 6) { 
					upper = "Are you sure you want to enable all ";
				} else {
					upper = "Are you sure you want to disable all ";
				}
				
				if (button.id % 3 == 0) {
					upper += "other";
				} else if (button.id % 3 == 1) {
					upper += "passive";
				} else if (button.id % 3 == 2) {
					upper += "hostile";
				}
				
				upper += " entities?";
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
		
		this.drawCenteredString(this.fontRendererObj, "§n§lEntity presets",
				this.width / 2, this.height / 4 - 40, 0xFFFFFF);
		this.drawCenteredString(this.fontRendererObj, "Ranges", this.width / 2,
				this.height / 4 - fontRendererObj.FONT_HEIGHT - 2, 0xFFFFFF);
		this.drawCenteredString(this.fontRendererObj, "Enable / Disable",
				this.width / 2, this.height / 4 + 28, 0xFFFFFF);
		
		int infoX = this.width / 2 - 150;
		int infoY = 2 * this.height / 3;
		int y = infoY + 5;
		
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
		} else if (allOtherButton.isMouseOver()) {
			infoText = "Enable all `other' entities.\n\n(Other means " +
					"anything that is not an animal or a monster)";
		} else if (noOtherButton.isMouseOver()) {
			infoText = "Disable all `other' entities.\n\n(Other means " +
					"anything that is not an animal or a monster)";
		} else if (allPassiveButton.isMouseOver()) {
			infoText = "Enable all `pasive' entities.\n\n(Passive means " +
					"all animals, including bats and squids)";
		} else if (noPassiveButton.isMouseOver()) {
			infoText = "Disable all `pasive' entities.\n\n(Passive means " +
					"all animals, including bats and squids)";
		} else if (allHostileButton.isMouseOver()) {
			infoText = "Enable all `hostile' entities.\n\n(Hostile means " +
					"monsters)";
		} else if (noHostileButton.isMouseOver()) {
			infoText = "Disable all `hostile' entities.\n\n(Hostile means " +
					"monsters)";
		}
		
		if (infoText != null) {
			drawRect(infoX, infoY, infoX + 300, infoY + 100, 0x7F000000);
			
			List<String> info = paginate(infoText, 290);
			
			for (String s : info) {
				drawString(fontRendererObj, s, infoX + 5, y, 0xFFFFFF);
				y += fontRendererObj.FONT_HEIGHT;
			}
		}
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void confirmClicked(boolean result, int id) {
		if (result) {
			if (id < 3) {
				List<String> entities = EntityUtils.getEntityTypes();
				
				if (id == 0) {
					for (String entity : entities) {
						WDL.worldProps.setProperty("Entity." + entity
								+ ".TrackDistance", Integer.toString(
								EntityUtils.getVanillaEntityRange(entity)));
					}
				} else if (id == 1) {
					for (String entity : entities) {
						WDL.worldProps.setProperty("Entity." + entity
								+ ".TrackDistance", Integer.toString(
								EntityUtils.getSpigotEntityRange(entity)));
					}
				} else if (id == 2) { 
					for (String entity : entities) {
						WDL.worldProps.setProperty("Entity." + entity
								+ ".TrackDistance", Integer.toString(
								WDLPluginChannels.getEntityRange(entity)));
					}
				}
			} else {
				List<String> entities = null;
				String value = Boolean.toString(id < 6);
				
				//TODO
				/*if (id % 3 == 0) {
					entities = EntityUtils.otherEntityList;
				} else if (id % 3 == 1) {
					entities = EntityUtils.passiveEntityList;
				} else if (id % 3 == 2) {
					entities = EntityUtils.hostileEntityList;
				}
				
				for (String entity : entities) {
					WDL.worldProps.setProperty("Entity." + entity
							+ ".Enabled", value);
				}*/
			}
		}
		
		mc.displayGuiScreen(this);
	}
	
	/**
	 * Converts a string into a list of lines that are each shorter than the 
	 * given width.  Takes \n into consideration.
	 * 
	 * @return A list of lines.
	 */
	private List<String> paginate(String s, int width) {
		/**
		 * It's a method that formats and paginates text.
		 * 
		 * Args: 
		 * <ul>
		 * <li>The text to format.</li>
		 * <li>The width</li>
		 * <li>The font renderer.</li>
		 * <li>IDK</li>
		 * <li>Whether to include color codes.</li>
		 * </ul>
		 */
		List<ChatComponentText> texts = GuiUtilRenderComponents.func_178908_a(
				new ChatComponentText(s), width, fontRendererObj, true, true);
		
		List<String> returned = new ArrayList<String>();
		for (ChatComponentText component : texts) {
			returned.add(component.getFormattedText());
		}
		
		return returned;
	}
}
