package net.minecraft.src;

public class GuiIngameMenu extends GuiScreen
{
    /** Also counts the number of updates, not certain as to why yet. */
    private int updateCounter2 = 0;

    /** Counts the number of screen updates. */
    private int updateCounter = 0;

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        this.updateCounter2 = 0;
        this.controlList.clear();
        byte var1 = -16;
        this.controlList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + var1, StatCollector.translateToLocal("menu.returnToMenu")));

        if (!this.mc.isIntegratedServerRunning())
        {
            ((GuiButton)this.controlList.get(0)).displayString = StatCollector.translateToLocal("menu.disconnect");
        }

        this.controlList.add(new GuiButton(4, this.width / 2 - 100, this.height / 4 + 24 + var1, StatCollector.translateToLocal("menu.returnToGame")));
        this.controlList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + var1, 98, 20, StatCollector.translateToLocal("menu.options")));
        GuiButton var3;
        this.controlList.add(var3 = new GuiButton(7, this.width / 2 + 2, this.height / 4 + 96 + var1, 98, 20, StatCollector.translateToLocal("menu.shareToLan")));
        this.controlList.add(new GuiButton(5, this.width / 2 - 100, this.height / 4 + 48 + var1, 98, 20, StatCollector.translateToLocal("gui.achievements")));
        this.controlList.add(new GuiButton(6, this.width / 2 + 2, this.height / 4 + 48 + var1, 98, 20, StatCollector.translateToLocal("gui.stats")));
        var3.enabled = this.mc.isSingleplayer() && !this.mc.getIntegratedServer().func_71344_c();
        /*WDL>>>*/
        // This code adds the start, stop and options buttons to the menu:
        if( !mc.isIntegratedServerRunning() ) // (If connected to real server)
        {
            GuiButton wdlDownload = new GuiButton(50, width / 2 - 100, height / 4 + 72 + var1, 170, 20, "WDL bug!");
            wdlDownload.displayString = (WDL.downloading ? (WDL.isSavingChunks ? "Still saving..." : "Stop download") : "Download this world");
            controlList.add(wdlDownload);
            wdlDownload.enabled = (!WDL.downloading || (WDL.downloading && !WDL.isSavingChunks));
            GuiButton wdlOptions = new GuiButton(51, width / 2 + 71, height / 4 + 72 + var1, 28, 20, "...");
            controlList.add(wdlOptions);
            wdlOptions.enabled = (!WDL.downloading || (WDL.downloading && !WDL.isSavingChunks));
            ((GuiButton)controlList.get(0)).yPosition = height / 4 + 144 + var1;
            ((GuiButton)controlList.get(2)).yPosition = height / 4 + 120 + var1;
            ((GuiButton)controlList.get(3)).yPosition = height / 4 + 120 + var1;
        }
        /*<<<WDL*/
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        switch (par1GuiButton.id)
        {
            case 0:
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;

            case 1:
                par1GuiButton.enabled = false;
                this.mc.statFileWriter.readStat(StatList.leaveGameStat, 1);
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
                this.mc.displayGuiScreen(new GuiAchievements(this.mc.statFileWriter));
                break;

            case 6:
                this.mc.displayGuiScreen(new GuiStats(this, this.mc.statFileWriter));
                break;

            case 7:
                this.mc.displayGuiScreen(new GuiShareToLan(this));
            /*WDL>>>*/
            case 50:
                if( WDL.downloading == true )
                    WDL.stop();
                else
                    WDL.start();
                
                mc.displayGuiScreen(null);
                mc.setIngameFocus();
                break;
            
            case 51:
                 mc.displayGuiScreen( new GuiWDL( this ) );
                 break;
            /*<<<WDL*/
        }
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        super.updateScreen();
        ++this.updateCounter;
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, "Game menu", this.width / 2, 40, 16777215);
        super.drawScreen(par1, par2, par3);
    }
}
