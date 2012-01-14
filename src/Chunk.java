package net.minecraft.src;

import java.io.PrintStream;
import java.util.*;

/* WORLD DOWNLOADER ---> */
import java.io.File;
import java.io.IOException;
import net.minecraft.client.Minecraft;
/* <--- WORLD DOWNLOADER */

public class Chunk
{
    public static boolean isLit;
    public byte blocks[];
    public int precipitationHeightMap[];
    public boolean updateSkylightColumns[];
    public boolean isChunkLoaded;
    public World worldObj;
    public NibbleArray data;
    public NibbleArray skylightMap;
    public NibbleArray blocklightMap;
    public byte heightMap[];
    public int lowestBlockHeight;
    public final int xPosition;
    public final int zPosition;
    private boolean field_40741_v;
    public Map chunkTileEntityMap;
    public List entities[];
    public boolean isTerrainPopulated;
    public boolean isModified;
    public boolean neverSave;
    public boolean hasEntities;
    public long lastSaveTime;
    boolean field_35846_u;

    /* WORLD DOWNLOADER ---> */
    public boolean isFilled = false; // Used to only save chunks that have already been received.
    public Map myChunkTileEntityMap; // Copy of the TileEntity data that only my code touches (never overwritten by junk)
    /* <--- WORLD DOWNLOADER */

    
    public Chunk(World world, int i, int j)
    {
        precipitationHeightMap = new int[256];
        updateSkylightColumns = new boolean[256];
        field_40741_v = false;
        chunkTileEntityMap = new HashMap();
        /* WORLD DOWNLOADER ---> */
        myChunkTileEntityMap = new HashMap();
        /* <--- WORLD DOWNLOADER */
        isTerrainPopulated = false;
        isModified = false;
        hasEntities = false;
        lastSaveTime = 0L;
        field_35846_u = false;
        entities = new List[world.worldHeight / 16];
        worldObj = world;
        xPosition = i;
        zPosition = j;
        heightMap = new byte[256];
        for (int k = 0; k < entities.length; k++)
        {
            entities[k] = new ArrayList();
        }

        Arrays.fill(precipitationHeightMap, -999);
    }

    public Chunk(World world, byte abyte0[], int i, int j)
    {
        this(world, i, j);
        blocks = abyte0;
        data = new NibbleArray(abyte0.length, world.heightShift);
        skylightMap = new NibbleArray(abyte0.length, world.heightShift);
        blocklightMap = new NibbleArray(abyte0.length, world.heightShift);
    }

    public boolean isAtLocation(int i, int j)
    {
        return i == xPosition && j == zPosition;
    }

    public int getHeightValue(int i, int j)
    {
        return heightMap[j << 4 | i] & 0xff;
    }

    public void func_1014_a()
    {
    }

    public void generateHeightMap()
    {
        int i = worldObj.worldHeight - 1;
        for (int j = 0; j < 16; j++)
        {
            for (int k = 0; k < 16; k++)
            {
                precipitationHeightMap[j + (k << 4)] = -999;
                int l = worldObj.worldHeight - 1;
                for (int i1 = j << worldObj.xShift | k << worldObj.heightShift; l > 0 && Block.lightOpacity[blocks[(i1 + l) - 1] & 0xff] == 0; l--) { }
                heightMap[k << 4 | j] = (byte)l;
                if (l < i)
                {
                    i = l;
                }
            }
        }

        lowestBlockHeight = i;
        isModified = true;
    }

