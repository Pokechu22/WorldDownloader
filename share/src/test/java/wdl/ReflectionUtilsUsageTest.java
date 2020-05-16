/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2020 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;

import java.io.File;
import java.util.List;

import org.junit.Test;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EquineEntity;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.ContainerHorseChest;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.IInventory;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import wdl.gui.GuiWDLSaveProgress;
import wdl.handler.block.BrewingStandHandler;
import wdl.handler.block.DispenserHandler;
import wdl.handler.block.DropperHandler;
import wdl.handler.block.FurnaceHandler;
import wdl.handler.block.HopperHandler;
import wdl.handler.entity.HorseHandler;
import wdl.handler.entity.VillagerHandler;
import wdl.versioned.VersionedFunctions;

/**
 * Tests all situations where {@link ReflectionUtils} is used, to verify that
 * the field exists.
 */
public class ReflectionUtilsUsageTest extends MaybeMixinTest {

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

	/** Handles {@link WDLChunkLoaderBase#WDLChunkLoaderBase(File)} */
	@Test
	public void testWDLChunkLoaderInit() {
		ReflectionUtils.findField(AnvilChunkLoader.class, VersionedFunctions.getChunksToSaveClass());
	}

	/** Handles {@link BrewingStandHandler#handle} */
	@Test
	public void testBrewingStandHandler() {
		ReflectionUtils.findField(ContainerBrewingStand.class, IInventory.class);
	}

	/** Handles {@link DispenserHandler#handle} and {@link DropperHandler#handle} */
	@Test
	public void testDispenserHandler() {
		ReflectionUtils.findField(ContainerDispenser.class, IInventory.class);
	}

	/** Handles {@link FurnaceHandler#handle} */
	@Test
	public void testFuranceHandler() {
		ReflectionUtils.findField(ContainerFurnace.class, IInventory.class);
	}

	/** Handles {@link HopperHandler#handle} */
	@Test
	public void testHopperHandler() {
		ReflectionUtils.findField(ContainerHopper.class, IInventory.class);
	}

	// Skipping ShulkerBoxHandler due to version properties; testing is mostly redundant anyways
	// since the handlers have their own dedicated tests

	/** Handles {@link HorseHandler#copyData} and {@link HorseHandler#checkRiding} */
	@Test
	public void testHorseHandler() {
		ReflectionUtils.findField(EquineEntity.class, ContainerHorseChest.class);
	}

	/** Handles {@link VillagerHandler#copyData(ContainerMerchant, EntityVillager, boolean)} */
	@Test
	public void testVillagerHandler() {
		ReflectionUtils.findField(ContainerMerchant.class, IMerchant.class);
		ReflectionUtils.findField(EntityVillager.class, MerchantRecipeList.class);
	}
}
