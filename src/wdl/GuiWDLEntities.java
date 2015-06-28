package wdl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.text.Collator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiKeyBindingList;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;

/**
 * GUI that controls what entities are saved.
 */
public class GuiWDLEntities extends GuiScreen {
	private class GuiEntityList extends GuiListExtended {
		/**
		 * Width of the largest entry.
		 */
		private int largestWidth;
		
		private List<IGuiListEntry> entries = new ArrayList<IGuiListEntry>() {{
			try {
				List<String> passiveEntities = new ArrayList<String>();
				List<String> hostileEntities = new ArrayList<String>();
				List<String> otherEntities = new ArrayList<String>();
				
				//Attempt to steal the 'stringToClassMapping' field. 
				Map<String, Class> stringToClassMapping = null;
				
				for (Field field : EntityList.class.getDeclaredFields()) {
					if (field.getType().equals(Map.class)) {
						field.setAccessible(true);
						Map m = (Map)field.get(null);
						
						Map.Entry e = (Map.Entry)m.entrySet().toArray()[0];
						if (e.getKey() instanceof String && 
								e.getValue() instanceof Class) {
							stringToClassMapping = (Map<String, Class>)m;
						}
					}
				}
				
				if (stringToClassMapping == null) {
					throw new Exception("Failed to find stringToClassMapping");
				}
				
				//Now build an actual list.
				for (Map.Entry<String, Class> e : 
						stringToClassMapping.entrySet()) {
					String entity = e.getKey();
					Class c = e.getValue();
					
					if (IMob.class.isAssignableFrom(c)) {
						hostileEntities.add(entity);
					} else if (IAnimals.class.isAssignableFrom(c)) {
						passiveEntities.add(entity);
					} else {
						otherEntities.add(entity);
					}
				}
				
				otherEntities.add("Hologram");
				
				Collections.sort(hostileEntities, Collator.getInstance());
				Collections.sort(passiveEntities, Collator.getInstance());
				Collections.sort(otherEntities, Collator.getInstance());
				
				int largestWidthSoFar = 0;
				
				add(new CategoryEntry("General"));
				for (String entity : otherEntities) {
					add(new EntityEntry(entity));
					
					int width = fontRendererObj.getStringWidth(entity);
					if (width > largestWidthSoFar) {
						largestWidthSoFar = width;
					}
				}
				add(new CategoryEntry("Passive mobs"));
				for (String entity : passiveEntities) {
					add(new EntityEntry(entity));
					
					int width = fontRendererObj.getStringWidth(entity);
					if (width > largestWidthSoFar) {
						largestWidthSoFar = width;
					}
				}
				add(new CategoryEntry("Hostile mobs"));
				for (String entity : hostileEntities) {
					add(new EntityEntry(entity));
					
					int width = fontRendererObj.getStringWidth(entity);
					if (width > largestWidthSoFar) {
						largestWidthSoFar = width;
					}
				}
				
				largestWidth = largestWidthSoFar;
				
			} catch (Exception e) {
				WDL.chatError("Error setting up Entity List UI: " + e);
				e.printStackTrace();
				
				Minecraft.getMinecraft().displayGuiScreen(null);
			}
		}};
		
		/**
		 * Provides a label.
		 * 
		 * Based off of 
		 * {@link net.minecraft.client.gui.GuiKeyBindingList.CategoryEntry}.
		 */
		private class CategoryEntry implements GuiListExtended.IGuiListEntry {
			private final String labelText;
			private final int labelWidth;

			public CategoryEntry(String text) {
				this.labelText = text; //I18n.format(text, new Object[0]);
				this.labelWidth = mc.fontRendererObj.getStringWidth(this.labelText);
			}

			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				mc.fontRendererObj.drawString(this.labelText,
						mc.currentScreen.width / 2 - this.labelWidth / 2, y
								+ slotHeight + mc.fontRendererObj.FONT_HEIGHT
								- 1, 0xFFFFFF);
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
			public void setSelected(int p_178011_1_, int p_178011_2_,
					int p_178011_3_) {
			}
		}
		
		/**
		 * Contains an actual entity's data.
		 * 
		 * TODO: Implement buttons and such.
		 * 
		 * Based off of 
		 * {@link net.minecraft.client.gui.GuiKeyBindingList.CategoryEntry}.
		 */
		private class EntityEntry implements GuiListExtended.IGuiListEntry {
			private final String labelText;
			private final int labelWidth;

			public EntityEntry(String entity) {
				this.labelText = entity;
				this.labelWidth = mc.fontRendererObj.getStringWidth(this.labelText);
			}

			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				mc.fontRendererObj.drawString(this.labelText,
						x + 90 - largestWidth, y + slotHeight + 
								mc.fontRendererObj.FONT_HEIGHT - 1, 0xFFFFFF);
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
			public void setSelected(int p_178011_1_, int p_178011_2_,
					int p_178011_3_) {
			}
		}
		
		public GuiEntityList() {
			super(GuiWDLEntities.this.mc, GuiWDLEntities.this.width,
					GuiWDLEntities.this.height, 63,
					GuiWDLEntities.this.height - 32, 20);
		}

		@Override
		public IGuiListEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}
	}
	
	private GuiEntityList entityList;
	private GuiScreen parent;
	
	public GuiWDLEntities(GuiScreen parent) {
		this.parent = parent;
	}
	
	@Override
	public void initGui() {
		this.buttonList.add(new GuiButton(200, this.width / 2 - 155,
				this.height - 29, 150, 20, "OK"));
		
		this.entityList = new GuiEntityList();
	}
	
	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.entityList.func_178039_p();
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 200) {
			mc.displayGuiScreen(parent);
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
			throws IOException {
		if (entityList.func_148179_a(mouseX, mouseY, mouseButton)) {
			return;
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if (entityList.func_148181_b(mouseX, mouseY, state)) {
			return;
		}
		super.mouseReleased(mouseX, mouseY, state);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.entityList.drawScreen(mouseX, mouseY, partialTicks);
		
		this.drawCenteredString(this.fontRendererObj, "Entity configuration",
				this.width / 2, 8, 0xFFFFFF);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
