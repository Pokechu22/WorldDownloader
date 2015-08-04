package wdl.api;

import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;

/**
 * Represents a mod that listens to WDL's events.
 * 
 * To use this, it MUST be added to the list of WDL mods via
 * {@link WDLApi#addWDLMod(IWDLMod)}.
 * 
 * Also, implement the subinterfaces for this to be useful.
 */
public interface IWDLMod {
	/**
	 * Gets the name of the mod.
	 * 
	 * @return The name of the mod.
	 */
	public abstract String getName();
}
