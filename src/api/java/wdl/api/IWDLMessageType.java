/*
 * This file is part of the World Downloader API.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * You are free to include the World Downloader API within your own mods, as
 * permitted via the MMPLv2.
 */
package wdl.api;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;


/**
 * Represents a message type capable of being used in WDL logging.
 * <br/>
 * It is recommended that an enum is used to implement this.  See
 * {@link wdl.WDLMessageTypes} for an example.
 */
public interface IWDLMessageType {
	/**
	 * Gets the <a href="https://minecraft.gamepedia.com/Formatting_codes">color
	 * code</a> used for the "[WorldDL]" text before the message.
	 *
	 * @return The format code.
	 */
	public abstract TextFormatting getTitleColor();
	/**
	 * Gets the <a href="https://minecraft.gamepedia.com/Formatting_codes">color
	 * code</a> used for the main message.
	 *
	 * @return The format code.
	 */
	public abstract TextFormatting getTextColor();
	/**
	 * Gets the name to use on buttons and such.
	 */
	public abstract ITextComponent getDisplayName();
	/**
	 * Gets the description text for this message, used in the messages GUI.
	 * <br/>
	 * If you want to include a newline, use <code>\n</code>.
	 */
	public abstract ITextComponent getDescription();
	/**
	 * Whether this type of message should be enabled by default.
	 */
	public abstract boolean isEnabledByDefault();
}
