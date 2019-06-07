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

import static wdl.versioned.VersionedFunctions.*;

import java.util.function.BiConsumer;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.AbstractFurnaceContainer;
import net.minecraft.inventory.container.FurnaceContainer;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import wdl.ReflectionUtils;
import wdl.handler.HandlerException;

public class FurnaceHandler extends BlockHandler<FurnaceTileEntity, FurnaceContainer> {
	public FurnaceHandler() {
		super(FurnaceTileEntity.class, FurnaceContainer.class, "container.furnace");
	}

	@Override
	public ITextComponent handle(BlockPos clickedPos, FurnaceContainer container,
			FurnaceTileEntity blockEntity, IBlockReader world,
			BiConsumer<BlockPos, FurnaceTileEntity> saveMethod) throws HandlerException {
		IInventory furnaceInventory = ReflectionUtils.findAndGetPrivateField(
				container, AbstractFurnaceContainer.class, IInventory.class);
		String title = getCustomDisplayName(furnaceInventory);
		saveContainerItems(container, blockEntity, 0);
		saveInventoryFields(AbstractFurnaceContainer.class, container, AbstractFurnaceTileEntity.class, blockEntity);
		if (title != null) {
			blockEntity.func_213903_a(customName(title));
		}
		saveMethod.accept(clickedPos, blockEntity);
		return new TranslationTextComponent("wdl.messages.onGuiClosedInfo.savedTileEntity.furnace");
	}
}
