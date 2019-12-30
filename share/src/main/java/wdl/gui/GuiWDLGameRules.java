/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.GameRules;
import wdl.WDL;
import wdl.gui.widget.ButtonDisplayGui;
import wdl.gui.widget.GuiList;
import wdl.gui.widget.GuiList.GuiListEntry;
import wdl.gui.widget.GuiNumericTextField;
import wdl.gui.widget.WDLButton;
import wdl.gui.widget.WDLScreen;
import wdl.versioned.VersionedFunctions;
import wdl.versioned.VersionedFunctions.GameRuleType;

public class GuiWDLGameRules extends WDLScreen {
	/**
	 * Text to draw (set from inner classes)
	 */
	@Nullable
	private String hoveredToolTip;

	/**
	 * Colors for the text field on numeric entries when there is/is not a
	 * modified rule.
	 */
	private static final int SET_TEXT_FIELD = 0xE0E0E0, DEFAULT_TEXT_FIELD = 0x808080;

	private class GuiGameRuleList extends GuiList<GuiGameRuleList.RuleEntry> {
		/** The entry that was last clicked.  This should be compared by ref. */
		@Nullable
		private RuleEntry lastClickedEntry = null;

		public GuiGameRuleList() {
			super(GuiWDLGameRules.this.minecraft, GuiWDLGameRules.this.width,
					GuiWDLGameRules.this.height, 39,
					GuiWDLGameRules.this.height - 32, 24);
			List<RuleEntry> entries = this.getEntries();
			for (String rule : vanillaGameRules.keySet()) {
				GameRuleType type = VersionedFunctions.getRuleType(rules, rule);
				if (type == null) continue;
				switch (type) {
				case INTEGER:
					entries.add(new IntRuleEntry(rule));
					break;
				case BOOLEAN:
					entries.add(new BooleanRuleEntry(rule));
					break;
				}
			}
		}

		private abstract class RuleEntry extends GuiListEntry<RuleEntry> {
			@Nonnull
			protected final String ruleName;
			private WDLButton resetButton;

			public RuleEntry(@Nonnull String ruleName) {
				this.ruleName = ruleName;
				resetButton = this.addButton(new WDLButton(0, 0, 50, 20,
						I18n.format("wdl.gui.gamerules.resetRule")) {
					public @Override void performAction() {
						performResetAction();
					}
				}, 110, 0);
			}

			@Override
			public void drawEntry(int x, int y, int width, int height, int mouseX, int mouseY) {
				this.resetButton.setEnabled(isRuleNonDefault(this.ruleName));

				super.drawEntry(x, y, width, height, mouseX, mouseY);

				drawString(font, this.ruleName, x, y + 6, 0xFFFFFFFF);

				if (this.isHoveredControl(mouseX, mouseY)) {
					String key = "wdl.gui.gamerules.rules." + ruleName;
					if (I18n.hasKey(key)) { // may return false for mods
						hoveredToolTip = I18n.format(key);
					}
				}
			}

			@Override
			public boolean mouseDown(int mouseX, int mouseY, int mouseButton) {
				lastClickedEntry = this;
				return super.mouseDown(mouseX, mouseY, mouseButton);
			}

			protected abstract boolean isHoveredControl(int mouseX, int mouseY);

			@Override
			public boolean isSelected() {
				return lastClickedEntry == this;
			}

			/** Called when the reset button is clicked. */
			protected void performResetAction() {
				clearRule(this.ruleName);
			}
		}

		private class IntRuleEntry extends RuleEntry {
			private GuiNumericTextField field;

			public IntRuleEntry(String ruleName) {
				super(ruleName);
				field = this.addTextField(new GuiNumericTextField(
						0, font, 0, 0, 100, 20), 0, 0);
				field.setText(getRule(ruleName));
			}

			@Override
			public void drawEntry(int x, int y, int width, int height, int mouseX, int mouseY) {
				super.drawEntry(x, y, width, height, mouseX, mouseY);
				if (!this.isSelected()) {
					field.setFocused2(false);
				}
				if (isRuleNonDefault(this.ruleName)) {
					field.setTextColor(SET_TEXT_FIELD);
				} else {
					field.setTextColor(DEFAULT_TEXT_FIELD);
				}
			}

