package wdl.api;


/**
 * Marker interface for an extension that listens to WDL's events.
 * <br/>
 * To use this, it MUST be added to the list of WDL mods via
 * {@link WDLApi#addWDLMod(IWDLMod)}. Extensions are not loaded automatically.
 * <br/>
 * For this to actually be useful, implement one of the subinterfaces.
 * <br/>
 * It is recommended to implement {@link IWDLModDescripted} to provide
 * additional information on the extension, but that is not required.
 */
public interface IWDLMod {
	/**
	 * Checks whether this extension is compatible with the given version of WDL
	 * and the rest of the environment (e.g. other mods that it needs) are
	 * correct. The extension will not be loaded if the environment isn't set up
	 * correctly.
	 * 
	 * This is intended to handle compatibility checks before the game crashes.
	 * 
	 * @param version
	 *            The version string for WDL, as found with
	 *            {@link wdl.WDL#VERSION}. It's recommended that you check
	 *            against it.
	 */
	public abstract boolean isValidEnvironment(String version);
	
	/**
	 * Gets info about why the current environment is not valid. Will only be
	 * called if {@link #isValidEnvironment(String)} returned false.
	 * 
	 * @param version
	 *            The version string for WDL.
	 * @return A (possibly translated) error message to display to the user
	 *         about why the environment is not set up correctly. May be null in
	 *         which case a default message is used. If there is no case where
	 *         the build environment would be invalid, this method should return
	 *         null.
	 */
	public abstract String getEnvironmentErrorMessage(String version);
}
