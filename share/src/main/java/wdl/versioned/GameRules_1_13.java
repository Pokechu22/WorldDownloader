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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.world.GameRules;
import wdl.versioned.VersionedFunctions.GameRuleType;

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
	static GameRuleType getRuleType(GameRules rules, String rule) {
		GameRules.Value value = rules.get(rule);
		if (value == null) {
			return null;
		} else {
			switch (value.getType()) {
			case NUMERICAL_VALUE:
				return GameRuleType.INTEGER;
			case BOOLEAN_VALUE:
				return GameRuleType.BOOLEAN;
			default:
				return null;
			}
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
	 * @see VersionedFunctions#setRuleValue
	 */
	static void setRuleValue(GameRules rules, String rule, String value) {
		if (getRuleType(rules, rule) == null) {
			throw new IllegalArgumentException("No rule named " + rule + " exists in " + rules + " (setting to " + value + ", rules list is " + getGameRules(rules) + ")");
		}
		rules.setOrCreateGameRule(rule, value, null); // Server argument is for changecallbacks and can be null
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getGameRules
	 */
	static Map<String, String> getGameRules(GameRules rules) {
		Map<String, String> result = GameRules.getDefinitions()
				.keySet().stream()
				.collect(Collectors.toMap(
						rule -> rule,
						rule -> getRuleValue(rules, rule),
						(a, b) -> {throw new IllegalArgumentException("Mutliple rules with the same name!  " + a + "," + b);},
						TreeMap::new));
		return Collections.unmodifiableMap(result);
	}
}
