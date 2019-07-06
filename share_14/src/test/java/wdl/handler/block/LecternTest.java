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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.inventory.container.LecternContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tileentity.LecternTileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import wdl.ReflectionUtils;
import wdl.handler.HandlerException;

public class LecternTest extends AbstractBlockHandlerTest<LecternTileEntity, LecternContainer, LecternHandler> {

	public LecternTest() {
		super(LecternTileEntity.class, LecternContainer.class, LecternHandler.class);
	}

	@Test
	public void testLectern() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.LECTERN);
		 makeBlockEntity(pos);
		ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
		ListNBT pages = new ListNBT();
		pages.add(new StringNBT("{\"text\": \"Hello world!\"}"));
		stack.setTagInfo("pages", pages);
		assertTrue(LecternBlock.tryPlaceBook(serverWorld, pos, serverWorld.getBlockState(pos), stack));

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	@Test
	public void testPageNumber() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.LECTERN);
		LecternTileEntity te = makeBlockEntity(pos);
		ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
		ListNBT pages = new ListNBT();
		pages.add(new StringNBT("\"p1\""));
		pages.add(new StringNBT("\"p2\""));
		pages.add(new StringNBT("\"p3\""));
		pages.add(new StringNBT("\"p4\""));
		pages.add(new StringNBT("\"p5\""));
		stack.setTagInfo("pages", pages);
		LecternBlock.tryPlaceBook(serverWorld, pos, serverWorld.getBlockState(pos), stack);
		IIntArray fields = ReflectionUtils.findAndGetPrivateField(te, IIntArray.class);
		fields.set(0, 3); // Update the page
		assertThat(te.getPage(), is(3));

		LecternContainer test = (LecternContainer)makeClientContainer(pos);
		runHandler(pos, test);
		checkAllTEs();
	}
}