    public void generateSkylightMap()
    {
        int i = worldObj.worldHeight - 1;
        for (int j = 0; j < 16; j++)
        {
            for (int l = 0; l < 16; l++)
            {
                int j1 = worldObj.worldHeight - 1;
                int k1;
                for (k1 = j << worldObj.xShift | l << worldObj.heightShift; j1 > 0 && Block.lightOpacity[blocks[(k1 + j1) - 1] & 0xff] == 0; j1--) { }
                heightMap[l << 4 | j] = (byte)j1;
                if (j1 < i)
                {
                    i = j1;
                }
                if (worldObj.worldProvider.hasNoSky)
                {
                    continue;
                }
                int l1 = 15;
                int i2 = worldObj.worldHeight - 1;
                do
                {
                    l1 -= Block.lightOpacity[blocks[k1 + i2] & 0xff];
                    if (l1 > 0)
                    {
                        skylightMap.setNibble(j, i2, l, l1);
                    }
                }
                while (--i2 > 0 && l1 > 0);
            }
        }

        lowestBlockHeight = i;
        for (int k = 0; k < 16; k++)
        {
            for (int i1 = 0; i1 < 16; i1++)
            {
                propagateSkylightOcclusion(k, i1);
            }
        }

        isModified = true;
    }

    public void func_4143_d()
    {
    }

    private void propagateSkylightOcclusion(int i, int j)
    {
        updateSkylightColumns[i + j * 16] = true;
        field_40741_v = true;
    }

    private void updateSkylight_do()
    {
        if (worldObj.doChunksNearChunkExist(xPosition * 16 + 8, worldObj.worldHeight / 2, zPosition * 16 + 8, 16))
        {
            for (int i = 0; i < 16; i++)
            {
                for (int j = 0; j < 16; j++)
                {
                    if (!updateSkylightColumns[i + j * 16])
                    {
                        continue;
                    }
                    updateSkylightColumns[i + j * 16] = false;
                    int k = getHeightValue(i, j);
                    int l = xPosition * 16 + i;
                    int i1 = zPosition * 16 + j;
                    int j1 = worldObj.getHeightValue(l - 1, i1);
                    int k1 = worldObj.getHeightValue(l + 1, i1);
                    int l1 = worldObj.getHeightValue(l, i1 - 1);
                    int i2 = worldObj.getHeightValue(l, i1 + 1);
                    if (k1 < j1)
                    {
                        j1 = k1;
                    }
                    if (l1 < j1)
                    {
                        j1 = l1;
                    }
                    if (i2 < j1)
                    {
                        j1 = i2;
                    }
                    checkSkylightNeighborHeight(l, i1, j1);
                    checkSkylightNeighborHeight(l - 1, i1, k);
                    checkSkylightNeighborHeight(l + 1, i1, k);
                    checkSkylightNeighborHeight(l, i1 - 1, k);
                    checkSkylightNeighborHeight(l, i1 + 1, k);
                    field_40741_v = false;
                }
            }
        }
    }

    private void checkSkylightNeighborHeight(int i, int j, int k)
    {
        int l = worldObj.getHeightValue(i, j);
        if (l > k)
        {
            updateSkylightNeighborHeight(i, j, k, l + 1);
        }
        else if (l < k)
        {
            updateSkylightNeighborHeight(i, j, l, k + 1);
        }
    }

    private void updateSkylightNeighborHeight(int i, int j, int k, int l)
    {
        if (l > k && worldObj.doChunksNearChunkExist(i, worldObj.worldHeight / 2, j, 16))
        {
            for (int i1 = k; i1 < l; i1++)
            {
                worldObj.updateLightByType(EnumSkyBlock.Sky, i, i1, j);
            }

            isModified = true;
        }
    }

