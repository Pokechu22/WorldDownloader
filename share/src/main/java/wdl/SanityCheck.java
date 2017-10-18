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

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Sanity checks, run at compile and run time.
 */
enum SanityCheck {
	TRIPWIRE("wdl.sanity.tripwire") {
		/**
		 * Tripwire sometimes has the wrong state due to
		 * https://github.com/MinecraftForge/MinecraftForge/issues/3924
		 */
		@Override
		public void run() throws Exception {
			int wireID = Block.getIdFromBlock(Blocks.TRIPWIRE);
			for (int meta = 0; meta <= 15; meta++) {
				int id = wireID << 4 | meta;
				// Note: Deprecated but supported under forge, and this is
				// what the game actually uses, so we should too for checking
				IBlockState state = Block.BLOCK_STATE_IDS.getByValue(id);
				Block block = (state != null ? state.getBlock() : null);
				LOGGER.trace("id {} ({}) => {} ({})", id, meta, state, block);

				// meta 15 is unused for some reason, ignore it
				if (meta == 15) {
					continue;
				}
				if (state == null) {
					throw new Exception("Unexpected null state for meta " + meta + " (" + id + ")");
				}
				if (block != Blocks.TRIPWIRE) {
					throw new Exception("Unexpected block for meta " + meta + " (" + id + "): " + state);
				}
			}
		}
	},
	VERSION("wdl.sanity.version") {
		@Override
		public void run() throws Exception {
			String expected = VersionConstants.getExpectedVersion();
			String actual = VersionConstants.getMinecraftVersion();
			if (expected == null) {
				throw new Exception("Unexpected null expected version!");
			}
			if (actual == null) {
				throw new Exception("Unexpected null running version!");
			}
			if (!expected.equals(actual)) {
				throw new Exception("Unexpected version mismatch - expected to be running on `" + expected + "' but was running on `" + actual + "'!");
			}
		}
	},
	TRANSLATION("wdl.sanity.translation") {
		@Override
		public void run() throws Exception {
			if (!I18n.hasKey(this.errorMessage)) {
				// Verbose, because obviously the normal string will not be translated.
				throw new Exception("Translation strings are not present!  All messages will be the untranslated keys (e.g. `wdl.sanity.translation').  Please redownload the mod.  If this problem persists, file a bug report.");
			}
		}
	}
	;
	/** Translation key for the general message */
	public final String errorMessage;
	private SanityCheck(String message) {
		this.errorMessage = message;
	}
	private static final Logger LOGGER = LogManager.getLogger();
	/**
	 * Performs this sanity check.
	 * Methods are encouraged to log trace information.
	 *
	 * @throws Exception on failure
	 */
	public abstract void run() throws Exception;
}
