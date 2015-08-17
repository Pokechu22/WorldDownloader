package wdl;

import wdl.api.IWDLMessageType;

public class WDLMessages {
	//TODO: Move logic for what types of messages are enabled here.
}

/**
 * Enum containing WDL's default {@link IWDLMessageType}s.
 * <br/>
 * Contains a modifiable boolean that states whether the level is
 * enabled.  
 */
enum WDLMessageTypes implements IWDLMessageType {
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
	
	private WDLMessageTypes(String displayText, boolean enabledByDefault) {
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
		
		for (WDLMessageTypes value : values()) {
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
	
	public String getDisplayName() {
		return this.displayText;
	}
	
	@Override
	public String toString() {
		return displayText + ": " + (enabled ? "On" : "Off");
	}

	@Override
	public String getTitleColor() {
		return "§2";
	}
	
	@Override
	public String getTextColor() {
		return "§6";
	}

	@Override
	public String getName() {
		return this.name();
	}

	@Override
	public String getDescription() {
		// TODO NYI
		return "";
	}
}