    private void relightBlock(int i, int j, int k)
    {
        int l = heightMap[k << 4 | i] & 0xff;
        int i1 = l;
        if (j > l)
        {
            i1 = j;
        }
        for (int j1 = i << worldObj.xShift | k << worldObj.heightShift; i1 > 0 && Block.lightOpacity[blocks[(j1 + i1) - 1] & 0xff] == 0; i1--) { }
        if (i1 == l)
        {
            return;
        }
        worldObj.markBlocksDirtyVertical(i, k, i1, l);
        heightMap[k << 4 | i] = (byte)i1;
        if (i1 < lowestBlockHeight)
        {
            lowestBlockHeight = i1;
        }
        else
        {
            int k1 = worldObj.worldHeight - 1;
            for (int i2 = 0; i2 < 16; i2++)
            {
                for (int k2 = 0; k2 < 16; k2++)
                {
                    if ((heightMap[k2 << 4 | i2] & 0xff) < k1)
                    {
                        k1 = heightMap[k2 << 4 | i2] & 0xff;
                    }
                }
            }

            lowestBlockHeight = k1;
        }
        int l1 = xPosition * 16 + i;
        int j2 = zPosition * 16 + k;
        if (!worldObj.worldProvider.hasNoSky)
        {
            if (i1 < l)
            {
                for (int l2 = i1; l2 < l; l2++)
                {
                    skylightMap.setNibble(i, l2, k, 15);
                }
            }
            else
            {
                for (int i3 = l; i3 < i1; i3++)
                {
                    skylightMap.setNibble(i, i3, k, 0);
                }
            }
            for (int j3 = 15; i1 > 0 && j3 > 0;)
            {
                i1--;
                int k3 = Block.lightOpacity[getBlockID(i, i1, k)];
                if (k3 == 0)
                {
                    k3 = 1;
                }
                j3 -= k3;
                if (j3 < 0)
                {
                    j3 = 0;
                }
                skylightMap.setNibble(i, i1, k, j3);
            }
        }
        byte byte0 = heightMap[k << 4 | i];
        int l3 = l;
        int i4 = byte0;
        if (i4 < l3)
        {
            int j4 = l3;
            l3 = i4;
            i4 = j4;
        }
        if (!worldObj.worldProvider.hasNoSky)
        {
            updateSkylightNeighborHeight(l1 - 1, j2, l3, i4);
            updateSkylightNeighborHeight(l1 + 1, j2, l3, i4);
            updateSkylightNeighborHeight(l1, j2 - 1, l3, i4);
            updateSkylightNeighborHeight(l1, j2 + 1, l3, i4);
            updateSkylightNeighborHeight(l1, j2, l3, i4);
        }
        isModified = true;
    }

    public int getBlockID(int i, int j, int k)
    {
        return blocks[i << worldObj.xShift | k << worldObj.heightShift | j] & 0xff;
    }

    public boolean setBlockIDWithMetadata(int i, int j, int k, int l, int i1)
    {
        byte byte0 = (byte)l;
        int j1 = k << 4 | i;
        if (j >= precipitationHeightMap[j1] - 1)
        {
            precipitationHeightMap[j1] = -999;
        }
        int k1 = heightMap[k << 4 | i] & 0xff;
        int l1 = blocks[i << worldObj.xShift | k << worldObj.heightShift | j] & 0xff;
        if (l1 == l && data.getNibble(i, j, k) == i1)
        {
            return false;
        }
        int i2 = xPosition * 16 + i;
        int j2 = zPosition * 16 + k;
        blocks[i << worldObj.xShift | k << worldObj.heightShift | j] = (byte)(byte0 & 0xff);
        if (l1 != 0)
        {
            if (!worldObj.multiplayerWorld)
            {
                Block.blocksList[l1].onBlockRemoval(worldObj, i2, j, j2);
            }
            else if ((Block.blocksList[l1] instanceof BlockContainer) && l1 != l)
            {
                worldObj.removeBlockTileEntity(i2, j, j2);
            }
        }
        data.setNibble(i, j, k, i1);
        if (!worldObj.worldProvider.hasNoSky)
        {
            if (Block.lightOpacity[byte0 & 0xff] != 0)
            {
                if (j >= k1)
                {
                    relightBlock(i, j + 1, k);
                }
            }
            else if (j == k1 - 1)
            {
                relightBlock(i, j, k);
            }
            worldObj.scheduleLightingUpdate(EnumSkyBlock.Sky, i2, j, j2, i2, j, j2);
        }
        worldObj.scheduleLightingUpdate(EnumSkyBlock.Block, i2, j, j2, i2, j, j2);
        propagateSkylightOcclusion(i, k);
        data.setNibble(i, j, k, i1);
        if (l != 0)
        {
            if (!worldObj.multiplayerWorld)
            {
                Block.blocksList[l].onBlockAdded(worldObj, i2, j, j2);
            }
            if (Block.blocksList[l] instanceof BlockContainer)
            {
                TileEntity tileentity = getChunkBlockTileEntity(i, j, k);
                if (tileentity == null)
                {
                    tileentity = ((BlockContainer)Block.blocksList[l]).getBlockEntity();
                    worldObj.setBlockTileEntity(i2, j, j2, tileentity);
                }
                if (tileentity != null)
                {
                    tileentity.updateContainingBlockInfo();
                }
            }
        }
        else if (l1 > 0 && (Block.blocksList[l1] instanceof BlockContainer))
        {
            TileEntity tileentity1 = getChunkBlockTileEntity(i, j, k);
            if (tileentity1 != null)
            {
                tileentity1.updateContainingBlockInfo();
            }
        }
        isModified = true;
        return true;
    }

