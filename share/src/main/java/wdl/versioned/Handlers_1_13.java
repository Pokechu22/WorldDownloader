/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.versioned;

import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBeacon;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDropper;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.BlockTrappedChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.SaveHandler;
import wdl.handler.block.BeaconHandler;
import wdl.handler.block.BlockHandler;
import wdl.handler.block.BrewingStandHandler;
import wdl.handler.block.ChestHandler;
import wdl.handler.block.DispenserHandler;
import wdl.handler.block.DropperHandler;
import wdl.handler.block.FurnaceHandler;
import wdl.handler.block.HopperHandler;
import wdl.handler.block.ShulkerBoxHandler;
import wdl.handler.block.TrappedChestHandler;
import wdl.handler.blockaction.BlockActionHandler;
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
		// 1.11+: use hasSkyLight
		return world.dimension.hasSkyLight();
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
			new HopperHandler(),
			new ShulkerBoxHandler(),
			new TrappedChestHandler()
	);

	/* (non-javadoc)
	 * @see VersionedFunctions#BLOCK_ACTION_HANDLERS
	 */
	static final ImmutableList<BlockActionHandler<?, ?>> BLOCK_ACTION_HANDLERS = ImmutableList.of();

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
	 * @see VersionedFunctions#shouldImportBlockEntity
	 */
	static boolean shouldImportBlockEntity(String entityID, BlockPos pos,
			Block block, NBTTagCompound blockEntityNBT, Chunk chunk) {
		// Note blocks do not have a block entity in this version.
		if (block instanceof BlockChest && entityID.equals("minecraft:chest")) {
			return true;
		} else if (block instanceof BlockTrappedChest && entityID.equals("minecraft:trapped_chest")) {
			// Separate block entity from regular chests in this version.
			return true;
		} else if (block instanceof BlockDispenser && entityID.equals("minecraft:dispenser")) {
			return true;
		} else if (block instanceof BlockDropper && entityID.equals("minecraft:dropper")) {
			return true;
		} else if (block instanceof BlockFurnace && entityID.equals("minecraft:furnace")) {
			return true;
		} else if (block instanceof BlockBrewingStand && entityID.equals("minecraft:brewing_stand")) {
			return true;
		} else if (block instanceof BlockHopper && entityID.equals("minecraft:hopper")) {
			return true;
		} else if (block instanceof BlockBeacon && entityID.equals("minecraft:beacon")) {
			return true;
		} else if (block instanceof BlockShulkerBox && entityID.equals("minecraft:shulker_box")) {
			return true;
		} else if (block instanceof BlockCommandBlock && entityID.equals("minecraft:command_block")) {
			// Only import command blocks if the current world doesn't have a command set
			// for the one there, as WDL doesn't explicitly save them so we need to use the
			// one currently present in the world.
			TileEntity temp = chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
			if (temp == null || !(temp instanceof TileEntityCommandBlock)) {
				// Bad/missing data currently there, import the old data
				return true;
			}
			TileEntityCommandBlock te = (TileEntityCommandBlock) temp;
			boolean currentBlockHasCommand = !te.getCommandBlockLogic().getCommand().isEmpty();
			// Only import if the current command block has no command.
			return !currentBlockHasCommand;
		} else {
			return false;
		}
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#createNewBlockEntity
	 */
	@Nullable
	static TileEntity createNewBlockEntity(World world, BlockContainer block, IBlockState state) {
		return block.createNewTileEntity(world);
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getSaveHandler
	 */
	static SaveHandler getSaveHandler(Minecraft minecraft, String worldName) {
		// Null => No server to use when saving player data.  This is fine for WDL
		// we handle player data manually.
		return (SaveHandler)minecraft.getSaveLoader().getSaveLoader(worldName, null);
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#VANILLA_VILLAGER_CAREERS
	 */
	static final Int2ObjectMap<BiMap<String, Integer>> VANILLA_VILLAGER_CAREERS = new Int2ObjectArrayMap<>();
	static {
		BiMap<String, Integer> farmer = HashBiMap.create(4);
		farmer.put("entity.minecraft.villager.farmer", 1);
		farmer.put("entity.minecraft.villager.fisherman", 2);
		farmer.put("entity.minecraft.villager.shepherd", 3);
		farmer.put("entity.minecraft.villager.fletcher", 4);
		BiMap<String, Integer> librarian = HashBiMap.create(2);
		librarian.put("entity.minecraft.villager.librarian", 1);
		librarian.put("entity.minecraft.villager.cartographer", 2);
		BiMap<String, Integer> priest = HashBiMap.create(1);
		priest.put("entity.minecraft.villager.cleric", 1);
		BiMap<String, Integer> blacksmith = HashBiMap.create(3);
		blacksmith.put("entity.minecraft.villager.armorer", 1);
		blacksmith.put("entity.minecraft.villager.weapon_smith", 2);
		blacksmith.put("entity.minecraft.villager.tool_smith", 3);
		BiMap<String, Integer> butcher = HashBiMap.create(2);
		butcher.put("entity.minecraft.villager.butcher", 1);
		butcher.put("entity.minecraft.villager.leatherworker", 2);
		BiMap<String, Integer> nitwit = HashBiMap.create(1);
		nitwit.put("entity.minecraft.villager.nitwit", 1);

		VANILLA_VILLAGER_CAREERS.put(0, farmer);
		VANILLA_VILLAGER_CAREERS.put(1, librarian);
		VANILLA_VILLAGER_CAREERS.put(2, priest);
		VANILLA_VILLAGER_CAREERS.put(3, blacksmith);
		VANILLA_VILLAGER_CAREERS.put(4, butcher);
		VANILLA_VILLAGER_CAREERS.put(5, nitwit);
	}
}
