/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui;

import java.lang.reflect.Field;
import java.nio.IntBuffer;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.storage.RegionFile;
import wdl.WDL;
import wdl.config.settings.MiscSettings;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.WDLScreen;
import wdl.versioned.VersionedFunctions;

/**
 * A GUI that shows chunks that have been already saved.
 */
public class GuiSavedChunks extends WDLScreen {
	private static final int TOP_MARGIN = 36, BOTTOM_MARGIN = 32;

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

	/**
	 * Time in epoch seconds after which a chunk is considered to have been saved
	 * after the world was downloaded.
	 *
	 * We can't just use the LAST_SAVED value because that is calculated before the
	 * currently loaded chunks are saved, so it would always mark those as old.
	 * Instead, we use that time plus a (rather arbitrary) 5 seconds.
	 */
	private final int savedAfterLastDownloadTime;
	private static final int SAVE_TIME_LEWAY = 5;

	public GuiSavedChunks(@Nullable GuiScreen parent, WDL wdl) {
		super("wdl.gui.savedChunks.title");
		this.parent = parent;
		this.wdl = wdl;

		if (wdl.player != null) {
			this.scrollX = wdl.player.chunkCoordX;
			this.scrollZ = wdl.player.chunkCoordZ;
		}

		int saveTime = (int)(wdl.worldProps.getValue(MiscSettings.LAST_SAVED) / 1000);
		this.savedAfterLastDownloadTime = saveTime + SAVE_TIME_LEWAY;
	}

