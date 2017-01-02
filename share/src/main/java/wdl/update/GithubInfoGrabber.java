package wdl.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import net.minecraft.client.Minecraft;
import wdl.WDL;
import wdl.WDLMessageTypes;
import wdl.WDLMessages;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Uses <a href="https://developer.github.com/v3/">GitHub's API</a> to get
 * various data.
 */
public class GithubInfoGrabber {
	private static final String USER_AGENT;
	private static final JsonParser PARSER = new JsonParser();
	/**
	 * Location of the entire release list.
	 */
	private static final String RELEASE_LIST_LOCATION = "https://api.github.com/repos/" + WDL.GITHUB_REPO + "/releases?per_page=100";
	/**
	 * File for the release cache.
	 */
	private static final File CACHED_RELEASES_FILE = new File(
			Minecraft.getMinecraft().mcDataDir,
			"WorldDownloader_Update_Cache.json");
	
	static {
		String mcVersion = WDL.getMinecraftVersionInfo();
		String wdlVersion = WDL.VERSION;
		
		USER_AGENT = String.format("WorldDownloader mod by Pokechu22 "
				+ "(Minecraft %s; WDL %s) ", mcVersion, wdlVersion);
	}
	
	/**
	 * Gets a list of all releases.
	 * 
	 * @see https://developer.github.com/v3/repos/releases/#list-releases-for-a-repository
	 */
	public static List<Release> getReleases() throws Exception {
		JsonArray array = query(RELEASE_LIST_LOCATION).getAsJsonArray();
		List<Release> returned = new ArrayList<Release>();
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
	public static JsonElement query(String path) throws Exception {
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
			if (WDL.globalProps.getProperty("UpdateETag") != null) {
				String etag = WDL.globalProps.getProperty("UpdateETag");
				if (!etag.isEmpty()) {
					connection.setRequestProperty("If-None-Match", etag);
				}
			}
			
			connection.connect();
			
			if (connection.getResponseCode() == HttpsURLConnection.HTTP_NOT_MODIFIED) {
				// 304 not modified; use the cached version.
				WDLMessages.chatMessageTranslated(WDLMessageTypes.UPDATE_DEBUG,
						"wdl.messages.updates.usingCachedUpdates");
				
				stream = new FileInputStream(CACHED_RELEASES_FILE);
			} else if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
				// 200 OK
				WDLMessages.chatMessageTranslated(WDLMessageTypes.UPDATE_DEBUG,
						"wdl.messages.updates.grabingUpdatesFromGithub");
				
				stream = connection.getInputStream();
			} else {
				throw new Exception("Unexpected response while getting " + path
						+ ": " + connection.getResponseCode() + " "
						+ connection.getResponseMessage());
			}
			
			InputStreamReader reader = null;
			
			try {
				reader = new InputStreamReader(stream);
				JsonElement element = PARSER.parse(reader);
				
				// Write that cached version to disk, and save the ETAG.
				PrintStream output = null;
				String etag = null;
				try {
					output = new PrintStream(CACHED_RELEASES_FILE);
					output.println(element.toString());
					
					etag = connection.getHeaderField("ETag");
				} catch (Exception e) {
					// We don't want to cache an old version if didn't save.
					etag = null;
					throw e;
				} finally {
					if (output != null) {
						output.close();
					}
					
					if (etag != null) {
						WDL.globalProps.setProperty("UpdateETag", etag);
					} else {
						WDL.globalProps.remove("UpdateETag");
					}
					
					WDL.saveGlobalProps();
				}
				
				return element;
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
}
