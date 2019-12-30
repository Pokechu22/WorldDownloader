
/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.handler.block;

import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static wdl.versioned.VersionedFunctions.customName;

import org.junit.Test;

import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.math.BlockPos;
import wdl.handler.HandlerException;

/**
 * Tests beacons.
 */
public class BeaconTest extends AbstractBlockHandlerTest<TileEntityBeacon, ContainerBeacon, BeaconHandler> {

	public BeaconTest() {
		super(TileEntityBeacon.class, ContainerBeacon.class, BeaconHandler.class);
	}

	/**
	 * Checks a beacon with no defined effect.
	 */
	@Test
	public void testEffectlessBeacon() throws HandlerException {
		BlockPos center = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(center, Blocks.BEACON);
		makeBlockEntity(center);

		runHandler(center, makeClientContainer(center));
		checkAllTEs();
	}

	/**
	 * Checks a beacon with an effect.
	 */
	@Test
	public void testOneEffectBeacon() throws HandlerException {
		BlockPos center = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(center, Blocks.BEACON);
		TileEntityBeacon te = makeBlockEntity(center);
		te.setField(1, Potion.getId(MobEffects.JUMP_BOOST));

		runHandler(center, makeClientContainer(center));
		checkAllTEs();
	}

	/**
	 * Checks a beacon with an effect.
	 */
	@Test
	public void testTwoEffectBeacon() throws HandlerException {
		BlockPos center = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(center, Blocks.BEACON);
		TileEntityBeacon te = makeBlockEntity(center);
		te.setField(1, Potion.getId(MobEffects.JUMP_BOOST));
		te.setField(2, Potion.getId(MobEffects.REGENERATION));

		runHandler(center, makeClientContainer(center));
		checkAllTEs();
	}

	/**
	 * Checks a beacon with a custom name.
	 *
	 * Skipped in 1.13 and earlier.
	 */
	@Test
	public void testCustomName() throws HandlerException {
		BlockPos center = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(center, Blocks.BEACON);
		TileEntityBeacon te = makeBlockEntity(center);
		te.setCustomName(customName("Delicious pig meat"));

		assumeHasCustomName(te);

		runHandler(center, makeClientContainer(center));
		checkAllTEs();
	}

	/**
	 * Checks a beacon with a custom name that matches its actual name.
	 *
	 * Skipped in 1.13 and earlier.
	 */
	@Test
	public void testCustomNameVanilla() throws HandlerException {
		assumeMixinsApplied();

		BlockPos center = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(center, Blocks.BEACON);
		TileEntityBeacon te = makeBlockEntity(center);
		te.setCustomName(customName("Beacon"));

		assumeHasCustomName(te);

		runHandler(center, makeClientContainer(center));
		checkAllTEs();
	}

	/**
	 * If beacons don't have a custom name tag in this version, skip the test.
	 *
	 * Note that beacons do not have custom names currently:
	 * <a href="https://bugs.mojang.com/browse/MC-124395">MC-124395</a>. However,
	 * this is fixed in 1.14.
	 *
	 * @param te The server-sided TE
	 */
	private static void assumeHasCustomName(TileEntityBeacon te) {
		assertTrue("Should have set a custom name on the server version for this to make sense",
				te.hasCustomName());

		NBTTagCompound nbt = new NBTTagCompound();
		te.write(nbt);

		assumeTrue("NBT needs to have a CustomName to test saving CustomName",
				nbt.contains("CustomName"));
	}
}