	@Override
	public void init() {
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

		hLine(playerPosX - 3, playerPosX + 3, playerPosZ, 0xFFFFFFFF);
		// Vertical is 1px taller because it seems to be needed to make it proportional
		vLine(playerPosX, playerPosZ - 4, playerPosZ + 4, 0xFFFFFFFF);

		// Draw the main borders now so that positions are hidden behind it.
		Utils.drawBorder(TOP_MARGIN, BOTTOM_MARGIN, 0, 0, height, width);

		if (mouseY > TOP_MARGIN && mouseY < height - BOTTOM_MARGIN) {
			int x = displayXToChunkX(mouseX);
			int z = displayZToChunkZ(mouseY);
			if (wdl.savedChunks.contains(new ChunkPos(x, z))) {
				this.drawString(this.font,
						I18n.format("wdl.gui.savedChunks.savedNow", x, z),
						12, 24, 0xFFFFFF);
			} else {
				RegionFile region = loadRegion(x >> 5, z >> 5);
				int timestamp = 0;
				if (region != null) {
					IntBuffer timestamps = getChunkTimestamps(region);
					timestamp = timestamps.get(computeTimestampIndex(x, z));
				}
				if (timestamp > savedAfterLastDownloadTime) {
					this.drawString(this.font,
							I18n.format("wdl.gui.savedChunks.savedAfterDownload", x, z, timestamp * 1000L),
							12, 24, 0xFFFFFF);
				} else if (timestamp != 0) {
					this.drawString(this.font,
							I18n.format("wdl.gui.savedChunks.lastSaved", x, z, timestamp * 1000L),
							12, 24, 0xFFFFFF);
				} else {
					this.drawString(this.font,
							I18n.format("wdl.gui.savedChunks.neverSaved", x, z),
							12, 24, 0xFFFFFF);
				}
			}
		}

		if (wdl.chunkLoader == null) {
			// XXX Untranslated, temporary strings
			this.drawCenteredString(font, "Start download to see information about saved chunks, from now and earlier.", width / 2, height / 2 - font.FONT_HEIGHT, 0xFFFFFF);
			this.drawCenteredString(font, "In the future, this GUI will work even when downloading hasn't been started.", width / 2, height / 2, 0xFFFFFF);
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
		if (wdl.chunkLoader == null) {
			return null;
		}
		return wdl.chunkLoader.getRegionFileIfExists(x, z);
	}

	/**
	 * Gets the timestamp index corresponding to the given chunk coordinates. The
	 * chunk coordinates are not constrained to 0-31.
	 *
	 * @param x X chunk coordinate
	 * @param z Z chunk coordinate
	 * @return The index into a region file's timestamp array
	 */
	private int computeTimestampIndex(int x, int z) {
		return (x & (REGION_SIZE - 1)) + (z & (REGION_SIZE - 1)) * REGION_SIZE;
	}

	private static final Field CHUNK_TIMESTAMPS_FIELD;
	private static final boolean CHUNK_TIMESTAMPS_IS_INT_BUFFER; // True in 1.15+
	static {
		Field chunkTimestampsField = null;
		boolean isIntBuffer = false;

		int nArray = 0;
		int nBuf = 0;
		for (Field field : RegionFile.class.getDeclaredFields()) {
			if (field.getType() == int[].class) {
				nArray++;
				if (nArray == 2) {
					chunkTimestampsField = field;
					isIntBuffer = false;
					break;
				}
			}
			if (field.getType() == IntBuffer.class) {
				nBuf++;
				if (nBuf == 2) {
					chunkTimestampsField = field;
					isIntBuffer = true;
					break;
				}
			}
		}
		if (chunkTimestampsField == null) {
			throw new AssertionError("Failed to find chunkTimestamps field (nArray=" + nArray + ", nBuf=" + nBuf + ")");
		}
		CHUNK_TIMESTAMPS_FIELD = chunkTimestampsField;
		CHUNK_TIMESTAMPS_FIELD.setAccessible(true);
		CHUNK_TIMESTAMPS_IS_INT_BUFFER = isIntBuffer;
	}
	private IntBuffer getChunkTimestamps(RegionFile region) {
		try {
			Object value = CHUNK_TIMESTAMPS_FIELD.get(region);
			if (CHUNK_TIMESTAMPS_IS_INT_BUFFER) {
				return (IntBuffer)value;
			} else {
				return IntBuffer.wrap((int[])value);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private static final int YELLOW_THRESHOLD = 60 * 60 * 24; // 1 day in seconds
	private static final int RED_THRESHOLD = 60 * 60 * 24 * 30; // 1 month

	private void drawRegion(RegionFile region, int regionX, int regionZ) {
		// n.b. Vanilla doesn't read these values at all, which is odd.
		IntBuffer chunkTimestamps = getChunkTimestamps(region);
		int now = (int)(System.currentTimeMillis() / 1000);
		for (int zOff = 0; zOff < REGION_SIZE; zOff++) {
			for (int xOff = 0; xOff < REGION_SIZE; xOff++) {
				ChunkPos pos = new ChunkPos(xOff + regionX * REGION_SIZE, zOff + regionZ * REGION_SIZE);
				if (wdl.savedChunks.contains(pos)) {
					continue;
				}
				int saveTime = chunkTimestamps.get(computeTimestampIndex(pos.x, pos.z));
				if (saveTime == 0) {
					continue;
				}
				int color;
				if (saveTime > savedAfterLastDownloadTime) {
					// Saved after the previous download finished.  Due to the check for
					// it being in savedChunks, we don't need to worry about chunks saved in this session.
					color = 0xFF404040; // dark gray
				} else {
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
					color = 0xFF000000 | r << 16 | g << 8;
				}
				drawChunk(pos, color);
			}
		}
	}

	private void drawChunk(ChunkPos pos, int color) {
		int x1 = chunkXToDisplayX(pos.x);
		int z1 = chunkZToDisplayZ(pos.z);
		int x2 = x1 + SCALE - 1;
		int z2 = z1 + SCALE - 1;

		fill(x1, z1, x2, z2, color);

		int colorDark = darken(color);

		vLine(x1, z1, z2, colorDark);
		vLine(x2, z1, z2, colorDark);
		hLine(x1, x2, z1, colorDark);
		hLine(x1, x2, z2, colorDark);
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
