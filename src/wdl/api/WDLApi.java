package wdl.api;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wdl.EntityRealigner;
import wdl.HologramHandler;
import wdl.MessageTypeCategory;
import wdl.WDL;
import wdl.WDLMessages;
import wdl.WDLPluginChannels;

import com.google.common.collect.ImmutableMap;

/**
 * Tool to allow other mods to interact with WDL.
 */
public class WDLApi {
	private static Logger logger = LogManager.getLogger();
	
	private static Map<String, ModInfo<?>> wdlMods = new HashMap<String, ModInfo<?>>();
	
	/**
	 * Saved a TileEntity to the given position.
	 * 
	 * @param pos The position to save at.
	 * @param te The TileEntity to save.
	 */
	public static void saveTileEntity(BlockPos pos, TileEntity te) {
		if (!WDLPluginChannels.canSaveTileEntities(pos.getX() << 16,
				pos.getZ() << 16)) {
			logger.warn("API attempted to call saveTileEntity when " +
					"saving TileEntities is not allowed!  Pos: " + pos +
					", te: " + te + ".  StackTrace: ");
			logStackTrace();
			
			return;
		}
		
		WDL.saveTileEntity(pos, te);
	}
	
	/**
	 * Adds a mod to the list of the listened mods.
	 */
	public static void addWDLMod(String id, String version, IWDLMod mod) {
		if (id == null) {
			throw new IllegalArgumentException("id must not be null!  (mod="
					+ mod + ", version=" + version + ")");
		}
		if (version == null) {
			throw new IllegalArgumentException("version must not be null!  "
					+ "(mod=" + mod + ", id=" + version + ")");
		}
		if (mod == null) {
			throw new IllegalArgumentException("mod must not be null!  " +
					"(id=" + id + ", version=" + version + ")");
		}

		ModInfo<IWDLMod> info = new ModInfo<IWDLMod>(id, version, mod);
		if (wdlMods.containsKey(id)) {
			throw new IllegalArgumentException("A mod by the name of '"
					+ id + "' is already registered by "
					+ wdlMods.get(id) + " (tried to register "
					+ info + " over it)");
		}
		if (!mod.isValidEnvironment(WDL.VERSION)) {
			String errorMessage = mod.getEnvironmentErrorMessage(WDL.VERSION);
			if (errorMessage != null) {
				throw new IllegalArgumentException(errorMessage);
			} else {
				throw new IllegalArgumentException("Environment for " + info
						+ " is incorrect!  Perhaps it is for a different"
						+ " version of WDL?  You are running " + WDL.VERSION + ".");
			}
		}
		
		wdlMods.put(id, info);
		
		// IMessageAdder doesn't seem possible to do dynamically
		if (mod instanceof IMessageTypeAdder) {
			Map<String, IWDLMessageType> types = 
					((IMessageTypeAdder) mod).getMessageTypes();
			
			ModMessageTypeCategory category = new ModMessageTypeCategory(info);
			
			for (Map.Entry<String, IWDLMessageType> e : types.entrySet()) {
				WDLMessages.registerMessage(e.getKey(), e.getValue(), category);
			}
		}
	}
	
	/**
	 * Gets a list of all enabled {@link IWDLMod}s that implement the given
	 * interface.
	 * 
	 * @param clazz
	 *            The class to check for implementation of.
	 * @return A list of all implementing extensions.
	 */
	public static <T extends IWDLMod> List<ModInfo<T>> getImplementingExtensions(
			Class<T> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz must not be null!");
		}
		List<ModInfo<T>> returned = new ArrayList<ModInfo<T>>();
		
		for (ModInfo<?> info : wdlMods.values()) {
			if (!info.isEnabled()) {
				continue;
			}
			
			if (clazz.isAssignableFrom(info.mod.getClass())) {
				// We know the actual type of the given mod is correct,
				// so it's safe to do this cast.
				@SuppressWarnings("unchecked")
				ModInfo<T> infoCasted = (ModInfo<T>)info;
				returned.add(infoCasted);
			}
		}
		
