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

import com.google.common.collect.Multimap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import wdl.EntityUtils;
import wdl.WDL;
import wdl.WDLMessageTypes;
import wdl.WDLMessages;
import wdl.config.IConfiguration;
import wdl.config.settings.EntitySettings;
import wdl.config.settings.EntitySettings.TrackDistanceMode;
import wdl.gui.widget.Button;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.GuiList;
import wdl.gui.widget.GuiList.GuiListEntry;
import wdl.gui.widget.GuiSlider;
import wdl.gui.widget.SettingButton;

/**
 * GUI that controls what entities are saved.
 */
public class GuiWDLEntities extends GuiScreen {
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
						.mapToInt(fontRenderer::getStringWidth)
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
				WDLMessages.chatMessageTranslated(WDL.baseProps,
						WDLMessageTypes.ERROR, "wdl.messages.generalError.failedToSetUpEntityUI", e);

				Minecraft.getMinecraft().displayGuiScreen(null);
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

			private final Button enableGroupButton;

			private boolean groupEnabled;

			public CategoryEntry(String group) {
				this.displayGroup = EntityUtils.getDisplayGroup(group);
				this.labelWidth = mc.fontRenderer.getStringWidth(displayGroup);

				this.groupEnabled = config.isEntityGroupEnabled(group);

				this.enableGroupButton = new Button(0, 0, 90, 18, getButtonText()) {
					public @Override void performAction() {
						groupEnabled ^= true;
						this.displayString = getButtonText();
						config.setEntityGroupEnabled(group, groupEnabled);
					}
				};
				this.addButton(enableGroupButton, 0, 0);
			}

			@Override
			public void drawEntry(int x, int y, int width, int height, int mouseX, int mouseY) {
				this.enableGroupButton.displayString = getButtonText();
				super.drawEntry(x, y, width, height, mouseX, mouseY);
				mc.fontRenderer.drawString(this.displayGroup, (x + 110 / 2)
						- (this.labelWidth / 2), y + slotHeight
						- mc.fontRenderer.FONT_HEIGHT - 1, 0xFFFFFF);
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

			private final Button onOffButton;
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

				this.onOffButton = new Button(0, 0, 75, 18, getButtonText()) {
					public @Override void performAction() {
						entityEnabled ^= true;
						onOffButton.displayString = getButtonText();
						config.setEntityTypeEnabled(entity, entityEnabled);
					}
				};
				this.onOffButton.enabled = category.isGroupEnabled();
				this.addButton(onOffButton, buttonOffset, 0);

				this.rangeSlider = new GuiSlider(0, 0, 150, 18,
						"wdl.gui.entities.trackDistance", range, 256);
				this.addButton(rangeSlider, buttonOffset + 85, 0);

				this.cachedMode = config.getValue(EntitySettings.TRACK_DISTANCE_MODE);

				rangeSlider.enabled = (cachedMode == TrackDistanceMode.USER);
			}

			@Override
			public void drawEntry(int x, int y, int width, int height, int mouseX, int mouseY) {
				this.onOffButton.enabled = category.isGroupEnabled();
				this.onOffButton.displayString = getButtonText();

				// XXX calculating this each time
				TrackDistanceMode mode = config.getValue(EntitySettings.TRACK_DISTANCE_MODE);
				if (this.cachedMode != mode) {
					cachedMode = mode;
					rangeSlider.enabled = canEditRanges();

					rangeSlider.setValue(EntityUtils
							.getEntityTrackDistance(entity));
				}

				super.drawEntry(x, y, width, height, mouseX, mouseY);

				mc.fontRenderer.drawString(this.displayEntity,
						x, y + height / 2 - mc.fontRenderer.FONT_HEIGHT / 2, 0xFFFFFF);
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
			super(GuiWDLEntities.this.mc, GuiWDLEntities.this.width,
					GuiWDLEntities.this.height, 39,
					GuiWDLEntities.this.height - 32, 20);
		}

		@Override
		public int getEntryWidth() {
			return totalWidth;
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
		this.buttonList.add(new ButtonDisplayGui(this.width / 2 - 100, this.height - 29,
				200, 20, this.parent));

		rangeModeButton = new SettingButton(EntitySettings.TRACK_DISTANCE_MODE, this.config,
				this.width / 2 - 155, 18, 150, 20) {
			public @Override void performAction() {
				super.performAction();
				presetsButton.enabled = canEditRanges();
			}
		};
		presetsButton = new ButtonDisplayGui(this.width / 2 + 5, 18, 150, 20,
				I18n.format("wdl.gui.entities.rangePresets"), () -> new GuiWDLEntityRangePresets(this, config));

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
