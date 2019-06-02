/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.handler.blockaction;

import org.junit.Test;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.util.math.BlockPos;
import wdl.handler.HandlerException;


import net.minecraft.block.BlockNote;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.math.BlockPos;
import wdl.handler.HandlerException;

public class NoteBlockTest extends AbstractBlockActionHandlerTest<NoteBlock, TileEntityNote, NoteBlockHandler> {

	public NoteBlockTest() {
		super(NoteBlock.class, TileEntityNote.class, NoteBlockHandler.class);
	}

	@Test
	public void testTunePlay() throws HandlerException {
		makeMockWorld();
		placeBlockAt(BlockPos.ORIGIN, Blocks.NOTE_BLOCK);
		serverWorld.addTileEntity(new TileEntityNote());
		serverWorld.interactBlock(BlockPos.ORIGIN, serverPlayer);
		runHandler(BlockPos.ORIGIN);
		checkAllTEs();
	}

	@Test
	public void testHitPlay() throws HandlerException {
		makeMockWorld();
		placeBlockAt(BlockPos.ORIGIN, Blocks.NOTE_BLOCK);
		serverWorld.addTileEntity(new TileEntityNote());
		serverWorld.clickBlock(BlockPos.ORIGIN, serverPlayer);
		runHandler(BlockPos.ORIGIN);
		checkAllTEs();
	}

	@Test
	public void testAllNotes() throws HandlerException {
		for (byte i = 0; i < 25; i++) {
			makeMockWorld();
			placeBlockAt(BlockPos.ORIGIN, Blocks.NOTE_BLOCK);
			TileEntityNote te = new TileEntityNote();
			te.note = i;
			serverWorld.addTileEntity(te);
			serverWorld.clickBlock(BlockPos.ORIGIN, serverPlayer);
			runHandler(BlockPos.ORIGIN);
			checkAllTEs();
		}
	}
}
