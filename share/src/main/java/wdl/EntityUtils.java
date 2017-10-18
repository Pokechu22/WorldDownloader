/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.command.CommandException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import wdl.api.IEntityManager;
import wdl.api.WDLApi;
import wdl.api.WDLApi.ModInfo;

/**
 * Provides utility functions for recognizing entities.
 */
public class EntityUtils {
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Gets a collection of all types of entities, both basic ones and special
	 * entities.
	 *
	 * This value is calculated each time and is not cached.
	 */
	public static Set<String> getEntityTypes() {
		Set<String> set = new HashSet<>();
		for (IEntityManager manager : getEntityManagers()) {
			for (String type : manager.getProvidedEntities()) {
				set.add(type);
			}
		}
		return set;
	}
	/**
	 * Gets a collection of all active IEntityManager in order.
	 */
	public static List<IEntityManager> getEntityManagers() {
		// XXX This order isn't necessarily the one a user would want
		List<IEntityManager> managers = new ArrayList<>();
		for (ModInfo<IEntityManager> info : WDLApi.getImplementingExtensions(IEntityManager.class)) {
			managers.add(info.mod);
		}
		managers.addAll(StandardEntityManagers.DEFAULTS);
		return managers;
	}

	/**
	 * Gets a multimap of entity groups to entity types, for both regular
	 * entities and special entities.  The group is the key.
	 *
	 * This value is calculated each time and is not cached.
	 */
	public static Multimap<String, String> getEntitiesByGroup() {
		Multimap<String, String> returned = HashMultimap.create();

		Set<String> types = getEntityTypes();

		for (String type : types) {
			returned.put(getEntityGroup(type), type);
		}

		return returned;
	}

	/**
	 * Gets the track distance for the given entity in the current mode.
	 *
	 * @param entity
	 * @return
	 */
	public static int getEntityTrackDistance(@Nonnull Entity entity) {
		String type = getEntityType(entity);
		if (type == null) {
			return -1;
		}
		return getEntityTrackDistance(getTrackDistanceMode(), type, entity);
	}

	/**
	 * Gets the track distance for the given entity in the current mode.
	 *
	 * @param type
	 * @return
	 */
	public static int getEntityTrackDistance(@Nonnull String type) {
		return getEntityTrackDistance(getTrackDistanceMode(), type, null);
	}

	/**
	 * Gets the track distance for the given entity in the specified mode.
	 *
	 * @param mode
	 * @param type
	 * @param entity
	 * @return
	 */
	public static int getEntityTrackDistance(String mode, @Nonnull String type, @Nullable Entity entity) {
		if ("default".equals(mode)) {
			for (IEntityManager manager : getEntityManagers()) {
				if (!manager.getProvidedEntities().contains(type)) {
					continue;
				}
				int distance = manager.getTrackDistance(type, entity);
				if (distance != -1) {
					return distance;
				}
			}
			LOGGER.warn("Failed to get track distance for " + type + " (" + entity + ")");
			return -1;
		} else if ("server".equals(mode)) {
			int serverDistance = WDLPluginChannels
					.getEntityRange(type);

			if (serverDistance < 0) {
				return getEntityTrackDistance("default", type, entity);
			}

			return serverDistance;
		} else if ("user".equals(mode)) {
			String prop = WDL.worldProps.getProperty("Entity." +
					type + ".TrackDistance", "-1");

			int value = Integer.valueOf(prop);

			if (value == -1) {
				return getEntityTrackDistance("server", type, entity);
			} else {
				return value;
			}
		} else {
			throw new IllegalArgumentException("Mode is not a valid mode: " + mode);
		}
	}

	/**
	 * Gets the group for the given entity type.
	 *
	 * @param identifier
	 * @return The group, or "Unknown" if none is found.
	 */
	@Nonnull
	public static String getEntityGroup(@Nonnull String identifier) {
		for (IEntityManager manager : getEntityManagers()) {
			if (!manager.getProvidedEntities().contains(identifier)) {
				continue;
			}
			String group = manager.getGroup(identifier);
			if (group != null) {
				return group;
			}
		}
		LOGGER.warn("Failed to find entity group for " + identifier);
		return "Unknown";
	}

	/**
	 * Checks if an entity is enabled.
	 *
	 * @param e The entity to check.
	 * @return
	 */
	public static boolean isEntityEnabled(@Nonnull Entity e) {
		String type = getEntityType(e);
		if (type == null) {
			return false;
		} else {
			return isEntityEnabled(type);
		}
	}

	/**
	 * Checks if an entity is enabled.
	 *
	 * @param type The type of the entity (from {@link #getEntityType(Entity)})
	 * @return
	 */
	public static boolean isEntityEnabled(@Nonnull String type) {
		boolean groupEnabled = WDL.worldProps.getProperty("EntityGroup." +
				getEntityGroup(type) + ".Enabled", "true").equals("true");
		boolean singleEnabled = WDL.worldProps.getProperty("Entity." +
				type + ".Enabled", "true").equals("true");

		return groupEnabled && singleEnabled;
	}

