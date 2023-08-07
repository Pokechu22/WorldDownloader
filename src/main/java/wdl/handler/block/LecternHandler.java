/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.handler.block;

import java.util.function.BiConsumer;

import net.minecraft.inventory.container.LecternContainer;
import net.minecraft.tileentity.LecternTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import wdl.handler.HandlerException;

public class LecternHandler extends BlockHandler<LecternTileEntity, LecternContainer> {

	public LecternHandler() {
		super(LecternTileEntity.class, LecternContainer.class, "container.lectern");
	}

	@Override
	public ITextComponent handle(BlockPos clickedPos, LecternContainer container, LecternTileEntity blockEntity,
			IBlockReader world, BiConsumer<BlockPos, LecternTileEntity> saveMethod) throws HandlerException {
		blockEntity.setBook(container.getBook());
		saveInventoryFields(container, blockEntity); // current page
		// NOTE: Cannot be renamed
		saveMethod.accept(clickedPos, blockEntity);
		return new TranslationTextComponent("wdl.messages.onGuiClosedInfo.savedTileEntity.lectern");
	}

}
