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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import wdl.EntityUtils;
import wdl.WDL;
import wdl.WDLMessageTypes;
import wdl.WDLMessages;
import wdl.config.IConfiguration;
import wdl.config.settings.EntitySettings;
import wdl.config.settings.EntitySettings.TrackDistanceMode;
import wdl.gui.widget.GuiListEntry;
import wdl.gui.widget.GuiSlider;
import wdl.gui.widget.SettingButton;

import com.google.common.collect.Multimap;

/**
 * GUI that controls what entities are saved.
 */
public class GuiWDLEntities extends GuiScreen {
	private class GuiEntityList extends GuiListExtended {
		/**
		 * Width of the largest entry.
		 */
		private int largestWidth;
		/**
		 * Width of an entire entry.
		 *
		 * Equal to largestWidth + 255.
		 */
		private int totalWidth;

		private final List<GuiListEntry> entries;
		{
			entries = new ArrayList<>();
			try {
				int largestWidthSoFar = 0;

				Multimap<String, String> entities = EntityUtils
						.getEntitiesByGroup();

				// Partially sort map items so that the basic things are
				// near the top. In some cases, there will be more items
				// than just "Passive"/"Hostile"/"Other", which we want
				// further down, but for Passive/Hostile/Other it's better
				// to have it in consistent places.
				List<String> categories = new ArrayList<>(entities.keySet());
				categories.remove("Passive");
				categories.remove("Hostile");
				categories.remove("Other");
				Collections.sort(categories);
				categories.add(0, "Hostile");
				categories.add(1, "Passive");
				categories.add("Other");

				for (String category : categories) {
					CategoryEntry categoryEntry = new CategoryEntry(category);
					entries.add(categoryEntry);

					List<String> categoryEntities = new ArrayList<>(
							entities.get(category));
					Collections.sort(categoryEntities);

					for (String entity : categoryEntities) {
						entries.add(new EntityEntry(categoryEntry, entity));

						int width = fontRenderer.getStringWidth(entity);
						if (width > largestWidthSoFar) {
							largestWidthSoFar = width;
						}
					}
				}

				largestWidth = largestWidthSoFar;
				totalWidth = largestWidth + 255;
			} catch (Exception e) {
				WDLMessages.chatMessageTranslated(WDL.baseProps,
						WDLMessageTypes.ERROR, "wdl.messages.generalError.failedToSetUpEntityUI", e);

				Minecraft.getMinecraft().displayGuiScreen(null);
			}
		}

		/**
		 * Provides a label.
		 *
		 * Based off of
		 * {@link net.minecraft.client.gui.GuiKeyBindingList.CategoryEntry}.
		 */
		private class CategoryEntry extends GuiListEntry {
			private final String group;
			private final String displayGroup;
			private final int labelWidth;

			private final GuiButton enableGroupButton;

			private boolean groupEnabled;

			public CategoryEntry(String group) {
				this.group = group;
				this.displayGroup = EntityUtils.getDisplayGroup(group);
				this.labelWidth = mc.fontRenderer.getStringWidth(displayGroup);

				this.groupEnabled = config.isEntityGroupEnabled(group);

				this.enableGroupButton = new GuiButton(0, 0, 0, 90, 18,
						getButtonText());
			}

			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				mc.fontRenderer.drawString(this.displayGroup, (x + 110 / 2)
						- (this.labelWidth / 2), y + slotHeight
						- mc.fontRenderer.FONT_HEIGHT - 1, 0xFFFFFF);

				this.enableGroupButton.x = x + 110;
				this.enableGroupButton.y = y;
				this.enableGroupButton.displayString = getButtonText();

				LocalUtils.drawButton(this.enableGroupButton, mc, mouseX, mouseY);
			}

			@Override
			public boolean mousePressed(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				if (enableGroupButton.mousePressed(mc, x, y)) {
					groupEnabled ^= true;

					enableGroupButton.playPressSound(mc.getSoundHandler());

					this.enableGroupButton.displayString = getButtonText();

					config.setEntityGroupEnabled(group, groupEnabled);
					return true;
				}
				return false;
			}

			@Override
			public void mouseReleased(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
			}

			boolean isGroupEnabled() {
				return groupEnabled;
			}

			/**
			 * Gets the text for the on/off button.
			 */
			private String getButtonText() {
				if (groupEnabled) {
					return I18n.format("wdl.gui.entities.group.enabled");
				} else {
					return I18n.format("wdl.gui.entities.group.disabled");
				}
			}
		}

		/**
		 * Contains an actual entity's data.
		 *
		 * Based off of
		 * {@link net.minecraft.client.gui.GuiKeyBindingList.KeyEntry}.
		 */
		private class EntityEntry extends GuiListEntry {
			private final CategoryEntry category;
			private final String entity;
			private final String displayEntity;

			private final GuiButton onOffButton;
			private final GuiSlider rangeSlider;

			private boolean entityEnabled;
			private int range;

			private TrackDistanceMode cachedMode; // XXX this is an ugly hack

