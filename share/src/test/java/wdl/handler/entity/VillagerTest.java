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

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.lang.reflect.Field;
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

	/**
	 * Verifies that all villager careers are stored in {@link VillagerHandler#VANILLA_VILLAGER_CAREERS}.
	 */
	@Test
	public void testCareerIdentification() throws Exception {
		// Figure out how many professions and careers there are
		Field field = EntityVillager.class.getDeclaredField("DEFAULT_TRADE_LIST_MAP");
		field.setAccessible(true);
		Object[][][][] tradesByProf = (Object[][][][]) field.get(null);
		int numProfessions = tradesByProf.length;
		for (int prof = 0; prof < numProfessions; prof++) {
			int numCareers = tradesByProf[prof].length;
			for (int career = 1; career <= numCareers; career++) { // careers start at 1
				makeMockWorld();

				EntityVillager villager = new EntityVillager(clientWorld, prof);
				VillagerHandler.CAREER_ID_FIELD.setInt(villager, career);
				// Needed to avoid the entity recalculating the career when it gets the display name name
				VillagerHandler.CAREER_LEVEL_FIELD.setInt(villager, 1);
				addEntity(villager);

				runHandler(villager.getEntityId(), createClientContainer(villager));

				checkAllEntities();
				// Verify the career was also saved correctly.
				Object clientVillager = clientWorld.getEntityByID(villager.getEntityId());
				assertThat(VillagerHandler.CAREER_ID_FIELD.getInt(clientVillager), is(career));
			}
		}
	}

	/**
	 * Verifies that the villager career field was identified correctly.
	 */
	@Test
	public void checkCareerField() {
		assertThat(VillagerHandler.CAREER_ID_FIELD.getName(), is("careerId"));
		assertThat(VillagerHandler.CAREER_LEVEL_FIELD.getName(), is("careerLevel"));
	}
}
