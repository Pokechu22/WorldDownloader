package wdl.api;

import net.minecraft.client.multiplayer.WorldClient;

public interface IWorldLoadListener extends IWDLMod {
	/**
	 * Called when a world loads. (Occurs after the previous world has saved).
	 * 
	 * @param world The world that has loaded.
	 * @param sameServer Whether the server is the same one as before.
	 */
	public abstract void onWorldLoad(WorldClient world, boolean sameServer);
}
