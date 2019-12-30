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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static wdl.versioned.VersionedFunctions.customName;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.nbt.CompoundNBT;
import wdl.ReflectionUtils;
import wdl.handler.HandlerException;

public class VillagerTest extends AbstractEntityHandlerTest<VillagerEntity, MerchantContainer, VillagerHandler> {

	public VillagerTest() {
		super(VillagerEntity.class, MerchantContainer.class, VillagerHandler.class);
	}

	@Test
	public void testActiveTrade() throws HandlerException {
		makeMockWorld();

		VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, serverWorld);
		MerchantOffers recipes = villager.getOffers();
		recipes.clear();
		recipes.add(new MerchantOffer(new ItemStack(Items.DIAMOND, 64), ItemStack.EMPTY, new ItemStack(Items.EMERALD), 1, 1, 5));
		ReflectionUtils.findAndSetPrivateField(villager, AbstractVillagerEntity.class, MerchantOffers.class, recipes);
		addEntity(villager);

		runHandler(villager.getEntityId(), createClientContainer(villager));
		checkAllEntities();
	}

	// The career cannot be identified with a custom name.  However, trades should still be saved.
	@Test
	public void testActiveTradeCustomName() {
		assumeCustomNamesNotBroken();
		makeMockWorld();

		VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, serverWorld);
		villager.setCustomName(customName("Testificate"));
		MerchantOffers recipes = villager.getOffers();
		recipes.clear();
		recipes.add(new MerchantOffer(new ItemStack(Items.DIAMOND, 64), ItemStack.EMPTY, new ItemStack(Items.EMERALD), 1, 1, 5));
		ReflectionUtils.findAndSetPrivateField(villager, AbstractVillagerEntity.class, MerchantOffers.class, recipes);
		addEntity(villager);

		HandlerException ex = null;
		try {
			runHandler(villager.getEntityId(), createClientContainer(villager));
		} catch (HandlerException e) {
			// Should throw an exception due to the custom name.
			ex = e;
		}
		assertThat(ex, is(notNullValue()));

		VillagerEntity clientVillager = (VillagerEntity)clientWorld.getEntityByID(villager.getEntityId());
		CompoundNBT serverNbt = new CompoundNBT();
		villager.writeWithoutTypeId(serverNbt);
		CompoundNBT clientNbt = new CompoundNBT();
		clientVillager.writeWithoutTypeId(clientNbt);
		// Check specifically the Offers tag; career will vary so we can't use it.
		assertSameNBT(serverNbt.getCompound("Offers"), clientNbt.getCompound("Offers"));
	}

	@Override
	protected List<String> getIgnoreTags() {
		// We have no way to get this value, and it's not useful anyways.
		return ImmutableList.<String>builder().addAll(super.getIgnoreTags()).add("CareerLevel").build();
	}

	/**
	 * Verifies that all villager careers are stored in {@link VillagerHandler#VANILLA_VILLAGER_CAREERS}.
	 */
	/*@Test
	public void testCareerIdentification() throws Exception {
		// Figure out how many professions and careers there are
		Field field = VillagerEntity.class.getDeclaredField("DEFAULT_TRADE_LIST_MAP");
		field.setAccessible(true);
		Object[][][][] tradesByProf = (Object[][][][]) field.get(null);
		int numProfessions = tradesByProf.length;
		for (int prof = 0; prof < numProfessions; prof++) {
			if (prof == 5) {
				// Skip nitwit, as it does not have any trades and thus no trade GUI
				continue;
			}
			int numCareers = tradesByProf[prof].length;
			for (int career = 1; career <= numCareers; career++) { // careers start at 1
				makeMockWorld();

				VillagerEntity villager = new VillagerEntity(serverWorld, prof);
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
	}*/

	/**
	 * Verifies that the villager career field was identified correctly.
	 */
	/*@Test
	public void checkCareerField() {
		assertThat(VillagerHandler.CAREER_ID_FIELD.getName(), is("careerId"));
		assertThat(VillagerHandler.CAREER_LEVEL_FIELD.getName(), is("careerLevel"));
	}*/
}
