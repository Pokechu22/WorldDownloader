package net.minecraft.src;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;

/**
 * This is the main class that does most of the work.
 */
public class WDL
{
	public static boolean DEBUG = false; // Setting to false will supress debug output in chat console
    // References:
    public static Minecraft   mc; // Reference to the Minecraft object
    public static WorldClient wc; // Reference to the World object that WDL uses
    public static NetworkManager nm = null; // Reference to a connection specific object. Used to detect a new connection.
    
    public static Container windowContainer; // Reference to the place where all the item stacks end up after receiving them.
    public static int lastX = 0, lastY = 0, lastZ = 0; // Last right clicked block. Needed for TileEntity creation!
    public static Entity lastEntity; // Last entity clicked (used for non-block tiles like minecarts with chests)
    
    public static SaveHandler  saveHandler; // For player files and the level.dat file
    public static IChunkLoader chunkLoader; // For the chunks (despite it's name it does also SAVE them)
    
    // Positions of newly created TileEntities that will overwrite the imported ones when saving:
    public static HashSet<ChunkPosition> newTileEntities = new HashSet<ChunkPosition>();
    
    public static GuiScreen guiToShowAsync = null; // A Gui to show in the next world tick. Needed so that the mouse works.
    
    // State variables:
    public static boolean downloading   = false; // Read-only outside of this class!
    public static boolean isMultiworld  = false; // Is this a multiworld server?
    public static boolean propsFound    = false; // Are there saved properties available?
    public static boolean startOnChange = false; // Automatically restart after world changes?
    
    public static boolean isSavingChunks = false;
    
    // Names:
    public static String worldName      = "WorldDownloaderERROR"; // safe default
	public static String baseFolderName = "WorldDownloaderERROR"; // safe default
    
	// Properties:
    public static Properties baseProps;
    public static Properties worldProps;
    public static Properties defaultProps;
	
    // Initialization:
    static
    {
        // Get the static Minecraft reference:
        mc = (Minecraft) stealAndGetField( Minecraft.class, Minecraft.class );
        
        // Initialize the Properties template:
        defaultProps = new Properties();
        defaultProps.setProperty("ServerName", "");
        defaultProps.setProperty("WorldName", "");
        defaultProps.setProperty("LinkedWorlds", "");
        defaultProps.setProperty("AutoStart", "false");
        defaultProps.setProperty("Backup", "off");
        defaultProps.setProperty("BackupPath", ""); // Represents folder or zip-file name
        defaultProps.setProperty("BackupsToKeep", "1");
        defaultProps.setProperty("BackupCommand", "");
        defaultProps.setProperty("GameType", "keep");
        defaultProps.setProperty("Time", "keep");
        defaultProps.setProperty("Weather", "keep");
        defaultProps.setProperty("MapFeatures", "false");
        defaultProps.setProperty("RandomSeed", "");
        defaultProps.setProperty("GeneratorName", "flat");
        defaultProps.setProperty("GeneratorVersion", "0");
        defaultProps.setProperty("Spawn", "player");
        defaultProps.setProperty("SpawnX", "8");
        defaultProps.setProperty("SpawnY", "127");
        defaultProps.setProperty("SpawnZ", "8");
        defaultProps.setProperty("PlayerPos", "keep");
        defaultProps.setProperty("PlayerX", "8");
        defaultProps.setProperty("PlayerY", "127");
        defaultProps.setProperty("PlayerZ", "8");
        defaultProps.setProperty("PlayerHealth", "20");
        defaultProps.setProperty("PlayerFood", "20");
        
        baseProps = new Properties(defaultProps);
        worldProps = new Properties(baseProps);
    }
    
    
    //// Methods to control World Downloader \\\\
    
    /** Starts the download */
    public static void start( )
    {
        wc = mc.theWorld;
        
        if( isMultiworld && worldName.isEmpty() )
        {
            // Ask the user which world is loaded
            guiToShowAsync = new GuiWDLMultiworldSelect( null );
            return;
        }
        
        if( ! propsFound )
        {
            // Never seen this world before. Ask user about multiworlds:
            guiToShowAsync = new GuiWDLMultiworld( null );
        }
        
        worldProps = loadWorldProps( worldName );
        
        saveHandler = (SaveHandler) mc.getSaveLoader().getSaveLoader( getWorldFolderName(worldName), true );
        chunkLoader = saveHandler.getChunkLoader( wc.provider );
        
        newTileEntities = new HashSet<ChunkPosition>();
        
        if( baseProps.getProperty( "ServerName" ).isEmpty() )
            baseProps.setProperty( "ServerName", getServerName() );
        
        startOnChange = true;
        downloading = true;
        chatMsg( "Download started" );
    }
    
