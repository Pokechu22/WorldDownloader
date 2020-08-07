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

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

public final class TestWorld {
	private TestWorld() { throw new AssertionError(); }

	public static ClientWorld makeClient() {
		return makeClient(DimensionType.OVERWORLD);
	}

	public static ClientWorld makeClient(DimensionType dim) {
		return new ClientWorld(dim);
	}

	public static ServerWorld makeServer() {
		return makeServer(DimensionType.OVERWORLD);
	}

	public static ServerWorld makeServer(DimensionType dim) {
		return new ServerWorld(dim);
	}

	public static final class ClientWorld extends ExtWorldClient implements AutoCloseable {
		private ClientWorld(DimensionType dim) {
			super(dim);
		}

		public void addEntity(Entity e, int eid) {
			e.setEntityId(eid);
			e.setUniqueId(new UUID(0, eid));
			super.addEntity(eid, e);
		}

		// Provided for 1.13 support.  Does nothing.
		@Override
		public void close() { }
	}
	public static final class ServerWorld extends ExtWorldServer implements AutoCloseable {
		private ServerWorld(DimensionType dim) {
			super(dim);
		}

		public void addEntity(Entity e, int eid) {
			e.setEntityId(eid);
			e.setUniqueId(new UUID(0, eid));
			this.addEntity(e);
		}

		@Override
		public BlockPos getSpawnPoint() {
			return BlockPos.ZERO;
		}

		// Provided for 1.13 support.  Does nothing.
		@Override
		public void close() { }
	}

	/**
	 * A chunk manager that has public methods and a functioning constructor.
	 * Intended to be mocked.
	 */
	public static class MockableChunkManager extends MockableChunkManagerBase {
		@Override
		public void setViewDistance(int viewDistance) {
			super.setViewDistance(viewDistance);
		}

		@Override
		public void track(Entity entityIn) {
			super.track(entityIn);
		}

		@Override
		public void untrack(Entity entityIn) {
			super.untrack(entityIn);
		}

		@Override
		public void tickEntityTracker() {
			super.tickEntityTracker();
		}
	}
}
