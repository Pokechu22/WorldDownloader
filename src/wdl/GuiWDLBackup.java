package net.minecraft.wdl;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class GuiWDLBackup extends GuiScreen
{
    private String title = "";

    private GuiScreen parent;

    private GuiTextField commandField;
    private GuiButton backupBtn;

    boolean cmdBox = false;

    public GuiWDLBackup(GuiScreen parent)
    {
        this.parent = parent;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        this.buttonList.clear();

        this.title = "Backup Options for " + WDL.baseFolderName.replace('@', ':');

        int w = this.width / 2;
        int h = this.height / 4;

        this.backupBtn = new GuiButton(10, w - 100, h + 105, "Backup: ERROR");
        this.buttonList.add(this.backupBtn);
        this.updateBackup(false);

        this.commandField = new GuiTextField(this.fontRendererObj, w - 98, h + 126, 196, 17);

        this.buttonList.add(new GuiButton(100, w - 100, h + 150, "Done"));
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton var1)
    {
        if (var1.enabled)
        {
            if (var1.id == 10)
            {
                this.updateBackup(true);
            }
            else if (var1.id == 100)
            {
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

        if (this.cmdBox)
        {
            this.commandField.mouseClicked(var1, var2, var3);
        }
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char var1, int var2)
    {
        super.keyTyped(var1, var2);
        this.commandField.textboxKeyTyped(var1, var2);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        this.commandField.updateCursorCounter();
        super.updateScreen();
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int var1, int var2, float var3)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, this.height / 4 - 40, 16777215);
        this.drawString(this.fontRendererObj, "Name:", this.width / 2 - 99, 50, 16777215);

        if (this.cmdBox)
        {
            this.commandField.drawTextBox();
        }

        super.drawScreen(var1, var2, var3);
    }

    public void updateBackup(boolean var1)
    {
        this.cmdBox = false;
        String var2 = WDL.baseProps.getProperty("Backup");

        if (var2 == "off")
        {
            if (var1)
            {
                WDL.baseProps.setProperty("Backup", "folder");
                this.updateBackup(false);
            }
            else
            {
                this.backupBtn.displayString = "Backup: Disabled";
            }
        }
        else if (var2 == "folder")
        {
            if (var1)
            {
                WDL.baseProps.setProperty("Backup", "zip");
                this.updateBackup(false);
            }
            else
            {
                this.backupBtn.displayString = "Backup: Copy World Folder";
            }
        }
        else if (var2 == "zip")
        {
            if (var1)
            {
                WDL.baseProps.setProperty("Backup", "command");
                this.updateBackup(false);
            }
            else
            {
                this.backupBtn.displayString = "Backup: Zip World Folder";
            }
        }
        else if (var2 == "command")
        {
            if (var1)
            {
                WDL.baseProps.setProperty("Backup", "off");
                this.updateBackup(false);
            }
            else
            {
                this.backupBtn.displayString = "Backup: Run the following command";
                this.cmdBox = true;
            }
        }
        else
        {
            WDL.baseProps.setProperty("Backup", "off");
            this.updateBackup(false);
        }
    }
}
