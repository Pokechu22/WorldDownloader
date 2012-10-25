package net.minecraft.src;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

public class WorldDL {

	public static Minecraft mc;
	public static WorldClient wc;

	public static boolean downloading = false; // Indicator if it's running
    public static SaveHandler mySaveHandler; // Used to load/save world metadata and player data
    
    public static int lastClickedX; // Stores the position of the last right-clicked block.
    public static int lastClickedY; // We need to save this because the server doesn't give that information
    public static int lastClickedZ; //  when it sends the item stacks for container blocks.
    
    public static String lastServerHostname = "";
    public static long lastSeed = 0;
    public static int lastDimension = Integer.MIN_VALUE;
    
    public static String serverHostname = "";
    
    public static void startDownload( )
    {
    	if( serverHostname.isEmpty() )
    	{
    		serverHostname = "Downloaded world"; // Should not happen, but well... safe defaults :)
    	}
    	
    	wc = (WorldClient)mc.theWorld;
    	wc.worldInfo.setWorldName(serverHostname);
		mySaveHandler = (SaveHandler) mc.getSaveLoader().getSaveLoader(serverHostname, false); // false = don't generate "Players" dir
		wc.myChunkLoader = mySaveHandler.getChunkLoader( wc.provider );

		((ChunkProviderClient) wc.chunkProvider).importOldTileEntities(); // Get all old TileEntities (for Chests etc.)

		downloading = true; // Everything set up.

		mc.thePlayer.addChatMessage("§c[WorldDL] §6Download started.");
    }
    
    /*
    public static void continueDownload( WorldClient newWc )
    {
    	if( !downloading )
    		return;
    	
    	WorldInfo wi = newWc.worldInfo;
    	if(    !serverHostname.equals( lastServerHostname )
    		|| wi.getSeed() != lastSeed
    		|| newWc.provider.worldType != lastDimension )
    	{
    		downloading = false; // Don't save anything from the new world
    		stopDownload(); // and stop because we are on a different server or have reconnected after an error or kick.
    		return;
    	}
    	
    	newWc.worldInfo.setWorldName(serverHostname);
    	
    	newWc.myChunkLoader = mySaveHandler.getChunkLoader( newWc.provider );
    	
    	mc.thePlayer.addChatMessage("§c[WorldDL] §6Continuing download.");
    	
    	wc = newWc;
    }
    */
    
    public static void stopDownload()
    {
    	if( wc != null)
    	{
    		wc.saveWorld(true, null); // true has no effect afaik. I use it like I found it. null means no progress bar.
    		wc.myChunkLoader = null;
    	}
		mySaveHandler = null;

		downloading = false; // We're done here.

		mc.thePlayer.addChatMessage("§c[WorldDL] §6Download stopped......");
		mc.thePlayer.addChatMessage("§c[WorldDL] §6Saved as single player world \"" + wc.worldInfo.getWorldName() + "\"");
    }

    // Helper method to get the world folder's size in bytes
    public static long getFileSizeRecursive(File f)
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

    public static void setPlayerEnderChestInventory( IInventory inv )
    {
    	InventoryEnderChest iec = mc.thePlayer.getInventoryEnderChest();
    	
    	for( int i = 0; i < 27; i++)
    	{
    		iec.setInventorySlotContents(i, inv.getStackInSlot(i));
    	}
    }
    
    // Normal chests consist of one TileEntityChest, large chests consist of two.
    // This method determines where the second chest block is (if any) and stores both halfs of the received items in TileEntities.
    // First half goes in the block with lower x- or z-coordinate.
    // This is a modified version of the Minecraft method BlockChest.blockActivated(World, int, int, int, EntityPlayer)
    public static void setChestTileEntitiy( IInventory inv )
    {
    	if(wc.getBlockId(lastClickedX, lastClickedY, lastClickedZ) == Block.enderChest.blockID)
    	{
    		InventoryEnderChest iec = WorldDL.mc.thePlayer.getInventoryEnderChest();
    		for( int i = 0; i < inv.getSizeInventory(); i++ )
    			iec.setInventorySlotContents(i, inv.getStackInSlot(i));
    		return;
    	}
    	
    	TileEntityChest tec1 = new TileEntityChest();
    	for( int i = 0; i < 27; i++)
    	{
    		tec1.setInventorySlotContents(i, inv.getStackInSlot(i));
    	}
    	
    	if( inv.getSizeInventory() == 27 )
    	{
    		wc.setMyBlockTileEntity(lastClickedX, lastClickedY, lastClickedZ, tec1);
    		return;
    	}
    	
    	TileEntityChest tec2 = new TileEntityChest();
    	for( int i = 0; i < 27; i++)
    	{
    		tec2.setInventorySlotContents(i, inv.getStackInSlot(27 + i));
    	}
    	
        if(wc.getBlockId(lastClickedX - 1, lastClickedY, lastClickedZ) == Block.chest.blockID)
        {
        	wc.setMyBlockTileEntity(lastClickedX - 1, lastClickedY, lastClickedZ, tec1);
        	wc.setMyBlockTileEntity(lastClickedX, lastClickedY, lastClickedZ, tec2);
        }
        if(wc.getBlockId(lastClickedX + 1, lastClickedY, lastClickedZ) == Block.chest.blockID)
        {
        	wc.setMyBlockTileEntity(lastClickedX, lastClickedY, lastClickedZ, tec1);
        	wc.setMyBlockTileEntity(lastClickedX + 1, lastClickedY, lastClickedZ, tec2);
        }
        if(wc.getBlockId(lastClickedX, lastClickedY, lastClickedZ - 1) == Block.chest.blockID)
        {
        	wc.setMyBlockTileEntity(lastClickedX, lastClickedY, lastClickedZ - 1, tec1);
        	wc.setMyBlockTileEntity(lastClickedX, lastClickedY, lastClickedZ, tec2);
        }
        if(wc.getBlockId(lastClickedX, lastClickedY, lastClickedZ + 1) == Block.chest.blockID)
        {
        	wc.setMyBlockTileEntity(lastClickedX, lastClickedY, lastClickedZ, tec1);
        	wc.setMyBlockTileEntity(lastClickedX, lastClickedY, lastClickedZ + 1, tec2);
        }
    }
    
    public static void handleServerSeedMessage(String msg)
    {
    	if(downloading && msg.startsWith("Seed: "))
    	{
    		String seed = msg.substring(6);
    		try
    		{
    			long seedval = Long.parseLong(seed);
    			wc.worldInfo.setSeed(seedval);
    			mc.thePlayer.addChatMessage("§c[WorldDL] §6Set single-player world seed to " + seedval);
    		}
    		catch(NumberFormatException e)
    		{
    			mc.thePlayer.addChatMessage("§c[WorldDL] §6Could not parse server seed");
    		}
    	}
    }
}
