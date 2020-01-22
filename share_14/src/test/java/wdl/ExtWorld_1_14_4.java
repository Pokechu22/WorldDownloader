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
package wdl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

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
import net.minecraft.profiler.IProfiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;

abstract class ExtWorldClient extends ClientWorld {
	private static final int VIEW_DISTANCE = 16;

	public ExtWorldClient(ClientPlayNetHandler netHandler, WorldSettings settings, DimensionType dim,
			Difficulty difficulty, IProfiler profilerIn) {
		super(netHandler, settings, dim, VIEW_DISTANCE, profilerIn, mock(WorldRenderer.class));
		getChunkProvider(); // Make sure it's injected
	}

	private boolean injectedChunkProvider = false;

	@SuppressWarnings("resource")
	@Override
	public ClientChunkProvider getChunkProvider() {
		if (!injectedChunkProvider) {
			// Also, note that this is changing a final field, but doesn't seem to be causing any issues...
			ReflectionUtils.findAndSetPrivateField(this, World.class, AbstractChunkProvider.class, new SimpleChunkProvider());
			assertThat(this.chunkProvider, is(instanceOf(SimpleChunkProvider.class)));
			injectedChunkProvider = true;
		}
		return super.getChunkProvider();
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
						new BlockRayTraceResult(new Vec3d(pos), direction, pos, false)));
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

	private static final Biome[] NO_BIOMES = new Biome[16*16];
	static {
		Arrays.fill(NO_BIOMES, Biomes.THE_VOID);
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

		@Override
		public ChunkGenerator<?> getChunkGenerator() {
			return null; // This is allowed, albeit awkward; see ChunkProviderClient
		}
	}
}

abstract class ExtWorldServer extends ServerWorld {
	public ExtWorldServer(MinecraftServer server, SaveHandler saveHandlerIn, WorldInfo info, DimensionType dim,
			IProfiler profilerIn) {
		super(server, task -> task.run(), saveHandlerIn, info, dim, profilerIn, null);
		getChunkProvider(); // Make sure it's injected
	}

	/**
	 * Class of the real/vanilla ServerWorld.
	 */
	public static final Class<? super ExtWorldServer> SERVER_WORLD_CLASS = ServerWorld.class;

	private final RecipeManager recipeManager = mock(RecipeManager.class);
	private final ServerScoreboard scoreboard = mock(ServerScoreboard.class, withSettings().useConstructor(new Object[] {null}));
	private boolean injectedChunkProvider = false;

	@SuppressWarnings("resource")
	@Override
	public ServerChunkProvider getChunkProvider() {
		if (!injectedChunkProvider) {
			// NOTE: we have to do this here instead of the constructor, since it's called in the parent's constructor
			// Also, note that this is changing a final field, but doesn't seem to be causing any issues...
			ReflectionUtils.findAndSetPrivateField(this, World.class, AbstractChunkProvider.class, new SimpleChunkProvider());
			assertThat(this.chunkProvider, is(instanceOf(SimpleChunkProvider.class)));
			injectedChunkProvider = true;
		}
		return super.getChunkProvider();
	}

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
						new BlockRayTraceResult(new Vec3d(pos), direction, pos, false)));
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
		BlockRayTraceResult rayTraceResult = new BlockRayTraceResult(new Vec3d(pos), Direction.DOWN, pos, false);
		state.func_215687_a(this, player, Hand.MAIN_HAND, rayTraceResult);
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
	public void createSpawnPosition(WorldSettings p_73052_1_) {
		// Do nothing
	}

	@Override
	public void playSound(double p_184134_1_, double p_184134_3_, double p_184134_5_, SoundEvent p_184134_7_,
			SoundCategory p_184134_8_, float p_184134_9_, float p_184134_10_, boolean p_184134_11_) {
		// Do nothing
	}

	private static final Biome[] NO_BIOMES = new Biome[16*16];
	static {
		Arrays.fill(NO_BIOMES, Biomes.THE_VOID);
	}

	/**
	 * A simple chunk provider implementation that just caches chunks in a map.
	 */
	private final class SimpleChunkProvider extends ExtServerChunkProvider {
		private final Long2ObjectMap<Chunk> map = new Long2ObjectOpenHashMap<>();

		public SimpleChunkProvider() {
			super(ExtWorldServer.this, null, null, null, task -> task.run(), null, 0, null, null);
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
		public ChunkGenerator<?> getChunkGenerator() {
			return null; // This is allowed, albeit awkward; see ChunkProviderClient
		}
	}

	public void setPlayerSneaking(PlayerEntity player, boolean sneaking) {
		// Exists only due to name issues
		player.func_70095_a(sneaking);
	}
}
