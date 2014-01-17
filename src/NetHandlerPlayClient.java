package net.minecraft.client.network;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenDemo;
import net.minecraft.client.gui.GuiScreenDisconnectedOnline;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.IProgressMeter;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityCrit2FX;
import net.minecraft.client.particle.EntityPickupFX;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.NpcMerchant;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S0APacketUseBed;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S0FPacketSpawnMob;
import net.minecraft.network.play.server.S10PacketSpawnPainting;
import net.minecraft.network.play.server.S11PacketSpawnExperienceOrb;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.network.play.server.S19PacketEntityHeadLook;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1EPacketRemoveEntityEffect;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.network.play.server.S20PacketEntityProperties;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S24PacketBlockAction;
import net.minecraft.network.play.server.S25PacketBlockBreakAnim;
import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S28PacketEffect;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.network.play.server.S31PacketWindowProperty;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.network.play.server.S33PacketUpdateSign;
import net.minecraft.network.play.server.S34PacketMaps;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.network.play.server.S36PacketSignEditorOpen;
import net.minecraft.network.play.server.S37PacketStatistics;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.network.play.server.S39PacketPlayerAbilities;
import net.minecraft.network.play.server.S3APacketTabComplete;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import net.minecraft.network.play.server.S3CPacketUpdateScore;
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.Explosion;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* WDL >>> */
import net.minecraft.wdl.WDL;
/* <<< WDL */

public class NetHandlerPlayClient implements INetHandlerPlayClient
{
    private static final Logger logger = LogManager.getLogger();

    /**
     * The NetworkManager instance used to communicate with the server (used only by handlePlayerPosLook to update
     * positioning and handleJoinGame to inform the server of the client distribution/mods)
     */
    private final NetworkManager netManager;

    /**
     * Reference to the Minecraft instance, which many handler methods operate on
     */
    private Minecraft gameController;

    /**
     * Reference to the current ClientWorld instance, which many handler methods operate on
     */
    private WorldClient clientWorldController;

    /**
     * True if the client has finished downloading terrain and may spawn. Set upon receipt of S08PacketPlayerPosLook,
     * reset upon respawning
     */
    private boolean doneLoadingTerrain;

    /**
     * Origin of the central MapStorage serving as a public reference for WorldClient. Not used in this class
     */
    public MapStorage mapStorageOrigin = new MapStorage((ISaveHandler)null);

    /**
     * A mapping from player names to their respective GuiPlayerInfo (specifies the clients response time to the server)
     */
    private Map playerInfoMap = new HashMap();

    /**
     * An ArrayList of GuiPlayerInfo (includes all the players' GuiPlayerInfo on the current server)
     */
    public List playerInfoList = new ArrayList();
    public int currentServerMaxPlayers = 20;

    /**
     * Seems to be either null (integrated server) or an instance of either GuiMultiplayer (when connecting to a server)
     * or GuiScreenReamlsTOS (when connecting to MCO server)
     */
    private GuiScreen guiScreenServer;
    private boolean field_147308_k = false;

    /**
     * Just an ordinary random number generator, used to randomize audio pitch of item/orb pickup and randomize both
     * particlespawn offset and velocity
     */
    private Random avRandomizer = new Random();
    private static final String __OBFID = "CL_00000878";

    public NetHandlerPlayClient(Minecraft p_i45061_1_, GuiScreen p_i45061_2_, NetworkManager p_i45061_3_)
    {
        this.gameController = p_i45061_1_;
        this.guiScreenServer = p_i45061_2_;
        this.netManager = p_i45061_3_;
    }

    /**
     * Clears the WorldClient instance associated with this NetHandlerPlayClient
     */
    public void cleanup()
    {
        this.clientWorldController = null;
    }

    /**
     * For scheduled network tasks. Used in NetHandlerPlayServer to send keep-alive packets and in NetHandlerLoginServer
     * for a login-timeout
     */
    public void onNetworkTick() {}

    /**
     * Registers some server properties (gametype,hardcore-mode,terraintype,difficulty,player limit), creates a new
     * WorldClient and sets the player initial dimension
     */
    public void handleJoinGame(S01PacketJoinGame p_147282_1_)
    {
        this.gameController.playerController = new PlayerControllerMP(this.gameController, this);
        this.clientWorldController = new WorldClient(this, new WorldSettings(0L, p_147282_1_.func_149198_e(), false, p_147282_1_.func_149195_d(), p_147282_1_.func_149196_i()), p_147282_1_.func_149194_f(), p_147282_1_.func_149192_g(), this.gameController.mcProfiler);
        this.clientWorldController.isClient = true;
        this.gameController.loadWorld(this.clientWorldController);
        this.gameController.thePlayer.dimension = p_147282_1_.func_149194_f();
        this.gameController.displayGuiScreen(new GuiDownloadTerrain(this));
        this.gameController.thePlayer.setEntityId(p_147282_1_.func_149197_c());
        this.currentServerMaxPlayers = p_147282_1_.func_149193_h();
        this.gameController.playerController.setGameType(p_147282_1_.func_149198_e());
        this.gameController.gameSettings.sendSettingsToServer();
        this.netManager.scheduleOutboundPacket(new C17PacketCustomPayload("MC|Brand", ClientBrandRetriever.getClientModName().getBytes(Charsets.UTF_8)), new GenericFutureListener[0]);
    }

    /**
     * Spawns an instance of the objecttype indicated by the packet and sets its position and momentum
     */
    public void handleSpawnObject(S0EPacketSpawnObject p_147235_1_)
    {
        double var2 = (double)p_147235_1_.func_148997_d() / 32.0D;
        double var4 = (double)p_147235_1_.func_148998_e() / 32.0D;
        double var6 = (double)p_147235_1_.func_148994_f() / 32.0D;
        Object var8 = null;

        if (p_147235_1_.func_148993_l() == 10)
        {
            var8 = EntityMinecart.createMinecart(this.clientWorldController, var2, var4, var6, p_147235_1_.func_149009_m());
        }
        else if (p_147235_1_.func_148993_l() == 90)
        {
            Entity var9 = this.clientWorldController.getEntityByID(p_147235_1_.func_149009_m());

            if (var9 instanceof EntityPlayer)
            {
                var8 = new EntityFishHook(this.clientWorldController, var2, var4, var6, (EntityPlayer)var9);
            }

            p_147235_1_.func_149002_g(0);
        }
        else if (p_147235_1_.func_148993_l() == 60)
        {
            var8 = new EntityArrow(this.clientWorldController, var2, var4, var6);
        }
        else if (p_147235_1_.func_148993_l() == 61)
        {
            var8 = new EntitySnowball(this.clientWorldController, var2, var4, var6);
        }
        else if (p_147235_1_.func_148993_l() == 71)
        {
            var8 = new EntityItemFrame(this.clientWorldController, (int)var2, (int)var4, (int)var6, p_147235_1_.func_149009_m());
            p_147235_1_.func_149002_g(0);
        }
        else if (p_147235_1_.func_148993_l() == 77)
        {
            var8 = new EntityLeashKnot(this.clientWorldController, (int)var2, (int)var4, (int)var6);
            p_147235_1_.func_149002_g(0);
        }
        else if (p_147235_1_.func_148993_l() == 65)
        {
            var8 = new EntityEnderPearl(this.clientWorldController, var2, var4, var6);
        }
        else if (p_147235_1_.func_148993_l() == 72)
        {
            var8 = new EntityEnderEye(this.clientWorldController, var2, var4, var6);
        }
        else if (p_147235_1_.func_148993_l() == 76)
        {
            var8 = new EntityFireworkRocket(this.clientWorldController, var2, var4, var6, (ItemStack)null);
        }
        else if (p_147235_1_.func_148993_l() == 63)
        {
            var8 = new EntityLargeFireball(this.clientWorldController, var2, var4, var6, (double)p_147235_1_.func_149010_g() / 8000.0D, (double)p_147235_1_.func_149004_h() / 8000.0D, (double)p_147235_1_.func_148999_i() / 8000.0D);
            p_147235_1_.func_149002_g(0);
        }
        else if (p_147235_1_.func_148993_l() == 64)
        {
            var8 = new EntitySmallFireball(this.clientWorldController, var2, var4, var6, (double)p_147235_1_.func_149010_g() / 8000.0D, (double)p_147235_1_.func_149004_h() / 8000.0D, (double)p_147235_1_.func_148999_i() / 8000.0D);
            p_147235_1_.func_149002_g(0);
        }
        else if (p_147235_1_.func_148993_l() == 66)
        {
            var8 = new EntityWitherSkull(this.clientWorldController, var2, var4, var6, (double)p_147235_1_.func_149010_g() / 8000.0D, (double)p_147235_1_.func_149004_h() / 8000.0D, (double)p_147235_1_.func_148999_i() / 8000.0D);
            p_147235_1_.func_149002_g(0);
        }
        else if (p_147235_1_.func_148993_l() == 62)
        {
            var8 = new EntityEgg(this.clientWorldController, var2, var4, var6);
        }
        else if (p_147235_1_.func_148993_l() == 73)
        {
            var8 = new EntityPotion(this.clientWorldController, var2, var4, var6, p_147235_1_.func_149009_m());
            p_147235_1_.func_149002_g(0);
        }
        else if (p_147235_1_.func_148993_l() == 75)
        {
            var8 = new EntityExpBottle(this.clientWorldController, var2, var4, var6);
            p_147235_1_.func_149002_g(0);
        }
        else if (p_147235_1_.func_148993_l() == 1)
        {
            var8 = new EntityBoat(this.clientWorldController, var2, var4, var6);
        }
        else if (p_147235_1_.func_148993_l() == 50)
        {
            var8 = new EntityTNTPrimed(this.clientWorldController, var2, var4, var6, (EntityLivingBase)null);
        }
        else if (p_147235_1_.func_148993_l() == 51)
        {
            var8 = new EntityEnderCrystal(this.clientWorldController, var2, var4, var6);
        }
        else if (p_147235_1_.func_148993_l() == 2)
        {
            var8 = new EntityItem(this.clientWorldController, var2, var4, var6);
        }
        else if (p_147235_1_.func_148993_l() == 70)
        {
            var8 = new EntityFallingBlock(this.clientWorldController, var2, var4, var6, Block.getBlockById(p_147235_1_.func_149009_m() & 65535), p_147235_1_.func_149009_m() >> 16);
            p_147235_1_.func_149002_g(0);
        }

        if (var8 != null)
        {
            ((Entity)var8).serverPosX = p_147235_1_.func_148997_d();
            ((Entity)var8).serverPosY = p_147235_1_.func_148998_e();
            ((Entity)var8).serverPosZ = p_147235_1_.func_148994_f();
            ((Entity)var8).rotationPitch = (float)(p_147235_1_.func_149008_j() * 360) / 256.0F;
            ((Entity)var8).rotationYaw = (float)(p_147235_1_.func_149006_k() * 360) / 256.0F;
            Entity[] var12 = ((Entity)var8).getParts();

            if (var12 != null)
            {
                int var10 = p_147235_1_.func_149001_c() - ((Entity)var8).getEntityId();

                for (int var11 = 0; var11 < var12.length; ++var11)
                {
                    var12[var11].setEntityId(var12[var11].getEntityId() + var10);
                }
            }

            ((Entity)var8).setEntityId(p_147235_1_.func_149001_c());
            this.clientWorldController.addEntityToWorld(p_147235_1_.func_149001_c(), (Entity)var8);

            if (p_147235_1_.func_149009_m() > 0)
            {
                if (p_147235_1_.func_148993_l() == 60)
                {
                    Entity var13 = this.clientWorldController.getEntityByID(p_147235_1_.func_149009_m());

                    if (var13 instanceof EntityLivingBase)
                    {
                        EntityArrow var14 = (EntityArrow)var8;
                        var14.shootingEntity = var13;
                    }
                }

                ((Entity)var8).setVelocity((double)p_147235_1_.func_149010_g() / 8000.0D, (double)p_147235_1_.func_149004_h() / 8000.0D, (double)p_147235_1_.func_148999_i() / 8000.0D);
            }
        }
    }

