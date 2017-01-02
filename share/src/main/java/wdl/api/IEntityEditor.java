package wdl.api;

import net.minecraft.entity.Entity;

/**
 * Something that can edit entities as they are being saved.
 */
public interface IEntityEditor extends IWDLMod {
	/**
	 * Should the given entity be edited by this {@link IEntityEditor}?
	 * 
	 * A simple implementation may just use a <code>instanceof</code> check, but
	 * more fancy things can be done.
	 * 
	 * @param e
	 *            The entity to check.
	 * @return Whether it should be edited.
	 */
	public abstract boolean shouldEdit(Entity e);
	
	/**
	 * Edit the given tile entity. Will only be called if
	 * {@link #shouldEdit(Entity)} returned true. This entity should be modified
	 * "in-place".
	 * 
	 * @param e
	 *            The entity to edit.
	 */
	public abstract void editEntity(Entity e);
}
