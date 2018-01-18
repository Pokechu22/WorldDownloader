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

import org.junit.Ignore;
import org.junit.Test;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerShulkerBox;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.math.BlockPos;
import wdl.handler.AbstractWorldBehaviorTest;
import wdl.handler.block.BlockHandler.HandlerException;

public class ShulkerBoxTest extends AbstractWorldBehaviorTest {
	@Override
	protected BlockHandler<?, ?> makeHandler() {
		return new ShulkerBoxHandler();
	}

	@Override
	protected Container makeContainer(String guiID, EntityPlayer player, IInventory clientInv) {
		if ("minecraft:shulker_box".equals(guiID)) {
			return new ContainerShulkerBox(player.inventory, clientInv, player);
		} else {
			return super.makeContainer(guiID, player, clientInv);
		}
	}

	@Test
	public void testShulkerBox() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.RED_SHULKER_BOX);
		TileEntityShulkerBox te = new TileEntityShulkerBox();
		te.setInventorySlotContents(0, new ItemStack(Blocks.REDSTONE_BLOCK, 64));
		placeTEAt(pos, te);

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	@Test
	@Ignore("Not yet implemented")
	public void testCustomName() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.LIME_SHULKER_BOX);
		TileEntityShulkerBox te = new TileEntityShulkerBox();
		te.setCustomName("Favorite things");
		te.setInventorySlotContents(13, new ItemStack(Items.POISONOUS_POTATO, 64));
		placeTEAt(pos, te);

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	@Test
	@Ignore("Known to fail")
	public void testCustomNameVanila() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.PURPLE_SHULKER_BOX);
		TileEntityShulkerBox te = new TileEntityShulkerBox();
		te.setCustomName("Shulker Box");
		placeTEAt(pos, te);

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}
}
