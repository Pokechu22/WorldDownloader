package wdl.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import wdl.WDL;
import wdl.WDLMessages;
import wdl.api.IWDLMessageType;
import wdl.WDLPluginChannels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;

public class GuiWDLDebug extends GuiScreen {
	private class GuiDebugList extends GuiListExtended {
		public GuiDebugList() {
			super(GuiWDLDebug.this.mc, GuiWDLDebug.this.width,
					GuiWDLDebug.this.height, 39,
					GuiWDLDebug.this.height - 32, 20);
		}

		private class DebugEntry implements IGuiListEntry {
			private final GuiButton button;
			private final IWDLMessageType type;
			
			public DebugEntry(IWDLMessageType type) {
				this.type = type;
				this.button = new GuiButton(0, 0, 0, type.toString());
			}
			
			@Override
			public void setSelected(int p_178011_1_, int p_178011_2_,
					int p_178011_3_) {
				
			}

			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				button.xPosition = GuiWDLDebug.this.width / 2 - 100;
				button.yPosition = y;
				
				button.displayString = type.getDisplayName() + ": " + 
						(WDLMessages.isEnabled(type) ? "On" : "Off");
				button.enabled = WDLMessages.enableAllMessages;
				
				button.drawButton(mc, mouseX, mouseY);
			}

			@Override
			public boolean mousePressed(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				if (button.mousePressed(mc, x, y)) {
					WDLMessages.toggleEnabled(type);
					
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
			for (IWDLMessageType cause : WDLMessages.getTypes().values()) {
				add(new DebugEntry(cause));
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
	
	private GuiScreen parent;
	private GuiDebugList list;
	
	public GuiWDLDebug(GuiScreen parent) {
		this.parent = parent;
	}
	
	private GuiButton masterDebugSwitch;
	
	@Override
	public void initGui() {
		int x = (this.width / 2) - 100;
		
		masterDebugSwitch = new GuiButton(100, x, 18, "Master debug switch: " + 
				(WDLMessages.enableAllMessages ? "On" : "Off"));
		this.buttonList.add(masterDebugSwitch);
		
		this.list = new GuiDebugList();
		
		this.buttonList.add(new GuiButton(101, x, this.height - 29, "Done"));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (!button.enabled) {
			return;
		}
		
		if (button.id == 100) {
			//"Master switch"
			WDLMessages.enableAllMessages ^= true;
			
			button.displayString = "Master debug switch: " + 
					(WDLMessages.enableAllMessages ? "On" : "Off");
		} else if (button.id == 101) {
			this.mc.displayGuiScreen(this.parent);
		}
	}
	
	@Override
	public void onGuiClosed() {
		WDL.baseProps.setProperty("Debug.globalDebugEnabled", 
				WDLMessages.enableAllMessages ? "true" : "false");
		for (IWDLMessageType type : WDLMessages.getTypes().values()) {
			WDL.baseProps.setProperty("Debug." + type.getName(),
					WDLMessages.isEnabled(type) ? "true" : "false");
		}
		WDL.saveProps();
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
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
			throws IOException {
		if (list.func_148179_a(mouseX, mouseY, mouseButton)) {
			return;
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if (list.func_148181_b(mouseX, mouseY, state)) {
			return;
		}
		super.mouseReleased(mouseX, mouseY, state);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.list.drawScreen(mouseX, mouseY, partialTicks);
		
		this.drawCenteredString(this.fontRendererObj, "Debug options",
				this.width / 2, 8, 0xFFFFFF);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
