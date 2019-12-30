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

import java.util.function.BiConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import wdl.handler.BaseHandler;
import wdl.handler.HandlerException;
import wdl.versioned.VersionedFunctions;

/**
 * A handler for block actions. See {@linkplain wdl.handler.blockaction the
 * package} for more info.
 *
 * @param <B> The type of block to handle.
 * @param <E> The type of block entity to handle.
 */
public abstract class BlockActionHandler<B extends Block, E extends TileEntity> extends BaseHandler {
	/**
	 * Constructor.
	 *
	 * @param blockClass
	 *            A strong reference to the block class this handles.
	 * @param blockEntityClass
	 *            A strong reference to the block entity class this handles.
	 */
	protected BlockActionHandler(Class<B> blockClass, Class<E> blockEntityClass) {
		this.blockClass = blockClass;
		this.blockEntityClass = blockEntityClass;
	}
	protected final @Nonnull Class<B> blockClass;
	protected final @Nonnull Class<E> blockEntityClass;

	/** Gets the type of block handled by this. */
	public final Class<B> getBlockClass() {
		return blockClass;
	}
	/** Gets the type of block entity handled by this. */
	public final Class<E> getBlockEntityClass() {
		return blockEntityClass;
	}

	/**
	 * Updates a block entity from the given block action.
	 *
	 * @param pos
	 *            The position of the block action.
	 * @param block
	 *            The block at the given position.  An instance of <code>B</code>.
	 * @param blockEntity
	 *            The block entity at the given position. Must be an instance of
	 *            <code>E</code>.
	 * @param data1
	 *            The first parameter in the packet (what wiki.vg calls the action ID)
	 * @param data2
	 *            The second parameter in the packet (what wiki.vg calls the action param)
	 * @param world
	 *            The world to query if more information is needed.
	 * @param saveMethod
	 *            The method to call to save block entities.
	 * @return A message to put into chat describing what was saved.
	 * @throws HandlerException
	 *             When something is handled wrong.
	 * @throws ClassCastException
	 *             If block or blockEntity are not instances of the handled class.
	 */
	public final ITextComponent handleCasting(BlockPos pos, Block block,
			TileEntity blockEntity, int data1, int data2, IBlockReader world,
			BiConsumer<BlockPos, E> saveMethod) throws HandlerException, ClassCastException {
		B b = blockClass.cast(block);
		E e = blockEntityClass.cast(blockEntity);
		return handle(pos, b, e, data1, data2, world, saveMethod);
	}

	/**
	 * Saves the contents of a block entity from the container.
	 *
	 * @param pos
	 *            The position of the event.
	 * @param block
	 *            The block at the given position.
	 * @param blockEntity
	 *            The block entity at the given position.
	 * @param data1
	 *            The first parameter in the packet (what wiki.vg calls the action ID)
	 * @param data2
	 *            The second parameter in the packet (what wiki.vg calls the action param)
	 * @param world
	 *            The world to query if more information is needed.
	 * @param saveMethod
	 *            The method to call to save block entities.
	 * @return A message to put into chat describing what was saved.
	 * @throws HandlerException
	 *             When something is handled wrong.
	 */
	public abstract ITextComponent handle(BlockPos pos, B block,
			E blockEntity, int data1, int data2, IBlockReader world,
			BiConsumer<BlockPos, E> saveMethod) throws HandlerException;

	/**
	 * Looks up the handler that handles the given block/block entity combo,
	 * from {@link VersionedFunctions#BLOCK_ACTION_HANDLERS}.
	 *
	 * @param blockClass The type for the block.
	 * @param blockEntityClass The type for the block entity.
	 * @return The handler, or null if none is found.
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public static <B extends Block, E extends TileEntity> BlockActionHandler<B, E> getHandler(Class<B> blockClass, Class<E> blockEntityClass) {
		for (BlockActionHandler<?, ?> h : VersionedFunctions.BLOCK_ACTION_HANDLERS) {
			if (h.getBlockEntityClass().equals(blockEntityClass) &&
					h.getBlockClass().equals(blockClass)) {
				return (BlockActionHandler<B, E>)h;
			}
		}

		return null;
	}
}
