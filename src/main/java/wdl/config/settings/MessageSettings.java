/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.config.settings;

import java.util.Optional;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import wdl.MessageTypeCategory;
import wdl.WDLMessages.MessageRegistration;
import wdl.api.IWDLMessageType;
import wdl.config.BooleanSetting;
import wdl.config.CyclableSetting;
import wdl.config.IConfiguration;

/**
 * Contains settings related to WDL's message system ({@link wdl.WDLMessages}).
 */
public final class MessageSettings {
	private MessageSettings() { throw new AssertionError(); }

	public static final BooleanSetting ENABLE_ALL_MESSAGES =
			new BooleanSetting("Messages.enableAll", true, "wdl.gui.messages.all");

	/**
	 * A setting that controls whether or not a {@link MessageTypeCategory} is enabled.
	 */
	public static class MessageCategorySetting implements CyclableSetting<Boolean> {
		private final String configKey;
		private final MessageTypeCategory category;

		/**
		 * Constructor.
		 * @param category The category.
		 */
		public MessageCategorySetting(MessageTypeCategory category) {
			this.category = category;
			this.configKey = "MessageGroup." + category.internalName;
		}

		@Override
		public String getConfigurationKey() {
			return this.configKey;
		}

		@Override
		public Boolean deserializeFromString(String text) {
			return Boolean.valueOf(text);
		}

		@Override
		public String serializeToString(Boolean value) {
			return value.toString();
		}

		@Override
		public Boolean getDefault(IConfiguration context) {
			return true;
		}

		@Override
		public Optional<Boolean> overrideFromContext(IConfiguration context) {
			if (!context.getValue(ENABLE_ALL_MESSAGES)) {
				return Optional.of(false);
			} else {
				return Optional.empty();
			}
		}

		@Override
		public Boolean cycle(Boolean value) {
			return !value;
		}

		@Override
		public ITextComponent getButtonText(Boolean curValue) {
			return new TranslationTextComponent("wdl.gui.messages.group." + curValue);
		}

		@Override
		public ITextComponent getDescription() {
			return category.getDescription();
		}
	}

	/**
	 * A setting that controls whether or not an {@link IWDLMessageType} is enabled.
	 */
	public static class MessageTypeSetting implements CyclableSetting<Boolean> {
		private final MessageRegistration registration;
		private final String configKey;

		/**
		 * Constructor.
		 * @param typeRegistration The message type registration
		 * @param name The name used for the message type at registration
		 */
		public MessageTypeSetting(MessageRegistration typeRegistration) {
			this.registration = typeRegistration;
			this.configKey = "Messages." + typeRegistration.name;
		}

		@Override
		public String getConfigurationKey() {
			return this.configKey;
		}

		@Override
		public Boolean deserializeFromString(String text) {
			return Boolean.valueOf(text);
		}

		@Override
		public String serializeToString(Boolean value) {
			return value.toString();
		}

		@Override
		public Boolean getDefault(IConfiguration context) {
			return registration.type.isEnabledByDefault();
		}

		@Override
		public Optional<Boolean> overrideFromContext(IConfiguration context) {
			if (!context.getValue(registration.category.setting)) {
				return Optional.of(false);
			} else {
				return Optional.empty();
			}
		}

		@Override
		public Boolean cycle(Boolean value) {
			return !value;
		}

		@Override
		public ITextComponent getButtonText(Boolean curValue) {
			return new TranslationTextComponent("wdl.gui.messages.message." + curValue, registration.type.getDisplayName());
		}

		@Override
		public ITextComponent getDescription() {
			return registration.type.getDescription();
		}
	}
}
