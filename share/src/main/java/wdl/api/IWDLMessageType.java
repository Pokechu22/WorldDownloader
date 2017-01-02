package wdl.api;

import net.minecraft.util.text.TextFormatting;


/**
 * Represents a message type capable of being used in WDL logging.
 * <br/>
 * It is recomended that an enum is used to implement this.  See
 * {@link wdl.WDLMessageTypes} for an example.
 */
public interface IWDLMessageType {
	/**
	 * Gets the <a href="http://minecraft.gamepedia.com/Formatting_codes">color
	 * code</a> used for the "[WorldDL]" text before the message.
	 * 
	 * @return The format code.
	 */
	public abstract TextFormatting getTitleColor();
	/**
	 * Gets the <a href="http://minecraft.gamepedia.com/Formatting_codes">color
	 * code</a> used for the main message.
	 * 
	 * @return The format code.
	 */
	public abstract TextFormatting getTextColor();
	/**
	 * Gets the name to use on buttons and such.
	 */
	public abstract String getDisplayName();
	/**
	 * Gets the description text for this message, used in the messages GUI.
	 * <br/>
	 * If you want to include a newline, use <code>\n</code>.
	 */
	public abstract String getDescription();
	/**
	 * Wheter this type of message should be enabled by default.
	 */
	public abstract boolean isEnabledByDefault();
}
