package net.minecraft.wdl;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class GuiWDLGenerator extends GuiScreen
{
    private String title = "";
    private GuiScreen parent;
    private GuiTextField seedField;
    private GuiButton generatorBtn;
    private GuiButton generateStructuresBtn;

    public GuiWDLGenerator(GuiScreen var1)
    {
        this.parent = var1;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        this.buttonList.clear();
        this.title = "World Generator Options for " + WDL.baseFolderName.replace('@', ':');
        int var1 = this.width / 2;
        int var2 = this.height / 4;
        int var3 = var2 - 15;
        this.seedField = new GuiTextField(this.fontRendererObj, this.width / 2 - 70, var3, 168, 18);
        this.seedField.setText("ERROR");
        this.updateSeed(false);
        var3 += 22;
        this.generatorBtn = new GuiButton(1, var1 - 100, var3, "World Generator: ERROR");
        this.buttonList.add(this.generatorBtn);
        this.updateGenerator(false);
        var3 += 22;
        this.generateStructuresBtn = new GuiButton(2, var1 - 100, var3, "Generate Structures: ERROR");
        this.buttonList.add(this.generateStructuresBtn);
        this.updateGenerateStructures(false);
        this.buttonList.add(new GuiButton(100, var1 - 100, var2 + 150, "Done"));
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton var1)
    {
        if (var1.enabled)
        {
            if (var1.id == 1)
            {
                this.updateGenerator(true);
            }
            else if (var1.id == 2)
            {
                this.updateGenerateStructures(true);
            }
            else if (var1.id == 100)
            {
                this.updateSeed(true);
                WDL.saveProps();
                this.mc.displayGuiScreen(this.parent);
            }
        }
    }

    /**
     * Called when the mouse is clicked.
     */
    protected void mouseClicked(int var1, int var2, int var3)
    {
        super.mouseClicked(var1, var2, var3);
        this.seedField.mouseClicked(var1, var2, var3);
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char var1, int var2)
    {
        super.keyTyped(var1, var2);
        this.seedField.textboxKeyTyped(var1, var2);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        this.seedField.updateCursorCounter();
        super.updateScreen();
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int var1, int var2, float var3)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, this.height / 4 - 40, 16777215);
        this.drawString(this.fontRendererObj, "Seed:", this.width / 2 - 99, this.height / 4 - 10, 16777215);
        this.seedField.drawTextBox();
        super.drawScreen(var1, var2, var3);
    }

    private void updateGenerator(boolean var1)
    {
        String var2 = WDL.worldProps.getProperty("GeneratorName");

        if (var2.equals("default"))
        {
            if (var1)
            {
                WDL.worldProps.setProperty("GeneratorName", "flat");
                WDL.worldProps.setProperty("GeneratorVersion", "0");
                this.updateGenerator(false);
            }
            else
            {
                this.generatorBtn.displayString = "World Generator: Default";
            }
        }
        else if (var1)
        {
            WDL.worldProps.setProperty("GeneratorName", "default");
            WDL.worldProps.setProperty("GeneratorVersion", "1");
            this.updateGenerator(false);
        }
        else
        {
            this.generatorBtn.displayString = "World Generator: Flat";
        }
    }

    private void updateGenerateStructures(boolean var1)
    {
        String var2 = WDL.worldProps.getProperty("MapFeatures");

        if (var2.equals("true"))
        {
            if (var1)
            {
                WDL.worldProps.setProperty("MapFeatures", "false");
                this.updateGenerateStructures(false);
            }
            else
            {
                this.generateStructuresBtn.displayString = "Generate Structures: ON";
            }
        }
        else if (var1)
        {
            WDL.worldProps.setProperty("MapFeatures", "true");
            this.updateGenerateStructures(false);
        }
        else
        {
            this.generateStructuresBtn.displayString = "Generate Structures: OFF";
        }
    }

    private void updateSeed(boolean var1)
    {
        if (var1)
        {
            WDL.worldProps.setProperty("RandomSeed", this.seedField.getText());
        }
        else
        {
            this.seedField.setText(WDL.worldProps.getProperty("RandomSeed"));
        }
    }
}
