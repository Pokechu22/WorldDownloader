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

import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.HorseInventoryContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import wdl.ReflectionUtils;
import wdl.handler.HandlerException;

public class HorseHandler extends EntityHandler<AbstractHorseEntity, HorseInventoryContainer> {
	/**
	 * The number of slots used for the player inventory, so that the size
	 * of the horse's inventory can be computed.
	 */
	private static final int PLAYER_INVENTORY_SLOTS = 4 * 9;

	public HorseHandler() {
		super(AbstractHorseEntity.class, HorseInventoryContainer.class);
	}

	@Override
	public boolean checkRiding(HorseInventoryContainer container, AbstractHorseEntity riddenHorse) {
		AbstractHorseEntity horseInContainer = ReflectionUtils
				.findAndGetPrivateField(container, AbstractHorseEntity.class);

		// Intentional reference equals
		return horseInContainer == riddenHorse;
	}

	@Override
	public ITextComponent copyData(HorseInventoryContainer container, AbstractHorseEntity horse, boolean riding) throws HandlerException {
		Inventory horseInventory = new Inventory(container.inventorySlots.size() - PLAYER_INVENTORY_SLOTS);

		for (int i = 0; i < horseInventory.getSizeInventory(); i++) {
			Slot slot = container.getSlot(i);
			if (slot.getHasStack()) {
				horseInventory.setInventorySlotContents(i, slot.getStack());
			}
		}

		ReflectionUtils.findAndSetPrivateField(horse, AbstractHorseEntity.class, Inventory.class, horseInventory);

		if (riding) {
			return new TranslationTextComponent("wdl.messages.onGuiClosedInfo.savedRiddenEntity.horse");
		} else {
			return new TranslationTextComponent("wdl.messages.onGuiClosedInfo.savedEntity.horse");
		}
	}

}
