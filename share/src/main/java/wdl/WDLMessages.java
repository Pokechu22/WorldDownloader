/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;
import wdl.api.IWDLMessageType;

/**
 * Handles enabling and disabling of all of the messages.
 */
public class WDLMessages {
	private static final Logger LOGGER = LogManager.getLogger();

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
			new ArrayList<>();

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
	@Nonnull
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
	public static void chatMessage(@Nonnull IWDLMessageType type, @Nonnull String message) {
		chatMessage(type, new TextComponentString(message));
	}

	/**
	 * Prints a translated chat message into the chat.
	 *
	 * @param type
	 *            The type of the message.
	 * @param translationKey
	 *            I18n key that is translated.
	 * @param args
	 *            The arguments to pass to the {@link TextComponentTranslation}.
	 *            A limited amount of processing is performed: {@link Entity}s
	 *            will be converted properly with a tooltip like the one
	 *            generated by {@link Entity#getDisplayName()}.
	 */
	public static void chatMessageTranslated(@Nonnull IWDLMessageType type,
			@Nonnull String translationKey, @Nonnull Object... args) {
		List<Throwable> exceptionsToPrint = new ArrayList<>();

		for (int i = 0; i < args.length; i++) {
			if (args[i] == null) {
				TextComponentString text = new TextComponentString("null");
				text.getStyle().setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new TextComponentString("~~null~~")));
				args[i] = text;
			} else if (args[i] instanceof Entity) {
				Entity e = (Entity)args[i];

				args[i] = convertEntityToComponent(e);
			} else if (args[i] instanceof Throwable) {
				Throwable t = (Throwable) args[i];

				args[i] = convertThrowableToComponent(t);
				exceptionsToPrint.add(t);
			} else if (args[i] instanceof BlockPos) {
				// Manually toString BlockPos instances to deal with obfuscation
				BlockPos pos = (BlockPos) args[i];
				args[i] = String.format("Pos[x=%d, y=%d, z=%d]", pos.getX(), pos.getY(), pos.getZ());
			}
		}

		final ITextComponent component;
		if (I18n.hasKey(translationKey)) {
			component = new TextComponentTranslation(translationKey, args);
		} else {
			// Oh boy, no translation text.  Manually apply parameters.
			String message = translationKey;
			component = new TextComponentString(message);
			component.appendText("[");
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof ITextComponent) {
					component.appendSibling((ITextComponent) args[i]);
				} else {
					component.appendText(String.valueOf(args[i]));
				}
				if (i != args.length - 1) {
					component.appendText(", ");
				}
			}
			component.appendText("]");
		}

		chatMessage(type, component);

		for (int i = 0; i < exceptionsToPrint.size(); i++) {
			LOGGER.warn("Exception #" + (i + 1) + ": ", exceptionsToPrint.get(i));
		}
	}

	/**
	 * Prints the given message into the chat.
	 *
	 * @param type The type of the message.
	 * @param message The message to display.
	 */
	public static void chatMessage(@Nonnull IWDLMessageType type, @Nonnull ITextComponent message) {
		// Can't use a TextComponentTranslation here because it doesn't like new lines.
		String tooltipText = I18n.format("wdl.messages.tooltip",
				type.getDisplayName()).replace("\r", "");
		ITextComponent tooltip = new TextComponentString(tooltipText);

		ITextComponent text = new TextComponentString("");

		ITextComponent header = new TextComponentString("[WorldDL]");
		header.getStyle().setColor(type.getTitleColor());
		header.getStyle().setHoverEvent(
				new HoverEvent(Action.SHOW_TEXT, tooltip));

		// If the message has its own style, it'll use that instead.
		// TODO: Better way?
		TextComponentString messageFormat = new TextComponentString(" ");
		messageFormat.getStyle().setColor(type.getTextColor());

		messageFormat.appendSibling(message);
		text.appendSibling(header);
		text.appendSibling(messageFormat);
		if (isEnabled(type)) {
			Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(
					text);
		} else {
			LOGGER.info(text.getUnformattedText());
		}
	}

	@Nonnull
	private static ITextComponent convertEntityToComponent(@Nonnull Entity e) {
		ITextComponent wdlName, displayName;

		try {
			String identifier = EntityUtils.getEntityType(e);
			if (identifier == null) {
				wdlName = new TextComponentTranslation("wdl.messages.entityData.noKnownName");
			} else {
				String group = EntityUtils.getEntityGroup(identifier);
				String displayIdentifier = EntityUtils.getDisplayType(identifier);
				String displayGroup = EntityUtils.getDisplayGroup(group);
				wdlName = new TextComponentString(displayIdentifier);

				ITextComponent hoverText = new TextComponentString("");
				hoverText.appendSibling(new TextComponentTranslation("wdl.messages.entityData.internalName", identifier));
				hoverText.appendText("\n");
				hoverText.appendSibling(new TextComponentTranslation("wdl.messages.entityData.group", displayGroup, group));

				wdlName.getStyle().setHoverEvent(new HoverEvent(Action.SHOW_TEXT, hoverText));
			}
		} catch (Exception ex) {
			LOGGER.warn("[WDL] Exception in entity name!", ex);
			wdlName = convertThrowableToComponent(ex);
		}
		try {
			displayName = e.getDisplayName();
		} catch (Exception ex) {
			LOGGER.warn("[WDL] Exception in entity display name!", ex);
			displayName = convertThrowableToComponent(ex);
		}

		return new TextComponentTranslation("wdl.messages.entityData", wdlName, displayName);
	}

	@Nonnull
	private static ITextComponent convertThrowableToComponent(@Nonnull Throwable t) {
		ITextComponent component = new TextComponentString(t.toString());

		// http://stackoverflow.com/a/1149721/3991344
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		String exceptionAsString = sw.toString();

		exceptionAsString = exceptionAsString.replace("\r", "")
				.replace("\t", "    ");

		HoverEvent event = new HoverEvent(Action.SHOW_TEXT,
				new TextComponentString(exceptionAsString));

		component.getStyle().setHoverEvent(event);

		return component;
	}
}
