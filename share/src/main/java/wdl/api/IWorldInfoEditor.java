package wdl.api;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;

/**
 * {@link IWDLMod} that edits the world info NBT file (level.dat).
 */
public interface IWorldInfoEditor extends IWDLMod {
	/**
	 * Edits the world info NBT before it is saved.
	 * 
	 * @param world
	 *            The world that is being saved ({@link wdl.WDL#worldClient})
	 * @param info
	 *            The given world's {@link WorldInfo}.
	 * @param saveHandler
	 *            The current saveHandler ({@link wdl.WDL#saveHandler}).
	 * @param tag
	 *            The current {@link NBTTagCompound} that is being saved. Edit
	 *            or add info to this.
	 */
	public abstract void editWorldInfo(WorldClient world, WorldInfo info,
			SaveHandler saveHandler, NBTTagCompound tag);
}
