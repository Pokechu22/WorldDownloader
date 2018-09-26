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

import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.mockito.AdditionalAnswers;

import junit.framework.ComparisonFailure;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.player.inventory.ContainerLocalMenu;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
	protected EntityPlayerSP clientPlayer;
	protected EntityPlayerMP serverPlayer;

	protected Minecraft mc;
	private final Container[] openContainerReference = { null };

	/**
	 * Creates a mock world, returning air for blocks and null for TEs.
	 */
	protected void makeMockWorld() {
		clientPlayer = mock(EntityPlayerSP.class, "Client player");
		serverPlayer = mock(EntityPlayerMP.class, "Server player");

		clientWorld = TestWorld.makeClient();
		serverWorld = TestWorld.makeServer();

		clientPlayer.inventory = new InventoryPlayer(clientPlayer);
		serverPlayer.inventory = new InventoryPlayer(serverPlayer);
		when(clientPlayer.getHorizontalFacing()).thenCallRealMethod();
		when(serverPlayer.getHorizontalFacing()).thenCallRealMethod();
		clientPlayer.world = clientWorld;
		serverPlayer.world = serverWorld;
		doCallRealMethod().when(clientPlayer).displayGUIChest(any());
		doCallRealMethod().when(serverPlayer).displayGUIChest(any());

		mc = mock(Minecraft.class);
		ReflectionUtils.findAndSetPrivateField(null, Minecraft.class, Minecraft.class, mc);

		openContainerReference[0] = null;
		doAnswer(AdditionalAnswers.<GuiScreen>answerVoid(screen -> {
			GuiContainer container = (GuiContainer)screen;
			openContainerReference[0] = container.inventorySlots;
		})).when(mc).displayGuiScreen(any());
		ReflectionUtils.findAndSetPrivateField(clientPlayer, EntityPlayerSP.class, Minecraft.class, mc);
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
	 * Puts the given block into the mock worlds at the given position.
	 *
	 * @param pos The position
	 * @param block The block to put
	 * @param facing The direction to place the block from
	 */
	protected void placeBlockAt(BlockPos pos, Block block, EnumFacing facing) {
		clientWorld.placeBlockAt(pos, block, clientPlayer, facing);
		serverWorld.placeBlockAt(pos, block, serverPlayer, facing);
	}

	/**
	 * Creates a container with the given GUI id.
	 *
	 * @param guiID
	 *            The ID
	 * @param serverInv
	 *            The inventory for the container (will be copied in this method).
	 * @return The container.  If it can't be figured out, throws an exception.
	 */
	protected Container makeContainer(String guiID, IInventory serverInv) {
		ContainerLocalMenu m = new ContainerLocalMenu(guiID, serverInv.getDisplayName(), serverInv.getSizeInventory());

		// Defer to displayGUIChest for actual creation logic
		clientPlayer.displayGUIChest(m);
		Container container = openContainerReference[0];
		openContainerReference[0] = null;

		assertNotNull("Should have created a container with ID " + guiID, container);

		// Copy items and fields.
		for (int i = 0; i < serverInv.getSizeInventory(); i++) {
			ItemStack serverItem = serverInv.getStackInSlot(i);
			if (serverItem == null) {
				// In older versions with nullable items
				continue;
			}
			m.setInventorySlotContents(i, serverItem.copy());
		}
		for (int i = 0; i < serverInv.getFieldCount(); i++) {
			m.setField(i, serverInv.getField(i));
		}

		return container;
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
			throw new ComparisonFailure("Mismatched NBT!", VersionedFunctions.nbtString(expected), VersionedFunctions.nbtString(actual));
		}
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
