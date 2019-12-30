/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.update;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import wdl.VersionConstants;
import wdl.WDL;
import wdl.WDLMessageTypes;
import wdl.WDLMessages;
import wdl.config.settings.MiscSettings;
import wdl.update.Release.HashData;

/**
 * Performs the update checking.
 */
public class WDLUpdateChecker extends Thread {
	/**
	 * Has the update check started?
	 */
	private static volatile boolean started = false;
	/**
	 * Has the update check finished?
	 */
	private static volatile boolean finished = false;
	/**
	 * Did something go wrong with the update check?
	 */
	private static volatile boolean failed = false;
	/**
	 * If something went wrong with the update check, what was it?
	 */
	@Nullable
	private static volatile String failReason = null;

	/**
	 * List of releases.  May be null if the checker has not finished.
	 */
	@Nullable
	private static volatile List<Release> releases;

	/**
	 * The release that is currently running.
	 *
	 * May be null.
	 */
	@Nullable
	private static volatile Release runningRelease;

	/**
	 * Gets the current list of releases. May be null if the checker has not
	 * finished.
	 */
	@Nullable
	public static List<Release> getReleases() {
		return releases;
	}

	/**
	 * Gets the current release.  May be null if the checker has not finished
	 * or if the current version isn't released.
	 */
	@Nullable
	public static Release getRunningRelease() {
		return runningRelease;
	}

	/**
	 * Calculates the release that should be used based off of the user's options.
	 *
	 * May be null if the checker has not finished.
	 */
	@Nullable
	public static Release getRecomendedRelease() {
		if (releases == null || releases.isEmpty()) {
			return null;
		}

		String version = "v" + VersionConstants.getModVersion();
		if (isSnapshot(version)) {
			// Running a snapshot version?  Check if a full version was released.
			String realVersion = getRealVersion(version);
			boolean hasRelease = false;
			for (Release release : releases) {
				if (realVersion.equals(release.tag)) {
					hasRelease = true;
				}
			}
			if (!hasRelease) {
				// No full release?  OK, don't recommend they go backwards.
				return null;
				// If there is a full release, we'd recommend the latest release.
			}
		}
		return releases.get(0);
	}

	/**
	 * Is there a new version that should be used?
	 *
	 * True if the running release is not null and if the recommended
	 * release is not the running release.
	 *
	 * The return value of this method may change as the update checker
	 * runs.
	 */
	public static boolean hasNewVersion() {
		if (releases == null || releases.isEmpty()) {
			// Hasn't finished running yet.
			return false;
		}
		Release recomendedRelease = getRecomendedRelease();
		// Note: runningRelease may be unknown; getRecomendedRelease handles that (for snapshots)
		// However, if both are null, we don't want to recommend updating to null; that's pointless
		if (recomendedRelease == null) {
			return false;
		}
		return runningRelease != recomendedRelease;
	}

	/**
	 * Call once the world has loaded.  Will check and start a new update checker
	 * if needed.
	 */
	public static void startIfNeeded() {
		if (!started) {
			started = true;

			new WDLUpdateChecker().start();
		}
	}

	/**
	 * Has the update check finished?
	 */
	public static boolean hasFinishedUpdateCheck() {
		return finished;
	}

	/**
	 * Did something go wrong with the update check?
	 */
	public static boolean hasUpdateCheckFailed() {
		return failed;
	}
	/**
	 * If the update check failed, why?
	 */
	public static String getUpdateCheckFailReason() {
		return failReason;
	}

	private static final String FORUMS_THREAD_USAGE_LINK = "https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds#Usage";
	private static final String WIKI_LINK = "https://github.com/pokechu22/WorldDownloader/wiki";
	private static final String GITHUB_LINK = "https://github.com/pokechu22/WorldDownloader";
	private static final String GITHUB_ISSUES_LINK = "https://github.com/pokechu22/WorldDownloader/issues";
	private static final String REDISTRIBUTION_LINK = "https://pokechu22.github.io/WorldDownloader/redistribution";
	private static final String SMR_LINK = "https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/mods-discussion/2314237-list-of-sites-stealing-minecraft-content";

	private WDLUpdateChecker() {
		super("World Downloader update check thread");
	}

