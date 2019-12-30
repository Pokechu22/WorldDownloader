/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.versioned;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.MapData;


/**
 * Functions related to maps (the item).
 *
 * In 1.12.2 and earlier, loadMapData takes an int and the dimension field is a
 * byte.
 */
final class MapFunctions {
	private MapFunctions() { throw new AssertionError(); }
	private static final Logger LOGGER = LogManager.getLogger();

	/* (non-javadoc)
	 * {@see VersionedFunctions#getMapData}
	 */
	@Nullable
	static MapData getMapData(World world, SPacketMaps mapPacket) {
		return ItemMap.loadMapData(mapPacket.getMapId(), world);
	}

	/* (non-javadoc)
	 * {@see VersionedFunctions#getMapID}
	 */
	static int getMapID(ItemStack stack) {
		// Map ID is based on its damage value, yay!
		// See ItemMap.getMapData
		return stack.getMetadata();
	}

	/* (non-javadoc)
	 * {@see VersionedFunctions#isMapDimensionNull}
	 */
	static boolean isMapDimensionNull(MapData map) {
		return false; // A primitive byte can't be null
	}

	private static boolean useForgeMethod = false;
	/* (non-javadoc)
	 * {@see VersionedFunctions#setMapDimension}
	 */
	static void setMapDimension(MapData map, DimensionType dim) {
		if (!useForgeMethod) {
			try {
				setMapDimensionVanilla(map, dim.getId());
			} catch (NoSuchFieldError e) {
				// Forge changes the type of this field from a byte to an int: https://git.io/fpReX
				// While this change is nice, it does make things messy and we need to set the right field
				// Right now, just use reflection and the SRG name of the field, since that'd be what
				// the field is named at runtime (and forge won't be present in the dev environment).
				// (See soccerguy's comment on issue 106)
				LOGGER.info("[WDL] Failed to set map dimension using vanilla field; switching to forge field...", e);
				try {
					setMapDimensionForge(map, dim.getId());
					useForgeMethod = true;
					LOGGER.info("[WDL] The forge field worked; it will be used for future attempts at setting the dimension.");
				} catch (Exception e2) {
					LOGGER.fatal("[WDL] Failed to set map dimension using both vanilla and forge fields", e2);
					RuntimeException ex = new RuntimeException("Failed to set map dimension", e2);
					ex.addSuppressed(e);
					throw ex;
				}
			}
		} else {
			// The forge version worked once before; use that in the future.
			try {
				setMapDimensionForge(map, dim.getId());
			} catch (Exception ex) {
				LOGGER.fatal("[WDL] Failed to set map dimension using both forge field, but it worked before?", ex);
				throw new RuntimeException("Failed to set map dimension with forge", ex);
			}
		}
	}

	/**
	 * Uses the vanilla field to set the map dimension.
	 * @throws NoSuchFieldError when forge is installed
	 */
	private static void setMapDimensionVanilla(MapData map, int dim) throws NoSuchFieldError {
		map.dimension = (byte)dim;
	}

	/**
	 * Sets the forge field for the map dimension. This only works in a SRG name
	 * environment with forge installed.
	 */
	private static void setMapDimensionForge(MapData map, int dim) throws Exception {
		MapData.class.getField("field_76200_c").setInt(map, dim);
	}
}
