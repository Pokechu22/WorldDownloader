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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static wdl.config.settings.SettingTestUtils.*;

import java.util.function.BooleanSupplier;

import org.junit.Test;

import wdl.MaybeMixinTest;
import wdl.config.Configuration;
import wdl.config.DefaultConfiguration;
import wdl.config.IConfiguration;
import wdl.config.settings.EntitySettings.TrackDistanceMode;

public class EntitySettingsTest extends MaybeMixinTest {

	@Test
	public void testTrackDistanceMode() {
		doWithHasServerRange(() -> true, () -> {
			checkAllText(EntitySettings.TRACK_DISTANCE_MODE);
			checkParsability(EntitySettings.TRACK_DISTANCE_MODE);
		});
	}

	@Test
	public void testCycleSimple() {
		doWithHasServerRange(() -> false, () -> {
			IConfiguration config = new Configuration(new DefaultConfiguration());
			assertThat(config.getValue(EntitySettings.TRACK_DISTANCE_MODE), is(TrackDistanceMode.DEFAULT)); // default
			config.cycle(EntitySettings.TRACK_DISTANCE_MODE);
			assertThat(config.getValue(EntitySettings.TRACK_DISTANCE_MODE), is(TrackDistanceMode.USER));
			config.cycle(EntitySettings.TRACK_DISTANCE_MODE);
			assertThat(config.getValue(EntitySettings.TRACK_DISTANCE_MODE), is(TrackDistanceMode.DEFAULT));
		});
		doWithHasServerRange(() -> true, () -> {
			IConfiguration config = new Configuration(new DefaultConfiguration());
			assertThat(config.getValue(EntitySettings.TRACK_DISTANCE_MODE), is(TrackDistanceMode.SERVER)); // default
			config.cycle(EntitySettings.TRACK_DISTANCE_MODE);
			assertThat(config.getValue(EntitySettings.TRACK_DISTANCE_MODE), is(TrackDistanceMode.USER));
			config.cycle(EntitySettings.TRACK_DISTANCE_MODE);
			assertThat(config.getValue(EntitySettings.TRACK_DISTANCE_MODE), is(TrackDistanceMode.DEFAULT));
			config.cycle(EntitySettings.TRACK_DISTANCE_MODE);
			assertThat(config.getValue(EntitySettings.TRACK_DISTANCE_MODE), is(TrackDistanceMode.SERVER));
		});
	}

	private synchronized void doWithHasServerRange(BooleanSupplier hasServerRange, Runnable action) {
		BooleanSupplier old = EntitySettings.hasServerEntityRange;
		try {
			EntitySettings.hasServerEntityRange = hasServerRange;
			action.run();
		} finally {
			EntitySettings.hasServerEntityRange = old;
		}
	}
}
