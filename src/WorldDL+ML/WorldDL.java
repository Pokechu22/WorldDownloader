package net.minecraft.src;

import java.io.File;

import net.minecraft.client.Minecraft;

public class WorldDL {

	public static Minecraft mc;
	public static WorldClient wc;
	
	public static boolean downloading = false; // Indicator if it's running
    public static IChunkLoader myChunkLoader; // Despite it's name this is used to SAVE the chunks
    public static SaveHandler mySaveHandler; // Used to load/save world metadata and player data
    
    public static int lastClickedX; // Stores the position of the last right-clicked block.
    public static int lastClickedY; // We need to save this because the server doesn't give that information
    public static int lastClickedZ; //  when it sends the item stacks for container blocks.
    
    public static String serverHostname = "";
    
    public static void startDownload()
    {
    	if( serverHostname.isEmpty() ) serverHostname = "Downloaded world"; // Should not happen, but well... safe defaults :)
    	
    	wc = (WorldClient)mc.theWorld;
    	
    	wc.worldInfo.setWorldName(serverHostname);
		mySaveHandler = (SaveHandler) mc.getSaveLoader().getSaveLoader(serverHostname, false); // false = don't generate "Players" dir
		myChunkLoader = mySaveHandler.getChunkLoader(wc.worldProvider);
		
		((ChunkProviderClient) wc.chunkProvider).importOldTileEntities(); // Get all old TileEntities (for Chests etc.)
		
		downloading = true; // Everything set up.
		
		mc.ingameGUI.addChatMessage("§c[WorldDL] §6Download started.");
		/* I don't like the text at all */
    }
    
    public static void stopDownload()
    {
    	WorldClient wc = (WorldClient)mc.theWorld;
		wc.saveWorld(true, null); // true has no effect afaik. I use it like I found it. null means no progress bar.
		
		downloading = false; // We're done here.
		
		myChunkLoader = null; // Better remove them
		mySaveHandler = null;
		
		mc.ingameGUI.addChatMessage("§c[WorldDL] §6Download stopped.");
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

    // Normal chests consist of one TileEntityChest, large chests consist of two.
    // This method determines where the second chest block is (if any) and stores both halfs of the received items in TileEntities.
    // First half goes in the block with lower x- or z-coordinate.
    // This is a modified version of the Minecraft method BlockChest.blockActivated(World, int, int, int, EntityPlayer)
    public static void setChestTileEntitiy( IInventory inv )
    {
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
}
