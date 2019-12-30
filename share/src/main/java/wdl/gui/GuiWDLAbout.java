/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import wdl.VersionConstants;
import wdl.WDL;
import wdl.gui.widget.WDLButton;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.WDLScreen;
import wdl.gui.widget.TextList;
import wdl.versioned.VersionedFunctions;

/**
 * Contains information about the current installation of WDL.
 */
public class GuiWDLAbout extends WDLScreen {
	/**
	 * GUI to display afterwards.
	 */
	@Nullable
	private final GuiScreen parent;
	private final WDL wdl;

	private static final String FORUMS_THREAD = "https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds";
	private static final String ALL_GITHUB = "https://github.com/Pokechu22/WorldDownloader";
	private static final String MMPLV2 = "https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md";

	private static final String FASTUTIL_PAGE = "http://fastutil.di.unimi.it/";
	private static final String APACHE_LICENSE_2_0 = "https://www.apache.org/licenses/LICENSE-2.0.html";

	private TextList list;

	/**
	 * Creates a GUI with the specified parent.
	 */
	public GuiWDLAbout(@Nullable GuiScreen parent, WDL wdl) {
		super("wdl.gui.about.title");
		this.parent = parent;
		this.wdl = wdl;
	}

	@Override
	public void init() {
		this.addButton(new ButtonDisplayGui((this.width / 2) - 155, 18, 150, 20,
				I18n.format("wdl.gui.about.extensions"), () -> new GuiWDLExtensions(this)));
		this.addButton(new WDLButton((this.width / 2) + 5, 18, 150, 20,
				I18n.format("wdl.gui.about.debugInfo")) {
			public @Override void performAction() {
				// Copy debug info
				VersionedFunctions.setClipboardString(wdl.getDebugInfo());
				// Change text to "copied" once clicked
				setMessage(I18n.format("wdl.gui.about.debugInfo.copied"));
			}
		});
		this.addButton(new ButtonDisplayGui((this.width / 2) - 100, this.height - 29,
				200, 20, parent));

		String wdlVersion = VersionConstants.getModVersion();

		String mcVersion = VersionConstants.getMinecraftVersionInfo();

		list = this.addList(new TextList(minecraft, width, height, 39, 32));
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
	protected void renderTitle(int mouseX, int mouseY, float partialTicks) {
		// Draw at y=2 instead of y=8, to avoid drawing over the buttons
		this.drawCenteredString(this.font, this.title.getFormattedText(), this.width / 2, 8, 0xFFFFFF);
	}
}
