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
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.junit.Test;
import org.mockito.AdditionalAnswers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

/**
 * An experimental test around the entity tracking code.  Not particularly complete.
 */
public class EntityUtilsTest extends MaybeMixinTest {

	@Test
	public void simpleTrackerTest() {
		runTrackerTest(EntityPig::new, 80, 10,
				(tick, entity) -> true,
				(tick) -> new Vec3d(-150 + tick, tick, -150 + tick));
	}

	protected void runTrackerTest(Function<World, ? extends Entity> entitySupplier, int threshold,
			int serverViewDistance, BiPredicate<Integer, Entity> keepEntity, IntFunction<Vec3d> posFunc) {
		WorldServer world = mock(WorldServer.class, withSettings().defaultAnswer(RETURNS_MOCKS)
				.useConstructor(mock(MinecraftServer.class, RETURNS_MOCKS), mock(ISaveHandler.class), mock(WorldInfo.class), 0, mock(Profiler.class)));

		EntityPlayerMP player = mock(EntityPlayerMP.class, RETURNS_DEEP_STUBS);
		List<Entity> trackedEntities = new ArrayList<>();
		doAnswer(AdditionalAnswers.<Entity>answerVoid(trackedEntities::add)).when(player).addEntity(any());
		doAnswer(AdditionalAnswers.<Entity>answerVoid(trackedEntities::remove)).when(player).removeEntity(any());
		when(player.getServerWorld().getPlayerChunkMap().isPlayerWatchingChunk(eq(player), anyInt(), anyInt())).thenReturn(true);

		List<Entity> entities = new ArrayList<>(); // all known entities; if killed they're removed from this list
		List<Entity> tracked = new ArrayList<>(); // entities being tracked by the mock player
		player.connection = mock(NetHandlerPlayServer.class);

		doAnswer(AdditionalAnswers.<Entity>answerVoid((e) -> {
			assertThat("Tried to track an entity that was already tracked", tracked, not(hasItem(e)));
			tracked.add(e);
		})).when(player).addEntity(any());
		doAnswer(AdditionalAnswers.<Entity>answerVoid((e) -> {
			assertThat("Tried to untrack an entity that was not tracked", tracked, hasItem(e));
			tracked.remove(e);

			boolean isPresent = (entities.contains(e));
			assertEquals(EntityUtils.isWithinSavingDistance(e, player, threshold, serverViewDistance), isPresent);
		})).when(player).removeEntity(any());

		world.playerEntities.add(player);

		EntityTracker tracker = new EntityTracker(world);
		// Required because world doesn't set it up right for a mock, and mocking it
		// would be making assumptions about how this is calculated
		tracker.setViewDistance(serverViewDistance);

		int eid = 0;
		for (int x = -100; x <= 100; x += 10) {
			for (int z = -100; z <= 100; z += 10) {
				Entity e = entitySupplier.apply(world);
				entities.add(e);
				e.setEntityId(eid++);
				e.posX = x;
				e.posZ = z;
				tracker.track(e);
			}
		}

		for (int tick = 0; tick <= 300; tick++) {
			Vec3d pos = posFunc.apply(tick);
			player.posX = pos.x;
			player.posY = pos.y;
			player.posZ = pos.z;
			for (Iterator<Entity> itr = entities.iterator(); itr.hasNext();) {
				if (!keepEntity.test(tick, itr.next())) {
					itr.remove();
				}
			}
			tracker.tick();
		}
	}

}
