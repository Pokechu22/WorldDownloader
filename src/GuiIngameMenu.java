package net.minecraft.client.gui;

import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;

/* WDL >>> */
import net.minecraft.wdl.GuiWDL;
import net.minecraft.wdl.WDL;
/* <<< WDL */

public class GuiIngameMenu extends GuiScreen
{
    private int field_146445_a;
    private int field_146444_f;
    private static final String __OBFID = "CL_00000703";

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        this.field_146445_a = 0;
        this.buttonList.clear();
        byte var1 = -16;
        boolean var2 = true;
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + var1, I18n.format("menu.returnToMenu", new Object[0])));

        if (!this.mc.isIntegratedServerRunning())
        {
            ((GuiButton)this.buttonList.get(0)).displayString = I18n.format("menu.disconnect", new Object[0]);
        }

        this.buttonList.add(new GuiButton(4, this.width / 2 - 100, this.height / 4 + 24 + var1, I18n.format("menu.returnToGame", new Object[0])));
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + var1, 98, 20, I18n.format("menu.options", new Object[0])));
        GuiButton var3;
        this.buttonList.add(var3 = new GuiButton(7, this.width / 2 + 2, this.height / 4 + 96 + var1, 98, 20, I18n.format("menu.shareToLan", new Object[0])));
        this.buttonList.add(new GuiButton(5, this.width / 2 - 100, this.height / 4 + 48 + var1, 98, 20, I18n.format("gui.achievements", new Object[0])));
        this.buttonList.add(new GuiButton(6, this.width / 2 + 2, this.height / 4 + 48 + var1, 98, 20, I18n.format("gui.stats", new Object[0])));
        var3.enabled = this.mc.isSingleplayer() && !this.mc.getIntegratedServer().getPublic();

        /* WDL >>> */
        // This code adds the start, stop and options buttons to the menu:
        if (!this.mc.isIntegratedServerRunning()) // If connected to a real server
        {
            GuiButton wdlDownload = new GuiButton(50, this.width / 2 - 100, this.height / 4 + 72 + var1, 170, 20, "WDL bug!");
            wdlDownload.displayString = (WDL.downloading ? (WDL.saving ? "Still saving..." : "Stop download") : "Download this world");
            this.buttonList.add(wdlDownload);
            wdlDownload.enabled = (!WDL.downloading || (WDL.downloading && !WDL.saving));
            GuiButton wdlOptions = new GuiButton(51, this.width / 2 + 71, this.height / 4 + 72 + var1, 28, 20, "...");
            this.buttonList.add(wdlOptions);
            wdlOptions.enabled = (!WDL.downloading || (WDL.downloading && !WDL.saving));
            ((GuiButton)this.buttonList.get(0)).field_146129_i = this.height / 4 + 144 + var1;
            ((GuiButton)this.buttonList.get(2)).field_146129_i = this.height / 4 + 120 + var1;
            ((GuiButton)this.buttonList.get(3)).field_146129_i = this.height / 4 + 120 + var1;
        }
        /* <<< WDL */



    }

    protected void actionPerformed(GuiButton p_146284_1_)
    {
        switch (p_146284_1_.id)
        {
            case 0:
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;

            case 1:
                p_146284_1_.enabled = false;
                
                /* WDL >>> */
                WDL.stop();
                /* <<< WDL */
                
                this.mc.theWorld.sendQuittingDisconnectingPacket();
                this.mc.loadWorld((WorldClient)null);
                this.mc.displayGuiScreen(new GuiMainMenu());

            case 2:
            case 3:
            default:
                break;

            case 4:
                this.mc.displayGuiScreen((GuiScreen)null);
                this.mc.setIngameFocus();
                break;

            case 5:
                this.mc.displayGuiScreen(new GuiAchievements(this, this.mc.thePlayer.func_146107_m()));
                break;

            case 6:
                this.mc.displayGuiScreen(new GuiStats(this, this.mc.thePlayer.func_146107_m()));
                break;

            case 7:
                this.mc.displayGuiScreen(new GuiShareToLan(this));


            /* WDL >>> */
                break;
                
            case 50:
                if (WDL.downloading)
                {
                    WDL.stop();
                }
                else
                {
                    WDL.start();
                }

                this.mc.displayGuiScreen((GuiScreen)null);
                this.mc.setIngameFocus();
                break;

            case 51:
                this.mc.displayGuiScreen(new GuiWDL(this));
                break;
            /* <<< WDL */

        }
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        super.updateScreen();
        ++this.field_146444_f;
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Game menu", this.width / 2, 40, 16777215);
        super.drawScreen(par1, par2, par3);
    }
}
