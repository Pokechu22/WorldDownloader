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

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.function.BiConsumer;

import org.junit.Test;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class DoubleChestTest extends AbstractWorldBehaviorTest {

	@Test
	public void testSimpleDoubleChest() {
		IBlockAccess world = makeMockWorld();
		placeBlockAt(world, 0, 0, 0, Blocks.CHEST);
		placeBlockAt(world, 0, 0, 1, Blocks.CHEST);
		TileEntityChest te1 = new TileEntityChest();
		TileEntityChest te2 = new TileEntityChest();
		placeTEAt(world, 0, 0, 0, te1);
		placeTEAt(world, 0, 0, 1, te2);

		@SuppressWarnings("unchecked")
		BiConsumer<BlockPos, TileEntityChest> method = mock(BiConsumer.class);

		ContainerChest container = new ContainerChest(new InventoryPlayer(null), new InventoryBasic("Test", true, 53), null);
		assertTrue(WDLEvents.saveDoubleChest(new BlockPos(0, 0, 0), container, world, method));

		verify(method).accept(new BlockPos(0, 0, 0), te1);
		verify(method).accept(new BlockPos(0, 0, 1), te2);
	}

}
