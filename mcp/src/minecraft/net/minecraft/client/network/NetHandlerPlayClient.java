package net.minecraft.client.network;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
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
import net.minecraft.client.gui.GuiScreenRealmsProxy;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.IProgressMeter;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityCrit2FX;
import net.minecraft.client.particle.EntityPickupFX;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.stream.MetadataAchievement;
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
import net.minecraft.realms.DisconnectedOnlineScreen;
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

public class NetHandlerPlayClient implements INetHandlerPlayClient {
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
	 * True if the client has finished downloading terrain and may spawn. Set upon receipt of S08PacketPlayerPosLook, reset
	 * upon respawning
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
	 * Seems to be either null (integrated server) or an instance of either GuiMultiplayer (when connecting to a server) or
	 * GuiScreenReamlsTOS (when connecting to MCO server)
	 */
	private GuiScreen guiScreenServer;
	private boolean field_147308_k = false;

	/**
	 * Just an ordinary random number generator, used to randomize audio pitch of item/orb pickup and randomize both
	 * particlespawn offset and velocity
	 */
	private Random avRandomizer = new Random();
	private static final String __OBFID = "CL_00000878";

	public NetHandlerPlayClient(Minecraft p_i45061_1_, GuiScreen p_i45061_2_, NetworkManager p_i45061_3_) {
		this.gameController = p_i45061_1_;
		this.guiScreenServer = p_i45061_2_;
		this.netManager = p_i45061_3_;
	}

