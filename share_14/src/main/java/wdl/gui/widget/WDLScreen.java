/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
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
package wdl.gui.widget;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * A specialty screen that can store text fields and lists.
 */
public class WDLScreen extends ExtGuiScreen {

	public WDLScreen(String titleI18nKey) {
		this(new TranslationTextComponent(titleI18nKey));
	}

	public WDLScreen(ITextComponent title) {
		super(title);
	}

	@Override
	public void mouseDown(int mouseX, int mouseY) { }

	@Override
	public void mouseDragged(int mouseX, int mouseY) { }

	@Override
	public void mouseUp(int mouseX, int mouseY) { }

	@Override
	public void charTyped(char keyChar) { }

	@Override
	public void anyKeyPressed() { }

	@Override
	public boolean onCloseAttempt() {
		return true;
	}
}
