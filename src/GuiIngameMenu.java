// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

package net.minecraft.src;

import java.util.List;
import net.minecraft.client.Minecraft;

// Referenced classes of package net.minecraft.src:
//            GuiScreen, GuiOptions, StatCollector, GuiStats, 
//            World, GuiMainMenu, MathHelper, StatList, 
//            GuiButton, StatFileWriter, GuiAchievements

public class GuiIngameMenu extends GuiScreen
{

    public GuiIngameMenu()
    {
        updateCounter2 = 0;
        updateCounter = 0;
    }

    public void initGui()
    {
        updateCounter2 = 0;
        controlList.clear();
        byte byte0 = -16;
        controlList.add(new GuiButton(1, width / 2 - 100, height / 4 + 120 + byte0, "Save and quit to title"));
        if(mc.isMultiplayerWorld())
        {
            ((GuiButton)controlList.get(0)).displayString = "Disconnect";
            ((GuiButton)controlList.get(0)).yPosition = height / 4 + 144 + byte0;
            if( mc.theWorld.downloadThisWorld == false )
            	controlList.add(new GuiButton(7, width / 2 - 100, height / 4 + 120 + byte0, "Download this world"));
            else
            	controlList.add(new GuiButton(7, width / 2 - 100, height / 4 + 120 + byte0, "Stop downloading this world"));
        }
        controlList.add(new GuiButton(4, width / 2 - 100, height / 4 + 24 + byte0, "Back to game"));
        controlList.add(new GuiButton(0, width / 2 - 100, height / 4 + 96 + byte0, "Options..."));
        controlList.add(new GuiButton(5, width / 2 - 100, height / 4 + 48 + byte0, 98, 20, StatCollector.translateToLocal("gui.achievements")));
        controlList.add(new GuiButton(6, width / 2 + 2, height / 4 + 48 + byte0, 98, 20, StatCollector.translateToLocal("gui.stats")));
    }

    protected void actionPerformed(GuiButton guibutton)
    {
        if(guibutton.id == 0)
        {
            mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
        }
        if(guibutton.id == 1)
        {
            mc.field_25001_G.func_25100_a(StatList.leaveGameStat, 1);
            if(mc.isMultiplayerWorld())
            {
    			if( mc.theWorld.downloadThisWorld == true )
    				stopDownload();
                mc.theWorld.sendQuittingDisconnectingPacket();
            }
            mc.changeWorld1(null);
            mc.displayGuiScreen(new GuiMainMenu());
        }
        if(guibutton.id == 4)
        {
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        }
        if(guibutton.id == 5)
        {
            mc.displayGuiScreen(new GuiAchievements(mc.field_25001_G));
        }
        if(guibutton.id == 6)
        {
            mc.displayGuiScreen(new GuiStats(this, mc.field_25001_G));
        }
        if(guibutton.id == 7)
        {
			if( mc.theWorld.downloadThisWorld == true )
				stopDownload();
			else
				startDownload();
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        }
    }
    
    private void startDownload()
    {
    	String worldName = mc.gameSettings.lastServer;
    	if( worldName.isEmpty() ) worldName = "Downloaded World";
    	
    	mc.theWorld.worldInfo.setWorldName(worldName);
		mc.theWorld.downloadSaveHandler = (SaveHandler) mc.getSaveLoader().getSaveLoader(worldName, true); // true = generate Players dir
		mc.theWorld.downloadChunkLoader = mc.theWorld.downloadSaveHandler.getChunkLoader(null); // null = normal world
		
		mc.theWorld.downloadThisWorld = true;
		
		mc.ingameGUI.addChatMessage("§cEverything you see will be downloaded.");
		mc.ingameGUI.addChatMessage("§cTravel around to cover more of this beautiful world!");
    }
    
    private void stopDownload()
    {
		mc.theWorld.saveWorld(true, null);
		
		mc.theWorld.downloadThisWorld = false;
		
		mc.theWorld.downloadChunkLoader = null;
		mc.theWorld.downloadSaveHandler = null;
		
		mc.ingameGUI.addChatMessage("§cDownload stopped.");
    }

    public void updateScreen()
    {
        super.updateScreen();
        updateCounter++;
    }

    public void drawScreen(int i, int j, float f)
    {
        drawDefaultBackground();
        boolean flag = !mc.theWorld.func_650_a(updateCounter2++);
        if(flag || updateCounter < 20)
        {
            float f1 = ((float)(updateCounter % 10) + f) / 10F;
            f1 = MathHelper.sin(f1 * 3.141593F * 2.0F) * 0.2F + 0.8F;
            int k = (int)(255F * f1);
            drawString(fontRenderer, "Saving level..", 8, height - 16, k << 16 | k << 8 | k);
        }
        drawCenteredString(fontRenderer, "Game menu", width / 2, 40, 0xffffff);
        super.drawScreen(i, j, f);
    }

    private int updateCounter2;
    private int updateCounter;
}
