package net.minecraft.src;

public class WorldInfo
{
    /** Holds the seed of the currently world. */
    private long randomSeed;
    private WorldType terrainType;
    private String field_82576_c;

    /** The spawn zone position X coordinate. */
    private int spawnX;

    /** The spawn zone position Y coordinate. */
    private int spawnY;

    /** The spawn zone position Z coordinate. */
    private int spawnZ;
    private long field_82575_g;

    /** The current world time in ticks, ranging from 0 to 23999. */
    private long worldTime;

    /** The last time the player was in this world. */
    private long lastTimePlayed;

    /** The size of entire save of current world on the disk, isn't exactly. */
    private long sizeOnDisk;
    private NBTTagCompound playerTag;
    private int dimension;

    /** The name of the save defined at world creation. */
    private String levelName;

    /** Introduced in beta 1.3, is the save version for future control. */
    private int saveVersion;

    /** True if it's raining, false otherwise. */
    private boolean raining;

    /** Number of ticks until next rain. */
    private int rainTime;

    /** Is thunderbolts failing now? */
    private boolean thundering;

    /** Number of ticks untils next thunderbolt. */
    private int thunderTime;

    /** The Game Type. */
    private EnumGameType theGameType;

    /**
     * Whether the map features (e.g. strongholds) generation is enabled or disabled.
     */
    private boolean mapFeaturesEnabled;

    /** Hardcore mode flag */
    private boolean hardcore;
    private boolean allowCommands;
    private boolean initialized;
    private GameRules field_82577_x;

    protected WorldInfo()
    {
        this.terrainType = WorldType.DEFAULT;
        this.field_82576_c = "";
        this.field_82577_x = new GameRules();
    }

    public WorldInfo(NBTTagCompound par1NBTTagCompound)
    {
        this.terrainType = WorldType.DEFAULT;
        this.field_82576_c = "";
        this.field_82577_x = new GameRules();
        this.randomSeed = par1NBTTagCompound.getLong("RandomSeed");

        if (par1NBTTagCompound.hasKey("generatorName"))
        {
            String var2 = par1NBTTagCompound.getString("generatorName");
            this.terrainType = WorldType.parseWorldType(var2);

            if (this.terrainType == null)
            {
                this.terrainType = WorldType.DEFAULT;
            }
            else if (this.terrainType.isVersioned())
            {
                int var3 = 0;

                if (par1NBTTagCompound.hasKey("generatorVersion"))
                {
                    var3 = par1NBTTagCompound.getInteger("generatorVersion");
                }

                this.terrainType = this.terrainType.getWorldTypeForGeneratorVersion(var3);
            }

            if (par1NBTTagCompound.hasKey("generatorOptions"))
            {
                this.field_82576_c = par1NBTTagCompound.getString("generatorOptions");
            }
        }

        this.theGameType = EnumGameType.getByID(par1NBTTagCompound.getInteger("GameType"));

        if (par1NBTTagCompound.hasKey("MapFeatures"))
        {
            this.mapFeaturesEnabled = par1NBTTagCompound.getBoolean("MapFeatures");
        }
        else
        {
            this.mapFeaturesEnabled = true;
        }

        this.spawnX = par1NBTTagCompound.getInteger("SpawnX");
        this.spawnY = par1NBTTagCompound.getInteger("SpawnY");
        this.spawnZ = par1NBTTagCompound.getInteger("SpawnZ");
        this.field_82575_g = par1NBTTagCompound.getLong("Time");

        if (par1NBTTagCompound.hasKey("DayTime"))
        {
            this.worldTime = par1NBTTagCompound.getLong("DayTime");
        }
        else
        {
            this.worldTime = this.field_82575_g;
        }

        this.lastTimePlayed = par1NBTTagCompound.getLong("LastPlayed");
        this.sizeOnDisk = par1NBTTagCompound.getLong("SizeOnDisk");
        this.levelName = par1NBTTagCompound.getString("LevelName");
        this.saveVersion = par1NBTTagCompound.getInteger("version");
        this.rainTime = par1NBTTagCompound.getInteger("rainTime");
        this.raining = par1NBTTagCompound.getBoolean("raining");
        this.thunderTime = par1NBTTagCompound.getInteger("thunderTime");
        this.thundering = par1NBTTagCompound.getBoolean("thundering");
        this.hardcore = par1NBTTagCompound.getBoolean("hardcore");

        if (par1NBTTagCompound.hasKey("initialized"))
        {
            this.initialized = par1NBTTagCompound.getBoolean("initialized");
        }
        else
        {
            this.initialized = true;
        }

        if (par1NBTTagCompound.hasKey("allowCommands"))
        {
            this.allowCommands = par1NBTTagCompound.getBoolean("allowCommands");
        }
        else
        {
            this.allowCommands = this.theGameType == EnumGameType.CREATIVE;
        }

        if (par1NBTTagCompound.hasKey("Player"))
        {
            this.playerTag = par1NBTTagCompound.getCompoundTag("Player");
            this.dimension = this.playerTag.getInteger("Dimension");
        }

        if (par1NBTTagCompound.hasKey("GameRules"))
        {
            this.field_82577_x.func_82768_a(par1NBTTagCompound.getCompoundTag("GameRules"));
        }
    }

