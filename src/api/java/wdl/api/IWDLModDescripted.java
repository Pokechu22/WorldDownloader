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
	 * or a minecraftforum link.
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
