package wdl.gui;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import wdl.WDL;

/**
 * A GUI for selecting which world the player is currently in.
 */
public class GuiWDLMultiworldSelect extends GuiTurningCameraBase {
	/**
	 * The cancel button.
	 */
	private GuiButton cancelBtn;
	/**
	 * The "New name" field.
	 */
	private GuiTextField newNameField;
	/**
	 * Whether a new multiworld is being created.
	 */
	private boolean newWorld = false;
	/**
	 * A map of all worlds to their buttons.
	 */
	private String[] worlds;
	/**
	 * A map of all worlds to their buttons.
	 */
	private GuiButton[] buttons;
	/**
	 * The parent GUI screen.
	 */
	private GuiScreen parent;

	public GuiWDLMultiworldSelect(GuiScreen parent) {
		this.parent = parent;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		
		int buttonsPerRow = this.width / 150;

		if (buttonsPerRow == 0) {
			buttonsPerRow = 1;
		}

		int var4 = this.width / buttonsPerRow - 5;
		this.cancelBtn = new GuiButton(-1, this.width / 2 - 100, this.height - 30,
				I18n.format("gui.cancel"));
		this.buttonList.add(this.cancelBtn);
		String[] linkedWorlds = WDL.baseProps.getProperty("LinkedWorlds")
				.split("|");
		String[] linkedWorldsDup = new String[linkedWorlds.length];
		int var8 = 0;
		
		for (int i = 0; i < linkedWorlds.length; i++) {
			if (linkedWorlds[i].isEmpty()) {
				linkedWorlds[i] = null;
			} else {
				Properties prop = WDL.loadWorldProps(linkedWorlds[i]);

				if (prop == null) {
					linkedWorlds[i] = null;
				} else {
					++var8;
					linkedWorldsDup[i] = prop.getProperty("WorldName");
				}
			}
		}

		if (buttonsPerRow > var8 + 1) {
			buttonsPerRow = var8 + 1;
		}

		int temp = (this.width - buttonsPerRow * var4) / 2;
		this.worlds = new String[var8];
		this.buttons = new GuiButton[var8 + 1];
		
		for (int i = 0, usedIndex = 0; i < linkedWorlds.length; ++i) {
			if (linkedWorlds[i] != null) {
				this.worlds[usedIndex] = linkedWorlds[i];
				this.buttons[usedIndex] = new GuiButton(usedIndex, usedIndex % buttonsPerRow * var4
						+ temp, this.height - 60 - usedIndex / buttonsPerRow * 21, var4, 20,
						linkedWorldsDup[i]);
				this.buttonList.add(this.buttons[usedIndex]);
				
				usedIndex++;
			}
		}

		int lastButtonIndex = this.buttons.length - 1;

		if (!this.newWorld) {
			this.buttons[lastButtonIndex] = new GuiButton(lastButtonIndex, lastButtonIndex  % buttonsPerRow * var4
					+ temp, this.height - 60 - lastButtonIndex / buttonsPerRow * 21, var4, 20,
					I18n.format("wdl.gui.multiworld.newName"));
			this.buttonList.add(this.buttons[lastButtonIndex]);
		}

		this.newNameField = new GuiTextField(40, this.fontRendererObj, lastButtonIndex
				% buttonsPerRow * var4 + temp, this.height - 60 - lastButtonIndex / buttonsPerRow * 21 + 1,
				var4, 18);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			this.newWorld = false;

			if (button.id == this.worlds.length) {
				this.newWorld = true;
				this.buttonList.remove(this.buttons[this.worlds.length]);
			} else if (button.id == -1) {
				this.mc.displayGuiScreen((GuiScreen) null);
				this.mc.setIngameFocus();
			} else {
				this.worldSelected(this.worlds[button.id]);
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

		if (this.newWorld) {
			this.newNameField.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	/**
	 * Fired when a key is typed. This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e).
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);

		if (this.newNameField.isFocused()) {
			this.newNameField.textboxKeyTyped(typedChar, keyCode);

			if (keyCode == 28) {
				String newName = this.newNameField.getText();

				if (newName != null && !newName.isEmpty()) {
					this.worldSelected(this.addMultiworld(newName));
				}
			}
		}
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	@Override
	public void updateScreen() {
		this.newNameField.updateCursorCounter();
		super.updateScreen();
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawRect(this.width / 2 - 120, 0, this.width / 2 + 120,
				this.height / 16 + 25, 0xC0000000);

		if (this.parent == null) {
			this.drawCenteredString(this.fontRendererObj,
					I18n.format("wdl.gui.multiworld.title.startDownload"),
					this.width / 2, this.height / 16, 0xFFFFFF);
		} else {
			this.drawCenteredString(this.fontRendererObj,
					I18n.format("wdl.gui.multiworld.title.changeOptions"),
					this.width / 2, this.height / 16, 0xFFFFFF);
		}

		this.drawCenteredString(this.fontRendererObj,
				I18n.format("wdl.gui.multiworld.subtitle"),
				this.width / 2, this.height / 16 + 10, 0xFF0000);
		
		if (this.newWorld) {
			this.newNameField.drawTextBox();
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void worldSelected(String worldName) {
		WDL.worldName = worldName;
		WDL.isMultiworld = true;
		WDL.propsFound = true;

		if (this.parent == null) {
			WDL.start();
			this.mc.displayGuiScreen((GuiScreen) null);
			this.mc.setIngameFocus();
		} else {
			WDL.worldProps = WDL.loadWorldProps(worldName);
			this.mc.displayGuiScreen(new GuiWDL(this.parent));
		}
	}

	/**
	 * Creates a new multiworld.
	 */
	private String addMultiworld(String worldName) {
		String fileSafeWorldname = worldName;
		char[] unsafeChars = "\\/:*?\"<>|".toCharArray();
		
		//TODO: ReplaceAll with a regular expression may be cleaner
		for (char unsafeChar : unsafeChars) {
			fileSafeWorldname = fileSafeWorldname.replace(unsafeChar, '_');
		}

		(new File(this.mc.mcDataDir, "saves/" + WDL.baseFolderName + " - "
				+ fileSafeWorldname)).mkdirs();
		Properties worldProps = new Properties(WDL.baseProps);
		worldProps.setProperty("WorldName", worldName);
		
		//TODO: Stop manually redoing the array here
		String[] newWorlds = new String[this.worlds.length + 1];

		for (int i = 0; i < this.worlds.length; ++i) {
			newWorlds[i] = this.worlds[i];
		}

		newWorlds[newWorlds.length - 1] = fileSafeWorldname;
		
		//TODO: StringBuilder
		String linkedWorldsProp = "";
		for (int i = 0; i < newWorlds.length; i++) {
			linkedWorldsProp += newWorlds[i] + "|";
		}

		WDL.baseProps.setProperty("LinkedWorlds", linkedWorldsProp);
		WDL.saveProps(fileSafeWorldname, worldProps);
		
		return fileSafeWorldname;
	}
}
