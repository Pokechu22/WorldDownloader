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

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
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
	 * Various map decoration values.
	 *
	 * Note that we can't use the enum in MapDecoration because it didn't exist
	 * in 1.9.
	 */
	@VisibleForTesting
	static final byte DECORATION_PLAYER = 0, DECORATION_ITEM_FRAME = 1, DECORATION_OFF_PLAYER = 6;
	/**
	 * Additionally, note that this decoration was added in 1.11.
	 */
	@VisibleForTesting
	static final byte DECORATION_FAR_OFF_PLAYER = 7;

	/**
	 * Fills in more information about a map based on existing marker information.
	 * @param mapID The ID of the map.
	 * @param mapData The MapData to fix.
	 * @param player {@link WDL#player}.
	 * @return The MapData to save, though currently it is the same reference as the parameter.
	 */
	public static MapDataResult repairMapData(int mapID, @Nonnull MapData mapData, @Nonnull EntityPlayer player) {
		MapDataResult result = checkPlayerHasMap(mapID, mapData, player);
		if (result == null) result = checkFrameHasMap(mapID, mapData, player.world);
		if (result == null) result = new MapDataResult(mapData, null, null);

		result.fixDimension();
		result.fixCenter();

		return result;
	}

	@Nullable
	private static MapDataResult checkPlayerHasMap(int mapID, MapData mapData, EntityPlayer player) {
		// If there's only one decoration, and our player has the map in their inventory,
		// assume that the icon is our player (which might not be the case all the time)
		List<MapDecoration> playerDecorations = mapData.mapDecorations.values().stream()
				.filter(dec -> dec.getImage() == DECORATION_PLAYER)
				.collect(Collectors.toList());

		if (playerDecorations.size() != 1) {
			return null;
		}
		MapDecoration playerDecoration = playerDecorations.get(0);

		boolean mapInInventory = false;
		// Note: mainInventory does NOT contain the offhand or armor
		// Thus, we need an explicit offhand check later.  Maps shoudln't ever be in armor though.
		for (ItemStack stack : player.inventory.mainInventory) {
			if (isMapWithID(stack, mapID)) {
				mapInInventory = true;
				break;
			}
		}
		if (!mapInInventory) mapInInventory = isMapWithID(player.getHeldItemOffhand(), mapID);

		if (mapInInventory) {
			return new MapDataResult(mapData, player, playerDecoration);
		} else {
			return null;
		}
	}

	@Nullable
	private static MapDataResult checkFrameHasMap(int mapID, MapData mapData, World world) {
		List<MapDecoration> frameDecorations = mapData.mapDecorations.values().stream()
				.filter(dec -> dec.getImage() == DECORATION_ITEM_FRAME)
				.collect(Collectors.toList());
		if (frameDecorations.isEmpty()) {
			// No frames on the map?  No reason to look for any frames in the world.
			return null;
		}

		List<EntityItemFrame> actualFrames = world.getEntities(
				EntityItemFrame.class, frameWithMapID(mapID));
		for (EntityItemFrame frame : actualFrames) {
			// Since not all frames on the map are necessarily loaded by the client,
			// look for frames in the world that aren't on the map instead.
			// However, it's entirely possible to also have frames that just aren't on the map...

			// Find the center assuming this frame is on the map
			// (copied from MapData.computeMapCenter)
			int size = 128 * (1 << mapData.scale);
			int x = MathHelper.floor((frame.posX + 64.0D) / (double)size);
			int z = MathHelper.floor((frame.posZ + 64.0D) / (double)size);
			int xCenter = x * size + size / 2 - 64;
			int zCenter = z * size + size / 2 - 64;
			// Now find the icon position on the map...
			// (copied from MapData.updateDecorations)
			float iconX = (float)(frame.posX - (double)xCenter) / (float)size;
			float iconZ = (float)(frame.posZ - (double)zCenter) / (float)size;
			byte byteIconX = (byte)((int)((double)(iconX * 2.0F) + 0.5D)); // I like these casts...
			byte byteIconZ = (byte)((int)((double)(iconZ * 2.0F) + 0.5D));

			// Now we'd check if there's a frame at that position.
			// Could be ambiguous...  There could also be multiple possibilities...
			// Probably should check whether other icons still make sense in this case.
			for (MapDecoration decoration : frameDecorations) {
				if (decoration.getX() == byteIconX && decoration.getY() == byteIconZ) {
					return new MapDataResult(mapData, frame, decoration);
				}
			}
		}
		return null;
	}

	/**
	 * Needed since different MC versions use either guava's or java 8's predicate
	 * (and in versions < 1.12, the union of them still has 2 abstract methods).
	 */
	@FunctionalInterface
	private static interface FramePredicate extends java.util.function.Predicate<EntityItemFrame>,
			com.google.common.base.Predicate<EntityItemFrame> {
		@Override
		public default boolean apply(EntityItemFrame frame) {
			return test(frame);
		}

		@Override
		public abstract boolean test(EntityItemFrame frame);
	}

	private static FramePredicate frameWithMapID(int mapID) {
		return frame -> {
			ItemStack stack = frame.getDisplayedItem();
			return isMapWithID(stack, mapID);
		};
	}

	private static boolean isMapWithID(@Nullable ItemStack stack, int mapID) {
		if (stack == null) {
			// Applies to 1.10 and earlier.
			// In later versions, getItem() will return Items.AIR, which means the next check fails instead
			return false;
		}
		if (stack.getItem() != Items.FILLED_MAP) {
			return false;
		}
		int id = VersionedFunctions.getMapId(stack);

		return id == mapID;
	}

	public static class MapDataResult {
		MapDataResult(MapData map, @Nullable Entity confirmedOwner, @Nullable MapDecoration decoration) {
			this.map = map;
			this.confirmedOwner = confirmedOwner;
			this.decoration = decoration;
		}
		/**
		 * The associated created MapData.
		 */
		public final MapData map;
		/**
		 * An entity that is known to be holding the map, or null.
		 */
		@Nullable
		public final Entity confirmedOwner;
		/**
		 * The decoration associated with the owner.
		 */
		@Nullable
		public final MapDecoration decoration;
		/**
		 * True if the x and z center values were successfully computed.
		 */
		public boolean hasCenter;
		/**
		 * The computed x center value, if known.
		 */
		public int xCenter;
		/**
		 * The computed z center value, if known.
		 */
		public int zCenter;
		/**
		 * The dimension found, or null if it wasn't.
		 */
		@Nullable
		public DimensionType dim = null;

		/**
		 * Sets the dimension of the map, assuming that it's known. The game crashes if
		 * this isn't set in 1.13.1 or 1.13.2.
		 */
		void fixDimension() {
			if (confirmedOwner != null) {
				assert confirmedOwner.world != null;
				DimensionType dim = confirmedOwner.world.dimension.getType();
				assert dim != null;
				VersionedFunctions.setMapDimension(map, dim);
				this.dim = dim;
			} else if (VersionedFunctions.isMapDimensionNull(map)) {
				// Ensure that some dimension is set, so that the game doesn't crash.
				VersionedFunctions.setMapDimension(map, DimensionType.OVERWORLD);
				// The dimension wasn't confirmed, so don't notify this in chat
			}
		}

		void fixCenter() {
			if (confirmedOwner != null && decoration != null) {
				// Maps have 128 pixels but the icon unit is a byte (256 values)
				// 0: 128 blocks -> .5 blocks/icon unit, 1/pixel
				// 4: 2048 blocks -> 8 blocks/icon unit, 16/pixel
				byte iconX = decoration.getX();
				byte iconZ = decoration.getY();
				if (iconX == -128 || iconX == 127 || iconZ == -128 || iconZ == 127) {
					// Boundary case; if they're right on the edge we can't be completely sure
					return;
				}

				// Maps are always snapped to a grid based on the player position when it's created
				// If the player is on the map, there's only one possible center position and the
				// built-in function calculates it.
				map.calculateMapCenter(confirmedOwner.posX, confirmedOwner.posZ, map.scale);

				hasCenter = true;
				xCenter = map.xCenter;
				zCenter = map.zCenter;
			}
		}

		/**
		 * Makes a string component version of what's known about the result.
		 */
		public ITextComponent toComponent() {
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
