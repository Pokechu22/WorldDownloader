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
package wdl.handler.block;

import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityTrappedChest;

/**
 * This weird wrapper is to have a public class but the file with a different name.
 * JUnit refuses to use non-public test classes, but it's fine with this.
 *
 * Kinda awful, but IMO better than introducing a third file; this setup is already getting
 * rather enterprise-grade...
 */
class TrappedChestTest {
	public static class TestImpl extends BaseChestTest<TileEntityTrappedChest, TrappedChestHandler> {
		public TestImpl() {
			super(Blocks.TRAPPED_CHEST, Blocks.CHEST, TileEntityTrappedChest.class, TrappedChestHandler.class);
		}
	}
}
