/*
 * This file is from the documentation for LaunchWrapperTestSuite:
 * https://github.com/SpongePowered/LaunchWrapperTestSuite.
 */
// NOTE: the package for the tweaker is excluded from tweaks, so it must be in a different one
package wdl.lwts;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.lwts.AbstractTestTweaker;

import net.minecraft.launchwrapper.LaunchClassLoader;

class WDLTestTweakerBase extends AbstractTestTweaker {

	@Override
	public void injectIntoClassLoader(LaunchClassLoader loader) {
		// Important so we can configure some settings for JUnit
		super.injectIntoClassLoader(loader);

		// Mixin environment
		MixinBootstrap.init();
		Mixins.addConfiguration("mixins.ForgeWDL.json");
		// Set Mixin side, otherwise you get a warning when running the tests
		MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
	}

}
