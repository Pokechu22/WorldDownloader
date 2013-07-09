package net.minecraft.src;

import com.google.common.base.Charsets;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.crypto.SecretKey;
import net.minecraft.client.ClientBrandRetriever;
import org.lwjgl.input.Keyboard;

public class NetClientHandler extends NetHandler
{
    /** True if kicked or disconnected from the server. */
    private boolean disconnected;

    /** Reference to the NetworkManager object. */
    private INetworkManager netManager;
    public String field_72560_a;

    /** Reference to the Minecraft object. */
    private Minecraft mc;
    private WorldClient worldClient;

    /**
     * True if the client has finished downloading terrain and may spawn. Set upon receipt of a player position packet,
     * reset upon respawning.
     */
    private boolean doneLoadingTerrain;
    public MapStorage mapStorage = new MapStorage((ISaveHandler)null);

    /** A HashMap of all player names and their player information objects */
    private Map playerInfoMap = new HashMap();

    /**
     * An ArrayList of GuiPlayerInfo (includes all the players' GuiPlayerInfo on the current server)
     */
    public List playerInfoList = new ArrayList();
    public int currentServerMaxPlayers = 20;
    private GuiScreen field_98183_l;

    /** RNG. */
    Random rand = new Random();

    public NetClientHandler(Minecraft par1Minecraft, String par2Str, int par3) throws IOException
    {
        this.mc = par1Minecraft;
        Socket var4 = new Socket(InetAddress.getByName(par2Str), par3);
        this.netManager = new TcpConnection(par1Minecraft.getLogAgent(), var4, "Client", this);
    }

    public NetClientHandler(Minecraft par1Minecraft, String par2Str, int par3, GuiScreen par4GuiScreen) throws IOException
    {
        this.mc = par1Minecraft;
        this.field_98183_l = par4GuiScreen;
        Socket var5 = new Socket(InetAddress.getByName(par2Str), par3);
        this.netManager = new TcpConnection(par1Minecraft.getLogAgent(), var5, "Client", this);
    }

    public NetClientHandler(Minecraft par1Minecraft, IntegratedServer par2IntegratedServer) throws IOException
    {
        this.mc = par1Minecraft;
        this.netManager = new MemoryConnection(par1Minecraft.getLogAgent(), this);
        par2IntegratedServer.getServerListeningThread().func_71754_a((MemoryConnection)this.netManager, par1Minecraft.func_110432_I().func_111285_a());
    }

    /**
     * sets netManager and worldClient to null
     */
    public void cleanup()
    {
        if (this.netManager != null)
        {
            this.netManager.wakeThreads();
        }

        this.netManager = null;
        this.worldClient = null;
    }

    /**
     * Processes the packets that have been read since the last call to this function.
     */
    public void processReadPackets()
    {
        if (!this.disconnected && this.netManager != null)
        {
            this.netManager.processReadPackets();
        }

        if (this.netManager != null)
        {
            this.netManager.wakeThreads();
        }
    }

    public void handleServerAuthData(Packet253ServerAuthData par1Packet253ServerAuthData)
    {
        String var2 = par1Packet253ServerAuthData.getServerId().trim();
        PublicKey var3 = par1Packet253ServerAuthData.getPublicKey();
        SecretKey var4 = CryptManager.createNewSharedKey();

        if (!"-".equals(var2))
        {
            String var5 = (new BigInteger(CryptManager.getServerIdHash(var2, var3, var4))).toString(16);
            String var6 = this.sendSessionRequest(this.mc.func_110432_I().func_111285_a(), this.mc.func_110432_I().func_111286_b(), var5);

            if (!"ok".equalsIgnoreCase(var6))
            {
                this.netManager.networkShutdown("disconnect.loginFailedInfo", new Object[] {var6});
                return;
            }
        }

        this.addToSendQueue(new Packet252SharedKey(var4, var3, par1Packet253ServerAuthData.getVerifyToken()));
    }

    /**
     * Send request to http://session.minecraft.net with user's sessionId and serverId hash
     */
    private String sendSessionRequest(String par1Str, String par2Str, String par3Str)
    {
        try
        {
            URL var4 = new URL("http://session.minecraft.net/game/joinserver.jsp?user=" + urlEncode(par1Str) + "&sessionId=" + urlEncode(par2Str) + "&serverId=" + urlEncode(par3Str));
            InputStream var5 = var4.openConnection(this.mc.func_110437_J()).getInputStream();
            BufferedReader var6 = new BufferedReader(new InputStreamReader(var5));
            String var7 = var6.readLine();
            var6.close();
            return var7;
        }
        catch (IOException var8)
        {
            return var8.toString();
        }
    }

    /**
     * Encode the given string for insertion into a URL
     */
    private static String urlEncode(String par0Str) throws IOException
    {
        return URLEncoder.encode(par0Str, "UTF-8");
    }

    public void handleSharedKey(Packet252SharedKey par1Packet252SharedKey)
    {
        this.addToSendQueue(new Packet205ClientCommand(0));
    }

    public void handleLogin(Packet1Login par1Packet1Login)
    {
        this.mc.playerController = new PlayerControllerMP(this.mc, this);
        this.mc.statFileWriter.readStat(StatList.joinMultiplayerStat, 1);
        this.worldClient = new WorldClient(this, new WorldSettings(0L, par1Packet1Login.gameType, false, par1Packet1Login.hardcoreMode, par1Packet1Login.terrainType), par1Packet1Login.dimension, par1Packet1Login.difficultySetting, this.mc.mcProfiler, this.mc.getLogAgent());
        this.worldClient.isRemote = true;
        this.mc.loadWorld(this.worldClient);
        this.mc.thePlayer.dimension = par1Packet1Login.dimension;
        this.mc.displayGuiScreen(new GuiDownloadTerrain(this));
        this.mc.thePlayer.entityId = par1Packet1Login.clientEntityId;
        this.currentServerMaxPlayers = par1Packet1Login.maxPlayers;
        this.mc.playerController.setGameType(par1Packet1Login.gameType);
        this.mc.gameSettings.sendSettingsToServer();
        this.netManager.addToSendQueue(new Packet250CustomPayload("MC|Brand", ClientBrandRetriever.getClientModName().getBytes(Charsets.UTF_8)));
    }

