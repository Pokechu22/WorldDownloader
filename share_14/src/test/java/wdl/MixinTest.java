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
package wdl;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.spongepowered.lwts.runner.DelegateRunner.DelegatedRunWith;
import org.spongepowered.lwts.runner.LaunchWrapperDelegateRunner;

/**
 * This is a more or less empty class that is used to specify the runner that
 * JUnit should use.
 *
 * The only purpose is to make use of the {@link RunWith @RunWith} annotation,
 * which is inherited into subclasses.
 *
 * Subclasses can use {@link DelegatedRunWith @DelegatedRunWith} to specify a
 * different runner.
 */
@RunWith(LaunchWrapperDelegateRunner.class)
@DelegatedRunWith(JUnit4.class)
abstract class MaybeMixinTestBase {
}
