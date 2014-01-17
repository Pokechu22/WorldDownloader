package net.minecraft.wdl;

import java.io.File;
import java.util.Properties;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.Session;

public class GuiWDLMultiworldSelect extends GuiScreen
{
    private GuiButton cancelBtn;
    private GuiTextField newNameField;
    private boolean newWorld = false;
    private int positionID;
    private float yaw;
    private int thirdPersonViewSave;
    private GuiButton[] buttons;
    private String[] worlds;
    private GuiScreen parent;
    EntityPlayerSP cam;

    public GuiWDLMultiworldSelect(GuiScreen var1)
    {
        this.parent = var1;
        EntityClientPlayerMP var2 = WDL.tp;
        this.cam = new EntityPlayerSP(WDL.mc, WDL.wc, new Session("Camera", "", ""), var2.dimension);
        this.cam.setLocationAndAngles(var2.posX, var2.posY - (double)var2.yOffset, var2.posZ, var2.rotationYaw, 0.0F);
        this.yaw = var2.rotationYaw;
        this.thirdPersonViewSave = WDL.mc.gameSettings.thirdPersonView;
        WDL.mc.gameSettings.thirdPersonView = 0;
        WDL.mc.renderViewEntity = this.cam;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        this.buttonList.clear();
        int var1 = this.width / 2;
        int var2 = this.height / 4;
        int var3 = this.width / 150;

        if (var3 == 0)
        {
            var3 = 1;
        }

        int var4 = this.width / var3 - 5;
        this.cancelBtn = new GuiButton(100, var1 - 100, this.height - 30, "Cancel");
        this.buttonList.add(this.cancelBtn);
        String var5 = WDL.baseProps.getProperty("LinkedWorlds");
        String[] var6 = var5.split("[|]");
        String[] var7 = new String[var6.length];
        int var8 = 0;
        int var9;

        for (var9 = 0; var9 < var6.length; ++var9)
        {
            if (var6[var9].isEmpty())
            {
                var6[var9] = null;
            }
            else
            {
                Properties var10 = WDL.loadWorldProps(var6[var9]);

                if (var10 == null)
                {
                    var6[var9] = null;
                }
                else
                {
                    ++var8;
                    var7[var9] = var10.getProperty("WorldName");
                }
            }
        }

        if (var3 > var8 + 1)
        {
            var3 = var8 + 1;
        }

        var9 = (this.width - var3 * var4) / 2;
        this.worlds = new String[var8];
        this.buttons = new GuiButton[var8 + 1];
        int var12 = 0;
        int var11;

        for (var11 = 0; var11 < var6.length; ++var11)
        {
            if (var6[var11] != null)
            {
                this.worlds[var12] = var6[var11];
                this.buttons[var12] = new GuiButton(var12, var12 % var3 * var4 + var9, this.height - 60 - var12 / var3 * 21, var4, 20, var7[var11]);
                this.buttonList.add(this.buttons[var12]);
                ++var12;
            }
        }

        var11 = this.buttons.length - 1;

        if (!this.newWorld)
        {
            this.buttons[var11] = new GuiButton(var11, var11 % var3 * var4 + var9, this.height - 60 - var11 / var3 * 21, var4, 20, "< New Name >");
            this.buttonList.add(this.buttons[var11]);
        }

        this.newNameField = new GuiTextField(this.fontRendererObj, var11 % var3 * var4 + var9, this.height - 60 - var11 / var3 * 21 + 1, var4, 18);
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton var1)
    {
        if (var1.enabled)
        {
            this.newWorld = false;

            if (var1.id == this.worlds.length)
            {
                this.newWorld = true;
                this.buttonList.remove(this.buttons[this.worlds.length]);
            }
            else if (var1.id == 100)
            {
                this.mc.displayGuiScreen((GuiScreen)null);
                this.mc.setIngameFocus();
            }
            else
            {
                this.worldSelected(this.worlds[var1.id]);
            }
        }
    }

