// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst 

package net.minecraft.src;

import java.util.*;

// Referenced classes of package net.minecraft.src:
//            World, SaveHandlerMP, WorldProvider, IntHashMap, 
//            ChunkCoordinates, NetClientHandler, Entity, WorldBlockPositionType, 
//            ChunkProviderClient, Packet255KickDisconnect, WorldInfo, WorldSettings, 
//            IChunkProvider

public class WorldClient extends World
{

    private LinkedList blocksToReceive;
    private NetClientHandler sendQueue;
    private ChunkProviderClient field_20915_C;
    private IntHashMap entityHashSet;
    private Set entityList;
    private Set entitySpawnQueue;
    
    /* WORLD DOWNLOADER ---> */
    public IChunkLoader myChunkLoader; // Despite it's name this is used to SAVE the chunks
    /* <--- WORLD DOWNLOADER */


    public WorldClient(NetClientHandler netclienthandler, WorldSettings worldsettings, int i, int j)
    {
        super(new SaveHandlerMP(), "MpServer", WorldProvider.getProviderForDimension(i), worldsettings);
        blocksToReceive = new LinkedList();
        entityHashSet = new IntHashMap();
        entityList = new HashSet();
        entitySpawnQueue = new HashSet();
        sendQueue = netclienthandler;
        difficultySetting = j;
        setSpawnPoint(new ChunkCoordinates(8, 64, 8));
        mapStorage = netclienthandler.mapStorage;
        /* WORLD DOWNLOADER ---> */
        WorldDL.continueDownload( this ); // Will only continue if it was running before
        WorldDL.lastSeed = worldInfo.getRandomSeed();
        WorldDL.lastDimension = i;
        WorldDL.lastServerHostname = WorldDL.serverHostname;
        /* <--- WORLD DOWNLOADER */
    }

    public void tick()
    {
        setWorldTime(getWorldTime() + 1L);
        for(int i = 0; i < 10 && !entitySpawnQueue.isEmpty(); i++)
        {
            Entity entity = (Entity)entitySpawnQueue.iterator().next();
            if(!loadedEntityList.contains(entity))
            {
                entityJoinedWorld(entity);
            }
        }

        sendQueue.processReadPackets();
        for(int j = 0; j < blocksToReceive.size(); j++)
        {
            WorldBlockPositionType worldblockpositiontype = (WorldBlockPositionType)blocksToReceive.get(j);
            if(--worldblockpositiontype.acceptCountdown == 0)
            {
                super.setBlockAndMetadata(worldblockpositiontype.posX, worldblockpositiontype.posY, worldblockpositiontype.posZ, worldblockpositiontype.blockID, worldblockpositiontype.metadata);
                super.markBlockNeedsUpdate(worldblockpositiontype.posX, worldblockpositiontype.posY, worldblockpositiontype.posZ);
                blocksToReceive.remove(j--);
            }
        }

    }

    public void invalidateBlockReceiveRegion(int i, int j, int k, int l, int i1, int j1)
    {
        for(int k1 = 0; k1 < blocksToReceive.size(); k1++)
        {
            WorldBlockPositionType worldblockpositiontype = (WorldBlockPositionType)blocksToReceive.get(k1);
            if(worldblockpositiontype.posX >= i && worldblockpositiontype.posY >= j && worldblockpositiontype.posZ >= k && worldblockpositiontype.posX <= l && worldblockpositiontype.posY <= i1 && worldblockpositiontype.posZ <= j1)
            {
                blocksToReceive.remove(k1--);
            }
        }

    }

    protected IChunkProvider getChunkProvider()
    {
        field_20915_C = new ChunkProviderClient(this);
        return field_20915_C;
    }

    public void setSpawnLocation()
    {
    	/* WORLD DOWNLOADER ---> */
        //setSpawnPoint(new ChunkCoordinates(8, 64, 8));
    	/* <--- WORLD DOWNLOADER */
    }

    protected void updateBlocksAndPlayCaveSounds()
    {
    }

    public void scheduleBlockUpdate(int i, int j, int k, int l, int i1)
    {
    }

    public boolean tickUpdates(boolean flag)
    {
        return false;
    }

    public void doPreChunk(int i, int j, boolean flag)
    {
        if(flag)
        {
            field_20915_C.loadChunk(i, j);
        } else
        {
            field_20915_C.func_539_c(i, j);
        }
        if(!flag)
        {
            markBlocksDirty(i * 16, 0, j * 16, i * 16 + 15, field_35472_c, j * 16 + 15);
        }
    }

    public boolean entityJoinedWorld(Entity entity)
    {
        boolean flag = super.entityJoinedWorld(entity);
        entityList.add(entity);
        if(!flag)
        {
            entitySpawnQueue.add(entity);
        }
        return flag;
    }

    public void setEntityDead(Entity entity)
    {
        super.setEntityDead(entity);
        entityList.remove(entity);
    }

    protected void obtainEntitySkin(Entity entity)
    {
        super.obtainEntitySkin(entity);
        if(entitySpawnQueue.contains(entity))
        {
            entitySpawnQueue.remove(entity);
        }
    }

    protected void releaseEntitySkin(Entity entity)
    {
        super.releaseEntitySkin(entity);
        if(entityList.contains(entity))
        {
            entitySpawnQueue.add(entity);
        }
    }

