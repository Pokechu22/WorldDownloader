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

import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ListMultimap;

import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;
import wdl.MessageTypeCategory;
import wdl.WDL;
import wdl.WDLMessages;
import wdl.WDLMessages.MessageRegistration;
import wdl.config.IConfiguration;
import wdl.config.settings.MessageSettings;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.GuiList;
import wdl.gui.widget.GuiList.GuiListEntry;
import wdl.gui.widget.SettingButton;
import wdl.gui.widget.WDLButton;
import wdl.gui.widget.WDLScreen;

public class GuiWDLMessages extends WDLScreen {
	/**
	 * Set from inner classes; this is the text to draw.
	 */
	@Nullable
	private String hoveredButtonTooltip = null;

	private class GuiMessageTypeList extends GuiList<GuiMessageTypeList.Entry> {
		public GuiMessageTypeList() {
			super(GuiWDLMessages.this.minecraft, GuiWDLMessages.this.width,
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
				button.setEnabled(config.getValue(MessageSettings.ENABLE_ALL_MESSAGES));

				super.drawEntry(x, y, width, height, mouseX, mouseY);

				drawCenteredString(font, category.getDisplayName().getFormattedText(),
						GuiWDLMessages.this.width / 2 - 40, y + height
						- minecraft.fontRenderer.FONT_HEIGHT - 1, 0xFFFFFF);

				if (button.isHovered()) {
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
				button.setEnabled(config.getValue(typeRegistration.category.setting));

				super.drawEntry(x, y, width, height, mouseX, mouseY);

				if (button.isHovered()) {
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

	@Nullable
	private final Screen parent;
	private final WDL wdl;
	private final IConfiguration config;

	public GuiWDLMessages(@Nullable Screen parent, WDL wdl) {
		super("wdl.gui.messages.message.title");
		this.parent = parent;
		this.wdl = wdl;
		this.config = WDL.serverProps;
	}

	private SettingButton enableAllButton;
	private WDLButton resetButton;

	private static final int ID_RESET_ALL = 101;

	@Override
	public void init() {
		enableAllButton = this.addButton(new SettingButton(
				MessageSettings.ENABLE_ALL_MESSAGES, this.config,
				(this.width / 2) - 155, 18, 150, 20));
		resetButton = this.addButton(new ButtonDisplayGui(
				(this.width / 2) + 5, 18, 150, 20,
				I18n.format("wdl.gui.messages.reset"),
				() -> new ConfirmScreen(result -> confirmResult(result, ID_RESET_ALL),
						new TranslationTextComponent("wdl.gui.messages.reset.confirm.title"),
						new TranslationTextComponent("wdl.gui.messages.reset.confirm.subtitle"))));

		this.addList(new GuiMessageTypeList());

		this.addButton(new ButtonDisplayGui((this.width / 2) - 100, this.height - 29,
				200, 20, this.parent));
	}

	private void confirmResult(boolean result, int id) {
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

		minecraft.displayGuiScreen(this);
	}

	@Override
	public void removed() {
		wdl.saveProps();
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		hoveredButtonTooltip = null;

		this.renderBackground();
		super.render(mouseX, mouseY, partialTicks);

		String tooltip = null;
		if (hoveredButtonTooltip != null) {
			tooltip = hoveredButtonTooltip;
		} else if (enableAllButton.isHovered()) {
			tooltip = enableAllButton.getTooltip();
		} else if (resetButton.isHovered()) {
			tooltip = I18n.format("wdl.gui.messages.reset.description");
		}

		if (tooltip != null) {
			Utils.drawGuiInfoBox(tooltip, width, height, 48);
		}
	}
}
