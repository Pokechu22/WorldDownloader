/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;

import java.lang.reflect.Field;
import java.util.Map;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;

/**
 * Helper that determines version-specific information about things, such
 * as whether a world has skylight.
 */
public class VersionedProperties {
	/**
	 * Returns true if the given world has skylight data.
	 *
	 * @return a boolean
	 */
	public static boolean hasSkyLight(World world) {
		// 1.10-: use isNether (hasNoSky)
		return !world.provider.isNether();
	}

	static {
		Map<Class<? extends TileEntity>, String> result = null;
		try {
			for (Field field : TileEntity.class.getDeclaredFields()) {
				if (field.getType().equals(Map.class)) {
					field.setAccessible(true);
					Map<?, ?> map = (Map<?, ?>) field.get(null);
					// Check for a Map<Class, String>
					if (map.containsKey(TileEntityFurnace.class)) {
						@SuppressWarnings("unchecked")
						Map<Class<? extends TileEntity>, String> tmp =
							(Map<Class<? extends TileEntity>, String>) map;
						result = tmp;
						break;
					}
				}
			}
			if (result == null) {
				throw new RuntimeException("Could not locate TileEntity.classToNameMap!");
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		TE_REVERSE_MAP = result;
	}

	/**
	 * A reference to {@link TileEntity#classToNameMap} (field_145853_j).
	 */
	private static final Map<Class<? extends TileEntity>, String> TE_REVERSE_MAP;

	/**
	 * Returns the ID used for block entities with the given class in the given version
	 *
	 * @return The ID, or an empty string if the given TE is not registered.
	 */
	public static String getBlockEntityID(Class<? extends TileEntity> clazz) {
		// 1.10-: There is no nice way to get the ID; use reflection
		return TE_REVERSE_MAP.getOrDefault(clazz, "");
	}

	/**
	 * Called when any GUI is closed, to handle shulker boxes.
	 *
	 * XXX This is not a good approach to version specific block entities.
	 */
	public static boolean handleShulkerGuiClosed(TileEntity te) {
		return false;
	}
	/**
	 * Checks if the given block is a shulker box, and the block entity ID matches.
	 */
	public static boolean isImportableShulkerBox(String entityID, Block block) {
		return false;
	}

	/**
	 * Gets the class used to store the list of chunks in ChunkProviderClient.
	 */
	@SuppressWarnings("rawtypes")
	public static Class<Long2ObjectMap> getChunkListClass() {
		return Long2ObjectMap.class;
	}
}
