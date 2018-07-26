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
package wdl.versioned;

import java.lang.reflect.Field;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;
import wdl.handler.block.BeaconHandler;
import wdl.handler.block.BlockHandler;
import wdl.handler.block.BrewingStandHandler;
import wdl.handler.block.ChestHandler;
import wdl.handler.block.DispenserHandler;
import wdl.handler.block.DropperHandler;
import wdl.handler.block.FurnaceHandler;
import wdl.handler.block.HopperHandler;
import wdl.handler.entity.EntityHandler;
import wdl.handler.entity.HopperMinecartHandler;
import wdl.handler.entity.HorseHandler;
import wdl.handler.entity.StorageMinecartHandler;
import wdl.handler.entity.VillagerHandler;

final class HandlerFunctions {
	private HandlerFunctions() { throw new AssertionError(); }

	/* (non-javadoc)
	 * @see VersionedFunctions#hasSkyLight
	 */
	static boolean hasSkyLight(World world) {
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

	/* (non-javadoc)
	 * @see VersionedFunctions#getBlockEntityID
	 */
	static String getBlockEntityID(Class<? extends TileEntity> clazz) {
		// 1.10-: There is no nice way to get the ID; use reflection
		return TE_REVERSE_MAP.getOrDefault(clazz, "");
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#BLOCK_HANDLERS
	 */
	static final ImmutableList<BlockHandler<?, ?>> BLOCK_HANDLERS = ImmutableList.of(
			new BeaconHandler(),
			new BrewingStandHandler(),
			new ChestHandler(),
			new DispenserHandler(),
			new DropperHandler(),
			new FurnaceHandler(),
			new HopperHandler()
	);

	/* (non-javadoc)
	 * @see VersionedFunctions#ENTITY_HANDLERS
	 */
	static final ImmutableList<EntityHandler<?, ?>> ENTITY_HANDLERS = ImmutableList.of(
			new HopperMinecartHandler(),
			new HorseHandler(),
			new StorageMinecartHandler(),
			new VillagerHandler()
	);

	/* (non-javadoc)
	 * @see VersionedFunctions#isImportableShulkerBox
	 */
	static boolean isImportableShulkerBox(String entityID, Block block) {
		return false;
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getChunkListClass
	 */
	@SuppressWarnings("rawtypes")
	static Class<Long2ObjectMap> getChunkListClass() {
		return Long2ObjectMap.class;
	}
}
