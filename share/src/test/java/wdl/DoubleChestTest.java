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

import static org.junit.Assert.*;

import org.junit.Test;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class DoubleChestTest extends AbstractWorldBehaviorTest {

	@Test
	public void testSimpleDoubleChest() {
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
			te1.setInventorySlotContents(8, new ItemStack(Items.COOKED_BEEF));
			placeTEAt(center, te1);
			placeTEAt(offset, te2);

			ContainerChest container = (ContainerChest) makeClientContainer(center);

			assertTrue(WDLEvents.saveDoubleChest(center, container, clientWorld, tileEntities::put));

			checkWorld();
		}
	}

}
