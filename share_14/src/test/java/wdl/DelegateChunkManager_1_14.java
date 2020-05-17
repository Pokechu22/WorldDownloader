/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import com.mojang.datafixers.DataFixer;

import net.minecraft.world.ServerWorld;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ServerChunkProvider;
import net.minecraft.world.chunk.TicketManager;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.storage.DimensionSavedDataManager;

/**
 * A chunk manager that a functioning no-arg constructor. Has a subclass that
 * can be mocked. It needs to be public (and have public methods there) for
 * mockito to work with when using LWTS, though that requirement doesn't exist
 * when LWTS is not in use.
 */
class MockableChunkManagerBase extends ChunkManager {
	public static final Class<? super MockableChunkManagerBase> CHUNK_MANAGER_CLASS = ChunkManager.class;
	public static final Class<?> TICKET_MANAGER_CLASS = TicketManager.class;

	public MockableChunkManagerBase() {
		super(null, null, null, null, null, null, null, null, null, null, 0, 0);
	}

	protected void setViewDistance(int viewDistance) {
		super.setViewDistance(viewDistance, viewDistance);
	}
}

/**
 * Created because the constructor lost an argument in 1.14.3 (the same one
 * removed from ChunkManager).
 */
abstract class ExtServerChunkProvider extends ServerChunkProvider {
	protected ExtServerChunkProvider(ServerWorld worldIn, File worldDirectory, DataFixer dataFixer,
			TemplateManager p_i51537_4_, Executor p_i51537_5_, ChunkGenerator<?> p_i51537_6_, int viewDistance,
			IChunkStatusListener p_i51537_8_, Supplier<DimensionSavedDataManager> p_i51537_9_) {
		super(worldIn, worldDirectory, dataFixer, p_i51537_4_, p_i51537_5_, p_i51537_6_, viewDistance, viewDistance,
				p_i51537_8_, p_i51537_9_);
	}
}