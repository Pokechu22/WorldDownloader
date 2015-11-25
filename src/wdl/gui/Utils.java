package wdl.gui;

import static org.lwjgl.opengl.GL11.GL_FLAT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;

class Utils {
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	/**
	 * Draws a semitransparent description box.
	 * 
	 * @param text
	 *            Text to display. Takes \n into consideration.
	 * @param guiWidth
	 *            Width of the GUI.
	 * @param guiHeight
	 *            Height of the GUI.
	 * @param bottomPadding
	 *            The amount of space to put below the bottom of the info box.
	 */
	public static void drawGuiInfoBox(String text, int guiWidth, int guiHeight,
			int bottomPadding) {
		drawGuiInfoBox(text, 300, 100, guiWidth, guiHeight, bottomPadding);
	}

	/**
	 * Draws a semitransparent description box.
	 * 
	 * @param text
	 *            Text to display. Takes \n into consideration.
	 * @param infoBoxWidth
	 *            The width of the info box.
	 * @param infoBoxHeight
	 *            The height of the info box.
	 * @param guiWidth
	 *            Width of the GUI.
	 * @param guiHeight
	 *            Height of the GUI.
	 * @param bottomPadding
	 *            The amount of space to put below the bottom of the info box.
	 */
	public static void drawGuiInfoBox(String text, int infoBoxWidth,
			int infoBoxHeight, int guiWidth, int guiHeight, int bottomPadding) {
		if (text == null) {
			return;
		}
		
		int infoX = guiWidth / 2 - infoBoxWidth / 2;
		int infoY = guiHeight - bottomPadding - infoBoxHeight;
		int y = infoY + 5;
		
		GuiScreen.drawRect(infoX, infoY, infoX + infoBoxWidth, infoY
				+ infoBoxHeight, 0x7F000000);
		
		List<String> lines = wordWrap(text, infoBoxWidth - 10);
		
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
		s = s.replace("\r", ""); // If we got a \r\n in the text somehow, remove it.
		
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
		@SuppressWarnings("unchecked")
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
	public static void drawListBackground(int topMargin, int bottomMargin, int top, int left, int bottom, int right) {
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
		
		drawBorder(topMargin, bottomMargin, top, left, bottom, right);
		
		//Flags are reset by drawBorder
	}
	
	/**
	 * Draws the top and bottom borders found on gui lists (but no background).
	 * <br/> 
	 * Based off of
	 * {@link net.minecraft.client.gui.GuiSlot#overlayBackground(int, int, int, int)}.
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
		GlStateManager.disableDepth();
		byte padding = 4;
		
		mc.getTextureManager().bindTexture(Gui.optionsBackground);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		
		float textureSize = 32.0F;
		
		Tessellator t = Tessellator.getInstance();
		WorldRenderer wr = t.getWorldRenderer();
		
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

/**
 * A slider that doesn't require a bunch of interfaces to work.
 * 
 * Based off of {@link net.minecraft.client.gui.GuiOptionSlider}.
 */
class GuiSlider extends GuiButton {
	private float sliderValue;
	private boolean dragging;
	/**
	 * I18n key for this slider.
	 */
	private final String text;
	/**
	 * Maximum value for the slider.
	 */
	private final int max;

	public GuiSlider(int id, int x, int y, int width, int height, 
			String text, int value, int max) {
		super(id, x, y, width, height, text);
		
		this.text = text;
		this.max = max;
		
		setValue(value);
	}

	/**
	 * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over
	 * this button and 2 if it IS hovering over this button.
	 */
	@Override
	protected int getHoverState(boolean mouseOver) {
		return 0;
	}

	/**
	 * Fired when the mouse button is dragged. Equivalent of
	 * MouseListener.mouseDragged(MouseEvent e).
	 */
	@Override
	protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible) {
			if (this.dragging) {
				this.sliderValue = (float)(mouseX - (this.xPosition + 4))
						/ (float)(this.width - 8);
				this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F,
						1.0F);
				this.dragging = true;
				
				this.displayString = I18n.format(text, getValue());
			}

			mc.getTextureManager().bindTexture(buttonTextures);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			
			if (this.enabled) {
				this.drawTexturedModalRect(this.xPosition
						+ (int) (this.sliderValue * (this.width - 8)),
						this.yPosition, 0, 66, 4, 20);
				this.drawTexturedModalRect(this.xPosition
						+ (int) (this.sliderValue * (this.width - 8))
						+ 4, this.yPosition, 196, 66, 4, 20);
			} else {
				this.drawTexturedModalRect(this.xPosition
						+ (int) (this.sliderValue * (this.width - 8)),
						this.yPosition, 0, 46, 4, 20);
				this.drawTexturedModalRect(this.xPosition
						+ (int) (this.sliderValue * (this.width - 8))
						+ 4, this.yPosition, 196, 46, 4, 20);
			}
		}
	}

	/**
	 * Returns true if the mouse has been pressed on this control. Equivalent of
	 * MouseListener.mousePressed(MouseEvent e).
	 */
	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			this.sliderValue = (float)(mouseX - (this.xPosition + 4))
					/ (float)(this.width - 8);
			this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F,
					1.0F);
			this.displayString = I18n.format(text, getValue());
			
			this.dragging = true;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Gets the current value of the slider.
	 * @return
	 */
	public int getValue() {
		return (int)(sliderValue * max);
	}
	
	/**
	 * Gets the current value of the slider.
	 * @return
	 */
	public void setValue(int value) {
		this.sliderValue = value / (float)max;
		
		this.displayString = I18n.format(text, getValue());
	}
	
	/**
	 * Fired when the mouse button is released. Equivalent of
	 * MouseListener.mouseReleased(MouseEvent e).
	 */
	@Override
	public void mouseReleased(int mouseX, int mouseY) {
		this.dragging = false;
	}
}

/**
 * {@link GuiTextField} that only accepts numbers.
 */
class GuiNumericTextField extends GuiTextField {
	public GuiNumericTextField(int id, FontRenderer fontRenderer,
			int x, int y, int width, int height) {
		super(id, fontRenderer, x, y, width,
				height);
		setText("0");
	}
	
	/**
	 * Last text that was successfully entered.
	 */
	private String lastSafeText = "0";
	
	@Override
	public void drawTextBox() {
		// Save last safe text.
		try {
			Integer.parseInt("0" + getText());
			lastSafeText = getText();
		} catch (NumberFormatException e) {
			setText(lastSafeText);
		}
		
		super.drawTextBox();
	}
	
	/**
	 * Gets the current value.
	 * @return
	 */
	public int getValue() {
		try {
			return Integer.parseInt("0" + getText());
		} catch (NumberFormatException e) {
			// Should not happen, hopefully.
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * Sets the value.
	 * @param value
	 * @return
	 */
	public void setValue(int value) {
		String text = String.valueOf(value);
		lastSafeText = text;
		setText(text);
	}
	
	@Override
	public String getText() {
		String text = super.getText();
		
		try {
			int value = Integer.parseInt("0" + text);
			return String.valueOf(value);
		} catch (NumberFormatException e) {
			setText(lastSafeText);
			return lastSafeText;
		}
	}
	
	@Override
	public void setText(String text) {
		String value;
		
		try {
			value = String.valueOf(Integer.parseInt("0" + text));
		} catch (NumberFormatException e) {
			value = lastSafeText;
		}
		
		super.setText(value);
		lastSafeText = value;
	}
}