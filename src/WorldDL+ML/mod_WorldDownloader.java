package net.minecraft.src;

import java.io.File;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.src.ModLoader;

public class mod_WorldDownloader extends BaseMod
{
	//@MLProp(name="Key", info="Keycode that starts and stops the download. http://goo.gl/S9Q2W")
	public static int key = 38; //Key_L // http://www.lwjgl.org/javadoc/constant-values.html#org.lwjgl.input.Keyboard.CHAR_NONE
	

	
    
	int keyDownTimer = 0; // Counts down every tick from 10 after L was pressed. Stops rapid button activation.
	KeyBinding kb = new KeyBinding("key.downloadWorld", key);
	boolean stopDownloadNextFrame = false;
	
	public mod_WorldDownloader() {
		WorldDL.mc = ModLoader.getMinecraftInstance();
	}
	
	public void ModsLoaded() {
		ModLoader.RegisterKey(this, kb, true); // Keys in options.txt overwrite the L key!
		ModLoader.SetInGUIHook(this, true, false); // To draw the text in the lower right corner
	}
	
	public void KeyboardEvent(KeyBinding kb) {
		if( !WorldDL.mc.theWorld.multiplayerWorld )
			return;
		
		if( !(WorldDL.mc.currentScreen instanceof GuiIngameMenu) && WorldDL.mc.currentScreen != null )
			return; // Only start or stop if in game menu or in game.
		
		if(keyDownTimer <= 0) // prevent rapid starts/stops
		{
			WorldDL.wc = (WorldClient)WorldDL.mc.theWorld;
			keyDownTimer = 10;
			ModLoader.SetInGameHook(this, true, true);
			if(WorldDL.downloading) // if downloading...
			{
				stopDownloadNextFrame = true; // First draw the stop text, then stop in the next frame
				
				if( WorldDL.mc.currentScreen instanceof GuiIngameMenu ) // Draw it on screen
				{
					GuiIngameMenu igm = (GuiIngameMenu)WorldDL.mc.currentScreen;
					String saving = "Saving a shitload of data...";
					igm.drawString(igm.fontRenderer, saving, igm.width-igm.fontRenderer.getStringWidth(saving)-5, igm.height-25, 0xCCCC00);
				}
				else // or add it to the chat
				{
			    	WorldDL.mc.ingameGUI.addChatMessage("§c[WorldDL] §6Saving a shitload of data...");
					ModLoader.SetInGameHook(this, true, true);
				}
			}
			else // if not downloading...
			{
				WorldDL.startDownload();
			}
		}
	}
	
	public boolean OnTickInGame(Minecraft minecraft)
	{
		if(stopDownloadNextFrame)
		{
			WorldDL.stopDownload();
			stopDownloadNextFrame = false;
			return true;
		}
		
		if(keyDownTimer <= 0)
			ModLoader.SetInGameHook(this, false, true);
		keyDownTimer--;
		return true;
	}
	
	public boolean OnTickInGUI(Minecraft minecraft, GuiScreen gs) // Draws the text in the lower right corner
	{
		if( gs instanceof GuiIngameMenu && WorldDL.mc.theWorld.multiplayerWorld ) // only in the game menu in SMP
		{
			GuiIngameMenu igm = (GuiIngameMenu) gs;
			String info;
			
			if( WorldDL.downloading )
				info = "Downloading -- To stop press " + Keyboard.getKeyName(key);
			else
				info = "To download the world press " + Keyboard.getKeyName(key);
			igm.drawString(igm.fontRenderer, info, igm.width-igm.fontRenderer.getStringWidth(info)-5, igm.height-15, 0xCC0000);
			
			if(stopDownloadNextFrame)
			{
				WorldDL.stopDownload();
				stopDownloadNextFrame = false;
			}
		}
		return true;
	}
	
	/* DON'T FORGET TO UPDATE THE VERSION !!! */
	public String Version()
	{
		return "1.8.1";
	}

}
