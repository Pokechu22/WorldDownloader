package wdl.gui;

import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import wdl.WDL;
import wdl.WDLMessages;
import wdl.WDLPluginChannels;
import wdl.WorldBackup.WorldBackupType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;

/**
 * GUI that shows the current permissions for the user.
 */
public class GuiWDLPermissions extends GuiScreen {
	/**
	 * Margins for the top and the bottom of the list.
	 */
	private static final int TOP_MARGIN = 39, BOTTOM_MARGIN = 32;
	
	/**
	 * Whether the UI is in the request mode.  True: requesting new
	 * permissions; false, displaying current permissions.
	 */
	private boolean requestMode;
	
	/**
	 * Request mode button.
	 */
	private GuiButton requestButton;
	/**
	 * Reload permissions button
	 */
	private GuiButton reloadButton;
	
	/**
	 * List of permissions.
	 */
	private class PermissionsList extends GuiListExtended {
		public PermissionsList() {
			super(GuiWDLPermissions.this.mc, GuiWDLPermissions.this.width,
					GuiWDLPermissions.this.height, TOP_MARGIN,
					GuiWDLPermissions.this.height - BOTTOM_MARGIN, 
					fontRendererObj.FONT_HEIGHT * 2 + 2);
		}
		
		/**
		 * IGuiListEntry that displays or requests a permission.
		 */
		private class PermissionEntry implements IGuiListEntry {
			private final String line1;
			private final String line2;
			
			private final PermissionEntry parent;
			
			private final GuiButton button;
			
			/**
			 * How far to indent the text.
			 */
			private final int indent;
			
			public boolean checked;
			
			/**
			 * Creates a PermissionEntry with no parent.
			 * 
			 * @param line1 Main line of description (title)
			 * @param line2 Detail of permission
			 */
			public PermissionEntry(String line1, String line2) {
				this(line1, line2, null);
			}
			
			/**
			 * Creates a PermissionEntry.
			 * 
			 * @param line1 Main line of description (title)
			 * @param line2 Detail of permission
			 * @param parent A permission that is required for this permission.
			 * May be null.
			 */
			public PermissionEntry(String line1, String line2, 
					PermissionEntry parent) {
				this.line1 = line1;
				this.line2 = "§7" + line2;
				
				this.parent = parent;
				
				this.button = new GuiButton(0, 0, 0, 120, 20, "Request");
				button.visible = requestMode;
				
				if (parent != null) {
					this.indent = parent.indent + 5;
				} else {
					this.indent = 0;
				}
			}
			
			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				fontRendererObj.drawString(line1, x + indent, y + 1, 0xFFFFFF);
				fontRendererObj.drawString(line2, x + indent, y + 2
						+ fontRendererObj.FONT_HEIGHT, 0xFFFFFF);

				button.xPosition = GuiWDLPermissions.this.width - 130;
				button.yPosition = y;
				button.visible = requestMode;
				
				if (!areRequirementsMet()) {
					button.displayString = "Prerequisites not met";
					button.enabled = false;
				} else if (!checked) {
					button.displayString = "Not requested";
					button.enabled = true;
				} else {
					button.displayString = "Requested";
					button.enabled = true;
				}
				
				button.drawButton(mc, mouseX, mouseY);
			}

			@Override
			public boolean mousePressed(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				if (button.mousePressed(mc, x, y)) {
					button.playPressSound(mc.getSoundHandler());
					
					checked ^= true;
					
					return true;
				}
				return false;
			}

			@Override
			public void mouseReleased(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				button.mouseReleased(x, y);
			}

			@Override
			public void setSelected(int slotIndex, int p_178011_2_,
					int p_178011_3_) {
				
			}
			
			/**
			 * Are the requirements for this permission met?
			 */
			public boolean areRequirementsMet() {
				return parent == null ||
						parent.areRequirementsMet() && parent.checked;
			}
		}
		
		/**
		 * Permission entry that is only visible when requesting permissions.
		 */
		private class RequestOnlyPermissionEntry extends PermissionEntry {
			/**
			 * Creates a RequestOnlyPermissionEntry with no parent.
			 * 
			 * @param line1 Main line of description (title)
			 * @param line2 Detail of permission
			 */
			public RequestOnlyPermissionEntry(String line1, String line2) {
				super(line1, line2);
			}
			
