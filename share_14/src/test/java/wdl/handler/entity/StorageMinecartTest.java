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
import net.minecraft.entity.item.minecart.ChestMinecartEntity;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import wdl.handler.HandlerException;

public class StorageMinecartTest extends AbstractEntityHandlerTest<ChestMinecartEntity, ChestContainer, StorageMinecartHandler> {

	public StorageMinecartTest() {
		super(ChestMinecartEntity.class, ChestContainer.class, StorageMinecartHandler.class);
	}

	@Test
	public void testStorageMinecart() throws HandlerException {
		makeMockWorld();
		ChestMinecartEntity minecart = new ChestMinecartEntity(EntityType.CHEST_MINECART, serverWorld);
		minecart.setInventorySlotContents(2, new ItemStack(Items.BEEF));
		addEntity(minecart);

		runHandler(minecart.getEntityId(), createClientContainer(minecart));

		checkAllEntities();
	}
}
