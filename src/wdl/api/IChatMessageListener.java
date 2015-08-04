package wdl.api;

import net.minecraft.client.multiplayer.WorldClient;

public interface IChatMessageListener {
	/**
	 * Called when a chat message is received.<br/>
	 * I really hope no mods are using chat to send data anymore, but some
	 * might.
	 * 
	 * @param world
	 *            The current world.
	 * @param message
	 *            The received message, in raw format.
	 */
	public abstract void onChat(WorldClient world, String message);
}
