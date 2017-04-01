package wdl.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import org.lwjgl.input.Keyboard;

import wdl.WDL;
import wdl.gui.GuiWDLMultiworldSelect.*;

/**
 * A GUI for selecting which world the player is currently in.
 * 
 * TODO: I might want to move the multiworld setup logic elsewhere.
 */
public class GuiWDLMultiworldSelectCubic extends GuiTurningCameraBase {
	private static final FontRenderer SANE_FONT_RENDERER = Minecraft.getMinecraft().fontRenderer;

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
			
			if (info != null && info == selectedMultiWorld) {
				drawRect(this.xPosition - 2, this.yPosition - 2,
						this.xPosition + width + 2, this.yPosition + height + 2,
						0xFF007F00);
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
			if (location >= linkedWorldsFiltered.size()) {
				return null;
			}
			
			return linkedWorldsFiltered.get(location);
		}
	}
	
	/**
	 * The callback for when the done button is clicked.
	 */
	private final WorldSelectionCallback callback;
	/**
	 * Title of the GUI.
	 */
	private final String title;
	/**
	 * The cancel button.
	 */
	private GuiButton cancelBtn;
	/**
	 * The accept button.
	 */
	private GuiButton acceptBtn;
	/**
	 * Worlds linked together for the given server.
	 */
	private List<MultiworldInfo> linkedWorlds;
	/**
	 * List of worlds to display.
	 */
	private List<MultiworldInfo> linkedWorldsFiltered;
	/**
	 * The currently selected multiworld.
	 */
	private MultiworldInfo selectedMultiWorld;
	/**
	 * Scrolling index.
	 */
	private int index = 0;
	/**
	 * The number of world buttons there are.
	 */
	private final int numWorldButtons = 3;
	/**
	 * Text currently being searched for.
	 */
	private String searchText = "";

	public GuiWDLMultiworldSelectCubic(String title, WorldSelectionCallback callback) {
		this.title = title;
		this.callback = callback;
		
		// Build a list of world names.
		String[] worldNames = WDL.baseProps.getProperty("LinkedWorlds")
				.split("\\|");
		linkedWorlds = new ArrayList<MultiworldInfo>();
		
		for (String worldName : worldNames) {
			if (worldName == null || worldName.isEmpty()) {
				continue;
			}
			
			Properties props = WDL.loadWorldProps(worldName);

			if (!props.containsKey("WorldName")) {
				continue;
			}
			
			String displayName = props.getProperty("WorldName", worldName);
			linkedWorlds.add(new MultiworldInfo(worldName, displayName));
		}
		
		linkedWorldsFiltered = new ArrayList<MultiworldInfo>();
		linkedWorldsFiltered.addAll(linkedWorlds);
	}

	private abstract class Face {
		private int tick = 0;
		private static final int SIZE = 250;
		private int x0;
		private int color;
		protected List<GuiButton> buttons = new ArrayList<GuiButton>();
		protected List<GuiTextField> fields = new ArrayList<GuiTextField>();
		public Face(int x0, int color) {
			this.x0 = x0;
			this.color = color | 0xFF000000;
		}
		public void update() {
			tick++;
			for (GuiTextField field : fields) {
				field.updateCursorCounter();
			}
		}
		public boolean keyTyped(char c, int codePoint) {
			for (GuiTextField field : fields) {
				if (field.textboxKeyTyped(c, codePoint)) {
					return true;
				}
			}
			return false;
		}
		private float rx(float partial) {
			return ((x0 + 3 * (tick + partial)) % 360) - 180;
		}
		private int normX(int x, int y, float partial) {
			// This method doesn't make sense, but doesn't have to
			float rx = rx(partial);
			x -= width/2;  // center
			double xOffset = ((SIZE/2) * Math.sin(Math.toRadians(rx)));
			double scale = (Math.cos(Math.toRadians(rx)));
			return (int)((x - xOffset) / scale);
		}
		private int normY(int x, int y, float partial) {
			return y - height/2;
		}
		public void click(int x, int y, int button) {
			if (button != 0) return;
			int nx = normX(x, y, 0);
			int ny = normY(x, y, 0);
			for (GuiButton btn : buttons) {
				if (btn.mousePressed(mc, nx, ny)) {
					btn.playPressSound(mc.getSoundHandler());
					action(btn);
				}
			}
			for (GuiTextField field : fields) {
				field.mouseClicked(nx, ny, button);
			}
		}
		public void draw(int mouseX, int mouseY, float partial) {
			float rx = rx(partial);
			if (Math.abs(rx) >= 90) {
				// Don't draw invisible faces
				return;
			}
			GlStateManager.pushMatrix();
			GlStateManager.pushAttrib();
			int nx = normX(mouseX, mouseY, partial);
			int ny = normY(mouseX, mouseY, partial);
			GlStateManager.translate(width / 2, height / 2, 0);
			GlStateManager.rotate(rx(partial), 0, 1, 0);
			GlStateManager.translate(0, 0, SIZE/2);
			drawHorizontalLine(-SIZE/2, SIZE/2, -SIZE/2, color &~0x808080);
			drawHorizontalLine(-SIZE/2, SIZE/2, SIZE/2, color &~0x808080);
			drawVerticalLine(-SIZE/2, -SIZE/2, SIZE/2, color &~0x808080);
			drawVerticalLine(SIZE/2, -SIZE/2, SIZE/2, color &~0x808080);
			drawRect(-SIZE/2 + 1, -SIZE/2 + 1, SIZE/2, SIZE/2, color);
			GlStateManager.translate(0, 0, 1);  // z-ordering, no fighting
			for (GuiButton btn : buttons) {
				btn.drawButton(mc, nx, ny);
			}
			for (GuiTextField field : fields) {
				field.drawTextBox();
			}
			paintInner();
			GlStateManager.popAttrib();
			GlStateManager.popMatrix();
		}
		protected abstract void action(GuiButton btn);
		protected void paintInner() {}
	}

	private Face[] faces;

	@Override
	public void initGui() {
		super.initGui();
		
		faces = new Face[4];
		faces[0] = new Face(0, 0x00FF00) {
			private GuiButton newBtn;
			private GuiTextField field;
			{
				buttons.add(newBtn = new GuiButton(0, -100, -80, I18n.format("wdl.gui.multiworldSelect.newName")));
				fields.add(field = new GuiTextField(1, fontRenderer, -100, -10, 200, 20));
				field.setVisible(false);
			}
			@Override
			protected void action(GuiButton btn) {
				if (btn.id == 0) {
					btn.visible = false;
					field.setVisible(true);
				}
			}
			@Override
			public boolean keyTyped(char c, int codePoint) {
				boolean val = super.keyTyped(c, codePoint);
				if (codePoint == Keyboard.KEY_RETURN) {
					String newName = this.field.getText();

					if (newName != null && !newName.isEmpty()) {
						//TODO: Ensure that the new world is in view.
						addMultiworld(newName);
						field.setText("");
						field.setVisible(false);
						newBtn.visible = true;
					}
				}
				return val;
			}
		};
		faces[1] = new Face(90, 0xFF0000) {
			/**
			 * The next button (scrolls the list right).
			 */
			private GuiButton nextButton;
			/**
			 * The previous button (scrolls the list left).
			 */
			private GuiButton prevButton;

			{
				prevButton = new GuiButton(-4, -100, -50, 200, 20, "^ UP ^");
				this.buttons.add(prevButton);
				
				for (int i = 0; i < numWorldButtons; i++) {
					this.buttons.add(new WorldGuiButton(i, -100, -25 + 25 * i, 200, 20));
				}
				
				nextButton = new GuiButton(-5, -100, 50, 200, 20, "v DOWN v");
				this.buttons.add(nextButton);
			}
			@Override
			public void update() {
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
				super.update();
			}
			@Override
			protected void action(GuiButton button) {
				if (button instanceof WorldGuiButton) {
					selectedMultiWorld = ((WorldGuiButton) button).getWorldInfo();
					if (selectedMultiWorld != null) {
						acceptBtn.enabled = true;
					} else {
						acceptBtn.enabled = false;
					}

				} else if (button.id == -4) {
					index--;
				} else if (button.id == -5) {
					index++;
				}
			}
		};
		faces[2] = new Face(180, 0x0000FF) {
			private GuiTextField searchField;
			{
				this.fields.add(searchField = new GuiTextField(41, fontRenderer,
						-75, 80, 150, 20));
				this.searchField.setText(searchText);
			}
			@Override
			protected void action(GuiButton btn) {
				
			}
			@Override
			public boolean keyTyped(char c, int codePoint) {
				boolean val = super.keyTyped(c, codePoint);
				if (val) {
					searchText = searchField.getText();
					rebuildFilteredWorlds();
				}
				return val;
			}
			@Override
			protected void paintInner() {
				//Hint as to what the text box does
				if (this.searchField.getText().isEmpty() && !this.searchField.isFocused()) {
					drawString(fontRenderer,
							I18n.format("wdl.gui.multiworldSelect.search"),
							searchField.xPosition + 4, searchField.yPosition + 6,
							0x909090);
				}
			}
		};
		faces[3] = new Face(270, 0xFFFFFF) {
			{
				
			}
			@Override
			protected void action(GuiButton btn) {
				
			}
			@Override
			protected void paintInner() {
				if (selectedMultiWorld == null) {
					return;
				}
				String title = "Info about "
						+ selectedMultiWorld.displayName;
				List<String> description = selectedMultiWorld.getDescription();
				
				drawString(fontRenderer, title, -100, -100, 0x808080);
				
				int y = -100 + fontRenderer.FONT_HEIGHT;
				for (String s : description) {
					drawString(fontRenderer, s, -100, y, 0x808080);
					y += fontRenderer.FONT_HEIGHT;
				}
			}
		};
		
		this.cancelBtn = new GuiButton(-1, this.width / 2 - 155, this.height - 25,
				150, 20, I18n.format("gui.cancel"));
		this.buttonList.add(this.cancelBtn);
		
		this.buttonList.add(new GuiButton(-101, this.width / 2 - 155, 29, 150, 20, "AAAAAAAAAA"));
		this.buttonList.add(new GuiButton(-102, this.width / 2 + 5, 29, 150, 20, "I NO CAN COMPUTER"));
		
		this.acceptBtn = new GuiButton(-2, this.width / 2 + 5, this.height - 25,
				150, 20, I18n.format("wdl.gui.multiworldSelect.done"));
		this.acceptBtn.enabled = (selectedMultiWorld != null);
		this.buttonList.add(this.acceptBtn);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id == -1) {
				callback.onCancel();
			} else if (button.id == -2) {
				callback.onWorldSelected(selectedMultiWorld.folderName);
			} else if (button.id == -101) {
				// Escape
				this.mc.displayGuiScreen(new GuiWDLMultiworldSelect(this.title, this.callback));
			} else if (button.id == -102) {
				if (this.fontRenderer == SANE_FONT_RENDERER) {
					this.fontRenderer = mc.standardGalacticFontRenderer;
					mc.fontRenderer = mc.standardGalacticFontRenderer;
				} else {
					this.fontRenderer = SANE_FONT_RENDERER;
					mc.fontRenderer = SANE_FONT_RENDERER;
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

		for (Face face : faces) {
			if (face != null) {
				face.click(mouseX, mouseY, mouseButton);
			}
		}
	}

	/**
	 * Fired when a key is typed. This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e).
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			callback.onCancel();
		}
		
		super.keyTyped(typedChar, keyCode);

		for (Face face : faces) {
			if (face != null) {
				face.keyTyped(typedChar, keyCode);
			}
		}
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	@Override
	public void updateScreen() {
		for (Face face : faces) {
			if (face != null) {
				face.update();
			}
		}
		super.updateScreen();
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		/**/
		
		Utils.drawBorder(53, 53, 0, 0, height, width);
		
		this.drawCenteredString(this.fontRenderer, title, this.width / 2, 8,
				0xFFFFFF);

		this.drawCenteredString(this.fontRenderer,
				I18n.format("wdl.gui.multiworldSelect.subtitle"),
				this.width / 2, 18, 0xFF0000);
		
		for (Face face : faces) {
			if (face != null) {
				face.draw(mouseX, mouseY, partialTicks);
			}
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
		
		if (!GuiWDLMultiworldSelect.is()) {
			GlStateManager.pushMatrix();
			GlStateManager.pushAttrib();
			GlStateManager.disableDepth();
			GlStateManager.translate(width / 2, height / 2, 200);
			GlStateManager.rotate(-25, 0, 0, 1);
			GlStateManager.scale(10, 10, 0);
			drawCenteredString(fontRenderer, "April Fools!", 0, 0, 0xff55ff);
			GlStateManager.popAttrib();
			GlStateManager.popMatrix();
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

		Properties worldProps = new Properties(WDL.baseProps);
		worldProps.setProperty("WorldName", worldName);
		
		String linkedWorldsProp = WDL.baseProps.getProperty("LinkedWorlds");
		linkedWorldsProp += "|" + folderName;

		WDL.baseProps.setProperty("LinkedWorlds", linkedWorldsProp);
		WDL.saveProps(folderName, worldProps);
		
		linkedWorlds.add(new MultiworldInfo(folderName, worldName));
		
		rebuildFilteredWorlds();
	}
	
	/**
	 * Rebuilds the {@link #linkedWorldsFiltered} list after a change
	 * has occurred to the search or the {@link #linkedWorlds} list.
	 */
	private void rebuildFilteredWorlds() {
		String searchFilter = searchText.toLowerCase();
		linkedWorldsFiltered.clear();
		for (MultiworldInfo info : linkedWorlds) {
			if (info.displayName.toLowerCase().contains(searchFilter)) {
				linkedWorldsFiltered.add(info);
			}
		}
		
	}

	@Override
	public void onGuiClosed() {
		mc.fontRenderer = SANE_FONT_RENDERER;
		super.onGuiClosed();
	}
}
