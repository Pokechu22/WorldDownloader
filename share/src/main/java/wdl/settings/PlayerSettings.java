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

import static wdl.settings.Utils.*;

import java.util.Map;

import net.minecraft.util.IStringSerializable;

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

	public enum Health implements IStringSerializable {
		KEEP("keep"),
		FULL("20");

		private final String confName;
		private static final Map<String, Health> FROM_STRING = makeFromString(values(), t -> t.confName);

		private Health(String confName) {
			this.confName = confName;
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
		KEEP("keep"),
		FULL("20");

		private final String confName;
		private static final Map<String, Hunger> FROM_STRING = makeFromString(values(), t -> t.confName);

		private Hunger(String confName) {
			this.confName = confName;
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
