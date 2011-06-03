// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

package net.minecraft.src;

import java.io.IOException;
import java.util.*;

// Referenced classes of package net.minecraft.src:
//            IChunkProvider, EmptyChunk, Chunk, NibbleArray, 
//            ChunkCoordIntPair, World, IProgressUpdate

public class ChunkProviderClient
    implements IChunkProvider
{

    public ChunkProviderClient(World world)
    {
        chunkMapping = new HashMap();
        field_889_c = new ArrayList();
        blankChunk = new EmptyChunk(world, new byte[32768], 0, 0);
        worldObj = world;
    }

    public boolean chunkExists(int i, int j)
    {
        if(this != null)
        {
            return true;
        } else
        {
            ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(i, j);
            return chunkMapping.containsKey(chunkcoordintpair);
        }
    }

    public void func_539_c(int i, int j)
    {
        Chunk chunk = provideChunk(i, j);
        if(!chunk.func_21167_h())
        {
            chunk.onChunkUnload();
        }
        if(((WorldClient)worldObj).downloadThisWorld == true && chunk.neverSave == false && chunk.isFilled == true )
        {
        	saveChunk(chunk);
				try {
					((WorldClient)worldObj).downloadChunkLoader.saveExtraChunkData(worldObj, chunk);
				} catch (IOException e) {
					e.printStackTrace();
				}
        }
        chunkMapping.remove(new ChunkCoordIntPair(i, j));
        field_889_c.remove(chunk);
    }

    public Chunk func_538_d(int i, int j)
    {
        ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(i, j);
        byte abyte0[] = new byte[32768];
        Chunk chunk = new Chunk(worldObj, abyte0, i, j);
        Arrays.fill(chunk.skylightMap.data, (byte)-1);
        chunkMapping.put(chunkcoordintpair, chunk);
        chunk.isChunkLoaded = true;
        if(((WorldClient)worldObj).downloadThisWorld)
        {
        	chunk.importOldChunkTileEntities();
        }
        return chunk;
    }

    public Chunk provideChunk(int i, int j)
    {
        ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(i, j);
        Chunk chunk = (Chunk)chunkMapping.get(chunkcoordintpair);
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
    	if(((WorldClient)worldObj).downloadThisWorld == false)
    		return true;
    	
        for(Object ccip : chunkMapping.keySet())
        {
        	Chunk c = (Chunk)chunkMapping.get(ccip);
            if( flag && c != null && !c.neverSave && c.isFilled )
            {
            	try {
					((WorldClient)worldObj).downloadChunkLoader.saveExtraChunkData(worldObj, c);
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
            if( c != null && c.neverSave == false && c.isFilled )
            	saveChunk(c);
        }

        if(flag)
        {
        	((WorldClient)worldObj).downloadChunkLoader.saveExtraData();
        }

        return true;
    }
    
    private void saveChunk(Chunk chunk)
    {
        if(((WorldClient)worldObj).downloadThisWorld == false)
        	return;
        chunk.lastSaveTime = worldObj.getWorldTime();
        chunk.isTerrainPopulated = true;
        try {
            for( Object ob : chunk.newChunkTileEntityMap.keySet() )
            {
            	TileEntity te = (TileEntity) chunk.newChunkTileEntityMap.get(ob);
            	if(te != null)
            	{
            		Block block = Block.blocksList[worldObj.getBlockId(te.xCoord, te.yCoord, te.zCoord)];
            		if( block instanceof BlockChest || block instanceof BlockDispenser || block instanceof BlockFurnace || block instanceof BlockNote )
            			chunk.chunkTileEntityMap.put(ob, te);
            	}
            }
        	((WorldClient)worldObj).downloadChunkLoader.saveChunk(worldObj, chunk);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public Chunk loadChunk(int i, int j)
    {
    	Chunk ret = null;
    	try {
			ret = ((WorldClient)worldObj).downloadChunkLoader.loadChunk(worldObj, i, j);
		} catch (IOException e) {}
		return ret;
    }

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

    public boolean func_532_a()
    {
        return false;
    }

    public boolean func_536_b()
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
    private Map chunkMapping;
    private List field_889_c;
    private World worldObj;
}
