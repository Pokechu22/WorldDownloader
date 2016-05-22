package wdl.gui;

import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLAT;
import static org.lwjgl.opengl.GL11.GL_FOG;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glShadeModel;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

class Utils {
	private static final Minecraft mc = Minecraft.getMinecraft();
	private static final Logger logger = LogManager.getLogger();
	
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
		
		@SuppressWarnings("unchecked")
		List<String> lines = mc.fontRendererObj.listFormattedStringToWidth(s, width);
		
		return lines;
	}
	
	/**
	 * Draws the background/border used by list GUIs.
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
		drawDarkBackground(top, left, bottom, right);
		drawBorder(topMargin, bottomMargin, top, left, bottom, right);
	}
	
	/**
	 * Draws a dark background, similar to {@link GuiScreen#drawBackground(int)} but darker.
	 * Same appearence as the background in lists. 
	 * 
	 * @param top Where to start drawing (usually, 0)
	 * @param left Where to start drawing (usually, 0)
	 * @param bottom Where to stop drawing (usually, height).
	 * @param right Where to stop drawing (usually, width)
	 */
	public static void drawDarkBackground(int top, int left, int bottom, int right) {
		glDisable(GL_LIGHTING);
		glDisable(GL_FOG);
		Tessellator t = Tessellator.instance;
		//t.setColorOpaque_I sets the color.
		//t.func_178974_a sets the color and the alpha.
		
		mc.getTextureManager().bindTexture(Gui.optionsBackground);
		glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		float textureSize = 32.0F;
		t.startDrawingQuads();
		t.setColorOpaque_I(0x202020);
		t.addVertexWithUV(0, bottom, 0, 0 / textureSize, 
				bottom / textureSize);
		t.addVertexWithUV(right, bottom, 0, right / textureSize, 
				bottom / textureSize);
		t.addVertexWithUV(right, top, 0, right / textureSize, 
				top / textureSize);
		t.addVertexWithUV(left, top, 0, left / textureSize, 
				top / textureSize);
		t.draw();
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
		glDisable(GL_LIGHTING);
		glDisable(GL_FOG);
		glDisable(GL_DEPTH_TEST);
		byte padding = 4;
		
		mc.getTextureManager().bindTexture(Gui.optionsBackground);
		glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		float textureSize = 32.0F;
		
		Tessellator t = Tessellator.instance;
		
		//Box code is GuiSlot.overlayBackground
		//Upper box
		int upperBoxEnd = top + topMargin;

		glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		t.startDrawingQuads();
		t.setColorRGBA_I(0x404040, 255);
		t.addVertexWithUV(left, upperBoxEnd, 0.0D, 0.0D, upperBoxEnd
				/ textureSize);
		t.addVertexWithUV(right, upperBoxEnd, 0.0D, right / textureSize,
				upperBoxEnd / textureSize);
		t.setColorRGBA_I(0x404040, 255);
		t.addVertexWithUV(right, top, 0.0D, right / textureSize, top
				/ textureSize);
		t.addVertexWithUV(left, top, 0.0D, 0.0D, top / textureSize);
		t.draw();

		// Lower box
		int lowerBoxStart = bottom - bottomMargin;

		glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		t.startDrawingQuads();
		t.setColorRGBA_I(0x404040, 255);
		t.addVertexWithUV(left, bottom, 0.0D, 0.0D, bottom / textureSize);
		t.addVertexWithUV(right, bottom, 0.0D, right / textureSize, bottom
				/ textureSize);
		t.setColorRGBA_I(0x404040, 255);
		t.addVertexWithUV(right, lowerBoxStart, 0.0D, right / textureSize,
				lowerBoxStart / textureSize);
		t.addVertexWithUV(left, lowerBoxStart, 0.0D, 0.0D, lowerBoxStart
				/ textureSize);
		t.draw();
		
		//Gradients
		glEnable(GL_BLEND);
		OpenGlHelper.glBlendFunc(GL_SRC_ALPHA,
				GL_ONE_MINUS_SRC_ALPHA, 0, 1);
		glDisable(GL_ALPHA_TEST);
		GL11.glShadeModel(GL_SMOOTH);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		t.startDrawingQuads();
		t.setColorRGBA_I(0, 0);
		t.addVertexWithUV(left, upperBoxEnd + padding, 0.0D, 0.0D, 1.0D);
		t.addVertexWithUV(right, upperBoxEnd + padding, 0.0D, 1.0D, 1.0D);
		t.setColorRGBA_I(0, 255);
		t.addVertexWithUV(right, upperBoxEnd, 0.0D, 1.0D, 0.0D);
		t.addVertexWithUV(left, upperBoxEnd, 0.0D, 0.0D, 0.0D);
		t.draw();
		t.startDrawingQuads();
		t.setColorRGBA_I(0, 255);
		t.addVertexWithUV(left, lowerBoxStart, 0.0D, 0.0D, 1.0D);
		t.addVertexWithUV(right, lowerBoxStart, 0.0D, 1.0D, 1.0D);
		t.setColorRGBA_I(0, 0);
		t.addVertexWithUV(right, lowerBoxStart - padding, 0.0D, 1.0D, 0.0D);
		t.addVertexWithUV(left, lowerBoxStart - padding, 0.0D, 0.0D, 0.0D);
		t.draw();
		
		glEnable(GL_TEXTURE_2D);
		glShadeModel(GL_FLAT);
		glEnable(GL_ALPHA_TEST);
		glDisable(GL_BLEND);
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
		// :(
		Field xPosField = GuiTextField.class.getDeclaredFields()[1];
		Field yPosField = GuiTextField.class.getDeclaredFields()[2];
		int xPosition, yPosition;
		try {
			xPosField.setAccessible(true);
			xPosition = xPosField.getInt(textBox);
			yPosField.setAccessible(true);
			yPosition = yPosField.getInt(textBox);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		int scaledX = mouseX - xPosition;
		int scaledY = mouseY - yPosition;
		
		// Standard text box height -- there is no actual getter for the real
		// one.
		final int height = 20;

		return scaledX >= 0 && scaledX < textBox.getWidth() && scaledY >= 0
				&& scaledY < height;
	}
	
	/**
	 * Attempts to open a link.
	 * @param path the URL to open.
	 */
	public static void openLink(String path) {
		try {
			Class<?> desktopClass = Class.forName("java.awt.Desktop");
			Object desktop = desktopClass.getMethod("getDesktop").invoke(
					null);
			desktopClass.getMethod("browse", URI.class).invoke(desktop,
					new URI(path));
		} catch (Throwable e) {
			logger.error("Couldn\'t open link", e);
		}
	}
	
	/**
	 * Draws a string with a shadow.
	 * 
	 * Needed because of obfuscation.
	 */
	public static void drawStringWithShadow(String s, int x, int y, int color) {
		mc.fontRendererObj.drawStringWithShadow(s, x, y, color);
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
	public int getHoverState(boolean mouseOver) {
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
			glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			
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
	public GuiNumericTextField(FontRenderer fontRenderer,
			int x, int y, int width, int height) {
		super(fontRenderer, x, y, width,
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

/**
 * {@link GuiListExtended} that provides scrollable lines of text, and support
 * for embedding links in it.
 */
class TextList extends GuiListExtended {
	public final int topMargin;
	public final int bottomMargin;
	
	/**
	 * Creates a new TextList with no text.
	 */
	public TextList(Minecraft mc, int width, int height, int topMargin,
			int bottomMargin) {
		super(mc, width, height, topMargin, height - bottomMargin,
				mc.fontRendererObj.FONT_HEIGHT + 1);
		
		this.topMargin = topMargin;
		this.bottomMargin = bottomMargin;
		
		this.entries = new ArrayList<IGuiListEntry>();
	}

	private List<IGuiListEntry> entries;
	
	@Override
	public IGuiListEntry getListEntry(int index) {
		return entries.get(index);
	}
	
	@Override
	protected int getSize() {
		return entries.size();
	}
	
	@Override
	protected int getScrollBarX() {
		return width - 10;
	}
	
	@Override
	public int getListWidth() {
		return width - 18;
	}
	
	public void addLine(String text) {
		List<String> lines = Utils.wordWrap(text, getListWidth());
		for (String line : lines) {
			entries.add(new TextEntry(mc, line, 0xFFFFFF));
		}
	}
	
	public void addBlankLine() {
		entries.add(new TextEntry(mc, "", 0xFFFFFF));
	}
	
	public void addLinkLine(String text, String URL) {
		List<String> lines = Utils.wordWrap(text, getListWidth());
		for (String line : lines) {
			entries.add(new LinkEntry(mc, line, URL));
		}
	}
	
	public void clearLines() {
		entries.clear();
	}
}

/**
 * {@link IGuiListEntry} that displays a single line of text.
 */
class TextEntry implements IGuiListEntry {
	private final String text;
	private final int color;
	protected final Minecraft mc;
	
	/**
	 * Creates a new TextEntry with the default color.
	 */
	public TextEntry(Minecraft mc, String text) {
		this(mc, text, 0xFFFFF);
	}
	
	/**
	 * Creates a new TextEntry.
	 */
	public TextEntry(Minecraft mc, String text, int color) {
		this.mc = mc;
		this.text = text;
		this.color = color;
	}
	
	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth,
			int slotHeight, int mouseX, int mouseY, boolean isSelected) {
		if (y < 0) {
			return;
		}
		Utils.drawStringWithShadow(text, x, y + 1, color);
	}
	
	@Override
	public boolean mousePressed(int slotIndex, int x, int y, int mouseEvent,
			int relativeX, int relativeY) {
		return false;
	}
	
	@Override
	public void mouseReleased(int slotIndex, int x, int y, int mouseEvent,
			int relativeX, int relativeY) {
		
	}
	
	@Override
	public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_) {
		
	}
}

/**
 * {@link IGuiListEntry} that displays a single clickable link.
 */
class LinkEntry extends TextEntry {
	private final String link;
	private final int textWidth;
	private final int linkWidth;
	
	public LinkEntry(Minecraft mc, String text, String link) {
		super(mc, text, 0x5555FF);
		
		this.link = link;
		this.textWidth = mc.fontRendererObj.getStringWidth(text);
		this.linkWidth = mc.fontRendererObj.getStringWidth(link);
	}
	
	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth,
			int slotHeight, int mouseX, int mouseY, boolean isSelected) {
		if (y < 0) {
			return;
		}
		
		super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX,
				mouseY, isSelected);
		
		int relativeX = mouseX - x;
		int relativeY = mouseY - y;
		if (relativeX >= 0 && relativeX <= textWidth &&
				relativeY >= 0 && relativeY <= slotHeight) {
			int drawX = mouseX - 2;
			if (drawX + linkWidth + 4 > listWidth + x) {
				drawX = listWidth + x - (4 + linkWidth);
			}
			Gui.drawRect(drawX, mouseY - 2, drawX + linkWidth + 4,
					mouseY + mc.fontRendererObj.FONT_HEIGHT + 2, 0x80000000);
			
			Utils.drawStringWithShadow(link, drawX + 2, mouseY, 0xFFFFFF);
		}
	}
	
	@Override
	public boolean mousePressed(int slotIndex, int x, int y, int mouseEvent,
			int relativeX, int relativeY) {
		if (relativeX >= 0 && relativeX <= textWidth) {
			Utils.openLink(link);
			return true;
		}
		return false;
	}
}

/**
 * Backport of GuiListExtended from 1.8. Becasuse the 1.7.10 one is not very
 * good.
 */
abstract class GuiListExtended {
	protected final Minecraft mc;
	protected int width;
	protected int height;

	/** The top of the slot container. Affects the overlays and scrolling. */
	protected int top;

	/** The bottom of the slot container. Affects the overlays and scrolling. */
	protected int bottom;
	protected int right;
	protected int left;

	/** The height of a slot. */
	protected final int slotHeight;

	/** The buttonID of the button used to scroll up */
	private int scrollUpButtonID;

	/** The buttonID of the button used to scroll down */
	private int scrollDownButtonID;
	protected int mouseX;
	protected int mouseY;
	protected boolean field_148163_i = true;

	/** Where the mouse was in the window when you first clicked to scroll */
	protected float initialClickY = -2.0F;

	/**
	 * What to multiply the amount you moved your mouse by (used for slowing
	 * down scrolling when over the items and not on the scroll bar)
	 */
	protected float scrollMultiplier;

	/** How far down this slot has been scrolled */
	protected float amountScrolled;

	/** The element in the list that was selected */
	protected int selectedElement = -1;

	/** The time when this button was last clicked. */
	protected long lastClicked;
	protected boolean field_178041_q = true;

	/**
	 * Set to true if a selected element in this gui will show an outline box
	 */
	protected boolean showSelectionBox = true;
	protected boolean hasListHeader;
	protected int headerPadding;
	private boolean enabled = true;

	public GuiListExtended(Minecraft mcIn, int width, int height,
			int p_i1052_4_, int p_i1052_5_, int p_i1052_6_) {
		this.mc = mcIn;
		this.width = width;
		this.height = height;
		this.top = p_i1052_4_;
		this.bottom = p_i1052_5_;
		this.slotHeight = p_i1052_6_;
		this.left = 0;
		this.right = width;
	}

	public void setDimensions(int p_148122_1_, int p_148122_2_,
			int p_148122_3_, int p_148122_4_) {
		this.width = p_148122_1_;
		this.height = p_148122_2_;
		this.top = p_148122_3_;
		this.bottom = p_148122_4_;
		this.left = 0;
		this.right = p_148122_1_;
	}

	public void setShowSelectionBox(boolean p_148130_1_) {
		this.showSelectionBox = p_148130_1_;
	}

	/**
	 * Sets hasListHeader and headerHeight. Params: hasListHeader, headerHeight.
	 * If hasListHeader is false headerHeight is set to 0.
	 */
	protected void setHasListHeader(boolean p_148133_1_, int p_148133_2_) {
		this.hasListHeader = p_148133_1_;
		this.headerPadding = p_148133_2_;

		if (!p_148133_1_) {
			this.headerPadding = 0;
		}
	}

	protected abstract int getSize();

	/**
	 * Gets the IGuiListEntry object for the given index
	 */
	public abstract IGuiListEntry getListEntry(int p_148180_1_);

	/**
	 * The element in the slot that was clicked, boolean for whether it was
	 * double clicked or not
	 */
	protected void elementClicked(int slotIndex, boolean isDoubleClick,
			int mouseX, int mouseY) {
	}

	/**
	 * Returns true if the element passed in is currently selected
	 */
	protected boolean isSelected(int slotIndex) {
		return false;
	}

	protected void drawBackground() {
	}

	protected void drawSlot(int p_180791_1_, int p_180791_2_, int p_180791_3_,
			int p_180791_4_, int p_180791_5_, int p_180791_6_) {
		this.getListEntry(p_180791_1_)
				.drawEntry(
						p_180791_1_,
						p_180791_2_,
						p_180791_3_,
						this.getListWidth(),
						p_180791_4_,
						p_180791_5_,
						p_180791_6_,
						this.getSlotIndexFromScreenCoords(p_180791_5_,
								p_180791_6_) == p_180791_1_);
	}

	protected void func_178040_a(int p_178040_1_, int p_178040_2_,
			int p_178040_3_) {
		//this.getListEntry(p_178040_1_).setSelected(p_178040_1_, p_178040_2_,
		//		p_178040_3_);
	}

	public boolean func_148179_a(int p_148179_1_, int p_148179_2_,
			int p_148179_3_) {
		if (this.isMouseYWithinSlotBounds(p_148179_2_)) {
			int var4 = this.getSlotIndexFromScreenCoords(p_148179_1_,
					p_148179_2_);

			if (var4 >= 0) {
				int var5 = this.left + this.width / 2 - this.getListWidth() / 2
						+ 2;
				int var6 = this.top + 4 - this.getAmountScrolled() + var4
						* this.slotHeight + this.headerPadding;
				int var7 = p_148179_1_ - var5;
				int var8 = p_148179_2_ - var6;

				if (this.getListEntry(var4).mousePressed(var4, p_148179_1_,
						p_148179_2_, p_148179_3_, var7, var8)) {
					this.setEnabled(false);
					return true;
				}
			}
		}

		return false;
	}

	public boolean func_148181_b(int p_148181_1_, int p_148181_2_,
			int p_148181_3_) {
		for (int var4 = 0; var4 < this.getSize(); ++var4) {
			int var5 = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
			int var6 = this.top + 4 - this.getAmountScrolled() + var4
					* this.slotHeight + this.headerPadding;
			int var7 = p_148181_1_ - var5;
			int var8 = p_148181_2_ - var6;
			this.getListEntry(var4).mouseReleased(var4, p_148181_1_,
					p_148181_2_, p_148181_3_, var7, var8);
		}

		this.setEnabled(true);
		return false;
	}

	/**
	 * Handles drawing a list's header row.
	 */
	protected void drawListHeader(int p_148129_1_, int p_148129_2_,
			Tessellator p_148129_3_) {
	}

	protected void func_148132_a(int p_148132_1_, int p_148132_2_) {
	}

	protected void func_148142_b(int p_148142_1_, int p_148142_2_) {
	}

	public int getSlotIndexFromScreenCoords(int p_148124_1_, int p_148124_2_) {
		int var3 = this.left + this.width / 2 - this.getListWidth() / 2;
		int var4 = this.left + this.width / 2 + this.getListWidth() / 2;
		int var5 = p_148124_2_ - this.top - this.headerPadding
				+ (int) this.amountScrolled - 4;
		int var6 = var5 / this.slotHeight;
		return p_148124_1_ < this.getScrollBarX() && p_148124_1_ >= var3
				&& p_148124_1_ <= var4 && var6 >= 0 && var5 >= 0
				&& var6 < this.getSize() ? var6 : -1;
	}

	/**
	 * Registers the IDs that can be used for the scrollbar's up/down buttons.
	 */
	public void registerScrollButtons(int p_148134_1_, int p_148134_2_) {
		this.scrollUpButtonID = p_148134_1_;
		this.scrollDownButtonID = p_148134_2_;
	}

	/**
	 * Stop the thing from scrolling out of bounds
	 */
	protected void bindAmountScrolled() {
		int var1 = this.func_148135_f();

		if (var1 < 0) {
			var1 /= 2;
		}

		if (!this.field_148163_i && var1 < 0) {
			var1 = 0;
		}

		this.amountScrolled = MathHelper.clamp_float(this.amountScrolled, 0.0F,
				var1);
	}

	public int func_148135_f() {
		return Math.max(0, this.getContentHeight()
				- (this.bottom - this.top - 4));
	}

	/**
	 * Returns the amountScrolled field as an integer.
	 */
	public int getAmountScrolled() {
		return (int) this.amountScrolled;
	}

	public boolean isMouseYWithinSlotBounds(int p_148141_1_) {
		return p_148141_1_ >= this.top && p_148141_1_ <= this.bottom
				&& this.mouseX >= this.left && this.mouseX <= this.right;
	}

	/**
	 * Scrolls the slot by the given amount. A positive value scrolls down, and
	 * a negative value scrolls up.
	 */
	public void scrollBy(int p_148145_1_) {
		this.amountScrolled += p_148145_1_;
		this.bindAmountScrolled();
		this.initialClickY = -2.0F;
	}

	public void actionPerformed(GuiButton p_148147_1_) {
		if (p_148147_1_.enabled) {
			if (p_148147_1_.id == this.scrollUpButtonID) {
				this.amountScrolled -= this.slotHeight * 2 / 3;
				this.initialClickY = -2.0F;
				this.bindAmountScrolled();
			} else if (p_148147_1_.id == this.scrollDownButtonID) {
				this.amountScrolled += this.slotHeight * 2 / 3;
				this.initialClickY = -2.0F;
				this.bindAmountScrolled();
			}
		}
	}

	public void drawScreen(int p_148128_1_, int p_148128_2_, float p_148128_3_) {
		if (this.field_178041_q) {
			this.mouseX = p_148128_1_;
			this.mouseY = p_148128_2_;
			this.drawBackground();
			int var4 = this.getScrollBarX();
			int var5 = var4 + 6;
			this.bindAmountScrolled();
			glDisable(GL_LIGHTING);
			glDisable(GL_FOG);
			Tessellator tess = Tessellator.instance;
			this.mc.getTextureManager().bindTexture(Gui.optionsBackground);
			glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			float var8 = 32.0F;
			tess.startDrawingQuads();
			tess.setColorOpaque_I(2105376);
			tess.addVertexWithUV(this.left, this.bottom, 0.0D,
					this.left / var8, (this.bottom + (int) this.amountScrolled)
							/ var8);
			tess.addVertexWithUV(this.right, this.bottom, 0.0D, this.right
					/ var8, (this.bottom + (int) this.amountScrolled) / var8);
			tess.addVertexWithUV(this.right, this.top, 0.0D, this.right / var8,
					(this.top + (int) this.amountScrolled) / var8);
			tess.addVertexWithUV(this.left, this.top, 0.0D, this.left / var8,
					(this.top + (int) this.amountScrolled) / var8);
			tess.draw();
			int var9 = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
			int var10 = this.top + 4 - (int) this.amountScrolled;

			if (this.hasListHeader) {
				this.drawListHeader(var9, var10, tess);
			}

			this.drawSelectionBox(var9, var10, p_148128_1_, p_148128_2_);
			glDisable(GL_DEPTH_TEST);
			byte var11 = 4;
			this.overlayBackground(0, this.top, 255, 255);
			this.overlayBackground(this.bottom, this.height, 255, 255);
			glEnable(GL_BLEND);
			OpenGlHelper.glBlendFunc(GL_SRC_ALPHA,
					GL_ONE_MINUS_SRC_ALPHA, 0, 1);
			glDisable(GL_ALPHA_TEST);
			GL11.glShadeModel(GL_SMOOTH);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			tess.startDrawingQuads();
			tess.setColorRGBA_I(0, 0);
			tess.addVertexWithUV(this.left, this.top + var11, 0.0D, 0.0D, 1.0D);
			tess.addVertexWithUV(this.right, this.top + var11, 0.0D, 1.0D, 1.0D);
			tess.setColorRGBA_I(0, 255);
			tess.addVertexWithUV(this.right, this.top, 0.0D, 1.0D, 0.0D);
			tess.addVertexWithUV(this.left, this.top, 0.0D, 0.0D, 0.0D);
			tess.draw();
			tess.startDrawingQuads();
			tess.setColorRGBA_I(0, 255);
			tess.addVertexWithUV(this.left, this.bottom, 0.0D, 0.0D, 1.0D);
			tess.addVertexWithUV(this.right, this.bottom, 0.0D, 1.0D, 1.0D);
			tess.setColorRGBA_I(0, 0);
			tess.addVertexWithUV(this.right, this.bottom - var11, 0.0D, 1.0D,
					0.0D);
			tess.addVertexWithUV(this.left, this.bottom - var11, 0.0D, 0.0D,
					0.0D);
			tess.draw();
			int var12 = this.func_148135_f();

			if (var12 > 0) {
				int var13 = (this.bottom - this.top) * (this.bottom - this.top)
						/ this.getContentHeight();
				var13 = MathHelper.clamp_int(var13, 32, this.bottom - this.top
						- 8);
				int var14 = (int) this.amountScrolled
						* (this.bottom - this.top - var13) / var12 + this.top;

				if (var14 < this.top) {
					var14 = this.top;
				}

				tess.startDrawingQuads();
				tess.setColorRGBA_I(0, 255);
				tess.addVertexWithUV(var4, this.bottom, 0.0D, 0.0D, 1.0D);
				tess.addVertexWithUV(var5, this.bottom, 0.0D, 1.0D, 1.0D);
				tess.addVertexWithUV(var5, this.top, 0.0D, 1.0D, 0.0D);
				tess.addVertexWithUV(var4, this.top, 0.0D, 0.0D, 0.0D);
				tess.draw();
				tess.startDrawingQuads();
				tess.setColorRGBA_I(8421504, 255);
				tess.addVertexWithUV(var4, var14 + var13, 0.0D, 0.0D, 1.0D);
				tess.addVertexWithUV(var5, var14 + var13, 0.0D, 1.0D, 1.0D);
				tess.addVertexWithUV(var5, var14, 0.0D, 1.0D, 0.0D);
				tess.addVertexWithUV(var4, var14, 0.0D, 0.0D, 0.0D);
				tess.draw();
				tess.startDrawingQuads();
				tess.setColorRGBA_I(12632256, 255);
				tess.addVertexWithUV(var4, var14 + var13 - 1, 0.0D, 0.0D, 1.0D);
				tess.addVertexWithUV(var5 - 1, var14 + var13 - 1, 0.0D, 1.0D,
						1.0D);
				tess.addVertexWithUV(var5 - 1, var14, 0.0D, 1.0D, 0.0D);
				tess.addVertexWithUV(var4, var14, 0.0D, 0.0D, 0.0D);
				tess.draw();
			}

			this.func_148142_b(p_148128_1_, p_148128_2_);
			glEnable(GL_TEXTURE_2D);
			glShadeModel(GL_FLAT);
			glEnable(GL_ALPHA_TEST);
			glDisable(GL_BLEND);
		}
	}

	public void handleMouseInput() {
		if (this.isMouseYWithinSlotBounds(this.mouseY)) {
			if (Mouse.isButtonDown(0) && this.getEnabled()) {
				if (this.initialClickY == -1.0F) {
					boolean var1 = true;

					if (this.mouseY >= this.top && this.mouseY <= this.bottom) {
						int var2 = this.width / 2 - this.getListWidth() / 2;
						int var3 = this.width / 2 + this.getListWidth() / 2;
						int var4 = this.mouseY - this.top - this.headerPadding
								+ (int) this.amountScrolled - 4;
						int var5 = var4 / this.slotHeight;

						if (this.mouseX >= var2 && this.mouseX <= var3
								&& var5 >= 0 && var4 >= 0
								&& var5 < this.getSize()) {
							boolean var6 = var5 == this.selectedElement
									&& Minecraft.getSystemTime()
											- this.lastClicked < 250L;
							this.elementClicked(var5, var6, this.mouseX,
									this.mouseY);
							this.selectedElement = var5;
							this.lastClicked = Minecraft.getSystemTime();
						} else if (this.mouseX >= var2 && this.mouseX <= var3
								&& var4 < 0) {
							this.func_148132_a(this.mouseX - var2, this.mouseY
									- this.top + (int) this.amountScrolled - 4);
							var1 = false;
						}

						int var11 = this.getScrollBarX();
						int var7 = var11 + 6;

						if (this.mouseX >= var11 && this.mouseX <= var7) {
							this.scrollMultiplier = -1.0F;
							int var8 = this.func_148135_f();

							if (var8 < 1) {
								var8 = 1;
							}

							int var9 = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top)) / (float) this
									.getContentHeight());
							var9 = MathHelper.clamp_int(var9, 32, this.bottom
									- this.top - 8);
							this.scrollMultiplier /= (float) (this.bottom
									- this.top - var9)
									/ (float) var8;
						} else {
							this.scrollMultiplier = 1.0F;
						}

						if (var1) {
							this.initialClickY = this.mouseY;
						} else {
							this.initialClickY = -2.0F;
						}
					} else {
						this.initialClickY = -2.0F;
					}
				} else if (this.initialClickY >= 0.0F) {
					this.amountScrolled -= (this.mouseY - this.initialClickY)
							* this.scrollMultiplier;
					this.initialClickY = this.mouseY;
				}
			} else {
				this.initialClickY = -1.0F;
			}

			int var10 = Mouse.getEventDWheel();

			if (var10 != 0) {
				if (var10 > 0) {
					var10 = -1;
				} else if (var10 < 0) {
					var10 = 1;
				}

				this.amountScrolled += var10 * this.slotHeight / 2;
			}
		}
	}

	public void setEnabled(boolean p_148143_1_) {
		this.enabled = p_148143_1_;
	}

	public boolean getEnabled() {
		return this.enabled;
	}

	/**
	 * Gets the width of the list
	 */
	public int getListWidth() {
		return 220;
	}

	/**
	 * Draws the selection box around the selected slot element.
	 */
	protected void drawSelectionBox(int p_148120_1_, int p_148120_2_,
			int p_148120_3_, int p_148120_4_) {
		int var5 = this.getSize();
		Tessellator tess = Tessellator.instance;

		for (int var8 = 0; var8 < var5; ++var8) {
			int var9 = p_148120_2_ + var8 * this.slotHeight
					+ this.headerPadding;
			int var10 = this.slotHeight - 4;

			if (var9 > this.bottom || var9 + var10 < this.top) {
				this.func_178040_a(var8, p_148120_1_, var9);
			}

			if (this.showSelectionBox && this.isSelected(var8)) {
				int var11 = this.left
						+ (this.width / 2 - this.getListWidth() / 2);
				int var12 = this.left + this.width / 2 + this.getListWidth()
						/ 2;
				glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				glDisable(GL_TEXTURE_2D);
				tess.startDrawingQuads();
				tess.setColorOpaque_I(8421504);
				tess.addVertexWithUV(var11, var9 + var10 + 2, 0.0D, 0.0D, 1.0D);
				tess.addVertexWithUV(var12, var9 + var10 + 2, 0.0D, 1.0D, 1.0D);
				tess.addVertexWithUV(var12, var9 - 2, 0.0D, 1.0D, 0.0D);
				tess.addVertexWithUV(var11, var9 - 2, 0.0D, 0.0D, 0.0D);
				tess.setColorOpaque_I(0);
				tess.addVertexWithUV(var11 + 1, var9 + var10 + 1, 0.0D, 0.0D,
						1.0D);
				tess.addVertexWithUV(var12 - 1, var9 + var10 + 1, 0.0D, 1.0D,
						1.0D);
				tess.addVertexWithUV(var12 - 1, var9 - 1, 0.0D, 1.0D, 0.0D);
				tess.addVertexWithUV(var11 + 1, var9 - 1, 0.0D, 0.0D, 0.0D);
				tess.draw();
				glEnable(GL_TEXTURE_2D);
			}

			this.drawSlot(var8, p_148120_1_, var9, var10, p_148120_3_,
					p_148120_4_);
		}
	}

	protected int getScrollBarX() {
		return this.width / 2 + 124;
	}

	/**
	 * Overlays the background to hide scrolled items
	 */
	protected void overlayBackground(int p_148136_1_, int p_148136_2_,
			int p_148136_3_, int p_148136_4_) {
		Tessellator tess = Tessellator.instance;
		this.mc.getTextureManager().bindTexture(Gui.optionsBackground);
		glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		float var7 = 32.0F;
		tess.startDrawingQuads();
		tess.setColorRGBA_I(4210752, p_148136_4_);
		tess.addVertexWithUV(this.left, p_148136_2_, 0.0D, 0.0D, p_148136_2_
				/ var7);
		tess.addVertexWithUV(this.left + this.width, p_148136_2_, 0.0D,
				this.width / var7, p_148136_2_ / var7);
		tess.setColorRGBA_I(4210752, p_148136_3_);
		tess.addVertexWithUV(this.left + this.width, p_148136_1_, 0.0D,
				this.width / var7, p_148136_1_ / var7);
		tess.addVertexWithUV(this.left, p_148136_1_, 0.0D, 0.0D, p_148136_1_
				/ var7);
		tess.draw();
	}

	/**
	 * Sets the left and right bounds of the slot. Param is the left bound,
	 * right is calculated as left + width.
	 */
	public void setSlotXBoundsFromLeft(int p_148140_1_) {
		this.left = p_148140_1_;
		this.right = p_148140_1_ + this.width;
	}

	public int getSlotHeight() {
		return this.slotHeight;
	}
	
	/**
	 * Return the height of the content being scrolled
	 */
	protected int getContentHeight() {
		return this.getSize() * this.slotHeight + this.headerPadding;
	}
}

interface IGuiListEntry {
	// Unused, but defined to keep things simple
	void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_);

	void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight,
			int mouseX, int mouseY, boolean isSelected);

	boolean mousePressed(int p_148278_1_, int p_148278_2_, int p_148278_3_,
			int p_148278_4_, int p_148278_5_, int p_148278_6_);

	void mouseReleased(int slotIndex, int x, int y, int mouseEvent,
			int relativeX, int relativeY);
}