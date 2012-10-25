package net.minecraft.src;

public class GuiIngameMenu extends GuiScreen
{
    /** Also counts the number of updates, not certain as to why yet. */
    private int updateCounter2 = 0;

    /** Counts the number of screen updates. */
    private int updateCounter = 0;

    /* WORLD DOWNLOADER ---> */
    private int stopDownloadIn = -1;
    private int disconnectIn = -1;
    /* <--- WORLD DOWNLOADER */
    
    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    /* WORLD DOWNLOADER ---> */
    public void initGui()
    {
        this.updateCounter2 = 0;
        this.controlList.clear();
        byte byte0 = -16;

        int leftcolumn = this.width / 2 - 100;
        int rightcolumn = this.width / 2 + 2;
        int row1 = this.height / 4 + 24 + byte0;
        int row2 = row1 + 24;
        int row3 = row2 + 24;
        int row4 = row3 + 24;
        int row5 = row4 + 24;
        int row6 = row5 + 24;


        this.controlList.add(new GuiButton(1, leftcolumn, row5, StatCollector.translateToLocal("menu.returnToMenu")));

        if (!this.mc.isIntegratedServerRunning())
        {
            ((GuiButton)this.controlList.get(0)).displayString = StatCollector.translateToLocal("menu.disconnect");

            /* WORLD DOWNLOADER ---> */
            if( WorldDL.downloading == false )
                controlList.add(new GuiButton(8, leftcolumn, row6, "Download this world"));
            else
                controlList.add(new GuiButton(8, leftcolumn, row6, "Stop download"));
            /* <--- WORLD DOWNLOADER */
        }

        this.controlList.add(new GuiButton(4, leftcolumn, row1, StatCollector.translateToLocal("menu.returnToGame")));

        // ROW
        this.controlList.add(new GuiButton(0, leftcolumn, row4, 98, 20, StatCollector.translateToLocal("menu.options")));
        GuiButton var3;
        this.controlList.add(var3 = new GuiButton(7, rightcolumn, row4, 98, 20, StatCollector.translateToLocal("menu.shareToLan")));

        this.controlList.add(new GuiButton(5, leftcolumn, row2, 98, 20, StatCollector.translateToLocal("gui.achievements")));
        this.controlList.add(new GuiButton(6, rightcolumn, row2, 98, 20, StatCollector.translateToLocal("gui.stats")));
        var3.enabled = this.mc.isSingleplayer() && !this.mc.getIntegratedServer().getPublic();
    }
    /* <--- WORLD DOWNLOADER */

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

                /* WORLD DOWNLOADER ---> */
                if( WorldDL.downloading == true )
                {
                        ((GuiButton)this.controlList.get(1)).displayString = "Saving world data...";
                    WorldDL.stopDownload();
                    disconnectIn = 100;
                }
                else
                {
                        this.mc.theWorld.sendQuittingDisconnectingPacket();
                        this.mc.loadWorld((WorldClient)null);
                        this.mc.displayGuiScreen(new GuiMainMenu());
                }
                break;
                /* <--- WORLD DOWNLOADER */
            case 2:
            case 3:
            default:
                break;

            case 4:
                this.mc.displayGuiScreen((GuiScreen)null);
                this.mc.setIngameFocus();
                this.mc.sndManager.func_82461_f();
                break;

            case 5:
                this.mc.displayGuiScreen(new GuiAchievements(this.mc.statFileWriter));
                break;

            case 6:
                this.mc.displayGuiScreen(new GuiStats(this, this.mc.statFileWriter));
                break;

            case 7:
                this.mc.displayGuiScreen(new GuiShareToLan(this));
                /* WORLD DOWNLOADER ---> */
                break;
            case 8:
                WorldDL.mc = this.mc;
                WorldDL.wc = (WorldClient)this.mc.theWorld;
                if( WorldDL.downloading == true )
                {
                        ((GuiButton)this.controlList.get(1)).displayString = "Saving world data...";
                    stopDownloadIn = 100;
                }
                else
                {
                    WorldDL.startDownload();
                    this.mc.displayGuiScreen(null);
                    this.mc.setIngameFocus();
                }
                break;
            /* <--- WORLD DOWNLOADER */
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
        /* WORLD DOWNLOADER ---> */
        if( stopDownloadIn == 0 )
        {
            WorldDL.stopDownload();
            this.mc.displayGuiScreen(null);
            this.mc.setIngameFocus();
        }
        else if( stopDownloadIn > 0 )
            stopDownloadIn--;

        if( disconnectIn == 0)
        {
                this.mc.theWorld.sendQuittingDisconnectingPacket();
            this.mc.loadWorld((WorldClient)null);
            this.mc.displayGuiScreen(new GuiMainMenu());
        }
        else if(disconnectIn > 0)
                disconnectIn--;
        /* <--- WORLD DOWNLOADER */
    }
}
