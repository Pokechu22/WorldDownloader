/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2020 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui.widget;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;

/**
 * Extendible text field, to deal with changing constructors between versions.
 * The actual implementation is {@link WDLTextField}.
 */
abstract class ExtTextField extends TextFieldWidget {
	/**
	 * @deprecated Do not use; use {@link #setEnabled} instead.
	 */
	@Deprecated
	protected static final Void active = null;
	@Nullable
	private MatrixStack matrixStack = null;

	public ExtTextField(FontRenderer fontRenderer, int x, int y, int width, int height, ITextComponent label) {
		super(fontRenderer, x, y, width, height, label);
	}
}
