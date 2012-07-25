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
    private LongHashMap chunkMapping;
    private List field_889_c;

    /** Reference to the World object. */
    private World worldObj;

    public ChunkProviderClient(World par1World)
    {
        chunkMapping = new LongHashMap();
        field_889_c = new ArrayList();
        blankChunk = new EmptyChunk(par1World, 0, 0);
        worldObj = par1World;
    }

    /**
     * Checks to see if a chunk exists at x, y
     */
    public boolean chunkExists(int par1, int par2)
    {
        if (this != null)
        {
            return true;
        }
        else
        {
            return chunkMapping.containsItem(ChunkCoordIntPair.chunkXZ2Int(par1, par2));
        }
    }

    public void func_539_c(int par1, int par2)
    {
        Chunk chunk = provideChunk(par1, par2);

        if (!chunk.isEmpty())
        {
            chunk.onChunkUnload();
        }
        
        /* WORLD DOWNLOADER ---> */
        if(WorldDL.downloading == true && chunk.isFilled == true )
        	saveChunk(chunk);
        /* <--- WORLD DOWNLOADER */
        
        chunkMapping.remove(ChunkCoordIntPair.chunkXZ2Int(par1, par2));
        field_889_c.remove(chunk);
    }

    /**
     * Creates an empty chunk ready to put data from the server in
     */
    public Chunk loadChunk(int par1, int par2)
    {
        Chunk chunk = new Chunk(worldObj, par1, par2);
        chunkMapping.add(ChunkCoordIntPair.chunkXZ2Int(par1, par2), chunk);
        chunk.isChunkLoaded = true;
        
        /* WORLD DOWNLOADER ---> */
        
    	if( WorldDL.wc != worldObj ) // Has the world object been replaced? (e.g. by a dimension change)
    		WorldDL.worldChange( );
    	
        if( WorldDL.downloading )
        	chunk.importOldChunkTileEntities( );
        
        /* <--- WORLD DOWNLOADER */
        
        return chunk;
    }

    /**
     * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
     * specified chunk from the map seed and chunk seed
     */
    public Chunk provideChunk(int par1, int par2)
    {
        Chunk chunk = (Chunk)chunkMapping.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(par1, par2));

        if (chunk == null)
        {
            return blankChunk;
        }
        else
        {
            return chunk;
        }
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
    			Chunk c = (Chunk)lhme.value;
    			
                if( c != null && c.isFilled )
                	saveChunk(c);
                
    			lhme = lhme.nextEntry; // Get next Entry in this linked list 
    		}
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
            		
            		if( isBlockAndTileEntitySavable(block, te) )
            			chunk.chunkTileEntityMap.put(ob, te);
            	}
            }
            ((WorldClient)worldObj).myChunkLoader.saveChunk(worldObj, chunk);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private boolean isBlockAndTileEntitySavable( Block block, TileEntity te )
    {
    	if( block instanceof BlockChest && te instanceof TileEntityChest )
    		return true;
    	if( block instanceof BlockDispenser && te instanceof TileEntityDispenser )
    		return true;
    	if( block instanceof BlockFurnace && te instanceof TileEntityFurnace )
    		return true;
    	if( block instanceof BlockNote && te instanceof TileEntityNote )
    		return true;
    	if( block instanceof BlockBrewingStand && te instanceof TileEntityBrewingStand )
    		return true;
    	// else:
    	return false;
    }

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
    public void populate(IChunkProvider ichunkprovider, int i, int j)
    {
    }

    /**
     * Converts the instance data to a readable string.
     */
    public String makeString()
    {
        return (new StringBuilder()).append("MultiplayerChunkCache: ").append(chunkMapping.getNumHashElements()).toString();
    }

    /**
     * Returns a list of creatures of the specified type that can spawn at the given location.
     */
    public List getPossibleCreatures(EnumCreatureType par1EnumCreatureType, int par2, int par3, int i)
    {
        return null;
    }

    /**
     * Returns the location of the closest structure of the specified type. If not found returns null.
     */
    public ChunkPosition findClosestStructure(World par1World, String par2Str, int par3, int i, int j)
    {
        return null;
    }
}
