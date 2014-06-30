package wdl;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@Mod( modid = WDL.MODID, version = WDL.VERSION )
public class WDL
{
    public static final String MODID = "WorldDownloader";
    public static final String VERSION = "1.7.10";
    
    @EventHandler
    public void init( FMLInitializationEvent event )
    {
    	MinecraftForge.EVENT_BUS.register( this );
    }
    
	
    @SubscribeEvent
    public void onChunkLoad( ChunkEvent.Load event )
    {
    	if( event.world.isRemote )
    	{
    		System.out.println("####### Loaded chunk: " + event.getChunk().xPosition + " " + event.getChunk().zPosition );
    	}
    }
    
    @SubscribeEvent
    public void onChunkUnload( ChunkEvent.Unload event )
    {
    	if( event.world.isRemote )
    	{
    		System.out.println("####### Unloaded chunk: " + event.getChunk().xPosition + " " + event.getChunk().zPosition );
    	}
    }
    
    @SubscribeEvent
    public void onWorldLoad( WorldEvent.Load event )
    {
    	if( event.world.isRemote )
    	{
    		System.out.println("####### Loaded world: " + event.world.toString() );
    	}
    	else
    	{
    		FMLLog.getLogger().error("World Downloader is a client side mod! Please remove it from the server.");
    	}
    }
    
    @SubscribeEvent
    public void onWorldUnload( WorldEvent.Unload event )
    {
    	if( event.world.isRemote )
    	{
    		System.err.println("####### Unloaded world: " + event.world.toString() );
    	}
    }
}
