/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.MapData;
import wdl.versioned.VersionedFunctions;

/**
 * Attempts to reconstruct information about a MapData that isn't present.
 *
 * From {@link SPacketMaps#setMapdataTo}, we already have:
 *
 * <ul>
 * <li>mapScale (byte for size)
 * <li>trackingPosition (bool for if player and frames are on the map)
 * <li>colors (which is the most important thing)
 * <li>mapDecorations (though the keys are NOT right, and also this isn't saved directly)
 * </ul>
 *
 * What we don't have:
 *
 * <ul>
 * <li>dimension (which can in fact cause crashes if not set in some versions, see #106)</li>
 * <li>xCenter and zCenter (matters for adding new stuff to the map/not messing it up when going to (0, 0))</li>
 * <li>unlimitedTracking (like trackingPosition, but used for explorer maps)</li>
 * <li>banners (indicated on maps)</li>
 * <li>frames (indicated on maps, but insignificant since it's just generated at runtime)</li>
 * <li>playersArrayList and playersHashMap are not known, but they don't matter</li>
 * </ul>
 */
public final class MapDataHandler {
	private MapDataHandler() { throw new AssertionError(); }

	/**
	 * Fills in more 
	 * @param mapID The ID of the map.
	 * @param mapData The MapData to fix.
	 * @param player {@link WDL#player}.
	 * @return The MapData to save, though currently it is the same reference as the parameter.
	 */
	public static MapData repairMapData(int mapID, @Nonnull MapData mapData, @Nonnull EntityPlayerSP player) {
		// (assume player is the owner for the moment)
		fixDimension(mapData, player);
		return mapData;
	}

	/**
	 * Sets the dimension of the map, assuming that it's known. The game crashes if
	 * this isn't set in 1.13.1 or 1.13.2.
	 *
	 * @param mapData The MapData.
	 * @param confirmedOwner An entity that is known to be holding that map, or null.
	 * @return true if the dimension was identified.
	 */
	static boolean fixDimension(MapData mapData, @Nullable Entity confirmedOwner) {
		if (confirmedOwner != null) {
			assert confirmedOwner.world != null;
			VersionedFunctions.setMapDimension(mapData, confirmedOwner.world.dimension.getType());
			return true;
		} else if (VersionedFunctions.isMapDimensionNull(mapData)) {
			// Ensure that some dimension is set, so that the game doesn't crash.
			VersionedFunctions.setMapDimension(mapData, DimensionType.OVERWORLD);
		}
		return false;
	}
}