    public WorldInfo(WorldSettings par1WorldSettings, String par2Str)
    {
        this.terrainType = WorldType.DEFAULT;
        this.field_82576_c = "";
        this.field_82577_x = new GameRules();
        this.randomSeed = par1WorldSettings.getSeed();
        this.theGameType = par1WorldSettings.getGameType();
        this.mapFeaturesEnabled = par1WorldSettings.isMapFeaturesEnabled();
        this.levelName = par2Str;
        this.hardcore = par1WorldSettings.getHardcoreEnabled();
        this.terrainType = par1WorldSettings.getTerrainType();
        this.field_82576_c = par1WorldSettings.func_82749_j();
        this.allowCommands = par1WorldSettings.areCommandsAllowed();
        this.initialized = false;
    }

    public WorldInfo(WorldInfo par1WorldInfo)
    {
        this.terrainType = WorldType.DEFAULT;
        this.field_82576_c = "";
        this.field_82577_x = new GameRules();
        this.randomSeed = par1WorldInfo.randomSeed;
        this.terrainType = par1WorldInfo.terrainType;
        this.field_82576_c = par1WorldInfo.field_82576_c;
        this.theGameType = par1WorldInfo.theGameType;
        this.mapFeaturesEnabled = par1WorldInfo.mapFeaturesEnabled;
        this.spawnX = par1WorldInfo.spawnX;
        this.spawnY = par1WorldInfo.spawnY;
        this.spawnZ = par1WorldInfo.spawnZ;
        this.field_82575_g = par1WorldInfo.field_82575_g;
        this.worldTime = par1WorldInfo.worldTime;
        this.lastTimePlayed = par1WorldInfo.lastTimePlayed;
        this.sizeOnDisk = par1WorldInfo.sizeOnDisk;
        this.playerTag = par1WorldInfo.playerTag;
        this.dimension = par1WorldInfo.dimension;
        this.levelName = par1WorldInfo.levelName;
        this.saveVersion = par1WorldInfo.saveVersion;
        this.rainTime = par1WorldInfo.rainTime;
        this.raining = par1WorldInfo.raining;
        this.thunderTime = par1WorldInfo.thunderTime;
        this.thundering = par1WorldInfo.thundering;
        this.hardcore = par1WorldInfo.hardcore;
        this.allowCommands = par1WorldInfo.allowCommands;
        this.initialized = par1WorldInfo.initialized;
        this.field_82577_x = par1WorldInfo.field_82577_x;
    }

    /**
     * Gets the NBTTagCompound for the worldInfo
     */
    public NBTTagCompound getNBTTagCompound()
    {
        NBTTagCompound var1 = new NBTTagCompound();
        this.updateTagCompound(var1, this.playerTag);
        return var1;
    }

