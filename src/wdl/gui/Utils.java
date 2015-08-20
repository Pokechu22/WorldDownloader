package wdl.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.ChatComponentText;

class Utils {
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	/**
	 * Draws a transparent description box.
	 * 
	 * @param text Text to display.  Takes \n into consideration.
	 * @param width Width of the GUI.
	 * @param height Height of the GUI.
	 */
	public static void drawGuiInfoBox(String text, int width, int height) {
		int infoX = width / 2 - 150;
		int infoY = 2 * height / 3;
		int y = infoY + 5;
		
		GuiScreen.drawRect(infoX, infoY, infoX + 300, infoY + 100, 0x7F000000);
		
		List<String> lines = wordWrap(text, 290);
		
		for (String s : lines) {
			mc.fontRendererObj.drawString(s, infoX + 5, y, 0xFFFFFF);
			y += mc.fontRendererObj.FONT_HEIGHT;
		}
	}
	
	/**
	 * Converts a string into a list of lines that are each shorter than the 
	 * given width.  Takes \n into consideration.
	 * 
	 * @param s The string to word wrap.
	 * @param width The width to use.
	 * @return A list of lines.
	 */
	public static List<String> wordWrap(String s, int width) {
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
				new ChatComponentText(s), width, mc.fontRendererObj, true, true);
		
		List<String> returned = new ArrayList<String>();
		for (ChatComponentText component : texts) {
			returned.add(component.getFormattedText());
		}
		
		return returned;
	}
}
