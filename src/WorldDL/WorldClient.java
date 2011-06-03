// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

package net.minecraft.src;

import java.util.*;

// Referenced classes of package net.minecraft.src:
//            World, ChunkCoordinates, WorldInfo, MCHash, 
//            NetClientHandler, IWorldAccess, Entity, WorldBlockPositionType, 
//            ChunkProviderClient, SaveHandlerMP, WorldProvider, Packet255KickDisconnect, 
//            IChunkProvider

public class WorldClient extends World
{

    public WorldClient(NetClientHandler netclienthandler, long l, int i)
    {
        super(new SaveHandlerMP(), "MpServer", WorldProvider.func_4101_a(i), l);
        field_1057_z = new LinkedList();
        field_1055_D = new MCHash();
        field_20914_E = new HashSet();
        field_1053_F = new HashSet();
        sendQueue = netclienthandler;
        setSpawnPoint(new ChunkCoordinates(8, 64, 8));
        field_28108_z = netclienthandler.field_28118_b;
        wc = this;
    }

    public void tick()
    {
        setWorldTime(getWorldTime() + 1L);
        int i = calculateSkylightSubtracted(1.0F);
        if(i != skylightSubtracted)
        {
            skylightSubtracted = i;
            for(int j = 0; j < worldAccesses.size(); j++)
            {
                ((IWorldAccess)worldAccesses.get(j)).updateAllRenderers();
            }

        }
        for(int k = 0; k < 10 && !field_1053_F.isEmpty(); k++)
        {
            Entity entity = (Entity)field_1053_F.iterator().next();
            if(!loadedEntityList.contains(entity))
            {
                entityJoinedWorld(entity);
            }
        }

        sendQueue.processReadPackets();
        for(int l = 0; l < field_1057_z.size(); l++)
        {
            WorldBlockPositionType worldblockpositiontype = (WorldBlockPositionType)field_1057_z.get(l);
            if(--worldblockpositiontype.field_1206_d == 0)
            {
                super.setBlockAndMetadata(worldblockpositiontype.field_1202_a, worldblockpositiontype.field_1201_b, worldblockpositiontype.field_1207_c, worldblockpositiontype.field_1205_e, worldblockpositiontype.field_1204_f);
                super.markBlockNeedsUpdate(worldblockpositiontype.field_1202_a, worldblockpositiontype.field_1201_b, worldblockpositiontype.field_1207_c);
                field_1057_z.remove(l--);
            }
        }

    }

