package wdl;

/**
 * Enum containing various causes for debug text, and whether or not that
 * cause is currently enabled.
 * <br/>
 * Contains a modifiable boolean that states whether the level is
 * enabled.  
 */
public enum WDLDebugMessageCause {
	LOAD_TILE_ENTITY("Loading TileEntity", false),
	ON_WORLD_LOAD("World loaded", false),
	ON_BLOCK_EVENT("Block Event", true),
	ON_MAP_SAVED("Map data saved", false),
	ON_CHUNK_NO_LONGER_NEEDED("Chunk unloaded", false), 
	ON_GUI_CLOSED_INFO("GUI Closed -- Info", true),
	ON_GUI_CLOSED_WARNING("GUI Closed -- Warning", true),
	SAVING("Saving data", true),
	REMOVE_ENTITY("Removing entity", false),
	PLUGIN_CHANNEL_MESSAGE("Plugin channel message", true);
	
	private WDLDebugMessageCause(String displayText, boolean enabledByDefault) {
		this.displayText = displayText;
		this.enabledByDefault = enabledByDefault;
		
		this.enabled = this.enabledByDefault;
	}
	
	/**
	 * Whether this enum value is enabled by default.
	 */
	private final boolean enabledByDefault;
	
	/**
	 * Whether this enum value is currently enabled.
	 */
	private boolean enabled;
	/**
	 * Text to display on a button for this enum value.
	 */
	private final String displayText;

	/**
	 * Whether or not the global debug logging is enabled.
	 */
	public static boolean globalDebugEnabled = true;
	
	/**
	 * Resets all values to their default enabled state.
	 */
	public static void resetEnabledToDefaults() {
		globalDebugEnabled = true;
		
		for (WDLDebugMessageCause value : values()) {
			value.enabled = value.enabledByDefault;
		}
	}
	
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
