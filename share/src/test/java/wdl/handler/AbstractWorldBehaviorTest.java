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
package wdl.handler;

import static org.junit.Assume.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.annotation.Nullable;

import junit.framework.ComparisonFailure;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import wdl.MaybeMixinTest;
import wdl.ReflectionUtils;
import wdl.ducks.INetworkNameable;

/**
 * Base logic shared between all tests that use blocks.
 *
 * Ensures that the {@link Bootstrap} is initialized, so that classes such as
 * {@link Blocks} can be used.
 */
public abstract class AbstractWorldBehaviorTest extends MaybeMixinTest {
	/** Worlds corresponding to what the server and client know */
	protected World serverWorld, clientWorld;
	/** Player entities.  Both have valid, empty inventories. */
	protected EntityPlayer clientPlayer, serverPlayer;

	/**
	 * https://stackoverflow.com/a/3301720/3991344
	 *
	 * Needed because the world provider must be set for entities
	 * for things not to explode :/
	 */
	private static final Field worldProviderField;
	static {
		try {
			worldProviderField = ReflectionUtils.findField(World.class, WorldProvider.class);
	
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(worldProviderField, worldProviderField.getModifiers() & ~Modifier.FINAL);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a mock world, returning air for blocks and null for TEs.
	 */
	protected void makeMockWorld() {
		clientWorld = mock(World.class);
		serverWorld = mock(World.class);

		clientPlayer = mock(EntityPlayer.class);
		serverPlayer = mock(EntityPlayer.class);

		when(clientWorld.getBlockState(any())).thenReturn(Blocks.AIR.getDefaultState());
		when(serverWorld.getBlockState(any())).thenReturn(Blocks.AIR.getDefaultState());

		when(clientWorld.getScoreboard()).thenReturn(mock(Scoreboard.class));
		when(serverWorld.getScoreboard()).thenReturn(mock(Scoreboard.class));

		populateProviderEvily(clientWorld);
		populateProviderEvily(serverWorld);

		clientPlayer.inventory = new InventoryPlayer(clientPlayer);
		serverPlayer.inventory = new InventoryPlayer(serverPlayer);
	}

	/**
	 * Sets the <em>final</em> {@link World#provider} field to a new mock provider.
	 */
	private void populateProviderEvily(World world) {
		WorldProvider fakeProvider = mock(WorldProvider.class);
		when(fakeProvider.getDimensionType()).thenReturn(DimensionType.OVERWORLD);
		try {
			worldProviderField.set(world, fakeProvider);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new AssertionError(e);
		}
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
	protected void placeBlockAt(BlockPos pos, IBlockState state) {
		when(clientWorld.getBlockState(pos)).thenReturn(state);
		when(serverWorld.getBlockState(pos)).thenReturn(state);
	}

	/**
	 * Creates a container with the given GUI ID. This method is needed to implement
	 * shulker boxes (as we can't reference them in older versions), but is a fairly
	 * ugly hack.
	 *
	 * @param guiID
	 *            The ID
	 * @param player
	 *            The player for the GUI
	 * @param clientInv
	 *            The other inventory
	 * @return the container, or null if it can't be figured out
	 */
	@Nullable
	protected Container makeContainer(String guiID, EntityPlayer player, IInventory clientInv) {
		if ("minecraft:chest".equals(guiID)) {
			return new ContainerChest(player.inventory, clientInv, player);
		} else if ("minecraft:hopper".equals(guiID)) {
			return new ContainerHopper(player.inventory, clientInv, player);
		} else if ("minecraft:furnace".equals(guiID)) {
			return new ContainerFurnace(player.inventory, clientInv);
		} else if ("minecraft:brewing_stand".equals(guiID)) {
			return new ContainerBrewingStand(player.inventory, clientInv);
		} else if ("minecraft:beacon".equals(guiID)) {
			return new ContainerBeacon(player.inventory, clientInv);
		} else if ("minecraft:dispenser".equals(guiID) || "minecraft:dropper".equals(guiID)) {
			return new ContainerDispenser(player.inventory, clientInv);
		}
		return null;
	}

	/**
	 * Compares the two compounds, raising an assertion error if they do not match.
	 *
	 * @param expected The expected NBT
	 * @param actual The actual NBT
	 */
	protected void assertSameNBT(NBTTagCompound expected, NBTTagCompound actual) {
		// Don't use real AssertionError, but instead use a special JUnit one,
		// which has an interactive comparison tool
		if (!expected.equals(actual)) {
			throw new ComparisonFailure("Mismatched NBT!", expected.toString(), actual.toString());
		}
	}

	/**
	 * Checks that mixins were applied, and if not, then the test will be ignored
	 * (not failed).
	 *
	 * @see org.junit.Assume
	 */
	protected static void assumeMixinsApplied() {
		assumeTrue("Expected mixins to be applied", INetworkNameable.class.isAssignableFrom(InventoryBasic.class));
	}
}