			/**
			 * Creates a RequestOnlyPermissionEntry.
			 * 
			 * @param line1 Main line of description (title)
			 * @param line2 Detail of permission
			 * @param parent A permission that is required for this permission.
			 * May be null.
			 */
			public RequestOnlyPermissionEntry(String line1, String line2, 
					PermissionEntry parent) {
				super(line1, line2, parent);
			}
			
			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				if (requestMode) {
					super.drawEntry(slotIndex, x, y, listWidth, slotHeight,
							mouseX, mouseY, isSelected);
				}
			}
			
			@Override
			public boolean mousePressed(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				if (requestMode) {
					return super.mousePressed(slotIndex, x, y, mouseEvent,
							relativeX, relativeY);
				} else {
					return false;
				}
			}
		}
		
		/**
		 * IGuiListEntry for the download radius option.
		 * 
		 * TODO: Shared base class with PermissionEntry.
		 */
		private class DownloadRadiusEntry implements IGuiListEntry {
			private final String line1;
			private final String line2;
			
			private final PermissionEntry parent;
			
			private final GuiButton slider;
			
			private final int indent;
			
			/**
			 * Creates a DownloadRadiusEntry.
			 * 
			 * @param line1 Main line of description (title)
			 * @param line2 Detail of permission
			 * @param parent A permission that is required for this permission.
			 * May be null.
			 */
			public DownloadRadiusEntry(String line1, String line2, 
					PermissionEntry parent) {
				this.line1 = line1;
				this.line2 = "§7" + line2;
				
				this.parent = parent;
				
				this.slider = new GuiSlider(0, 0, 0, 120, 20, "Save radius", 0, 32);
				slider.visible = requestMode;
				
				if (parent != null) {
					this.indent = parent.indent + 5;
				} else {
					this.indent = 0;
				}
			}
			
			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				fontRendererObj.drawString(line1, x + indent, y + 1, 0xFFFFFF);
				fontRendererObj.drawString(line2, x + indent, y + 2
						+ fontRendererObj.FONT_HEIGHT, 0xFFFFFF);

				slider.xPosition = GuiWDLPermissions.this.width - 130;
				slider.yPosition = y;
				slider.visible = requestMode;
				
				if (!areRequirementsMet()) {
					slider.displayString = "Prerequisites not met";
					slider.enabled = false;
				} else {
					slider.displayString = "Requested range";
					slider.enabled = true;
				}
				
				slider.drawButton(mc, mouseX, mouseY);
			}

			@Override
			public boolean mousePressed(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				if (slider.mousePressed(mc, x, y)) {
					slider.playPressSound(mc.getSoundHandler());
				}
				return false;
			}

			@Override
			public void mouseReleased(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				slider.mouseReleased(x, y);
				
				//TODO update value.
			}

			@Override
			public void setSelected(int slotIndex, int p_178011_2_,
					int p_178011_3_) {
				
			}
			
			/**
			 * Are the requirements for this permission met?
			 */
			public boolean areRequirementsMet() {
				return parent != null ||
						!parent.areRequirementsMet() || !parent.checked;
			}
		}
		
		private List<IGuiListEntry> entries = new ArrayList<IGuiListEntry>() {{
			if (WDLPluginChannels.hasPermissions()) {
				PermissionEntry canDownloadInGeneral = new PermissionEntry(
						"Can download: "
								+ WDLPluginChannels.canDownloadInGeneral(),
						"Controls whether you are able to download");
				PermissionEntry canCacheChunks = new PermissionEntry(
						"Can cache chunks: "
								+ WDLPluginChannels.canCacheChunks(),
						"Controls whether chunks are saved as you move about "
								+ "the world", 
						canDownloadInGeneral);
				DownloadRadiusEntry saveRadius = new DownloadRadiusEntry(
						"Download radius: "
								+ WDLPluginChannels.getSaveRadius(),
						"Radius for downloading chunks (only when caching disabled)",
						canCacheChunks);
				PermissionEntry canSaveEntities = new PermissionEntry(
						"Can save entities: "
								+ WDLPluginChannels.canSaveEntities(),
						"Controls whether you can save entities",
						canDownloadInGeneral);
				PermissionEntry canSaveTileEntities = new PermissionEntry(
						"Can save tile entities: "
								+ WDLPluginChannels.canSaveTileEntities(),
						"Controls whether you can save tile entities",
						canDownloadInGeneral);
				PermissionEntry canSaveContainers = new PermissionEntry(
						"Can save containers: "
								+ WDLPluginChannels.canSaveContainers(),
						"Controls whether you can save containers",
						canSaveTileEntities);
				PermissionEntry canDoUnknownThings = new PermissionEntry(
						"Can use functions unknown to the server: "
								+ WDLPluginChannels
										.canUseFunctionsUnknownToServer(),
						"Controls whether you can use newer functions of WDL.",
						canDownloadInGeneral);
				//TODO: Entity ranges.
				PermissionEntry allWorlds = new RequestOnlyPermissionEntry(
						"Single world: TODO",
						"Controls whether the options effect all worlds or " +
						"just the current one");
				
				add(canDownloadInGeneral);
				add(canCacheChunks);
				add(saveRadius);
				add(canSaveEntities);
				add(canSaveTileEntities);
				add(canSaveContainers);
				add(canDoUnknownThings);
				add(allWorlds);
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
		
		@Override
		protected int getScrollBarX() {
			return GuiWDLPermissions.this.width - 10;
		}
		
		@Override
		public int getListWidth() {
			return GuiWDLPermissions.this.width - 18;
		}
	}
	
	private final GuiScreen parent;
	
	private PermissionsList list;
	
	/**
	 * Creates a new GUI with the given parent.
	 * 
	 * @param parent
	 */
	public GuiWDLPermissions(GuiScreen parent) {
		this.parent = parent;
	}
	
	/**
	 * Creates a new GUI.
	 * 
	 * Attempts to infer the parent based off of the currently open GUI.
	 */
	public GuiWDLPermissions() {
		GuiScreen openGui = WDL.minecraft.currentScreen;
		
		if (openGui instanceof GuiWDLPermissions) {
			this.parent = ((GuiWDLPermissions) openGui).parent;
		} else {
			//openGui may be null, but that's not an issue, as that
			//would just close the GUI.
			this.parent = openGui;
		}
	}
	
	@Override
	public void initGui() {
		this.buttonList.add(new GuiButton(100, width / 2 - 100, height - 29,
				"Done"));
		
		requestButton = new GuiButton(0, (this.width / 2) - 155, 18, 150, 20,
				requestMode ? "Cancel request" : "Switch to request mode");
		this.buttonList.add(requestButton);
		reloadButton = new GuiButton(1, (this.width / 2) + 5, 18, 150, 20,
				"Reload permissions");
		this.buttonList.add(reloadButton);
		
		// Plugin not installed? No point in requesting.
		this.requestButton.enabled = WDLPluginChannels.hasPermissions();
		
		this.list = new PermissionsList();
	}
	
	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
	throws IOException {
		list.func_148179_a(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.list.func_178039_p();
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if (list.func_148181_b(mouseX, mouseY, state)) {
			return;
		}
		super.mouseReleased(mouseX, mouseY, state);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			requestMode ^= true;
			requestButton.displayString = requestMode ? "Cancel request"
					: "Switch to request mode";
		}
		if (button.id == 1) {
			// Send the init packet.
			C17PacketCustomPayload initPacket;
			try {
				initPacket = new C17PacketCustomPayload("WDL|INIT",
						new PacketBuffer(Unpooled.copiedBuffer(WDL.VERSION
								.getBytes("UTF-8"))));
			} catch (UnsupportedEncodingException e) {
				WDL.chatError("Your computer doesn't support the UTF-8 charset."
						+ "You should feel bad.  " + (e.toString()));
				e.printStackTrace();

				initPacket = new C17PacketCustomPayload("WDL|INIT",
						new PacketBuffer(Unpooled.buffer()));
			}
			WDL.minecraft.getNetHandler().addToSendQueue(initPacket);
			
			button.enabled = false;
			button.displayString = "Refershing...";
		}
		if (button.id == 100) {
			this.mc.displayGuiScreen(this.parent);
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.list.drawScreen(mouseX, mouseY, partialTicks);
		
		this.drawCenteredString(this.fontRendererObj, "Permission info",
				this.width / 2, 8, 0xFFFFFF);
		
		if (!WDLPluginChannels.hasPermissions()) {
			this.drawCenteredString(this.fontRendererObj,
					"No permissions sent; defaulting to everything enabled.",
					this.width / 2, (this.height - 32 - 23) / 2 + 23
							- fontRendererObj.FONT_HEIGHT / 2, 0xFFFFFF);
		}
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
