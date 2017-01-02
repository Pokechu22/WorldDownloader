package wdl.api;

import java.io.File;

import net.minecraft.world.storage.SaveHandler;

/**
 * Interface for {@link IWDLMod}s that want to save additional data with the
 * world.
 * 
 * A new progress bar step is created for each implementation.
 */
public interface ISaveListener extends IWDLMod {
	/**
	 * Called after all of the chunks in the world have been saved.
	 * 
	 * @param worldFolder
	 *            The base file for the world, as returned by
	 *            {@link SaveHandler#getWorldDirectory()};
	 */
	public abstract void afterChunksSaved(File worldFolder);
}
