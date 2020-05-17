/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018-2020 Pokechu22, julialy
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.spongepowered.lwts.runner.DelegateRunner.DelegatedRunWith;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import wdl.TestWorld.MockableChunkManager;

/**
 * Tests the data contained within StandardEntityManagers.
 */
@DelegatedRunWith(Parameterized.class)
public class StandardEntityManagersTest extends MaybeMixinTest {
	@Parameters(name="{0}")
	public static List<Object[]> data() {
		return StandardEntityManagers.VANILLA.getProvidedEntities().stream()
				.sorted()
				.map(id->new Object[]{id})
				.collect(Collectors.toList());
	}

	/**
	 * Entity identifier to test.
	 */
	private final String identifier;
	/**
	 * EntityType associated with the entity to test
	 */
	private final EntityType<?> type;

	public StandardEntityManagersTest(String identifier) {
		this.identifier = identifier;
		this.type = EntityType.byKey(identifier).get();
	}

	/**
	 * Checks that the identifier matches the class.
	 */
	@Test
	public void testIdentifier() throws Exception {
		TestWorld.ServerWorld world = TestWorld.makeServer();
		Entity entity = this.type.create(world);
		assertThat(StandardEntityManagers.VANILLA.getIdentifierFor(entity), is(identifier));
		world.close();
	}

	/**
	 * Checks that the range associated with the entity is the actual range assigned by EntityTracker.
	 */
	@Test
	public void testVanillaRange() throws Exception {
		TestWorld.ServerWorld world = TestWorld.makeServer();

		MockableChunkManager tracker = mock(MockableChunkManager.class);
		// We bypass the constructor, so this needs to be manually set
		Int2ObjectMap<?> trackedEntities = new Int2ObjectOpenHashMap<>();
		ReflectionUtils.findAndSetPrivateField(tracker, MockableChunkManager.CHUNK_MANAGER_CLASS, Int2ObjectMap.class, trackedEntities);
		ReflectionUtils.findAndSetPrivateField(tracker, MockableChunkManager.CHUNK_MANAGER_CLASS, TestWorld.ServerWorld.SERVER_WORLD_CLASS, world);
		doCallRealMethod().when(tracker).track(any());

		Entity entity = type.create(world);

		int expectedDistance = StandardEntityManagers.VANILLA.getTrackDistance(identifier, entity);

		tracker.track(entity);
		assertThat(trackedEntities, hasKey(entity.getEntityId()));
		Object entityTrackerEntry = trackedEntities.get(entity.getEntityId());
		Field actualDistanceField = ReflectionUtils.findField(entityTrackerEntry.getClass(), int.class);
		int actualDistance = actualDistanceField.getInt(entityTrackerEntry);

		assertEquals(expectedDistance, actualDistance);

		tracker.close();
		world.close();
	}
}