    public void handleVehicleSpawn(Packet23VehicleSpawn par1Packet23VehicleSpawn)
    {
        double var2 = (double)par1Packet23VehicleSpawn.xPosition / 32.0D;
        double var4 = (double)par1Packet23VehicleSpawn.yPosition / 32.0D;
        double var6 = (double)par1Packet23VehicleSpawn.zPosition / 32.0D;
        Object var8 = null;

        if (par1Packet23VehicleSpawn.type == 10)
        {
            var8 = EntityMinecart.createMinecart(this.worldClient, var2, var4, var6, par1Packet23VehicleSpawn.throwerEntityId);
        }
        else if (par1Packet23VehicleSpawn.type == 90)
        {
            Entity var9 = this.getEntityByID(par1Packet23VehicleSpawn.throwerEntityId);

            if (var9 instanceof EntityPlayer)
            {
                var8 = new EntityFishHook(this.worldClient, var2, var4, var6, (EntityPlayer)var9);
            }

            par1Packet23VehicleSpawn.throwerEntityId = 0;
        }
        else if (par1Packet23VehicleSpawn.type == 60)
        {
            var8 = new EntityArrow(this.worldClient, var2, var4, var6);
        }
        else if (par1Packet23VehicleSpawn.type == 61)
        {
            var8 = new EntitySnowball(this.worldClient, var2, var4, var6);
        }
        else if (par1Packet23VehicleSpawn.type == 71)
        {
            var8 = new EntityItemFrame(this.worldClient, (int)var2, (int)var4, (int)var6, par1Packet23VehicleSpawn.throwerEntityId);
            par1Packet23VehicleSpawn.throwerEntityId = 0;
        }
        else if (par1Packet23VehicleSpawn.type == 77)
        {
            var8 = new EntityLeashKnot(this.worldClient, (int)var2, (int)var4, (int)var6);
            par1Packet23VehicleSpawn.throwerEntityId = 0;
        }
        else if (par1Packet23VehicleSpawn.type == 65)
        {
            var8 = new EntityEnderPearl(this.worldClient, var2, var4, var6);
        }
        else if (par1Packet23VehicleSpawn.type == 72)
        {
            var8 = new EntityEnderEye(this.worldClient, var2, var4, var6);
        }
        else if (par1Packet23VehicleSpawn.type == 76)
        {
            var8 = new EntityFireworkRocket(this.worldClient, var2, var4, var6, (ItemStack)null);
        }
        else if (par1Packet23VehicleSpawn.type == 63)
        {
            var8 = new EntityLargeFireball(this.worldClient, var2, var4, var6, (double)par1Packet23VehicleSpawn.speedX / 8000.0D, (double)par1Packet23VehicleSpawn.speedY / 8000.0D, (double)par1Packet23VehicleSpawn.speedZ / 8000.0D);
            par1Packet23VehicleSpawn.throwerEntityId = 0;
        }
        else if (par1Packet23VehicleSpawn.type == 64)
        {
            var8 = new EntitySmallFireball(this.worldClient, var2, var4, var6, (double)par1Packet23VehicleSpawn.speedX / 8000.0D, (double)par1Packet23VehicleSpawn.speedY / 8000.0D, (double)par1Packet23VehicleSpawn.speedZ / 8000.0D);
            par1Packet23VehicleSpawn.throwerEntityId = 0;
        }
        else if (par1Packet23VehicleSpawn.type == 66)
        {
            var8 = new EntityWitherSkull(this.worldClient, var2, var4, var6, (double)par1Packet23VehicleSpawn.speedX / 8000.0D, (double)par1Packet23VehicleSpawn.speedY / 8000.0D, (double)par1Packet23VehicleSpawn.speedZ / 8000.0D);
            par1Packet23VehicleSpawn.throwerEntityId = 0;
        }
        else if (par1Packet23VehicleSpawn.type == 62)
        {
            var8 = new EntityEgg(this.worldClient, var2, var4, var6);
        }
        else if (par1Packet23VehicleSpawn.type == 73)
        {
            var8 = new EntityPotion(this.worldClient, var2, var4, var6, par1Packet23VehicleSpawn.throwerEntityId);
            par1Packet23VehicleSpawn.throwerEntityId = 0;
        }
        else if (par1Packet23VehicleSpawn.type == 75)
        {
            var8 = new EntityExpBottle(this.worldClient, var2, var4, var6);
            par1Packet23VehicleSpawn.throwerEntityId = 0;
        }
        else if (par1Packet23VehicleSpawn.type == 1)
        {
            var8 = new EntityBoat(this.worldClient, var2, var4, var6);
        }
        else if (par1Packet23VehicleSpawn.type == 50)
        {
            var8 = new EntityTNTPrimed(this.worldClient, var2, var4, var6, (EntityLivingBase)null);
        }
        else if (par1Packet23VehicleSpawn.type == 51)
        {
            var8 = new EntityEnderCrystal(this.worldClient, var2, var4, var6);
        }
        else if (par1Packet23VehicleSpawn.type == 2)
        {
            var8 = new EntityItem(this.worldClient, var2, var4, var6);
        }
        else if (par1Packet23VehicleSpawn.type == 70)
        {
            var8 = new EntityFallingSand(this.worldClient, var2, var4, var6, par1Packet23VehicleSpawn.throwerEntityId & 65535, par1Packet23VehicleSpawn.throwerEntityId >> 16);
            par1Packet23VehicleSpawn.throwerEntityId = 0;
        }

        if (var8 != null)
        {
            ((Entity)var8).serverPosX = par1Packet23VehicleSpawn.xPosition;
            ((Entity)var8).serverPosY = par1Packet23VehicleSpawn.yPosition;
            ((Entity)var8).serverPosZ = par1Packet23VehicleSpawn.zPosition;
            ((Entity)var8).rotationPitch = (float)(par1Packet23VehicleSpawn.pitch * 360) / 256.0F;
            ((Entity)var8).rotationYaw = (float)(par1Packet23VehicleSpawn.yaw * 360) / 256.0F;
            Entity[] var12 = ((Entity)var8).getParts();

            if (var12 != null)
            {
                int var10 = par1Packet23VehicleSpawn.entityId - ((Entity)var8).entityId;

                for (int var11 = 0; var11 < var12.length; ++var11)
                {
                    var12[var11].entityId += var10;
                }
            }

            ((Entity)var8).entityId = par1Packet23VehicleSpawn.entityId;
            this.worldClient.addEntityToWorld(par1Packet23VehicleSpawn.entityId, (Entity)var8);

            if (par1Packet23VehicleSpawn.throwerEntityId > 0)
            {
                if (par1Packet23VehicleSpawn.type == 60)
                {
                    Entity var13 = this.getEntityByID(par1Packet23VehicleSpawn.throwerEntityId);

                    if (var13 instanceof EntityLivingBase)
                    {
                        EntityArrow var14 = (EntityArrow)var8;
                        var14.shootingEntity = var13;
                    }
                }

                ((Entity)var8).setVelocity((double)par1Packet23VehicleSpawn.speedX / 8000.0D, (double)par1Packet23VehicleSpawn.speedY / 8000.0D, (double)par1Packet23VehicleSpawn.speedZ / 8000.0D);
            }
        }
    }

    /**
     * Handle a entity experience orb packet.
     */
    public void handleEntityExpOrb(Packet26EntityExpOrb par1Packet26EntityExpOrb)
    {
        EntityXPOrb var2 = new EntityXPOrb(this.worldClient, (double)par1Packet26EntityExpOrb.posX, (double)par1Packet26EntityExpOrb.posY, (double)par1Packet26EntityExpOrb.posZ, par1Packet26EntityExpOrb.xpValue);
        var2.serverPosX = par1Packet26EntityExpOrb.posX;
        var2.serverPosY = par1Packet26EntityExpOrb.posY;
        var2.serverPosZ = par1Packet26EntityExpOrb.posZ;
        var2.rotationYaw = 0.0F;
        var2.rotationPitch = 0.0F;
        var2.entityId = par1Packet26EntityExpOrb.entityId;
        this.worldClient.addEntityToWorld(par1Packet26EntityExpOrb.entityId, var2);
    }

    /**
     * Handles weather packet
     */
    public void handleWeather(Packet71Weather par1Packet71Weather)
    {
        double var2 = (double)par1Packet71Weather.posX / 32.0D;
        double var4 = (double)par1Packet71Weather.posY / 32.0D;
        double var6 = (double)par1Packet71Weather.posZ / 32.0D;
        EntityLightningBolt var8 = null;

        if (par1Packet71Weather.isLightningBolt == 1)
        {
            var8 = new EntityLightningBolt(this.worldClient, var2, var4, var6);
        }

        if (var8 != null)
        {
            var8.serverPosX = par1Packet71Weather.posX;
            var8.serverPosY = par1Packet71Weather.posY;
            var8.serverPosZ = par1Packet71Weather.posZ;
            var8.rotationYaw = 0.0F;
            var8.rotationPitch = 0.0F;
            var8.entityId = par1Packet71Weather.entityID;
            this.worldClient.addWeatherEffect(var8);
        }
    }

    /**
     * Packet handler
     */
    public void handleEntityPainting(Packet25EntityPainting par1Packet25EntityPainting)
    {
        EntityPainting var2 = new EntityPainting(this.worldClient, par1Packet25EntityPainting.xPosition, par1Packet25EntityPainting.yPosition, par1Packet25EntityPainting.zPosition, par1Packet25EntityPainting.direction, par1Packet25EntityPainting.title);
        this.worldClient.addEntityToWorld(par1Packet25EntityPainting.entityId, var2);
    }

    /**
     * Packet handler
     */
    public void handleEntityVelocity(Packet28EntityVelocity par1Packet28EntityVelocity)
    {
        Entity var2 = this.getEntityByID(par1Packet28EntityVelocity.entityId);

        if (var2 != null)
        {
            var2.setVelocity((double)par1Packet28EntityVelocity.motionX / 8000.0D, (double)par1Packet28EntityVelocity.motionY / 8000.0D, (double)par1Packet28EntityVelocity.motionZ / 8000.0D);
        }
    }

    /**
     * Packet handler
     */
    public void handleEntityMetadata(Packet40EntityMetadata par1Packet40EntityMetadata)
    {
        Entity var2 = this.getEntityByID(par1Packet40EntityMetadata.entityId);

        if (var2 != null && par1Packet40EntityMetadata.getMetadata() != null)
        {
            var2.getDataWatcher().updateWatchedObjectsFromList(par1Packet40EntityMetadata.getMetadata());
        }
    }

