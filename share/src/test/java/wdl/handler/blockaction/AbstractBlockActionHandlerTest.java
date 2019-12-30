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
package wdl.handler.blockaction;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import wdl.handler.AbstractWorldBehaviorTest;
import wdl.handler.HandlerException;
import wdl.versioned.VersionedFunctions;

/**
 * A test for block actions. See {@linkplain wdl.handler.blockaction the
 * package} for more info.
 *
 * @param <B> The type of block to handle.
 * @param <E> The type of block entity to handle.
 * @param <H> The type of the handler.
 */
public abstract class AbstractBlockActionHandlerTest<B extends Block, E extends TileEntity, H extends BlockActionHandler<B, E>>
		extends AbstractWorldBehaviorTest {

	/**
	 * Constructor.
	 *
	 * @param blockClass
	 *            A strong reference to the block class that is handled by
	 *            the handler.
	 * @param blockEntityClass
	 *            A strong reference to the block entity class that is handled by
	 *            the handler.
	 * @param handlerClass
	 *            A strong reference to the handler's class.
	 */
	protected AbstractBlockActionHandlerTest(Class<B> blockClass, Class<E> blockEntityClass, Class<H> handlerClass) {
		this.blockClass = blockClass;
		this.blockEntityClass = blockEntityClass;
		this.handlerClass = handlerClass;

		try {
			// TODO: may in the future want to have other constructors, which
			// wouldn't work with this
			this.handler = handlerClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	protected final Class<B> blockClass;
	protected final Class<E> blockEntityClass;
	protected final Class<H> handlerClass;

	/**
	 * Handler under test.  Will be a new object, not the handler registered in
	 * {@link VersionedFunctions}.
	 */
	protected final H handler;

	/** Block actions that were triggered on the server world. */
	private Map<BlockPos, BlockEventData> blockActions;
	/** A map of block entities for the user to save into. */
	protected Map<BlockPos, TileEntity> tileEntities;

	@Override
	protected void makeMockWorld() {
		super.makeMockWorld();
		blockActions = new HashMap<>();
		tileEntities = new HashMap<>();
		serverWorld.onBlockEvent = (e -> this.blockActions.put(e.getPosition(), e));
	}

	/**
	 * Verifies that the handler is registered.
	 *
	 * Note that this does not actually use the {@link #handler} instance.
	 */
	@Test
	public final void testHandlerExists() {
		BlockActionHandler<B, E> handler = BlockActionHandler.getHandler(blockClass, blockEntityClass);

		assertThat(handler, is(notNullValue()));
		assertThat(handler, is(instanceOf(handlerClass)));
		assertThat(handler.getBlockClass(), is(equalTo(blockClass)));
		assertThat(handler.getBlockEntityClass(), is(equalTo(blockEntityClass)));
	}

	/**
	 * Runs the handler, performing tile entity lookup and casting.
	 *
	 * @param pos The position to check
	 * @throws HandlerException when the handler does
	 */
	protected void runHandler(BlockPos pos) throws HandlerException {
		assertThat(blockActions, hasKey(pos));
		BlockEventData data = blockActions.remove(pos);
		TileEntity te = clientWorld.getTileEntity(pos);
		handler.handleCasting(pos, data.getBlock(), te, data.getEventID(),
				data.getEventParameter(), clientWorld, tileEntities::put);
	}

	/**
	 * Checks that the saved world matches the original.
	 */
	protected void checkAllTEs() {
		assertThat("Must use all block actions", blockActions.keySet(), is(empty()));

		for (BlockPos pos : tileEntities.keySet()) {
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
		NBTTagCompound expectedNBT = new NBTTagCompound();
		expected.write(expectedNBT);
		NBTTagCompound actualNBT = new NBTTagCompound();
		actual.write(actualNBT);
		assertSameNBT(expectedNBT, actualNBT);
	}

	@Override
	public void resetState() {
		super.resetState();
		blockActions = null;
		tileEntities = null;
	}
}
