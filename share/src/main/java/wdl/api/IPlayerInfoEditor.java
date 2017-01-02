package wdl.api;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.SaveHandler;

/**
 * {@link IWDLMod} that edits the player info NBT file.
 */
public interface IPlayerInfoEditor extends IWDLMod {
	/**
	 * Edits the world info NBT before it is saved.
	 * 
	 * @param player
	 *            The player that is being saved ({@link wdl.WDL#thePlayer})
	 * @param saveHandler
	 *            The current saveHandler ({@link wdl.WDL#saveHandler}).
	 * @param tag
	 *            The current {@link NBTTagCompound} that is being saved. Edit
	 *            or add info to this.
	 */
	public abstract void editPlayerInfo(EntityPlayerSP player,
			SaveHandler saveHandler, NBTTagCompound tag);
}
