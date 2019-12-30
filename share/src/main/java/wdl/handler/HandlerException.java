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
package wdl.handler;

import javax.annotation.Nonnull;

import net.minecraft.client.resources.I18n;
import wdl.WDLMessageTypes;
import wdl.api.IWDLMessageType;

/**
 * An exception used for errors during a block or entity handler,
 * which references a translated message.
 */
@SuppressWarnings("serial")
public class HandlerException extends Exception {
	/**
	 * Constructs a HandlerException with
	 * {@link WDLMessageTypes#ON_GUI_CLOSED_WARNING} as the message type.
	 *
	 * @param translationKey
	 *            The translation key for the message.
	 * @param args
	 *            A (potentially empty) list of arguments to use with the
	 *            translation key
	 */
	public HandlerException(String translationKey, Object... args) {
		this(WDLMessageTypes.ON_GUI_CLOSED_WARNING, translationKey, args);
	}

	/**
	 * Constructs a HandlerException with the given the message type.
	 *
	 * @param messageType
	 *            The message type to use.
	 * @param translationKey
	 *            The translation key for the message.
	 * @param args
	 *            A (potentially empty) list of arguments to use with the
	 *            translation key
	 */
	public HandlerException(IWDLMessageType messageType, String translationKey, Object... args) {
		this.translationKey = translationKey;
		this.messageType = messageType;
		this.args = args;
	}

	/**
	 * A translation key to use for the error message.
	 */
	public final @Nonnull String translationKey;
	/**
	 * The message type to use for the message.
	 */
	public final @Nonnull IWDLMessageType messageType;
	/**
	 * The arguments to use for formatting the translation key.
	 */
	public final @Nonnull Object[] args;

	@Override
	public String getLocalizedMessage() {
		return I18n.format(translationKey, args);
	}
}