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

import com.mojang.blaze3d.systems.RenderSystem;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
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
	 * @see VersionedFunctions#getPointOfView
	 */
	static Object getPointOfView(GameSettings settings) {
		return settings.thirdPersonView;
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#setFirstPersonPointOfView
	 */
	static void setFirstPersonPointOfView(GameSettings settings) {
		settings.thirdPersonView = 0;
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#restorePointOfView
	 */
	static void restorePointOfView(GameSettings settings, Object value) {
		settings.thirdPersonView = (int)value;
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#drawDarkBackground
	 */
	static void drawDarkBackground(int top, int left, int bottom, int right) {
		RenderSystem.disableLighting();
		RenderSystem.disableFog();

		Tessellator t = Tessellator.getInstance();
		BufferBuilder b = t.getBuffer();

		Minecraft.getInstance().getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		float textureSize = 32.0F;
		b.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
		b.pos(0, bottom, 0).color(32, 32, 32, 255).tex(0 / textureSize, bottom / textureSize).endVertex();
		b.pos(right, bottom, 0).color(32, 32, 32, 255).tex(right / textureSize, bottom / textureSize).endVertex();
		b.pos(right, top, 0).color(32, 32, 32, 255).tex(right / textureSize, top / textureSize).endVertex();
		b.pos(left, top, 0).color(32, 32, 32, 255).tex(left / textureSize, top / textureSize).endVertex();
		t.draw();
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#drawBorder
	 */
	static void drawBorder(int topMargin, int bottomMargin, int top, int left, int bottom, int right) {
		RenderSystem.disableLighting();
		RenderSystem.disableFog();
		RenderSystem.disableDepthTest();
		byte padding = 4;

		Minecraft.getInstance().getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		float textureSize = 32.0F;

		Tessellator t = Tessellator.getInstance();
		BufferBuilder b = t.getBuffer();

		// Box code is GuiSlot.overlayBackground
		// Upper box
		int upperBoxEnd = top + topMargin;

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		b.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
		b.pos(left, upperBoxEnd, 0.0D).color(64, 64, 64, 255).tex(0, upperBoxEnd / textureSize).endVertex();
		b.pos(right, upperBoxEnd, 0.0D).color(64, 64, 64, 255).tex(right / textureSize, upperBoxEnd / textureSize)
				.endVertex();
		b.pos(right, top, 0.0D).color(64, 64, 64, 255).tex(right / textureSize, top / textureSize).endVertex();
		b.pos(left, top, 0.0D).color(64, 64, 64, 255).tex(0, top / textureSize).endVertex();
		t.draw();

		// Lower box
		int lowerBoxStart = bottom - bottomMargin;

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		b.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
		b.pos(left, bottom, 0.0D).color(64, 64, 64, 255).tex(0, bottom / textureSize).endVertex();
		b.pos(right, bottom, 0.0D).color(64, 64, 64, 255).tex(right / textureSize, bottom / textureSize).endVertex();
		b.pos(right, lowerBoxStart, 0.0D).color(64, 64, 64, 255).tex(right / textureSize, lowerBoxStart / textureSize)
				.endVertex();
		b.pos(left, lowerBoxStart, 0.0D).color(64, 64, 64, 255).tex(0, lowerBoxStart / textureSize).endVertex();
		t.draw();

		// Gradients
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 0, 1);
		RenderSystem.disableAlphaTest();
		RenderSystem.shadeModel(GL_SMOOTH);
		RenderSystem.disableTexture();
		b.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
		b.pos(left, upperBoxEnd + padding, 0.0D).color(0, 0, 0, 0).tex(0, 1).endVertex();
		b.pos(right, upperBoxEnd + padding, 0.0D).color(0, 0, 0, 0).tex(1, 1).endVertex();
		b.pos(right, upperBoxEnd, 0.0D).color(0, 0, 0, 255).tex(1, 0).endVertex();
		b.pos(left, upperBoxEnd, 0.0D).color(0, 0, 0, 255).tex(0, 0).endVertex();
		t.draw();
		b.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
		b.pos(left, lowerBoxStart, 0.0D).color(0, 0, 0, 255).tex(0, 1).endVertex();
		b.pos(right, lowerBoxStart, 0.0D).color(0, 0, 0, 255).tex(1, 1).endVertex();
		b.pos(right, lowerBoxStart - padding, 0.0D).color(0, 0, 0, 0).tex(1, 0).endVertex();
		b.pos(left, lowerBoxStart - padding, 0.0D).color(0, 0, 0, 0).tex(0, 0).endVertex();
		t.draw();

		RenderSystem.enableTexture();
		RenderSystem.shadeModel(GL_FLAT);
		RenderSystem.enableAlphaTest();
		RenderSystem.disableBlend();
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
	static void glColor4f(float r, float g, float b, float a) {
		RenderSystem.color4f(r, g, b, a);
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#glTranslatef
	 */
	static void glTranslatef(float x, float y, float z) {
		RenderSystem.translatef(x, y, z);
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#applyLinkFormatting
	 */
	static Style createLinkFormatting(String url) {
		return new Style()
				.setColor(TextFormatting.BLUE)
				.setUnderlined(true)
				.setClickEvent(new ClickEvent(Action.OPEN_URL, url));
	}

	/* (non-javadoc)
	 * @See VersionedFunctions#createConfirmScreen
	 */
	static ConfirmScreen createConfirmScreen(BooleanConsumer action, ITextComponent line1,
			ITextComponent line2, ITextComponent confirm, ITextComponent cancel) {
		return new ConfirmScreen(action, line1, line2,
				confirm.getFormattedText(), cancel.getFormattedText());
	}
}
