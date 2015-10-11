package wdl.gui;

import java.io.IOException;

import wdl.WDL;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class GuiWDLPlayer extends GuiScreen {
	private String title = "";
	private GuiScreen parent;
	private GuiButton healthBtn;
	private GuiButton hungerBtn;
	private GuiButton playerPosBtn;
	private GuiButton pickPosBtn;
	private boolean showPosFields = false;
	private GuiTextField posX;
	private GuiTextField posY;
	private GuiTextField posZ;
	private int posTextY;

	public GuiWDLPlayer(GuiScreen var1) {
		this.parent = var1;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui() {
		this.buttonList.clear();
		this.title = "Player Options for "
				+ WDL.baseFolderName.replace('@', ':');
		int var1 = this.width / 2;
		int var2 = this.height / 4;
		int var3 = var2 - 15;
		this.healthBtn = new GuiButton(1, var1 - 100, var3, "Health: ERROR");
		this.buttonList.add(this.healthBtn);
		this.updateHealth(false);
		var3 += 22;
		this.hungerBtn = new GuiButton(2, var1 - 100, var3, "Hunger: ERROR");
		this.buttonList.add(this.hungerBtn);
		this.updateHunger(false);
		var3 += 22;
		this.playerPosBtn = new GuiButton(3, var1 - 100, var3,
				"Player Position: ERROR");
		this.buttonList.add(this.playerPosBtn);
		var3 += 22;
		this.posTextY = var3 + 4;
		this.posX = new GuiTextField(40, this.fontRendererObj, var1 - 87, var3,
				50, 16);
		this.posY = new GuiTextField(41, this.fontRendererObj, var1 - 19, var3,
				50, 16);
		this.posZ = new GuiTextField(42, this.fontRendererObj, var1 + 48, var3,
				50, 16);
		this.posX.setMaxStringLength(7);
		this.posY.setMaxStringLength(7);
		this.posZ.setMaxStringLength(7);
		var3 += 18;
		this.pickPosBtn = new GuiButton(4, var1 - 0, var3, 100, 20,
				"Current position");
		this.buttonList.add(this.pickPosBtn);
		this.updatePlayerPos(false);
		this.updatePosXYZ(false);
		this.buttonList.add(new GuiButton(100, var1 - 100, height - 29, "Done"));
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton var1) {
		if (var1.enabled) {
			if (var1.id == 1) {
				this.updateHealth(true);
			} else if (var1.id == 2) {
				this.updateHunger(true);
			} else if (var1.id == 3) {
				this.updatePlayerPos(true);
			} else if (var1.id == 4) {
				this.pickPlayerPos();
			} else if (var1.id == 100) {
				this.mc.displayGuiScreen(this.parent);
			}
		}
	}

	@Override
	public void onGuiClosed() {
		if (this.showPosFields) {
			this.updatePosXYZ(true);
		}

		WDL.saveProps();
	}
	
	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
	throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (this.showPosFields) {
			this.posX.mouseClicked(mouseX, mouseY, mouseButton);
			this.posY.mouseClicked(mouseX, mouseY, mouseButton);
			this.posZ.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	/**
	 * Fired when a key is typed. This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e).
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		this.posX.textboxKeyTyped(typedChar, keyCode);
		this.posY.textboxKeyTyped(typedChar, keyCode);
		this.posZ.textboxKeyTyped(typedChar, keyCode);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	@Override
	public void updateScreen() {
		this.posX.updateCursorCounter();
		this.posY.updateCursorCounter();
		this.posZ.updateCursorCounter();
		super.updateScreen();
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		Utils.drawListBackground(23, 32, 0, 0, height, width);
		
		this.drawCenteredString(this.fontRendererObj, this.title,
				this.width / 2, 8, 0xFFFFFF);
		
		String tooltip = null;

		if (this.showPosFields) {
			this.drawString(this.fontRendererObj, "X:", this.width / 2 - 99,
					this.posTextY, 0xFFFFFF);
			this.drawString(this.fontRendererObj, "Y:", this.width / 2 - 31,
					this.posTextY, 0xFFFFFF);
			this.drawString(this.fontRendererObj, "Z:", this.width / 2 + 37,
					this.posTextY, 0xFFFFFF);
			this.posX.drawTextBox();
			this.posY.drawTextBox();
			this.posZ.drawTextBox();
			
			String posToolTip = "Use these text boxes to specify the exact " +
			"player position.\n\n";
			
			if (Utils.isMouseOverTextBox(mouseX, mouseY, posX)) {
				tooltip = posToolTip
						+ "This text box specifies your X coordinate.";
			} else if (Utils.isMouseOverTextBox(mouseX, mouseY, posY)) {
				tooltip = posToolTip
						+ "This text box specifies your Y coordinate.";
			} else if (Utils.isMouseOverTextBox(mouseX, mouseY, posZ)) {
				tooltip = posToolTip
						+ "This text box specifies your Z coordinate.";
			}
			
			if (pickPosBtn.isMouseOver()) {
				tooltip = "Set the position to your current position.";
			}
		}
		
		if (healthBtn.isMouseOver()) {
			tooltip = "This option sets the health that the player has in " +
					"in the downloaded copy of the world.\n\n" + 
					"§lDon't change§r: Keep the health at its value from " +
					"when the download was stopped.\n" +
					"§lFull§r: Set the player's health to the maximum value.";
		}
		if (hungerBtn.isMouseOver()) {
			tooltip = "This option sets the hunger that the player has in " +
					"in the downloaded copy of the world.\n\n" + 
					"§lDon't change§r: Keep the hunger at its value from " +
					"when the download was stopped.\n" +
					"§lFull§r: Set the player's hunger to the maximum value.";
		}
		if (playerPosBtn.isMouseOver()) {
			tooltip = "This option allows choosing where you want the " +
					"player to be located in the downloaded world.\n\n" +
					"§lDon't change§r: Use the position that the player was " +
					"in when the download was stopped.\n" +
					"§lXYZ§r: Allows specifying the exact coordinates.";
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
		
		if (tooltip != null) {
			Utils.drawGuiInfoBox(tooltip, width, height);
		}
	}

	private void updateHealth(boolean var1) {
		String var2 = WDL.baseProps.getProperty("PlayerHealth");

		if (var2.equals("keep")) {
			if (var1) {
				WDL.baseProps.setProperty("PlayerHealth", "20");
				this.updateHealth(false);
			} else {
				this.healthBtn.displayString = "Health: Don\'t change";
			}
		} else if (var2.equals("20")) {
			if (var1) {
				WDL.baseProps.setProperty("PlayerHealth", "keep");
				this.updateHealth(false);
			} else {
				this.healthBtn.displayString = "Health: Full";
			}
		}
	}

	private void updateHunger(boolean var1) {
		String var2 = WDL.baseProps.getProperty("PlayerFood");

		if (var2.equals("keep")) {
			if (var1) {
				WDL.baseProps.setProperty("PlayerFood", "20");
				this.updateHunger(false);
			} else {
				this.hungerBtn.displayString = "Hunger: Don\'t change";
			}
		} else if (var2.equals("20")) {
			if (var1) {
				WDL.baseProps.setProperty("PlayerFood", "keep");
				this.updateHunger(false);
			} else {
				this.hungerBtn.displayString = "Hunger: Full";
			}
		}
	}

	private void updatePlayerPos(boolean var1) {
		String var2 = WDL.worldProps.getProperty("PlayerPos");
		this.showPosFields = false;
		this.pickPosBtn.visible = false;

		if (var2.equals("keep")) {
			if (var1) {
				WDL.worldProps.setProperty("PlayerPos", "xyz");
				this.updatePlayerPos(false);
			} else {
				this.playerPosBtn.displayString = "Player Position: Don\'t change";
			}
		} else if (var2.equals("xyz")) {
			if (var1) {
				WDL.worldProps.setProperty("PlayerPos", "keep");
				this.updatePlayerPos(false);
			} else {
				this.playerPosBtn.displayString = "Player Position:";
				this.showPosFields = true;
				this.pickPosBtn.visible = true;
			}
		}
	}

	private void updatePosXYZ(boolean var1) {
		if (var1) {
			try {
				int var2 = Integer.parseInt(this.posX.getText());
				int var3 = Integer.parseInt(this.posY.getText());
				int var4 = Integer.parseInt(this.posZ.getText());
				WDL.worldProps.setProperty("PlayerX", String.valueOf(var2));
				WDL.worldProps.setProperty("PlayerY", String.valueOf(var3));
				WDL.worldProps.setProperty("PlayerZ", String.valueOf(var4));
			} catch (NumberFormatException var5) {
				this.updatePlayerPos(true);
			}
		} else {
			this.posX.setText(WDL.worldProps.getProperty("PlayerX"));
			this.posY.setText(WDL.worldProps.getProperty("PlayerY"));
			this.posZ.setText(WDL.worldProps.getProperty("PlayerZ"));
		}
	}

	private void pickPlayerPos() {
		int var1 = (int) Math.floor(WDL.thePlayer.posX);
		int var2 = (int) Math.floor(WDL.thePlayer.posY);
		int var3 = (int) Math.floor(WDL.thePlayer.posZ);
		this.posX.setText(String.valueOf(var1));
		this.posY.setText(String.valueOf(var2));
		this.posZ.setText(String.valueOf(var3));
	}
}
