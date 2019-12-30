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

import net.minecraft.block.Blocks;
import net.minecraft.inventory.container.HopperContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.util.math.BlockPos;
import wdl.handler.HandlerException;

public class HopperTest extends AbstractBlockHandlerTest<HopperTileEntity, HopperContainer, HopperHandler> {

	public HopperTest() {
		super(HopperTileEntity.class, HopperContainer.class, HopperHandler.class);
	}

	@Test
	public void testHopper() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.HOPPER);
		HopperTileEntity te = makeBlockEntity(pos);
		te.setInventorySlotContents(0, new ItemStack(Items.DIAMOND));
		// Old reference, and not just to the SethBling short
		te.setInventorySlotContents(1, new ItemStack(Items.PAPER).setDisplayName(customName("This is the filler! //")));
		te.setInventorySlotContents(2, new ItemStack(Items.PAPER).setDisplayName(customName("Fi-ll-er dance! //")));
		te.setInventorySlotContents(3, new ItemStack(Items.PAPER).setDisplayName(customName("When ya don't have 'nuff stuff")));
		te.setInventorySlotContents(4, new ItemStack(Items.PAPER).setDisplayName(customName("for a whole hopper slot! //")));

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	@Test
	public void testCustomName() throws HandlerException {
		assumeCustomNamesNotBroken();
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.HOPPER);
		HopperTileEntity te = makeBlockEntity(pos);
		te.setCustomName(customName("Bin"));

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	@Test
	public void testCustomNameVanila() throws HandlerException {
		assumeCustomNamesNotBroken();
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.HOPPER);
		HopperTileEntity te = makeBlockEntity(pos);
		te.setCustomName(customName("Hopper"));

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}
}
