// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

package net.minecraft.src;

import java.io.*;
import java.net.*;
import java.util.Random;
import net.minecraft.client.Minecraft;

// Referenced classes of package net.minecraft.src:
//            NetHandler, MapStorage, NetworkManager, PlayerControllerMP, 
//            StatList, StatFileWriter, WorldClient, Packet1Login, 
//            EntityPlayerSP, GuiDownloadTerrain, Packet21PickupSpawn, EntityItem, 
//            ItemStack, Packet23VehicleSpawn, EntityMinecart, EntityFish, 
//            EntityArrow, EntitySnowball, EntityFireball, EntityEgg, 
//            EntityBoat, EntityTNTPrimed, EntityFallingSand, Block, 
//            Entity, EntityLiving, Packet71Weather, EntityLightningBolt, 
//            EntityPainting, Packet25EntityPainting, Packet28EntityVelocity, Packet40EntityMetadata, 
//            DataWatcher, Packet20NamedEntitySpawn, EntityOtherPlayerMP, InventoryPlayer, 
//            Packet34EntityTeleport, Packet30Entity, Packet29DestroyEntity, EntityPlayer, 
//            Packet10Flying, AxisAlignedBB, Packet50PreChunk, Packet52MultiBlockChange, 
//            Chunk, Packet51MapChunk, Packet53BlockChange, GuiConnectFailed, 
//            Packet255KickDisconnect, Packet22Collect, EntityPickupFX, EffectRenderer, 
//            Packet3Chat, GuiIngame, Packet18Animation, Packet17Sleep, 
//            Packet2Handshake, Session, Packet24MobSpawn, EntityList, 
//            Packet4UpdateTime, World, ChunkCoordinates, Packet6SpawnPosition, 
//            WorldInfo, Packet39AttachEntity, Packet38EntityStatus, Packet8UpdateHealth, 
//            Packet9Respawn, Explosion, Packet60Explosion, Packet100OpenWindow, 
//            InventoryBasic, Container, TileEntityFurnace, TileEntityDispenser, 
//            MathHelper, Packet103SetSlot, Slot, Packet106Transaction, 
//            Packet104WindowItems, Packet130UpdateSign, TileEntitySign, Packet105UpdateProgressbar, 
//            Packet5PlayerInventory, Packet54PlayNoteBlock, Packet70Bed, Packet131MapData, 
//            Item, ItemMap, MapData, Packet61DoorChange, 
//            EntityClientPlayerMP, Packet200Statistic, Packet, Packet101CloseWindow

public class NetClientHandler extends NetHandler
{

    public NetClientHandler(Minecraft minecraft, String s, int i)
        throws UnknownHostException, IOException
    {
        disconnected = false;
        field_1210_g = false;
        field_28118_b = new MapStorage(null);
        rand = new Random();
        mc = minecraft;
        Socket socket = new Socket(InetAddress.getByName(s), i);
        netManager = new NetworkManager(socket, "Client", this);
    }

    public void processReadPackets()
    {
        if(!disconnected)
        {
            netManager.processReadPackets();
        }
        netManager.wakeThreads();
    }

    public void handleLogin(Packet1Login packet1login)
    {
        mc.playerController = new PlayerControllerMP(mc, this);
        mc.statFileWriter.readStat(StatList.joinMultiplayerStat, 1);
        worldClient = new WorldClient(this, packet1login.mapSeed, packet1login.dimension);
        worldClient.multiplayerWorld = true;
        mc.changeWorld1(worldClient);
        mc.thePlayer.dimension = packet1login.dimension;
        mc.displayGuiScreen(new GuiDownloadTerrain(this));
        mc.thePlayer.entityId = packet1login.protocolVersion;
    }

    public void handlePickupSpawn(Packet21PickupSpawn packet21pickupspawn)
    {
        double d = (double)packet21pickupspawn.xPosition / 32D;
        double d1 = (double)packet21pickupspawn.yPosition / 32D;
        double d2 = (double)packet21pickupspawn.zPosition / 32D;
        EntityItem entityitem = new EntityItem(worldClient, d, d1, d2, new ItemStack(packet21pickupspawn.itemID, packet21pickupspawn.count, packet21pickupspawn.itemDamage));
        entityitem.motionX = (double)packet21pickupspawn.rotation / 128D;
        entityitem.motionY = (double)packet21pickupspawn.pitch / 128D;
        entityitem.motionZ = (double)packet21pickupspawn.roll / 128D;
        entityitem.serverPosX = packet21pickupspawn.xPosition;
        entityitem.serverPosY = packet21pickupspawn.yPosition;
        entityitem.serverPosZ = packet21pickupspawn.zPosition;
        worldClient.func_712_a(packet21pickupspawn.entityId, entityitem);
    }

