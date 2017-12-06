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

import java.util.function.BiConsumer;

import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class DropperHandler extends BlockHandler<TileEntityDropper, ContainerDispenser> {
	public DropperHandler() {
		super(TileEntityDropper.class, ContainerDispenser.class);
	}

	@Override
	public String handle(BlockPos clickedPos, ContainerDispenser container,
			TileEntityDropper blockEntity, IBlockAccess world,
			BiConsumer<BlockPos, TileEntityDropper> saveMethod) throws HandlerException {
		saveContainerItems(container, blockEntity, 0);
		saveMethod.accept(clickedPos, blockEntity);
		return "wdl.messages.onGuiClosedInfo.savedTileEntity.dropper";
	}
}
