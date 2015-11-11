package wdl.gui;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import wdl.WDL;

public class GuiWDLMultiworld extends GuiScreen {
	private GuiScreen parent;
	private GuiButton multiworldEnabledBtn;
	boolean newMultiworldState = false;

	private static final String multiworldMessage = 
			"Multiworld support is required if at least one of the " +
			"following conditions are met:\n" +
			" - \"Multiworld\" is mentioned on the server\'s website\n" +
			" - The server has more than 3 dimensions (or worlds)\n" +
			" - The server has other dimensions than the official ones " +
			"(the overworld, the nether, and the end)\n\n" +
			"Multiworld support requests which world you are in before " +
			"the download is started.  If it isn't enabled but the server " +
			"is a multiworld server, parts of the map may be overwritten.";
	
	//TODO: Some of these things can be constants, but for consistancy aren't.
	//Maybe refactor it?
	private int infoBoxWidth;
	private int infoBoxHeight;
	private int infoBoxX;
	private int infoBoxY;
	private List<String> infoBoxLines;
	
	public GuiWDLMultiworld(GuiScreen var1) {
		this.parent = var1;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		this.buttonList.clear();
		
		infoBoxWidth = 320;
		infoBoxLines = Utils.wordWrap(multiworldMessage, infoBoxWidth - 20);
		infoBoxHeight = (fontRendererObj.FONT_HEIGHT * (infoBoxLines.size() + 1)) + 40;
		
		infoBoxX = this.width / 2 - infoBoxWidth / 2;
		infoBoxY = this.height / 2 - infoBoxHeight / 2;
		
		this.multiworldEnabledBtn = new GuiButton(1, this.width / 2 - 100,
				infoBoxY + infoBoxHeight - 30, 
				"Multiworld support: ERROR");
		this.buttonList.add(this.multiworldEnabledBtn);
		this.updateMultiworldEnabled(false);
		
		this.buttonList.add(new GuiButton(100, this.width / 2 - 100,
				this.height - 29, "OK"));
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton var1) {
		if (var1.enabled) {
			if (var1.id == 1) {
				this.updateMultiworldEnabled(true);
			} else if (var1.id == 100) {
				if (this.newMultiworldState) {
					this.mc.displayGuiScreen(new GuiWDLMultiworldSelect(
							this.parent));
				} else {
					WDL.baseProps.setProperty("LinkedWorlds", "");
					WDL.saveProps();
					WDL.propsFound = true;

					if (this.parent != null) {
						this.mc.displayGuiScreen(new GuiWDL(this.parent));
					} else {
						WDL.start();
						this.mc.displayGuiScreen((GuiScreen) null);
						this.mc.setIngameFocus();
					}
				}
			}
		}
	}

	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
	throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Fired when a key is typed. This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e).
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	@Override
	public void updateScreen() {
		super.updateScreen();
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		Utils.drawBorder(32, 32, 0, 0, height, width);
		
		this.drawCenteredString(this.fontRendererObj, 
				"WorldDownloader: §cMultiworld support",
				this.width / 2, 8, 0xFFFFFF);
		
		drawRect(infoBoxX, infoBoxY, infoBoxX + infoBoxWidth, infoBoxY
				+ infoBoxHeight, 0xB0000000);
		
		int x = infoBoxX + 10;
		int y = infoBoxY + 10;
		
		for (String s : infoBoxLines) {
			this.drawString(fontRendererObj, s, x, y, 0xFFFFFF);
			y += fontRendererObj.FONT_HEIGHT;
		}
		
		//Red box around "multiworld support" button.
		drawRect(
				multiworldEnabledBtn.xPosition - 2,
				multiworldEnabledBtn.yPosition - 2,
				multiworldEnabledBtn.xPosition
						+ multiworldEnabledBtn.getButtonWidth() + 2,
				multiworldEnabledBtn.yPosition + 20 + 2, 0xFFFF0000);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void updateMultiworldEnabled(boolean var1) {
		if (!this.newMultiworldState) {
			if (var1) {
				this.newMultiworldState = true;
				this.updateMultiworldEnabled(false);
			} else {
				this.multiworldEnabledBtn.displayString = "Multiworld support: Disabled";
			}
		} else if (var1) {
			this.newMultiworldState = false;
			this.updateMultiworldEnabled(false);
		} else {
			this.multiworldEnabledBtn.displayString = "Multiworld support: Enabled";
		}
	}
}
