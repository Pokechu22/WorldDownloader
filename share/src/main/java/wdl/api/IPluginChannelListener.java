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
