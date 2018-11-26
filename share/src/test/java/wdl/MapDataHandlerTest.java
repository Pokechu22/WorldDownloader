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
package wdl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapData.MapInfo;
import wdl.MapDataHandler.MapDataResult;
import wdl.versioned.VersionedFunctions;

public class MapDataHandlerTest extends MaybeMixinTest {
	// (n.b. DimensionType.values() doesn't exist in 1.13.1+)
	private static final DimensionType[] DIMENSIONS = { DimensionType.NETHER, DimensionType.OVERWORLD, DimensionType.THE_END };
	private static final ItemStack SOME_MAP_ITEM = new ItemStack(Items.FILLED_MAP);

	/**
	 * Tests that the map dimension is copied successfully.
	 */
	@Test
	public void testDimension() {
		for (DimensionType dim : DIMENSIONS) {
			EntityPlayer owner = new EntityOtherPlayerMP(TestWorld.makeClient(dim), mock(GameProfile.class));
			owner.inventory.setInventorySlotContents(10, SOME_MAP_ITEM);
			MapData map = new MapData("test");
			map.trackingPosition = true;
			VersionedFunctions.setMapDimension(map, dim);
			map.updateVisiblePlayers(owner, SOME_MAP_ITEM);
			MapData client = copyMapData(map);

			MapDataResult result = MapDataHandler.repairMapData(0, client, owner);
			assertNotNull(result.dim);
			assertEquals(map.dimension, client.dimension);
		}
	}

	/**
	 * Tests that the map dimension is copied successfully.
	 */
	@Test
	public void testCenter() {
		int[][] positions = {
			{900, 900},
			{900, -900},
			{-900, 900},
			{-900, -900},
			{0, 0},
			{4200, 50}
		};
		for (int[] pos : positions) {
			for (byte scale = 0; scale <= 4; scale++) {
				EntityPlayer owner = new EntityOtherPlayerMP(TestWorld.makeClient(), mock(GameProfile.class));
				owner.inventory.setInventorySlotContents(10, SOME_MAP_ITEM);
				owner.posX = pos[0];
				owner.posZ = pos[1];
				MapData map = new MapData("test");
				map.scale = scale;
				map.trackingPosition = true;
				VersionedFunctions.setMapDimension(map, DimensionType.OVERWORLD);
				map.calculateMapCenter(owner.posX, owner.posZ, map.scale);
				map.updateVisiblePlayers(owner, SOME_MAP_ITEM);

				MapData client = copyMapData(map);
				MapDataResult result = MapDataHandler.repairMapData(0, client, owner);

				assertTrue(result.hasCenter);
				assertEquals(map.xCenter, client.xCenter);
				assertEquals(map.zCenter, client.zCenter);
			}
		}
	}

	/**
	 * Creates a clientside map based on a server map.
	 */
	private MapData copyMapData(MapData map) {
		MapInfo info = map.new MapInfo(null);
		SPacketMaps mapPacket = (SPacketMaps)info.getPacket(SOME_MAP_ITEM);
		MapData result = new MapData("test");
		mapPacket.setMapdataTo(result);
		return result;
	}
}