    /**
     * Spawns an experience orb and sets its value (amount of XP)
     */
    public void handleSpawnExperienceOrb(S11PacketSpawnExperienceOrb p_147286_1_)
    {
        EntityXPOrb var2 = new EntityXPOrb(this.clientWorldController, (double)p_147286_1_.func_148984_d(), (double)p_147286_1_.func_148983_e(), (double)p_147286_1_.func_148982_f(), p_147286_1_.func_148986_g());
        var2.serverPosX = p_147286_1_.func_148984_d();
        var2.serverPosY = p_147286_1_.func_148983_e();
        var2.serverPosZ = p_147286_1_.func_148982_f();
        var2.rotationYaw = 0.0F;
        var2.rotationPitch = 0.0F;
        var2.setEntityId(p_147286_1_.func_148985_c());
        this.clientWorldController.addEntityToWorld(p_147286_1_.func_148985_c(), var2);
    }

    /**
     * Handles globally visible entities. Used in vanilla for lightning bolts
     */
    public void handleSpawnGlobalEntity(S2CPacketSpawnGlobalEntity p_147292_1_)
    {
        double var2 = (double)p_147292_1_.func_149051_d() / 32.0D;
        double var4 = (double)p_147292_1_.func_149050_e() / 32.0D;
        double var6 = (double)p_147292_1_.func_149049_f() / 32.0D;
        EntityLightningBolt var8 = null;

        if (p_147292_1_.func_149053_g() == 1)
        {
            var8 = new EntityLightningBolt(this.clientWorldController, var2, var4, var6);
        }

        if (var8 != null)
        {
            var8.serverPosX = p_147292_1_.func_149051_d();
            var8.serverPosY = p_147292_1_.func_149050_e();
            var8.serverPosZ = p_147292_1_.func_149049_f();
            var8.rotationYaw = 0.0F;
            var8.rotationPitch = 0.0F;
            var8.setEntityId(p_147292_1_.func_149052_c());
            this.clientWorldController.addWeatherEffect(var8);
        }
    }

    /**
     * Handles the spawning of a painting object
     */
    public void handleSpawnPainting(S10PacketSpawnPainting p_147288_1_)
    {
        EntityPainting var2 = new EntityPainting(this.clientWorldController, p_147288_1_.func_148964_d(), p_147288_1_.func_148963_e(), p_147288_1_.func_148962_f(), p_147288_1_.func_148966_g(), p_147288_1_.func_148961_h());
        this.clientWorldController.addEntityToWorld(p_147288_1_.func_148965_c(), var2);
    }

    /**
     * Sets the velocity of the specified entity to the specified value
     */
    public void handleEntityVelocity(S12PacketEntityVelocity p_147244_1_)
    {
        Entity var2 = this.clientWorldController.getEntityByID(p_147244_1_.func_149412_c());

        if (var2 != null)
        {
            var2.setVelocity((double)p_147244_1_.func_149411_d() / 8000.0D, (double)p_147244_1_.func_149410_e() / 8000.0D, (double)p_147244_1_.func_149409_f() / 8000.0D);
        }
    }

    /**
     * Invoked when the server registers new proximate objects in your watchlist or when objects in your watchlist have
     * changed -> Registers any changes locally
     */
    public void handleEntityMetadata(S1CPacketEntityMetadata p_147284_1_)
    {
        Entity var2 = this.clientWorldController.getEntityByID(p_147284_1_.func_149375_d());

        if (var2 != null && p_147284_1_.func_149376_c() != null)
        {
            var2.getDataWatcher().updateWatchedObjectsFromList(p_147284_1_.func_149376_c());
        }
    }

    /**
     * Handles the creation of a nearby player entity, sets the position and held item
     */
    public void handleSpawnPlayer(S0CPacketSpawnPlayer p_147237_1_)
    {
        double var2 = (double)p_147237_1_.func_148942_f() / 32.0D;
        double var4 = (double)p_147237_1_.func_148949_g() / 32.0D;
        double var6 = (double)p_147237_1_.func_148946_h() / 32.0D;
        float var8 = (float)(p_147237_1_.func_148941_i() * 360) / 256.0F;
        float var9 = (float)(p_147237_1_.func_148945_j() * 360) / 256.0F;
        EntityOtherPlayerMP var10 = new EntityOtherPlayerMP(this.gameController.theWorld, p_147237_1_.func_148948_e());
        var10.prevPosX = var10.lastTickPosX = (double)(var10.serverPosX = p_147237_1_.func_148942_f());
        var10.prevPosY = var10.lastTickPosY = (double)(var10.serverPosY = p_147237_1_.func_148949_g());
        var10.prevPosZ = var10.lastTickPosZ = (double)(var10.serverPosZ = p_147237_1_.func_148946_h());
        int var11 = p_147237_1_.func_148947_k();

        if (var11 == 0)
        {
            var10.inventory.mainInventory[var10.inventory.currentItem] = null;
        }
        else
        {
            var10.inventory.mainInventory[var10.inventory.currentItem] = new ItemStack(Item.getItemById(var11), 1, 0);
        }

        var10.setPositionAndRotation(var2, var4, var6, var8, var9);
        this.clientWorldController.addEntityToWorld(p_147237_1_.func_148943_d(), var10);
        List var12 = p_147237_1_.func_148944_c();

        if (var12 != null)
        {
            var10.getDataWatcher().updateWatchedObjectsFromList(var12);
        }
    }

    /**
     * Updates an entity's position and rotation as specified by the packet
     */
    public void handleEntityTeleport(S18PacketEntityTeleport p_147275_1_)
    {
        Entity var2 = this.clientWorldController.getEntityByID(p_147275_1_.func_149451_c());

        if (var2 != null)
        {
            var2.serverPosX = p_147275_1_.func_149449_d();
            var2.serverPosY = p_147275_1_.func_149448_e();
            var2.serverPosZ = p_147275_1_.func_149446_f();
            double var3 = (double)var2.serverPosX / 32.0D;
            double var5 = (double)var2.serverPosY / 32.0D + 0.015625D;
            double var7 = (double)var2.serverPosZ / 32.0D;
            float var9 = (float)(p_147275_1_.func_149450_g() * 360) / 256.0F;
            float var10 = (float)(p_147275_1_.func_149447_h() * 360) / 256.0F;
            var2.setPositionAndRotation2(var3, var5, var7, var9, var10, 3);
        }
    }

