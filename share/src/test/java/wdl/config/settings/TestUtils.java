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
package wdl.config.settings;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.function.Consumer;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import wdl.config.CyclableSetting;
import wdl.config.DefaultConfiguration;

/**
 * Utilities for testing settings.
 */
final class TestUtils {
	private TestUtils() { throw new AssertionError(); }

	/**
	 * Verifies that all translation strings used by the given setting exist.
	 */
	public static <T> void checkAllText(CyclableSetting<T> setting) {
		forEachValue(setting, value -> {
			assertValidTranslationString(setting.getButtonText(value));
		});
	}

	/**
	 * Verifies that all values for the setting can be parsed.
	 */
	public static <T> void checkParsability(CyclableSetting<T> setting) {
		forEachValue(setting, value -> {
			assertThat(setting.deserializeFromString(setting.serializeToString(value)), is(value));
		});
	}

	private static <T> void forEachValue(CyclableSetting<T> setting, Consumer<T> action) {
		T def = setting.getDefault(new DefaultConfiguration());
		assertValidTranslationString(setting.getDescription());
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

	public static void assertThrows(Runnable action) {
		try {
			action.run();
		} catch (Exception ex) {
			return;
		}
		fail("Expected an exception to be thrown");
	}
}
