/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2018 Pokechu22, julialy
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
import wdl.settings.CyclableSetting;
import wdl.settings.IConfiguration;
import wdl.settings.MessageSettings;

/**
 * Responsible for displaying messages in chat or the log, depending on whether
 * they are enabled.
 */
public class WDLMessages {
	private static final Logger LOGGER = LogManager.getLogger();
	// XXX This shouldn't be kept here...  Also, is directly referencing it correct?
	private static IConfiguration configuration = WDL.baseProps;

	/**
	 * Information about an individual message type.
	 */
	public static class MessageRegistration {
		public final String name;
		public final IWDLMessageType type;
		public final MessageTypeCategory category;
		public final CyclableSetting<Boolean> setting;

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
			this.setting = new MessageSettings.MessageTypeSetting(this);
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
	 * List of all registrations, by category.
	 */
	private static ListMultimap<MessageTypeCategory, MessageRegistration> registrations = LinkedListMultimap.create();

	/**
	 * Gets the {@link MessageRegistration} for the given name.
	 * @param name The name to look for
	 * @return The registration
	 * @throws IllegalArgumentException for unknown names
	 */
	@Nonnull
	public static MessageRegistration getRegistration(String name) {
		for (MessageRegistration r : registrations.values()) {
			if (r.name.equals(name)) {
				return r;
			}
		}
		throw new IllegalArgumentException("Asked for the registration for " + name + ", but there is no registration for that!");
	}

	/**
	 * Gets the {@link MessageRegistration} for the given {@link IWDLMessageType}.
	 * @param type The type to look for
	 * @return The registration
	 * @throws IllegalArgumentException for unknown names
	 */
	@Nonnull
	public static MessageRegistration getRegistration(IWDLMessageType type) {
		for (MessageRegistration r : registrations.values()) {
			if (r.type.equals(type)) {
				return r;
			}
		}
		throw new IllegalArgumentException("Asked for the registration for " + type + ", but there is no registration for that!");
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
		registrations.put(category, new MessageRegistration(name, type, category));
	}

	/**
	 * Is the specified type enabled?
	 */
	public static boolean isEnabled(IWDLMessageType type) {
		MessageRegistration r = getRegistration(type);
		return configuration.getValue(r.setting);
	}

	/**
	 * Gets whether the given group is enabled.
	 */
	public static boolean isGroupEnabled(MessageTypeCategory group) {
		return configuration.getValue(group.setting);
	}

	/**
	 * Gets all of the MessageTypes
	 * @return All the types, ordered by the category.
	 */
	@Nonnull
	public static ListMultimap<MessageTypeCategory, IWDLMessageType> getTypes() {
		ImmutableListMultimap.Builder<MessageTypeCategory, IWDLMessageType> returned = ImmutableListMultimap.builder();

		for (MessageRegistration r : registrations.values()) {
			returned.put(r.category, r.type);
		}

		return returned.build();
	}

	/**
	 * Reset all settings to default.
	 */
	public static void resetEnabledToDefaults() {
		configuration.clearValue(MessageSettings.ENABLE_ALL_MESSAGES);

		for (MessageTypeCategory cat : registrations.keySet()) {
			configuration.clearValue(cat.setting);
		}
		for (MessageRegistration r : registrations.values()) {
			configuration.clearValue(r.setting);
		}
	}

	/**
	 * Should be called when the server has changed.
	 */
	public static void onNewServer() {
		configuration = WDL.baseProps; // XXX
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

	static {
		for (WDLMessageTypes type : WDLMessageTypes.values()) {
			registerMessage(type.name(), type, type.category);
		}
	}
}
