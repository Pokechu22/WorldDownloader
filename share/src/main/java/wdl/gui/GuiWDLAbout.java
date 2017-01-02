package wdl.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import wdl.WDL;

/**
 * Contains information about the current installation of WDL.
 */
public class GuiWDLAbout extends GuiScreen {
	/**
	 * GUI to display afterwards.
	 */
	private final GuiScreen parent;
	
	private static final String FORUMS_THREAD = "http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465";
	private static final String COREMOD_GITHUB = "https://github.com/Pokechu22/WorldDownloader";
	private static final String LITEMOD_GITHUB = "https://github.com/uyjulian/LiteModWDL/";
	
	private TextList list;
	
	/**
	 * Creates a GUI with the specified parent.
	 */
	public GuiWDLAbout(GuiScreen parent) {
		
		
		
		
		this.parent = parent;
	}
	
	@Override
	public void initGui() {
		buttonList.add(new GuiButton(0, (this.width / 2) - 155, 18, 150, 20,
				I18n.format("wdl.gui.about.extensions")));
		buttonList.add(new GuiButton(1, (this.width / 2) + 5, 18, 150, 20,
				I18n.format("wdl.gui.about.debugInfo")));
		buttonList.add(new GuiButton(2, (this.width / 2) - 100,
				this.height - 29, I18n.format("gui.done")));
		
		String wdlVersion = WDL.VERSION;
		
		String mcVersion = WDL.getMinecraftVersionInfo();
		
		list = new TextList(mc, width, height, 39, 32);
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
		list.addLinkLine(I18n.format("wdl.gui.about.coremodSrc"), COREMOD_GITHUB);
		list.addBlankLine();
		list.addLinkLine(I18n.format("wdl.gui.about.litemodSrc"), LITEMOD_GITHUB);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			// Extensions
			mc.displayGuiScreen(new GuiWDLExtensions(this));
		} else if (button.id == 1) {
			// Copy debug info
			setClipboardString(WDL.getDebugInfo());
			// Change text to "copied" once clicked
			button.displayString = I18n
					.format("wdl.gui.about.debugInfo.copied");
		} else if (button.id == 2) {
			// Done
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
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		drawCenteredString(fontRendererObj, I18n.format("wdl.gui.about.title"),
				width / 2, 2, 0xFFFFFF);
	}
}
