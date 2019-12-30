/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.handler.block;

import static wdl.versioned.VersionedFunctions.*;

import org.junit.Test;

import net.minecraft.block.Blocks;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.BarrelTileEntity;
import net.minecraft.util.math.BlockPos;
import wdl.handler.HandlerException;

/**
 * Tests single and double chests.
 */
public class BarrelTest extends AbstractBlockHandlerTest<BarrelTileEntity, ChestContainer, BarrelHandler> {
	public BarrelTest() {
		super(BarrelTileEntity.class, ChestContainer.class, BarrelHandler.class);
	}

	@Test
	public void testBarrel() throws HandlerException {
		BlockPos center = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(center, Blocks.BARREL);
		BarrelTileEntity te = makeBlockEntity(center);
		te.setInventorySlotContents(2, new ItemStack(Items.MUTTON));

		runHandler(center, makeClientContainer(center));

		checkAllTEs();
	}

	/**
	 * Tests a single chest with a custom name.
	 */
	@Test
	public void testCustomName() throws HandlerException {
		assumeCustomNamesNotBroken();
		BlockPos center = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(center, Blocks.BARREL);
		BarrelTileEntity te = makeBlockEntity(center);
		te.setCustomName(customName("Bibarrel"));
		te.setInventorySlotContents(2, new ItemStack(Items.MUTTON));

		runHandler(center, makeClientContainer(center));

		checkAllTEs();
	}

	@Test
	public void testCustomNameMatchesRealName() throws HandlerException {
		assumeCustomNamesNotBroken();
		assumeMixinsApplied();

		BlockPos center = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(center, Blocks.BARREL);
		BarrelTileEntity te = makeBlockEntity(center);
		te.setCustomName(customName("Barrel"));
		te.setInventorySlotContents(2, new ItemStack(Items.MUTTON));

		runHandler(center, makeClientContainer(center));

		checkAllTEs();
	}
}
