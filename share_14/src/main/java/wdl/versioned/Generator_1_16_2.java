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
import java.nio.file.Path;
import java.util.function.Consumer;

import javax.annotation.Nullable;

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
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.screen.FlatPresetsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldSettingsImport;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import wdl.WDL;
import wdl.config.settings.GeneratorSettings.Generator;

final class GeneratorFunctions {
	private GeneratorFunctions() { throw new AssertionError(); }
	private static final Logger LOGGER = LogManager.getLogger();

	/* (non-javadoc)
	 * @see VersionedFunctions#isAvailableGenerator
	 */
	static boolean isAvaliableGenerator(Generator generator) {
		switch (generator) {
		// Generators that no longer exists
		case CUSTOMIZED:
		case BUFFET:
		// Single-biome generators - should possibly exist (or maybe are just buffet),
		// but not implemented (TODO)
		case SINGLE_BIOME_SURFACE:
		case SINGLE_BIOME_CAVES:
		case SINGLE_BIOME_FLOATING_ISLANDS:
			return false;
		default:
			return true;
		}
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
		private final Consumer<String> callback;

		public GuiCreateFlatWorldProxy(Screen parent, String config, Consumer<String> callback) {
			super(CreateWorldScreenProxy.create(),
					settings -> LOGGER.warn("[WDL] Unexpected GuiCreateFlatWorldProxy callback, ignoring: " + settings),
					convertConfigToSettings(config));
			this.parent = parent;
			this.callback = callback;
		}

		private static String convertSettingsToConfig(FlatGenerationSettings settings) {
			// XXX This doesn't seem to work quite right; the biome's data is still serialized instead of the biome key
			WorldSettingsImport<JsonElement> ops = WorldSettingsImport.func_244335_a(JsonOps.INSTANCE,
					WDL.getInstance().minecraft.getResourceManager(), DynamicRegistries.func_239770_b_());
			return FlatGenerationSettings.field_236932_a_
					.encodeStart(ops, settings)
					.map(JsonElement::toString)
					.getOrThrow(true, LOGGER::error);
		}

		private static FlatGenerationSettings convertConfigToSettings(String config) {
			WorldSettingsImport<JsonElement> ops = WorldSettingsImport.func_244335_a(JsonOps.INSTANCE,
					WDL.getInstance().minecraft.getResourceManager(), DynamicRegistries.func_239770_b_());
			JsonObject jsonobject = config.isEmpty() ? new JsonObject() : JSONUtils.fromJson(config);
			return FlatGenerationSettings.field_236932_a_
					.parse(ops, jsonobject)
					.resultOrPartial(LOGGER::error)
					.orElseGet(() -> {
						Registry<Biome> biomesReg = DynamicRegistries.func_239770_b_().func_243612_b(Registry.field_239720_u_);
						return FlatGenerationSettings.func_242869_a(biomesReg);
					});
		}

		@Override
		public void init() {
			// The flat presets screen only can have a CreateFlatWorld screen as a parent.  Thus,
			// this proxy acts as its parent but directly changes to the real parent.
			minecraft.displayGuiScreen(parent);
			// We can't directly get a callback from the flat presets screen,
			// and the callback in the constructor is only used when the done button here is clicked.
			// Instead, call our callback when exiting this screen.
			callback.accept(convertSettingsToConfig(this.func_238603_g_()));
		}

		@Override
		public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			// Do nothing
		}
	}

	/**
	 * Further fake implementations, which make things annoying.  Needed because {@link FlatPresetsScreen} has
	 * <pre>Registry<Biome> registry = this.parentScreen.createWorldGui.field_238934_c_.func_239055_b_().func_243612_b(Registry.field_239720_u_);</pre>
	 *
	 * (field_238934_c_ is a WorldOptionsScreen, but CreateWorldScreen creates it in the constructor)
	 */
	private static class CreateWorldScreenProxy extends CreateWorldScreen {
		public CreateWorldScreenProxy(@Nullable Screen p_i242064_1_, WorldSettings p_i242064_2_,
				DimensionGeneratorSettings p_i242064_3_, @Nullable Path p_i242064_4_, DatapackCodec p_i242064_5_,
				DynamicRegistries.Impl p_i242064_6_) {
			super(p_i242064_1_, p_i242064_2_, p_i242064_3_, p_i242064_4_, p_i242064_5_, p_i242064_6_);
		}

		public static CreateWorldScreenProxy create() {
			WorldSettings worldSettings = new WorldSettings("LevelName", GameType.CREATIVE, false,
					Difficulty.NORMAL, true, new GameRules(), DatapackCodec.field_234880_a_);
			Registry<DimensionType> dimType = DynamicRegistries.func_239770_b_().func_230520_a_();
			Registry<Biome> biomes = DynamicRegistries.func_239770_b_().func_243612_b(Registry.field_239720_u_);
			Registry<DimensionSettings> dimSettings = DynamicRegistries.func_239770_b_().func_243612_b(Registry.field_243549_ar);
			DimensionGeneratorSettings genSettings = DimensionGeneratorSettings.func_242751_a(dimType, biomes, dimSettings);
			return new CreateWorldScreenProxy(null, worldSettings, genSettings,
					null, DatapackCodec.field_234880_a_, DynamicRegistries.func_239770_b_());
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
	static final String VOID_FLAT_CONFIG = "{features:0,lakes:0,layers:[{block:\"minecraft:air\",height:1b}],biome:\"minecraft:the_void\",structures:{structures:{}}}";

	/* (non-javadoc)
	 * @see GeneratorFunctions#writeGeneratorOptions
	 *
	 * An example (normal terrain):
	 * 
	 * <pre>
	 * + WorldGenSettings: 4 entries
	 *   + bonus_chest: 0
	 *   + generate_features: 1
	 *   + seed: -4511540289422318412
	 *   + dimensions: 3 entries
	 *     + minecraft:overworld: 2 entries
	 *     | + type: minecraft:overworld
	 *     | + generator: 4 entries
	 *     |   + seed: -4511540289422318412
	 *     |   + settings: minecraft:overworld
	 *     |   + type: minecraft:noise
	 *     |   + biome_source: 3 entries
	 *     |     + large_biomes: 0
	 *     |     + seed: -4511540289422318412
	 *     |     + type: minecraft:vanilla_layered
	 *     + minecraft:the_end: 2 entries
	 *     | + type: minecraft:the_end
	 *     | + generator: 4 entries
	 *     |   + seed: -4511540289422318412
	 *     |   + settings: minecraft:end
	 *     |   + type: minecraft:noise
	 *     |   + biome_source: 2 entries
	 *     |     + seed: -4511540289422318412
	 *     |     + type: minecraft:the_end
	 *     + minecraft:the_nether: 2 entries
	 *       + type: minecraft:the_nether
	 *       + generator: 4 entries
	 *         + seed: -4511540289422318412
	 *         + settings: minecraft:nether
	 *         + type: minecraft:noise
	 *         + biome_source: 3 entries
	 *           + seed: -4511540289422318412
	 *           + preset: minecraft:nether
	 *           + type: minecraft:multi_noise
	 * </pre>
	 */
	static void writeGeneratorOptions(CompoundNBT worldInfoNBT, long randomSeed, boolean mapFeatures, String generatorName, String generatorOptions, int generatorVersion) {
		CompoundNBT genSettings = new CompoundNBT();
		genSettings.putBoolean("bonus_chest", false);
		genSettings.putBoolean("generate_features", mapFeatures);
		genSettings.putLong("seed", randomSeed);
		CompoundNBT dimensions = new CompoundNBT();
		dimensions.put("minecraft:overworld", createOverworld(randomSeed, generatorName, generatorOptions, generatorVersion));
		dimensions.put("minecraft:the_end", createDefaultEnd(randomSeed));
		dimensions.put("minecraft:the_nether", createDefaultNether(randomSeed));
		genSettings.put("dimensions", dimensions);
		worldInfoNBT.put("WorldGenSettings", genSettings);
	}

	private static CompoundNBT createOverworld(long seed, String name, String options, int version) {
		// TODO: this isn't a complete implementation (e.g. it doesn't handle buffet)
		if (name.equals("flat")) {
			return createFlatGenerator(seed, options);
		}
		boolean isAmplified = name.equals("amplified");
		boolean isLargeBiomes = name.equals("largeBiomes");
		boolean isLegacy = name.equals("default_1_1") || (name.equals("default") && version == 0);
		return createOverworldGenerator(seed, isAmplified, isLargeBiomes, isLegacy);
	}

	private static CompoundNBT createFlatGenerator(long seed, String options) {
		CompoundNBT result = new CompoundNBT();
		result.putString("type", "minecraft:overworld");
		CompoundNBT generator = new CompoundNBT();
		generator.putString("type", "minecraft:flat");
		CompoundNBT settings;
		try {
			settings = JsonToNBT.getTagFromJson(options);
		} catch (CommandSyntaxException e) {
			settings = new CompoundNBT();
		}
		generator.put("settings", settings);
		result.put("generator", generator);
		return result;
	}

	private static CompoundNBT createOverworldGenerator(long seed, boolean amplified, boolean largeBiomes, boolean legacy) {
		// Refer to WorldGenSetting.func_233427_a_ and func_233423_a_
		CompoundNBT result = new CompoundNBT();
		result.putString("type", "minecraft:overworld");
		CompoundNBT generator = new CompoundNBT();
		generator.putLong("seed", seed);
		generator.putString("settings", amplified ? "minecraft:amplified" : "minecraft:overworld");
		generator.putString("type", "minecraft:noise");
		CompoundNBT biomeSource = new CompoundNBT();
		biomeSource.putBoolean("large_biomes", largeBiomes);
		biomeSource.putLong("seed", seed);
		biomeSource.putString("type", "minecraft:vanilla_layered");
		if (legacy) {
			biomeSource.putBoolean("legacy_biome_init_layer", true);
		}
		generator.put("biome_source", biomeSource);
		result.put("generator", generator);
		return result;
	}

	// TODO: These should be configurable
	private static CompoundNBT createDefaultNether(long seed) {
		CompoundNBT result = new CompoundNBT();
		result.putString("type", "minecraft:the_nether");
		CompoundNBT generator = new CompoundNBT();
		generator.putLong("seed", seed);
		generator.putString("settings", "minecraft:nether");
		generator.putString("type", "minecraft:noise");
		CompoundNBT biomeSource = new CompoundNBT();
		biomeSource.putLong("seed", seed);
		biomeSource.putString("preset", "minecraft:nether");
		biomeSource.putString("type", "minecraft:multi_noise");
		generator.put("biome_source", biomeSource);
		result.put("generator", generator);
		return result;
	}

	private static CompoundNBT createDefaultEnd(long seed) {
		CompoundNBT result = new CompoundNBT();
		result.putString("type", "minecraft:the_end");
		CompoundNBT generator = new CompoundNBT();
		generator.putLong("seed", seed);
		generator.putString("settings", "minecraft:end");
		generator.putString("type", "minecraft:noise");
		CompoundNBT biomeSource = new CompoundNBT();
		biomeSource.putLong("seed", seed);
		biomeSource.putString("type", "minecraft:the_end");
		generator.put("biome_source", biomeSource);
		result.put("generator", generator);
		return result;
	}
}
