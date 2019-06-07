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

import org.junit.Test;

import net.minecraft.block.Blocks;
import net.minecraft.inventory.container.BrewingStandContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import wdl.ReflectionUtils;
import wdl.handler.HandlerException;

public class BrewingStandTest extends AbstractBlockHandlerTest<BrewingStandTileEntity, BrewingStandContainer, BrewingStandHandler> {

	public BrewingStandTest() {
		super(BrewingStandTileEntity.class, BrewingStandContainer.class, BrewingStandHandler.class);
	}

	/**
	 * Simple case for brewing stands
	 */
	@Test
	public void testInventory() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.BREWING_STAND);
		BrewingStandTileEntity te = makeBlockEntity(pos);
		te.setInventorySlotContents(0, new ItemStack(Items.GLASS_BOTTLE));
		te.setInventorySlotContents(1, new ItemStack(Items.GLASS_BOTTLE));
		te.setInventorySlotContents(2, new ItemStack(Items.GLASS_BOTTLE));
		te.setInventorySlotContents(3, new ItemStack(Items.SPIDER_EYE));
		te.setInventorySlotContents(4, new ItemStack(Items.BLAZE_POWDER));

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	/**
	 * Test brewing stand fields
	 */
	@Test
	public void testFields() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.BREWING_STAND);
		BrewingStandTileEntity te = makeBlockEntity(pos);
		IIntArray fields = ReflectionUtils.findAndGetPrivateField(te, IIntArray.class);
		fields.func_221477_a(0, 10); // brew time
		fields.func_221477_a(1, 13); // fuel

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	/**
	 * Test a normal custom name
	 */
	@Test
	public void testCustomName() throws HandlerException {
		assumeCustomNamesNotBroken();
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.BREWING_STAND);
		BrewingStandTileEntity te = makeBlockEntity(pos);
		te.func_213903_a(customName("Potion maker"));

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	/**
	 * Test a normal custom name matching the vanilla name
	 */
	@Test
	public void testCustomNameVanilla() throws HandlerException {
		assumeCustomNamesNotBroken();
		assumeMixinsApplied();

		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.BREWING_STAND);
		BrewingStandTileEntity te = makeBlockEntity(pos);
		te.func_213903_a(customName("Brewing Stand"));

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}
}
