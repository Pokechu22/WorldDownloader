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