			@Override
			public void anyKeyPressed() {
				if (this.field.isFocused()) {
					setRule(ruleName, Integer.toString(this.field.getValue()));
				}
			}

			@Override
			protected boolean isHoveredControl(int mouseX, int mouseY) {
				return Utils.isHoveredTextBox(mouseX, mouseY, field);
			}

			@Override
			protected void performResetAction() {
				super.performResetAction();
				this.field.setText(getRule(this.ruleName)); // Reset field text to default
			}
		}

		private class BooleanRuleEntry extends RuleEntry {
			private WDLButton button;

			public BooleanRuleEntry(String ruleName) {
				super(ruleName);
				button = this.addButton(new WDLButton(0, 0, 100, 20, "") {
					public @Override void performAction() {
						boolean oldValue = getRule(ruleName).equals("true");
						setRule(ruleName, oldValue ? "false" : "true");
					}
				}, 0, 0);
			}

			@Override
			public void drawEntry(int x, int y, int width, int height, int mouseX, int mouseY) {
				this.button.setMessage(getRule(ruleName));
				super.drawEntry(x, y, width, height, mouseX, mouseY);
			}

			@Override
			protected boolean isHoveredControl(int mouseX, int mouseY) {
				return button.isHovered();
			}
		}

		@Override
		public int getEntryWidth() {
			return 210 * 2;
		}
	}

	@Nullable
	private final GuiScreen parent;
	private final WDL wdl;

	/** The gamerules object to modify */
	private final GameRules rules;
	/** All vanilla game rules and their default values; this list is immutable. */
	private final Map<String, String> vanillaGameRules;

	private WDLButton doneButton;

	/**
	 * Gets the value of the given rule.
	 *
	 * @param ruleName
	 *            The name of the rule.
	 * @return The rule's string value. Will be null if no such rule exists.
	 */
	@Nullable
	private String getRule(@Nonnull String ruleName) {
		return VersionedFunctions.getRuleValue(rules, ruleName);
	}

	/**
	 * Updates the value of the given rule.
	 *
	 * @param ruleName
	 *            The name of the rule.
	 * @param value
	 *            The new value. Must not be null; to clear a rule, use
	 *            {@link #clearRule(String)}.
	 */
	private void setRule(@Nonnull String ruleName, @Nonnull String value) {
		VersionedFunctions.setRuleValue(rules, ruleName, value);
	}

	/**
	 * Returns the given rule to its default value.
	 *
	 * @param ruleName
	 *            The name of the rule.
	 */
	private void clearRule(@Nonnull String ruleName) {
		setRule(ruleName, vanillaGameRules.get(ruleName));
	}

	/**
	 * Checks if the given rule is overridden.
	 *
	 * @param ruleName
	 *            The name of the rule.
	 * @return True if the rule is overriden; false otherwise (not overriden or
	 *         no such rule exists).
	 */
	private boolean isRuleNonDefault(@Nonnull String ruleName) {
		return !vanillaGameRules.get(ruleName).equals(getRule(ruleName));
	}

	public GuiWDLGameRules(@Nullable GuiScreen parent, WDL wdl) {
		super("wdl.gui.gamerules.title");
		this.parent = parent;
		this.wdl = wdl;
		this.rules = wdl.gameRules;
		// We're not currently modifying the rules on worldClient itself, so they can be considered
		// to be the defaults... probably.
		GameRules defaultRules = this.wdl.worldClient.getGameRules();
		this.vanillaGameRules = VersionedFunctions.getGameRules(defaultRules);
	}

	@Override
	public void init() {
		this.addList(new GuiGameRuleList());

		this.doneButton = this.addButton(new ButtonDisplayGui(this.width / 2 - 100,
				this.height - 29, 200, 20, this.parent));
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		hoveredToolTip = null;

		super.render(mouseX, mouseY, partialTicks);

		if (this.doneButton.isHovered()) {
			Utils.drawGuiInfoBox(I18n.format("wdl.gui.gamerules.doneInfo"),
					width, height, 48);
		} else if (hoveredToolTip != null) {
			Utils.drawGuiInfoBox(hoveredToolTip, width, height, 48);
		}
	}

	@Override
	public void removed() {
		// Can't save anywhere until the download actually occurs...
	}
}
