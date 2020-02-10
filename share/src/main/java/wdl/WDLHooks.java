/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2020 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;

import java.util.Collection;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.util.text.ITextComponent;


/**
 * The various hooks for wdl. <br/>
 * All of these should be called regardless of any WDL state variables.
 * This class forwards the hooks to the appropriate locations.
 */
public final class WDLHooks {
	private WDLHooks() { throw new AssertionError(); }

	/**
	 * Listener which should receive event calls.
	 */
	@Nonnull
	static IHooksListener listener = new BootstrapHooksListener();

	public static interface IHooksListener {
		default void onWorldClientTick(WorldClient sender) {};
		default void onWorldClientRemoveEntityFromWorld(WorldClient sender, int eid) {};
		default void onNHPCHandleChunkUnload(NetHandlerPlayClient sender, WorldClient world, SPacketUnloadChunk packet) {};
		default void onNHPCHandleChat(NetHandlerPlayClient sender, SPacketChat packet) {};
		default void onNHPCHandleMaps(NetHandlerPlayClient sender, SPacketMaps packet) {};
		default void onNHPCHandleCustomPayload(NetHandlerPlayClient sender, SPacketCustomPayload packet) {};
		default void onNHPCHandleBlockAction(NetHandlerPlayClient sender, SPacketBlockAction packet) {};
		default void onNHPCDisconnect(NetHandlerPlayClient sender, ITextComponent reason) {};
		default void onCrashReportPopulateEnvironment(CrashReport report) {};
		default void injectWDLButtons(GuiIngameMenu gui, Collection<GuiButton> buttonList, Consumer<GuiButton> addButton) {};
		default void handleWDLButtonClick(GuiIngameMenu gui, GuiButton button) {};
	}

	private static class BootstrapHooksListener implements IHooksListener {
		@Override
		public void onWorldClientTick(WorldClient sender) {
			// Use this as a time to set up the real listener
			WDL.bootstrap(Minecraft.getInstance());
			if (listener == this) {
				throw new AssertionError("WDL bootstrap failed to change WDLHooks listener from " + this);
			}
			// Forward the event to the new listener
			listener.onWorldClientTick(sender);
		}

		@Override
		public void onCrashReportPopulateEnvironment(CrashReport report) {
			CrashReportCategory cat = report.makeCategory("World Downloader Mod - not bootstrapped yet");
			cat.addDetail("WDL version", VersionConstants::getModVersion);
			cat.addDetail("Targeted MC version", VersionConstants::getExpectedVersion);
			cat.addDetail("Actual MC version", VersionConstants::getMinecraftVersion);
		}
	}

	/**
	 * Called when {@link WorldClient#tick()} is called.
	 * <br/>
	 * Should be at end of the method.
	 */
	public static void onWorldClientTick(WorldClient sender) {
		listener.onWorldClientTick(sender);
	}

	/**
	 * Called when {@link WorldClient#removeEntityFromWorld(int)} is called.
	 * <br/>
	 * Should be at the start of the method.
	 *
	 * @param eid
	 *            The entity's unique ID.
	 */
	public static void onWorldClientRemoveEntityFromWorld(WorldClient sender,
			int eid) {
		listener.onWorldClientRemoveEntityFromWorld(sender, eid);
	}

	/**
	 * Called when {@link NetHandlerPlayClient#processChunkUnload(SPacketUnloadChunk)} is called.
	 * <br/>
	 * Should be at the start of the method.
	 */
	public static void onNHPCHandleChunkUnload(NetHandlerPlayClient sender,
			WorldClient world, SPacketUnloadChunk packet) {
		listener.onNHPCHandleChunkUnload(sender, world, packet);
	}

	/**
	 * Called when {@link NetHandlerPlayClient#handleChat(SPacketChat)} is
	 * called.
	 * <br/>
	 * Should be at the end of the method.
	 */
	public static void onNHPCHandleChat(NetHandlerPlayClient sender,
			SPacketChat packet) {
		listener.onNHPCHandleChat(sender, packet);
	}

	/**
	 * Called when {@link NetHandlerPlayClient#handleMaps(SPacketMaps)} is
	 * called.
	 * <br/>
	 * Should be at the end of the method.
	 */
	public static void onNHPCHandleMaps(NetHandlerPlayClient sender,
			SPacketMaps packet) {
		listener.onNHPCHandleMaps(sender, packet);
	}

	/**
	 * Called when
	 * {@link NetHandlerPlayClient#handleCustomPayload(SPacketCustomPayload)}
	 * is called.
	 * <br/>
	 * Should be at the end of the method.
	 */
	public static void onNHPCHandleCustomPayload(NetHandlerPlayClient sender,
			SPacketCustomPayload packet) {
		listener.onNHPCHandleCustomPayload(sender, packet);
	}

	/**
	 * Called when
	 * {@link NetHandlerPlayClient#handleBlockAction(SPacketBlockAction)} is
	 * called.
	 * <br/>
	 * Should be at the end of the method.
	 */
	public static void onNHPCHandleBlockAction(NetHandlerPlayClient sender,
			SPacketBlockAction packet) {
		listener.onNHPCHandleBlockAction(sender, packet);
	}

	/**
	 * Called when {@link NetHandlerPlayClient#onDisconnect(ITextComponent)} is called.
	 * <br/>
	 * Should be at the start of the method.
	 *
	 * @param reason The reason for the disconnect, as passed to onDisconnect.
	 */
	public static void onNHPCDisconnect(NetHandlerPlayClient sender, ITextComponent reason) {
		listener.onNHPCDisconnect(sender, reason);
	}

	/**
	 * Injects WDL information into a crash report.
	 *
	 * Called at the end of {@link CrashReport#populateEnvironment()}.
	 * @param report
	 */
	public static void onCrashReportPopulateEnvironment(CrashReport report) {
		listener.onCrashReportPopulateEnvironment(report);
	}

	/**
	 * Adds WDL's buttons to the pause menu GUI.
	 *
	 * @param gui        The GUI
	 * @param buttonList The list of buttons in the GUI. This list should not be
	 *                   modified directly.
	 * @param addButton  Method to add a button to the GUI.
	 */
	public static void injectWDLButtons(GuiIngameMenu gui, Collection<GuiButton> buttonList,
			Consumer<GuiButton> addButton) {
		listener.injectWDLButtons(gui, buttonList, addButton);
	}

	/**
	 * Handle clicks to the ingame pause GUI, specifically for the disconnect
	 * button.
	 *
	 * @param gui    The GUI
	 * @param button The button that was clicked.
	 */
	public static void handleWDLButtonClick(GuiIngameMenu gui, GuiButton button) {
		listener.handleWDLButtonClick(gui, button);
	}
}
