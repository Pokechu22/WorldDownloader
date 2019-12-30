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
import net.minecraft.inventory.container.ShulkerBoxContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.util.math.BlockPos;
import wdl.handler.HandlerException;

public class ShulkerBoxTest extends AbstractBlockHandlerTest<ShulkerBoxTileEntity, ShulkerBoxContainer, ShulkerBoxHandler> {

	public ShulkerBoxTest() {
		super(ShulkerBoxTileEntity.class, ShulkerBoxContainer.class, ShulkerBoxHandler.class);
	}

	@Test
	public void testShulkerBox() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.RED_SHULKER_BOX);
		ShulkerBoxTileEntity te = makeBlockEntity(pos);
		te.setInventorySlotContents(0, new ItemStack(Blocks.REDSTONE_BLOCK, 64));

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	@Test
	public void testCustomName() throws HandlerException {
		assumeCustomNamesNotBroken();
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.LIME_SHULKER_BOX);
		ShulkerBoxTileEntity te = makeBlockEntity(pos);
		te.setCustomName(customName("Favorite things"));
		te.setInventorySlotContents(13, new ItemStack(Items.POISONOUS_POTATO, 64));

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	@Test
	public void testCustomNameVanilla() throws HandlerException {
		assumeCustomNamesNotBroken();
		assumeMixinsApplied();

		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.PURPLE_SHULKER_BOX);
		ShulkerBoxTileEntity te = makeBlockEntity(pos);
		te.setCustomName(customName("Shulker Box"));

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}
}
