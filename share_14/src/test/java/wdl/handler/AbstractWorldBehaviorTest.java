/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.handler;

import static org.junit.Assume.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assume;
import org.mockito.AdditionalAnswers;

import com.mojang.authlib.GameProfile;

import junit.framework.ComparisonFailure;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.util.text.StringTextComponent;
import wdl.MaybeMixinTest;
import wdl.ReflectionUtils;
import wdl.TestWorld;
import wdl.ducks.INetworkNameable;
import wdl.versioned.VersionedFunctions;

/**
 * Base logic shared between all tests that use blocks.
 *
 * Ensures that the {@link Bootstrap} is initialized, so that classes such as
 * {@link Blocks} can be used.
 */
public abstract class AbstractWorldBehaviorTest extends MaybeMixinTest {
	private static final Logger LOGGER = LogManager.getLogger();

	protected TestWorld.ClientWorld clientWorld;
	protected TestWorld.ServerWorld serverWorld;
	/** Player entities.  Both have valid, empty inventories. */
	protected ClientPlayerEntity clientPlayer;
	protected ServerPlayerEntity serverPlayer;

	protected Minecraft mc;

	/**
	 * Creates a mock world, returning air for blocks and null for TEs.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	protected void makeMockWorld() {
		mc = mock(Minecraft.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
		ReflectionUtils.findAndSetPrivateField(null, Minecraft.class, Minecraft.class, mc);

		doAnswer(AdditionalAnswers.<Screen>answerVoid(screen -> {
			if (screen instanceof IHasContainer<?>){
				clientPlayer.openContainer = ((IHasContainer<?>)screen).getContainer();
			} else {
				clientPlayer.openContainer = clientPlayer.container;
			}
			mc.currentScreen = screen;
		})).when(mc).displayGuiScreen(any());
		when(mc.isOnExecutionThread()).thenReturn(true);
		ReflectionUtils.findAndSetPrivateField(mc, Minecraft.class, IngameGui.class, mock(IngameGui.class));
		when(mc.ingameGUI.getChatGUI()).thenReturn(mock(NewChatGui.class));

		clientWorld = TestWorld.makeClient();
		serverWorld = TestWorld.makeServer();

		ClientPlayNetHandler nhpc = new ClientPlayNetHandler(mc, new Screen(new StringTextComponent("")) {}, null, new GameProfile(UUID.randomUUID(), "ClientPlayer"));
		ReflectionUtils.findAndSetPrivateField(nhpc, ClientWorld.class, clientWorld);
		clientPlayer = VersionedFunctions.makePlayer(mc, clientWorld, nhpc,
				mock(ClientPlayerEntity.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS))); // Use a mock for the rest of the defaults
		mc.player = clientPlayer;
		Class gameRendererClass = VersionedFunctions.getGameRendererClass();
		Object mockGameRenderer = mock(gameRendererClass);
		ReflectionUtils.findAndSetPrivateField(mc, Minecraft.class, gameRendererClass, mockGameRenderer); // UGLY WORKAROUND, see getGameRendererClass for more info
		// We need to use the constructor, as otherwise getMapInstanceIfExists will fail (it cannot be mocked due to the return type)
		when(mc.gameRenderer.getMapItemRenderer()).thenReturn(
				mock(MapItemRenderer.class, withSettings().useConstructor(new Object[] {null})));
		mc.world = clientWorld;

		ReflectionUtils.findAndSetPrivateField(mc, Minecraft.class, GameSettings.class, VersionedFunctions.createNewGameSettings());

		ServerPlayNetHandler nhps = mock(ServerPlayNetHandler.class);
		doAnswer(AdditionalAnswers.<IPacket<ClientPlayNetHandler>>answerVoid(
				packet -> packet.processPacket(nhpc)))
				.when(nhps).sendPacket(any());
		serverPlayer = new ServerPlayerEntity(mock(MinecraftServer.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS)),
				serverWorld, new GameProfile(UUID.randomUUID(), "ServerPlayer"),
				mock(PlayerInteractionManager.class));
		serverPlayer.connection = nhps;

		//serverPlayer.inventory = new PlayerInventory(serverPlayer);
		clientPlayer.world = clientWorld;
		serverPlayer.world = serverWorld;
	}

	/**
	 * Puts the given block into the mock worlds at the given position.
	 *
	 * @param pos The position
	 * @param block The block to put
	 */
	protected void placeBlockAt(BlockPos pos, Block block) {
		placeBlockAt(pos, block.getDefaultState());
	}

	/**
	 * Puts the given block into the mock worlds at the given position.
	 *
	 * @param pos The position
	 * @param block The block to put
	 */
	protected void placeBlockAt(BlockPos pos, BlockState state) {
		clientWorld.setBlockState(pos, state);
		serverWorld.setBlockState(pos, state);
	}

	/**
	 * Puts the given block into the mock worlds at the given position.
	 *
	 * @param pos The position
	 * @param block The block to put
	 * @param facing The direction to place the block from
	 */
	protected void placeBlockAt(BlockPos pos, Block block, Direction facing) {
		clientWorld.placeBlockAt(pos, block, clientPlayer, facing);
		serverWorld.placeBlockAt(pos, block, serverPlayer, facing);
	}

	/**
	 * Compares the two compounds, raising an assertion error if they do not match.
	 *
	 * @param expected The expected NBT
	 * @param actual The actual NBT
	 */
	protected void assertSameNBT(CompoundNBT expected, CompoundNBT actual) {
		// Don't use real AssertionError, but instead use a special JUnit one,
		// which has an interactive comparison tool
		if (!expected.equals(actual)) {
			throw new ComparisonFailure("Mismatched NBT!", VersionedFunctions.nbtString(expected), VersionedFunctions.nbtString(actual));
		}
	}

	/**
	 * With a name like this, it's quite obvious that this only exists _because_
	 * custom names are completely broken...
	 */
	protected static void assumeCustomNamesNotBroken() {
		Assume.assumeTrue("Custom names are just completely broken, YAY!", false);
	}

	/**
	 * Checks that mixins were applied, and if not, then the test will be ignored
	 * (not failed).
	 *
	 * @see org.junit.Assume
	 */
	protected static void assumeMixinsApplied() {
		boolean applied = INetworkNameable.class.isAssignableFrom(Inventory.class);
		if (!applied) {
			LOGGER.warn("Mixins were not applied; skipping this test");
		}
		assumeTrue("Expected mixins to be applied", applied);
	}

	@After
	public void resetState() {
		// Just to avoid dangling references to mocks
		clientWorld = null;
		serverWorld = null;
		clientPlayer = null;
		serverPlayer = null;
		mc = null;
		ReflectionUtils.findAndSetPrivateField(null, Minecraft.class, Minecraft.class, null);
	}
}