    /** Stops the download */
    public static void stop( )
    {
    	if(downloading)
    	{
	    	WDLSaveAsync saver = new WDLSaveAsync();
	    	Thread thread = new Thread(saver, "WDL Save Thread");
	    	thread.start();
    	}
    }
    
    //// Callback methods for important events. Call them from suitable locations in the base classes. \\\\
    
    /** Must be called after the static World object in Minecraft has been replaced */
    public static void onWorldLoad( )
    {
        if( mc.isIntegratedServerRunning() )
            return;
        
        if( downloading ) // This should NEVER happen! (famous last words, lol)
            throw new RuntimeException( "World Downloader: Couldn't stop download! Check your worlds!" );
        
        worldName = ""; // The new (multi-)world name is unknown at the moment
        wc = mc.theWorld;
        WDL.windowContainer = WDL.mc.thePlayer.craftingInventory;
        
        // Is this a different server?
        NetworkManager newNM = mc.getSendQueue().getNetManager();
        
        if( nm != newNM )
        {
            // Different server, different world!
            chatDebug( "onWorldLoad: different server!" );
            nm = newNM;
            loadBaseProps();
            if( baseProps.getProperty( "AutoStart" ).equals( "true" ) )
                start();
            else
                startOnChange = false;
        }
        else
        {
            // Same server, different world!
            chatDebug( "onWorldLoad: same server!" );
            if( startOnChange )
                start();
        }
    }
    
    /** Must be called when the world is no longer used */
    public static void onWorldUnload( )
    {
    	/*
        if( downloading )
        {
            chatMsg( "onWorldUnload" );
            saveEverything();
            downloading = false;
            mc.getSaveLoader().flushCache();
            saveHandler.flush();
        }
        */
    	stop();
        wc = null;
    }
    
    /** Must be called when a chunk is no longer needed and should be removed */
    public static void onChunkNoLongerNeeded( Chunk unneededChunk )
    {
        if( unneededChunk == null || unneededChunk.isModified == false )
            return;
        
        chatDebug( "onChunkNoLongerNeeded: " + unneededChunk.xPosition + ", " + unneededChunk.zPosition );
        saveChunk( unneededChunk );
    }
    
    /** Must be called when a GUI that receives item stacks from the server is shown */
    public static void onItemGuiOpened( )
    {
        if( mc.objectMouseOver == null )
            return;
        
        if( mc.objectMouseOver.typeOfHit == EnumMovingObjectType.ENTITY)
        {
        	lastEntity = mc.objectMouseOver.entityHit;
        }
        else
        {
        	lastEntity = null;
	        lastX = mc.objectMouseOver.blockX;
	        lastY = mc.objectMouseOver.blockY;
	        lastZ = mc.objectMouseOver.blockZ;
        }
    }
    