    public void handleVehicleSpawn(Packet23VehicleSpawn packet23vehiclespawn)
    {
        double d = (double)packet23vehiclespawn.xPosition / 32D;
        double d1 = (double)packet23vehiclespawn.yPosition / 32D;
        double d2 = (double)packet23vehiclespawn.zPosition / 32D;
        Entity obj = null;
        if(packet23vehiclespawn.type == 10)
        {
            obj = new EntityMinecart(worldClient, d, d1, d2, 0);
        }
        if(packet23vehiclespawn.type == 11)
        {
            obj = new EntityMinecart(worldClient, d, d1, d2, 1);
        }
        if(packet23vehiclespawn.type == 12)
        {
            obj = new EntityMinecart(worldClient, d, d1, d2, 2);
        }
        if(packet23vehiclespawn.type == 90)
        {
            obj = new EntityFish(worldClient, d, d1, d2);
        }
        if(packet23vehiclespawn.type == 60)
        {
            obj = new EntityArrow(worldClient, d, d1, d2);
        }
        if(packet23vehiclespawn.type == 61)
        {
            obj = new EntitySnowball(worldClient, d, d1, d2);
        }
        if(packet23vehiclespawn.type == 63)
        {
            obj = new EntityFireball(worldClient, d, d1, d2, (double)packet23vehiclespawn.field_28047_e / 8000D, (double)packet23vehiclespawn.field_28046_f / 8000D, (double)packet23vehiclespawn.field_28045_g / 8000D);
            packet23vehiclespawn.field_28044_i = 0;
        }
        if(packet23vehiclespawn.type == 62)
        {
            obj = new EntityEgg(worldClient, d, d1, d2);
        }
        if(packet23vehiclespawn.type == 1)
        {
            obj = new EntityBoat(worldClient, d, d1, d2);
        }
        if(packet23vehiclespawn.type == 50)
        {
            obj = new EntityTNTPrimed(worldClient, d, d1, d2);
        }
        if(packet23vehiclespawn.type == 70)
        {
            obj = new EntityFallingSand(worldClient, d, d1, d2, Block.sand.blockID);
        }
        if(packet23vehiclespawn.type == 71)
        {
            obj = new EntityFallingSand(worldClient, d, d1, d2, Block.gravel.blockID);
        }
        if(obj != null)
        {
            obj.serverPosX = packet23vehiclespawn.xPosition;
            obj.serverPosY = packet23vehiclespawn.yPosition;
            obj.serverPosZ = packet23vehiclespawn.zPosition;
            obj.rotationYaw = 0.0F;
            obj.rotationPitch = 0.0F;
            obj.entityId = packet23vehiclespawn.entityId;
            worldClient.func_712_a(packet23vehiclespawn.entityId, ((Entity) (obj)));
            if(packet23vehiclespawn.field_28044_i > 0)
            {
                if(packet23vehiclespawn.type == 60)
                {
                    Entity entity = getEntityByID(packet23vehiclespawn.field_28044_i);
                    if(entity instanceof EntityLiving)
                    {
                        ((EntityArrow)obj).owner = (EntityLiving)entity;
                    }
                }
                ((Entity) (obj)).setVelocity((double)packet23vehiclespawn.field_28047_e / 8000D, (double)packet23vehiclespawn.field_28046_f / 8000D, (double)packet23vehiclespawn.field_28045_g / 8000D);
            }
        }
    }

    public void handleWeather(Packet71Weather packet71weather)
    {
        double d = (double)packet71weather.field_27053_b / 32D;
        double d1 = (double)packet71weather.field_27057_c / 32D;
        double d2 = (double)packet71weather.field_27056_d / 32D;
        EntityLightningBolt entitylightningbolt = null;
        if(packet71weather.field_27055_e == 1)
        {
            entitylightningbolt = new EntityLightningBolt(worldClient, d, d1, d2);
        }
        if(entitylightningbolt != null)
        {
            entitylightningbolt.serverPosX = packet71weather.field_27053_b;
            entitylightningbolt.serverPosY = packet71weather.field_27057_c;
            entitylightningbolt.serverPosZ = packet71weather.field_27056_d;
            entitylightningbolt.rotationYaw = 0.0F;
            entitylightningbolt.rotationPitch = 0.0F;
            entitylightningbolt.entityId = packet71weather.field_27054_a;
            worldClient.addWeatherEffect(entitylightningbolt);
        }
    }

