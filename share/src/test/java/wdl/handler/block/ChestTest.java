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
package wdl.handler.block;

import static wdl.versioned.VersionedFunctions.customName;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

import org.junit.Ignore;
import org.junit.Test;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import wdl.handler.HandlerException;

/**
 * Tests single and double chests.
 */
public class ChestTest extends AbstractBlockHandlerTest<TileEntityChest, ContainerChest, ChestHandler> {

	public ChestTest() {
		super(TileEntityChest.class, ContainerChest.class, ChestHandler.class);
	}

	@Test
	public void testSingleChest() throws HandlerException {
		BlockPos center = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(center, Blocks.CHEST);
		TileEntityChest te = new TileEntityChest();
		te.setInventorySlotContents(2, new ItemStack(Items.BEEF));
		placeTEAt(center, te);

		ContainerChest container = (ContainerChest) makeClientContainer(center);

		runHandler(center, container);

		checkAllTEs();
	}

	@Test
	public void testSimpleDoubleChest() throws HandlerException {
		// TODO: Handle orientation of chests
		// Not important in this version, but will matter later

		// TODO: Maybe vary this, might help with +/- issues
		BlockPos center = new BlockPos(0, 0, 0);

		for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
			makeMockWorld();

			BlockPos offset = center.offset(direction);

			placeBlockAt(center, Blocks.CHEST);
			placeBlockAt(offset, Blocks.CHEST);
			TileEntityChest te1 = new TileEntityChest();
			te1.setInventorySlotContents(2, new ItemStack(Items.BEEF));
			TileEntityChest te2 = new TileEntityChest();
			te2.setInventorySlotContents(8, new ItemStack(Items.COOKED_BEEF));
			placeTEAt(center, te1);
			placeTEAt(offset, te2);

			ContainerChest container = (ContainerChest) makeClientContainer(center);
			assumeTrue("Expected a large test", container.inventorySlots.size() == 90);

			runHandler(center, container);

			checkAllTEs();
		}
	}

	@Test
	public void testRegularAndTrappedChest() throws HandlerException {
		// TODO: As with before, orientation

		// TODO: Maybe vary this, might help with +/- issues
		BlockPos center = new BlockPos(0, 0, 0);

		for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
			makeMockWorld();

			BlockPos offset = center.offset(direction);

			placeBlockAt(center, Blocks.CHEST);
			placeBlockAt(offset, Blocks.CHEST);
			TileEntityChest te1 = new TileEntityChest();
			te1.setInventorySlotContents(2, new ItemStack(Items.BEEF));
			TileEntityChest te2 = new TileEntityChest();
			te2.setInventorySlotContents(8, new ItemStack(Items.COOKED_BEEF));
			placeTEAt(center, te1);
			placeTEAt(offset, te2);

			for (EnumFacing direction2 : EnumFacing.Plane.HORIZONTAL) {
				if (direction2 == direction) continue;
				BlockPos offset2 = center.offset(direction2);
				placeBlockAt(offset2, Blocks.TRAPPED_CHEST);
				TileEntityChest badTE = new TileEntityChest();
				badTE.setInventorySlotContents(0, new ItemStack(Blocks.TNT));
				placeTEAt(offset2, badTE);
			}

			ContainerChest container = (ContainerChest) makeClientContainer(center);
			assumeTrue("Expected a large test", container.inventorySlots.size() == 90);

			runHandler(center, container);

			// Only those two were saved
			assertThat(tileEntities.keySet(), containsInAnyOrder(center, offset));

			assertSameNBT(tileEntities.get(center), te1);
			assertSameNBT(tileEntities.get(offset), te2);
		}
	}

	/**
	 * Naive test case for renamed double chests.  Does not handle only one
	 * chest being named (or two chests with different names).
	 */
	@Test
	public void testCustomNameNaive() throws HandlerException {
		makeMockWorld();

		BlockPos center = new BlockPos(0, 0, 0);
		BlockPos offset = new BlockPos(0, 0, 1);

		placeBlockAt(center, Blocks.CHEST);
		placeBlockAt(offset, Blocks.CHEST);
		TileEntityChest te1 = new TileEntityChest();
		te1.setInventorySlotContents(2, new ItemStack(Items.BEEF));
		te1.setCustomName(customName("Food"));
		TileEntityChest te2 = new TileEntityChest();
		te2.setInventorySlotContents(8, new ItemStack(Items.COOKED_BEEF));
		te2.setCustomName(customName("Food"));
		placeTEAt(center, te1);
		placeTEAt(offset, te2);

		ContainerChest container = (ContainerChest) makeClientContainer(center);
		assumeTrue("Expected a large test", container.inventorySlots.size() == 90);
		runHandler(center, container);
		checkAllTEs();
	}

	/**
	 * Real-world tests, that is known to fail. Two chests with conflicting names.
	 */
	@Test
	@Ignore("Known failure")
	public void testCustomNameConflict() throws HandlerException {
		makeMockWorld();

		BlockPos center = new BlockPos(0, 0, 0);
		BlockPos offset = new BlockPos(0, 0, 1);

		placeBlockAt(center, Blocks.CHEST);
		placeBlockAt(offset, Blocks.CHEST);
		TileEntityChest te1 = new TileEntityChest();
		te1.setInventorySlotContents(2, new ItemStack(Items.BEEF));
		te1.setCustomName(customName("Name 1"));
		TileEntityChest te2 = new TileEntityChest();
		te2.setInventorySlotContents(8, new ItemStack(Items.COOKED_BEEF));
		te2.setCustomName(customName("Name 2"));
		placeTEAt(center, te1);
		placeTEAt(offset, te2);

		ContainerChest container = (ContainerChest) makeClientContainer(center);
		runHandler(center, container);
		// Fails as both chests are named "Name 1"
		checkAllTEs();
	}

	/**
	 * Real-world tests, that is known to fail. Only one of two chests is named.
	 */
	@Test
	@Ignore("Known failure")
	public void testCustomNameSingle() throws HandlerException {
		makeMockWorld();

		BlockPos center = new BlockPos(0, 0, 0);
		BlockPos offset = new BlockPos(0, 0, 1);

		placeBlockAt(center, Blocks.CHEST);
		placeBlockAt(offset, Blocks.CHEST);
		TileEntityChest te1 = new TileEntityChest();
		te1.setInventorySlotContents(2, new ItemStack(Items.BEEF));
		te1.setCustomName(customName("Name 1"));
		TileEntityChest te2 = new TileEntityChest();
		te2.setInventorySlotContents(8, new ItemStack(Items.COOKED_BEEF));
		placeTEAt(center, te1);
		placeTEAt(offset, te2);

		ContainerChest container = (ContainerChest) makeClientContainer(center);
		runHandler(center, container);
		// Fails as both chests are named "Name 1" even though one should be unnamed
		checkAllTEs();
	}

	/**
	 * Two chests with a name that matches the vanilla name (this matters because
	 * translation)
	 */
	@Test
	public void testCustomNameMatchesRealName() throws HandlerException {
		assumeMixinsApplied();

		makeMockWorld();

		BlockPos center = new BlockPos(0, 0, 0);
		BlockPos offset = new BlockPos(0, 0, 1);

		placeBlockAt(center, Blocks.CHEST);
		placeBlockAt(offset, Blocks.CHEST);
		TileEntityChest te1 = new TileEntityChest();
		te1.setInventorySlotContents(2, new ItemStack(Items.BEEF));
		te1.setCustomName(customName("Large Chest"));
		TileEntityChest te2 = new TileEntityChest();
		te2.setInventorySlotContents(8, new ItemStack(Items.COOKED_BEEF));
		te2.setCustomName(customName("Large Chest"));
		placeTEAt(center, te1);
		placeTEAt(offset, te2);

		ContainerChest container = (ContainerChest) makeClientContainer(center);
		assumeTrue("Expected a large test", container.inventorySlots.size() == 90);
		runHandler(center, container);

		checkAllTEs();
	}

	/**
	 * Tests a single chest with a custom name.
	 */
	@Test
	public void testSingleChestCustomName() throws HandlerException {
		BlockPos center = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(center, Blocks.CHEST);
		TileEntityChest te = new TileEntityChest();
		te.setCustomName(customName("A chest"));
		te.setInventorySlotContents(2, new ItemStack(Items.BEEF));
		placeTEAt(center, te);

		ContainerChest container = (ContainerChest) makeClientContainer(center);

		runHandler(center, container);

		checkAllTEs();
	}

	/**
	 * A single chest with a name that matches the vanilla name (this matters
	 * because translation)
	 */
	@Test
	public void testSingleChestCustomNameMatchesRealName() throws HandlerException {
		assumeMixinsApplied();

		BlockPos center = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(center, Blocks.CHEST);
		TileEntityChest te = new TileEntityChest();
		te.setCustomName(customName("Chest"));
		te.setInventorySlotContents(2, new ItemStack(Items.BEEF));
		placeTEAt(center, te);

		ContainerChest container = (ContainerChest) makeClientContainer(center);

		runHandler(center, container);

		checkAllTEs();
	}

	/**
	 * Real-world tests, that is known to fail. A quintuple chest system.
	 */
	@Test
	@Ignore("Known failure")
	public void testQuintupleChest() throws HandlerException {
		makeMockWorld();

		BlockPos center = new BlockPos(0, 0, 0);

		placeBlockAt(center, Blocks.CHEST);
		TileEntityChest te = new TileEntityChest();
		te.setInventorySlotContents(2, new ItemStack(Items.BEEF));
		placeTEAt(center, te);
		for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
			BlockPos offset = center.offset(direction);
			placeBlockAt(offset, Blocks.CHEST);
			TileEntityChest te2 = new TileEntityChest();
			te2.setInventorySlotContents(8, new ItemStack(Items.COOKED_BEEF).setDisplayName(customName(direction.getName())));
			placeTEAt(offset, te2);
		}

		ContainerChest container = (ContainerChest) makeClientContainer(center);
		runHandler(center, container);
		// Fails due to only handling 2 of the chests
		checkAllTEs();
	}
}
