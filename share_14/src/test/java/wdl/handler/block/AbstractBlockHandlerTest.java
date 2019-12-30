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
package wdl.handler.block;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import wdl.handler.AbstractWorldBehaviorTest;
import wdl.handler.HandlerException;
import wdl.versioned.VersionedFunctions;

/**
 * Test for block entity handlers.
 *
 * @param <B> The type of block entity to handle.
 * @param <C> The type of container associated with that block entity.
 * @param <H> The block handler that handles both of those things.
 */
public abstract class AbstractBlockHandlerTest<B extends TileEntity, C extends Container, H extends BlockHandler<B, C>>
		extends AbstractWorldBehaviorTest {

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
	}

	protected final Class<B> blockEntityClass;
	protected final Class<C> containerClass;
	protected final Class<H> handlerClass;

	/** A set containing all original TEs. */
	private Set<BlockPos> origTEPoses;
	/** A map of block entities for the user to save into. */
	protected Map<BlockPos, TileEntity> tileEntities;

	/**
	 * Verifies that the handler is registered, preemptively.
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
	}

	/**
	 * Creates a new block entity at the given position and returns the server-sided
	 * version, for modification. The client-side one will be the default block
	 * entity for that block.
	 *
	 * @param pos The position
	 * @param     <T> The type of block entity at the position, automatically
	 *            inferred.  Calling code will throw an exception if it doesn't match.
	 *
	 * @return The server-sided block entity at that position.
	 */
	@SuppressWarnings("unchecked")
	protected <T extends TileEntity> T makeBlockEntity(BlockPos pos) {
		origTEPoses.add(pos);

		BlockState serverState = clientWorld.getBlockState(pos);
		ContainerBlock serverBlock = (ContainerBlock)(serverState.getBlock());
		TileEntity serverBE = VersionedFunctions.createNewBlockEntity(clientWorld, serverBlock, serverState);

		BlockState clientState = clientWorld.getBlockState(pos);
		ContainerBlock clientBlock = (ContainerBlock)(clientState.getBlock());
		TileEntity clientBE = VersionedFunctions.createNewBlockEntity(clientWorld, clientBlock, clientState);

		assertNotNull(clientBE);
		assertNotNull(serverBE);

		serverWorld.setTileEntity(pos, serverBE);
		clientWorld.setTileEntity(pos, clientBE);

		return (T)serverBE;
	}

	/**
	 * Makes a container as the client would have.
	 *
	 * @param pos Position of the container to open
	 */
	protected Container makeClientContainer(BlockPos pos) {
		serverPlayer.closeScreen();
		assertSame(clientPlayer.openContainer, clientPlayer.container);
		assertSame(serverPlayer.openContainer, serverPlayer.container);
		serverWorld.interactBlock(pos, serverPlayer);
		assertNotNull(clientPlayer.openContainer);
		assertNotNull(serverPlayer.openContainer);
		serverPlayer.sendContainerToPlayer(serverPlayer.openContainer);

		return clientPlayer.openContainer;
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
		BlockHandler<?, ?> handler = BlockHandler.getHandler(te.getClass(), container.getClass());
		assertThat("Unexpected null handler",
				handler, is(notNullValue()));
		assertThat("Unexpected handler; should be the one tested by this class",
				handler, is(instanceOf(handlerClass)));
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

			assertSameNBT(serverTE, savedTE);
		}
	}

	/**
	 * Helper to call {@link #assertSameNBT(NBTTagCompound, NBTTagCompound)
	 *
	 * @param expected Block entity with expected NBT (the server entity)
	 * @param actual Block entity with the actual NBT (the client entity)
	 */
	protected void assertSameNBT(TileEntity expected, TileEntity actual) {
		// Can't call these methods directly because writeToNBT returns void in 1.9
		CompoundNBT expectedNBT = new CompoundNBT();
		expected.write(expectedNBT);
		CompoundNBT actualNBT = new CompoundNBT();
		actual.write(actualNBT);
		assertSameNBT(expectedNBT, actualNBT);
	}

	@Override
	public void resetState() {
		super.resetState();
		origTEPoses = null;
		tileEntities = null;
	}
}
