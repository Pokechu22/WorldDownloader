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
package wdl.handler.block;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static wdl.versioned.VersionedFunctions.customName;

import org.junit.Ignore;
import org.junit.Test;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import wdl.VersionConstants;
import wdl.handler.HandlerException;

/**
 * Tests single and double chests.
 */
public abstract class BaseChestTest<B extends ChestTileEntity, H extends BaseChestHandler<B>> extends AbstractBlockHandlerTest<B, ChestContainer, H> {
	private final Block ownBlock, otherBlock;

	/**
	 * @param ownBlock The block used by this class (e.g. regular chests)
	 * @param otherBlock The block not used by this class (e.g. trapped chests)
	 * @param blockEntityClass As per AbstractBlockHandlerTest
	 * @param handlerClass As per AbstractBlockHandlerTest
	 */
	protected BaseChestTest(Block ownBlock, Block otherBlock,
			Class<B> blockEntityClass, Class<H> handlerClass) {
		super(blockEntityClass, ChestContainer.class, handlerClass);
		this.ownBlock = ownBlock;
		this.otherBlock = otherBlock;
	}

	@Test
	public void testSingleChest() throws HandlerException {
		BlockPos center = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(center, ownBlock);
		ChestTileEntity te = makeBlockEntity(center);
		te.setInventorySlotContents(2, new ItemStack(Items.BEEF));

		ChestContainer container = (ChestContainer) makeClientContainer(center);

		runHandler(center, container);

		checkAllTEs();
	}

