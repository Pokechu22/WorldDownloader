package wdl.api;

import java.util.List;

/**
 * Interface for WDL mods that deal with new types of entities.
 * <br/>
 * This is for handling *new* entities that have names that aren't used in
 * vanilla minecraft.  Use {@link ISpecialEntityHandler} if you want to handle
 * using an entity for a non-intended purpose (eg holograms).
 */
public interface IEntityAdder extends IWDLMod {
	/**
	 * Gets a List of all entities that this mod handles.
	 * 
	 * @return A list of all the names of entities that this mod handles. Entity
	 *         names should be the ones that are registered with
	 *         {@link net.minecraft.entity.EntityList}.
	 */
	public abstract List<String> getModEntities();

	/**
	 * Gets the default track distance for the given entity. <br/>
	 * This method will only ever be called with an entity in the list returned
	 * by {@link #getModEntities()}.
	 * 
	 * @param entity
	 *            The name of the entity.
	 * @return The default distance at which the entity is removed from the
	 *         player's view.
	 * 
	 */
	public abstract int getDefaultEntityTrackDistance(String entity);

	/**
	 * Gets the category to put the given entity under. <br/>
	 * It's recommended to put the mod's name here, such as "More creepers". If
	 * the mod contains a lot of entities, you can divide it into subcategories.
	 * This data used for the Entity gui.
	 * 
	 * @param entity The name of the entity.
	 * @return The category.
	 */
	public abstract String getEntityCategory(String entity);
}
