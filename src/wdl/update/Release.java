package wdl.update;

import com.google.gson.JsonObject;

/**
 * An individual GitHub release.
 * <br/>
 * Does not contain all of the info, but the used info is included.
 * 
 * @see https://developer.github.com/v3/repos/releases/#get-a-release-by-tag-name
 */
public class Release {
	public Release(JsonObject object) {
		this.object = object;
		
		this.URL = object.get("html_url").getAsString();
		this.tag = object.get("tag_name").getAsString();
		this.title = object.get("name").getAsString();
		this.date = object.get("published_at").getAsString();
		this.prerelease = object.get("prerelease").getAsBoolean();
	}
	
	/**
	 * {@link JsonObject} used to create this.
	 */
	public final JsonObject object;
	/**
	 * URL to the release page.
	 */
	public final String URL;
	/**
	 * Tag name.
	 */
	public final String tag;
	/**
	 * Title for this release.
	 */
	public final String title;
	/**
	 * Date that the release was published on.
	 */
	public final String date;
	/**
	 * Whether the release is a prerelease.
	 */
	public final boolean prerelease;
	@Override
	public String toString() {
		return "Release [URL=" + URL + ", tag=" + tag + ", title=" + title
				+ ", date=" + date + ", prerelease=" + prerelease + "]";
	}
}
