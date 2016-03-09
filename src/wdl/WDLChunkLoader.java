package wdl;

import java.io.File;
import java.lang.reflect.Method;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.storage.SaveHandler;

/**
 * Alternative implementation of {@link AnvilChunkLoader} that handles editing
 * WDL-specific properties of chunks as they are being saved.
 */
public class WDLChunkLoader extends AnvilChunkLoader {

	public static WDLChunkLoader create(SaveHandler handler,
			WorldProvider provider) {
		return new WDLChunkLoader(getWorldSaveFolder(handler, provider));
	}
	
	/**
	 * Gets the save folder for the given WorldProvider, respecting Forge's
	 * dimension names if forge is present.
	 */
	private static File getWorldSaveFolder(SaveHandler handler,
			WorldProvider provider) {
		File baseFolder = handler.getWorldDirectory();
		
		// Based off of AnvilSaveHandler.getChunkLoader, but also accounts
		// for forge changes.
		try {
			// See forge changes here:
			// https://github.com/MinecraftForge/MinecraftForge/blob/250a77b35936e7ac68006dfd28a9e93c6def9128/patches/minecraft/net/minecraft/world/WorldProvider.java.patch#L85-L93
			// https://github.com/MinecraftForge/MinecraftForge/blob/250a77b35936e7ac68006dfd28a9e93c6def9128/patches/minecraft/net/minecraft/world/chunk/storage/AnvilSaveHandler.java.patch
			Method forgeGetSaveFolderMethod = provider.getClass().getMethod("getSaveFolder");
			
			String name = (String) forgeGetSaveFolderMethod.invoke(provider);
			if (name != null) {
				File file = new File(baseFolder, name);
				file.mkdirs();
				return file;
			}
			return baseFolder;
		} catch (Exception e) {
			// Not a forge setup - emulate the vanilla method in 
			// AnvilSaveHandler.getChunkLoader.
			
			if (provider instanceof WorldProviderHell) {
				File file = new File(baseFolder, "DIM-1");
				file.mkdirs();
				return file;
			} else if (provider instanceof WorldProviderEnd) {
				File file = new File(baseFolder, "DIM1");
				file.mkdirs();
				return file;
			}
			
			return baseFolder;
		}
	}
	
	public WDLChunkLoader(File file) {
		super(file);
	}

}
