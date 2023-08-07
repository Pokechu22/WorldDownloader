/*
 * This file is part of the World Downloader API.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2017-2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * You are free to include the World Downloader API within your own mods, as
 * permitted via the MMPLv2.
 */
package wdl.api;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Tool to allow other mods to interact with WDL.
 */
public class WDLApi {
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * The actual instance.  Null until registration.
	 */
	@Nullable
	private static transient APIInstance INSTANCE;

	/**
	 * Saved a TileEntity to the given position.
	 *
	 * @param pos The position to save at.
	 * @param te The TileEntity to save.
	 */
	public static void saveTileEntity(BlockPos pos, TileEntity te) {
		checkState();
		INSTANCE.saveTileEntity(pos, te);
	}

	/**
	 * Adds a mod to the list of the listened mods.
	 */
	public static void addWDLMod(String id, String version, IWDLMod mod) {
		checkState();
		INSTANCE.addWDLMod(id, version, mod);
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
		checkState();
		return INSTANCE.getImplementingExtensions(clazz);
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
		checkState();
		return INSTANCE.getAllImplementingExtensions(clazz);
	}

	/**
	 * Gets an immutable map of WDL mods.
	 */
	public static Map<String, ModInfo<?>> getWDLMods() {
		checkState();
		return INSTANCE.getWDLMods();
	}

	/**
	 * Gets detailed information on the given mod.
	 * @param name Name of the mod.
	 * @return The details.
	 */
	public static String getModInfo(String name) {
		checkState();
		return INSTANCE.getModInfo(name);
	}

	/**
	 * Sets the instance.  Intended for internal use only.
	 *
	 * Does not check if there is an existing instance.
	 */
	public static void setInstance(APIInstance instance) {
		LOGGER.debug("Changing api instance from {} to {}", INSTANCE, instance);
		INSTANCE = instance;
	}

	/**
	 * Verifies that the instance has been set up.
	 */
	private static void checkState() throws IllegalStateException {
		if (INSTANCE == null) {
			throw new IllegalStateException("API called before it has been initialized!");
		}
	}

	/**
	 * Information about a single extension.
	 */
	public static class ModInfo<T extends IWDLMod> {
		public final String id;
		public final String version;
		public final T mod;

		protected ModInfo(String id, String version, T mod) {
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
				//https://stackoverflow.com/q/320542/3991344
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
		 * Checks whether this extension is enabled in the global config file.
		 */
		public boolean isEnabled() {
			checkState();
			return INSTANCE.isEnabled(this.id);
		}

		/**
		 * Sets whether or not this extension is enabled in the global config
		 * file, and saves the global config file afterwards.
		 */
		public void setEnabled(boolean enabled) {
			checkState();
			INSTANCE.setEnabled(this.id, enabled);
		}

		/**
		 * Toggles whether or not this extension is enabled, and saves the
		 * global config file afterwards.
		 */
		public void toggleEnabled() {
			this.setEnabled(!this.isEnabled());
		}
	}

	/**
	 * Delegates API logic.
	 */
	public static interface APIInstance {
		/** @see {@link WDLApi#saveTileEntity(BlockPos, TileEntity)} */
		abstract void saveTileEntity(BlockPos pos, TileEntity te);
		/** @see {@link WDLApi#addWDLMod(String, String, IWDLMod)} */
		abstract void addWDLMod(String id, String version, IWDLMod mod);
		/** @see {@link WDLApi#getImplementingExtensions(Class)} */
		abstract <T extends IWDLMod> List<ModInfo<T>> getImplementingExtensions(Class<T> clazz);
		/** @see {@link WDLApi#getAllImplementingExtensions(Class)} */
		abstract <T extends IWDLMod> List<ModInfo<T>> getAllImplementingExtensions(Class<T> clazz);
		/** @see {@link WDLApi#getWDLMods()} */
		abstract Map<String, ModInfo<?>> getWDLMods();
		/** @see {@link WDLApi#getModInfo(String)} */
		abstract String getModInfo(String name);

		/** @see {@link ModInfo#isEnabled()} */
		abstract boolean isEnabled(String modID);
		/** @see {@link ModInfo#setEnabled(boolean)} */
		abstract void setEnabled(String modID, boolean enabled);
	}
}
