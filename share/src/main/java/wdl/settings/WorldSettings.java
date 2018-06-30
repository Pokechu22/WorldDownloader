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

import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

import net.minecraft.util.IStringSerializable;

/**
 * Contains various settings and enums enums for world settings.
 */
public final class WorldSettings {
	private WorldSettings() { throw new AssertionError(); }

	public static final BooleanSetting ALLOW_CHEATS =
			new BooleanSetting("AllowCheats", true, "wdl.gui.world.allowCheats");
	public static final EnumSetting<GameMode> GAME_MODE =
			new EnumSetting<>("GameType", GameMode.KEEP, "wdl.gui.world.gamemode", GameMode.values(), GameMode::fromString);
	public static final EnumSetting<Time> TIME =
			new EnumSetting<>("Time", Time.KEEP, "wdl.gui.world.time", Time.values(), Time::fromString);
	public static final EnumSetting<Weather> WEATHER =
			new EnumSetting<>("Weather", Weather.KEEP, "wdl.gui.world.weather", Weather.values(), Weather::fromString);
	public static final EnumSetting<SpawnMode> SPAWN =
			new EnumSetting<>("Spawn", SpawnMode.AUTO, "wdl.gui.world.spawn", SpawnMode.values(), SpawnMode::fromString);

	public enum GameMode implements IStringSerializable {
		KEEP("keep"),
		CREATIVE("creative"),
		SURVIVAL("survival"),
		HARDCORE("hardcore");

		private final String confName;
		private static final Map<String, GameMode> FROM_STRING = makeFromString(values(), t -> t.confName);

		private GameMode(String confName) {
			this.confName = confName;
		}

		public static GameMode fromString(String confName) {
			return FROM_STRING.get(confName);
		}

		@Override
		public String getName() {
			return confName;
		}
	}

	public enum Weather implements IStringSerializable {
		KEEP("keep"),
		SUNNY("sunny"),
		RAIN("rain"),
		THUNDERSTORM("thunderstorm");

		private final String confName;
		private static final Map<String, Weather> FROM_STRING = makeFromString(values(), t -> t.confName);

		private Weather(String confName) {
			this.confName = confName;
		}

		public static Weather fromString(String confName) {
			return FROM_STRING.get(confName);
		}

		@Override
		public String getName() {
			return confName;
		}
	}

	public enum SpawnMode implements IStringSerializable {
		AUTO("auto"),
		PLAYER("player"),
		XYZ("xyz");

		private final String confName;
		private static final Map<String, SpawnMode> FROM_STRING = makeFromString(values(), t -> t.confName);

		private SpawnMode(String confName) {
			this.confName = confName;
		}

		public static SpawnMode fromString(String confName) {
			return FROM_STRING.get(confName);
		}

		@Override
		public String getName() {
			return confName;
		}
	}

	public enum Time implements IStringSerializable {
		KEEP("keep"),
		SUNRISE("23000"),
		MORNING("0"),
		NOON("6000"),
		EVENING("11500"),
		SUNSET("12500"),
		MIDNIGHT("18000");

		private final String confName;
		private static final Map<String, Time> FROM_STRING = makeFromString(values(), t -> t.confName);

		private Time(String confName) {
			this.confName = confName;
		}

		public static Time fromString(String confName) {
			return FROM_STRING.get(confName);
		}

		@Override
		public String getName() {
			return confName;
		}
	}

	// Can't use streams and toImmutableMap because of older versions
	private static <T> ImmutableMap<String, T> makeFromString(T[] values, Function<T, String> keyFunc) {
		ImmutableMap.Builder<String, T> b = ImmutableMap.builder();
		for (T t : values) {
			b.put(keyFunc.apply(t), t);
		}
		return b.build();
	}
}
