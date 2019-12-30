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

import static wdl.versioned.VersionedFunctions.customName;

import org.junit.Test;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.util.math.BlockPos;
import wdl.handler.HandlerException;

public class DropperTest extends AbstractBlockHandlerTest<TileEntityDropper, ContainerDispenser, DropperHandler> {

	public DropperTest() {
		super(TileEntityDropper.class, ContainerDispenser.class, DropperHandler.class);
	}

	@Test
	public void testDropper() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.DROPPER);
		TileEntityDropper te = makeBlockEntity(pos);
		te.setInventorySlotContents(0, new ItemStack(Items.COD)); // something something flopper
		te.setInventorySlotContents(8, new ItemStack(Items.COD));

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	@Test
	public void testCustomName() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.DROPPER);
		TileEntityDropper te = makeBlockEntity(pos);
		te.setCustomName(customName("Something"));

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	@Test
	public void testCustomNameVanilla() throws HandlerException {
		assumeMixinsApplied();

		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.DROPPER);
		TileEntityDropper te = makeBlockEntity(pos);
		te.setCustomName(customName("Dropper"));

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}
}
