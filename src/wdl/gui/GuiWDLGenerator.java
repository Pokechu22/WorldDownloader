package wdl.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import wdl.WDL;

public class GuiWDLGenerator extends GuiScreen {
	private String title;
	private GuiScreen parent;
	private GuiTextField seedField;
	private GuiButton generatorBtn;
	private GuiButton generateStructuresBtn;

	private String seedText;
	
	public GuiWDLGenerator(GuiScreen parent) {
		this.parent = parent;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		this.seedText = I18n.format("wdl.gui.generator.seed");
		int seedWidth = fontRendererObj.getStringWidth(seedText + " ");
		
		this.buttonList.clear();
		this.title = I18n.format("wdl.gui.generator.title",
				 WDL.baseFolderName.replace('@', ':'));
		int y = this.height / 4 - 15;
		this.seedField = new GuiTextField(40, this.fontRendererObj,
				this.width / 2 - (100 - seedWidth), y, 200 - seedWidth, 18);
		this.seedField.setText(WDL.worldProps.getProperty("RandomSeed"));
		y += 22;
		this.generatorBtn = new GuiButton(1, this.width / 2 - 100, y,
				getGeneratorText());
		this.buttonList.add(this.generatorBtn);
		y += 22;
		this.generateStructuresBtn = new GuiButton(2, this.width / 2 - 100, y,
				getGenerateStructuresText());
		this.buttonList.add(this.generateStructuresBtn);
		
		this.buttonList.add(new GuiButton(100, this.width / 2 - 100, height - 29, 
				I18n.format("gui.done")));
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id == 1) {
				this.cycleGenerator();
			} else if (button.id == 2) {
				this.cycleGenerateStructures();
			} else if (button.id == 100) {
				this.mc.displayGuiScreen(this.parent);
			}
		}
	}
	
	@Override
	public void onGuiClosed() {
		WDL.worldProps.setProperty("RandomSeed", this.seedField.getText());
		
		WDL.saveProps();
	}

	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
	throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.seedField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Fired when a key is typed. This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e).
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		this.seedField.textboxKeyTyped(typedChar, keyCode);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	@Override
	public void updateScreen() {
		this.seedField.updateCursorCounter();
		super.updateScreen();
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		Utils.drawListBackground(23, 32, 0, 0, height, width);
		
		this.drawCenteredString(this.fontRendererObj, this.title,
				this.width / 2, 8, 0xFFFFFF);
		
		this.drawString(this.fontRendererObj, seedText, this.width / 2 - 100,
				this.height / 4 - 10, 0xFFFFFF);
		this.seedField.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		String tooltip = null;
		
		if (Utils.isMouseOverTextBox(mouseX, mouseY, seedField)) {
			tooltip = I18n.format("wdl.gui.generator.seed.description");
		} else if (generatorBtn.isMouseOver()) {
			tooltip = I18n.format("wdl.gui.generator.generator.description");
		} else if (generateStructuresBtn.isMouseOver()) {
			tooltip = I18n.format("wdl.gui.generator.generateStructures.description");
		}
		Utils.drawGuiInfoBox(tooltip, width, height, 48);
	}

	private void cycleGenerator() {
		String prop = WDL.worldProps.getProperty("MapGenerator");
		if (prop.equals("flat")) {
			WDL.worldProps.setProperty("MapGenerator", "default");
			WDL.worldProps.setProperty("GeneratorName", "default");
			WDL.worldProps.setProperty("GeneratorVersion", "1");
		} else if (prop.equals("default")) {
			WDL.worldProps.setProperty("MapGenerator", "largeBiomes");
			WDL.worldProps.setProperty("GeneratorName", "largeBiomes");
			WDL.worldProps.setProperty("GeneratorVersion", "0");
		} else if (prop.equals("largeBiomes")) {
			WDL.worldProps.setProperty("MapGenerator", "amplified");
			WDL.worldProps.setProperty("GeneratorName", "amplified");
			WDL.worldProps.setProperty("GeneratorVersion", "0");
		} else if (prop.equals("amplified")) {
			// Legacy (1.1) world generator
			WDL.worldProps.setProperty("MapGenerator", "legacy");
			WDL.worldProps.setProperty("GeneratorName", "default_1_1");
			WDL.worldProps.setProperty("GeneratorVersion", "0");
		} else {
			WDL.worldProps.setProperty("MapGenerator", "flat");
			WDL.worldProps.setProperty("GeneratorName", "flat");
			WDL.worldProps.setProperty("GeneratorVersion", "0");
		}
		
		this.generatorBtn.displayString = getGeneratorText();
	}

	private void cycleGenerateStructures() {
		if (WDL.worldProps.getProperty("MapFeatures").equals("true")) {
			WDL.worldProps.setProperty("MapFeatures", "false");
		} else {
			WDL.worldProps.setProperty("MapFeatures", "true");
		}
		
		this.generateStructuresBtn.displayString = getGenerateStructuresText();
	}
	
	private String getGeneratorText() {
		return I18n.format("wdl.gui.generator.generator." + 
				WDL.worldProps.getProperty("MapGenerator"));
	}
	
	private String getGenerateStructuresText() {
		return I18n.format("wdl.gui.generator.generateStructures." +
				WDL.worldProps.getProperty("MapFeatures"));
	}
}
