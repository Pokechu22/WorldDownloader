package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.realms.RealmsBridge;

public class GuiIngameMenu extends GuiScreen {
	private int saveStep;
	private int visibleTime;

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called
	 * when the GUI is displayed and when the window resizes, the buttonList is
	 * cleared beforehand.
	 */
	public void initGui() {
		this.saveStep = 0;
		this.buttonList.clear();
		int i = -16;
		int j = 98;
		this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height
				/ 4 + 120 + -16, I18n
				.format("menu.returnToMenu", new Object[0])));

		if (!this.mc.isIntegratedServerRunning()) {
			((GuiButton) this.buttonList.get(0)).displayString = I18n.format(
					"menu.disconnect", new Object[0]);
		}

		this.buttonList
				.add(new GuiButton(4, this.width / 2 - 100, this.height / 4
						+ 24 + -16, I18n.format("menu.returnToGame",
						new Object[0])));
		this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height
				/ 4 + 96 + -16, 98, 20, I18n.format("menu.options",
				new Object[0])));
		GuiButton guibutton = this.addButton(new GuiButton(7,
				this.width / 2 + 2, this.height / 4 + 96 + -16, 98, 20, I18n
						.format("menu.shareToLan", new Object[0])));
		guibutton.enabled = this.mc.isSingleplayer()
				&& !this.mc.getIntegratedServer().getPublic();
		this.buttonList.add(new GuiButton(5, this.width / 2 - 100, this.height
				/ 4 + 48 + -16, 98, 20, I18n.format("gui.achievements",
				new Object[0])));
		this.buttonList
				.add(new GuiButton(6, this.width / 2 + 2, this.height / 4 + 48
						+ -16, 98, 20, I18n.format("gui.stats", new Object[0])));
		/* WDL >>> */
		wdl.WDLHooks.injectWDLButtons(this, buttonList);
		/* <<< WDL */
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed
	 * for buttons)
	 */
	protected void actionPerformed(GuiButton button) throws IOException {
		/* WDL >>> */
		wdl.WDLHooks.handleWDLButtonClick(this, button); 
		/* <<< WDL */
		
		switch (button.id) {
		case 0:
			this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
			break;

		case 1:
			boolean flag = this.mc.isIntegratedServerRunning();
			boolean flag1 = this.mc.isConnectedToRealms();
			button.enabled = false;
			this.mc.world.sendQuittingDisconnectingPacket();
			this.mc.loadWorld((WorldClient) null);

			if (flag) {
				this.mc.displayGuiScreen(new GuiMainMenu());
			} else if (flag1) {
				RealmsBridge realmsbridge = new RealmsBridge();
				realmsbridge.switchToRealms(new GuiMainMenu());
			} else {
				this.mc.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
			}

		case 2:
		case 3:
		default:
			break;

		case 4:
			this.mc.displayGuiScreen((GuiScreen) null);
			this.mc.setIngameFocus();
			break;

		case 5:
			this.mc.displayGuiScreen(new GuiAchievements(this,
					this.mc.player.getStatFileWriter()));
			break;

		case 6:
			this.mc.displayGuiScreen(new GuiStats(this,
					this.mc.player.getStatFileWriter()));
			break;

		case 7:
			this.mc.displayGuiScreen(new GuiShareToLan(this));
		}
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {
		super.updateScreen();
		++this.visibleTime;
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRendererObj,
				I18n.format("menu.game", new Object[0]), this.width / 2, 40,
				16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
