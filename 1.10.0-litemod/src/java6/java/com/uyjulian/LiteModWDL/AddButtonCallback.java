/*
 * This file is part of LiteModWDL.  LiteModWDL contains the liteloader-specific
 * code for World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package com.uyjulian.LiteModWDL;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiButton;

/**
 * A callback that adds a button to the given list.
 *
 * This cannot be an inner class, because that isn't supported until a much later mixin version.
 * Nor can it be a lambda for this::addButton, as we are required to use Java 6.
 */
public class AddButtonCallback implements Consumer<GuiButton> {
	private final List<GuiButton> buttons;
	public AddButtonCallback(List<GuiButton> buttons) {
		this.buttons = buttons;
	}

	@Override
	public void accept(GuiButton btn) {
		this.buttons.add(btn);
	}
}
