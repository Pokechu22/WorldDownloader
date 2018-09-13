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
package wdl.versioned;

import java.io.IOException;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateFlatWorld;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiCustomizeWorldScreen;
import net.minecraft.client.gui.GuiFlatPresets;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Biomes;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.biome.Biome;
import wdl.config.settings.GeneratorSettings.Generator;

final class GeneratorFunctions {
	private GeneratorFunctions() { throw new AssertionError(); }
	private static final Logger LOGGER = LogManager.getLogger();

	/* (non-javadoc)
	 * @see VersionedFunctions#isAvailableGenerator
	 */
	static boolean isAvaliableGenerator(Generator generator) {
		return generator != Generator.BUFFET;
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#makeGeneratorSettingsGui
	 */
	static GuiScreen makeGeneratorSettingsGui(Generator generator, GuiScreen parent,
			String generatorConfig, Consumer<String> callback) {
		switch (generator) {
		case FLAT:
			return new GuiFlatPresets(new GuiCreateFlatWorldProxy(parent, generatorConfig, callback));
		case CUSTOMIZED:
			return new GuiCustomizeWorldScreen(new GuiCreateWorldProxy(parent, generatorConfig, callback), generatorConfig);
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
	private static class GuiCreateFlatWorldProxy extends GuiCreateFlatWorld {
		private final GuiScreen parent;
		private final String generatorConfig;
		private final Consumer<String> callback;

		public GuiCreateFlatWorldProxy(GuiScreen parent, String generatorConfig, Consumer<String> callback) {
			super(null, generatorConfig);
			this.parent = parent;
			this.generatorConfig = generatorConfig;
			this.callback = callback;
		}

		@Override
		public void initGui() {
			mc.displayGuiScreen(parent);
		}

		@Override
		protected void actionPerformed(GuiButton button) throws IOException {
			// Do nothing
		}

		@Override
		public void render(int mouseX, int mouseY, float partialTicks) {
			// Do nothing
		}

		/**
		 * Gets the current flat preset.
		 */
		@Override
		public String getPreset() {
			return generatorConfig;
		}

		/**
		 * Sets the current flat preset.
		 */
		@Override
		public void setPreset(@Nullable String preset) {
			callback.accept(preset == null ? "" : preset);
		}
	}

	/**
	 * Fake implementation of {@link GuiCreateWorld} that allows use of
	 * {@link GuiCustomizeWorldScreen}.  Doesn't actually do anything; just passed in
	 * to the constructor to forward the information we need and to switch
	 * back to the main GUI afterwards.
	 */
	private static class GuiCreateWorldProxy extends GuiCreateWorld {
		private final GuiScreen parent;
		private final Consumer<String> callback;

		public GuiCreateWorldProxy(GuiScreen parent, String generatorConfig, Consumer<String> callback) {
			super(parent);

			this.parent = parent;
			this.callback = callback;

			this.chunkProviderSettingsJson = generatorConfig;
		}

		@Override
		public void initGui() {
			callback.accept(this.chunkProviderSettingsJson);
			mc.displayGuiScreen(this.parent);
		}

		@Override
		protected void actionPerformed(GuiButton button) throws IOException {
			// Do nothing
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
		// No toasts in this version
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#VOID_FLAT_PRESET
	 */
	static final String VOID_FLAT_PRESET = "3;minecraft:air;127";

	static {
		// Make sure that the void biome exists
		// (this check partially exists so that this class will not compile in versions without it)
		if (Biome.getIdForBiome(Biomes.THE_VOID) != 127) {
			LOGGER.warn("[WDL] Mismatched ID for void biome: " + Biomes.THE_VOID + " = " + Biome.getIdForBiome(Biomes.THE_VOID));
		}
	}

	/* (non-javadoc)
	 * @see GeneratorFunctions#createGeneratorOptionsTag
	 */
	public static NBTTagString createGeneratorOptionsTag(String generatorOptions) {
		return new NBTTagString(generatorOptions);
	}
}
