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
package wdl.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import wdl.EntityRealigner;
import wdl.HologramHandler;
import wdl.MessageTypeCategory;
import wdl.VersionConstants;
import wdl.WDL;
import wdl.WDLMessages;
import wdl.WDLPluginChannels;
import wdl.api.WDLApi.ModInfo;
import wdl.config.Setting;
import wdl.config.settings.MiscSettings.ExtensionEnabledSetting;

/**
 * {@link WDLApi.APIInstance} implementation.
 */
public class APIImpl implements WDLApi.APIInstance {
	private static final Logger LOGGER = LogManager.getLogger();

	private static Map<String, ModInfoImpl<?>> wdlMods = new HashMap<>();

	private APIImpl() { }  // Internal use only

	@Override
	public void saveTileEntity(BlockPos pos, TileEntity te) {
		if (!WDLPluginChannels.canSaveTileEntities(pos.getX() >> 4,
				pos.getZ() >> 4)) {
			LOGGER.warn("API attempted to call saveTileEntity when " +
					"saving TileEntities is not allowed!  Pos: " + pos +
					", te: " + te + ".  StackTrace: ");
			logStackTrace();

			return;
		}

		WDL.getInstance().saveTileEntity(pos, te);
	}

	@Override
	public void addWDLMod(String id, String version, IWDLMod mod) {
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

		ModInfoImpl<IWDLMod> info = new ModInfoImpl<>(id, version, mod);
		if (wdlMods.containsKey(id)) {
			throw new IllegalArgumentException("A mod by the name of '"
					+ id + "' is already registered by "
					+ wdlMods.get(id) + " (tried to register "
					+ info + " over it)");
		}
		if (!mod.isValidEnvironment(VersionConstants.getModVersion())) {
			String errorMessage = mod
					.getEnvironmentErrorMessage(VersionConstants
							.getModVersion());
			if (errorMessage != null) {
				throw new IllegalArgumentException(errorMessage);
			} else {
				throw new IllegalArgumentException("Environment for " + info
						+ " is incorrect!  Perhaps it is for a different"
						+ " version of WDL?  You are running "
						+ VersionConstants.getModVersion() + ".");
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

	@Override
	public <T extends IWDLMod> List<ModInfo<T>> getImplementingExtensions(
			Class<T> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz must not be null!");
		}
		List<ModInfo<T>> returned = new ArrayList<>();

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

	@Override
	public <T extends IWDLMod> List<ModInfo<T>> getAllImplementingExtensions(
			Class<T> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz must not be null!");
		}
		List<ModInfo<T>> returned = new ArrayList<>();

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

	@Override
	public Map<String, ModInfo<?>> getWDLMods() {
		return ImmutableMap.copyOf(wdlMods);
	}

	@Override
	public String getModInfo(String name) {
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
			LOGGER.warn(e.toString());
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
		public ITextComponent getDisplayName() {
			return new StringTextComponent(mod.getDisplayName());
		}

		@Override
		public ITextComponent getDescription() {
			return new TranslationTextComponent("Messages for " + mod.getDisplayName()); // XXX Not translated
		}
	}

	@Override
	public boolean isEnabled(String modID) {
		if (wdlMods.containsKey(modID)) {
			return WDL.globalProps.getValue(wdlMods.get(modID).enabledSetting);
		} else {
			return false;
		}
	}

	@Override
	public void setEnabled(String modID, boolean enabled) {
		if (wdlMods.containsKey(modID)) {
			WDL.globalProps.setValue(wdlMods.get(modID).enabledSetting, enabled);
			WDL.saveGlobalProps();
		}
	}

	private static class ModInfoImpl<T extends IWDLMod> extends ModInfo<T> {
		public final Setting<Boolean> enabledSetting;

		ModInfoImpl(String id, String version, T mod) {
			super(id, version, mod);
			this.enabledSetting = new ExtensionEnabledSetting(id);
		}
	}

	public static void ensureInitialized() {
		LOGGER.debug("APIImpl.ensureInitialized()");
	}

	static {
		LOGGER.debug("Setting instance");
		WDLApi.APIInstance instance = new APIImpl();
		WDLApi.setInstance(instance);
		LOGGER.debug("Loading default WDL extensions");
		// Don't do this statically to avoid problems
		instance.addWDLMod("Hologram", "2.0", new HologramHandler());
		instance.addWDLMod("EntityRealigner", "1.0", new EntityRealigner());
	}
}
