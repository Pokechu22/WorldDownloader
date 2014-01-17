package net.minecraft.wdl;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiWDLMultiworld extends GuiScreen
{
    private GuiScreen parent;
    private GuiButton multiworldEnabledBtn;
    boolean newMultiworldState = false;

    public GuiWDLMultiworld(GuiScreen var1)
    {
        this.parent = var1;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        this.buttonList.clear();
        int var1 = this.width / 2;
        int var2 = this.height / 4;
        int var3 = var2 + 115;
        this.multiworldEnabledBtn = new GuiButton(1, var1 - 100, var3, "Multiworld support: ERROR");
        this.buttonList.add(this.multiworldEnabledBtn);
        this.updateMultiworldEnabled(false);
        this.buttonList.add(new GuiButton(100, var1 - 100, var2 + 150, "OK"));
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
                this.updateMultiworldEnabled(true);
            }
            else if (var1.id == 100)
            {
                if (this.newMultiworldState)
                {
                    this.mc.displayGuiScreen(new GuiWDLMultiworldSelect(this.parent));
                }
                else
                {
                    WDL.baseProps.setProperty("LinkedWorlds", "");
                    WDL.saveProps();
                    WDL.propsFound = true;

                    if (this.parent != null)
                    {
                        this.mc.displayGuiScreen(new GuiWDL(this.parent));
                    }
                    else
                    {
                        WDL.start();
                        this.mc.displayGuiScreen((GuiScreen)null);
                        this.mc.setIngameFocus();
                    }
                }
            }
        }
    }

    /**
     * Called when the mouse is clicked.
     */
    protected void mouseClicked(int var1, int var2, int var3)
    {
        super.mouseClicked(var1, var2, var3);
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char var1, int var2)
    {
        super.keyTyped(var1, var2);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        super.updateScreen();
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int var1, int var2, float var3)
    {
        this.drawDefaultBackground();
        drawRect(this.width / 2 - 160, this.height / 4 - 60, this.width / 2 + 160, this.height / 4 + 180, -1342177280);
        this.drawCenteredString(this.fontRendererObj, "Multiworld Support", this.width / 2, this.height / 4 - 40, 16711680);
        this.drawString(this.fontRendererObj, "Multiworld support is required if at least one of the", this.width / 2 - 150, this.height / 4 - 15, 16777215);
        this.drawString(this.fontRendererObj, " following conditions is met:", this.width / 2 - 150, this.height / 4 - 5, 16777215);
        this.drawString(this.fontRendererObj, "- \"Multiworld\" is mentioned on the server\'s website", this.width / 2 - 150, this.height / 4 + 15, 16777215);
        this.drawString(this.fontRendererObj, "- The server has more than 3 dimensions (or worlds)", this.width / 2 - 150, this.height / 4 + 35, 16777215);
        this.drawString(this.fontRendererObj, "- The server has other dimensions than the official ones", this.width / 2 - 150, this.height / 4 + 55, 16777215);
        this.drawString(this.fontRendererObj, "   (Earth, Nether, The End)", this.width / 2 - 150, this.height / 4 + 65, 16777215);
        drawRect(this.width / 2 - 102, this.height / 4 + 113, this.width / 2 + 102, this.height / 4 + 137, -65536);
        super.drawScreen(var1, var2, var3);
    }

    private void updateMultiworldEnabled(boolean var1)
    {
        if (!this.newMultiworldState)
        {
            if (var1)
            {
                this.newMultiworldState = true;
                this.updateMultiworldEnabled(false);
            }
            else
            {
                this.multiworldEnabledBtn.displayString = "Multiworld support: Disabled";
            }
        }
        else if (var1)
        {
            this.newMultiworldState = false;
            this.updateMultiworldEnabled(false);
        }
        else
        {
            this.multiworldEnabledBtn.displayString = "Multiworld support: Enabled";
        }
    }
}
