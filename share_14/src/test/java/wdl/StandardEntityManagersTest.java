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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
//import net.minecraft.entity.EntityTracker;

/**
 * Tests the data contained within StandardEntityManagers.
 */
@RunWith(Parameterized.class)
public class StandardEntityManagersTest {
	static {
		MaybeMixinTest.init();
	}

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
		this.type = Registry.ENTITY_TYPE.func_218349_b(new ResourceLocation(identifier)).get();
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
	/*@Test
	public void testVanillaRange() throws Exception {
		TestWorld.ServerWorld world = TestWorld.makeServer();

		EntityTracker tracker = mock(EntityTracker.class);
		doCallRealMethod().when(tracker).track(any());
		doCallRealMethod().when(tracker).track(any(), anyInt(), anyInt());

		Class<? extends Entity> cls = StandardEntityManagers.entityClassFor(StandardEntityManagers.VANILLA, identifier);
		Entity entity = cls.getConstructor(EntityType.class, World.class).newInstance(type, world);

		int expectedDistance = StandardEntityManagers.VANILLA.getTrackDistance(identifier, entity);

		tracker.track(entity);
		verify(tracker).track(same(entity), eq(expectedDistance), anyInt(), anyBoolean());

		world.close();
	}*/
}
