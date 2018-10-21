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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.MatchesPattern;
import javax.annotation.Nullable;
import javax.annotation.RegEx;
import javax.annotation.meta.TypeQualifierNickname;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.SaveHandler;
import wdl.config.settings.GeneratorSettings.Generator;
import wdl.handler.block.BlockHandler;
import wdl.handler.blockaction.BlockActionHandler;
import wdl.handler.entity.EntityHandler;

/**
 * Helper that determines version-specific information about things, such
 * as whether a world has skylight.
 */
public class VersionedFunctions {
	private VersionedFunctions() { throw new AssertionError(); }

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
	 * All supported {@link BlockActionHandler}s.  Each type will only be represented once.
	 */
	public static final ImmutableList<BlockActionHandler<?, ?>> BLOCK_ACTION_HANDLERS = HandlerFunctions.BLOCK_ACTION_HANDLERS;

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
	 * A map mapping villager professions to a map from each career's I18n name to
	 * the career's ID.
	 *
	 * @see https://minecraft.gamepedia.com/Villager#Professions_and_careers
	 * @see EntityVillager#getDisplayName
	 */
	public static final Int2ObjectMap<BiMap<String, Integer>> VANILLA_VILLAGER_CAREERS = HandlerFunctions.VANILLA_VILLAGER_CAREERS;

	/**
	 * Returns a well-formated String version of the tag, suitable for tests and logging.
	 * This will usually be multiple lines long.
	 *
	 * @param tag The tag to use
	 * @return The string version.
	 */
	public static String nbtString(INBTBase tag) {
		return HandlerFunctions.nbtString(tag);
	}

	/**
	 * A regex that indicates whether a name is valid for a plugin channel.
	 * In 1.13, channels are namespaced identifiers; in 1.12 they are not.
	 */
	@RegEx
	public static final String CHANNEL_NAME_REGEX = PacketFunctions.CHANNEL_NAME_REGEX;

