package wdl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.event.HoverEvent;
import net.minecraft.event.HoverEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wdl.api.IWDLMessageType;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

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
	@SuppressWarnings("unused")
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
		
		WDL.defaultProps.setProperty("Messages." + name, 
					Boolean.toString(type.isEnabledByDefault()));
		WDL.defaultProps.setProperty("MessageGroup." + category.internalName, 
				"true");
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
		enableAllMessages = WDL.globalProps.getProperty("Messages.enableAll",
				"true").equals("true");
		
		for (MessageRegistration r : registrations) {
			WDL.baseProps.setProperty(
					"MessageGroup." + r.category.internalName,
					WDL.globalProps.getProperty("MessageGroup."
							+ r.category.internalName, "true"));
			WDL.baseProps.setProperty(
					"Messages." + r.name,
					WDL.globalProps.getProperty("Messages." + r.name));
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
				WDL.baseProps.setProperty("Messages.enableAll",
						WDL.globalProps.getProperty("Messages.enableAll", "true"));
			}
		}
		
		enableAllMessages = WDL.baseProps.getProperty("Messages.enableAll")
				.equals("true");
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
	 * Prints a translated chat message into the chat.
	 * 
	 * @param type
	 *            The type of the message.
	 * @param translationKey
	 *            I18n key that is translated.
	 * @param args
	 *            The arguments to pass to the {@link ChatComponentTranslation}.
	 *            A limited amount of processing is performed: {@link Entity}s
	 *            will be converted properly with a tooltip like the one
	 *            generated by {@link Entity#getDisplayName()}.
	 */
	public static void chatMessageTranslated(IWDLMessageType type,
			String translationKey, Object... args) {
		List<Throwable> exceptionsToPrint = new ArrayList<Throwable>();
		
		for (int i = 0; i < args.length; i++) {
			if (args[i] instanceof Entity) {
				Entity e = (Entity)args[i];
				String entityType = EntityUtils.getEntityType(e);
				HoverEvent event = null;
				String customName = null;
				
				try {
					// Doesn't exist in 1.7.10
					//event = e.getDisplayName().getChatStyle().getChatHoverEvent();
					
					if (e instanceof EntityLiving) {
						if (((EntityLiving) e).hasCustomNameTag()) {
							customName = ((EntityLiving) e).getCustomNameTag();
						}
					}
				} catch (Exception ex) {
					// Maybe an unsafe entity?  Just ignore the exception.
				}
				
				IChatComponent component;
				
				if (customName != null) {
					component = new ChatComponentTranslation(
							"wdl.messages.entityTypeAndCustomName", entityType,
							customName);
				} else {
					component = new ChatComponentText(entityType);
				}
				component.setChatStyle(component.getChatStyle()
						.setChatHoverEvent(event));
				
				args[i] = component;
			} else if (args[i] instanceof Throwable) {
				Throwable t = (Throwable)args[i];
				IChatComponent component = new ChatComponentText(t.toString());
				
				// http://stackoverflow.com/a/1149721/3991344
				StringWriter sw = new StringWriter();
				t.printStackTrace(new PrintWriter(sw));
				String exceptionAsString = sw.toString();
				
				exceptionAsString = exceptionAsString.replace("\r", "")
						.replace("\t", "    ");
				
				HoverEvent event = new HoverEvent(Action.SHOW_TEXT,
						new ChatComponentText(exceptionAsString));
				
				component.setChatStyle(component.getChatStyle()
						.setChatHoverEvent(event));
				
				// Also, log the stacktrace for future use.
				logger.warn(t);
				
				args[i] = component;
				
				exceptionsToPrint.add(t);
			}
		}
		
		chatMessage(type, new ChatComponentTranslation(translationKey, args));
		
		for (int i = 0; i < exceptionsToPrint.size(); i++) {
			logger.warn("Exception #" + (i + 1) + ": ", exceptionsToPrint.get(i));
		}
	}
	
	/**
	 * Prints the given message into the chat.
	 * 
	 * @param type The type of the message.
	 * @param message The message to display.
	 */
	public static void chatMessage(IWDLMessageType type, IChatComponent message) {
		// Can't use a ChatComponentTranslation here because it doesn't like new lines.
		String tooltipText = I18n.format("wdl.messages.tooltip",
				type.getDisplayName()).replace("\r", "");
		IChatComponent tooltip = new ChatComponentText(tooltipText);
		
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
