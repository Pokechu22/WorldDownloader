/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2020 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.versioned;

import static org.lwjgl.opengl.GL11.*;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Util;
import net.minecraft.world.World;

/**
 * Versioned functions related to GUIs.
 */
final class GuiFunctions {
	private GuiFunctions() { throw new AssertionError(); }

	/* (non-javadoc)
	 * @see VersionedFunctions#makePlayer
	 */
	static ClientPlayerEntity makePlayer(Minecraft minecraft, World world, ClientPlayNetHandler nhpc, ClientPlayerEntity base) {
		return new ClientPlayerEntity(minecraft, (ClientWorld)world, nhpc,
				base.getStats(), base.getRecipeBook());
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#drawDarkBackground
	 */
	static void drawDarkBackground(int top, int left, int bottom, int right) {
		GlStateManager.disableLighting();
		GlStateManager.disableFog();

		Tessellator t = Tessellator.getInstance();
		BufferBuilder b = t.getBuffer();

		Minecraft.getInstance().getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		float textureSize = 32.0F;
		b.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		b.func_181662_b(0, bottom, 0).func_187315_a(0 / textureSize,
				bottom / textureSize).func_181669_b(32, 32, 32, 255).endVertex();
		b.func_181662_b(right, bottom, 0).func_187315_a(right / textureSize,
				bottom / textureSize).func_181669_b(32, 32, 32, 255).endVertex();
		b.func_181662_b(right, top, 0).func_187315_a(right / textureSize,
				top / textureSize).func_181669_b(32, 32, 32, 255).endVertex();
		b.func_181662_b(left, top, 0).func_187315_a(left / textureSize,
				top / textureSize).func_181669_b(32, 32, 32, 255).endVertex();
		t.draw();
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#drawBorder
	 */
	static void drawBorder(int topMargin, int bottomMargin, int top, int left, int bottom, int right) {
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		GlStateManager.disableDepthTest();
		byte padding = 4;

		Minecraft.getInstance().getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		float textureSize = 32.0F;

		Tessellator t = Tessellator.getInstance();
		BufferBuilder b = t.getBuffer();

		//Box code is GuiSlot.overlayBackground
		//Upper box
		int upperBoxEnd = top + topMargin;

		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		b.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		b.func_181662_b(left, upperBoxEnd, 0.0D).func_187315_a(0, upperBoxEnd
				/ textureSize).func_181669_b(64, 64, 64, 255).endVertex();
		b.func_181662_b(right, upperBoxEnd, 0.0D).func_187315_a(right / textureSize,
				upperBoxEnd / textureSize).func_181669_b(64, 64, 64, 255).endVertex();
		b.func_181662_b(right, top, 0.0D).func_187315_a(right / textureSize, top / textureSize)
		.func_181669_b(64, 64, 64, 255).endVertex();
		b.func_181662_b(left, top, 0.0D).func_187315_a(0, top / textureSize)
		.func_181669_b(64, 64, 64, 255).endVertex();
		t.draw();

		// Lower box
		int lowerBoxStart = bottom - bottomMargin;

		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		b.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		b.func_181662_b(left, bottom, 0.0D).func_187315_a(0, bottom / textureSize)
		.func_181669_b(64, 64, 64, 255).endVertex();
		b.func_181662_b(right, bottom, 0.0D).func_187315_a(right / textureSize, bottom
				/ textureSize).func_181669_b(64, 64, 64, 255).endVertex();
		b.func_181662_b(right, lowerBoxStart, 0.0D)
		.func_187315_a(right / textureSize, lowerBoxStart / textureSize)
		.func_181669_b(64, 64, 64, 255).endVertex();
		b.func_181662_b(left, lowerBoxStart, 0.0D).func_187315_a(0, lowerBoxStart
				/ textureSize).func_181669_b(64, 64, 64, 255).endVertex();
		t.draw();

		//Gradients
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(GL_SRC_ALPHA,
				GL_ONE_MINUS_SRC_ALPHA, 0, 1);
		GlStateManager.disableAlphaTest();
		GlStateManager.shadeModel(GL_SMOOTH);
		GlStateManager.disableTexture();
		b.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		b.func_181662_b(left, upperBoxEnd + padding, 0.0D).func_187315_a(0, 1)
		.func_181669_b(0, 0, 0, 0).endVertex();
		b.func_181662_b(right, upperBoxEnd + padding, 0.0D).func_187315_a(1, 1)
		.func_181669_b(0, 0, 0, 0).endVertex();
		b.func_181662_b(right, upperBoxEnd, 0.0D).func_187315_a(1, 0).func_181669_b(0, 0, 0, 255)
		.endVertex();
		b.func_181662_b(left, upperBoxEnd, 0.0D).func_187315_a(0, 0).func_181669_b(0, 0, 0, 255)
		.endVertex();
		t.draw();
		b.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		b.func_181662_b(left, lowerBoxStart, 0.0D).func_187315_a(0, 1).func_181669_b(0, 0, 0, 255)
		.endVertex();
		b.func_181662_b(right, lowerBoxStart, 0.0D).func_187315_a(1, 1).func_181669_b(0, 0, 0, 255)
		.endVertex();
		b.func_181662_b(right, lowerBoxStart - padding, 0.0D).func_187315_a(1, 0)
		.func_181669_b(0, 0, 0, 0).endVertex();
		b.func_181662_b(left, lowerBoxStart - padding, 0.0D).func_187315_a(0, 0)
		.func_181669_b(0, 0, 0, 0).endVertex();
		t.draw();

		GlStateManager.enableTexture();
		GlStateManager.shadeModel(GL_FLAT);
		GlStateManager.enableAlphaTest();
		GlStateManager.disableBlend();
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#setClipboardString
	 */
	static void setClipboardString(String text) {
		Minecraft.getInstance().keyboardListener.setClipboardString(text);
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#openLink
	 */
	static void openLink(String url) {
		Util.getOSType().openURI(url);
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#glColor4f
	 */
	public static void glColor4f(float r, float g, float b, float a) {
		GlStateManager.color4f(r, g, b, a);
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#glTranslatef
	 */
	public static void glTranslatef(float x, float y, float z) {
		GlStateManager.translatef(x, y, z);
	}
}
