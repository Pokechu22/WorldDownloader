/*
 * This file is from the documentation for LaunchWrapperTestSuite:
 * https://github.com/SpongePowered/LaunchWrapperTestSuite.
 */
// NOTE: the package for the tweaker is excluded from tweaks, so it must be in a different one
package wdl.lwts;


import net.minecraft.launchwrapper.LaunchClassLoader;

class WDLTestTweakerBase extends AbstractTestTweaker {

	@Override
	public void injectIntoClassLoader(LaunchClassLoader loader) {
		// Important so we can configure some settings for JUnit
		super.injectIntoClassLoader(loader);

		// Mixin environment
		MixinBootstrap.init();
		Mixins.addConfiguration("mixins.LiteModWDL.json");
		// Set Mixin side, otherwise you get a warning when running the tests
		MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
	}

}
