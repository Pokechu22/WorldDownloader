/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018-2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.junit.Test;
import org.mockito.AdditionalAnswers;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import wdl.versioned.VersionedFunctions;

/**
 * An experimental test around the entity tracking code.  Not particularly complete.
 */
public class EntityUtilsTest extends MaybeMixinTest {

	/**
	 * Some basic tests, with varying paths but no entity removal.
	 */
	@Test
	public void testTrackerSimple() throws Exception  {
		runTrackerTest(world -> new PigEntity(EntityType.PIG, world), 80, 10, 300,
				(tick, entity) -> true,
				(tick) -> new Vec3d(-150 + tick, tick, -150 + tick));
		runTrackerTest(world -> new ArmorStandEntity(EntityType.ARMOR_STAND, world), 160, 10, 300,
				(tick, entity) -> true,
				(tick) -> new Vec3d(150 * Math.sin(tick * 300 / (2 * Math.PI)), tick,
						150 * Math.cos(tick * 300 / (2 * Math.PI))));
	}

	/**
	 * Tracker test, where some entities are removed.
	 */
	@Test
	public void testTrackerRemove() throws Exception {
		runTrackerTest(ZombieEntity::new, 80, 10, 110, // Why does this still work?
				(tick, entity) -> tick <= 100,
				(tick) -> new Vec3d(-150 + tick, tick, -150 + tick));
		runTrackerTest(world -> new CreeperEntity(EntityType.CREEPER, world), 80, 10, 110,
				(tick, entity) -> tick <= 100 || VersionedFunctions.getEntityX(entity) <= (-150 + tick),
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
			int serverViewDistance, int numTicks, BiPredicate<Integer, Entity> keepEntity, IntFunction<Vec3d> posFunc) throws Exception {
		TestWorld.ServerWorld world = TestWorld.makeServer();

		ServerPlayerEntity player = mock(ServerPlayerEntity.class, RETURNS_DEEP_STUBS);
		List<Entity> trackedEntities = new ArrayList<>();
		when(player.toString()).thenCallRealMethod();
		doAnswer(AdditionalAnswers.<Entity>answerVoid(trackedEntities::add)).when(player).addEntity(any());
		doAnswer(AdditionalAnswers.<Entity>answerVoid(trackedEntities::remove)).when(player).removeEntity(any());

		List<Entity> entities = new ArrayList<>(); // all known entities; if killed they're removed from this list
		List<Entity> tracked = new ArrayList<>(); // entities being tracked by the mock player
		player.connection = mock(ServerPlayNetHandler.class);

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

		world.addNewPlayer(player);

		MockableChunkManager tracker = mock(MockableChunkManager.class);
		doCallRealMethod().when(tracker).setViewDistance(anyInt());
		doCallRealMethod().when(tracker).track(any());
		doCallRealMethod().when(tracker).untrack(any());
		doCallRealMethod().when(tracker).tickEntityTracker();
		// We bypass the constructor, so this needs to be manually set
		Class<?> ticketManagerClass = Arrays
				.stream(MockableChunkManager.CHUNK_MANAGER_CLASS.getDeclaredClasses())
				.filter(MockableChunkManager.TICKET_MANAGER_CLASS::isAssignableFrom)
				.findAny().get();
		setToMock(tracker, MockableChunkManager.CHUNK_MANAGER_CLASS, ticketManagerClass);
		Long2ObjectLinkedOpenHashMap<?> chunkHolders1 = new Long2ObjectLinkedOpenHashMap<>();
		ReflectionUtils.findAndSetPrivateField(tracker, MockableChunkManager.CHUNK_MANAGER_CLASS, Long2ObjectLinkedOpenHashMap.class, chunkHolders1);
		Int2ObjectMap<?> trackerTrackedEntities = new Int2ObjectOpenHashMap<>();
		ReflectionUtils.findAndSetPrivateField(tracker, MockableChunkManager.CHUNK_MANAGER_CLASS, Int2ObjectMap.class, trackerTrackedEntities);
		ReflectionUtils.findAndSetPrivateField(tracker, MockableChunkManager.CHUNK_MANAGER_CLASS, TestWorld.ServerWorld.SERVER_WORLD_CLASS, world);
		// Required because world doesn't set it up right for a mock, and mocking it
		// would be making assumptions about how this is calculated
		// (NOTE: I'm not sure what the difference between the two parameters are)
		tracker.setViewDistance(serverViewDistance);

		int eid = 0;
		for (int x = -100; x <= 100; x += 10) {
			for (int z = -100; z <= 100; z += 10) {
				Entity e = entitySupplier.apply(world);
				entities.add(e);
				e.setEntityId(eid++);
				VersionedFunctions.setEntityPos(e, x, 0, z);
				tracker.track(e);
			}
		}

		for (int tick = 0; tick <= numTicks; tick++) {
			Vec3d pos = posFunc.apply(tick);
			VersionedFunctions.setEntityPos(player, pos.x, pos.y, pos.z);
			for (Iterator<Entity> itr = entities.iterator(); itr.hasNext();) {
				Entity e = itr.next();
				if (!keepEntity.test(tick, e)) {
					itr.remove();
					tracker.untrack(e);
				}
			}
			tracker.tickEntityTracker();
		}

		tracker.close();
		world.close();
	}

	// Needed for silly wildcard reasons...
	private static <A, B> void setToMock(A a, Class<A> aClass, Class<B> mockClass) {
		B b = mock(mockClass);
		ReflectionUtils.findAndSetPrivateField(a, aClass, mockClass, b);
	}
}