    public boolean setBlockID(int i, int j, int k, int l)
    {
        byte byte0 = (byte)l;
        int i1 = k << 4 | i;
        if (j >= precipitationHeightMap[i1] - 1)
        {
            precipitationHeightMap[i1] = -999;
        }
        int j1 = heightMap[i1] & 0xff;
        int k1 = blocks[i << worldObj.xShift | k << worldObj.heightShift | j] & 0xff;
        if (k1 == l)
        {
            return false;
        }
        int l1 = xPosition * 16 + i;
        int i2 = zPosition * 16 + k;
        blocks[i << worldObj.xShift | k << worldObj.heightShift | j] = (byte)(byte0 & 0xff);
        if (k1 != 0)
        {
            Block.blocksList[k1].onBlockRemoval(worldObj, l1, j, i2);
        }
        data.setNibble(i, j, k, 0);
        if (Block.lightOpacity[byte0 & 0xff] != 0)
        {
            if (j >= j1)
            {
                relightBlock(i, j + 1, k);
            }
        }
        else if (j == j1 - 1)
        {
            relightBlock(i, j, k);
        }
        worldObj.scheduleLightingUpdate(EnumSkyBlock.Sky, l1, j, i2, l1, j, i2);
        worldObj.scheduleLightingUpdate(EnumSkyBlock.Block, l1, j, i2, l1, j, i2);
        propagateSkylightOcclusion(i, k);
        if (l != 0)
        {
            if (!worldObj.multiplayerWorld)
            {
                Block.blocksList[l].onBlockAdded(worldObj, l1, j, i2);
            }
            if (l > 0 && (Block.blocksList[l] instanceof BlockContainer))
            {
                TileEntity tileentity = getChunkBlockTileEntity(i, j, k);
                if (tileentity == null)
                {
                    tileentity = ((BlockContainer)Block.blocksList[l]).getBlockEntity();
                    worldObj.setBlockTileEntity(l1, j, i2, tileentity);
                }
                if (tileentity != null)
                {
                    tileentity.updateContainingBlockInfo();
                }
            }
        }
        else if (k1 > 0 && (Block.blocksList[k1] instanceof BlockContainer))
        {
            TileEntity tileentity1 = getChunkBlockTileEntity(i, j, k);
            if (tileentity1 != null)
            {
                tileentity1.updateContainingBlockInfo();
            }
        }
        isModified = true;
        return true;
    }

    public int getBlockMetadata(int i, int j, int k)
    {
        return data.getNibble(i, j, k);
    }

    public boolean setBlockMetadata(int i, int j, int k, int l)
    {
        isModified = true;
        int i1 = data.getNibble(i, j, k);
        if (i1 == l)
        {
            return false;
        }
        data.setNibble(i, j, k, l);
        int j1 = getBlockID(i, j, k);
        if (j1 > 0 && (Block.blocksList[j1] instanceof BlockContainer))
        {
            TileEntity tileentity = getChunkBlockTileEntity(i, j, k);
            if (tileentity != null)
            {
                tileentity.updateContainingBlockInfo();
                tileentity.blockMetadata = l;
            }
        }
        return true;
    }

