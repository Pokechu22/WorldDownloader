/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018-2019 Pokechu22, julialy
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

import net.minecraft.entity.Entity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.MathHelper;
import wdl.config.BooleanSetting;
import wdl.config.EnumSetting;
import wdl.config.IConfiguration;
import wdl.config.IntSetting;
import wdl.versioned.VersionedFunctions;

/**
 * Contains various settings and enums for world settings.
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
	public static final IntSetting SPAWN_X = new IntSetting("SpawnX", 8);
	public static final IntSetting SPAWN_Y = new IntSetting("SpawnY", 127);
	public static final IntSetting SPAWN_Z = new IntSetting("SpawnZ", 8);

	public enum GameMode implements IStringSerializable {
		KEEP("keep", -1, false),
		CREATIVE("creative", 1, false),
		SURVIVAL("survival", 0, false),
		HARDCORE("hardcore", 0, true);

		private final String confName;
		private static final Map<String, GameMode> FROM_STRING = makeFromString(values(), t -> t.confName);

		public final int gamemodeID;
		public final boolean hardcore;

		private GameMode(String confName, int gamemodeID, boolean hardcore) {
			this.confName = confName;
			this.gamemodeID = gamemodeID;
			this.hardcore = hardcore;
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
		KEEP("keep", false, -1, false, -1),
		SUNNY("sunny", false, 0, false, 0),
		RAIN("rain", true, 24000, false, 0),
		THUNDERSTORM("thunderstorm", true, 24000, true, 24000);

		private final String confName;
		private static final Map<String, Weather> FROM_STRING = makeFromString(values(), t -> t.confName);

		public final boolean raining;
		public final int rainTime;
		public final boolean thundering;
		public final int thunderTime;

		private Weather(String confName, boolean raining, int rainTime, boolean thundering, int thunderTime) {
			this.confName = confName;
			this.raining = raining;
			this.rainTime = rainTime;
			this.thundering = thundering;
			this.thunderTime = thunderTime;
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
		PLAYER("player") {
			@Override
			public int getX(Entity player, IConfiguration config) {
				return MathHelper.floor(VersionedFunctions.getEntityX(player));
			}
			@Override
			public int getY(Entity player, IConfiguration config) {
				return MathHelper.floor(VersionedFunctions.getEntityY(player));
			}
			@Override
			public int getZ(Entity player, IConfiguration config) {
				return MathHelper.floor(VersionedFunctions.getEntityZ(player));
			}
		},
		XYZ("xyz") {
			@Override
			public int getX(Entity player, IConfiguration config) {
				return config.getValue(SPAWN_X);
			}
			@Override
			public int getY(Entity player, IConfiguration config) {
				return config.getValue(SPAWN_Y);
			}
			@Override
			public int getZ(Entity player, IConfiguration config) {
				return config.getValue(SPAWN_Z);
			}
		};

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

		public int getX(Entity player, IConfiguration config) {
			throw new UnsupportedOperationException();
		}
		public int getY(Entity player, IConfiguration config) {
			throw new UnsupportedOperationException();
		}
		public int getZ(Entity player, IConfiguration config) {
			throw new UnsupportedOperationException();
		}
	}

	public enum Time implements IStringSerializable {
		KEEP("keep", -1),
		SUNRISE("23000", 23000),
		MORNING("0", 0),
		NOON("6000", 6000),
		EVENING("11500", 11500),
		SUNSET("12500", 12500),
		MIDNIGHT("18000", 18000);

		private final String confName;
		private static final Map<String, Time> FROM_STRING = makeFromString(values(), t -> t.confName);

		public final long timeValue;

		private Time(String confName, long timeValue) {
			this.confName = confName;
			this.timeValue = timeValue;
		}

		public static Time fromString(String confName) {
			return FROM_STRING.get(confName);
		}

		@Override
		public String getName() {
			return confName;
		}
	}
}