    /** Must be called when a GUI that triggered an onItemGuiOpened is no longer shown */
    public static void onItemGuiClosed( )
    {
    	String saveName = "";
    	
    	// If the last thing clicked was an ENTITY
    	if(lastEntity != null)
    	{
    		
    		if(lastEntity instanceof EntityMinecart && windowContainer instanceof ContainerChest)
    		{
    			EntityMinecart emc = (EntityMinecart)lastEntity;
    			if(emc.minecartType == 1)
    			{
    				for(int i = 0; i < emc.getSizeInventory(); i++)
    				{
    					emc.setInventorySlotContents(i, windowContainer.getSlot(i).getStack());
    					saveName = "Storage Minecart contents";
    				}
    			}
    		}
    		else if(lastEntity instanceof EntityVillager && windowContainer instanceof ContainerMerchant)
    		{
    			EntityVillager ev = (EntityVillager)lastEntity;
    			WDL.chatMsg("Saving villager offers is not yet supported.");
    			saveName = "Villager offers";
    			return;
    		}
    		else
    		{
    			WDL.chatMsg("Unsupported entity cannot be saved:" + lastEntity.getEntityString());
    		}
    		WDL.chatMsg("Saved " + saveName + ".");
    		return;
    	}
    	
    	// Else, the last thing clicked was a TILE ENTITY
    	// Get the tile entity which we are going to update the inventory for 
        TileEntity te = wc.getBlockTileEntity(lastX, lastY, lastZ);
        if(te == null)
        {
        	WDL.chatDebug("onItemGuiClosed could not get TE at " + lastX + " " + lastY + " " + lastZ);
        	return;
        }
        
        if( windowContainer instanceof ContainerChest && te instanceof TileEntityChest)
        {
        	if(windowContainer.inventorySlots.size() > 63 )
        	{
        		TileEntity te2;
        		ChunkPosition cp1 = new ChunkPosition(lastX, lastY, lastZ);
        		ChunkPosition cp2;
        		TileEntityChest tec1, tec2;
        		if((te2 = wc.getBlockTileEntity(lastX, lastY, lastZ+1)) instanceof TileEntityChest)
            	{
        			tec1 = (TileEntityChest)te;
        			tec2 = (TileEntityChest)te2;
        			cp2 = new ChunkPosition(lastX, lastY, lastZ+1);
            	}
        		else if((te2 = wc.getBlockTileEntity(lastX, lastY, lastZ-1)) instanceof TileEntityChest)
            	{
            		tec1 = (TileEntityChest)te2;
        			tec2 = (TileEntityChest)te;
        			cp2 = new ChunkPosition(lastX, lastY, lastZ-1);
            	}
        		else if((te2 = wc.getBlockTileEntity(lastX+1, lastY, lastZ)) instanceof TileEntityChest)
            	{
        			tec1 = (TileEntityChest)te;
        			tec2 = (TileEntityChest)te2;
        			cp2 = new ChunkPosition(lastX+1, lastY, lastZ);
            	}
        		else if((te2 = wc.getBlockTileEntity(lastX-1, lastY, lastZ)) instanceof TileEntityChest)
            	{
        			tec1 = (TileEntityChest)te2;
        			tec2 = (TileEntityChest)te;
        			cp2 = new ChunkPosition(lastX-1, lastY, lastZ);
            	}
        		else
        		{
        			WDL.chatMsg("Could not save this chest!");
        			return;
        		}
        		copyItemStacks( windowContainer, (TileEntityChest)tec1, 0 );
                copyItemStacks( windowContainer, (TileEntityChest)tec2, 27);
                newTileEntities.add(cp1);
                newTileEntities.add(cp2);
                saveName = "Double Chest contents";
        	}
        	// basic chest
        	else
        	{
    			copyItemStacks(windowContainer, (TileEntityChest)te, 0);
    			newTileEntities.add(new ChunkPosition(lastX, lastY, lastZ));
    			saveName = "Chest contents";
        	}
        }
        else if( windowContainer instanceof ContainerChest && te instanceof TileEntityEnderChest)
		{
			InventoryEnderChest inventoryEnderChest = mc.thePlayer.getInventoryEnderChest();
        	int inventorySize = inventoryEnderChest.getSizeInventory();
        	int containerSize = windowContainer.inventorySlots.size();
        	for(int i = 0; i < containerSize && i < inventorySize; i++)
        	{
        		inventoryEnderChest.setInventorySlotContents(i, windowContainer.getSlot(i).getStack());
        	}
        	saveName = "Ender Chest contents";
        }
        else if( windowContainer instanceof ContainerBrewingStand )
        {
            copyItemStacks( windowContainer, (TileEntityBrewingStand)te, 0);
            newTileEntities.add(new ChunkPosition(lastX, lastY, lastZ));
            saveName = "Brewing Stand contents";
        }
        else if( windowContainer instanceof ContainerDispenser )
        {
        	copyItemStacks( windowContainer, (TileEntityDispenser)te, 0);
        	newTileEntities.add(new ChunkPosition(lastX, lastY, lastZ));
        	saveName = "Dispenser contents";
        }
        else if( windowContainer instanceof ContainerFurnace )
        {
        	copyItemStacks( windowContainer, (TileEntityFurnace)te, 0);
        	newTileEntities.add(new ChunkPosition(lastX, lastY, lastZ));
        	saveName = "Furnace contents";
        }
        else
        {
        	WDL.chatDebug("onItemGuiClosed unhandled TE: " + te);
        	return;
        }

        WDL.chatMsg("Saved " + saveName + ".");
        return;
    }
    
