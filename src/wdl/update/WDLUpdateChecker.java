package wdl.update;

import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import wdl.WDL;
import wdl.WDLMessages;
import wdl.api.IWDLMessageType;

/**
 * Performs the update checking.
 */
public class WDLUpdateChecker extends Thread {
	private static volatile boolean shown = false;
	
	/**
	 * Call once the world has loaded.  Will check and start a new update checker
	 * if needed.
	 */
	public static void startIfNeeded(IWDLMessageType mainMessageType,
			IWDLMessageType debugMessageType) {
		if (!shown) {
			shown = true;
			
			new WDLUpdateChecker(mainMessageType, debugMessageType).start();
		}
	}
	
	public final IWDLMessageType mainMessageType;
	public final IWDLMessageType debugMessageType;
	
	private static final String FORUMS_THREAD_USAGE_LINK = "http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465#Usage";
	private static final String GITHUB_LINK = "https://github.com/pokechu22/WorldDownloader";
	
	private WDLUpdateChecker(IWDLMessageType mainMessageType,
			IWDLMessageType debugMessageType) {
		super("WorldDownloader update check thread");
		
		this.mainMessageType = mainMessageType;
		this.debugMessageType = debugMessageType;
	}
	
	@Override
	public void run() {
		try {
			if (!WDL.defaultProps.getProperty("TutorialShown").equals("true")) {
				sleep(5000);
				
				ChatComponentText usageMsg = new ChatComponentText(
						"For information on how to use this mod, please see the ");
				ChatComponentText forumsLink = new ChatComponentText(
						"Official MinecraftForums thread");
				forumsLink.getChatStyle().setColor(EnumChatFormatting.BLUE)
						.setUnderlined(true)
						.setChatClickEvent(
								new ClickEvent(ClickEvent.Action.OPEN_URL,
										FORUMS_THREAD_USAGE_LINK));
				ChatComponentText period = new ChatComponentText(".");
				usageMsg.appendSibling(forumsLink);
				usageMsg.appendSibling(period);
				ChatComponentText contributeMsg = new ChatComponentText(
						"To report a bug, suggest a feature, contribute code, " +
								"or help translate, check out ");
				ChatComponentText githubLink = new ChatComponentText(
						"the GitHub repository");
				githubLink.getChatStyle().setColor(EnumChatFormatting.BLUE)
						.setUnderlined(true)
						.setChatClickEvent(
								new ClickEvent(ClickEvent.Action.OPEN_URL,
										GITHUB_LINK));
				contributeMsg.appendSibling(githubLink);
				contributeMsg.appendSibling(period);
				
				WDLMessages.chatMessage(mainMessageType,
						"The WorldDownloader mod has been successfully installed!");
				WDLMessages.chatMessage(mainMessageType, usageMsg);
				WDLMessages.chatMessage(mainMessageType, contributeMsg);
				WDLMessages.chatMessage(mainMessageType,
						"Note: If you downloaded this mod from a location other than the Minecraft Forums or github, you may have been exposed to malware.");
				
				WDL.defaultProps.setProperty("TutorialShown", "true");
				WDL.saveDefaultProps();
			}
		} catch (Exception e) {
			WDL.chatMessage(debugMessageType, "Failed to perform update check: "
					+ e.toString());
			e.printStackTrace();
		}
	}
}
