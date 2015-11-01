package wdl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.event.HoverEvent;
import net.minecraft.event.HoverEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;

import wdl.api.IWDLMessageType;
import wdl.api.IWDLMod;

/**
 * Handles enabling and disabling of all of the messages.
 */
public class WDLMessages {
	private static Logger logger = LogManager.getLogger();
	
	/**
	 * Information about an individual message type.
	 */
	private static class MessageRegistration {
		public final String name;
		public final IWDLMessageType type;
		public final MessageTypeCategory category;
		
		/**
		 * Creates a MessageRegistration.
		 * 
		 * @param name The name to use.
		 * @param type The type bound to this registration.
		 * @param category The category.
		 */
		public MessageRegistration(String name, IWDLMessageType type,
				MessageTypeCategory category) {
			this.name = name;
			this.type = type;
			this.category = category;
		}
		
		@Override
		public String toString() {
			return "MessageRegistration [name=" + name + ", type=" + type
					+ ", category=" + category + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((category == null) ? 0 : category.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof MessageRegistration)) {
				return false;
			}
			MessageRegistration other = (MessageRegistration) obj;
			if (name == null) {
				if (other.name != null) {
					return false;
				}
			} else if (!name.equals(other.name)) {
				return false;
			}
			if (category == null) {
				if (other.category != null) {
					return false;
				}
			} else if (!category.equals(other.category)) {
				return false;
			}
			if (type == null) {
				if (other.type != null) {
					return false;
				}
			} else if (!type.equals(other.type)) {
				return false;
			}
			return true;
		}
	}
	
	/**
	 * If <code>false</code>, all messages are disabled.  Otherwise, per-
	 * message settings are used.
	 */
	public static boolean enableAllMessages = true;
	
	/**
	 * List of all registrations.
	 */
	private static List<MessageRegistration> registrations =
			new ArrayList<MessageRegistration>();
	
	/**
	 * Gets the {@link MessageRegistration} for the given name.
	 * @param name
	 * @return The registration or null if none is found.
	 */
	private static MessageRegistration getRegistration(String name) {
		for (MessageRegistration r : registrations) {
			if (r.name.equals(name)) {
				return r;
			}
		}
		return null;
	}
	
	/**
	 * Gets the {@link MessageRegistration} for the given {@link IWDLMessageType}.
	 * @param name
	 * @return The registration or null if none is found.
	 */
	private static MessageRegistration getRegistration(IWDLMessageType type) {
		for (MessageRegistration r : registrations) {
			if (r.type.equals(type)) {
				return r;
			}
		}
		return null;
	}
	
	/**
	 * Adds registration for another type of message.
	 * 
	 * @param name The programmatic name.
	 * @param type The type.
	 * @param category The category.
	 */
	public static void registerMessage(String name, IWDLMessageType type,
			MessageTypeCategory category) {
		registrations.add(new MessageRegistration(name, type, category));
	}
	
	/**
	 * Is the specified type enabled?
	 */
	public static boolean isEnabled(IWDLMessageType type) {
		if (type == null) {
			return false;
		}
		if (!enableAllMessages) {
			return false;
		}
		MessageRegistration r = getRegistration(type);
		if (r == null) {
			return false;
		}
		
		if (!isGroupEnabled(r.category)) {
			return false;
		}
		
		if (!WDL.baseProps.containsKey("Messages." + r.name)) {
			if (WDL.baseProps.containsKey("Debug." + r.name)) {
				//Updating from older version
				WDL.baseProps.put("Messages." + r.name,
						WDL.baseProps.remove("Debug." + r.name));
			} else {
				WDL.baseProps.setProperty("Messages." + r.name,
						Boolean.toString(r.type.isEnabledByDefault()));
			}
		}
		return WDL.baseProps.getProperty("Messages." + r.name).equals("true");
	}
	
	/**
	 * Toggles whether the given type is enabled.
	 * @param type
	 */
	public static void toggleEnabled(IWDLMessageType type) {
		MessageRegistration r = getRegistration(type);
		
		if (r != null) {
			WDL.baseProps.setProperty("Messages." + r.name,
					Boolean.toString(!isEnabled(type)));
		}
	}
	
	/**
	 * Gets whether the given group is enabled.
	 */
	public static boolean isGroupEnabled(MessageTypeCategory group) {
		if (!enableAllMessages) {
			return false;
		}
		
		return WDL.baseProps.getProperty(
				"MessageGroup." + group.internalName, "true").equals(
				"true");
	}
	
	/**
	 * Toggles whether a group is enabled or not.
	 */
	public static void toggleGroupEnabled(MessageTypeCategory group) {
		WDL.baseProps.setProperty("MessageGroup." + group.internalName,
				Boolean.toString(!isGroupEnabled(group)));
	}
	
	/**
	 * Gets all of the MessageTypes 
	 * @return All the types, ordered by the category.
	 */
	public static ListMultimap<MessageTypeCategory, IWDLMessageType> getTypes() {
		ListMultimap<MessageTypeCategory, IWDLMessageType> returned = LinkedListMultimap
				.create();
		
		for (MessageRegistration r : registrations) {
			returned.put(r.category, r.type);
		}
		
		return ImmutableListMultimap.copyOf(returned);
	}
	
	/**
	 * Reset all settings to default.
	 */
	public static void resetEnabledToDefaults() {
		WDL.baseProps.setProperty("Messages.enableAll", "true");
		enableAllMessages = true;
		
		for (MessageRegistration r : registrations) {
			WDL.baseProps.setProperty(
					"MessageGroup." + r.category.internalName, "true");
			WDL.baseProps.setProperty("Messages." + r.name,
					Boolean.toString(r.type.isEnabledByDefault()));
		}
	}
	
	/**
	 * Should be called when the server has changed.
	 */
	public static void onNewServer() {
		if (!WDL.baseProps.containsKey("Messages.enableAll")) {
			if (WDL.baseProps.containsKey("Debug.globalDebugEnabled")) {
				//Port from old version.
				WDL.baseProps.put("Messages.enableAll",
						WDL.baseProps.remove("Debug.globalDebugEnabled"));
			} else {
				WDL.baseProps.setProperty("Messages.enableAll", "true");
			}
			enableAllMessages = WDL.baseProps.getProperty("Messages.enableAll")
					.equals("true");
		}
	}
	
	/**
	 * Prints the given message into the chat.
	 * 
	 * @param type The type of the message.
	 * @param message The message to display.
	 */
	public static void chatMessage(IWDLMessageType type, String message) {
		chatMessage(type, new ChatComponentText(message));
	}
	
	/**
	 * Prints the given message into the chat.
	 * 
	 * @param type The type of the message.
	 * @param message The message to display.
	 */
	public static void chatMessage(IWDLMessageType type, IChatComponent message) {
		IChatComponent tooltip = new ChatComponentText(
				"§rThis is a message generated by the \"World Downloader\" " +
				"mod.\n§rYou can disable this message by turning off \n§r" +
				"§l" + type.getDisplayName() + "§r in the WDL messages GUI.");
		
		IChatComponent text = new ChatComponentText("");
		
		IChatComponent header = new ChatComponentText("[WorldDL]");
		header.getChatStyle().setColor(type.getTitleColor());
		header.getChatStyle().setChatHoverEvent(
				new HoverEvent(Action.SHOW_TEXT, tooltip));

		// If the message has its own style, it'll use that instead.
		// TODO: Better way?
		ChatComponentText messageFormat = new ChatComponentText(" ");
		messageFormat.getChatStyle().setColor(type.getTextColor());

		messageFormat.appendSibling(message);
		text.appendSibling(header);
		text.appendSibling(messageFormat);
		if (isEnabled(type)) {
			Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(
					text);
		} else {
			logger.info(text.getUnformattedText());
		}
	}
}