    /**
     * Creates a new NBTTagCompound for the world, with the given NBTTag as the "Player"
     */
    public NBTTagCompound cloneNBTCompound(NBTTagCompound par1NBTTagCompound)
    {
        NBTTagCompound var2 = new NBTTagCompound();
        this.updateTagCompound(var2, par1NBTTagCompound);
        return var2;
    }

    private void updateTagCompound(NBTTagCompound par1NBTTagCompound, NBTTagCompound par2NBTTagCompound)
    {
        par1NBTTagCompound.setLong("RandomSeed", this.randomSeed);
        par1NBTTagCompound.setString("generatorName", this.terrainType.getWorldTypeName());
        par1NBTTagCompound.setInteger("generatorVersion", this.terrainType.getGeneratorVersion());
        par1NBTTagCompound.setString("generatorOptions", this.field_82576_c);
        par1NBTTagCompound.setInteger("GameType", this.theGameType.getID());
        par1NBTTagCompound.setBoolean("MapFeatures", this.mapFeaturesEnabled);
        par1NBTTagCompound.setInteger("SpawnX", this.spawnX);
        par1NBTTagCompound.setInteger("SpawnY", this.spawnY);
        par1NBTTagCompound.setInteger("SpawnZ", this.spawnZ);
        par1NBTTagCompound.setLong("Time", this.field_82575_g);
        par1NBTTagCompound.setLong("DayTime", this.worldTime);
        par1NBTTagCompound.setLong("SizeOnDisk", this.sizeOnDisk);
        par1NBTTagCompound.setLong("LastPlayed", System.currentTimeMillis());
        par1NBTTagCompound.setString("LevelName", this.levelName);
        par1NBTTagCompound.setInteger("version", this.saveVersion);
        par1NBTTagCompound.setInteger("rainTime", this.rainTime);
        par1NBTTagCompound.setBoolean("raining", this.raining);
        par1NBTTagCompound.setInteger("thunderTime", this.thunderTime);
        par1NBTTagCompound.setBoolean("thundering", this.thundering);
        par1NBTTagCompound.setBoolean("hardcore", this.hardcore);
        par1NBTTagCompound.setBoolean("allowCommands", this.allowCommands);
        par1NBTTagCompound.setBoolean("initialized", this.initialized);
        par1NBTTagCompound.setCompoundTag("GameRules", this.field_82577_x.func_82770_a());

        if (par2NBTTagCompound != null)
        {
            par1NBTTagCompound.setCompoundTag("Player", par2NBTTagCompound);
        }
    }

    /**
     * Returns the seed of current world.
     */
    public long getSeed()
    {
        return this.randomSeed;
    }

    /* WORLD DOWNLOADER ---> */
    public void setSeed(long seed)
    {
        this.randomSeed = seed;
    }
    
    public void setSizeOnDisk(long s)
    {
        sizeOnDisk = s;
    }
    /* <--- WORLD DOWNLOADER */
    
    /**
     * Returns the x spawn position
     */
    public int getSpawnX()
    {
        return this.spawnX;
    }

    /**
     * Return the Y axis spawning point of the player.
     */
    public int getSpawnY()
    {
        return this.spawnY;
    }

    /**
     * Returns the z spawn position
     */
    public int getSpawnZ()
    {
        return this.spawnZ;
    }

    public long func_82573_f()
    {
        return this.field_82575_g;
    }

    /**
     * Get current world time
     */
    public long getWorldTime()
    {
        return this.worldTime;
    }

    public long getSizeOnDisk()
    {
        return this.sizeOnDisk;
    }

    /**
     * Returns the player's NBTTagCompound to be loaded
     */
    public NBTTagCompound getPlayerNBTTagCompound()
    {
        return this.playerTag;
    }

    public int getDimension()
    {
        return this.dimension;
    }

