/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017 Pokechu22, julialy
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
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * WDL configuration.  Right now, just a thin wrapper around {@link Properties}.
 */
public class Configuration {

	@Nullable
	private final Configuration parent;
	private final Properties properties;

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

	// Rework slightly, and maybe rename

	public void load(Reader reader) throws IOException {
		this.properties.load(reader);
	}

	public void store(Writer writer, String comments) throws IOException {
		this.properties.store(writer, comments);
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

	// Slightly smelly, but the purpose is needed; just the signature is bad

	public boolean containsKey(Object key) {
		return this.properties.contains(key);
	}

	public Object remove(String key) {
		return this.properties.remove(key);
	}

	// Things to definitely get rid of - smelly

	public Object put(Object key, Object value) {
		return this.properties.put(key, value);
	}

	public Set<String> stringPropertyNames() {
		return this.properties.stringPropertyNames();
	}

	public boolean isEmpty() {
		return this.properties.isEmpty();
	}

	public Set<Map.Entry<Object, Object>> entrySet() {
		return this.properties.entrySet();
	}

	public void putAll(Configuration conf) {
		this.properties.putAll(conf.properties);
	}
}
