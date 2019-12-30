/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import wdl.EntityUtils;
import wdl.WDL;
import wdl.WDLMessageTypes;
import wdl.WDLMessages;
import wdl.config.IConfiguration;
import wdl.config.settings.EntitySettings;
import wdl.config.settings.EntitySettings.TrackDistanceMode;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.GuiList;
import wdl.gui.widget.GuiList.GuiListEntry;
import wdl.gui.widget.GuiSlider;
import wdl.gui.widget.SettingButton;
import wdl.gui.widget.WDLButton;
import wdl.gui.widget.WDLScreen;

/**
 * GUI that controls what entities are saved.
 */
public class GuiWDLEntities extends WDLScreen {
	private class GuiEntityList extends GuiList<GuiEntityList.Entry> {
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

		{
			List<Entry> entries = this.getEntries();
			try {
				Multimap<String, String> entities = EntityUtils
						.getEntitiesByGroup();
				largestWidth = entities.values().stream()
						.mapToInt(font::getStringWidth)
						.max().orElse(0);
				totalWidth = largestWidth + 255;

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

					entities.get(category).stream()
							.sorted()
							.map(entity -> new EntityEntry(categoryEntry, entity))
							.forEachOrdered(entries::add);
				}
			} catch (Exception e) {
				WDLMessages.chatMessageTranslated(WDL.serverProps,
						WDLMessageTypes.ERROR, "wdl.messages.generalError.failedToSetUpEntityUI", e);

				Minecraft.getInstance().displayGuiScreen(null);
			}
		}

		/** Needed for proper generics behavior, unfortunately. */
		private abstract class Entry extends GuiListEntry<Entry> { }

		/**
		 * Provides a label.
		 *
		 * Based off of
		 * {@link net.minecraft.client.gui.GuiKeyBindingList.CategoryEntry}.
		 */
		private class CategoryEntry extends Entry {
			private final String displayGroup;
			private final int labelWidth;

			private final WDLButton enableGroupButton;

			private boolean groupEnabled;

			public CategoryEntry(String group) {
				this.displayGroup = EntityUtils.getDisplayGroup(group);
				this.labelWidth = minecraft.fontRenderer.getStringWidth(displayGroup);

				this.groupEnabled = config.isEntityGroupEnabled(group);

				this.enableGroupButton = this.addButton(new WDLButton(
						0, 0, 90, 18, getButtonText()) {
					public @Override void performAction() {
						groupEnabled ^= true;
						this.setMessage(getButtonText());
						config.setEntityGroupEnabled(group, groupEnabled);
					}
				}, 0, 0);
			}

			@Override
			public void drawEntry(int x, int y, int width, int height, int mouseX, int mouseY) {
				this.enableGroupButton.setMessage(getButtonText());
				super.drawEntry(x, y, width, height, mouseX, mouseY);
				minecraft.fontRenderer.drawString(this.displayGroup, (x + 110 / 2)
						- (this.labelWidth / 2), y + height
						- minecraft.fontRenderer.FONT_HEIGHT - 1, 0xFFFFFF);
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
		private class EntityEntry extends Entry {
			private final CategoryEntry category;
			private final String entity;
			private final String displayEntity;

			private final WDLButton onOffButton;
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

				int buttonOffset = -(totalWidth / 2) + largestWidth + 10;

				this.onOffButton = this.addButton(new WDLButton(
						0, 0, 75, 18, getButtonText()) {
					public @Override void performAction() {
						entityEnabled ^= true;
						onOffButton.setMessage(getButtonText());
						config.setEntityTypeEnabled(entity, entityEnabled);
					}
				}, buttonOffset, 0);
				this.onOffButton.setEnabled(category.isGroupEnabled());

				this.rangeSlider = this.addButton(new GuiSlider(0, 0, 150, 18,
						"wdl.gui.entities.trackDistance", range, 256),
						buttonOffset + 85, 0);

				this.cachedMode = config.getValue(EntitySettings.TRACK_DISTANCE_MODE);

				rangeSlider.setEnabled(cachedMode == TrackDistanceMode.USER);
			}

			@Override
			public void drawEntry(int x, int y, int width, int height, int mouseX, int mouseY) {
				this.onOffButton.setEnabled(category.isGroupEnabled());
				this.onOffButton.setMessage(getButtonText());

				// XXX calculating this each time
				TrackDistanceMode mode = config.getValue(EntitySettings.TRACK_DISTANCE_MODE);
				if (this.cachedMode != mode) {
					cachedMode = mode;
					rangeSlider.setEnabled(canEditRanges());

					rangeSlider.setValue(EntityUtils
							.getEntityTrackDistance(entity));
				}

				super.drawEntry(x, y, width, height, mouseX, mouseY);

				minecraft.fontRenderer.drawString(this.displayEntity,
						x, y + height / 2 - minecraft.fontRenderer.FONT_HEIGHT / 2, 0xFFFFFF);
			}

			@Override
			public void mouseUp(int mouseX, int mouseY, int mouseButton) {
				super.mouseUp(mouseX, mouseY, mouseButton);
				if (this.cachedMode == TrackDistanceMode.USER) {
					range = rangeSlider.getValue();

					config.setUserEntityTrackDistance(entity, range);
				}
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
			super(GuiWDLEntities.this.minecraft, GuiWDLEntities.this.width,
					GuiWDLEntities.this.height, 39,
					GuiWDLEntities.this.height - 32, 20);
		}

		@Override
		public int getEntryWidth() {
			return totalWidth;
		}
	}

	@Nullable
	private final Screen parent;
	private final WDL wdl;
	private final IConfiguration config;

	private SettingButton rangeModeButton;
	private WDLButton presetsButton;

	public GuiWDLEntities(@Nullable Screen parent, WDL wdl) {
		super("wdl.gui.entities.title");
		this.parent = parent;
		this.wdl = wdl;
		this.config = wdl.worldProps;
	}

	@Override
	public void init() {
		this.addButton(new ButtonDisplayGui(this.width / 2 - 100, this.height - 29,
				200, 20, this.parent));

		rangeModeButton = this.addButton(new SettingButton(
				EntitySettings.TRACK_DISTANCE_MODE, this.config,
				this.width / 2 - 155, 18, 150, 20) {
			public @Override void performAction() {
				super.performAction();
				presetsButton.setEnabled(canEditRanges());
			}
		});
		presetsButton = this.addButton(new ButtonDisplayGui(this.width / 2 + 5, 18, 150, 20,
				I18n.format("wdl.gui.entities.rangePresets"),
				() -> new GuiWDLEntityRangePresets(this, wdl, config)));

		this.presetsButton.setEnabled(this.canEditRanges());

		this.addList(new GuiEntityList());
	}

	@Override
	public void removed() {
		wdl.saveProps();
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		this.renderBackground();

		super.render(mouseX, mouseY, partialTicks);

		if (this.rangeModeButton.isHovered()) {
			Utils.drawGuiInfoBox(this.rangeModeButton.getTooltip(), width, height, 48);
		}
	}

	/**
	 * Returns true if the various controls can be edited on the current mode.
	 */
	private boolean canEditRanges() {
		return config.getValue(EntitySettings.TRACK_DISTANCE_MODE) == TrackDistanceMode.USER;
	}
}
