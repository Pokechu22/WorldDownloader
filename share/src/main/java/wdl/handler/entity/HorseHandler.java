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

import net.minecraft.entity.passive.EquineEntity;
import net.minecraft.inventory.ContainerHorseChest;
import net.minecraft.inventory.ContainerHorseInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import wdl.ReflectionUtils;
import wdl.handler.HandlerException;

public class HorseHandler extends EntityHandler<EquineEntity, ContainerHorseInventory> {
	/**
	 * The number of slots used for the player inventory, so that the size
	 * of the horse's inventory can be computed.
	 */
	private static final int PLAYER_INVENTORY_SLOTS = 4 * 9;

	public HorseHandler() {
		super(EquineEntity.class, ContainerHorseInventory.class);
	}

	@Override
	public boolean checkRiding(ContainerHorseInventory container, EquineEntity riddenHorse) {
		EquineEntity horseInContainer = ReflectionUtils
				.findAndGetPrivateField(container, EquineEntity.class);

		// Intentional reference equals
		return horseInContainer == riddenHorse;
	}

	@Override
	public ITextComponent copyData(ContainerHorseInventory container, EquineEntity horse, boolean riding) throws HandlerException {
		ContainerHorseChest horseInventory = new ContainerHorseChest(
				horse.getName(), // This was hardcoded to "HorseChest" in 1.12, but the name in 1.13.  The actual value is unused.
				container.inventorySlots.size() - PLAYER_INVENTORY_SLOTS);

		for (int i = 0; i < horseInventory.getSizeInventory(); i++) {
			Slot slot = container.getSlot(i);
			if (slot.getHasStack()) {
				horseInventory.setInventorySlotContents(i, slot.getStack());
			}
		}

		ReflectionUtils.findAndSetPrivateField(horse, EquineEntity.class, ContainerHorseChest.class, horseInventory);

		if (riding) {
			return new TextComponentTranslation("wdl.messages.onGuiClosedInfo.savedRiddenEntity.horse");
		} else {
			return new TextComponentTranslation("wdl.messages.onGuiClosedInfo.savedEntity.horse");
		}
	}

}
