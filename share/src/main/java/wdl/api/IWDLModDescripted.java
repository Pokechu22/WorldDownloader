package wdl.api;

/**
 * WDL mod that has additional description.
 */
public interface IWDLModDescripted extends IWDLMod {
	/**
	 * Gets the display name for the mod.  Should be translated if the mod
	 * supports translation.
	 * 
	 * @return The display name for the mod.
	 */
	public abstract String getDisplayName();
	/**
	 * Get the author of the mod.
	 * If there is more than 1, return the main author here, and use
	 * {@link #getAuthors()} to specify the rest.
	 * 
	 * If there is no main author (IE, 2 people doing the same amount
	 * of work), return null and specify using {@link #getAuthors()}.
	 * 
	 * @return The main author of the mod.
	 */
	public abstract String getMainAuthor();
	/**
	 * The rest of the authors of the mod.  If the main author
	 * is included in this list, they will not be included here
	 * (they will still be displayed in the main author slot).
	 * 
	 * @return The remaining authors, or null.
	 */
	public abstract String[] getAuthors();
	/**
	 * A URL for more information about the extension, EG a github link
	 * or a minecraftforums link.
	 * 
	 * @return A URL, or null if there is no URL.
	 */
	public abstract String getURL();
	/**
	 * A detailed description of the extension.  Color codes (§) and 
	 * <code>\n</code> are allowed.
	 * 
	 * @return A description, or null.
	 */
	public abstract String getDescription();
}
