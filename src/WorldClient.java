package net.minecraft.client.multiplayer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSoundMinecart;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.EntityFireworkStarterFX;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;

/* WDL >>> */
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.wdl.WDL;
import net.minecraft.world.IWorldAccess;
/* <<< WDL */


public class WorldClient extends World
{
    /** The packets that need to be sent to the server. */
    private NetHandlerPlayClient sendQueue;

    /** The ChunkProviderClient instance */
    private ChunkProviderClient clientChunkProvider;

    /**
     * The hash set of entities handled by this client. Uses the entity's ID as the hash set's key.
     */
    private IntHashMap entityHashSet = new IntHashMap();

    /** Contains all entities for this client, both spawned and non-spawned. */
    private Set entityList = new HashSet();

    /**
     * Contains all entities for this client that were not spawned due to a non-present chunk. The game will attempt to
     * spawn up to 10 pending entities with each subsequent tick until the spawn queue is empty.
     */
    private Set entitySpawnQueue = new HashSet();
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Set previousActiveChunkSet = new HashSet();
    private static final String __OBFID = "CL_00000882";

    public WorldClient(NetHandlerPlayClient p_i45063_1_, WorldSettings p_i45063_2_, int p_i45063_3_, EnumDifficulty p_i45063_4_, Profiler p_i45063_5_)
    {
        super(new SaveHandlerMP(), "MpServer", WorldProvider.getProviderForDimension(p_i45063_3_), p_i45063_2_, p_i45063_5_);
        this.sendQueue = p_i45063_1_;
        this.difficultySetting = p_i45063_4_;
        this.setSpawnLocation(8, 64, 8);
        this.mapStorage = p_i45063_1_.mapStorageOrigin;
    }

    /**
     * Runs a single tick for the world
     */
    public void tick()
    {
        super.tick();
        this.func_82738_a(this.getTotalWorldTime() + 1L);

        if (this.getGameRules().getGameRuleBooleanValue("doDaylightCycle"))
        {
            this.setWorldTime(this.getWorldTime() + 1L);
        }

        this.theProfiler.startSection("reEntryProcessing");

        for (int var1 = 0; var1 < 10 && !this.entitySpawnQueue.isEmpty(); ++var1)
        {
            Entity var2 = (Entity)this.entitySpawnQueue.iterator().next();
            this.entitySpawnQueue.remove(var2);

            if (!this.loadedEntityList.contains(var2))
            {
                this.spawnEntityInWorld(var2);
            }
        }

        this.theProfiler.endStartSection("connection");
        this.sendQueue.onNetworkTick();
        this.theProfiler.endStartSection("chunkCache");
        this.clientChunkProvider.unloadQueuedChunks();
        this.theProfiler.endStartSection("blocks");
        this.func_147456_g();
        this.theProfiler.endSection();
        
        /* WDL >>> */
        if( WDL.guiToShowAsync != null )
        {
            WDL.mc.displayGuiScreen( WDL.guiToShowAsync );
            WDL.guiToShowAsync = null;
        }
        if( WDL.downloading )
        {
            if( WDL.tp.openContainer != WDL.windowContainer )
            {
                if( WDL.tp.openContainer == WDL.tp.inventoryContainer )
                    WDL.onItemGuiClosed();
                else
                    WDL.onItemGuiOpened();
                WDL.windowContainer = WDL.tp.openContainer;
            }
        }
        /* <<< WDL */
    }

    /**
     * Invalidates an AABB region of blocks from the receive queue, in the event that the block has been modified
     * client-side in the intervening 80 receive ticks.
     */
    public void invalidateBlockReceiveRegion(int par1, int par2, int par3, int par4, int par5, int par6) {}

    /**
     * Creates the chunk provider for this world. Called in the constructor. Retrieves provider from worldProvider?
     */
    protected IChunkProvider createChunkProvider()
    {
        this.clientChunkProvider = new ChunkProviderClient(this);
        return this.clientChunkProvider;
    }

