/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EquineEntity;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerHorseChest;
import net.minecraft.inventory.ContainerHorseInventory;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import wdl.gui.GuiWDLSaveProgress;
import wdl.versioned.VersionedFunctions;

/**
 * Tests all situations where {@link ReflectionUtils} is used, to verify that
 * the field exists.
 */
public class ReflectionUtilsUsageTest {

	/** Handles {@link CapeHandler#setPlayerCape(NetworkPlayerInfo, ResourceLocation)} */
	@Test
	public void testCapeHandlerSetPlayerCape() {
		ReflectionUtils.findField(NetworkPlayerInfo.class, Map.class);
	}

	/** Handles {@link CapeHandler#setupPlayer(AbstractClientPlayer)} */
	@Test
	public void testCapeHandlerSetupPlayer() {
		ReflectionUtils.findField(AbstractClientPlayer.class, NetworkPlayerInfo.class);
	}

	/** Handles {@link WDL#crashed(Throwable, String)} */
	@Test
	public void testWDLCrashed() {
		ReflectionUtils.findField(CrashReport.class, List.class);
	}

	/** Handles {@link WDL#getRealmName()} */
	@Test
	public void testWDLGetRealmName() {
		ReflectionUtils.findField(NetHandlerPlayClient.class, GuiScreen.class);
	}

	/** Handles {@link WDL#saveChunks(GuiWDLSaveProgress)} */
	@Test
	public void testWDLSaveChunks() {
		ReflectionUtils.findField(ChunkProviderClient.class, VersionedFunctions.getChunkListClass());
	}

	/** Handles {@link WDLChunkLoader#WDLChunkLoader(File)} */
	@Test
	public void testWDLChunkLoaderInit() {
		ReflectionUtils.findField(AnvilChunkLoader.class, Map.class);
	}

	/** Handles {@link WDLEvents#onItemGuiClosed()} */
	@Test
	public void testWDLEventsOnItemGuiClosed() {
		ReflectionUtils.findField(ContainerHorseInventory.class, EquineEntity.class);
		ReflectionUtils.findField(ContainerMerchant.class, IMerchant.class);
		ReflectionUtils.findField(EntityVillager.class, MerchantRecipeList.class);
		ReflectionUtils.findField(ContainerBrewingStand.class, IInventory.class);
		ReflectionUtils.findField(ContainerFurnace.class, IInventory.class);
	}

	/** Handles {@link WDLEvents#saveHorse(ContainerHorseInventory, EquineEntity)} */
	@Test
	public void testWDLEventsSaveHorse() {
		ReflectionUtils.findField(EquineEntity.class, ContainerHorseChest.class);
	}
}
