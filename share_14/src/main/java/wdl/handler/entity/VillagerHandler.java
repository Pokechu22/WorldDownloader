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

import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import wdl.ReflectionUtils;
import wdl.handler.HandlerException;

public class VillagerHandler extends EntityHandler<VillagerEntity, MerchantContainer> {
	public VillagerHandler() {
		super(VillagerEntity.class, MerchantContainer.class);
	}

	@Override
	public ITextComponent copyData(MerchantContainer container, VillagerEntity villager, boolean riding) throws HandlerException {
		IMerchant merchant = ReflectionUtils.findAndGetPrivateField(
				container, IMerchant.class);
		MerchantOffers recipes = merchant.getOffers();
		ReflectionUtils.findAndSetPrivateField(villager, AbstractVillagerEntity.class, MerchantOffers.class, recipes);

		return new TranslationTextComponent("wdl.messages.onGuiClosedInfo.savedEntity.villager.tradesOnly");
		// Other data is actually transfered properly now, fortunately
	}
}
