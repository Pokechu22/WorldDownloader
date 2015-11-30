package wdl.update;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import wdl.WDL;
import wdl.WDLDebugMessageCause;
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
	public static void startIfNeeded() {
		if (!shown) {
			shown = true;
			
			new WDLUpdateChecker().start();
		}
	}
	
	private static final String FORUMS_THREAD_USAGE_LINK = "http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465#Usage";
	private static final String GITHUB_LINK = "https://github.com/pokechu22/WorldDownloader";
	private static final String REDISTRIBUTION_LINK = "http://pokechu22.github.io/WorldDownloader/redistribution";
	private static final String SMR_LINK = "http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/mods-discussion/2314237";
	
	private WDLUpdateChecker() {
		super("WorldDownloader update check thread");
	}
	
	@Override
	public void run() {
		try {
			if (!WDL.baseProps.getProperty("TutorialShown", "false").equals("true")) {
				sleep(5000);
				
				IChatComponent success = new ChatComponentText(
						"The WorldDownloader mod has been successfully installed!");
				IChatComponent mcfThread = new ChatComponentText(
						"Official MinecraftForums thread");
				mcfThread.getChatStyle().setColor(EnumChatFormatting.BLUE)
						.setChatClickEvent(
								new ClickEvent(Action.OPEN_URL,
										FORUMS_THREAD_USAGE_LINK));
				IChatComponent usage = new ChatComponentText(
						"For information on how to use this mod, please see the ")
						.appendSibling(mcfThread).appendText(".");
				ChatComponentText githubRepo = new ChatComponentText(
						"GitHub repository");
				githubRepo.getChatStyle().setColor(EnumChatFormatting.BLUE)
						.setChatClickEvent(
								new ClickEvent(Action.OPEN_URL,
										GITHUB_LINK));
				IChatComponent contribute = new ChatComponentText(
						"To report a bug, suggest a feature, contribute code, or help translate, check out the ")
						.appendSibling(githubRepo);
				IChatComponent redistributionList = new ChatComponentText(
						"the redistribution list");
				redistributionList.getChatStyle().setColor(EnumChatFormatting.BLUE)
						.setChatClickEvent(
								new ClickEvent(Action.OPEN_URL,
										REDISTRIBUTION_LINK));
				IChatComponent warning = new ChatComponentText(
						"WARNING");
				warning.getChatStyle().setColor(EnumChatFormatting.DARK_RED)
						.setBold(true);
				IChatComponent illegally = new ChatComponentText(
						"ILLEGALLY");
				illegally.getChatStyle().setColor(EnumChatFormatting.DARK_RED)
						.setBold(true);
				IChatComponent stolen = new ChatComponentText("")
						.appendSibling(warning)
						.appendText(
								": If you downloaded this mod from a location other than the Minecraft Forums or GitHub (or another site on ")
						.appendSibling(redistributionList)
						.appendText("), you may have used a site that is ")
						.appendSibling(illegally)
						.appendText(" redistributing this mod.");
				IChatComponent smr = new ChatComponentText(
						"More information: StopModReposts!");
				smr.getChatStyle().setColor(EnumChatFormatting.BLUE)
						.setChatClickEvent(
								new ClickEvent(Action.OPEN_URL,
										SMR_LINK));
				IChatComponent stolenBeware = new ChatComponentText(
						"Beware of such sites, as they can include malware.  ")
						.appendSibling(smr);
				
				WDL.chatMessage(success);
				WDL.chatMessage(usage);
				WDL.chatMessage(contribute);
				WDL.chatMessage(stolen);
				WDL.chatMessage(stolenBeware);
				
				WDL.baseProps.setProperty("TutorialShown", "true");
				WDL.saveProps();
			}
			
			sleep(5000);
			
			List<Release> releases = GithubInfoGrabber.getReleases();
			WDL.chatDebug(WDLDebugMessageCause.UPDATE_DEBUG, "Found " + releases.size()
					+ " releases.");
			String launchedVersion = Minecraft.getMinecraft().getVersion();
			
			//TODO: I might want to save this data.
			Release newestCompatibleRelease = null;
			Release activeRelease = null;
			
			for (int i = 0; i < releases.size(); i++) {
				Release release = releases.get(i);
				
				if (newestCompatibleRelease == null) {
					if (release.hiddenInfo != null) {
						if (release.hiddenInfo.mainMinecraftVersion
								.equals(launchedVersion)) {
							newestCompatibleRelease = release;
						}
					}
				}
				
				if (release.tag.equalsIgnoreCase(WDL.VERSION)) {
					activeRelease = release;
				}
			}
			
			if (activeRelease == null) {
				WDL.chatDebug(WDLDebugMessageCause.UPDATE_DEBUG, "Could not find a release "
						+ "for " + WDL.VERSION + ".  You may be running a "
						+ "version that hasn't been released yet.");
				return;
			}
			
			if (newestCompatibleRelease != activeRelease) {
				WDL.chatMsg("You're not running the latest version!  The most recent " 
						+ "version is " + newestCompatibleRelease.tag + "; You are "
						+ "running " + activeRelease.tag + ".");
				
				ChatComponentText linkText = new ChatComponentText(
						"Download the newest release ");
				ChatComponentText link = new ChatComponentText("here");
				link.setChatStyle(link.getChatStyle().setChatClickEvent(
						new ClickEvent(Action.OPEN_URL,
								newestCompatibleRelease.URL)));
				linkText.appendSibling(link);
				
				WDL.chatMessage(linkText);
			}
			
			if (activeRelease.hiddenInfo == null) {
				WDL.chatDebug(WDLDebugMessageCause.UPDATE_DEBUG, "Could not find hidden "
						+ "data for release.  Skipping hashing.");
				return;
			}
			//Check the hashes, and list any failing ones.
			Map<HashData, Object> failed = new HashMap<HashData, Object>();
			
			hashLoop: for (HashData data : activeRelease.hiddenInfo.hashes) {
				try {
					String hash = ClassHasher.hash(data.relativeTo, data.file);
					
					for (String validHash : data.validHashes) {
						if (validHash.equalsIgnoreCase(hash)) {
							// Labeled continues / breaks _are_ a thing.
							// This just continues the outer loop.
							continue hashLoop;
						}
					}
					
					WDL.chatDebug(WDLDebugMessageCause.UPDATE_DEBUG, 
							"Bad hash for " + data.file + " (relative to "
									+ data.relativeTo + "): Expected "
									+ Arrays.toString(data.validHashes)
									+ ", got " + hash);
					
					failed.put(data,  hash);
					continue;
				} catch (Exception e) {
					WDL.chatDebug(WDLDebugMessageCause.UPDATE_DEBUG, "Bad hash for "
							+ data.file + " (relative to " + data.relativeTo
							+ "): Exception: " + e);
					
					failed.put(data,  e);
				}
			}
			
			if (failed.size() > 0) {
				WDL.chatMsg("§cSome files have invalid " +
						"hashes!  Your installation may be corrupt or " +
						"compromised.  If you are running a custom build, " +
						"this is normal.");
			}
		} catch (Exception e) {
			WDL.chatDebug(WDLDebugMessageCause.UPDATE_DEBUG, "Failed to perform update check: "
					+ e.toString());
			e.printStackTrace();
		}
	}
}