	@Override
	public void run() {
		try {
			if (!WDL.globalProps.getValue(MiscSettings.TUTORIAL_SHOWN)) {
				sleep(5000);

				TextComponentTranslation success = new TextComponentTranslation(
						"wdl.intro.success");
				TextComponentTranslation mcfThread = new TextComponentTranslation(
						"wdl.intro.forumsLink");
				mcfThread.getStyle().setColor(TextFormatting.BLUE).setUnderlined(true)
				.setClickEvent(new ClickEvent(Action.OPEN_URL, FORUMS_THREAD_USAGE_LINK));
				TextComponentTranslation wikiLink = new TextComponentTranslation(
						"wdl.intro.wikiLink");
				wikiLink.getStyle().setColor(TextFormatting.BLUE).setUnderlined(true)
				.setClickEvent(new ClickEvent(Action.OPEN_URL, WIKI_LINK));
				TextComponentTranslation usage = new TextComponentTranslation(
						"wdl.intro.usage", mcfThread, wikiLink);
				TextComponentTranslation githubRepo = new TextComponentTranslation(
						"wdl.intro.githubRepo");
				githubRepo.getStyle().setColor(TextFormatting.BLUE).setUnderlined(true)
				.setClickEvent(new ClickEvent(Action.OPEN_URL, GITHUB_LINK));
				TextComponentTranslation contribute = new TextComponentTranslation(
						"wdl.intro.contribute", githubRepo);
				TextComponentTranslation redistributionList = new TextComponentTranslation(
						"wdl.intro.redistributionList");
				redistributionList.getStyle().setColor(TextFormatting.BLUE).setUnderlined(true)
				.setClickEvent(new ClickEvent(Action.OPEN_URL, REDISTRIBUTION_LINK));
				TextComponentTranslation warning = new TextComponentTranslation(
						"wdl.intro.warning");
				warning.getStyle().setColor(TextFormatting.DARK_RED).setBold(true);
				TextComponentTranslation illegally = new TextComponentTranslation(
						"wdl.intro.illegally");
				illegally.getStyle().setColor(TextFormatting.DARK_RED).setBold(true);
				TextComponentTranslation stolen = new TextComponentTranslation(
						"wdl.intro.stolen", warning, redistributionList, illegally);
				TextComponentTranslation smr = new TextComponentTranslation(
						"wdl.intro.stopModReposts");
				smr.getStyle().setColor(TextFormatting.BLUE).setUnderlined(true)
				.setClickEvent(new ClickEvent(Action.OPEN_URL, SMR_LINK));
				TextComponentTranslation stolenBeware = new TextComponentTranslation(
						"wdl.intro.stolenBeware", smr);

				WDLMessages.chatMessage(WDL.serverProps, WDLMessageTypes.UPDATES, success);
				WDLMessages.chatMessage(WDL.serverProps, WDLMessageTypes.UPDATES, usage);
				WDLMessages.chatMessage(WDL.serverProps, WDLMessageTypes.UPDATES, contribute);
				WDLMessages.chatMessage(WDL.serverProps, WDLMessageTypes.UPDATES, stolen);
				WDLMessages.chatMessage(WDL.serverProps, WDLMessageTypes.UPDATES, stolenBeware);

				WDL.globalProps.setValue(MiscSettings.TUTORIAL_SHOWN, true);
				WDL.saveGlobalProps();
			}

			sleep(5000);

			releases = GithubInfoGrabber.getReleases();
			WDLMessages.chatMessageTranslated(WDL.serverProps,
					WDLMessageTypes.UPDATE_DEBUG, "wdl.messages.updates.releaseCount", releases.size());

			if (releases.isEmpty()) {
				failed = true;
				failReason = "No releases found.";
				return;
			}

			String version = VersionConstants.getModVersion();
			String currentTag = "v" + version;
			for (int i = 0; i < releases.size(); i++) {
				Release release = releases.get(i);

				if (release.tag.equalsIgnoreCase(currentTag)) {
					runningRelease = release;
				}
			}

			if (runningRelease == null) {
				if (!isSnapshot(version)) {
					WDLMessages.chatMessageTranslated(WDL.serverProps,
							WDLMessageTypes.UPDATES,
							"wdl.messages.updates.failedToFindMatchingRelease", currentTag);
				} else {
					WDLMessages.chatMessageTranslated(WDL.serverProps,
							WDLMessageTypes.UPDATES,
							"wdl.messages.updates.failedToFindMatchingRelease.snapshot", currentTag, getRealVersion(version));
				}
				// Wait until the new version check finishes before returning.
			}

			if (hasNewVersion()) {
				Release recomendedRelease = getRecomendedRelease();

				TextComponentTranslation updateLink = new TextComponentTranslation(
						"wdl.messages.updates.newRelease.updateLink");
				updateLink.getStyle().setColor(TextFormatting.BLUE)
				.setUnderlined(true).setClickEvent(
						new ClickEvent(Action.OPEN_URL,
								recomendedRelease.URL));

				// Show the new version available message, and give a link.
				WDLMessages.chatMessageTranslated(WDL.serverProps,
						WDLMessageTypes.UPDATES, "wdl.messages.updates.newRelease",
						currentTag, recomendedRelease.tag, updateLink);
			}

			// Next up: Check if the version is untested.
			if (VersionConstants.isUntestedVersion()) {
				TextComponentTranslation githubIssues = new TextComponentTranslation(
						"wdl.intro.githubRepo");
				githubIssues.getStyle().setColor(TextFormatting.BLUE).setUnderlined(true)
						.setClickEvent(new ClickEvent(Action.OPEN_URL, GITHUB_ISSUES_LINK));
				WDLMessages.chatMessageTranslated(WDL.serverProps,
						WDLMessageTypes.UPDATES, "wdl.messages.updates.untestedVersion",
						VersionConstants.getMinecraftVersion(), githubIssues);
			}

			if (runningRelease == null) {
				// Can't hash without a release, but that's a normal condition (unlike below)
				return;
			}

			if (runningRelease.hiddenInfo == null) {
				WDLMessages.chatMessageTranslated(WDL.serverProps,
						WDLMessageTypes.UPDATE_DEBUG,
						"wdl.messages.updates.failedToFindMetadata", currentTag);
				return;
			}
			//Check the hashes, and list any failing ones.
			Map<HashData, Object> failed = new HashMap<>();

			hashLoop: for (HashData data : runningRelease.hiddenInfo.hashes) {
				try {
					String hash = ClassHasher.hash(data.relativeTo, data.file);

					for (String validHash : data.validHashes) {
						if (validHash.equalsIgnoreCase(hash)) {
							// Labeled continues / breaks _are_ a thing.
							// This just continues the outer loop.
							continue hashLoop;
						}
					}

					WDLMessages.chatMessageTranslated(
							WDL.serverProps,
							WDLMessageTypes.UPDATE_DEBUG, "wdl.messages.updates.incorrectHash",
							data.file, data.relativeTo,
							Arrays.toString(data.validHashes), hash);

					failed.put(data, hash);
					continue;
				} catch (Exception e) {
					WDLMessages.chatMessageTranslated(
							WDL.serverProps,
							WDLMessageTypes.UPDATE_DEBUG, "wdl.messages.updates.hashException",
							data.file, data.relativeTo,
							Arrays.toString(data.validHashes), e);

					failed.put(data, e);
				}
			}

			if (failed.size() > 0) {
				TextComponentTranslation mcfThread = new TextComponentTranslation(
						"wdl.intro.forumsLink");
				mcfThread.getStyle().setColor(TextFormatting.BLUE)
				.setUnderlined(true).setClickEvent(
						new ClickEvent(Action.OPEN_URL,
								FORUMS_THREAD_USAGE_LINK));
				WDLMessages.chatMessageTranslated(WDL.serverProps,
						WDLMessageTypes.UPDATES, "wdl.messages.updates.badHashesFound", mcfThread);
			}
		} catch (Exception e) {
			WDLMessages.chatMessageTranslated(WDL.serverProps,
					WDLMessageTypes.UPDATE_DEBUG, "wdl.messages.updates.updateCheckError", e);

			failed = true;
			failReason = e.toString();
		} finally {
			finished = true;
		}
	}

	private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

	/**
	 * Checks if a version is a snapshot build.
	 *
	 * @param version
	 *            The version to check
	 * @return true if the version is a SNAPSHOT build
	 */
	private static boolean isSnapshot(@Nonnull String version) {
		return version.endsWith(SNAPSHOT_SUFFIX);
	}

	/**
	 * For a snapshot version, gets the version name for the real version.
	 *
	 * @param version
	 *            The version to use. <strong>Must</strong>
	 *            {@linkplain #isSnapshot(String) be a snapshot version}.
	 * @return the regular version name for that snapshot, without the SNAPSHOT suffix.
	 */
	@Nonnull
	private static String getRealVersion(@Nonnull String version) {
		assert isSnapshot(version) : "getRealVersion should only be used with snapshots; got " + version;

		return version.substring(0, version.length() - SNAPSHOT_SUFFIX.length());
	}
}
