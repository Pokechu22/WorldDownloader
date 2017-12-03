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
package wdl.handler.block;

import java.util.function.BiConsumer;

import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import wdl.WDLMessageTypes;

public class ChestHandler extends BlockHandler<TileEntityChest, ContainerChest> {
	public ChestHandler() {
		super(TileEntityChest.class, ContainerChest.class);
	}

	@Override
	public String handle(BlockPos clickedPos, ContainerChest container, TileEntityChest blockEntity,
			IBlockAccess world, BiConsumer<BlockPos, TileEntityChest> saveMethod) throws HandlerException {
		if (container.inventorySlots.size() > 63) {
			return saveDoubleChest(clickedPos, container, blockEntity, world, saveMethod);
		} else {
			return saveSingleChest(clickedPos, container, blockEntity, world, saveMethod);
		}
	}
	/**
	 * Saves the contents of a single chest.
	 */
	private String saveSingleChest(BlockPos clickedPos, ContainerChest container, TileEntityChest blockEntity,
			IBlockAccess world, BiConsumer<BlockPos, TileEntityChest> saveMethod) throws HandlerException {
		// Note: It would look like getDisplayName should work
		// and that you'd be able to identify an ITextComponent as either
		// a translation component or a text component, but that'd be wrong
		// due to strange server/client stuff that I haven't fully explored.
		String title = container.getLowerChestInventory().getName();

		saveContainerItems(container, blockEntity, 0);
		if (!title.equals(I18n.format("container.chest"))) {
			// Custom name set
			blockEntity.setCustomName(title);
		}
		saveMethod.accept(clickedPos, blockEntity);
		return "wdl.messages.onGuiClosedInfo.savedTileEntity.singleChest";
	}
	/**
	 * Saves the contents of a double-chest, first identifying the location of both
	 * chests. This method does not handle triple/quadruple/quintuple chests.
	 */
	private String saveDoubleChest(BlockPos clickedPos, ContainerChest container, TileEntityChest blockEntity,
			IBlockAccess world, BiConsumer<BlockPos, TileEntityChest> saveMethod) throws HandlerException {
		// This is messy, but it needs to be like this because
		// the left and right chests must be in the right positions.

		BlockPos pos1, pos2;
		TileEntity te1, te2;

		pos1 = clickedPos;
		te1 = world.getTileEntity(clickedPos);
		assert te1 instanceof TileEntityChest;

		// We need separate variables for the above reason --
		// pos1 isn't always the same as chestPos1 (and thus
		// chest1 isn't always te1).
		BlockPos chestPos1 = null, chestPos2 = null;
		TileEntityChest chest1 = null, chest2 = null;

		pos2 = pos1.add(0, 0, 1);
		te2 = world.getTileEntity(pos2);
		if (te2 instanceof TileEntityChest &&
				((TileEntityChest) te2).getChestType() ==
				((TileEntityChest) te1).getChestType()) {

			chest1 = (TileEntityChest) te1;
			chest2 = (TileEntityChest) te2;

			chestPos1 = pos1;
			chestPos2 = pos2;
		}

		pos2 = pos1.add(0, 0, -1);
		te2 = world.getTileEntity(pos2);
		if (te2 instanceof TileEntityChest &&
				((TileEntityChest) te2).getChestType() ==
				((TileEntityChest) te1).getChestType()) {

			chest1 = (TileEntityChest) te2;
			chest2 = (TileEntityChest) te1;

			chestPos1 = pos2;
			chestPos2 = pos1;
		}

		pos2 = pos1.add(1, 0, 0);
		te2 = world.getTileEntity(pos2);
		if (te2 instanceof TileEntityChest &&
				((TileEntityChest) te2).getChestType() ==
				((TileEntityChest) te1).getChestType()) {
			chest1 = (TileEntityChest) te1;
			chest2 = (TileEntityChest) te2;

			chestPos1 = pos1;
			chestPos2 = pos2;
		}

		pos2 = pos1.add(-1, 0, 0);
		te2 = world.getTileEntity(pos2);
		if (te2 instanceof TileEntityChest &&
				((TileEntityChest) te2).getChestType() ==
				((TileEntityChest) te1).getChestType()) {
			chest1 = (TileEntityChest) te2;
			chest2 = (TileEntityChest) te1;

			chestPos1 = pos2;
			chestPos2 = pos1;
		}

		if (chest1 == null || chest2 == null ||
				chestPos1 == null || chestPos2 == null) {
			throw new HandlerException("wdl.messages.onGuiClosedWarning.failedToFindDoubleChest", WDLMessageTypes.ERROR);
		}

		saveContainerItems(container, chest1, 0);
		saveContainerItems(container, chest2, 27);

		// Custom name stuff:
		// Note: It would look like getDisplayName should work
		// and that you'd be able to identify an ITextComponent as either
		// a translation component or a text component, but that'd be wrong
		// due to strange server/client stuff that I haven't fully explored.
		String title = container.getLowerChestInventory().getName();
		// Due to normal I18n not being available in unit tests
		String expected = new TextComponentTranslation("container.chestDouble").getUnformattedText();
		if (!title.equals(expected)) {
			// This is NOT server-accurate.  But making it correct is not easy.
			// Only one of the chests needs to have the name.
			chest1.setCustomName(title);
			chest2.setCustomName(title);
		}

		saveMethod.accept(chestPos1, chest1);
		saveMethod.accept(chestPos2, chest2);

		return "wdl.messages.onGuiClosedInfo.savedTileEntity.doubleChest";
	}
}
