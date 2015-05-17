package wdl;

/**
 * Enum containing various causes for debug text, and whether or not that
 * cause is currently enabled.
 * <br/>
 * Contains a modifiable boolean that states whether the level is
 * enabled.  
 */
public enum WDLDebugMessageCause {
	LOAD_TILE_ENTITY("Loading TileEntity"),
	ON_WORLD_LOAD("World loaded"),
	ON_BLOCK_EVENT("Block Event"),
	ON_MAP_SAVED("Map data saved"),
	ON_CHUNK_NO_LONGER_NEEDED("Chunk unloaded"), 
	ON_GUI_CLOSED_INFO("GUI Closed -- Info"),
	ON_GUI_CLOSED_WARNING("GUI Closed -- Warning"),
	SAVING("Saving data"),
	REMOVE_ENTITY("Removing entity"),
	PLUGIN_CHANNEL_MESSAGE("Plugin channel message");
	
	private WDLDebugMessageCause(String displayText) {
		this.displayText = displayText;
	}
	
	private boolean enabled = true;
	/**
	 * Text to display on a button for this enum value.
	 */
	private final String displayText;

	/**
	 * Whether or not the global debug logging is enabled.
	 */
	public static boolean globalDebugEnabled = true;
	
	public boolean isEnabled() {
		return globalDebugEnabled && this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void toggleEnabled() {
		this.enabled = !this.enabled;
	}
	
	public String getDisplayText() {
		return this.displayText;
	}
	
	@Override
	public String toString() {
		return displayText + ": " + (enabled ? "On" : "Off");
	}
}
