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
package wdl.handler.block;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matcher;
import org.junit.Test;

import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.player.inventory.ContainerLocalMenu;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IInteractionObject;
import wdl.VersionedProperties;
import wdl.handler.AbstractWorldBehaviorTest;
import wdl.handler.HandlerException;

/**
 * A handler for an arbitrary block entity.
 *
 * @param <B> The type of block entity to handle.
 * @param <C> The type of container associated with that block entity.
 * @param <H> The block handler that handles both of those things.
 */
public abstract class AbstractBlockHandlerTest<B extends TileEntity, C extends Container, H extends BlockHandler<B, C>>
		extends AbstractWorldBehaviorTest {

	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Constructor.
	 *
	 * @param blockEntityClass
	 *            A strong reference to the block entity class that is handled by
	 *            the handler.
	 * @param containerClass
	 *            A strong reference to the container class that is handled by the
	 *            handler.
	 * @param handlerClass
	 *            A strong reference to the handler's class.
	 */
	protected AbstractBlockHandlerTest(Class<B> blockEntityClass, Class<C> containerClass, Class<H> handlerClass) {
		this.blockEntityClass = blockEntityClass;
		this.containerClass = containerClass;
		this.handlerClass = handlerClass;

		try {
			// TODO: may in the future want to have other constructors, which
			// wouldn't work with this
			this.handler = handlerClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	protected final Class<B> blockEntityClass;
	protected final Class<C> containerClass;
	protected final Class<H> handlerClass;

	/**
	 * Handler under test.  Will be a new object, not the handler registered in
	 * {@link VersionedProperties}.
	 */
	protected final H handler;

	/** A set containing all original TEs. */
	private Set<BlockPos> origTEPoses;
	/** A map of block entities for the user to save into. */
	protected Map<BlockPos, TileEntity> tileEntities;

	/**
	 * Verifies that the handler is registered.
	 *
	 * Note that this does not actually use the {@link #handler} instance.
	 */
	@Test
	public final void testHandlerExists() {
		BlockHandler<B, C> handler = BlockHandler.getHandler(blockEntityClass, containerClass);

		assertThat(handler, is(notNullValue()));
		assertThat(handler, is(instanceOf(handlerClass)));
		assertThat(handler.getBlockEntityClass(), is(equalTo(blockEntityClass)));
		assertThat(handler.getContainerClass(), is(equalTo(containerClass)));
	}

	@Override
	protected void makeMockWorld() {
		super.makeMockWorld();

		tileEntities = new HashMap<>();
		origTEPoses = new HashSet<>();
		when(clientWorld.getTileEntity(any())).thenReturn(null);
		when(serverWorld.getTileEntity(any())).thenReturn(null);
	}

	/**
	 * Puts the given block entity into the mock worlds at the given position.
	 * The server world gets the exact block entity; the client gets a default one.
	 *
	 * @param pos The position
	 * @param te The block entity to put
	 */
	protected void placeTEAt(BlockPos pos, TileEntity te) {
		origTEPoses.add(pos);

		IBlockState curState = clientWorld.getBlockState(pos);
		BlockContainer curBlock = (BlockContainer)(curState.getBlock());
		TileEntity defaultAtPos = curBlock.createNewTileEntity(clientWorld, curBlock.getMetaFromState(curState));
		defaultAtPos.setWorld(clientWorld);
		defaultAtPos.setPos(pos);

		when(clientWorld.getTileEntity(pos)).thenReturn(defaultAtPos);
		when(serverWorld.getTileEntity(pos)).thenReturn(te);
		te.setWorld(serverWorld);
		te.setPos(pos);
	}

	/**
	 * Makes a container as the client would have.
	 *
	 * @param pos Position of the container to open
	 */
	protected Container makeClientContainer(BlockPos pos) {
		// This is a bit of a mess, but inventories are a bit of a mess.
		// First, prepare an IInventory as the server would see it:
		TileEntity serverTE = serverWorld.getTileEntity(pos);
		// Preconditions for this to make sense
		assertNotNull(serverTE);
		assertTrue(serverTE instanceof IInventory);

		IInventory serverInv;
		if (serverWorld.getBlockState(pos).getBlock() instanceof BlockChest) {
			// Special-casing for large chests
			BlockChest chest = (BlockChest) serverWorld.getBlockState(pos).getBlock();
			serverInv = chest.getLockableContainer(serverWorld, pos);
		} else {
			serverInv = (IInventory) serverTE;
		}
		String guiID = ((IInteractionObject) serverTE).getGuiID();

		// Now do stuff as the client would see it...
		// NetHandlerPlayClient.handleOpenWindow(SPacketOpenWindow)
		IInventory clientInv;
		if (guiID.equals("minecraft:container")) {
			// Ender chest -- don't know if we can actually get here
			clientInv = new InventoryBasic(serverInv.getDisplayName(),
					serverInv.getSizeInventory());
		} else { // skip a few
			clientInv = new ContainerLocalMenu(guiID, serverInv.getDisplayName(),
					serverInv.getSizeInventory());
		}
		// Copy items and fields (this normally happens later, but whatever)
		for (int i = 0; i < serverInv.getSizeInventory(); i++) {
			ItemStack serverItem = serverInv.getStackInSlot(i);
			if (serverItem == null) {
				// In older versions with nullable items
				continue;
			}
			clientInv.setInventorySlotContents(i, serverItem.copy());
		}
		for (int i = 0; i < serverInv.getFieldCount(); i++) {
			clientInv.setField(i, serverInv.getField(i));
		}

		Container container = makeContainer(guiID, player, clientInv);
		if (container == null) {
			// Unknown -- i.e. minecraft:container
			LOGGER.warn("Unknown container type {} for {} at {}", guiID, serverTE, pos);
			return new ContainerChest(player.inventory, clientInv, player);
		} else {
			return container;
		}
	}

	/**
	 * Runs the handler, performing tile entity lookup and casting.
	 *
	 * @param pos The position to check
	 * @param container The container to use
	 * @throws HandlerException when the handler does
	 */
	protected void runHandler(BlockPos pos, Container container) throws HandlerException {
		TileEntity te = clientWorld.getTileEntity(pos);
		handler.handleCasting(pos, container, te, clientWorld, tileEntities::put);
	}

	/**
	 * Checks that the saved world matches the original.
	 */
	protected void checkAllTEs() {
		assertThat("Must save all TEs", tileEntities.keySet(), is(origTEPoses));

		for (BlockPos pos : origTEPoses) {
			TileEntity serverTE = serverWorld.getTileEntity(pos);
			TileEntity savedTE = tileEntities.get(pos);

			assertThat(savedTE, hasSameNBTAs(serverTE));
		}
	}

	protected static class HasSameBlockEntityNBT extends HasSameNBT<TileEntity> {
		public HasSameBlockEntityNBT(TileEntity te) {
			super(te, "block entity");
		}

		@Override
		protected NBTTagCompound getNBT(TileEntity te) {
			NBTTagCompound tag = new NBTTagCompound();
			te.writeToNBT(tag);
			return tag;
		}
	}

	protected static Matcher<TileEntity> hasSameNBTAs(TileEntity serverTE) {
		return new HasSameBlockEntityNBT(serverTE);
	}
}
