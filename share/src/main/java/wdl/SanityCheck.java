package wdl;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