    public void func_21146_a(Packet25EntityPainting packet25entitypainting)
    {
        EntityPainting entitypainting = new EntityPainting(worldClient, packet25entitypainting.xPosition, packet25entitypainting.yPosition, packet25entitypainting.zPosition, packet25entitypainting.direction, packet25entitypainting.title);
        worldClient.func_712_a(packet25entitypainting.entityId, entitypainting);
    }

    public void func_6498_a(Packet28EntityVelocity packet28entityvelocity)
    {
        Entity entity = getEntityByID(packet28entityvelocity.entityId);
        if(entity == null)
        {
            return;
        } else
        {
            entity.setVelocity((double)packet28entityvelocity.motionX / 8000D, (double)packet28entityvelocity.motionY / 8000D, (double)packet28entityvelocity.motionZ / 8000D);
            return;
        }
    }

    public void func_21148_a(Packet40EntityMetadata packet40entitymetadata)
    {
        Entity entity = getEntityByID(packet40entitymetadata.entityId);
        if(entity != null && packet40entitymetadata.func_21047_b() != null)
        {
            entity.getDataWatcher().updateWatchedObjectsFromList(packet40entitymetadata.func_21047_b());
        }
    }

    public void handleNamedEntitySpawn(Packet20NamedEntitySpawn packet20namedentityspawn)
    {
        double d = (double)packet20namedentityspawn.xPosition / 32D;
        double d1 = (double)packet20namedentityspawn.yPosition / 32D;
        double d2 = (double)packet20namedentityspawn.zPosition / 32D;
        float f = (float)(packet20namedentityspawn.rotation * 360) / 256F;
        float f1 = (float)(packet20namedentityspawn.pitch * 360) / 256F;
        EntityOtherPlayerMP entityotherplayermp = new EntityOtherPlayerMP(mc.theWorld, packet20namedentityspawn.name);
        entityotherplayermp.prevPosX = entityotherplayermp.lastTickPosX = entityotherplayermp.serverPosX = packet20namedentityspawn.xPosition;
        entityotherplayermp.prevPosY = entityotherplayermp.lastTickPosY = entityotherplayermp.serverPosY = packet20namedentityspawn.yPosition;
        entityotherplayermp.prevPosZ = entityotherplayermp.lastTickPosZ = entityotherplayermp.serverPosZ = packet20namedentityspawn.zPosition;
        int i = packet20namedentityspawn.currentItem;
        if(i == 0)
        {
            entityotherplayermp.inventory.mainInventory[entityotherplayermp.inventory.currentItem] = null;
        } else
        {
            entityotherplayermp.inventory.mainInventory[entityotherplayermp.inventory.currentItem] = new ItemStack(i, 1, 0);
        }
        entityotherplayermp.setPositionAndRotation(d, d1, d2, f, f1);
        worldClient.func_712_a(packet20namedentityspawn.entityId, entityotherplayermp);
    }

    public void handleEntityTeleport(Packet34EntityTeleport packet34entityteleport)
    {
        Entity entity = getEntityByID(packet34entityteleport.entityId);
        if(entity == null)
        {
            return;
        } else
        {
            entity.serverPosX = packet34entityteleport.xPosition;
            entity.serverPosY = packet34entityteleport.yPosition;
            entity.serverPosZ = packet34entityteleport.zPosition;
            double d = (double)entity.serverPosX / 32D;
            double d1 = (double)entity.serverPosY / 32D + 0.015625D;
            double d2 = (double)entity.serverPosZ / 32D;
            float f = (float)(packet34entityteleport.yaw * 360) / 256F;
            float f1 = (float)(packet34entityteleport.pitch * 360) / 256F;
            entity.setPositionAndRotation2(d, d1, d2, f, f1, 3);
            return;
        }
    }

