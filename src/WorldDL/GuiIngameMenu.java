// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

package net.minecraft.src;

import java.io.File;
import java.util.List;
import net.minecraft.client.Minecraft;

// Referenced classes of package net.minecraft.src:
//            GuiScreen, GuiButton, StatCollector, GuiOptions, 
//            StatList, StatFileWriter, World, GuiMainMenu, 
//            GuiAchievements, GuiStats, MathHelper

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
            if( ((WorldClient)mc.theWorld).downloadThisWorld == false )
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
            mc.statFileWriter.readStat(StatList.leaveGameStat, 1);
            if(mc.isMultiplayerWorld())
            {
    			if( ((WorldClient)mc.theWorld).downloadThisWorld == true )
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
            mc.displayGuiScreen(new GuiAchievements(mc.statFileWriter));
        }
        if(guibutton.id == 6)
        {
            mc.displayGuiScreen(new GuiStats(this, mc.statFileWriter));
        }
        if(guibutton.id == 7)
        {
			if( ((WorldClient)mc.theWorld).downloadThisWorld == true )
			{
				((GuiButton)controlList.get(1)).displayString = "Saving a shitload of data...";
				stopDownloadIn = 2;
			}
			else
			{
				startDownload();
				mc.displayGuiScreen(null);
				mc.setIngameFocus();
			}
        }

    }

    private void startDownload()
    {
    	String worldName = mc.gameSettings.lastServer;
    	if( worldName.isEmpty() ) worldName = "Downloaded World";
    	
    	WorldClient wc = (WorldClient)mc.theWorld;
    	
    	wc.worldInfo.setWorldName(worldName);
		wc.downloadSaveHandler = (SaveHandler) mc.getSaveLoader().getSaveLoader(worldName, false); // false = don't generate "Players" dir
		wc.downloadChunkLoader = wc.downloadSaveHandler.getChunkLoader(wc.worldProvider);
		wc.worldInfo.setSizeOnDisk( getFileSizeRecursive(wc.downloadSaveHandler.getSaveDirectory()) );
		Chunk.wc = wc;
		((ChunkProviderClient) wc.chunkProvider).importOldTileEntities();
		wc.downloadThisWorld = true;
		
		mc.ingameGUI.addChatMessage("§c[WorldDL] §cDownloading everything you can see...");
		mc.ingameGUI.addChatMessage("§c[WorldDL] §6You can increase that area by travelling around.");
    }
    
    private void stopDownload()
    {
    	WorldClient wc = (WorldClient)mc.theWorld;
		wc.saveWorld(true, null);
		
		wc.downloadThisWorld = false;
		
		wc.downloadChunkLoader = null;
		wc.downloadSaveHandler = null;
		
		mc.ingameGUI.addChatMessage("§c[WorldDL] §cDownload stopped.");
    }

    private long getFileSizeRecursive(File f)
    {
    	long size = 0;
    	File[] list = f.listFiles();
    	for(File nf : list)
    	{
    		if( nf.isDirectory() )
    			size += getFileSizeRecursive(nf);
    		else if( nf.isFile() )
    			size += nf.length();
    	}
    	return size;
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
        if( stopDownloadIn == 0 )
        {
        	stopDownload();
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
        }
        else if( stopDownloadIn > 0 )
        	stopDownloadIn--;
    }

    private int updateCounter2;
    private int updateCounter;
    private int stopDownloadIn = -1;
}
