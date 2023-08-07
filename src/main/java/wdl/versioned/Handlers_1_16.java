/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2020 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.versioned;


import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Lifecycle;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.BeaconBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.TrappedChestBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.ClientWorld.ClientWorldInfo;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.IDynamicRegistries;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.storage.SaveFormat.LevelSave;
import net.minecraft.world.storage.ServerWorldInfo;
import wdl.handler.block.BarrelHandler;
import wdl.handler.block.BeaconHandler;
import wdl.handler.block.BlastFurnaceHandler;
import wdl.handler.block.BlockHandler;
import wdl.handler.block.BrewingStandHandler;
import wdl.handler.block.ChestHandler;
import wdl.handler.block.DispenserHandler;
import wdl.handler.block.DropperHandler;
import wdl.handler.block.FurnaceHandler;
import wdl.handler.block.HopperHandler;
import wdl.handler.block.LecternHandler;
import wdl.handler.block.ShulkerBoxHandler;
import wdl.handler.block.SmokerHandler;
import wdl.handler.block.TrappedChestHandler;
import wdl.handler.blockaction.BlockActionHandler;
import wdl.handler.entity.EntityHandler;
import wdl.handler.entity.HopperMinecartHandler;
import wdl.handler.entity.HorseHandler;
import wdl.handler.entity.StorageMinecartHandler;
import wdl.handler.entity.VillagerHandler;

final class HandlerFunctions {
	private HandlerFunctions() { throw new AssertionError(); }

	// NOTE: func_239770_b_ creates a new instance each time!  Even this use might be wrong;
	// probably vanilla's should be in use.  (XXX)
	static final IDynamicRegistries.Impl DYNAMIC_REGISTRIES = IDynamicRegistries.func_239770_b_();

	static final DimensionWrapper NETHER = new DimensionWrapper(DimensionType.field_236000_d_, World.field_234919_h_);
	static final DimensionWrapper OVERWORLD = new DimensionWrapper(DimensionType.field_235999_c_, World.field_234918_g_);
	static final DimensionWrapper END = new DimensionWrapper(DimensionType.field_236001_e_, World.field_234920_i_);

	// TODO: This doesn't interact with the values above, but I'm not sure how to best handle that
	private static Map<World, DimensionWrapper> dimensions = new WeakHashMap<>();

