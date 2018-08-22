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
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.SaveHandler;
import wdl.config.settings.GeneratorSettings.Generator;
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
	 * (EVIL) Converts name to the appropriate type for a custom name on this
	 * version.
	 *
	 * @param name The name. Non-null.
	 * @param <T> The type that is expected to be returned, based on the method
	 *             being called.
	 * @return Either a String or a TextComponentString, depending on the version; T
	 *         should be inferred to the right one of those.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T customName(String name) {
		return (T)HandlerFunctions.customName(name);
	}

	/**
	 * Creates a new block entity.  For use in testing only.
	 *
	 * @param world The world to create it in.
	 * @param block The block to use to create it.
	 * @param state The block state.
	 * @return The block entity, or null if the creation function fails.
	 */
	@Nullable
	public static TileEntity createNewBlockEntity(World world, BlockContainer block, IBlockState state) {
		return HandlerFunctions.createNewBlockEntity(world, block, state);
	}

	/**
	 * Gets the container for a large chest. For use in testing only.
	 *
	 * @param block             The chest
	 * @param world             The world
	 * @param pos               The position to check
	 * @param allowBlockedChest If false, then if the chest is blocked then
	 *                          <code>null</code> will be returned. If true, ignores
	 *                          blocking for the chest at the given position (but,
	 *                          due to
	 *                          <a href="https://bugs.mojang.com/browse/MC-99321">a
	 *                          bug</a>, still checks if the neighbor is blocked in
	 *                          versions before 1.13).
	 */
	public static ILockableContainer getLargeChest(BlockChest block, World world, BlockPos pos, boolean allowBlockedChest) {
		return HandlerFunctions.getLargeChest(block, world, pos, allowBlockedChest);
	}

	/**
	 * Gets the SaveHandler for the world with the given name.
	 *
	 * @param minecraft The Minecraft instance
	 * @param worldName The name of the world.
	 * @return The SaveHandler.
	 */
	public static SaveHandler getSaveHandler(Minecraft minecraft, String worldName) {
		return HandlerFunctions.getSaveHandler(minecraft, worldName);
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
	 * Gets the map data associated with the given packet.
	 *
	 * @param world The client world.
	 * @param mapPacket The packet.
	 * @return The map data, or null if the underlying function returns null.
	 */
	@Nullable
	public static MapData getMapData(World world, SPacketMaps mapPacket) {
		return PacketFunctions.getMapData(world, mapPacket);
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

	/**
	 * Returns true if the given generator can be used in this Minecraft version.
	 * @param generator The generator
	 * @return True if it is usable.
	 */
	public static boolean isAvaliableGenerator(Generator generator) {
		return GeneratorFunctions.isAvaliableGenerator(generator);
	}

	/**
	 * Creates a settings GUI for the given world generator (e.g. superflat
	 * options).
	 *
	 * @param generator       The generator.
	 * @param parent          The GUI to return to when the settings GUI is closed.
	 * @param generatorConfig The configuration for the generator, which depends on
	 *                        the generator.
	 * @param callback        Called with the new generator config.
	 * @return The new GUI, or the parent if there is no valid GUI.
	 */
	public static GuiScreen makeGeneratorSettingsGui(Generator generator, GuiScreen parent,
			String generatorConfig, Consumer<String> callback) {
		return GeneratorFunctions.makeGeneratorSettingsGui(generator, parent, generatorConfig, callback);
	}
}
