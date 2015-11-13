package wdl.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

import org.lwjgl.input.Keyboard;

import wdl.WDL;

/**
 * A GUI for selecting which world the player is currently in.
 * 
 * TODO: I might want to move the multiworld setup logic elsewhere.
 */
public class GuiWDLMultiworldSelect extends GuiTurningCameraBase {
	private class WorldGuiButton extends GuiButton {
		public WorldGuiButton(int offset, int x, int y, int width,
				int height) {
			super(offset, x, y, width, height, "");
		}

		public WorldGuiButton(int offset, int x, int y, String worldName,
				String displayName) {
			super(offset, x, y, "");
		}
		
		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY) {
			MultiworldInfo info = getWorldInfo();
			if (info == null) {
				displayString = "";
				enabled = false;
			} else {
				displayString = info.displayName;
				enabled = true;
			}
			super.drawButton(mc, mouseX, mouseY);
		}
		
		/**
		 * Gets the world folder name marked by this button.
		 * 
		 * @return the world info, or null if there isn't any.
		 */
		public MultiworldInfo getWorldInfo() {
			int location = index + id;
			if (location < 0) {
				return null;
			}
			if (location >= linkedWorlds.size()) {
				return null;
			}
			
			return linkedWorlds.get(location);
		}
	}
	
	/**
	 * Info for a Multiworld.
	 * 
	 * TODO: I may want to move this elsewhere.
	 */
	private static class MultiworldInfo {
		public MultiworldInfo(String folderName, String displayName) {
			this.folderName = folderName;
			this.displayName = displayName;
		}
		
		public final String folderName;
		public final String displayName;
	}
	
	/**
	 * The cancel button.
	 */
	private GuiButton cancelBtn;
	/**
	 * The "New name" field.
	 */
	private GuiTextField newNameField;
	/**
	 * The "New world" button.
	 */
	private GuiButton newWorldButton;
	/**
	 * Should the {@link #newNameField} be shown?
	 */
	private boolean showNewWorldTextBox;
	/**
	 * Worlds linked together for the given server.
	 */
	private List<MultiworldInfo> linkedWorlds;
	/**
	 * Scrolling index.
	 */
	private int index = 0;
	/**
	 * The parent GUI screen.
	 */
	private GuiScreen parent;
	private GuiButton nextButton;
	private GuiButton prevButton;
	private int numWorldButtons;

	public GuiWDLMultiworldSelect(GuiScreen parent) {
		this.parent = parent;
		
		// Build a list of world names.
		String[] worldNames = WDL.baseProps.getProperty("LinkedWorlds")
				.split("\\|");
		linkedWorlds = new ArrayList<MultiworldInfo>();
		
		for (String worldName : worldNames) {
			if (worldName == null || worldName.isEmpty()) {
				continue;
			}
			
			Properties props = WDL.loadWorldProps(worldName);

			if (props == null) {
				continue;
			}
			
			String displayName = props.getProperty("WorldName", worldName);
			linkedWorlds.add(new MultiworldInfo(worldName, displayName));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		
		int numButtons = (this.width - 50) / 155;

		if (numButtons < 2) {
			numButtons = 2;
		}
		
		numWorldButtons = numButtons - 1;
		
		int offset = (numButtons * 155 + 50) / 2;
		int y = this.height - 60;
		
		this.cancelBtn = new GuiButton(-1, this.width / 2 - 100, this.height - 30,
				I18n.format("gui.cancel"));
		this.buttonList.add(this.cancelBtn);
		
		prevButton = new GuiButton(-4, this.width / 2 - offset, y, 20, 20, "<");
		this.buttonList.add(prevButton);
		
		for (int i = 0; i < numWorldButtons; i++) {
			this.buttonList.add(new WorldGuiButton(i, this.width / 2 - offset
					+ i * 155 + 25, y, 150, 20));
		}
		
		int rightArrowX = this.width / 2 - offset + 25 + numWorldButtons * 155;
		
		nextButton = new GuiButton(-5, rightArrowX, y, 20, 20, ">");
		this.buttonList.add(nextButton);
		
		this.newWorldButton = new GuiButton(-3, rightArrowX + 25, y, 150, 20,
				I18n.format("wdl.gui.multiworld.newName"));
		this.buttonList.add(newWorldButton);

		this.newNameField = new GuiTextField(40, this.fontRendererObj,
				rightArrowX + 25, y, 150, 20);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button instanceof WorldGuiButton) {
				//TODO: I want to outline the selected world, not return it.
				this.worldSelected(((WorldGuiButton) button).getWorldInfo().folderName);
			} else if (button.id == -1) {
				this.mc.displayGuiScreen((GuiScreen) null);
				this.mc.setIngameFocus();
			} else if (button.id == -2) {
				//TODO: Accept button.
			} else if (button.id == -3) {
				this.showNewWorldTextBox = true;
			} else if (button.id == -4) {
				index--;
			} else if (button.id == -5) {
				index++;
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

		if (this.showNewWorldTextBox) {
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

		if (this.showNewWorldTextBox && this.newNameField.isFocused()) {
			this.newNameField.textboxKeyTyped(typedChar, keyCode);

			if (keyCode == Keyboard.KEY_RETURN) {
				String newName = this.newNameField.getText();

				if (newName != null && !newName.isEmpty()) {
					//TODO: Ensure that the new world is in view.
					this.addMultiworld(newName);
					this.newNameField.setText("");
					this.showNewWorldTextBox = false;
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
		if (index >= linkedWorlds.size() - numWorldButtons) {
			index = linkedWorlds.size() - numWorldButtons;
			nextButton.enabled = false;
		} else {
			nextButton.enabled = true;
		}
		if (index <= 0) {
			index = 0;
			prevButton.enabled = false;
		} else {
			prevButton.enabled = true;
		}
		
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
		
		if (this.showNewWorldTextBox) {
			this.newNameField.drawTextBox();
		}
		
		newWorldButton.visible = !showNewWorldTextBox;

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
	 * Creates a new multiworld, and adds it to the list.
	 */
	private void addMultiworld(String worldName) {
		String folderName = worldName;
		char[] unsafeChars = "\\/:*?\"<>|.".toCharArray();
		
		//TODO: ReplaceAll with a regular expression may be cleaner
		for (char unsafeChar : unsafeChars) {
			folderName = folderName.replace(unsafeChar, '_');
		}

		//TODO: Create these folders properly.  This is somewhat redundant.
		(new File(this.mc.mcDataDir, "saves/" + WDL.baseFolderName + " - "
				+ folderName)).mkdirs();
		Properties worldProps = new Properties(WDL.baseProps);
		worldProps.setProperty("WorldName", worldName);
		
		String linkedWorldsProp = WDL.baseProps.getProperty("LinkedWorlds");
		linkedWorldsProp += "|" + folderName;

		WDL.baseProps.setProperty("LinkedWorlds", linkedWorldsProp);
		WDL.saveProps(folderName, worldProps);
		
		linkedWorlds.add(new MultiworldInfo(folderName, worldName));
	}
}
