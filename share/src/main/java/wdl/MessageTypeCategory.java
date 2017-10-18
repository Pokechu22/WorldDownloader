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
package wdl;

import net.minecraft.client.resources.I18n;
import wdl.api.IWDLMessageType;

/**
 * Category / collection of {@link IWDLMessageType}s.
 */
public abstract class MessageTypeCategory {
	public MessageTypeCategory(String internalName) {
		this.internalName = internalName;
	}

	/**
	 * The internal name.
	 *
	 * Used when saving.
	 */
	public final String internalName;

	/**
	 * Gets the user-facing display name.
	 */
	public abstract String getDisplayName();

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
		public final String i18nKey;

		public I18nableMessageTypeCategory(String internalName, String i18nKey) {
			super(internalName);
			this.i18nKey = i18nKey;
		}

		@Override
		public String getDisplayName() {
			return I18n.format(i18nKey);
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
