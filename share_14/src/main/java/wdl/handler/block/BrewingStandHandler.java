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
import net.minecraft.inventory.container.BrewingStandContainer;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import wdl.ReflectionUtils;
import wdl.handler.HandlerException;

public class BrewingStandHandler extends BlockHandler<BrewingStandTileEntity, BrewingStandContainer> {
	public BrewingStandHandler() {
		super(BrewingStandTileEntity.class, BrewingStandContainer.class, "container.brewing");
	}

	@Override
	public ITextComponent handle(BlockPos clickedPos, BrewingStandContainer container,
			BrewingStandTileEntity blockEntity, IBlockReader world,
			BiConsumer<BlockPos, BrewingStandTileEntity> saveMethod) throws HandlerException {
		IInventory brewingInventory = ReflectionUtils.findAndGetPrivateField(
				container, IInventory.class);
		String title = getCustomDisplayName(brewingInventory);
		saveContainerItems(container, blockEntity, 0);
		saveInventoryFields(container, blockEntity);
		if (title != null) {
			blockEntity.setCustomName(customName(title));
		}
		saveMethod.accept(clickedPos, blockEntity);
		return new TranslationTextComponent("wdl.messages.onGuiClosedInfo.savedTileEntity.brewingStand");
	}
}
