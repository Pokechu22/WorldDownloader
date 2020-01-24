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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.tree.CommandNode;

import net.minecraft.command.CommandSource;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.IRuleEntryVisitor;
import net.minecraft.world.GameRules.RuleKey;
import net.minecraft.world.GameRules.RuleType;
import net.minecraft.world.GameRules.RuleValue;
import wdl.versioned.VersionedFunctions.GameRuleType;

/**
 * Contains functions related to gamerules. This version of the class is used
 * for Minecraft 1.14.3.
 */
final class GameRuleFunctions {
	private GameRuleFunctions() { throw new AssertionError(); }
	private static final Logger LOGGER = LogManager.getLogger();

	private static class RuleInfo<T extends GameRules.RuleValue<T>> {
		public RuleInfo(RuleKey<T> key, RuleType<T> type) {
			this.key = key;
			this.type = type;
			this.commandNode = this.type.createArgument("value").build();
			T defaultValue = type.createValue();
			if (defaultValue instanceof GameRules.BooleanValue) {
				this.wdlType = GameRuleType.BOOLEAN;
			} else if (defaultValue instanceof GameRules.IntegerValue) {
				this.wdlType = GameRuleType.INTEGER;
			} else {
				LOGGER.warn("[WDL] Unknown gamerule type {} for {}, default value {} ({})", type, key, defaultValue, (defaultValue != null ? defaultValue.getClass() : "null"));
				this.wdlType = null;
			}
		}
		public final GameRules.RuleKey<T> key;
		public final GameRules.RuleType<T> type;
		@Nullable
		public final GameRuleType wdlType;
		private final CommandNode<CommandSource> commandNode;

		// I'm not particularly happy about the lack of a public generalized string set method...
		public void set(GameRules rules, String value) {
			try {
				CommandContextBuilder<CommandSource> ctxBuilder = new CommandContextBuilder<>(null, null, null, 0);
				StringReader reader = new StringReader(value);
				this.commandNode.parse(reader, ctxBuilder);
				rules.get(this.key).updateValue(ctxBuilder.build(value), "value");
			} catch (Exception ex) {
				LOGGER.error("[WDL] Failed to set rule {} to {}", key, value, ex);
				throw new IllegalArgumentException("Failed to set rule " + key + " to " + value, ex);
			}
		}
	}
	private static final Map<String, RuleInfo<?>> RULES;
	static {
		RULES = new TreeMap<>();
		GameRules.visitAll(new IRuleEntryVisitor() {
			@Override
			public <T extends RuleValue<T>> void visit(RuleKey<T> key, RuleType<T> type) {
				RULES.put(key.getName(), new RuleInfo<>(key, type));
			}
		});
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getRuleType
	 */
	@Nullable
	static GameRuleType getRuleType(GameRules rules, String rule) {
		if (RULES.containsKey(rule)) {
			return RULES.get(rule).wdlType;
		} else {
			return null;
		}
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getRuleValue
	 */
	@Nullable
	static String getRuleValue(GameRules rules, String rule) { 
		if (RULES.containsKey(rule)) {
			return rules.get(RULES.get(rule).key).toString();
		} else {
			return null;
		}
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#setRuleValue
	 */
	static void setRuleValue(GameRules rules, String rule, String value) {
		if (!RULES.containsKey(rule)) {
			throw new IllegalArgumentException("No rule named " + rule + " exists in " + rules + " (setting to " + value + ", rules list is " + getGameRules(rules) + ")");
		} else {
			RULES.get(rule).set(rules, value);
		}
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#getGameRules
	 */
	static Map<String, String> getGameRules(GameRules rules) {
		Map<String, String> result = RULES
				.keySet().stream()
				.collect(Collectors.toMap(
						rule -> rule,
						rule -> getRuleValue(rules, rule),
						(a, b) -> {throw new IllegalArgumentException("Mutliple rules with the same name!  " + a + "," + b);},
						TreeMap::new));
		return Collections.unmodifiableMap(result);
	}
}
