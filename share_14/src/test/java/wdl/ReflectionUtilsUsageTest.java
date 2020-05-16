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

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.junit.Test;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.AbstractFurnaceContainer;
import net.minecraft.inventory.container.BrewingStandContainer;
import net.minecraft.inventory.container.DispenserContainer;
import net.minecraft.inventory.container.HopperContainer;
import net.minecraft.inventory.container.HorseInventoryContainer;
import wdl.gui.GuiWDLSaveProgress;
import wdl.handler.block.BrewingStandHandler;
import wdl.handler.block.DispenserHandler;
import wdl.handler.block.DropperHandler;
import wdl.handler.block.FurnaceHandler;
import wdl.handler.block.HopperHandler;
import wdl.handler.entity.HorseHandler;

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
		ReflectionUtils.findField(ClientPlayNetHandler.class, Screen.class);
	}

	/** Handles {@link WDL#saveChunks(GuiWDLSaveProgress)} */
	@Test
	public void testWDLSaveChunks() {
		Class<?>[] chunkArrayClasses = ClientChunkProvider.class.getDeclaredClasses();
		assertThat(chunkArrayClasses, is(arrayWithSize(1)));
		Class<?> chunkArrayClass = chunkArrayClasses[0];
		ReflectionUtils.findField(ClientChunkProvider.class, chunkArrayClass);
		ReflectionUtils.findField(chunkArrayClass, AtomicReferenceArray.class);
	}

	/** Handles {@link BrewingStandHandler#handle} */
	@Test
	public void testBrewingStandHandler() {
		ReflectionUtils.findField(BrewingStandContainer.class, IInventory.class);
	}

	/** Handles {@link DispenserHandler#handle} and {@link DropperHandler#handle} */
	@Test
	public void testDispenserHandler() {
		ReflectionUtils.findField(DispenserContainer.class, IInventory.class);
	}

	/** Handles {@link FurnaceHandler#handle} */
	@Test
	public void testFuranceHandler() {
		ReflectionUtils.findField(AbstractFurnaceContainer.class, IInventory.class);
	}

	/** Handles {@link HopperHandler#handle} */
	@Test
	public void testHopperHandler() {
		ReflectionUtils.findField(HopperContainer.class, IInventory.class);
	}

	// Skipping ShulkerBoxHandler due to version properties; testing is mostly redundant anyways
	// since the handlers have their own dedicated tests

	/** Handles {@link HorseHandler#copyData} and {@link HorseHandler#checkRiding} */
	@Test
	public void testHorseHandler() {
		ReflectionUtils.findField(HorseInventoryContainer.class, AbstractHorseEntity.class);
		ReflectionUtils.findField(AbstractHorseEntity.class, Inventory.class);
	}
}
