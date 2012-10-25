package net.minecraft.src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    /**
     * Used to store block IDs, block MSBs, Sky-light maps, Block-light maps, and metadata. Each entry corresponds to a
     * logical segment of 16x16x16 blocks, stacked vertically.
     */
    private ExtendedBlockStorage[] storageArrays;

    /**
     * Contains a 16x16 mapping on the X/Z plane of the biome ID to which each colum belongs.
     */
    private byte[] blockBiomeArray;

    /**
     * A map, similar to heightMap, that tracks how far down precipitation can fall.
     */
    public int[] precipitationHeightMap;

    /** Which columns need their skylightMaps updated. */
    public boolean[] updateSkylightColumns;

    /** Whether or not this Chunk is currently loaded into the World */
    public boolean isChunkLoaded;

    /** Reference to the World object. */
    public World worldObj;
    public int[] heightMap;

    /** The x coordinate of the chunk. */
    public final int xPosition;

    /** The z coordinate of the chunk. */
    public final int zPosition;
    private boolean isGapLightingUpdated;

    /** A Map of ChunkPositions to TileEntities in this chunk */
    public Map chunkTileEntityMap;

    /**
     * Array of Lists containing the entities in this Chunk. Each List represents a 16 block subchunk.
     */
    public List[] entityLists;

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
    public boolean deferRender;
    public int field_82912_p;

    /* WORLD DOWNLOADER ---> */
    public boolean isFilled = false; // Used to only save chunks that have already been received.
    public Map myChunkTileEntityMap; // Copy of the TileEntity data that only my code touches (never overwritten by junk)
    /* <--- WORLD DOWNLOADER */
    
    /**
     * Contains the current round-robin relight check index, and is implied as the relight check location as well.
     */
    private int queuedLightChecks;
    boolean field_76653_p;

    public Chunk(World par1World, int par2, int par3)
    {
        this.storageArrays = new ExtendedBlockStorage[16];
        this.blockBiomeArray = new byte[256];
        this.precipitationHeightMap = new int[256];
        this.updateSkylightColumns = new boolean[256];
        this.isGapLightingUpdated = false;
        this.chunkTileEntityMap = new HashMap();
        /* WORLD DOWNLOADER ---> */
        myChunkTileEntityMap = new HashMap();
        /* <--- WORLD DOWNLOADER */
        this.isTerrainPopulated = false;
        this.isModified = false;
        this.hasEntities = false;
        this.lastSaveTime = 0L;
        this.deferRender = false;
        this.field_82912_p = 0;
        this.queuedLightChecks = 4096;
        this.field_76653_p = false;
        this.entityLists = new List[16];
        this.worldObj = par1World;
        this.xPosition = par2;
        this.zPosition = par3;
        this.heightMap = new int[256];

        for (int var4 = 0; var4 < this.entityLists.length; ++var4)
        {
            this.entityLists[var4] = new ArrayList();
        }

        Arrays.fill(this.precipitationHeightMap, -999);
        Arrays.fill(this.blockBiomeArray, (byte) - 1);
    }

    public Chunk(World par1World, byte[] par2ArrayOfByte, int par3, int par4)
    {
        this(par1World, par3, par4);
        int var5 = par2ArrayOfByte.length / 256;

        for (int var6 = 0; var6 < 16; ++var6)
        {
            for (int var7 = 0; var7 < 16; ++var7)
            {
                for (int var8 = 0; var8 < var5; ++var8)
                {
                    byte var9 = par2ArrayOfByte[var6 << 11 | var7 << 7 | var8];

                    if (var9 != 0)
                    {
                        int var10 = var8 >> 4;

                        if (this.storageArrays[var10] == null)
                        {
                            this.storageArrays[var10] = new ExtendedBlockStorage(var10 << 4);
                        }

                        this.storageArrays[var10].setExtBlockID(var6, var8 & 15, var7, var9);
                    }
                }
            }
        }
    }

    /**
     * Checks whether the chunk is at the X/Z location specified
     */
    public boolean isAtLocation(int par1, int par2)
    {
        return par1 == this.xPosition && par2 == this.zPosition;
    }

    /**
     * Returns the value in the height map at this x, z coordinate in the chunk
     */
    public int getHeightValue(int par1, int par2)
    {
        return this.heightMap[par2 << 4 | par1];
    }

    /**
     * Returns the topmost ExtendedBlockStorage instance for this Chunk that actually contains a block.
     */
    public int getTopFilledSegment()
    {
        for (int var1 = this.storageArrays.length - 1; var1 >= 0; --var1)
        {
            if (this.storageArrays[var1] != null)
            {
                return this.storageArrays[var1].getYLocation();
            }
        }

        return 0;
    }

    /**
     * Returns the ExtendedBlockStorage array for this Chunk.
     */
    public ExtendedBlockStorage[] getBlockStorageArray()
    {
        return this.storageArrays;
    }

    /**
     * Generates the height map for a chunk from scratch
     */
    public void generateHeightMap()
    {
        int var1 = this.getTopFilledSegment();

        for (int var2 = 0; var2 < 16; ++var2)
        {
            int var3 = 0;

            while (var3 < 16)
            {
                this.precipitationHeightMap[var2 + (var3 << 4)] = -999;
                int var4 = var1 + 16 - 1;

                while (true)
                {
                    if (var4 > 0)
                    {
                        int var5 = this.getBlockID(var2, var4 - 1, var3);

                        if (Block.lightOpacity[var5] == 0)
                        {
                            --var4;
                            continue;
                        }

                        this.heightMap[var3 << 4 | var2] = var4;
                    }

                    ++var3;
                    break;
                }
            }
        }

        this.isModified = true;
    }

    /**
     * Generates the initial skylight map for the chunk upon generation or load.
     */
    public void generateSkylightMap()
    {
        int var1 = this.getTopFilledSegment();
        this.field_82912_p = Integer.MAX_VALUE;
        int var2;
        int var3;

        for (var2 = 0; var2 < 16; ++var2)
        {
            var3 = 0;

            while (var3 < 16)
            {
                this.precipitationHeightMap[var2 + (var3 << 4)] = -999;
                int var4 = var1 + 16 - 1;

                while (true)
                {
                    if (var4 > 0)
                    {
                        if (this.getBlockLightOpacity(var2, var4 - 1, var3) == 0)
                        {
                            --var4;
                            continue;
                        }

                        this.heightMap[var3 << 4 | var2] = var4;

                        if (var4 < this.field_82912_p)
                        {
                            this.field_82912_p = var4;
                        }
                    }

                    if (!this.worldObj.provider.hasNoSky)
                    {
                        var4 = 15;
                        int var5 = var1 + 16 - 1;

                        do
                        {
                            var4 -= this.getBlockLightOpacity(var2, var5, var3);

                            if (var4 > 0)
                            {
                                ExtendedBlockStorage var6 = this.storageArrays[var5 >> 4];

                                if (var6 != null)
                                {
                                    var6.setExtSkylightValue(var2, var5 & 15, var3, var4);
                                    this.worldObj.markBlockNeedsUpdateForAll((this.xPosition << 4) + var2, var5, (this.zPosition << 4) + var3);
                                }
                            }

                            --var5;
                        }
                        while (var5 > 0 && var4 > 0);
                    }

                    ++var3;
                    break;
                }
            }
        }

        this.isModified = true;

        for (var2 = 0; var2 < 16; ++var2)
        {
            for (var3 = 0; var3 < 16; ++var3)
            {
                this.propagateSkylightOcclusion(var2, var3);
            }
        }
    }

    /**
     * Propagates a given sky-visible block's light value downward and upward to neighboring blocks as necessary.
     */
    private void propagateSkylightOcclusion(int par1, int par2)
    {
        this.updateSkylightColumns[par1 + par2 * 16] = true;
        this.isGapLightingUpdated = true;
    }

    /**
     * Runs delayed skylight updates.
     */
    private void updateSkylight_do()
    {
        this.worldObj.theProfiler.startSection("recheckGaps");

        if (this.worldObj.doChunksNearChunkExist(this.xPosition * 16 + 8, 0, this.zPosition * 16 + 8, 16))
        {
            for (int var1 = 0; var1 < 16; ++var1)
            {
                for (int var2 = 0; var2 < 16; ++var2)
                {
                    if (this.updateSkylightColumns[var1 + var2 * 16])
                    {
                        this.updateSkylightColumns[var1 + var2 * 16] = false;
                        int var3 = this.getHeightValue(var1, var2);
                        int var4 = this.xPosition * 16 + var1;
                        int var5 = this.zPosition * 16 + var2;
                        int var6 = this.worldObj.func_82734_g(var4 - 1, var5);
                        int var7 = this.worldObj.func_82734_g(var4 + 1, var5);
                        int var8 = this.worldObj.func_82734_g(var4, var5 - 1);
                        int var9 = this.worldObj.func_82734_g(var4, var5 + 1);

                        if (var7 < var6)
                        {
                            var6 = var7;
                        }

                        if (var8 < var6)
                        {
                            var6 = var8;
                        }

                        if (var9 < var6)
                        {
                            var6 = var9;
                        }

                        this.checkSkylightNeighborHeight(var4, var5, var6);
                        this.checkSkylightNeighborHeight(var4 - 1, var5, var3);
                        this.checkSkylightNeighborHeight(var4 + 1, var5, var3);
                        this.checkSkylightNeighborHeight(var4, var5 - 1, var3);
                        this.checkSkylightNeighborHeight(var4, var5 + 1, var3);
                    }
                }
            }

            this.isGapLightingUpdated = false;
        }

        this.worldObj.theProfiler.endSection();
    }

    /**
     * Checks the height of a block next to a sky-visible block and schedules a lighting update as necessary.
     */
    private void checkSkylightNeighborHeight(int par1, int par2, int par3)
    {
        int var4 = this.worldObj.getHeightValue(par1, par2);

        if (var4 > par3)
        {
            this.updateSkylightNeighborHeight(par1, par2, par3, var4 + 1);
        }
        else if (var4 < par3)
        {
            this.updateSkylightNeighborHeight(par1, par2, var4, par3 + 1);
        }
    }

    private void updateSkylightNeighborHeight(int par1, int par2, int par3, int par4)
    {
        if (par4 > par3 && this.worldObj.doChunksNearChunkExist(par1, 0, par2, 16))
        {
            for (int var5 = par3; var5 < par4; ++var5)
            {
                this.worldObj.updateLightByType(EnumSkyBlock.Sky, par1, var5, par2);
            }

            this.isModified = true;
        }
    }

    /**
     * Initiates the recalculation of both the block-light and sky-light for a given block inside a chunk.
     */
    private void relightBlock(int par1, int par2, int par3)
    {
        int var4 = this.heightMap[par3 << 4 | par1] & 255;
        int var5 = var4;

        if (par2 > var4)
        {
            var5 = par2;
        }

        while (var5 > 0 && this.getBlockLightOpacity(par1, var5 - 1, par3) == 0)
        {
            --var5;
        }

        if (var5 != var4)
        {
            this.worldObj.markBlocksDirtyVertical(par1 + this.xPosition * 16, par3 + this.zPosition * 16, var5, var4);
            this.heightMap[par3 << 4 | par1] = var5;
            int var6 = this.xPosition * 16 + par1;
            int var7 = this.zPosition * 16 + par3;
            int var8;
            int var12;

            if (!this.worldObj.provider.hasNoSky)
            {
                ExtendedBlockStorage var9;

                if (var5 < var4)
                {
                    for (var8 = var5; var8 < var4; ++var8)
                    {
                        var9 = this.storageArrays[var8 >> 4];

                        if (var9 != null)
                        {
                            var9.setExtSkylightValue(par1, var8 & 15, par3, 15);
                            this.worldObj.markBlockNeedsUpdateForAll((this.xPosition << 4) + par1, var8, (this.zPosition << 4) + par3);
                        }
                    }
                }
                else
                {
                    for (var8 = var4; var8 < var5; ++var8)
                    {
                        var9 = this.storageArrays[var8 >> 4];

                        if (var9 != null)
                        {
                            var9.setExtSkylightValue(par1, var8 & 15, par3, 0);
                            this.worldObj.markBlockNeedsUpdateForAll((this.xPosition << 4) + par1, var8, (this.zPosition << 4) + par3);
                        }
                    }
                }

                var8 = 15;

                while (var5 > 0 && var8 > 0)
                {
                    --var5;
                    var12 = this.getBlockLightOpacity(par1, var5, par3);

                    if (var12 == 0)
                    {
                        var12 = 1;
                    }

                    var8 -= var12;

                    if (var8 < 0)
                    {
                        var8 = 0;
                    }

                    ExtendedBlockStorage var10 = this.storageArrays[var5 >> 4];

                    if (var10 != null)
                    {
                        var10.setExtSkylightValue(par1, var5 & 15, par3, var8);
                    }
                }
            }

            var8 = this.heightMap[par3 << 4 | par1];
            var12 = var4;
            int var13 = var8;

            if (var8 < var4)
            {
                var12 = var8;
                var13 = var4;
            }

            if (var8 < this.field_82912_p)
            {
                this.field_82912_p = var8;
            }

            if (!this.worldObj.provider.hasNoSky)
            {
                this.updateSkylightNeighborHeight(var6 - 1, var7, var12, var13);
                this.updateSkylightNeighborHeight(var6 + 1, var7, var12, var13);
                this.updateSkylightNeighborHeight(var6, var7 - 1, var12, var13);
                this.updateSkylightNeighborHeight(var6, var7 + 1, var12, var13);
                this.updateSkylightNeighborHeight(var6, var7, var12, var13);
            }

            this.isModified = true;
        }
    }

    public int getBlockLightOpacity(int par1, int par2, int par3)
    {
        return Block.lightOpacity[this.getBlockID(par1, par2, par3)];
    }

    /**
     * Return the ID of a block in the chunk.
     */
    public int getBlockID(int par1, int par2, int par3)
    {
        if (par2 >> 4 >= this.storageArrays.length)
        {
            return 0;
        }
        else
        {
            ExtendedBlockStorage var4 = this.storageArrays[par2 >> 4];
            return var4 != null ? var4.getExtBlockID(par1, par2 & 15, par3) : 0;
        }
    }

    /**
     * Return the metadata corresponding to the given coordinates inside a chunk.
     */
    public int getBlockMetadata(int par1, int par2, int par3)
    {
        if (par2 >> 4 >= this.storageArrays.length)
        {
            return 0;
        }
        else
        {
            ExtendedBlockStorage var4 = this.storageArrays[par2 >> 4];
            return var4 != null ? var4.getExtBlockMetadata(par1, par2 & 15, par3) : 0;
        }
    }

    /**
     * Sets a blockID for a position in the chunk. Args: x, y, z, blockID
     */
    public boolean setBlockID(int par1, int par2, int par3, int par4)
    {
        return this.setBlockIDWithMetadata(par1, par2, par3, par4, 0);
    }

    /**
     * Sets a blockID of a position within a chunk with metadata. Args: x, y, z, blockID, metadata
     */
    public boolean setBlockIDWithMetadata(int par1, int par2, int par3, int par4, int par5)
    {
        int var6 = par3 << 4 | par1;

        if (par2 >= this.precipitationHeightMap[var6] - 1)
        {
            this.precipitationHeightMap[var6] = -999;
        }

        int var7 = this.heightMap[var6];
        int var8 = this.getBlockID(par1, par2, par3);
        int var9 = this.getBlockMetadata(par1, par2, par3);

        if (var8 == par4 && var9 == par5)
        {
            return false;
        }
        else
        {
            ExtendedBlockStorage var10 = this.storageArrays[par2 >> 4];
            boolean var11 = false;

            if (var10 == null)
            {
                if (par4 == 0)
                {
                    return false;
                }

                var10 = this.storageArrays[par2 >> 4] = new ExtendedBlockStorage(par2 >> 4 << 4);
                var11 = par2 >= var7;
            }

            int var12 = this.xPosition * 16 + par1;
            int var13 = this.zPosition * 16 + par3;

            if (var8 != 0 && !this.worldObj.isRemote)
            {
                Block.blocksList[var8].onSetBlockIDWithMetaData(this.worldObj, var12, par2, var13, var9);
            }

            var10.setExtBlockID(par1, par2 & 15, par3, par4);

            if (var8 != 0)
            {
                if (!this.worldObj.isRemote)
                {
                    Block.blocksList[var8].breakBlock(this.worldObj, var12, par2, var13, var8, var9);
                }
                else if (Block.blocksList[var8] instanceof BlockContainer && var8 != par4)
                {
                    this.worldObj.removeBlockTileEntity(var12, par2, var13);
                }
            }

            if (var10.getExtBlockID(par1, par2 & 15, par3) != par4)
            {
                return false;
            }
            else
            {
                var10.setExtBlockMetadata(par1, par2 & 15, par3, par5);

                if (var11)
                {
                    this.generateSkylightMap();
                }
                else
                {
                    if (Block.lightOpacity[par4 & 4095] > 0)
                    {
                        if (par2 >= var7)
                        {
                            this.relightBlock(par1, par2 + 1, par3);
                        }
                    }
                    else if (par2 == var7 - 1)
                    {
                        this.relightBlock(par1, par2, par3);
                    }

                    this.propagateSkylightOcclusion(par1, par3);
                }

                TileEntity var14;

                if (par4 != 0)
                {
                    if (!this.worldObj.isRemote)
                    {
                        Block.blocksList[par4].onBlockAdded(this.worldObj, var12, par2, var13);
                    }

                    if (Block.blocksList[par4] instanceof BlockContainer)
                    {
                        var14 = this.getChunkBlockTileEntity(par1, par2, par3);

                        if (var14 == null)
                        {
                            var14 = ((BlockContainer)Block.blocksList[par4]).createNewTileEntity(this.worldObj);
                            this.worldObj.setBlockTileEntity(var12, par2, var13, var14);
                        }

                        if (var14 != null)
                        {
                            var14.updateContainingBlockInfo();
                        }
                    }
                }
                else if (var8 > 0 && Block.blocksList[var8] instanceof BlockContainer)
                {
                    var14 = this.getChunkBlockTileEntity(par1, par2, par3);

                    if (var14 != null)
                    {
                        var14.updateContainingBlockInfo();
                    }
                }

                this.isModified = true;
                return true;
            }
        }
    }

    /**
     * Set the metadata of a block in the chunk
     */
    public boolean setBlockMetadata(int par1, int par2, int par3, int par4)
    {
        ExtendedBlockStorage var5 = this.storageArrays[par2 >> 4];

        if (var5 == null)
        {
            return false;
        }
        else
        {
            int var6 = var5.getExtBlockMetadata(par1, par2 & 15, par3);

            if (var6 == par4)
            {
                return false;
            }
            else
            {
                this.isModified = true;
                var5.setExtBlockMetadata(par1, par2 & 15, par3, par4);
                int var7 = var5.getExtBlockID(par1, par2 & 15, par3);

                if (var7 > 0 && Block.blocksList[var7] instanceof BlockContainer)
                {
                    TileEntity var8 = this.getChunkBlockTileEntity(par1, par2, par3);

                    if (var8 != null)
                    {
                        var8.updateContainingBlockInfo();
                        var8.blockMetadata = par4;
                    }
                }

                return true;
            }
        }
    }

    /**
     * Gets the amount of light saved in this block (doesn't adjust for daylight)
     */
    public int getSavedLightValue(EnumSkyBlock par1EnumSkyBlock, int par2, int par3, int par4)
    {
        ExtendedBlockStorage var5 = this.storageArrays[par3 >> 4];
        return var5 == null ? (this.canBlockSeeTheSky(par2, par3, par4) ? par1EnumSkyBlock.defaultLightValue : 0) : (par1EnumSkyBlock == EnumSkyBlock.Sky ? var5.getExtSkylightValue(par2, par3 & 15, par4) : (par1EnumSkyBlock == EnumSkyBlock.Block ? var5.getExtBlocklightValue(par2, par3 & 15, par4) : par1EnumSkyBlock.defaultLightValue));
    }

    /**
     * Sets the light value at the coordinate. If enumskyblock is set to sky it sets it in the skylightmap and if its a
     * block then into the blocklightmap. Args enumSkyBlock, x, y, z, lightValue
     */
    public void setLightValue(EnumSkyBlock par1EnumSkyBlock, int par2, int par3, int par4, int par5)
    {
        ExtendedBlockStorage var6 = this.storageArrays[par3 >> 4];

        if (var6 == null)
        {
            var6 = this.storageArrays[par3 >> 4] = new ExtendedBlockStorage(par3 >> 4 << 4);
            this.generateSkylightMap();
        }

        this.isModified = true;

        if (par1EnumSkyBlock == EnumSkyBlock.Sky)
        {
            if (!this.worldObj.provider.hasNoSky)
            {
                var6.setExtSkylightValue(par2, par3 & 15, par4, par5);
            }
        }
        else if (par1EnumSkyBlock == EnumSkyBlock.Block)
        {
            var6.setExtBlocklightValue(par2, par3 & 15, par4, par5);
        }
    }

    /**
     * Gets the amount of light on a block taking into account sunlight
     */
    public int getBlockLightValue(int par1, int par2, int par3, int par4)
    {
        ExtendedBlockStorage var5 = this.storageArrays[par2 >> 4];

        if (var5 == null)
        {
            return !this.worldObj.provider.hasNoSky && par4 < EnumSkyBlock.Sky.defaultLightValue ? EnumSkyBlock.Sky.defaultLightValue - par4 : 0;
        }
        else
        {
            int var6 = this.worldObj.provider.hasNoSky ? 0 : var5.getExtSkylightValue(par1, par2 & 15, par3);

            if (var6 > 0)
            {
                isLit = true;
            }

            var6 -= par4;
            int var7 = var5.getExtBlocklightValue(par1, par2 & 15, par3);

            if (var7 > var6)
            {
                var6 = var7;
            }

            return var6;
        }
    }

    /**
     * Adds an entity to the chunk. Args: entity
     */
    public void addEntity(Entity par1Entity)
    {
        this.hasEntities = true;
        int var2 = MathHelper.floor_double(par1Entity.posX / 16.0D);
        int var3 = MathHelper.floor_double(par1Entity.posZ / 16.0D);

        if (var2 != this.xPosition || var3 != this.zPosition)
        {
            System.out.println("Wrong location! " + par1Entity);
            Thread.dumpStack();
        }

        int var4 = MathHelper.floor_double(par1Entity.posY / 16.0D);

        if (var4 < 0)
        {
            var4 = 0;
        }

        if (var4 >= this.entityLists.length)
        {
            var4 = this.entityLists.length - 1;
        }

        par1Entity.addedToChunk = true;
        par1Entity.chunkCoordX = this.xPosition;
        par1Entity.chunkCoordY = var4;
        par1Entity.chunkCoordZ = this.zPosition;
        this.entityLists[var4].add(par1Entity);
    }

    /**
     * removes entity using its y chunk coordinate as its index
     */
    public void removeEntity(Entity par1Entity)
    {
        this.removeEntityAtIndex(par1Entity, par1Entity.chunkCoordY);
    }

    /**
     * Removes entity at the specified index from the entity array.
     */
    public void removeEntityAtIndex(Entity par1Entity, int par2)
    {
        if (par2 < 0)
        {
            par2 = 0;
        }

        if (par2 >= this.entityLists.length)
        {
            par2 = this.entityLists.length - 1;
        }

        this.entityLists[par2].remove(par1Entity);
    }

    /**
     * Returns whether is not a block above this one blocking sight to the sky (done via checking against the heightmap)
     */
    public boolean canBlockSeeTheSky(int par1, int par2, int par3)
    {
        return par2 >= this.heightMap[par3 << 4 | par1];
    }

    /**
     * Gets the TileEntity for a given block in this chunk
     */
    public TileEntity getChunkBlockTileEntity(int par1, int par2, int par3)
    {
        ChunkPosition var4 = new ChunkPosition(par1, par2, par3);
        TileEntity var5 = (TileEntity)this.chunkTileEntityMap.get(var4);

        if (var5 == null)
        {
            int var6 = this.getBlockID(par1, par2, par3);

            if (var6 <= 0 || !Block.blocksList[var6].hasTileEntity())
            {
                return null;
            }

            if (var5 == null)
            {
                var5 = ((BlockContainer)Block.blocksList[var6]).createNewTileEntity(this.worldObj);
                this.worldObj.setBlockTileEntity(this.xPosition * 16 + par1, par2, this.zPosition * 16 + par3, var5);
            }

            var5 = (TileEntity)this.chunkTileEntityMap.get(var4);
        }

        if (var5 != null && var5.isInvalid())
        {
            this.chunkTileEntityMap.remove(var4);
            return null;
        }
        else
        {
            return var5;
        }
    }

    /**
     * Adds a TileEntity to a chunk
     */
    public void addTileEntity(TileEntity par1TileEntity)
    {
        int var2 = par1TileEntity.xCoord - this.xPosition * 16;
        int var3 = par1TileEntity.yCoord;
        int var4 = par1TileEntity.zCoord - this.zPosition * 16;
        this.setChunkBlockTileEntity(var2, var3, var4, par1TileEntity);

        if (this.isChunkLoaded)
        {
            this.worldObj.loadedTileEntityList.add(par1TileEntity);
        }
    }

    /**
     * Sets the TileEntity for a given block in this chunk
     */
    public void setChunkBlockTileEntity(int par1, int par2, int par3, TileEntity par4TileEntity)
    {
        ChunkPosition var5 = new ChunkPosition(par1, par2, par3);
        par4TileEntity.setWorldObj(this.worldObj);
        par4TileEntity.xCoord = this.xPosition * 16 + par1;
        par4TileEntity.yCoord = par2;
        par4TileEntity.zCoord = this.zPosition * 16 + par3;

        if (this.getBlockID(par1, par2, par3) != 0 && Block.blocksList[this.getBlockID(par1, par2, par3)] instanceof BlockContainer)
        {
            par4TileEntity.validate();
            this.chunkTileEntityMap.put(var5, par4TileEntity);
        }
    }

    /**
     * Removes the TileEntity for a given block in this chunk
     */
    public void removeChunkBlockTileEntity(int par1, int par2, int par3)
    {
        ChunkPosition var4 = new ChunkPosition(par1, par2, par3);

        if (this.isChunkLoaded)
        {
            TileEntity var5 = (TileEntity)this.chunkTileEntityMap.remove(var4);

            if (var5 != null)
            {
                var5.invalidate();
            }
        }
    }

    /**
     * Called when this Chunk is loaded by the ChunkProvider
     */
    public void onChunkLoad()
    {
        this.isChunkLoaded = true;
        this.worldObj.addTileEntity(this.chunkTileEntityMap.values());
        List[] var1 = this.entityLists;
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3)
        {
            List var4 = var1[var3];
            this.worldObj.addLoadedEntities(var4);
        }
    }

    /**
     * Called when this Chunk is unloaded by the ChunkProvider
     */
    public void onChunkUnload()
    {
        this.isChunkLoaded = false;
        Iterator var1 = this.chunkTileEntityMap.values().iterator();

        while (var1.hasNext())
        {
            TileEntity var2 = (TileEntity)var1.next();
            this.worldObj.markTileEntityForDespawn(var2);
        }

        List[] var5 = this.entityLists;
        int var6 = var5.length;

        for (int var3 = 0; var3 < var6; ++var3)
        {
            List var4 = var5[var3];
            this.worldObj.unloadEntities(var4);
        }
    }

    /**
     * Sets the isModified flag for this Chunk
     */
    public void setChunkModified()
    {
        this.isModified = true;
    }

    /**
     * Fills the given list of all entities that intersect within the given bounding box that aren't the passed entity
     * Args: entity, aabb, listToFill
     */
    public void getEntitiesWithinAABBForEntity(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB, List par3List)
    {
        int var4 = MathHelper.floor_double((par2AxisAlignedBB.minY - 2.0D) / 16.0D);
        int var5 = MathHelper.floor_double((par2AxisAlignedBB.maxY + 2.0D) / 16.0D);

        if (var4 < 0)
        {
            var4 = 0;
        }

        if (var5 >= this.entityLists.length)
        {
            var5 = this.entityLists.length - 1;
        }

        for (int var6 = var4; var6 <= var5; ++var6)
        {
            List var7 = this.entityLists[var6];
            Iterator var8 = var7.iterator();

            while (var8.hasNext())
            {
                Entity var9 = (Entity)var8.next();

                if (var9 != par1Entity && var9.boundingBox.intersectsWith(par2AxisAlignedBB))
                {
                    par3List.add(var9);
                    Entity[] var10 = var9.getParts();

                    if (var10 != null)
                    {
                        for (int var11 = 0; var11 < var10.length; ++var11)
                        {
                            var9 = var10[var11];

                            if (var9 != par1Entity && var9.boundingBox.intersectsWith(par2AxisAlignedBB))
                            {
                                par3List.add(var9);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets all entities that can be assigned to the specified class. Args: entityClass, aabb, listToFill
     */
    public void getEntitiesOfTypeWithinAAAB(Class par1Class, AxisAlignedBB par2AxisAlignedBB, List par3List, IEntitySelector par4IEntitySelector)
    {
        int var5 = MathHelper.floor_double((par2AxisAlignedBB.minY - 2.0D) / 16.0D);
        int var6 = MathHelper.floor_double((par2AxisAlignedBB.maxY + 2.0D) / 16.0D);

        if (var5 < 0)
        {
            var5 = 0;
        }
        else if (var5 >= this.entityLists.length)
        {
            var5 = this.entityLists.length - 1;
        }

        if (var6 >= this.entityLists.length)
        {
            var6 = this.entityLists.length - 1;
        }
        else if (var6 < 0)
        {
            var6 = 0;
        }

        for (int var7 = var5; var7 <= var6; ++var7)
        {
            List var8 = this.entityLists[var7];
            Iterator var9 = var8.iterator();

            while (var9.hasNext())
            {
                Entity var10 = (Entity)var9.next();

                if (par1Class.isAssignableFrom(var10.getClass()) && var10.boundingBox.intersectsWith(par2AxisAlignedBB) && (par4IEntitySelector == null || par4IEntitySelector.func_82704_a(var10)))
                {
                    par3List.add(var10);
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
            if (this.hasEntities && this.worldObj.func_82737_E() != this.lastSaveTime)
            {
                return true;
            }
        }
        else if (this.hasEntities && this.worldObj.func_82737_E() >= this.lastSaveTime + 600L)
        {
            return true;
        }

        return this.isModified;
    }

    public Random getRandomWithSeed(long par1)
    {
        return new Random(this.worldObj.getSeed() + (long)(this.xPosition * this.xPosition * 4987142) + (long)(this.xPosition * 5947611) + (long)(this.zPosition * this.zPosition) * 4392871L + (long)(this.zPosition * 389711) ^ par1);
    }

    public boolean isEmpty()
    {
        return false;
    }

    public void populateChunk(IChunkProvider par1IChunkProvider, IChunkProvider par2IChunkProvider, int par3, int par4)
    {
        if (!this.isTerrainPopulated && par1IChunkProvider.chunkExists(par3 + 1, par4 + 1) && par1IChunkProvider.chunkExists(par3, par4 + 1) && par1IChunkProvider.chunkExists(par3 + 1, par4))
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
        int var3 = par1 | par2 << 4;
        int var4 = this.precipitationHeightMap[var3];

        if (var4 == -999)
        {
            int var5 = this.getTopFilledSegment() + 15;
            var4 = -1;

            while (var5 > 0 && var4 == -1)
            {
                int var6 = this.getBlockID(par1, var5, par2);
                Material var7 = var6 == 0 ? Material.air : Block.blocksList[var6].blockMaterial;

                if (!var7.blocksMovement() && !var7.isLiquid())
                {
                    --var5;
                }
                else
                {
                    var4 = var5 + 1;
                }
            }

            this.precipitationHeightMap[var3] = var4;
        }

        return var4;
    }

    /**
     * Checks whether skylight needs updated; if it does, calls updateSkylight_do
     */
    public void updateSkylight()
    {
        if (this.isGapLightingUpdated && !this.worldObj.provider.hasNoSky)
        {
            this.updateSkylight_do();
        }
    }

    /**
     * Gets a ChunkCoordIntPair representing the Chunk's position.
     */
    public ChunkCoordIntPair getChunkCoordIntPair()
    {
        return new ChunkCoordIntPair(this.xPosition, this.zPosition);
    }

    /**
     * Returns whether the ExtendedBlockStorages containing levels (in blocks) from arg 1 to arg 2 are fully empty
     * (true) or not (false).
     */
    public boolean getAreLevelsEmpty(int par1, int par2)
    {
        if (par1 < 0)
        {
            par1 = 0;
        }

        if (par2 >= 256)
        {
            par2 = 255;
        }

        for (int var3 = par1; var3 <= par2; var3 += 16)
        {
            ExtendedBlockStorage var4 = this.storageArrays[var3 >> 4];

            if (var4 != null && !var4.isEmpty())
            {
                return false;
            }
        }

        return true;
    }

    public void setStorageArrays(ExtendedBlockStorage[] par1ArrayOfExtendedBlockStorage)
    {
        this.storageArrays = par1ArrayOfExtendedBlockStorage;
    }

    /**
     * Initialise this chunk with new binary data
     */
    public void fillChunk(byte[] par1ArrayOfByte, int par2, int par3, boolean par4)
    {
        int var5 = 0;
        int var6;

        for (var6 = 0; var6 < this.storageArrays.length; ++var6)
        {
            if ((par2 & 1 << var6) != 0)
            {
                if (this.storageArrays[var6] == null)
                {
                    this.storageArrays[var6] = new ExtendedBlockStorage(var6 << 4);
                }

                byte[] var7 = this.storageArrays[var6].getBlockLSBArray();
                System.arraycopy(par1ArrayOfByte, var5, var7, 0, var7.length);
                var5 += var7.length;
            }
            else if (par4 && this.storageArrays[var6] != null)
            {
                this.storageArrays[var6] = null;
            }
        }

        NibbleArray var8;

        for (var6 = 0; var6 < this.storageArrays.length; ++var6)
        {
            if ((par2 & 1 << var6) != 0 && this.storageArrays[var6] != null)
            {
                var8 = this.storageArrays[var6].getMetadataArray();
                System.arraycopy(par1ArrayOfByte, var5, var8.data, 0, var8.data.length);
                var5 += var8.data.length;
            }
        }

        for (var6 = 0; var6 < this.storageArrays.length; ++var6)
        {
            if ((par2 & 1 << var6) != 0 && this.storageArrays[var6] != null)
            {
                var8 = this.storageArrays[var6].getBlocklightArray();
                System.arraycopy(par1ArrayOfByte, var5, var8.data, 0, var8.data.length);
                var5 += var8.data.length;
            }
        }

        for (var6 = 0; var6 < this.storageArrays.length; ++var6)
        {
            if ((par2 & 1 << var6) != 0 && this.storageArrays[var6] != null)
            {
                var8 = this.storageArrays[var6].getSkylightArray();
                System.arraycopy(par1ArrayOfByte, var5, var8.data, 0, var8.data.length);
                var5 += var8.data.length;
            }
        }

        for (var6 = 0; var6 < this.storageArrays.length; ++var6)
        {
            if ((par3 & 1 << var6) != 0)
            {
                if (this.storageArrays[var6] == null)
                {
                    var5 += 2048;
                }
                else
                {
                    var8 = this.storageArrays[var6].getBlockMSBArray();

                    if (var8 == null)
                    {
                        var8 = this.storageArrays[var6].createBlockMSBArray();
                    }

                    System.arraycopy(par1ArrayOfByte, var5, var8.data, 0, var8.data.length);
                    var5 += var8.data.length;
                }
            }
            else if (par4 && this.storageArrays[var6] != null && this.storageArrays[var6].getBlockMSBArray() != null)
            {
                this.storageArrays[var6].clearMSBArray();
            }
        }

        if (par4)
        {
            System.arraycopy(par1ArrayOfByte, var5, this.blockBiomeArray, 0, this.blockBiomeArray.length);
            int var10000 = var5 + this.blockBiomeArray.length;
        }

        for (var6 = 0; var6 < this.storageArrays.length; ++var6)
        {
            if (this.storageArrays[var6] != null && (par2 & 1 << var6) != 0)
            {
                this.storageArrays[var6].removeInvalidBlocks();
            }
        }

        this.generateHeightMap();
        Iterator var10 = this.chunkTileEntityMap.values().iterator();

        while (var10.hasNext())
        {
            TileEntity var9 = (TileEntity)var10.next();
            var9.updateContainingBlockInfo();
        }
        /* WORLD DOWNLOADER ---> */
        isFilled = true;
        /* <--- WORLD DOWNLOADER */
    }

    /**
     * This method retrieves the biome at a set of coordinates
     */
    public BiomeGenBase getBiomeGenForWorldCoords(int par1, int par2, WorldChunkManager par3WorldChunkManager)
    {
        int var4 = this.blockBiomeArray[par2 << 4 | par1] & 255;

        if (var4 == 255)
        {
            BiomeGenBase var5 = par3WorldChunkManager.getBiomeGenAt((this.xPosition << 4) + par1, (this.zPosition << 4) + par2);
            var4 = var5.biomeID;
            this.blockBiomeArray[par2 << 4 | par1] = (byte)(var4 & 255);
        }

        return BiomeGenBase.biomeList[var4] == null ? BiomeGenBase.plains : BiomeGenBase.biomeList[var4];
    }

    /**
     * Returns an array containing a 16x16 mapping on the X/Z of block positions in this Chunk to biome IDs.
     */
    public byte[] getBiomeArray()
    {
        return this.blockBiomeArray;
    }

    /**
     * Accepts a 256-entry array that contains a 16x16 mapping on the X/Z plane of block positions in this Chunk to
     * biome IDs.
     */
    public void setBiomeArray(byte[] par1ArrayOfByte)
    {
        this.blockBiomeArray = par1ArrayOfByte;
    }

    /**
     * Resets the relight check index to 0 for this Chunk.
     */
    public void resetRelightChecks()
    {
        this.queuedLightChecks = 0;
    }

    /**
     * Called once-per-chunk-per-tick, and advances the round-robin relight check index per-storage-block by up to 8
     * blocks at a time. In a worst-case scenario, can potentially take up to 1.6 seconds, calculated via
     * (4096/(8*16))/20, to re-check all blocks in a chunk, which could explain both lagging light updates in certain
     * cases as well as Nether relight
     */
    public void enqueueRelightChecks()
    {
        for (int var1 = 0; var1 < 8; ++var1)
        {
            if (this.queuedLightChecks >= 4096)
            {
                return;
            }

            int var2 = this.queuedLightChecks % 16;
            int var3 = this.queuedLightChecks / 16 % 16;
            int var4 = this.queuedLightChecks / 256;
            ++this.queuedLightChecks;
            int var5 = (this.xPosition << 4) + var3;
            int var6 = (this.zPosition << 4) + var4;

            for (int var7 = 0; var7 < 16; ++var7)
            {
                int var8 = (var2 << 4) + var7;

                if (this.storageArrays[var2] == null && (var7 == 0 || var7 == 15 || var3 == 0 || var3 == 15 || var4 == 0 || var4 == 15) || this.storageArrays[var2] != null && this.storageArrays[var2].getExtBlockID(var3, var7, var4) == 0)
                {
                    if (Block.lightValue[this.worldObj.getBlockId(var5, var8 - 1, var6)] > 0)
                    {
                        this.worldObj.updateAllLightTypes(var5, var8 - 1, var6);
                    }

                    if (Block.lightValue[this.worldObj.getBlockId(var5, var8 + 1, var6)] > 0)
                    {
                        this.worldObj.updateAllLightTypes(var5, var8 + 1, var6);
                    }

                    if (Block.lightValue[this.worldObj.getBlockId(var5 - 1, var8, var6)] > 0)
                    {
                        this.worldObj.updateAllLightTypes(var5 - 1, var8, var6);
                    }

                    if (Block.lightValue[this.worldObj.getBlockId(var5 + 1, var8, var6)] > 0)
                    {
                        this.worldObj.updateAllLightTypes(var5 + 1, var8, var6);
                    }

                    if (Block.lightValue[this.worldObj.getBlockId(var5, var8, var6 - 1)] > 0)
                    {
                        this.worldObj.updateAllLightTypes(var5, var8, var6 - 1);
                    }

                    if (Block.lightValue[this.worldObj.getBlockId(var5, var8, var6 + 1)] > 0)
                    {
                        this.worldObj.updateAllLightTypes(var5, var8, var6 + 1);
                    }

                    this.worldObj.updateAllLightTypes(var5, var8, var6);
                }
            }
        }
    }

	/* WORLD DOWNLOADER ---> */
	public void importOldChunkTileEntities() 
	{
		File file = WorldDL.mySaveHandler.getSaveDirectory();
		if (WorldDL.wc.provider instanceof WorldProviderHell) 
		{
			file = new File(file, "DIM-1");
			file.mkdirs();
		}
		if (WorldDL.wc.provider instanceof WorldProviderEnd) 
		{
			file = new File(file, "DIM1");
			file.mkdirs();
		}

		java.io.DataInputStream datainputstream = RegionFileCache
				.getChunkInputStream(file, xPosition, zPosition);
		NBTTagCompound nbttagcompound;
		if (datainputstream != null) 
		{
			try 
			{
				nbttagcompound = CompressedStreamTools.read(datainputstream);
			} catch (IOException e) 
			{
				return;
			}
		} 
		else
			return;

		if (!nbttagcompound.hasKey("Level"))
			return;

		NBTTagList nbttaglist1 = nbttagcompound.getCompoundTag("Level").getTagList("TileEntities");
		if (nbttaglist1 != null) 
		{
			for (int l = 0; l < nbttaglist1.tagCount(); l++) 
			{
				NBTTagCompound nbttagcompound2 = (NBTTagCompound) nbttaglist1.tagAt(l);
				TileEntity te = TileEntity.createAndLoadEntity(nbttagcompound2);
				if (te != null) 
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
