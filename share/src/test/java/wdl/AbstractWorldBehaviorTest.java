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

import static org.mockito.Mockito.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Base logic shared between all tests that use blocks.
 *
 * Ensures that the {@link Bootstrap} is initialized, so that classes such as
 * {@link Blocks} can be used.
 */
public abstract class AbstractWorldBehaviorTest {
	private static final Logger LOGGER = LogManager.getLogger();

	@Before
	public void initBootstarp() {
		if (Bootstrap.isRegistered()) {
			LOGGER.debug("Bootstrap already initialized. ({})", this.getClass());
			return;
		}
		LOGGER.debug("Initializing bootstrap... ({})", this.getClass());
		Bootstrap.register();
		LOGGER.debug("Initialized bootstrap.");
		// Note: not checking Bootstrap.hasErrored as that didn't exist in older
		// versions
	}

	/**
	 * Creates a mock world, returning air for blocks and null for TEs.
	 *
	 * @return A mock world, with default behavior.
	 */
	protected IBlockAccess makeMockWorld() {
		IBlockAccess world = mock(IBlockAccess.class);

		when(world.getBlockState(any())).thenReturn(Blocks.AIR.getDefaultState());
		when(world.getTileEntity(any())).thenReturn(null);
		return world;
	}

	/**
	 * Puts the given block into the mock world at the given position.
	 *
	 * @param world A mock world
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @param z z-coordinate
	 * @param block The block to put
	 */
	protected void placeBlockAt(IBlockAccess world, int x, int y, int z, Block block) {
		placeBlockAt(world, x, y, z, block.getDefaultState());
	}

	/**
	 * Puts the given block into the mock world at the given position.
	 *
	 * @param world A mock world
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @param z z-coordinate
	 * @param block The block to put
	 */
	protected void placeBlockAt(IBlockAccess world, int x, int y, int z, IBlockState state) {
		when(world.getBlockState(new BlockPos(x, y, z))).thenReturn(state);
	}

	/**
	 * Puts the given block entity into the mock world at the given position.
	 *
	 * @param world A mock world
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @param z z-coordinate
	 * @param te The block entity to put
	 */
	protected void placeTEAt(IBlockAccess world, int x, int y, int z, TileEntity te) {
		when(world.getTileEntity(new BlockPos(x, y, z))).thenReturn(te);
	}
}