/**
 * Enum containing WDL's default {@link IWDLMessageType}s.
 * <br/>
 * Contains a modifiable boolean that states whether the level is
 * enabled.  
 */
enum WDLMessageTypes implements IWDLMessageType {
	INFO("General info", EnumChatFormatting.RED, EnumChatFormatting.GOLD, 
			"General messages, such as \"Download started\" and \"Save " + 
			"complete\".\n\nRecomended as it includes information about " +
			"when it is safe to stop playing, and generally is very minimal.",
			true, MessageTypeCategory.CORE_RECOMMENDED),
	ERROR("General errors", EnumChatFormatting.DARK_GREEN, EnumChatFormatting.DARK_RED,
			"General errors, such as \"Failed to save chunk\" and \"Failed " +
			"to import tile entities\".\n\nRecomended as it is generally " + 
			"important information.", true, MessageTypeCategory.CORE_RECOMMENDED),
	LOAD_TILE_ENTITY("Loading TileEntity", 
			"Occurs when a tile entity is imported from the previously " +
			"saved version of a chunk.  There may be many tile " + 
			"entities in a chunk.", false),
	ON_WORLD_LOAD("World loaded", 
			"Information when a world loads, including whether the player " +
			"is on the same server or changed servers, and the server's " +
			"brand (version).", false),
	ON_BLOCK_EVENT("Block Event", 
			"Information about a block event.  This is only used for note " +
			"blocks to include the saved pitch.", true),
	ON_MAP_SAVED("Map data saved", 
			"Occurs when new map (the item) data is saved.  Note that map " +
			"data is sent about once per second when a map is in an item " +
			"frame, and even more frequently when in one's inventory.", false),
	ON_CHUNK_NO_LONGER_NEEDED("Chunk unloaded", 
			"Occurs when a chunk is unloaded and saved.", false), 
	ON_GUI_CLOSED_INFO("GUI Closed -- Info", 
			"Information about when an item's GUI is closed.  This message " + 
			"is used to state that the contents of the item were saved " + 
			"successfully.", true),
	ON_GUI_CLOSED_WARNING("GUI Closed -- Warning", 
			"Warnings when an item's GUI is closed.  This message is used " +
			"when something went wrong -- usually, when you are looking at " +
			"something else.", true),
	SAVING("Saving data", 
			"Gives details about the current progress in saving the " + 
			"downloaded world.", true),
	REMOVE_ENTITY("Removing entity", 
			"Occurs whenever an entity is removed from the world, and " +
			"whether that entity was saved.", false),
	PLUGIN_CHANNEL_MESSAGE("Plugin channel message", 
			"Information about the serverside configuration system and user " +
			"rights.", false);
	
