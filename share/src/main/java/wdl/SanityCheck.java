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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryBasic;
import wdl.ducks.IBaseChangesApplied;
import wdl.ducks.INetworkNameable;
import wdl.versioned.VersionedFunctions;

/**
 * Sanity checks, run at compile and run time.
 */
enum SanityCheck {
	// Separately list all of these so that we know exactly which ones failed (otherwise an exception would only indicate the first failure)
	MIXIN_INVENTORYBASIC("wdl.sanity.mixin") {
		@Override
		public void run() throws Exception {
			if (!INetworkNameable.class.isAssignableFrom(InventoryBasic.class)) {
				throw new Exception("InventoryBasic does not implement INetworkNameable!");
			}
		}
	},
	MIXIN_GUIINGAMEMENU("wdl.sanity.mixin") {
		@Override
		public void run() throws Exception {
			if (!IBaseChangesApplied.class.isAssignableFrom(GuiIngameMenu.class)) {
				// This one almost certainly can't happen at runtime, since the button isn't reachable
				throw new Exception("GuiIngameMenu is missing base changes!");
			}
		}
	},
	MIXIN_WORLDCLIENT("wdl.sanity.mixin") {
		@Override
		public void run() throws Exception {
			if (!IBaseChangesApplied.class.isAssignableFrom(WorldClient.class)) {
				throw new Exception("WorldClient is missing base changes!");
			}
		}
	},
	MIXIN_NHPC("wdl.sanity.mixin") {
		@Override
		public void run() throws Exception {
			if (!IBaseChangesApplied.class.isAssignableFrom(NetHandlerPlayClient.class)) {
				throw new Exception("NetHandlerPlayClient is missing base changes!");
			}
		}
	},
	MIXIN_CRASHREPORT("wdl.sanity.mixin") {
		@Override
		public void run() throws Exception {
			if (!IBaseChangesApplied.class.isAssignableFrom(CrashReport.class)) {
				throw new Exception("CrashReport is missing base changes!");
			}
		}
	},
	// n.b. ClientBrandRetriever and DefaultResourcePack changes aren't needed/present on liteloader, so don't check them
	ENCODING("wdl.sanity.encoding") {
		@Override
		public void run() throws Exception {
			compare("§aSection-sign text§r", "\u00a7aSection-sign text\u00a7r");
			compare("༼ つ ◕_◕ ༽つ  Give UNICODE", "\u0F3C \u3064 \u25D5_\u25D5 \u0F3D\u3064  Give UNICODE");
			compare("ＴＥＳＴ", "\uFF34\uFF25\uFF33\uFF34");
		}
		private void compare(String actual, String expected) throws Exception {
			if (!actual.equals(expected)) {
				throw new Exception("Mismatched strings -- expected " + expected + " but got " + actual);
			}
		}
	},
	TRIPWIRE("wdl.sanity.tripwire") {
		@Override
		public boolean canRun() {
			return VersionConstants.getDataVersion() < 1451; // < 17w47a (flattening)
		}
		/**
		 * Tripwire sometimes has the wrong state due to
		 * https://github.com/MinecraftForge/MinecraftForge/issues/3924
		 */
		@Override
		public void run() throws Exception {
			int wireID = VersionedFunctions.getBlockId(Blocks.TRIPWIRE);
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
	},
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

	/**
	 * Returns true if this sanity check can even run in this context.
	 * @return True if this sanity check makes sense to run.
	 */
	public boolean canRun() {
		return true;
	}
}
