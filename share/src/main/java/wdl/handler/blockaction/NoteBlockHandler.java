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

import java.util.function.BiConsumer;

import net.minecraft.block.BlockNote;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockReader;
import wdl.handler.HandlerException;

/**
 * Handles note blocks.
 *
 * For 1.12.2 and below, data1 is the instrument (which we don't care about,
 * since it is determined by the block below) and data2 is the pitch (which we
 * do need to save).
 *
 * For 1.13, this data is stored in the block state and there is no block entity
 * at all.  As such, this code is not used then.
 */
public class NoteBlockHandler extends BlockActionHandler<BlockNote, TileEntityNote> {
	public NoteBlockHandler() {
		super(BlockNote.class, TileEntityNote.class);
	}

	@Override
	public ITextComponent handle(BlockPos pos, BlockNote block, TileEntityNote blockEntity, int data1, int data2,
			IBlockReader world, BiConsumer<BlockPos, TileEntityNote> saveMethod) throws HandlerException {
		blockEntity.note = (byte)(data2 % 25);
		saveMethod.accept(pos, blockEntity);
		return new TextComponentTranslation("wdl.messages.onBlockEvent.noteblock", pos, data2, blockEntity);
	}
}
