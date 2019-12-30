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

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * This is a more or less empty class that is used to specify the runner that
 * JUnit should use, for tests that rely upon mixins or base changes.  It also
 * initializes the bootstrap, and sets up language stuff.
 *
 * The only purpose is to make use of the {@link RunWith @RunWith} annotation,
 * which is inherited into subclasses.
 *
 * This version specifically is responsible for making the class public.
 */
public abstract class MaybeMixinTest extends MaybeMixinTestBase {
	// This method needs to exist because junit can't call methods from non-public classes,
	// even if inherited via a public class
	@BeforeClass
	public static void init() {
		MaybeMixinTestBase.init0();
	}
}
