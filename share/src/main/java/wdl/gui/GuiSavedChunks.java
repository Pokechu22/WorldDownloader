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

import java.io.File;
import java.lang.reflect.Field;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.storage.RegionFile;
import net.minecraft.world.chunk.storage.RegionFileCache;
import wdl.WDL;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.Screen;
import wdl.versioned.VersionedFunctions;

/**
 * A GUI that shows chunks that have been already saved.
 */
public class GuiSavedChunks extends Screen {
	private static final int TOP_MARGIN = 61, BOTTOM_MARGIN = 32;

	private static final int REGION_SIZE = 32;

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

		// Old chunks
		int minX = MathHelper.floor(displayXToChunkX(0) / 32.0);
		int maxX = MathHelper.floor(displayXToChunkX(width) / 32.0);
		int minZ = MathHelper.floor(displayZToChunkZ(0) / 32.0);
		int maxZ = MathHelper.floor(displayZToChunkZ(height) / 32.0);
		for (int rx = minX; rx <= maxX; rx++) {
			for (int rz = minZ; rz <= maxZ; rz++) {
				RegionFile region = loadRegion(rx, rz);
				if (region != null) {
					drawRegion(region, rx, rz);
				}
			}
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

		this.drawCenteredString(this.fontRenderer, I18n.format("wdl.gui.savedChunks.title"),
				this.width / 2, 8, 0xFFFFFF);

		if (mouseY > TOP_MARGIN && mouseY < height - BOTTOM_MARGIN) {
			int x = displayXToChunkX(mouseX);
			int z = displayZToChunkZ(mouseY);
			RegionFile region = loadRegion(x >> 5, z >> 5);
			int timestamp = 0;
			if (region != null) {
				int[] timestamps = getChunkTimestamps(region);
				timestamp = timestamps[(x & (REGION_SIZE - 1)) + (z & (REGION_SIZE - 1)) * REGION_SIZE];
			}
			if (timestamp != 0) {
				this.drawString(this.fontRenderer,
						I18n.format("wdl.gui.savedChunks.lastSaved", x, z, timestamp * 1000L),
						12, height - 12, 0xFFFFFF);
			} else {
				this.drawString(this.fontRenderer,
						I18n.format("wdl.gui.savedChunks.neverSaved", x, z),
						12, height - 12, 0xFFFFFF);
			}
		}

		super.render(mouseX, mouseY, partialTicks);
	}

	/**
	 * Loads a region file if it exists.
	 *
	 * @param x Region x coordinate (chunk / 32)
	 * @param z Region z coordinate (chunk / 32)
	 * @return The region file if it exists, or else null
	 */
	@Nullable
	private RegionFile loadRegion(int x, int z) {
		// Impl note: not exactly great, probably all this data should be cached.
		// Also, I _really_ need to refactor this world folder stuff...
		File worldFolder = wdl.saveHandler.getWorldDirectory();
		// 1.12- has func_191065_b or getRegionFileIfExists, but 1.13 doesn't,
		// so we get this...
		File regionFolder = new File(worldFolder, "region");
		File region = new File(regionFolder, "r." + x + "." + z + ".mca");
		if (region.exists()) {
			return RegionFileCache.createOrLoadRegionFile(worldFolder, x << 5, z << 5);
		} else {
			return null;
		}
	}

	private static final Field CHUNK_TIMESTAMPS_FIELD;
	static {
		Field chunkTimestampsField = null;
		int n = 0;
		for (Field field : RegionFile.class.getDeclaredFields()) {
			if (field.getType() == int[].class) {
				n++;
				if (n == 2) {
					chunkTimestampsField = field;
					break;
				}
			}
		}
		if (chunkTimestampsField == null) {
			throw new AssertionError("Failed to find chunkTimestamps field (n=" + n + ")");
		}
		CHUNK_TIMESTAMPS_FIELD = chunkTimestampsField;
		CHUNK_TIMESTAMPS_FIELD.setAccessible(true);
	}
	private int[] getChunkTimestamps(RegionFile region) {
		try {
			return (int[])CHUNK_TIMESTAMPS_FIELD.get(region);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private static final int YELLOW_THRESHOLD = 60 * 60 * 24; // 1 day in seconds
	private static final int RED_THRESHOLD = 60 * 60 * 24 * 30; // 1 month

	private void drawRegion(RegionFile region, int regionX, int regionZ) {
		// n.b. Vanilla doesn't read these values at all, which is odd.
		int[] chunkTimestamps = getChunkTimestamps(region);
		int now = (int)(System.currentTimeMillis() / 1000);
		for (int z = 0; z < REGION_SIZE; z++) {
			for (int x = 0; x < REGION_SIZE; x++) {
				int saveTime = chunkTimestamps[x + z * REGION_SIZE];
				if (saveTime == 0) {
					continue;
				}
				int age = now - saveTime; // in seconds
				int r, g;
				// Make the color go from red -> yellow in ~1 day and then
				// yellow -> red in ~1 month
				if (age <= YELLOW_THRESHOLD) {
					r = MathHelper.clamp(0xFF * age / YELLOW_THRESHOLD, 0, 0xFF);
					g = 0xFF;
				} else {
					r = 0xFF;
					g = 0xFF - MathHelper.clamp((age - YELLOW_THRESHOLD) / RED_THRESHOLD, 0, 0xFF);
				}
				int color = 0xFF000000 | r << 16 | g << 8;
				drawChunk(new ChunkPos(x + regionX * REGION_SIZE, z + regionZ * REGION_SIZE), color);
			}
		}
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
