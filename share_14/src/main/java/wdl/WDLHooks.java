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
		void onWorldClientTick(ClientWorld sender);
		void onWorldClientRemoveEntityFromWorld(ClientWorld sender, int eid);
		void onNHPCHandleChunkUnload(ClientPlayNetHandler sender, ClientWorld world, SUnloadChunkPacket packet);
		void onNHPCHandleChat(ClientPlayNetHandler sender, SChatPacket packet);
		void onNHPCHandleMaps(ClientPlayNetHandler sender, SMapDataPacket packet);
		void onNHPCHandleCustomPayload(ClientPlayNetHandler sender, SCustomPayloadPlayPacket packet);
		void onNHPCHandleBlockAction(ClientPlayNetHandler sender, SBlockActionPacket packet);
		void onNHPCDisconnect(ClientPlayNetHandler sender, ITextComponent reason);
		void onCrashReportPopulateEnvironment(CrashReport report);
		void injectWDLButtons(IngameMenuScreen gui, Collection<Widget> buttonList, Consumer<Widget> addButton);
		void handleWDLButtonClick(IngameMenuScreen gui, Button button);
	}

	private static class BootstrapHooksListener implements IHooksListener {
		// All of these methods other than the crash one first bootstrap WDL,
		// and then forward the event to the new listener (which should have changed)
		private void bootstrap() {
			WDL.bootstrap(Minecraft.getInstance());
			if (listener == this) {
				throw new AssertionError("WDL bootstrap failed to change WDLHooks listener from " + this);
			}
		}

		@Override
		public void onWorldClientTick(ClientWorld sender) {
			bootstrap();
			listener.onWorldClientTick(sender);
		}

		@Override
		public void onWorldClientRemoveEntityFromWorld(ClientWorld sender, int eid) {
			bootstrap();
			listener.onWorldClientRemoveEntityFromWorld(sender, eid);
		}

		@Override
		public void onNHPCHandleChunkUnload(ClientPlayNetHandler sender, ClientWorld world, SUnloadChunkPacket packet) {
			bootstrap();
			listener.onNHPCHandleChunkUnload(sender, world, packet);
		}

		@Override
		public void onNHPCHandleChat(ClientPlayNetHandler sender, SChatPacket packet) {
			bootstrap();
			listener.onNHPCHandleChat(sender, packet);
		}

		@Override
		public void onNHPCHandleMaps(ClientPlayNetHandler sender, SMapDataPacket packet) {
			bootstrap();
			listener.onNHPCHandleMaps(sender, packet);
		}

		@Override
		public void onNHPCHandleCustomPayload(ClientPlayNetHandler sender, SCustomPayloadPlayPacket packet) {
			bootstrap();
			listener.onNHPCHandleCustomPayload(sender, packet);
		}

		@Override
		public void onNHPCHandleBlockAction(ClientPlayNetHandler sender, SBlockActionPacket packet) {
			bootstrap();
			listener.onNHPCHandleBlockAction(sender, packet);
		}

		@Override
		public void onNHPCDisconnect(ClientPlayNetHandler sender, ITextComponent reason) {
			bootstrap();
			listener.onNHPCDisconnect(sender, reason);
		}

		@Override
		public void injectWDLButtons(IngameMenuScreen gui, Collection<Widget> buttonList, Consumer<Widget> addButton) {
			bootstrap();
			listener.injectWDLButtons(gui, buttonList, addButton);
		}

		@Override
		public void handleWDLButtonClick(IngameMenuScreen gui, Button button) {
			bootstrap();
			listener.handleWDLButtonClick(gui, button);
		}

		// NOTE: This does NOT bootstrap, as we do not want to bootstrap in a crash
		@Override
		public void onCrashReportPopulateEnvironment(CrashReport report) {
			// Trick the crash report handler into not storing a stack trace
			// (we don't want it)
			int stSize;
			try {
				stSize = Thread.currentThread().getStackTrace().length - 1;
			} catch (Exception e) {
				// Ignore
				stSize = 0;
			}
			CrashReportCategory cat = report.makeCategoryDepth("World Downloader Mod - not bootstrapped yet", stSize);
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
