package wdl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wdl.api.IEntityManager;
import wdl.api.WDLApi;
import wdl.api.WDLApi.ModInfo;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Provides utility functions for recognizing entities.
 */
public class EntityUtils {
	static final Logger logger = LogManager.getLogger();

	/**
	 * Gets a collection of all types of entities, both basic ones and special
	 * entities.
	 *
	 * This value is calculated each time and is not cached.
	 */
	public static Set<String> getEntityTypes() {
		Set<String> set = new HashSet<String>();
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
		List<IEntityManager> managers = new ArrayList<IEntityManager>();
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
			logger.warn("Failed to get track distance for " + type + " (" + entity + ")");
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
		logger.warn("Failed to find entity group for " + identifier);
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
			logger.warn("Can't get type for null entity", new Exception());
			return null;
		}

		for (IEntityManager manager : getEntityManagers()) {
			String type = manager.getIdentifierFor(e);
			if (type != null) {
				return type;
			}
		}
		logger.warn("Failed to classify entity " + e);
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
		logger.debug("Failed to get display name for " + identifier);
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
		logger.debug("Failed to get display name for group " + group);
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
}
