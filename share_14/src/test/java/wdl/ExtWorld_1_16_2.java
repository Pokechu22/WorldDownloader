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
package wdl;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import com.mojang.datafixers.DataFixer;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.BlockState;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SaveFormat.LevelSave;
import net.minecraft.world.storage.ServerWorldInfo;
import wdl.versioned.IDimensionWrapper;

abstract class ExtWorldClient extends ClientWorld {
	private static final int VIEW_DISTANCE = 16;

	@SuppressWarnings({ "resource", "unchecked" })
	public ExtWorldClient(IDimensionWrapper dim) {
		super(mock(ClientPlayNetHandler.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS)),
				mock(ClientWorldInfo.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS)),
				(RegistryKey<World>) dim.getWorldKey(),
				(DimensionType) dim.getType(), VIEW_DISTANCE,
				() -> EmptyProfiler.INSTANCE, mock(WorldRenderer.class), false, 0L);

		// Note that this is changing a final field, but doesn't seem to be causing any issues...
		ReflectionUtils.findAndSetPrivateField(this, ClientWorld.class, ClientChunkProvider.class, new SimpleChunkProvider());
		assertThat(super.getChunkProvider(), is(instanceOf(SimpleChunkProvider.class)));
	}

	private final RecipeManager recipeManager = mock(RecipeManager.class);
	private final Scoreboard scoreboard = mock(Scoreboard.class, withSettings().useConstructor());

	@Override
	public RecipeManager getRecipeManager() {
		return this.recipeManager;
	}

	@Override
	public Scoreboard getScoreboard() {
		return this.scoreboard;
	}

	/** Places a block, creating the proper state. */
	public void placeBlockAt(BlockPos pos, Block block, PlayerEntity player, Direction direction) {
		player.rotationYaw = direction.getHorizontalAngle();
		player.setHeldItem(Hand.MAIN_HAND, new ItemStack(block));
		BlockItemUseContext context = new BlockItemUseContext(
				new ItemUseContext(player, Hand.MAIN_HAND,
						new BlockRayTraceResult(new Vector3d(pos.getX(), pos.getY(), pos.getZ()),
								direction, pos, false)));
		BlockState state = block.getStateForPlacement(context);
		setBlockState(pos, state);
	}

	/** Defers to the 1.13.1-specific tick method */
	public void tick() {
		this.tick(() -> true);
	}

	@Override
	public void playSound(double p_184134_1_, double p_184134_3_, double p_184134_5_, SoundEvent p_184134_7_,
			SoundCategory p_184134_8_, float p_184134_9_, float p_184134_10_, boolean p_184134_11_) {
		// Do nothing
	}

	private static final BiomeContainer NO_BIOMES;
	static {
		Biome[] biomes = new Biome[BiomeContainer.BIOMES_SIZE];
		Arrays.fill(biomes, Biomes.THE_VOID);
		Registry<Biome> biomesReg = DynamicRegistries.func_239770_b_().func_243612_b(Registry.field_239720_u_);
		NO_BIOMES = new BiomeContainer(biomesReg, biomes);
	}

	/**
	 * A simple chunk provider implementation that just caches chunks in a map.
	 */
	private final class SimpleChunkProvider extends ClientChunkProvider {
		private final Long2ObjectMap<Chunk> map = new Long2ObjectOpenHashMap<>();

		public SimpleChunkProvider() {
			super(ExtWorldClient.this, VIEW_DISTANCE);
		}

		@Override
		public Chunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean load) {
			return map.computeIfAbsent(ChunkPos.asLong(x, z), k -> new Chunk(ExtWorldClient.this, new ChunkPos(x, z), NO_BIOMES));
		}

		@Override
		public void tick(BooleanSupplier hasTimeLeft) {
		}

		@Override
		public String makeString() {
			return this.toString();
		}

		@Override
		public boolean chunkExists(int x, int z) {
			return true;
		}
	}
}

abstract class ExtWorldServer extends ServerWorld {
	@SuppressWarnings({ "resource", "unchecked" })
	public ExtWorldServer(IDimensionWrapper dim) {
		super(mock(MinecraftServer.class, withSettings().defaultAnswer(RETURNS_MOCKS)),
				task -> task.run(), mock(LevelSave.class), mock(ServerWorldInfo.class),
				(RegistryKey<World>) dim.getWorldKey(),
				(DimensionType) dim.getType(), null, null, false, 0, null, false);

		// Note that this is changing a final field, but doesn't seem to be causing any issues...
		ReflectionUtils.findAndSetPrivateField(this, ServerWorld.class, ServerChunkProvider.class, new SimpleChunkProvider());
		assertThat(super.getChunkProvider(), is(instanceOf(SimpleChunkProvider.class)));
	}

