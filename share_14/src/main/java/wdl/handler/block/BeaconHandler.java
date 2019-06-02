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

import java.util.function.BiConsumer;

import net.minecraft.inventory.container.BeaconContainer;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import wdl.handler.HandlerException;

public class BeaconHandler extends BlockHandler<BeaconTileEntity, BeaconContainer> {
	public BeaconHandler() {
		super(BeaconTileEntity.class, BeaconContainer.class, "container.beacon");
	}

	@Override
	public ITextComponent handle(BlockPos clickedPos, BeaconContainer container,
			BeaconTileEntity blockEntity, IBlockReader world,
			BiConsumer<BlockPos, BeaconTileEntity> saveMethod) throws HandlerException {
		/*IInventory beaconInventory = container.getTileEntity();
		String title = getCustomDisplayName(beaconInventory);
		saveContainerItems(container, blockEntity, 0);
		saveInventoryFields(beaconInventory, blockEntity);
		saveMethod.accept(clickedPos, blockEntity);
		if (title != null) {
			blockEntity.setCustomName(customName(title));
		}
		return new TranslationTextComponent("wdl.messages.onGuiClosedInfo.savedTileEntity.beacon");*/
		throw new HandlerException("Beacons are borked");
	}
}
