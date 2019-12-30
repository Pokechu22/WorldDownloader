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

import java.lang.reflect.Field;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BiMap;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipeList;
import wdl.ReflectionUtils;
import wdl.handler.HandlerException;
import wdl.versioned.VersionedFunctions;

public class VillagerHandler extends EntityHandler<EntityVillager, ContainerMerchant> {
	public VillagerHandler() {
		super(EntityVillager.class, ContainerMerchant.class);
	}

	@Override
	public ITextComponent copyData(ContainerMerchant container, EntityVillager villager, boolean riding) throws HandlerException {
		IMerchant merchant = ReflectionUtils.findAndGetPrivateField(
				container, IMerchant.class);
		MerchantRecipeList recipes = merchant.getOffers(merchant.getCustomer()); // note: parameter is ignored by all implementations
		ReflectionUtils.findAndSetPrivateField(villager, MerchantRecipeList.class, recipes);

		if (CAREER_ID_FIELD != null) {
			ITextComponent displayName = merchant.getDisplayName();
			if (!(displayName instanceof TextComponentTranslation)) {
				// Taking the toString to reflect JSON structure
				String componentDesc = String.valueOf(displayName);
				throw new HandlerException("wdl.messages.onGuiClosedWarning.villagerCareer.notAComponent", componentDesc);
			}
	
			TextComponentTranslation displayNameTranslation = ((TextComponentTranslation) displayName);
			String key = displayNameTranslation.getKey();

			int profession = villager.getProfession();
			int career;
			try {
				career = getCareer(key, profession);

				CAREER_ID_FIELD.setInt(villager, career);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new HandlerException("wdl.messages.onGuiClosedWarning.villagerCareer.exception", e);
			}
			return new TextComponentTranslation("wdl.messages.onGuiClosedInfo.savedEntity.villager.tradesAndCareer", displayName, profession, career);
		} else {
			return new TextComponentTranslation("wdl.messages.onGuiClosedInfo.savedEntity.villager.tradesOnly");
		}
	}

	/**
	 * A reference to {@link EntityVillager#careerId}.  May be null if it can't be found.
	 */
	@Nullable
	@VisibleForTesting
	static final Field CAREER_ID_FIELD;
	/**
	 * A reference to {@link EntityVillager#careerLevel}.  May be null if it can't be found.
	 */
	@Nullable
	@VisibleForTesting
	static final Field CAREER_LEVEL_FIELD;

	static {
		// XXX Iteration order of fields is undefined, and this is generally sloppy
		// careerId is the 4th field
		int fieldIndex = 0;
		Field careerLevelField = null;
		Field careerIdField = null;
		for (Field field : EntityVillager.class.getDeclaredFields()) {
			if (field.getType().equals(int.class)) {
				fieldIndex++;
				if (fieldIndex == 4) {
					careerIdField = field;
				} else if (fieldIndex == 5) {
					careerLevelField = field;
					break;
				}
			}
		}
		CAREER_ID_FIELD = careerIdField;
		CAREER_LEVEL_FIELD = careerLevelField;
		if (CAREER_ID_FIELD != null) {
			CAREER_ID_FIELD.setAccessible(true);
		}
		if (CAREER_LEVEL_FIELD != null) {
			CAREER_LEVEL_FIELD.setAccessible(true);
		}
	}
	/**
	 * Gets the career ID associated with the given translation name and villager
	 * profession ID.
	 *
	 * @implNote Does not handle forge
	 * @param profession
	 *            The current profession of the villager
	 * @param i18nKey
	 *            The i18n key used in the villager GUI
	 * @throws HandlerException when a known issue occurs (bad data).  Contains translation info.
	 */
	public static int getCareer(String i18nKey, int profession) throws HandlerException {
		if (!VersionedFunctions.VANILLA_VILLAGER_CAREERS.containsKey(profession)) {
			throw new HandlerException("wdl.messages.onGuiClosedWarning.villagerCareer.unknownProfession", profession);
		}

		BiMap<String, Integer> careerData = VersionedFunctions.VANILLA_VILLAGER_CAREERS.get(profession);

		if (!careerData.containsKey(i18nKey)) {
			throw new HandlerException("wdl.messages.onGuiClosedWarning.villagerCareer.unknownTitle", i18nKey, profession);
		}

		return careerData.get(i18nKey);
	}
}
