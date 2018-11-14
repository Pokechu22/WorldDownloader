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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
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
	public static MapDataResult repairMapData(int mapID, @Nonnull MapData mapData, @Nonnull EntityPlayerSP player) {
		// (assume player is the owner for the moment)
		DimensionType dim = fixDimension(mapData, player); 
		return new MapDataResult(mapData, null, null, dim);
	}

	/**
	 * Sets the dimension of the map, assuming that it's known. The game crashes if
	 * this isn't set in 1.13.1 or 1.13.2.
	 *
	 * @param mapData The MapData.
	 * @param confirmedOwner An entity that is known to be holding that map, or null.
	 * @return the dimension that was identified if one was found, or null otherwise
	 */
	@Nullable
	static DimensionType fixDimension(MapData mapData, @Nullable Entity confirmedOwner) {
		if (confirmedOwner != null) {
			assert confirmedOwner.world != null;
			DimensionType dim = confirmedOwner.world.dimension.getType();
			assert dim != null;
			VersionedFunctions.setMapDimension(mapData, dim);
			return dim;
		} else if (VersionedFunctions.isMapDimensionNull(mapData)) {
			// Ensure that some dimension is set, so that the game doesn't crash.
			VersionedFunctions.setMapDimension(mapData, DimensionType.OVERWORLD);
			// The dimension wasn't confirmed, so return null
		}
		return null;
	}

	public static class MapDataResult {
		private MapDataResult(MapData map, @Nullable Integer xCenter, @Nullable Integer zCenter, @Nullable DimensionType dim) {
			assert (xCenter == null) == (zCenter == null); // Either both should be null or neither should be
			this.map = map;
			this.xCenter = xCenter;
			this.zCenter = zCenter;
			this.dim = dim;
		}
		/**
		 * The associated created MapData.
		 */
		public final MapData map;
		/**
		 * The computed x center value, or null if it couldn't be computed.
		 */
		@Nullable
		public final Integer xCenter;
		/**
		 * The computed z center value, or null if it couldn't be computed. Note that
		 * this is only null if xCenter is.
		 */
		@Nullable
		public final Integer zCenter;
		/**
		 * The dimension found.
		 */
		@Nullable
		public final DimensionType dim;

		/**
		 * Makes a string component version of what's known about the result.
		 */
		public ITextComponent toComponent() {
			boolean hasCenter = (xCenter != null) && (zCenter != null);
			boolean hasDim = (dim != null);
			if (hasDim) {
				if (hasCenter) {
					return new TextComponentTranslation("wdl.messages.onMapSaved.dimAndCenterKnown", dim, xCenter, zCenter);
				} else {
					return new TextComponentTranslation("wdl.messages.onMapSaved.onlyDimKnown", dim);
				}
			} else {
				if (hasCenter) {
					return new TextComponentTranslation("wdl.messages.onMapSaved.onlyCenterKnown", xCenter, zCenter);
				} else {
					return new TextComponentTranslation("wdl.messages.onMapSaved.neitherKnown");
				}
			}
		}
	}
}