    public void func_711_c(int i, int j, int k, int l, int i1, int j1)
    {
        for(int k1 = 0; k1 < field_1057_z.size(); k1++)
        {
            WorldBlockPositionType worldblockpositiontype = (WorldBlockPositionType)field_1057_z.get(k1);
            if(worldblockpositiontype.field_1202_a >= i && worldblockpositiontype.field_1201_b >= j && worldblockpositiontype.field_1207_c >= k && worldblockpositiontype.field_1202_a <= l && worldblockpositiontype.field_1201_b <= i1 && worldblockpositiontype.field_1207_c <= j1)
            {
                field_1057_z.remove(k1--);
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
        setSpawnPoint(new ChunkCoordinates(8, 64, 8));
    }

    protected void updateBlocksAndPlayCaveSounds()
    {
    }

    public void scheduleBlockUpdate(int i, int j, int k, int l, int i1)
    {
    }

    public boolean TickUpdates(boolean flag)
    {
        return false;
    }

    public void doPreChunk(int i, int j, boolean flag)
    {
        if(flag)
        {
            field_20915_C.func_538_d(i, j);
        } else
        {
            field_20915_C.func_539_c(i, j);
        }
        if(!flag)
        {
            markBlocksDirty(i * 16, 0, j * 16, i * 16 + 15, 128, j * 16 + 15);
        }
    }

    public boolean entityJoinedWorld(Entity entity)
    {
        boolean flag = super.entityJoinedWorld(entity);
        field_20914_E.add(entity);
        if(!flag)
        {
            field_1053_F.add(entity);
        }
        return flag;
    }

    public void setEntityDead(Entity entity)
    {
        super.setEntityDead(entity);
        field_20914_E.remove(entity);
    }

    protected void obtainEntitySkin(Entity entity)
    {
        super.obtainEntitySkin(entity);
        if(field_1053_F.contains(entity))
        {
            field_1053_F.remove(entity);
        }
    }

    protected void releaseEntitySkin(Entity entity)
    {
        super.releaseEntitySkin(entity);
        if(field_20914_E.contains(entity))
        {
            field_1053_F.add(entity);
        }
    }

    public void func_712_a(int i, Entity entity)
    {
        Entity entity1 = func_709_b(i);
        if(entity1 != null)
        {
            setEntityDead(entity1);
        }
        field_20914_E.add(entity);
        entity.entityId = i;
        if(!entityJoinedWorld(entity))
        {
            field_1053_F.add(entity);
        }
        field_1055_D.addKey(i, entity);
    }

    public Entity func_709_b(int i)
    {
        return (Entity)field_1055_D.lookup(i);
    }

    public Entity removeEntityFromWorld(int i)
    {
        Entity entity = (Entity)field_1055_D.removeObject(i);
        if(entity != null)
        {
            field_20914_E.remove(entity);
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
            field_1057_z.add(new WorldBlockPositionType(this, i, j, k, i1, j1));
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
            field_1057_z.add(new WorldBlockPositionType(this, i, j, k, j1, k1));
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
            field_1057_z.add(new WorldBlockPositionType(this, i, j, k, i1, j1));
            return true;
        } else
        {
            return false;
        }
    }

    public boolean func_714_c(int i, int j, int k, int l, int i1)
    {
        func_711_c(i, j, k, i, j, k);
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
        if(worldProvider.field_6478_e)
        {
            return;
        }
        if(field_27168_F > 0)
        {
            field_27168_F--;
        }
        field_26901_B = field_27171_C;
        if(worldInfo.getRaining())
        {
            field_27171_C += 0.01D;
        } else
        {
            field_27171_C -= 0.01D;
        }
        if(field_27171_C < 0.0F)
        {
            field_27171_C = 0.0F;
        }
        if(field_27171_C > 1.0F)
        {
            field_27171_C = 1.0F;
        }
        field_27170_D = field_27169_E;
        if(worldInfo.getThundering())
        {
            field_27169_E += 0.01D;
        } else
        {
            field_27169_E -= 0.01D;
        }
        if(field_27169_E < 0.0F)
        {
            field_27169_E = 0.0F;
        }
        if(field_27169_E > 1.0F)
        {
            field_27169_E = 1.0F;
        }
    }
    
    public void saveWorld(boolean flag, IProgressUpdate iprogressupdate) {
    	if(downloadThisWorld == true)
    	{
    		downloadSaveHandler.saveWorldInfoAndPlayer(worldInfo, playerEntities);
    		chunkProvider.saveChunks(flag, iprogressupdate);
    	}
    	super.saveWorld(flag, iprogressupdate);
    }

    public void playNoteAt(int i, int j, int k, int l, int i1)
    {
    	super.playNoteAt(i, j, k, l, i1);
    	if(downloadThisWorld == false)
    		return;
    	
    	TileEntityNote tileentitynote = (TileEntityNote)getBlockTileEntity(i, j, k);
    	if( tileentitynote == null)
    		setBlockTileEntity(i, j, k, new TileEntityNote());
        tileentitynote.note = (byte)(i1 % 25);
        tileentitynote.onInventoryChanged();
        setNewBlockTileEntity(i, j, k, tileentitynote);
    }

    public void setNewBlockTileEntity(int i, int j, int k, TileEntity tileentity)
    {
        Chunk chunk = getChunkFromChunkCoords(i >> 4, k >> 4);
        if(chunk != null)
        {
            chunk.setNewChunkBlockTileEntity(i & 0xf, j, k & 0xf, tileentity);
        }
    }
    
    private LinkedList field_1057_z;
    private NetClientHandler sendQueue;
    private ChunkProviderClient field_20915_C;
    private MCHash field_1055_D;
    private Set field_20914_E;
    private Set field_1053_F;
    
    public boolean downloadThisWorld = false;
    public IChunkLoader downloadChunkLoader;
    public SaveHandler downloadSaveHandler;
    public Packet15Place openContainerPacket;
    public static WorldClient wc;
}
