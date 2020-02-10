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
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.network.play.server.SBlockActionPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.network.play.server.SMapDataPacket;
import net.minecraft.network.play.server.SUnloadChunkPacket;
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
		default void onWorldClientTick(ClientWorld sender) {};
		default void onWorldClientRemoveEntityFromWorld(ClientWorld sender, int eid) {};
		default void onNHPCHandleChunkUnload(ClientPlayNetHandler sender, ClientWorld world, SUnloadChunkPacket packet) {};
		default void onNHPCHandleChat(ClientPlayNetHandler sender, SChatPacket packet) {};
		default void onNHPCHandleMaps(ClientPlayNetHandler sender, SMapDataPacket packet) {};
		default void onNHPCHandleCustomPayload(ClientPlayNetHandler sender, SCustomPayloadPlayPacket packet) {};
		default void onNHPCHandleBlockAction(ClientPlayNetHandler sender, SBlockActionPacket packet) {};
		default void onNHPCDisconnect(ClientPlayNetHandler sender, ITextComponent reason) {};
		default void onCrashReportPopulateEnvironment(CrashReport report) {};
		default void injectWDLButtons(IngameMenuScreen gui, Collection<Widget> buttonList, Consumer<Widget> addButton) {};
		default void handleWDLButtonClick(IngameMenuScreen gui, Button button) {};
	}

	private static class BootstrapHooksListener implements IHooksListener {
		@Override
		public void onWorldClientTick(ClientWorld sender) {
			// Use this as a time to set up the real instance
			WDL.bootstrap(Minecraft.getInstance());
			if (listener == this) {
				throw new AssertionError("WDL bootstrap failed to change WDLHooks listener from " + this);
			}
			// Forward the event to the new instance
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
	 * Called when {@link ClientWorld#tick()} is called.
	 * <br/>
	 * Should be at end of the method.
	 */
	public static void onWorldClientTick(ClientWorld sender) {
		listener.onWorldClientTick(sender);
	}

	/**
	 * Called when {@link ClientWorld#removeEntityFromWorld(int)} is called.
	 * <br/>
	 * Should be at the start of the method.
	 *
	 * @param eid
	 *            The entity's unique ID.
	 */
	public static void onWorldClientRemoveEntityFromWorld(ClientWorld sender,
			int eid) {
		listener.onWorldClientRemoveEntityFromWorld(sender, eid);
	}

	/**
	 * Called when {@link ClientPlayNetHandler#processChunkUnload(SUnloadChunkPacket)} is called.
	 * <br/>
	 * Should be at the start of the method.
	 */
	public static void onNHPCHandleChunkUnload(ClientPlayNetHandler sender,
			ClientWorld world, SUnloadChunkPacket packet) {
		listener.onNHPCHandleChunkUnload(sender, world, packet);
	}

	/**
	 * Called when {@link ClientPlayNetHandler#handleChat(SChatPacket)} is
	 * called.
	 * <br/>
	 * Should be at the end of the method.
	 */
	public static void onNHPCHandleChat(ClientPlayNetHandler sender,
			SChatPacket packet) {
		listener.onNHPCHandleChat(sender, packet);
	}

	/**
	 * Called when {@link ClientPlayNetHandler#handleMaps(SMapDataPacket)} is
	 * called.
	 * <br/>
	 * Should be at the end of the method.
	 */
	public static void onNHPCHandleMaps(ClientPlayNetHandler sender,
			SMapDataPacket packet) {
		listener.onNHPCHandleMaps(sender, packet);
	}

	/**
	 * Called when
	 * {@link ClientPlayNetHandler#handleCustomPayload(SCustomPayloadPlayPacket)}
	 * is called.
	 * <br/>
	 * Should be at the end of the method.
	 */
	public static void onNHPCHandleCustomPayload(ClientPlayNetHandler sender,
			SCustomPayloadPlayPacket packet) {
		listener.onNHPCHandleCustomPayload(sender, packet);
	}

	/**
	 * Called when
	 * {@link ClientPlayNetHandler#handleBlockAction(SBlockActionPacket)} is
	 * called.
	 * <br/>
	 * Should be at the end of the method.
	 */
	public static void onNHPCHandleBlockAction(ClientPlayNetHandler sender,
			SBlockActionPacket packet) {
		listener.onNHPCHandleBlockAction(sender, packet);
	}

	/**
	 * Called when {@link ClientPlayNetHandler#onDisconnect(ITextComponent)} is called.
	 * <br/>
	 * Should be at the start of the method.
	 *
	 * @param reason The reason for the disconnect, as passed to onDisconnect.
	 */
	public static void onNHPCDisconnect(ClientPlayNetHandler sender, ITextComponent reason) {
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
	public static void injectWDLButtons(IngameMenuScreen gui, Collection<Widget> buttonList,
			Consumer<Widget> addButton) {
		listener.injectWDLButtons(gui, buttonList, addButton);
	}
	/**
	 * Handle clicks to the ingame pause GUI, specifically for the disconnect
	 * button.
	 *
	 * @param gui    The GUI
	 * @param button The button that was clicked.
	 */
	public static void handleWDLButtonClick(IngameMenuScreen gui, Button button) {
		listener.handleWDLButtonClick(gui, button);
	}
}
