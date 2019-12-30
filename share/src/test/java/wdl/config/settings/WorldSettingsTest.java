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

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static wdl.config.settings.SettingTestUtils.*;

import org.junit.Test;

import net.minecraft.entity.Entity;
import wdl.MaybeMixinTest;
import wdl.config.Configuration;
import wdl.config.DefaultConfiguration;
import wdl.config.settings.WorldSettings.SpawnMode;

public class WorldSettingsTest extends MaybeMixinTest {

	@Test
	public void testAllowCheats() {
		checkAllText(WorldSettings.ALLOW_CHEATS);
	}

	@Test
	public void testGameMode() {
		checkAllText(WorldSettings.GAME_MODE);
		checkParsability(WorldSettings.GAME_MODE);
	}

	@Test
	public void testTime() {
		checkAllText(WorldSettings.TIME);
		checkParsability(WorldSettings.TIME);
	}

	@Test
	public void testWeather() {
		checkAllText(WorldSettings.WEATHER);
		checkParsability(WorldSettings.WEATHER);
	}

	@Test
	public void testSpawn() {
		checkAllText(WorldSettings.SPAWN);
		checkParsability(WorldSettings.SPAWN);
	}

	@Test
	public void testSpawnCoords() {
		Configuration config = new Configuration(new DefaultConfiguration());
		config.setValue(WorldSettings.SPAWN_X, 42);
		config.setValue(WorldSettings.SPAWN_Y, 43);
		config.setValue(WorldSettings.SPAWN_Z, 44);
		Entity entity = mock(Entity.class);
		entity.posX = 90;
		entity.posY = 24;
		entity.posZ = 36;

		// All of these are invalid.
		assertThrows(() -> SpawnMode.AUTO.getX(entity, config));
		assertThrows(() -> SpawnMode.AUTO.getY(entity, config));
		assertThrows(() -> SpawnMode.AUTO.getZ(entity, config));

		// These should use the earlier settings
		assertThat(SpawnMode.XYZ.getX(entity, config), is(42));
		assertThat(SpawnMode.XYZ.getY(entity, config), is(43));
		assertThat(SpawnMode.XYZ.getZ(entity, config), is(44));

		// These should use the entity position
		assertThat(SpawnMode.PLAYER.getX(entity, config), is(90));
		assertThat(SpawnMode.PLAYER.getY(entity, config), is(24));
		assertThat(SpawnMode.PLAYER.getZ(entity, config), is(36));
	}
}
