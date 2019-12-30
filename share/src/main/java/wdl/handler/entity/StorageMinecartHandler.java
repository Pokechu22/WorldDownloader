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
package wdl.handler.entity;

import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import wdl.handler.HandlerException;

public class StorageMinecartHandler extends EntityHandler<EntityMinecartChest, ContainerChest> {

	public StorageMinecartHandler() {
		super(EntityMinecartChest.class, ContainerChest.class);
	}

	@Override
	public ITextComponent copyData(ContainerChest container, EntityMinecartChest minecart, boolean riding) throws HandlerException {
		for (int i = 0; i < minecart.getSizeInventory(); i++) {
			Slot slot = container.getSlot(i);
			if (slot.getHasStack()) {
				minecart.setInventorySlotContents(i, slot.getStack());
			}
		}

		return new TextComponentTranslation("wdl.messages.onGuiClosedInfo.savedEntity.storageMinecart");
	}

}