    /**
     * Updates which hotbar slot of the player is currently selected
     */
    public void handleHeldItemChange(S09PacketHeldItemChange p_147257_1_)
    {
        if (p_147257_1_.func_149385_c() >= 0 && p_147257_1_.func_149385_c() < InventoryPlayer.getHotbarSize())
        {
            this.gameController.thePlayer.inventory.currentItem = p_147257_1_.func_149385_c();
        }
    }

    /**
     * Updates the specified entity's position by the specified relative moment and absolute rotation. Note that
     * subclassing of the packet allows for the specification of a subset of this data (e.g. only rel. position, abs.
     * rotation or both).
     */
    public void handleEntityMovement(S14PacketEntity p_147259_1_)
    {
        Entity var2 = p_147259_1_.func_149065_a(this.clientWorldController);

        if (var2 != null)
        {
            var2.serverPosX += p_147259_1_.func_149062_c();
            var2.serverPosY += p_147259_1_.func_149061_d();
            var2.serverPosZ += p_147259_1_.func_149064_e();
            double var3 = (double)var2.serverPosX / 32.0D;
            double var5 = (double)var2.serverPosY / 32.0D;
            double var7 = (double)var2.serverPosZ / 32.0D;
            float var9 = p_147259_1_.func_149060_h() ? (float)(p_147259_1_.func_149066_f() * 360) / 256.0F : var2.rotationYaw;
            float var10 = p_147259_1_.func_149060_h() ? (float)(p_147259_1_.func_149063_g() * 360) / 256.0F : var2.rotationPitch;
            var2.setPositionAndRotation2(var3, var5, var7, var9, var10, 3);
        }
    }

    /**
     * Updates the direction in which the specified entity is looking, normally this head rotation is independent of the
     * rotation of the entity itself
     */
    public void handleEntityHeadLook(S19PacketEntityHeadLook p_147267_1_)
    {
        Entity var2 = p_147267_1_.func_149381_a(this.clientWorldController);

        if (var2 != null)
        {
            float var3 = (float)(p_147267_1_.func_149380_c() * 360) / 256.0F;
            var2.setRotationYawHead(var3);
        }
    }

    /**
     * Locally eliminates the entities. Invoked by the server when the items are in fact destroyed, or the player is no
     * longer registered as required to monitor them. The latter  happens when distance between the player and item
     * increases beyond a certain treshold (typically the viewing distance)
     */
    public void handleDestroyEntities(S13PacketDestroyEntities p_147238_1_)
    {
        for (int var2 = 0; var2 < p_147238_1_.func_149098_c().length; ++var2)
        {
            this.clientWorldController.removeEntityFromWorld(p_147238_1_.func_149098_c()[var2]);
        }
    }

    /**
     * Handles changes in player positioning and rotation such as when travelling to a new dimension, (re)spawning,
     * mounting horses etc. Seems to immediately reply to the server with the clients post-processing perspective on the
     * player positioning
     */
    public void handlePlayerPosLook(S08PacketPlayerPosLook p_147258_1_)
    {
        EntityClientPlayerMP var2 = this.gameController.thePlayer;
        double var3 = p_147258_1_.func_148932_c();
        double var5 = p_147258_1_.func_148928_d();
        double var7 = p_147258_1_.func_148933_e();
        float var9 = p_147258_1_.func_148931_f();
        float var10 = p_147258_1_.func_148930_g();
        var2.ySize = 0.0F;
        var2.motionX = var2.motionY = var2.motionZ = 0.0D;
        var2.setPositionAndRotation(var3, var5, var7, var9, var10);
        this.netManager.scheduleOutboundPacket(new C03PacketPlayer.C06PacketPlayerPosLook(var2.posX, var2.boundingBox.minY, var2.posY, var2.posZ, p_147258_1_.func_148931_f(), p_147258_1_.func_148930_g(), p_147258_1_.func_148929_h()), new GenericFutureListener[0]);

        if (!this.doneLoadingTerrain)
        {
            this.gameController.thePlayer.prevPosX = this.gameController.thePlayer.posX;
            this.gameController.thePlayer.prevPosY = this.gameController.thePlayer.posY;
            this.gameController.thePlayer.prevPosZ = this.gameController.thePlayer.posZ;
            this.doneLoadingTerrain = true;
            this.gameController.displayGuiScreen((GuiScreen)null);
        }
    }

    /**
     * Received from the servers PlayerManager if between 1 and 64 blocks in a chunk are changed. If only one block
     * requires an update, the server sends S23PacketBlockChange and if 64 or more blocks are changed, the server sends
     * S21PacketChunkData
     */
    public void handleMultiBlockChange(S22PacketMultiBlockChange p_147287_1_)
    {
        int var2 = p_147287_1_.func_148920_c().chunkXPos * 16;
        int var3 = p_147287_1_.func_148920_c().chunkZPos * 16;

        if (p_147287_1_.func_148921_d() != null)
        {
            DataInputStream var4 = new DataInputStream(new ByteArrayInputStream(p_147287_1_.func_148921_d()));

            try
            {
                for (int var5 = 0; var5 < p_147287_1_.func_148922_e(); ++var5)
                {
                    short var6 = var4.readShort();
                    short var7 = var4.readShort();
                    int var8 = var7 >> 4 & 4095;
                    int var9 = var7 & 15;
                    int var10 = var6 >> 12 & 15;
                    int var11 = var6 >> 8 & 15;
                    int var12 = var6 & 255;
                    this.clientWorldController.func_147492_c(var10 + var2, var12, var11 + var3, Block.getBlockById(var8), var9);
                }
            }
            catch (IOException var13)
            {
                ;
            }
        }
    }

    /**
     * Updates the specified chunk with the supplied data, marks it for re-rendering and lighting recalculation
     */
    public void handleChunkData(S21PacketChunkData p_147263_1_)
    {
        if (p_147263_1_.func_149274_i())
        {
            if (p_147263_1_.func_149276_g() == 0)
            {
                this.clientWorldController.doPreChunk(p_147263_1_.func_149273_e(), p_147263_1_.func_149271_f(), false);
                return;
            }

            this.clientWorldController.doPreChunk(p_147263_1_.func_149273_e(), p_147263_1_.func_149271_f(), true);
        }

        this.clientWorldController.invalidateBlockReceiveRegion(p_147263_1_.func_149273_e() << 4, 0, p_147263_1_.func_149271_f() << 4, (p_147263_1_.func_149273_e() << 4) + 15, 256, (p_147263_1_.func_149271_f() << 4) + 15);
        Chunk var2 = this.clientWorldController.getChunkFromChunkCoords(p_147263_1_.func_149273_e(), p_147263_1_.func_149271_f());
        var2.fillChunk(p_147263_1_.func_149272_d(), p_147263_1_.func_149276_g(), p_147263_1_.func_149270_h(), p_147263_1_.func_149274_i());
        this.clientWorldController.markBlockRangeForRenderUpdate(p_147263_1_.func_149273_e() << 4, 0, p_147263_1_.func_149271_f() << 4, (p_147263_1_.func_149273_e() << 4) + 15, 256, (p_147263_1_.func_149271_f() << 4) + 15);

        if (!p_147263_1_.func_149274_i() || !(this.clientWorldController.provider instanceof WorldProviderSurface))
        {
            var2.resetRelightChecks();
        }
    }

    /**
     * Updates the block and metadata and generates a blockupdate (and notify the clients)
     */
    public void handleBlockChange(S23PacketBlockChange p_147234_1_)
    {
        this.clientWorldController.func_147492_c(p_147234_1_.func_148879_d(), p_147234_1_.func_148878_e(), p_147234_1_.func_148877_f(), p_147234_1_.func_148880_c(), p_147234_1_.func_148881_g());
    }

    /**
     * Closes the network channel
     */
    public void handleDisconnect(S40PacketDisconnect p_147253_1_)
    {
        /* WDL >>> */
        if (WDL.downloading)
        {
            WDL.stop();

            try
            {
                Thread.sleep(2000L);
            }
            catch (Exception var3)
            {
                ;
            }
        }
        /* <<< WDL */
        
        this.netManager.closeChannel(p_147253_1_.func_149165_c());
    }

    /**
     * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
     */
    public void onDisconnect(IChatComponent p_147231_1_)
    {
        /* WDL >>> */
        if (WDL.downloading)
        {
            WDL.stop();

            try
            {
                Thread.sleep(2000L);
            }
            catch (Exception var3)
            {
                ;
            }
        }
        /* <<< WDL */
        this.gameController.loadWorld((WorldClient)null);

        if (this.guiScreenServer != null)
        {
            this.gameController.displayGuiScreen(new GuiScreenDisconnectedOnline(this.guiScreenServer, "disconnect.lost", p_147231_1_));
        }
        else
        {
            this.gameController.displayGuiScreen(new GuiDisconnected(new GuiMultiplayer(new GuiMainMenu()), "disconnect.lost", p_147231_1_));
        }
    }

