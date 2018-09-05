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
import static org.mockito.Mockito.*;

import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import junit.framework.ComparisonFailure;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
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
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import wdl.MaybeMixinTest;
import wdl.ducks.INetworkNameable;

/**
 * Base logic shared between all tests that use blocks.
 *
 * Ensures that the {@link Bootstrap} is initialized, so that classes such as
 * {@link Blocks} can be used.
 */
public abstract class AbstractWorldBehaviorTest extends MaybeMixinTest {
	private static final Logger LOGGER = LogManager.getLogger();

	/** Worlds corresponding to what the server and client know */
	protected TestWorld serverWorld, clientWorld;
	/** Player entities.  Both have valid, empty inventories. */
	protected EntityPlayer clientPlayer, serverPlayer;

	/**
	 * Creates a mock world, returning air for blocks and null for TEs.
	 */
	protected void makeMockWorld() {
		clientPlayer = mock(EntityPlayer.class, "Client player");
		serverPlayer = mock(EntityPlayer.class, "Server player");

		clientWorld = TestWorld.create(true);
		serverWorld = TestWorld.create(false);

		clientPlayer.inventory = new InventoryPlayer(clientPlayer);
		serverPlayer.inventory = new InventoryPlayer(serverPlayer);
	}

	protected static class TestWorld extends SimpleWorld {
		public static TestWorld create(boolean client) {
			ISaveHandler saveHandler = mock(ISaveHandler.class);
			WorldInfo info = mock(WorldInfo.class);
			Dimension dimension = mock(Dimension.class);
			when(dimension.getType()).thenReturn(DimensionType.OVERWORLD);

			String name = client ? "Client world" : "Server world";

			return new TestWorld(saveHandler, info, dimension, new Profiler(), client, name);
		}

		private TestWorld(ISaveHandler saveHandlerIn, WorldInfo info, Dimension dimension, Profiler profilerIn, boolean client, String name) {
			super(saveHandlerIn, info, dimension, profilerIn, client);
			this.name = name;
		}

		private final String name;

		@Override
		public String toString() {
			return this.name;
		}

		public void addEntity(Entity e, int eid) {
			e.setEntityId(eid);
			e.setUniqueId(new UUID(0, eid));
			this.spawnEntity(e);
			this.entitiesById.addKey(eid, e);
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
		clientWorld.setBlockState(pos, state);
		serverWorld.setBlockState(pos, state);
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
			throw new ComparisonFailure("Mismatched NBT!", nbtString(expected), nbtString(actual));
		}
	}

	/**
	 * Produces a formatted NBT string.
	 */
	private String nbtString(NBTTagCompound tag) {
		String result = tag.toString();
		result = result.replaceAll("\\{", "\\{\n");
		result = result.replaceAll("\\}", "\n\\}");
		return result;
	}

	/**
	 * Checks that mixins were applied, and if not, then the test will be ignored
	 * (not failed).
	 *
	 * @see org.junit.Assume
	 */
	protected static void assumeMixinsApplied() {
		boolean applied = INetworkNameable.class.isAssignableFrom(InventoryBasic.class);
		if (!applied) {
			LOGGER.warn("Mixins were not applied; skipping this test");
		}
		assumeTrue("Expected mixins to be applied", applied);
	}
}
