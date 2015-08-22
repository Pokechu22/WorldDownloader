package wdl;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.util.MathHelper;

/**
 * GUI that controls what entities are saved.
 */
public class GuiWDLEntities extends GuiScreen {
	private class GuiEntityList extends GuiListExtended {
		/**
		 * A slider that doesn't require a bunch of interfaces to work.
		 * 
		 * Based off of {@link net.minecraft.client.gui.GuiOptionSlider}.
		 */
		private class GuiSlider extends GuiButton {
			private float sliderValue;
			private boolean dragging;
			/**
			 * Text put before to the progress.
			 */
			private final String prepend;
			/**
			 * Maximum value for the slider.
			 */
			private final int max;

			public GuiSlider(int id, int x, int y, int width, int height, 
					String text, int value, int max) {
				super(id, x, y, width, height, text);
				
				this.prepend = text;
				this.max = max;
				
				setValue(value);
			}

			/**
			 * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over
			 * this button and 2 if it IS hovering over this button.
			 */
			@Override
			public int getHoverState(boolean mouseOver) {
				return 0;
			}

			/**
			 * Fired when the mouse button is dragged. Equivalent of
			 * MouseListener.mouseDragged(MouseEvent e).
			 */
			@Override
			protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
				if (this.visible) {
					if (this.dragging) {
						this.sliderValue = (float)(mouseX - (this.xPosition + 4))
								/ (float)(this.width - 8);
						this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F,
								1.0F);
						this.dragging = true;
						
						this.displayString = prepend + ": " + getValue();
					}

					mc.getTextureManager().bindTexture(buttonTextures);
					
					if (this.enabled) {
						this.drawTexturedModalRect(this.xPosition
								+ (int) (this.sliderValue * (this.width - 8)),
								this.yPosition, 0, 66, 4, 20);
						this.drawTexturedModalRect(this.xPosition
								+ (int) (this.sliderValue * (this.width - 8))
								+ 4, this.yPosition, 196, 66, 4, 20);
					} else {
						this.drawTexturedModalRect(this.xPosition
								+ (int) (this.sliderValue * (this.width - 8)),
								this.yPosition, 0, 46, 4, 20);
						this.drawTexturedModalRect(this.xPosition
								+ (int) (this.sliderValue * (this.width - 8))
								+ 4, this.yPosition, 196, 46, 4, 20);
					}
				}
			}

			/**
			 * Returns true if the mouse has been pressed on this control. Equivalent of
			 * MouseListener.mousePressed(MouseEvent e).
			 */
			@Override
			public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
				if (super.mousePressed(mc, mouseX, mouseY)) {
					this.sliderValue = (float)(mouseX - (this.xPosition + 4))
							/ (float)(this.width - 8);
					this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F,
							1.0F);
					this.displayString = prepend + ": " + getValue();
					
