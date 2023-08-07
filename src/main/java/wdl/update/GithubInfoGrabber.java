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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.minecraft.client.Minecraft;
import wdl.VersionConstants;
import wdl.WDL;
import wdl.WDLMessageTypes;
import wdl.WDLMessages;
import wdl.config.settings.MiscSettings;

/**
 * Uses <a href="https://developer.github.com/v3/">GitHub's API</a> to get
 * various data.
 */
public class GithubInfoGrabber {
	@Nonnull
	private static final String USER_AGENT;
	@Nonnull
	private static final JsonParser PARSER = new JsonParser();
	/**
	 * Location of the entire release list.
	 */
	@Nonnull
	private static final String RELEASE_LIST_LOCATION = "https://api.github.com/repos/" + WDL.GITHUB_REPO + "/releases?per_page=100";
	/**
	 * File for the release cache.
	 */
	@Nonnull
	private static final File CACHED_RELEASES_FILE = new File(
			Minecraft.getInstance().gameDir,
			"WorldDownloader_Update_Cache.json");

	static {
		String mcVersion = VersionConstants.getMinecraftVersionInfo();
		String wdlVersion = VersionConstants.getModVersion();

		USER_AGENT = String.format("World Downloader mod by Pokechu22 "
				+ "(Minecraft %s; WDL %s) ", mcVersion, wdlVersion);
	}

	/**
	 * Gets a list of all releases.
	 *
	 * @see https://developer.github.com/v3/repos/releases/#list-releases-for-a-repository
	 */
	@Nonnull
	public static List<Release> getReleases() throws Exception {
		JsonArray array = query(RELEASE_LIST_LOCATION).getAsJsonArray();
		List<Release> returned = new ArrayList<>();
		for (JsonElement element : array) {
			returned.add(new Release(element.getAsJsonObject()));
		}
		return returned;
	}

	/**
	 * Fetches the given URL, and converts it into a {@link JsonElement}.
	 * @param path
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public static JsonElement query(@Nonnull String path) throws Exception {
		InputStream stream = null;
		try {
			HttpsURLConnection connection = (HttpsURLConnection) (new URL(path))
					.openConnection();

			connection.setRequestProperty("User-Agent", USER_AGENT);
			connection.setRequestProperty("Accept",
					"application/vnd.github.v3.full+json");
			// ETag - allows checking if the value was modified (and helps
			// avoid getting rate-limited, as if it is unchanged it no
			// longer counts).
			// See https://developer.github.com/v3/#conditional-requests
			Optional<String> oldEtag = WDL.globalProps.getValue(MiscSettings.UPDATE_ETAG);
			if (oldEtag.isPresent()) {
				connection.setRequestProperty("If-None-Match", oldEtag.get());
			}

			connection.connect();

			if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
				// 304 not modified; use the cached version.
				WDLMessages.chatMessageTranslated(WDL.serverProps,
						WDLMessageTypes.UPDATE_DEBUG, "wdl.messages.updates.usingCachedUpdates");

				stream = new FileInputStream(CACHED_RELEASES_FILE);
			} else if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				// 200 OK
				WDLMessages.chatMessageTranslated(WDL.serverProps,
						WDLMessageTypes.UPDATE_DEBUG, "wdl.messages.updates.grabingUpdatesFromGithub");

				stream = connection.getInputStream();
			} else {
				throw new Exception("Unexpected response while getting " + path
						+ ": " + connection.getResponseCode() + " "
						+ connection.getResponseMessage());
			}

			try (InputStreamReader reader = new InputStreamReader(stream)) {
				JsonElement element = PARSER.parse(reader);

				// Write that cached version to disk, and save the ETAG.
				String etag = null;
				try (PrintStream output = new PrintStream(CACHED_RELEASES_FILE)) {
					output.println(element.toString()); // Write to file

					etag = connection.getHeaderField("ETag");
				} catch (Exception e) {
					// We don't want to cache an old version if didn't save.
					etag = null;
					throw e;
				} finally {
					WDL.globalProps.setValue(MiscSettings.UPDATE_ETAG, Optional.ofNullable(etag));
					WDL.saveGlobalProps();
				}

				return element;
			}
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
}
