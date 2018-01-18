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
package wdl.handler;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.BeforeClass;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.player.inventory.ContainerLocalMenu;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import wdl.ReflectionUtils;
import wdl.handler.block.BlockHandler;
import wdl.handler.block.BlockHandler.HandlerException;

/**
 * Base logic shared between all tests that use blocks.
 *
 * Ensures that the {@link Bootstrap} is initialized, so that classes such as
 * {@link Blocks} can be used.
 */
public abstract class AbstractWorldBehaviorTest {
	private static final Logger LOGGER = LogManager.getLogger();
	/** Handler under test */
	protected BlockHandler<?, ?> handler;
	/** Worlds corresponding to what the server and client know */
	protected World serverWorld, clientWorld;
	/** A player entity.  Has a valid inventory. */
	protected EntityPlayer player = mock(EntityPlayer.class);
	/** A set containing all original TEs. */
	private Set<BlockPos> origTEPoses;
	/** A map of block entities for the user to save into. */
	protected Map<BlockPos, TileEntity> tileEntities;

	@BeforeClass
	public static void initBootstarp() {
		if (Bootstrap.isRegistered()) {
			LOGGER.warn("Bootstrap already initialized.");
			return;
		}
		LOGGER.debug("Initializing bootstrap...");
		Bootstrap.register();
		LOGGER.debug("Initialized bootstrap.");
		// Note: not checking Bootstrap.hasErrored as that didn't exist in older
		// versions

		LOGGER.debug("Setting up I18n...");
		initI18n();
		LOGGER.debug("Set up I18n.");
	}

	/**
	 * Called by JUnit; sets {@link #handler}.
	 */
	@Before
	public final void prepareHandler() {
		this.handler = makeHandler();
	}

	/**
	 * Creates a handler instance.
	 */
	protected abstract BlockHandler<?, ?> makeHandler();

	/**
	 * Prepares a fake Locale instance for I18n.
	 */
	private static void initI18n() {
		@SuppressWarnings("deprecation")
		class FakeLocale extends Locale {
			@Override
			public String formatMessage(String translateKey, Object[] parameters) {
				return net.minecraft.util.text.translation.I18n.translateToLocalFormatted(translateKey, parameters);
			}
			@Override
			public boolean hasKey(String key) {
				return net.minecraft.util.text.translation.I18n.canTranslate(key);
			}
		}
		ReflectionUtils.findAndSetPrivateField(I18n.class, Locale.class, new FakeLocale());
	}

	/**
	 * Creates a mock world, returning air for blocks and null for TEs.
	 */
	protected void makeMockWorld() {
		clientWorld = mock(World.class);
		serverWorld = mock(World.class);
		tileEntities = new HashMap<>();
		origTEPoses = new HashSet<>();

		when(clientWorld.getBlockState(any())).thenReturn(Blocks.AIR.getDefaultState());
		when(serverWorld.getBlockState(any())).thenReturn(Blocks.AIR.getDefaultState());
		when(clientWorld.getTileEntity(any())).thenReturn(null);
		when(serverWorld.getTileEntity(any())).thenReturn(null);
	}

	/**
	 * Puts the given block into the mock worlds at the given position.
	 *
	 * @param pos The position
	 * @param block The block to put
	 */
	protected void placeBlockAt(BlockPos pos, Block block) {
		placeBlockAt(pos, block.getDefaultState());
	}

	/**
	 * Puts the given block into the mock worlds at the given position.
	 *
	 * @param pos The position
	 * @param block The block to put
	 */
	protected void placeBlockAt(BlockPos pos, IBlockState state) {
		when(clientWorld.getBlockState(pos)).thenReturn(state);
		when(serverWorld.getBlockState(pos)).thenReturn(state);
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

		// EntityPlayerSP.displayGUIChest(IInventory) found in handleOpenWindow
		// melded with the container code in each GUI
		Container container;
		if ("minecraft:chest".equals(guiID)) {
			container = new ContainerChest(player.inventory, clientInv, player);
		} else if ("minecraft:hopper".equals(guiID)) {
			container = new ContainerHopper(player.inventory, clientInv, player);
		} else if ("minecraft:furnace".equals(guiID)) {
			container = new ContainerFurnace(player.inventory, clientInv);
		} else if ("minecraft:brewing_stand".equals(guiID)) {
			container = new ContainerBrewingStand(player.inventory, clientInv);
		} else if ("minecraft:beacon".equals(guiID)) {
			container = new ContainerBeacon(player.inventory, clientInv);
		} else if ("minecraft:dispenser".equals(guiID) && "minecraft:dropper".equals(guiID)) {
			container = new ContainerDispenser(player.inventory, clientInv);
		// } else if ("minecraft:shulker_box".equals(guiID)) {
		//     container = new ContainerShulkerBox(player.inventory, clientInv, player);
		} else {
			// Unknown -- i.e. minecraft:container
			container = new ContainerChest(player.inventory, clientInv, player);
		}

		return container;
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

	protected static class HasSameNBT extends TypeSafeMatcher<TileEntity> {
		private final TileEntity serverTE;

		public HasSameNBT(TileEntity serverTE) {
			this.serverTE = serverTE;
		}

		@Override
		protected boolean matchesSafely(@Nonnull TileEntity te) {
			return getNBT(serverTE).equals(getNBT(te));
		}

		@Override
		public void describeTo(@Nonnull Description description) {
			description.appendText("a block entity that was equal to ").appendValue(serverTE)
					.appendText(" with this NBT ").appendValue(getNBT(serverTE));
		}

		@Override
		protected void describeMismatchSafely(@Nonnull TileEntity te,
				@Nonnull Description mismatchDescription) {
			mismatchDescription.appendText("was ").appendValue(getNBT(te))
					.appendText(" (te: ").appendValue(te).appendText(")");
		}

		private NBTTagCompound getNBT(TileEntity te) {
			NBTTagCompound tag = new NBTTagCompound();
			te.writeToNBT(tag);
			return tag;
		}
	}

	protected static Matcher<TileEntity> hasSameNBTAs(TileEntity serverTE) {
		return new HasSameNBT(serverTE);
	}
}