					this.dragging = true;
					return true;
				} else {
					return false;
				}
			}

			/**
			 * Gets the current value of the slider.
			 * @return
			 */
			public int getValue() {
				return (int)(sliderValue * max);
			}
			
			/**
			 * Gets the current value of the slider.
			 * @return
			 */
			public void setValue(int value) {
				this.sliderValue = value / (float)max;
				
				this.displayString = prepend + ": " + getValue();
			}
			
			/**
			 * Fired when the mouse button is released. Equivalent of
			 * MouseListener.mouseReleased(MouseEvent e).
			 */
			@Override
			public void mouseReleased(int mouseX, int mouseY) {
				this.dragging = false;
			}
		}
		
		/**
		 * Width of the largest entry.
		 */
		private int largestWidth;
		
		private List<IGuiListEntry> entries = new ArrayList<IGuiListEntry>() {{
			try {
				int largestWidthSoFar = 0;
				
				add(new CategoryEntry("General"));
				for (String entity : EntityUtils.otherEntityList) {
					add(new EntityEntry(entity));
					
					int width = fontRendererObj.getStringWidth(entity);
					if (width > largestWidthSoFar) {
						largestWidthSoFar = width;
					}
				}
				add(new CategoryEntry("Passive mobs"));
				for (String entity : EntityUtils.passiveEntityList) {
					add(new EntityEntry(entity));
					
					int width = fontRendererObj.getStringWidth(entity);
					if (width > largestWidthSoFar) {
						largestWidthSoFar = width;
					}
				}
				add(new CategoryEntry("Hostile mobs"));
				for (String entity : EntityUtils.hostileEntityList) {
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
					int slotHeight, Tessellator t, int mouseX, int mouseY, boolean isSelected) {
				mc.fontRendererObj.drawString(this.labelText,
						mc.currentScreen.width / 2 - this.labelWidth / 2, y
								+ slotHeight - mc.fontRendererObj.FONT_HEIGHT 
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
			private final String entity;
			private final GuiButton onOffButton;
			private final GuiSlider rangeSlider;
			
			private boolean enabled;
			private int range;
			
			private String cachedMode;
			
			public EntityEntry(String entity) {
				this.entity = entity;
				
				enabled = EntityUtils.isEntityEnabled(entity);
				range = EntityUtils.getEntityTrackDistance(entity);
				
				this.onOffButton = new GuiButton(0, 0, 0, 75, 18, 
						enabled ? "§aIncluded" : "§cIgnored");
				
				this.rangeSlider = new GuiSlider(1, 0, 0, 150, 18,
						"Track Distance", range, 256);
				
				this.cachedMode = mode;
				
				rangeSlider.enabled = (cachedMode.equals("user"));
			}

			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, Tessellator t, int mouseX, int mouseY,
					boolean isSelected) {
				mc.fontRendererObj.drawString(this.entity,
						x - 60 - largestWidth, y + slotHeight / 2 - 
								mc.fontRendererObj.FONT_HEIGHT / 2, 0xFFFFFF);
				
				this.onOffButton.xPosition = x - 45;
				this.onOffButton.yPosition = y;
				this.onOffButton.displayString = 
						enabled ? "§aIncluded" : "§cIgnored";
				
				this.rangeSlider.xPosition = x + 50;
				this.rangeSlider.yPosition = y;
				
				if (!this.cachedMode.equals(mode)) {
					cachedMode = mode;
					rangeSlider.enabled = (cachedMode.equals("user"));
					
					rangeSlider.setValue(EntityUtils
							.getEntityTrackDistance(entity));
				}
				
				this.onOffButton.drawButton(mc, mouseX, mouseY);
				this.rangeSlider.drawButton(mc, mouseX, mouseY);
			}

			@Override
			public boolean mousePressed(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				if (onOffButton.mousePressed(mc, x, y)) {
					enabled ^= true;
					
					onOffButton.playPressSound(mc.getSoundHandler());
					
					this.onOffButton.displayString = 
							enabled ? "§aIncluded" : "§cIgnored";
					
					WDL.worldProps.setProperty("Entity." + entity + 
							".Enabled", Boolean.toString(enabled));
					return true;
				}
				if (rangeSlider.mousePressed(mc, x, y)) {
					range = rangeSlider.getValue();
					
					WDL.worldProps.setProperty("Entity." + entity
							+ ".TrackDistance",
							Integer.toString(range));
					
					return true;
				}
				
				return false;
			}

			@Override
			public void mouseReleased(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				rangeSlider.mouseReleased(x, y);
				
				if (this.cachedMode.equals("user")) {
					range = rangeSlider.getValue();
					
					WDL.worldProps.setProperty("Entity." + entity
							+ ".TrackDistance",
							Integer.toString(range));
				}
			}
		}
		
		public GuiEntityList() {
			super(GuiWDLEntities.this.mc, GuiWDLEntities.this.width,
					GuiWDLEntities.this.height, 39,
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
	
	private GuiButton rangeModeButton;
	private GuiButton presetsButton;
	
	private String mode;
	
	public GuiWDLEntities(GuiScreen parent) {
		this.parent = parent;
	}
	
	@Override
	public void initGui() {
		this.buttonList.add(new GuiButton(200, this.width / 2 - 155,
				this.height - 29, 150, 20, "OK"));
		
		rangeModeButton = new GuiButton(100, this.width / 2 - 155, 18, 150,
				20, "Track distance: Error");
		presetsButton = new GuiButton(101, this.width / 2 + 5, 18, 150, 20, 
				"Presets...");
		
		this.mode = WDL.worldProps.getProperty("Entity.TrackDistanceMode");
		if (mode.equals("default")) {
			rangeModeButton.displayString = "Track distance: Default";
			
			presetsButton.enabled = false;
		} else if (mode.equals("server")) {
			if (WDLPluginChannels.hasServerEntityRange()) {
				rangeModeButton.displayString = "Track distance: Server";
			} else {
				rangeModeButton.displayString = "Track distance: Default";
			}
			
			presetsButton.enabled = false;
		} else {
			rangeModeButton.displayString = "Track distance: User";
			
			presetsButton.enabled = true;
		}
		
		this.buttonList.add(rangeModeButton);
		this.buttonList.add(presetsButton);
		
		this.entityList = new GuiEntityList();
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 100) {
			if (mode.equals("default")) {
				if (WDLPluginChannels.hasServerEntityRange()) {
					mode = "server";
				} else {
					mode = "user";
				}
			} else if (mode.equals("server")) {
				mode = "user";
			} else {
				mode = "default";
			}
			
			if (mode.equals("default")) {
				rangeModeButton.displayString = "Track distance: Default";
				
				presetsButton.enabled = false;
			} else if (mode.equals("server")) {
				if (WDLPluginChannels.hasServerEntityRange()) {
					rangeModeButton.displayString = "Track distance: Server";
				} else {
					rangeModeButton.displayString = "Track distance: Default";
				}
				
				presetsButton.enabled = false;
			} else {
				rangeModeButton.displayString = "Track distance: User";
				
				presetsButton.enabled = true;
			}
			
			WDL.worldProps.setProperty("Entity.TrackDistanceMode", mode);
		}
		if (button.id == 101 && button.enabled) {
			WDL.saveProps();
			
			mc.displayGuiScreen(new GuiWDLEntityPresets(this));
		}
		if (button.id == 200) {
			WDL.saveProps();
			
			mc.displayGuiScreen(parent);
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
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