	/**
	 * Class of the real/vanilla ServerWorld.
	 */
	public static final Class<? super ExtWorldServer> SERVER_WORLD_CLASS = ServerWorld.class;

	private final RecipeManager recipeManager = mock(RecipeManager.class);
	private final ServerScoreboard scoreboard = mock(ServerScoreboard.class, withSettings().useConstructor(new Object[] {null}));

	@Override
	public RecipeManager getRecipeManager() {
		return this.recipeManager;
	}

	@Override
	public ServerScoreboard getScoreboard() {
		return this.scoreboard;
	}

	/** Places a block, creating the proper state. */
	public void placeBlockAt(BlockPos pos, Block block, PlayerEntity player, Direction direction) {
		player.rotationYaw = direction.getHorizontalAngle();
		BlockItemUseContext context = new BlockItemUseContext(
				new ItemUseContext(player, Hand.MAIN_HAND,
						new BlockRayTraceResult(new Vector3d(pos.getX(), pos.getY(), pos.getZ()),
								direction, pos, false)));
		BlockState state = block.getStateForPlacement(context);
		setBlockState(pos, state);
	}

	/** Left-clicks a block. */
	public void clickBlock(BlockPos pos, PlayerEntity player) {
		BlockState state = this.getBlockState(pos);
		state.onBlockClicked(this, pos, player);
	}

	/** Right-clicks a block. */
	public void interactBlock(BlockPos pos, PlayerEntity player) {
		BlockState state = this.getBlockState(pos);
		BlockRayTraceResult rayTraceResult = new BlockRayTraceResult(
				new Vector3d(pos.getX(), pos.getY(), pos.getZ()),
				Direction.DOWN, pos, false);
		state.onBlockActivated(this, player, Hand.MAIN_HAND, rayTraceResult);
	}

	/** Right-clicks an entity. */
	public void interactEntity(Entity entity, PlayerEntity player) {
		entity.processInitialInteract(player, Hand.MAIN_HAND);
	}

	public Consumer<BlockEventData> onBlockEvent = (e -> super.addBlockEvent(e.getPosition(), e.getBlock(), e.getEventID(), e.getEventParameter()));

	@Override
	public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam) {
		onBlockEvent.accept(new BlockEventData(pos, blockIn, eventID, eventParam));
	}

	/** Defers to the 1.13.1-specific tick method */
	public void tick() {
		this.tick(() -> true);
	}

	@Override
	public void playSound(double p_184134_1_, double p_184134_3_, double p_184134_5_, SoundEvent p_184134_7_,
			SoundCategory p_184134_8_, float p_184134_9_, float p_184134_10_, boolean p_184134_11_) {
		// Do nothing
	}

	private static final BiomeContainer NO_BIOMES;
	static {
		Biome[] biomes = new Biome[BiomeContainer.BIOMES_SIZE];
		Arrays.fill(biomes, Biomes.THE_VOID);
		Registry<Biome> biomesReg = DynamicRegistries.func_239770_b_().func_243612_b(Registry.field_239720_u_);
		NO_BIOMES = new BiomeContainer(biomesReg, biomes);
	}

	/**
	 * A simple chunk provider implementation that just caches chunks in a map.
	 */
	private final class SimpleChunkProvider extends ExtServerChunkProvider {
		private final Long2ObjectMap<Chunk> map = new Long2ObjectOpenHashMap<>();

		public SimpleChunkProvider() {
			super(ExtWorldServer.this, mock(LevelSave.class), mock(DataFixer.class),
					null, task -> task.run(), null, 0, false, null, null);
		}

		@Override
		public IChunk getChunk(int x, int z, ChunkStatus p_212849_3_, boolean p_212849_4_) {
			return map.computeIfAbsent(ChunkPos.asLong(x, z), k -> new Chunk(ExtWorldServer.this, new ChunkPos(x, z), NO_BIOMES));
		}

		@Override
		public void tick(BooleanSupplier hasTimeLeft) {
		}

		@Override
		public String makeString() {
			return this.toString();
		}

		@Override
		public int getLoadedChunkCount() {
			return map.size();
		}

		@Override
		public boolean chunkExists(int x, int z) {
			return true;
		}

		@Override
		public ChunkGenerator getChunkGenerator() {
			return null; // This is allowed, albeit awkward; see ChunkProviderClient
		}
	}

	public void setPlayerSneaking(PlayerEntity player, boolean sneaking) {
		// Exists only due to name issues
		player.setSneaking(sneaking);
	}
}