    protected void func_147456_g()
    {
        super.func_147456_g();
        this.previousActiveChunkSet.retainAll(this.activeChunkSet);

        if (this.previousActiveChunkSet.size() == this.activeChunkSet.size())
        {
            this.previousActiveChunkSet.clear();
        }

        int var1 = 0;
        Iterator var2 = this.activeChunkSet.iterator();

        while (var2.hasNext())
        {
            ChunkCoordIntPair var3 = (ChunkCoordIntPair)var2.next();

            if (!this.previousActiveChunkSet.contains(var3))
            {
                int var4 = var3.chunkXPos * 16;
                int var5 = var3.chunkZPos * 16;
                this.theProfiler.startSection("getChunk");
                Chunk var6 = this.getChunkFromChunkCoords(var3.chunkXPos, var3.chunkZPos);
                this.func_147467_a(var4, var5, var6);
                this.theProfiler.endSection();
                this.previousActiveChunkSet.add(var3);
                ++var1;

                if (var1 >= 10)
                {
                    return;
                }
            }
        }
    }

    public void doPreChunk(int par1, int par2, boolean par3)
    {
        if (par3)
        {
            /* WDL >>> */
            if( this != WDL.wc )
                WDL.onWorldLoad();
            /* <<< WDL */
            
            this.clientChunkProvider.loadChunk(par1, par2);
        }
        else
        {
            /* WDL >>> */
            if( WDL.downloading )
                WDL.onChunkNoLongerNeeded( chunkProvider.provideChunk(par1, par2) );
            /* <<< WDL */
            
            this.clientChunkProvider.unloadChunk(par1, par2);
        }

        if (!par3)
        {
            this.markBlockRangeForRenderUpdate(par1 * 16, 0, par2 * 16, par1 * 16 + 15, 256, par2 * 16 + 15);
        }
    }

    /**
     * Called to place all entities as part of a world
     */
    public boolean spawnEntityInWorld(Entity par1Entity)
    {
        boolean var2 = super.spawnEntityInWorld(par1Entity);
        this.entityList.add(par1Entity);

        if (!var2)
        {
            this.entitySpawnQueue.add(par1Entity);
        }
        else if (par1Entity instanceof EntityMinecart)
        {
            this.mc.getSoundHandler().playSound(new MovingSoundMinecart((EntityMinecart)par1Entity));
        }

        return var2;
    }

    /**
     * Schedule the entity for removal during the next tick. Marks the entity dead in anticipation.
     */
    public void removeEntity(Entity par1Entity)
    {
        super.removeEntity(par1Entity);
        this.entityList.remove(par1Entity);
    }

    protected void onEntityAdded(Entity par1Entity)
    {
        super.onEntityAdded(par1Entity);

        if (this.entitySpawnQueue.contains(par1Entity))
        {
            this.entitySpawnQueue.remove(par1Entity);
        }
    }

    protected void onEntityRemoved(Entity par1Entity)
    {
        super.onEntityRemoved(par1Entity);
        boolean var2 = false;

        if (this.entityList.contains(par1Entity))
        {
            if (par1Entity.isEntityAlive())
            {
                this.entitySpawnQueue.add(par1Entity);
                var2 = true;
            }
            else
            {
                this.entityList.remove(par1Entity);
            }
        }

        if (RenderManager.instance.getEntityRenderObject(par1Entity).func_147905_a() && !var2)
        {
            this.mc.renderGlobal.onStaticEntitiesChanged();
        }
    }

    /**
     * Add an ID to Entity mapping to entityHashSet
     */
    public void addEntityToWorld(int par1, Entity par2Entity)
    {
        Entity var3 = this.getEntityByID(par1);

        if (var3 != null)
        {
            this.removeEntity(var3);
        }

        this.entityList.add(par2Entity);
        par2Entity.setEntityId(par1);

        if (!this.spawnEntityInWorld(par2Entity))
        {
            this.entitySpawnQueue.add(par2Entity);
        }

        this.entityHashSet.addKey(par1, par2Entity);

        if (RenderManager.instance.getEntityRenderObject(par2Entity).func_147905_a())
        {
            this.mc.renderGlobal.onStaticEntitiesChanged();
        }
    }

    /**
     * Returns the Entity with the given ID, or null if it doesn't exist in this World.
     */
    public Entity getEntityByID(int par1)
    {
        return (Entity)(par1 == this.mc.thePlayer.getEntityId() ? this.mc.thePlayer : (Entity)this.entityHashSet.lookup(par1));
    }

