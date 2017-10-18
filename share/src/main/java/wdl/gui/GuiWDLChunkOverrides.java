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

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import wdl.WDL;
import wdl.WDLPluginChannels;
import wdl.WDLPluginChannels.ChunkRange;

import com.google.common.collect.Multimap;

/**
 * A GUI that lists and allows requesting chunk overrides.
 *
 * Also, expect a possible minimap integration in the future.
 */
public class GuiWDLChunkOverrides extends GuiScreen {
	private static final int TOP_MARGIN = 61, BOTTOM_MARGIN = 32;

	/**
	 * Location of the button overlay textures.
	 */
	private static final ResourceLocation WIDGET_TEXTURES = new ResourceLocation(
			"wdl:textures/permission_widgets.png");

	private static enum Mode {
		PANNING(0, 128),
		REQUESTING(16, 128),
		ERASING(32, 128),
		MOVING(48, 128);

		private Mode(int overlayU, int overlayV) {
			this.overlayU = overlayU;
			this.overlayV = overlayV;
		}

		/**
		 * Coordinates for the U and V of the texture for the button associated
		 * with this mode.
		 */
		public final int overlayU, overlayV;
	}

	/**
	 * Parent GUI screen; displayed when this GUI is closed.
	 */
	private final GuiScreen parent;

	private GuiButton startDownloadButton;

	/**
	 * The current position.
	 */
	private float scrollX, scrollZ;
	/**
	 * How large each chunk is on-screen.
	 */
	private static final int SCALE = 8;

	/**
	 * Current mode for the GUI
	 */
	private Mode mode = Mode.PANNING;

	/**
	 * Is the request end coordinate being set (true) or start coordinate being
	 * set (false)?
	 */
	private boolean partiallyRequested;
	/**
	 * Coordinates of the active request.
	 */
	private int requestStartX, requestStartZ, requestEndX, requestEndZ;
	/**
	 * Is the background being dragged?
	 */
	private boolean dragging;
	/**
	 * The position of the mouse on the last tick, for dragging.
	 */
	private int lastTickX, lastTickY;

	public GuiWDLChunkOverrides(GuiScreen parent) {
		this.parent = parent;

		if (WDL.thePlayer != null) {
			this.scrollX = WDL.thePlayer.chunkCoordX;
			this.scrollZ = WDL.thePlayer.chunkCoordZ;
		}
	}

	@Override
	public void initGui() {
		this.buttonList.add(new RequestModeButton(0, width / 2 - 155, 18, Mode.PANNING));
		this.buttonList.add(new RequestModeButton(1, width / 2 - 130, 18, Mode.REQUESTING));
		this.buttonList.add(new RequestModeButton(2, width / 2 - 105, 18, Mode.ERASING) {{ enabled = false; }});
		this.buttonList.add(new RequestModeButton(3, width / 2 - 80, 18, Mode.MOVING) {{ enabled = false; }});

		this.buttonList.add(new GuiButton(4, width / 2 - 80, 18, 80, 20,
				"Send request"));

		startDownloadButton = new GuiButton(6, width / 2 + 5, 18, 150, 20,
				"Start download in these ranges");
		startDownloadButton.enabled = WDLPluginChannels.canDownloadAtAll();
		this.buttonList.add(startDownloadButton);

		this.buttonList.add(new GuiButton(100, width / 2 - 100, height - 29,
				I18n.format("gui.done")));

		this.buttonList.add(new GuiButton(200, this.width / 2 - 155, 39, 100, 20,
				I18n.format("wdl.gui.permissions.current")));
		this.buttonList.add(new GuiButton(201, this.width / 2 - 50, 39, 100, 20,
				I18n.format("wdl.gui.permissions.request")));
		this.buttonList.add(new GuiButton(202, this.width / 2 + 55, 39, 100, 20,
				I18n.format("wdl.gui.permissions.overrides")));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			mode = Mode.PANNING;
		}
		if (button.id == 1) {
			mode = Mode.REQUESTING;
			partiallyRequested = false;
		}
		if (button.id == 4) {
			WDLPluginChannels.sendRequests();
		}
		if (button.id == 6) {
			if (!WDLPluginChannels.canDownloadAtAll()) {
				button.enabled = false;
				return;
			}
			WDL.startDownload();
		}

