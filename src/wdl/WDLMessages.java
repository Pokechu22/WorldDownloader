package wdl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
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
		public final String owner;
		
		/**
		 * Creates a MessageRegistration.
		 * 
		 * @param name The name to use.
		 * @param type The type bound to this registration.
		 * @param owner The name of the mod adding it.  ("Core" for base WDL)
		 */
		public MessageRegistration(String name, IWDLMessageType type,
				String owner) {
			this.name = name;
			this.type = type;
			this.owner = owner;
		}
		
		@Override
		public String toString() {
			return "MessageRegistration [name=" + name + ", type=" + type
					+ ", owner=" + owner + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((owner == null) ? 0 : owner.hashCode());
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
			if (owner == null) {
				if (other.owner != null) {
					return false;
				}
			} else if (!owner.equals(other.owner)) {
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
	 * @param owner The owning mod (used as a category).
	 */
	public static void registerMessage(String name, IWDLMessageType type,
			String owner) {
		registrations.add(new MessageRegistration(name, type, owner));
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
	 * Gets all of the MessageTypes 
	 * @return All the types, ordered by the creating mod.
	 */
	public static ListMultimap<String, IWDLMessageType> getTypes() {
		ListMultimap<String, IWDLMessageType> returned = LinkedListMultimap.create();
		
		for (MessageRegistration r : registrations) {
			returned.put(r.name, r.type);
		}
		
		return ImmutableListMultimap.copyOf(returned);
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
		IChatComponent text = new ChatComponentText("[WorldDL] ");
		text.getChatStyle().setColor(type.getTitleColor());

		// If the message has its own style, it'll use that instead.
		// TODO: Better way?
		ChatComponentText messageFormat = new ChatComponentText("");
		messageFormat.getChatStyle().setColor(type.getTextColor());

		messageFormat.appendSibling(message);
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
	INFO("General info", EnumChatFormatting.RED, EnumChatFormatting.GOLD, "", true, "Core (Recomended)"),
	ERROR("General errors", EnumChatFormatting.DARK_GREEN, EnumChatFormatting.DARK_RED, "", true, "Core (Recomended)"),
	LOAD_TILE_ENTITY("Loading TileEntity", "", false),
	ON_WORLD_LOAD("World loaded", "", false),
	ON_BLOCK_EVENT("Block Event", "", true),
	ON_MAP_SAVED("Map data saved", "", false),
	ON_CHUNK_NO_LONGER_NEEDED("Chunk unloaded", "", false), 
	ON_GUI_CLOSED_INFO("GUI Closed -- Info", "", true),
	ON_GUI_CLOSED_WARNING("GUI Closed -- Warning", "", true),
	SAVING("Saving data", "", true),
	REMOVE_ENTITY("Removing entity", "", false),
	PLUGIN_CHANNEL_MESSAGE("Plugin channel message", "", true);
	
	/**
	 * Constructor with the default values for a debug message.
	 */
	private WDLMessageTypes(String displayText, String description,
			boolean enabledByDefault) {
		this(displayText, EnumChatFormatting.DARK_GREEN,
				EnumChatFormatting.GOLD, description, enabledByDefault, "Core");
	}
	/**
	 * Constructor that allows specification of all values.
	 */
	private WDLMessageTypes(String displayText, EnumChatFormatting titleColor,
			EnumChatFormatting textColor, String description, boolean enabledByDefault,
			String group) {
		this.displayText = displayText;
		this.titleColor = titleColor;
		this.textColor = textColor;
		this.description = description;
		this.enabledByDefault = enabledByDefault;
		
		WDLMessages.registerMessage(this.name(), this, group);
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
