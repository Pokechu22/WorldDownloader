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

import java.util.concurrent.Executor;
import java.util.function.Supplier;

import com.mojang.datafixers.DataFixer;

import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketManager;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.SaveFormat;

/**
 * A chunk manager that a functioning no-arg constructor. Has a subclass that
 * can be mocked. It needs to be public (and have public methods there) for
 * mockito to work with when using LWTS, though that requirement doesn't exist
 * when LWTS is not in use.
 */
abstract class MockableChunkManagerBase extends ChunkManager {
	public static final Class<? super MockableChunkManagerBase> CHUNK_MANAGER_CLASS = ChunkManager.class;
	// This changed packages in 1.14.4
	public static final Class<?> TICKET_MANAGER_CLASS = TicketManager.class;

	public MockableChunkManagerBase() {
		super(null, null, null, null, null, null, null, null, null, null, 0, false);
	}
}

/**
 * Created because the constructor lost an argument in 1.14.3 (the same one
 * removed from ChunkManager).
 */
abstract class ExtServerChunkProvider extends ServerChunkProvider {
	protected ExtServerChunkProvider(ServerWorld worldIn, SaveFormat.LevelSave p_i232603_2_,
			DataFixer dataFixer, TemplateManager p_i232603_4_, Executor p_i232603_5_, ChunkGenerator p_i232603_6_,
			int viewDistance, boolean p_i232603_8_, IChunkStatusListener p_i232603_9_,
			Supplier<DimensionSavedDataManager> p_i232603_10_) {
		super(worldIn, p_i232603_2_, dataFixer, p_i232603_4_, p_i232603_5_, p_i232603_6_, viewDistance,
				p_i232603_8_, p_i232603_9_, p_i232603_10_);
	}
}