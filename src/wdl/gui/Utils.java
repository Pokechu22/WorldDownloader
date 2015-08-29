package wdl.gui;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
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
		if (text == null) {
			return;
		}
		
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
	
	/**
	 * Draws the border used by list GUIs.
	 * <br/> 
	 * Based off of
	 * {@link net.minecraft.client.gui.GuiSlot#drawScreen(int, int, float)}.
	 * 
	 * Note that there is an additional 4-pixel padding on the margins for the gradient.
	 * 
	 * @param topMargin Amount of space to give for the upper box.
	 * @param bottomMargin Amount of space to give for the lower box.
	 * @param top Where to start drawing (usually, 0)
	 * @param left Where to start drawing (usually, 0)
	 * @param bottom Where to stop drawing (usually, height).
	 * @param right Where to stop drawing (usually, width)
	 */
	public static void drawBorder(int topMargin, int bottomMargin, int top, int left, int bottom, int right) {
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		Tessellator t = Tessellator.getInstance();
		WorldRenderer wr = t.getWorldRenderer();
		//wr.func_178991_c sets the color.
		//wr.func_178974_a sets the color and the alpha.
		
		mc.getTextureManager().bindTexture(Gui.optionsBackground);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		
		float textureSize = 32.0F;
		wr.startDrawingQuads();
		wr.func_178991_c(0x202020);
		wr.addVertexWithUV(0, bottom, 0, 0 / textureSize, 
				bottom / textureSize);
		wr.addVertexWithUV(right, bottom, 0, right / textureSize, 
				bottom / textureSize);
		wr.addVertexWithUV(right, top, 0, right / textureSize, 
				top / textureSize);
		wr.addVertexWithUV(left, top, 0, left / textureSize, 
				top / textureSize);
		t.draw();
		
		GlStateManager.disableDepth();
		byte padding = 4;
		
		//Box code is GuiSlot.overlayBackground
		//Upper box
		int upperBoxEnd = top + topMargin;

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		wr.startDrawingQuads();
		wr.func_178974_a(0x404040, 255);
		wr.addVertexWithUV(left, upperBoxEnd, 0.0D, 0.0D, upperBoxEnd
				/ textureSize);
		wr.addVertexWithUV(right, upperBoxEnd, 0.0D, right / textureSize,
				upperBoxEnd / textureSize);
		wr.func_178974_a(0x404040, 255);
		wr.addVertexWithUV(right, top, 0.0D, right / textureSize, top
				/ textureSize);
		wr.addVertexWithUV(left, top, 0.0D, 0.0D, top / textureSize);
		t.draw();

		// Lower box
		int lowerBoxStart = bottom - bottomMargin;

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		wr.startDrawingQuads();
		wr.func_178974_a(0x404040, 255);
		wr.addVertexWithUV(left, bottom, 0.0D, 0.0D, bottom / textureSize);
		wr.addVertexWithUV(right, bottom, 0.0D, right / textureSize, bottom
				/ textureSize);
		wr.func_178974_a(0x404040, 255);
		wr.addVertexWithUV(right, lowerBoxStart, 0.0D, right / textureSize,
				lowerBoxStart / textureSize);
		wr.addVertexWithUV(left, lowerBoxStart, 0.0D, 0.0D, lowerBoxStart
				/ textureSize);
		t.draw();
		
		//Gradients
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA,
				GL_ONE_MINUS_SRC_ALPHA, 0, 1);
		GlStateManager.disableAlpha();
		GlStateManager.shadeModel(GL_SMOOTH);
		GlStateManager.func_179090_x();
		wr.startDrawingQuads();
		wr.func_178974_a(0, 0);
		wr.addVertexWithUV(left, upperBoxEnd + padding, 0.0D, 0.0D, 1.0D);
		wr.addVertexWithUV(right, upperBoxEnd + padding, 0.0D, 1.0D, 1.0D);
		wr.func_178974_a(0, 255);
		wr.addVertexWithUV(right, upperBoxEnd, 0.0D, 1.0D, 0.0D);
		wr.addVertexWithUV(left, upperBoxEnd, 0.0D, 0.0D, 0.0D);
		t.draw();
		wr.startDrawingQuads();
		wr.func_178974_a(0, 255);
		wr.addVertexWithUV(left, lowerBoxStart, 0.0D, 0.0D, 1.0D);
		wr.addVertexWithUV(right, lowerBoxStart, 0.0D, 1.0D, 1.0D);
		wr.func_178974_a(0, 0);
		wr.addVertexWithUV(right, lowerBoxStart - padding, 0.0D, 1.0D, 0.0D);
		wr.addVertexWithUV(left, lowerBoxStart - padding, 0.0D, 0.0D, 0.0D);
		t.draw();
		
		GlStateManager.func_179098_w();
		GlStateManager.shadeModel(GL_FLAT);
		GlStateManager.enableAlpha();
		GlStateManager.disableBlend();
	}
	
	/**
	 * Is the mouse over the given text box?
	 * @param mouseX The mouse's current (scaled) x.
	 * @param mouseY The mouse's current (scaled) y.
	 * @param textBox The text box.
	 * @return Whether the mouse is over the given text box.
	 */
	public static boolean isMouseOverTextBox(int mouseX, int mouseY,
			GuiTextField textBox) {
		int scaledX = mouseX - textBox.xPosition;
		int scaledY = mouseY - textBox.yPosition;
		
		// Standard text box height -- there is no actual getter for the real
		// one.
		final int height = 20;

		return scaledX >= 0 && scaledX < textBox.getWidth() && scaledY >= 0
				&& scaledY < height;
	}
}
