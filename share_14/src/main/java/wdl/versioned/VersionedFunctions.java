/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.versioned;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
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
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.network.play.server.SMapDataPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
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
public final class VersionedFunctions {
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
			Block block, CompoundNBT blockEntityNBT, Chunk chunk) {
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
	public static TileEntity createNewBlockEntity(World world, ContainerBlock block, BlockState state) {
		return HandlerFunctions.createNewBlockEntity(world, block, state);
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
	 * Gets an entity's x coordinate.
	 *
	 * @param e The entity
	 * @return {@link Entity#posX}
	 */
	public static double getEntityX(Entity e) {
		return HandlerFunctions.getEntityX(e);
	}

	/**
	 * Gets an entity's y coordinate.
	 *
	 * @param e The entity
	 * @return {@link Entity#posY}
	 */
	public static double getEntityY(Entity e) {
		return HandlerFunctions.getEntityY(e);
	}

	/**
	 * Gets an entity's z coordinate.
	 *
	 * @param e The entity
	 * @return {@link Entity#posZ}
	 */
	public static double getEntityZ(Entity e) {
		return HandlerFunctions.getEntityZ(e);
	}

	/**
	 * Sets an entity's position directly.
	 *
	 * @param e The entity
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param z The z coordinate
	 */
	public static void setEntityPos(Entity e, double x, double y, double z) {
		HandlerFunctions.setEntityPos(e, x, y, z);
	}

	/**
	 * Returns a well-formated String version of the tag, suitable for tests and logging.
	 * This will usually be multiple lines long.
	 *
	 * @param tag The tag to use
	 * @return The string version.
	 */
	public static String nbtString(INBT tag) {
		return NBTFunctions.nbtString(tag);
	}

	/**
	 * Creates an NBT list based on the given float values.
	 *
	 * @param values The varargs array of values.
	 * @return A new list tag.
	 */
	public static ListNBT createFloatListTag(float... values) {
		return NBTFunctions.createFloatListTag(values);
	}

	/**
	 * Creates an NBT list based on the given double values.
	 *
	 * @param values The varargs array of values.
	 * @return A new list tag.
	 */
	public static ListNBT createDoubleListTag(double... values) {
		return NBTFunctions.createDoubleListTag(values);
	}

	/**
	 * Creates an NBT list based on the given short values.
	 *
	 * @param values The varargs array of values.
	 * @return A new list tag.
	 */
	public static ListNBT createShortListTag(short... values) {
		return NBTFunctions.createShortListTag(values);
	}

	/**
	 * Creates an NBT string based on the given string.
	 *
	 * @param value The string to use.
	 * @return A new string tag.
	 */
	public static StringNBT createStringTag(String value) {
		return NBTFunctions.createStringTag(value);
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
	public static CCustomPayloadPacket makePluginMessagePacket(@ChannelName String channel, byte[] bytes) {
		return PacketFunctions.makePluginMessagePacket(channel, bytes);
	}

	/**
	 * Creates a server plugin message packet.  Intended for use in tests.
	 * @param channel The channel to send on.
	 * @param bytes The payload.
	 * @return The new packet.
	 */
	public static SCustomPayloadPlayPacket makeServerPluginMessagePacket(@ChannelName String channel, byte[] bytes) {
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
	public static MapData getMapData(World world, SMapDataPacket mapPacket) {
		return MapFunctions.getMapData(world, mapPacket);
	}

	/**
	 * Gets the ID of the map associated with the given item stack.
	 * Assumes that the item is a map in the first place.
	 *
	 * @param stack The map item stack.
	 * @return The map ID
	 */
	public static int getMapId(ItemStack stack) {
		assert stack.getItem() == Items.FILLED_MAP;
		return MapFunctions.getMapID(stack);
	}

	/**
	 * Returns true if the map has a null dimension.  This can happen in 1.13.1 and later.
	 */
	public static boolean isMapDimensionNull(MapData map) {
		// n.b. this could be written as return (Object)mapData.dimension == null
		// but that's rather awkward and we already need versioned code for other reasons
		// (And, if that's not extracted to its own method, it can create
		// (true, but unwanted) dead code warnings)
		return MapFunctions.isMapDimensionNull(map);
	}

	/**
	 * Sets the map's dimension to the given dimension.  In some versions,
	 * the {@link MapData#dimension} field is a byte, while in other ones it is
	 * a DimensionType (which might start out null).
	 */
	public static void setMapDimension(MapData map, DimensionType dim) {
		MapFunctions.setMapDimension(map, dim);
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

	public static enum GameRuleType {
		INTEGER,
		BOOLEAN;
	}

	/**
	 * Checks if the given game rule is of the given type.
	 * @param rules The rule collection
	 * @param rule The name of the rule
	 * @return The type, or null if no info could be found.
	 */
	@Nullable
	public static GameRuleType getRuleType(GameRules rules, String rule) {
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
	 * Sets the given rule to the given value.  If the rule doesn't exist, throws an exception.
	 * @param rules The rules object.
	 * @param rule The rule's name
	 * @param value The new value
	 */
	public static void setRuleValue(GameRules rules, String rule, String value) {
		GameRuleFunctions.setRuleValue(rules, rule, value);
	}

	/**
	 * Gets a collection of gamerules and their values.
	 * @param rules The rules object.
	 * @return A map of all rule names to their values.
	 */
	public static Map<String, String> getGameRules(GameRules rules) {
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
	public static Screen makeGeneratorSettingsGui(Generator generator, Screen parent,
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
	 * Makes a backup failed toast, on versions that support it.
	 *
	 * @param ex The exception.
	 */
	public static void makeBackupFailedToast(IOException ex) {
		GeneratorFunctions.makeBackupFailedToast(ex);
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
	public static INBT createGeneratorOptionsTag(String generatorOptions) {
		return GeneratorFunctions.createGeneratorOptionsTag(generatorOptions);
	}

	/**
	 * Creates a new instance of {@link ClientPlayerEntity}.
	 *
	 * @param minecraft The minecraft instance
	 * @param world The world
	 * @param nhpc The connection
	 * @param base The original player to copy other data from
	 */
	public static ClientPlayerEntity makePlayer(Minecraft minecraft, World world, ClientPlayNetHandler nhpc, ClientPlayerEntity base) {
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
	 * Opens a link using the default browser.
	 * @param url The URL to open.
	 * @see Screen#openLink()
	 */
	public static void openLink(String url) {
		GuiFunctions.openLink(url);
	}

	/**
	 * Calls glColor4f.
	 */
	public static void glColor4f(float r, float g, float b, float a) {
		GuiFunctions.glColor4f(r, g, b, a);
	}

	/**
	 * Calls glTranslatef.
	 */
	public static void glTranslatef(float x, float y, float z) {
		GuiFunctions.glTranslatef(x, y, z);
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
	 * Gets the class used to store the list of chunks in pending saving in ChunkManager
	 * ({@link ChunkManager#chunksToSave}).
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
	 * @return Either a String or a StringTextComponent, depending on the version; T
	 *         should be inferred to the right one of those.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T customName(String name) {
		return (T)TypeFunctions.customName(name);
	}

	/**
	 * (EVIL) Returns a new instance of GameSettings. This class was repackaged in
	 * 1.13, partially due to my own request out of confusion of how remapping
	 * works. Unfortunately, we can't actually update our own mappings for 1.12,
	 * because liteloader uses this class in various mixins.
	 *
	 * Intended for use in unit tests.
	 *
	 * @param <T> The type that is expected to be returned, which is inferred.
	 * @return A GameSettings instance, but the package containing GameSettings
	 *         might vary.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T createNewGameSettings() {
		return (T)TypeFunctions.createNewGameSettings();
	}

	/**
	 * Gets the class used for GameRenderer, né EntityRenderer. This was renamed in
	 * 1.13.2. However, since liteloader uses it, we can't remap it in older
	 * versions.
	 *
	 * Intended for use in unit tests. Unfortunately, we can't use mock(...)
	 * directly on this class, since we don't have mockito on actual release code
	 * (which this file is). We also can't do the ugly <T> thing here, since that
	 * won't carry through the call to mock. And because this is a wildcard,
	 * mock(...) doesn't necessarily match what we've got either (as far as the
	 * compiler knows).  The only real solution is raw types, though that's ugly.
	 */
	@SuppressWarnings("rawtypes")
	public static Class getGameRendererClass() {
		return TypeFunctions.getGameRendererClass();
	}
}