	/**
	 * Marks a parameter as requiring a valid channel name for a plugin message.
	 */
	@Documented
	@TypeQualifierNickname @MatchesPattern(CHANNEL_NAME_REGEX)
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE_USE})
	public static @interface ChannelName { }

	/**
	 * Creates a plugin message packet.
	 * @param channel The channel to send on.
	 * @param bytes The payload.
	 * @return The new packet.
	 */
	public static CPacketCustomPayload makePluginMessagePacket(@ChannelName String channel, byte[] bytes) {
		return PacketFunctions.makePluginMessagePacket(channel, bytes);
	}

	/**
	 * Creates a server plugin message packet.  Intended for use in tests.
	 * @param channel The channel to send on.
	 * @param bytes The payload.
	 * @return The new packet.
	 */
	public static SPacketCustomPayload makeServerPluginMessagePacket(@ChannelName String channel, byte[] bytes) {
		return PacketFunctions.makeServerPluginMessagePacket(channel, bytes);
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
	 * Gets the name of the channel that is used to register plugin messages.
	 * @return The channel name.
	 */
	@ChannelName
	public static String getRegisterChannel() {
		return PacketFunctions.getRegisterChannel();
	}

	/**
	 * Gets the name of the channel that is used to unregister plugin messages.
	 * @return The channel name.
	 */
	@ChannelName
	public static String getUnregisterChannel() {
		return PacketFunctions.getUnregisterChannel();
	}

	/**
	 * Creates a list of channel names based on the given list, but with names that
	 * are not valid for this version removed.
	 *
	 * @param names The names list.
	 * @return A sanitized list of names.
	 */
	public static ImmutableList<@ChannelName String> removeInvalidChannelNames(String... names) {
		ImmutableList.Builder<@ChannelName String> list = ImmutableList.builder();
		for (String name : names) {
			if (name.matches(CHANNEL_NAME_REGEX)) {
				list.add(name);
			}
		}
		return list.build();
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

	/**
	 * Makes a backup toast, on versions that support it.
	 *
	 * @param name The name of the world
	 * @param fileSize The size of the file in bytes
	 */
	public static void makeBackupToast(String name, long fileSize) {
		GeneratorFunctions.makeBackupToast(name, fileSize);
	}

	/**
	 * A superflat configuration that generates only air.
	 *
	 * This should be similar to the "the void" preset present since 1.9 (including the
	 * "void" biome), but shouldn't include any decoration (the 31x31 platform).  In versions
	 * without the void biome, another biome (ocean since it's 0, maybe) should be used.
	 */
	public static final String VOID_FLAT_CONFIG = GeneratorFunctions.VOID_FLAT_CONFIG;

	/**
	 * Creates the generator options tag,
	 *
	 * @param generatorOptions The content.  Either a string or an SNBT representation of the data.
	 * @return An NBT tag of some type.
	 */
	public static INBTBase createGeneratorOptionsTag(String generatorOptions) {
		return GeneratorFunctions.createGeneratorOptionsTag(generatorOptions);
	}

	/**
	 * Creates a new instance of {@link EntityPlayerSP}.
	 *
	 * @param minecraft The minecraft instance
	 * @param world The world
	 * @param nhpc The connection
	 * @param base The original player to copy other data from
	 */
	public static EntityPlayerSP makePlayer(Minecraft minecraft, World world, NetHandlerPlayClient nhpc, EntityPlayerSP base) {
		return GuiFunctions.makePlayer(minecraft, world, nhpc, base);
	}

	/**
	 * Draws a dark background, similar to {@link GuiScreen#drawBackground(int)} but darker.
	 * Same appearance as the background in lists.
	 *
	 * @param top Where to start drawing (usually, 0)
	 * @param left Where to start drawing (usually, 0)
	 * @param bottom Where to stop drawing (usually, height).
	 * @param right Where to stop drawing (usually, width)
	 */
	public static void drawDarkBackground(int top, int left, int bottom, int right) {
		GuiFunctions.drawDarkBackground(top, left, bottom, right);
	}

	/**
	 * Draws the top and bottom borders found on gui lists (but no background).
	 * <br/>
	 * Based off of
	 * {@link net.minecraft.client.gui.GuiSlot#overlayBackground(int, int, int, int)}.
	 *
	 * Note that there is an additional 4-pixel padding on the margins for the gradient.
	 *
	 * @param topMargin Amount of space to give for the upper box.
	 * @param bottomMargin Amount of space to give for the lower box.
	 * @param top Where to start drawing (usually, 0)
	 * @param left Where to start drawing (usually, 0)
	 * @param bottom Where to stop drawing (usually, height).
	 * @param right Where to stop drawing (usually, width)
	 */
	public static void drawBorder(int topMargin, int bottomMargin, int top, int left, int bottom, int right) {
		GuiFunctions.drawBorder(topMargin, bottomMargin, top, left, bottom, right);
	}

	/**
	 * Copies the given text into the system clipboard.
	 * @param text The text to copy
	 */
	public static void setClipboardString(String text) {
		GuiFunctions.setClipboardString(text);
	}

	/**
	 * Gets the numeric ID for the given block.
	 * @return A numeric ID, the meaning and value of which is unspecified.
	 */
	public static int getBlockId(Block block) {
		return RegistryFunctions.getBlockId(block);
	}

	/**
	 * Gets the numeric ID for the given biome.
	 * @return A numeric ID, the meaning and value of which is unspecified.
	 */
	public static int getBiomeId(Biome biome) {
		return RegistryFunctions.getBiomeId(biome);
	}

	/**
	 * Gets the class used to store the list of chunks in ChunkProviderClient
	 * ({@link ChunkProviderClient#loadedChunks}).
	 */
	public static Class<?> getChunkListClass() {
		return TypeFunctions.getChunkListClass();
	}

	/**
	 * Gets the class used to store the list of chunks in pending saving in AnvilChunkLoader
	 * ({@link AnvilChunkLoader#chunksToSave}).
	 */
	@SuppressWarnings("rawtypes")
	public static Class<? extends Map> getChunksToSaveClass() {
		return TypeFunctions.getChunksToSaveClass();
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
		return (T)TypeFunctions.customName(name);
	}
}
