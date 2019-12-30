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
package wdl.versioned;


import java.io.IOException;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screen.CreateFlatWorldScreen;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.screen.FlatPresetsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import wdl.config.settings.GeneratorSettings.Generator;

final class GeneratorFunctions {
	private GeneratorFunctions() { throw new AssertionError(); }
	private static final Logger LOGGER = LogManager.getLogger();

	/* (non-javadoc)
	 * @see VersionedFunctions#isAvailableGenerator
	 */
	static boolean isAvaliableGenerator(Generator generator) {
		return generator != Generator.CUSTOMIZED;
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#makeGeneratorSettingsGui
	 */
	static Screen makeGeneratorSettingsGui(Generator generator, Screen parent,
			String generatorConfig, Consumer<String> callback) {
		// NOTE: These give SNBT values, but the actual code expects NBT.
		switch (generator) {
		case FLAT:
			return new FlatPresetsScreen(new GuiCreateFlatWorldProxy(parent, generatorConfig, callback));
		case BUFFET: {
			CompoundNBT generatorNBT;
			try {
				generatorNBT = JsonToNBT.getTagFromJson(generatorConfig);
			} catch (CommandSyntaxException ex) {
				generatorNBT = new CompoundNBT();
			}
			return new CreateBuffetWorldScreen(new GuiCreateWorldProxy(parent, generatorNBT, callback), generatorNBT);
		}
		default:
			LOGGER.warn("Generator lacks extra settings; cannot make a settings GUI: " + generator);
			return parent;
		}
	}

	/**
	 * Fake implementation of {@link GuiCreateFlatWorld} that allows use of
	 * {@link GuiFlatPresets}.  Doesn't actually do anything; just passed in
	 * to the constructor to forward the information we need and to switch
	 * back to the main GUI afterwards.
	 */
	private static class GuiCreateFlatWorldProxy extends CreateFlatWorldScreen {
		private final Screen parent;
		private final String generatorConfig;
		private final Consumer<String> callback;

		public GuiCreateFlatWorldProxy(Screen parent, String generatorConfig, Consumer<String> callback) {
			super(null, new CompoundNBT());
			this.parent = parent;
			this.generatorConfig = generatorConfig;
			this.callback = callback;
		}

		@Override
		public void init() {
			minecraft.displayGuiScreen(parent);
		}

		@Override
		public void render(int mouseX, int mouseY, float partialTicks) {
			// Do nothing
		}

		@Override
		public String getPreset() {
			return generatorConfig;
		}

		@Override
		public void setPreset(@Nullable String preset) {
			super.setPreset(preset);
			// Note: super.setPreset fills in the FlatGenSettings instance.
			// Then we can call getGeneratorOptions to get it as NBT.
			callback.accept(this.getGeneratorOptions().toString());
		}
	}

	/**
	 * Fake implementation of {@link GuiCreateWorld} that allows use of
	 * {@link GuiCustomizeWorldScreen}.  Doesn't actually do anything; just passed in
	 * to the constructor to forward the information we need and to switch
	 * back to the main GUI afterwards.
	 */
	private static class GuiCreateWorldProxy extends CreateWorldScreen {
		private final Screen parent;
		private final Consumer<String> callback;

		public GuiCreateWorldProxy(Screen parent, CompoundNBT generatorNBT, Consumer<String> callback) {
			super(parent);

			this.parent = parent;
			this.callback = callback;

			this.chunkProviderSettingsJson = generatorNBT;
		}

		@Override
		public void init() {
			callback.accept(this.chunkProviderSettingsJson.toString());
			minecraft.displayGuiScreen(this.parent);
		}

		@Override
		public void render(int mouseX, int mouseY, float partialTicks) {
			// Do nothing
		}
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#makeBackupToast
	 */
	static void makeBackupToast(String name, long fileSize) {
		// See GuiWorldEdit.createBackup
		Minecraft.getInstance().enqueue(() -> {
			ToastGui guitoast = Minecraft.getInstance().getToastGui();
			ITextComponent top = new TranslationTextComponent("selectWorld.edit.backupCreated", name);
			ITextComponent bot = new TranslationTextComponent("selectWorld.edit.backupSize", MathHelper.ceil(fileSize / 1048576.0));
			guitoast.add(new SystemToast(SystemToast.Type.WORLD_BACKUP, top, bot));
		});
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#makeBackupFailedToast
	 */
	static void makeBackupFailedToast(IOException ex) {
		// See GuiWorldEdit.createBackup
		String message = ex.getMessage();
		Minecraft.getInstance().enqueue(() -> {
			ToastGui guitoast = Minecraft.getInstance().getToastGui();
			// NOTE: vanilla translation string was missing (MC-137308) until 1.14
			ITextComponent top = new TranslationTextComponent("wdl.toast.backupFailed");
			ITextComponent bot = new StringTextComponent(message);
			guitoast.add(new SystemToast(SystemToast.Type.WORLD_BACKUP, top, bot));
		});
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#VOID_FLAT_CONFIG
	 */
	static final String VOID_FLAT_CONFIG = "{layers:[{block:\"minecraft:air\",height:1b}],biome:\"minecraft:the_void\"}";

	/* (non-javadoc)
	 * @see GeneratorFunctions#createGeneratorOptionsTag
	 */
	public static CompoundNBT createGeneratorOptionsTag(String generatorOptions) {
		try {
			return JsonToNBT.getTagFromJson(generatorOptions);
		} catch (CommandSyntaxException e) {
			return new CompoundNBT();
		}
	}
}
