package wdl.update;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import wdl.WDL;
import wdl.WDLMessages;
import wdl.api.IWDLMessageType;
import wdl.update.Release.HashData;

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
			
			sleep(5000);
			
			List<Release> releases = GithubInfoGrabber.getReleases();
			WDL.chatMessage(debugMessageType, "Found " + releases.size()
					+ " releases.");
			Release currentRelease = null;
			
			for (Release release : releases) {
				if (release.tag.equalsIgnoreCase(WDL.VERSION)) {
					currentRelease = release;
					break;
				}
			}
			
			if (currentRelease == null) {
				WDL.chatMessage(debugMessageType, "Could not find a release " +
						"for " + WDL.VERSION + ".");
				return;
			}
			
			if (currentRelease.hiddenInfo == null) {
				WDL.chatMessage(debugMessageType, "Could not find hidden " +
						"data for release.  Skipping hashing.");
				return;
			}
			//Check the hashes, and list any failing ones.
			Map<HashData, Object> failed = new HashMap<HashData, Object>();
			
			hashLoop: for (HashData data : currentRelease.hiddenInfo.hashes) {
				try {
					String hash = ClassHasher.hash(data.relativeTo, data.file);
					
					for (String validHash : data.validHashes) {
						if (validHash.equalsIgnoreCase(hash)) {
							// Labeled continues / breaks _are_ a thing.
							// This just continues the outer loop.
							continue hashLoop;
						}
					}
					
					WDL.chatMessage(
							debugMessageType,
							"Bad hash for " + data.file + " (relative to "
									+ data.relativeTo + "): Expected "
									+ Arrays.toString(data.validHashes)
									+ ", got " + hash);
					
					failed.put(data,  hash);
					continue;
				} catch (Exception e) {
					WDL.chatMessage(debugMessageType, "Bad hash for "
							+ data.file + " (relative to " + data.relativeTo
							+ "): Exception: " + e);
					
					failed.put(data,  e);
				}
			}
			
			if (failed.size() > 0) {
				WDL.chatMessage(mainMessageType, "§cSome files have invalid " +
						"hashes!  Your installation may be corrupt or " +
						"compromised.  If you are running a custom build, " +
						"this is normal.");
				WDL.chatMessage(mainMessageType, "§cFailures: " + failed);
			}
		} catch (Exception e) {
			WDL.chatMessage(debugMessageType, "Failed to perform update check: "
					+ e.toString());
			e.printStackTrace();
		}
	}
}
