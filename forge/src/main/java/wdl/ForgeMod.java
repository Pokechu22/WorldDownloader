package wdl;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@Mod( modid = ForgeMod.MODID, version = ForgeMod.VERSION )
public class ForgeMod
{
    public static final String MODID = "WDL";
    public static final String VERSION = "1.7.10";
    
	private boolean debug = true;
	Minecraft mc;
    
    @EventHandler
    public void init( FMLInitializationEvent event )
    {
    	MinecraftForge.EVENT_BUS.register( this );
    	mc = Minecraft.getMinecraft();
    }
    
	
    @SubscribeEvent
    public void onChunkLoad( ChunkEvent.Load event )
    {
    	if( event.world.isRemote )
    	{
    		chatDebug("Loaded chunk: " + event.getChunk().xPosition + " " + event.getChunk().zPosition );
    	}
    }
    
    @SubscribeEvent
    public void onChunkUnload( ChunkEvent.Unload event )
    {
    	if( event.world.isRemote )
    	{
    		chatDebug("Unloaded chunk: " + event.getChunk().xPosition + " " + event.getChunk().zPosition );
    	}
    }
    
    @SubscribeEvent
    public void onWorldLoad( WorldEvent.Load event )
    {
    	if( event.world.isRemote )
    	{
    		chatDebug("Loaded world: " + event.world.toString() );
    	}
    }
    
    @SubscribeEvent
    public void onWorldUnload( WorldEvent.Unload event )
    {
    	if( event.world.isRemote )
    	{
    		chatDebug("Unloaded world: " + event.world.toString() );
    	}
    }
    
    @SubscribeEvent
    public void onGuiDrawn( InitGuiEvent.Post event )
    {
    	chatDebug("GUI initialized: " + event.gui );
    }
    
    @SubscribeEvent
    public void onGuiSwitch( GuiOpenEvent event )
    {
    	chatDebug("GUI switched: " + event.gui );
    }
    
    @SubscribeEvent
    public void onGuiButtonClicked( ActionPerformedEvent.Pre event)
    {
    	chatDebug("Button clicked: " + event.button );
    }
    
    @SubscribeEvent
    public void onChatMessage( ClientChatReceivedEvent event )
    {
    	// Parse seed if found
    }
    
    private void chatDebug( String msg )
    {
    	if( debug )
    	{
    		mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("\u00A72[WorldDL]\u00A76 " + msg));
    	}
    }
}