    public int getSavedLightValue(EnumSkyBlock enumskyblock, int i, int j, int k)
    {
        if (enumskyblock == EnumSkyBlock.Sky)
        {
            return skylightMap.getNibble(i, j, k);
        }
        if (enumskyblock == EnumSkyBlock.Block)
        {
            return blocklightMap.getNibble(i, j, k);
        }
        else
        {
            return 0;
        }
    }

    public void setLightValue(EnumSkyBlock enumskyblock, int i, int j, int k, int l)
    {
        isModified = true;
        if (enumskyblock == EnumSkyBlock.Sky)
        {
            if (!worldObj.worldProvider.hasNoSky)
            {
                skylightMap.setNibble(i, j, k, l);
            }
        }
        else if (enumskyblock == EnumSkyBlock.Block)
        {
            blocklightMap.setNibble(i, j, k, l);
        }
        else
        {
            return;
        }
    }

    public int getBlockLightValue(int i, int j, int k, int l)
    {
        int i1 = worldObj.worldProvider.hasNoSky ? 0 : skylightMap.getNibble(i, j, k);
        if (i1 > 0)
        {
            isLit = true;
        }
        i1 -= l;
        int j1 = blocklightMap.getNibble(i, j, k);
        if (j1 > i1)
        {
            i1 = j1;
        }
        return i1;
    }

    public void addEntity(Entity entity)
    {
        hasEntities = true;
        int i = MathHelper.floor_double(entity.posX / 16D);
        int j = MathHelper.floor_double(entity.posZ / 16D);
        if (i != xPosition || j != zPosition)
        {
            System.out.println((new StringBuilder()).append("Wrong location! ").append(entity).toString());
            Thread.dumpStack();
        }
        int k = MathHelper.floor_double(entity.posY / 16D);
        if (k < 0)
        {
            k = 0;
        }
        if (k >= entities.length)
        {
            k = entities.length - 1;
        }
        entity.addedToChunk = true;
        entity.chunkCoordX = xPosition;
        entity.chunkCoordY = k;
        entity.chunkCoordZ = zPosition;
        entities[k].add(entity);
    }

    public void removeEntity(Entity entity)
    {
        removeEntityAtIndex(entity, entity.chunkCoordY);
    }

    public void removeEntityAtIndex(Entity entity, int i)
    {
        if (i < 0)
        {
            i = 0;
        }
        if (i >= entities.length)
        {
            i = entities.length - 1;
        }
        entities[i].remove(entity);
    }

    public boolean canBlockSeeTheSky(int i, int j, int k)
    {
        return j >= (heightMap[k << 4 | i] & 0xff);
    }

    public TileEntity getChunkBlockTileEntity(int i, int j, int k)
    {
        ChunkPosition chunkposition = new ChunkPosition(i, j, k);
        TileEntity tileentity = (TileEntity)chunkTileEntityMap.get(chunkposition);
        if (tileentity == null)
        {
            int l = getBlockID(i, j, k);
            if (!Block.isBlockContainer[l])
            {
                return null;
            }
            if (tileentity == null)
            {
                tileentity = ((BlockContainer)Block.blocksList[l]).getBlockEntity();
                worldObj.setBlockTileEntity(xPosition * 16 + i, j, zPosition * 16 + k, tileentity);
            }
            tileentity = (TileEntity)chunkTileEntityMap.get(chunkposition);
        }
        if (tileentity != null && tileentity.isInvalid())
        {
            chunkTileEntityMap.remove(chunkposition);
            return null;
        }
        else
        {
            return tileentity;
        }
    }

