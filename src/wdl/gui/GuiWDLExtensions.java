package wdl.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import wdl.WDL;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.util.ResourceLocation;

public class GuiWDLExtensions extends GuiScreen {
	private class ModList extends GuiListExtended {
		public ModList() {
			super(GuiWDLExtensions.this.mc, GuiWDLExtensions.this.width,
					GuiWDLExtensions.this.height, 39,
					GuiWDLExtensions.this.height - 32, 20);
			this.showSelectionBox = true;
		}
		
		private class ModEntry implements IGuiListEntry {
			String text;
			public ModEntry(String text) {
				this.text = text;
			}
			
			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				fontRendererObj.drawString(text, x, y + slotHeight
						- fontRendererObj.FONT_HEIGHT - 1, 0xFFFFFF);
			}

			@Override
			public boolean mousePressed(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				if (selectedElement != slotIndex) {
					selectedElement = slotIndex;
					
					mc.getSoundHandler().playSound(
							PositionedSoundRecord.createPositionedSoundRecord(
									new ResourceLocation("gui.button.press"),
									1.0F));
				}
				return true;
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
		
		private List<IGuiListEntry> entries = new ArrayList<IGuiListEntry>() {{
			add(new ModEntry("1"));
			add(new ModEntry("2"));
		}};
		
		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			super.drawScreen(mouseX, mouseY, partialTicks);
		}
		
		@Override
		public IGuiListEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}
		
		@Override
		protected boolean isSelected(int slotIndex) {
			return slotIndex == selectedElement;
		}
		
		@Override
		public int getListWidth() {
			return GuiWDLExtensions.this.width - 20;
		}
	}
	
	/**
	 * Gui to display after this is closed.
	 */
	private final GuiScreen parent;
	/**
	 * List of mods.
	 */
	private ModList list;
	
	public GuiWDLExtensions(GuiScreen parent) {
		this.parent = parent;
	}
	
	@Override
	public void initGui() {
		this.list = new ModList();
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
		
		this.drawCenteredString(this.fontRendererObj, "WDL extensions",
				this.width / 2, 8, 0xFFFFFF);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
