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
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.TextFormatting;
import wdl.api.IWDLModWithGui;
import wdl.api.WDLApi;
import wdl.api.WDLApi.ModInfo;

/**
 * GUI showing the currently enabled mods, and their information.
 *
 * It's composed of two halves, one that lists enabled extensions that can
 * be clicked, and the other that shows the details on the selected extension.
 * The two halves can be dragged up and down (which is why the logic is so
 * complex here; {@link GuiListExtended} was not designed for that).
 *
 * @author Pokechu22
 */
public class GuiWDLExtensions extends GuiScreen {
	/**
	 * Top of the bottom list.
	 */
	private int bottomLocation;

	/**
	 * Height of the bottom area.
	 */
	private static final int TOP_HEIGHT = 23;
	/**
	 * Height of the middle section.
	 *
	 * Equal to <code>{@link FontRenderer#FONT_HEIGHT} + 10</code>.
	 */
	private static final int MIDDLE_HEIGHT = 19;
	/**
	 * Height of the top area.
	 */
	private static final int BOTTOM_HEIGHT = 32;

	/**
	 * The currently selected mod.
	 */
	private int selectedModIndex = -1;

	private class ModList extends GuiListExtended {
		public ModList() {
			super(GuiWDLExtensions.this.mc, GuiWDLExtensions.this.width,
					bottomLocation, TOP_HEIGHT, bottomLocation, 22);
			this.showSelectionBox = true;
		}

		private class ModEntry extends GuiListEntry {
			public final ModInfo<?> mod;
			/**
			 * Constant information about the extension (name & version)
			 */
			private final String modDescription;
			/**
			 * The {@link #modDescription}, formated depending on whether
			 * the mod is enabled.
			 */
			private String label;
			private GuiButton button;
			private GuiButton disableButton;

			public ModEntry(ModInfo<?> mod) {
				this.mod = mod;
				String name = mod.getDisplayName();
				this.modDescription = I18n.format("wdl.gui.extensions.modVersion",
						name, mod.version);

				if (!mod.isEnabled()) {
					this.label = "" + TextFormatting.GRAY
							+ TextFormatting.ITALIC + modDescription;
				} else {
					this.label = modDescription;
				}

				if (mod.mod instanceof IWDLModWithGui) {
					IWDLModWithGui guiMod = (IWDLModWithGui) mod.mod;
					String buttonName = (guiMod).getButtonName();
					if (buttonName == null || buttonName.isEmpty()) {
						buttonName = I18n.format("wdl.gui.extensions.defaultSettingsButtonText");
					}

					button = new GuiButton(0, 0, 0, 80, 20,
							guiMod.getButtonName());
				}

				disableButton = new GuiButton(0, 0, 0, 80, 20,
						I18n.format("wdl.gui.extensions."
								+ (mod.isEnabled() ? "enabled" : "disabled")));
			}

			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				if (button != null) {
					button.x = GuiWDLExtensions.this.width - 180;
					button.y = y - 1;

					LocalUtils.drawButton(button, mc, mouseX, mouseY);
				}

				disableButton.x = GuiWDLExtensions.this.width - 92;
				disableButton.y = y - 1;
				LocalUtils.drawButton(disableButton, mc, mouseX, mouseY);

				int centerY = y + slotHeight / 2
						- fontRenderer.FONT_HEIGHT / 2;
				fontRenderer.drawString(label, x, centerY, 0xFFFFFF);
			}

			@Override
			public boolean mousePressed(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				if (button != null) {
					if (button.mousePressed(mc, x, y)) {
						if (mod.mod instanceof IWDLModWithGui) {
							((IWDLModWithGui) mod.mod).openGui(GuiWDLExtensions.this);
						}

						button.playPressSound(mc.getSoundHandler());
						return true;
					}
				}
				if (disableButton.mousePressed(mc, x, y)) {
					mod.toggleEnabled();

					disableButton.playPressSound(mc.getSoundHandler());
					disableButton.displayString = I18n.format("wdl.gui.extensions."
							+ (mod.isEnabled() ? "enabled" : "disabled"));

					if (!mod.isEnabled()) {
						this.label = "" + TextFormatting.GRAY
								+ TextFormatting.ITALIC + modDescription;
					} else {
						this.label = modDescription;
					}
					return true;
				}

				if (selectedModIndex != slotIndex) {
					selectedModIndex = slotIndex;

					mc.getSoundHandler().playSound(
							PositionedSoundRecord.getMasterRecord(
									SoundEvents.UI_BUTTON_CLICK, 1.0F));

					updateDetailsList(mod);

					return true;
				}

				return false;
			}

			@Override
			public void mouseReleased(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				if (button != null) {
					button.mouseReleased(x, y);
				}
			}
		}

		private List<GuiListEntry> entries = WDLApi.getWDLMods().values()
				.stream().map(ModEntry::new).collect(Collectors.toList());

		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			this.height = this.bottom = bottomLocation;

