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
package wdl.versioned;

import java.util.Map;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.GameSettings;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.text.TextComponentString;

/**
 * Functions that help deal with things that vary in type between versions.
 */
final class TypeFunctions {
	/* (non-javadoc)
	 * @see VersionedFunctions#getChunkListClass
	 */
	@SuppressWarnings("rawtypes")
	static Class<Long2ObjectMap> getChunkListClass() {
		return Long2ObjectMap.class;
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getChunksToSaveClass
	 */
	@SuppressWarnings("rawtypes")
	static Class<Map> getChunksToSaveClass() {
		return Map.class;
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#customName
	 */
	static TextComponentString customName(String name) {
		return new TextComponentString(name);
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#createNewGameSettings
	 */
	static GameSettings createNewGameSettings() {
		return new GameSettings();
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getGameRendererClass
	 */
	static Class<GameRenderer> getGameRendererClass() {
		return GameRenderer.class;
	}
}
