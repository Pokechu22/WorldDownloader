/*
 * This file is part of the World Downloader API.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2017-2020 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * You are free to include the World Downloader API within your own mods, as
 * permitted via the MMPLv2.
 */
package wdl.api;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

/**
 * {@link IWDLMod} that edits the player info NBT file.
 */
public interface IPlayerInfoEditor extends IWDLMod {
	/**
	 * Edits the world info NBT before it is saved.
	 *
	 * @param player
	 *            The player that is being saved ({@link wdl.WDL#player})
	 * @param saveHandler
	 *            The current saveHandler ({@link wdl.WDL#saveHandler}).
	 * @param tag
	 *            The current {@link NBTTagCompound} that is being saved. Edit
	 *            or add info to this.
	 */
	public abstract void editPlayerInfo(ClientPlayerEntity player,
			Object saveHandler, CompoundNBT tag);
}
