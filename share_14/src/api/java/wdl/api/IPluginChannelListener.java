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

import net.minecraft.client.multiplayer.WorldClient;

public interface IPluginChannelListener extends IWDLMod {
	/**
	 * Called when a Plugin channel packet is received.
	 *
	 * @param world
	 *            The current world.
	 * @param channel
	 *            The channel the packet was sent on.
	 * @param packetData
	 *            The payload of the packet.
	 */
	public abstract void onPluginChannelPacket(WorldClient world,
			String channel, byte[] packetData);
}
