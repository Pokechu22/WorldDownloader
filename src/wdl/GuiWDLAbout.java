package wdl;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wdl.WDL;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;

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
	private static final String INFO = 
"WorldDownloader is a mod developed by nariol, cubic72, and pokechu22 (" +
"litemod version by julialy), with help from others (including soccerguy3, " +
"UltiNaruto, Net32, and El_garoo) that allows downloading a copy of a " +
"multiplayer world for singleplayer use.  Source code is publicly available.\n\n" +
"You are running version " + WDL.VERSION + " on Minecraft version " + 
Minecraft.getMinecraft().getVersion() /* returns launched version */ + "/" + 
ClientBrandRetriever.getClientModName() + ".";
	
	/**
	 * Locations of the various labels, updated whenever the screen is drawn.
	 */
	private int forumLinkY, coremodLinkY, litemodLinkY;
	
	/**
	 * Creates a GUI with the specified parent.
	 */
	public GuiWDLAbout(GuiScreen parent) {
		this.parent = parent;
	}
	
	@Override
	public void initGui() {
		GuiButton extensionsButton = new GuiButton(0, (this.width / 2) - 155, 18, 150, 20,
				"Extensions (NYI)");
		extensionsButton.enabled = false;
		buttonList.add(extensionsButton);
		buttonList.add(new GuiButton(1, (this.width / 2) + 5, 18, 150, 20,
				"Copy debug info"));
		buttonList.add(new GuiButton(2, (this.width / 2) - 100,
				this.height - 29, "Done"));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			//NYI
		} else if (button.id == 1) {
			setClipboardString(WDL.getDebugInfo());
		} else if (button.id == 2) {
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
		drawBackground(0);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		drawCenteredString(fontRendererObj, "About WorldDownloader", width / 2,
				2, 0xFFFFFF);
		
		List<String> lines = fontRendererObj.listFormattedStringToWidth(INFO, width - 10);
		
		int y = 43;
		for (String s : lines) {
			drawString(fontRendererObj, s, 5, y, 0xFFFFFF);
			y += fontRendererObj.FONT_HEIGHT;
		}
		y += fontRendererObj.FONT_HEIGHT;
		forumLinkY = y;
		drawString(fontRendererObj, "\u00A79\u00A7\u00A7nView the minecraft forum thread", 
				5, y, 0xFFFFFF);
		y += fontRendererObj.FONT_HEIGHT;
		y += fontRendererObj.FONT_HEIGHT;
		coremodLinkY = y;
		drawString(fontRendererObj, "\u00A79\u00A7nCoremod source code on github", 
				5, y, 0xFFFFFF);
		y += fontRendererObj.FONT_HEIGHT;
		y += fontRendererObj.FONT_HEIGHT;
		litemodLinkY = y;
		drawString(fontRendererObj, "\u00A79\u00A7nLitemod source code on github", 
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
			Class desktopClass = Class.forName("java.awt.Desktop");
			Object desktop = desktopClass.getMethod("getDesktop").invoke(
					null);
			desktopClass.getMethod("browse", URI.class).invoke(desktop,
					new URI(path));
		} catch (Throwable e) {
			logger.error("Couldn\'t open link", e);
		}
	}
}

