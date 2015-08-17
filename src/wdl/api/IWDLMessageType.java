package wdl.api;

/**
 * Represents a message type capable of being used in WDL logging.
 */
public interface IWDLMessageType {
	/**
	 * Gets the <a href="http://minecraft.gamepedia.com/Formatting_codes">color
	 * code</a> used for the "[WorldDL]" text before the message.
	 * 
	 * @return The format code.
	 */
	public abstract String getTitleColor();
	/**
	 * Gets the <a href="http://minecraft.gamepedia.com/Formatting_codes">color
	 * code</a> used for the main message.
	 * 
	 * @return The format code.
	 */
	public abstract String getTextColor();
	/**
	 * Gets the name to use programmatically for checking if enabled.
	 */
	public abstract String getName();
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
}