		return returned;
	}
	
	/**
	 * Gets a list of all {@link IWDLMod}s that implement the given
	 * interface, regardless as to whether they are enabled or not.
	 * 
	 * @param clazz
	 *            The class to check for implementation of.
	 * @return A list of all implementing extensions.
	 */
	public static <T extends IWDLMod> List<ModInfo<T>> getAllImplementingExtensions(
			Class<T> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz must not be null!");
		}
		List<ModInfo<T>> returned = new ArrayList<ModInfo<T>>();
		
		for (ModInfo<?> info : wdlMods.values()) {
			if (clazz.isAssignableFrom(info.mod.getClass())) {
				// We know the actual type of the given mod is correct,
				// so it's safe to do this cast.
				@SuppressWarnings("unchecked")
				ModInfo<T> infoCasted = (ModInfo<T>)info;
				returned.add(infoCasted);
			}
		}
		
		return returned;
	}
	
	/**
	 * Gets an immutable map of WDL mods.
	 */
	public static Map<String, ModInfo<?>> getWDLMods() {
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
		
		return wdlMods.get(name).getInfo();
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
	
	/**
	 * Implementation of {@link MessageTypeCategory} for {@link IWDLMod}s.
	 */
	private static class ModMessageTypeCategory extends MessageTypeCategory {
		private ModInfo<?> mod;
		
		public ModMessageTypeCategory(ModInfo<?> mod) {
			super(mod.id);
		}
		
		@Override
		public String getDisplayName() {
			return mod.getDisplayName();
		}
	}

	/**
	 * Information about a single extension.
	 */
	public static class ModInfo<T extends IWDLMod> {
		public final String id;
		public final String version;
		public final T mod;
		
		private ModInfo(String id, String version, T mod) {
			this.id = id;
			this.version = version;
			this.mod = mod;
		}
		
		@Override
		public String toString() {
			return id + "v" + version + " (" + mod.toString() + "/"
					+ mod.getClass().getName() + ")";
		}

		/**
		 * Gets the display name for this extension, using the ID if no display
		 * name is specified.
		 */
		public String getDisplayName() {
			if (mod instanceof IWDLModDescripted) {
				String name = ((IWDLModDescripted) mod).getDisplayName();
				if (name != null && !name.isEmpty()) {
					return name;
				}
			}
			return id;
		}
		
		/**
		 * Gets detailed information about this extension.
		 */
		public String getInfo() {
			StringBuilder info = new StringBuilder();
			
			info.append("Id: ").append(id).append('\n');
			info.append("Version: ").append(version).append('\n');
			if (mod instanceof IWDLModDescripted) {
				IWDLModDescripted dmod = (IWDLModDescripted)mod;
				
				String displayName = dmod.getDisplayName();
				String mainAuthor = dmod.getMainAuthor();
				String[] authors = dmod.getAuthors();
				String url = dmod.getURL();
				String description = dmod.getDescription();
				
				if (displayName != null && !displayName.isEmpty()) {
					info.append("Display name: ").append(displayName).append('\n');
				}
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
			info.append("Superclass: ")
					.append(mod.getClass().getSuperclass().getName()).append('\n');
			ClassLoader loader = mod.getClass().getClassLoader();
			info.append("Classloader: ").append(loader);
			if (loader != null) {
				info.append(" (").append(loader.getClass().getName()).append(')');
			}
			info.append('\n');
			Annotation[] annotations = mod.getClass().getAnnotations();
			info.append("Annotations (").append(annotations.length)
					.append(")\n");
			for (int i = 0; i < annotations.length; i++) {
				info.append(i).append(": ").append(annotations[i].toString())
						.append(" (")
						.append(annotations[i].annotationType().getName())
						.append(")\n");
			}
			
			return info.toString();
		}
		
		/**
		 * Checks whether this extension is enabled in the
		 * {@linkplain WDL#globalProps global config file}.
		 */
		public boolean isEnabled() {
			return WDL.globalProps.getProperty("Extensions." + id + ".enabled",
					"true").equals("true");
		}
		
		/**
		 * Sets whether or not this extension is enabled in the
		 * {@linkplain WDL#globalProps global config file}, and saves
		 * the global config file afterwards.
		 */
		public void setEnabled(boolean enabled) {
			WDL.globalProps.setProperty("Extensions." + id + ".enabled",
					Boolean.toString(enabled));
			WDL.saveGlobalProps();
		}
		
		/**
		 * Toggles whether or not this extension is enabled, and saves the
		 * global config file afterwards.
		 */
		public void toggleEnabled() {
			this.setEnabled(!this.isEnabled());
		}
	}

	static {
		logger.info("Loading default WDL extensions");
		addWDLMod("Hologram", "1.0", new HologramHandler());
		addWDLMod("EntityRealigner", "1.0", new EntityRealigner());
	}
}
