// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

package net.minecraft.src;

/* WORLD DOWNLOADER ---> */
import java.io.IOException;
/* <--- WORLD DOWNLOADER */
import java.util.*;

// Referenced classes of package net.minecraft.src:
//            IChunkProvider, PlayerList, EmptyChunk, ChunkCoordIntPair, 
//            Chunk, NibbleArray, World, IProgressUpdate

public class ChunkProviderClient
    implements IChunkProvider
{

    public ChunkProviderClient(World world)
    {
        //chunkMapping = new PlayerList(); // MCP Decompile Error?
    	chunkMapping = new HashMap();
        field_889_c = new ArrayList();
        world.getClass();
        blankChunk = new EmptyChunk(world, new byte[256 * 128], 0, 0);
        worldObj = world;
    }

    public boolean chunkExists(int i, int j)
    {
        if(this != null)
        {
            return true;
        } else
        {
            return chunkMapping.containsKey(ChunkCoordIntPair.chunkXZ2Int(i, j));
        }
    }

    public void func_539_c(int i, int j)
    {
        Chunk chunk = provideChunk(i, j);
        if(!chunk.func_21167_h())
        {
            chunk.onChunkUnload();
        }
        /* WORLD DOWNLOADER ---> */
        if(WorldDL.downloading == true && chunk.neverSave == false && chunk.isFilled == true )
        {
        	saveChunk(chunk);
				try {
					((WorldClient)worldObj).myChunkLoader.saveExtraChunkData(worldObj, chunk);
				} catch (IOException e) {
					e.printStackTrace();
				}
        }
        /* <--- WORLD DOWNLOADER */
        chunkMapping.remove(ChunkCoordIntPair.chunkXZ2Int(i, j));
        field_889_c.remove(chunk);
    }

    public Chunk loadChunk(int i, int j)
    {
        worldObj.getClass();
        byte abyte0[] = new byte[256 * 128];
        Chunk chunk = new Chunk(worldObj, abyte0, i, j);
        Arrays.fill(chunk.skylightMap.data, (byte)-1);
        chunkMapping.put(ChunkCoordIntPair.chunkXZ2Int(i, j), chunk);
        chunk.isChunkLoaded = true;
        /* WORLD DOWNLOADER ---> */
        if(WorldDL.downloading)
        {
        	chunk.importOldChunkTileEntities();
        }
        /* <--- WORLD DOWNLOADER */
        return chunk;
    }

    public Chunk provideChunk(int i, int j)
    {
        Chunk chunk = (Chunk)chunkMapping.get(ChunkCoordIntPair.chunkXZ2Int(i, j));
        if(chunk == null)
        {
            return blankChunk;
        } else
        {
            return chunk;
        }
    }

    public boolean saveChunks(boolean flag, IProgressUpdate iprogressupdate)
    {
    	/* WORLD DOWNLOADER ---> */
    	if(WorldDL.downloading == false)
    		return true;
    	
        for(Object ccip : chunkMapping.keySet())
        {
        	Chunk c = (Chunk)chunkMapping.get(ccip);
            if( flag && c != null && !c.neverSave && c.isFilled )
            {
            	try {
            		((WorldClient)worldObj).myChunkLoader.saveExtraChunkData(worldObj, c);
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
            if( c != null && c.neverSave == false && c.isFilled )
            	saveChunk(c);
        }

        if(flag)
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
            		if( block instanceof BlockChest || block instanceof BlockDispenser || block instanceof BlockFurnace || block instanceof BlockNote )
            			chunk.chunkTileEntityMap.put(ob, te);
            	}
            }
            ((WorldClient)worldObj).myChunkLoader.saveChunk(worldObj, chunk);
		} catch (IOException e) {
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
        for(Object ccip : chunkMapping.keySet())
        {
        	Chunk c = (Chunk)chunkMapping.get(ccip);
            if( c != null && c.isFilled )
            {
            	c.importOldChunkTileEntities();
            }
        }
    }
    /* <--- WORLD DOWNLOADER */

    public boolean unload100OldestChunks()
    {
        return false;
    }

    public boolean canSave()
    {
        return false;
    }

    public void populate(IChunkProvider ichunkprovider, int i, int j)
    {
    }

    public String makeString()
    {
        return (new StringBuilder()).append("MultiplayerChunkCache: ").append(chunkMapping.size()).toString();
    }

    private Chunk blankChunk;
    //private PlayerList chunkMapping; // MCP Decompile Error?
    private HashMap chunkMapping;
    private List field_889_c;
    private World worldObj;
}
