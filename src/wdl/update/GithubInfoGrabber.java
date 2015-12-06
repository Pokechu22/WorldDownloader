package wdl.update;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import wdl.WDL;

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
	private static final String RELEASE_LIST_LOCATION = "https://api.github.com/repos/pokechu22/WorldDownloader/releases?per_page=100";
	
	static {
		String mcVersion = WDL.getMinecraftVersionInfo();
		String wdlVersion = WDL.VERSION;
		
		USER_AGENT = String.format("WorldDownloader mod by Pokechu22 ("
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
			
			connection.connect();
			
			if (connection.getResponseCode() != 200) {
				throw new Exception("Unexpected response while getting " + path
						+ ": " + connection.getResponseCode() + " "
						+ connection.getResponseMessage());
			}
			
			stream = connection.getInputStream();
			
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
}
