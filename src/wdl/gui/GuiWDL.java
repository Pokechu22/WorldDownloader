package wdl.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import wdl.WDL;
import wdl.WDLPluginChannels;
import wdl.update.WDLUpdateChecker;

public class GuiWDL extends GuiScreen {
	/**
	 * Tooltip to display on the given frame.
	 */
	private String displayedTooltip = null;
	
	private class GuiWDLButtonList extends GuiListExtended {
		public GuiWDLButtonList() {
			super(GuiWDL.this.mc, GuiWDL.this.width, GuiWDL.this.height, 39,
					GuiWDL.this.height - 32, 20);
		}

		private class ButtonEntry implements IGuiListEntry {
			private final GuiButton button;
			private final GuiScreen toOpen;
			
			private final String tooltip;
			
			/**
			 * Constructor.
			 * 
			 * @param key
			 *            The I18n key, which will have the base for this GUI
			 *            prepended.
			 * @param toOpen
			 *            The gui screen to open when the button is clicked.
			 * @param needsPerms
			 *            Whether the player needs download permission to use
			 *            this button.
			 */
			public ButtonEntry(String key, GuiScreen toOpen, boolean needsPerms) {
				this.button = new GuiButton(0, 0, 0, I18n.format("wdl.gui.wdl."
						+ key + ".name"));
				this.toOpen = toOpen;
				if (needsPerms) {
					button.enabled = WDLPluginChannels.canDownloadAtAll();
				}
				
				this.tooltip = I18n.format("wdl.gui.wdl." + key + ".description");
			}
			
			@Override
			public void setSelected(int p_178011_1_, int p_178011_2_,
					int p_178011_3_) {
				
			}

			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				button.xPosition = GuiWDL.this.width / 2 - 100;
				button.yPosition = y;
				
				button.drawButton(mc, mouseX, mouseY);
				
				if (button.isMouseOver()) {
					displayedTooltip = tooltip;
				}
			}

			@Override
			public boolean mousePressed(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				if (button.mousePressed(mc, x, y)) {
					mc.displayGuiScreen(toOpen);
					
					button.playPressSound(mc.getSoundHandler());
					
					return true;
				}
				
				return false;
			}

			@Override
			public void mouseReleased(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				
			}
		}
		
		private List<IGuiListEntry> entries = new ArrayList<IGuiListEntry>() {{
			// TODO: This might be a performance bottleneck, as a bunch of
			// GUI instances are created.  Although they aren't displayed.
			
			add(new ButtonEntry("worldOverrides", new GuiWDLWorld(
					GuiWDL.this), true));
			add(new ButtonEntry("generatorOverrides", new GuiWDLGenerator(
					GuiWDL.this), true));
			add(new ButtonEntry("playerOverrides", new GuiWDLPlayer(
					GuiWDL.this), true));
			//add(new ButtonEntry("entityOptions", new GuiWDLEntities(
			//		GuiWDL.this), true));
			add(new ButtonEntry("backupOptions", new GuiWDLBackup(
					GuiWDL.this), true));
			add(new ButtonEntry("messageOptions", new GuiWDLMessages(
					GuiWDL.this), false));
			add(new ButtonEntry("permissionsInfo", new GuiWDLPermissions(
					GuiWDL.this), false));
			add(new ButtonEntry("about", new GuiWDLAbout(GuiWDL.this), false));
			if (WDLUpdateChecker.hasNewVersion()) {
				add(0, new ButtonEntry("updates.hasNew", new GuiWDLUpdates(
						GuiWDL.this), false));
			} else {
				add(new ButtonEntry("updates", new GuiWDLUpdates(
						GuiWDL.this), false));
			}
		}};
		