    public void addTileEntity(TileEntity tileentity)
    {
        int i = tileentity.xCoord - xPosition * 16;
        int j = tileentity.yCoord;
        int k = tileentity.zCoord - zPosition * 16;
        setChunkBlockTileEntity(i, j, k, tileentity);
        if (isChunkLoaded)
        {
            worldObj.loadedTileEntityList.add(tileentity);
        }
    }

    public void setChunkBlockTileEntity(int i, int j, int k, TileEntity tileentity)
    {
        ChunkPosition chunkposition = new ChunkPosition(i, j, k);
        tileentity.worldObj = worldObj;
        tileentity.xCoord = xPosition * 16 + i;
        tileentity.yCoord = j;
        tileentity.zCoord = zPosition * 16 + k;
        if (getBlockID(i, j, k) == 0 || !(Block.blocksList[getBlockID(i, j, k)] instanceof BlockContainer))
        {
            return;
        }
        else
        {
            tileentity.validate();
            chunkTileEntityMap.put(chunkposition, tileentity);
            return;
        }
    }

    public void removeChunkBlockTileEntity(int i, int j, int k)
    {
        ChunkPosition chunkposition = new ChunkPosition(i, j, k);
        if (isChunkLoaded)
        {
            TileEntity tileentity = (TileEntity)chunkTileEntityMap.remove(chunkposition);
            if (tileentity != null)
            {
                tileentity.invalidate();
            }
        }
    }

    public void onChunkLoad()
    {
        isChunkLoaded = true;
        worldObj.addTileEntity(chunkTileEntityMap.values());
        for (int i = 0; i < entities.length; i++)
        {
            worldObj.addLoadedEntities(entities[i]);
        }
    }

    public void onChunkUnload()
    {
        isChunkLoaded = false;
        TileEntity tileentity;
        for (Iterator iterator = chunkTileEntityMap.values().iterator(); iterator.hasNext(); worldObj.markEntityForDespawn(tileentity))
        {
            tileentity = (TileEntity)iterator.next();
        }

        for (int i = 0; i < entities.length; i++)
        {
            worldObj.unloadEntities(entities[i]);
        }
    }

    public void setChunkModified()
    {
        isModified = true;
    }

    public void getEntitiesWithinAABBForEntity(Entity entity, AxisAlignedBB axisalignedbb, List list)
    {
        int i = MathHelper.floor_double((axisalignedbb.minY - 2D) / 16D);
        int j = MathHelper.floor_double((axisalignedbb.maxY + 2D) / 16D);
        if (i < 0)
        {
            i = 0;
        }
        if (j >= entities.length)
        {
            j = entities.length - 1;
        }
        for (int k = i; k <= j; k++)
        {
            List list1 = entities[k];
            for (int l = 0; l < list1.size(); l++)
            {
                Entity entity1 = (Entity)list1.get(l);
                if (entity1 == entity || !entity1.boundingBox.intersectsWith(axisalignedbb))
                {
                    continue;
                }
                list.add(entity1);
                Entity aentity[] = entity1.getParts();
                if (aentity == null)
                {
                    continue;
                }
                for (int i1 = 0; i1 < aentity.length; i1++)
                {
                    Entity entity2 = aentity[i1];
                    if (entity2 != entity && entity2.boundingBox.intersectsWith(axisalignedbb))
                    {
                        list.add(entity2);
                    }
                }
            }
        }
    }

    public void getEntitiesOfTypeWithinAAAB(Class class1, AxisAlignedBB axisalignedbb, List list)
    {
        int i = MathHelper.floor_double((axisalignedbb.minY - 2D) / 16D);
        int j = MathHelper.floor_double((axisalignedbb.maxY + 2D) / 16D);
        if (i < 0)
        {
            i = 0;
        }
        else if (i >= entities.length)
        {
            i = entities.length - 1;
        }
        if (j >= entities.length)
        {
            j = entities.length - 1;
        }
        else if (j < 0)
        {
            j = 0;
        }
        for (int k = i; k <= j; k++)
        {
            List list1 = entities[k];
            for (int l = 0; l < list1.size(); l++)
            {
                Entity entity = (Entity)list1.get(l);
                if (class1.isAssignableFrom(entity.getClass()) && entity.boundingBox.intersectsWith(axisalignedbb))
                {
                    list.add(entity);
                }
            }
        }
    }

