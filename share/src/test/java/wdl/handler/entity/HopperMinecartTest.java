/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018-2020 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.handler.entity;

import org.junit.Test;

import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.item.ItemStack;
import wdl.handler.HandlerException;

public class HopperMinecartTest extends AbstractEntityHandlerTest<EntityMinecartHopper, ContainerHopper, HopperMinecartHandler> {

	public HopperMinecartTest() {
		super(EntityMinecartHopper.class, ContainerHopper.class, HopperMinecartHandler.class);
	}

	@Test
	public void testStorageHopper() throws HandlerException {
		makeMockWorld();
		EntityMinecartHopper minecart = new EntityMinecartHopper(serverWorld);
		minecart.setInventorySlotContents(2, new ItemStack(Items.DIAMOND));
		addEntity(minecart);

		runHandler(minecart.getEntityId(), createClientContainer(minecart, false));

		checkAllEntities();
	}
}