    /**
     * Called when the mouse is clicked.
     */
    protected void mouseClicked(int var1, int var2, int var3)
    {
        super.mouseClicked(var1, var2, var3);

        if (this.newWorld)
        {
            this.newNameField.mouseClicked(var1, var2, var3);
        }
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char var1, int var2)
    {
        super.keyTyped(var1, var2);

        if (this.newNameField.isFocused())
        {
            this.newNameField.textboxKeyTyped(var1, var2);

            if (var2 == 28)
            {
                String var3 = this.newNameField.getText();

                if (var3 != null && !var3.isEmpty())
                {
                    this.worldSelected(this.addMultiworld(var3));
                }
            }
        }
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        this.newNameField.updateCursorCounter();
        super.updateScreen();
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int var1, int var2, float var3)
    {
        drawRect(this.width / 2 - 120, 0, this.width / 2 + 120, this.height / 16 + 25, -1073741824);

        if (this.parent == null)
        {
            this.drawCenteredString(this.fontRendererObj, "World Downloader - Trying To Start Download", this.width / 2, this.height / 16, 16777215);
        }
        else
        {
            this.drawCenteredString(this.fontRendererObj, "World Downloader - Trying To Change Options", this.width / 2, this.height / 16, 16777215);
        }

        this.drawCenteredString(this.fontRendererObj, "Where are you?", this.width / 2, this.height / 16 + 10, 16711680);
        this.cam.prevRotationPitch = this.cam.rotationPitch = 0.0F;
        this.cam.prevRotationYaw = this.cam.rotationYaw = this.yaw;
        float var4 = 0.475F;
        this.cam.lastTickPosY = this.cam.prevPosY = this.cam.posY = WDL.tp.posY;
        this.cam.lastTickPosX = this.cam.prevPosX = this.cam.posX = WDL.tp.posX - (double)var4 * Math.sin((double)this.yaw / 180.0D * Math.PI);
        this.cam.lastTickPosZ = this.cam.prevPosZ = this.cam.posZ = WDL.tp.posZ + (double)var4 * Math.cos((double)this.yaw / 180.0D * Math.PI);
        float var5 = 1.0F;
        this.yaw = (float)((double)this.yaw + (double)var5 * (1.0D + 0.699999988079071D * Math.cos((double)(this.yaw + 45.0F) / 45.0D * Math.PI)));

        if (this.newWorld)
        {
            this.newNameField.drawTextBox();
        }

        super.drawScreen(var1, var2, var3);
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {
        super.onGuiClosed();
        WDL.mc.gameSettings.thirdPersonView = this.thirdPersonViewSave;
        this.mc.renderViewEntity = WDL.tp;
    }

    private void worldSelected(String var1)
    {
        WDL.worldName = var1;
        WDL.isMultiworld = true;
        WDL.propsFound = true;

        if (this.parent == null)
        {
            WDL.start();
            this.mc.displayGuiScreen((GuiScreen)null);
            this.mc.setIngameFocus();
        }
        else
        {
            WDL.worldProps = WDL.loadWorldProps(var1);
            this.mc.displayGuiScreen(new GuiWDL(this.parent));
        }
    }

    private String addMultiworld(String var1)
    {
        String var2 = var1;
        String var3 = "\\/:*?\"<>|";
        char[] var4 = var3.toCharArray();
        int var5 = var4.length;
        int var6;

        for (var6 = 0; var6 < var5; ++var6)
        {
            char var7 = var4[var6];
            var2 = var2.replace(var7, '_');
        }

        (new File(this.mc.mcDataDir, "saves/" + WDL.baseFolderName + " - " + var2)).mkdirs();
        Properties var11 = new Properties(WDL.baseProps);
        var11.setProperty("WorldName", var1);
        String[] var12 = new String[this.worlds.length + 1];

        for (var6 = 0; var6 < this.worlds.length; ++var6)
        {
            var12[var6] = this.worlds[var6];
        }

        var12[var12.length - 1] = var2;
        String var13 = "";
        String[] var14 = var12;
        int var8 = var12.length;

        for (int var9 = 0; var9 < var8; ++var9)
        {
            String var10 = var14[var9];
            var13 = var13 + var10 + "|";
        }

        WDL.baseProps.setProperty("LinkedWorlds", var13);
        WDL.saveProps(var2, var11);
        return var2;
    }
}
