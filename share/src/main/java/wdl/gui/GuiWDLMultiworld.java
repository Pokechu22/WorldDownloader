/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui;

import java.util.List;

import net.minecraft.client.resources.I18n;
import wdl.gui.widget.WDLButton;
import wdl.gui.widget.WDLScreen;

public class GuiWDLMultiworld extends WDLScreen {
	private final MultiworldCallback callback;
	private WDLButton multiworldEnabledBtn;
	private boolean enableMultiworld = false;

	//TODO: Some of these things can be constants, but for consistancy aren't.
	//Maybe refactor it?
	private int infoBoxWidth;
	private int infoBoxHeight;
	private int infoBoxX;
	private int infoBoxY;
	private List<String> infoBoxLines;

	public static interface MultiworldCallback {
		public abstract void onCancel();
		public abstract void onSelect(boolean enableMutliworld);
	}

	public GuiWDLMultiworld(MultiworldCallback callback) {
		super("wdl.gui.multiworld.title");
		this.callback = callback;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void init() {
		String multiworldMessage = I18n
				.format("wdl.gui.multiworld.descirption.requiredWhen")
				+ "\n\n"
				+ I18n.format("wdl.gui.multiworld.descirption.whatIs");

		infoBoxWidth = 320;
		infoBoxLines = Utils.wordWrap(multiworldMessage, infoBoxWidth - 20);
		infoBoxHeight = (font.FONT_HEIGHT * (infoBoxLines.size() + 1)) + 40;

		infoBoxX = this.width / 2 - infoBoxWidth / 2;
		infoBoxY = this.height / 2 - infoBoxHeight / 2;

		this.multiworldEnabledBtn = this.addButton(new WDLButton(
				this.width / 2 - 100, infoBoxY + infoBoxHeight - 30, 200, 20,
				this.getMultiworldEnabledText()) {
			public @Override void performAction() {
				toggleMultiworldEnabled();
			}
		});

		this.addButton(new WDLButton(this.width / 2 - 155,
				this.height - 29, 150, 20, I18n.format("gui.cancel")) {
			public @Override void performAction() {
				callback.onCancel();
			}
		});

		this.addButton(new WDLButton(this.width / 2 + 5,
				this.height - 29, 150, 20, I18n.format("gui.done")) {
			public @Override void performAction() {
				callback.onSelect(enableMultiworld);
			}
		});
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		this.renderBackground();
		Utils.drawBorder(32, 32, 0, 0, height, width);

		fill(infoBoxX, infoBoxY, infoBoxX + infoBoxWidth, infoBoxY
				+ infoBoxHeight, 0xB0000000);

		int x = infoBoxX + 10;
		int y = infoBoxY + 10;

		for (String s : infoBoxLines) {
			this.drawString(font, s, x, y, 0xFFFFFF);
			y += font.FONT_HEIGHT;
		}

		//Red box around "multiworld support" button.
		fill(multiworldEnabledBtn.x - 2,
				multiworldEnabledBtn.y - 2,
				multiworldEnabledBtn.x
				+ multiworldEnabledBtn.getWidth() + 2,
				multiworldEnabledBtn.y + 20 + 2, 0xFFFF0000);

		super.render(mouseX, mouseY, partialTicks);
	}

	/**
	 * Toggles whether multiworld support is enabled.
	 */
	private void toggleMultiworldEnabled() {
		if (this.enableMultiworld) {
			this.enableMultiworld = false;
		} else {
			this.enableMultiworld = true;
		}

		this.multiworldEnabledBtn.setMessage(getMultiworldEnabledText());
	}

	/**
	 * Gets the text to display on the multiworld enabled button.
	 */
	private String getMultiworldEnabledText() {
		return I18n.format("wdl.gui.multiworld." + enableMultiworld);
	}

	@Override
	public boolean onCloseAttempt() {
		callback.onCancel();
		return true;
	}
}
