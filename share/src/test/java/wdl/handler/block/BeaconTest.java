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
package wdl.handler.block;

import org.junit.Test;

import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.math.BlockPos;
import wdl.handler.AbstractWorldBehaviorTest;
import wdl.handler.block.BlockHandler.HandlerException;

/**
 * Tests beacons.
 */
public class BeaconTest extends AbstractWorldBehaviorTest {

	@Override
	protected BlockHandler<?, ?> makeHandler() {
		return new BeaconHandler();
	}

	/**
	 * Checks a beacon with no defined effect.
	 */
	@Test
	public void testEffectlessBeacon() throws HandlerException {
		BlockPos center = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(center, Blocks.BEACON);
		placeTEAt(center, new TileEntityBeacon());

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
		TileEntityBeacon te = new TileEntityBeacon();
		te.setField(1, Potion.getIdFromPotion(MobEffects.JUMP_BOOST));
		placeTEAt(center, te);

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
		TileEntityBeacon te = new TileEntityBeacon();
		te.setField(1, Potion.getIdFromPotion(MobEffects.JUMP_BOOST));
		te.setField(2, Potion.getIdFromPotion(MobEffects.REGENERATION));
		placeTEAt(center, te);

		runHandler(center, makeClientContainer(center));
		checkAllTEs();
	}

	// Note that beacons do not have custom names: https://bugs.mojang.com/browse/MC-124395
}
