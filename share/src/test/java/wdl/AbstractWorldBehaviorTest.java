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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.player.inventory.ContainerLocalMenu;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;

/**
 * Base logic shared between all tests that use blocks.
 *
 * Ensures that the {@link Bootstrap} is initialized, so that classes such as
 * {@link Blocks} can be used.
 */
public abstract class AbstractWorldBehaviorTest {
	private static final Logger LOGGER = LogManager.getLogger();
	/** Worlds corresponding to what the server and client know */
	protected World serverWorld, clientWorld;
	/** A player entity.  Has a valid inventory. */
	protected EntityPlayer player = mock(EntityPlayer.class);
	/** A set containing all original TEs. */
	protected Set<BlockPos> origTEPoses;
	/** A map of block entities for the user to save into. */
	protected Map<BlockPos, TileEntity> tileEntities;

	@Before
	public void initBootstarp() {
		if (Bootstrap.isRegistered()) {
			LOGGER.debug("Bootstrap already initialized. ({})", this.getClass());
			return;
		}
		LOGGER.debug("Initializing bootstrap... ({})", this.getClass());
		Bootstrap.register();
		LOGGER.debug("Initialized bootstrap.");
		// Note: not checking Bootstrap.hasErrored as that didn't exist in older
		// versions
	}

	/**
	 * Creates a mock world, returning air for blocks and null for TEs.
	 *
	 * @return A mock world, with default behavior.
	 */
	protected void makeMockWorld() {
		clientWorld = mock(World.class);
		serverWorld = mock(World.class);
		tileEntities = new HashMap<>();
		origTEPoses = new HashSet<>();

		when(clientWorld.getBlockState(any())).thenReturn(Blocks.AIR.getDefaultState());
		when(serverWorld.getBlockState(any())).thenReturn(Blocks.AIR.getDefaultState());
		when(clientWorld.getTileEntity(any())).thenReturn(null);
		when(serverWorld.getTileEntity(any())).thenReturn(null);
	}

	/**
	 * Puts the given block into the mock worlds at the given position.
	 *
	 * @param world A mock world
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @param z z-coordinate
	 * @param block The block to put
	 */
	protected void placeBlockAt(int x, int y, int z, Block block) {
		placeBlockAt(x, y, z, block.getDefaultState());
	}

	/**
	 * Puts the given block into the mock worlds at the given position.
	 *
	 * @param world A mock world
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @param z z-coordinate
	 * @param block The block to put
	 */
	protected void placeBlockAt(int x, int y, int z, IBlockState state) {
		BlockPos pos = new BlockPos(x, y, z);

		when(clientWorld.getBlockState(pos)).thenReturn(state);
		when(serverWorld.getBlockState(pos)).thenReturn(state);
	}

	/**
	 * Puts the given block entity into the mock worlds at the given position.
	 * The server world gets the exact block entity; the client gets a default one.
	 *
	 * @param world A mock world
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @param z z-coordinate
	 * @param te The block entity to put
	 */
	protected void placeTEAt(int x, int y, int z, TileEntity te) {
		BlockPos pos = new BlockPos(x, y, z);

		origTEPoses.add(pos);

		IBlockState curState = clientWorld.getBlockState(pos);
		BlockContainer curBlock = (BlockContainer)(curState.getBlock());
		TileEntity defaultAtPos = curBlock.createNewTileEntity(clientWorld, curBlock.getMetaFromState(curState));

		when(clientWorld.getTileEntity(pos)).thenReturn(defaultAtPos);
		when(serverWorld.getTileEntity(pos)).thenReturn(te);
		te.setWorld(serverWorld);
	}

	/**
	 * Makes a container as the client would have.
	 *
	 * @param x x-coordinate of the block to open
	 * @param y y-coordinate of the block to open
	 * @param z z-coordinate of the block to open
	 */
	protected Container makeClientContainer(int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		// This is a bit of a mess, but inventories are a bit of a mess.
		// First, prepare an IInventory as the server would see it:
		TileEntity serverTE = serverWorld.getTileEntity(pos);
		// Preconditions for this to make sense
		assertNotNull(serverTE);
		assertTrue(serverTE instanceof IInventory);

		IInventory serverInv;
		if (serverWorld.getBlockState(pos).getBlock() instanceof BlockChest) {
			// Special-casing for large chests
			BlockChest chest = (BlockChest) serverWorld.getBlockState(pos).getBlock();
			serverInv = chest.getLockableContainer(serverWorld, pos);
		} else {
			serverInv = (IInventory) serverTE;
		}
		String guiID = ((IInteractionObject) serverTE).getGuiID();

		// Now do stuff as the client would see it...
		// NetHandlerPlayClient.handleOpenWindow(SPacketOpenWindow)
		IInventory clientInv;
		if (guiID.equals("minecraft:container")) {
			// Ender chest -- don't know if we can actually get here
			clientInv = new InventoryBasic(serverInv.getDisplayName(),
					serverInv.getSizeInventory());
		} else { // skip a few
			clientInv = new ContainerLocalMenu(guiID, serverInv.getDisplayName(),
					serverInv.getSizeInventory());
		}
		// Copy items and fields (this normally happens later, but whatever)
		for (int i = 0; i < serverInv.getSizeInventory(); i++) {
			clientInv.setInventorySlotContents(i, serverInv.getStackInSlot(i).copy());
		}
		for (int i = 0; i < serverInv.getFieldCount(); i++) {
			clientInv.setField(i, serverInv.getField(i));
		}

		// EntityPlayerSP.displayGUIChest(IInventory) found in handleOpenWindow
		// melded with the container code in each GUI
		Container container;
		if ("minecraft:chest".equals(guiID)) {
			container = new ContainerChest(player.inventory, clientInv, player);
		} else if ("minecraft:hopper".equals(guiID)) {
			container = new ContainerHopper(player.inventory, clientInv, player);
		} else if ("minecraft:furnace".equals(guiID)) {
			container = new ContainerFurnace(player.inventory, clientInv);
		} else if ("minecraft:brewing_stand".equals(guiID)) {
			container = new ContainerBrewingStand(player.inventory, clientInv);
		} else if ("minecraft:beacon".equals(guiID)) {
			container = new ContainerBeacon(player.inventory, clientInv);
		} else if ("minecraft:dispenser".equals(guiID) && "minecraft:dropper".equals(guiID)) {
			container = new ContainerDispenser(player.inventory, clientInv);
		// } else if ("minecraft:shulker_box".equals(guiID)) {
		//     container = new ContainerShulkerBox(player.inventory, clientInv, player);
		} else {
			// Unknown -- i.e. minecraft:container
			container = new ContainerChest(player.inventory, clientInv, player);
		}

		return container;
	}

	/**
	 * Checks that the saved world matches the original.
	 */
	protected void checkWorld() {
		assertThat("Must save all TEs", tileEntities.keySet(), is(origTEPoses));

		for (BlockPos pos : origTEPoses) {
			TileEntity server = serverWorld.getTileEntity(pos);
			TileEntity saved = tileEntities.get(pos);

			NBTTagCompound serverNBT = new NBTTagCompound();
			server.writeToNBT(serverNBT);
			NBTTagCompound savedNBT = new NBTTagCompound();
			saved.writeToNBT(savedNBT);

			assertThat(savedNBT, is(serverNBT));
		}
	}
}
