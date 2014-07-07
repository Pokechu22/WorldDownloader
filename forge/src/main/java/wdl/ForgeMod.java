package wdl;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

// TODO: Use the Forge mod proxy stuff to avoid performance impact in singleplayer.

@Mod(modid = ForgeMod.MODID, version = ForgeMod.VERSION)
public class ForgeMod
{
    public static final String MODID = "WDL";
    public static final String VERSION = "1.7.10";
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	FMLCommonHandler.instance().bus().register(this);
    	MinecraftForge.EVENT_BUS.register(this);
    }
	
    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event)
    {
    	if(event.world != WDL.wc)
    	{
    		WDL.onWorldLoad();
    	}
    }
    
    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event)
    {
    	if(WDL.downloading && event.world.isRemote)
    	{
    		WDL.onChunkNoLongerNeeded(event.getChunk());
    	}
    }
    
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
    	if(event.world.isRemote)
    	{
    		//WDL.onWorldLoad(); //Does NOT work! The player is not yet initialized here.
    		event.world.addWorldAccess(new WDLWorldAccess());
    	}
    }
    
    /*
    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
    	if(event.world.isRemote)
    	{
    		// not used
    		//WDL.onWorldUnload();
    	}
    }
    */
    
    @SubscribeEvent
    public void onGuiSwitch(GuiOpenEvent event)
    {
    	
    	if(WDL.downloading)
    	{
    		if(WDL.tp.openContainer != WDL.windowContainer)
    		{
    			if(WDL.tp.openContainer == WDL.tp.inventoryContainer)
    			{
    				WDL.onItemGuiClosed();
    			}
    			WDL.windowContainer = WDL.tp.openContainer;
    		}
    	}
    	
    }
    
    @SubscribeEvent
    public void onNoteBlock(NoteBlockEvent event)
    {
    	if(WDL.downloading && event.world.isRemote)
    	{
    		WDL.onBlockEvent(event.x, event.y, event.z, event.block, 0, event.getVanillaNoteId());
    	}
    }
    
    @SubscribeEvent
    public void onDisconnect(ClientDisconnectionFromServerEvent event)
    {
    	WDL.stop();
    }
    
    /*
    @SubscribeEvent
    public void onConnect(ClientConnectedToServerEvent event)
    {
    	// mc.theWorld is still null!
    }
    */
    
    @SubscribeEvent
    public void onGuiDrawn(InitGuiEvent.Post event)
    {
    	if(!WDL.mc.isIntegratedServerRunning() && event.gui instanceof GuiIngameMenu)
    	{
    		injectWDLButtons((GuiIngameMenu)event.gui, event.buttonList);
    	}
    	
    	if(WDL.downloading)
    	{
    		if(WDL.tp.openContainer != WDL.windowContainer)
    		{
    			if(WDL.tp.openContainer != WDL.tp.inventoryContainer)
    			{
    				WDL.onItemGuiOpened();
    			}
    			WDL.windowContainer = WDL.tp.openContainer;
    		}
    	}
    }
    
    @SubscribeEvent
    public void onGuiButtonClicked(ActionPerformedEvent.Pre event)
    {
    	if(!WDL.mc.isIntegratedServerRunning() && event.gui instanceof GuiIngameMenu)
    	{
    		handleWDLButtonClick((GuiIngameMenu)event.gui, event.button);
    	}
    }
    
    @SubscribeEvent
    public void onChatMessage(ClientChatReceivedEvent event)
    {
    	WDL.handleServerSeedMessage(event.message.getFormattedText());
    }
    
    
    // Add World Downloader buttons to GuiIngameMenu
    private void injectWDLButtons(GuiIngameMenu gui, List buttonList)
    {
    	int insertAtYPos = 0;
    	for( Object obj : buttonList)
    	{
    		GuiButton btn = (GuiButton)obj;
    		if(btn.id == 5) // Button "Achievements"
    		{
    			insertAtYPos = btn.yPosition + btn.height + 4;
    			break;
    		}
    	}
    	
    	// Move other buttons down one slot (= 24 height units)
    	for( Object obj : buttonList)
    	{
    		GuiButton btn = (GuiButton)obj;
    		if(btn.yPosition >= insertAtYPos)
    		{
    			btn.yPosition += 24;
    		}
    	}
    	
    	// Insert buttons... The IDs are chosen to be unique (hopefully). They are ASCII encoded strings: "WDLs" and "WDLo"
    	GuiButton wdlDownload = new GuiButton(0x57444C73, gui.width / 2 - 100, insertAtYPos, 170, 20, "WDL bug!");
        GuiButton wdlOptions = new GuiButton(0x57444C6F, gui.width / 2 + 71, insertAtYPos, 28, 20, "...");
    	
        wdlDownload.displayString = (WDL.downloading ? (WDL.saving ? "Still saving..." : "Stop download") : "Download this world");
        wdlDownload.enabled = (!WDL.downloading || (WDL.downloading && !WDL.saving));

        wdlOptions.enabled = (!WDL.downloading || (WDL.downloading && !WDL.saving));
        
        buttonList.add(wdlDownload);
        buttonList.add(wdlOptions);
    }
    
    private void handleWDLButtonClick(GuiIngameMenu gui, GuiButton button)
    {
    	if(button.id == 0x57444C73) // "Start/Stop Download"
    	{
            if (WDL.downloading)
            {
                WDL.stop();
                WDL.mc.displayGuiScreen((GuiScreen)null);
                WDL.mc.setIngameFocus();
            }
            else
            {
                WDL.start();
            }
    	}
    	else if( button.id == 0x57444C6F) // "..." (options)
    	{
    		WDL.mc.displayGuiScreen(new GuiWDL(gui));
    	}
    	else if( button.id == 1) // "Disconnect"
    	{
    		WDL.stop();
    	}
    }
}
