/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
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
 *
 * Almost all of these are just functions missing mappings, and can be undone later.
 * (There are a few changes to the parameters to tex() being floats instead of doubles,
 * but those can be backported)
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
		GlStateManager.func_227722_g_();
		GlStateManager.func_227769_y_();

		Tessellator t = Tessellator.getInstance();
		BufferBuilder b = t.getBuffer();

		Minecraft.getInstance().getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
		GlStateManager.func_227702_d_(1.0F, 1.0F, 1.0F, 1.0F);

		float textureSize = 32.0F;
		b.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		b.func_225582_a_(0, bottom, 0).func_225583_a_(0 / textureSize,
				bottom / textureSize).func_225586_a_(32, 32, 32, 255).endVertex();
		b.func_225582_a_(right, bottom, 0).func_225583_a_(right / textureSize,
				bottom / textureSize).func_225586_a_(32, 32, 32, 255).endVertex();
		b.func_225582_a_(right, top, 0).func_225583_a_(right / textureSize,
				top / textureSize).func_225586_a_(32, 32, 32, 255).endVertex();
		b.func_225582_a_(left, top, 0).func_225583_a_(left / textureSize,
				top / textureSize).func_225586_a_(32, 32, 32, 255).endVertex();
		t.draw();
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#drawBorder
	 */
	static void drawBorder(int topMargin, int bottomMargin, int top, int left, int bottom, int right) {
		GlStateManager.func_227722_g_();
		GlStateManager.func_227769_y_();
		GlStateManager.func_227731_j_();
		byte padding = 4;

		Minecraft.getInstance().getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
		GlStateManager.func_227702_d_(1.0F, 1.0F, 1.0F, 1.0F);

		float textureSize = 32.0F;

		Tessellator t = Tessellator.getInstance();
		BufferBuilder b = t.getBuffer();

		//Box code is GuiSlot.overlayBackground
		//Upper box
		int upperBoxEnd = top + topMargin;

		GlStateManager.func_227702_d_(1.0F, 1.0F, 1.0F, 1.0F);
		b.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		b.func_225582_a_(left, upperBoxEnd, 0.0D).func_225583_a_(0, upperBoxEnd
				/ textureSize).func_225586_a_(64, 64, 64, 255).endVertex();
		b.func_225582_a_(right, upperBoxEnd, 0.0D).func_225583_a_(right / textureSize,
				upperBoxEnd / textureSize).func_225586_a_(64, 64, 64, 255).endVertex();
		b.func_225582_a_(right, top, 0.0D).func_225583_a_(right / textureSize, top / textureSize)
		.func_225586_a_(64, 64, 64, 255).endVertex();
		b.func_225582_a_(left, top, 0.0D).func_225583_a_(0, top / textureSize)
		.func_225586_a_(64, 64, 64, 255).endVertex();
		t.draw();

		// Lower box
		int lowerBoxStart = bottom - bottomMargin;

		GlStateManager.func_227702_d_(1.0F, 1.0F, 1.0F, 1.0F);
		b.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		b.func_225582_a_(left, bottom, 0.0D).func_225583_a_(0, bottom / textureSize)
		.func_225586_a_(64, 64, 64, 255).endVertex();
		b.func_225582_a_(right, bottom, 0.0D).func_225583_a_(right / textureSize, bottom
				/ textureSize).func_225586_a_(64, 64, 64, 255).endVertex();
		b.func_225582_a_(right, lowerBoxStart, 0.0D)
		.func_225583_a_(right / textureSize, lowerBoxStart / textureSize)
		.func_225586_a_(64, 64, 64, 255).endVertex();
		b.func_225582_a_(left, lowerBoxStart, 0.0D).func_225583_a_(0, lowerBoxStart
				/ textureSize).func_225586_a_(64, 64, 64, 255).endVertex();
		t.draw();

		//Gradients
		GlStateManager.func_227740_m_();
		GlStateManager.func_227644_a_(GL_SRC_ALPHA,
				GL_ONE_MINUS_SRC_ALPHA, 0, 1);
		GlStateManager.func_227700_d_();
		GlStateManager.func_227762_u_(GL_SMOOTH);
		GlStateManager.func_227621_I_();
		b.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		b.func_225582_a_(left, upperBoxEnd + padding, 0.0D).func_225583_a_(0, 1)
		.func_225586_a_(0, 0, 0, 0).endVertex();
		b.func_225582_a_(right, upperBoxEnd + padding, 0.0D).func_225583_a_(1, 1)
		.func_225586_a_(0, 0, 0, 0).endVertex();
		b.func_225582_a_(right, upperBoxEnd, 0.0D).func_225583_a_(1, 0).func_225586_a_(0, 0, 0, 255)
		.endVertex();
		b.func_225582_a_(left, upperBoxEnd, 0.0D).func_225583_a_(0, 0).func_225586_a_(0, 0, 0, 255)
		.endVertex();
		t.draw();
		b.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		b.func_225582_a_(left, lowerBoxStart, 0.0D).func_225583_a_(0, 1).func_225586_a_(0, 0, 0, 255)
		.endVertex();
		b.func_225582_a_(right, lowerBoxStart, 0.0D).func_225583_a_(1, 1).func_225586_a_(0, 0, 0, 255)
		.endVertex();
		b.func_225582_a_(right, lowerBoxStart - padding, 0.0D).func_225583_a_(1, 0)
		.func_225586_a_(0, 0, 0, 0).endVertex();
		b.func_225582_a_(left, lowerBoxStart - padding, 0.0D).func_225583_a_(0, 0)
		.func_225586_a_(0, 0, 0, 0).endVertex();
		t.draw();

		GlStateManager.func_227619_H_();
		GlStateManager.func_227762_u_(GL_FLAT);
		GlStateManager.func_227709_e_();
		GlStateManager.func_227737_l_();
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
}