     /**
     * Must be called when a block event is scheduled for the next tick.
     * The caller has to check if WDL.downloading is true!
     */
    public static void onBlockEvent( int x, int y, int z, int blockID, int event, int param )
    {
        if( blockID == Block.music.blockID )
        {
            TileEntityNote newTE = new TileEntityNote();
            newTE.note = (byte)( param % 25 );
            wc.setBlockTileEntity( x, y, z, newTE );
            newTileEntities.add( new ChunkPosition( x, y, z ) );
            chatDebug( "onBlockEvent: Note Block: " + x + " " + y + " " + z + " pitch: " + param + " - " + newTE );
        }
        // Pistons, Chests (open, close), EnderChests, ... (see references to WorldServer.addBlockEvent)
    }
    
    
    //// Importing and exporting methods \\\\
    
    /** Load the previously saved TileEntities and add them to the Chunk **/
    public static void importTileEntities( Chunk chunk )
    {
        File chunkSaveLocation = (File) stealAndGetField( chunkLoader, File.class );
        DataInputStream dis = RegionFileCache.getChunkInputStream( chunkSaveLocation, chunk.xPosition, chunk.zPosition );
        try
        {
            NBTTagCompound chunkNBT = CompressedStreamTools.read( dis );
            NBTTagCompound levelNBT = chunkNBT.getCompoundTag( "Level" );
            // The official code checks if the chunk is in the right location. Should I too?.
            NBTTagList tileEntitiesNBT = levelNBT.getTagList( "TileEntities" );
            for( int i = 0; i < tileEntitiesNBT.tagCount(); i++ )
            {
                NBTTagCompound tileEntityNBT = (NBTTagCompound) tileEntitiesNBT.tagAt( i );
                TileEntity te = TileEntity.createAndLoadEntity( tileEntityNBT );
                String entityType = null;
                if(( entityType = isImportableTileEntity( te ) ) != null)
                {
                    if( ! newTileEntities.contains( new ChunkPosition( te.xCoord, te.yCoord, te.zCoord ) ) )
                    {
                        chunk.addTileEntity( te );
                        chatDebug("Loaded TE: " + entityType + " at " + te.xCoord + " " + te.yCoord + " " + te.zCoord );
                    }
                    else
                	{
                    	chatDebug( "Dropping old TE: " + entityType + " at " + te.xCoord + " " + te.yCoord + " " + te.zCoord );
                	}
                }
                else 
                {
                	chatDebug( "Old TE is not importable: " + entityType + " at " + te.xCoord + " " + te.yCoord + " " + te.zCoord );
                }
            }
        }
        catch ( Exception e ) { } // Couldn't load the old chunk. Nothing unusual. Happens with every not downloaded chunk.
    }
    
    /** Checks if the TileEntity should be imported. Only "problematic" TEs will be imported. */
    public static String isImportableTileEntity( TileEntity te )
    {
        Block block = Block.blocksList[ wc.getBlockId( te.xCoord, te.yCoord, te.zCoord ) ];
        if( block instanceof BlockChest && te instanceof TileEntityChest )
            return "TileEntityChest";
        else if( block instanceof BlockDispenser && te instanceof TileEntityDispenser )
            return "TileEntityDispenser";
        else if( block instanceof BlockFurnace && te instanceof TileEntityFurnace )
            return "TileEntityFurnace";
        else if( block instanceof BlockNote && te instanceof TileEntityNote )
            return "TileEntityNote";
        else if( block instanceof BlockBrewingStand && te instanceof TileEntityBrewingStand )
            return "TileEntityBrewingStand";
        else
            return null;
    }
    
    /** Saves all remaining chunks, world info and player info. Usually called when stopping. */
    public static void saveEverything( )
    {
        saveProps();
        
        try
        {
            saveHandler.checkSessionLock();
        }
        catch (MinecraftException e)
        {
            throw new RuntimeException( "WorldDownloader: Couldn't get session lock for saving the world!" );
        }
        
        NBTTagCompound playerNBT = new NBTTagCompound();
        mc.thePlayer.writeToNBT( playerNBT );
        applyOverridesToPlayer( playerNBT );
        
        wc.worldInfo.setSaveVersion( ((AnvilSaveConverter)mc.getSaveLoader()).getSaveVersion() );
        NBTTagCompound worldInfoNBT = wc.worldInfo.cloneNBTCompound( playerNBT );
        applyOverridesToWorldInfo( worldInfoNBT );
        
        savePlayer( playerNBT );
        saveWorldInfo( worldInfoNBT );
        saveChunks();
    }
    