    public void handleEntity(Packet30Entity packet30entity)
    {
        Entity entity = getEntityByID(packet30entity.entityId);
        if(entity == null)
        {
            return;
        } else
        {
            entity.serverPosX += packet30entity.xPosition;
            entity.serverPosY += packet30entity.yPosition;
            entity.serverPosZ += packet30entity.zPosition;
            double d = (double)entity.serverPosX / 32D;
            double d1 = (double)entity.serverPosY / 32D;
            double d2 = (double)entity.serverPosZ / 32D;
            float f = packet30entity.rotating ? (float)(packet30entity.yaw * 360) / 256F : entity.rotationYaw;
            float f1 = packet30entity.rotating ? (float)(packet30entity.pitch * 360) / 256F : entity.rotationPitch;
            entity.setPositionAndRotation2(d, d1, d2, f, f1, 3);
            return;
        }
    }

    public void handleDestroyEntity(Packet29DestroyEntity packet29destroyentity)
    {
        worldClient.removeEntityFromWorld(packet29destroyentity.entityId);
    }

    public void handleFlying(Packet10Flying packet10flying)
    {
        EntityPlayerSP entityplayersp = mc.thePlayer;
        double d = ((EntityPlayer) (entityplayersp)).posX;
        double d1 = ((EntityPlayer) (entityplayersp)).posY;
        double d2 = ((EntityPlayer) (entityplayersp)).posZ;
        float f = ((EntityPlayer) (entityplayersp)).rotationYaw;
        float f1 = ((EntityPlayer) (entityplayersp)).rotationPitch;
        if(packet10flying.moving)
        {
            d = packet10flying.xPosition;
            d1 = packet10flying.yPosition;
            d2 = packet10flying.zPosition;
        }
        if(packet10flying.rotating)
        {
            f = packet10flying.yaw;
            f1 = packet10flying.pitch;
        }
        entityplayersp.ySize = 0.0F;
        entityplayersp.motionX = entityplayersp.motionY = entityplayersp.motionZ = 0.0D;
        entityplayersp.setPositionAndRotation(d, d1, d2, f, f1);
        packet10flying.xPosition = ((EntityPlayer) (entityplayersp)).posX;
        packet10flying.yPosition = ((EntityPlayer) (entityplayersp)).boundingBox.minY;
        packet10flying.zPosition = ((EntityPlayer) (entityplayersp)).posZ;
        packet10flying.stance = ((EntityPlayer) (entityplayersp)).posY;
        netManager.addToSendQueue(packet10flying);
        if(!field_1210_g)
        {
            mc.thePlayer.prevPosX = mc.thePlayer.posX;
            mc.thePlayer.prevPosY = mc.thePlayer.posY;
            mc.thePlayer.prevPosZ = mc.thePlayer.posZ;
            field_1210_g = true;
            mc.displayGuiScreen(null);
        }
    }

    public void handlePreChunk(Packet50PreChunk packet50prechunk)
    {
        worldClient.doPreChunk(packet50prechunk.xPosition, packet50prechunk.yPosition, packet50prechunk.mode);
    }

    public void handleMultiBlockChange(Packet52MultiBlockChange packet52multiblockchange)
    {
        Chunk chunk = worldClient.getChunkFromChunkCoords(packet52multiblockchange.xPosition, packet52multiblockchange.zPosition);
        int i = packet52multiblockchange.xPosition * 16;
        int j = packet52multiblockchange.zPosition * 16;
        for(int k = 0; k < packet52multiblockchange.size; k++)
        {
            short word0 = packet52multiblockchange.coordinateArray[k];
            int l = packet52multiblockchange.typeArray[k] & 0xff;
            byte byte0 = packet52multiblockchange.metadataArray[k];
            int i1 = word0 >> 12 & 0xf;
            int j1 = word0 >> 8 & 0xf;
            int k1 = word0 & 0xff;
            chunk.setBlockIDWithMetadata(i1, k1, j1, l, byte0);
            worldClient.invalidateBlockReceiveRegion(i1 + i, k1, j1 + j, i1 + i, k1, j1 + j);
            worldClient.markBlocksDirty(i1 + i, k1, j1 + j, i1 + i, k1, j1 + j);
        }

    }

    public void handleMapChunk(Packet51MapChunk packet51mapchunk)
    {
        worldClient.invalidateBlockReceiveRegion(packet51mapchunk.xPosition, packet51mapchunk.yPosition, packet51mapchunk.zPosition, (packet51mapchunk.xPosition + packet51mapchunk.xSize) - 1, (packet51mapchunk.yPosition + packet51mapchunk.ySize) - 1, (packet51mapchunk.zPosition + packet51mapchunk.zSize) - 1);
        worldClient.setChunkData(packet51mapchunk.xPosition, packet51mapchunk.yPosition, packet51mapchunk.zPosition, packet51mapchunk.xSize, packet51mapchunk.ySize, packet51mapchunk.zSize, packet51mapchunk.chunk);
    }

