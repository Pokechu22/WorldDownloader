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

import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ListMultimap;

import net.minecraft.client.gui.GuiButton;
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
import wdl.gui.widget.GuiList;
import wdl.gui.widget.Screen;
import wdl.gui.widget.GuiList.GuiListEntry;
import wdl.gui.widget.SettingButton;

public class GuiWDLMessages extends Screen {
	/**
	 * Set from inner classes; this is the text to draw.
	 */
	@Nullable
	private String hoveredButtonTooltip = null;

	private class GuiMessageTypeList extends GuiList<GuiMessageTypeList.Entry> {
		public GuiMessageTypeList() {
			super(GuiWDLMessages.this.mc, GuiWDLMessages.this.width,
					GuiWDLMessages.this.height, 39,
					GuiWDLMessages.this.height - 32, 20);
		}

		/** Needed for proper generics behavior, unfortunately. */
		private abstract class Entry extends GuiListEntry<Entry> { }

		private class CategoryEntry extends Entry {
			private final SettingButton button;
			private final MessageTypeCategory category;

			public CategoryEntry(MessageTypeCategory category) {
				this.category = category;
				this.button = this.addButton(new SettingButton(
						category.setting, config, 0, 0, 80, 20), 20, 0);
			}

			@Override
			public void drawEntry(int x, int y, int width, int height, int mouseX, int mouseY) {
				button.enabled = config.getValue(MessageSettings.ENABLE_ALL_MESSAGES);

				super.drawEntry(x, y, width, height, mouseX, mouseY);

				drawCenteredString(fontRenderer, category.getDisplayName().getFormattedText(),
						GuiWDLMessages.this.width / 2 - 40, y + slotHeight
						- mc.fontRenderer.FONT_HEIGHT - 1, 0xFFFFFF);

				if (button.isMouseOver()) {
					hoveredButtonTooltip = button.getTooltip();
				}
			}
		}

		private class MessageTypeEntry extends Entry {
			private final SettingButton button;
			private final MessageRegistration typeRegistration;

			public MessageTypeEntry(MessageRegistration registration) {
				this.typeRegistration = registration;
				this.button = this.addButton(new SettingButton(
						registration.setting, config, 0, 0), -100, 0);
			}

			@Override
			public void drawEntry(int x, int y, int width, int height, int mouseX, int mouseY) {
				button.enabled = config.getValue(typeRegistration.category.setting);

				super.drawEntry(x, y, width, height, mouseX, mouseY);

				if (button.isMouseOver()) {
					hoveredButtonTooltip = button.getTooltip();
				}
			}
		}

		// The call to Stream.concat is somewhat hacky, but hard to avoid
		// (we want both a header an the items in it)
		{
			WDLMessages.getRegistrations().asMap().entrySet().stream()
				.flatMap(e -> Stream.concat(
						Stream.of(new CategoryEntry(e.getKey())),
						e.getValue().stream().map(MessageTypeEntry::new)))
				.forEach(getEntries()::add);
		}

	}

	private final GuiScreen parent;
	private final IConfiguration config;

	public GuiWDLMessages(GuiScreen parent) {
		this.parent = parent;
		this.config = WDL.baseProps;
	}

	private SettingButton enableAllButton;
	private GuiButton resetButton;

	private static final int ID_RESET_ALL = 101;

	@Override
	public void initGui() {
		enableAllButton = this.addButton(new SettingButton(
				MessageSettings.ENABLE_ALL_MESSAGES, this.config,
				(this.width / 2) - 155, 18, 150, 20));
		resetButton = this.addButton(new ButtonDisplayGui(
				(this.width / 2) + 5, 18, 150, 20,
				I18n.format("wdl.gui.messages.reset"),
				() -> new GuiYesNo(this,
						I18n.format("wdl.gui.messages.reset.confirm.title"),
						I18n.format("wdl.gui.messages.reset.confirm.subtitle"),
						ID_RESET_ALL)));

		this.addList(new GuiMessageTypeList());

		this.addButton(new ButtonDisplayGui((this.width / 2) - 100, this.height - 29,
				200, 20, this.parent));
	}

	@Override
	public void confirmResult(boolean result, int id) {
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

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		hoveredButtonTooltip = null;

		this.drawDefaultBackground();
		super.render(mouseX, mouseY, partialTicks);

		this.drawCenteredString(this.fontRenderer,
				I18n.format("wdl.gui.messages.message.title"),
				this.width / 2, 8, 0xFFFFFF);

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
