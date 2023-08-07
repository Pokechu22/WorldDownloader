/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import wdl.api.IWDLMessageType;
import wdl.config.CyclableSetting;
import wdl.config.settings.MessageSettings;

/**
 * Category / collection of {@link IWDLMessageType}s.
 */
public abstract class MessageTypeCategory {
	public MessageTypeCategory(String internalName) {
		this.internalName = internalName;
		this.setting = new MessageSettings.MessageCategorySetting(this);
	}

	/**
	 * The internal name.
	 *
	 * Used when saving.
	 */
	public final String internalName;
	/**
	 * The setting associated with this category.
	 */
	public final CyclableSetting<Boolean> setting;

	/**
	 * Gets the user-facing display name.
	 */
	public abstract ITextComponent getDisplayName();

	/**
	 * Gets the user-facing description.
	 */
	public abstract ITextComponent getDescription();

	@Override
	public String toString() {
		return "MessageTypeCategory [internalName=" + internalName
				+ ", displayName=" + getDisplayName() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((internalName == null) ? 0 : internalName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MessageTypeCategory other = (MessageTypeCategory) obj;
		if (internalName == null) {
			if (other.internalName != null) {
				return false;
			}
		} else if (!internalName.equals(other.internalName)) {
			return false;
		}
		return true;
	}

	/**
	 * Simple {@link MessageTypeCategory} that gets the display name from
	 * an internationalization key.
	 */
	public static class I18nableMessageTypeCategory extends MessageTypeCategory {
		private final String titleKey;
		private final String descKey;

		public I18nableMessageTypeCategory(String internalName, String i18nKey) {
			super(internalName);
			this.titleKey = i18nKey;
			this.descKey = i18nKey + ".description";
		}

		@Override
		public ITextComponent getDisplayName() {
			return new TranslationTextComponent(titleKey);
		}

		@Override
		public ITextComponent getDescription() {
			return new TranslationTextComponent(descKey);
		}

	}

	/**
	 * Core recommended category.
	 *
	 * Put in here instead of {@link WDLMessageTypes} because of field load
	 * orders.
	 */
	static final MessageTypeCategory CORE_RECOMMENDED =
			new MessageTypeCategory.I18nableMessageTypeCategory("CORE_RECOMMENDED",
					"wdl.messages.category.core_recommended");
	/**
	 * Core recommended category.
	 *
	 * Put in here instead of {@link WDLMessageTypes} because of field load
	 * orders.
	 */
	static final MessageTypeCategory CORE_DEBUG =
			new MessageTypeCategory.I18nableMessageTypeCategory("CORE_DEBUG",
					"wdl.messages.category.core_debug");
}
