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
package wdl.versioned;


import java.io.IOException;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screen.CreateFlatWorldScreen;
import net.minecraft.client.gui.screen.FlatPresetsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.FlatGenerationSettings;
import wdl.config.settings.GeneratorSettings.Generator;

final class GeneratorFunctions {
	private GeneratorFunctions() { throw new AssertionError(); }
	private static final Logger LOGGER = LogManager.getLogger();

	/* (non-javadoc)
	 * @see VersionedFunctions#isAvailableGenerator
	 */
	static boolean isAvaliableGenerator(Generator generator) {
		return generator != Generator.CUSTOMIZED
				&& generator != Generator.BUFFET;
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
		case SINGLE_BIOME_SURFACE:
		case SINGLE_BIOME_CAVES:
		case SINGLE_BIOME_FLOATING_ISLANDS: {
			return new CreateBuffetWorldScreen(parent, DynamicRegistries.func_239770_b_(), convertBiomeToConfig(callback), convertConfigToBiome(generatorConfig));
		}
		default:
			LOGGER.warn("Generator lacks extra settings; cannot make a settings GUI: " + generator);
			return parent;
		}
	}

	private static Consumer<Biome> convertBiomeToConfig(Consumer<String> callback) {
		// TODO
		return biome -> {
			Biome.field_242418_b
					.encodeStart(JsonOps.INSTANCE, biome)
					.map(JsonElement::toString)
					.getOrThrow(true, LOGGER::error);
		};
	}

	private static Biome convertConfigToBiome(String config) {
		// TODO
		JsonObject jsonobject = config.isEmpty() ? new JsonObject() : JSONUtils.fromJson(config);
		return Biome.field_242418_b
				.parse(JsonOps.INSTANCE, jsonobject)
				.resultOrPartial(LOGGER::error)
				.orElseGet(() -> {
					Registry<Biome> biomesReg = DynamicRegistries.func_239770_b_().func_243612_b(Registry.field_239720_u_);
					return biomesReg.func_230516_a_(Biomes.THE_VOID);
				});
	}

	/**
	 * Fake implementation of {@link GuiCreateFlatWorld} that allows use of
	 * {@link GuiFlatPresets}.  Doesn't actually do anything; just passed in
	 * to the constructor to forward the information we need and to switch
	 * back to the main GUI afterwards.
	 */
	private static class GuiCreateFlatWorldProxy extends CreateFlatWorldScreen {
		private final Screen parent;

		public GuiCreateFlatWorldProxy(Screen parent, String config, Consumer<String> callback) {
			super(null, convertSettingsToConfig(callback), convertConfigToSettings(config));
			this.parent = parent;
		}

		private static Consumer<FlatGenerationSettings> convertSettingsToConfig(Consumer<String> callback) {
			return settings -> {
				FlatGenerationSettings.field_236932_a_
						.encodeStart(JsonOps.INSTANCE, settings)
						.map(JsonElement::toString)
						.getOrThrow(true, LOGGER::error);
			};
		}

		private static FlatGenerationSettings convertConfigToSettings(String config) {
			JsonObject jsonobject = config.isEmpty() ? new JsonObject() : JSONUtils.fromJson(config);
			return FlatGenerationSettings.field_236932_a_
					.parse(JsonOps.INSTANCE, jsonobject)
					.resultOrPartial(LOGGER::error)
					.orElseGet(() -> {
						Registry<Biome> biomesReg = DynamicRegistries.func_239770_b_().func_243612_b(Registry.field_239720_u_);
						return FlatGenerationSettings.func_242869_a(biomesReg);
					});
		}

		@Override
		public void init() {
			minecraft.displayGuiScreen(parent);
		}

		@Override
		public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			// Do nothing
		}
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#makeBackupToast
	 */
	static void makeBackupToast(String name, long fileSize) {
		// See GuiWorldEdit.createBackup
		Minecraft.getInstance().execute(() -> {
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
		Minecraft.getInstance().execute(() -> {
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
	static CompoundNBT createGeneratorOptionsTag(String generatorOptions) {
		try {
			return JsonToNBT.getTagFromJson(generatorOptions);
		} catch (CommandSyntaxException e) {
			return new CompoundNBT();
		}
	}
}