    public void handleNamedEntitySpawn(Packet20NamedEntitySpawn par1Packet20NamedEntitySpawn)
    {
        double var2 = (double)par1Packet20NamedEntitySpawn.xPosition / 32.0D;
        double var4 = (double)par1Packet20NamedEntitySpawn.yPosition / 32.0D;
        double var6 = (double)par1Packet20NamedEntitySpawn.zPosition / 32.0D;
        float var8 = (float)(par1Packet20NamedEntitySpawn.rotation * 360) / 256.0F;
        float var9 = (float)(par1Packet20NamedEntitySpawn.pitch * 360) / 256.0F;
        EntityOtherPlayerMP var10 = new EntityOtherPlayerMP(this.mc.theWorld, par1Packet20NamedEntitySpawn.name);
        var10.prevPosX = var10.lastTickPosX = (double)(var10.serverPosX = par1Packet20NamedEntitySpawn.xPosition);
        var10.prevPosY = var10.lastTickPosY = (double)(var10.serverPosY = par1Packet20NamedEntitySpawn.yPosition);
        var10.prevPosZ = var10.lastTickPosZ = (double)(var10.serverPosZ = par1Packet20NamedEntitySpawn.zPosition);
        int var11 = par1Packet20NamedEntitySpawn.currentItem;

        if (var11 == 0)
        {
            var10.inventory.mainInventory[var10.inventory.currentItem] = null;
        }
        else
        {
            var10.inventory.mainInventory[var10.inventory.currentItem] = new ItemStack(var11, 1, 0);
        }

        var10.setPositionAndRotation(var2, var4, var6, var8, var9);
        this.worldClient.addEntityToWorld(par1Packet20NamedEntitySpawn.entityId, var10);
        List var12 = par1Packet20NamedEntitySpawn.getWatchedMetadata();

        if (var12 != null)
        {
            var10.getDataWatcher().updateWatchedObjectsFromList(var12);
        }
    }

    public void handleEntityTeleport(Packet34EntityTeleport par1Packet34EntityTeleport)
    {
        Entity var2 = this.getEntityByID(par1Packet34EntityTeleport.entityId);

        if (var2 != null)
        {
            var2.serverPosX = par1Packet34EntityTeleport.xPosition;
            var2.serverPosY = par1Packet34EntityTeleport.yPosition;
            var2.serverPosZ = par1Packet34EntityTeleport.zPosition;
            double var3 = (double)var2.serverPosX / 32.0D;
            double var5 = (double)var2.serverPosY / 32.0D + 0.015625D;
            double var7 = (double)var2.serverPosZ / 32.0D;
            float var9 = (float)(par1Packet34EntityTeleport.yaw * 360) / 256.0F;
            float var10 = (float)(par1Packet34EntityTeleport.pitch * 360) / 256.0F;
            var2.setPositionAndRotation2(var3, var5, var7, var9, var10, 3);
        }
    }

    public void handleBlockItemSwitch(Packet16BlockItemSwitch par1Packet16BlockItemSwitch)
    {
        if (par1Packet16BlockItemSwitch.id >= 0 && par1Packet16BlockItemSwitch.id < InventoryPlayer.getHotbarSize())
        {
            this.mc.thePlayer.inventory.currentItem = par1Packet16BlockItemSwitch.id;
        }
    }

    public void handleEntity(Packet30Entity par1Packet30Entity)
    {
        Entity var2 = this.getEntityByID(par1Packet30Entity.entityId);

        if (var2 != null)
        {
            var2.serverPosX += par1Packet30Entity.xPosition;
            var2.serverPosY += par1Packet30Entity.yPosition;
            var2.serverPosZ += par1Packet30Entity.zPosition;
            double var3 = (double)var2.serverPosX / 32.0D;
            double var5 = (double)var2.serverPosY / 32.0D;
            double var7 = (double)var2.serverPosZ / 32.0D;
            float var9 = par1Packet30Entity.rotating ? (float)(par1Packet30Entity.yaw * 360) / 256.0F : var2.rotationYaw;
            float var10 = par1Packet30Entity.rotating ? (float)(par1Packet30Entity.pitch * 360) / 256.0F : var2.rotationPitch;
            var2.setPositionAndRotation2(var3, var5, var7, var9, var10, 3);
        }
    }

    public void handleEntityHeadRotation(Packet35EntityHeadRotation par1Packet35EntityHeadRotation)
    {
        Entity var2 = this.getEntityByID(par1Packet35EntityHeadRotation.entityId);

        if (var2 != null)
        {
            float var3 = (float)(par1Packet35EntityHeadRotation.headRotationYaw * 360) / 256.0F;
            var2.setRotationYawHead(var3);
        }
    }

    public void handleDestroyEntity(Packet29DestroyEntity par1Packet29DestroyEntity)
    {
        for (int var2 = 0; var2 < par1Packet29DestroyEntity.entityId.length; ++var2)
        {
            this.worldClient.removeEntityFromWorld(par1Packet29DestroyEntity.entityId[var2]);
        }
    }

    public void handleFlying(Packet10Flying par1Packet10Flying)
    {
        EntityClientPlayerMP var2 = this.mc.thePlayer;
        double var3 = var2.posX;
        double var5 = var2.posY;
        double var7 = var2.posZ;
        float var9 = var2.rotationYaw;
        float var10 = var2.rotationPitch;

        if (par1Packet10Flying.moving)
        {
            var3 = par1Packet10Flying.xPosition;
            var5 = par1Packet10Flying.yPosition;
            var7 = par1Packet10Flying.zPosition;
        }

        if (par1Packet10Flying.rotating)
        {
            var9 = par1Packet10Flying.yaw;
            var10 = par1Packet10Flying.pitch;
        }

        var2.ySize = 0.0F;
        var2.motionX = var2.motionY = var2.motionZ = 0.0D;
        var2.setPositionAndRotation(var3, var5, var7, var9, var10);
        par1Packet10Flying.xPosition = var2.posX;
        par1Packet10Flying.yPosition = var2.boundingBox.minY;
        par1Packet10Flying.zPosition = var2.posZ;
        par1Packet10Flying.stance = var2.posY;
        this.netManager.addToSendQueue(par1Packet10Flying);

        if (!this.doneLoadingTerrain)
        {
            this.mc.thePlayer.prevPosX = this.mc.thePlayer.posX;
            this.mc.thePlayer.prevPosY = this.mc.thePlayer.posY;
            this.mc.thePlayer.prevPosZ = this.mc.thePlayer.posZ;
            this.doneLoadingTerrain = true;
            this.mc.displayGuiScreen((GuiScreen)null);
        }
    }

    public void handleMultiBlockChange(Packet52MultiBlockChange par1Packet52MultiBlockChange)
    {
        int var2 = par1Packet52MultiBlockChange.xPosition * 16;
        int var3 = par1Packet52MultiBlockChange.zPosition * 16;

        if (par1Packet52MultiBlockChange.metadataArray != null)
        {
            DataInputStream var4 = new DataInputStream(new ByteArrayInputStream(par1Packet52MultiBlockChange.metadataArray));

            try
            {
                for (int var5 = 0; var5 < par1Packet52MultiBlockChange.size; ++var5)
                {
                    short var6 = var4.readShort();
                    short var7 = var4.readShort();
                    int var8 = var7 >> 4 & 4095;
                    int var9 = var7 & 15;
                    int var10 = var6 >> 12 & 15;
                    int var11 = var6 >> 8 & 15;
                    int var12 = var6 & 255;
                    this.worldClient.setBlockAndMetadataAndInvalidate(var10 + var2, var12, var11 + var3, var8, var9);
                }
            }
            catch (IOException var13)
            {
                ;
            }
        }
    }

    /**
     * Handle Packet51MapChunk (full chunk update of blocks, metadata, light levels, and optionally biome data)
     */
    public void handleMapChunk(Packet51MapChunk par1Packet51MapChunk)
    {
        if (par1Packet51MapChunk.includeInitialize)
        {
            if (par1Packet51MapChunk.yChMin == 0)
            {
                this.worldClient.doPreChunk(par1Packet51MapChunk.xCh, par1Packet51MapChunk.zCh, false);
                return;
            }

            this.worldClient.doPreChunk(par1Packet51MapChunk.xCh, par1Packet51MapChunk.zCh, true);
        }

        this.worldClient.invalidateBlockReceiveRegion(par1Packet51MapChunk.xCh << 4, 0, par1Packet51MapChunk.zCh << 4, (par1Packet51MapChunk.xCh << 4) + 15, 256, (par1Packet51MapChunk.zCh << 4) + 15);
        Chunk var2 = this.worldClient.getChunkFromChunkCoords(par1Packet51MapChunk.xCh, par1Packet51MapChunk.zCh);

        if (par1Packet51MapChunk.includeInitialize && var2 == null)
        {
            this.worldClient.doPreChunk(par1Packet51MapChunk.xCh, par1Packet51MapChunk.zCh, true);
            var2 = this.worldClient.getChunkFromChunkCoords(par1Packet51MapChunk.xCh, par1Packet51MapChunk.zCh);
        }

        if (var2 != null)
        {
            var2.fillChunk(par1Packet51MapChunk.getCompressedChunkData(), par1Packet51MapChunk.yChMin, par1Packet51MapChunk.yChMax, par1Packet51MapChunk.includeInitialize);
            this.worldClient.markBlockRangeForRenderUpdate(par1Packet51MapChunk.xCh << 4, 0, par1Packet51MapChunk.zCh << 4, (par1Packet51MapChunk.xCh << 4) + 15, 256, (par1Packet51MapChunk.zCh << 4) + 15);

            if (!par1Packet51MapChunk.includeInitialize || !(this.worldClient.provider instanceof WorldProviderSurface))
            {
                var2.resetRelightChecks();
            }
        }
    }