    /** Save the player (position, health, inventory, ...) into its own file in the players directory */
    public static void savePlayer( NBTTagCompound playerNBT )
    {
        chatMsg( "Saving player data...");
        try
        {
            File playersDirectory = new File( saveHandler.getSaveDirectory(), "players" );
            File playerFile = new File( playersDirectory, mc.thePlayer.username + ".dat.tmp" );
            File playerFileOld = new File( playersDirectory, mc.thePlayer.username + ".dat" );
            
            CompressedStreamTools.writeCompressed( playerNBT, new FileOutputStream( playerFile ) );

            if( playerFileOld.exists() )
                playerFileOld.delete();
            playerFile.renameTo( playerFileOld );
        }
        catch (Exception e)
        {
            throw new RuntimeException( "Couldn't save the player!" );
        }
        chatMsg( "Player data saved.");
    }
    
    /** Save the world metadata (time, gamemode, seed, ...) into the level.dat file */
    public static void saveWorldInfo( NBTTagCompound worldInfoNBT )
    {
        chatMsg( "Saving world metadata...");
        File saveDirectory = saveHandler.getSaveDirectory();
        NBTTagCompound dataNBT = new NBTTagCompound();
        dataNBT.setTag( "Data", worldInfoNBT );
        
        try
        {
            File dataFile = new File( saveDirectory, "level.dat_new" );
            File dataFileBackup = new File( saveDirectory, "level.dat_old" );
            File dataFileOld = new File( saveDirectory, "level.dat" );
            CompressedStreamTools.writeCompressed( dataNBT, new FileOutputStream( dataFile ) );
            
            if( dataFileBackup.exists() )
                dataFileBackup.delete();
            
            dataFileOld.renameTo( dataFileBackup );
            if( dataFileOld.exists() )
                dataFileOld.delete();
            
            dataFile.renameTo( dataFileOld );
            if( dataFile.exists() )
                dataFile.delete();
        }
        catch (Exception e)
        {
            throw new RuntimeException( "Couldn't save the world metadata!" );
        }
        chatMsg( "World data saved.");
    }
    
    /** Calls saveChunk for all currently loaded chunks */
    public static void saveChunks( )
    {
        chatMsg( "Saving chunks...");
        LongHashMap chunkMapping = (LongHashMap) stealAndGetField( wc.chunkProvider, LongHashMap.class );
        LongHashMapEntry[] hashArray = (LongHashMapEntry[]) stealAndGetField( chunkMapping , LongHashMapEntry[].class );
        
        isSavingChunks = true;
        WDLSaveProgressReporter progressReporter = new WDLSaveProgressReporter();
        progressReporter.start();
        
        // Now that we have the HashMap, lets start iterating over it:
        for( LongHashMapEntry lhme : hashArray )
        {
            while( lhme != null )
            {
                Chunk c = (Chunk) lhme.value;
                if( c != null && c.isModified ) // only save filled chunks
                {
                    saveChunk( c );
                    
                    try
                    {
                    	ThreadedFileIOBase.threadedIOInstance.waitForFinish();
                    }
                    catch(Exception e)
                    {
                    	chatMsg( "Threw exception waiting for asynchronous IO to finish. Hmmm.");
                    }
                }
                else
                    chatMsg( "Didn't save chunk " + c.xPosition + " " + c.zPosition + " because isModified is false!" );
                
                lhme = lhme.nextEntry; // Get next Entry in this linked list
            }
        }
        isSavingChunks = false;
        chatMsg( "Chunk saving complete. Your single player file should be ready to play!");
    }
    
    /**
     * Renders World Downloader save progress bar
     */
    /*
    public static void renderSaveProgress()
    {
    	if (saveProgress == 0)
    		return;
        FontRenderer fontRenderer = mc.fontRenderer;
        ScaledResolution scaledResolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
        int scaledWidth = scaledResolution.getScaledWidth();
        short width = 182;
        int xPos = scaledWidth / 2 - width / 2;
        byte yPos = 12;
        mc.ingameGUI.drawTexturedModalRect(xPos, yPos, 0, 74, width, 5);
        mc.ingameGUI.drawTexturedModalRect(xPos, yPos, 0, 74, width, 5);
    	mc.ingameGUI.drawTexturedModalRect(xPos, yPos, 0, 79, saveProgress * width, 5);

        String var9 = "Save Progress";
        fontRenderer.drawStringWithShadow(var9, scaledWidth / 2 - fontRenderer.getStringWidth(var9) / 2, yPos - 10, 16711935);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/gui/icons.png"));
    }
    */
    
