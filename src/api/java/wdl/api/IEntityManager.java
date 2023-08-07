/*
 * This file is part of the World Downloader API.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * You are free to include the World Downloader API within your own mods, as
 * permitted via the MMPLv2.
 */
package wdl.api;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;

/**
 * Used to manage and produce a list of entities and their track distances.
 * Multiple implementations will usually be present at once, with different
 * priorities.
 */
public interface IEntityManager extends IWDLMod {
	/**
	 * Gets a list of entity identifiers known to this extension.
	 * <p>
	 * There is no requirement for the structure of these names, but it is
	 * recommended that implementations use the conventional name for the entity
	 * (as would be found in {@link net.minecraft.entity.EntityList}). Matching
	 * names will be merged by the mod itself, which is beneficial.
	 * <p>
	 * However, non-standard entities don't have a standardized name. Thus, they
	 * should use the name that works best for them. For instance, entities
	 * added by forge may not have a canonical name (though most will).
	 * Implementations are free to use whatever names they want in this
	 * circumstance.
	 * <p>
	 * For entities which are special-cases composed of normal entities (such as
	 * holograms or player disguises) a valid convention would be to prefix the
	 * entity name with "x".
	 *
	 * @return A list of entity identifiers known to this extension
	 */
	@Nonnull
	public abstract Set<String> getProvidedEntities();

	/**
	 * Gets the identifier for an entity.
	 * <p>
	 * This method will be called sequentially on all instances of this class,
	 * in order of priority, until one returns a non-<code>null</code> value.
	 *
	 * @param entity
	 *            The entity to identify.
	 * @return The entity identifier, or <code>null</code> if this extension has
	 *         no name for that entity. <br>
	 *         If not <code>null</code>, the name MUST be one of the names
	 *         included in {@link #getProvidedEntities()}.
	 * @see #getProvidedEntities()
	 */
	@Nullable
	public abstract String getIdentifierFor(@Nonnull Entity entity);

	/**
	 * Gets the track distance for the given entity.
	 * <p>
	 * The track distance is the distance in meters (blocks), horizontally (but
	 * not taxicab) at which a server removes an entity from a client's view,
	 * sending a remove entity packet. It isn't necessarily the distance at
	 * which an entity stops rendering.
	 * <p>
	 * For vanilla entities, these values are provided in
	 * {@link net.minecraft.entity.EntityTracker#track(Entity)}.
	 * <p>
	 * This method will be called when {@link #getProvidedEntities()} includes
	 * the given identifier, even if this provider did not identify the entity.
	 *
	 * @param identifier
	 *            The identifier of the type of entity to get a track distance
	 *            for.
	 * @param entity
	 *            The entity that needs a track distance. May be null in static
	 *            situations, such as the entity list GUI. Provided for more
	 *            complicated situations; prefer the identifier when possible.
	 * @return The entity identifier, or <code>-1</code> if this extension has
	 *         no track distance for that entity. Note that it is legal to
	 *         return -1 even if this implementation did directly identify the
	 *         entity.
	 * @see #getProvidedEntities()
	 */
	public abstract int getTrackDistance(@Nonnull String identifier, @Nullable Entity entity);

	/**
	 * Gets an internal ID for the group to put that entity in. Used for
	 * grouping and storing data.
	 * <p>
	 * This method will be called when {@link #getProvidedEntities()} includes
	 * the given identifier, even if this provider did not identify the entity.
	 *
	 * @param identifier
	 *            The identifier.
	 * @return The group for the entity: an internal ID.
	 */
	@Nullable
	public abstract String getGroup(@Nonnull String identifier);

	/**
	 * Gets the player-visible display identifier for the given entity
	 * identifier.
	 * <p>
	 * This method will be called when {@link #getProvidedEntities()} includes
	 * the given identifier, even if this provider did not identify the entity.
	 *
	 * @param identifier
	 *            The internal identifier for the entity.
	 * @return The display version of the identifier, potentially translated, or
	 *         <code>null</code> if no translated identifier could be provided.
	 */
	@Nullable
	public abstract String getDisplayIdentifier(@Nonnull String identifier);

	/**
	 * Gets the player-visible name for the given entity group.
	 *
	 * @param group
	 *            The internal group name.
	 * @return The display name for the given group, potentially translated, or
	 *         <code>null</code> if no translated group name could be provided.
	 */
	@Nullable
	public abstract String getDisplayGroup(@Nonnull String group);

	/**
	 * Is the given type of entity enabled by default?
	 *
	 * @param identifier
	 *            The ID for the entity.
	 * @return True if that type of entity should be saved by default.
	 */
	public abstract boolean enabledByDefault(@Nonnull String identifier);
}
