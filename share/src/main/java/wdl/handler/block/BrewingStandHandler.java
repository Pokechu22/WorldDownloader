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

import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import wdl.ReflectionUtils;

public class BrewingStandHandler extends BlockHandler<TileEntityBrewingStand, ContainerBrewingStand> {
	public BrewingStandHandler() {
		super(TileEntityBrewingStand.class, ContainerBrewingStand.class, "container.brewing");
	}

	@Override
	public String handle(BlockPos clickedPos, ContainerBrewingStand container,
			TileEntityBrewingStand blockEntity, IBlockAccess world,
			BiConsumer<BlockPos, TileEntityBrewingStand> saveMethod) throws HandlerException {
		IInventory brewingInventory = ReflectionUtils.findAndGetPrivateField(
				container, IInventory.class);
		String title = getCustomDisplayName(brewingInventory);
		saveContainerItems(container, blockEntity, 0);
		saveInventoryFields(brewingInventory, blockEntity);
		if (title != null) {
			blockEntity.setName(title);
		}
		saveMethod.accept(clickedPos, blockEntity);
		return "wdl.messages.onGuiClosedInfo.savedTileEntity.brewingStand";
	}
}
