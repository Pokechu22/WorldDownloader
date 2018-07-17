/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

/**
 * A slider that doesn't require a bunch of interfaces to work.
 *
 * Based off of {@link net.minecraft.client.gui.GuiOptionSlider}.
 */
public class GuiSlider extends GuiButton {
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
				this.sliderValue = (float)(mouseX - (this.x + 4))
						/ (float)(this.width - 8);
				this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F,
						1.0F);
				this.dragging = true;

				this.displayString = I18n.format(text, getValue());
			}

			mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			if (this.enabled) {
				this.drawTexturedModalRect(this.x
						+ (int) (this.sliderValue * (this.width - 8)),
						this.y, 0, 66, 4, 20);
				this.drawTexturedModalRect(this.x
						+ (int) (this.sliderValue * (this.width - 8))
						+ 4, this.y, 196, 66, 4, 20);
			} else {
				this.drawTexturedModalRect(this.x
						+ (int) (this.sliderValue * (this.width - 8)),
						this.y, 0, 46, 4, 20);
				this.drawTexturedModalRect(this.x
						+ (int) (this.sliderValue * (this.width - 8))
						+ 4, this.y, 196, 46, 4, 20);
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
			this.sliderValue = (float)(mouseX - (this.x + 4))
					/ (float)(this.width - 8);
			this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F,
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