			public EntityEntry(CategoryEntry category, String entity) {
				this.category = category;
				this.entity = entity;
				this.displayEntity = EntityUtils.getDisplayType(entity);

				entityEnabled = config.isEntityTypeEnabled(entity);
				range = EntityUtils.getEntityTrackDistance(entity);

				this.onOffButton = new GuiButton(0, 0, 0, 75, 18,
						getButtonText());
				this.onOffButton.enabled = category.isGroupEnabled();

				this.rangeSlider = new GuiSlider(1, 0, 0, 150, 18,
						"wdl.gui.entities.trackDistance", range, 256);

				this.cachedMode = config.getValue(EntitySettings.TRACK_DISTANCE_MODE);

				rangeSlider.enabled = (cachedMode == TrackDistanceMode.USER);
			}

			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				//Center for everything but the labels.
				int center = (GuiWDLEntities.this.width / 2) - (totalWidth / 2)
						+ largestWidth + 10;

				mc.fontRenderer.drawString(this.displayEntity,
						center - largestWidth - 10, y + slotHeight / 2 -
						mc.fontRenderer.FONT_HEIGHT / 2, 0xFFFFFF);

				this.onOffButton.x = center;
				this.onOffButton.y = y;
				this.onOffButton.enabled = category.isGroupEnabled();
				this.onOffButton.displayString = getButtonText();

				this.rangeSlider.x = center + 85;
				this.rangeSlider.y = y;

				// XXX calculating this each time
				TrackDistanceMode mode = config.getValue(EntitySettings.TRACK_DISTANCE_MODE);
				if (this.cachedMode != mode) {
					cachedMode = mode;
					rangeSlider.enabled = canEditRanges();

					rangeSlider.setValue(EntityUtils
							.getEntityTrackDistance(entity));
				}

				LocalUtils.drawButton(onOffButton, mc, mouseX, mouseY);
				LocalUtils.drawButton(rangeSlider, mc, mouseX, mouseY);
			}

			@Override
			public boolean mousePressed(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				if (onOffButton.mousePressed(mc, x, y)) {
					entityEnabled ^= true;

					onOffButton.playPressSound(mc.getSoundHandler());
					onOffButton.displayString = getButtonText();

					config.setEntityTypeEnabled(entity, entityEnabled);
					return true;
				}
				if (rangeSlider.mousePressed(mc, x, y)) {
					range = rangeSlider.getValue();

					config.setUserEntityTrackDistance(entity, range);

					return true;
				}

				return false;
			}

			@Override
			public void mouseReleased(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				rangeSlider.mouseReleased(x, y);

				if (this.cachedMode == TrackDistanceMode.USER) {
					range = rangeSlider.getValue();

					config.setUserEntityTrackDistance(entity, range);
				}
			}

			@Override
			public void setSelected(int p_178011_1_, int p_178011_2_,
					int p_178011_3_) {
			}

			/**
			 * Gets the text for the on/off button.
			 */
			private String getButtonText() {
				if (category.isGroupEnabled() && entityEnabled) {
					return I18n.format("wdl.gui.entities.entity.included");
				} else {
					return I18n.format("wdl.gui.entities.entity.ignored");
				}
			}
		}

		public GuiEntityList() {
			super(GuiWDLEntities.this.mc, GuiWDLEntities.this.width,
					GuiWDLEntities.this.height, 39,
					GuiWDLEntities.this.height - 32, 20);
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
		protected int getScrollBarX() {
			return (GuiWDLEntities.this.width) / 2 + (totalWidth / 2) + 10;
		}
	}

	private GuiEntityList entityList;
	private final GuiScreen parent;
	private final IConfiguration config;

	private SettingButton rangeModeButton;
	private GuiButton presetsButton;

	public GuiWDLEntities(GuiScreen parent) {
		this.parent = parent;
		this.config = WDL.worldProps;
	}

	@Override
	public void initGui() {
		this.buttonList.add(new GuiButton(200, this.width / 2 - 100,
				this.height - 29, "OK"));

		rangeModeButton = new SettingButton(100, EntitySettings.TRACK_DISTANCE_MODE, this.config, this.width / 2 - 155, 18, 150, 20);
		presetsButton = new GuiButton(101, this.width / 2 + 5, 18, 150, 20,
				I18n.format("wdl.gui.entities.rangePresets"));

		this.presetsButton.enabled = this.canEditRanges();

		this.buttonList.add(rangeModeButton);
		this.buttonList.add(presetsButton);

		this.entityList = new GuiEntityList();
	}

	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.entityList.handleMouseInput();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 100) {
			this.presetsButton.enabled = this.canEditRanges();
		}
		if (button.id == 101 && button.enabled) {
			mc.displayGuiScreen(new GuiWDLEntityRangePresets(this, config));
		}
		if (button.id == 200) {
			mc.displayGuiScreen(parent);
		}
	}

	@Override
	public void onGuiClosed() {
		WDL.saveProps();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
			throws IOException {
		if (entityList.mouseClicked(mouseX, mouseY, mouseButton)) {
			return;
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if (entityList.mouseReleased(mouseX, mouseY, state)) {
			return;
		}
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.entityList.drawScreen(mouseX, mouseY, partialTicks);

		this.drawCenteredString(this.fontRenderer,
				I18n.format("wdl.gui.entities.title"), this.width / 2, 8,
				0xFFFFFF);

		if (this.rangeModeButton.isMouseOver()) {
			Utils.drawGuiInfoBox(this.rangeModeButton.getTooltip(), width, height, 48);
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	/**
	 * Returns true if the various controls can be edited on the current mode.
	 */
	private boolean canEditRanges() {
		return config.getValue(EntitySettings.TRACK_DISTANCE_MODE) == TrackDistanceMode.USER;
	}
}
