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

import static wdl.config.settings.Utils.*;

import java.util.Map;

import net.minecraft.util.IStringSerializable;
import wdl.config.EnumSetting;
import wdl.config.IntSetting;

/**
 * Contains various settings and enums for player settings.
 */
public final class PlayerSettings {
	private PlayerSettings() { throw new AssertionError(); }

	public static final EnumSetting<Health> HEALTH =
			new EnumSetting<>("PlayerHealth", Health.KEEP, "wdl.gui.player.health", Health.values(), Health::fromString);
	public static final EnumSetting<Hunger> HUNGER =
			new EnumSetting<>("PlayerFood", Hunger.KEEP, "wdl.gui.player.hunger", Hunger.values(), Hunger::fromString);
	public static final EnumSetting<PlayerPos> PLAYER_POSITION =
			new EnumSetting<>("PlayerPos", PlayerPos.KEEP, "wdl.gui.player.position", PlayerPos.values(), PlayerPos::fromString);
	// Used when PLAYER_POSITION is XYZ
	public static final IntSetting PLAYER_X = new IntSetting("PlayerX", 8);
	public static final IntSetting PLAYER_Y = new IntSetting("PlayerY", 127);
	public static final IntSetting PLAYER_Z = new IntSetting("PlayerZ", 8);

	public enum Health implements IStringSerializable {
		KEEP("keep", -1),
		FULL("20", 20);

		private final String confName;
		private static final Map<String, Health> FROM_STRING = makeFromString(values(), t -> t.confName);

		public final short healthValue;

		private Health(String confName, int healthValue) {
			this.confName = confName;
			this.healthValue = (short) healthValue;
		}

		public static Health fromString(String confName) {
			return FROM_STRING.get(confName);
		}

		@Override
		public String getName() {
			return confName;
		}
	}

	public enum Hunger implements IStringSerializable {
		KEEP("keep", -1, -1, -1, -1),
		FULL("20", 20, 0, 5.0f, 0.0f);

		private final String confName;
		private static final Map<String, Hunger> FROM_STRING = makeFromString(values(), t -> t.confName);

		// https://minecraft.gamepedia.com/Hunger#Mechanics
		public final int foodLevel;
		public final int foodTickTimer;
		public final float foodSaturationLevel; // should be 5 by default if full hunger
		public final float foodExhaustionLevel;

		private Hunger(String confName, int foodLevel, int foodTickTimer,
				float foodSaturationLevel, float foodExhaustionLevel) {
			this.confName = confName;
			this.foodLevel = foodLevel;
			this.foodTickTimer = foodTickTimer;
			this.foodSaturationLevel = foodSaturationLevel;
			this.foodExhaustionLevel = foodExhaustionLevel;
		}

		public static Hunger fromString(String confName) {
			return FROM_STRING.get(confName);
		}

		@Override
		public String getName() {
			return confName;
		}
	}

	public enum PlayerPos implements IStringSerializable {
		KEEP("keep"),
		XYZ("xyz");

		private final String confName;
		private static final Map<String, PlayerPos> FROM_STRING = makeFromString(values(), t -> t.confName);

		private PlayerPos(String confName) {
			this.confName = confName;
		}

		public static PlayerPos fromString(String confName) {
			return FROM_STRING.get(confName);
		}

		@Override
		public String getName() {
			return confName;
		}
	}
}
