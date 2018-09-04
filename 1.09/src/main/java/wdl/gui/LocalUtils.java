/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui;

import static org.lwjgl.opengl.GL11.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import wdl.WDL;

public class LocalUtils {
	private static final Minecraft mc = Minecraft.getInstance();
	private LocalUtils() { throw new AssertionError(); }

	/**
	 * Creates a new instance of {@link EntityPlayerSP}.
	 */
	public static EntityPlayerSP makePlayer() {
		return new EntityPlayerSP(WDL.minecraft, WDL.worldClient,
				WDL.thePlayer.connection, WDL.thePlayer.getStatFileWriter());
	}

	/**
	 * Draws a dark background, similar to {@link GuiScreen#drawBackground(int)} but darker.
	 * Same appearance as the background in lists.
	 *
	 * @param top Where to start drawing (usually, 0)
	 * @param left Where to start drawing (usually, 0)
	 * @param bottom Where to stop drawing (usually, height).
	 * @param right Where to stop drawing (usually, width)
	 */
	public static void drawDarkBackground(int top, int left, int bottom, int right) {
		GlStateManager.disableLighting();
		GlStateManager.disableFog();

		Tessellator t = Tessellator.getInstance();
		VertexBuffer b = t.getBuffer();

		mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		float textureSize = 32.0F;
		b.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		b.pos(0, bottom, 0).tex(0 / textureSize,
				bottom / textureSize).color(32, 32, 32, 255).endVertex();
		b.pos(right, bottom, 0).tex(right / textureSize,
				bottom / textureSize).color(32, 32, 32, 255).endVertex();
		b.pos(right, top, 0).tex(right / textureSize,
				top / textureSize).color(32, 32, 32, 255).endVertex();
		b.pos(left, top, 0).tex(left / textureSize,
				top / textureSize).color(32, 32, 32, 255).endVertex();
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
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		GlStateManager.disableDepth();
		byte padding = 4;

		mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		float textureSize = 32.0F;

		Tessellator t = Tessellator.getInstance();
		VertexBuffer b = t.getBuffer();

		//Box code is GuiSlot.overlayBackground
		//Upper box
		int upperBoxEnd = top + topMargin;

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		b.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		b.pos(left, upperBoxEnd, 0.0D).tex(0.0D, upperBoxEnd
				/ textureSize).color(64, 64, 64, 255).endVertex();
		b.pos(right, upperBoxEnd, 0.0D).tex(right / textureSize,
				upperBoxEnd / textureSize).color(64, 64, 64, 255).endVertex();
		b.pos(right, top, 0.0D).tex(right / textureSize, top / textureSize)
		.color(64, 64, 64, 255).endVertex();
		b.pos(left, top, 0.0D).tex(0.0D, top / textureSize)
		.color(64, 64, 64, 255).endVertex();
		t.draw();

		// Lower box
		int lowerBoxStart = bottom - bottomMargin;

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		b.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		b.pos(left, bottom, 0.0D).tex(0.0D, bottom / textureSize)
		.color(64, 64, 64, 255).endVertex();
		b.pos(right, bottom, 0.0D).tex(right / textureSize, bottom
				/ textureSize).color(64, 64, 64, 255).endVertex();
		b.pos(right, lowerBoxStart, 0.0D)
		.tex(right / textureSize, lowerBoxStart / textureSize)
		.color(64, 64, 64, 255).endVertex();
		b.pos(left, lowerBoxStart, 0.0D).tex(0.0D, lowerBoxStart
				/ textureSize).color(64, 64, 64, 255).endVertex();
		t.draw();

		//Gradients
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA,
				GL_ONE_MINUS_SRC_ALPHA, 0, 1);
		GlStateManager.disableAlpha();
		GlStateManager.shadeModel(GL_SMOOTH);
		GlStateManager.disableTexture2D();
		b.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		b.pos(left, upperBoxEnd + padding, 0.0D).tex(0.0D, 1.0D)
		.color(0, 0, 0, 0).endVertex();
		b.pos(right, upperBoxEnd + padding, 0.0D).tex(1.0D, 1.0D)
		.color(0, 0, 0, 0).endVertex();
		b.pos(right, upperBoxEnd, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255)
		.endVertex();
		b.pos(left, upperBoxEnd, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255)
		.endVertex();
		t.draw();
		b.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		b.pos(left, lowerBoxStart, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255)
		.endVertex();
		b.pos(right, lowerBoxStart, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255)
		.endVertex();
		b.pos(right, lowerBoxStart - padding, 0.0D).tex(1.0D, 0.0D)
		.color(0, 0, 0, 0).endVertex();
		b.pos(left, lowerBoxStart - padding, 0.0D).tex(0.0D, 0.0D)
		.color(0, 0, 0, 0).endVertex();
		t.draw();

		GlStateManager.enableTexture2D();
		GlStateManager.shadeModel(GL_FLAT);
		GlStateManager.enableAlpha();
		GlStateManager.disableBlend();
	}

	/**
	 * Copies the given text into the system clipboard.
	 * @param text The text to copy
	 */
	public static void setClipboardString(String text) {
		GuiScreen.setClipboardString(text);
	}
}

