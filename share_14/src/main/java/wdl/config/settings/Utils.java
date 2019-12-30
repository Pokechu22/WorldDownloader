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
package wdl.config.settings;

import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

final class Utils {
	private Utils() { throw new AssertionError(); }

	/**
	 * Creates an ImmutableMap intended for the FROM_STRING field for various enums.
	 */
	public static <T> ImmutableMap<String, T> makeFromString(T[] values, Function<T, String> keyFunc) {
		// Can't use streams and toImmutableMap because of older versions
		ImmutableMap.Builder<String, T> b = ImmutableMap.builder();
		for (T t : values) {
			b.put(keyFunc.apply(t), t);
		}
		return b.build();
	}

}
