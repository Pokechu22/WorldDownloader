/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018-2020 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;

import java.util.Collections;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;

import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.resources.ResourceIndex;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.init.Bootstrap;

/**
 * This class initializes Minecraft's bootstrap and language files.
 */
final class TestBootstrap {
	private TestBootstrap() { throw new AssertionError(); }

	private static final Logger LOGGER = LogManager.getLogger();
	private static boolean ran = false;

	static void init() {
		if (ran) {
			return;
		}
		if (Bootstrap.isRegistered()) {
			LOGGER.warn("Bootstrap already initialized.");
			return;
		}
		ran = true;
		LOGGER.debug("Initializing bootstrap...");
		Bootstrap.register();
		LOGGER.debug("Initialized bootstrap.");
		// Note: not checking Bootstrap.hasErrored as that didn't exist in this version

		LOGGER.debug("Setting up I18n...");
		// Prepare I18n by constructing a LanguageManager and preparing it...
		// (some tests depend on it)
		MetadataSerializer metadataSerializer = new MetadataSerializer();
		LanguageManager languageManager = new LanguageManager(metadataSerializer, "en_us");
		SimpleReloadableResourceManager resourceManager = new SimpleReloadableResourceManager(metadataSerializer);
		IResourcePack pack = new DefaultResourcePack(new ResourceIndex() {}) {
			@Override
			public Set<String> getResourceDomains() {
				return Sets.union(super.getResourceDomains(), Collections.singleton("wdl"));
			}
		};
		resourceManager.reloadResourcePack(pack);
		languageManager.onResourceManagerReload(resourceManager);
		LOGGER.debug("Set up I18n.");
	}
}