	/**
	 * Constructor with the default values for a debug message.
	 */
	private WDLMessageTypes(String displayText, String description,
			boolean enabledByDefault) {
		this(displayText, EnumChatFormatting.DARK_GREEN,
				EnumChatFormatting.GOLD, description, enabledByDefault,
				MessageTypeCategory.CORE_DEBUG);
	}
	/**
	 * Constructor that allows specification of all values.
	 */
	private WDLMessageTypes(String displayText, EnumChatFormatting titleColor,
			EnumChatFormatting textColor, String description, boolean enabledByDefault,
			MessageTypeCategory category) {
		this.displayText = displayText;
		this.titleColor = titleColor;
		this.textColor = textColor;
		this.description = description;
		this.enabledByDefault = enabledByDefault;
		
		WDLMessages.registerMessage(this.name(), this, category);
	}
	
	/**
	 * Text to display on a button for this enum value.
	 */
	private final String displayText;
	/**
	 * Format code for the '[WorldDL]' label.
	 */
	private final EnumChatFormatting titleColor;
	/**
	 * Format code for the text after the label.
	 */
	private final EnumChatFormatting textColor;
	/**
	 * Description text.
	 */
	private final String description;
	/**
	 * Whether this type of message is enabled by default.
	 */
	private final boolean enabledByDefault;
	
	public String getDisplayName() {
		return this.displayText;
	}

	@Override
	public EnumChatFormatting getTitleColor() {
		return titleColor;
	}
	
	@Override
	public EnumChatFormatting getTextColor() {
		return textColor;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public boolean isEnabledByDefault() {
		return enabledByDefault;
	}
}
