package wdl.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.ValueType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wdl.WDL;

public class GuiWDLGameRules extends GuiScreen {
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Text to draw (set from inner classes)
	 */
	@Nullable
	private String hoveredToolTip;

	private static interface KeyboardEntry extends IGuiListEntry {
		public abstract void keyDown(char typedChar, int keyCode);
		public abstract void onUpdate();
	}

	private class GuiGameRuleList extends GuiListExtended {
		public GuiGameRuleList() {
			super(GuiWDLGameRules.this.mc, GuiWDLGameRules.this.width,
					GuiWDLGameRules.this.height, 39,
					GuiWDLGameRules.this.height - 32, 24);
			this.entries = new ArrayList<IGuiListEntry>();
			for (String rule : rules.getRules()) {
				if (rules.areSameType(rule, ValueType.NUMERICAL_VALUE)) {
					this.entries.add(new IntRuleEntry(rule));
				} else if (rules.areSameType(rule, ValueType.BOOLEAN_VALUE)) {
					this.entries.add(new BooleanRuleEntry(rule));
				} else {
					LOGGER.debug("Couldn't identify type for game rule " + rule);
				}
			}
			this.entries.add(new TextEntry(GuiWDLGameRules.this.mc, I18n.format("wdl.gui.gamerules.custom")));
			//this.entries.add(new CustomRuleEntry());
		}

		private abstract class RuleEntry implements IGuiListEntry {
			protected final String ruleName;
			private GuiButton resetButton;

			public RuleEntry(String ruleName) {
				this.ruleName = ruleName;
				this.resetButton = new GuiButton(0, 0, 0, 50, 20, I18n.format("wdl.gui.gamerules.resetRule"));
			}

			@Override
			public void setSelected(int p_178011_1_, int p_178011_2_,
					int p_178011_3_) { }

			@Override
			public final void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				drawString(fontRenderer, this.ruleName, x, y + 6, 0xFFFFFFFF);
				this.resetButton.xPosition = x + listWidth / 2 + 110;
				this.resetButton.yPosition = y;
				this.resetButton.drawButton(mc, mouseX, mouseY);
				this.draw(x, y, listWidth, slotHeight, mouseX, mouseY);

				if (this.isMouseOverControl(mouseX, mouseY)) {
					String key = "wdl.gui.gamerules.rules." + ruleName;
					if (I18n.hasKey(key)) { // may return false for mods
						hoveredToolTip = I18n.format(key);
					}
				}
			}
			@Override
			public final boolean mousePressed(int slotIndex, int mouseX, int mouseY,
					int mouseEvent, int relativeX, int relativeY) {
				if (resetButton.mousePressed(mc, mouseX, mouseY)) {
					resetButton.playPressSound(mc.getSoundHandler());
					// TODO
					return true;
				}
				return mouseDown(mouseX, mouseY, mouseEvent);
			}
			@Override
			public final void mouseReleased(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				resetButton.mouseReleased(mouseX, mouseY);
				mouseUp(mouseX, mouseY, mouseEvent);
			}

			protected abstract void draw(int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY);
			protected abstract boolean mouseDown(int x, int y, int button);
			protected abstract void mouseUp(int x, int y, int button);
			protected abstract boolean isMouseOverControl(int mouseX, int mouseY);
		}

		private class IntRuleEntry extends RuleEntry implements KeyboardEntry {
			private GuiNumericTextField field;

			public IntRuleEntry(String ruleName) {
				super(ruleName);
				field = new GuiNumericTextField(0, fontRenderer, 0, 0, 100, 20);
				field.setText(rules.getString(ruleName));
			}

			@Override
			public void draw(int x, int y, int listWidth, int slotHeight,
					int mouseX, int mouseY) {
				field.xPosition = x + listWidth / 2;
				field.yPosition = y;
				field.drawTextBox();
			}