		@Override
		public IGuiListEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}
	}
	
	private String title = "";

	private GuiScreen parent;

	private GuiTextField worldname;
	private GuiWDLButtonList list;

	public GuiWDL(GuiScreen parent) {
		this.parent = parent;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui() {
		if (WDL.isMultiworld && WDL.worldName.isEmpty()) {
			this.mc.displayGuiScreen(new GuiWDLMultiworldSelect(I18n
					.format("wdl.gui.multiworldSelect.title.changeOptions"),
					new GuiWDLMultiworldSelect.WorldSelectionCallback() {
						@Override
						public void onWorldSelected(String selectedWorld) {
							WDL.worldName = selectedWorld;
							WDL.isMultiworld = true;
							WDL.propsFound = true;
							
							WDL.worldProps = WDL.loadWorldProps(selectedWorld);
							mc.displayGuiScreen(GuiWDL.this);
						}
						
						@Override
						public void onCancel() {
							mc.displayGuiScreen(null);
						}
					}));
			return;
		}

		if (!WDL.propsFound) {
			mc.displayGuiScreen(new GuiWDLMultiworld(new GuiWDLMultiworld.MultiworldCallback() {
				@Override
				public void onSelect(boolean enableMutliworld) {
					WDL.isMultiworld = enableMutliworld;
					
					if (WDL.isMultiworld) {
						// Ask the user which world is loaded
						// TODO: Copy-pasted code from above -- suboptimal.
						mc.displayGuiScreen(new GuiWDLMultiworldSelect(I18n
								.format("wdl.gui.multiworldSelect.title.changeOptions"),
								new GuiWDLMultiworldSelect.WorldSelectionCallback() {
									@Override
									public void onWorldSelected(String selectedWorld) {
										WDL.worldName = selectedWorld;
										WDL.isMultiworld = true;
										WDL.propsFound = true;
										
										WDL.worldProps = WDL.loadWorldProps(selectedWorld);
										mc.displayGuiScreen(GuiWDL.this);
									}
									
									@Override
									public void onCancel() {
										mc.displayGuiScreen(null);
									}
								}));
					} else {
						WDL.baseProps.setProperty("LinkedWorlds", "");
						WDL.saveProps();
						WDL.propsFound = true;

						mc.displayGuiScreen(GuiWDL.this);
					}
				}
				
				@Override
				public void onCancel() {
					mc.displayGuiScreen(null);
				}
			}));
			return;
		}

		this.buttonList.clear();
		this.title = I18n.format("wdl.gui.wdl.title",
				WDL.baseFolderName.replace('@', ':'));

		if (WDL.baseProps.getProperty("ServerName").isEmpty()) {
			WDL.baseProps.setProperty("ServerName", WDL.getServerName());
		}

		this.worldname = new GuiTextField(42, this.fontRendererObj,
				this.width / 2 - 155, 19, 150, 18);
		this.worldname.setText(WDL.baseProps.getProperty("ServerName"));

		this.buttonList.add(new GuiButton(100, this.width / 2 - 100,
				this.height - 29, I18n.format("gui.done")));
		
		this.list = new GuiWDLButtonList();
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton button) {
		if (!button.enabled) {
			return;
		}

		if (button.id == 100) { // Done
			this.mc.displayGuiScreen(this.parent);
		}
	}
	
	@Override
	public void onGuiClosed() {
		if (this.worldname != null) {
			WDL.baseProps.setProperty("ServerName", this.worldname.getText());
		
			WDL.saveProps();
		}
	}

	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
	throws IOException {
		list.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.worldname.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.list.handleMouseInput();
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if (list.mouseReleased(mouseX, mouseY, state)) {
			return;
		}
		super.mouseReleased(mouseX, mouseY, state);
	}

	/**
	 * Fired when a key is typed. This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e).
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		this.worldname.textboxKeyTyped(typedChar, keyCode);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	@Override
	public void updateScreen() {
		this.worldname.updateCursorCounter(); // updateCursorCounter
		super.updateScreen();
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		
		displayedTooltip = null;
		
		this.list.drawScreen(mouseX, mouseY, partialTicks);
		
		this.drawCenteredString(this.fontRendererObj, this.title,
				this.width / 2, 8, 0xFFFFFF);
		String name = I18n.format("wdl.gui.wdl.worldname");
		this.drawString(this.fontRendererObj, name, this.worldname.xPosition
				- this.fontRendererObj.getStringWidth(name + " "), 26, 0xFFFFFF);
		this.worldname.drawTextBox();
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		Utils.drawGuiInfoBox(displayedTooltip, width, height, 48);
	}
}