    public void handleBlockChange(Packet53BlockChange packet53blockchange)
    {
        worldClient.setBlockAndMetadataAndInvalidate(packet53blockchange.xPosition, packet53blockchange.yPosition, packet53blockchange.zPosition, packet53blockchange.type, packet53blockchange.metadata);
    }

    public void handleKickDisconnect(Packet255KickDisconnect packet255kickdisconnect)
    {
        netManager.networkShutdown("disconnect.kicked", new Object[0]);
        disconnected = true;
        mc.changeWorld1(null);
        mc.displayGuiScreen(new GuiConnectFailed("disconnect.disconnected", "disconnect.genericReason", new Object[] {
            packet255kickdisconnect.reason
        }));
    }

    public void handleErrorMessage(String s, Object aobj[])
    {
        if(disconnected)
        {
            return;
        } else
        {
            disconnected = true;
            mc.changeWorld1(null);
            mc.displayGuiScreen(new GuiConnectFailed("disconnect.lost", s, aobj));
            return;
        }
    }

    public void func_28117_a(Packet packet)
    {
        if(disconnected)
        {
            return;
        } else
        {
            netManager.addToSendQueue(packet);
            netManager.func_28142_c();
            return;
        }
    }

    public void addToSendQueue(Packet packet)
    {
        if(disconnected)
        {
            return;
        } else
        {
            netManager.addToSendQueue(packet);
            return;
        }
    }

    public void handleCollect(Packet22Collect packet22collect)
    {
        Entity entity = getEntityByID(packet22collect.collectedEntityId);
        Object obj = (EntityLiving)getEntityByID(packet22collect.collectorEntityId);
        if(obj == null)
        {
            obj = mc.thePlayer;
        }
        if(entity != null)
        {
            worldClient.playSoundAtEntity(entity, "random.pop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            mc.effectRenderer.addEffect(new EntityPickupFX(mc.theWorld, entity, ((Entity) (obj)), -0.5F));
            worldClient.removeEntityFromWorld(packet22collect.collectedEntityId);
        }
    }

    public void handleChat(Packet3Chat packet3chat)
    {
        mc.ingameGUI.addChatMessage(packet3chat.message);
    }

    public void handleArmAnimation(Packet18Animation packet18animation)
    {
        Entity entity = getEntityByID(packet18animation.entityId);
        if(entity == null)
        {
            return;
        }
        if(packet18animation.animate == 1)
        {
            EntityPlayer entityplayer = (EntityPlayer)entity;
            entityplayer.swingItem();
        } else
        if(packet18animation.animate == 2)
        {
            entity.performHurtAnimation();
        } else
        if(packet18animation.animate == 3)
        {
            EntityPlayer entityplayer1 = (EntityPlayer)entity;
            entityplayer1.wakeUpPlayer(false, false, false);
        } else
        if(packet18animation.animate == 4)
        {
            EntityPlayer entityplayer2 = (EntityPlayer)entity;
            entityplayer2.func_6420_o();
        }
    }

    public void handleSleep(Packet17Sleep packet17sleep)
    {
        Entity entity = getEntityByID(packet17sleep.entityID);
        if(entity == null)
        {
            return;
        }
        if(packet17sleep.field_22046_e == 0)
        {
            EntityPlayer entityplayer = (EntityPlayer)entity;
            entityplayer.sleepInBedAt(packet17sleep.bedX, packet17sleep.bedY, packet17sleep.bedZ);
        }
    }

    public void handleHandshake(Packet2Handshake packet2handshake)
    {
        if(packet2handshake.username.equals("-"))
        {
            addToSendQueue(new Packet1Login(mc.session.username, 14));
        } else
        {
            try
            {
                URL url = new URL((new StringBuilder()).append("http://www.minecraft.net/game/joinserver.jsp?user=").append(mc.session.username).append("&sessionId=").append(mc.session.sessionId).append("&serverId=").append(packet2handshake.username).toString());
                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(url.openStream()));
                String s = bufferedreader.readLine();
                bufferedreader.close();
                if(s.equalsIgnoreCase("ok"))
                {
                    addToSendQueue(new Packet1Login(mc.session.username, 14));
                } else
                {
                    netManager.networkShutdown("disconnect.loginFailedInfo", new Object[] {
                        s
                    });
                }
            }
            catch(Exception exception)
            {
                exception.printStackTrace();
                netManager.networkShutdown("disconnect.genericReason", new Object[] {
                    (new StringBuilder()).append("Internal client error: ").append(exception.toString()).toString()
                });
            }
        }
    }

