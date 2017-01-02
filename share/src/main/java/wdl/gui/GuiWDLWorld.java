package wdl.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import wdl.WDL;

public class GuiWDLWorld extends GuiScreen {
	private String title;
	private GuiScreen parent;
	private GuiButton allowCheatsBtn;
	private GuiButton gamemodeBtn;
	private GuiButton timeBtn;
	private GuiButton weatherBtn;
	private GuiButton spawnBtn;
	private GuiButton pickSpawnBtn;
	private boolean showSpawnFields = false;
	private GuiNumericTextField spawnX;
	private GuiNumericTextField spawnY;
	private GuiNumericTextField spawnZ;
	private int spawnTextY;

	public GuiWDLWorld(GuiScreen parent) {
		this.parent = parent;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui() {
		this.buttonList.clear();
		this.title = I18n.format("wdl.gui.world.title",
				WDL.baseFolderName.replace('@', ':'));
		
		int y = this.height / 4 - 15;
		
		this.gamemodeBtn = new GuiButton(1, this.width / 2 - 100, y,
				this.getGamemodeText());
		this.buttonList.add(this.gamemodeBtn);
		y += 22;
		this.allowCheatsBtn = new GuiButton(6, this.width / 2 - 100, y,
				this.getAllowCheatsText());
		this.buttonList.add(this.allowCheatsBtn);
		y += 22;
		this.timeBtn = new GuiButton(2, this.width / 2 - 100, y, 
				this.getTimeText());
		this.buttonList.add(this.timeBtn);
		y += 22;
		this.weatherBtn = new GuiButton(3, this.width / 2 - 100, y, 
				this.getWeatherText());
		this.buttonList.add(this.weatherBtn);
		y += 22;
		this.spawnBtn = new GuiButton(4, this.width / 2 - 100, y,
				this.getSpawnText());
		this.buttonList.add(this.spawnBtn);
		y += 22;
		this.spawnTextY = y + 4;
		this.spawnX = new GuiNumericTextField(40, this.fontRendererObj, this.width / 2 - 87,
				y, 50, 16);
		this.spawnY = new GuiNumericTextField(41, this.fontRendererObj, this.width / 2 - 19,
				y, 50, 16);
		this.spawnZ = new GuiNumericTextField(42, this.fontRendererObj, this.width / 2 + 48,
				y, 50, 16);
		spawnX.setText(WDL.worldProps.getProperty("SpawnX"));
		spawnY.setText(WDL.worldProps.getProperty("SpawnY"));
		spawnZ.setText(WDL.worldProps.getProperty("SpawnZ"));
		this.spawnX.setMaxStringLength(7);
		this.spawnY.setMaxStringLength(7);
		this.spawnZ.setMaxStringLength(7);
		y += 18;
		this.pickSpawnBtn = new GuiButton(5, this.width / 2, y, 100, 20,
				I18n.format("wdl.gui.world.setSpawnToCurrentPosition"));
		this.buttonList.add(this.pickSpawnBtn);
		
		updateSpawnTextBoxVisibility();
		
		this.buttonList.add(new GuiButton(100, this.width / 2 - 100,
				this.height - 29, I18n.format("gui.done")));
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id == 1) {
				this.cycleGamemode();
			} else if (button.id == 2) {
				this.cycleTime();
			} else if (button.id == 3) {
				this.cycleWeather();
			} else if (button.id == 4) {
				this.cycleSpawn();
			} else if (button.id == 5) {
				this.setSpawnToPlayerPosition();
			} else if (button.id == 6) {
				this.cycleAllowCheats();
			} else if (button.id == 100) {
				this.mc.displayGuiScreen(this.parent);
			}
		}
	}
	
	@Override
	public void onGuiClosed() {
		if (this.showSpawnFields) {
			WDL.worldProps.setProperty("SpawnX", spawnX.getText());
			WDL.worldProps.setProperty("SpawnY", spawnY.getText());
			WDL.worldProps.setProperty("SpawnZ", spawnZ.getText());
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

		if (this.showSpawnFields) {
			this.spawnX.mouseClicked(mouseX, mouseY, mouseButton);
			this.spawnY.mouseClicked(mouseX, mouseY, mouseButton);
			this.spawnZ.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	/**
	 * Fired when a key is typed. This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e).
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		this.spawnX.textboxKeyTyped(typedChar, keyCode);
		this.spawnY.textboxKeyTyped(typedChar, keyCode);
		this.spawnZ.textboxKeyTyped(typedChar, keyCode);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	@Override
	public void updateScreen() {
		this.spawnX.updateCursorCounter();
		this.spawnY.updateCursorCounter();
		this.spawnZ.updateCursorCounter();
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

		if (this.showSpawnFields) {
			this.drawString(this.fontRendererObj, "X:", this.width / 2 - 99,
					this.spawnTextY, 0xFFFFFF);
			this.drawString(this.fontRendererObj, "Y:", this.width / 2 - 31,
					this.spawnTextY, 0xFFFFFF);
			this.drawString(this.fontRendererObj, "Z:", this.width / 2 + 37,
					this.spawnTextY, 0xFFFFFF);
			this.spawnX.drawTextBox();
			this.spawnY.drawTextBox();
			this.spawnZ.drawTextBox();
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
		
		String tooltip = null;
		
		if (allowCheatsBtn.isMouseOver()) {
			tooltip = I18n.format("wdl.gui.world.allowCheats.description");
		} else if (gamemodeBtn.isMouseOver()) {
			tooltip = I18n.format("wdl.gui.world.gamemode.description");
		} else if (timeBtn.isMouseOver()) {
			tooltip = I18n.format("wdl.gui.world.time.description");
		} else if (weatherBtn.isMouseOver()) {
			tooltip = I18n.format("wdl.gui.world.weather.description");
		} else if (spawnBtn.isMouseOver()) {
			tooltip = I18n.format("wdl.gui.world.spawn.description");
		} else if (pickSpawnBtn.isMouseOver()) {
			tooltip = I18n.format("wdl.gui.world.setSpawnToCurrentPosition.description");
		}
		
		if (showSpawnFields) {
			if (Utils.isMouseOverTextBox(mouseX, mouseY, spawnX)) {
				tooltip = I18n.format("wdl.gui.world.spawnPos.description", "X");
			} else if (Utils.isMouseOverTextBox(mouseX, mouseY, spawnY)) {
				tooltip = I18n.format("wdl.gui.world.spawnPos.description", "Y");
			} else if (Utils.isMouseOverTextBox(mouseX, mouseY, spawnZ)) {
				tooltip = I18n.format("wdl.gui.world.spawnPos.description", "Z");
			}
		}
		
		Utils.drawGuiInfoBox(tooltip, width, height, 48);
	}

	private void cycleAllowCheats() {
		if (WDL.baseProps.getProperty("AllowCheats").equals("true")) {
			WDL.baseProps.setProperty("AllowCheats", "false");
		} else {
			WDL.baseProps.setProperty("AllowCheats", "true");
		}
		
		this.allowCheatsBtn.displayString = getAllowCheatsText();
	}
	
	private void cycleGamemode() {
		String prop = WDL.baseProps.getProperty("GameType");

		if (prop.equals("keep")) {
			WDL.baseProps.setProperty("GameType", "creative");
		} else if (prop.equals("creative")) {
			WDL.baseProps.setProperty("GameType", "survival");
		} else if (prop.equals("survival")) {
			WDL.baseProps.setProperty("GameType", "hardcore");
		} else if (prop.equals("hardcore")) {
			WDL.baseProps.setProperty("GameType", "keep");
		}
		
		this.gamemodeBtn.displayString = getGamemodeText();
	}

	private void cycleTime() {
		String prop = WDL.baseProps.getProperty("Time");

		if (prop.equals("keep")) {
			WDL.baseProps.setProperty("Time", "23000");
		} else if (prop.equals("23000")) {
			WDL.baseProps.setProperty("Time", "0");
		} else if (prop.equals("0")) {
			WDL.baseProps.setProperty("Time", "6000");
		} else if (prop.equals("6000")) {
			WDL.baseProps.setProperty("Time", "11500");
		} else if (prop.equals("11500")) {
			WDL.baseProps.setProperty("Time", "12500");
		} else if (prop.equals("12500")) {
			WDL.baseProps.setProperty("Time", "18000");
		} else { // prop.equals("18000")
			WDL.baseProps.setProperty("Time", "keep");
		}
		
		this.timeBtn.displayString = getTimeText();
	}

	private void cycleWeather() {
		String prop = WDL.baseProps.getProperty("Weather");

		if (prop.equals("keep")) {
			WDL.baseProps.setProperty("Weather", "sunny");
		} else if (prop.equals("sunny")) {
			WDL.baseProps.setProperty("Weather", "rain");
		} else if (prop.equals("rain")) {
			WDL.baseProps.setProperty("Weather", "thunderstorm");
		} else if (prop.equals("thunderstorm")) {
			WDL.baseProps.setProperty("Weather", "keep");
		}
		
		this.weatherBtn.displayString = getWeatherText();
	}

	private void cycleSpawn() {
		String prop = WDL.worldProps.getProperty("Spawn");
		
		if (prop.equals("auto")) {
			WDL.worldProps.setProperty("Spawn", "player");
		} else if (prop.equals("player")) {
			WDL.worldProps.setProperty("Spawn", "xyz");
		} else if (prop.equals("xyz")) {
			WDL.worldProps.setProperty("Spawn", "auto");
		}
		
		this.spawnBtn.displayString = getSpawnText();
		updateSpawnTextBoxVisibility();
	}
	
	private String getAllowCheatsText() {
		return I18n.format("wdl.gui.world.allowCheats."
				+ WDL.baseProps.getProperty("AllowCheats"));
	}
	
	private String getGamemodeText() {
		return I18n.format("wdl.gui.world.gamemode."
				+ WDL.baseProps.getProperty("GameType"));
	}
	
	private String getTimeText() {
		String result = I18n.format("wdl.gui.world.time."
				+ WDL.baseProps.getProperty("Time"));
		
		if (result.startsWith("wdl.gui.world.time.")) {
			// Unrecognized time -- not translated
			// Only done with time because time can have a value that
			// isn't on the normal list but still can be parsed
			result = I18n.format("wdl.gui.world.time.custom",
					WDL.baseProps.getProperty("Time"));
		}
		
		return result;
	}
	
	private String getWeatherText() {
		return I18n.format("wdl.gui.world.weather."
				+ WDL.baseProps.getProperty("Weather"));
	}
	
	private String getSpawnText() {
		return I18n.format("wdl.gui.world.spawn."
				+ WDL.worldProps.getProperty("Spawn"));
	}
	
	/**
	 * Recalculates whether the spawn text boxes should be displayed.
	 */
	private void updateSpawnTextBoxVisibility() {
		boolean show = WDL.worldProps.getProperty("Spawn").equals("xyz");
		
		this.showSpawnFields = show;
		this.pickSpawnBtn.visible = show;
	}

	private void setSpawnToPlayerPosition() {
		this.spawnX.setValue((int)WDL.thePlayer.posX);
		this.spawnY.setValue((int)WDL.thePlayer.posY);
		this.spawnZ.setValue((int)WDL.thePlayer.posZ);
	}
}