    /** Import all not overwritten TileEntities, then save the chunk */
    public static void saveChunk( Chunk c )
    {
        //chatMsg( "saveChunk at " + c.xPosition + " " + c.zPosition);
        importTileEntities( c );
        c.isTerrainPopulated = true;
        try
        {
            chunkLoader.saveChunk( wc, c );
        }
        catch ( Exception e )
        {
            // Better tell the player that something didn't work:
            chatMsg( "Chunk at chunk position " + c.xPosition + "," + c.zPosition + " can't be saved!" );
        }
    }
    
    
    //// Properties related methods \\\\
    
    /** Loads the server specific set of properties */
    public static void loadBaseProps( )
    {
        baseFolderName = getBaseFolderName();
        baseProps = new Properties( defaultProps );
        try
        {
            baseProps.load( new FileReader( new File( mc.mcDataDir, "saves/" + baseFolderName + "/WorldDownloader.txt" ) ) );
            propsFound = true;
        }
        catch( FileNotFoundException e )
        {
            propsFound = false;
        }
        catch( Exception e )
        { }
        
        if( baseProps.getProperty("LinkedWorlds").isEmpty() )
        {
            isMultiworld = false;
            worldProps = new Properties( baseProps );
        }
        else
            isMultiworld = true;
    }
    
    /** Loads the world specific set of properties */
    public static Properties loadWorldProps( String theWorldName )
    {
        Properties ret = new Properties( baseProps );
        if( ! theWorldName.isEmpty() )
        {
            String folder = getWorldFolderName( theWorldName );
        
            try { ret.load( new FileReader( new File( mc.mcDataDir, "saves/" + folder + "/WorldDownloader.txt" ) ) ); }
            catch ( Exception e )
            {
                return null;
            }
        }
        return ret;
    }
    
    /** Saves the currently used base and world properties in the corresponding folders */
    public static void saveProps( )
    {
        saveProps( worldName, worldProps );
    }
    
    /** Saves the specified world properties and the base properties in the corresponding folders */
    public static void saveProps( String theWorldName, Properties theWorldProps )
    {
        if( theWorldName.length() > 0 )
        {
            String folder = getWorldFolderName( theWorldName );
            try
            {
                theWorldProps.store( new FileWriter( new File( mc.mcDataDir, "saves/" + folder + "/WorldDownloader.txt" ) ), "" );
            }
            catch ( Exception e )
            { }
        }
        else if( !isMultiworld )
        {
            baseProps.putAll( theWorldProps );
        }
        
        File baseFolder = new File( mc.mcDataDir, "saves/" + baseFolderName );
        baseFolder.mkdirs();
        try
        {
            baseProps.store( new FileWriter( new File( baseFolder, "WorldDownloader.txt" ) ), "" );
        }
        catch (Exception e)
        { }
    }
    
    /** Change player specific fields according to the overrides found in the properties file */
    public static void applyOverridesToPlayer( NBTTagCompound playerNBT )
    {
        //Health
        String health = worldProps.getProperty("PlayerHealth");
        if( !health.equals("keep") )
        {
            short h = Short.parseShort(health);
            playerNBT.setShort("Health", h);
        }
        
        //foodLevel, foodTimer, foodSaturationLevel, foodExhaustionLevel
        String food = worldProps.getProperty("PlayerFood");
        if( !food.equals("keep") )
        {
            int f = Integer.parseInt(food);
            playerNBT.setInteger("foodLevel", f);
            playerNBT.setInteger("foodTickTimer", 0);
            if(f == 20)
                playerNBT.setFloat("foodSaturationLevel", 5.0f);
            else
                playerNBT.setFloat("foodSaturationLevel", 0.0f);
            playerNBT.setFloat("foodExhaustionLevel", 0.0f);
        }
        
        //Player Position
        String playerPos = worldProps.getProperty("PlayerPos");
        if( playerPos.equals("xyz") )
        {
            int x = Integer.parseInt(worldProps.getProperty("PlayerX") );
            int y = Integer.parseInt(worldProps.getProperty("PlayerY") );
            int z = Integer.parseInt(worldProps.getProperty("PlayerZ") );
            NBTTagList pos = playerNBT.getTagList("Pos");
            ((NBTTagDouble)pos.tagAt(0)).data = x + 0.5;
            ((NBTTagDouble)pos.tagAt(1)).data = (double)y + 0.621; //Player height
            ((NBTTagDouble)pos.tagAt(2)).data = z + 0.5;
            
            NBTTagList motion = playerNBT.getTagList("Motion");
            ((NBTTagDouble)motion.tagAt(0)).data = 0.0;
            ((NBTTagDouble)motion.tagAt(1)).data = -0.001; //Needed to land on the ground
            ((NBTTagDouble)motion.tagAt(2)).data = 0.0;
            
            NBTTagList rotation = playerNBT.getTagList("Rotation");
            ((NBTTagFloat)rotation.tagAt(0)).data = 0.0f;
            ((NBTTagFloat)rotation.tagAt(1)).data = 0.0f;
        }
    }
    
