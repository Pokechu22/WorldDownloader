/*
 * This file is part of WDL Forge.  WDL Forge contains the forge-specific
 * code for World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2019-2020 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wdl.WDL;
import wdl.WDLMessageTypes;
import wdl.WDLMessages;

@Mod("wdl")
public class WDLForgeMod {
	public WDLForgeMod() {
		WDL.bootstrap(Minecraft.getInstance());

		MinecraftForge.EVENT_BUS.register(this);
	}

	private static final String MIXIN_BOOTSTRAP_LINK = "https://www.curseforge.com/minecraft/mc-mods/mixinbootstrap";

	/**
	 * Perform a sanity check on world load to make sure mixins were set up
	 * properly, since if they weren't, the sanity checks won't run otherwise since
	 * the download button doesn't exist.
	 */
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		new Thread(this::runSanityCheck, "WDL startup sanity check").start();
	}

	private void runSanityCheck() {
		try {
			// Wait to ensure that the message isn't hidden behind server join messages
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}

		Minecraft.getInstance().enqueue(() -> {
			if (!WDL.getInstance().runSanityCheck(true)) {
				// This message might not be relevant for other sanity checks,
				// but the mixin sanity checks are the most likely ones to fail.
				// These can't be translated, though, because translations might not be working.
				ITextComponent helpMessage = new StringTextComponent("If mixin changes are missing, make sure ");
				ITextComponent helpMessage2 = new StringTextComponent("MixinBootstrap");
				helpMessage2.getStyle().setColor(TextFormatting.BLUE).setUnderlined(true)
						.setClickEvent(new ClickEvent(Action.OPEN_URL, MIXIN_BOOTSTRAP_LINK));
				helpMessage.appendSibling(helpMessage2);
				ITextComponent helpMessage3 = new StringTextComponent(" is installed.");
				helpMessage.appendSibling(helpMessage3);

				WDLMessages.chatMessage(WDL.serverProps, WDLMessageTypes.ERROR, helpMessage);
			}
		});
	}
}
