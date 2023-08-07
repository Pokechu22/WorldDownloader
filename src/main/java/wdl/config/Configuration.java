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
package wdl.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnegative;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.crash.CrashReportCategory;

/**
 * An implementation of {@link IConfiguration} that is backed by a
 * {@link Properties} object.
 */
public class Configuration implements IConfiguration {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IConfiguration parent;
	private final Properties properties;

	public Configuration(IConfiguration parent) {
		this.parent = parent;
		this.properties = new Properties();
	}

	@Override
	public <T> void setValue(Setting<T> setting, T value) {
		this.properties.setProperty(setting.getConfigurationKey(), setting.serializeToString(value));
	}

	@Override
	public <T> T getValue(Setting<T> setting, IConfiguration config) {
		Optional<T> override = setting.overrideFromContext(config);
		if (override.isPresent()) {
			return override.get();
		}
		String key = setting.getConfigurationKey();
		if (this.properties.containsKey(key)) {
			String value = this.properties.getProperty(key);
			try {
				T t = setting.deserializeFromString(value);
				if (t == null) {
					throw new NullPointerException("deserializeFromString returned null");
				}
				return t;
			} catch (Exception ex) {
				LOGGER.warn("Failed to deserialize {} from configuration {} with '{}'='{}'", setting, this, key, value, ex);
				LOGGER.warn("Clearing the value and using parent config now...");
				this.clearValue(setting);
			}
		}
		return parent.getValue(setting, config);
	}

	@Override
	public <T> void clearValue(Setting<T> setting) {
		this.properties.remove(setting.getConfigurationKey());
	}

	@Override
	public void load(File file) throws IOException {
		try (FileReader reader = new FileReader(file)) {
			this.properties.load(reader);
		}
	}

	@Override
	public void store(File file, String comments) throws IOException {
		try (FileWriter writer = new FileWriter(file)) {
			this.properties.store(writer, comments);
		}
	}

	/**
	 * Puts the contents of this configuration into the given crash report category.
	 */
	@Override
	public void addToCrashReport(CrashReportCategory category, String name) {
		if (!properties.isEmpty()) {
			for (Map.Entry<Object, Object> e : properties.entrySet()) {
				if (!(e.getKey() instanceof String)) {
					LOGGER.warn("Non-string key " + e.getKey() + " in " + name);
					continue;
				}
				category.addDetail((String)e.getKey(), e.getValue());
			}
		} else {
			category.addDetail("-", "empty");
		}
	}

	// Wrap-around things -- should be removed later, but needed to be ready for release
	private static final String ENTITY_TRACK_PREFIX = "Entity.", ENTITY_TRACK_SUFFIX = ".TrackDistance";

	@Override
	@CheckForSigned
	public int getUserEntityTrackDistance(String entityType) {
		String key = ENTITY_TRACK_PREFIX + entityType + ENTITY_TRACK_SUFFIX;
		if (this.properties.containsKey(key)) {
			return Integer.parseInt(this.properties.getProperty(key));
		} else {
			return parent.getUserEntityTrackDistance(entityType);
		}
	}

	@Override
	public void setUserEntityTrackDistance(String entityType, @Nonnegative int value) {
		assert value >= 0;
		String key = ENTITY_TRACK_PREFIX + entityType + ENTITY_TRACK_SUFFIX;
		this.properties.setProperty(key, Integer.toString(value));
	}

	private static final String ENTITY_TYPE_PREFIX = "Entity.", ENTITY_TYPE_SUFFIX = ".Enabled";

	@Override
	public boolean isEntityTypeEnabled(String entityType) {
		String key = ENTITY_TYPE_PREFIX + entityType + ENTITY_TYPE_SUFFIX;
		if (this.properties.containsKey(key)) {
			return Boolean.parseBoolean(this.properties.getProperty(key));
		} else {
			return parent.isEntityTypeEnabled(entityType);
		}
	}

	@Override
	public void setEntityTypeEnabled(String entityType, boolean value) {
		String key = ENTITY_TYPE_PREFIX + entityType + ENTITY_TYPE_SUFFIX;
		this.properties.setProperty(key, Boolean.toString(value));
	}

	private static final String ENTITY_GROUP_PREFIX = "EntityGroup.", ENTITY_GROUP_SUFFIX = ".Enabled";
	@Override
	public boolean isEntityGroupEnabled(String entityGroup) {
		String key = ENTITY_GROUP_PREFIX + entityGroup + ENTITY_GROUP_SUFFIX;
		if (this.properties.containsKey(key)) {
			return Boolean.parseBoolean(this.properties.getProperty(key));
		} else {
			return parent.isEntityGroupEnabled(entityGroup);
		}
	}

	@Override
	public void setEntityGroupEnabled(String entityGroup, boolean value) {
		String key = ENTITY_GROUP_PREFIX + entityGroup + ENTITY_GROUP_SUFFIX;
		this.properties.setProperty(key, Boolean.toString(value));
	}

	// Things to definitely get rid of - smelly

	@Deprecated
	public void putAll(Configuration conf) {
		this.properties.putAll(conf.properties);
	}
}
