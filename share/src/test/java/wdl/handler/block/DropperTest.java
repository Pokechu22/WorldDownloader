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
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.util.math.BlockPos;
import wdl.handler.AbstractWorldBehaviorTest;
import wdl.handler.HandlerException;

public class DropperTest extends AbstractWorldBehaviorTest {
	@Override
	protected BlockHandler<?, ?> makeHandler() {
		return new DropperHandler();
	}

	@Test
	public void testDropper() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.DROPPER);
		TileEntityDropper te = new TileEntityDropper();
		te.setInventorySlotContents(0, new ItemStack(Items.FISH)); // something something flopper
		te.setInventorySlotContents(8, new ItemStack(Items.FISH));
		placeTEAt(pos, te);

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	@Test
	public void testCustomName() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.DROPPER);
		TileEntityDropper te = new TileEntityDropper();
		te.setCustomName("Something");
		placeTEAt(pos, te);

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	@Test
	public void testCustomNameVanilla() throws HandlerException {
		assumeMixinsApplied();

		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.DROPPER);
		TileEntityDropper te = new TileEntityDropper();
		te.setCustomName("Dropper");
		placeTEAt(pos, te);

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}
}
