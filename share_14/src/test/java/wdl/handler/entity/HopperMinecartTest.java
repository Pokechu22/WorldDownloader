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

import org.junit.Test;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.minecart.HopperMinecartEntity;
import net.minecraft.inventory.container.HopperContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import wdl.handler.HandlerException;

public class HopperMinecartTest extends AbstractEntityHandlerTest<HopperMinecartEntity, HopperContainer, HopperMinecartHandler> {

	public HopperMinecartTest() {
		super(HopperMinecartEntity.class, HopperContainer.class, HopperMinecartHandler.class);
	}

	@Test
	public void testStorageHopper() throws HandlerException {
		makeMockWorld();
		HopperMinecartEntity minecart = new HopperMinecartEntity(EntityType.HOPPER_MINECART, serverWorld);
		minecart.setInventorySlotContents(2, new ItemStack(Items.DIAMOND));
		addEntity(minecart);

		runHandler(minecart.getEntityId(), createClientContainer(minecart));

		checkAllEntities();
	}
}
