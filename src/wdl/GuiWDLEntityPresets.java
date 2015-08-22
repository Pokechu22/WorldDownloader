package wdl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

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
	protected void actionPerformed(GuiButton button) {
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
				
				if (id % 3 == 0) {
					entities = EntityUtils.otherEntityList;
				} else if (id % 3 == 1) {
					entities = EntityUtils.passiveEntityList;
				} else if (id % 3 == 2) {
					entities = EntityUtils.hostileEntityList;
				}
				
				for (String entity : entities) {
					WDL.worldProps.setProperty("Entity." + entity
							+ ".Enabled", value);
				}
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
		List<ChatComponentText> texts = func_178908_a(
				new ChatComponentText(s), width, fontRendererObj, true, true);
		
		List<String> returned = new ArrayList<String>();
		for (ChatComponentText component : texts) {
			returned.add(component.getFormattedText());
		}
		
		return returned;
	}
	
	//Taken from net.minecraft.client.gui.GuiUtilRenderComponents
	private static String func_178909_a(String p_178909_0_, boolean p_178909_1_) {
		return !p_178909_1_
			   && !Minecraft.getMinecraft().gameSettings.chatColours ? EnumChatFormatting
			   .getTextWithoutFormattingCodes(p_178909_0_) : p_178909_0_;
	}
	
	private static List func_178908_a(IChatComponent p_178908_0_,
			int p_178908_1_, FontRenderer p_178908_2_, boolean p_178908_3_,
			boolean p_178908_4_) {
		int var5 = 0;
		ChatComponentText var6 = new ChatComponentText("");
		ArrayList var7 = Lists.newArrayList();
		ArrayList var8 = Lists.newArrayList(p_178908_0_);

		for (int var9 = 0; var9 < var8.size(); ++var9) {
			IChatComponent var10 = (IChatComponent) var8.get(var9);
			String var11 = var10.getUnformattedTextForChat();
			boolean var12 = false;
			String var14;

			if (var11.contains("\n")) {
				int var13 = var11.indexOf(10);
				var14 = var11.substring(var13 + 1);
				var11 = var11.substring(0, var13 + 1);
				ChatComponentText var15 = new ChatComponentText(var14);
				var15.setChatStyle(var10.getChatStyle().createShallowCopy());
				var8.add(var9 + 1, var15);
				var12 = true;
			}

			String var21 = func_178909_a(var10.getChatStyle()
					.getFormattingCode() + var11, p_178908_4_);
			var14 = var21.endsWith("\n") ? var21.substring(0,
					var21.length() - 1) : var21;
			int var22 = p_178908_2_.getStringWidth(var14);
			ChatComponentText var16 = new ChatComponentText(var14);
			var16.setChatStyle(var10.getChatStyle().createShallowCopy());

			if (var5 + var22 > p_178908_1_) {
				String var17 = p_178908_2_.trimStringToWidth(var21, p_178908_1_
						- var5, false);
				String var18 = var17.length() < var21.length() ? var21
						.substring(var17.length()) : null;

				if (var18 != null && var18.length() > 0) {
					int var19 = var17.lastIndexOf(" ");

					if (var19 >= 0
							&& p_178908_2_.getStringWidth(var21.substring(0,
									var19)) > 0) {
						var17 = var21.substring(0, var19);

						if (p_178908_3_) {
							++var19;
						}

						var18 = var21.substring(var19);
					} else if (var5 > 0 && !var21.contains(" ")) {
						var17 = "";
						var18 = var21;
					}

					ChatComponentText var20 = new ChatComponentText(var18);
					var20.setChatStyle(var10.getChatStyle().createShallowCopy());
					var8.add(var9 + 1, var20);
				}

				var22 = p_178908_2_.getStringWidth(var17);
				var16 = new ChatComponentText(var17);
				var16.setChatStyle(var10.getChatStyle().createShallowCopy());
				var12 = true;
			}

			if (var5 + var22 <= p_178908_1_) {
				var5 += var22;
				var6.appendSibling(var16);
			} else {
				var12 = true;
			}

			if (var12) {
				var7.add(var6);
				var5 = 0;
				var6 = new ChatComponentText("");
			}
		}

		var7.add(var6);
		return var7;
	}
}