    public void disconnect()
    {
        disconnected = true;
        netManager.wakeThreads();
        netManager.networkShutdown("disconnect.closed", new Object[0]);
    }

    public void handleMobSpawn(Packet24MobSpawn packet24mobspawn)
    {
        double d = (double)packet24mobspawn.xPosition / 32D;
        double d1 = (double)packet24mobspawn.yPosition / 32D;
        double d2 = (double)packet24mobspawn.zPosition / 32D;
        float f = (float)(packet24mobspawn.yaw * 360) / 256F;
        float f1 = (float)(packet24mobspawn.pitch * 360) / 256F;
        EntityLiving entityliving = (EntityLiving)EntityList.createEntity(packet24mobspawn.type, mc.theWorld);
        entityliving.serverPosX = packet24mobspawn.xPosition;
        entityliving.serverPosY = packet24mobspawn.yPosition;
        entityliving.serverPosZ = packet24mobspawn.zPosition;
        entityliving.entityId = packet24mobspawn.entityId;
        entityliving.setPositionAndRotation(d, d1, d2, f, f1);
        entityliving.isMultiplayerEntity = true;
        worldClient.func_712_a(packet24mobspawn.entityId, entityliving);
        java.util.List list = packet24mobspawn.getMetadata();
        if(list != null)
        {
            entityliving.getDataWatcher().updateWatchedObjectsFromList(list);
        }
    }

    public void handleUpdateTime(Packet4UpdateTime packet4updatetime)
    {
        mc.theWorld.setWorldTime(packet4updatetime.time);
    }

    public void handleSpawnPosition(Packet6SpawnPosition packet6spawnposition)
    {
        mc.thePlayer.setPlayerSpawnCoordinate(new ChunkCoordinates(packet6spawnposition.xPosition, packet6spawnposition.yPosition, packet6spawnposition.zPosition));
        mc.theWorld.getWorldInfo().setSpawn(packet6spawnposition.xPosition, packet6spawnposition.yPosition, packet6spawnposition.zPosition);
    }

    public void func_6497_a(Packet39AttachEntity packet39attachentity)
    {
        Object obj = getEntityByID(packet39attachentity.entityId);
        Entity entity = getEntityByID(packet39attachentity.vehicleEntityId);
        if(packet39attachentity.entityId == mc.thePlayer.entityId)
        {
            obj = mc.thePlayer;
        }
        if(obj == null)
        {
            return;
        } else
        {
            ((Entity) (obj)).mountEntity(entity);
            return;
        }
    }

    public void func_9447_a(Packet38EntityStatus packet38entitystatus)
    {
        Entity entity = getEntityByID(packet38entitystatus.entityId);
        if(entity != null)
        {
            entity.handleHealthUpdate(packet38entitystatus.entityStatus);
        }
    }

    private Entity getEntityByID(int i)
    {
        if(i == mc.thePlayer.entityId)
        {
            return mc.thePlayer;
        } else
        {
            return worldClient.func_709_b(i);
        }
    }

    public void handleHealth(Packet8UpdateHealth packet8updatehealth)
    {
        mc.thePlayer.setHealth(packet8updatehealth.healthMP);
    }

    public void handleRespawn(Packet9Respawn packet9respawn)
    {
        if(packet9respawn.respawnDimension != mc.thePlayer.dimension)
        {
            field_1210_g = false;
            worldClient = new WorldClient(this, worldClient.getWorldInfo().getRandomSeed(), packet9respawn.respawnDimension);
            worldClient.multiplayerWorld = true;
            mc.changeWorld1(worldClient);
            mc.thePlayer.dimension = packet9respawn.respawnDimension;
            mc.displayGuiScreen(new GuiDownloadTerrain(this));
        }
        mc.respawn(true, packet9respawn.respawnDimension);
    }

    public void handleExplosion(Packet60Explosion packet60explosion)
    {
        Explosion explosion = new Explosion(mc.theWorld, null, packet60explosion.explosionX, packet60explosion.explosionY, packet60explosion.explosionZ, packet60explosion.explosionSize);
        explosion.destroyedBlockPositions = packet60explosion.destroyedBlockPositions;
        explosion.doExplosionB(true);
    }

