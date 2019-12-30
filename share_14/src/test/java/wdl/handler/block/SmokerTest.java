/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.handler.block;

import static wdl.versioned.VersionedFunctions.*;

import org.junit.Test;

import net.minecraft.block.Blocks;
import net.minecraft.inventory.container.SmokerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.SmokerTileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import wdl.ReflectionUtils;
import wdl.handler.HandlerException;

public class SmokerTest extends AbstractBlockHandlerTest<SmokerTileEntity, SmokerContainer, SmokerHandler> {

	public SmokerTest() {
		super(SmokerTileEntity.class, SmokerContainer.class, SmokerHandler.class);
	}

	@Test
	public void testFurance() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.SMOKER);
		SmokerTileEntity te = makeBlockEntity(pos);
		te.setInventorySlotContents(0, new ItemStack(Items.PORKCHOP));
		te.setInventorySlotContents(1, new ItemStack(Items.WOODEN_HOE));
		te.setInventorySlotContents(2, new ItemStack(Items.COOKED_PORKCHOP));

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	@Test
	public void testFields() throws HandlerException {
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.SMOKER);
		SmokerTileEntity te = makeBlockEntity(pos);
		te.setInventorySlotContents(0, new ItemStack(Items.PORKCHOP));
		te.setInventorySlotContents(1, new ItemStack(Items.WOODEN_HOE));
		te.setInventorySlotContents(2, new ItemStack(Items.COOKED_PORKCHOP));
		IIntArray fields = ReflectionUtils.findAndGetPrivateField(te, AbstractFurnaceTileEntity.class, IIntArray.class);
		fields.set(0, 100); // burn time
		// skip field 1 (total burn time) -- not saved: https://bugs.mojang.com/browse/MC-10025
		fields.set(2, 100); // cook time
		fields.set(3, 200); // total cook time (saved but unused?)

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	@Test
	public void testCustomName() throws HandlerException {
		assumeCustomNamesNotBroken();
		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.SMOKER);
		SmokerTileEntity te = makeBlockEntity(pos);
		te.setCustomName(customName("Smokester"));

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}

	@Test
	public void testCustomNameVanilla() throws HandlerException {
		assumeCustomNamesNotBroken();
		assumeMixinsApplied();

		BlockPos pos = new BlockPos(0, 0, 0);
		makeMockWorld();
		placeBlockAt(pos, Blocks.SMOKER);
		SmokerTileEntity te = makeBlockEntity(pos);
		te.setCustomName(customName("Smoker"));

		runHandler(pos, makeClientContainer(pos));
		checkAllTEs();
	}
}
