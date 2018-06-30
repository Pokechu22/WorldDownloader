/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.settings;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.crash.CrashReportCategory;

/**
 * WDL configuration.  Right now, just a thin wrapper around {@link Properties}.
 */
public class Configuration {
	private static final Logger LOGGER = LogManager.getLogger();

	@Nullable
	final Configuration parent;
	private final Properties properties;

	public final BaseSetting<Boolean> allowCheats = makeSetting(
			"AllowCheats", "true", c -> c.allowCheats);
	public final BaseSetting<String> gameMode = makeSetting(
			"GameType", "keep", c->c.gameMode);
	public final BaseSetting<String> time = makeSetting(
			"Time", "keep", c->c.time);
	public final BaseSetting<String> weather = makeSetting(
			"Weather", "keep", c -> c.weather);
	public final BaseSetting<String> spawn = makeSetting(
			"Spawn", "auto", c -> c.spawn);

	public Configuration() {
		this(null);
	}

	public Configuration(@Nullable Configuration parent) {
		this.parent = parent;
		if (parent != null) {
			this.properties = new Properties(parent.properties);
		} else {
			this.properties = new Properties();
		}
	}

	/**
	 * If this implementation is the base default configuration, this method should
	 * be overridden to return true.
	 *
	 * This is written because overwriting a method can be done before a constructor
	 * runs, while a field cannot be used (ugly hack).
	 *
	 * @return true if this is the default implementation
	 */
	protected boolean assignDefaultValues() {
		return false;
	}

	/**
	 * Creates a setting, assigning the right value if needed.
	 *
	 * @param name         The name of the setting, as used in the configuration.
	 * @param defaultValue The default value that should be used for the field.
	 * @param fieldGetter  A reference to the field, from the configuration.
	 * @return The new setting.
	 */
	protected <T> BaseSetting<T> makeSetting(String name, String defaultValue, Function<Configuration, BaseSetting<T>> fieldGetter) {
		String value;
		if (this.assignDefaultValues()) {
			value = defaultValue;
		} else {
			value = null;
		}
		return new BaseSetting<>(name, value, this, fieldGetter);
	}

	// Rework slightly, and maybe rename

	public void load(Reader reader) throws IOException {
		this.properties.load(reader);
	}

	public void store(Writer writer, String comments) throws IOException {
		this.properties.store(writer, comments);
	}

	/**
	 * Gets a map of gamerules to values set in this configuration. This includes ones
	 * inherited from the parent.
	 */
	public Map<String, String> getGameRules() {
		return this.properties.stringPropertyNames().stream()
				.filter(s -> s.startsWith("GameRule."))
				.collect(Collectors.toMap(
						s -> s.substring("GameRule.".length()),
						s -> getProperty(s)));
	}

	/**
	 * Puts the contents of this configuration into the given crash report category.
	 */
	public void addToCrashReport(CrashReportCategory category, String name) {
		if (!properties.isEmpty()) {
			for (Map.Entry<Object, Object> e : properties.entrySet()) {
				if (!(e.getKey() instanceof String)) {
					LOGGER.warn("Non-string key " + e.getKey() + " in " + name);
					continue;
				}
				category.addCrashSection((String)e.getKey(), e.getValue());
			}
		} else {
			category.addCrashSection("-", "empty");
		}
	}

	// Keep but rename

	public void setProperty(String key, String value) {
		this.properties.setProperty(key, value);
	}

	@Nullable
	public String getProperty(String key) {
		return this.properties.getProperty(key);
	}

	@Nonnull
	public String getProperty(String key, @Nonnull String defaultValue) {
		return this.properties.getProperty(key, defaultValue);
	}

	public boolean containsKey(String key) {
		return this.properties.containsKey(key);
	}

	public void remove(String key) {
		this.properties.remove(key);
	}

	// Things to definitely get rid of - smelly

	public void putAll(Configuration conf) {
		this.properties.putAll(conf.properties);
	}
}
