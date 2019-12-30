/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.handler.block;

import static wdl.versioned.VersionedFunctions.customName;

import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import net.minecraft.inventory.ContainerChest;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockReader;
import wdl.handler.HandlerException;

/**
 * Contains shared logic used by both chests and trapped chests.
 */
public class BaseChestHandler<B extends TileEntityChest> extends BaseLargeChestHandler<B> {
	public BaseChestHandler(Class<B> blockEntityClass, String... defaultNames) {
		super(blockEntityClass, ContainerChest.class, defaultNames);
	}

	@Override
	public ITextComponent handle(BlockPos clickedPos, ContainerChest container,
			B blockEntity, IBlockReader world,
			BiConsumer<BlockPos, B> saveMethod) throws HandlerException {
		String title = getCustomDisplayName(container.getLowerChestInventory());

		if (container.inventorySlots.size() > 63) {
			saveDoubleChest(clickedPos, container, blockEntity, world, saveMethod, title);
			return new TextComponentTranslation("wdl.messages.onGuiClosedInfo.savedTileEntity.doubleChest");
		} else {
			saveSingleChest(clickedPos, container, blockEntity, world, saveMethod, title);
			return new TextComponentTranslation("wdl.messages.onGuiClosedInfo.savedTileEntity.singleChest");
		}
	}
	/**
	 * Saves the contents of a single chest.
	 *
	 * @param clickedPos As per {@link #handle}
	 * @param container As per {@link #handle}
	 * @param blockEntity As per {@link #handle}
	 * @param world As per {@link #handle}
	 * @param saveMethod As per {@link #handle}
	 * @param displayName The custom name of the chest, or <code>null</code> if none is set.
	 * @throws HandlerException As per {@link #handle}
	 */
	private void saveSingleChest(BlockPos clickedPos, ContainerChest container,
			B blockEntity, IBlockReader world,
			BiConsumer<BlockPos, B> saveMethod,
			@Nullable String displayName) throws HandlerException {
		saveContainerItems(container, blockEntity, 0);
		if (displayName != null) {
			blockEntity.setCustomName(customName(displayName));
		}
		saveMethod.accept(clickedPos, blockEntity);
	}
}
