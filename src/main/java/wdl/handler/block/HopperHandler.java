/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.handler.block;

import static wdl.versioned.VersionedFunctions.*;

import java.util.function.BiConsumer;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.HopperContainer;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import wdl.ReflectionUtils;
import wdl.handler.HandlerException;

public class HopperHandler extends BlockHandler<HopperTileEntity, HopperContainer> {
	public HopperHandler() {
		super(HopperTileEntity.class, HopperContainer.class, "container.hopper");
	}

	@Override
	public ITextComponent handle(BlockPos clickedPos, HopperContainer container,
			HopperTileEntity blockEntity, IBlockReader world,
			BiConsumer<BlockPos, HopperTileEntity> saveMethod) throws HandlerException {
		IInventory hopperInventory = ReflectionUtils.findAndGetPrivateField(
				container, IInventory.class);
		String title = getCustomDisplayName(hopperInventory);
		saveContainerItems(container, blockEntity, 0);
		saveMethod.accept(clickedPos, blockEntity);
		if (title != null) {
			blockEntity.setCustomName(customName(title));
		}
		return new TranslationTextComponent("wdl.messages.onGuiClosedInfo.savedTileEntity.hopper");
	}
}
