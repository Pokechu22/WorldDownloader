package net.minecraft.world.chunk.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveFormatComparator;
import net.minecraft.world.storage.SaveFormatOld;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnvilSaveConverter extends SaveFormatOld
{
    private static final Logger field_151480_b = LogManager.getLogger();
    private static final String __OBFID = "CL_00000582";

    public AnvilSaveConverter(File par1File)
    {
        super(par1File);
    }

    public List getSaveList() throws AnvilConverterException
    {
        if (this.savesDirectory != null && this.savesDirectory.exists() && this.savesDirectory.isDirectory())
        {
            ArrayList var1 = new ArrayList();
            File[] var2 = this.savesDirectory.listFiles();
            File[] var3 = var2;
            int var4 = var2.length;

            for (int var5 = 0; var5 < var4; ++var5)
            {
                File var6 = var3[var5];

                if (var6.isDirectory())
                {
                    String var7 = var6.getName();
                    WorldInfo var8 = this.getWorldInfo(var7);

                    if (var8 != null && (var8.getSaveVersion() == 19132 || var8.getSaveVersion() == 19133))
                    {
                        boolean var9 = var8.getSaveVersion() != this.func_75812_c();
                        String var10 = var8.getWorldName();

                        if (var10 == null || MathHelper.stringNullOrLengthZero(var10))
                        {
                            var10 = var7;
                        }

                        long var11 = 0L;
                        var1.add(new SaveFormatComparator(var7, var10, var8.getLastTimePlayed(), var11, var8.getGameType(), var9, var8.isHardcoreModeEnabled(), var8.areCommandsAllowed()));
                    }
                }
            }

            return var1;
        }
        else
        {
            throw new AnvilConverterException("Unable to read or access folder where game worlds are saved!");
        }
    }

    protected int func_75812_c()
    {
        return 19133;
    }

    public void flushCache()
    {
        RegionFileCache.clearRegionFileReferences();
    }

    /**
     * Returns back a loader for the specified save directory
     */
    public ISaveHandler getSaveLoader(String par1Str, boolean par2)
    {
        return new AnvilSaveHandler(this.savesDirectory, par1Str, par2);
    }

    /**
     * Checks if the save directory uses the old map format
     */
    public boolean isOldMapFormat(String par1Str)
    {
        WorldInfo var2 = this.getWorldInfo(par1Str);
        return var2 != null && var2.getSaveVersion() != this.func_75812_c();
    }

    /**
     * Converts the specified map to the new map format. Args: worldName, loadingScreen
     */
    public boolean convertMapFormat(String par1Str, IProgressUpdate par2IProgressUpdate)
    {
        par2IProgressUpdate.setLoadingProgress(0);
        ArrayList var3 = new ArrayList();
        ArrayList var4 = new ArrayList();
        ArrayList var5 = new ArrayList();
        File var6 = new File(this.savesDirectory, par1Str);
        File var7 = new File(var6, "DIM-1");
        File var8 = new File(var6, "DIM1");
        field_151480_b.info("Scanning folders...");
        this.func_75810_a(var6, var3);

        if (var7.exists())
        {
            this.func_75810_a(var7, var4);
        }

        if (var8.exists())
        {
            this.func_75810_a(var8, var5);
        }

        int var9 = var3.size() + var4.size() + var5.size();
        field_151480_b.info("Total conversion count is " + var9);
        WorldInfo var10 = this.getWorldInfo(par1Str);
        Object var11 = null;

        if (var10.getTerrainType() == WorldType.FLAT)
        {
            var11 = new WorldChunkManagerHell(BiomeGenBase.plains, 0.5F);
        }
        else
        {
            var11 = new WorldChunkManager(var10.getSeed(), var10.getTerrainType());
        }

        this.func_75813_a(new File(var6, "region"), var3, (WorldChunkManager)var11, 0, var9, par2IProgressUpdate);
        this.func_75813_a(new File(var7, "region"), var4, new WorldChunkManagerHell(BiomeGenBase.hell, 0.0F), var3.size(), var9, par2IProgressUpdate);
        this.func_75813_a(new File(var8, "region"), var5, new WorldChunkManagerHell(BiomeGenBase.sky, 0.0F), var3.size() + var4.size(), var9, par2IProgressUpdate);
        var10.setSaveVersion(19133);

        if (var10.getTerrainType() == WorldType.DEFAULT_1_1)
        {
            var10.setTerrainType(WorldType.DEFAULT);
        }

        this.func_75809_f(par1Str);
        ISaveHandler var12 = this.getSaveLoader(par1Str, false);
        var12.saveWorldInfo(var10);
        return true;
    }

    private void func_75809_f(String par1Str)
    {
        File var2 = new File(this.savesDirectory, par1Str);

        if (!var2.exists())
        {
            field_151480_b.warn("Unable to create level.dat_mcr backup");
        }
        else
        {
            File var3 = new File(var2, "level.dat");

            if (!var3.exists())
            {
                field_151480_b.warn("Unable to create level.dat_mcr backup");
            }
            else
            {
                File var4 = new File(var2, "level.dat_mcr");

                if (!var3.renameTo(var4))
                {
                    field_151480_b.warn("Unable to create level.dat_mcr backup");
                }
            }
        }
    }

    private void func_75813_a(File par1File, Iterable par2Iterable, WorldChunkManager par3WorldChunkManager, int par4, int par5, IProgressUpdate par6IProgressUpdate)
    {
        Iterator var7 = par2Iterable.iterator();

        while (var7.hasNext())
        {
            File var8 = (File)var7.next();
            this.func_75811_a(par1File, var8, par3WorldChunkManager, par4, par5, par6IProgressUpdate);
            ++par4;
            int var9 = (int)Math.round(100.0D * (double)par4 / (double)par5);
            par6IProgressUpdate.setLoadingProgress(var9);
        }
    }

    private void func_75811_a(File par1File, File par2File, WorldChunkManager par3WorldChunkManager, int par4, int par5, IProgressUpdate par6IProgressUpdate)
    {
        try
        {
            String var7 = par2File.getName();
            RegionFile var8 = new RegionFile(par2File);
            RegionFile var9 = new RegionFile(new File(par1File, var7.substring(0, var7.length() - ".mcr".length()) + ".mca"));

            for (int var10 = 0; var10 < 32; ++var10)
            {
                int var11;

                for (var11 = 0; var11 < 32; ++var11)
                {
                    if (var8.isChunkSaved(var10, var11) && !var9.isChunkSaved(var10, var11))
                    {
                        DataInputStream var12 = var8.getChunkDataInputStream(var10, var11);

                        if (var12 == null)
                        {
                            field_151480_b.warn("Failed to fetch input stream");
                        }
                        else
                        {
                            NBTTagCompound var13 = CompressedStreamTools.read(var12);
                            var12.close();
                            NBTTagCompound var14 = var13.getCompoundTag("Level");
                            ChunkLoader.AnvilConverterData var15 = ChunkLoader.load(var14);
                            NBTTagCompound var16 = new NBTTagCompound();
                            NBTTagCompound var17 = new NBTTagCompound();
                            var16.setTag("Level", var17);
                            ChunkLoader.convertToAnvilFormat(var15, var17, par3WorldChunkManager);
                            DataOutputStream var18 = var9.getChunkDataOutputStream(var10, var11);
                            CompressedStreamTools.write(var16, var18);
                            var18.close();
                        }
                    }
                }

                var11 = (int)Math.round(100.0D * (double)(par4 * 1024) / (double)(par5 * 1024));
                int var20 = (int)Math.round(100.0D * (double)((var10 + 1) * 32 + par4 * 1024) / (double)(par5 * 1024));

                if (var20 > var11)
                {
                    par6IProgressUpdate.setLoadingProgress(var20);
                }
            }

            var8.close();
            var9.close();
        }
        catch (IOException var19)
        {
            var19.printStackTrace();
        }
    }

    private void func_75810_a(File par1File, Collection par2Collection)
    {
        File var3 = new File(par1File, "region");
        File[] var4 = var3.listFiles(new FilenameFilter()
        {
            private static final String __OBFID = "CL_00000583";
            public boolean accept(File par1File, String par2Str)
            {
                return par2Str.endsWith(".mcr");
            }
        });

        if (var4 != null)
        {
            Collections.addAll(par2Collection, var4);
        }
    }
    
	/* WDL >>> */
    public int getAnvilSaveVersion()
    {
        return func_75812_c();
    }
    /* <<< WDL */
}