    public void addToSendQueue(Packet p_147297_1_)
    {
        this.netManager.scheduleOutboundPacket(p_147297_1_, new GenericFutureListener[0]);
    }

    public void handleCollectItem(S0DPacketCollectItem p_147246_1_)
    {
        Entity var2 = this.clientWorldController.getEntityByID(p_147246_1_.func_149354_c());
        Object var3 = (EntityLivingBase)this.clientWorldController.getEntityByID(p_147246_1_.func_149353_d());

        if (var3 == null)
        {
            var3 = this.gameController.thePlayer;
        }

        if (var2 != null)
        {
            if (var2 instanceof EntityXPOrb)
            {
                this.clientWorldController.playSoundAtEntity(var2, "random.orb", 0.2F, ((this.avRandomizer.nextFloat() - this.avRandomizer.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }
            else
            {
                this.clientWorldController.playSoundAtEntity(var2, "random.pop", 0.2F, ((this.avRandomizer.nextFloat() - this.avRandomizer.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }

            this.gameController.effectRenderer.addEffect(new EntityPickupFX(this.gameController.theWorld, var2, (Entity)var3, -0.5F));
            this.clientWorldController.removeEntityFromWorld(p_147246_1_.func_149354_c());
        }
    }

    /**
     * Prints a chatmessage in the chat GUI
     */
    public void handleChat(S02PacketChat p_147251_1_)
    {
        /* WDL >>> */
        String var2 = p_147251_1_.func_148915_c().getFormattedText();
        WDL.handleServerSeedMessage(var2);
        /* <<< WDL */

        this.gameController.ingameGUI.getChatGUI().func_146227_a(p_147251_1_.func_148915_c());
    }

    /**
     * Renders a specified animation: Waking up a player, a living entity swinging its currently held item, being hurt
     * or receiving a critical hit by normal or magical means
     */
    public void handleAnimation(S0BPacketAnimation p_147279_1_)
    {
        Entity var2 = this.clientWorldController.getEntityByID(p_147279_1_.func_148978_c());

        if (var2 != null)
        {
            if (p_147279_1_.func_148977_d() == 0)
            {
                EntityLivingBase var3 = (EntityLivingBase)var2;
                var3.swingItem();
            }
            else if (p_147279_1_.func_148977_d() == 1)
            {
                var2.performHurtAnimation();
            }
            else if (p_147279_1_.func_148977_d() == 2)
            {
                EntityPlayer var4 = (EntityPlayer)var2;
                var4.wakeUpPlayer(false, false, false);
            }
            else if (p_147279_1_.func_148977_d() == 4)
            {
                this.gameController.effectRenderer.addEffect(new EntityCrit2FX(this.gameController.theWorld, var2));
            }
            else if (p_147279_1_.func_148977_d() == 5)
            {
                EntityCrit2FX var5 = new EntityCrit2FX(this.gameController.theWorld, var2, "magicCrit");
                this.gameController.effectRenderer.addEffect(var5);
            }
        }
    }

    /**
     * Retrieves the player identified by the packet, puts him to sleep if possible (and flags whether all players are
     * asleep)
     */
    public void handleUseBed(S0APacketUseBed p_147278_1_)
    {
        p_147278_1_.func_149091_a(this.clientWorldController).sleepInBedAt(p_147278_1_.func_149092_c(), p_147278_1_.func_149090_d(), p_147278_1_.func_149089_e());
    }

    /**
     * Spawns the mob entity at the specified location, with the specified rotation, momentum and type. Updates the
     * entities Datawatchers with the entity metadata specified in the packet
     */
    public void handleSpawnMob(S0FPacketSpawnMob p_147281_1_)
    {
        double var2 = (double)p_147281_1_.func_149023_f() / 32.0D;
        double var4 = (double)p_147281_1_.func_149034_g() / 32.0D;
        double var6 = (double)p_147281_1_.func_149029_h() / 32.0D;
        float var8 = (float)(p_147281_1_.func_149028_l() * 360) / 256.0F;
        float var9 = (float)(p_147281_1_.func_149030_m() * 360) / 256.0F;
        EntityLivingBase var10 = (EntityLivingBase)EntityList.createEntityByID(p_147281_1_.func_149025_e(), this.gameController.theWorld);
        var10.serverPosX = p_147281_1_.func_149023_f();
        var10.serverPosY = p_147281_1_.func_149034_g();
        var10.serverPosZ = p_147281_1_.func_149029_h();
        var10.rotationYawHead = (float)(p_147281_1_.func_149032_n() * 360) / 256.0F;
        Entity[] var11 = var10.getParts();

        if (var11 != null)
        {
            int var12 = p_147281_1_.func_149024_d() - var10.getEntityId();

            for (int var13 = 0; var13 < var11.length; ++var13)
            {
                var11[var13].setEntityId(var11[var13].getEntityId() + var12);
            }
        }

        var10.setEntityId(p_147281_1_.func_149024_d());
        var10.setPositionAndRotation(var2, var4, var6, var8, var9);
        var10.motionX = (double)((float)p_147281_1_.func_149026_i() / 8000.0F);
        var10.motionY = (double)((float)p_147281_1_.func_149033_j() / 8000.0F);
        var10.motionZ = (double)((float)p_147281_1_.func_149031_k() / 8000.0F);
        this.clientWorldController.addEntityToWorld(p_147281_1_.func_149024_d(), var10);
        List var14 = p_147281_1_.func_149027_c();

        if (var14 != null)
        {
            var10.getDataWatcher().updateWatchedObjectsFromList(var14);
        }
    }

    public void handleTimeUpdate(S03PacketTimeUpdate p_147285_1_)
    {
        this.gameController.theWorld.func_82738_a(p_147285_1_.func_149366_c());
        this.gameController.theWorld.setWorldTime(p_147285_1_.func_149365_d());
    }

    public void handleSpawnPosition(S05PacketSpawnPosition p_147271_1_)
    {
        this.gameController.thePlayer.setSpawnChunk(new ChunkCoordinates(p_147271_1_.func_149360_c(), p_147271_1_.func_149359_d(), p_147271_1_.func_149358_e()), true);
        this.gameController.theWorld.getWorldInfo().setSpawnPosition(p_147271_1_.func_149360_c(), p_147271_1_.func_149359_d(), p_147271_1_.func_149358_e());
    }

    public void handleEntityAttach(S1BPacketEntityAttach p_147243_1_)
    {
        Object var2 = this.clientWorldController.getEntityByID(p_147243_1_.func_149403_d());
        Entity var3 = this.clientWorldController.getEntityByID(p_147243_1_.func_149402_e());

        if (p_147243_1_.func_149404_c() == 0)
        {
            boolean var4 = false;

            if (p_147243_1_.func_149403_d() == this.gameController.thePlayer.getEntityId())
            {
                var2 = this.gameController.thePlayer;

                if (var3 instanceof EntityBoat)
                {
                    ((EntityBoat)var3).setIsBoatEmpty(false);
                }

                var4 = ((Entity)var2).ridingEntity == null && var3 != null;
            }
            else if (var3 instanceof EntityBoat)
            {
                ((EntityBoat)var3).setIsBoatEmpty(true);
            }

            if (var2 == null)
            {
                return;
            }

            ((Entity)var2).mountEntity(var3);

            if (var4)
            {
                GameSettings var5 = this.gameController.gameSettings;
                this.gameController.ingameGUI.func_110326_a(I18n.format("mount.onboard", new Object[] {GameSettings.getKeyDisplayString(var5.keyBindSneak.getKeyCode())}), false);
            }
        }
        else if (p_147243_1_.func_149404_c() == 1 && var2 != null && var2 instanceof EntityLiving)
        {
            if (var3 != null)
            {
                ((EntityLiving)var2).setLeashedToEntity(var3, false);
            }
            else
            {
                ((EntityLiving)var2).clearLeashed(false, false);
            }
        }
    }

    /**
     * Invokes the entities' handleUpdateHealth method which is implemented in LivingBase (hurt/death),
     * MinecartMobSpawner (spawn delay), FireworkRocket & MinecartTNT (explosion), IronGolem (throwing,...), Witch
     * (spawn particles), Zombie (villager transformation), Animal (breeding mode particles), Horse (breeding/smoke
     * particles), Sheep (...), Tameable (...), Villager (particles for breeding mode, angry and happy), Wolf (...)
     */
    public void handleEntityStatus(S19PacketEntityStatus p_147236_1_)
    {
        Entity var2 = p_147236_1_.func_149161_a(this.clientWorldController);

        if (var2 != null)
        {
            var2.handleHealthUpdate(p_147236_1_.func_149160_c());
        }
    }

    public void handleUpdateHealth(S06PacketUpdateHealth p_147249_1_)
    {
        this.gameController.thePlayer.setPlayerSPHealth(p_147249_1_.func_149332_c());
        this.gameController.thePlayer.getFoodStats().setFoodLevel(p_147249_1_.func_149330_d());
        this.gameController.thePlayer.getFoodStats().setFoodSaturationLevel(p_147249_1_.func_149331_e());
    }

    public void handleSetExperience(S1FPacketSetExperience p_147295_1_)
    {
        this.gameController.thePlayer.setXPStats(p_147295_1_.func_149397_c(), p_147295_1_.func_149396_d(), p_147295_1_.func_149395_e());
    }

    public void handleRespawn(S07PacketRespawn p_147280_1_)
    {
        if (p_147280_1_.func_149082_c() != this.gameController.thePlayer.dimension)
        {
            this.doneLoadingTerrain = false;
            Scoreboard var2 = this.clientWorldController.getScoreboard();
            this.clientWorldController = new WorldClient(this, new WorldSettings(0L, p_147280_1_.func_149083_e(), false, this.gameController.theWorld.getWorldInfo().isHardcoreModeEnabled(), p_147280_1_.func_149080_f()), p_147280_1_.func_149082_c(), p_147280_1_.func_149081_d(), this.gameController.mcProfiler);
            this.clientWorldController.setWorldScoreboard(var2);
            this.clientWorldController.isClient = true;
            this.gameController.loadWorld(this.clientWorldController);
            this.gameController.thePlayer.dimension = p_147280_1_.func_149082_c();
            this.gameController.displayGuiScreen(new GuiDownloadTerrain(this));
        }

        this.gameController.setDimensionAndSpawnPlayer(p_147280_1_.func_149082_c());
        this.gameController.playerController.setGameType(p_147280_1_.func_149083_e());
    }

    /**
     * Initiates a new explosion (sound, particles, drop spawn) for the affected blocks indicated by the packet.
     */
    public void handleExplosion(S27PacketExplosion p_147283_1_)
    {
        Explosion var2 = new Explosion(this.gameController.theWorld, (Entity)null, p_147283_1_.func_149148_f(), p_147283_1_.func_149143_g(), p_147283_1_.func_149145_h(), p_147283_1_.func_149146_i());
        var2.affectedBlockPositions = p_147283_1_.func_149150_j();
        var2.doExplosionB(true);
        this.gameController.thePlayer.motionX += (double)p_147283_1_.func_149149_c();
        this.gameController.thePlayer.motionY += (double)p_147283_1_.func_149144_d();
        this.gameController.thePlayer.motionZ += (double)p_147283_1_.func_149147_e();
    }

    /**
     * Displays a GUI by ID. In order starting from id 0: Chest, Workbench, Furnace, Dispenser, Enchanting table,
     * Brewing stand, Villager merchant, Beacon, Anvil, Hopper, Dropper, Horse
     */
    public void handleOpenWindow(S2DPacketOpenWindow p_147265_1_)
    {
        EntityClientPlayerMP var2 = this.gameController.thePlayer;

        switch (p_147265_1_.func_148899_d())
        {
            case 0:
                var2.displayGUIChest(new InventoryBasic(p_147265_1_.func_148902_e(), p_147265_1_.func_148900_g(), p_147265_1_.func_148898_f()));
                var2.openContainer.windowId = p_147265_1_.func_148901_c();
                break;

            case 1:
                var2.displayGUIWorkbench(MathHelper.floor_double(var2.posX), MathHelper.floor_double(var2.posY), MathHelper.floor_double(var2.posZ));
                var2.openContainer.windowId = p_147265_1_.func_148901_c();
                break;

            case 2:
                TileEntityFurnace var4 = new TileEntityFurnace();

                if (p_147265_1_.func_148900_g())
                {
                    var4.func_145951_a(p_147265_1_.func_148902_e());
                }

                var2.func_146101_a(var4);
                var2.openContainer.windowId = p_147265_1_.func_148901_c();
                break;

            case 3:
                TileEntityDispenser var7 = new TileEntityDispenser();

                if (p_147265_1_.func_148900_g())
                {
                    var7.func_146018_a(p_147265_1_.func_148902_e());
                }

                var2.func_146102_a(var7);
                var2.openContainer.windowId = p_147265_1_.func_148901_c();
                break;

            case 4:
                var2.displayGUIEnchantment(MathHelper.floor_double(var2.posX), MathHelper.floor_double(var2.posY), MathHelper.floor_double(var2.posZ), p_147265_1_.func_148900_g() ? p_147265_1_.func_148902_e() : null);
                var2.openContainer.windowId = p_147265_1_.func_148901_c();
                break;

            case 5:
                TileEntityBrewingStand var5 = new TileEntityBrewingStand();

                if (p_147265_1_.func_148900_g())
                {
                    var5.func_145937_a(p_147265_1_.func_148902_e());
                }

                var2.func_146098_a(var5);
                var2.openContainer.windowId = p_147265_1_.func_148901_c();
                break;

            case 6:
                var2.displayGUIMerchant(new NpcMerchant(var2), p_147265_1_.func_148900_g() ? p_147265_1_.func_148902_e() : null);
                var2.openContainer.windowId = p_147265_1_.func_148901_c();
                break;

            case 7:
                TileEntityBeacon var8 = new TileEntityBeacon();
                var2.func_146104_a(var8);

                if (p_147265_1_.func_148900_g())
                {
                    var8.func_145999_a(p_147265_1_.func_148902_e());
                }

                var2.openContainer.windowId = p_147265_1_.func_148901_c();
                break;

            case 8:
                var2.displayGUIAnvil(MathHelper.floor_double(var2.posX), MathHelper.floor_double(var2.posY), MathHelper.floor_double(var2.posZ));
                var2.openContainer.windowId = p_147265_1_.func_148901_c();
                break;

            case 9:
                TileEntityHopper var3 = new TileEntityHopper();

                if (p_147265_1_.func_148900_g())
                {
                    var3.func_145886_a(p_147265_1_.func_148902_e());
                }

                var2.func_146093_a(var3);
                var2.openContainer.windowId = p_147265_1_.func_148901_c();
                break;

            case 10:
                TileEntityDropper var6 = new TileEntityDropper();

                if (p_147265_1_.func_148900_g())
                {
                    var6.func_146018_a(p_147265_1_.func_148902_e());
                }

                var2.func_146102_a(var6);
                var2.openContainer.windowId = p_147265_1_.func_148901_c();
                break;

            case 11:
                Entity var9 = this.clientWorldController.getEntityByID(p_147265_1_.func_148897_h());

                if (var9 != null && var9 instanceof EntityHorse)
                {
                    var2.displayGUIHorse((EntityHorse)var9, new AnimalChest(p_147265_1_.func_148902_e(), p_147265_1_.func_148900_g(), p_147265_1_.func_148898_f()));
                    var2.openContainer.windowId = p_147265_1_.func_148901_c();
                }
        }
    }

    /**
     * Handles pickin up an ItemStack or dropping one in your inventory or an open (non-creative) container
     */
    public void handleSetSlot(S2FPacketSetSlot p_147266_1_)
    {
        EntityClientPlayerMP var2 = this.gameController.thePlayer;

        if (p_147266_1_.func_149175_c() == -1)
        {
            var2.inventory.setItemStack(p_147266_1_.func_149174_e());
        }
        else
        {
            boolean var3 = false;

            if (this.gameController.currentScreen instanceof GuiContainerCreative)
            {
                GuiContainerCreative var4 = (GuiContainerCreative)this.gameController.currentScreen;
                var3 = var4.func_147056_g() != CreativeTabs.tabInventory.getTabIndex();
            }

            if (p_147266_1_.func_149175_c() == 0 && p_147266_1_.func_149173_d() >= 36 && p_147266_1_.func_149173_d() < 45)
            {
                ItemStack var5 = var2.inventoryContainer.getSlot(p_147266_1_.func_149173_d()).getStack();

                if (p_147266_1_.func_149174_e() != null && (var5 == null || var5.stackSize < p_147266_1_.func_149174_e().stackSize))
                {
                    p_147266_1_.func_149174_e().animationsToGo = 5;
                }

                var2.inventoryContainer.putStackInSlot(p_147266_1_.func_149173_d(), p_147266_1_.func_149174_e());
            }
            else if (p_147266_1_.func_149175_c() == var2.openContainer.windowId && (p_147266_1_.func_149175_c() != 0 || !var3))
            {
                var2.openContainer.putStackInSlot(p_147266_1_.func_149173_d(), p_147266_1_.func_149174_e());
            }
        }
    }

    /**
     * Verifies that the server and client are synchronized with respect to the inventory/container opened by the player
     * and confirms if it is the case.
     */
    public void handleConfirmTransaction(S32PacketConfirmTransaction p_147239_1_)
    {
        Container var2 = null;
        EntityClientPlayerMP var3 = this.gameController.thePlayer;

        if (p_147239_1_.func_148889_c() == 0)
        {
            var2 = var3.inventoryContainer;
        }
        else if (p_147239_1_.func_148889_c() == var3.openContainer.windowId)
        {
            var2 = var3.openContainer;
        }

        if (var2 != null && !p_147239_1_.func_148888_e())
        {
            this.addToSendQueue(new C0FPacketConfirmTransaction(p_147239_1_.func_148889_c(), p_147239_1_.func_148890_d(), true));
        }
    }

    /**
     * Handles the placement of a specified ItemStack in a specified container/inventory slot
     */
    public void handleWindowItems(S30PacketWindowItems p_147241_1_)
    {
        EntityClientPlayerMP var2 = this.gameController.thePlayer;

        if (p_147241_1_.func_148911_c() == 0)
        {
            var2.inventoryContainer.putStacksInSlots(p_147241_1_.func_148910_d());
        }
        else if (p_147241_1_.func_148911_c() == var2.openContainer.windowId)
        {
            var2.openContainer.putStacksInSlots(p_147241_1_.func_148910_d());
        }
    }

    /**
     * Creates a sign in the specified location if it didn't exist and opens the GUI to edit its text
     */
    public void handleSignEditorOpen(S36PacketSignEditorOpen p_147268_1_)
    {
        Object var2 = this.clientWorldController.getTileEntity(p_147268_1_.func_149129_c(), p_147268_1_.func_149128_d(), p_147268_1_.func_149127_e());

        if (var2 == null)
        {
            var2 = new TileEntitySign();
            ((TileEntity)var2).setWorldObj(this.clientWorldController);
            ((TileEntity)var2).field_145851_c = p_147268_1_.func_149129_c();
            ((TileEntity)var2).field_145848_d = p_147268_1_.func_149128_d();
            ((TileEntity)var2).field_145849_e = p_147268_1_.func_149127_e();
        }

        this.gameController.thePlayer.func_146100_a((TileEntity)var2);
    }

    /**
     * Updates a specified sign with the specified text lines
     */
    public void handleUpdateSign(S33PacketUpdateSign p_147248_1_)
    {
        boolean var2 = false;

        if (this.gameController.theWorld.blockExists(p_147248_1_.func_149346_c(), p_147248_1_.func_149345_d(), p_147248_1_.func_149344_e()))
        {
            TileEntity var3 = this.gameController.theWorld.getTileEntity(p_147248_1_.func_149346_c(), p_147248_1_.func_149345_d(), p_147248_1_.func_149344_e());

            if (var3 instanceof TileEntitySign)
            {
                TileEntitySign var4 = (TileEntitySign)var3;

                if (var4.func_145914_a())
                {
                    for (int var5 = 0; var5 < 4; ++var5)
                    {
                        var4.field_145915_a[var5] = p_147248_1_.func_149347_f()[var5];
                    }

                    var4.onInventoryChanged();
                }

                var2 = true;
            }
        }

        if (!var2 && this.gameController.thePlayer != null)
        {
            this.gameController.thePlayer.addChatMessage(new ChatComponentText("Unable to locate sign at " + p_147248_1_.func_149346_c() + ", " + p_147248_1_.func_149345_d() + ", " + p_147248_1_.func_149344_e()));
        }
    }

    /**
     * Updates the NBTTagCompound metadata of instances of the following entitytypes: Mob spawners, command blocks,
     * beacons, skulls, flowerpot
     */
    public void handleUpdateTileEntity(S35PacketUpdateTileEntity p_147273_1_)
    {
        if (this.gameController.theWorld.blockExists(p_147273_1_.func_148856_c(), p_147273_1_.func_148855_d(), p_147273_1_.func_148854_e()))
        {
            TileEntity var2 = this.gameController.theWorld.getTileEntity(p_147273_1_.func_148856_c(), p_147273_1_.func_148855_d(), p_147273_1_.func_148854_e());

            if (var2 != null)
            {
                if (p_147273_1_.func_148853_f() == 1 && var2 instanceof TileEntityMobSpawner)
                {
                    var2.readFromNBT(p_147273_1_.func_148857_g());
                }
                else if (p_147273_1_.func_148853_f() == 2 && var2 instanceof TileEntityCommandBlock)
                {
                    var2.readFromNBT(p_147273_1_.func_148857_g());
                }
                else if (p_147273_1_.func_148853_f() == 3 && var2 instanceof TileEntityBeacon)
                {
                    var2.readFromNBT(p_147273_1_.func_148857_g());
                }
                else if (p_147273_1_.func_148853_f() == 4 && var2 instanceof TileEntitySkull)
                {
                    var2.readFromNBT(p_147273_1_.func_148857_g());
                }
                else if (p_147273_1_.func_148853_f() == 5 && var2 instanceof TileEntityFlowerPot)
                {
                    var2.readFromNBT(p_147273_1_.func_148857_g());
                }
            }
        }
    }

    /**
     * Sets the progressbar of the opened window to the specified value
     */
    public void handleWindowProperty(S31PacketWindowProperty p_147245_1_)
    {
        EntityClientPlayerMP var2 = this.gameController.thePlayer;

        if (var2.openContainer != null && var2.openContainer.windowId == p_147245_1_.func_149182_c())
        {
            var2.openContainer.updateProgressBar(p_147245_1_.func_149181_d(), p_147245_1_.func_149180_e());
        }
    }

    public void handleEntityEquipment(S04PacketEntityEquipment p_147242_1_)
    {
        Entity var2 = this.clientWorldController.getEntityByID(p_147242_1_.func_149389_d());

        if (var2 != null)
        {
            var2.setCurrentItemOrArmor(p_147242_1_.func_149388_e(), p_147242_1_.func_149390_c());
        }
    }

    /**
     * Resets the ItemStack held in hand and closes the window that is opened
     */
    public void handleCloseWindow(S2EPacketCloseWindow p_147276_1_)
    {
        this.gameController.thePlayer.closeScreenNoPacket();
    }

    /**
     * Triggers Block.onBlockEventReceived, which is implemented in BlockPistonBase for extension/retraction, BlockNote
     * for setting the instrument (including audiovisual feedback) and in BlockContainer to set the number of players
     * accessing a (Ender)Chest
     */
    public void handleBlockAction(S24PacketBlockAction p_147261_1_)
    {
        this.gameController.theWorld.func_147452_c(p_147261_1_.func_148867_d(), p_147261_1_.func_148866_e(), p_147261_1_.func_148865_f(), p_147261_1_.func_148868_c(), p_147261_1_.func_148869_g(), p_147261_1_.func_148864_h());
    }

    /**
     * Updates all registered IWorldAccess instances with destroyBlockInWorldPartially
     */
    public void handleBlockBreakAnim(S25PacketBlockBreakAnim p_147294_1_)
    {
        this.gameController.theWorld.destroyBlockInWorldPartially(p_147294_1_.func_148845_c(), p_147294_1_.func_148844_d(), p_147294_1_.func_148843_e(), p_147294_1_.func_148842_f(), p_147294_1_.func_148846_g());
    }

    public void handleMapChunkBulk(S26PacketMapChunkBulk p_147269_1_)
    {
        for (int var2 = 0; var2 < p_147269_1_.func_149254_d(); ++var2)
        {
            int var3 = p_147269_1_.func_149255_a(var2);
            int var4 = p_147269_1_.func_149253_b(var2);
            this.clientWorldController.doPreChunk(var3, var4, true);
            this.clientWorldController.invalidateBlockReceiveRegion(var3 << 4, 0, var4 << 4, (var3 << 4) + 15, 256, (var4 << 4) + 15);
            Chunk var5 = this.clientWorldController.getChunkFromChunkCoords(var3, var4);
            var5.fillChunk(p_147269_1_.func_149256_c(var2), p_147269_1_.func_149252_e()[var2], p_147269_1_.func_149257_f()[var2], true);
            this.clientWorldController.markBlockRangeForRenderUpdate(var3 << 4, 0, var4 << 4, (var3 << 4) + 15, 256, (var4 << 4) + 15);

            if (!(this.clientWorldController.provider instanceof WorldProviderSurface))
            {
                var5.resetRelightChecks();
            }
        }
    }

    public void handleChangeGameState(S2BPacketChangeGameState p_147252_1_)
    {
        EntityClientPlayerMP var2 = this.gameController.thePlayer;
        int var3 = p_147252_1_.func_149138_c();
        float var4 = p_147252_1_.func_149137_d();
        int var5 = MathHelper.floor_float(var4 + 0.5F);

        if (var3 >= 0 && var3 < S2BPacketChangeGameState.field_149142_a.length && S2BPacketChangeGameState.field_149142_a[var3] != null)
        {
            var2.addChatComponentMessage(new ChatComponentTranslation(S2BPacketChangeGameState.field_149142_a[var3], new Object[0]));
        }

        if (var3 == 1)
        {
            this.clientWorldController.getWorldInfo().setRaining(true);
            this.clientWorldController.setRainStrength(0.0F);
        }
        else if (var3 == 2)
        {
            this.clientWorldController.getWorldInfo().setRaining(false);
            this.clientWorldController.setRainStrength(1.0F);
        }
        else if (var3 == 3)
        {
            this.gameController.playerController.setGameType(WorldSettings.GameType.getByID(var5));
        }
        else if (var3 == 4)
        {
            this.gameController.displayGuiScreen(new GuiWinGame());
        }
        else if (var3 == 5)
        {
            GameSettings var6 = this.gameController.gameSettings;

            if (var4 == 0.0F)
            {
                this.gameController.displayGuiScreen(new GuiScreenDemo());
            }
            else if (var4 == 101.0F)
            {
                this.gameController.ingameGUI.getChatGUI().func_146227_a(new ChatComponentTranslation("demo.help.movement", new Object[] {GameSettings.getKeyDisplayString(var6.keyBindForward.getKeyCode()), GameSettings.getKeyDisplayString(var6.keyBindLeft.getKeyCode()), GameSettings.getKeyDisplayString(var6.keyBindBack.getKeyCode()), GameSettings.getKeyDisplayString(var6.keyBindRight.getKeyCode())}));
            }
            else if (var4 == 102.0F)
            {
                this.gameController.ingameGUI.getChatGUI().func_146227_a(new ChatComponentTranslation("demo.help.jump", new Object[] {GameSettings.getKeyDisplayString(var6.keyBindJump.getKeyCode())}));
            }
            else if (var4 == 103.0F)
            {
                this.gameController.ingameGUI.getChatGUI().func_146227_a(new ChatComponentTranslation("demo.help.inventory", new Object[] {GameSettings.getKeyDisplayString(var6.keyBindInventory.getKeyCode())}));
            }
        }
        else if (var3 == 6)
        {
            this.clientWorldController.playSound(var2.posX, var2.posY + (double)var2.getEyeHeight(), var2.posZ, "random.successful_hit", 0.18F, 0.45F, false);
        }
        else if (var3 == 7)
        {
            this.clientWorldController.setRainStrength(var4);
        }
        else if (var3 == 8)
        {
            this.clientWorldController.setThunderStrength(var4);
        }
    }

    /**
     * Updates the worlds MapStorage with the specified MapData for the specified map-identifier and invokes a
     * MapItemRenderer for it
     */
    public void handleMaps(S34PacketMaps p_147264_1_)
    {
        MapData var2 = ItemMap.func_150912_a(p_147264_1_.func_149188_c(), this.gameController.theWorld);
        var2.updateMPMapData(p_147264_1_.func_149187_d());
        this.gameController.entityRenderer.getMapItemRenderer().func_148246_a(var2);
    }

    public void handleEffect(S28PacketEffect p_147277_1_)
    {
        if (p_147277_1_.func_149244_c())
        {
            this.gameController.theWorld.playBroadcastSound(p_147277_1_.func_149242_d(), p_147277_1_.func_149240_f(), p_147277_1_.func_149243_g(), p_147277_1_.func_149239_h(), p_147277_1_.func_149241_e());
        }
        else
        {
            this.gameController.theWorld.playAuxSFX(p_147277_1_.func_149242_d(), p_147277_1_.func_149240_f(), p_147277_1_.func_149243_g(), p_147277_1_.func_149239_h(), p_147277_1_.func_149241_e());
        }
    }

    /**
     * Updates the players statistics or achievements
     */
    public void handleStatistics(S37PacketStatistics p_147293_1_)
    {
        boolean var2 = false;
        StatBase var5;
        int var6;

        for (Iterator var3 = p_147293_1_.func_148974_c().entrySet().iterator(); var3.hasNext(); this.gameController.thePlayer.func_146107_m().func_150873_a(this.gameController.thePlayer, var5, var6))
        {
            Entry var4 = (Entry)var3.next();
            var5 = (StatBase)var4.getKey();
            var6 = ((Integer)var4.getValue()).intValue();

            if (var5.isAchievement() && var6 > 0)
            {
                if (this.field_147308_k && this.gameController.thePlayer.func_146107_m().writeStat(var5) == 0)
                {
                    this.gameController.guiAchievement.func_146256_a((Achievement)var5);

                    if (var5 == AchievementList.openInventory)
                    {
                        this.gameController.gameSettings.showInventoryAchievementHint = false;
                        this.gameController.gameSettings.saveOptions();
                    }
                }

                var2 = true;
            }
        }

        if (!this.field_147308_k && !var2 && this.gameController.gameSettings.showInventoryAchievementHint)
        {
            this.gameController.guiAchievement.func_146255_b(AchievementList.openInventory);
        }

        this.field_147308_k = true;

        if (this.gameController.currentScreen instanceof IProgressMeter)
        {
            ((IProgressMeter)this.gameController.currentScreen).func_146509_g();
        }
    }

    public void handleEntityEffect(S1DPacketEntityEffect p_147260_1_)
    {
        Entity var2 = this.clientWorldController.getEntityByID(p_147260_1_.func_149426_d());

        if (var2 instanceof EntityLivingBase)
        {
            PotionEffect var3 = new PotionEffect(p_147260_1_.func_149427_e(), p_147260_1_.func_149425_g(), p_147260_1_.func_149428_f());
            var3.setPotionDurationMax(p_147260_1_.func_149429_c());
            ((EntityLivingBase)var2).addPotionEffect(var3);
        }
    }

    public void handleRemoveEntityEffect(S1EPacketRemoveEntityEffect p_147262_1_)
    {
        Entity var2 = this.clientWorldController.getEntityByID(p_147262_1_.func_149076_c());

        if (var2 instanceof EntityLivingBase)
        {
            ((EntityLivingBase)var2).removePotionEffectClient(p_147262_1_.func_149075_d());
        }
    }

    public void handlePlayerListItem(S38PacketPlayerListItem p_147256_1_)
    {
        GuiPlayerInfo var2 = (GuiPlayerInfo)this.playerInfoMap.get(p_147256_1_.func_149122_c());

        if (var2 == null && p_147256_1_.func_149121_d())
        {
            var2 = new GuiPlayerInfo(p_147256_1_.func_149122_c());
            this.playerInfoMap.put(p_147256_1_.func_149122_c(), var2);
            this.playerInfoList.add(var2);
        }

        if (var2 != null && !p_147256_1_.func_149121_d())
        {
            this.playerInfoMap.remove(p_147256_1_.func_149122_c());
            this.playerInfoList.remove(var2);
        }

        if (var2 != null && p_147256_1_.func_149121_d())
        {
            var2.responseTime = p_147256_1_.func_149120_e();
        }
    }

    public void handleKeepAlive(S00PacketKeepAlive p_147272_1_)
    {
        this.addToSendQueue(new C00PacketKeepAlive(p_147272_1_.func_149134_c()));
    }

    /**
     * Allows validation of the connection state transition. Parameters: from, to (connection state). Typically throws
     * IllegalStateException or UnsupportedOperationException if validation fails
     */
    public void onConnectionStateTransition(EnumConnectionState p_147232_1_, EnumConnectionState p_147232_2_)
    {
        throw new IllegalStateException("Unexpected protocol change!");
    }

    public void handlePlayerAbilities(S39PacketPlayerAbilities p_147270_1_)
    {
        EntityClientPlayerMP var2 = this.gameController.thePlayer;
        var2.capabilities.isFlying = p_147270_1_.func_149106_d();
        var2.capabilities.isCreativeMode = p_147270_1_.func_149103_f();
        var2.capabilities.disableDamage = p_147270_1_.func_149112_c();
        var2.capabilities.allowFlying = p_147270_1_.func_149105_e();
        var2.capabilities.setFlySpeed(p_147270_1_.func_149101_g());
        var2.capabilities.setPlayerWalkSpeed(p_147270_1_.func_149107_h());
    }

    /**
     * Displays the available command-completion options the server knows of
     */
    public void handleTabComplete(S3APacketTabComplete p_147274_1_)
    {
        String[] var2 = p_147274_1_.func_149630_c();

        if (this.gameController.currentScreen instanceof GuiChat)
        {
            GuiChat var3 = (GuiChat)this.gameController.currentScreen;
            var3.func_146406_a(var2);
        }
    }

    public void handleSoundEffect(S29PacketSoundEffect p_147255_1_)
    {
        this.gameController.theWorld.playSound(p_147255_1_.func_149207_d(), p_147255_1_.func_149211_e(), p_147255_1_.func_149210_f(), p_147255_1_.func_149212_c(), p_147255_1_.func_149208_g(), p_147255_1_.func_149209_h(), false);
    }

    /**
     * Handles packets that have room for a channel specification. Vanilla implemented channels are "MC|TrList" to
     * acquire a MerchantRecipeList trades for a villager merchant, "MC|Brand" which sets the server brand? on the
     * player instance and finally "MC|RPack" which the server uses to communicate the identifier of the default server
     * resourcepack for the client to load.
     */
    public void handleCustomPayload(S3FPacketCustomPayload p_147240_1_)
    {
        if ("MC|TrList".equals(p_147240_1_.func_149169_c()))
        {
            ByteBuf var2 = Unpooled.wrappedBuffer(p_147240_1_.func_149168_d());

            try
            {
                int var3 = var2.readInt();
                GuiScreen var4 = this.gameController.currentScreen;

                if (var4 != null && var4 instanceof GuiMerchant && var3 == this.gameController.thePlayer.openContainer.windowId)
                {
                    IMerchant var5 = ((GuiMerchant)var4).func_147035_g();
                    MerchantRecipeList var6 = MerchantRecipeList.func_151390_b(new PacketBuffer(var2));
                    var5.setRecipes(var6);
                }
            }
            catch (IOException var7)
            {
                logger.error("Couldn\'t load trade info", var7);
            }
        }
        else if ("MC|Brand".equals(p_147240_1_.func_149169_c()))
        {
            this.gameController.thePlayer.func_142020_c(new String(p_147240_1_.func_149168_d(), Charsets.UTF_8));
        }
        else if ("MC|RPack".equals(p_147240_1_.func_149169_c()))
        {
            final String var8 = new String(p_147240_1_.func_149168_d(), Charsets.UTF_8);

            if (this.gameController.gameSettings.serverTextures)
            {
                if (this.gameController.func_147104_D() != null && this.gameController.func_147104_D().func_147408_b())
                {
                    this.gameController.getResourcePackRepository().func_148526_a(var8);
                }
                else if (this.gameController.func_147104_D() == null || this.gameController.func_147104_D().func_147410_c())
                {
                    this.gameController.displayGuiScreen(new GuiYesNo(new GuiScreen()
                    {
                        private static final String __OBFID = "CL_00000879";
                        public void confirmClicked(boolean par1, int par2)
                        {
                            this.mc = Minecraft.getMinecraft();

                            if (this.mc.func_147104_D() != null)
                            {
                                this.mc.func_147104_D().setAcceptsTextures(par1);
                                ServerList.func_147414_b(this.mc.func_147104_D());
                            }

                            if (par1)
                            {
                                this.mc.getResourcePackRepository().func_148526_a(var8);
                            }

                            this.mc.displayGuiScreen((GuiScreen)null);
                        }
                    }, I18n.format("multiplayer.texturePrompt.line1", new Object[0]), I18n.format("multiplayer.texturePrompt.line2", new Object[0]), 0));
                }
            }
        }
    }

    /**
     * May create a scoreboard objective, remove an objective from the scoreboard or update an objectives' displayname
     */
    public void handleScoreboardObjective(S3BPacketScoreboardObjective p_147291_1_)
    {
        Scoreboard var2 = this.clientWorldController.getScoreboard();
        ScoreObjective var3;

        if (p_147291_1_.func_149338_e() == 0)
        {
            var3 = var2.addScoreObjective(p_147291_1_.func_149339_c(), IScoreObjectiveCriteria.field_96641_b);
            var3.setDisplayName(p_147291_1_.func_149337_d());
        }
        else
        {
            var3 = var2.getObjective(p_147291_1_.func_149339_c());

            if (p_147291_1_.func_149338_e() == 1)
            {
                var2.func_96519_k(var3);
            }
            else if (p_147291_1_.func_149338_e() == 2)
            {
                var3.setDisplayName(p_147291_1_.func_149337_d());
            }
        }
    }

    /**
     * Either updates the score with a specified value or removes the score for an objective
     */
    public void handleUpdateScore(S3CPacketUpdateScore p_147250_1_)
    {
        Scoreboard var2 = this.clientWorldController.getScoreboard();
        ScoreObjective var3 = var2.getObjective(p_147250_1_.func_149321_d());

        if (p_147250_1_.func_149322_f() == 0)
        {
            Score var4 = var2.func_96529_a(p_147250_1_.func_149324_c(), var3);
            var4.func_96647_c(p_147250_1_.func_149323_e());
        }
        else if (p_147250_1_.func_149322_f() == 1)
        {
            var2.func_96515_c(p_147250_1_.func_149324_c());
        }
    }

    /**
     * Removes or sets the ScoreObjective to be displayed at a particular scoreboard position (list, sidebar, below
     * name)
     */
    public void handleDisplayScoreboard(S3DPacketDisplayScoreboard p_147254_1_)
    {
        Scoreboard var2 = this.clientWorldController.getScoreboard();

        if (p_147254_1_.func_149370_d().length() == 0)
        {
            var2.func_96530_a(p_147254_1_.func_149371_c(), (ScoreObjective)null);
        }
        else
        {
            ScoreObjective var3 = var2.getObjective(p_147254_1_.func_149370_d());
            var2.func_96530_a(p_147254_1_.func_149371_c(), var3);
        }
    }

    /**
     * Updates a team managed by the scoreboard: Create/Remove the team registration, Register/Remove the player-team-
     * memberships, Set team displayname/prefix/suffix and/or whether friendly fire is enabled
     */
    public void handleTeams(S3EPacketTeams p_147247_1_)
    {
        Scoreboard var2 = this.clientWorldController.getScoreboard();
        ScorePlayerTeam var3;

        if (p_147247_1_.func_149307_h() == 0)
        {
            var3 = var2.createTeam(p_147247_1_.func_149312_c());
        }
        else
        {
            var3 = var2.getTeam(p_147247_1_.func_149312_c());
        }

        if (p_147247_1_.func_149307_h() == 0 || p_147247_1_.func_149307_h() == 2)
        {
            var3.setTeamName(p_147247_1_.func_149306_d());
            var3.setNamePrefix(p_147247_1_.func_149311_e());
            var3.setNameSuffix(p_147247_1_.func_149309_f());
            var3.func_98298_a(p_147247_1_.func_149308_i());
        }

        Iterator var4;
        String var5;

        if (p_147247_1_.func_149307_h() == 0 || p_147247_1_.func_149307_h() == 3)
        {
            var4 = p_147247_1_.func_149310_g().iterator();

            while (var4.hasNext())
            {
                var5 = (String)var4.next();
                var2.func_151392_a(var5, p_147247_1_.func_149312_c());
            }
        }

        if (p_147247_1_.func_149307_h() == 4)
        {
            var4 = p_147247_1_.func_149310_g().iterator();

            while (var4.hasNext())
            {
                var5 = (String)var4.next();
                var2.removePlayerFromTeam(var5, var3);
            }
        }

        if (p_147247_1_.func_149307_h() == 1)
        {
            var2.removeTeam(var3);
        }
    }

    /**
     * Spawns a specified number of particles at the specified location with a randomized displacement according to
     * specified bounds
     */
    public void handleParticles(S2APacketParticles p_147289_1_)
    {
        if (p_147289_1_.func_149222_k() == 0)
        {
            double var2 = (double)(p_147289_1_.func_149227_j() * p_147289_1_.func_149221_g());
            double var4 = (double)(p_147289_1_.func_149227_j() * p_147289_1_.func_149224_h());
            double var6 = (double)(p_147289_1_.func_149227_j() * p_147289_1_.func_149223_i());
            this.clientWorldController.spawnParticle(p_147289_1_.func_149228_c(), p_147289_1_.func_149220_d(), p_147289_1_.func_149226_e(), p_147289_1_.func_149225_f(), var2, var4, var6);
        }
        else
        {
            for (int var15 = 0; var15 < p_147289_1_.func_149222_k(); ++var15)
            {
                double var3 = this.avRandomizer.nextGaussian() * (double)p_147289_1_.func_149221_g();
                double var5 = this.avRandomizer.nextGaussian() * (double)p_147289_1_.func_149224_h();
                double var7 = this.avRandomizer.nextGaussian() * (double)p_147289_1_.func_149223_i();
                double var9 = this.avRandomizer.nextGaussian() * (double)p_147289_1_.func_149227_j();
                double var11 = this.avRandomizer.nextGaussian() * (double)p_147289_1_.func_149227_j();
                double var13 = this.avRandomizer.nextGaussian() * (double)p_147289_1_.func_149227_j();
                this.clientWorldController.spawnParticle(p_147289_1_.func_149228_c(), p_147289_1_.func_149220_d() + var3, p_147289_1_.func_149226_e() + var5, p_147289_1_.func_149225_f() + var7, var9, var11, var13);
            }
        }
    }

    /**
     * Updates en entity's attributes and their respective modifiers, which are used for speed bonusses (player
     * sprinting, animals fleeing, baby speed), weapon/tool attackDamage, hostiles followRange randomization, zombie
     * maxHealth and knockback resistance as well as reinforcement spawning chance.
     */
    public void handleEntityProperties(S20PacketEntityProperties p_147290_1_)
    {
        Entity var2 = this.clientWorldController.getEntityByID(p_147290_1_.func_149442_c());

        if (var2 != null)
        {
            if (!(var2 instanceof EntityLivingBase))
            {
                throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + var2 + ")");
            }
            else
            {
                BaseAttributeMap var3 = ((EntityLivingBase)var2).getAttributeMap();
                Iterator var4 = p_147290_1_.func_149441_d().iterator();

                while (var4.hasNext())
                {
                    S20PacketEntityProperties.Snapshot var5 = (S20PacketEntityProperties.Snapshot)var4.next();
                    IAttributeInstance var6 = var3.getAttributeInstanceByName(var5.func_151409_a());

                    if (var6 == null)
                    {
                        var6 = var3.registerAttribute(new RangedAttribute(var5.func_151409_a(), 0.0D, 2.2250738585072014E-308D, Double.MAX_VALUE));
                    }

                    var6.setBaseValue(var5.func_151410_b());
                    var6.removeAllModifiers();
                    Iterator var7 = var5.func_151408_c().iterator();

                    while (var7.hasNext())
                    {
                        AttributeModifier var8 = (AttributeModifier)var7.next();
                        var6.applyModifier(var8);
                    }
                }
            }
        }
    }

    /**
     * Returns this the NetworkManager instance registered with this NetworkHandlerPlayClient
     */
    public NetworkManager getNetworkManager()
    {
        return this.netManager;
    }
}
