/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.versioned;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import wdl.handler.block.BlockHandler;
import wdl.handler.entity.EntityHandler;

/**
 * Helper that determines version-specific information about things, such
 * as whether a world has skylight.
 */
public class VersionedFunctions {
	/**
	 * Returns true if the given world has skylight data.
	 *
	 * @return a boolean
	 */
	public static boolean hasSkyLight(World world) {
		return HandlerFunctions.hasSkyLight(world);
	}

	/**
	 * All supported {@link BlockHandler}s.  Each type will only be represented once.
	 */
	public static final ImmutableList<BlockHandler<?, ?>> BLOCK_HANDLERS = HandlerFunctions.BLOCK_HANDLERS;

	/**
	 * All supported {@link EntityHandler}s.  There will be no ambiguities.
	 */
	public static final ImmutableList<EntityHandler<?, ?>> ENTITY_HANDLERS = HandlerFunctions.ENTITY_HANDLERS;

	/**
	 * Checks if the block entity should be imported. Only "problematic" (IE,
	 * those that require manual interaction such as chests) block entities will
	 * be imported. Additionally, the block at the block entity's coordinates
	 * must be one that would normally be used with that block entity.
	 *
	 * @param entityID
	 *            The block entity's ID, as found in the 'id' tag.
	 * @param pos
	 *            The location of the block entity, as created by its 'x', 'y',
	 *            and 'z' tags.
	 * @param block
	 *            The block in the current world at the given position.
	 * @param blockEntityNBT
	 *            The full NBT tag of the existing block entity. May be used if
	 *            further identification is needed.
	 * @param chunk
	 *            The (current) chunk for which entities are being imported. May be used
	 *            if further identification is needed (e.g. nearby blocks).
	 * @return true if it should be imported
	 * @see wdl.WDLChunkLoader#shouldImportBlockEntity
	 */
	public static boolean shouldImportBlockEntity(String entityID, BlockPos pos,
			Block block, NBTTagCompound blockEntityNBT, Chunk chunk) {
		return HandlerFunctions.shouldImportBlockEntity(entityID, pos, block, blockEntityNBT, chunk);
	}

	/**
	 * Gets the class used to store the list of chunks in ChunkProviderClient.
	 */
	public static Class<?> getChunkListClass() {
		return HandlerFunctions.getChunkListClass();
	}

	/**
	 * Creates a plugin message packet.
	 * @param channel The channel to send on.
	 * @param bytes The payload.
	 * @return The new packet.
	 */
	public static CPacketCustomPayload makePluginMessagePacket(String channel, byte[] bytes) {
		return PacketFunctions.makePluginMessagePacket(channel, bytes);
	}

	/**
	 * Checks if the given game rule is of the given type.
	 * @param rules The rule collection
	 * @param rule The name of the rule
	 * @return The type, or null if no info could be found.
	 */
	@Nullable
	public static GameRules.ValueType getRuleType(GameRules rules, String rule) {
		return GameRuleFunctions.getRuleType(rules, rule);
	}

	/**
	 * Gets the value of a game rule.
	 * @param rules The rule collection
	 * @param rule The name of the rule
	 * @return The value, or null if no info could be found.
	 */
	@Nullable
	public static String getRuleValue(GameRules rules, String rule) { 
		return GameRuleFunctions.getRuleValue(rules, rule);
	}

	/**
	 * Gets a list of all game rules.
	 * @param rules The rules object.
	 * @return A list of all rule names.
	 */
	public static List<String> getGameRules(GameRules rules) {
		return GameRuleFunctions.getGameRules(rules);
	}
}
