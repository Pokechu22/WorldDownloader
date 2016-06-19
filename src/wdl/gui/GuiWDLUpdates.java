package wdl.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import wdl.WDL;
import wdl.update.Release;
import wdl.update.WDLUpdateChecker;

/**
 * Gui that lists updates fetched via {@link wdl.update.GithubInfoGrabber}.
 */
public class GuiWDLUpdates extends GuiScreen {
	private final GuiScreen parent;
	
	/**
	 * Margins for the top and the bottom of the list.
	 */
	private static final int TOP_MARGIN = 39, BOTTOM_MARGIN = 32;
	
	private class UpdateList extends GuiListExtended {
		public UpdateList() {
			super(GuiWDLUpdates.this.mc, GuiWDLUpdates.this.width,
					GuiWDLUpdates.this.height, TOP_MARGIN,
					GuiWDLUpdates.this.height - BOTTOM_MARGIN, 
					(fontRendererObj.FONT_HEIGHT + 1) * 6 + 2);
			this.showSelectionBox = true;
		}
		
		private class VersionEntry implements IGuiListEntry {
			private final Release release;
			
			private String title;
			private String caption;
			private String body1;
			private String body2;
			private String body3;
			private String time;
			
			private final int fontHeight;
			
			public VersionEntry(Release release) {
				this.release = release;
				this.fontHeight = fontRendererObj.FONT_HEIGHT + 1;
				
				this.title = buildReleaseTitle(release);
				this.caption = buildVersionInfo(release);
				
				List<String> body = Utils.wordWrap(release.textOnlyBody, getListWidth());
				
				body1 = (body.size() >= 1 ? body.get(0) : "");
				body2 = (body.size() >= 2 ? body.get(1) : "");
				body3 = (body.size() >= 3 ? body.get(2) : "");
				
				time = I18n.format("wdl.gui.updates.update.releaseDate", release.date);
			}
			
			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				String title;
				//The 'isSelected' parameter is actually 'isMouseOver'
				if (isSelected(slotIndex)) {
					title = I18n.format("wdl.gui.updates.currentVersion",
							this.title);
				} else if (this.release == recomendedRelease) {
					title = I18n.format("wdl.gui.updates.recomendedVersion",
							this.title);
				} else {
					title = this.title;
				}
				
				fontRendererObj.drawString(title, x, y + fontHeight * 0, 0xFFFFFF);
				fontRendererObj.drawString(caption, x, y + fontHeight * 1, 0x808080);
				fontRendererObj.drawString(body1, x, y + fontHeight * 2, 0xFFFFFF);
				fontRendererObj.drawString(body2, x, y + fontHeight * 3, 0xFFFFFF);
				fontRendererObj.drawString(body3, x, y + fontHeight * 4, 0xFFFFFF);
				fontRendererObj.drawString(time, x, y + fontHeight * 5, 0x808080);
				
				if (mouseX > x && mouseX < x + listWidth && mouseY > y
						&& mouseY < y + slotHeight) {
					drawRect(x - 2, y - 2, x + listWidth - 3, y + slotHeight + 2,
							0x1FFFFFFF);
				}
			}
			
			@Override
			public boolean mousePressed(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				if (relativeY > 0 && relativeY < slotHeight) {
					mc.displayGuiScreen(new GuiWDLSingleUpdate(GuiWDLUpdates.this,
							this.release));
					
					mc.getSoundHandler().playSound(
							PositionedSoundRecord.getMasterRecord(
									SoundEvents.UI_BUTTON_CLICK, 1.0f));
					return true;
				}
				return false;
			}
			
			@Override
			public void mouseReleased(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				
			}
			
			@Override
			public void setSelected(int p_178011_1_, int p_178011_2_,
					int p_178011_3_) {
				
			}
		}
		
		private List<VersionEntry> displayedVersions;
		/**
		 * Release that should be used.
		 */
		private Release recomendedRelease;
		
		/**
		 * Regenerates the {@linkplain #displayedVersions version list}.
		 * 
		 * TODO: This is probably a bit laggy; cache this data?  Right now it's
		 * being called each frame.
		 */
		private void regenerateVersionList() {
			displayedVersions = new ArrayList<VersionEntry>();
			
			if (WDLUpdateChecker.hasNewVersion()) {
				recomendedRelease = WDLUpdateChecker.getRecomendedRelease();
			} else {
				recomendedRelease = null;
			}
			
			List<Release> releases = WDLUpdateChecker.getReleases();
			
			if (releases == null) {
				return;
			}
			
			for (Release release : releases) {
				displayedVersions.add(new VersionEntry(release));
			}
		}
		
		@Override
		public VersionEntry getListEntry(int index) {
			return displayedVersions.get(index);
		}
		