    public void handleOpenWindow(Packet100OpenWindow packet100openwindow)
    {
    	Packet15Place ocp = worldClient.openContainerPacket;
        if(packet100openwindow.inventoryType == 0)
        {
        	IInventory inventory;
        	if(worldClient.downloadThisWorld && ocp != null && ocp.isID(Block.chest.blockID) )
        	{
        		inventory = BlockChest.buildEntity(worldClient, ocp.xPosition, ocp.yPosition, ocp.zPosition, packet100openwindow.slotsCount);
        	}
        	else
        	{
        		inventory = new InventoryBasic(packet100openwindow.windowTitle, packet100openwindow.slotsCount);
        	}
        	mc.thePlayer.displayGUIChest(inventory);
            mc.thePlayer.craftingInventory.windowId = packet100openwindow.windowId;
        } else
        if(packet100openwindow.inventoryType == 2)
        {
            TileEntityFurnace tileentityfurnace = new TileEntityFurnace();
            if(worldClient.downloadThisWorld && ocp != null && (ocp.isID(Block.stoneOvenIdle.blockID) || ocp.isID(Block.stoneOvenActive.blockID)))
            {
            	worldClient.setNewBlockTileEntity(ocp.xPosition, ocp.yPosition, ocp.zPosition, tileentityfurnace);
            }
            mc.thePlayer.displayGUIFurnace(tileentityfurnace);
            mc.thePlayer.craftingInventory.windowId = packet100openwindow.windowId;
        } else
        if(packet100openwindow.inventoryType == 3)
        {
            TileEntityDispenser tileentitydispenser = new TileEntityDispenser();
            if(worldClient.downloadThisWorld && ocp != null && ocp.isID(Block.dispenser.blockID) )
            {
            	worldClient.setNewBlockTileEntity(ocp.xPosition, ocp.yPosition, ocp.zPosition, tileentitydispenser);
            }
            mc.thePlayer.displayGUIDispenser(tileentitydispenser);
            mc.thePlayer.craftingInventory.windowId = packet100openwindow.windowId;
        } else
        if(packet100openwindow.inventoryType == 1)
        {
            EntityPlayerSP entityplayersp = mc.thePlayer;
            mc.thePlayer.displayWorkbenchGUI(MathHelper.floor_double(((EntityPlayer) (entityplayersp)).posX), MathHelper.floor_double(((EntityPlayer) (entityplayersp)).posY), MathHelper.floor_double(((EntityPlayer) (entityplayersp)).posZ));
            mc.thePlayer.craftingInventory.windowId = packet100openwindow.windowId;
        }
    }

    public void handleSetSlot(Packet103SetSlot packet103setslot)
    {
        if(packet103setslot.windowId == -1)
        {
            mc.thePlayer.inventory.setItemStack(packet103setslot.myItemStack);
        } else
        if(packet103setslot.windowId == 0 && packet103setslot.itemSlot >= 36 && packet103setslot.itemSlot < 45)
        {
            ItemStack itemstack = mc.thePlayer.inventorySlots.getSlot(packet103setslot.itemSlot).getStack();
            if(packet103setslot.myItemStack != null && (itemstack == null || itemstack.stackSize < packet103setslot.myItemStack.stackSize))
            {
                packet103setslot.myItemStack.animationsToGo = 5;
            }
            mc.thePlayer.inventorySlots.putStackInSlot(packet103setslot.itemSlot, packet103setslot.myItemStack);
        } else
        if(packet103setslot.windowId == mc.thePlayer.craftingInventory.windowId)
        {
            mc.thePlayer.craftingInventory.putStackInSlot(packet103setslot.itemSlot, packet103setslot.myItemStack);
        }
    }

    public void handleContainerTransaction(Packet106Transaction packet106transaction)
    {
        Container container = null;
        if(packet106transaction.windowId == 0)
        {
            container = mc.thePlayer.inventorySlots;
        } else
        if(packet106transaction.windowId == mc.thePlayer.craftingInventory.windowId)
        {
            container = mc.thePlayer.craftingInventory;
        }
        if(container != null)
        {
            if(packet106transaction.field_20030_c)
            {
                container.func_20113_a(packet106transaction.field_20028_b);
            } else
            {
                container.func_20110_b(packet106transaction.field_20028_b);
                addToSendQueue(new Packet106Transaction(packet106transaction.windowId, packet106transaction.field_20028_b, true));
            }
        }
    }

