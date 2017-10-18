/*
 * This file is part of the World Downloader API.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * You are free to include the World Downloader API within your own mods, as
 * permitted via the MMPLv2.
 */
package wdl.api;

import java.util.Map;

/**
 * Represents a WDL mod that register new {@link IWDLMessageType}s.
 */
public interface IMessageTypeAdder extends IWDLMod {
	/**
	 * Gets the various {@link IWDLMessageType}s to register.
	 *
	 * @return A map of name to type.
	 */
	public Map<String, IWDLMessageType> getMessageTypes();
}
