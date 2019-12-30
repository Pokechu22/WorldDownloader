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
package wdl;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import wdl.WDLMessages.MessageRegistration;
import wdl.api.IWDLMessageType;
import wdl.config.Configuration;
import wdl.config.DefaultConfiguration;
import wdl.config.IConfiguration;
import wdl.config.settings.MessageSettings;
import wdl.config.settings.MessageSettings.MessageCategorySetting;
import wdl.config.settings.MessageSettings.MessageTypeSetting;
import wdl.config.settings.SettingTestUtils;

/**
 * Test that checks the behavior of the various WDLMessageTypes, and the
 * settings related to them.
 */
public class WDLMessageTypesTest extends MaybeMixinTest {
	/**
	 * Tests the translation strings for each message type.
	 */
	@Test
	public void testTypeTranslationStrings() {
		for (IWDLMessageType type : WDLMessageTypes.values()) {
			SettingTestUtils.assertValidTranslationString(type.getDisplayName());
			SettingTestUtils.assertValidTranslationString(type.getDescription());
			MessageRegistration registration = WDLMessages.getRegistration(type);
			SettingTestUtils.checkAllText(registration.setting);
		}
	}

	/**
	 * Tests the translation strings for each message type category.
	 */
	@Test
	public void testCategoryTranslationStrings() {
		MessageTypeCategory[] categories = { MessageTypeCategory.CORE_RECOMMENDED, MessageTypeCategory.CORE_DEBUG };
		for (MessageTypeCategory category : categories) {
			SettingTestUtils.assertValidTranslationString(category.getDisplayName());
			SettingTestUtils.assertValidTranslationString(category.getDescription());
			SettingTestUtils.checkAllText(category.setting);
		}
	}

	@Test
	public void testTypeSerialization() {
		// n.b. Using getRegistration(String) only so that the method is used and covered;
		// it isn't used anywhere else and we technichally don't need to use it here either,
		// but it may be helpful in the future.
		MessageTypeSetting setting = new MessageTypeSetting(WDLMessages.getRegistration("INFO"));
		SettingTestUtils.checkParsability(setting);
	}

	@Test
	public void testCategorySerialization() {
		MessageCategorySetting setting = new MessageCategorySetting(MessageTypeCategory.CORE_RECOMMENDED);
		SettingTestUtils.checkParsability(setting);
	}

	/**
	 * Checks that the default value for each setting is correct.
	 */
	@Test
	public void testDefaults() {
		IConfiguration config = new DefaultConfiguration();
		for (IWDLMessageType type : WDLMessageTypes.values()) {
			MessageRegistration registration = WDLMessages.getRegistration(type);
			assertThat(config.getValue(registration.setting), is(type.isEnabledByDefault()));
		}
	}

	/**
	 * Checks behavior of the message types in context.
	 */
	@Test
	public void testContext() {
		IConfiguration config = new Configuration(new DefaultConfiguration());
		MessageTypeCategory category = MessageTypeCategory.CORE_RECOMMENDED;
		IWDLMessageType type = WDLMessageTypes.INFO;
		MessageRegistration registration = WDLMessages.getRegistration(type);
		config.setValue(MessageSettings.ENABLE_ALL_MESSAGES, true);
		config.setValue(category.setting, true);
		config.setValue(registration.setting, true);
		assertTrue(config.getValue(registration.setting));
		config.setValue(registration.setting, false);
		assertFalse(config.getValue(registration.setting)); // For obvious reasons
		config.setValue(category.setting, false);
		config.setValue(registration.setting, true);
		assertFalse(config.getValue(registration.setting)); // Category must be enabled, even if the setting is enabled
		config.setValue(registration.setting, false);
		assertFalse(config.getValue(registration.setting)); // For obvious reasons
		config.setValue(MessageSettings.ENABLE_ALL_MESSAGES, false);
		config.setValue(category.setting, true);
		config.setValue(registration.setting, true);
		assertFalse(config.getValue(registration.setting)); // If all messages are disabled, the other settings don't matter
		config.setValue(registration.setting, false);
		assertFalse(config.getValue(registration.setting));
		config.setValue(category.setting, false);
		config.setValue(registration.setting, true);
		assertFalse(config.getValue(registration.setting));
		config.setValue(registration.setting, false);
		assertFalse(config.getValue(registration.setting));
	}
}
