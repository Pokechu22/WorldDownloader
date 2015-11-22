package wdl.gui;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	
	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * Info text.
	 */
	private String info;

	/**
	 * Locations of the various labels, updated whenever the screen is drawn.
	 */
	private int forumLinkY, coremodLinkY, litemodLinkY;
	
	/**
	 * Creates a GUI with the specified parent.
	 */
	public GuiWDLAbout(GuiScreen parent) {
		String wdlVersion = WDL.VERSION;
		// Gets the launched version (appears in F3)
		String launchedVersion = Minecraft.getMinecraft().func_175600_c();
		String brand = ClientBrandRetriever.getClientModName();
		
		info = I18n.format("wdl.gui.about.blurb") + "\n\n" +
				I18n.format("wdl.gui.about.version", wdlVersion,
						launchedVersion, brand);
		
		String currentLanguage = WDL.minecraft.getLanguageManager()
				.getCurrentLanguage().toString();
		String translatorCredit = I18n.format("wdl.translatorCredit",
				currentLanguage);
		if (translatorCredit != null && !translatorCredit.isEmpty()) {
			info += "\n\n" + translatorCredit;
		}
		
		this.parent = parent;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		buttonList.add(new GuiButton(0, (this.width / 2) - 155, 18, 150, 20,
				I18n.format("wdl.gui.about.extensions")));
		buttonList.add(new GuiButton(1, (this.width / 2) + 5, 18, 150, 20,
				I18n.format("wdl.gui.about.debugInfo")));
		buttonList.add(new GuiButton(2, (this.width / 2) - 100,
				this.height - 29, I18n.format("gui.done")));
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
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
			throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		
		if (mouseY > forumLinkY
				&& mouseY < forumLinkY + fontRendererObj.FONT_HEIGHT) {
			openLink(FORUMS_THREAD);
		} else if (mouseY > coremodLinkY
				&& mouseY < coremodLinkY + fontRendererObj.FONT_HEIGHT) {
			openLink(COREMOD_GITHUB);
		} else if (mouseY > litemodLinkY
				&& mouseY < litemodLinkY + fontRendererObj.FONT_HEIGHT) {
			openLink(LITEMOD_GITHUB);
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		Utils.drawListBackground(39, 32, 0, 0, height, width);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		drawCenteredString(fontRendererObj, I18n.format("wdl.gui.about.title"),
				width / 2, 2, 0xFFFFFF);
		
		List<String> lines = Utils.wordWrap(info, width - 10);
		
		int y = 43;
		for (String s : lines) {
			drawString(fontRendererObj, s, 5, y, 0xFFFFFF);
			y += fontRendererObj.FONT_HEIGHT;
		}
		y += fontRendererObj.FONT_HEIGHT;
		forumLinkY = y;
		drawString(fontRendererObj, "§9§n" + I18n.format("wdl.gui.about.forumThread"), 
				5, y, 0xFFFFFF);
		y += fontRendererObj.FONT_HEIGHT;
		y += fontRendererObj.FONT_HEIGHT;
		coremodLinkY = y;
		drawString(fontRendererObj, "§9§n" + I18n.format("wdl.gui.about.coremodSrc"), 
				5, y, 0xFFFFFF);
		y += fontRendererObj.FONT_HEIGHT;
		y += fontRendererObj.FONT_HEIGHT;
		litemodLinkY = y;
		drawString(fontRendererObj, "§9§n" + I18n.format("wdl.gui.about.litemodSrc"), 
				5, y, 0xFFFFFF);
		
		if (mouseY > forumLinkY
				&& mouseY < forumLinkY + fontRendererObj.FONT_HEIGHT) {
			drawHoveringText(Arrays.asList(FORUMS_THREAD), mouseX, mouseY);
		} else if (mouseY > coremodLinkY
				&& mouseY < coremodLinkY + fontRendererObj.FONT_HEIGHT) {
			drawHoveringText(Arrays.asList(COREMOD_GITHUB), mouseX, mouseY);
		} else if (mouseY > litemodLinkY
				&& mouseY < litemodLinkY + fontRendererObj.FONT_HEIGHT) {
			drawHoveringText(Arrays.asList(LITEMOD_GITHUB), mouseX, mouseY);
		}
	}
	
	/**
	 * Attempts to open a link.
	 * @param path
	 */
	private static void openLink(String path) {
		try {
			Class<?> desktopClass = Class.forName("java.awt.Desktop");
			Object desktop = desktopClass.getMethod("getDesktop").invoke(
					null);
			desktopClass.getMethod("browse", URI.class).invoke(desktop,
					new URI(path));
		} catch (Throwable e) {
			logger.error("Couldn\'t open link", e);
		}
	}
}
