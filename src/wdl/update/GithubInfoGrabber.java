package wdl.update;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import wdl.WDL;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;

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
	private static final String RELEASE_LIST_LOCATION = "https://api.github.com/repos/pokechu22/WorldDownloader/releases";
	/**
	 * Location for a single release.  The tag for the release needs to be
	 * concatenated to this.
	 */
	private static final String RELEASE_SINGLE_LOCATION = "https://api.github.com/repos/pokechu22/WorldDownloader/releases/tags/";
	
	static {
		// Gets the launched version (appears in F3)
		String launchedVersion = Minecraft.getMinecraft().func_175600_c();
		String brand = ClientBrandRetriever.getClientModName();
		String wdlVersion = WDL.VERSION;
		
		USER_AGENT = String.format("WorldDownloader mod by Pokechu22 ("
				+ "(Minecraft %s/%s; WDL %s) ", launchedVersion, brand,
				wdlVersion);
	}
	
	/**
	 * Attempts to get the release for the current version of WDL.
	 * 
	 * The current version is determined via {@link WDL#VERSION}.
	 */
	public static Release getCurrentRelease() throws Exception {
		JsonElement element = queryJSON(RELEASE_SINGLE_LOCATION + WDL.VERSION);
		return new Release(element.getAsJsonObject());
	}
	
	/**
	 * Gets a list of all releases.
	 */
	public static List<Release> getReleases() throws Exception {
		JsonArray array = queryJSON(RELEASE_LIST_LOCATION).getAsJsonArray();
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
	public static JsonElement queryJSON(String path) throws Exception {
		InputStream stream = null;
		try {
			stream = query(path, "application/json");
			
			InputStreamReader reader = null;
			
			try {
				reader = new InputStreamReader(stream);
				return PARSER.parse(reader);
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
	
	/**
	 * Gets an InputStream from the given path.
	 * 
	 * @param path
	 *            The URL to request.
	 * @param type
	 *            The media type, EG <code>application/json</code> or
	 *            <code>application/octet-stream</code>.
	 */
	public static InputStream query(String path, String type) throws Exception {
		HttpsURLConnection connection = (HttpsURLConnection) (new URL(path))
				.openConnection();
		
		connection.setRequestProperty("User-Agent", USER_AGENT);
		connection.setRequestProperty("Accept", type);
		
		connection.connect();
		
		if (connection.getResponseCode() != 200) {
			throw new Exception("Unexpected response while getting " + path
					+ ": " + connection.getResponseCode() + " "
					+ connection.getResponseMessage());
		}
		
		return connection.getInputStream();
	}
}
