package net.minecraft.client.gui;

import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;

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
       wdl.WDL.injectWDLButtons(this, buttonList);
        /* <<< WDL */
    }

    protected void actionPerformed(GuiButton p_146284_1_)
    {
    	/* WDL >>> */
    	wdl.WDL.handleWDLButtonClick(this, p_146284_1_);
    	/* <<< WDL */
        switch (p_146284_1_.id)
        {
            case 0:
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;

            case 1:
                p_146284_1_.enabled = false;
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
                this.mc.displayGuiScreen(new GuiAchievements(this, this.mc.thePlayer.getStatFileWriter()));
                break;

            case 6:
                this.mc.displayGuiScreen(new GuiStats(this, this.mc.thePlayer.getStatFileWriter()));
                break;

            case 7:
                this.mc.displayGuiScreen(new GuiShareToLan(this));
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
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, I18n.format("menu.game", new Object[0]), this.width / 2, 40, 16777215);
        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }
}
