/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.settings;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import wdl.MessageTypeCategory;
import wdl.api.IWDLMessageType;

/**
 * Contains settings related to WDL's message system ({@link wdl.WDLMessages}).
 */
public class MessageSettings {
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
		public Boolean cycle(Boolean value) {
			return !value;
		}

		@Override
		public ITextComponent getButtonText(Boolean curValue) {
			return new TextComponentTranslation("wdl.gui.messages.group." + curValue);
		}

		@Override
		public ITextComponent getDescription() {
			return new TextComponentString(category.getDescription());
		}
	}

	/**
	 * A setting that controls whether or not an {@link IWDLMessageType} is enabled.
	 */
	public static class MessageTypeSetting implements CyclableSetting<Boolean> {
		private final IWDLMessageType type;
		private final String configKey;

		/**
		 * Constructor.
		 * @param type The message type
		 * @param name The name used for the message type at registration
		 */
		public MessageTypeSetting(IWDLMessageType type, String name) {
			this.type = type;
			this.configKey = "Messages." + name;
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
			return type.isEnabledByDefault();
		}

		@Override
		public Boolean cycle(Boolean value) {
			return !value;
		}

		@Override
		public ITextComponent getButtonText(Boolean curValue) {
			return new TextComponentTranslation("wdl.gui.messages.message." + curValue, type.getDisplayName());
		}

		@Override
		public ITextComponent getDescription() {
			return new TextComponentString(type.getDescription());
		}
	}
}
