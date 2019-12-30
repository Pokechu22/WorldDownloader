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
package wdl;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import wdl.TestWorld.ServerWorld;

/**
 * An experimental test around the entity tracking code.  Not particularly complete.
 */
public class EntityUtilsTest extends MaybeMixinTest {

	/**
	 * Some basic tests, with varying paths but no entity removal.
	 */
	@Test
	public void testTrackerSimple() {
		runTrackerTest(EntityPig::new, 80, 10, 300,
				(tick, entity) -> true,
				(tick) -> new Vec3d(-150 + tick, tick, -150 + tick));
		runTrackerTest(EntityArmorStand::new, 160, 10, 300,
				(tick, entity) -> true,
				(tick) -> new Vec3d(150 * Math.sin(tick * 300 / (2 * Math.PI)), tick,
						150 * Math.cos(tick * 300 / (2 * Math.PI))));
	}

	/**
	 * Tracker test, where some entities are removed.
	 */
	@Test
	public void testTrackerRemove() {
		runTrackerTest(EntityZombie::new, 80, 10, 110,
				(tick, entity) -> tick <= 100,
				(tick) -> new Vec3d(-150 + tick, tick, -150 + tick));
		runTrackerTest(EntityCreeper::new, 80, 10, 110,
				(tick, entity) -> tick <= 100 || entity.posX <= (-150 + tick),
				(tick) -> new Vec3d(-150 + tick, tick, -150 + tick));
	}

	/**
	 * A generalized test for the entity tracker.
	 *
	 * @param entitySupplier     Produces entities.
	 * @param threshold          The track distance for the produced entities.
	 * @param serverViewDistance The view distance (in chunks) that is used.
	 * @param numTicks           Number of ticks to simulate.
	 * @param keepEntity         Predicate taking the tick and the entity, to see if
	 *                           it should be "killed" on a tick.
	 * @param posFunc            Function providing player position by tick.
	 */
	protected void runTrackerTest(Function<World, ? extends Entity> entitySupplier, int threshold,
			int serverViewDistance, int numTicks, BiPredicate<Integer, Entity> keepEntity, IntFunction<Vec3d> posFunc) {
		ServerWorld world = TestWorld.makeServer();

		EntityPlayerMP player = mock(EntityPlayerMP.class, RETURNS_DEEP_STUBS);
		List<Entity> trackedEntities = new ArrayList<>();
		when(player.toString()).thenCallRealMethod();
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

			boolean keep = EntityUtils.isWithinSavingDistance(e, player, threshold, serverViewDistance);
			if (entities.contains(e)) {
				assertTrue(e + " should have been saved for " + player + " @ " + threshold, keep);
			} else {
				assertFalse(e + " should not have been saved for " + player + " @ " + threshold, keep);
			}
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

		for (int tick = 0; tick <= numTicks; tick++) {
			Vec3d pos = posFunc.apply(tick);
			player.posX = pos.x;
			player.posY = pos.y;
			player.posZ = pos.z;
			for (Iterator<Entity> itr = entities.iterator(); itr.hasNext();) {
				Entity e = itr.next();
				if (!keepEntity.test(tick, e)) {
					itr.remove();
					tracker.untrack(e);
				}
			}
			tracker.tick();
		}

		world.close();
	}
}
