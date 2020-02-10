/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018-2020 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;


import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.function.Consumer;

import org.junit.Test;

import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import wdl.WDLHooks.IHooksListener;
import wdl.handler.AbstractWorldBehaviorTest;
import wdl.versioned.VersionedFunctions;

/**
 * Test to make sure all hooks in WDLHooks are called at the right times.
 */
public class WDLHooksTest extends AbstractWorldBehaviorTest {
	@Test
	public void testWorldClientTick() {
		doWithMockHooks(mock -> {
			makeMockWorld();
			clientWorld.tick();

			verify(mock).onWorldClientTick(clientWorld);
		});
	}

	@Test
	public void testWorldClientRemoveEntityFromWorld() {
		doWithMockHooks(mock -> {
			makeMockWorld();
			clientWorld.removeEntityFromWorld(0);

			verify(mock).onWorldClientRemoveEntityFromWorld(clientWorld, 0);
		});
	}

	@Test
	public void testHandleChunkUnload() {
		doWithMockHooks(mock -> {
			makeMockWorld();
			SPacketUnloadChunk packet = new SPacketUnloadChunk(4, 4);
			clientPlayer.connection.processChunkUnload(packet);

			verify(mock).onNHPCHandleChunkUnload(same(clientPlayer.connection), any(), same(packet));
		});
	}

	@Test
	public void testDisconnect() {
		doWithMockHooks(mock -> {
			makeMockWorld();
			TextComponentString reason = new TextComponentString("Disconnected");
			clientPlayer.connection.onDisconnect(reason);

			verify(mock).onNHPCDisconnect(clientPlayer.connection, reason);
		});
	}

	@Test
	public void testHandleBlockAction() {
		doWithMockHooks(mock -> {
			makeMockWorld();
			SPacketBlockAction packet = new SPacketBlockAction(BlockPos.ZERO, Blocks.NOTE_BLOCK, 0, 0);
			clientPlayer.connection.handleBlockAction(packet);

			verify(mock).onNHPCHandleBlockAction(clientPlayer.connection, packet);
		});
	}

	@Test
	public void testHandleCustomPayload() {
		doWithMockHooks(mock -> {
			makeMockWorld();
			SPacketCustomPayload packet = VersionedFunctions.makeServerPluginMessagePacket("somechannel", new byte[0]);
			clientPlayer.connection.handleCustomPayload(packet);

			verify(mock).onNHPCHandleCustomPayload(clientPlayer.connection, packet);
		});
	}

	@Test
	public void testHandleChat() {
		doWithMockHooks(mock -> {
			makeMockWorld();
			SPacketChat packet = new SPacketChat(new TextComponentString("Hello world"));
			clientPlayer.connection.handleChat(packet);

			verify(mock).onNHPCHandleChat(clientPlayer.connection, packet);
		});
	}

	@Test
	public void testHandleMaps() {
		doWithMockHooks(mock -> {
			makeMockWorld();
			SPacketMaps packet = new SPacketMaps(0, (byte)0, false, Collections.emptyList(), new byte[0], 0, 0, 0, 0);
			clientPlayer.connection.handleMaps(packet);

			verify(mock).onNHPCHandleMaps(clientPlayer.connection, packet);
		});
	}

	/**
	 * Creates a mock WDLHooks listener and modifies the value of
	 * {@link WDLHooks#listener} temporarily.
	 *
	 * @param action Action to perform with the modified WDLHooks listener
	 */
	protected synchronized void doWithMockHooks(Consumer<IHooksListener> action) {
		IHooksListener prevListener = WDLHooks.listener;
		try {
			IHooksListener mockListener = mock(IHooksListener.class);
			WDLHooks.listener = mockListener;

			action.accept(mockListener);

			verifyNoMoreInteractions(mockListener);
		} finally {
			WDLHooks.listener = prevListener;
		}
	}
}