    /** Change world and generator specific fields according to the overrides found in the properties file */
    public static void applyOverridesToWorldInfo( NBTTagCompound worldInfoNBT )
    {
        // LevelName
        String baseName = baseProps.getProperty("ServerName");
        String worldName = worldProps.getProperty("WorldName");
        if( worldName.isEmpty() )
            worldInfoNBT.setString("LevelName", baseName);
        else
            worldInfoNBT.setString("LevelName", baseName + " - " + worldName);
        
        // GameType
        String gametypeOption = worldProps.getProperty("GameType");
        if( gametypeOption.equals("keep") )
        {
            if( mc.thePlayer.capabilities.isCreativeMode )
                worldInfoNBT.setInteger("GameType", 1); // Creative
            else
                worldInfoNBT.setInteger("GameType", 0); // Survival
        }
        else if( gametypeOption.equals("survival") )
            worldInfoNBT.setInteger("GameType", 0 );
        else if( gametypeOption.equals("creative") )
            worldInfoNBT.setInteger("GameType", 1);
        else if( gametypeOption.equals("hardcore") )
        {
            worldInfoNBT.setInteger("GameType", 0);
            worldInfoNBT.setBoolean("hardcore", true);
        }
        
        //Time
        String timeOption = worldProps.getProperty("Time");
        if( !timeOption.equals("keep") )
        {
            long t = Integer.parseInt(timeOption);
            worldInfoNBT.setLong("Time", t);
        }
        
        //RandomSeed
        String randomSeed = worldProps.getProperty("RandomSeed");
        long seed = 0;
        if ( !randomSeed.isEmpty() )
        {
            try
            {
                seed = Long.parseLong( randomSeed );
            }
            catch (NumberFormatException numberformatexception)
            {
                seed = randomSeed.hashCode();
            }
        }
        worldInfoNBT.setLong("RandomSeed", seed);
        
        //MapFeatures
        boolean mapFeatures = Boolean.parseBoolean( worldProps.getProperty("MapFeatures") );
        worldInfoNBT.setBoolean("MapFeatures", mapFeatures);
        
        //generatorName
        String generatorName = worldProps.getProperty("GeneratorName");
        worldInfoNBT.setString("generatorName", generatorName);
        
        //generatorVersion
        int generatorVersion = Integer.parseInt( worldProps.getProperty("GeneratorVersion") );
        worldInfoNBT.setInteger("generatorVersion", generatorVersion );
        
        //Weather
        String weather = worldProps.getProperty("Weather");
        if( weather.equals("sunny") )
        {
            worldInfoNBT.setBoolean("raining", false);
            worldInfoNBT.setInteger("rainTime", 0);
            worldInfoNBT.setBoolean("thundering", false);
            worldInfoNBT.setInteger("thunderTime", 0);
        }
        if( weather.equals("rain") )
        {
            worldInfoNBT.setBoolean("raining", true);
            worldInfoNBT.setInteger("rainTime", 24000);
            worldInfoNBT.setBoolean("thundering", false);
            worldInfoNBT.setInteger("thunderTime", 0);
        }
        if( weather.equals("thunderstorm") )
        {
            worldInfoNBT.setBoolean("raining", true);
            worldInfoNBT.setInteger("rainTime", 24000);
            worldInfoNBT.setBoolean("thundering", true);
            worldInfoNBT.setInteger("thunderTime", 24000);
        }
        
        //Spawn
        String spawn = worldProps.getProperty("Spawn");
        if( spawn.equals("player") )
        {
            int x = (int)Math.floor(mc.thePlayer.posX);
            int y = (int)Math.floor(mc.thePlayer.posY);
            int z = (int)Math.floor(mc.thePlayer.posZ);
            worldInfoNBT.setInteger("SpawnX", x);
            worldInfoNBT.setInteger("SpawnY", y);
            worldInfoNBT.setInteger("SpawnZ", z);
            worldInfoNBT.setBoolean("initialized", true);
        }
        else if( spawn.equals("xyz") )
        {
            int x = Integer.parseInt(worldProps.getProperty("SpawnX") );
            int y = Integer.parseInt(worldProps.getProperty("SpawnY") );
            int z = Integer.parseInt(worldProps.getProperty("SpawnZ") );
            worldInfoNBT.setInteger("SpawnX", x);
            worldInfoNBT.setInteger("SpawnY", y);
            worldInfoNBT.setInteger("SpawnZ", z);
            worldInfoNBT.setBoolean("initialized", true);
        }
    }
    
    
    //// Helper methods \\\\
    
