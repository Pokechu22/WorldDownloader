/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui;

import java.util.Collections;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import wdl.WDL;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.Screen;
import wdl.versioned.VersionedFunctions;

/**
 * A GUI that shows chunks that have been already saved.
 */
public class GuiSavedChunks extends Screen {
	private static final int TOP_MARGIN = 61, BOTTOM_MARGIN = 32;

	/**
	 * Parent GUI screen; displayed when this GUI is closed.
	 */
	@Nullable
	private final GuiScreen parent;
	private final WDL wdl;

	/**
	 * The current position.
	 */
	private float scrollX, scrollZ;
	/**
	 * How large each chunk is on-screen.
	 */
	private static final int SCALE = 8;

	/**
	 * The position of the mouse on the last tick, for dragging.
	 */
	private int lastTickX, lastTickY;

	public GuiSavedChunks(@Nullable GuiScreen parent, WDL wdl) {
		this.parent = parent;
		this.wdl = wdl;

		if (wdl.player != null) {
			this.scrollX = wdl.player.chunkCoordX;
			this.scrollZ = wdl.player.chunkCoordZ;
		}
	}

	@Override
	public void initGui() {
		this.addButton(new ButtonDisplayGui(width / 2 - 100, height - 29,
				200, 20, this.parent));
	}

	@Override
	public void mouseDown(int mouseX, int mouseY) {
		if (mouseY > TOP_MARGIN && mouseY < height - BOTTOM_MARGIN) {
			lastTickX = mouseX;
			lastTickY = mouseY;
		}
	}

	@Override
	public void mouseDragged(int mouseX, int mouseY) {
		int deltaX = lastTickX - mouseX;
		int deltaY = lastTickY - mouseY;

		lastTickX = mouseX;
		lastTickY = mouseY;

		scrollX += deltaX / (float)SCALE;
		scrollZ += deltaY / (float)SCALE;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		VersionedFunctions.drawDarkBackground(0, 0, height, width);

		// Old chunks (currently not implemented)
		for (ChunkPos pos : Collections.<ChunkPos>emptySet()) {
			drawChunk(pos, 0xFFFF0000);
		}
		// Chunks that have been saved already
		for (ChunkPos pos : wdl.savedChunks) {
			drawChunk(pos, 0xFF00FF00);
		}

		// Player position.
		int playerPosX = (int)(((wdl.player.posX / 16.0D) - scrollX) * SCALE + (width / 2));
		int playerPosZ = (int)(((wdl.player.posZ / 16.0D) - scrollZ) * SCALE + (height / 2));

		drawHorizontalLine(playerPosX - 3, playerPosX + 3, playerPosZ, 0xFFFFFFFF);
		// Vertical is 1px taller because it seems to be needed to make it proportional
		drawVerticalLine(playerPosX, playerPosZ - 4, playerPosZ + 4, 0xFFFFFFFF);

		// Draw the main borders now so that positions are hidden behind it.
		Utils.drawBorder(TOP_MARGIN, BOTTOM_MARGIN, 0, 0, height, width);

		this.drawCenteredString(this.fontRenderer, "Saved chunks",
				this.width / 2, 8, 0xFFFFFF);

		super.render(mouseX, mouseY, partialTicks);
	}

	private void drawChunk(ChunkPos pos, int color) {
		int x1 = chunkXToDisplayX(pos.x);
		int z1 = chunkZToDisplayZ(pos.z);
		int x2 = x1 + SCALE - 1;
		int z2 = z1 + SCALE - 1;

		drawRect(x1, z1, x2, z2, color);

		int colorDark = darken(color);

		drawVerticalLine(x1, z1, z2, colorDark);
		drawVerticalLine(x2, z1, z2, colorDark);
		drawHorizontalLine(x1, x2, z1, colorDark);
		drawHorizontalLine(x1, x2, z2, colorDark);
	}

	/**
	 * Converts a chunk x coordinate to a display x coordinate, taking
	 * into account the value of {@link scrollX}.
	 *
	 * @param chunkX The chunk's x coordinate.
	 * @return The display position.
	 */
	private int chunkXToDisplayX(int chunkX) {
		return (int)((chunkX - scrollX) * SCALE + (width / 2));
	}

	/**
	 * Converts a chunk z coordinate to a display z coordinate, taking
	 * into account the value of {@link scrollZ}.
	 *
	 * @param chunkZ The chunk's z coordinate.
	 * @return The display position.
	 */
	private int chunkZToDisplayZ(int chunkZ) {
		return (int)((chunkZ - scrollZ) * SCALE + (height / 2));
	}

	/**
	 * Converts a display x coordinate to a chunk x coordinate, taking
	 * into account the value of {@link scrollX}.
	 *
	 * @param displayX The display x coordinate.
	 * @return The chunk position.
	 */
	private int displayXToChunkX(int displayX) {
		return MathHelper.floor((displayX - (float)(width / 2)) / SCALE + scrollX);
	}

	/**
	 * Converts a display z coordinate to a chunk z coordinate, taking
	 * into account the value of {@link scrollZ}.
	 *
	 * @param displayZ The display z coordinate.
	 * @return The chunk position.
	 */
	private int displayZToChunkZ(int displayZ) {
		return MathHelper.floor((displayZ - (float)(height / 2)) / SCALE + scrollZ);
	}

	/**
	 * Halves the brightness of the given color.
	 */
	private int darken(int color) {
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;

		r /= 2;
		g /= 2;
		b /= 2;

		return (r << 16) + (g << 8) + b;
	}
}