		if (button.id == 100) {
			this.mc.displayGuiScreen(this.parent);
		}

		if (button.id == 200) {
			this.mc.displayGuiScreen(new GuiWDLPermissions(this.parent));
		}
		if (button.id == 201) {
			this.mc.displayGuiScreen(new GuiWDLPermissionRequest(this.parent));
		}
		if (button.id == 202) {
			// Would open this GUI; do nothing.
		}
	}

	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
			throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (mouseY > TOP_MARGIN && mouseY < height - BOTTOM_MARGIN && mouseButton == 0) {
			switch (mode) {
			case PANNING:
				dragging = true;
				lastTickX = mouseX;
				lastTickY = mouseY;
				break;
			case REQUESTING:
				if (partiallyRequested) {
					requestEndX = displayXToChunkX(mouseX);
					requestEndZ = displayZToChunkZ(mouseY);

					ChunkRange requestRange = new ChunkRange("", requestStartX,
							requestStartZ, requestEndX, requestEndZ);
					WDLPluginChannels.addChunkOverrideRequest(requestRange);

					partiallyRequested = false;
				} else {
					requestStartX = displayXToChunkX(mouseX);
					requestStartZ = displayZToChunkZ(mouseY);

					partiallyRequested = true;
				}

				mc.getSoundHandler().playSound(
						PositionedSoundRecord.getMasterRecord(
								SoundEvents.UI_BUTTON_CLICK, 1.0F));
				break;
			case ERASING:
				// TODO
				mc.getSoundHandler().playSound(
						PositionedSoundRecord.getMasterRecord(
								SoundEvents.BLOCK_DISPENSER_FAIL, 1.0F));
				break;
			case MOVING:
				// TODO
				mc.getSoundHandler().playSound(
						PositionedSoundRecord.getMasterRecord(
								SoundEvents.BLOCK_DISPENSER_FAIL, 1.0F));
				break;
			}
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		if (state == 0) {
			dragging = false;
		}
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY,
			int clickedMouseButton, long timeSinceLastClick) {
		if (dragging) {
			int deltaX = lastTickX - mouseX;
			int deltaY = lastTickY - mouseY;

			lastTickX = mouseX;
			lastTickY = mouseY;

			scrollX += deltaX / (float)SCALE;
			scrollZ += deltaY / (float)SCALE;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		LocalUtils.drawDarkBackground(0, 0, height, width);

		// Draw the current request range.
		if (mode == Mode.REQUESTING) {
			int x1 = (partiallyRequested ? requestStartX : displayXToChunkX(mouseX));
			int z1 = (partiallyRequested ? requestStartZ : displayZToChunkZ(mouseY));
			int x2 = displayXToChunkX(mouseX);
			int z2 = displayZToChunkZ(mouseY);

			// TODO: Maybe cache this range in some way rather than creating a new one each frame
			ChunkRange requestRange = new ChunkRange("", x1, z1, x2, z2);

			// Fancy sin alpha changing by time.
			int alpha = 127 + (int)(Math.sin(Minecraft.getSystemTime() * Math.PI / 5000) * 64);
			drawRange(requestRange, 0xffffff, alpha);
		}

		// Draw current ranges
		for (Multimap<String, ChunkRange> group : WDLPluginChannels.getChunkOverrides().values()) {
			for (ChunkRange range : group.values()) {
				drawRange(range, RNG_SEED, 0xFF);
			}
		}
		for (ChunkRange range : WDLPluginChannels.getChunkOverrideRequests()) {
			// Fancy sin alpha changing by time.
			int alpha = 127 + (int)(Math.sin(Minecraft.getSystemTime() * Math.PI / 5000) * 64);
			drawRange(range, 0x808080, alpha);
		}

		// Player position.
		int playerPosX = (int)(((WDL.thePlayer.posX / 16.0D) - scrollX) * SCALE + (width / 2));
		int playerPosZ = (int)(((WDL.thePlayer.posZ / 16.0D) - scrollZ) * SCALE + (height / 2));

		drawHorizontalLine(playerPosX - 3, playerPosX + 3, playerPosZ, 0xFFFFFFFF);
		// Vertical is 1px taller because it seems to be needed to make it proportional
		drawVerticalLine(playerPosX, playerPosZ - 4, playerPosZ + 4, 0xFFFFFFFF);

		// Draw the main borders now so that ranges are hidden behind it.
		Utils.drawBorder(TOP_MARGIN, BOTTOM_MARGIN, 0, 0, height, width);

		this.drawCenteredString(this.fontRenderer, "Chunk overrides",
				this.width / 2, 8, 0xFFFFFF);

		super.drawScreen(mouseX, mouseY, partialTicks);

		this.drawCenteredString(this.fontRenderer, "\u00A7c\u00A7lThis is a work in progress.",
				this.width / 2, this.height / 2, 0xFFFFFF);
	}

	/**
	 * Default color for a chunk range with tag. Xor'd with the hashcode. <br/>
	 * Preview: <span style=
	 * "width: 100px; height: 50px; background-color: #0BBDFC; color: #0BBDFC;"
	 * >&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
	 */
	private static final int RNG_SEED = 0xBBDFC;

	/**
	 * Draws the given range at the proper position on screen.
	 *
	 * @param range The range to draw.
	 * @param seed The default color for a tagless range. (See {@link #RNG_SEED})
	 * @param alpha The transparency.  0xFF: Fully solid, 0x00: Fully transparent
	 */
	private void drawRange(ChunkRange range, int seed, int alpha) {
		int color = (range.tag.hashCode() ^ seed) & 0x00FFFFFF;

		int x1 = chunkXToDisplayX(range.x1);
		int z1 = chunkZToDisplayZ(range.z1);
		int x2 = chunkXToDisplayX(range.x2) + SCALE - 1;
		int z2 = chunkZToDisplayZ(range.z2) + SCALE - 1;

		drawRect(x1, z1, x2, z2, color + (alpha << 24));

		int colorDark = darken(color);

		drawVerticalLine(x1, z1, z2, colorDark + (alpha << 24));
		drawVerticalLine(x2, z1, z2, colorDark + (alpha << 24));
		drawHorizontalLine(x1, x2, z1, colorDark + (alpha << 24));
		drawHorizontalLine(x1, x2, z2, colorDark + (alpha << 24));
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

	/**
	 * Button for a mode that displays the icon for the given mode.
	 */
	private class RequestModeButton extends ExtButton {
		/**
		 * The mode for this button.
		 */
		public final Mode mode;

		/**
		 * Constructor
		 * @param buttonId
		 * @param x
		 * @param y
		 * @param mode
		 */
		public RequestModeButton(int buttonId, int x, int y, Mode mode) {
			super(buttonId, x, y, 20, 20, "");
			this.mode = mode;
		}

		@Override
		public void beforeDraw() {
			if (GuiWDLChunkOverrides.this.mode == this.mode) {
				// Mode is currently selected - draw a green outline.
				drawRect(this.x - 2, this.y - 2,
						this.x + width + 2, this.y + height + 2,
						0xFF007F00);
			}
		}

		@Override
		public void afterDraw() {
			// Reset the color, which gets set somewhere (probably when drawing text)
			GlStateManager.color(1.0f, 1.0f, 1.0f);
			mc.getTextureManager().bindTexture(WIDGET_TEXTURES);

			this.drawTexturedModalRect(this.x + 2, this.y + 2,
					mode.overlayU, mode.overlayV, 16, 16);
		}
	}
}