    public void handleBlockChange(Packet53BlockChange par1Packet53BlockChange)
    {
        this.worldClient.setBlockAndMetadataAndInvalidate(par1Packet53BlockChange.xPosition, par1Packet53BlockChange.yPosition, par1Packet53BlockChange.zPosition, par1Packet53BlockChange.type, par1Packet53BlockChange.metadata);
    }

    public void handleKickDisconnect(Packet255KickDisconnect par1Packet255KickDisconnect)
    {
        /* WDL >>> */
        if(WDL.downloading)
        {
            WDL.stop();
            try{Thread.sleep(2000);}catch(Exception e){}
        }
        /* <<< WDL */

        this.netManager.networkShutdown("disconnect.kicked", new Object[0]);
        this.disconnected = true;
        this.mc.loadWorld((WorldClient)null);

        if (this.field_98183_l != null)
        {
            this.mc.displayGuiScreen(new GuiScreenDisconnectedOnline(this.field_98183_l, "disconnect.disconnected", "disconnect.genericReason", new Object[] {par1Packet255KickDisconnect.reason}));
        }
        else
        {
            this.mc.displayGuiScreen(new GuiDisconnected(new GuiMultiplayer(new GuiMainMenu()), "disconnect.disconnected", "disconnect.genericReason", new Object[] {par1Packet255KickDisconnect.reason}));
        }
    }

    public void handleErrorMessage(String par1Str, Object[] par2ArrayOfObj)
    {
        if (!this.disconnected)
        {
            /* WDL >>> */
            if(WDL.downloading)
            {
                WDL.stop();
                try{Thread.sleep(2000);}catch(Exception e){}
            }
            /* <<< WDL */

            this.disconnected = true;
            this.mc.loadWorld((WorldClient)null);

            if (this.field_98183_l != null)
            {
                this.mc.displayGuiScreen(new GuiScreenDisconnectedOnline(this.field_98183_l, "disconnect.lost", par1Str, par2ArrayOfObj));
            }
            else
            {
                this.mc.displayGuiScreen(new GuiDisconnected(new GuiMultiplayer(new GuiMainMenu()), "disconnect.lost", par1Str, par2ArrayOfObj));
            }
        }
    }

    public void quitWithPacket(Packet par1Packet)
    {
        if (!this.disconnected)
        {
            this.netManager.addToSendQueue(par1Packet);
            this.netManager.serverShutdown();
        }
    }

    /**
     * Adds the packet to the send queue
     */
    public void addToSendQueue(Packet par1Packet)
    {
        if (!this.disconnected)
        {
            this.netManager.addToSendQueue(par1Packet);
        }
    }

