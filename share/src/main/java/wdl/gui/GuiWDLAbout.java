/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import wdl.VersionConstants;
import wdl.WDL;
import wdl.gui.widget.Button;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.Screen;
import wdl.gui.widget.TextList;

/**
 * Contains information about the current installation of WDL.
 */
public class GuiWDLAbout extends Screen {
	/**
	 * GUI to display afterwards.
	 */
	private final GuiScreen parent;

	private static final String FORUMS_THREAD = "http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465";
	private static final String ALL_GITHUB = "https://github.com/Pokechu22/WorldDownloader";
	private static final String MMPLV2 = "https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md";

	private static final String FASTUTIL_PAGE = "http://fastutil.di.unimi.it/";
	private static final String APACHE_LICENSE_2_0 = "http://www.apache.org/licenses/LICENSE-2.0.html";

	private TextList list;

	/**
	 * Creates a GUI with the specified parent.
	 */
	public GuiWDLAbout(GuiScreen parent) {
		this.parent = parent;
	}

	@Override
	public void initGui() {
		this.addButton(new ButtonDisplayGui((this.width / 2) - 155, 18, 150, 20,
				I18n.format("wdl.gui.about.extensions"), () -> new GuiWDLExtensions(this)));
		this.addButton(new Button((this.width / 2) + 5, 18, 150, 20,
				I18n.format("wdl.gui.about.debugInfo")) {
			public @Override void performAction() {
				// Copy debug info
				LocalUtils.setClipboardString(WDL.getDebugInfo());
				// Change text to "copied" once clicked
				this.displayString = I18n.format("wdl.gui.about.debugInfo.copied");
			}
		});
		this.addButton(new ButtonDisplayGui((this.width / 2) - 100, this.height - 29,
				200, 20, parent));

		String wdlVersion = VersionConstants.getModVersion();

		String mcVersion = VersionConstants.getMinecraftVersionInfo();

		list = this.addList(new TextList(mc, width, height, 39, 32));
		list.addLine(I18n.format("wdl.gui.about.blurb"));
		list.addBlankLine();
		list.addLine(I18n.format("wdl.gui.about.version", wdlVersion,
				mcVersion));
		list.addBlankLine();

		String currentLanguage = WDL.minecraft.getLanguageManager()
				.getCurrentLanguage().toString();
		String translatorCredit = I18n.format("wdl.translatorCredit",
				currentLanguage);
		if (translatorCredit != null && !translatorCredit.isEmpty()) {
			list.addLine(translatorCredit);
			list.addBlankLine();
		}

		list.addLinkLine(I18n.format("wdl.gui.about.forumThread"), FORUMS_THREAD);
		list.addBlankLine();
		list.addLinkLine(I18n.format("wdl.gui.about.allSrc"), ALL_GITHUB);
		list.addBlankLine();
		list.addLinkLine(I18n.format("wdl.gui.about.license"), MMPLV2);

		if (VersionConstants.shadesFastUtil()) {
			list.addBlankLine();
			list.addLine(I18n.format("wdl.gui.about.fastutil.blurb"));
			list.addBlankLine();
			list.addLinkLine(I18n.format("wdl.gui.about.fastutil.website"), FASTUTIL_PAGE);
			list.addLinkLine(I18n.format("wdl.gui.about.fastutil.license"), APACHE_LICENSE_2_0);
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		super.render(mouseX, mouseY, partialTicks);

		drawCenteredString(fontRenderer, I18n.format("wdl.gui.about.title"),
				width / 2, 2, 0xFFFFFF);
	}
}
