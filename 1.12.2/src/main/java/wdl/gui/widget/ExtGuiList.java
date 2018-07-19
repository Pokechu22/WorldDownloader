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

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiTextField;
import wdl.gui.widget.ExtGuiList.ExtGuiListEntry;

abstract class ExtGuiList<T extends ExtGuiListEntry<T>> extends GuiListExtended implements IExtGuiList<T> {

	static abstract class ExtGuiListEntry<T extends ExtGuiListEntry<T>> implements IExtGuiListEntry<T>, IGuiListEntry {

		@Override
		public final void updatePosition(int slotIndex, int x, int y, float partialTicks) { }

		private static class ButtonWrapper {
			public final GuiButton button;
			public final int x;
			public final int y;
			public ButtonWrapper(GuiButton button, int x, int y) {
				this.button = button;
				this.x = x;
				this.y = y;
			}
		}
		private static class TextFieldWrapper {
			public final GuiTextField field;
			public final int x;
			public final int y;
			public TextFieldWrapper(GuiTextField field, int x, int y) {
				this.field = field;
				this.x = x;
				this.y = y;
			}
		}

		private final List<ButtonWrapper> buttonList = new ArrayList<>();
		private final List<TextFieldWrapper> fieldList = new ArrayList<>();
		@Nullable
		private ButtonWrapper activeButton;

		@Override
		public final void addButton(GuiButton button, int x, int y) {
			this.buttonList.add(new ButtonWrapper(button, x, y));
		}

		@Override
		public final void addTextField(GuiTextField field, int x, int y) {
			this.fieldList.add(new TextFieldWrapper(field, x, y));
		}

		@Override
		public final void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
				boolean isSelected, float partialTicks) {
			this.drawEntry(x, y, listWidth, slotHeight, mouseX, mouseY);
			for (ButtonWrapper button : this.buttonList) {
				button.button.x = button.x + x + (listWidth / 2);
				button.button.y = button.y + y;
				button.button.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);
			}
			for (TextFieldWrapper field : this.fieldList) {
				field.field.x = field.x + x + (listWidth / 2);
				field.field.y = field.y + y;
				field.field.drawTextBox();
			}
		}

		@Override
		public final boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX,
				int relativeY) {
			for (ButtonWrapper button : this.buttonList) {
				if (button.button.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
					this.activeButton = button;
					button.button.playPressSound(Minecraft.getMinecraft().getSoundHandler());
					return true;
				}
			}
			for (TextFieldWrapper field : this.fieldList) {
				if (field.field.getVisible()) {
					field.field.mouseClicked(mouseX, mouseY, mouseEvent);
				}
			}
			return this.mouseDown(mouseX, mouseY, mouseEvent);
		}

		@Override
		public final void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			if (this.activeButton != null) {
				this.activeButton.button.mouseReleased(x, y);
				this.activeButton = null;
				return;
			}
			this.mouseUp(x, y, mouseEvent);
		}

		final void keyTyped(char typedChar, int keyCode) {
			for (TextFieldWrapper field : this.fieldList) {
				if (field.field.getVisible()) {
					field.field.textboxKeyTyped(typedChar, keyCode);
				}
			}
			if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
				typedChar = '\n';
			}
			if (typedChar != 0) {
				this.charTyped(typedChar);
			}
		}

		final void updateScreen() {
			for (TextFieldWrapper field : this.fieldList) {
				field.field.updateCursorCounter();
			}
		}
	}

	public ExtGuiList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
		super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
	}

	private final List<T> entries = new ArrayList<>();

	@Override
	public List<T> getEntries() {
		return entries;
	}

	@Override
	protected final boolean isSelected(int slotIndex) {
		return entries.get(slotIndex).isSelected();
	}

	@Override
	public final IGuiListEntry getListEntry(int index) {
		return entries.get(index);
	}

	@Override
	protected final int getSize() {
		return entries.size();
	}

	final void updateScreen() {
		for (T t : this.entries) {
			t.updateScreen();
		}
	}

	final void keyTyped(char typedChar, int keyCode) {
		for (T t : this.entries) {
			t.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	public final int getListWidth() {
		return this.getEntryWidth();
	}

	@Override
	public abstract int getScrollBarX();

	@Override
	public void scroll(double by) {
		this.scrollBy((int)by);
	}

	@Override
	public final int getWidth() {
		return this.width;
	}
}
