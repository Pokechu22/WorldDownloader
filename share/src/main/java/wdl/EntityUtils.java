/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import wdl.api.IEntityManager;
import wdl.api.WDLApi;
import wdl.api.WDLApi.ModInfo;
import wdl.config.settings.EntitySettings;
import wdl.config.settings.EntitySettings.TrackDistanceMode;

/**
 * Provides utility functions for recognizing entities.
 */
public class EntityUtils {
	private static final Logger LOGGER = LogManager.getLogger();

	/** The entity manager used on vanilla. */
	public static final IEntityManager STANDARD_VANILLA_MANAGER = StandardEntityManagers.VANILLA;
	/** The entity manager used on spigot. */
	public static final ISpigotEntityManager STANDARD_SPIGOT_MANAGER = StandardEntityManagers.SPIGOT;

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
		managers.add(STANDARD_SPIGOT_MANAGER);
		managers.add(STANDARD_VANILLA_MANAGER);
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
	@CheckForSigned
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
	@CheckForSigned
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
	@CheckForSigned
	public static int getEntityTrackDistance(TrackDistanceMode mode, @Nonnull String type, @Nullable Entity entity) {
		switch (mode) {
		case DEFAULT: {
			for (IEntityManager manager : getEntityManagers()) {
				if (!manager.getProvidedEntities().contains(type)) {
					continue;
				}
				int distance = manager.getTrackDistance(type, entity);
				if (distance >= 0) {
					return distance;
				}
			}
			LOGGER.warn("Failed to get track distance for " + type + " (" + entity + ")");
			return -1;
		}
		case SERVER: {
			int serverDistance = WDLPluginChannels
					.getEntityRange(type);

			if (serverDistance < 0) {
				return getEntityTrackDistance(TrackDistanceMode.DEFAULT, type, entity);
			}

			return serverDistance;
		} 
		case USER: {
			int value = WDL.INSTANCE.worldProps.getUserEntityTrackDistance(type);

			if (value < 0) {
				return getEntityTrackDistance(TrackDistanceMode.SERVER, type, entity);
			} else {
				return value;
			}
		}
		}
		throw new IllegalArgumentException("Mode is not a valid mode: " + mode);
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
		boolean groupEnabled = WDL.INSTANCE.worldProps.isEntityGroupEnabled(getEntityGroup(type));
		boolean singleEnabled = WDL.INSTANCE.worldProps.isEntityTypeEnabled(type);

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
	public static TrackDistanceMode getTrackDistanceMode() {
		return WDL.INSTANCE.worldProps.getValue(EntitySettings.TRACK_DISTANCE_MODE);
	}

	/**
	 * Gets the display name for the given entity type. As a last resort,
	 * returns the type itself (this may be hit due to MC-68446).
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
	 * Checks if an entity should be saved, after it was removed clientside. An
	 * entity is untracked in two cases: when it dies, and when it moves too far
	 * away from the player. We only want to save the entity in the latter case. The
	 * case can be determined by the distance.
	 *
	 * Note that the distance case is <em>not</em> related to rendering; generally
	 * the client knows about entities slightly further away than it renders them,
	 * and entities are not untracked for vertical distance differences but only x/z
	 * differences (and in fact, it isn't a spherical or circular distance, but
	 * instead a square one).
	 *
	 * @param entity        The entity to test with.
	 * @param player        The player (used for position checks).
	 * @param trackDistance The track distance for the entity, probably from
	 *                      {@link #getEntityTrackDistance(Entity)}.
	 * @param viewDistance  The server's view-distance value, in chunks.
	 * @return True if the entity should be saved, false if it should be removed.
	 * @see EntityTrackerEntry#isVisibleTo EntityTrackerEntry.isVisibleTo (source of
	 *      this logic)
	 */
	public static boolean isWithinSavingDistance(Entity entity, Entity player,
			int trackDistance, int viewDistance) {
		// Ref EntityTracker.setViewDistance and PlayerList.getFurthestViewableBlock
		// (note that PlayerChunkMap.getFurthestViewableBlock is a misleading name)
		int maxRange = (viewDistance - 1) * 16;

		int threshold = Math.min(trackDistance, maxRange);

		// Entity track distance is a square, see EntityTrackerEntry.isVisibleTo
		double dx = Math.abs(entity.posX - player.posX);
		double dz = Math.abs(entity.posZ - player.posZ);

		double distance = Math.max(dx, dz);

		LOGGER.debug("removeEntity: {} is at distance {} from {} (dx {}, dz {}); configured track distance is {}"
				+ " and server distance for view distance {} is {}.  Entity kept: {}",
				entity, distance, player, dx, dz, trackDistance, viewDistance, maxRange, (distance > threshold));

		return distance > threshold;
	}

	/**
	 * Checks if the given entity type is specified as being enabled by default.
	 * A few dangerous entities are disabled by default.
	 *
	 * This method is used by the default configuration.
	 *
	 * @param identifier The type of entity.
	 * @return True if the entity is enabled by default, false if it isn't.
	 */
	public static boolean isEntityEnabledByDefault(String identifier) {
		for (IEntityManager manager : getEntityManagers()) {
			if (!manager.getProvidedEntities().contains(identifier)) {
				continue;
			}
			return manager.enabledByDefault(identifier);
		}
		// XXX Should an unknown entity be enabled by default?  Currently returning true,
		// but it'll when the track distance is -1
		return true;
	}

	public static interface ISpigotEntityManager extends IEntityManager {
		@Nonnull
		public abstract SpigotEntityType getSpigotType(String identifier);
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
}
