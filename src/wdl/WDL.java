package wdl;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.McoServer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.BlockNote;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenRealmsProxy;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.ThreadedFileIOBase;

/**
 * This is the main class that does most of the work.
 */
public class WDL
{
    //TODO: This class needs to be split into smaller classes. There is way too much different stuff in here.

    public static boolean DEBUG = false; // Setting to false will suppress debug output in chat console

    // References:
    public static Minecraft mc; // Reference to the Minecraft object
    public static WorldClient wc; // Reference to the World object that WDL uses
    public static NetworkManager nm = null; // Reference to a connection specific object. Used to detect a new connection.
    public static EntityClientPlayerMP tp;

    public static Container windowContainer; // Reference to the place where all the item stacks end up after receiving them.
    public static int lastX = 0, lastY = 0, lastZ = 0; // Last right clicked block. Needed for TileEntity creation!
    public static Entity lastEntity; // Last entity clicked (used for non-block tiles like minecarts with chests)

    public static SaveHandler saveHandler; // For player files and the level.dat file
    public static IChunkLoader chunkLoader; // For the chunks (despite it's name it does also SAVE them)

    // Positions of newly created TileEntities that will overwrite the imported ones when saving:
    public static HashSet<ChunkPosition> newTileEntities = new HashSet<ChunkPosition>();

    // State variables:
    public static boolean downloading = false; // Read-only outside of this class!
    public static boolean isMultiworld = false; // Is this a multiworld server?
    public static boolean propsFound = false; // Are there saved properties available?
    public static boolean startOnChange = false; // Automatically restart after world changes?

    public static boolean saving = false;
    public static boolean worldLoadingDeferred = false;

    // Names:
    public static String worldName = "WorldDownloaderERROR"; // safe default
    public static String baseFolderName = "WorldDownloaderERROR"; // safe default

    // Properties:
    public static Properties baseProps;
    public static Properties worldProps;
    public static Properties defaultProps;

