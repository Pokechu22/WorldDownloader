/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018-2020 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.versioned;


import javax.annotation.Nullable;

import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SMapDataPacket;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import wdl.versioned.HandlerFunctions.DimensionWrapper;

/**
 * Functions related to maps (the item).
 *
 * In 1.13.1 and later, loadMapData takes a string and the dimension field is a
 * DimensionType.
 */
final class MapFunctions {
	private MapFunctions() { throw new AssertionError(); }

	/* (non-javadoc)
	 * {@see VersionedFunctions#getMapData}
	 */
	@Nullable
	static MapData getMapData(World world, SMapDataPacket mapPacket) {
		return world.getMapData("map_" + mapPacket.getMapId());
	}

	/* (non-javadoc)
	 * {@see VersionedFunctions#getMapID}
	 */
	static int getMapID(ItemStack stack) {
		return FilledMapItem.getMapId(stack);
	}

	/* (non-javadoc)
	 * {@see VersionedFunctions#isMapDimensionNull}
	 */
	static boolean isMapDimensionNull(MapData map) {
		return map.dimension == null;
	}

	/* (non-javadoc)
	 * {@see VersionedFunctions#setMapDimension}
	 */
	static void setMapDimension(MapData map, IDimensionWrapper dim) {
		map.dimension = ((DimensionWrapper)dim).getWorldKey();
	}
}
