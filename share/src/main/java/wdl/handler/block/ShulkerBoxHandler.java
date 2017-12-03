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

import javax.annotation.Nonnull;

import net.minecraft.inventory.ContainerShulkerBox;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import wdl.ReflectionUtils;

public class ShulkerBoxHandler extends BlockHandler<TileEntityShulkerBox, ContainerShulkerBox> {
	public ShulkerBoxHandler() {
		super(TileEntityShulkerBox.class, ContainerShulkerBox.class);
	}

	@Override
	public @Nonnull String handle(@Nonnull BlockPos clickedPos, @Nonnull ContainerShulkerBox container,
			@Nonnull TileEntityShulkerBox blockEntity, @Nonnull IBlockAccess world,
			@Nonnull BiConsumer<BlockPos, TileEntityShulkerBox> saveMethod) throws HandlerException {
		IInventory brewingInventory = ReflectionUtils.findAndGetPrivateField(
				container, IInventory.class);
		saveContainerItems(container, blockEntity, 0);
		saveInventoryFields(brewingInventory, blockEntity);
		saveMethod.accept(clickedPos, blockEntity);
		return "wdl.messages.onGuiClosedInfo.savedTileEntity.shulkerBox";
	}
}
