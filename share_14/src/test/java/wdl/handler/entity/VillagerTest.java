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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static wdl.versioned.VersionedFunctions.*;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.merchant.villager.VillagerData;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import wdl.handler.HandlerException;

public class VillagerTest extends AbstractEntityHandlerTest<VillagerEntity, MerchantContainer, VillagerHandler> {

	public VillagerTest() {
		super(VillagerEntity.class, MerchantContainer.class, VillagerHandler.class);
	}

	@Test
	public void testActiveTrade() throws HandlerException {
		makeMockWorld();

		VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, serverWorld);
		MerchantOffers recipes = new MerchantOffers();
		recipes.add(new MerchantOffer(new ItemStack(Items.DIAMOND, 64), ItemStack.EMPTY, new ItemStack(Items.EMERALD), 1, 1, 5));
		villager.setOffers(recipes);
		addEntity(villager);

		runHandler(villager.getEntityId(), createClientContainer(villager, false));
		checkAllEntities();
	}

	// The career cannot be identified with a custom name.  However, trades should still be saved.
	@Test
	public void testActiveTradeCustomName() {
		assumeCustomNamesNotBroken();
		makeMockWorld();

		VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, serverWorld);
		villager.setCustomName(customName("Testificate"));
		MerchantOffers recipes = new MerchantOffers();
		recipes.add(new MerchantOffer(new ItemStack(Items.DIAMOND, 64), ItemStack.EMPTY, new ItemStack(Items.EMERALD), 1, 1, 5));
		villager.setOffers(recipes);
		addEntity(villager);

		HandlerException ex = null;
		try {
			runHandler(villager.getEntityId(), createClientContainer(villager, false));
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

	@Test
	public void testMultipleTrades() throws HandlerException {
		makeMockWorld();

		VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, serverWorld);
		MerchantOffers recipes = new MerchantOffers();
		recipes.add(new MerchantOffer(new ItemStack(Items.DIAMOND, 64), ItemStack.EMPTY, new ItemStack(Items.EMERALD), 1, 1, 5));
		recipes.add(new MerchantOffer(new ItemStack(Items.DIAMOND, 64), new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.BUCKET), 1, 1, 5));
		recipes.add(new MerchantOffer(new ItemStack(Items.POISONOUS_POTATO), ItemStack.EMPTY, new ItemStack(Items.POISONOUS_POTATO), 1, 4, 9));
		villager.setOffers(recipes);
		addEntity(villager);

		runHandler(villager.getEntityId(), createClientContainer(villager, false));
		checkAllEntities();
	}

	@Override
	protected List<String> getIgnoreTags() {
		// We have no way to get this value, and it's not useful anyways.
		// "tag" on tools is also somewhat volatile (mainly because Damage is added
		// clientside for tools with full durability, but not serverside)
		return ImmutableList.<String>builder().addAll(super.getIgnoreTags()).add("CareerLevel").add("tag").build();
	}

	/**
	 * Tests that professions, biomes, and levels are saved.
	 *
	 * Note that this does NOT check that AI doesn't immediately reset the profession on world loading;
	 * it only tests what is immediately saved to NBT.
	 *
	 * Since this data is sync'd as entity metadata, there isn't much that needs to be handled.
	 *
	 * This test also uses randomly-generated trades.
	 */
	@Test
	public void testProfessionIdentification() throws Exception {
		// Note: type is the biome the villager is from. We can't iterate directly over
		// the registry since the type is VillagerType in 1.16+ and IVillagerType
		// earlier; it's not something that's worth renaming.
		// Using forEach would work since lambdas infer types, except that we need to
		// throw a checked exception which isn't allowed.
		for (ResourceLocation type : Registry.VILLAGER_TYPE.keySet()) {
			for (VillagerProfession prof : Registry.VILLAGER_PROFESSION) {
				for (int level = 1; level <= 5; level++) {
					makeMockWorld();
					VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, serverWorld);
					villager.setVillagerData(new VillagerData(Registry.VILLAGER_TYPE.getOrDefault(type), prof, level));
					addEntity(villager);
					if (prof != VillagerProfession.NITWIT && prof != VillagerProfession.NONE) {
						// Nitwits and "none" have no containers to test with.
						runHandler(villager.getEntityId(), createClientContainer(villager, false));
					}
					checkAllEntities();
				}
			}
		}
	}

	/**
	 * Tests whether villager experience is saved.
	 */
	@Test
	public void testXP() throws HandlerException {
		makeMockWorld();

		VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, serverWorld);
		villager.setVillagerData(villager.getVillagerData()
				.withProfession(VillagerProfession.CARTOGRAPHER)
				.withLevel(2));
		villager.setXp(1);
		MerchantOffers recipes = new MerchantOffers();
		recipes.add(new MerchantOffer(new ItemStack(Items.DIAMOND, 64), ItemStack.EMPTY, new ItemStack(Items.EMERALD), 1, 1, 5));
		villager.setOffers(recipes);
		addEntity(villager);

		runHandler(villager.getEntityId(), createClientContainer(villager, false));
		checkAllEntities();
	}
}
