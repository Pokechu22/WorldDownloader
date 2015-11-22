package wdl.api;

import net.minecraft.tileentity.TileEntity;

/**
 * Something that can edit tile entities as they are being saved.
 */
public interface ITileEntityEditor {
	/**
	 * Should the given tile entity be edited by this {@link ITileEntityEditor}?
	 * 
	 * A simple implementation may just use a <code>instanceof</code> check, but
	 * more fancy things can be done.
	 * 
	 * The tile entity's position is set by the time this method is called.
	 * 
	 * @param te
	 *            The tile entity to check.
	 * @param wasImported
	 *            Whether the tile entity was saved during this downloading (
	 *            <code>false</code>) or was imported from the previously saved
	 *            copy of the chunks (<code>true</code>).
	 * @return Whether it should be edited.
	 */
	public abstract boolean shouldEdit(TileEntity te, boolean wasImported);
	
	/**
	 * Edit the given tile entity. Will only be called if
	 * {@link #shouldEdit(TileEntity, boolean)} returned true.
	 * 
	 * @param te
	 *            The tile entity to check.
	 * @param wasImported
	 *            Whether the tile entity was saved during this downloading (
	 *            <code>false</code>) or was imported from the previously saved
	 *            copy of the chunks (<code>true</code>).
	 * @return The modified tile entity.
	 */
	public abstract TileEntity editTileEntity(TileEntity te, boolean wasImported);
}
