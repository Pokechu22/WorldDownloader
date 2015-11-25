package wdl.gui;

import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import wdl.WDL;
import wdl.WDLPluginChannels;

/**
 * GUI that shows the current permissions for the user.
 */
public class GuiWDLPermissions extends GuiScreen {
	/**
	 * Margins for the top and the bottom of the list.
	 */
	private static final int TOP_MARGIN = 61, BOTTOM_MARGIN = 32;
	
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
	 * Ticks (20ths of a second) until this UI needs to refresh.
	 * 
	 * If -1, don't refresh.
	 */
	private int refreshTicks = -1;
	
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
			
			//TODO: This should be an I18n key.
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
	
	private class TextEntry implements IGuiListEntry {
		private final String line1;
		private final String line2;
		
		/**
		 * Creates a TextEntry.
		 */
		public TextEntry(String line1, String line2) {
			this.line1 = line1;
			this.line2 = line2;
		}
		
		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth,
				int slotHeight, int mouseX, int mouseY, boolean isSelected) {
			fontRendererObj.drawString(line1, x, y + 1, 0xFFFFFF);
			fontRendererObj.drawString(line2, x, y + 2
					+ fontRendererObj.FONT_HEIGHT, 0xFFFFFF);
		}

		@Override
		public boolean mousePressed(int slotIndex, int x, int y,
				int mouseEvent, int relativeX, int relativeY) {
			return false;
		}

		@Override
		public void mouseReleased(int slotIndex, int x, int y,
				int mouseEvent, int relativeX, int relativeY) {
			
		}

		@Override
		public void setSelected(int slotIndex, int p_178011_2_,
				int p_178011_3_) {
			
		}
	}
	
	private PermissionEntry canDownloadInGeneral;
	private PermissionEntry canCacheChunks;
	private DownloadRadiusEntry saveRadius;
	private PermissionEntry canSaveEntities;
	private PermissionEntry canSaveTileEntities;
	private PermissionEntry canSaveContainers;
	private PermissionEntry canDoUnknownThings;
	private PermissionEntry sendEntityRanges;
	private PermissionEntry allWorlds;
	
	/**
	 * Global list of entries.
	 */
	private List<IGuiListEntry> globalEntries;
	
	/**
	 * Recalculates the {@link #globalEntries} list.
	 */
	private void calculateEntries() {
		globalEntries = new ArrayList<IGuiListEntry>();
		
		if (WDLPluginChannels.hasPermissions()) {
			canDownloadInGeneral = new PermissionEntry(
					"Can download: "
							+ WDLPluginChannels.canDownloadInGeneral(),
					"Controls whether you are able to download");
			canCacheChunks = new PermissionEntry(
					"Can cache chunks: "
							+ WDLPluginChannels.canCacheChunks(),
					"Controls whether chunks are saved as you move about "
							+ "the world", 
					canDownloadInGeneral);
			saveRadius = new DownloadRadiusEntry(
					"Download radius: "
							+ WDLPluginChannels.getSaveRadius(),
					"Radius for downloading chunks (only when caching disabled)",
					canCacheChunks);
			canSaveEntities = new PermissionEntry(
					"Can save entities: "
							+ WDLPluginChannels.canSaveEntities(),
					"Controls whether you can save entities",
					canDownloadInGeneral);
			canSaveTileEntities = new PermissionEntry(
					"Can save tile entities: "
							+ WDLPluginChannels.canSaveTileEntities(),
					"Controls whether you can save tile entities",
					canDownloadInGeneral);
			canSaveContainers = new PermissionEntry(
					"Can save containers: "
							+ WDLPluginChannels.canSaveContainers(),
					"Controls whether you can save containers",
					canSaveTileEntities);
			canDoUnknownThings = new PermissionEntry(
					"Can use functions unknown to the server: "
							+ WDLPluginChannels
									.canUseFunctionsUnknownToServer(),
					"Controls whether you can use newer functions of WDL.",
					canDownloadInGeneral);
			sendEntityRanges = new PermissionEntry("Send entity ranges: "
					+ WDLPluginChannels.hasServerEntityRange()
					+ "(received "
					+ WDLPluginChannels.getEntityRanges().size() + ")",
					"Required if the server runs spigot and edits the "
							+ "entity track distances.  Not all servers "
							+ "do, and if not this permission is useless.");
			allWorlds = new RequestOnlyPermissionEntry(
					"Single world: TODO",
					"Controls whether the options effect all worlds or " +
					"just the current one");
			
			globalEntries.add(canDownloadInGeneral);
			globalEntries.add(canCacheChunks);
			globalEntries.add(saveRadius);
			globalEntries.add(canSaveEntities);
			globalEntries.add(canSaveTileEntities);
			globalEntries.add(canSaveContainers);
			globalEntries.add(canDoUnknownThings);
			globalEntries.add(sendEntityRanges);
			globalEntries.add(allWorlds);
		}
	}
	
	/**
	 * List of permissions.
	 */
	private class PermissionsList extends GuiListExtended {
		public PermissionsList() {
			super(GuiWDLPermissions.this.mc, GuiWDLPermissions.this.width,
					GuiWDLPermissions.this.height, TOP_MARGIN,
					GuiWDLPermissions.this.height - BOTTOM_MARGIN, 
					fontRendererObj.FONT_HEIGHT * 2 + 2);
			
			localEntries = new ArrayList<IGuiListEntry>();
			
			String message = WDLPluginChannels.getRequestMessage();
			if (message != null && !message.isEmpty()) {
				message = "§lNote from the server moderators: §r" + message;
				
				List<String> lines = Utils.wordWrap(message, getListWidth());
				
				Iterator<String> ittr = lines.iterator();
				
				while (ittr.hasNext()) {
					String line1 = ittr.next();
					String line2 = (ittr.hasNext() ? ittr.next() : "");
					
					localEntries.add(new TextEntry(line1, line2));
				}
			}
		}
		
		/**
		 * Line-wrapped message entries.  Stored here because it can change.
		 */
		private ArrayList<IGuiListEntry> localEntries;
		
		@Override
		public IGuiListEntry getListEntry(int index) {
			if (index < localEntries.size()) {
				return localEntries.get(index);
			} else {
				return globalEntries.get(index - localEntries.size());
			}
		}
		
		@Override
		protected int getSize() {
			return localEntries.size() + globalEntries.size();
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
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		this.buttonList.clear();
		
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
		
		calculateEntries();
		
		this.list = new PermissionsList();
	}
	
	@Override
	public void updateScreen() {
		if (refreshTicks > 0) {
			refreshTicks--;
		} else if (refreshTicks == 0) {
			calculateEntries();
			reloadButton.enabled = true;
			reloadButton.displayString = "Reload permissions";
			refreshTicks = -1;
		}
	}
	
	@Override
	public void onGuiClosed() {
		WDL.saveProps();
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
			
			refreshTicks = 50; // 2.5 seconds
		}
		if (button.id == 100) {
			this.mc.displayGuiScreen(this.parent);
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.list == null) {
			return;
		}
		
		this.list.drawScreen(mouseX, mouseY, partialTicks);
		
		this.drawCenteredString(this.fontRendererObj, "Permission info",
				this.width / 2, 8, 0xFFFFFF);
		
		if (!WDLPluginChannels.hasPermissions()) {
			this.drawCenteredString(this.fontRendererObj,
					"No permissions received; defaulting to everything enabled.",
					this.width / 2, (this.height - 32 - 23) / 2 + 23
							- fontRendererObj.FONT_HEIGHT / 2, 0xFFFFFF);
		}
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
