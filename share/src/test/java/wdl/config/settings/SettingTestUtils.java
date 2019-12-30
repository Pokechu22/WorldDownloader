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

import java.util.function.Consumer;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import wdl.config.CyclableSetting;
import wdl.config.DefaultConfiguration;
import wdl.config.Setting;

/**
 * Utilities for testing settings.
 */
public final class SettingTestUtils {
	private SettingTestUtils() { throw new AssertionError(); }

	/**
	 * Verifies that all translation strings used by the given setting exist.
	 */
	public static <T> void checkAllText(CyclableSetting<T> setting) {
		assertValidTranslationString(setting.getDescription());
		forEachValue(setting, value -> {
			assertValidTranslationString(setting.getButtonText(value));
		});
	}

	/**
	 * Verifies that all values for the setting can be parsed.
	 */
	public static <T> void checkParsability(CyclableSetting<T> setting) {
		forEachValue(setting, value -> {
			assertReserializesCorrectly(setting, value);
		});
	}

	public static <T> void forEachValue(CyclableSetting<T> setting, Consumer<T> action) {
		T def = setting.getDefault(new DefaultConfiguration());
		T value = def;
		do {
			action.accept(value);
			value = setting.cycle(value);
		} while (value != def);
	}

	public static void assertValidTranslationString(ITextComponent component) {
		// Check to make sure it is a translation component in the first place
		// (this is a bit of a dirty check, and if it fails in the future that's somewhat fine)
		assertThat(component, is(instanceOf(TextComponentTranslation.class)));
		TextComponentTranslation transComponent = (TextComponentTranslation) component;
		assertTrue("Translation key should be present: " + transComponent.getKey(),
				I18n.hasKey(transComponent.getKey()));
		// Make sure it can translate
		transComponent.getUnformattedComponentText();
	}

	public static <T> void assertReserializesCorrectly(Setting<T> setting, T value) {
		assertThat(setting.deserializeFromString(setting.serializeToString(value)), is(value));
	}

	public static void assertThrows(Runnable action) {
		try {
			action.run();
		} catch (Exception ex) {
			return;
		}
		fail("Expected an exception to be thrown");
	}
}