	/* (non-javadoc)
	 * @see VersionedFunctions#getDimension
	 */
	static DimensionWrapper getDimension(World world) {
		return dimensions.computeIfAbsent(world, DimensionWrapper::new);
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#hasSkyLight
	 */
	static boolean hasSkyLight(World world) {
		// 1.11+: use hasSkyLight
		return getDimension(world).getType().hasSkyLight();
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#BLOCK_HANDLERS
	 */
	static final ImmutableList<BlockHandler<?, ?>> BLOCK_HANDLERS = ImmutableList.of(
			new BarrelHandler(),
			new BeaconHandler(),
			new BrewingStandHandler(),
			new BlastFurnaceHandler(),
			new ChestHandler(),
			new DispenserHandler(),
			new DropperHandler(),
			new FurnaceHandler(),
			new HopperHandler(),
			new LecternHandler(),
			new ShulkerBoxHandler(),
			new SmokerHandler(),
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
			Block block, CompoundNBT blockEntityNBT, Chunk chunk) {
		// Note sBlock do not have a block entity in this version.
		if (block instanceof ChestBlock && entityID.equals("minecraft:chest")) {
			return true;
		} else if (block instanceof TrappedChestBlock && entityID.equals("minecraft:trapped_chest")) {
			// Separate block entity from regular chests in this version.
			return true;
		} else if (block instanceof DispenserBlock && entityID.equals("minecraft:dispenser")) {
			return true;
		} else if (block instanceof DropperBlock && entityID.equals("minecraft:dropper")) {
			return true;
		} else if (block instanceof FurnaceBlock && entityID.equals("minecraft:furnace")) {
			return true;
		} else if (block instanceof BrewingStandBlock && entityID.equals("minecraft:brewing_stand")) {
			return true;
		} else if (block instanceof HopperBlock && entityID.equals("minecraft:hopper")) {
			return true;
		} else if (block instanceof BeaconBlock && entityID.equals("minecraft:beacon")) {
			return true;
		} else if (block instanceof ShulkerBoxBlock && entityID.equals("minecraft:shulker_box")) {
			return true;
		} else if (block instanceof CommandBlockBlock && entityID.equals("minecraft:command_block")) {
			// Only import command sBlock if the current world doesn't have a command set
			// for the one there, as WDL doesn't explicitly save them so we need to use the
			// one currently present in the world.
			TileEntity temp = chunk.getTileEntity(pos, Chunk.CreateEntityType.CHECK);
			if (temp == null || !(temp instanceof CommandBlockTileEntity)) {
				// Bad/missing data currently there, import the old data
				return true;
			}
			CommandBlockTileEntity te = (CommandBlockTileEntity) temp;
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
	static TileEntity createNewBlockEntity(World world, ContainerBlock block, BlockState state) {
		return block.createNewTileEntity(world);
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getSaveHandler
	 */
	static ISaveHandlerWrapper getSaveHandler(Minecraft minecraft, String worldName) throws Exception {
		return new LevelSaveWrapper(minecraft.getSaveLoader().func_237274_c_(worldName));
	}

	static class LevelSaveWrapper implements ISaveHandlerWrapper {
		public final LevelSave save;
		public LevelSaveWrapper(LevelSave save) {
			this.save = save;
		}

		@Override
		public void close() throws Exception {
			this.save.close();
		}

		@Override
		public File getWorldDirectory() {
			// XXX: This is rather dubious
			return this.save.func_237291_a_(OVERWORLD.getWorldKey());
		}

		@Override
		public void checkSessionLock() throws Exception {
			// Happens automatically?  func_237301_i_ does it, but it's not public.
			// Use func_237298_f_, which calls it and otherwise doesn't do much (it gets icon.png)
			this.save.func_237298_f_();
		}

		@Override
		public Object getWrapped() {
			return this.save;
		}

		@Override
		public String toString() {
			return "LevelSaveWrapper [save=" + save + "]";
		}
	}

	static class DimensionWrapper implements IDimensionWrapper {
		private final DimensionType dimensionType;
		private final RegistryKey<DimensionType> dimensionTypeKey;
		private final RegistryKey<World> worldKey;

		public DimensionWrapper(World world) {
			this.dimensionType = world.func_230315_m_();
			this.dimensionTypeKey = world.func_234922_V_();
			this.worldKey = world.func_234923_W_();
		}

		public DimensionWrapper(RegistryKey<DimensionType> dimensionTypeKey,
				RegistryKey<World> worldKey) {
			Registry<DimensionType> dimTypeReg = DYNAMIC_REGISTRIES.func_230520_a_();
			this.dimensionType = dimTypeReg.func_230516_a_(dimensionTypeKey);
			this.dimensionTypeKey = dimensionTypeKey;
			this.worldKey = worldKey;
		}

		@Override
		public String getFolderName() {
			if (this.worldKey == World.field_234920_i_) {
				return "DIM1";
			} else if (this.worldKey == World.field_234919_h_) {
				return "DIM-1";
			}
			return null;
		}

		@Override
		public DimensionType getType() {
			return this.dimensionType;
		}

		@Override
		public RegistryKey<DimensionType> getTypeKey() {
			return this.dimensionTypeKey;
		}

		@Override
		public RegistryKey<World> getWorldKey() {
			return this.worldKey;
		}
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#writeAdditionalPlayerData
	 */
	static void writeAdditionalPlayerData(ClientPlayerEntity player, CompoundNBT nbt) {
		// TODO: handle everything in ServerPlayerEntity (but nothing is completely required)
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getWorldInfoNbt
	 */
	static CompoundNBT getWorldInfoNbt(ClientWorld world, CompoundNBT playerNBT) {
		ClientWorldInfo clientInfo = world.getWorldInfo();
		WorldSettings worldSettings = new WorldSettings("LevelName", GameType.CREATIVE, false,
				clientInfo.getDifficulty(), true, new GameRules(), DatapackCodec.field_234880_a_);
		DimensionGeneratorSettings genSettings = DimensionGeneratorSettings.func_236210_a_();
		ServerWorldInfo worldInfo = new ServerWorldInfo(worldSettings, genSettings, Lifecycle.stable());
		worldInfo.setGameTime(world.getGameTime());
		worldInfo.setDayTime(world.getDayTime());
		return worldInfo.func_230411_a_(DYNAMIC_REGISTRIES, playerNBT);
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

	/* (non-javadoc)
	 * @see VersionedFunctions#getEntityX
	 */
	static double getEntityX(Entity e) {
		return e.getPosX();
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getEntityY
	 */
	static double getEntityY(Entity e) {
		return e.getPosY();
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getEntityZ
	 */
	static double getEntityZ(Entity e) {
		return e.getPosZ();
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#setEntityPos
	 */
	static void setEntityPos(Entity e, double x, double y, double z) {
		e.setRawPosition(x, y, z);
	}
}
