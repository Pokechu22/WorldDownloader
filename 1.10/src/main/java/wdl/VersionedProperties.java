package wdl;

import net.minecraft.world.World;

/**
 * Helper that determines verison-specific information about things, such
 * as whether a world has skylight.
 */
public class VersionedProperties {
	/**
	 * Returns true if the given world has skylight data.
	 * 
	 * @return a boolean
	 */
	public static boolean hasSkyLight(World world) {
		// 1.10-: use hasNoSky
		return !world.provider.hasNoSky();
	}
}