		@Override
		protected int getSize() {
			return displayedVersions.size();
		}
		
		@Override
		protected boolean isSelected(int slotIndex) {
			VersionEntry entry = getListEntry(slotIndex);
			
			return WDL.VERSION.equals(entry.release.tag);
		}
		
		@Override
		public int getListWidth() {
			return width - 30;
		}
		
		@Override
		protected int getScrollBarX() {
			return width - 10;
		}
	}
	
	private UpdateList list;
	private GuiButton updateMinecraftVersionButton;
	private GuiButton updateAllowBetasButton;
	
	public GuiWDLUpdates(GuiScreen parent) {
		this.parent = parent;
	}
	
	@Override
	public void initGui() {
		this.list = new UpdateList();
		
		this.updateMinecraftVersionButton = new GuiButton(0,
				this.width / 2 - 155, 18, 150, 20,
				getUpdateMinecraftVersionText());
		this.buttonList.add(this.updateMinecraftVersionButton);
		this.updateAllowBetasButton = new GuiButton(1,
				this.width / 2 + 5, 18, 150, 20, getAllowBetasText());
		this.buttonList.add(this.updateAllowBetasButton);
		
		this.buttonList.add(new GuiButton(100, this.width / 2 - 100, 
				this.height - 29, I18n.format("gui.done")));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			cycleUpdateMinecraftVersion();
		}
		if (button.id == 1) {
			cycleAllowBetas();
		}
		if (button.id == 100) {
			mc.displayGuiScreen(parent);
		}
	}
	
	@Override
	public void onGuiClosed() {
		WDL.saveGlobalProps();
	}
	
	private void cycleUpdateMinecraftVersion() {
		String prop = WDL.globalProps.getProperty("UpdateMinecraftVersion");
		
		if (prop.equals("client")) {
			WDL.globalProps.setProperty("UpdateMinecraftVersion", "server");
		} else if (prop.equals("server")) {
			WDL.globalProps.setProperty("UpdateMinecraftVersion", "any");
		} else {
			WDL.globalProps.setProperty("UpdateMinecraftVersion", "client");
		}
		
		updateMinecraftVersionButton.displayString = getUpdateMinecraftVersionText();
	}
	
	private void cycleAllowBetas() {
		if (WDL.globalProps.getProperty("UpdateAllowBetas").equals("true")) {
			WDL.globalProps.setProperty("UpdateAllowBetas", "false");
		} else {
			WDL.globalProps.setProperty("UpdateAllowBetas", "true");
		}
		
		updateAllowBetasButton.displayString = getAllowBetasText();
	}
	
	private String getUpdateMinecraftVersionText() {
		return I18n.format("wdl.gui.updates.updateMinecraftVersion."
				+ WDL.globalProps.getProperty("UpdateMinecraftVersion"),
				WDL.getMinecraftVersion());
	}
	
	private String getAllowBetasText() {
		return I18n.format("wdl.gui.updates.updateAllowBetas."
				+ WDL.globalProps.getProperty("UpdateAllowBetas"));
	}
	
	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
	throws IOException {
		list.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.list.handleMouseInput();
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if (list.mouseReleased(mouseX, mouseY, state)) {
			return;
		}
		super.mouseReleased(mouseX, mouseY, state);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.list == null) {
			return;
		}
		
		this.list.regenerateVersionList();
		this.list.drawScreen(mouseX, mouseY, partialTicks);
		
		if (!WDLUpdateChecker.hasFinishedUpdateCheck()) {
			drawCenteredString(fontRendererObj,
					I18n.format("wdl.gui.updates.pleaseWait"), width / 2,
					height / 2, 0xFFFFFF);
		} else if (WDLUpdateChecker.hasUpdateCheckFailed()) {
			String reason = WDLUpdateChecker.getUpdateCheckFailReason();
			
			drawCenteredString(fontRendererObj,
					I18n.format("wdl.gui.updates.checkFailed"), width / 2,
					height / 2 - fontRendererObj.FONT_HEIGHT / 2, 0xFF5555);
			drawCenteredString(fontRendererObj, I18n.format(reason), width / 2,
					height / 2 + fontRendererObj.FONT_HEIGHT / 2, 0xFF5555);
		}
		
		drawCenteredString(fontRendererObj, I18n.format("wdl.gui.updates.title"),
				width / 2, 8, 0xFFFFFF);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		if (updateMinecraftVersionButton.isMouseOver()) {
			Utils.drawGuiInfoBox(
					I18n.format("wdl.gui.updates.updateMinecraftVersion.description",
							WDL.getMinecraftVersion()),
					width, height, BOTTOM_MARGIN);
		} else if (updateAllowBetasButton.isMouseOver()) {
			Utils.drawGuiInfoBox(
					I18n.format("wdl.gui.updates.updateAllowBetas.description"),
					width, height, BOTTOM_MARGIN);
		}
	}
	
	/**
	 * Gets the translated version info for the given release.
	 */
	private String buildVersionInfo(Release release) {
		String type = "?", mainVersion = "?", supportedVersions = "?";
		
		if (release.hiddenInfo != null) {
			type = release.hiddenInfo.loader;
			mainVersion = release.hiddenInfo.mainMinecraftVersion;
			
			String[] versions = release.hiddenInfo.supportedMinecraftVersions;
			if (versions.length == 1) {
				supportedVersions = I18n.format(
						"wdl.gui.updates.update.version.listSingle",
						versions[0]);
			} else if (versions.length == 2) {
				supportedVersions = I18n.format(
						"wdl.gui.updates.update.version.listDouble",
						versions[0], versions[1]);
			} else {
				StringBuilder builder = new StringBuilder();
				
				for (int i = 0; i < versions.length; i++) {
					if (i == 0) {
						builder.append(I18n.format(
								"wdl.gui.updates.update.version.listStart",
								versions[i]));
					} else if (i == versions.length - 1) {
						builder.append(I18n.format(
								"wdl.gui.updates.update.version.listEnd",
								versions[i]));
					} else {
						builder.append(I18n.format(
								"wdl.gui.updates.update.version.listMiddle",
								versions[i]));
					}
				}
				
				supportedVersions = builder.toString();
			}
		}
		
		return I18n.format("wdl.gui.updates.update.version", type, mainVersion,
				supportedVersions);
	}
	
	/**
	 * Gets the translated title for the given release.
	 */
	private String buildReleaseTitle(Release release) {
		String version = release.tag;
		String mcVersion = "?";
		
		if (release.hiddenInfo != null) {
			mcVersion = release.hiddenInfo.mainMinecraftVersion;
		}
		
		if (release.prerelease) {
			return I18n.format("wdl.gui.updates.update.title.prerelease", version, mcVersion);
		} else {
			return I18n.format("wdl.gui.updates.update.title.release", version, mcVersion);
		}
	}
	
	/**
	 * Gui that shows a single update.
	 */
	private class GuiWDLSingleUpdate extends GuiScreen {
		private final GuiWDLUpdates parent;
		private final Release release;
		
		private TextList list;
		
		public GuiWDLSingleUpdate(GuiWDLUpdates parent, Release releaseToShow) {
			this.parent = parent;
			this.release = releaseToShow;
		}
		
		@Override
		public void initGui() {
			this.buttonList.add(new GuiButton(0, this.width / 2 - 155, 
					18, 150, 20, I18n.format("wdl.gui.updates.update.viewOnline")));
			if (release.hiddenInfo != null) {
				this.buttonList.add(new GuiButton(1, this.width / 2 + 5, 
						18, 150, 20, I18n.format("wdl.gui.updates.update.viewForumPost")));
			}
			this.buttonList.add(new GuiButton(100, this.width / 2 - 100, 
					this.height - 29, I18n.format("gui.done")));
			
			this.list = new TextList(mc, width, height, TOP_MARGIN, BOTTOM_MARGIN);
			
			list.addLine(buildReleaseTitle(release));
			list.addLine(I18n.format("wdl.gui.updates.update.releaseDate", release.date));
			list.addLine(buildVersionInfo(release));
			list.addBlankLine();
			list.addLine(release.textOnlyBody);
		}
		
		@Override
		protected void actionPerformed(GuiButton button) throws IOException {
			if (button.id == 0) {
				Utils.openLink(release.URL);
			}
			if (button.id == 1) {
				Utils.openLink(release.hiddenInfo.post);
			}
			if (button.id == 100) {
				mc.displayGuiScreen(parent);
			}
		}
		
		/**
		 * Called when the mouse is clicked.
		 */
		@Override
		protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
		throws IOException {
			list.mouseClicked(mouseX, mouseY, mouseButton);
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
		
		/**
		 * Handles mouse input.
		 */
		@Override
		public void handleMouseInput() throws IOException {
			super.handleMouseInput();
			this.list.handleMouseInput();
		}
		
		@Override
		protected void mouseReleased(int mouseX, int mouseY, int state) {
			if (list.mouseReleased(mouseX, mouseY, state)) {
				return;
			}
			super.mouseReleased(mouseX, mouseY, state);
		}
		
		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			if (this.list == null) {
				return;
			}
			
			this.list.drawScreen(mouseX, mouseY, partialTicks);
			
			this.drawCenteredString(this.fontRendererObj, buildReleaseTitle(release),
					this.width / 2, 8, 0xFFFFFF);
			
			super.drawScreen(mouseX, mouseY, partialTicks);
		}
	}
}
