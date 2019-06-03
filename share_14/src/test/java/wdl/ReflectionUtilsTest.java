/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;

public class ReflectionUtilsTest {

	@Test
	public void testPrimitives() {
		@SuppressWarnings("unused")
		class Test {
			private int primitive;
			private Integer boxed;
		}

		assertThat(ReflectionUtils.findField(Test.class, int.class).getName(), is("primitive"));
		assertThat(ReflectionUtils.findField(Test.class, Integer.class).getName(), is("boxed"));
	}

	@Test
	public void testIsCreativeContainer() {
		// Need a player to create a creative inventory (not the best system)
		PlayerEntity player = mock(PlayerEntity.class);
		//player.inventory = mock(PlayerInventory.class);

		// OK, actually test
		Container creativeInventory = new CreativeScreen(player).func_212873_a_();

		assertTrue(ReflectionUtils.isCreativeContainer(creativeInventory.getClass()));
		assertFalse(ReflectionUtils.isCreativeContainer(PlayerContainer.class));
		assertFalse(ReflectionUtils.isCreativeContainer(ChestContainer.class));
	}
}
