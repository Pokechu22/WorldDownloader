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
package wdl.handler.block;

import org.junit.Test;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.BlockPos;
import wdl.handler.AbstractWorldBehaviorTest;
import wdl.handler.HandlerException;

public class FurnaceTest extends AbstractWorldBehaviorTest {
	@Override
	protected BlockHandler<?, ?> makeHandler() {
		return new FurnaceHandler();
	}

	@Test
	public void testFurance() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.FURNACE);
		TileEntityFurnace te = new TileEntityFurnace();
		te.setInventorySlotContents(0, new ItemStack(Blocks.COAL_ORE));
		te.setInventorySlotContents(1, new ItemStack(Items.WOODEN_HOE));
		te.setInventorySlotContents(2, new ItemStack(Items.COAL));
		placeTEAt(pos, te);

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	@Test
	public void testFields() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.FURNACE);
		TileEntityFurnace te = new TileEntityFurnace();
		te.setInventorySlotContents(0, new ItemStack(Blocks.COAL_ORE));
		te.setInventorySlotContents(1, new ItemStack(Items.WOODEN_HOE));
		te.setInventorySlotContents(2, new ItemStack(Items.COAL));
		te.setField(0, 100); // burn time
		// skip field 1 (total burn time) -- not saved: https://bugs.mojang.com/browse/MC-10025
		te.setField(2, 100); // cook time
		te.setField(3, 200); // total cook time (saved but unused?)
		placeTEAt(pos, te);

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	@Test
	public void testCustomName() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.FURNACE);
		TileEntityFurnace te = new TileEntityFurnace();
		te.setCustomInventoryName("Furni");
		placeTEAt(pos, te);

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	@Test
	public void testCustomNameVanilla() throws HandlerException {
		assumeMixinsApplied();

		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.FURNACE);
		TileEntityFurnace te = new TileEntityFurnace();
		te.setCustomInventoryName("Furnace");
		placeTEAt(pos, te);

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}
}