    public void handleCollect(Packet22Collect par1Packet22Collect)
    {
        Entity var2 = this.getEntityByID(par1Packet22Collect.collectedEntityId);
        Object var3 = (EntityLivingBase)this.getEntityByID(par1Packet22Collect.collectorEntityId);

        if (var3 == null)
        {
            var3 = this.mc.thePlayer;
        }

        if (var2 != null)
        {
            if (var2 instanceof EntityXPOrb)
            {
                this.worldClient.playSoundAtEntity(var2, "random.orb", 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }
            else
            {
                this.worldClient.playSoundAtEntity(var2, "random.pop", 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }

            this.mc.effectRenderer.addEffect(new EntityPickupFX(this.mc.theWorld, var2, (Entity)var3, -0.5F));
            this.worldClient.removeEntityFromWorld(par1Packet22Collect.collectedEntityId);
        }
    }

    public void handleChat(Packet3Chat par1Packet3Chat)
    {
        /* WDL >>> */
        String msg = par1Packet3Chat.message;
        WDL.handleServerSeedMessage(msg);
        /* <<< WDL */

        this.mc.ingameGUI.getChatGUI().printChatMessage(ChatMessageComponent.func_111078_c(par1Packet3Chat.message).func_111068_a(true));
    }

    public void handleAnimation(Packet18Animation par1Packet18Animation)
    {
        Entity var2 = this.getEntityByID(par1Packet18Animation.entityId);

        if (var2 != null)
        {
            if (par1Packet18Animation.animate == 1)
            {
                EntityLivingBase var3 = (EntityLivingBase)var2;
                var3.swingItem();
            }
            else if (par1Packet18Animation.animate == 2)
            {
                var2.performHurtAnimation();
            }
            else if (par1Packet18Animation.animate == 3)
            {
                EntityPlayer var4 = (EntityPlayer)var2;
                var4.wakeUpPlayer(false, false, false);
            }
            else if (par1Packet18Animation.animate != 4)
            {
                if (par1Packet18Animation.animate == 6)
                {
                    this.mc.effectRenderer.addEffect(new EntityCrit2FX(this.mc.theWorld, var2));
                }
                else if (par1Packet18Animation.animate == 7)
                {
                    EntityCrit2FX var5 = new EntityCrit2FX(this.mc.theWorld, var2, "magicCrit");
                    this.mc.effectRenderer.addEffect(var5);
                }
                else if (par1Packet18Animation.animate == 5 && var2 instanceof EntityOtherPlayerMP)
                {
                    ;
                }
            }
        }
    }

    public void handleSleep(Packet17Sleep par1Packet17Sleep)
    {
        Entity var2 = this.getEntityByID(par1Packet17Sleep.entityID);

        if (var2 != null)
        {
            if (par1Packet17Sleep.field_73622_e == 0)
            {
                EntityPlayer var3 = (EntityPlayer)var2;
                var3.sleepInBedAt(par1Packet17Sleep.bedX, par1Packet17Sleep.bedY, par1Packet17Sleep.bedZ);
            }
        }
    }

    /**
     * Disconnects the network connection.
     */
    public void disconnect()
    {
        this.disconnected = true;
        this.netManager.wakeThreads();
        this.netManager.networkShutdown("disconnect.closed", new Object[0]);
    }

    public void handleMobSpawn(Packet24MobSpawn par1Packet24MobSpawn)
    {
        double var2 = (double)par1Packet24MobSpawn.xPosition / 32.0D;
        double var4 = (double)par1Packet24MobSpawn.yPosition / 32.0D;
        double var6 = (double)par1Packet24MobSpawn.zPosition / 32.0D;
        float var8 = (float)(par1Packet24MobSpawn.yaw * 360) / 256.0F;
        float var9 = (float)(par1Packet24MobSpawn.pitch * 360) / 256.0F;
        EntityLivingBase var10 = (EntityLivingBase)EntityList.createEntityByID(par1Packet24MobSpawn.type, this.mc.theWorld);
        var10.serverPosX = par1Packet24MobSpawn.xPosition;
        var10.serverPosY = par1Packet24MobSpawn.yPosition;
        var10.serverPosZ = par1Packet24MobSpawn.zPosition;
        var10.rotationYawHead = (float)(par1Packet24MobSpawn.headYaw * 360) / 256.0F;
        Entity[] var11 = var10.getParts();

        if (var11 != null)
        {
            int var12 = par1Packet24MobSpawn.entityId - var10.entityId;

            for (int var13 = 0; var13 < var11.length; ++var13)
            {
                var11[var13].entityId += var12;
            }
        }

        var10.entityId = par1Packet24MobSpawn.entityId;
        var10.setPositionAndRotation(var2, var4, var6, var8, var9);
        var10.motionX = (double)((float)par1Packet24MobSpawn.velocityX / 8000.0F);
        var10.motionY = (double)((float)par1Packet24MobSpawn.velocityY / 8000.0F);
        var10.motionZ = (double)((float)par1Packet24MobSpawn.velocityZ / 8000.0F);
        this.worldClient.addEntityToWorld(par1Packet24MobSpawn.entityId, var10);
        List var14 = par1Packet24MobSpawn.getMetadata();

        if (var14 != null)
        {
            var10.getDataWatcher().updateWatchedObjectsFromList(var14);
        }
    }

    public void handleUpdateTime(Packet4UpdateTime par1Packet4UpdateTime)
    {
        this.mc.theWorld.func_82738_a(par1Packet4UpdateTime.worldAge);
        this.mc.theWorld.setWorldTime(par1Packet4UpdateTime.time);
    }

    public void handleSpawnPosition(Packet6SpawnPosition par1Packet6SpawnPosition)
    {
        this.mc.thePlayer.setSpawnChunk(new ChunkCoordinates(par1Packet6SpawnPosition.xPosition, par1Packet6SpawnPosition.yPosition, par1Packet6SpawnPosition.zPosition), true);
        this.mc.theWorld.getWorldInfo().setSpawnPosition(par1Packet6SpawnPosition.xPosition, par1Packet6SpawnPosition.yPosition, par1Packet6SpawnPosition.zPosition);
    }

    /**
     * Packet handler
     */
    public void handleAttachEntity(Packet39AttachEntity par1Packet39AttachEntity)
    {
        Object var2 = this.getEntityByID(par1Packet39AttachEntity.field_111006_b);
        Entity var3 = this.getEntityByID(par1Packet39AttachEntity.vehicleEntityId);

        if (par1Packet39AttachEntity.field_111007_a == 0)
        {
            boolean var4 = false;

            if (par1Packet39AttachEntity.field_111006_b == this.mc.thePlayer.entityId)
            {
                var2 = this.mc.thePlayer;

                if (var3 instanceof EntityBoat)
                {
                    ((EntityBoat)var3).func_70270_d(false);
                }

                var4 = ((Entity)var2).ridingEntity == null && var3 != null;
            }
            else if (var3 instanceof EntityBoat)
            {
                ((EntityBoat)var3).func_70270_d(true);
            }

            if (var2 == null)
            {
                return;
            }

            ((Entity)var2).mountEntity(var3);

            if (var4)
            {
                GameSettings var5 = this.mc.gameSettings;
                this.mc.ingameGUI.func_110326_a(I18n.func_135052_a("mount.onboard", new Object[] {GameSettings.getKeyDisplayString(var5.keyBindSneak.keyCode)}), false);
            }
        }
        else if (par1Packet39AttachEntity.field_111007_a == 1 && var2 != null && var2 instanceof EntityLiving)
        {
            if (var3 != null)
            {
                ((EntityLiving)var2).func_110162_b(var3, false);
            }
            else
            {
                ((EntityLiving)var2).func_110160_i(false, false);
            }
        }
    }

    /**
     * Packet handler
     */
    public void handleEntityStatus(Packet38EntityStatus par1Packet38EntityStatus)
    {
        Entity var2 = this.getEntityByID(par1Packet38EntityStatus.entityId);

        if (var2 != null)
        {
            var2.handleHealthUpdate(par1Packet38EntityStatus.entityStatus);
        }
    }

    private Entity getEntityByID(int par1)
    {
        return (Entity)(par1 == this.mc.thePlayer.entityId ? this.mc.thePlayer : this.worldClient.getEntityByID(par1));
    }

    /**
     * Recieves player health from the server and then proceeds to set it locally on the client.
     */
    public void handleUpdateHealth(Packet8UpdateHealth par1Packet8UpdateHealth)
    {
        this.mc.thePlayer.setHealth(par1Packet8UpdateHealth.healthMP);
        this.mc.thePlayer.getFoodStats().setFoodLevel(par1Packet8UpdateHealth.food);
        this.mc.thePlayer.getFoodStats().setFoodSaturationLevel(par1Packet8UpdateHealth.foodSaturation);
    }

    /**
     * Handle an experience packet.
     */
    public void handleExperience(Packet43Experience par1Packet43Experience)
    {
        this.mc.thePlayer.setXPStats(par1Packet43Experience.experience, par1Packet43Experience.experienceTotal, par1Packet43Experience.experienceLevel);
    }

    /**
     * respawns the player
     */
    public void handleRespawn(Packet9Respawn par1Packet9Respawn)
    {
        if (par1Packet9Respawn.respawnDimension != this.mc.thePlayer.dimension)
        {
            this.doneLoadingTerrain = false;
            Scoreboard var2 = this.worldClient.getScoreboard();
            this.worldClient = new WorldClient(this, new WorldSettings(0L, par1Packet9Respawn.gameType, false, this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled(), par1Packet9Respawn.terrainType), par1Packet9Respawn.respawnDimension, par1Packet9Respawn.difficulty, this.mc.mcProfiler, this.mc.getLogAgent());
            this.worldClient.func_96443_a(var2);
            this.worldClient.isRemote = true;
            this.mc.loadWorld(this.worldClient);
            this.mc.thePlayer.dimension = par1Packet9Respawn.respawnDimension;
            this.mc.displayGuiScreen(new GuiDownloadTerrain(this));
        }

        this.mc.setDimensionAndSpawnPlayer(par1Packet9Respawn.respawnDimension);
        this.mc.playerController.setGameType(par1Packet9Respawn.gameType);
    }

    public void handleExplosion(Packet60Explosion par1Packet60Explosion)
    {
        Explosion var2 = new Explosion(this.mc.theWorld, (Entity)null, par1Packet60Explosion.explosionX, par1Packet60Explosion.explosionY, par1Packet60Explosion.explosionZ, par1Packet60Explosion.explosionSize);
        var2.affectedBlockPositions = par1Packet60Explosion.chunkPositionRecords;
        var2.doExplosionB(true);
        this.mc.thePlayer.motionX += (double)par1Packet60Explosion.getPlayerVelocityX();
        this.mc.thePlayer.motionY += (double)par1Packet60Explosion.getPlayerVelocityY();
        this.mc.thePlayer.motionZ += (double)par1Packet60Explosion.getPlayerVelocityZ();
    }

    public void handleOpenWindow(Packet100OpenWindow par1Packet100OpenWindow)
    {
        EntityClientPlayerMP var2 = this.mc.thePlayer;

        switch (par1Packet100OpenWindow.inventoryType)
        {
            case 0:
                var2.displayGUIChest(new InventoryBasic(par1Packet100OpenWindow.windowTitle, par1Packet100OpenWindow.useProvidedWindowTitle, par1Packet100OpenWindow.slotsCount));
                var2.openContainer.windowId = par1Packet100OpenWindow.windowId;
                break;

            case 1:
                var2.displayGUIWorkbench(MathHelper.floor_double(var2.posX), MathHelper.floor_double(var2.posY), MathHelper.floor_double(var2.posZ));
                var2.openContainer.windowId = par1Packet100OpenWindow.windowId;
                break;

            case 2:
                TileEntityFurnace var4 = new TileEntityFurnace();

                if (par1Packet100OpenWindow.useProvidedWindowTitle)
                {
                    var4.setGuiDisplayName(par1Packet100OpenWindow.windowTitle);
                }

                var2.displayGUIFurnace(var4);
                var2.openContainer.windowId = par1Packet100OpenWindow.windowId;
                break;

            case 3:
                TileEntityDispenser var7 = new TileEntityDispenser();

                if (par1Packet100OpenWindow.useProvidedWindowTitle)
                {
                    var7.setCustomName(par1Packet100OpenWindow.windowTitle);
                }

                var2.displayGUIDispenser(var7);
                var2.openContainer.windowId = par1Packet100OpenWindow.windowId;
                break;

            case 4:
                var2.displayGUIEnchantment(MathHelper.floor_double(var2.posX), MathHelper.floor_double(var2.posY), MathHelper.floor_double(var2.posZ), par1Packet100OpenWindow.useProvidedWindowTitle ? par1Packet100OpenWindow.windowTitle : null);
                var2.openContainer.windowId = par1Packet100OpenWindow.windowId;
                break;

            case 5:
                TileEntityBrewingStand var5 = new TileEntityBrewingStand();

                if (par1Packet100OpenWindow.useProvidedWindowTitle)
                {
                    var5.func_94131_a(par1Packet100OpenWindow.windowTitle);
                }

                var2.displayGUIBrewingStand(var5);
                var2.openContainer.windowId = par1Packet100OpenWindow.windowId;
                break;

            case 6:
                var2.displayGUIMerchant(new NpcMerchant(var2), par1Packet100OpenWindow.useProvidedWindowTitle ? par1Packet100OpenWindow.windowTitle : null);
                var2.openContainer.windowId = par1Packet100OpenWindow.windowId;
                break;

            case 7:
                TileEntityBeacon var8 = new TileEntityBeacon();
                var2.displayGUIBeacon(var8);

                if (par1Packet100OpenWindow.useProvidedWindowTitle)
                {
                    var8.func_94047_a(par1Packet100OpenWindow.windowTitle);
                }

                var2.openContainer.windowId = par1Packet100OpenWindow.windowId;
                break;

            case 8:
                var2.displayGUIAnvil(MathHelper.floor_double(var2.posX), MathHelper.floor_double(var2.posY), MathHelper.floor_double(var2.posZ));
                var2.openContainer.windowId = par1Packet100OpenWindow.windowId;
                break;

            case 9:
                TileEntityHopper var3 = new TileEntityHopper();

                if (par1Packet100OpenWindow.useProvidedWindowTitle)
                {
                    var3.setInventoryName(par1Packet100OpenWindow.windowTitle);
                }

                var2.displayGUIHopper(var3);
                var2.openContainer.windowId = par1Packet100OpenWindow.windowId;
                break;

            case 10:
                TileEntityDropper var6 = new TileEntityDropper();

                if (par1Packet100OpenWindow.useProvidedWindowTitle)
                {
                    var6.setCustomName(par1Packet100OpenWindow.windowTitle);
                }

                var2.displayGUIDispenser(var6);
                var2.openContainer.windowId = par1Packet100OpenWindow.windowId;
                break;

            case 11:
                Entity var9 = this.getEntityByID(par1Packet100OpenWindow.field_111008_f);

                if (var9 != null && var9 instanceof EntityHorse)
                {
                    var2.func_110298_a((EntityHorse)var9, new AnimalChest(par1Packet100OpenWindow.windowTitle, par1Packet100OpenWindow.useProvidedWindowTitle, par1Packet100OpenWindow.slotsCount));
                    var2.openContainer.windowId = par1Packet100OpenWindow.windowId;
                }
        }
    }

    public void handleSetSlot(Packet103SetSlot par1Packet103SetSlot)
    {
        EntityClientPlayerMP var2 = this.mc.thePlayer;

        if (par1Packet103SetSlot.windowId == -1)
        {
            var2.inventory.setItemStack(par1Packet103SetSlot.myItemStack);
        }
        else
        {
            boolean var3 = false;

            if (this.mc.currentScreen instanceof GuiContainerCreative)
            {
                GuiContainerCreative var4 = (GuiContainerCreative)this.mc.currentScreen;
                var3 = var4.getCurrentTabIndex() != CreativeTabs.tabInventory.getTabIndex();
            }

            if (par1Packet103SetSlot.windowId == 0 && par1Packet103SetSlot.itemSlot >= 36 && par1Packet103SetSlot.itemSlot < 45)
            {
                ItemStack var5 = var2.inventoryContainer.getSlot(par1Packet103SetSlot.itemSlot).getStack();

                if (par1Packet103SetSlot.myItemStack != null && (var5 == null || var5.stackSize < par1Packet103SetSlot.myItemStack.stackSize))
                {
                    par1Packet103SetSlot.myItemStack.animationsToGo = 5;
                }

                var2.inventoryContainer.putStackInSlot(par1Packet103SetSlot.itemSlot, par1Packet103SetSlot.myItemStack);
            }
            else if (par1Packet103SetSlot.windowId == var2.openContainer.windowId && (par1Packet103SetSlot.windowId != 0 || !var3))
            {
                var2.openContainer.putStackInSlot(par1Packet103SetSlot.itemSlot, par1Packet103SetSlot.myItemStack);
            }
        }
    }

    public void handleTransaction(Packet106Transaction par1Packet106Transaction)
    {
        Container var2 = null;
        EntityClientPlayerMP var3 = this.mc.thePlayer;

        if (par1Packet106Transaction.windowId == 0)
        {
            var2 = var3.inventoryContainer;
        }
        else if (par1Packet106Transaction.windowId == var3.openContainer.windowId)
        {
            var2 = var3.openContainer;
        }

        if (var2 != null && !par1Packet106Transaction.accepted)
        {
            this.addToSendQueue(new Packet106Transaction(par1Packet106Transaction.windowId, par1Packet106Transaction.shortWindowId, true));
        }
    }

    public void handleWindowItems(Packet104WindowItems par1Packet104WindowItems)
    {
        EntityClientPlayerMP var2 = this.mc.thePlayer;

        if (par1Packet104WindowItems.windowId == 0)
        {
            var2.inventoryContainer.putStacksInSlots(par1Packet104WindowItems.itemStack);
        }
        else if (par1Packet104WindowItems.windowId == var2.openContainer.windowId)
        {
            var2.openContainer.putStacksInSlots(par1Packet104WindowItems.itemStack);
        }
    }

    public void func_142031_a(Packet133TileEditorOpen par1Packet133TileEditorOpen)
    {
        TileEntity var2 = this.worldClient.getBlockTileEntity(par1Packet133TileEditorOpen.field_142035_b, par1Packet133TileEditorOpen.field_142036_c, par1Packet133TileEditorOpen.field_142034_d);

        if (var2 != null)
        {
            this.mc.thePlayer.displayGUIEditSign(var2);
        }
        else if (par1Packet133TileEditorOpen.field_142037_a == 0)
        {
            TileEntitySign var3 = new TileEntitySign();
            var3.setWorldObj(this.worldClient);
            var3.xCoord = par1Packet133TileEditorOpen.field_142035_b;
            var3.yCoord = par1Packet133TileEditorOpen.field_142036_c;
            var3.zCoord = par1Packet133TileEditorOpen.field_142034_d;
            this.mc.thePlayer.displayGUIEditSign(var3);
        }
    }

    /**
     * Updates Client side signs
     */
    public void handleUpdateSign(Packet130UpdateSign par1Packet130UpdateSign)
    {
        boolean var2 = false;

        if (this.mc.theWorld.blockExists(par1Packet130UpdateSign.xPosition, par1Packet130UpdateSign.yPosition, par1Packet130UpdateSign.zPosition))
        {
            TileEntity var3 = this.mc.theWorld.getBlockTileEntity(par1Packet130UpdateSign.xPosition, par1Packet130UpdateSign.yPosition, par1Packet130UpdateSign.zPosition);

            if (var3 instanceof TileEntitySign)
            {
                TileEntitySign var4 = (TileEntitySign)var3;

                if (var4.isEditable())
                {
                    for (int var5 = 0; var5 < 4; ++var5)
                    {
                        var4.signText[var5] = par1Packet130UpdateSign.signLines[var5];
                    }

                    var4.onInventoryChanged();
                }

                var2 = true;
            }
        }

        if (!var2 && this.mc.thePlayer != null)
        {
            this.mc.thePlayer.sendChatToPlayer(ChatMessageComponent.func_111066_d("Unable to locate sign at " + par1Packet130UpdateSign.xPosition + ", " + par1Packet130UpdateSign.yPosition + ", " + par1Packet130UpdateSign.zPosition));
        }
    }

    public void handleTileEntityData(Packet132TileEntityData par1Packet132TileEntityData)
    {
        if (this.mc.theWorld.blockExists(par1Packet132TileEntityData.xPosition, par1Packet132TileEntityData.yPosition, par1Packet132TileEntityData.zPosition))
        {
            TileEntity var2 = this.mc.theWorld.getBlockTileEntity(par1Packet132TileEntityData.xPosition, par1Packet132TileEntityData.yPosition, par1Packet132TileEntityData.zPosition);

            if (var2 != null)
            {
                if (par1Packet132TileEntityData.actionType == 1 && var2 instanceof TileEntityMobSpawner)
                {
                    var2.readFromNBT(par1Packet132TileEntityData.customParam1);
                }
                else if (par1Packet132TileEntityData.actionType == 2 && var2 instanceof TileEntityCommandBlock)
                {
                    var2.readFromNBT(par1Packet132TileEntityData.customParam1);
                }
                else if (par1Packet132TileEntityData.actionType == 3 && var2 instanceof TileEntityBeacon)
                {
                    var2.readFromNBT(par1Packet132TileEntityData.customParam1);
                }
                else if (par1Packet132TileEntityData.actionType == 4 && var2 instanceof TileEntitySkull)
                {
                    var2.readFromNBT(par1Packet132TileEntityData.customParam1);
                }
            }
        }
    }

    public void handleUpdateProgressbar(Packet105UpdateProgressbar par1Packet105UpdateProgressbar)
    {
        EntityClientPlayerMP var2 = this.mc.thePlayer;
        this.unexpectedPacket(par1Packet105UpdateProgressbar);

        if (var2.openContainer != null && var2.openContainer.windowId == par1Packet105UpdateProgressbar.windowId)
        {
            var2.openContainer.updateProgressBar(par1Packet105UpdateProgressbar.progressBar, par1Packet105UpdateProgressbar.progressBarValue);
        }
    }

    public void handlePlayerInventory(Packet5PlayerInventory par1Packet5PlayerInventory)
    {
        Entity var2 = this.getEntityByID(par1Packet5PlayerInventory.entityID);

        if (var2 != null)
        {
            var2.setCurrentItemOrArmor(par1Packet5PlayerInventory.slot, par1Packet5PlayerInventory.getItemSlot());
        }
    }

    public void handleCloseWindow(Packet101CloseWindow par1Packet101CloseWindow)
    {
        this.mc.thePlayer.func_92015_f();
    }

    public void handleBlockEvent(Packet54PlayNoteBlock par1Packet54PlayNoteBlock)
    {
        this.mc.theWorld.addBlockEvent(par1Packet54PlayNoteBlock.xLocation, par1Packet54PlayNoteBlock.yLocation, par1Packet54PlayNoteBlock.zLocation, par1Packet54PlayNoteBlock.blockId, par1Packet54PlayNoteBlock.instrumentType, par1Packet54PlayNoteBlock.pitch);
    }

    public void handleBlockDestroy(Packet55BlockDestroy par1Packet55BlockDestroy)
    {
        this.mc.theWorld.destroyBlockInWorldPartially(par1Packet55BlockDestroy.getEntityId(), par1Packet55BlockDestroy.getPosX(), par1Packet55BlockDestroy.getPosY(), par1Packet55BlockDestroy.getPosZ(), par1Packet55BlockDestroy.getDestroyedStage());
    }

    public void handleMapChunks(Packet56MapChunks par1Packet56MapChunks)
    {
        for (int var2 = 0; var2 < par1Packet56MapChunks.getNumberOfChunkInPacket(); ++var2)
        {
            int var3 = par1Packet56MapChunks.getChunkPosX(var2);
            int var4 = par1Packet56MapChunks.getChunkPosZ(var2);
            this.worldClient.doPreChunk(var3, var4, true);
            this.worldClient.invalidateBlockReceiveRegion(var3 << 4, 0, var4 << 4, (var3 << 4) + 15, 256, (var4 << 4) + 15);
            Chunk var5 = this.worldClient.getChunkFromChunkCoords(var3, var4);

            if (var5 == null)
            {
                this.worldClient.doPreChunk(var3, var4, true);
                var5 = this.worldClient.getChunkFromChunkCoords(var3, var4);
            }

            if (var5 != null)
            {
                var5.fillChunk(par1Packet56MapChunks.getChunkCompressedData(var2), par1Packet56MapChunks.field_73590_a[var2], par1Packet56MapChunks.field_73588_b[var2], true);
                this.worldClient.markBlockRangeForRenderUpdate(var3 << 4, 0, var4 << 4, (var3 << 4) + 15, 256, (var4 << 4) + 15);

                if (!(this.worldClient.provider instanceof WorldProviderSurface))
                {
                    var5.resetRelightChecks();
                }
            }
        }
    }

    /**
     * If this returns false, all packets will be queued for the main thread to handle, even if they would otherwise be
     * processed asynchronously. Used to avoid processing packets on the client before the world has been downloaded
     * (which happens on the main thread)
     */
    public boolean canProcessPacketsAsync()
    {
        return this.mc != null && this.mc.theWorld != null && this.mc.thePlayer != null && this.worldClient != null;
    }

    public void handleGameEvent(Packet70GameEvent par1Packet70GameEvent)
    {
        EntityClientPlayerMP var2 = this.mc.thePlayer;
        int var3 = par1Packet70GameEvent.eventType;
        int var4 = par1Packet70GameEvent.gameMode;

        if (var3 >= 0 && var3 < Packet70GameEvent.clientMessage.length && Packet70GameEvent.clientMessage[var3] != null)
        {
            var2.addChatMessage(Packet70GameEvent.clientMessage[var3]);
        }

        if (var3 == 1)
        {
            this.worldClient.getWorldInfo().setRaining(true);
            this.worldClient.setRainStrength(0.0F);
        }
        else if (var3 == 2)
        {
            this.worldClient.getWorldInfo().setRaining(false);
            this.worldClient.setRainStrength(1.0F);
        }
        else if (var3 == 3)
        {
            this.mc.playerController.setGameType(EnumGameType.getByID(var4));
        }
        else if (var3 == 4)
        {
            this.mc.displayGuiScreen(new GuiWinGame());
        }
        else if (var3 == 5)
        {
            GameSettings var5 = this.mc.gameSettings;

            if (var4 == 0)
            {
                this.mc.displayGuiScreen(new GuiScreenDemo());
            }
            else if (var4 == 101)
            {
                this.mc.ingameGUI.getChatGUI().addTranslatedMessage("demo.help.movement", new Object[] {Keyboard.getKeyName(var5.keyBindForward.keyCode), Keyboard.getKeyName(var5.keyBindLeft.keyCode), Keyboard.getKeyName(var5.keyBindBack.keyCode), Keyboard.getKeyName(var5.keyBindRight.keyCode)});
            }
            else if (var4 == 102)
            {
                this.mc.ingameGUI.getChatGUI().addTranslatedMessage("demo.help.jump", new Object[] {Keyboard.getKeyName(var5.keyBindJump.keyCode)});
            }
            else if (var4 == 103)
            {
                this.mc.ingameGUI.getChatGUI().addTranslatedMessage("demo.help.inventory", new Object[] {Keyboard.getKeyName(var5.keyBindInventory.keyCode)});
            }
        }
        else if (var3 == 6)
        {
            this.worldClient.playSound(var2.posX, var2.posY + (double)var2.getEyeHeight(), var2.posZ, "random.successful_hit", 0.18F, 0.45F, false);
        }
    }

    /**
     * Contains logic for handling packets containing arbitrary unique item data. Currently this is only for maps.
     */
    public void handleMapData(Packet131MapData par1Packet131MapData)
    {
        if (par1Packet131MapData.itemID == Item.map.itemID)
        {
            ItemMap.getMPMapData(par1Packet131MapData.uniqueID, this.mc.theWorld).updateMPMapData(par1Packet131MapData.itemData);
        }
        else
        {
            this.mc.getLogAgent().logWarning("Unknown itemid: " + par1Packet131MapData.uniqueID);
        }
    }

    public void handleDoorChange(Packet61DoorChange par1Packet61DoorChange)
    {
        if (par1Packet61DoorChange.getRelativeVolumeDisabled())
        {
            this.mc.theWorld.func_82739_e(par1Packet61DoorChange.sfxID, par1Packet61DoorChange.posX, par1Packet61DoorChange.posY, par1Packet61DoorChange.posZ, par1Packet61DoorChange.auxData);
        }
        else
        {
            this.mc.theWorld.playAuxSFX(par1Packet61DoorChange.sfxID, par1Packet61DoorChange.posX, par1Packet61DoorChange.posY, par1Packet61DoorChange.posZ, par1Packet61DoorChange.auxData);
        }
    }

    /**
     * Increment player statistics
     */
    public void handleStatistic(Packet200Statistic par1Packet200Statistic)
    {
        this.mc.thePlayer.incrementStat(StatList.getOneShotStat(par1Packet200Statistic.statisticId), par1Packet200Statistic.amount);
    }

    /**
     * Handle an entity effect packet.
     */
    public void handleEntityEffect(Packet41EntityEffect par1Packet41EntityEffect)
    {
        Entity var2 = this.getEntityByID(par1Packet41EntityEffect.entityId);

        if (var2 instanceof EntityLivingBase)
        {
            PotionEffect var3 = new PotionEffect(par1Packet41EntityEffect.effectId, par1Packet41EntityEffect.duration, par1Packet41EntityEffect.effectAmplifier);
            var3.setPotionDurationMax(par1Packet41EntityEffect.isDurationMax());
            ((EntityLivingBase)var2).addPotionEffect(var3);
        }
    }

    /**
     * Handle a remove entity effect packet.
     */
    public void handleRemoveEntityEffect(Packet42RemoveEntityEffect par1Packet42RemoveEntityEffect)
    {
        Entity var2 = this.getEntityByID(par1Packet42RemoveEntityEffect.entityId);

        if (var2 instanceof EntityLivingBase)
        {
            ((EntityLivingBase)var2).removePotionEffectClient(par1Packet42RemoveEntityEffect.effectId);
        }
    }

    /**
     * determine if it is a server handler
     */
    public boolean isServerHandler()
    {
        return false;
    }

    /**
     * Handle a player information packet.
     */
    public void handlePlayerInfo(Packet201PlayerInfo par1Packet201PlayerInfo)
    {
        GuiPlayerInfo var2 = (GuiPlayerInfo)this.playerInfoMap.get(par1Packet201PlayerInfo.playerName);

        if (var2 == null && par1Packet201PlayerInfo.isConnected)
        {
            var2 = new GuiPlayerInfo(par1Packet201PlayerInfo.playerName);
            this.playerInfoMap.put(par1Packet201PlayerInfo.playerName, var2);
            this.playerInfoList.add(var2);
        }

        if (var2 != null && !par1Packet201PlayerInfo.isConnected)
        {
            this.playerInfoMap.remove(par1Packet201PlayerInfo.playerName);
            this.playerInfoList.remove(var2);
        }

        if (par1Packet201PlayerInfo.isConnected && var2 != null)
        {
            var2.responseTime = par1Packet201PlayerInfo.ping;
        }
    }

    /**
     * Handle a keep alive packet.
     */
    public void handleKeepAlive(Packet0KeepAlive par1Packet0KeepAlive)
    {
        this.addToSendQueue(new Packet0KeepAlive(par1Packet0KeepAlive.randomId));
    }

    /**
     * Handle a player abilities packet.
     */
    public void handlePlayerAbilities(Packet202PlayerAbilities par1Packet202PlayerAbilities)
    {
        EntityClientPlayerMP var2 = this.mc.thePlayer;
        var2.capabilities.isFlying = par1Packet202PlayerAbilities.getFlying();
        var2.capabilities.isCreativeMode = par1Packet202PlayerAbilities.isCreativeMode();
        var2.capabilities.disableDamage = par1Packet202PlayerAbilities.getDisableDamage();
        var2.capabilities.allowFlying = par1Packet202PlayerAbilities.getAllowFlying();
        var2.capabilities.setFlySpeed(par1Packet202PlayerAbilities.getFlySpeed());
        var2.capabilities.setPlayerWalkSpeed(par1Packet202PlayerAbilities.getWalkSpeed());
    }

    public void handleAutoComplete(Packet203AutoComplete par1Packet203AutoComplete)
    {
        String[] var2 = par1Packet203AutoComplete.getText().split("\u0000");

        if (this.mc.currentScreen instanceof GuiChat)
        {
            GuiChat var3 = (GuiChat)this.mc.currentScreen;
            var3.func_73894_a(var2);
        }
    }

    public void handleLevelSound(Packet62LevelSound par1Packet62LevelSound)
    {
        this.mc.theWorld.playSound(par1Packet62LevelSound.getEffectX(), par1Packet62LevelSound.getEffectY(), par1Packet62LevelSound.getEffectZ(), par1Packet62LevelSound.getSoundName(), par1Packet62LevelSound.getVolume(), par1Packet62LevelSound.getPitch(), false);
    }

    public void handleCustomPayload(Packet250CustomPayload par1Packet250CustomPayload)
    {
        if ("MC|TrList".equals(par1Packet250CustomPayload.channel))
        {
            DataInputStream var2 = new DataInputStream(new ByteArrayInputStream(par1Packet250CustomPayload.data));

            try
            {
                int var3 = var2.readInt();
                GuiScreen var4 = this.mc.currentScreen;

                if (var4 != null && var4 instanceof GuiMerchant && var3 == this.mc.thePlayer.openContainer.windowId)
                {
                    IMerchant var5 = ((GuiMerchant)var4).getIMerchant();
                    MerchantRecipeList var6 = MerchantRecipeList.readRecipiesFromStream(var2);
                    var5.setRecipes(var6);
                }
            }
            catch (IOException var7)
            {
                var7.printStackTrace();
            }
        }
        else if ("MC|Brand".equals(par1Packet250CustomPayload.channel))
        {
            this.mc.thePlayer.func_142020_c(new String(par1Packet250CustomPayload.data, Charsets.UTF_8));
        }
    }

    /**
     * Handle a set objective packet.
     */
    public void handleSetObjective(Packet206SetObjective par1Packet206SetObjective)
    {
        Scoreboard var2 = this.worldClient.getScoreboard();
        ScoreObjective var3;

        if (par1Packet206SetObjective.change == 0)
        {
            var3 = var2.func_96535_a(par1Packet206SetObjective.objectiveName, ScoreObjectiveCriteria.field_96641_b);
            var3.setDisplayName(par1Packet206SetObjective.objectiveDisplayName);
        }
        else
        {
            var3 = var2.getObjective(par1Packet206SetObjective.objectiveName);

            if (par1Packet206SetObjective.change == 1)
            {
                var2.func_96519_k(var3);
            }
            else if (par1Packet206SetObjective.change == 2)
            {
                var3.setDisplayName(par1Packet206SetObjective.objectiveDisplayName);
            }
        }
    }

    /**
     * Handle a set score packet.
     */
    public void handleSetScore(Packet207SetScore par1Packet207SetScore)
    {
        Scoreboard var2 = this.worldClient.getScoreboard();
        ScoreObjective var3 = var2.getObjective(par1Packet207SetScore.scoreName);

        if (par1Packet207SetScore.updateOrRemove == 0)
        {
            Score var4 = var2.func_96529_a(par1Packet207SetScore.itemName, var3);
            var4.func_96647_c(par1Packet207SetScore.value);
        }
        else if (par1Packet207SetScore.updateOrRemove == 1)
        {
            var2.func_96515_c(par1Packet207SetScore.itemName);
        }
    }

    /**
     * Handle a set display objective packet.
     */
    public void handleSetDisplayObjective(Packet208SetDisplayObjective par1Packet208SetDisplayObjective)
    {
        Scoreboard var2 = this.worldClient.getScoreboard();

        if (par1Packet208SetDisplayObjective.scoreName.length() == 0)
        {
            var2.func_96530_a(par1Packet208SetDisplayObjective.scoreboardPosition, (ScoreObjective)null);
        }
        else
        {
            ScoreObjective var3 = var2.getObjective(par1Packet208SetDisplayObjective.scoreName);
            var2.func_96530_a(par1Packet208SetDisplayObjective.scoreboardPosition, var3);
        }
    }

    /**
     * Handle a set player team packet.
     */
    public void handleSetPlayerTeam(Packet209SetPlayerTeam par1Packet209SetPlayerTeam)
    {
        Scoreboard var2 = this.worldClient.getScoreboard();
        ScorePlayerTeam var3;

        if (par1Packet209SetPlayerTeam.mode == 0)
        {
            var3 = var2.func_96527_f(par1Packet209SetPlayerTeam.teamName);
        }
        else
        {
            var3 = var2.func_96508_e(par1Packet209SetPlayerTeam.teamName);
        }

        if (par1Packet209SetPlayerTeam.mode == 0 || par1Packet209SetPlayerTeam.mode == 2)
        {
            var3.func_96664_a(par1Packet209SetPlayerTeam.teamDisplayName);
            var3.func_96666_b(par1Packet209SetPlayerTeam.teamPrefix);
            var3.func_96662_c(par1Packet209SetPlayerTeam.teamSuffix);
            var3.func_98298_a(par1Packet209SetPlayerTeam.friendlyFire);
        }

        Iterator var4;
        String var5;

        if (par1Packet209SetPlayerTeam.mode == 0 || par1Packet209SetPlayerTeam.mode == 3)
        {
            var4 = par1Packet209SetPlayerTeam.playerNames.iterator();

            while (var4.hasNext())
            {
                var5 = (String)var4.next();
                var2.func_96521_a(var5, var3);
            }
        }

        if (par1Packet209SetPlayerTeam.mode == 4)
        {
            var4 = par1Packet209SetPlayerTeam.playerNames.iterator();

            while (var4.hasNext())
            {
                var5 = (String)var4.next();
                var2.removePlayerFromTeam(var5, var3);
            }
        }

        if (par1Packet209SetPlayerTeam.mode == 1)
        {
            var2.func_96511_d(var3);
        }
    }

    /**
     * Handle a world particles packet.
     */
    public void handleWorldParticles(Packet63WorldParticles par1Packet63WorldParticles)
    {
        for (int var2 = 0; var2 < par1Packet63WorldParticles.getQuantity(); ++var2)
        {
            double var3 = this.rand.nextGaussian() * (double)par1Packet63WorldParticles.getOffsetX();
            double var5 = this.rand.nextGaussian() * (double)par1Packet63WorldParticles.getOffsetY();
            double var7 = this.rand.nextGaussian() * (double)par1Packet63WorldParticles.getOffsetZ();
            double var9 = this.rand.nextGaussian() * (double)par1Packet63WorldParticles.getSpeed();
            double var11 = this.rand.nextGaussian() * (double)par1Packet63WorldParticles.getSpeed();
            double var13 = this.rand.nextGaussian() * (double)par1Packet63WorldParticles.getSpeed();
            this.worldClient.spawnParticle(par1Packet63WorldParticles.getParticleName(), par1Packet63WorldParticles.getPositionX() + var3, par1Packet63WorldParticles.getPositionY() + var5, par1Packet63WorldParticles.getPositionZ() + var7, var9, var11, var13);
        }
    }

    public void func_110773_a(Packet44UpdateAttributes par1Packet44UpdateAttributes)
    {
        Entity var2 = this.getEntityByID(par1Packet44UpdateAttributes.func_111002_d());

        if (var2 != null)
        {
            if (!(var2 instanceof EntityLivingBase))
            {
                throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + var2 + ")");
            }
            else
            {
                BaseAttributeMap var3 = ((EntityLivingBase)var2).func_110140_aT();
                Iterator var4 = par1Packet44UpdateAttributes.func_111003_f().iterator();

                while (var4.hasNext())
                {
                    Packet44UpdateAttributesSnapshot var5 = (Packet44UpdateAttributesSnapshot)var4.next();
                    AttributeInstance var6 = var3.func_111152_a(var5.func_142040_a());

                    if (var6 == null)
                    {
                        var6 = var3.func_111150_b(new RangedAttribute(var5.func_142040_a(), 0.0D, 2.2250738585072014E-308D, Double.MAX_VALUE));
                    }

                    var6.func_111128_a(var5.func_142041_b());
                    var6.func_142049_d();
                    Iterator var7 = var5.func_142039_c().iterator();

                    while (var7.hasNext())
                    {
                        AttributeModifier var8 = (AttributeModifier)var7.next();
                        var6.func_111121_a(var8);
                    }
                }
            }
        }
    }

    /**
     * Return the NetworkManager instance used by this NetClientHandler
     */
    public INetworkManager getNetManager()
    {
        return this.netManager;
    }
}