    public Entity removeEntityFromWorld(int par1)
    {
        /* WDL >>> */
        // If the entity is being removed and it's outside the default tracking range,
        // go ahead and remember it until the chunk is saved.
        if(WDL.downloading)
        {
            Entity entity = (Entity)this.getEntityByID(par1);
            if(entity != null)
            {
                int threshold = 0;
                if ((entity instanceof EntityFishHook) ||
                    //(entity instanceof EntityArrow) ||
                    //(entity instanceof EntitySmallFireball) ||
                    //(entity instanceof EntitySnowball) ||
                    (entity instanceof EntityEnderPearl) ||
                    (entity instanceof EntityEnderEye) ||
                    (entity instanceof EntityEgg) ||
                    (entity instanceof EntityPotion) ||
                    (entity instanceof EntityExpBottle) ||
                    (entity instanceof EntityItem) ||
                    (entity instanceof EntitySquid))
                {
                    threshold = 64;
                }
                else if ((entity instanceof EntityMinecart) ||
                         (entity instanceof EntityBoat) ||
                         (entity instanceof IAnimals))
                {
                    threshold = 80;
                }
                else if ((entity instanceof EntityDragon) ||
                         (entity instanceof EntityTNTPrimed) ||
                         (entity instanceof EntityFallingBlock) ||
                         (entity instanceof EntityPainting) ||
                         (entity instanceof EntityXPOrb))
                {
                    threshold = 160;
                }
                double distance = entity.getDistance(WDL.tp.posX, entity.posY, WDL.tp.posZ);
                if( distance > (double)threshold)
                {
                    WDL.chatDebug("removeEntityFromWorld: Refusing to remove " + EntityList.getEntityString(entity) + " at distance " + distance);
                    return null;
                }
                WDL.chatDebug("removeEntityFromWorld: Removing " + EntityList.getEntityString(entity) + " at distance " + distance);
            }
        }
        /* <<< WDL */
        
        Entity var2 = (Entity)this.entityHashSet.removeObject(par1);

        if (var2 != null)
        {
            this.entityList.remove(var2);
            this.removeEntity(var2);
        }

        return var2;
    }

    public boolean func_147492_c(int p_147492_1_, int p_147492_2_, int p_147492_3_, Block p_147492_4_, int p_147492_5_)
    {
        this.invalidateBlockReceiveRegion(p_147492_1_, p_147492_2_, p_147492_3_, p_147492_1_, p_147492_2_, p_147492_3_);
        return super.setBlock(p_147492_1_, p_147492_2_, p_147492_3_, p_147492_4_, p_147492_5_, 3);
    }

    /**
     * If on MP, sends a quitting packet.
     */
    public void sendQuittingDisconnectingPacket()
    {
        this.sendQueue.getNetworkManager().closeChannel(new ChatComponentText("Quitting"));
    }

    /**
     * Updates all weather states.
     */
    protected void updateWeather()
    {
        if (!this.provider.hasNoSky)
        {
            ;
        }
    }

    public void doVoidFogParticles(int par1, int par2, int par3)
    {
        byte var4 = 16;
        Random var5 = new Random();

        for (int var6 = 0; var6 < 1000; ++var6)
        {
            int var7 = par1 + this.rand.nextInt(var4) - this.rand.nextInt(var4);
            int var8 = par2 + this.rand.nextInt(var4) - this.rand.nextInt(var4);
            int var9 = par3 + this.rand.nextInt(var4) - this.rand.nextInt(var4);
            Block var10 = this.getBlock(var7, var8, var9);

            if (var10.getMaterial() == Material.air)
            {
                if (this.rand.nextInt(8) > var8 && this.provider.getWorldHasVoidParticles())
                {
                    this.spawnParticle("depthsuspend", (double)((float)var7 + this.rand.nextFloat()), (double)((float)var8 + this.rand.nextFloat()), (double)((float)var9 + this.rand.nextFloat()), 0.0D, 0.0D, 0.0D);
                }
            }
            else
            {
                var10.randomDisplayTick(this, var7, var8, var9, var5);
            }
        }
    }

