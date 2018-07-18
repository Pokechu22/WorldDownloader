/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui.widget;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import wdl.gui.LocalUtils;
import wdl.gui.widget.GuiList.GuiListEntry;

public abstract class GuiList<T extends GuiListEntry> extends ExtGuiList<T> {
	public GuiList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int entryHeight) {
		super(mcIn, widthIn, heightIn, topIn, bottomIn, entryHeight);
	}

	public static class GuiListEntry extends ExtGuiListEntry {
		private static class ButtonWrapper {
			public final ExtButton button;
			public final int x;
			public final int y;
			public ButtonWrapper(ExtButton button, int x, int y) {
				this.button = button;
				this.x = x;
				this.y = y;
			}
		}
		private final List<ButtonWrapper> buttonList = new ArrayList<>();
		@Nullable
		private ButtonWrapper activeButton;

		/**
		 * Adds a button.
		 * 
		 * @param button The button.
		 * @param x      x coordinate relative to the center of the screen (may be
		 *               negative).
		 * @param y      y coordinate relative to the top of the entry.
		 */
		protected void addButton(ExtButton button, int x, int y) {
			this.buttonList.add(new ButtonWrapper(button, x, y));
		}

		@Override
		public void drawEntry(int x, int y, int width, int height, int mouseX, int mouseY) {
			for (ButtonWrapper button : this.buttonList) {
				button.button.x = button.x + x + (width / 2);
				button.button.y = button.y + y;
				LocalUtils.drawButton(button.button, mouseX, mouseY);
			}
		}

		@Override
		public boolean mouseDown(int mouseX, int mouseY, int mouseButton) {
			for (ButtonWrapper buttonW : this.buttonList) {
				ExtButton button = buttonW.button;
				if (button.isMouseOver()) {
					this.activeButton = buttonW;
					button.mouseDown(mouseX, mouseY);
					button.playPressSound(Minecraft.getMinecraft().getSoundHandler());
					return true;
				}
			}
			return false;
		}

		@Override
		public void mouseUp(int mouseX, int mouseY, int mouseButton) {
			if (this.activeButton != null) {
				this.activeButton.button.mouseUp(mouseX, mouseY);
				this.activeButton = null;
			}
		}

		@Override
		public boolean isSelected() {
			return false;
		}
	}

	//private int verticalOffset;

	@Override
	public void setVerticalOffset(int offset) {
//		this.verticalOffset = offset;
	}

	@Override
	public int getEntryWidth() {
		return 220;
	}

	@Override
	public int getScrollBarX() {
		return (getWidth() / 2) + (getEntryWidth() / 2) + 4;
	}
}
