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
		EntityPlayer player = mock(EntityPlayer.class);
		player.inventory = mock(InventoryPlayer.class);

		// OK, actually test
		Container creativeInventory = new GuiContainerCreative(player).inventorySlots;

		assertTrue(ReflectionUtils.isCreativeContainer(creativeInventory.getClass()));
		assertFalse(ReflectionUtils.isCreativeContainer(ContainerPlayer.class));
		assertFalse(ReflectionUtils.isCreativeContainer(ContainerChest.class));
	}
}