	@Test
	public void testSimpleDoubleChest() throws HandlerException {
		// TODO: Maybe vary this, might help with +/- issues
		BlockPos center = new BlockPos(0, 0, 0);

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			Direction[] orientations = { direction.rotateY(), direction.rotateYCCW() };
			for (Direction orientation : orientations) {
				makeMockWorld();

				BlockPos offset = center.offset(direction);

				placeBlockAt(center, ownBlock, orientation);
				placeBlockAt(offset, ownBlock, orientation);
				ChestTileEntity te1 = makeBlockEntity(center);
				te1.setInventorySlotContents(2, new ItemStack(Items.BEEF));
				ChestTileEntity te2 = makeBlockEntity(offset);
				te2.setInventorySlotContents(8, new ItemStack(Items.COOKED_BEEF));

				ChestContainer container = (ChestContainer) makeClientContainer(center);

				runHandler(center, container);

				checkAllTEs();
			}
		}
	}

	/**
	 * Tests a mix of both the regular chest and the other kind of chest next to each other.
	 */
	@Test
	public void testRegularAndOtherChest() throws HandlerException {
		// TODO: Maybe vary this, might help with +/- issues
		BlockPos center = new BlockPos(0, 0, 0);

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			Direction[] orientations = { direction.rotateY(), direction.rotateYCCW() };
			for (Direction orientation : orientations) {
				makeMockWorld();

				BlockPos offset = center.offset(direction);

				placeBlockAt(center, ownBlock, orientation);
				placeBlockAt(offset, ownBlock, orientation);
				ChestTileEntity te1 = makeBlockEntity(center);
				te1.setInventorySlotContents(2, new ItemStack(Items.BEEF));
				ChestTileEntity te2 = makeBlockEntity(offset);
				te2.setInventorySlotContents(8, new ItemStack(Items.COOKED_BEEF));

				for (Direction direction2 : Direction.Plane.HORIZONTAL) {
					if (direction2 == direction) continue;
					BlockPos offset2 = center.offset(direction2);
					placeBlockAt(offset2, otherBlock, orientation);
					ChestTileEntity badTE = makeBlockEntity(offset2);
					badTE.setInventorySlotContents(0, new ItemStack(Blocks.TNT));
				}

				ChestContainer container = (ChestContainer) makeClientContainer(center);

				runHandler(center, container);

				// Only those two were saved
				assertThat(tileEntities.keySet(), containsInAnyOrder(center, offset));

				assertSameNBT(tileEntities.get(center), te1);
				assertSameNBT(tileEntities.get(offset), te2);
			}
		}
	}

	/**
	 * Naive test case for renamed double chests.  Does not handle only one
	 * chest being named (or two chests with different names).
	 */
	@Test
	public void testCustomNameNaive() throws HandlerException {
		assumeCustomNamesNotBroken();
		makeMockWorld();

		BlockPos center = new BlockPos(0, 0, 0);
		BlockPos offset = new BlockPos(0, 0, 1);

		placeBlockAt(center, ownBlock, Direction.EAST);
		placeBlockAt(offset, ownBlock, Direction.EAST);
		ChestTileEntity te1 = makeBlockEntity(center);
		te1.setInventorySlotContents(2, new ItemStack(Items.BEEF));
		te1.setCustomName(customName("Food"));
		ChestTileEntity te2 = makeBlockEntity(offset);
		te2.setInventorySlotContents(8, new ItemStack(Items.COOKED_BEEF));
		te2.setCustomName(customName("Food"));

		ChestContainer container = (ChestContainer) makeClientContainer(center);
		runHandler(center, container);
		checkAllTEs();
	}

	/**
	 * Real-world tests, that is known to fail. Two chests with conflicting names.
	 */
	@Test
	@Ignore("Known failure")
	public void testCustomNameConflict() throws HandlerException {
		assumeCustomNamesNotBroken();
		makeMockWorld();

		BlockPos center = new BlockPos(0, 0, 0);
		BlockPos offset = new BlockPos(0, 0, 1);

		placeBlockAt(center, ownBlock, Direction.EAST);
		placeBlockAt(offset, ownBlock, Direction.EAST);
		ChestTileEntity te1 = makeBlockEntity(center);
		te1.setInventorySlotContents(2, new ItemStack(Items.BEEF));
		te1.setCustomName(customName("Name 1"));
		ChestTileEntity te2 = makeBlockEntity(offset);
		te2.setInventorySlotContents(8, new ItemStack(Items.COOKED_BEEF));
		te2.setCustomName(customName("Name 2"));

		ChestContainer container = (ChestContainer) makeClientContainer(center);
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
		assumeCustomNamesNotBroken();
		makeMockWorld();

		BlockPos center = new BlockPos(0, 0, 0);
		BlockPos offset = new BlockPos(0, 0, 1);

		placeBlockAt(center, ownBlock, Direction.EAST);
		placeBlockAt(offset, ownBlock, Direction.EAST);
		ChestTileEntity te1 = makeBlockEntity(center);
		te1.setInventorySlotContents(2, new ItemStack(Items.BEEF));
		te1.setCustomName(customName("Name 1"));
		ChestTileEntity te2 = makeBlockEntity(offset);
		te2.setInventorySlotContents(8, new ItemStack(Items.COOKED_BEEF));

		ChestContainer container = (ChestContainer) makeClientContainer(center);
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
		assumeCustomNamesNotBroken();
		assumeMixinsApplied();

		makeMockWorld();

		BlockPos center = new BlockPos(0, 0, 0);
		BlockPos offset = new BlockPos(0, 0, 1);

		placeBlockAt(center, ownBlock, Direction.EAST);
		placeBlockAt(offset, ownBlock, Direction.EAST);
		ChestTileEntity te1 = makeBlockEntity(center);
		te1.setInventorySlotContents(2, new ItemStack(Items.BEEF));
		te1.setCustomName(customName("Large Chest"));
		ChestTileEntity te2 = makeBlockEntity(offset);
		te2.setInventorySlotContents(8, new ItemStack(Items.COOKED_BEEF));
		te2.setCustomName(customName("Large Chest"));

		ChestContainer container = (ChestContainer) makeClientContainer(center);
		runHandler(center, container);

		checkAllTEs();
	}

	/**
	 * Tests a single chest with a custom name.
	 */
	@Test
	public void testSingleChestCustomName() throws HandlerException {
		assumeCustomNamesNotBroken();
		BlockPos center = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(center, ownBlock);
		ChestTileEntity te = makeBlockEntity(center);
		te.setCustomName(customName("A chest"));
		te.setInventorySlotContents(2, new ItemStack(Items.BEEF));

		ChestContainer container = (ChestContainer) makeClientContainer(center);

		runHandler(center, container);

		checkAllTEs();
	}

	/**
	 * A single chest with a name that matches the vanilla name (this matters
	 * because translation)
	 */
	@Test
	public void testSingleChestCustomNameMatchesRealName() throws HandlerException {
		assumeCustomNamesNotBroken();
		assumeMixinsApplied();

		BlockPos center = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(center, ownBlock);
		ChestTileEntity te = makeBlockEntity(center);
		te.setCustomName(customName("Chest"));
		te.setInventorySlotContents(2, new ItemStack(Items.BEEF));

		ChestContainer container = (ChestContainer) makeClientContainer(center);

		runHandler(center, container);

		checkAllTEs();
	}

	/**
	 * Real-world tests, that is known to fail. A quintuple chest system.
	 */
	@Test
	@Ignore("Known failure")
	public void testQuintupleChest() throws HandlerException {
		assumeTrue("Only applies in non-flattened versions", VersionConstants.getDataVersion() < 1451 /* FLATTENING */);
		makeMockWorld();

		BlockPos center = new BlockPos(0, 0, 0);

		placeBlockAt(center, ownBlock);
		ChestTileEntity te = makeBlockEntity(center);
		te.setInventorySlotContents(2, new ItemStack(Items.BEEF));
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos offset = center.offset(direction);
			placeBlockAt(offset, ownBlock, Direction.EAST);
			ChestTileEntity te2 = makeBlockEntity(offset);
			te2.setInventorySlotContents(8, new ItemStack(Items.COOKED_BEEF).setDisplayName(customName(direction.getName())));
		}

		ChestContainer container = (ChestContainer) makeClientContainer(center);
		runHandler(center, container);
		// Fails due to only handling 2 of the chests
		checkAllTEs();
	}

	/**
	 * Checks behavior of multiple large chests near each other.
	 */
	@Test
	public void testMultipleDoubleChests() throws HandlerException {
		assumeTrue("Only applies in flattened versions", VersionConstants.getDataVersion() >= 1451 /* FLATTENING */);
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			Direction[] orientations = { direction.rotateY(), direction.rotateYCCW() };
			for (Direction orientation : orientations) {
				makeMockWorld();

				BlockPos center = new BlockPos(0, 0, 0);
				BlockPos offset = center.offset(direction);

				// Prepare a 6x3 grid
				for (int row = -1; row <= 1; row++) {
					for (int col = -2; col < 4; col++) {
						BlockPos pos = center.offset(direction, col).offset(orientation, row);
						placeBlockAt(pos, ownBlock, orientation);
						ChestTileEntity te = makeBlockEntity(pos);
						te.setInventorySlotContents(13, new ItemStack(Items.NAME_TAG).setDisplayName(customName(pos.toString())));
					}
				}

				ChestContainer container = (ChestContainer) makeClientContainer(center);
				TileEntity te1 = serverWorld.getTileEntity(center);
				TileEntity te2 = serverWorld.getTileEntity(offset);

				runHandler(center, container);

				// Only those two were saved
				assertThat(tileEntities.keySet(), containsInAnyOrder(center, offset));
				assertSameNBT(tileEntities.get(center), te1);
				assertSameNBT(tileEntities.get(offset), te2);
			}
		}
	}
}