	/**
	 * Gets the type string for an entity.
	 *
	 * @param e
	 * @return
	 */
	@Nullable
	public static String getEntityType(@Nonnull Entity e) {
		if (e instanceof EntityPlayer || e instanceof EntityLightningBolt) {
			// These entities can't be saved at all; it's normal that they won't
			// be classified.
			return null;
		}
		if (e == null) {
			LOGGER.warn("Can't get type for null entity", new Exception());
			return null;
		}

		for (IEntityManager manager : getEntityManagers()) {
			String type = manager.getIdentifierFor(e);
			if (type != null) {
				return type;
			}
		}
		LOGGER.warn("Failed to classify entity " + e);
		return null;
	}
	/**
	 * Gets the currently selected track distance mode from {@link WDL#worldProps}.
	 */
	public static String getTrackDistanceMode() {
		return WDL.worldProps.getProperty("Entity.TrackDistanceMode", "server");
	}

	/**
	 * Gets the display name for the given entity type. As a last resort,
	 * returns the type itself.
	 */
	@Nonnull
	public static String getDisplayType(@Nonnull String identifier) {
		for (IEntityManager manager : getEntityManagers()) {
			if (!manager.getProvidedEntities().contains(identifier)) {
				continue;
			}
			String displayIdentifier = manager.getDisplayIdentifier(identifier);
			if (displayIdentifier != null) {
				return displayIdentifier;
			}
		}
		LOGGER.debug("Failed to get display name for " + identifier);
		return identifier;
	}
	/**
	 * Gets the display name for the given entity group. As a last resort,
	 * returns the group name itself.
	 */
	@Nonnull
	public static String getDisplayGroup(@Nonnull String group) {
		for (IEntityManager manager : getEntityManagers()) {
			String displayGroup = manager.getDisplayGroup(group);
			if (displayGroup != null) {
				return displayGroup;
			}
		}
		LOGGER.debug("Failed to get display name for group " + group);
		return group;
	}

	/**
	 * Entity types as classified by spigot.
	 */
	public static enum SpigotEntityType {
		// PLAYER(48, "Players"), // We don't save players
		ANIMAL(48, "Animals"),
		MONSTER(48, "Monsters"),
		MISC(32, "Misc."),
		OTHER(64, "Other"),
		UNKNOWN(-1, "N/A");

		private final int defaultRange;
		private final String descriptionKey;

		private SpigotEntityType(int defaultRange, String descriptionKey) {
			this.defaultRange = defaultRange;
			this.descriptionKey = descriptionKey;
		}

		public int getDefaultRange() {
			return defaultRange;
		}

		public String getDescription() {
			// XXX this should be translated
			return descriptionKey;
		}
	}

	/**
	 * A map mapping villager professions to a map from each career's I18n name to
	 * the career's ID.
	 *
	 * @see https://minecraft.gamepedia.com/Villager#Professions_and_careers
	 * @see EntityVillager#getDisplayName
	 */
	private static final Int2ObjectMap<BiMap<String, Integer>> VANILLA_VILLAGER_CAREERS = new Int2ObjectArrayMap<>();
	static {
		BiMap<String, Integer> farmer = HashBiMap.create(4);
		farmer.put("entity.Villager.farmer", 1);
		farmer.put("entity.Villager.fisherman", 2);
		farmer.put("entity.Villager.shepherd", 3);
		farmer.put("entity.Villager.fletcher", 4);
		BiMap<String, Integer> librarian = HashBiMap.create(2);
		librarian.put("entity.Villager.librarian", 1);
		// Since 1.11, but safe to include in 1.10 as the actual name won't appear then
		librarian.put("entity.Villager.cartographer", 2);
		BiMap<String, Integer> priest = HashBiMap.create(1);
		priest.put("entity.Villager.cleric", 1);
		BiMap<String, Integer> blacksmith = HashBiMap.create(3);
		blacksmith.put("entity.Villager.armor", 1);
		blacksmith.put("entity.Villager.weapon", 2);
		blacksmith.put("entity.Villager.tool", 3);
		BiMap<String, Integer> butcher = HashBiMap.create(2);
		butcher.put("entity.Villager.butcher", 1);
		butcher.put("entity.Villager.leather", 2);
		BiMap<String, Integer> nitwit = HashBiMap.create(1);
		nitwit.put("entity.Villager.nitwit", 1);

		VANILLA_VILLAGER_CAREERS.put(0, farmer);
		VANILLA_VILLAGER_CAREERS.put(1, librarian);
		VANILLA_VILLAGER_CAREERS.put(2, priest);
		VANILLA_VILLAGER_CAREERS.put(3, blacksmith);
		VANILLA_VILLAGER_CAREERS.put(4, butcher);
		VANILLA_VILLAGER_CAREERS.put(5, nitwit);
	}

	/**
	 * Gets the career ID associated with the given translation name and villager
	 * profession ID.
	 *
	 * @implNote Does not handle forge
	 * @param profession
	 *            The current profession of the villager
	 * @param i18nKey
	 *            The i18n key used in the villager GUI
	 * @throws CommandException when a known issue occurs (bad data).  Contains translation info.
	 */
	public static int getCareer(String i18nKey, int profession) throws CommandException {
		// '%s' is not a translation key that corresponds with a known villager career.
		if (!VANILLA_VILLAGER_CAREERS.containsKey(profession)) {
			throw new CommandException("wdl.messages.onGuiClosedWarning.villagerCareer.unknownProfession", profession);
		}

		BiMap<String, Integer> careerData = VANILLA_VILLAGER_CAREERS.get(profession);

		if (!careerData.containsKey(i18nKey)) {
			throw new CommandException("wdl.messages.onGuiClosedWarning.villagerCareer.unknownTitle", i18nKey, profession);
		}

		return careerData.get(i18nKey);
	}
}