			@Override
			protected boolean mouseDown(int x, int y, int button) {
				field.mouseClicked(x, y, button);
				return false;
			}

			@Override
			protected void mouseUp(int x, int y, int button) { }

			@Override
			public void onUpdate() {
				this.field.updateCursorCounter();
			}

			@Override
			public void keyDown(char typedChar, int keyCode) {
				if (this.field.textboxKeyTyped(typedChar, keyCode)) {
					rules.setOrCreateGameRule(ruleName, Integer.toString(this.field.getValue()));
				}
			}

			@Override
			protected boolean isMouseOverControl(int mouseX, int mouseY) {
				return Utils.isMouseOverTextBox(mouseX, mouseY, field);
			}
		}

		private class BooleanRuleEntry extends RuleEntry {
			private GuiButton button;

			public BooleanRuleEntry(String ruleName) {
				super(ruleName);
				button = new GuiButton(0, 0, 0, 100, 20, "");
			}

			@Override
			protected void draw(int x, int y, int listWidth, int slotHeight,
					int mouseX, int mouseY) {
				this.button.xPosition = x + listWidth / 2;
				this.button.yPosition = y;
				this.button.displayString = rules.getString(ruleName);
				this.button.drawButton(mc, mouseX, mouseY);
			}

			@Override
			protected boolean mouseDown(int x, int y, int button) {
				if (this.button.mousePressed(mc, x, y)) {
					this.button.playPressSound(mc.getSoundHandler());
					boolean value = !rules.getBoolean(ruleName);
					rules.setOrCreateGameRule(ruleName, Boolean.toString(value));
					return true;
				} else {
					return false;
				}
			}

			@Override
			protected void mouseUp(int x, int y, int button) {
				this.button.mouseReleased(x, y);
			}

			@Override
			protected boolean isMouseOverControl(int mouseX, int mouseY) {
				return button.isMouseOver();
			}
		}

		private final List<IGuiListEntry> entries;

		@Override
		public IGuiListEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}

		public void update() {
			for (IGuiListEntry entry : this.entries) {
				if (entry instanceof KeyboardEntry) {
					((KeyboardEntry) entry).onUpdate();
				}
			}
		}

		public void keyDown(char typedChar, int keyCode) {
			for (IGuiListEntry entry : this.entries) {
				if (entry instanceof KeyboardEntry) {
					((KeyboardEntry) entry).keyDown(typedChar, keyCode);
				}
			}
		}

		@Override
		public int getListWidth() {
			return 210 * 2;
		}

		@Override
		protected int getScrollBarX() {
			return this.width / 2 + getListWidth() / 2 + 4;
		}
	}

	private String title;
	private GuiGameRuleList list;
	@Nullable
	private final GuiScreen parent;
	private GameRules rules;

	public GuiWDLGameRules(@Nullable GuiScreen parent) {
		this.parent = parent;
		this.rules = WDL.worldClient.getGameRules();
	}

	@Override
	public void initGui() {
		this.title = I18n.format("wdl.gui.generator.title");
		this.list = new GuiGameRuleList();

		this.buttonList.add(new GuiButton(100, this.width / 2 - 100,
				this.height - 29, I18n.format("gui.done")));
	}

	@Override
	public void updateScreen() {
		this.list.update();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		hoveredToolTip = null;
		this.list.drawScreen(mouseX, mouseY, partialTicks);

		super.drawScreen(mouseX, mouseY, partialTicks);

		this.drawCenteredString(fontRenderer, title, width / 2, 4, 0xFFFFFFF);

		if (hoveredToolTip != null) {
			Utils.drawGuiInfoBox(hoveredToolTip, width, height, 48);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
			throws IOException {
		this.list.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		this.list.mouseReleased(mouseX, mouseY, state);
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		this.list.keyDown(typedChar, keyCode);
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.list.handleMouseInput();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 100) {
			this.mc.displayGuiScreen(this.parent);
		}
	}
}
