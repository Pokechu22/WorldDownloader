package net.minecraft.src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import net.minecraft.client.Minecraft;

public class WorldDL {
	
	public static Minecraft mc;
	public static WorldClient wc;
	public static NetClientHandler nch;
	
	public static String remoteHostName = "example.org";
	public static String remoteHostPort = "0";
    public static String folderName = "WorldDownloader_TEMP";
    public static String currentMultiworld = null;
	
	public static Properties baseProps;
	public static Properties worldProps;
	public static Properties defaultProps;
	
	public static boolean downloading = false; // Indicator if it's running
	public static boolean explicitStop = true; // Did the user click on the disconnect button?
	public static boolean isNewWorld = true; // Is this a world that was never downloaded before?
	public static boolean isMultiworld = false; // Has this server been marked as multiworld enabled by the user?
	
	public static boolean saveWorldCalled = false;
	public static GuiScreen guiToShow = null;
	
    public static SaveHandler mySaveHandler; // Used to load/save world metadata and player data
    
    public static int lastClickedX; // Stores the position of the last right-clicked block.
    public static int lastClickedY; // We need to save this because the server doesn't give that information
    public static int lastClickedZ; //  when it sends the item stacks for container blocks.
    
    static
    {
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
    
    // Start the download
    public static void startDownload( )
    {
    	if( isMultiworld && currentMultiworld.isEmpty() )
    	{
    		guiToShow = new GuiWorldDLMultiworldSelect( null );
    		return;
    	}
    	
    	if( isNewWorld )
    	{
    		guiToShow = new GuiWorldDLMultiworld( null );
    		return;
    	}
    	
    	worldProps = loadWorldProps( currentMultiworld );
    	
		if( currentMultiworld.isEmpty() )
			mySaveHandler = (SaveHandler) mc.getSaveLoader().getSaveLoader(folderName, false); // false = don't generate "Players" dir
		else
			mySaveHandler = (SaveHandler) mc.getSaveLoader().getSaveLoader(folderName + " - " + currentMultiworld, false);
    	
		wc.myChunkLoader = mySaveHandler.getChunkLoader( wc.worldProvider );
		
		((ChunkProviderClient) wc.chunkProvider).importOldTileEntities(); // Get all old TileEntities (for chests etc.)
		
		if( baseProps.getProperty("ServerName").isEmpty() )
		{
			baseProps.setProperty( "ServerName", findServerName() );
		}
		
		explicitStop = false;
		downloading = true; // Everything set up.
		
		mc.ingameGUI.addChatMessage("§c[WorldDL] §6Download started.");
    }
    
    
	// Stop the download
    public static void stopDownload( )
    {
    	explicitStop = true;
    	wc.saveWorldIndirectly(null);
    }
    
    
    // Don't call this method to stop the download! Use stopDownload() instead!
    public static void endDownload( )
    {
    	saveProps();
    	wc.chunkProvider.saveChunks(true, null); // If downloading==false, this doesn't save anything
		wc.worldInfo = new WorldDL.WorldInfoProxy(wc.worldInfo);
		wc.playerEntities.clear();
		wc.playerEntities.add(mc.thePlayer);
		mySaveHandler.saveWorldInfoAndPlayer(wc.worldInfo, wc.playerEntities);
    	
		wc.myChunkLoader = null;
		mySaveHandler = null;
		
		downloading = false; // We're done here.
		
		if( explicitStop )
			mc.ingameGUI.addChatMessage("§c[WorldDL] §6Download stopped.");
    }
    
    
    // This method is called whenever the WorldClient instance was exchanged with a new WorldClient and Chunk data is coming in.
    // Don't do anything that involves user interaction in this method. It has to return quickly.
	public static void worldChange( )
	{
		initBaseClassRefs();
		
		if( downloading )
			System.out.println("!!! World Downloader !!! Download still running after world change! This should never happen!");

		NetClientHandler newNch = mc.getSendQueue();
		if( newNch == nch ) // Same connection => Dimension change
		{
			if( explicitStop )
				return;
			
			startDownload();
		}
		else // Server change or reconnect
		{
			nch = newNch;
			
			loadBaseProps();
	    	if( baseProps.getProperty("AutoStart").equals("true") )
	    	{
	    		startDownload();
	    	}
	    	else
	    	{
				explicitStop = true;
	    	}
	    	
		}
	}
    
	
	public static String[] getRemoteHostName( )
	{	
    	// Get the field netManager of type NetworkManager from object nch of type NetClientHandler:
		NetworkManager netManager = null;
    	Field[] nchFields = NetClientHandler.class.getDeclaredFields();
    	for( Field f : nchFields )
    	{
    		if(f.getType() == NetworkManager.class)
    		{
    			try
    			{
    				f.setAccessible(true);
    				netManager = (NetworkManager) f.get(nch);
    			}
    			catch (Exception e) { e.printStackTrace(); }
    			break;
    		}
    	}
    	
    	// Get the field remoteSocketAddress of type InetSocketAddress from object netManager of type NetworkManager:
    	InetSocketAddress remoteSocketAddress = null;
    	Field[] nmFields = NetworkManager.class.getDeclaredFields();
    	for( Field f : nmFields )
    	{
    		if(f.getType() == SocketAddress.class)
    		{
    			try
    			{
    				f.setAccessible(true);
    				remoteSocketAddress = (InetSocketAddress) f.get(netManager);
    			}
    			catch (Exception e) { e.printStackTrace(); }
    			break;
    		}
    	}
		
		String[] address = new String[2];
		address[0] = "_UNNAMED_";
		address[1] = "";
    	
		// Call the method getHostString (which returns a String) from object remoteSocketAddress of type InetSocketAddress:
		Method m = null;
		Class cl = InetSocketAddress.class;
		while( m == null && cl != null )
		{
			try
			{
				m = cl.getDeclaredMethod("getHostString");
				m.setAccessible(true);
				address[0] = (String)m.invoke(remoteSocketAddress);
			}
			catch (NoSuchMethodException e) {
				cl = cl.getSuperclass();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if( remoteSocketAddress.getPort() != 25565 )
			address[1] = String.valueOf( remoteSocketAddress.getPort() );
		
		return address;
	}
	
	
    public static String getSaveFolderName( )
    {
		String[] address = getRemoteHostName();
		for( char c : ChatAllowedCharacters.allowedCharactersArray )
		{
			address[0] = address[0].replace(c, '_');
		}
		return address[0] + (address[1].isEmpty() ? "" : "@" + address[1]);
    }
    
    
    public static String findServerName()
    {
    	String ret = folderName; // Save default
        try
        {
            NBTTagCompound nbttagcompound = CompressedStreamTools.read(new File(mc.mcDataDir, "servers.dat"));
            NBTTagList nbttaglist = nbttagcompound.getTagList("servers");
            
            String[] connectedRemote = getRemoteHostName();
            String cR = connectedRemote[0] + ( connectedRemote[1].isEmpty() ? "" : ":" + connectedRemote[1] );
            
            for (int i = 0; i < nbttaglist.tagCount(); i++)
            {
            	NBTTagCompound server = (NBTTagCompound)nbttaglist.tagAt(i);
            	String remote = server.getString("ip");
            	if( remote.equals( cR ) )
            	{
            		ret = server.getString("name");
            	}
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
		
        return ret;
	}
    
    
    public static void loadBaseProps()
    {
    	folderName = getSaveFolderName();
		baseProps = new Properties(defaultProps);
    	try
    	{
    		baseProps.load( new FileReader( new File( mc.mcDataDir, "saves/" + folderName + "/WorldDownloader.txt" ) ) );
    		isNewWorld = false;
    	}
    	catch (FileNotFoundException e)
    	{
    		isNewWorld = true;
    	}
    	catch (Exception e) { }
    	
    	
    	if( baseProps.getProperty("LinkedWorlds").isEmpty() )
    	{
    		isMultiworld = false;
    		worldProps = new Properties(baseProps);
    	}
    	else
    		isMultiworld = true;
    }
    
    
    public static Properties loadWorldProps( String multiworldFolder )
    {
    	Properties ret = new Properties(baseProps);
    	if( multiworldFolder.length() > 0 )
    	{
    		String folder = folderName +  " - " + multiworldFolder;
    	
	    	try { ret.load( new FileReader( new File( mc.mcDataDir, "saves/" + folder + "/WorldDownloader.txt" ) ) ); }
	    	catch (Exception e)
	    	{
	    		return null;
	    	}
    	}
    	return ret;
    }
    
    public static void saveProps( )
    {
    	saveProps(currentMultiworld, worldProps);
    }
    
    public static void saveProps( String multiworld, Properties multiworldProps )
    {
    	if( multiworld.length() > 0 )
    	{
    		String folder = folderName + " - " + multiworld;
    		try { multiworldProps.store( new FileWriter( new File( mc.mcDataDir, "saves/" + folder + "/WorldDownloader.txt" ) ), "" ); }
    		catch (IOException e) { }
    	}
    	else if( !isMultiworld )
    	{
    		baseProps.putAll(multiworldProps);
    	}
    	
    	File baseFolder = new File( mc.mcDataDir, "saves/" + folderName );
    	baseFolder.mkdirs();
    	try { baseProps.store( new FileWriter( new File( baseFolder, "WorldDownloader.txt" ) ), "" ); }
    	catch (IOException e) { }
    }
    
    
    public static void initBaseClassRefs( )
    {
    	// Get the Minecraft reference
    	if( mc == null )
    	{
	    	Field[] mcFields = Minecraft.class.getDeclaredFields();
	    	for( Field f : mcFields )
	    	{
	    		if(f.getType() == Minecraft.class)
	    		{
	    			try
	    			{
	    				f.setAccessible(true);
	    				mc = (Minecraft) f.get(null);
	    			}
	    			catch (Exception e) { e.printStackTrace(); }
	    			break;
	    		}
	    	}
    	}
    	
    	// Get the WorldClient
    	wc = (WorldClient) mc.theWorld;
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
    
    
    public static class WorldInfoProxy extends WorldInfo
    {

		public WorldInfoProxy(WorldInfo worldinfo)
		{
			super(worldinfo);
		}
		
	    public NBTTagCompound getNBTTagCompoundWithPlayers(List list)
	    {
	    	ArrayList activePlayerList = new ArrayList(1);
	    	activePlayerList.add(mc.thePlayer);
	        NBTTagCompound proxy = super.getNBTTagCompoundWithPlayers(activePlayerList);
	        
	        // LevelName
	        String baseName = baseProps.getProperty("ServerName");
	        String worldName = worldProps.getProperty("WorldName");
	        if( worldName.isEmpty() )
	        	proxy.setString("LevelName", baseName);
	        else
	        	proxy.setString("LevelName", baseName + " - " + worldName);
	        
	        // GameType
	        String gametypeOption = worldProps.getProperty("GameType");
	        if( gametypeOption.equals("keep") )
	        {
		        if( mc.thePlayer.capabilities.allowFlying && mc.thePlayer.capabilities.depleteBuckets && mc.thePlayer.capabilities.disableDamage )
		        	proxy.setInteger("GameType", 1); // Creative
		        else
		        	proxy.setInteger("GameType", 0); // Survival
	        }
	        else if( gametypeOption.equals("survival") )
	        	proxy.setInteger("GameType", 0 );
	        else if( gametypeOption.equals("creative") )
	        	proxy.setInteger("GameType", 1);
	        else if( gametypeOption.equals("hardcore") )
	        {
	        	proxy.setInteger("GameType", 0);
	        	proxy.setBoolean("hardcore", true);
	        }
	        
	        //Time
	        String timeOption = worldProps.getProperty("Time");
	        if( !timeOption.equals("keep") )
	        {
	        	int t = Integer.parseInt(timeOption);
	        	if( t < 0 || t > 23999 )
	        		t = 0;
	        	proxy.setLong("Time", t);
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
            proxy.setLong("RandomSeed", seed);
            
            //MapFeatures
            boolean mapFeatures = Boolean.parseBoolean( worldProps.getProperty("MapFeatures") );
            proxy.setBoolean("MapFeatures", mapFeatures);
            
            //generatorName
            String generatorName = worldProps.getProperty("GeneratorName");
            proxy.setString("generatorName", generatorName);
            
            //generatorVersion
            int generatorVersion = Integer.parseInt( worldProps.getProperty("GeneratorVersion") );
            proxy.setInteger("generatorVersion", generatorVersion );
	        
            //Weather
            String weather = worldProps.getProperty("Weather");
            if( weather.equals("sunny") )
            {
            	proxy.setBoolean("raining", false);
            	proxy.setInteger("rainTime", 0);
            	proxy.setBoolean("thundering", false);
            	proxy.setInteger("thunderTime", 0);
            }
            if( weather.equals("rain") )
            {
            	proxy.setBoolean("raining", true);
            	proxy.setInteger("rainTime", 24000);
            	proxy.setBoolean("thundering", false);
            	proxy.setInteger("thunderTime", 0);
            }
            if( weather.equals("thunderstorm") )
            {
            	proxy.setBoolean("raining", true);
            	proxy.setInteger("rainTime", 24000);
            	proxy.setBoolean("thundering", true);
            	proxy.setInteger("thunderTime", 24000);
            }
            
            //Spawn
            String spawn = worldProps.getProperty("Spawn");
            if( spawn.equals("player") )
            {
            	int x = (int)Math.floor(mc.thePlayer.posX);
            	int y = (int)Math.floor(mc.thePlayer.posY);
            	int z = (int)Math.floor(mc.thePlayer.posZ);
            	proxy.setInteger("SpawnX", x);
            	proxy.setInteger("SpawnY", y);
            	proxy.setInteger("SpawnZ", z);
            }
            else if( spawn.equals("xyz") )
            {
            	int x = Integer.parseInt(worldProps.getProperty("SpawnX") );
            	int y = Integer.parseInt(worldProps.getProperty("SpawnY") );
            	int z = Integer.parseInt(worldProps.getProperty("SpawnZ") );
            	proxy.setInteger("SpawnX", x);
            	proxy.setInteger("SpawnY", y);
            	proxy.setInteger("SpawnZ", z);
            }
            
            NBTTagCompound player = proxy.getCompoundTag("Player");
            
            //Health
            String health = worldProps.getProperty("PlayerHealth");
            if( !health.equals("keep") )
            {
            	short h = Short.parseShort(health);
            	player.setShort("Health", h);
            }
            
            //foodLevel, foodTimer, foodSaturationLevel, foodExhaustionLevel
            String food = worldProps.getProperty("PlayerFood");
            if( !food.equals("keep") )
            {
            	int f = Integer.parseInt(food);
            	player.setInteger("foodLevel", f);
            	player.setInteger("foodTimer", 0);
            	if(f == 20)
            		player.setFloat("foodSaturationLevel", 5.0f);
            	else
            		player.setFloat("foodSaturationLevel", 0.0f);
            	player.setFloat("foodExhaustionLevel", 0.0f);
            }
            
            //Player Position
            String playerPos = worldProps.getProperty("PlayerPos");
            if( playerPos.equals("xyz") )
            {
            	int x = Integer.parseInt(worldProps.getProperty("PlayerX") );
            	int y = Integer.parseInt(worldProps.getProperty("PlayerY") );
            	int z = Integer.parseInt(worldProps.getProperty("PlayerZ") );
            	NBTTagList pos = player.getTagList("Pos");
            	((NBTTagDouble)pos.tagAt(0)).data = x + 0.5;
            	((NBTTagDouble)pos.tagAt(1)).data = (double)y + 0.621; //Player height
            	((NBTTagDouble)pos.tagAt(2)).data = z + 0.5;
            	
            	NBTTagList motion = player.getTagList("Motion");
            	((NBTTagDouble)motion.tagAt(0)).data = 0.0;
            	((NBTTagDouble)motion.tagAt(1)).data = -0.001; //Needed to land on the ground
            	((NBTTagDouble)motion.tagAt(2)).data = 0.0;
            	
            	NBTTagList rotation = player.getTagList("Rotation");
            	((NBTTagFloat)rotation.tagAt(0)).data = 0.0f;
            	((NBTTagFloat)rotation.tagAt(1)).data = 0.0f;
            }
            
	        return proxy;
	    }
    }
    
}
