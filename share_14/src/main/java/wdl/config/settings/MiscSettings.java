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
package wdl.config.settings;

import java.util.Optional;

import wdl.WDL;
import wdl.WorldBackup.WorldBackupType;
import wdl.config.BaseSetting;
import wdl.config.IConfiguration;
import wdl.config.Setting;
import wdl.config.StringSetting;

/**
 * Miscellaneous settings that don't fit into any other group.
 */
public final class MiscSettings {
	private MiscSettings() { throw new AssertionError(); }

	// General
	public static final Setting<String> SERVER_NAME =
			new ServerNameSetting("ServerName");
	public static final StringSetting WORLD_NAME =
			new StringSetting("WorldName", "");
	// XXX this really should be a different type
	public static final StringSetting LINKED_WORLDS =
			new StringSetting("LinkedWorlds", "");
	public static final Setting<Long> LAST_SAVED =
			new BaseSetting<>("LastSaved", -1L, Long::parseLong, Object::toString);

	private static class ServerNameSetting implements Setting<String> {
		private final String name;

		public ServerNameSetting(String name) {
			this.name = name;
		}

		@Override
		public String deserializeFromString(String text) {
			return text;
		}

		@Override
		public String serializeToString(String value) {
			return value;
		}

		@Override
		public String getConfigurationKey() {
			return name;
		}

		@Override
		public String getDefault(IConfiguration context) {
			return WDL.getInstance().getServerName();
		}
	}

	// Backup
	public static final Setting<WorldBackupType> BACKUP_TYPE =
			new BaseSetting<>("Backup", WorldBackupType.ZIP, WorldBackupType::valueOf, WorldBackupType::name);
	public static final StringSetting BACKUP_COMMAND_TEMPLATE =
			new StringSetting("BackupCommand", "7z a -bsp1 ${destination} ${source}");
	public static final StringSetting BACKUP_EXTENSION  =
			new StringSetting("BackupExtension", "7z");

	// Update checker
	public static final Setting<Boolean> TUTORIAL_SHOWN =
			new BaseSetting<>("TutorialShown", false, Boolean::valueOf, Object::toString);

	public static final Setting<Optional<String>> UPDATE_ETAG =
			new BaseSetting<>("UpdateETag", Optional.empty(),
					str -> str.isEmpty() ? Optional.empty() : Optional.of(str),
					opt -> opt.orElse(""));

	// Extensions
	public static class ExtensionEnabledSetting extends BaseSetting<Boolean> {
		public ExtensionEnabledSetting(String modID) {
			super("Extensions." + modID + ".enabled", true, Boolean::valueOf, Object::toString);
		}
	}

}