    /** Get the name of the server the user specified it in the server list */
    public static String getServerName( )
    {
        return mc.getServerData().serverName;
    }
    
    /** Get the base folder name for the server we are connected to */
    public static String getBaseFolderName( )
    {
        String hostAndPort = mc.getServerData().serverIP;
        int lastColon = hostAndPort.lastIndexOf( ":" );
        if( lastColon == -1 ) // domain name or IPv4
            return hostAndPort;
        
        if( lastColon == hostAndPort.indexOf( ":" ) ) // domain name or IPv4 address with port
            return hostAndPort.replace( ':', '@' );
        
        int lastSqBracket = hostAndPort.lastIndexOf( "]" );
        if( lastSqBracket == -1 ) //IPv6 address without port
            return hostAndPort.replace( ':', '_' );
        
        if( lastSqBracket+1 == lastColon ) //IPv6 address with port
            return hostAndPort.substring( 1, lastSqBracket ).replace(':', '_')
                   + hostAndPort.substring(lastColon).replace(':', '@');
        
        // IPv6 address in brackets without port
        return hostAndPort.substring( 1, lastSqBracket ).replace(':', '_');
    }
    
    /** Get the folder name for the specified world */
    public static String getWorldFolderName( String theWorldName )
    {
        if( theWorldName.isEmpty() )
            return baseFolderName;
        else
            return baseFolderName + " - " + theWorldName;
    }
    
    public static void copyItemStacks( Container c, IInventory i, int startInContainerAt )
    {
        int containerSize = c.inventorySlots.size();
        int inventorySize = i.getSizeInventory();
        int nc = startInContainerAt;
        int ni = 0;
        
        while( (nc < containerSize) && (ni < inventorySize) )
        {
            ItemStack is = c.getSlot( nc ).getStack();
            i.setInventorySlotContents( ni, is );
            ni++;
            nc++;
        }
    }
    
    /** Adds a chat message with a World Downloader prefix */
    public static void chatMsg( String msg )
    {
        //System.out.println( "WorldDownloader: " + msg ); // Just for debugging!
        mc.ingameGUI.getChatGUI().printChatMessage("\u00A7c[WorldDL]\u00A76 " + msg );
    }
    
    /** Adds a chat message with a World Downloader prefix */
    public static void chatDebug( String msg )
    {
    	if(!WDL.DEBUG)
    		return;
        //System.out.println( "WorldDownloader: " + msg ); // Just for debugging!
        mc.ingameGUI.getChatGUI().printChatMessage("\u00A72[WorldDL]\u00A76 " + msg );
    }
    
    /**
     * Uses Java's reflection API to get access to an unaccessible field
     * @param typeOfClass Class that the field should be read from
     * @param typeOfField The type of the field
     * @return An Object of type Field
     */
    public static Field stealField( Class typeOfClass, Class typeOfField )
    {
        Field[] fields = typeOfClass.getDeclaredFields();
        for( Field f : fields )
        {
            if( f.getType() == typeOfField )
            {
                try
                {
                    f.setAccessible( true );
                    return f;
                }
                catch (Exception e)
                {
                    break; // Throw the Exception
                }
            }
        }
        throw new RuntimeException("WorldDownloader: Couldn't steal Field of type \"" + typeOfField + "\" from class \"" + typeOfClass + "\" !" );
    }
    
    /**
     * Uses Java's reflection API to get access to an unaccessible field
     * @param object Object that the field should be read from or the type of the object if the field is static
     * @param typeOfField The type of the field
     * @return The value of the field
     */
    public static Object stealAndGetField( Object object, Class typeOfField )
    {
        Class typeOfObject;
        
        if( object instanceof Class ) // User asked for static field:
        {
            typeOfObject = (Class) object;
            object = null;
        }
        else
            typeOfObject = object.getClass();
        
        try
        {
            Field f = stealField( typeOfObject, typeOfField );
            return f.get( object );
        }
        catch( Exception e )
        {
            throw new RuntimeException("WorldDownloader: Couldn't get Field of type \"" + typeOfField + "\" from object \"" + object + "\" !" );
        }
    }
    
}