    public boolean needsSaving(boolean flag)
    {
        if (neverSave)
        {
            return false;
        }
        if (flag)
        {
            if (hasEntities && worldObj.getWorldTime() != lastSaveTime)
            {
                return true;
            }
        }
        else if (hasEntities && worldObj.getWorldTime() >= lastSaveTime + 600L)
        {
            return true;
        }
        return isModified;
    }

    public int setChunkData(byte abyte0[], int i, int j, int k, int l, int i1, int j1,
            int k1)
    {
        for (int l1 = i; l1 < l; l1++)
        {
            for (int l2 = k; l2 < j1; l2++)
            {
                int l3 = l1 << worldObj.xShift | l2 << worldObj.heightShift | j;
                int l4 = i1 - j;
                System.arraycopy(abyte0, k1, blocks, l3, l4);
                k1 += l4;
            }
        }

        generateHeightMap();
        for (int i2 = i; i2 < l; i2++)
        {
            for (int i3 = k; i3 < j1; i3++)
            {
                int i4 = (i2 << worldObj.xShift | i3 << worldObj.heightShift | j) >> 1;
                int i5 = (i1 - j) / 2;
                System.arraycopy(abyte0, k1, data.data, i4, i5);
                k1 += i5;
            }
        }

        for (int j2 = i; j2 < l; j2++)
        {
            for (int j3 = k; j3 < j1; j3++)
            {
                int j4 = (j2 << worldObj.xShift | j3 << worldObj.heightShift | j) >> 1;
                int j5 = (i1 - j) / 2;
                System.arraycopy(abyte0, k1, blocklightMap.data, j4, j5);
                k1 += j5;
            }
        }

        for (int k2 = i; k2 < l; k2++)
        {
            for (int k3 = k; k3 < j1; k3++)
            {
                int k4 = (k2 << worldObj.xShift | k3 << worldObj.heightShift | j) >> 1;
                int k5 = (i1 - j) / 2;
                System.arraycopy(abyte0, k1, skylightMap.data, k4, k5);
                k1 += k5;
            }
        }

        TileEntity tileentity;
        for (Iterator iterator = chunkTileEntityMap.values().iterator(); iterator.hasNext(); tileentity.updateContainingBlockInfo())
        {
            tileentity = (TileEntity)iterator.next();
        }

        /* WORLD DOWNLOADER ---> */
        isFilled = true;
        /* <--- WORLD DOWNLOADER */

        return k1;
    }

    public Random getRandomWithSeed(long l)
    {
        return new Random(worldObj.getWorldSeed() + (long)(xPosition * xPosition * 0x4c1906) + (long)(xPosition * 0x5ac0db) + (long)(zPosition * zPosition) * 0x4307a7L + (long)(zPosition * 0x5f24f) ^ l);
    }

    public boolean isEmpty()
    {
        return false;
    }

    public void removeUnknownBlocks()
    {
        ChunkBlockMap.removeUnknownBlockIDs(blocks);
    }

