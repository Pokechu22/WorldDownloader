package wdl.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import wdl.update.Release;
import wdl.update.WDLUpdateChecker;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

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
				
				this.title = release.title;
				if (release.hiddenInfo != null) {
					this.caption = release.hiddenInfo.loader + " version for "
							+ release.hiddenInfo.mainMinecraftVersion + " (supporting ";
					String[] versions = release.hiddenInfo.supportedMinecraftVersions;
					for (int i = 0; i < versions.length; i++) {
						caption += versions[i];
						
						if (i != versions.length - 1) {
							caption += ", ";
						}
						if (i == versions.length - 2) {
							caption += "and ";
						}
					}
					caption += ")";
				}
				
				List<String> body = Utils.wordWrap(release.textOnlyBody, getListWidth());
				
				body1 = (body.size() >= 1 ? body.get(0) : "");
				body2 = (body.size() >= 2 ? body.get(1) : "");
				body3 = (body.size() >= 3 ? body.get(2) : "");
				
				time = "Released on " + release.date;
			}
			
			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
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
					
					mc.getSoundHandler().playSound(PositionedSoundRecord
							.createPositionedSoundRecord(new ResourceLocation(
									"gui.button.press"), 1.0F));
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
		 * Regenerates the {@linkplain #displayedVersions version list}.
		 */
		private void regenerateVersionList() {
			displayedVersions = new ArrayList<VersionEntry>();
			
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
			
			//return WDL.VERSION.equals(entry.release.tag);
			return "1.8.8a-beta1".equals(entry.release.tag);
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
	
	public GuiWDLUpdates(GuiScreen parent) {
		this.parent = parent;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		this.list = new UpdateList();
		
		this.buttonList.add(new GuiButton(100, this.width / 2 - 100, 
				this.height - 29, I18n.format("gui.done")));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
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
		list.func_148179_a(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.list.func_178039_p();
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if (list.func_148181_b(mouseX, mouseY, state)) {
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
		
		super.drawScreen(mouseX, mouseY, partialTicks);
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
		
		@SuppressWarnings("unchecked")
		@Override
		public void initGui() {
			this.buttonList.add(new GuiButton(0, this.width / 2 - 155, 
					18, 150, 20, "View online (+ downloads)"));
			if (release.hiddenInfo != null) {
				this.buttonList.add(new GuiButton(1, this.width / 2 + 5, 
						18, 150, 20, "Forum post"));
			}
			this.buttonList.add(new GuiButton(100, this.width / 2 - 100, 
					this.height - 29, I18n.format("gui.done")));
			
			this.list = new TextList(mc, width, height, TOP_MARGIN, BOTTOM_MARGIN);
			
			list.addLine((release.prerelease ? "Prerelease " : "Release ")
					+ release.title);
			list.addLine("Released " + release.date);
			if (release.hiddenInfo != null) {
				String versionLine = release.hiddenInfo.loader + " version for " + 
						release.hiddenInfo.mainMinecraftVersion + " (supports ";
				
				String[] versions = release.hiddenInfo.supportedMinecraftVersions;
				for (int i = 0; i < versions.length; i++) {
					versionLine += versions[i];
					
					if (i != versions.length - 1) {
						versionLine += ", ";
					}
					if (i == versions.length - 2) {
						versionLine += "and ";
					}
				}
				versionLine += ")";
				
				list.addLine(versionLine);
			}
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
			list.func_148179_a(mouseX, mouseY, mouseButton);
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
		
		/**
		 * Handles mouse input.
		 */
		@Override
		public void handleMouseInput() throws IOException {
			super.handleMouseInput();
			this.list.func_178039_p();
		}
		
		@Override
		protected void mouseReleased(int mouseX, int mouseY, int state) {
			if (list.func_148181_b(mouseX, mouseY, state)) {
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
			
			this.drawCenteredString(this.fontRendererObj, release.title,
					this.width / 2, 8, 0xFFFFFF);
			
			super.drawScreen(mouseX, mouseY, partialTicks);
		}
	}
}
