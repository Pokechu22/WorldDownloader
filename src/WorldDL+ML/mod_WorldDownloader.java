package net.minecraft.src;

import java.io.File;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.src.ModLoader;

public class mod_WorldDownloader extends BaseMod
{
	@MLProp(name="Key", info="Keycode that starts and stops the download")
	public int key = 38; //Key_L // http://www.lwjgl.org/javadoc/constant-values.html#org.lwjgl.input.Keyboard.CHAR_NONE
	
	Minecraft mc;
	WorldClient wc;
	int keyDownTimer = 0;
	KeyBinding kb = new KeyBinding("key.downloadWorld", key);
	boolean stopDownloadNextFrame = false;
	
	public mod_WorldDownloader() {
		mc = ModLoader.getMinecraftInstance();
		ModLoader.RegisterKey(this, kb, true);
		ModLoader.SetInGUIHook(this, true, false);
	}
	
	public void KeyboardEvent(KeyBinding kb) {
		if( !mc.theWorld.multiplayerWorld )
			return;
		
		if(keyDownTimer <= 0)
		{
			wc = (WorldClient)mc.theWorld;
			keyDownTimer = 10;
			ModLoader.SetInGameHook(this, true, true);
			if(wc.downloadThisWorld)
			{
				stopDownloadNextFrame = true;
				if( mc.currentScreen instanceof GuiIngameMenu )
				{
					GuiIngameMenu igm = (GuiIngameMenu)mc.currentScreen;
					String saving = "Saving a shitload of data...";
					igm.drawString(igm.fontRenderer, saving, igm.width-igm.fontRenderer.getStringWidth(saving)-5, igm.height-25, 0xCCCC00);
				}
				else
				{
			    	mc.ingameGUI.addChatMessage("§c[WorldDL] §6Saving a shitload of data...");
					ModLoader.SetInGameHook(this, true, true);
				}
			}
			else
			{
				startDownload();
			}
		}
	}
	
	public void OnTickInGame(Minecraft minecraft)
	{
		if(stopDownloadNextFrame)
		{
			stopDownload();
			stopDownloadNextFrame = false;
			return;
		}
		if(keyDownTimer <= 0)
			ModLoader.SetInGameHook(this, false, true);
		keyDownTimer--;
		return;
	}
	
	public void OnTickInGUI(Minecraft minecraft, GuiScreen gs)
	{
		if( gs instanceof GuiIngameMenu && mc.theWorld.multiplayerWorld )
		{
			GuiIngameMenu igm = (GuiIngameMenu) gs;
			String info;
			if( ((WorldClient)mc.theWorld).downloadThisWorld )
				info = "Downloading -- To stop press " + Keyboard.getKeyName(key);
			else
				info = "To download the world press " + Keyboard.getKeyName(key);
			igm.drawString(igm.fontRenderer, info, igm.width-igm.fontRenderer.getStringWidth(info)-5, igm.height-15, 0xCC0000);
			if(stopDownloadNextFrame)
			{
				stopDownload();
				stopDownloadNextFrame = false;
			}
		}
	}
	
	public String Version()
	{
		return "1.6.6";
	}

    private void startDownload()
    {
    	String worldName = mc.gameSettings.lastServer;
    	if( worldName.isEmpty() ) worldName = "Downloaded world";
    	
    	wc = (WorldClient)mc.theWorld;
    	
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

}
