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

import static org.mockito.Mockito.*;

import java.util.UUID;

import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.profiler.IProfiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;

public final class TestWorld {
	private TestWorld() { throw new AssertionError(); }

	public static ClientWorld makeClient() {
		return makeClient(DimensionType.OVERWORLD);
	}

	public static ClientWorld makeClient(DimensionType dim) {
		ClientPlayNetHandler nhpc = mock(ClientPlayNetHandler.class);
		WorldSettings settings = new WorldSettings(0L, null, false, false, WorldType.DEFAULT);
		Difficulty difficulty = Difficulty.NORMAL;
		IProfiler profiler = EmptyProfiler.INSTANCE;

		return new ClientWorld(nhpc, settings, dim, difficulty, profiler);
	}

	public static ServerWorld makeServer() {
		return makeServer(DimensionType.OVERWORLD);
	}

	@SuppressWarnings("resource")
	public static ServerWorld makeServer(DimensionType dim) {
		MinecraftServer server = mock(MinecraftServer.class, withSettings().defaultAnswer(RETURNS_MOCKS));
		SaveHandler saveHandler = mock(SaveHandler.class);
		WorldInfo info = new WorldInfo() {};
		IProfiler profiler = EmptyProfiler.INSTANCE;

		return new ServerWorld(server, saveHandler, info, dim, profiler);
	}

	public static final class ClientWorld extends ExtWorldClient implements AutoCloseable {
		private ClientWorld(ClientPlayNetHandler netHandler, WorldSettings settings, DimensionType dim,
				Difficulty difficulty, IProfiler profilerIn) {
			super(netHandler, settings, dim, difficulty, profilerIn);
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
		private ServerWorld(MinecraftServer server, SaveHandler saveHandlerIn, WorldInfo info, DimensionType dim,
				IProfiler profilerIn) {
			super(server, saveHandlerIn, info, dim, profilerIn);
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

}
