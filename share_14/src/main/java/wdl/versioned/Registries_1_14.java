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
package wdl.versioned;

import net.minecraft.block.Block;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

/**
 * Contains functions that interract with registries.
 *
 * This version is used in 1.13.1, where registries were all moved to
 * {@link IRegistry}. Note that this is <em>not</em> used in 1.13.0.
 */
final class RegistryFunctions {
	private RegistryFunctions() { throw new AssertionError(); }

	/* (non-javadoc)
	 * @see VersionedFunctions#getBlockId
	 */
	static final int getBlockId(Block block) {
		return Registry.BLOCK.getId(block);
	}
	/* (non-javadoc)
	 * @see VersionedFunctions#getBiomeId
	 */
	static final int getBiomeId(Biome biome) {
		return Registry.BIOME.getId(biome);
	}
}
