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

import java.lang.reflect.Field;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.village.MerchantRecipeList;
import wdl.ReflectionUtils;
import wdl.WDL;
import wdl.WDLMessageTypes;
import wdl.WDLMessages;
import wdl.handler.HandlerException;

public class VillagerHandler extends EntityHandler<EntityVillager, ContainerMerchant> {

	public VillagerHandler() {
		super(EntityVillager.class, ContainerMerchant.class);
	}

	@Override
	public String copyData(ContainerMerchant container, EntityVillager villager, boolean riding) throws HandlerException {
		IMerchant merchant = ReflectionUtils.findAndGetPrivateField(
				container, IMerchant.class);
		MerchantRecipeList recipes = merchant.getRecipes(WDL.thePlayer);
		ReflectionUtils.findAndSetPrivateField(villager, MerchantRecipeList.class, recipes);

		ITextComponent displayName = merchant.getDisplayName();
		if (!(displayName instanceof TextComponentTranslation)) {
			// Taking the toString to reflect JSON structure
			String componentDesc = String.valueOf(displayName);
			throw new HandlerException("wdl.messages.onGuiClosedWarning.villagerCareer.notAComponent", componentDesc);
		}

		TextComponentTranslation displayNameTranslation = ((TextComponentTranslation) displayName);
		String key = displayNameTranslation.getKey();

		try {
			int career = getCareer(key, villager.getProfession());
	
			// XXX Iteration order of fields is undefined, and this is generally sloppy
			// careerId is the 4th field
			int fieldIndex = 0;
			Field careerIdField = null;
			for (Field field : EntityVillager.class.getDeclaredFields()) {
				if (field.getType().equals(int.class)) {
					fieldIndex++;
					if (fieldIndex == 4) {
						careerIdField = field;
						break;
					}
				}
			}
			if (careerIdField == null) {
				throw new HandlerException("wdl.messages.onGuiClosedWarning.villagerCareer.professionField");
			}
	
			careerIdField.setAccessible(true);
			careerIdField.setInt(villager, career);
	
			// Re-create this component rather than modifying the old one
			ITextComponent dispCareer = new TextComponentTranslation(key, displayNameTranslation.getFormatArgs());
			dispCareer.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(key)));
	
			WDLMessages.chatMessageTranslated(WDLMessageTypes.ON_GUI_CLOSED_INFO, "wdl.messages.onGuiClosedInfo.savedEntity.villager.career", dispCareer, career);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new HandlerException("wdl.messages.onGuiClosedWarning.villagerCareer.exception", e);
		}

		return "wdl.messages.onGuiClosedInfo.savedEntity.villager";
	}

	/**
	 * A map mapping villager professions to a map from each career's I18n name to
	 * the career's ID.
	 *
	 * @see https://minecraft.gamepedia.com/Villager#Professions_and_careers
	 * @see EntityVillager#getDisplayName
	 */
	private static final Int2ObjectMap<BiMap<String, Integer>> VANILLA_VILLAGER_CAREERS = new Int2ObjectArrayMap<>();
	static {
		BiMap<String, Integer> farmer = HashBiMap.create(4);
		farmer.put("entity.Villager.farmer", 1);
		farmer.put("entity.Villager.fisherman", 2);
		farmer.put("entity.Villager.shepherd", 3);
		farmer.put("entity.Villager.fletcher", 4);
		BiMap<String, Integer> librarian = HashBiMap.create(2);
		librarian.put("entity.Villager.librarian", 1);
		// Since 1.11, but safe to include in 1.10 as the actual name won't appear then
		librarian.put("entity.Villager.cartographer", 2);
		BiMap<String, Integer> priest = HashBiMap.create(1);
		priest.put("entity.Villager.cleric", 1);
		BiMap<String, Integer> blacksmith = HashBiMap.create(3);
		blacksmith.put("entity.Villager.armor", 1);
		blacksmith.put("entity.Villager.weapon", 2);
		blacksmith.put("entity.Villager.tool", 3);
		BiMap<String, Integer> butcher = HashBiMap.create(2);
		butcher.put("entity.Villager.butcher", 1);
		butcher.put("entity.Villager.leather", 2);
		BiMap<String, Integer> nitwit = HashBiMap.create(1);
		nitwit.put("entity.Villager.nitwit", 1);

		VANILLA_VILLAGER_CAREERS.put(0, farmer);
		VANILLA_VILLAGER_CAREERS.put(1, librarian);
		VANILLA_VILLAGER_CAREERS.put(2, priest);
		VANILLA_VILLAGER_CAREERS.put(3, blacksmith);
		VANILLA_VILLAGER_CAREERS.put(4, butcher);
		VANILLA_VILLAGER_CAREERS.put(5, nitwit);
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
		if (!VANILLA_VILLAGER_CAREERS.containsKey(profession)) {
			throw new HandlerException("wdl.messages.onGuiClosedWarning.villagerCareer.unknownProfession", profession);
		}

		BiMap<String, Integer> careerData = VANILLA_VILLAGER_CAREERS.get(profession);

		if (!careerData.containsKey(i18nKey)) {
			throw new HandlerException("wdl.messages.onGuiClosedWarning.villagerCareer.unknownTitle", i18nKey, profession);
		}

		return careerData.get(i18nKey);
	}
}
