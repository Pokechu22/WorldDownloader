/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2020 Pokechu22, julialy
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
import static org.mockito.Mockito.*;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerPlayer;

import org.junit.Test;

public class ReflectionUtilsTest extends MaybeMixinTest {

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
		EntityPlayer player = mock(EntityPlayer.class);
		player.inventory = mock(InventoryPlayer.class);

		// OK, actually test
		Container creativeInventory = new GuiContainerCreative(player).container;

		assertTrue(ReflectionUtils.isCreativeContainer(creativeInventory.getClass()));
		assertFalse(ReflectionUtils.isCreativeContainer(ContainerPlayer.class));
		assertFalse(ReflectionUtils.isCreativeContainer(ContainerChest.class));
	}
}