	/**
	 * Clears the WorldClient instance associated with this NetHandlerPlayClient
	 */
	public void cleanup() {
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
	public void handleJoinGame(S01PacketJoinGame packetIn) {
		this.gameController.playerController = new PlayerControllerMP(this.gameController, this);
		this.clientWorldController = new WorldClient(this, new WorldSettings(0L, packetIn.func_149198_e(), false, packetIn.func_149195_d(), packetIn.func_149196_i()), packetIn.func_149194_f(), packetIn.func_149192_g(), this.gameController.mcProfiler);
		this.clientWorldController.isRemote = true;
		this.gameController.loadWorld(this.clientWorldController);
		this.gameController.thePlayer.dimension = packetIn.func_149194_f();
		this.gameController.displayGuiScreen(new GuiDownloadTerrain(this));
		this.gameController.thePlayer.setEntityId(packetIn.func_149197_c());
		this.currentServerMaxPlayers = packetIn.func_149193_h();
		this.gameController.playerController.setGameType(packetIn.func_149198_e());
		this.gameController.gameSettings.sendSettingsToServer();
		this.netManager.scheduleOutboundPacket(new C17PacketCustomPayload("MC|Brand", ClientBrandRetriever.getClientModName().getBytes(Charsets.UTF_8)), new GenericFutureListener[0]);
	}

	/**
	 * Spawns an instance of the objecttype indicated by the packet and sets its position and momentum
	 */
	public void handleSpawnObject(S0EPacketSpawnObject packetIn) {
		double var2 = (double)packetIn.func_148997_d() / 32.0D;
		double var4 = (double)packetIn.func_148998_e() / 32.0D;
		double var6 = (double)packetIn.func_148994_f() / 32.0D;
		Object var8 = null;

		if (packetIn.func_148993_l() == 10) {
			var8 = EntityMinecart.createMinecart(this.clientWorldController, var2, var4, var6, packetIn.func_149009_m());
		} else if (packetIn.func_148993_l() == 90) {
			Entity var9 = this.clientWorldController.getEntityByID(packetIn.func_149009_m());

			if (var9 instanceof EntityPlayer) {
				var8 = new EntityFishHook(this.clientWorldController, var2, var4, var6, (EntityPlayer)var9);
			}

			packetIn.func_149002_g(0);
		} else if (packetIn.func_148993_l() == 60) {
			var8 = new EntityArrow(this.clientWorldController, var2, var4, var6);
		} else if (packetIn.func_148993_l() == 61) {
			var8 = new EntitySnowball(this.clientWorldController, var2, var4, var6);
		} else if (packetIn.func_148993_l() == 71) {
			var8 = new EntityItemFrame(this.clientWorldController, (int)var2, (int)var4, (int)var6, packetIn.func_149009_m());
			packetIn.func_149002_g(0);
		} else if (packetIn.func_148993_l() == 77) {
			var8 = new EntityLeashKnot(this.clientWorldController, (int)var2, (int)var4, (int)var6);
			packetIn.func_149002_g(0);
		} else if (packetIn.func_148993_l() == 65) {
			var8 = new EntityEnderPearl(this.clientWorldController, var2, var4, var6);
		} else if (packetIn.func_148993_l() == 72) {
			var8 = new EntityEnderEye(this.clientWorldController, var2, var4, var6);
		} else if (packetIn.func_148993_l() == 76) {
			var8 = new EntityFireworkRocket(this.clientWorldController, var2, var4, var6, (ItemStack)null);
		} else if (packetIn.func_148993_l() == 63) {
			var8 = new EntityLargeFireball(this.clientWorldController, var2, var4, var6, (double)packetIn.func_149010_g() / 8000.0D, (double)packetIn.func_149004_h() / 8000.0D, (double)packetIn.func_148999_i() / 8000.0D);
			packetIn.func_149002_g(0);
		} else if (packetIn.func_148993_l() == 64) {
			var8 = new EntitySmallFireball(this.clientWorldController, var2, var4, var6, (double)packetIn.func_149010_g() / 8000.0D, (double)packetIn.func_149004_h() / 8000.0D, (double)packetIn.func_148999_i() / 8000.0D);
			packetIn.func_149002_g(0);
		} else if (packetIn.func_148993_l() == 66) {
			var8 = new EntityWitherSkull(this.clientWorldController, var2, var4, var6, (double)packetIn.func_149010_g() / 8000.0D, (double)packetIn.func_149004_h() / 8000.0D, (double)packetIn.func_148999_i() / 8000.0D);
			packetIn.func_149002_g(0);
		} else if (packetIn.func_148993_l() == 62) {
			var8 = new EntityEgg(this.clientWorldController, var2, var4, var6);
		} else if (packetIn.func_148993_l() == 73) {
			var8 = new EntityPotion(this.clientWorldController, var2, var4, var6, packetIn.func_149009_m());
			packetIn.func_149002_g(0);
		} else if (packetIn.func_148993_l() == 75) {
			var8 = new EntityExpBottle(this.clientWorldController, var2, var4, var6);
			packetIn.func_149002_g(0);
		} else if (packetIn.func_148993_l() == 1) {
			var8 = new EntityBoat(this.clientWorldController, var2, var4, var6);
		} else if (packetIn.func_148993_l() == 50) {
			var8 = new EntityTNTPrimed(this.clientWorldController, var2, var4, var6, (EntityLivingBase)null);
		} else if (packetIn.func_148993_l() == 51) {
			var8 = new EntityEnderCrystal(this.clientWorldController, var2, var4, var6);
		} else if (packetIn.func_148993_l() == 2) {
			var8 = new EntityItem(this.clientWorldController, var2, var4, var6);
		} else if (packetIn.func_148993_l() == 70) {
			var8 = new EntityFallingBlock(this.clientWorldController, var2, var4, var6, Block.getBlockById(packetIn.func_149009_m() & 65535), packetIn.func_149009_m() >> 16);
			packetIn.func_149002_g(0);
		}

		if (var8 != null) {
			((Entity)var8).serverPosX = packetIn.func_148997_d();
			((Entity)var8).serverPosY = packetIn.func_148998_e();
			((Entity)var8).serverPosZ = packetIn.func_148994_f();
			((Entity)var8).rotationPitch = (float)(packetIn.func_149008_j() * 360) / 256.0F;
			((Entity)var8).rotationYaw = (float)(packetIn.func_149006_k() * 360) / 256.0F;
			Entity[] var12 = ((Entity)var8).getParts();

			if (var12 != null) {
				int var10 = packetIn.func_149001_c() - ((Entity)var8).getEntityId();

				for (int var11 = 0; var11 < var12.length; ++var11) {
					var12[var11].setEntityId(var12[var11].getEntityId() + var10);
				}
			}

			((Entity)var8).setEntityId(packetIn.func_149001_c());
			this.clientWorldController.addEntityToWorld(packetIn.func_149001_c(), (Entity)var8);

			if (packetIn.func_149009_m() > 0) {
				if (packetIn.func_148993_l() == 60) {
					Entity var13 = this.clientWorldController.getEntityByID(packetIn.func_149009_m());

					if (var13 instanceof EntityLivingBase) {
						EntityArrow var14 = (EntityArrow)var8;
						var14.shootingEntity = var13;
					}
				}

				((Entity)var8).setVelocity((double)packetIn.func_149010_g() / 8000.0D, (double)packetIn.func_149004_h() / 8000.0D, (double)packetIn.func_148999_i() / 8000.0D);
			}
		}
	}

	/**
	 * Spawns an experience orb and sets its value (amount of XP)
	 */
	public void handleSpawnExperienceOrb(S11PacketSpawnExperienceOrb packetIn) {
		EntityXPOrb var2 = new EntityXPOrb(this.clientWorldController, (double)packetIn.func_148984_d(), (double)packetIn.func_148983_e(), (double)packetIn.func_148982_f(), packetIn.func_148986_g());
		var2.serverPosX = packetIn.func_148984_d();
		var2.serverPosY = packetIn.func_148983_e();
		var2.serverPosZ = packetIn.func_148982_f();
		var2.rotationYaw = 0.0F;
		var2.rotationPitch = 0.0F;
		var2.setEntityId(packetIn.func_148985_c());
		this.clientWorldController.addEntityToWorld(packetIn.func_148985_c(), var2);
	}

	/**
	 * Handles globally visible entities. Used in vanilla for lightning bolts
	 */
	public void handleSpawnGlobalEntity(S2CPacketSpawnGlobalEntity packetIn) {
		double var2 = (double)packetIn.func_149051_d() / 32.0D;
		double var4 = (double)packetIn.func_149050_e() / 32.0D;
		double var6 = (double)packetIn.func_149049_f() / 32.0D;
		EntityLightningBolt var8 = null;

		if (packetIn.func_149053_g() == 1) {
			var8 = new EntityLightningBolt(this.clientWorldController, var2, var4, var6);
		}

		if (var8 != null) {
			var8.serverPosX = packetIn.func_149051_d();
			var8.serverPosY = packetIn.func_149050_e();
			var8.serverPosZ = packetIn.func_149049_f();
			var8.rotationYaw = 0.0F;
			var8.rotationPitch = 0.0F;
			var8.setEntityId(packetIn.func_149052_c());
			this.clientWorldController.addWeatherEffect(var8);
		}
	}

	/**
	 * Handles the spawning of a painting object
	 */
	public void handleSpawnPainting(S10PacketSpawnPainting packetIn) {
		EntityPainting var2 = new EntityPainting(this.clientWorldController, packetIn.func_148964_d(), packetIn.func_148963_e(), packetIn.func_148962_f(), packetIn.func_148966_g(), packetIn.func_148961_h());
		this.clientWorldController.addEntityToWorld(packetIn.func_148965_c(), var2);
	}

	/**
	 * Sets the velocity of the specified entity to the specified value
	 */
	public void handleEntityVelocity(S12PacketEntityVelocity packetIn) {
		Entity var2 = this.clientWorldController.getEntityByID(packetIn.func_149412_c());

		if (var2 != null) {
			var2.setVelocity((double)packetIn.func_149411_d() / 8000.0D, (double)packetIn.func_149410_e() / 8000.0D, (double)packetIn.func_149409_f() / 8000.0D);
		}
	}

	/**
	 * Invoked when the server registers new proximate objects in your watchlist or when objects in your watchlist have
	 * changed -> Registers any changes locally
	 */
	public void handleEntityMetadata(S1CPacketEntityMetadata packetIn) {
		Entity var2 = this.clientWorldController.getEntityByID(packetIn.func_149375_d());

		if (var2 != null && packetIn.func_149376_c() != null) {
			var2.getDataWatcher().updateWatchedObjectsFromList(packetIn.func_149376_c());
		}
	}

	/**
	 * Handles the creation of a nearby player entity, sets the position and held item
	 */
	public void handleSpawnPlayer(S0CPacketSpawnPlayer packetIn) {
		double var2 = (double)packetIn.func_148942_f() / 32.0D;
		double var4 = (double)packetIn.func_148949_g() / 32.0D;
		double var6 = (double)packetIn.func_148946_h() / 32.0D;
		float var8 = (float)(packetIn.func_148941_i() * 360) / 256.0F;
		float var9 = (float)(packetIn.func_148945_j() * 360) / 256.0F;
		GameProfile var10 = packetIn.func_148948_e();
		EntityOtherPlayerMP var11 = new EntityOtherPlayerMP(this.gameController.theWorld, packetIn.func_148948_e());
		var11.prevPosX = var11.lastTickPosX = (double)(var11.serverPosX = packetIn.func_148942_f());
		var11.prevPosY = var11.lastTickPosY = (double)(var11.serverPosY = packetIn.func_148949_g());
		var11.prevPosZ = var11.lastTickPosZ = (double)(var11.serverPosZ = packetIn.func_148946_h());
		int var12 = packetIn.func_148947_k();

		if (var12 == 0) {
			var11.inventory.mainInventory[var11.inventory.currentItem] = null;
		} else {
			var11.inventory.mainInventory[var11.inventory.currentItem] = new ItemStack(Item.getItemById(var12), 1, 0);
		}

		var11.setPositionAndRotation(var2, var4, var6, var8, var9);
		this.clientWorldController.addEntityToWorld(packetIn.func_148943_d(), var11);
		List var13 = packetIn.func_148944_c();

		if (var13 != null) {
			var11.getDataWatcher().updateWatchedObjectsFromList(var13);
		}
	}

	/**
	 * Updates an entity's position and rotation as specified by the packet
	 */
	public void handleEntityTeleport(S18PacketEntityTeleport packetIn) {
		Entity var2 = this.clientWorldController.getEntityByID(packetIn.func_149451_c());

		if (var2 != null) {
			var2.serverPosX = packetIn.func_149449_d();
			var2.serverPosY = packetIn.func_149448_e();
			var2.serverPosZ = packetIn.func_149446_f();
			double var3 = (double)var2.serverPosX / 32.0D;
			double var5 = (double)var2.serverPosY / 32.0D + 0.015625D;
			double var7 = (double)var2.serverPosZ / 32.0D;
			float var9 = (float)(packetIn.func_149450_g() * 360) / 256.0F;
			float var10 = (float)(packetIn.func_149447_h() * 360) / 256.0F;
			var2.setPositionAndRotation2(var3, var5, var7, var9, var10, 3);
		}
	}

	/**
	 * Updates which hotbar slot of the player is currently selected
	 */
	public void handleHeldItemChange(S09PacketHeldItemChange packetIn) {
		if (packetIn.func_149385_c() >= 0 && packetIn.func_149385_c() < InventoryPlayer.getHotbarSize()) {
			this.gameController.thePlayer.inventory.currentItem = packetIn.func_149385_c();
		}
	}

	/**
	 * Updates the specified entity's position by the specified relative moment and absolute rotation. Note that
	 * subclassing of the packet allows for the specification of a subset of this data (e.g. only rel. position, abs.
	 * rotation or both).
	 */
	public void handleEntityMovement(S14PacketEntity packetIn) {
		Entity var2 = packetIn.func_149065_a(this.clientWorldController);

		if (var2 != null) {
			var2.serverPosX += packetIn.func_149062_c();
			var2.serverPosY += packetIn.func_149061_d();
			var2.serverPosZ += packetIn.func_149064_e();
			double var3 = (double)var2.serverPosX / 32.0D;
			double var5 = (double)var2.serverPosY / 32.0D;
			double var7 = (double)var2.serverPosZ / 32.0D;
			float var9 = packetIn.func_149060_h() ? (float)(packetIn.func_149066_f() * 360) / 256.0F : var2.rotationYaw;
			float var10 = packetIn.func_149060_h() ? (float)(packetIn.func_149063_g() * 360) / 256.0F : var2.rotationPitch;
			var2.setPositionAndRotation2(var3, var5, var7, var9, var10, 3);
		}
	}

	/**
	 * Updates the direction in which the specified entity is looking, normally this head rotation is independent of the
	 * rotation of the entity itself
	 */
	public void handleEntityHeadLook(S19PacketEntityHeadLook packetIn) {
		Entity var2 = packetIn.func_149381_a(this.clientWorldController);

		if (var2 != null) {
			float var3 = (float)(packetIn.func_149380_c() * 360) / 256.0F;
			var2.setRotationYawHead(var3);
		}
	}

	/**
	 * Locally eliminates the entities. Invoked by the server when the items are in fact destroyed, or the player is no
	 * longer registered as required to monitor them. The latter  happens when distance between the player and item
	 * increases beyond a certain treshold (typically the viewing distance)
	 */
	public void handleDestroyEntities(S13PacketDestroyEntities packetIn) {
		for (int var2 = 0; var2 < packetIn.func_149098_c().length; ++var2) {
			this.clientWorldController.removeEntityFromWorld(packetIn.func_149098_c()[var2]);
		}
	}

	/**
	 * Handles changes in player positioning and rotation such as when travelling to a new dimension, (re)spawning,
	 * mounting horses etc. Seems to immediately reply to the server with the clients post-processing perspective on the
	 * player positioning
	 */
	public void handlePlayerPosLook(S08PacketPlayerPosLook packetIn) {
		EntityClientPlayerMP var2 = this.gameController.thePlayer;
		double var3 = packetIn.func_148932_c();
		double var5 = packetIn.func_148928_d();
		double var7 = packetIn.func_148933_e();
		float var9 = packetIn.func_148931_f();
		float var10 = packetIn.func_148930_g();
		var2.yOffset2 = 0.0F;
		var2.motionX = var2.motionY = var2.motionZ = 0.0D;
		var2.setPositionAndRotation(var3, var5, var7, var9, var10);
		this.netManager.scheduleOutboundPacket(new C03PacketPlayer.C06PacketPlayerPosLook(var2.posX, var2.boundingBox.minY, var2.posY, var2.posZ, packetIn.func_148931_f(), packetIn.func_148930_g(), packetIn.func_148929_h()), new GenericFutureListener[0]);

		if (!this.doneLoadingTerrain) {
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
	public void handleMultiBlockChange(S22PacketMultiBlockChange packetIn) {
		int var2 = packetIn.func_148920_c().chunkXPos * 16;
		int var3 = packetIn.func_148920_c().chunkZPos * 16;

		if (packetIn.func_148921_d() != null) {
			DataInputStream var4 = new DataInputStream(new ByteArrayInputStream(packetIn.func_148921_d()));

			try {
				for (int var5 = 0; var5 < packetIn.func_148922_e(); ++var5) {
					short var6 = var4.readShort();
					short var7 = var4.readShort();
					int var8 = var7 >> 4 & 4095;
					int var9 = var7 & 15;
					int var10 = var6 >> 12 & 15;
					int var11 = var6 >> 8 & 15;
					int var12 = var6 & 255;
					this.clientWorldController.func_147492_c(var10 + var2, var12, var11 + var3, Block.getBlockById(var8), var9);
				}
			} catch (IOException var13) {
				;
			}
		}
	}

	/**
	 * Updates the specified chunk with the supplied data, marks it for re-rendering and lighting recalculation
	 */
	public void handleChunkData(S21PacketChunkData packetIn) {
		if (packetIn.func_149274_i()) {
			if (packetIn.func_149276_g() == 0) {
				this.clientWorldController.doPreChunk(packetIn.func_149273_e(), packetIn.func_149271_f(), false);
				return;
			}

			this.clientWorldController.doPreChunk(packetIn.func_149273_e(), packetIn.func_149271_f(), true);
		}

		this.clientWorldController.invalidateBlockReceiveRegion(packetIn.func_149273_e() << 4, 0, packetIn.func_149271_f() << 4, (packetIn.func_149273_e() << 4) + 15, 256, (packetIn.func_149271_f() << 4) + 15);
		Chunk var2 = this.clientWorldController.getChunkFromChunkCoords(packetIn.func_149273_e(), packetIn.func_149271_f());
		var2.fillChunk(packetIn.func_149272_d(), packetIn.func_149276_g(), packetIn.func_149270_h(), packetIn.func_149274_i());
		this.clientWorldController.markBlockRangeForRenderUpdate(packetIn.func_149273_e() << 4, 0, packetIn.func_149271_f() << 4, (packetIn.func_149273_e() << 4) + 15, 256, (packetIn.func_149271_f() << 4) + 15);

		if (!packetIn.func_149274_i() || !(this.clientWorldController.provider instanceof WorldProviderSurface)) {
			var2.resetRelightChecks();
		}
	}

	/**
	 * Updates the block and metadata and generates a blockupdate (and notify the clients)
	 */
	public void handleBlockChange(S23PacketBlockChange packetIn) {
		this.clientWorldController.func_147492_c(packetIn.func_148879_d(), packetIn.func_148878_e(), packetIn.func_148877_f(), packetIn.func_148880_c(), packetIn.func_148881_g());
	}

	/**
	 * Closes the network channel
	 */
	public void handleDisconnect(S40PacketDisconnect packetIn) {
		this.netManager.closeChannel(packetIn.func_149165_c());
	}

	/**
	 * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
	 */
	public void onDisconnect(IChatComponent reason) {
		this.gameController.loadWorld((WorldClient)null);

		if (this.guiScreenServer != null) {
			if (this.guiScreenServer instanceof GuiScreenRealmsProxy) {
				this.gameController.displayGuiScreen((new DisconnectedOnlineScreen(((GuiScreenRealmsProxy)this.guiScreenServer).func_154321_a(), "disconnect.lost", reason)).getProxy());
			} else {
				this.gameController.displayGuiScreen(new GuiDisconnected(this.guiScreenServer, "disconnect.lost", reason));
			}
		} else {
			this.gameController.displayGuiScreen(new GuiDisconnected(new GuiMultiplayer(new GuiMainMenu()), "disconnect.lost", reason));
		}
	}

	public void addToSendQueue(Packet p_147297_1_) {
		this.netManager.scheduleOutboundPacket(p_147297_1_, new GenericFutureListener[0]);
	}

	public void handleCollectItem(S0DPacketCollectItem packetIn) {
		Entity var2 = this.clientWorldController.getEntityByID(packetIn.func_149354_c());
		Object var3 = (EntityLivingBase)this.clientWorldController.getEntityByID(packetIn.func_149353_d());

		if (var3 == null) {
			var3 = this.gameController.thePlayer;
		}

		if (var2 != null) {
			if (var2 instanceof EntityXPOrb) {
				this.clientWorldController.playSoundAtEntity(var2, "random.orb", 0.2F, ((this.avRandomizer.nextFloat() - this.avRandomizer.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			} else {
				this.clientWorldController.playSoundAtEntity(var2, "random.pop", 0.2F, ((this.avRandomizer.nextFloat() - this.avRandomizer.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			}

			this.gameController.effectRenderer.addEffect(new EntityPickupFX(this.gameController.theWorld, var2, (Entity)var3, -0.5F));
			this.clientWorldController.removeEntityFromWorld(packetIn.func_149354_c());
		}
	}

	/**
	 * Prints a chatmessage in the chat GUI
	 */
	public void handleChat(S02PacketChat packetIn) {
		this.gameController.ingameGUI.getChatGUI().printChatMessage(packetIn.func_148915_c());
	}

	/**
	 * Renders a specified animation: Waking up a player, a living entity swinging its currently held item, being hurt or
	 * receiving a critical hit by normal or magical means
	 */
	public void handleAnimation(S0BPacketAnimation packetIn) {
		Entity var2 = this.clientWorldController.getEntityByID(packetIn.func_148978_c());

		if (var2 != null) {
			if (packetIn.func_148977_d() == 0) {
				EntityLivingBase var3 = (EntityLivingBase)var2;
				var3.swingItem();
			} else if (packetIn.func_148977_d() == 1) {
				var2.performHurtAnimation();
			} else if (packetIn.func_148977_d() == 2) {
				EntityPlayer var4 = (EntityPlayer)var2;
				var4.wakeUpPlayer(false, false, false);
			} else if (packetIn.func_148977_d() == 4) {
				this.gameController.effectRenderer.addEffect(new EntityCrit2FX(this.gameController.theWorld, var2));
			} else if (packetIn.func_148977_d() == 5) {
				EntityCrit2FX var5 = new EntityCrit2FX(this.gameController.theWorld, var2, "magicCrit");
				this.gameController.effectRenderer.addEffect(var5);
			}
		}
	}

	/**
	 * Retrieves the player identified by the packet, puts him to sleep if possible (and flags whether all players are
	 * asleep)
	 */
	public void handleUseBed(S0APacketUseBed packetIn) {
		packetIn.getPlayer(this.clientWorldController).sleepInBedAt(packetIn.getX(), packetIn.getY(), packetIn.getZ());
	}

	/**
	 * Spawns the mob entity at the specified location, with the specified rotation, momentum and type. Updates the
	 * entities Datawatchers with the entity metadata specified in the packet
	 */
	public void handleSpawnMob(S0FPacketSpawnMob packetIn) {
		double var2 = (double)packetIn.func_149023_f() / 32.0D;
		double var4 = (double)packetIn.func_149034_g() / 32.0D;
		double var6 = (double)packetIn.func_149029_h() / 32.0D;
		float var8 = (float)(packetIn.func_149028_l() * 360) / 256.0F;
		float var9 = (float)(packetIn.func_149030_m() * 360) / 256.0F;
		EntityLivingBase var10 = (EntityLivingBase)EntityList.createEntityByID(packetIn.func_149025_e(), this.gameController.theWorld);
		var10.serverPosX = packetIn.func_149023_f();
		var10.serverPosY = packetIn.func_149034_g();
		var10.serverPosZ = packetIn.func_149029_h();
		var10.rotationYawHead = (float)(packetIn.func_149032_n() * 360) / 256.0F;
		Entity[] var11 = var10.getParts();

		if (var11 != null) {
			int var12 = packetIn.func_149024_d() - var10.getEntityId();

			for (int var13 = 0; var13 < var11.length; ++var13) {
				var11[var13].setEntityId(var11[var13].getEntityId() + var12);
			}
		}

		var10.setEntityId(packetIn.func_149024_d());
		var10.setPositionAndRotation(var2, var4, var6, var8, var9);
		var10.motionX = (double)((float)packetIn.func_149026_i() / 8000.0F);
		var10.motionY = (double)((float)packetIn.func_149033_j() / 8000.0F);
		var10.motionZ = (double)((float)packetIn.func_149031_k() / 8000.0F);
		this.clientWorldController.addEntityToWorld(packetIn.func_149024_d(), var10);
		List var14 = packetIn.func_149027_c();

		if (var14 != null) {
			var10.getDataWatcher().updateWatchedObjectsFromList(var14);
		}
	}

	public void handleTimeUpdate(S03PacketTimeUpdate packetIn) {
		this.gameController.theWorld.func_82738_a(packetIn.func_149366_c());
		this.gameController.theWorld.setWorldTime(packetIn.func_149365_d());
	}

	public void handleSpawnPosition(S05PacketSpawnPosition packetIn) {
		this.gameController.thePlayer.setSpawnChunk(new ChunkCoordinates(packetIn.func_149360_c(), packetIn.func_149359_d(), packetIn.func_149358_e()), true);
		this.gameController.theWorld.getWorldInfo().setSpawnPosition(packetIn.func_149360_c(), packetIn.func_149359_d(), packetIn.func_149358_e());
	}

	public void handleEntityAttach(S1BPacketEntityAttach packetIn) {
		Object var2 = this.clientWorldController.getEntityByID(packetIn.func_149403_d());
		Entity var3 = this.clientWorldController.getEntityByID(packetIn.func_149402_e());

		if (packetIn.func_149404_c() == 0) {
			boolean var4 = false;

			if (packetIn.func_149403_d() == this.gameController.thePlayer.getEntityId()) {
				var2 = this.gameController.thePlayer;

				if (var3 instanceof EntityBoat) {
					((EntityBoat)var3).setIsBoatEmpty(false);
				}

				var4 = ((Entity)var2).ridingEntity == null && var3 != null;
			} else if (var3 instanceof EntityBoat) {
				((EntityBoat)var3).setIsBoatEmpty(true);
			}

			if (var2 == null) {
				return;
			}

			((Entity)var2).mountEntity(var3);

			if (var4) {
				GameSettings var5 = this.gameController.gameSettings;
				this.gameController.ingameGUI.setRecordPlaying(I18n.format("mount.onboard", new Object[] {GameSettings.getKeyDisplayString(var5.keyBindSneak.getKeyCode())}), false);
			}
		} else if (packetIn.func_149404_c() == 1 && var2 != null && var2 instanceof EntityLiving) {
			if (var3 != null) {
				((EntityLiving)var2).setLeashedToEntity(var3, false);
			} else {
				((EntityLiving)var2).clearLeashed(false, false);
			}
		}
	}

	/**
	 * Invokes the entities' handleUpdateHealth method which is implemented in LivingBase (hurt/death), MinecartMobSpawner
	 * (spawn delay), FireworkRocket & MinecartTNT (explosion), IronGolem (throwing,...), Witch (spawn particles), Zombie
	 * (villager transformation), Animal (breeding mode particles), Horse (breeding/smoke particles), Sheep (...), Tameable
	 * (...), Villager (particles for breeding mode, angry and happy), Wolf (...)
	 */
	public void handleEntityStatus(S19PacketEntityStatus packetIn) {
		Entity var2 = packetIn.func_149161_a(this.clientWorldController);

		if (var2 != null) {
			var2.handleHealthUpdate(packetIn.func_149160_c());
		}
	}

	public void handleUpdateHealth(S06PacketUpdateHealth packetIn) {
		this.gameController.thePlayer.setPlayerSPHealth(packetIn.getHealth());
		this.gameController.thePlayer.getFoodStats().setFoodLevel(packetIn.getFoodLevel());
		this.gameController.thePlayer.getFoodStats().setFoodSaturationLevel(packetIn.getSaturationLevel());
	}

	public void handleSetExperience(S1FPacketSetExperience packetIn) {
		this.gameController.thePlayer.setXPStats(packetIn.func_149397_c(), packetIn.func_149396_d(), packetIn.func_149395_e());
	}

	public void handleRespawn(S07PacketRespawn packetIn) {
		if (packetIn.func_149082_c() != this.gameController.thePlayer.dimension) {
			this.doneLoadingTerrain = false;
			Scoreboard var2 = this.clientWorldController.getScoreboard();
			this.clientWorldController = new WorldClient(this, new WorldSettings(0L, packetIn.func_149083_e(), false, this.gameController.theWorld.getWorldInfo().isHardcoreModeEnabled(), packetIn.func_149080_f()), packetIn.func_149082_c(), packetIn.func_149081_d(), this.gameController.mcProfiler);
			this.clientWorldController.setWorldScoreboard(var2);
			this.clientWorldController.isRemote = true;
			this.gameController.loadWorld(this.clientWorldController);
			this.gameController.thePlayer.dimension = packetIn.func_149082_c();
			this.gameController.displayGuiScreen(new GuiDownloadTerrain(this));
		}

		this.gameController.setDimensionAndSpawnPlayer(packetIn.func_149082_c());
		this.gameController.playerController.setGameType(packetIn.func_149083_e());
	}

	/**
	 * Initiates a new explosion (sound, particles, drop spawn) for the affected blocks indicated by the packet.
	 */
	public void handleExplosion(S27PacketExplosion packetIn) {
		Explosion var2 = new Explosion(this.gameController.theWorld, (Entity)null, packetIn.func_149148_f(), packetIn.func_149143_g(), packetIn.func_149145_h(), packetIn.func_149146_i());
		var2.affectedBlockPositions = packetIn.func_149150_j();
		var2.doExplosionB(true);
		this.gameController.thePlayer.motionX += (double)packetIn.func_149149_c();
		this.gameController.thePlayer.motionY += (double)packetIn.func_149144_d();
		this.gameController.thePlayer.motionZ += (double)packetIn.func_149147_e();
	}

	/**
	 * Displays a GUI by ID. In order starting from id 0: Chest, Workbench, Furnace, Dispenser, Enchanting table, Brewing
	 * stand, Villager merchant, Beacon, Anvil, Hopper, Dropper, Horse
	 */
	public void handleOpenWindow(S2DPacketOpenWindow packetIn) {
		EntityClientPlayerMP var2 = this.gameController.thePlayer;

		switch (packetIn.func_148899_d()) {
		case 0:
			var2.displayGUIChest(new InventoryBasic(packetIn.func_148902_e(), packetIn.func_148900_g(), packetIn.func_148898_f()));
			var2.openContainer.windowId = packetIn.func_148901_c();
			break;

		case 1:
			var2.displayGUIWorkbench(MathHelper.floor_double(var2.posX), MathHelper.floor_double(var2.posY), MathHelper.floor_double(var2.posZ));
			var2.openContainer.windowId = packetIn.func_148901_c();
			break;

		case 2:
			TileEntityFurnace var4 = new TileEntityFurnace();

			if (packetIn.func_148900_g()) {
				var4.setCustomInventoryName(packetIn.func_148902_e());
			}

			var2.func_146101_a(var4);
			var2.openContainer.windowId = packetIn.func_148901_c();
			break;

		case 3:
			TileEntityDispenser var7 = new TileEntityDispenser();

			if (packetIn.func_148900_g()) {
				var7.func_146018_a(packetIn.func_148902_e());
			}

			var2.func_146102_a(var7);
			var2.openContainer.windowId = packetIn.func_148901_c();
			break;

		case 4:
			var2.displayGUIEnchantment(MathHelper.floor_double(var2.posX), MathHelper.floor_double(var2.posY), MathHelper.floor_double(var2.posZ), packetIn.func_148900_g() ? packetIn.func_148902_e() : null);
			var2.openContainer.windowId = packetIn.func_148901_c();
			break;

		case 5:
			TileEntityBrewingStand var5 = new TileEntityBrewingStand();

			if (packetIn.func_148900_g()) {
				var5.func_145937_a(packetIn.func_148902_e());
			}

			var2.func_146098_a(var5);
			var2.openContainer.windowId = packetIn.func_148901_c();
			break;

		case 6:
			var2.displayGUIMerchant(new NpcMerchant(var2), packetIn.func_148900_g() ? packetIn.func_148902_e() : null);
			var2.openContainer.windowId = packetIn.func_148901_c();
			break;

		case 7:
			TileEntityBeacon var8 = new TileEntityBeacon();
			var2.func_146104_a(var8);

			if (packetIn.func_148900_g()) {
				var8.func_145999_a(packetIn.func_148902_e());
			}

			var2.openContainer.windowId = packetIn.func_148901_c();
			break;

		case 8:
			var2.displayGUIAnvil(MathHelper.floor_double(var2.posX), MathHelper.floor_double(var2.posY), MathHelper.floor_double(var2.posZ));
			var2.openContainer.windowId = packetIn.func_148901_c();
			break;

		case 9:
			TileEntityHopper var3 = new TileEntityHopper();

			if (packetIn.func_148900_g()) {
				var3.func_145886_a(packetIn.func_148902_e());
			}

			var2.func_146093_a(var3);
			var2.openContainer.windowId = packetIn.func_148901_c();
			break;

		case 10:
			TileEntityDropper var6 = new TileEntityDropper();

			if (packetIn.func_148900_g()) {
				var6.func_146018_a(packetIn.func_148902_e());
			}

			var2.func_146102_a(var6);
			var2.openContainer.windowId = packetIn.func_148901_c();
			break;

		case 11:
			Entity var9 = this.clientWorldController.getEntityByID(packetIn.func_148897_h());

			if (var9 != null && var9 instanceof EntityHorse) {
				var2.displayGUIHorse((EntityHorse)var9, new AnimalChest(packetIn.func_148902_e(), packetIn.func_148900_g(), packetIn.func_148898_f()));
				var2.openContainer.windowId = packetIn.func_148901_c();
			}
		}
	}

	/**
	 * Handles pickin up an ItemStack or dropping one in your inventory or an open (non-creative) container
	 */
	public void handleSetSlot(S2FPacketSetSlot packetIn) {
		EntityClientPlayerMP var2 = this.gameController.thePlayer;

		if (packetIn.func_149175_c() == -1) {
			var2.inventory.setItemStack(packetIn.func_149174_e());
		} else {
			boolean var3 = false;

			if (this.gameController.currentScreen instanceof GuiContainerCreative) {
				GuiContainerCreative var4 = (GuiContainerCreative)this.gameController.currentScreen;
				var3 = var4.func_147056_g() != CreativeTabs.tabInventory.getTabIndex();
			}

			if (packetIn.func_149175_c() == 0 && packetIn.func_149173_d() >= 36 && packetIn.func_149173_d() < 45) {
				ItemStack var5 = var2.inventoryContainer.getSlot(packetIn.func_149173_d()).getStack();

				if (packetIn.func_149174_e() != null && (var5 == null || var5.stackSize < packetIn.func_149174_e().stackSize)) {
					packetIn.func_149174_e().animationsToGo = 5;
				}

				var2.inventoryContainer.putStackInSlot(packetIn.func_149173_d(), packetIn.func_149174_e());
			} else if (packetIn.func_149175_c() == var2.openContainer.windowId && (packetIn.func_149175_c() != 0 || !var3)) {
				var2.openContainer.putStackInSlot(packetIn.func_149173_d(), packetIn.func_149174_e());
			}
		}
	}

	/**
	 * Verifies that the server and client are synchronized with respect to the inventory/container opened by the player
	 * and confirms if it is the case.
	 */
	public void handleConfirmTransaction(S32PacketConfirmTransaction packetIn) {
		Container var2 = null;
		EntityClientPlayerMP var3 = this.gameController.thePlayer;

		if (packetIn.func_148889_c() == 0) {
			var2 = var3.inventoryContainer;
		} else if (packetIn.func_148889_c() == var3.openContainer.windowId) {
			var2 = var3.openContainer;
		}

		if (var2 != null && !packetIn.func_148888_e()) {
			this.addToSendQueue(new C0FPacketConfirmTransaction(packetIn.func_148889_c(), packetIn.func_148890_d(), true));
		}
	}

	/**
	 * Handles the placement of a specified ItemStack in a specified container/inventory slot
	 */
	public void handleWindowItems(S30PacketWindowItems packetIn) {
		EntityClientPlayerMP var2 = this.gameController.thePlayer;

		if (packetIn.func_148911_c() == 0) {
			var2.inventoryContainer.putStacksInSlots(packetIn.func_148910_d());
		} else if (packetIn.func_148911_c() == var2.openContainer.windowId) {
			var2.openContainer.putStacksInSlots(packetIn.func_148910_d());
		}
	}

	/**
	 * Creates a sign in the specified location if it didn't exist and opens the GUI to edit its text
	 */
	public void handleSignEditorOpen(S36PacketSignEditorOpen packetIn) {
		Object var2 = this.clientWorldController.getTileEntity(packetIn.func_149129_c(), packetIn.func_149128_d(), packetIn.func_149127_e());

		if (var2 == null) {
			var2 = new TileEntitySign();
			((TileEntity)var2).setWorldObj(this.clientWorldController);
			((TileEntity)var2).xCoord = packetIn.func_149129_c();
			((TileEntity)var2).yCoord = packetIn.func_149128_d();
			((TileEntity)var2).zCoord = packetIn.func_149127_e();
		}

		this.gameController.thePlayer.displayGUIEditSign((TileEntity)var2);
	}

	/**
	 * Updates a specified sign with the specified text lines
	 */
	public void handleUpdateSign(S33PacketUpdateSign packetIn) {
		boolean var2 = false;

		if (this.gameController.theWorld.blockExists(packetIn.func_149346_c(), packetIn.func_149345_d(), packetIn.func_149344_e())) {
			TileEntity var3 = this.gameController.theWorld.getTileEntity(packetIn.func_149346_c(), packetIn.func_149345_d(), packetIn.func_149344_e());

			if (var3 instanceof TileEntitySign) {
				TileEntitySign var4 = (TileEntitySign)var3;

				if (var4.getIsEditable()) {
					for (int var5 = 0; var5 < 4; ++var5) {
						var4.signText[var5] = packetIn.func_149347_f()[var5];
					}

					var4.markDirty();
				}

				var2 = true;
			}
		}

		if (!var2 && this.gameController.thePlayer != null) {
			this.gameController.thePlayer.addChatMessage(new ChatComponentText("Unable to locate sign at " + packetIn.func_149346_c() + ", " + packetIn.func_149345_d() + ", " + packetIn.func_149344_e()));
		}
	}

	/**
	 * Updates the NBTTagCompound metadata of instances of the following entitytypes: Mob spawners, command blocks,
	 * beacons, skulls, flowerpot
	 */
	public void handleUpdateTileEntity(S35PacketUpdateTileEntity packetIn) {
		if (this.gameController.theWorld.blockExists(packetIn.getX(), packetIn.getY(), packetIn.getZ())) {
			TileEntity var2 = this.gameController.theWorld.getTileEntity(packetIn.getX(), packetIn.getY(), packetIn.getZ());

			if (var2 != null) {
				if (packetIn.getTileEntityType() == 1 && var2 instanceof TileEntityMobSpawner) {
					var2.readFromNBT(packetIn.getNbtCompound());
				} else if (packetIn.getTileEntityType() == 2 && var2 instanceof TileEntityCommandBlock) {
					var2.readFromNBT(packetIn.getNbtCompound());
				} else if (packetIn.getTileEntityType() == 3 && var2 instanceof TileEntityBeacon) {
					var2.readFromNBT(packetIn.getNbtCompound());
				} else if (packetIn.getTileEntityType() == 4 && var2 instanceof TileEntitySkull) {
					var2.readFromNBT(packetIn.getNbtCompound());
				} else if (packetIn.getTileEntityType() == 5 && var2 instanceof TileEntityFlowerPot) {
					var2.readFromNBT(packetIn.getNbtCompound());
				}
			}
		}
	}

	/**
	 * Sets the progressbar of the opened window to the specified value
	 */
	public void handleWindowProperty(S31PacketWindowProperty packetIn) {
		EntityClientPlayerMP var2 = this.gameController.thePlayer;

		if (var2.openContainer != null && var2.openContainer.windowId == packetIn.func_149182_c()) {
			var2.openContainer.updateProgressBar(packetIn.func_149181_d(), packetIn.func_149180_e());
		}
	}

	public void handleEntityEquipment(S04PacketEntityEquipment packetIn) {
		Entity var2 = this.clientWorldController.getEntityByID(packetIn.func_149389_d());

		if (var2 != null) {
			var2.setCurrentItemOrArmor(packetIn.func_149388_e(), packetIn.func_149390_c());
		}
	}

	/**
	 * Resets the ItemStack held in hand and closes the window that is opened
	 */
	public void handleCloseWindow(S2EPacketCloseWindow packetIn) {
		this.gameController.thePlayer.closeScreenNoPacket();
	}

	/**
	 * Triggers Block.onBlockEventReceived, which is implemented in BlockPistonBase for extension/retraction, BlockNote for
	 * setting the instrument (including audiovisual feedback) and in BlockContainer to set the number of players accessing
	 * a (Ender)Chest
	 */
	public void handleBlockAction(S24PacketBlockAction packetIn) {
		this.gameController.theWorld.addBlockEvent(packetIn.getX(), packetIn.getY(), packetIn.getZ(), packetIn.getBlockType(), packetIn.getData1(), packetIn.getData2());
	}

	/**
	 * Updates all registered IWorldAccess instances with destroyBlockInWorldPartially
	 */
	public void handleBlockBreakAnim(S25PacketBlockBreakAnim packetIn) {
		this.gameController.theWorld.destroyBlockInWorldPartially(packetIn.func_148845_c(), packetIn.func_148844_d(), packetIn.func_148843_e(), packetIn.func_148842_f(), packetIn.func_148846_g());
	}

	public void handleMapChunkBulk(S26PacketMapChunkBulk packetIn) {
		for (int var2 = 0; var2 < packetIn.func_149254_d(); ++var2) {
			int var3 = packetIn.func_149255_a(var2);
			int var4 = packetIn.func_149253_b(var2);
			this.clientWorldController.doPreChunk(var3, var4, true);
			this.clientWorldController.invalidateBlockReceiveRegion(var3 << 4, 0, var4 << 4, (var3 << 4) + 15, 256, (var4 << 4) + 15);
			Chunk var5 = this.clientWorldController.getChunkFromChunkCoords(var3, var4);
			var5.fillChunk(packetIn.func_149256_c(var2), packetIn.func_149252_e()[var2], packetIn.func_149257_f()[var2], true);
			this.clientWorldController.markBlockRangeForRenderUpdate(var3 << 4, 0, var4 << 4, (var3 << 4) + 15, 256, (var4 << 4) + 15);

			if (!(this.clientWorldController.provider instanceof WorldProviderSurface)) {
				var5.resetRelightChecks();
			}
		}
	}

	public void handleChangeGameState(S2BPacketChangeGameState packetIn) {
		EntityClientPlayerMP var2 = this.gameController.thePlayer;
		int var3 = packetIn.func_149138_c();
		float var4 = packetIn.func_149137_d();
		int var5 = MathHelper.floor_float(var4 + 0.5F);

		if (var3 >= 0 && var3 < S2BPacketChangeGameState.MESSAGE_NAMES.length && S2BPacketChangeGameState.MESSAGE_NAMES[var3] != null) {
			var2.addChatComponentMessage(new ChatComponentTranslation(S2BPacketChangeGameState.MESSAGE_NAMES[var3], new Object[0]));
		}

		if (var3 == 1) {
			this.clientWorldController.getWorldInfo().setRaining(true);
			this.clientWorldController.setRainStrength(0.0F);
		} else if (var3 == 2) {
			this.clientWorldController.getWorldInfo().setRaining(false);
			this.clientWorldController.setRainStrength(1.0F);
		} else if (var3 == 3) {
			this.gameController.playerController.setGameType(WorldSettings.GameType.getByID(var5));
		} else if (var3 == 4) {
			this.gameController.displayGuiScreen(new GuiWinGame());
		} else if (var3 == 5) {
			GameSettings var6 = this.gameController.gameSettings;

			if (var4 == 0.0F) {
				this.gameController.displayGuiScreen(new GuiScreenDemo());
			} else if (var4 == 101.0F) {
				this.gameController.ingameGUI.getChatGUI().printChatMessage(new ChatComponentTranslation("demo.help.movement", new Object[] {GameSettings.getKeyDisplayString(var6.keyBindForward.getKeyCode()), GameSettings.getKeyDisplayString(var6.keyBindLeft.getKeyCode()), GameSettings.getKeyDisplayString(var6.keyBindBack.getKeyCode()), GameSettings.getKeyDisplayString(var6.keyBindRight.getKeyCode())}));
			} else if (var4 == 102.0F) {
				this.gameController.ingameGUI.getChatGUI().printChatMessage(new ChatComponentTranslation("demo.help.jump", new Object[] {GameSettings.getKeyDisplayString(var6.keyBindJump.getKeyCode())}));
			} else if (var4 == 103.0F) {
				this.gameController.ingameGUI.getChatGUI().printChatMessage(new ChatComponentTranslation("demo.help.inventory", new Object[] {GameSettings.getKeyDisplayString(var6.keyBindInventory.getKeyCode())}));
			}
		} else if (var3 == 6) {
			this.clientWorldController.playSound(var2.posX, var2.posY + (double)var2.getEyeHeight(), var2.posZ, "random.successful_hit", 0.18F, 0.45F, false);
		} else if (var3 == 7) {
			this.clientWorldController.setRainStrength(var4);
		} else if (var3 == 8) {
			this.clientWorldController.setThunderStrength(var4);
		}
	}

	/**
	 * Updates the worlds MapStorage with the specified MapData for the specified map-identifier and invokes a
	 * MapItemRenderer for it
	 */
	public void handleMaps(S34PacketMaps packetIn) {
		MapData var2 = ItemMap.loadMapData(packetIn.getMapId(), this.gameController.theWorld);
		var2.updateMPMapData(packetIn.getData());
		this.gameController.entityRenderer.getMapItemRenderer().func_148246_a(var2);
	}

	public void handleEffect(S28PacketEffect packetIn) {
		if (packetIn.isSoundServerwide()) {
			this.gameController.theWorld.playBroadcastSound(packetIn.getSoundType(), packetIn.getPosX(), packetIn.getPosY(), packetIn.getPosZ(), packetIn.getSoundData());
		} else {
			this.gameController.theWorld.playAuxSFX(packetIn.getSoundType(), packetIn.getPosX(), packetIn.getPosY(), packetIn.getPosZ(), packetIn.getSoundData());
		}
	}

	/**
	 * Updates the players statistics or achievements
	 */
	public void handleStatistics(S37PacketStatistics packetIn) {
		boolean var2 = false;
		StatBase var5;
		int var6;

		for (Iterator var3 = packetIn.func_148974_c().entrySet().iterator(); var3.hasNext(); this.gameController.thePlayer.getStatFileWriter().func_150873_a(this.gameController.thePlayer, var5, var6)) {
			Entry var4 = (Entry)var3.next();
			var5 = (StatBase)var4.getKey();
			var6 = ((Integer)var4.getValue()).intValue();

			if (var5.isAchievement() && var6 > 0) {
				if (this.field_147308_k && this.gameController.thePlayer.getStatFileWriter().writeStat(var5) == 0) {
					Achievement var7 = (Achievement)var5;
					this.gameController.guiAchievement.displayAchievement(var7);
					this.gameController.getTwitchStream().func_152911_a(new MetadataAchievement(var7), 0L);

					if (var5 == AchievementList.openInventory) {
						this.gameController.gameSettings.showInventoryAchievementHint = false;
						this.gameController.gameSettings.saveOptions();
					}
				}

				var2 = true;
			}
		}

		if (!this.field_147308_k && !var2 && this.gameController.gameSettings.showInventoryAchievementHint) {
			this.gameController.guiAchievement.displayUnformattedAchievement(AchievementList.openInventory);
		}

		this.field_147308_k = true;

		if (this.gameController.currentScreen instanceof IProgressMeter) {
			((IProgressMeter)this.gameController.currentScreen).doneLoading();
		}
	}

	public void handleEntityEffect(S1DPacketEntityEffect packetIn) {
		Entity var2 = this.clientWorldController.getEntityByID(packetIn.func_149426_d());

		if (var2 instanceof EntityLivingBase) {
			PotionEffect var3 = new PotionEffect(packetIn.func_149427_e(), packetIn.func_149425_g(), packetIn.func_149428_f());
			var3.setPotionDurationMax(packetIn.func_149429_c());
			((EntityLivingBase)var2).addPotionEffect(var3);
		}
	}

	public void handleRemoveEntityEffect(S1EPacketRemoveEntityEffect packetIn) {
		Entity var2 = this.clientWorldController.getEntityByID(packetIn.func_149076_c());

		if (var2 instanceof EntityLivingBase) {
			((EntityLivingBase)var2).removePotionEffectClient(packetIn.func_149075_d());
		}
	}

	public void handlePlayerListItem(S38PacketPlayerListItem packetIn) {
		GuiPlayerInfo var2 = (GuiPlayerInfo)this.playerInfoMap.get(packetIn.func_149122_c());

		if (var2 == null && packetIn.func_149121_d()) {
			var2 = new GuiPlayerInfo(packetIn.func_149122_c());
			this.playerInfoMap.put(packetIn.func_149122_c(), var2);
			this.playerInfoList.add(var2);
		}

		if (var2 != null && !packetIn.func_149121_d()) {
			this.playerInfoMap.remove(packetIn.func_149122_c());
			this.playerInfoList.remove(var2);
		}

		if (var2 != null && packetIn.func_149121_d()) {
			var2.responseTime = packetIn.func_149120_e();
		}
	}

	public void handleKeepAlive(S00PacketKeepAlive packetIn) {
		this.addToSendQueue(new C00PacketKeepAlive(packetIn.func_149134_c()));
	}

	/**
	 * Allows validation of the connection state transition. Parameters: from, to (connection state). Typically throws
	 * IllegalStateException or UnsupportedOperationException if validation fails
	 */
	public void onConnectionStateTransition(EnumConnectionState oldState, EnumConnectionState newState) {
		throw new IllegalStateException("Unexpected protocol change!");
	}

	public void handlePlayerAbilities(S39PacketPlayerAbilities packetIn) {
		EntityClientPlayerMP var2 = this.gameController.thePlayer;
		var2.capabilities.isFlying = packetIn.isFlying();
		var2.capabilities.isCreativeMode = packetIn.isCreativeMode();
		var2.capabilities.disableDamage = packetIn.isInvulnerable();
		var2.capabilities.allowFlying = packetIn.isAllowFlying();
		var2.capabilities.setFlySpeed(packetIn.getFlySpeed());
		var2.capabilities.setPlayerWalkSpeed(packetIn.getWalkSpeed());
	}

	/**
	 * Displays the available command-completion options the server knows of
	 */
	public void handleTabComplete(S3APacketTabComplete packetIn) {
		String[] var2 = packetIn.func_149630_c();

		if (this.gameController.currentScreen instanceof GuiChat) {
			GuiChat var3 = (GuiChat)this.gameController.currentScreen;
			var3.onAutocompleteResponse(var2);
		}
	}

	public void handleSoundEffect(S29PacketSoundEffect packetIn) {
		this.gameController.theWorld.playSound(packetIn.func_149207_d(), packetIn.func_149211_e(), packetIn.func_149210_f(), packetIn.func_149212_c(), packetIn.func_149208_g(), packetIn.func_149209_h(), false);
	}

	/**
	 * Handles packets that have room for a channel specification. Vanilla implemented channels are "MC|TrList" to acquire
	 * a MerchantRecipeList trades for a villager merchant, "MC|Brand" which sets the server brand? on the player instance
	 * and finally "MC|RPack" which the server uses to communicate the identifier of the default server resourcepack for
	 * the client to load.
	 */
	public void handleCustomPayload(S3FPacketCustomPayload packetIn) {
		if ("MC|TrList".equals(packetIn.func_149169_c())) {
			ByteBuf var2 = Unpooled.wrappedBuffer(packetIn.func_149168_d());

			try {
				int var3 = var2.readInt();
				GuiScreen var4 = this.gameController.currentScreen;

				if (var4 != null && var4 instanceof GuiMerchant && var3 == this.gameController.thePlayer.openContainer.windowId) {
					IMerchant var5 = ((GuiMerchant)var4).getMerchant();
					MerchantRecipeList var6 = MerchantRecipeList.func_151390_b(new PacketBuffer(var2));
					var5.setRecipes(var6);
				}
			} catch (IOException var10) {
				logger.error("Couldn\'t load trade info", var10);
			} finally {
				var2.release();
			}
		} else if ("MC|Brand".equals(packetIn.func_149169_c())) {
			this.gameController.thePlayer.setClientBrand(new String(packetIn.func_149168_d(), Charsets.UTF_8));
		} else if ("MC|RPack".equals(packetIn.func_149169_c())) {
			final String var12 = new String(packetIn.func_149168_d(), Charsets.UTF_8);

			if (this.gameController.getCurrentServerData() != null && this.gameController.getCurrentServerData().getResourceMode() == ServerData.ServerResourceMode.ENABLED) {
				this.gameController.getResourcePackRepository().obtainResourcePack(var12);
			} else if (this.gameController.getCurrentServerData() == null || this.gameController.getCurrentServerData().getResourceMode() == ServerData.ServerResourceMode.PROMPT) {
				this.gameController.displayGuiScreen(new GuiYesNo(new GuiYesNoCallback() {
					private static final String __OBFID = "CL_00000879";
					public void confirmClicked(boolean result, int id) {
						NetHandlerPlayClient.this.gameController = Minecraft.getMinecraft();

						if (NetHandlerPlayClient.this.gameController.getCurrentServerData() != null) {
							NetHandlerPlayClient.this.gameController.getCurrentServerData().setResourceMode(ServerData.ServerResourceMode.ENABLED);
							ServerList.func_147414_b(NetHandlerPlayClient.this.gameController.getCurrentServerData());
						}

						if (result) {
							NetHandlerPlayClient.this.gameController.getResourcePackRepository().obtainResourcePack(var12);
						}

						NetHandlerPlayClient.this.gameController.displayGuiScreen((GuiScreen)null);
					}
				}, I18n.format("multiplayer.texturePrompt.line1", new Object[0]), I18n.format("multiplayer.texturePrompt.line2", new Object[0]), 0));
			}
		}
	}

	/**
	 * May create a scoreboard objective, remove an objective from the scoreboard or update an objectives' displayname
	 */
	public void handleScoreboardObjective(S3BPacketScoreboardObjective packetIn) {
		Scoreboard var2 = this.clientWorldController.getScoreboard();
		ScoreObjective var3;

		if (packetIn.func_149338_e() == 0) {
			var3 = var2.addScoreObjective(packetIn.func_149339_c(), IScoreObjectiveCriteria.DUMMY);
			var3.setDisplayName(packetIn.func_149337_d());
		} else {
			var3 = var2.getObjective(packetIn.func_149339_c());

			if (packetIn.func_149338_e() == 1) {
				var2.func_96519_k(var3);
			} else if (packetIn.func_149338_e() == 2) {
				var3.setDisplayName(packetIn.func_149337_d());
			}
		}
	}

	/**
	 * Either updates the score with a specified value or removes the score for an objective
	 */
	public void handleUpdateScore(S3CPacketUpdateScore packetIn) {
		Scoreboard var2 = this.clientWorldController.getScoreboard();
		ScoreObjective var3 = var2.getObjective(packetIn.func_149321_d());

		if (packetIn.func_149322_f() == 0) {
			Score var4 = var2.getValueFromObjective(packetIn.func_149324_c(), var3);
			var4.setScorePoints(packetIn.func_149323_e());
		} else if (packetIn.func_149322_f() == 1) {
			var2.func_96515_c(packetIn.func_149324_c());
		}
	}

	/**
	 * Removes or sets the ScoreObjective to be displayed at a particular scoreboard position (list, sidebar, below name)
	 */
	public void handleDisplayScoreboard(S3DPacketDisplayScoreboard packetIn) {
		Scoreboard var2 = this.clientWorldController.getScoreboard();

		if (packetIn.func_149370_d().length() == 0) {
			var2.setObjectiveInDisplaySlot(packetIn.func_149371_c(), (ScoreObjective)null);
		} else {
			ScoreObjective var3 = var2.getObjective(packetIn.func_149370_d());
			var2.setObjectiveInDisplaySlot(packetIn.func_149371_c(), var3);
		}
	}

	/**
	 * Updates a team managed by the scoreboard: Create/Remove the team registration, Register/Remove the player-team-
	 * memberships, Set team displayname/prefix/suffix and/or whether friendly fire is enabled
	 */
	public void handleTeams(S3EPacketTeams packetIn) {
		Scoreboard var2 = this.clientWorldController.getScoreboard();
		ScorePlayerTeam var3;

		if (packetIn.func_149307_h() == 0) {
			var3 = var2.createTeam(packetIn.func_149312_c());
		} else {
			var3 = var2.getTeam(packetIn.func_149312_c());
		}

		if (packetIn.func_149307_h() == 0 || packetIn.func_149307_h() == 2) {
			var3.setTeamName(packetIn.func_149306_d());
			var3.setNamePrefix(packetIn.func_149311_e());
			var3.setNameSuffix(packetIn.func_149309_f());
			var3.func_98298_a(packetIn.func_149308_i());
		}

		Iterator var4;
		String var5;

		if (packetIn.func_149307_h() == 0 || packetIn.func_149307_h() == 3) {
			var4 = packetIn.func_149310_g().iterator();

			while (var4.hasNext()) {
				var5 = (String)var4.next();
				var2.func_151392_a(var5, packetIn.func_149312_c());
			}
		}

		if (packetIn.func_149307_h() == 4) {
			var4 = packetIn.func_149310_g().iterator();

			while (var4.hasNext()) {
				var5 = (String)var4.next();
				var2.removePlayerFromTeam(var5, var3);
			}
		}

		if (packetIn.func_149307_h() == 1) {
			var2.removeTeam(var3);
		}
	}

	/**
	 * Spawns a specified number of particles at the specified location with a randomized displacement according to
	 * specified bounds
	 */
	public void handleParticles(S2APacketParticles packetIn) {
		if (packetIn.func_149222_k() == 0) {
			double var2 = (double)(packetIn.func_149227_j() * packetIn.func_149221_g());
			double var4 = (double)(packetIn.func_149227_j() * packetIn.func_149224_h());
			double var6 = (double)(packetIn.func_149227_j() * packetIn.func_149223_i());
			this.clientWorldController.spawnParticle(packetIn.func_149228_c(), packetIn.func_149220_d(), packetIn.func_149226_e(), packetIn.func_149225_f(), var2, var4, var6);
		} else {
			for (int var15 = 0; var15 < packetIn.func_149222_k(); ++var15) {
				double var3 = this.avRandomizer.nextGaussian() * (double)packetIn.func_149221_g();
				double var5 = this.avRandomizer.nextGaussian() * (double)packetIn.func_149224_h();
				double var7 = this.avRandomizer.nextGaussian() * (double)packetIn.func_149223_i();
				double var9 = this.avRandomizer.nextGaussian() * (double)packetIn.func_149227_j();
				double var11 = this.avRandomizer.nextGaussian() * (double)packetIn.func_149227_j();
				double var13 = this.avRandomizer.nextGaussian() * (double)packetIn.func_149227_j();
				this.clientWorldController.spawnParticle(packetIn.func_149228_c(), packetIn.func_149220_d() + var3, packetIn.func_149226_e() + var5, packetIn.func_149225_f() + var7, var9, var11, var13);
			}
		}
	}

	/**
	 * Updates en entity's attributes and their respective modifiers, which are used for speed bonusses (player sprinting,
	 * animals fleeing, baby speed), weapon/tool attackDamage, hostiles followRange randomization, zombie maxHealth and
	 * knockback resistance as well as reinforcement spawning chance.
	 */
	public void handleEntityProperties(S20PacketEntityProperties packetIn) {
		Entity var2 = this.clientWorldController.getEntityByID(packetIn.func_149442_c());

		if (var2 != null) {
			if (!(var2 instanceof EntityLivingBase)) {
				throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + var2 + ")");
			} else {
				BaseAttributeMap var3 = ((EntityLivingBase)var2).getAttributeMap();
				Iterator var4 = packetIn.func_149441_d().iterator();

				while (var4.hasNext()) {
					S20PacketEntityProperties.Snapshot var5 = (S20PacketEntityProperties.Snapshot)var4.next();
					IAttributeInstance var6 = var3.getAttributeInstanceByName(var5.func_151409_a());

					if (var6 == null) {
						var6 = var3.registerAttribute(new RangedAttribute(var5.func_151409_a(), 0.0D, 2.2250738585072014E-308D, Double.MAX_VALUE));
					}

					var6.setBaseValue(var5.func_151410_b());
					var6.removeAllModifiers();
					Iterator var7 = var5.func_151408_c().iterator();

					while (var7.hasNext()) {
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
	public NetworkManager getNetworkManager() {
		return this.netManager;
	}
}
