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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.spongepowered.lwts.runner.DelegateRunner.DelegatedRunWith;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.world.World;

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

	public StandardEntityManagersTest(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Checks that the identifier matches the class.
	 */
	@Test
	public void testIdentifier() throws Exception {
		TestWorld.ServerWorld world = TestWorld.makeServer();
		Class<? extends Entity> cls = StandardEntityManagers.entityClassFor(StandardEntityManagers.VANILLA, identifier);
		Entity entity = cls.getConstructor(World.class).newInstance(world);
		assertThat(StandardEntityManagers.VANILLA.getIdentifierFor(entity), is(identifier));
		world.close();
	}

	/**
	 * Checks that the range associated with the entity is the actual range assigned by EntityTracker.
	 */
	@Test
	public void testVanillaRange() throws Exception {
		TestWorld.ServerWorld world = TestWorld.makeServer();

		EntityTracker tracker = mock(EntityTracker.class);
		doCallRealMethod().when(tracker).track(any());
		doCallRealMethod().when(tracker).track(any(), anyInt(), anyInt());

		Class<? extends Entity> cls = StandardEntityManagers.entityClassFor(StandardEntityManagers.VANILLA, identifier);
		Entity entity = cls.getConstructor(World.class).newInstance(world);

		int expectedDistance = StandardEntityManagers.VANILLA.getTrackDistance(identifier, entity);

		tracker.track(entity);
		verify(tracker).track(same(entity), eq(expectedDistance), anyInt(), anyBoolean());

		world.close();
	}
}