    // Initialization:
    static
    {
        mc = Minecraft.getMinecraft();

        // Initialize the Properties template:
        defaultProps = new Properties();
        defaultProps.setProperty("ServerName", "");
        defaultProps.setProperty("WorldName", "");
        defaultProps.setProperty("LinkedWorlds", "");
        defaultProps.setProperty("AutoStart", "false");
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

    /** Starts the download */
    public static void start()
    {
        wc = mc.theWorld;
        if (isMultiworld && worldName.isEmpty())
        {
            // Ask the user which world is loaded
            mc.displayGuiScreen(new GuiWDLMultiworldSelect(null));
            return;
        }

        if (!propsFound)
        {
            // Never seen this world before. Ask user about multiworlds:
            mc.displayGuiScreen(new GuiWDLMultiworld(null));
            return;
        }

        WDL.mc.displayGuiScreen((GuiScreen)null);
        WDL.mc.setIngameFocus();

        worldProps = loadWorldProps(worldName);

        saveHandler = (SaveHandler)mc.getSaveLoader().getSaveLoader(getWorldFolderName(worldName), true);
        chunkLoader = saveHandler.getChunkLoader(wc.provider);

        newTileEntities = new HashSet<ChunkPosition>();

        if (baseProps.getProperty("ServerName").isEmpty())
            baseProps.setProperty("ServerName", getServerName());

        startOnChange = true;
        downloading = true;
        chatMsg("Download started");
    }

    /** Stops the download */
    public static void stop()
    {
        if (downloading)
        {
            // Indicate that downloading has stopped
            downloading = false;
            startOnChange = false;
            chatMsg("Download stopped");

            startSaveThread();
        }
    }

    private static void startSaveThread()
    {
        // Indicate that we are saving
        WDL.chatMsg("Save started.");
        WDL.saving = true;
        WDLSaveAsync saver = new WDLSaveAsync();
        Thread thread = new Thread(saver, "WDL Save Thread");
        thread.start();
    }

    /** Must be called after the static World object in Minecraft has been replaced */
    public static void onWorldLoad()
    {
        if (mc.isIntegratedServerRunning())
            return;

        // If already downloading
        if (downloading)
        {
            // If not currently saving, stop the current download and start saving now
            if (!saving)
            {
                WDL.chatMsg("World change detected. Download will start once current save completes.");
                // worldLoadingDeferred = true;
                startSaveThread();
            }
            return;
        }
        loadWorld();
    }

    public static void loadWorld()
    {
        worldName = ""; // The new (multi-)world name is unknown at the moment
        wc = mc.theWorld;
        tp = mc.thePlayer;
        windowContainer = tp.openContainer;

        // Is this a different server?
        NetworkManager newNM = tp.sendQueue.getNetworkManager(); // tp.sendQueue.getNetManager()

        if (nm != newNM)
        {
            // Different server, different world!
            chatDebug("onWorldLoad: different server!");
            nm = newNM;
            loadBaseProps();
            if (baseProps.getProperty("AutoStart").equals("true"))
                start();
            else
                startOnChange = false;
        }
        else
        {
            // Same server, different world!
            chatDebug("onWorldLoad: same server!");
            if (startOnChange)
                start();
        }
    }

    /** Must be called when the world is no longer used */
    public static void onWorldUnload()
    {
    }

    public static void onSaveComplete()
    {
        WDL.mc.getSaveLoader().flushCache();
        WDL.saveHandler.flush();
        WDL.wc = null;

        // If still downloading, load the current world and keep on downloading
        if (downloading)
        {
            WDL.chatMsg("Save complete. Starting download again.");
            WDL.loadWorld();
            return;
        }

        WDL.chatMsg("Save complete. Your single player file is ready to play!");
    }

    /** Must be called when a chunk is no longer needed and should be removed */
    public static void onChunkNoLongerNeeded(Chunk unneededChunk)
    {
        if (unneededChunk == null)
            return;

        chatDebug("onChunkNoLongerNeeded: " + unneededChunk.xPosition + ", " + unneededChunk.zPosition);
        saveChunk(unneededChunk);
    }

    /** Must be called when a GUI that receives item stacks from the server is shown */
    public static void onItemGuiOpened()
    {
        if (mc.objectMouseOver == null)
            return;

        if (mc.objectMouseOver.typeOfHit == MovingObjectType.ENTITY)
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
    public static void onItemGuiClosed()
    {
        String saveName = "";

        // If the last thing clicked was an ENTITY
        if (lastEntity != null)
        {

            if (lastEntity instanceof EntityMinecart && windowContainer instanceof ContainerChest)
            {
                EntityMinecart emc = (EntityMinecart)lastEntity;
                if (emc instanceof EntityMinecartChest)
                {
                    EntityMinecartChest emcc = (EntityMinecartChest)emc;
                    for (int i = 0; i < emcc.getSizeInventory(); i++)
                    {
                        emcc.setInventorySlotContents(i, windowContainer.getSlot(i).getStack());
                        saveName = "Storage Minecart contents";
                    }
                }
            }
            else if (lastEntity instanceof EntityVillager && windowContainer instanceof ContainerMerchant)
            {
                EntityVillager ev = (EntityVillager)lastEntity;
                WDL.chatDebug("Saving villager offers is not yet supported.");
                saveName = "Villager offers";
                return;
            }
            else
            {
                WDL.chatMsg("Unsupported entity cannot be saved:" + EntityList.getEntityString(lastEntity));
            }
            WDL.chatDebug("Saved " + saveName + ".");
            return;
        }

        // Else, the last thing clicked was a TILE ENTITY
        // Get the tile entity which we are going to update the inventory for
        TileEntity te = wc.getTileEntity(lastX, lastY, lastZ);
        if (te == null)
        {
            WDL.chatDebug("onItemGuiClosed could not get TE at " + lastX + " " + lastY + " " + lastZ);
            return;
        }

        if (windowContainer instanceof ContainerChest && te instanceof TileEntityChest)
        {
            if (windowContainer.inventorySlots.size() > 63)
            {
                TileEntity te2;
                ChunkPosition cp1 = new ChunkPosition(lastX, lastY, lastZ);
                ChunkPosition cp2;
                TileEntityChest tec1, tec2;
                if ((te2 = wc.getTileEntity(lastX, lastY, lastZ + 1)) instanceof TileEntityChest &&
                        ((TileEntityChest)te2).getChestType() == ((TileEntityChest)te).getChestType())
                {
                    tec1 = (TileEntityChest)te;
                    tec2 = (TileEntityChest)te2;
                    cp2 = new ChunkPosition(lastX, lastY, lastZ + 1);
                }
                else if ((te2 = wc.getTileEntity(lastX, lastY, lastZ - 1)) instanceof TileEntityChest &&
                        ((TileEntityChest)te2).getChestType() == ((TileEntityChest)te).getChestType())
                {
                    tec1 = (TileEntityChest)te2;
                    tec2 = (TileEntityChest)te;
                    cp2 = new ChunkPosition(lastX, lastY, lastZ - 1);
                }
                else if ((te2 = wc.getTileEntity(lastX + 1, lastY, lastZ)) instanceof TileEntityChest &&
                        ((TileEntityChest)te2).getChestType() == ((TileEntityChest)te).getChestType())
                {
                    tec1 = (TileEntityChest)te;
                    tec2 = (TileEntityChest)te2;
                    cp2 = new ChunkPosition(lastX + 1, lastY, lastZ);
                }
                else if ((te2 = wc.getTileEntity(lastX - 1, lastY, lastZ)) instanceof TileEntityChest &&
                        ((TileEntityChest)te2).getChestType() == ((TileEntityChest)te).getChestType())
                {
                    tec1 = (TileEntityChest)te2;
                    tec2 = (TileEntityChest)te;
                    cp2 = new ChunkPosition(lastX - 1, lastY, lastZ);
                }
                else
                {
                    WDL.chatMsg("Could not save this chest!");
                    return;
                }
                copyItemStacks(windowContainer, (TileEntityChest)tec1, 0);
                copyItemStacks(windowContainer, (TileEntityChest)tec2, 27);
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
        else if (windowContainer instanceof ContainerChest && te instanceof TileEntityEnderChest)
        {
            InventoryEnderChest inventoryEnderChest = tp.getInventoryEnderChest();
            int inventorySize = inventoryEnderChest.getSizeInventory();
            int containerSize = windowContainer.inventorySlots.size();
            for (int i = 0; i < containerSize && i < inventorySize; i++)
            {
                inventoryEnderChest.setInventorySlotContents(i, windowContainer.getSlot(i).getStack());
            }
            saveName = "Ender Chest contents";
        }
        else if (windowContainer instanceof ContainerBrewingStand)
        {
            copyItemStacks(windowContainer, (TileEntityBrewingStand)te, 0);
            newTileEntities.add(new ChunkPosition(lastX, lastY, lastZ));
            saveName = "Brewing Stand contents";
        }
        else if (windowContainer instanceof ContainerDispenser)
        {
            copyItemStacks(windowContainer, (TileEntityDispenser)te, 0);
            newTileEntities.add(new ChunkPosition(lastX, lastY, lastZ));
            saveName = "Dispenser contents";
        }
        else if (windowContainer instanceof ContainerFurnace)
        {
            copyItemStacks(windowContainer, (TileEntityFurnace)te, 0);
            newTileEntities.add(new ChunkPosition(lastX, lastY, lastZ));
            saveName = "Furnace contents";
        }
        else
        {
            WDL.chatDebug("onItemGuiClosed unhandled TE: " + te);
            return;
        }

        WDL.chatDebug("Saved " + saveName + ".");
        return;
    }

    /**
     * Must be called when a block event is scheduled for the next tick. The caller has to check if WDL.downloading is true!
     */
    public static void onBlockEvent(int x, int y, int z, Block block, int event, int param)
    {
        // if( blockID == Block.music.blockID )
        if (block == Blocks.noteblock)
        {
            TileEntityNote newTE = new TileEntityNote();
            newTE.note = (byte)(param % 25);
            wc.setTileEntity(x, y, z, newTE);
            newTileEntities.add(new ChunkPosition(x, y, z));
            chatDebug("onBlockEvent: Note Block: " + x + " " + y + " " + z + " pitch: " + param + " - " + newTE);
        }
        // Pistons, Chests (open, close), EnderChests, ... (see references to WorldServer.addBlockEvent)
    }


    /**
     * Must be called when an entity is about to be removed from the world.
     * @return true if the entity should not be removed, false if it can be
     */
    public static boolean shouldKeepEntity(Entity entity)
    {
        // If the entity is being removed and it's outside the default tracking range,
        // go ahead and remember it until the chunk is saved.
        if(WDL.downloading)
        {
            if(entity != null)
            {
                int threshold = 0;
                if ((entity instanceof EntityFishHook) ||
                        //(entity instanceof EntityArrow) ||
                        //(entity instanceof EntitySmallFireball) ||
                        //(entity instanceof EntitySnowball) ||
                        (entity instanceof EntityEnderPearl) ||
                        (entity instanceof EntityEnderEye) ||
                        (entity instanceof EntityEgg) ||
                        (entity instanceof EntityPotion) ||
                        (entity instanceof EntityExpBottle) ||
                        (entity instanceof EntityItem) ||
                        (entity instanceof EntitySquid))
                {
                    threshold = 64;
                }
                else if ((entity instanceof EntityMinecart) ||
                        (entity instanceof EntityBoat) ||
                        (entity instanceof IAnimals))
                {
                    threshold = 80;
                }
                else if ((entity instanceof EntityDragon) ||
                        (entity instanceof EntityTNTPrimed) ||
                        (entity instanceof EntityFallingBlock) ||
                        (entity instanceof EntityPainting) ||
                        (entity instanceof EntityXPOrb))
                {
                    threshold = 160;
                }
                double distance = entity.getDistance(WDL.tp.posX, entity.posY, WDL.tp.posZ);
                if( distance > (double)threshold)
                {
                    WDL.chatDebug("removeEntityFromWorld: Refusing to remove " + EntityList.getEntityString(entity) + " at distance " + distance);
                    return true;
                }
                WDL.chatDebug("removeEntityFromWorld: Removing " + EntityList.getEntityString(entity) + " at distance " + distance);
            }
        }
        return false;
    }

    /** Load the previously saved TileEntities and add them to the Chunk **/
    public static void importTileEntities(Chunk chunk)
    {
        File chunkSaveLocation = (File)stealAndGetField(chunkLoader, File.class);
        DataInputStream dis = RegionFileCache.getChunkInputStream(chunkSaveLocation, chunk.xPosition, chunk.zPosition);
        try
        {
            NBTTagCompound chunkNBT = CompressedStreamTools.read(dis);

            // NBTTagCompound levelNBT = chunkNBT.getCompoundTag( "Level" );
            NBTTagCompound levelNBT = chunkNBT.getCompoundTag("Level");

            // The official code checks if the chunk is in the right location. Should I too?.
            NBTTagList tileEntitiesNBT = levelNBT.getTagList("TileEntities", 10);
            if (tileEntitiesNBT != null)
            {
                for (int i = 0; i < tileEntitiesNBT.tagCount(); i++)
                {
                    NBTTagCompound tileEntityNBT = (NBTTagCompound)tileEntitiesNBT.getCompoundTagAt(i);
                    TileEntity te = TileEntity.createAndLoadEntity(tileEntityNBT);
                    String entityType = null;
                    if ((entityType = isImportableTileEntity(te)) != null)
                    {
                        if (!newTileEntities.contains(new ChunkPosition(te.xCoord, te.yCoord, te.zCoord)))
                        {
                            wc.setTileEntity(te.xCoord, te.yCoord, te.zCoord, te);
                            chatDebug("Loaded TE: " + entityType + " at " + te.xCoord + " " + te.yCoord + " " + te.zCoord);
                        }
                        else
                        {
                            chatDebug("Dropping old TE: " + entityType + " at " + te.xCoord + " " + te.yCoord + " " + te.zCoord);
                        }
                    }
                    else
                    {
                        chatDebug("Old TE is not importable: " + entityType + " at " + te.xCoord + " " + te.yCoord + " " + te.zCoord);
                    }
                }
            }
        }
        catch (Exception e)
        {
        } // Couldn't load the old chunk. Nothing unusual. Happens with every not downloaded chunk.
    }

    /** Checks if the TileEntity should be imported. Only "problematic" TEs will be imported. */
    public static String isImportableTileEntity(TileEntity te)
    {
        Block block = wc.getBlock(te.xCoord, te.yCoord, te.zCoord);
        if (block instanceof BlockChest && te instanceof TileEntityChest)
        {
            return "TileEntityChest";
        }
        else if (block instanceof BlockDispenser && te instanceof TileEntityDispenser)
        {
            return "TileEntityDispenser";
        }
        else if (block instanceof BlockFurnace && te instanceof TileEntityFurnace)
        {
            return "TileEntityFurnace";
        }
        else if (block instanceof BlockNote && te instanceof TileEntityNote)
        {
            return "TileEntityNote";
        }
        else if (block instanceof BlockBrewingStand && te instanceof TileEntityBrewingStand)
        {
            return "TileEntityBrewingStand";
        }
        else
        {
            return null;
        }
    }

    /** Saves all remaining chunks, world info and player info. Usually called when stopping. */
    public static void saveEverything()
    {
        saveProps();

        try
        {
            saveHandler.checkSessionLock();
        }
        catch (MinecraftException e)
        {
            throw new RuntimeException("WorldDownloader: Couldn't get session lock for saving the world!");
        }

        NBTTagCompound playerNBT = new NBTTagCompound();
        tp.writeToNBT(playerNBT);
        applyOverridesToPlayer(playerNBT);

        ISaveHandler saveHAndler = wc.getSaveHandler();
        AnvilSaveConverter saveConverter = (AnvilSaveConverter)mc.getSaveLoader();

        wc.getWorldInfo().setSaveVersion(getSaveVersion(saveConverter));

        NBTTagCompound worldInfoNBT = wc.getWorldInfo().cloneNBTCompound(playerNBT);
        applyOverridesToWorldInfo(worldInfoNBT);

        savePlayer(playerNBT);
        saveWorldInfo(worldInfoNBT);
        try
        {
            saveChunks();
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    /** Save the player (position, health, inventory, ...) into its own file in the players directory */
    public static void savePlayer(NBTTagCompound playerNBT)
    {
        chatDebug("Saving player data...");
        try
        {
            File playersDirectory = new File(saveHandler.getWorldDirectory(), "playerdata");
            File playerFile = new File(playersDirectory, tp.getUniqueID().toString() + ".dat.tmp");
            File playerFileOld = new File(playersDirectory, tp.getUniqueID().toString() + ".dat");

            CompressedStreamTools.writeCompressed(playerNBT, new FileOutputStream(playerFile));

            if (playerFileOld.exists())
            {
                playerFileOld.delete();
            }
            playerFile.renameTo(playerFileOld);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Couldn't save the player!");
        }
        chatDebug("Player data saved.");
    }

    /** Save the world metadata (time, gamemode, seed, ...) into the level.dat file */
    public static void saveWorldInfo(NBTTagCompound worldInfoNBT)
    {
        chatDebug("Saving world metadata...");
        File saveDirectory = saveHandler.getWorldDirectory();
        NBTTagCompound dataNBT = new NBTTagCompound();
        dataNBT.setTag("Data", worldInfoNBT);

        try
        {
            File dataFile = new File(saveDirectory, "level.dat_new");
            File dataFileBackup = new File(saveDirectory, "level.dat_old");
            File dataFileOld = new File(saveDirectory, "level.dat");
            CompressedStreamTools.writeCompressed(dataNBT, new FileOutputStream(dataFile));

            if (dataFileBackup.exists())
            {
                dataFileBackup.delete();
            }

            dataFileOld.renameTo(dataFileBackup);
            if (dataFileOld.exists())
            {
                dataFileOld.delete();
            }

            dataFile.renameTo(dataFileOld);
            if (dataFile.exists())
            {
                dataFile.delete();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Couldn't save the world metadata!");
        }
        chatDebug("World data saved.");
    }

    /**
     * Calls saveChunk for all currently loaded chunks
     * 
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static void saveChunks() throws IllegalArgumentException, IllegalAccessException
    {
        chatDebug("Saving chunks...");
        // Get the ChunkProviderClient from WorldClient
        ChunkProviderClient chunkProvider = (ChunkProviderClient)wc.getChunkProvider();

        // Get the hashArray field and set it accessible
        Field hashArrayField = null;
        Field[] lhmFields = LongHashMap.class.getDeclaredFields();
        //System.out.println("Looking for hashArray field...");
        for (Field f : lhmFields)
        {
            //System.out.println("Found field " + f.getName() + " of type " + f.getType().getName());
            if (f.getType().isArray())
            {
                hashArrayField = f;
                break;
            }
        }
        if (hashArrayField == null)
        {
            chatMsg("Could not save chunks. Reflection error.");
            return;
        }
        //System.out.println("Setting hashArrayField of type " + hashArrayField.getType().getName() + " accessible.");
        hashArrayField.setAccessible(true);

        // Steal the instance of LongHashMap from our chunk provider
        //System.out.println("Stealing field from chunkProvider (type=" + chunkProvider.getClass().getName() + ") of type " + LongHashMap.class.getName());
        LongHashMap lhm = (LongHashMap)stealAndGetField(chunkProvider, LongHashMap.class);
        /*      
        if (lhm != null)
        {
            System.out.println("Successfully got lhm of type" + lhm.getClass().getName());
        }
         */
        // Get the LongHashMap.Entry[] through the now accessible field using a
        // LongHashMap we steal from our chunkProvider.
        Object[] hashArray = (Object[])hashArrayField.get(lhm);
        //System.out.println("hashArray is of type " + hashArray.getClass().getName());

        //System.out.println("hashArray.length = " + hashArray.length);
        if (hashArray.length == 0)
        {
            chatError("ChunkProviderClient has no chunk data!");
            return;
        }
        else
        {
            // Get the actual class for LongHashMap.Entry
            Class<?> Entry = null;
            for (Object o : hashArray)
            {
                if(o != null)
                {
                    Entry = o.getClass();
                    break;
                }
            }
            if(Entry == null)
            {
                chatError("Could not get class for LongHashMap.Entry.");
                return;
            }

            // Find the private fields for 'value' and 'nextEntry' in
            // LongHashMap.Entry and make them accessible
            Field valueField = Entry.getDeclaredFields()[1]; // value
            valueField.setAccessible(true);
            Field nextEntryField = Entry.getDeclaredFields()[2]; // nextEntry
            nextEntryField.setAccessible(true);

            WDLSaveProgressReporter progressReporter = new WDLSaveProgressReporter();
            progressReporter.start();

            for (int i = 0; i < hashArray.length; ++i)
            {
                // for (LongHashMap.Entry entry = hashArray[i]; entry != null;
                // entry = entry.nextEntry)
                for (Object lhme = hashArray[i]; lhme != null; lhme = nextEntryField.get(lhme))
                {
                    // Chunk c = (Chunk)lhme.getValue();
                    Chunk c = (Chunk)valueField.get(lhme);
                    if (c != null)
                    {
                        saveChunk(c);
                    }
                }
            }
            try
            {
                ThreadedFileIOBase.threadedIOInstance.waitForFinish();
            } catch (Exception e)
            {
                chatMsg("Threw exception waiting for asynchronous IO to finish. Hmmm.");
            }
            chatDebug("Chunk data saved.");
        }
    }

    /**
     * Renders World Downloader save progress bar
     */
    /*
     * public static void renderSaveProgress() { if (saveProgress == 0) return; FontRenderer fontRendererObj = mc.fontRendererObj; ScaledResolution scaledResolution = new ScaledResolution(mc.gameSettings,
     * mc.displayWidth, mc.displayHeight); int scaledWidth = scaledResolution.getScaledWidth(); short width = 182; int xPos = scaledWidth / 2 - width / 2; byte yPos = 12;
     * mc.ingameGUI.drawTexturedModalRect(xPos, yPos, 0, 74, width, 5); mc.ingameGUI.drawTexturedModalRect(xPos, yPos, 0, 74, width, 5); mc.ingameGUI.drawTexturedModalRect(xPos, yPos, 0, 79,
     * saveProgress * width, 5);
     * 
     * String var9 = "Save Progress"; fontRendererObj.drawStringWithShadow(var9, scaledWidth / 2 - fontRendererObj.getStringWidth(var9) / 2, yPos - 10, 16711935); GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
     * GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/gui/icons.png")); }
     */

    /** Import all not overwritten TileEntities, then save the chunk */
    public static void saveChunk(Chunk c)
    {
        // chatMsg( "saveChunk at " + c.xPosition + " " + c.zPosition);
        importTileEntities(c);
        c.isTerrainPopulated = true;
        try
        {
            chunkLoader.saveChunk(wc, c);
        }
        catch (Exception e)
        {
            // Better tell the player that something didn't work:
            chatMsg("Chunk at chunk position " + c.xPosition + "," + c.zPosition + " can't be saved!");
        }
    }

    /** Loads the server specific set of properties */
    public static void loadBaseProps()
    {
        baseFolderName = getBaseFolderName();
        baseProps = new Properties(defaultProps);
        try
        {
            baseProps.load(new FileReader(new File(mc.mcDataDir, "saves/" + baseFolderName + "/WorldDownloader.txt")));
            propsFound = true;
        }
        catch (FileNotFoundException e)
        {
            propsFound = false;
        }
        catch (Exception e)
        {
        }

        if (baseProps.getProperty("LinkedWorlds").isEmpty())
        {
            isMultiworld = false;
            worldProps = new Properties(baseProps);
        }
        else
            isMultiworld = true;
    }

    /** Loads the world specific set of properties */
    public static Properties loadWorldProps(String theWorldName)
    {
        Properties ret = new Properties(baseProps);
        if (!theWorldName.isEmpty())
        {
            String folder = getWorldFolderName(theWorldName);

            try
            {
                ret.load(new FileReader(new File(mc.mcDataDir, "saves/" + folder + "/WorldDownloader.txt")));
            }
            catch (Exception e)
            {
                return null;
            }
        }
        return ret;
    }

    /** Saves the currently used base and world properties in the corresponding folders */
    public static void saveProps()
    {
        saveProps(worldName, worldProps);
    }

    /** Saves the specified world properties and the base properties in the corresponding folders */
    public static void saveProps(String theWorldName, Properties theWorldProps)
    {
        if (theWorldName.length() > 0)
        {
            String folder = getWorldFolderName(theWorldName);
            try
            {
                theWorldProps.store(new FileWriter(new File(mc.mcDataDir, "saves/" + folder + "/WorldDownloader.txt")), "");
            }
            catch (Exception e)
            {
            }
        }
        else if (!isMultiworld)
        {
            baseProps.putAll(theWorldProps);
        }

        File baseFolder = new File(mc.mcDataDir, "saves/" + baseFolderName);
        baseFolder.mkdirs();
        try
        {
            baseProps.store(new FileWriter(new File(baseFolder, "WorldDownloader.txt")), "");
        }
        catch (Exception e)
        {
        }
    }

    /** Change player specific fields according to the overrides found in the properties file */
    public static void applyOverridesToPlayer(NBTTagCompound playerNBT)
    {
        // Health
        String health = worldProps.getProperty("PlayerHealth");
        if (!health.equals("keep"))
        {
            short h = Short.parseShort(health);
            playerNBT.setShort("Health", h);
        }

        // foodLevel, foodTimer, foodSaturationLevel, foodExhaustionLevel
        String food = worldProps.getProperty("PlayerFood");
        if (!food.equals("keep"))
        {
            int f = Integer.parseInt(food);
            playerNBT.setInteger("foodLevel", f);
            playerNBT.setInteger("foodTickTimer", 0);
            if (f == 20)
            {
                playerNBT.setFloat("foodSaturationLevel", 5.0f);
            }
            else
            {
                playerNBT.setFloat("foodSaturationLevel", 0.0f);
            }
            playerNBT.setFloat("foodExhaustionLevel", 0.0f);
        }

        // Player Position
        String playerPos = worldProps.getProperty("PlayerPos");
        if (playerPos.equals("xyz"))
        {
            int x = Integer.parseInt(worldProps.getProperty("PlayerX"));
            int y = Integer.parseInt(worldProps.getProperty("PlayerY"));
            int z = Integer.parseInt(worldProps.getProperty("PlayerZ"));

            NBTTagList pos = playerNBT.getTagList("Pos", 6);
            // Out with the old, in with the new
            pos.removeTag(0); // removeTag
            pos.removeTag(0);
            pos.removeTag(0);
            pos.appendTag(new NBTTagDouble(x + 0.5D)); // appendTag
            pos.appendTag(new NBTTagDouble((double)y + 0.621D)); // Player height
            pos.appendTag(new NBTTagDouble(z + 0.5D));

            NBTTagList motion = playerNBT.getTagList("Motion", 6);
            // Out with the old, in with the new
            motion.removeTag(0);
            motion.removeTag(0);
            motion.removeTag(0);
            motion.appendTag(new NBTTagDouble(0.0D));
            motion.appendTag(new NBTTagDouble(-0.0001D)); // Needed to land on the ground
            motion.appendTag(new NBTTagDouble(0.0D));

            NBTTagList rotation = playerNBT.getTagList("Rotation", 5);
            // Out with the old, in with the new
            rotation.removeTag(0);
            rotation.removeTag(0);
            rotation.appendTag(new NBTTagFloat(0.0f));
            rotation.appendTag(new NBTTagFloat(0.0f));
        }
    }

    /** Change world and generator specific fields according to the overrides found in the properties file */
    public static void applyOverridesToWorldInfo(NBTTagCompound worldInfoNBT)
    {
        // LevelName
        String baseName = baseProps.getProperty("ServerName");
        String worldName = worldProps.getProperty("WorldName");
        if (worldName.isEmpty())
        {
            worldInfoNBT.setString("LevelName", baseName);
        }
        else
        {
            worldInfoNBT.setString("LevelName", baseName + " - " + worldName);
        }

        // GameType
        String gametypeOption = worldProps.getProperty("GameType");
        if (gametypeOption.equals("keep"))
        {
            if (tp.capabilities.isCreativeMode) // capabilities
            {
                worldInfoNBT.setInteger("GameType", 1); // Creative
            }
            else
            {
                worldInfoNBT.setInteger("GameType", 0); // Survival
            }
        }
        else if (gametypeOption.equals("survival"))
        {
            worldInfoNBT.setInteger("GameType", 0);
        }
        else if (gametypeOption.equals("creative"))
        {
            worldInfoNBT.setInteger("GameType", 1);
        }
        else if (gametypeOption.equals("hardcore"))
        {
            worldInfoNBT.setInteger("GameType", 0);
            worldInfoNBT.setBoolean("hardcore", true);
        }

        // Time
        String timeOption = worldProps.getProperty("Time");
        if (!timeOption.equals("keep"))
        {
            long t = Integer.parseInt(timeOption);
            worldInfoNBT.setLong("Time", t);
        }

        // RandomSeed
        String randomSeed = worldProps.getProperty("RandomSeed");
        long seed = 0;
        if (!randomSeed.isEmpty())
        {
            try
            {
                seed = Long.parseLong(randomSeed);
            }
            catch (NumberFormatException numberformatexception)
            {
                seed = randomSeed.hashCode();
            }
        }
        worldInfoNBT.setLong("RandomSeed", seed);

        // MapFeatures
        boolean mapFeatures = Boolean.parseBoolean(worldProps.getProperty("MapFeatures"));
        worldInfoNBT.setBoolean("MapFeatures", mapFeatures);

        // generatorName
        String generatorName = worldProps.getProperty("GeneratorName");
        worldInfoNBT.setString("generatorName", generatorName);

        // generatorVersion
        int generatorVersion = Integer.parseInt(worldProps.getProperty("GeneratorVersion"));
        worldInfoNBT.setInteger("generatorVersion", generatorVersion);

        // Weather
        String weather = worldProps.getProperty("Weather");
        if (weather.equals("sunny"))
        {
            worldInfoNBT.setBoolean("raining", false);
            worldInfoNBT.setInteger("rainTime", 0);
            worldInfoNBT.setBoolean("thundering", false);
            worldInfoNBT.setInteger("thunderTime", 0);
        }
        if (weather.equals("rain"))
        {
            worldInfoNBT.setBoolean("raining", true);
            worldInfoNBT.setInteger("rainTime", 24000);
            worldInfoNBT.setBoolean("thundering", false);
            worldInfoNBT.setInteger("thunderTime", 0);
        }
        if (weather.equals("thunderstorm"))
        {
            worldInfoNBT.setBoolean("raining", true);
            worldInfoNBT.setInteger("rainTime", 24000);
            worldInfoNBT.setBoolean("thundering", true);
            worldInfoNBT.setInteger("thunderTime", 24000);
        }

        // Spawn
        String spawn = worldProps.getProperty("Spawn");
        if (spawn.equals("player"))
        {
            int x = (int)Math.floor(tp.posX);
            int y = (int)Math.floor(tp.posY);
            int z = (int)Math.floor(tp.posZ);
            worldInfoNBT.setInteger("SpawnX", x);
            worldInfoNBT.setInteger("SpawnY", y);
            worldInfoNBT.setInteger("SpawnZ", z);
            worldInfoNBT.setBoolean("initialized", true);
        }
        else if (spawn.equals("xyz"))
        {
            int x = Integer.parseInt(worldProps.getProperty("SpawnX"));
            int y = Integer.parseInt(worldProps.getProperty("SpawnY"));
            int z = Integer.parseInt(worldProps.getProperty("SpawnZ"));
            worldInfoNBT.setInteger("SpawnX", x);
            worldInfoNBT.setInteger("SpawnY", y);
            worldInfoNBT.setInteger("SpawnZ", z);
            worldInfoNBT.setBoolean("initialized", true);
        }
    }

    /** Get the name of the server the user specified it in the server list */
    public static String getServerName()
    {
        try
        {
            if (mc.getCurrentServerData() != null)
            {
                String name = mc.getCurrentServerData().serverName;
                
                if(name.equals(I18n.format("selectServer.defaultName")))
                {
                	// Direct connection using domain name or IP (and port)
                	name =  mc.getCurrentServerData().serverIP;
                }
                return name;
            }
            else
            {
                String realmName = getRealmName();
                if(realmName != null)
                {
                    return realmName;
                }
            }
        }
        catch (Exception e)
        {
        }
        return "Unidentified Server";
    }

    public static String getRealmName()
    {
        // Is this the only way to get the name of the Realms server? Really Mojang?
        // If this function turns out to be a pain to update, just remove Realms support completely.
        // I doubt anyone will need this anyway since Realms support downloading the world out of the box.

        // Try to get the value of mc.getNetHandler().guiScreenServer:
        GuiScreen screen = (GuiScreen) stealAndGetField(mc.getNetHandler(), GuiScreen.class);

        // If it is not a GuiScreenRealmsProxy we are not using a Realms server
        if(!(screen instanceof GuiScreenRealmsProxy)) return null;

        // Get the proxy's RealmsScreen object
        GuiScreenRealmsProxy screenProxy = (GuiScreenRealmsProxy) screen;
        RealmsScreen rs = screenProxy.func_154321_a();

        // It needs to be of type RealmsMainScreen (this should always be the case)
        if(!(rs instanceof RealmsMainScreen)) return null;

        RealmsMainScreen rms = (RealmsMainScreen) rs;
        McoServer mcos = null;
        try
        {
            // Find the ID of the selected Realms server. Fortunately unobfuscated names!
            Field selectedServerId = rms.getClass().getDeclaredField("selectedServerId");
            selectedServerId.setAccessible(true);
            Object obj = selectedServerId.get(rms);
            if(!(obj instanceof Long)) return null;
            long id = ((Long)obj).longValue();

            // Get the McoServer instance that was selected
            Method findServer = rms.getClass().getDeclaredMethod("findServer", long.class);
            findServer.setAccessible(true);
            obj = findServer.invoke(rms, id);
            if(!(obj instanceof McoServer)) return null;
            mcos = (McoServer)obj;
        }
        catch (Exception e)
        {
            return null;
        }

        // Return its name. Not sure if this is the best naming scheme...
        return mcos.name;
    }

    /** Get the base folder name for the server we are connected to */
    public static String getBaseFolderName()
    {
        return getServerName().replaceAll("\\W+", "_");
    }

    /** Get the folder name for the specified world */
    public static String getWorldFolderName(String theWorldName)
    {
        if (theWorldName.isEmpty())
        {
            return baseFolderName;
        }
        else
        {
            return baseFolderName + " - " + theWorldName;
        }
    }

    public static void copyItemStacks(Container c, IInventory i, int startInContainerAt)
    {
        int containerSize = c.inventorySlots.size();
        int inventorySize = i.getSizeInventory();
        int nc = startInContainerAt;
        int ni = 0;

        while ((nc < containerSize) && (ni < inventorySize))
        {
            ItemStack is = c.getSlot(nc).getStack();
            i.setInventorySlotContents(ni, is);
            ni++;
            nc++;
        }
    }

    /** Adds a chat message with a World Downloader prefix */
    public static void chatMsg(String msg)
    {
        // System.out.println( "WorldDownloader: " + msg ); // Just for debugging!
        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("\u00A7c[WorldDL]\u00A76 " + msg));
    }

    /** Adds a chat message with a World Downloader prefix */
    public static void chatDebug(String msg)
    {
        if (!WDL.DEBUG)
            return;
        // System.out.println( "WorldDownloader: " + msg ); // Just for debugging!
        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("\u00A72[WorldDL]\u00A76 " + msg));
    }

    /** Adds a chat message with a World Downloader prefix */
    public static void chatError(String msg)
    {
        // System.out.println( "WorldDownloader: " + msg ); // Just for debugging!
        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("\u00A72[WorldDL]\u00A74 " + msg));
    }


    private static int getSaveVersion(AnvilSaveConverter asc)
    {
        int saveVersion = 0;
        try
        {
            Method[] anvilMethods = AnvilSaveConverter.class.getDeclaredMethods();
            for (Method m : anvilMethods)
            {
                if (m.getParameterTypes().length == 0 && m.getReturnType().equals(int.class))
                {
                    m.setAccessible(true);
                    saveVersion = (Integer)m.invoke(asc);
                    break;
                }
            }
        } catch (Throwable t)
        {
            t.printStackTrace();
        }
        if (saveVersion == 0)
        {
            saveVersion = 19133; // Version for 1.7.2 just in case we can't get
            // it
        }
        return saveVersion;
    }


    /**
     * Uses Java's reflection API to get access to an unaccessible field
     * 
     * @param typeOfClass
     *            Class that the field should be read from
     * @param typeOfField
     *            The type of the field
     * @return An Object of type Field
     */
    public static Field stealField(Class typeOfClass, Class typeOfField)
    {
        //System.out.println("stealField: typeOfClass = " + typeOfClass.getName());
        //System.out.println("stealField: typeOfField = " + typeOfField.getName());
        Field[] fields = typeOfClass.getDeclaredFields();
        for (Field f : fields)
        {
            //System.out.println("stealField: Found field " + f.getName() + 
            //        " of type " + f.getType());

            if (f.getType().equals(typeOfField))
            {
                try
                {
                    f.setAccessible(true);
                    return f;
                }
                catch (Exception e)
                {
                    break; // Throw the Exception
                }
            }
        }
        throw new RuntimeException("WorldDownloader: Couldn't steal Field of type \"" + typeOfField + "\" from class \"" + typeOfClass + "\" !");
    }

    /**
     * Uses Java's reflection API to get access to an unaccessible field
     * 
     * @param object
     *            Object that the field should be read from or the type of the object if the field is static
     * @param typeOfField
     *            The type of the field
     * @return The value of the field
     */
    public static Object stealAndGetField(Object object, Class typeOfField)
    {
        Class typeOfObject;

        if (object instanceof Class) // User asked for static field:
        {
            typeOfObject = (Class)object;
            object = null;
        }
        else
        {
            typeOfObject = object.getClass();
        }

        try
        {
            Field f = stealField(typeOfObject, typeOfField);
            return f.get(object);
        }
        catch (Exception e)
        {
            throw new RuntimeException("WorldDownloader: Couldn't get Field of type \"" + typeOfField + "\" from object \"" + object + "\" !");
        }
    }

    public static void handleServerSeedMessage(String msg)
    {
        if (downloading && msg.startsWith("Seed: "))
        {
            String seed = msg.substring(6);
            worldProps.setProperty("RandomSeed", seed);
            WDL.chatMsg("Setting single-player world seed to " + seed);
        }
        /*
         * else { WDL.chatMsg("Could not retrieve server seed"); }
         */
    }

    // Add World Downloader buttons to GuiIngameMenu
    public static void injectWDLButtons(GuiIngameMenu gui, List buttonList)
    {
        if (mc.isIntegratedServerRunning())
        {
            return; // WDL not available if in singleplayer or LAN server mode
        }

        int insertAtYPos = 0;
        for( Object obj : buttonList)
        {
            GuiButton btn = (GuiButton)obj;
            if(btn.id == 5) // Button "Achievements"
            {
                insertAtYPos = btn.yPosition + 24;
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

    public static void handleWDLButtonClick(GuiIngameMenu gui, GuiButton button)
    {
        if (mc.isIntegratedServerRunning())
        {
            return; // WDL not available if in singleplayer or LAN server mode
        }

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
