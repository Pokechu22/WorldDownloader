/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.handler.entity;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import wdl.handler.HandlerException;

public class VillagerTest extends AbstractEntityHandlerTest<EntityVillager, ContainerMerchant, VillagerHandler> {

	public VillagerTest() {
		super(EntityVillager.class, ContainerMerchant.class, VillagerHandler.class);
	}

	@Test
	public void testActiveTrade() throws HandlerException {
		makeMockWorld();

		EntityVillager villager = new EntityVillager(clientWorld);
		MerchantRecipeList recipes = new MerchantRecipeList();
		recipes.add(new MerchantRecipe(new ItemStack(Items.DIAMOND, 64), Items.EMERALD));
		villager.setRecipes(recipes);
		addEntity(villager);

		runHandler(villager.getEntityId(), createClientContainer(villager));
		checkAllEntities();
	}

	@Override
	protected List<String> getIgnoreTags() {
		// We have no way to get this value, and it's not useful anyways.
		return ImmutableList.of("CareerLevel");
	}
}
