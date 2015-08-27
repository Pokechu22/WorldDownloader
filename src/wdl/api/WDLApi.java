package wdl.api;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;

import wdl.EntityUtils;
import wdl.WDL;
import wdl.WDLEvents;
import wdl.WDLHooks;
import wdl.WDLMessages;
import wdl.WDLPluginChannels;

/**
 * Tool to allow other mods to interact with WDL.
 */
public class WDLApi {
	private static Logger logger = LogManager.getLogger();
	
	private static Map<String, IWDLMod> wdlMods = new HashMap<String, IWDLMod>();
	
	/**
	 * Saved a TileEntity to the given position.
	 * 
	 * @param pos The position to save at.
	 * @param te The TileEntity to save.
	 */
	public static void saveTileEntity(BlockPos pos, TileEntity te) {
		if (!WDLPluginChannels.canSaveTileEntities()) {
			logger.warn("API attempted to call saveTileEntity when " +
					"saving TileEntities is not allowed!  Pos: " + pos +
					", te: " + te + ".  StackTrace: ");
			logStackTrace();
			
			return;
		}
		
		WDL.newTileEntities.put(pos, te);
	}
	
	/**
	 * Adds a mod to the list of the listened mods.
	 */
	public static void addWDLMod(IWDLMod mod) {
		if (mod == null) {
			throw new IllegalArgumentException("mod must not be null!");
		}
		
		String modName = mod.getName();
		if (wdlMods.containsKey(modName)) {
			throw new IllegalArgumentException("A mod by the name of '"
					+ modName + "' is already registered by "
					+ wdlMods.get(modName) + " (tried to register "
					+ mod + " over it)");
		}
		
		wdlMods.put(modName, mod);
		if (mod instanceof IGuiHooksListener) {
			WDLHooks.guiListeners.put(modName, (IGuiHooksListener) mod);
		}
		if (mod instanceof IBlockEventListener) {
			WDLHooks.blockEventListeners.put(modName, (IBlockEventListener) mod);
		}
		if (mod instanceof IChatMessageListener) {
			WDLHooks.chatMessageListeners.put(modName, (IChatMessageListener) mod);
		}
		if (mod instanceof IPluginChannelListener) {
			WDLHooks.pluginChannelListeners.put(modName, (IPluginChannelListener) mod);
		}
		if (mod instanceof IWorldLoadListener) {
			WDLEvents.worldLoadListeners.put(modName, (IWorldLoadListener) mod);
		}
		if (mod instanceof IEntityAdder) {
			EntityUtils.addEntityAdder((IEntityAdder) mod);
		}
		if (mod instanceof ISpecialEntityHandler) {
			EntityUtils.addSpecialEntityHandler((ISpecialEntityHandler) mod);
		}
		if (mod instanceof IMessageTypeAdder) {
			Map<String, IWDLMessageType> types = 
					((IMessageTypeAdder) mod).getMessageTypes();
			
			for (Map.Entry<String, IWDLMessageType> e : types.entrySet()) {
				WDLMessages.registerMessage(e.getKey(), e.getValue(), modName);
			}
		}
	}
	
	/**
	 * Gets an immutable map of WDL mods.
	 */
	public static Map<String, IWDLMod> getWDLMods() {
		return ImmutableMap.copyOf(wdlMods);
	}
	
	/**
	 * Gets detailed information on the given mod.
	 * @param name Name of the mod.
	 * @return The details.
	 */
	public static String getModInfo(String name) {
		if (!wdlMods.containsKey(name)) {
			return null;
		}
		
		return getModInfo(wdlMods.get(name));
	}
	
	/**
	 * Gets detailed information on the given mod.
	 * @param mod The mod to get info of.
	 * @return The details.
	 */
	public static String getModInfo(IWDLMod mod) {
		if (mod == null) {
			return null;
		}
		
		StringBuilder info = new StringBuilder();
		
		info.append("Name: ").append(mod.getName()).append('\n');
		info.append("Version: ").append(mod.getVersion()).append('\n');
		if (mod instanceof IWDLModDescripted) {
			IWDLModDescripted dmod = (IWDLModDescripted)mod;
			
			String mainAuthor = dmod.getMainAuthor();
			String[] authors = dmod.getAuthors();
			String url = dmod.getURL();
			String description = dmod.getDescription();
			
			if (mainAuthor != null && !mainAuthor.isEmpty()) {
				info.append("Main author: ").append(mainAuthor).append('\n');
			}
			if (authors != null && authors.length > 0) {
				info.append("Authors: ");
				
				for (int i = 0; i < authors.length; i++) {
					if (authors[i].equals(mainAuthor)) {
						continue;
					}
					
					if (i <= authors.length - 2) {
						info.append(", ");
					} else if (i == authors.length - 1) {
						info.append(", and ");
					} else {
						info.append('\n');
					}
				}
			}
			
			if (url != null && !url.isEmpty()) {
				info.append("URL: ").append(url).append('\n');
			}
			if (description != null && !description.isEmpty()) {
				info.append("Description: \n").append(description).append('\n');
			}
		}
		
		info.append("Main class: ").append(mod.getClass().getName()).append('\n');
		info.append("Containing file: ");
		try {
			//http://stackoverflow.com/q/320542/3991344
			String path = new File(mod.getClass().getProtectionDomain()
					.getCodeSource().getLocation().toURI()).getPath();
			
			//Censor username.
			String username = System.getProperty("user.name");
			path = path.replace(username, "<USERNAME>");
			
			info.append(path);
		} catch (Exception e) {
			info.append("Unknown (").append(e.toString()).append(')');
		}
		info.append('\n');
		Class<?>[] interfaces = mod.getClass().getInterfaces();
		info.append("Implemented interfaces (").append(interfaces.length)
				.append(")\n");
		for (int i = 0; i < interfaces.length; i++) {
			info.append(i).append(": ").append(interfaces[i].getName())
					.append('\n');
		}
		
		return info.toString();
	}
	
	/**
	 * Writes out the current stacktrace to the logger in warn mode.
	 */
	private static void logStackTrace() {
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		for (StackTraceElement e : elements) {
			logger.warn(e.toString());
		}
	}
}
