
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
import static wdl.versioned.VersionedFunctions.*;

import org.junit.Test;

import net.minecraft.block.Blocks;
import net.minecraft.inventory.container.BeaconContainer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import wdl.ReflectionUtils;
import wdl.handler.HandlerException;

/**
 * Tests beacons.
 */
public class BeaconTest extends AbstractBlockHandlerTest<BeaconTileEntity, BeaconContainer, BeaconHandler> {

	public BeaconTest() {
		super(BeaconTileEntity.class, BeaconContainer.class, BeaconHandler.class);
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
		BeaconTileEntity te = makeBlockEntity(center);
		IIntArray fields = ReflectionUtils.findAndGetPrivateField(te, IIntArray.class);
		fields.set(1, Effect.getId(Effects.JUMP_BOOST));

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
		BeaconTileEntity te = makeBlockEntity(center);
		IIntArray fields = ReflectionUtils.findAndGetPrivateField(te, IIntArray.class);
		fields.set(1, Effect.getId(Effects.JUMP_BOOST));
		fields.set(2, Effect.getId(Effects.REGENERATION));

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
		assumeCustomNamesNotBroken();
		BlockPos center = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(center, Blocks.BEACON);
		BeaconTileEntity te = makeBlockEntity(center);
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
		assumeCustomNamesNotBroken();
		assumeMixinsApplied();

		BlockPos center = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(center, Blocks.BEACON);
		BeaconTileEntity te = makeBlockEntity(center);
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
	private static void assumeHasCustomName(BeaconTileEntity te) {
		assertTrue("Should have set a custom name on the server version for this to make sense",
				te.getDisplayName() != null); // This check is wrong I think

		CompoundNBT nbt = new CompoundNBT();
		te.write(nbt);

		assumeTrue("NBT needs to have a CustomName to test saving CustomName",
				nbt.contains("CustomName"));
	}
}
