package wdl.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collection;

import wdl.WDL;
import wdl.WDLMessages;
import wdl.api.IWDLMessageType;
import wdl.WDLPluginChannels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;

public class GuiWDLMessages extends GuiScreen {
	private class GuiMessageTypeList extends GuiListExtended {
		public GuiMessageTypeList() {
			super(GuiWDLMessages.this.mc, GuiWDLMessages.this.width,
					GuiWDLMessages.this.height, 39,
					GuiWDLMessages.this.height - 32, 20);
		}

		private class CategoryEntry implements IGuiListEntry {
			private final GuiButton button;
			private final String category;
			
			public CategoryEntry(String category) {
				this.category = category;
				this.button = new GuiButton(0, 0, 0, 80, 20, "");
			}
			
			@Override
			public void setSelected(int p_178011_1_, int p_178011_2_,
					int p_178011_3_) {
				
			}

			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				drawCenteredString(fontRendererObj, category,
						GuiWDLMessages.this.width / 2 - 40, y + slotHeight
						- mc.fontRendererObj.FONT_HEIGHT - 1, 0xFFFFFF);
				
				button.xPosition = GuiWDLMessages.this.width / 2 + 20;
				button.yPosition = y;
				
				//TODO logic for enabling groups
				button.displayString = (true ? "Group enabled" : "Group disabled");
				button.enabled = false; //WDLMessages.enableAllMessages;
				
				button.drawButton(mc, mouseX, mouseY);
			}

			@Override
			public boolean mousePressed(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				if (button.mousePressed(mc, x, y)) {
					//TODO logic for enabling groups
					
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
		
		private class MessageTypeEntry implements IGuiListEntry {
			private final GuiButton button;
			private final IWDLMessageType type;
			
			public MessageTypeEntry(IWDLMessageType type) {
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
				button.xPosition = GuiWDLMessages.this.width / 2 - 100;
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
			Map<String, Collection<IWDLMessageType>> map = WDLMessages.getTypes().asMap();
			for (Map.Entry<String, Collection<IWDLMessageType>> e : map.entrySet()) {
				add(new CategoryEntry(e.getKey()));
				
				for (IWDLMessageType type : e.getValue()) {
					add(new MessageTypeEntry(type));
				}
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
	private GuiMessageTypeList list;
	
	public GuiWDLMessages(GuiScreen parent) {
		this.parent = parent;
	}
	
	private GuiButton masterDebugSwitch;
	
	@Override
	public void initGui() {
		int x = (this.width / 2) - 100;
		
		masterDebugSwitch = new GuiButton(100, x, 18, "Show WDL messages: " + 
				(WDLMessages.enableAllMessages ? "Yes" : "No"));
		this.buttonList.add(masterDebugSwitch);
		
		this.list = new GuiMessageTypeList();
		
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
			
			WDL.baseProps.setProperty("Messages.enableAll",
					Boolean.toString(WDLMessages.enableAllMessages));
			button.displayString = "Show WDL messages (Recomended): " + 
					(WDLMessages.enableAllMessages ? "Yes" : "No");
		} else if (button.id == 101) {
			this.mc.displayGuiScreen(this.parent);
		}
	}
	
	@Override
	public void onGuiClosed() {
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
		
		this.drawCenteredString(this.fontRendererObj, "Message options",
				this.width / 2, 8, 0xFFFFFF);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