    public void populateChunk(IChunkProvider ichunkprovider, IChunkProvider ichunkprovider1, int i, int j)
    {
        if (!isTerrainPopulated && ichunkprovider.chunkExists(i + 1, j + 1) && ichunkprovider.chunkExists(i, j + 1) && ichunkprovider.chunkExists(i + 1, j))
        {
            ichunkprovider.populate(ichunkprovider1, i, j);
        }
        if (ichunkprovider.chunkExists(i - 1, j) && !ichunkprovider.provideChunk(i - 1, j).isTerrainPopulated && ichunkprovider.chunkExists(i - 1, j + 1) && ichunkprovider.chunkExists(i, j + 1) && ichunkprovider.chunkExists(i - 1, j + 1))
        {
            ichunkprovider.populate(ichunkprovider1, i - 1, j);
        }
        if (ichunkprovider.chunkExists(i, j - 1) && !ichunkprovider.provideChunk(i, j - 1).isTerrainPopulated && ichunkprovider.chunkExists(i + 1, j - 1) && ichunkprovider.chunkExists(i + 1, j - 1) && ichunkprovider.chunkExists(i + 1, j))
        {
            ichunkprovider.populate(ichunkprovider1, i, j - 1);
        }
        if (ichunkprovider.chunkExists(i - 1, j - 1) && !ichunkprovider.provideChunk(i - 1, j - 1).isTerrainPopulated && ichunkprovider.chunkExists(i, j - 1) && ichunkprovider.chunkExists(i - 1, j))
        {
            ichunkprovider.populate(ichunkprovider1, i - 1, j - 1);
        }
    }

    public int getPrecipitationHeight(int i, int j)
    {
        int k = i | j << 4;
        int l = precipitationHeightMap[k];
        if (l == -999)
        {
            int i1 = worldObj.worldHeight - 1;
            for (l = -1; i1 > 0 && l == -1;)
            {
                int j1 = getBlockID(i, i1, j);
                Material material = j1 != 0 ? Block.blocksList[j1].blockMaterial : Material.air;
                if (!material.getIsSolid() && !material.getIsLiquid())
                {
                    i1--;
                }
                else
                {
                    l = i1 + 1;
                }
            }

            precipitationHeightMap[k] = l;
        }
        return l;
    }

    public void updateSkylight()
    {
        if (field_40741_v && !worldObj.worldProvider.hasNoSky)
        {
            updateSkylight_do();
        }
    }

    public ChunkCoordIntPair getChunkCoordIntPair()
    {
        return new ChunkCoordIntPair(xPosition, zPosition);
    }
	/* WORLD DOWNLOADER ---> */
	public void importOldChunkTileEntities()
	{
        File file = WorldDL.mySaveHandler.getSaveDirectory();
        if(WorldDL.wc.worldProvider instanceof WorldProviderHell)
        {
            file = new File(file, "DIM-1");
            file.mkdirs();
        }
        if(WorldDL.wc.worldProvider instanceof WorldProviderEnd)
        {
            file = new File(file, "DIM1");
            file.mkdirs();
        }
		
        java.io.DataInputStream datainputstream = RegionFileCache.getChunkInputStream(file, xPosition, zPosition);
        NBTTagCompound nbttagcompound;
        if(datainputstream != null)
        {
            try {
				nbttagcompound = CompressedStreamTools.read(datainputstream);
			} catch (IOException e) {
				return;
			}
        }
        else return;
        
        if( !nbttagcompound.hasKey("Level") )
        	return;
		
        NBTTagList nbttaglist1 = nbttagcompound.getCompoundTag("Level").getTagList("TileEntities");
        if(nbttaglist1 != null)
        {
            for(int l = 0; l < nbttaglist1.tagCount(); l++)
            {
                NBTTagCompound nbttagcompound2 = (NBTTagCompound)nbttaglist1.tagAt(l);
                TileEntity te = TileEntity.createAndLoadEntity(nbttagcompound2);
                if(te != null )
                {
                	ChunkPosition cp = new ChunkPosition(te.xCoord & 0xf, te.yCoord, te.zCoord & 0xf);
                	te.worldObj = worldObj;
                    myChunkTileEntityMap.put(cp, te);
                }
            }
        }
	}
	
	public void setMyChunkBlockTileEntity(int i, int j, int k, TileEntity tileentity)
    {
        ChunkPosition chunkposition = new ChunkPosition(i, j, k);
        tileentity.worldObj = worldObj;
        tileentity.xCoord = xPosition * 16 + i;
        tileentity.yCoord = j;
        tileentity.zCoord = zPosition * 16 + k;
        myChunkTileEntityMap.put(chunkposition, tileentity);
    }
	/* <--- WORLD DOWNLOADER */

}
