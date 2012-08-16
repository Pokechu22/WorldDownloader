package net.minecraft.src;

import java.util.ArrayList;
import java.util.List;

/* WORLD DOWNLOADER ---> */
import java.io.IOException;
/* <--- WORLD DOWNLOADER */

public class ChunkProviderClient implements IChunkProvider
{
    /**
     * The completely empty chunk used by ChunkProviderClient when chunkMapping doesn't contain the requested
     * coordinates.
     */
    private Chunk blankChunk;

    /**
     * The mapping between ChunkCoordinates and Chunks that ChunkProviderClient maintains.
     */
    private LongHashMap chunkMapping = new LongHashMap();

    /**
     * This may have been intended to be an iterable version of all currently loaded chunks (MultiplayerChunkCache),
     * with identical contents to chunkMapping's values. However it is never actually added to.
     */
    private List chunkListing = new ArrayList();

    /** Reference to the World object. */
    private World worldObj;

    public ChunkProviderClient(World par1World)
    {
        this.blankChunk = new EmptyChunk(par1World, 0, 0);
        this.worldObj = par1World;
    }

    /**
     * Checks to see if a chunk exists at x, y
     */
    public boolean chunkExists(int par1, int par2)
    {
        return true;
    }

    /**
     * Unload chunk from ChunkProviderClient's hashmap. Called in response to a Packet50PreChunk with its mode field set
     * to false
     */
    public void unloadChunk(int par1, int par2)
    {
        Chunk var3 = this.provideChunk(par1, par2);

        if (!var3.isEmpty())
        {
            var3.onChunkUnload();
        }

        /* WORLD DOWNLOADER ---> */
        if(WorldDL.downloading == true && var3.isFilled == true )
        {
        	saveChunk(var3);
			((WorldClient)worldObj).myChunkLoader.saveExtraChunkData(worldObj, var3);
        }
        /* <--- WORLD DOWNLOADER */

        this.chunkMapping.remove(ChunkCoordIntPair.chunkXZ2Int(par1, par2));
        this.chunkListing.remove(var3);
    }

    /**
     * loads or generates the chunk at the chunk location specified
     */
    public Chunk loadChunk(int par1, int par2)
    {
        Chunk var3 = new Chunk(this.worldObj, par1, par2);
        this.chunkMapping.add(ChunkCoordIntPair.chunkXZ2Int(par1, par2), var3);
        var3.isChunkLoaded = true;
        /* WORLD DOWNLOADER ---> */
        if(WorldDL.downloading)
        {
        	var3.importOldChunkTileEntities();
        }
        /* <--- WORLD DOWNLOADER */

        return var3;
    }

    /**
     * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
     * specified chunk from the map seed and chunk seed
     */
    public Chunk provideChunk(int par1, int par2)
    {
        Chunk var3 = (Chunk)this.chunkMapping.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(par1, par2));
        return var3 == null ? this.blankChunk : var3;
    }

    /**
     * Two modes of operation: if passed true, save all Chunks in one go.  If passed false, save up to two chunks.
     * Return true if all chunks have been saved.
     */
    public boolean saveChunks(boolean par1, IProgressUpdate par2IProgressUpdate)
    {
        /* WORLD DOWNLOADER ---> */
    	if(WorldDL.downloading == false)
    		return true;
    	for(LongHashMapEntry lhme : chunkMapping.getEntries())
    	{
    		while( lhme != null )
    		{
    			Chunk chunk = (Chunk)lhme.value;
    			
    			if(chunk != null && chunk.isFilled)
    			{
    				if(par1)
						((WorldClient)worldObj).myChunkLoader.saveExtraChunkData(worldObj, chunk);
					saveChunk(chunk);
    			}
    			lhme = lhme.nextEntry; // Get next Entry in this linked list 
    		}
    	}

        if(par1)
        {
        	((WorldClient)worldObj).myChunkLoader.saveExtraData();
        }
        /* <--- WORLD DOWNLOADER */

        return true;
    }
    /* WORLD DOWNLOADER ---> */
    private void saveChunk(Chunk chunk)
    {
        if(WorldDL.downloading == false)
        	return;
        chunk.lastSaveTime = worldObj.getWorldTime();
        chunk.isTerrainPopulated = true;
        try {
            for( Object ob : chunk.myChunkTileEntityMap.keySet() )
            {
            	TileEntity te = (TileEntity) chunk.myChunkTileEntityMap.get(ob);
            	if(te != null)
            	{
            		Block block = Block.blocksList[worldObj.getBlockId(te.xCoord, te.yCoord, te.zCoord)];
            		if( block instanceof BlockChest || block instanceof BlockDispenser || block instanceof BlockFurnace || block instanceof BlockNote || block instanceof BlockBrewingStand )
            			chunk.chunkTileEntityMap.put(ob, te);
            	}
            }
            ((WorldClient)worldObj).myChunkLoader.saveChunk(worldObj, chunk);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
//    public Chunk loadChunk(int i, int j)
//    {
//    	Chunk ret = null;
//    	try {
//			ret = WorldDL.myChunkLoader.loadChunk(worldObj, i, j);
//		} catch (IOException e) {}
//		return ret;
//    }

    public void importOldTileEntities()
    {
    	for(LongHashMapEntry lhme : chunkMapping.getEntries())
    	{
    		while( lhme != null )
    		{
    			Chunk c = (Chunk)lhme.value;
                
                if( c != null && c.isFilled )
                {
                	c.importOldChunkTileEntities();
                }
    			
    			lhme = lhme.nextEntry; // Get next Entry in this linked list 
    		}
    	}
    }
    /* <--- WORLD DOWNLOADER */

    /**
     * Unloads the 100 oldest chunks from memory, due to a bug with chunkSet.add() never being called it thinks the list
     * is always empty and will not remove any chunks.
     */
    public boolean unload100OldestChunks()
    {
        return false;
    }

    /**
     * Returns if the IChunkProvider supports saving.
     */
    public boolean canSave()
    {
        return false;
    }

    /**
     * Populates chunk with ores etc etc
     */
    public void populate(IChunkProvider par1IChunkProvider, int par2, int par3) {}

    /**
     * Converts the instance data to a readable string.
     */
    public String makeString()
    {
        return "MultiplayerChunkCache: " + this.chunkMapping.getNumHashElements();
    }

    /**
     * Returns a list of creatures of the specified type that can spawn at the given location.
     */
    public List getPossibleCreatures(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4)
    {
        return null;
    }

    /**
     * Returns the location of the closest structure of the specified type. If not found returns null.
     */
    public ChunkPosition findClosestStructure(World par1World, String par2Str, int par3, int par4, int par5)
    {
        return null;
    }

    public int getLoadedChunkCount()
    {
        return this.chunkListing.size();
    }
}
