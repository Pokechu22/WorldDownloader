/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
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
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ListMultimap;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;
import wdl.MessageTypeCategory;
import wdl.WDL;
import wdl.WDLMessages;
import wdl.WDLMessages.MessageRegistration;
import wdl.config.IConfiguration;
import wdl.config.settings.MessageSettings;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.GuiListEntry;
import wdl.gui.widget.SettingButton;

public class GuiWDLMessages extends GuiScreen {
	/**
	 * Set from inner classes; this is the text to draw.
	 */
	@Nullable
	private String hoveredButtonTooltip = null;

	private class GuiMessageTypeList extends GuiListExtended {
		public GuiMessageTypeList() {
			super(GuiWDLMessages.this.mc, GuiWDLMessages.this.width,
					GuiWDLMessages.this.height, 39,
					GuiWDLMessages.this.height - 32, 20);
		}

		private class CategoryEntry extends GuiListEntry {
			private final SettingButton button;
			private final MessageTypeCategory category;

			public CategoryEntry(MessageTypeCategory category) {
				this.category = category;
				this.button = new SettingButton(0, category.setting, config, 0, 0, 80, 20);
			}

			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				drawCenteredString(fontRenderer, category.getDisplayName(),
						GuiWDLMessages.this.width / 2 - 40, y + slotHeight
						- mc.fontRenderer.FONT_HEIGHT - 1, 0xFFFFFF);

				button.x = GuiWDLMessages.this.width / 2 + 20;
				button.y = y;

				button.enabled = config.getValue(MessageSettings.ENABLE_ALL_MESSAGES);

				LocalUtils.drawButton(this.button, mc, mouseX, mouseY);

				if (button.isMouseOver()) {
					hoveredButtonTooltip = button.getTooltip();
				}
			}

			@Override
			public boolean mousePressed(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				if (button.mousePressed(mc, x, y)) {
					button.playPressSound(mc.getSoundHandler());

					return true;
				}

				return false;
			}

			@Override
			public void mouseReleased(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {

			}
		}

		private class MessageTypeEntry extends GuiListEntry {
			private final SettingButton button;
			private final MessageRegistration typeRegistration;

			public MessageTypeEntry(MessageRegistration registration) {
				this.typeRegistration = registration;
				this.button = new SettingButton(0, registration.setting, config, 0, 0);
			}

			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				button.x = GuiWDLMessages.this.width / 2 - 100;
				button.y = y;

				button.enabled = config.getValue(typeRegistration.category.setting);

				LocalUtils.drawButton(this.button, mc, mouseX, mouseY);

				if (button.isMouseOver()) {
					hoveredButtonTooltip = button.getTooltip();
				}
			}

			@Override
			public boolean mousePressed(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				if (button.mousePressed(mc, x, y)) {
					button.playPressSound(mc.getSoundHandler());

					return true;
				}

				return false;
			}

			@Override
			public void mouseReleased(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {

			}
		}

		// The call to Stream.concat is somewhat hacky, but hard to avoid
		// (we want both a header an the items in it)
		private List<GuiListEntry> entries = WDLMessages
				.getRegistrations().asMap().entrySet().stream()
				.flatMap(e -> Stream.concat(
						Stream.of(new CategoryEntry(e.getKey())),
						e.getValue().stream().map(MessageTypeEntry::new)))
				.collect(Collectors.toList());

		@Override
		public IGuiListEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}

	}

	private final GuiScreen parent;
	private final IConfiguration config;
	private GuiMessageTypeList list;

	public GuiWDLMessages(GuiScreen parent) {
		this.parent = parent;
		this.config = WDL.baseProps;
	}

	private SettingButton enableAllButton;
	private GuiButton resetButton;

	private static final int ID_RESET_ALL = 101;

	@Override
	public void initGui() {
		enableAllButton = new SettingButton(100, MessageSettings.ENABLE_ALL_MESSAGES, this.config, (this.width / 2) - 155, 18, 150, 20);
		this.buttonList.add(enableAllButton);
		resetButton = new ButtonDisplayGui((this.width / 2) + 5, 18,
				150, 20, I18n.format("wdl.gui.messages.reset"),
				() -> new GuiYesNo(this,
						I18n.format("wdl.gui.messages.reset.confirm.title"),
						I18n.format("wdl.gui.messages.reset.confirm.subtitle"),
						ID_RESET_ALL));
		this.buttonList.add(resetButton);

		this.list = new GuiMessageTypeList();

		this.buttonList.add(new ButtonDisplayGui((this.width / 2) - 100, this.height - 29,
				200, 20, this.parent));
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		if (result) {
			if (id == ID_RESET_ALL) {
				ListMultimap<MessageTypeCategory, MessageRegistration> registrations = WDLMessages.getRegistrations();
				config.clearValue(MessageSettings.ENABLE_ALL_MESSAGES);

				for (MessageTypeCategory cat : registrations.keySet()) {
					config.clearValue(cat.setting);
				}
				for (MessageRegistration r : registrations.values()) {
					config.clearValue(r.setting);
				}
			}
		}

		mc.displayGuiScreen(this);
	}

	@Override
	public void onGuiClosed() {
		WDL.saveProps();
	}

	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.list.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
			throws IOException {
		if (list.mouseClicked(mouseX, mouseY, mouseButton)) {
			return;
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if (list.mouseReleased(mouseX, mouseY, state)) {
			return;
		}
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		hoveredButtonTooltip = null;

		this.drawDefaultBackground();
		this.list.drawScreen(mouseX, mouseY, partialTicks);

		this.drawCenteredString(this.fontRenderer,
				I18n.format("wdl.gui.messages.message.title"),
				this.width / 2, 8, 0xFFFFFF);

		super.drawScreen(mouseX, mouseY, partialTicks);

		String tooltip = null;
		if (hoveredButtonTooltip != null) {
			tooltip = hoveredButtonTooltip;
		} else if (enableAllButton.isMouseOver()) {
			tooltip = enableAllButton.getTooltip();
		} else if (resetButton.isMouseOver()) {
			tooltip = I18n.format("wdl.gui.messages.reset.description");
		}

		if (tooltip != null) {
			Utils.drawGuiInfoBox(tooltip, width, height, 48);
		}
	}
}
