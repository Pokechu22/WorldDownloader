/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui;

import javax.annotation.Nullable;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import wdl.WDL;
import wdl.WDLPluginChannels;
import wdl.gui.widget.Button;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.TextList;

/**
 * GUI that shows the current permissions for the user.
 */
public class GuiWDLPermissions extends Screen {
	/**
	 * Margins for the top and the bottom of the list.
	 */
	private static final int TOP_MARGIN = 61, BOTTOM_MARGIN = 32;

	/**
	 * Ticks (20ths of a second) until this UI needs to refresh.
	 *
	 * If -1, don't refresh.
	 */
	private int refreshTicks = -1;

	@Nullable
	private final Screen parent;
	private final WDL wdl;

	private TextList list;

	/**
	 * Creates a new GUI with the given parent.
	 *
	 * @param parent
	 */
	public GuiWDLPermissions(@Nullable Screen parent, WDL wdl) {
		this.parent = parent;
		this.wdl = wdl;
	}

	@Override
	public void initGui() {
		this.addButton(new ButtonDisplayGui(width / 2 - 100, height - 29,
				200, 20, this.parent));

		this.addButton(new Button(this.width / 2 - 155, 39, 100, 20,
				I18n.format("wdl.gui.permissions.current")) {
			public @Override void performAction() {
				// Would open this GUI; do nothing.
			};
		});
		if (WDLPluginChannels.canRequestPermissions()) {
			this.addButton(new ButtonDisplayGui(this.width / 2 - 50, 39, 100, 20,
					I18n.format("wdl.gui.permissions.request"), () -> new GuiWDLPermissionRequest(this.parent, this.wdl)));
			this.addButton(new ButtonDisplayGui(this.width / 2 + 55, 39, 100, 20,
					I18n.format("wdl.gui.permissions.overrides"), () -> new GuiWDLChunkOverrides(this.parent, this.wdl)));
		}

		this.addButton(new Button((this.width / 2) + 5, 18, 150, 20,
				"Reload permissions") {
			public @Override void performAction() {
				// Send the init packet.
				WDLPluginChannels.sendInitPacket("Refresh?");

				enabled = false;
				displayString = "Refreshing...";

				refreshTicks = 50; // 2.5 seconds
			}
		});

		this.list = this.addList(new TextList(mc, width, height, TOP_MARGIN, BOTTOM_MARGIN));

		list.addLine("\u00A7c\u00A7lThis is a work in progress.");

		if (!WDLPluginChannels.hasPermissions()) {
			return;
		}

		list.addBlankLine();
		if (!WDLPluginChannels.canRequestPermissions()) {
			list.addLine("\u00A7cThe serverside permission plugin is out of date " +
					"and does support permission requests.  Please go ask a " +
					"server administrator to update the plugin.");
			list.addBlankLine();
		}

		if (WDLPluginChannels.getRequestMessage() != null) {
			list.addLine("Note from the server moderators: ");
			list.addLine(WDLPluginChannels.getRequestMessage());
			list.addBlankLine();
		}

		list.addLine("These are your current permissions:");
		// TODO: I'd like to return the description lines here, but can't yet.
		// Of course, I'd need to put in some better lines than before.
		// Maybe also skip unsent permissions?
		list.addLine("Can download: "
				+ WDLPluginChannels.canDownloadInGeneral());
		list.addLine("Can save chunks as you move: " + WDLPluginChannels.canCacheChunks());
		if (!WDLPluginChannels.canCacheChunks() && WDLPluginChannels.canDownloadInGeneral()) {
			list.addLine("Nearby chunk save radius: " + WDLPluginChannels.getSaveRadius());
		}
		list.addLine("Can save entities: "
				+ WDLPluginChannels.canSaveEntities());
		list.addLine("Can save tile entities: "
				+ WDLPluginChannels.canSaveTileEntities());
		list.addLine("Can save containers: "
				+ WDLPluginChannels.canSaveContainers());
		list.addLine("Received entity ranges: "
				+ WDLPluginChannels.hasServerEntityRange() + " ("
				+ WDLPluginChannels.getEntityRanges().size() + " total)");
	}

	@Override
	public void tick() {
		if (refreshTicks > 0) {
			refreshTicks--;
		} else if (refreshTicks == 0) {
			initGui();
			refreshTicks = -1;
		}
		super.tick();
	}

	@Override
	public void onGuiClosed() {
		wdl.saveProps();
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		super.render(mouseX, mouseY, partialTicks);

		this.drawCenteredString(this.fontRenderer, "Permission info",
				this.width / 2, 8, 0xFFFFFF);

		if (!WDLPluginChannels.hasPermissions()) {
			this.drawCenteredString(this.fontRenderer,
					"No permissions received; defaulting to everything enabled.",
					this.width / 2, (this.height - 32 - 23) / 2 + 23
					- fontRenderer.FONT_HEIGHT / 2, 0xFFFFFF);
		}
	}
}
