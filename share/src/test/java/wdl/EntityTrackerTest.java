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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.AdditionalAnswers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

/**
 * An experimental test around the entity tracking code.  Not particularly complete.
 */
public class EntityTrackerTest extends MaybeMixinTest {

	@Test
	public void test() {
		WorldServer world = mock(WorldServer.class, withSettings().defaultAnswer(RETURNS_MOCKS)
				.useConstructor(mock(MinecraftServer.class, RETURNS_MOCKS), mock(ISaveHandler.class), mock(WorldInfo.class), 0, mock(Profiler.class)));

		EntityPlayerMP player = mock(EntityPlayerMP.class, RETURNS_DEEP_STUBS);
		List<Entity> trackedEntities = new ArrayList<>();
		doAnswer(AdditionalAnswers.<Entity>answerVoid(trackedEntities::add)).when(player).addEntity(any());
		doAnswer(AdditionalAnswers.<Entity>answerVoid(trackedEntities::remove)).when(player).removeEntity(any());
		when(player.getServerWorld().getPlayerChunkMap().isPlayerWatchingChunk(eq(player), anyInt(), anyInt())).thenReturn(true);

		List<Entity> entities = new ArrayList<>();
		class EntityTrackCollection {
			public List<Entity> list = new ArrayList<>();
			public void add(Entity e) {
				if (list.contains(e)) {
					throw new RuntimeException("Tried to add an entity that was already present: " + e);
				}
				list.add(e);
			}
			public void remove(Entity e) {
				if (!list.contains(e)) {
					throw new RuntimeException("Tried to remove an entity that was not already present: " + e);
				}

				int threshold = 80; // XXX can't even get the threshold because EntityUtils doesn't work in tests
				int serverViewDistance = 10; // XXX hardcoded for now

				if (!EntityUtils.isWithinSavingDistance(e, player, threshold, serverViewDistance)) {
					throw new RuntimeException("Unexpected removal of " + e);
				}
				System.out.println("-" + e);
				list.remove(e);
			}
		}
		EntityTrackCollection col = new EntityTrackCollection();
		player.connection = mock(NetHandlerPlayServer.class);
		doAnswer(AdditionalAnswers.answerVoid(col::add)).when(player).addEntity(any());
		doAnswer(AdditionalAnswers.answerVoid(col::remove)).when(player).removeEntity(any());

		world.playerEntities.add(player);

		EntityTracker tracker = new EntityTracker(world);
		// Required because world doesn't set it up right for a mock, and mocking it
		// would be making assumptions about how this is calculated
		tracker.setViewDistance(10);

		int i = 0;
		for (int x = -100; x <= 100; x += 10) {
			for (int z = -100; z <= 100; z += 10) {
				EntityPig pig = new EntityPig(world);
				entities.add(pig);
				pig.setEntityId(i++);
				pig.posX = x;
				pig.posZ = z;
				tracker.track(pig);
			}
		}

		for (int tick = 0; tick <= 300; tick++) {
			//TODO also remove some entities by death
			player.posX = player.posZ = -150 + tick;
			tracker.tick();
		}
	}

}