			super.drawScreen(mouseX, mouseY, partialTicks);
		}

		@Override
		public IGuiListEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}

		@Override
		protected boolean isSelected(int slotIndex) {
			return slotIndex == selectedModIndex;
		}

		@Override
		public int getListWidth() {
			return GuiWDLExtensions.this.width - 20;
		}

		@Override
		protected int getScrollBarX() {
			return GuiWDLExtensions.this.width - 10;
		}

		@Override
		public void handleMouseInput() {
			if (mouseY < bottomLocation) {
				super.handleMouseInput();
			}
		}
	}

	private class ModDetailList extends TextList {
		public ModDetailList() {
			super(GuiWDLExtensions.this.mc, GuiWDLExtensions.this.width,
					GuiWDLExtensions.this.height - bottomLocation,
					MIDDLE_HEIGHT, BOTTOM_HEIGHT);
		}

		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			GlStateManager.translate(0, bottomLocation, 0);

			this.height = GuiWDLExtensions.this.height - bottomLocation;
			this.bottom = this.height - 32;

			super.drawScreen(mouseX, mouseY, partialTicks);

			drawCenteredString(fontRenderer,
					I18n.format("wdl.gui.extensions.detailsCaption"),
					GuiWDLExtensions.this.width / 2, 5, 0xFFFFFF);

			GlStateManager.translate(0, -bottomLocation, 0);
		}

		/**
		 * Used by the drawing routine; edited to reduce weirdness.
		 *
		 * (Don't move the bottom with the size of the screen).
		 */
		@Override
		protected void overlayBackground(int y1, int y2,
				int alpha1, int alpha2) {
			if (y1 == 0) {
				super.overlayBackground(y1, y2, alpha1, alpha2);
				return;
			} else {
				GlStateManager.translate(0, -bottomLocation, 0);

				super.overlayBackground(y1 + bottomLocation, y2
						+ bottomLocation, alpha1, alpha2);

				GlStateManager.translate(0, bottomLocation, 0);
			}
		}

		@Override
		public void handleMouseInput() {
			mouseY -= bottomLocation;

			if (mouseY > 0) {
				super.handleMouseInput();
			}

			mouseY += bottomLocation;
		}
	}

	private void updateDetailsList(ModInfo<?> selectedMod) {
		detailsList.clearLines();

		if (selectedMod != null) {
			String info = selectedMod.getInfo();

			detailsList.addLine(info);
		}
	}

	/**
	 * Gui to display after this is closed.
	 */
	private final GuiScreen parent;
	/**
	 * List of mods.
	 */
	private ModList list;
	/**
	 * Details on the selected mod.
	 */
	private ModDetailList detailsList;

	public GuiWDLExtensions(GuiScreen parent) {
		this.parent = parent;
	}

	@Override
	public void initGui() {
		bottomLocation = height - 100;
		dragging = false;

		this.list = new ModList();
		this.detailsList = new ModDetailList();

		this.buttonList.add(new GuiButton(0, width / 2 - 100, height - 29, I18n
				.format("gui.done")));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			mc.displayGuiScreen(parent);
		}
	}

	/**
	 * Whether the center section is being dragged.
	 */
	private boolean dragging = false;
	private int dragOffset;

	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.list.handleMouseInput();
		this.detailsList.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
			throws IOException {
		if (mouseY > bottomLocation && mouseY < bottomLocation + MIDDLE_HEIGHT) {
			dragging = true;
			dragOffset = mouseY - bottomLocation;

			return;
		}

		if (list.mouseClicked(mouseX, mouseY, mouseButton)) {
			return;
		}
		if (detailsList.mouseClicked(mouseX, mouseY, mouseButton)) {
			return;
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		dragging = false;

		if (list.mouseReleased(mouseX, mouseY, state)) {
			return;
		}
		if (detailsList.mouseReleased(mouseX, mouseY, state)) {
			return;
		}
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY,
			int clickedMouseButton, long timeSinceLastClick) {
		if (dragging) {
			bottomLocation = mouseY - dragOffset;
		}

		//Clamp bottomLocation.
		if (bottomLocation < TOP_HEIGHT + 8) {
			bottomLocation = TOP_HEIGHT + 8;
		}
		if (bottomLocation > height - BOTTOM_HEIGHT - 8) {
			bottomLocation = height - BOTTOM_HEIGHT - 8;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();

		//Clamp bottomLocation.
		if (bottomLocation < TOP_HEIGHT + 33) {
			bottomLocation = TOP_HEIGHT + 33;
		}
		if (bottomLocation > height - MIDDLE_HEIGHT - BOTTOM_HEIGHT - 33) {
			bottomLocation = height - MIDDLE_HEIGHT - BOTTOM_HEIGHT - 33;
		}

		this.list.drawScreen(mouseX, mouseY, partialTicks);
		this.detailsList.drawScreen(mouseX, mouseY, partialTicks);

		this.drawCenteredString(this.fontRenderer,
				I18n.format("wdl.gui.extensions.title"), this.width / 2, 8,
				0xFFFFFF);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