    public void func_712_a(int i, Entity entity)
    {
        Entity entity1 = func_709_b(i);
        if(entity1 != null)
        {
            setEntityDead(entity1);
        }
        entityList.add(entity);
        entity.entityId = i;
        if(!entityJoinedWorld(entity))
        {
            entitySpawnQueue.add(entity);
        }
        entityHashSet.addKey(i, entity);
    }

    public Entity func_709_b(int i)
    {
        return (Entity)entityHashSet.lookup(i);
    }

    public Entity removeEntityFromWorld(int i)
    {
        Entity entity = (Entity)entityHashSet.removeObject(i);
        if(entity != null)
        {
            entityList.remove(entity);
            setEntityDead(entity);
        }
        return entity;
    }

    public boolean setBlockMetadata(int i, int j, int k, int l)
    {
        int i1 = getBlockId(i, j, k);
        int j1 = getBlockMetadata(i, j, k);
        if(super.setBlockMetadata(i, j, k, l))
        {
            blocksToReceive.add(new WorldBlockPositionType(this, i, j, k, i1, j1));
            return true;
        } else
        {
            return false;
        }
    }

    public boolean setBlockAndMetadata(int i, int j, int k, int l, int i1)
    {
        int j1 = getBlockId(i, j, k);
        int k1 = getBlockMetadata(i, j, k);
        if(super.setBlockAndMetadata(i, j, k, l, i1))
        {
            blocksToReceive.add(new WorldBlockPositionType(this, i, j, k, j1, k1));
            return true;
        } else
        {
            return false;
        }
    }

    public boolean setBlock(int i, int j, int k, int l)
    {
        int i1 = getBlockId(i, j, k);
        int j1 = getBlockMetadata(i, j, k);
        if(super.setBlock(i, j, k, l))
        {
            blocksToReceive.add(new WorldBlockPositionType(this, i, j, k, i1, j1));
            return true;
        } else
        {
            return false;
        }
    }

    public boolean setBlockAndMetadataAndInvalidate(int i, int j, int k, int l, int i1)
    {
        invalidateBlockReceiveRegion(i, j, k, i, j, k);
        if(super.setBlockAndMetadata(i, j, k, l, i1))
        {
            notifyBlockChange(i, j, k, l);
            return true;
        } else
        {
            return false;
        }
    }

    public void sendQuittingDisconnectingPacket()
    {
        sendQueue.func_28117_a(new Packet255KickDisconnect("Quitting"));
    }

    protected void updateWeather()
    {
        if(worldProvider.hasNoSky)
        {
            return;
        }
        if(lastLightningBolt > 0)
        {
            lastLightningBolt--;
        }
        prevRainingStrength = rainingStrength;
        if(worldInfo.getIsRaining())
        {
            rainingStrength += 0.01D;
        } else
        {
            rainingStrength -= 0.01D;
        }
        if(rainingStrength < 0.0F)
        {
            rainingStrength = 0.0F;
        }
        if(rainingStrength > 1.0F)
        {
            rainingStrength = 1.0F;
        }
        prevThunderingStrength = thunderingStrength;
        if(worldInfo.getIsThundering())
        {
            thunderingStrength += 0.01D;
        } else
        {
            thunderingStrength -= 0.01D;
        }
        if(thunderingStrength < 0.0F)
        {
            thunderingStrength = 0.0F;
        }
        if(thunderingStrength > 1.0F)
        {
            thunderingStrength = 1.0F;
        }
    }

    /* WORLD DOWNLOADER ---> */
    public void saveWorld(boolean flag, IProgressUpdate iprogressupdate) {
    	if(WorldDL.downloading == true)
    	{
    		chunkProvider.saveChunks(flag, iprogressupdate);
    		worldInfo.setSizeOnDisk( WorldDL.getFileSizeRecursive(WorldDL.mySaveHandler.getSaveDirectory()) );
    		worldInfo = new WorldDL.WorldInfoProxy(worldInfo);
    		WorldDL.mySaveHandler.saveWorldInfoAndPlayer(worldInfo, playerEntities);
    	}
    	super.saveWorld(flag, iprogressupdate);
    }

    public void playNoteAt(int i, int j, int k, int l, int i1)
    {
    	super.playNoteAt(i, j, k, l, i1);
    	if(WorldDL.downloading == false)
    		return;
    	if( getBlockId(i, j, k) == Block.music.blockID)
    	{
	    	TileEntityNote tileentitynote = (TileEntityNote)getBlockTileEntity(i, j, k);
	    	if( tileentitynote == null)
	    		setBlockTileEntity(i, j, k, new TileEntityNote());
	        tileentitynote.note = (byte)(i1 % 25);
	        tileentitynote.onInventoryChanged();
	        setMyBlockTileEntity(i, j, k, tileentitynote);
    	}
    }

    public void setMyBlockTileEntity(int i, int j, int k, TileEntity tileentity)
    {
        Chunk chunk = getChunkFromChunkCoords(i >> 4, k >> 4);
        if(chunk != null)
        {
            chunk.setMyChunkBlockTileEntity(i & 0xf, j, k & 0xf, tileentity);
        }
    }
    /* <--- WORLD DOWNLOADER */

}