    public void handleWindowItems(Packet104WindowItems packet104windowitems)
    {
        if(packet104windowitems.windowId == 0)
        {
            mc.thePlayer.inventorySlots.putStacksInSlots(packet104windowitems.itemStack);
        } else
        if(packet104windowitems.windowId == mc.thePlayer.craftingInventory.windowId)
        {
            mc.thePlayer.craftingInventory.putStacksInSlots(packet104windowitems.itemStack);
        }
    }

    public void handleSignUpdate(Packet130UpdateSign packet130updatesign)
    {
        if(mc.theWorld.blockExists(packet130updatesign.xPosition, packet130updatesign.yPosition, packet130updatesign.zPosition))
        {
            TileEntity tileentity = mc.theWorld.getBlockTileEntity(packet130updatesign.xPosition, packet130updatesign.yPosition, packet130updatesign.zPosition);
            if(tileentity instanceof TileEntitySign)
            {
                TileEntitySign tileentitysign = (TileEntitySign)tileentity;
                for(int i = 0; i < 4; i++)
                {
                    tileentitysign.signText[i] = packet130updatesign.signLines[i];
                }

                tileentitysign.onInventoryChanged();
            }
        }
    }

    public void handleCraftingProgress(Packet105UpdateProgressbar packet105updateprogressbar)
    {
        registerPacket(packet105updateprogressbar);
        if(mc.thePlayer.craftingInventory != null && mc.thePlayer.craftingInventory.windowId == packet105updateprogressbar.windowId)
        {
            mc.thePlayer.craftingInventory.func_20112_a(packet105updateprogressbar.progressBar, packet105updateprogressbar.progressBarValue);
        }
    }

    public void handlePlayerInventory(Packet5PlayerInventory packet5playerinventory)
    {
        Entity entity = getEntityByID(packet5playerinventory.entityID);
        if(entity != null)
        {
            entity.outfitWithItem(packet5playerinventory.slot, packet5playerinventory.itemID, packet5playerinventory.itemDamage);
        }
    }

    public void handleCloseWindow(Packet101CloseWindow packet101closewindow)
    {
        mc.thePlayer.closeScreen();
    }

    public void handleNotePlay(Packet54PlayNoteBlock packet54playnoteblock)
    {
        mc.theWorld.playNoteAt(packet54playnoteblock.xLocation, packet54playnoteblock.yLocation, packet54playnoteblock.zLocation, packet54playnoteblock.instrumentType, packet54playnoteblock.pitch);
    }

    public void handleBedUpdate(Packet70Bed packet70bed)
    {
        int i = packet70bed.bedState;
        if(i >= 0 && i < Packet70Bed.bedChat.length && Packet70Bed.bedChat[i] != null)
        {
            mc.thePlayer.addChatMessage(Packet70Bed.bedChat[i]);
        }
        if(i == 1)
        {
            worldClient.getWorldInfo().setRaining(true);
            worldClient.setRainStrength(1.0F);
        } else
        if(i == 2)
        {
            worldClient.getWorldInfo().setRaining(false);
            worldClient.setRainStrength(0.0F);
        }
    }

    public void processItemData(Packet131MapData packet131mapdata)
    {
        if(packet131mapdata.itemID == Item.mapItem.shiftedIndex)
        {
            ItemMap.func_28013_a(packet131mapdata.uniqueID, mc.theWorld).func_28171_a(packet131mapdata.itemData);
        } else
        {
            System.out.println((new StringBuilder()).append("Unknown itemid: ").append(packet131mapdata.uniqueID).toString());
        }
    }

    public void handleAuxSFX(Packet61DoorChange packet61doorchange)
    {
        mc.theWorld.playAuxSFX(packet61doorchange.sfxID, packet61doorchange.posX, packet61doorchange.posY, packet61doorchange.posZ, packet61doorchange.auxData);
    }

    public void func_27245_a(Packet200Statistic packet200statistic)
    {
        ((EntityClientPlayerMP)mc.thePlayer).func_27027_b(StatList.func_27361_a(packet200statistic.field_27052_a), packet200statistic.field_27051_b);
    }

    public boolean isServerHandler()
    {
        return false;
    }

    private boolean disconnected;
    private NetworkManager netManager;
    public String field_1209_a;
    private Minecraft mc;
    private WorldClient worldClient;
    private boolean field_1210_g;
    public MapStorage field_28118_b;
    Random rand;
}
