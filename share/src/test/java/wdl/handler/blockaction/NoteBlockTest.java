/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.handler.blockaction;

import org.junit.Test;

import net.minecraft.block.BlockNote;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.math.BlockPos;
import wdl.handler.HandlerException;

public class NoteBlockTest extends AbstractBlockActionHandlerTest<BlockNote, TileEntityNote, NoteBlockHandler> {

	public NoteBlockTest() {
		super(BlockNote.class, TileEntityNote.class, NoteBlockHandler.class);
	}

	@Test
	public void testTunePlay() throws HandlerException {
		makeMockWorld();
		placeBlockAt(BlockPos.ZERO, Blocks.NOTE_BLOCK);
		serverWorld.addTileEntity(new TileEntityNote());
		serverWorld.interactBlock(BlockPos.ZERO, serverPlayer);
		runHandler(BlockPos.ZERO);
		checkAllTEs();
	}

	@Test
	public void testHitPlay() throws HandlerException {
		makeMockWorld();
		placeBlockAt(BlockPos.ZERO, Blocks.NOTE_BLOCK);
		serverWorld.addTileEntity(new TileEntityNote());
		serverWorld.clickBlock(BlockPos.ZERO, serverPlayer);
		runHandler(BlockPos.ZERO);
		checkAllTEs();
	}

	@Test
	public void testAllNotes() throws HandlerException {
		for (byte i = 0; i < 25; i++) {
			makeMockWorld();
			placeBlockAt(BlockPos.ZERO, Blocks.NOTE_BLOCK);
			TileEntityNote te = new TileEntityNote();
			te.note = i;
			serverWorld.addTileEntity(te);
			serverWorld.clickBlock(BlockPos.ZERO, serverPlayer);
			runHandler(BlockPos.ZERO);
			checkAllTEs();
		}
	}
}
