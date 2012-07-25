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
    /**
     * Determines if the chunk is lit or not at a light value greater than 0.
     */
    public static boolean isLit;
    private ExtendedBlockStorage field_48505_p[];
    private byte field_48504_q[];
    public int precipitationHeightMap[];
    public boolean updateSkylightColumns[];

    /** Whether or not this Chunk is currently loaded into the World */
    public boolean isChunkLoaded;

    /** Reference to the World object. */
    public World worldObj;
    public int field_48501_f[];

    /** The x coordinate of the chunk. */
    public final int xPosition;

    /** The z coordinate of the chunk. */
    public final int zPosition;
    private boolean field_40741_v;

    /** A Map of ChunkPositions to TileEntities in this chunk */
    public Map chunkTileEntityMap;
    public List field_48502_j[];

    /** Boolean value indicating if the terrain is populated. */
    public boolean isTerrainPopulated;

    /**
     * Set to true if the chunk has been modified and needs to be updated internally.
     */
    public boolean isModified;

    /**
     * Whether this Chunk has any Entities and thus requires saving on every tick
     */
    public boolean hasEntities;

    /** The time according to World.worldTime when this chunk was last saved */
    public long lastSaveTime;
    private int field_48503_s;
    boolean field_35846_u;

    /* WORLD DOWNLOADER ---> */
    public boolean isFilled = false; // Used to only save chunks that have already been received.
    public Map myChunkTileEntityMap; // Copy of the TileEntity data that only my code touches (never overwritten by junk)
    /* <--- WORLD DOWNLOADER */

    
    public Chunk(World par1World, int par2, int par3)
    {
        field_48505_p = new ExtendedBlockStorage[16];
        field_48504_q = new byte[256];
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
        field_48503_s = 4096;
        field_35846_u = false;
        field_48502_j = new List[16];
        worldObj = par1World;
        xPosition = par2;
        zPosition = par3;
        field_48501_f = new int[256];

        for (int i = 0; i < field_48502_j.length; i++)
        {
            field_48502_j[i] = new ArrayList();
        }

        Arrays.fill(precipitationHeightMap, -999);
        Arrays.fill(field_48504_q, (byte) - 1);
    }

    public Chunk(World par1World, byte par2ArrayOfByte[], int par3, int par4)
    {
        this(par1World, par3, par4);
        int i = par2ArrayOfByte.length / 256;

        for (int j = 0; j < 16; j++)
        {
            for (int k = 0; k < 16; k++)
            {
                for (int l = 0; l < i; l++)
                {
                    byte byte0 = par2ArrayOfByte[j << 11 | k << 7 | l];

                    if (byte0 == 0)
                    {
                        continue;
                    }

                    int i1 = l >> 4;

                    if (field_48505_p[i1] == null)
                    {
                        field_48505_p[i1] = new ExtendedBlockStorage(i1 << 4);
                    }

                    field_48505_p[i1].func_48691_a(j, l & 0xf, k, byte0);
                }
            }
        }
    }

    /**
     * Checks whether the chunk is at the X/Z location specified
     */
    public boolean isAtLocation(int par1, int par2)
    {
        return par1 == xPosition && par2 == zPosition;
    }

    /**
     * Returns the value in the height map at this x, z coordinate in the chunk
     */
    public int getHeightValue(int par1, int par2)
    {
        return field_48501_f[par2 << 4 | par1];
    }

    public int func_48498_h()
    {
        for (int i = field_48505_p.length - 1; i >= 0; i--)
        {
            if (field_48505_p[i] != null)
            {
                return field_48505_p[i].func_48707_c();
            }
        }

        return 0;
    }

    public ExtendedBlockStorage[] func_48495_i()
    {
        return field_48505_p;
    }

    /**
     * Generates the height map for a chunk from scratch
     */
    public void generateHeightMap()
    {
        int i = func_48498_h();

        for (int j = 0; j < 16; j++)
        {
            label0:

            for (int k = 0; k < 16; k++)
            {
                precipitationHeightMap[j + (k << 4)] = -999;
                int l = (i + 16) - 1;

                do
                {
                    if (l <= 0)
                    {
                        continue label0;
                    }

                    int i1 = getBlockID(j, l - 1, k);

                    if (Block.lightOpacity[i1] != 0)
                    {
                        field_48501_f[k << 4 | j] = l;
                        continue label0;
                    }

                    l--;
                }
                while (true);
            }
        }

        isModified = true;
    }

    /**
     * Generates the initial skylight map for the chunk upon generation or load.
     */
    public void generateSkylightMap()
    {
        int i = func_48498_h();

        for (int j = 0; j < 16; j++)
        {
            for (int l = 0; l < 16; l++)
            {
                precipitationHeightMap[j + (l << 4)] = -999;
                int j1 = (i + 16) - 1;

                do
                {
                    if (j1 <= 0)
                    {
                        break;
                    }

                    if (func_48499_b(j, j1 - 1, l) != 0)
                    {
                        field_48501_f[l << 4 | j] = j1;
                        break;
                    }

                    j1--;
                }
                while (true);

                if (worldObj.worldProvider.hasNoSky)
                {
                    continue;
                }

                j1 = 15;
                int k1 = (i + 16) - 1;

                do
                {
                    j1 -= func_48499_b(j, k1, l);

                    if (j1 > 0)
                    {
                        ExtendedBlockStorage extendedblockstorage = field_48505_p[k1 >> 4];

                        if (extendedblockstorage != null)
                        {
                            extendedblockstorage.func_48702_c(j, k1 & 0xf, l, j1);
                            worldObj.func_48464_p((xPosition << 4) + j, k1, (zPosition << 4) + l);
                        }
                    }
                }
                while (--k1 > 0 && j1 > 0);
            }
        }

        isModified = true;

        for (int k = 0; k < 16; k++)
        {
            for (int i1 = 0; i1 < 16; i1++)
            {
                propagateSkylightOcclusion(k, i1);
            }
        }
    }

    public void func_4143_d()
    {
    }

    /**
     * Propagates a given sky-visible block's light value downward and upward to neighboring blocks as necessary.
     */
    private void propagateSkylightOcclusion(int par1, int par2)
    {
        updateSkylightColumns[par1 + par2 * 16] = true;
        field_40741_v = true;
    }

    /**
     * Runs delayed skylight updates.
     */
    private void updateSkylight_do()
    {
        Profiler.startSection("recheckGaps");

        if (worldObj.doChunksNearChunkExist(xPosition * 16 + 8, 0, zPosition * 16 + 8, 16))
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
                }
            }

            field_40741_v = false;
        }

        Profiler.endSection();
    }

    /**
     * Checks the height of a block next to a sky-visible block and schedules a lighting update as necessary.
     */
    private void checkSkylightNeighborHeight(int par1, int par2, int par3)
    {
        int i = worldObj.getHeightValue(par1, par2);

        if (i > par3)
        {
            updateSkylightNeighborHeight(par1, par2, par3, i + 1);
        }
        else if (i < par3)
        {
            updateSkylightNeighborHeight(par1, par2, i, par3 + 1);
        }
    }

    private void updateSkylightNeighborHeight(int par1, int par2, int par3, int par4)
    {
        if (par4 > par3 && worldObj.doChunksNearChunkExist(par1, 0, par2, 16))
        {
            for (int i = par3; i < par4; i++)
            {
                worldObj.updateLightByType(EnumSkyBlock.Sky, par1, i, par2);
            }

            isModified = true;
        }
    }

    /**
     * Initiates the recalculation of both the block-light and sky-light for a given block inside a chunk.
     */
    private void relightBlock(int par1, int par2, int par3)
    {
        int i = field_48501_f[par3 << 4 | par1];
        int j = i;

        if (par2 > i)
        {
            j = par2;
        }

        for (; j > 0 && func_48499_b(par1, j - 1, par3) == 0; j--) { }

        if (j == i)
        {
            return;
        }

        worldObj.markBlocksDirtyVertical(par1, par3, j, i);
        field_48501_f[par3 << 4 | par1] = j;
        int k = xPosition * 16 + par1;
        int l = zPosition * 16 + par3;

        if (!worldObj.worldProvider.hasNoSky)
        {
            if (j < i)
            {
                for (int i1 = j; i1 < i; i1++)
                {
                    ExtendedBlockStorage extendedblockstorage = field_48505_p[i1 >> 4];

                    if (extendedblockstorage != null)
                    {
                        extendedblockstorage.func_48702_c(par1, i1 & 0xf, par3, 15);
                        worldObj.func_48464_p((xPosition << 4) + par1, i1, (zPosition << 4) + par3);
                    }
                }
            }
            else
            {
                for (int j1 = i; j1 < j; j1++)
                {
                    ExtendedBlockStorage extendedblockstorage1 = field_48505_p[j1 >> 4];

                    if (extendedblockstorage1 != null)
                    {
                        extendedblockstorage1.func_48702_c(par1, j1 & 0xf, par3, 0);
                        worldObj.func_48464_p((xPosition << 4) + par1, j1, (zPosition << 4) + par3);
                    }
                }
            }

            int k1 = 15;

            do
            {
                if (j <= 0 || k1 <= 0)
                {
                    break;
                }

                j--;
                int i2 = func_48499_b(par1, j, par3);

                if (i2 == 0)
                {
                    i2 = 1;
                }

                k1 -= i2;

                if (k1 < 0)
                {
                    k1 = 0;
                }

                ExtendedBlockStorage extendedblockstorage2 = field_48505_p[j >> 4];

                if (extendedblockstorage2 != null)
                {
                    extendedblockstorage2.func_48702_c(par1, j & 0xf, par3, k1);
                }
            }
            while (true);
        }

        int l1 = field_48501_f[par3 << 4 | par1];
        int j2 = i;
        int k2 = l1;

        if (k2 < j2)
        {
            int l2 = j2;
            j2 = k2;
            k2 = l2;
        }

        if (!worldObj.worldProvider.hasNoSky)
        {
            updateSkylightNeighborHeight(k - 1, l, j2, k2);
            updateSkylightNeighborHeight(k + 1, l, j2, k2);
            updateSkylightNeighborHeight(k, l - 1, j2, k2);
            updateSkylightNeighborHeight(k, l + 1, j2, k2);
            updateSkylightNeighborHeight(k, l, j2, k2);
        }

        isModified = true;
    }

    public int func_48499_b(int par1, int par2, int par3)
    {
        return Block.lightOpacity[getBlockID(par1, par2, par3)];
    }

    /**
     * Return the ID of a block in the chunk.
     */
    public int getBlockID(int par1, int par2, int par3)
    {
        ExtendedBlockStorage extendedblockstorage = field_48505_p[par2 >> 4];

        if (extendedblockstorage != null)
        {
            return extendedblockstorage.func_48703_a(par1, par2 & 0xf, par3);
        }
        else
        {
            return 0;
        }
    }

    /**
     * Return the metadata corresponding to the given coordinates inside a chunk.
     */
    public int getBlockMetadata(int par1, int par2, int par3)
    {
        ExtendedBlockStorage extendedblockstorage = field_48505_p[par2 >> 4];

        if (extendedblockstorage != null)
        {
            return extendedblockstorage.func_48694_b(par1, par2 & 0xf, par3);
        }
        else
        {
            return 0;
        }
    }

    /**
     * Sets a blockID for a position in the chunk. Args: x, y, z, blockID
     */
    public boolean setBlockID(int par1, int par2, int par3, int par4)
    {
        return setBlockIDWithMetadata(par1, par2, par3, par4, 0);
    }

    /**
     * Sets a blockID of a position within a chunk with metadata. Args: x, y, z, blockID, metadata
     */
    public boolean setBlockIDWithMetadata(int par1, int par2, int par3, int par4, int par5)
    {
        int i = par3 << 4 | par1;

        if (par2 >= precipitationHeightMap[i] - 1)
        {
            precipitationHeightMap[i] = -999;
        }

        int j = field_48501_f[i];
        int k = getBlockID(par1, par2, par3);

        if (k == par4 && getBlockMetadata(par1, par2, par3) == par5)
        {
            return false;
        }

        ExtendedBlockStorage extendedblockstorage = field_48505_p[par2 >> 4];
        boolean flag = false;

        if (extendedblockstorage == null)
        {
            if (par4 == 0)
            {
                return false;
            }

            extendedblockstorage = field_48505_p[par2 >> 4] = new ExtendedBlockStorage((par2 >> 4) << 4);
            flag = par2 >= j;
        }

        extendedblockstorage.func_48691_a(par1, par2 & 0xf, par3, par4);
        int l = xPosition * 16 + par1;
        int i1 = zPosition * 16 + par3;

        if (k != 0)
        {
            if (!worldObj.isRemote)
            {
                Block.blocksList[k].onBlockRemoval(worldObj, l, par2, i1);
            }
            else if ((Block.blocksList[k] instanceof BlockContainer) && k != par4)
            {
                worldObj.removeBlockTileEntity(l, par2, i1);
            }
        }

        extendedblockstorage.func_48690_b(par1, par2 & 0xf, par3, par5);

        if (flag)
        {
            generateSkylightMap();
        }
        else
        {
            if (Block.lightOpacity[par4 & 0xfff] > 0)
            {
                if (par2 > j)
                {
                    relightBlock(par1, par2 + 1, par3);
                }
            }
            else if (par2 == j - 1)
            {
                relightBlock(par1, par2, par3);
            }

            propagateSkylightOcclusion(par1, par3);
        }

        if (par4 != 0)
        {
            if (!worldObj.isRemote)
            {
                Block.blocksList[par4].onBlockAdded(worldObj, l, par2, i1);
            }

            if (Block.blocksList[par4] instanceof BlockContainer)
            {
                TileEntity tileentity = getChunkBlockTileEntity(par1, par2, par3);

                if (tileentity == null)
                {
                    tileentity = ((BlockContainer)Block.blocksList[par4]).getBlockEntity();
                    worldObj.setBlockTileEntity(l, par2, i1, tileentity);
                }

                if (tileentity != null)
                {
                    tileentity.updateContainingBlockInfo();
                }
            }
        }
        else if (k > 0 && (Block.blocksList[k] instanceof BlockContainer))
        {
            TileEntity tileentity1 = getChunkBlockTileEntity(par1, par2, par3);

            if (tileentity1 != null)
            {
                tileentity1.updateContainingBlockInfo();
            }
        }

        isModified = true;
        return true;
    }

    /**
     * Set the metadata of a block in the chunk
     */
    public boolean setBlockMetadata(int par1, int par2, int par3, int par4)
    {
        ExtendedBlockStorage extendedblockstorage = field_48505_p[par2 >> 4];

        if (extendedblockstorage == null)
        {
            return false;
        }

        int i = extendedblockstorage.func_48694_b(par1, par2 & 0xf, par3);

        if (i == par4)
        {
            return false;
        }

        isModified = true;
        extendedblockstorage.func_48690_b(par1, par2 & 0xf, par3, par4);
        int j = extendedblockstorage.func_48703_a(par1, par2 & 0xf, par3);

        if (j > 0 && (Block.blocksList[j] instanceof BlockContainer))
        {
            TileEntity tileentity = getChunkBlockTileEntity(par1, par2, par3);

            if (tileentity != null)
            {
                tileentity.updateContainingBlockInfo();
                tileentity.blockMetadata = par4;
            }
        }

        return true;
    }

    /**
     * Gets the amount of light saved in this block (doesn't adjust for daylight)
     */
    public int getSavedLightValue(EnumSkyBlock par1EnumSkyBlock, int par2, int par3, int par4)
    {
        ExtendedBlockStorage extendedblockstorage = field_48505_p[par3 >> 4];

        if (extendedblockstorage == null)
        {
            return par1EnumSkyBlock.defaultLightValue;
        }

        if (par1EnumSkyBlock == EnumSkyBlock.Sky)
        {
            return extendedblockstorage.func_48709_c(par2, par3 & 0xf, par4);
        }

        if (par1EnumSkyBlock == EnumSkyBlock.Block)
        {
            return extendedblockstorage.func_48712_d(par2, par3 & 0xf, par4);
        }
        else
        {
            return par1EnumSkyBlock.defaultLightValue;
        }
    }

    /**
     * Sets the light value at the coordinate. If enumskyblock is set to sky it sets it in the skylightmap and if its a
     * block then into the blocklightmap. Args enumSkyBlock, x, y, z, lightValue
     */
    public void setLightValue(EnumSkyBlock par1EnumSkyBlock, int par2, int par3, int par4, int par5)
    {
        ExtendedBlockStorage extendedblockstorage = field_48505_p[par3 >> 4];

        if (extendedblockstorage == null)
        {
            extendedblockstorage = field_48505_p[par3 >> 4] = new ExtendedBlockStorage((par3 >> 4) << 4);
            generateSkylightMap();
        }

        isModified = true;

        if (par1EnumSkyBlock == EnumSkyBlock.Sky)
        {
            if (!worldObj.worldProvider.hasNoSky)
            {
                extendedblockstorage.func_48702_c(par2, par3 & 0xf, par4, par5);
            }
        }
        else if (par1EnumSkyBlock == EnumSkyBlock.Block)
        {
            extendedblockstorage.func_48699_d(par2, par3 & 0xf, par4, par5);
        }
        else
        {
            return;
        }
    }

    /**
     * Gets the amount of light on a block taking into account sunlight
     */
    public int getBlockLightValue(int par1, int par2, int par3, int par4)
    {
        ExtendedBlockStorage extendedblockstorage = field_48505_p[par2 >> 4];

        if (extendedblockstorage == null)
        {
            if (!worldObj.worldProvider.hasNoSky && par4 < EnumSkyBlock.Sky.defaultLightValue)
            {
                return EnumSkyBlock.Sky.defaultLightValue - par4;
            }
            else
            {
                return 0;
            }
        }

        int i = worldObj.worldProvider.hasNoSky ? 0 : extendedblockstorage.func_48709_c(par1, par2 & 0xf, par3);

        if (i > 0)
        {
            isLit = true;
        }

        i -= par4;
        int j = extendedblockstorage.func_48712_d(par1, par2 & 0xf, par3);

        if (j > i)
        {
            i = j;
        }

        return i;
    }

    /**
     * Adds an entity to the chunk. Args: entity
     */
    public void addEntity(Entity par1Entity)
    {
        hasEntities = true;
        int i = MathHelper.floor_double(par1Entity.posX / 16D);
        int j = MathHelper.floor_double(par1Entity.posZ / 16D);

        if (i != xPosition || j != zPosition)
        {
            System.out.println((new StringBuilder()).append("Wrong location! ").append(par1Entity).toString());
            Thread.dumpStack();
        }

        int k = MathHelper.floor_double(par1Entity.posY / 16D);

        if (k < 0)
        {
            k = 0;
        }

        if (k >= field_48502_j.length)
        {
            k = field_48502_j.length - 1;
        }

        par1Entity.addedToChunk = true;
        par1Entity.chunkCoordX = xPosition;
        par1Entity.chunkCoordY = k;
        par1Entity.chunkCoordZ = zPosition;
        field_48502_j[k].add(par1Entity);
    }

    /**
     * removes entity using its y chunk coordinate as its index
     */
    public void removeEntity(Entity par1Entity)
    {
        removeEntityAtIndex(par1Entity, par1Entity.chunkCoordY);
    }

    /**
     * removes entity at index i from entity array
     */
    public void removeEntityAtIndex(Entity par1Entity, int par2)
    {
        if (par2 < 0)
        {
            par2 = 0;
        }

        if (par2 >= field_48502_j.length)
        {
            par2 = field_48502_j.length - 1;
        }

        field_48502_j[par2].remove(par1Entity);
    }

    /**
     * Returns whether is not a block above this one blocking sight to the sky (done via checking against the heightmap)
     */
    public boolean canBlockSeeTheSky(int par1, int par2, int par3)
    {
        return par2 >= field_48501_f[par3 << 4 | par1];
    }

    /**
     * Gets the TileEntity for a given block in this chunk
     */
    public TileEntity getChunkBlockTileEntity(int par1, int par2, int par3)
    {
        ChunkPosition chunkposition = new ChunkPosition(par1, par2, par3);
        TileEntity tileentity = (TileEntity)chunkTileEntityMap.get(chunkposition);

        if (tileentity == null)
        {
            int i = getBlockID(par1, par2, par3);

            if (i <= 0 || !Block.blocksList[i].func_48205_p())
            {
                return null;
            }

            if (tileentity == null)
            {
                tileentity = ((BlockContainer)Block.blocksList[i]).getBlockEntity();
                worldObj.setBlockTileEntity(xPosition * 16 + par1, par2, zPosition * 16 + par3, tileentity);
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

    /**
     * Adds a TileEntity to a chunk
     */
    public void addTileEntity(TileEntity par1TileEntity)
    {
        int i = par1TileEntity.xCoord - xPosition * 16;
        int j = par1TileEntity.yCoord;
        int k = par1TileEntity.zCoord - zPosition * 16;
        setChunkBlockTileEntity(i, j, k, par1TileEntity);

        if (isChunkLoaded)
        {
            worldObj.loadedTileEntityList.add(par1TileEntity);
        }
    }

    /**
     * Sets the TileEntity for a given block in this chunk
     */
    public void setChunkBlockTileEntity(int par1, int par2, int par3, TileEntity par4TileEntity)
    {
        ChunkPosition chunkposition = new ChunkPosition(par1, par2, par3);
        par4TileEntity.worldObj = worldObj;
        par4TileEntity.xCoord = xPosition * 16 + par1;
        par4TileEntity.yCoord = par2;
        par4TileEntity.zCoord = zPosition * 16 + par3;

        if (getBlockID(par1, par2, par3) == 0 || !(Block.blocksList[getBlockID(par1, par2, par3)] instanceof BlockContainer))
        {
            return;
        }
        else
        {
            par4TileEntity.validate();
            chunkTileEntityMap.put(chunkposition, par4TileEntity);
            return;
        }
    }

    /**
     * Removes the TileEntity for a given block in this chunk
     */
    public void removeChunkBlockTileEntity(int par1, int par2, int par3)
    {
        ChunkPosition chunkposition = new ChunkPosition(par1, par2, par3);

        if (isChunkLoaded)
        {
            TileEntity tileentity = (TileEntity)chunkTileEntityMap.remove(chunkposition);

            if (tileentity != null)
            {
                tileentity.invalidate();
            }
        }
    }

    /**
     * Called when this Chunk is loaded by the ChunkProvider
     */
    public void onChunkLoad()
    {
        isChunkLoaded = true;
        worldObj.addTileEntity(chunkTileEntityMap.values());

        for (int i = 0; i < field_48502_j.length; i++)
        {
            worldObj.addLoadedEntities(field_48502_j[i]);
        }
    }

    /**
     * Called when this Chunk is unloaded by the ChunkProvider
     */
    public void onChunkUnload()
    {
        isChunkLoaded = false;
        TileEntity tileentity;

        for (Iterator iterator = chunkTileEntityMap.values().iterator(); iterator.hasNext(); worldObj.markTileEntityForDespawn(tileentity))
        {
            tileentity = (TileEntity)iterator.next();
        }

        for (int i = 0; i < field_48502_j.length; i++)
        {
            worldObj.unloadEntities(field_48502_j[i]);
        }
    }

    /**
     * Sets the isModified flag for this Chunk
     */
    public void setChunkModified()
    {
        isModified = true;
    }

    /**
     * Fills the given list of all entities that intersect within the given bounding box that aren't the passed entity
     * Args: entity, aabb, listToFill
     */
    public void getEntitiesWithinAABBForEntity(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB, List par3List)
    {
        int i = MathHelper.floor_double((par2AxisAlignedBB.minY - 2D) / 16D);
        int j = MathHelper.floor_double((par2AxisAlignedBB.maxY + 2D) / 16D);

        if (i < 0)
        {
            i = 0;
        }

        if (j >= field_48502_j.length)
        {
            j = field_48502_j.length - 1;
        }

        for (int k = i; k <= j; k++)
        {
            List list = field_48502_j[k];

            for (int l = 0; l < list.size(); l++)
            {
                Entity entity = (Entity)list.get(l);

                if (entity == par1Entity || !entity.boundingBox.intersectsWith(par2AxisAlignedBB))
                {
                    continue;
                }

                par3List.add(entity);
                Entity aentity[] = entity.getParts();

                if (aentity == null)
                {
                    continue;
                }

                for (int i1 = 0; i1 < aentity.length; i1++)
                {
                    Entity entity1 = aentity[i1];

                    if (entity1 != par1Entity && entity1.boundingBox.intersectsWith(par2AxisAlignedBB))
                    {
                        par3List.add(entity1);
                    }
                }
            }
        }
    }

    /**
     * Gets all entities that can be assigned to the specified class. Args: entityClass, aabb, listToFill
     */
    public void getEntitiesOfTypeWithinAAAB(Class par1Class, AxisAlignedBB par2AxisAlignedBB, List par3List)
    {
        int i = MathHelper.floor_double((par2AxisAlignedBB.minY - 2D) / 16D);
        int j = MathHelper.floor_double((par2AxisAlignedBB.maxY + 2D) / 16D);

        if (i < 0)
        {
            i = 0;
        }
        else if (i >= field_48502_j.length)
        {
            i = field_48502_j.length - 1;
        }

        if (j >= field_48502_j.length)
        {
            j = field_48502_j.length - 1;
        }
        else if (j < 0)
        {
            j = 0;
        }

        for (int k = i; k <= j; k++)
        {
            List list = field_48502_j[k];

            for (int l = 0; l < list.size(); l++)
            {
                Entity entity = (Entity)list.get(l);

                if (par1Class.isAssignableFrom(entity.getClass()) && entity.boundingBox.intersectsWith(par2AxisAlignedBB))
                {
                    par3List.add(entity);
                }
            }
        }
    }

    /**
     * Returns true if this Chunk needs to be saved
     */
    public boolean needsSaving(boolean par1)
    {
        if (par1)
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

    public Random getRandomWithSeed(long par1)
    {
        return new Random(worldObj.getSeed() + (long)(xPosition * xPosition * 0x4c1906) + (long)(xPosition * 0x5ac0db) + (long)(zPosition * zPosition) * 0x4307a7L + (long)(zPosition * 0x5f24f) ^ par1);
    }

    public boolean isEmpty()
    {
        return false;
    }

    /**
     * Turns unknown blocks into air blocks to avoid crashing Minecraft.
     */
    public void removeUnknownBlocks()
    {
        ExtendedBlockStorage aextendedblockstorage[] = field_48505_p;
        int i = aextendedblockstorage.length;

        for (int j = 0; j < i; j++)
        {
            ExtendedBlockStorage extendedblockstorage = aextendedblockstorage[j];

            if (extendedblockstorage != null)
            {
                extendedblockstorage.func_48711_e();
            }
        }
    }

    public void populateChunk(IChunkProvider par1IChunkProvider, IChunkProvider par2IChunkProvider, int par3, int par4)
    {
        if (!isTerrainPopulated && par1IChunkProvider.chunkExists(par3 + 1, par4 + 1) && par1IChunkProvider.chunkExists(par3, par4 + 1) && par1IChunkProvider.chunkExists(par3 + 1, par4))
        {
            par1IChunkProvider.populate(par2IChunkProvider, par3, par4);
        }

        if (par1IChunkProvider.chunkExists(par3 - 1, par4) && !par1IChunkProvider.provideChunk(par3 - 1, par4).isTerrainPopulated && par1IChunkProvider.chunkExists(par3 - 1, par4 + 1) && par1IChunkProvider.chunkExists(par3, par4 + 1) && par1IChunkProvider.chunkExists(par3 - 1, par4 + 1))
        {
            par1IChunkProvider.populate(par2IChunkProvider, par3 - 1, par4);
        }

        if (par1IChunkProvider.chunkExists(par3, par4 - 1) && !par1IChunkProvider.provideChunk(par3, par4 - 1).isTerrainPopulated && par1IChunkProvider.chunkExists(par3 + 1, par4 - 1) && par1IChunkProvider.chunkExists(par3 + 1, par4 - 1) && par1IChunkProvider.chunkExists(par3 + 1, par4))
        {
            par1IChunkProvider.populate(par2IChunkProvider, par3, par4 - 1);
        }

        if (par1IChunkProvider.chunkExists(par3 - 1, par4 - 1) && !par1IChunkProvider.provideChunk(par3 - 1, par4 - 1).isTerrainPopulated && par1IChunkProvider.chunkExists(par3, par4 - 1) && par1IChunkProvider.chunkExists(par3 - 1, par4))
        {
            par1IChunkProvider.populate(par2IChunkProvider, par3 - 1, par4 - 1);
        }
    }

    /**
     * Gets the height to which rain/snow will fall. Calculates it if not already stored.
     */
    public int getPrecipitationHeight(int par1, int par2)
    {
        int i = par1 | par2 << 4;
        int j = precipitationHeightMap[i];

        if (j == -999)
        {
            int k = func_48498_h() + 15;

            for (j = -1; k > 0 && j == -1;)
            {
                int l = getBlockID(par1, k, par2);
                Material material = l != 0 ? Block.blocksList[l].blockMaterial : Material.air;

                if (!material.blocksMovement() && !material.isLiquid())
                {
                    k--;
                }
                else
                {
                    j = k + 1;
                }
            }

            precipitationHeightMap[i] = j;
        }

        return j;
    }

    /**
     * Checks whether skylight needs updated; if it does, calls updateSkylight_do ( aka updateSkylight_do() ).
     */
    public void updateSkylight()
    {
        if (field_40741_v && !worldObj.worldProvider.hasNoSky)
        {
            updateSkylight_do();
        }
    }

    /**
     * Gets a ChunkCoordIntPair representing the Chunk's position.
     */
    public ChunkCoordIntPair getChunkCoordIntPair()
    {
        return new ChunkCoordIntPair(xPosition, zPosition);
    }

    public boolean func_48492_c(int par1, int par2)
    {
        if (par1 < 0)
        {
            par1 = 0;
        }

        if (par2 >= 256)
        {
            par2 = 255;
        }

        for (int i = par1; i <= par2; i += 16)
        {
            ExtendedBlockStorage extendedblockstorage = field_48505_p[i >> 4];

            if (extendedblockstorage != null && !extendedblockstorage.func_48693_a())
            {
                return false;
            }
        }

        return true;
    }

    public void func_48500_a(ExtendedBlockStorage par1ArrayOfExtendedBlockStorage[])
    {
        field_48505_p = par1ArrayOfExtendedBlockStorage;
    }

    public void func_48494_a(byte par1ArrayOfByte[], int par2, int par3, boolean par4)
    {
        int i = 0;

        for (int j = 0; j < field_48505_p.length; j++)
        {
            if ((par2 & 1 << j) != 0)
            {
                if (field_48505_p[j] == null)
                {
                    field_48505_p[j] = new ExtendedBlockStorage(j << 4);
                }

                byte abyte0[] = field_48505_p[j].func_48692_g();
                System.arraycopy(par1ArrayOfByte, i, abyte0, 0, abyte0.length);
                i += abyte0.length;
                continue;
            }

            if (par4 && field_48505_p[j] != null)
            {
                field_48505_p[j] = null;
            }
        }

        for (int k = 0; k < field_48505_p.length; k++)
        {
            if ((par2 & 1 << k) != 0 && field_48505_p[k] != null)
            {
                NibbleArray nibblearray = field_48505_p[k].func_48697_j();
                System.arraycopy(par1ArrayOfByte, i, nibblearray.data, 0, nibblearray.data.length);
                i += nibblearray.data.length;
            }
        }

        for (int l = 0; l < field_48505_p.length; l++)
        {
            if ((par2 & 1 << l) != 0 && field_48505_p[l] != null)
            {
                NibbleArray nibblearray1 = field_48505_p[l].func_48705_k();
                System.arraycopy(par1ArrayOfByte, i, nibblearray1.data, 0, nibblearray1.data.length);
                i += nibblearray1.data.length;
            }
        }

        for (int i1 = 0; i1 < field_48505_p.length; i1++)
        {
            if ((par2 & 1 << i1) != 0 && field_48505_p[i1] != null)
            {
                NibbleArray nibblearray2 = field_48505_p[i1].func_48714_l();
                System.arraycopy(par1ArrayOfByte, i, nibblearray2.data, 0, nibblearray2.data.length);
                i += nibblearray2.data.length;
            }
        }

        for (int j1 = 0; j1 < field_48505_p.length; j1++)
        {
            if ((par3 & 1 << j1) != 0)
            {
                if (field_48505_p[j1] == null)
                {
                    i += 2048;
                    continue;
                }

                NibbleArray nibblearray3 = field_48505_p[j1].func_48704_i();

                if (nibblearray3 == null)
                {
                    nibblearray3 = field_48505_p[j1].func_48696_m();
                }

                System.arraycopy(par1ArrayOfByte, i, nibblearray3.data, 0, nibblearray3.data.length);
                i += nibblearray3.data.length;
                continue;
            }

            if (par4 && field_48505_p[j1] != null && field_48505_p[j1].func_48704_i() != null)
            {
                field_48505_p[j1].func_48715_h();
            }
        }

        if (par4)
        {
            System.arraycopy(par1ArrayOfByte, i, field_48504_q, 0, field_48504_q.length);
            i += field_48504_q.length;
        }

        for (int k1 = 0; k1 < field_48505_p.length; k1++)
        {
            if (field_48505_p[k1] != null && (par2 & 1 << k1) != 0)
            {
                field_48505_p[k1].func_48708_d();
            }
        }

        generateHeightMap();
        TileEntity tileentity;

        for (Iterator iterator = chunkTileEntityMap.values().iterator(); iterator.hasNext(); tileentity.updateContainingBlockInfo())
        {
            tileentity = (TileEntity)iterator.next();
        }
        
        /* WORLD DOWNLOADER ---> */
        isFilled = true;
        /* <--- WORLD DOWNLOADER */
        
    }

    public BiomeGenBase func_48490_a(int par1, int par2, WorldChunkManager par3WorldChunkManager)
    {
        int i = field_48504_q[par2 << 4 | par1] & 0xff;

        if (i == 255)
        {
            BiomeGenBase biomegenbase = par3WorldChunkManager.getBiomeGenAt((xPosition << 4) + par1, (zPosition << 4) + par2);
            i = biomegenbase.biomeID;
            field_48504_q[par2 << 4 | par1] = (byte)(i & 0xff);
        }

        if (BiomeGenBase.biomeList[i] == null)
        {
            return BiomeGenBase.plains;
        }
        else
        {
            return BiomeGenBase.biomeList[i];
        }
    }

    public byte[] func_48493_m()
    {
        return field_48504_q;
    }

    public void func_48497_a(byte par1ArrayOfByte[])
    {
        field_48504_q = par1ArrayOfByte;
    }

    public void func_48496_n()
    {
        field_48503_s = 0;
    }

    public void func_48491_o()
    {
        for (int i = 0; i < 8; i++)
        {
            if (field_48503_s >= 4096)
            {
                return;
            }

            int j = field_48503_s % 16;
            int k = (field_48503_s / 16) % 16;
            int l = field_48503_s / 256;
            field_48503_s++;
            int i1 = (xPosition << 4) + k;
            int j1 = (zPosition << 4) + l;

            for (int k1 = 0; k1 < 16; k1++)
            {
                int l1 = (j << 4) + k1;

                if ((field_48505_p[j] != null || k1 != 0 && k1 != 15 && k != 0 && k != 15 && l != 0 && l != 15) && (field_48505_p[j] == null || field_48505_p[j].func_48703_a(k, k1, l) != 0))
                {
                    continue;
                }

                if (Block.lightValue[worldObj.getBlockId(i1, l1 - 1, j1)] > 0)
                {
                    worldObj.updateAllLightTypes(i1, l1 - 1, j1);
                }

                if (Block.lightValue[worldObj.getBlockId(i1, l1 + 1, j1)] > 0)
                {
                    worldObj.updateAllLightTypes(i1, l1 + 1, j1);
                }

                if (Block.lightValue[worldObj.getBlockId(i1 - 1, l1, j1)] > 0)
                {
                    worldObj.updateAllLightTypes(i1 - 1, l1, j1);
                }

                if (Block.lightValue[worldObj.getBlockId(i1 + 1, l1, j1)] > 0)
                {
                    worldObj.updateAllLightTypes(i1 + 1, l1, j1);
                }

                if (Block.lightValue[worldObj.getBlockId(i1, l1, j1 - 1)] > 0)
                {
                    worldObj.updateAllLightTypes(i1, l1, j1 - 1);
                }

                if (Block.lightValue[worldObj.getBlockId(i1, l1, j1 + 1)] > 0)
                {
                    worldObj.updateAllLightTypes(i1, l1, j1 + 1);
                }

                worldObj.updateAllLightTypes(i1, l1, j1);
            }
        }
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