    /**
     * Set the x spawn position to the passed in value
     */
    public void setSpawnX(int par1)
    {
        this.spawnX = par1;
    }

    /**
     * Sets the y spawn position
     */
    public void setSpawnY(int par1)
    {
        this.spawnY = par1;
    }

    /**
     * Set the z spawn position to the passed in value
     */
    public void setSpawnZ(int par1)
    {
        this.spawnZ = par1;
    }

    public void func_82572_b(long par1)
    {
        this.field_82575_g = par1;
    }

    /**
     * Set current world time
     */
    public void setWorldTime(long par1)
    {
        this.worldTime = par1;
    }

    /**
     * Sets the spawn zone position. Args: x, y, z
     */
    public void setSpawnPosition(int par1, int par2, int par3)
    {
        this.spawnX = par1;
        this.spawnY = par2;
        this.spawnZ = par3;
    }

    /**
     * Get current world name
     */
    public String getWorldName()
    {
        return this.levelName;
    }

    public void setWorldName(String par1Str)
    {
        this.levelName = par1Str;
    }

    /**
     * Returns the save version of this world
     */
    public int getSaveVersion()
    {
        return this.saveVersion;
    }

    /**
     * Sets the save version of the world
     */
    public void setSaveVersion(int par1)
    {
        this.saveVersion = par1;
    }

    /**
     * Return the last time the player was in this world.
     */
    public long getLastTimePlayed()
    {
        return this.lastTimePlayed;
    }

    /**
     * Returns true if it is thundering, false otherwise.
     */
    public boolean isThundering()
    {
        return this.thundering;
    }

    /**
     * Sets whether it is thundering or not.
     */
    public void setThundering(boolean par1)
    {
        this.thundering = par1;
    }

    /**
     * Returns the number of ticks until next thunderbolt.
     */
    public int getThunderTime()
    {
        return this.thunderTime;
    }

    /**
     * Defines the number of ticks until next thunderbolt.
     */
    public void setThunderTime(int par1)
    {
        this.thunderTime = par1;
    }

    /**
     * Returns true if it is raining, false otherwise.
     */
    public boolean isRaining()
    {
        return this.raining;
    }

    /**
     * Sets whether it is raining or not.
     */
    public void setRaining(boolean par1)
    {
        this.raining = par1;
    }

    /**
     * Return the number of ticks until rain.
     */
    public int getRainTime()
    {
        return this.rainTime;
    }

    /**
     * Sets the number of ticks until rain.
     */
    public void setRainTime(int par1)
    {
        this.rainTime = par1;
    }

    /**
     * Gets the GameType.
     */
    public EnumGameType getGameType()
    {
        return this.theGameType;
    }

    /**
     * Get whether the map features (e.g. strongholds) generation is enabled or disabled.
     */
    public boolean isMapFeaturesEnabled()
    {
        return this.mapFeaturesEnabled;
    }

    /**
     * Sets the GameType.
     */
    public void setGameType(EnumGameType par1EnumGameType)
    {
        this.theGameType = par1EnumGameType;
    }

    /**
     * Returns true if hardcore mode is enabled, otherwise false
     */
    public boolean isHardcoreModeEnabled()
    {
        return this.hardcore;
    }

    public WorldType getTerrainType()
    {
        return this.terrainType;
    }

    public void setTerrainType(WorldType par1WorldType)
    {
        this.terrainType = par1WorldType;
    }

    public String func_82571_y()
    {
        return this.field_82576_c;
    }

    /**
     * Returns true if commands are allowed on this World.
     */
    public boolean areCommandsAllowed()
    {
        return this.allowCommands;
    }

    /**
     * Returns true if the World is initialized.
     */
    public boolean isInitialized()
    {
        return this.initialized;
    }

    /**
     * Sets the initialization status of the World.
     */
    public void setServerInitialized(boolean par1)
    {
        this.initialized = par1;
    }

    public GameRules func_82574_x()
    {
        return this.field_82577_x;
    }
}
