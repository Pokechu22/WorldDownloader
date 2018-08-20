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
package wdl.versioned;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.GameRules;

/**
 * Contains functions related to gamerules. This version of the class is used
 * for Minecraft 1.13.
 */
final class GameRuleFunctions {
	private GameRuleFunctions() { throw new AssertionError(); }

	/* (non-javadoc)
	 * @see VersionedFunctions#getRuleType
	 */
	@Nullable
	static GameRules.ValueType getRuleType(GameRules rules, String rule) {
		GameRules.Value value = rules.get(rule);
		if (value == null) {
			return null;
		} else {
			return value.getType();
		}
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getRuleValue
	 */
	@Nullable
	static String getRuleValue(GameRules rules, String rule) { 
		GameRules.Value value = rules.get(rule);
		if (value == null) {
			return null;
		} else {
			return value.getString();
		}
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getGameRules
	 */
	static List<String> getGameRules(GameRules rules) {
		return ImmutableList.copyOf(GameRules.getDefinitions().keySet()); // Static now for some reason
	}
}