    /**
     * also releases skins.
     */
    public void removeAllEntities()
    {
        this.loadedEntityList.removeAll(this.unloadedEntityList);
        int var1;
        Entity var2;
        int var3;
        int var4;

        for (var1 = 0; var1 < this.unloadedEntityList.size(); ++var1)
        {
            var2 = (Entity)this.unloadedEntityList.get(var1);
            var3 = var2.chunkCoordX;
            var4 = var2.chunkCoordZ;

            if (var2.addedToChunk && this.chunkExists(var3, var4))
            {
                this.getChunkFromChunkCoords(var3, var4).removeEntity(var2);
            }
        }

        for (var1 = 0; var1 < this.unloadedEntityList.size(); ++var1)
        {
            this.onEntityRemoved((Entity)this.unloadedEntityList.get(var1));
        }

        this.unloadedEntityList.clear();

        for (var1 = 0; var1 < this.loadedEntityList.size(); ++var1)
        {
            var2 = (Entity)this.loadedEntityList.get(var1);

            if (var2.ridingEntity != null)
            {
                if (!var2.ridingEntity.isDead && var2.ridingEntity.riddenByEntity == var2)
                {
                    continue;
                }

                var2.ridingEntity.riddenByEntity = null;
                var2.ridingEntity = null;
            }

            if (var2.isDead)
            {
                var3 = var2.chunkCoordX;
                var4 = var2.chunkCoordZ;

                if (var2.addedToChunk && this.chunkExists(var3, var4))
                {
                    this.getChunkFromChunkCoords(var3, var4).removeEntity(var2);
                }

                this.loadedEntityList.remove(var1--);
                this.onEntityRemoved(var2);
            }
        }
    }

    /**
     * Adds some basic stats of the world to the given crash report.
     */
    public CrashReportCategory addWorldInfoToCrashReport(CrashReport par1CrashReport)
    {
        CrashReportCategory var2 = super.addWorldInfoToCrashReport(par1CrashReport);
        var2.addCrashSectionCallable("Forced entities", new Callable()
        {
            private static final String __OBFID = "CL_00000883";
            public String call()
            {
                return WorldClient.this.entityList.size() + " total; " + WorldClient.this.entityList.toString();
            }
        });
        var2.addCrashSectionCallable("Retry entities", new Callable()
        {
            private static final String __OBFID = "CL_00000884";
            public String call()
            {
                return WorldClient.this.entitySpawnQueue.size() + " total; " + WorldClient.this.entitySpawnQueue.toString();
            }
        });
        var2.addCrashSectionCallable("Server brand", new Callable()
        {
            private static final String __OBFID = "CL_00000885";
            public String call()
            {
                return WorldClient.this.mc.thePlayer.func_142021_k();
            }
        });
        var2.addCrashSectionCallable("Server type", new Callable()
        {
            private static final String __OBFID = "CL_00000886";
            public String call()
            {
                return WorldClient.this.mc.getIntegratedServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server";
            }
        });
        return var2;
    }

    /**
     * par8 is loudness, all pars passed to minecraftInstance.sndManager.playSound
     */
    public void playSound(double par1, double par3, double par5, String par7Str, float par8, float par9, boolean par10)
    {
        double var11 = this.mc.renderViewEntity.getDistanceSq(par1, par3, par5);
        PositionedSoundRecord var13 = new PositionedSoundRecord(new ResourceLocation(par7Str), par8, par9, (float)par1, (float)par3, (float)par5);

        if (par10 && var11 > 100.0D)
        {
            double var14 = Math.sqrt(var11) / 40.0D;
            this.mc.getSoundHandler().playDelayedSound(var13, (int)(var14 * 20.0D));
        }
        else
        {
            this.mc.getSoundHandler().playSound(var13);
        }
    }

    public void makeFireworks(double par1, double par3, double par5, double par7, double par9, double par11, NBTTagCompound par13NBTTagCompound)
    {
        this.mc.effectRenderer.addEffect(new EntityFireworkStarterFX(this, par1, par3, par5, par7, par9, par11, this.mc.effectRenderer, par13NBTTagCompound));
    }

    public void setWorldScoreboard(Scoreboard par1Scoreboard)
    {
        this.worldScoreboard = par1Scoreboard;
    }

    /**
     * Sets the world time.
     */
    public void setWorldTime(long par1)
    {
        if (par1 < 0L)
        {
            par1 = -par1;
            this.getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
        }
        else
        {
            this.getGameRules().setOrCreateGameRule("doDaylightCycle", "true");
        }

        super.setWorldTime(par1);
    }
    
    /* WDL >>> */
    @Override
    public void removeWorldAccess(IWorldAccess par1iWorldAccess)
    {
        super.removeWorldAccess(par1iWorldAccess);
        // the old world: this (!= null)
        // the new world: mc.theWorld (!= null)
        //if( WDL.downloading )
        // WDL.onWorldUnload();
    }

    @Override
    public void func_147452_c(int par1, int par2, int par3, Block par4, int par5, int par6)
    {
        super.func_147452_c(par1, par2, par3, par4, par5, par6);
        if( WDL.downloading )
            WDL.onBlockEvent( par1, par2, par3, par4, par5, par6 );
    }
    /* <<< WDL */
}
