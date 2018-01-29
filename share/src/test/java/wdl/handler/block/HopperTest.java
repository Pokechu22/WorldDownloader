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
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.math.BlockPos;
import wdl.handler.AbstractWorldBehaviorTest;
import wdl.handler.block.BlockHandler.HandlerException;

public class HopperTest extends AbstractWorldBehaviorTest {
	@Override
	protected BlockHandler<?, ?> makeHandler() {
		return new HopperHandler();
	}

	@Test
	public void testHopper() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.HOPPER);
		TileEntityHopper te = new TileEntityHopper();
		te.setInventorySlotContents(0, new ItemStack(Items.DIAMOND));
		te.setInventorySlotContents(1, new ItemStack(Items.PAPER).setStackDisplayName("This is the filler! //"));
		te.setInventorySlotContents(2, new ItemStack(Items.PAPER).setStackDisplayName("Fi-ll-er dance! //"));
		te.setInventorySlotContents(3, new ItemStack(Items.PAPER).setStackDisplayName("When ya don't have 'nuff stuff"));
		te.setInventorySlotContents(4, new ItemStack(Items.PAPER).setStackDisplayName("for a whole hopper slot! //"));
		placeTEAt(pos, te);

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	@Test
	public void testCustomName() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.HOPPER);
		TileEntityHopper te = new TileEntityHopper();
		te.setCustomName("Bin");
		placeTEAt(pos, te);

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	@Test
	public void testCustomNameVanila() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.HOPPER);
		TileEntityHopper te = new TileEntityHopper();
		te.setCustomName("Hopper");
		placeTEAt(pos, te);

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}
}
