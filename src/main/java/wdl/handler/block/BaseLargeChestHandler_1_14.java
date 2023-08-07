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

import static wdl.versioned.VersionedFunctions.*;

import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import wdl.WDLMessageTypes;
import wdl.handler.HandlerException;

/**
 * This class specifically provides support for handling large chests, which
 * behaves differently between versions.
 *
 * This version simply uses information in the block state, which is now available in 1.13.
 *
 * It is also important to note that sometimes, this logic is flipped from the behavior in 1.12,
 * as chests now are rotated properly.
 */
abstract class BaseLargeChestHandler<B extends ChestTileEntity> extends BlockHandler<B, ChestContainer> {
	protected BaseLargeChestHandler(Class<B> blockEntityClass, Class<ChestContainer> containerClass,
			String... defaultNames) {
		super(blockEntityClass, containerClass, defaultNames);
	}

	/**
	 * Saves the contents of a double-chest, first identifying the location of both
	 * chests.
	 *
	 * @param clickedPos As per {@link #handle}
	 * @param container As per {@link #handle}
	 * @param blockEntity As per {@link #handle}
	 * @param world As per {@link #handle}
	 * @param saveMethod As per {@link #handle}
	 * @param displayName The custom name of the chest, or <code>null</code> if none is set.
	 * @throws HandlerException As per {@link #handle}
	 */
	protected void saveDoubleChest(BlockPos clickedPos, ChestContainer container,
			B blockEntity, IBlockReader world,
			BiConsumer<BlockPos, B> saveMethod,
			@Nullable String displayName) throws HandlerException {
		BlockState state = world.getBlockState(clickedPos);
		ChestType type = state.get(ChestBlock.TYPE);

		boolean right;
		switch (type) {
		case LEFT: right = false; break;
		case RIGHT: right = true; break;
		default: throw new HandlerException("wdl.messages.onGuiClosedWarning.failedToFindDoubleChest", WDLMessageTypes.ERROR);
		}

		BlockPos otherPos = clickedPos.offset(ChestBlock.getDirectionToAttached(state));

		BlockPos chestPos1 = (right ? clickedPos : otherPos);
		BlockPos chestPos2 = (right ? otherPos : clickedPos);
		TileEntity te1 = world.getTileEntity(chestPos1);
		TileEntity te2 = world.getTileEntity(chestPos2);

		if (!(blockEntityClass.isInstance(te1) && blockEntityClass.isInstance(te2))) {
			throw new HandlerException("wdl.messages.onGuiClosedWarning.failedToFindDoubleChest", WDLMessageTypes.ERROR);
		}

		B chest1 = blockEntityClass.cast(te1);
		B chest2 = blockEntityClass.cast(te2);

		saveContainerItems(container, chest1, 0);
		saveContainerItems(container, chest2, 27);

		if (displayName != null) {
			// This is NOT server-accurate.  But making it correct is not easy.
			// Only one of the chests needs to have the name.
			chest1.setCustomName(customName(displayName));
			chest2.setCustomName(customName(displayName));
		}

		saveMethod.accept(chestPos1, chest1);
		saveMethod.accept(chestPos2, chest2);
	}
}
