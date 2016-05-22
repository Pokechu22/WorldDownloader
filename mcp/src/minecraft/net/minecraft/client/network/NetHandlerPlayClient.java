package net.minecraft.client.network;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.mojang.authlib.GameProfile;

import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.GuardianSound;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiCommandBlock;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
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
import net.minecraft.client.particle.EntityPickupFX;
import net.minecraft.client.player.inventory.ContainerLocalMenu;
import net.minecraft.client.player.inventory.LocalBlockIntercommunication;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.NpcMerchant;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityArmorStand;
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
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketResourcePackStatus;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketCamera;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketCloseWindow;
import net.minecraft.network.play.server.SPacketCollectItem;
import net.minecraft.network.play.server.SPacketCombatEvent;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import net.minecraft.network.play.server.SPacketCooldown;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.network.play.server.SPacketDisplayObjective;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketEntity;
import net.minecraft.network.play.server.SPacketEntityAttach;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import net.minecraft.network.play.server.SPacketEntityHeadLook;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.network.play.server.SPacketEntityProperties;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.network.play.server.SPacketKeepAlive;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketPlayerListHeaderFooter;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketRemoveEntityEffect;
import net.minecraft.network.play.server.SPacketResourcePackSend;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketScoreboardObjective;
import net.minecraft.network.play.server.SPacketServerDifficulty;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.network.play.server.SPacketSignEditorOpen;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnExperienceOrb;
import net.minecraft.network.play.server.SPacketSpawnGlobalEntity;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.network.play.server.SPacketSpawnPainting;
import net.minecraft.network.play.server.SPacketSpawnPlayer;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.network.play.server.SPacketStatistics;
import net.minecraft.network.play.server.SPacketTabComplete;
import net.minecraft.network.play.server.SPacketTeams;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.network.play.server.SPacketUpdateEntityNBT;
import net.minecraft.network.play.server.SPacketUpdateHealth;
import net.minecraft.network.play.server.SPacketUpdateScore;
import net.minecraft.network.play.server.SPacketUpdateSign;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.network.play.server.SPacketUseBed;
import net.minecraft.network.play.server.SPacketWindowItems;
import net.minecraft.network.play.server.SPacketWindowProperty;
import net.minecraft.network.play.server.SPacketWorldBorder;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITabCompleter;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.Explosion;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetHandlerPlayClient implements INetHandlerPlayClient {
	private static final Logger logger = LogManager.getLogger();

	/**
	 * The NetworkManager instance used to communicate with the server (used
	 * only by handlePlayerPosLook to update positioning and handleJoinGame to
	 * inform the server of the client distribution/mods)
	 */
	private final NetworkManager netManager;
	private final GameProfile profile;

	/**
	 * Seems to be either null (integrated server) or an instance of either
	 * GuiMultiplayer (when connecting to a server) or GuiScreenReamlsTOS (when
	 * connecting to MCO server)
	 */
	private final GuiScreen guiScreenServer;

	/**
	 * Reference to the Minecraft instance, which many handler methods operate
	 * on
	 */
	private Minecraft gameController;

	/**
	 * Reference to the current ClientWorld instance, which many handler methods
	 * operate on
	 */
	private WorldClient clientWorldController;

	/**
	 * True if the client has finished downloading terrain and may spawn. Set
	 * upon receipt of S08PacketPlayerPosLook, reset upon respawning
	 */
	private boolean doneLoadingTerrain;
	private final Map<UUID, NetworkPlayerInfo> playerInfoMap = Maps
			.<UUID, NetworkPlayerInfo> newHashMap();
	public int currentServerMaxPlayers = 20;
	private boolean hasStatistics = false;

	/**
	 * Just an ordinary random number generator, used to randomize audio pitch
	 * of item/orb pickup and randomize both particlespawn offset and velocity
	 */
	private final Random avRandomizer = new Random();

	public NetHandlerPlayClient(Minecraft mcIn, GuiScreen p_i46300_2_,
			NetworkManager networkManagerIn, GameProfile profileIn) {
		this.gameController = mcIn;
		this.guiScreenServer = p_i46300_2_;
		this.netManager = networkManagerIn;
		this.profile = profileIn;
	}

	/**
	 * Clears the WorldClient instance associated with this NetHandlerPlayClient
	 */
	public void cleanup() {
		this.clientWorldController = null;
	}

	/**
	 * Registers some server properties
	 * (gametype,hardcore-mode,terraintype,difficulty,player limit), creates a
	 * new WorldClient and sets the player initial dimension
	 */
	public void handleJoinGame(SPacketJoinGame packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		this.gameController.playerController = new PlayerControllerMP(
				this.gameController, this);
		this.clientWorldController = new WorldClient(this, new WorldSettings(
				0L, packetIn.getGameType(), false, packetIn.isHardcoreMode(),
				packetIn.getWorldType()), packetIn.getDimension(),
				packetIn.getDifficulty(), this.gameController.mcProfiler);
		this.gameController.gameSettings.difficulty = packetIn.getDifficulty();
		this.gameController.loadWorld(this.clientWorldController);
		this.gameController.thePlayer.dimension = packetIn.getDimension();
		this.gameController.displayGuiScreen(new GuiDownloadTerrain(this));
		this.gameController.thePlayer.setEntityId(packetIn.getEntityId());
		this.currentServerMaxPlayers = packetIn.getMaxPlayers();
		this.gameController.thePlayer.setReducedDebug(packetIn
				.isReducedDebugInfo());
		this.gameController.playerController
				.setGameType(packetIn.getGameType());
		this.gameController.gameSettings.sendSettingsToServer();
		this.netManager.sendPacket(new CPacketCustomPayload("MC|Brand",
				(new PacketBuffer(Unpooled.buffer()))
						.writeString(ClientBrandRetriever.getClientModName())));
	}

	/**
	 * Spawns an instance of the objecttype indicated by the packet and sets its
	 * position and momentum
	 */
	public void handleSpawnObject(SPacketSpawnObject packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		double d0 = packetIn.getX();
		double d1 = packetIn.getY();
		double d2 = packetIn.getZ();
		Entity entity = null;

		if (packetIn.getType() == 10) {
			entity = EntityMinecart.func_184263_a(this.clientWorldController,
					d0, d1, d2,
					EntityMinecart.Type.func_184955_a(packetIn.getData()));
		} else if (packetIn.getType() == 90) {
			Entity entity1 = this.clientWorldController.getEntityByID(packetIn
					.getData());

			if (entity1 instanceof EntityPlayer) {
				entity = new EntityFishHook(this.clientWorldController, d0, d1,
						d2, (EntityPlayer) entity1);
			}

			packetIn.setData(0);
		} else if (packetIn.getType() == 60) {
			entity = new EntityTippedArrow(this.clientWorldController, d0, d1,
					d2);
		} else if (packetIn.getType() == 91) {
			entity = new EntitySpectralArrow(this.clientWorldController, d0,
					d1, d2);
		} else if (packetIn.getType() == 61) {
			entity = new EntitySnowball(this.clientWorldController, d0, d1, d2);
		} else if (packetIn.getType() == 71) {
			entity = new EntityItemFrame(this.clientWorldController,
					new BlockPos(d0, d1, d2), EnumFacing.getHorizontal(packetIn
							.getData()));
			packetIn.setData(0);
		} else if (packetIn.getType() == 77) {
			entity = new EntityLeashKnot(this.clientWorldController,
					new BlockPos(MathHelper.floor_double(d0),
							MathHelper.floor_double(d1),
							MathHelper.floor_double(d2)));
			packetIn.setData(0);
		} else if (packetIn.getType() == 65) {
			entity = new EntityEnderPearl(this.clientWorldController, d0, d1,
					d2);
		} else if (packetIn.getType() == 72) {
			entity = new EntityEnderEye(this.clientWorldController, d0, d1, d2);
		} else if (packetIn.getType() == 76) {
			entity = new EntityFireworkRocket(this.clientWorldController, d0,
					d1, d2, (ItemStack) null);
		} else if (packetIn.getType() == 63) {
			entity = new EntityLargeFireball(this.clientWorldController, d0,
					d1, d2, (double) packetIn.getSpeedX() / 8000.0D,
					(double) packetIn.getSpeedY() / 8000.0D,
					(double) packetIn.getSpeedZ() / 8000.0D);
			packetIn.setData(0);
		} else if (packetIn.getType() == 93) {
			entity = new EntityDragonFireball(this.clientWorldController, d0,
					d1, d2, (double) packetIn.getSpeedX() / 8000.0D,
					(double) packetIn.getSpeedY() / 8000.0D,
					(double) packetIn.getSpeedZ() / 8000.0D);
			packetIn.setData(0);
		} else if (packetIn.getType() == 64) {
			entity = new EntitySmallFireball(this.clientWorldController, d0,
					d1, d2, (double) packetIn.getSpeedX() / 8000.0D,
					(double) packetIn.getSpeedY() / 8000.0D,
					(double) packetIn.getSpeedZ() / 8000.0D);
			packetIn.setData(0);
		} else if (packetIn.getType() == 66) {
			entity = new EntityWitherSkull(this.clientWorldController, d0, d1,
					d2, (double) packetIn.getSpeedX() / 8000.0D,
					(double) packetIn.getSpeedY() / 8000.0D,
					(double) packetIn.getSpeedZ() / 8000.0D);
			packetIn.setData(0);
		} else if (packetIn.getType() == 67) {
			entity = new EntityShulkerBullet(this.clientWorldController, d0,
					d1, d2, (double) packetIn.getSpeedX() / 8000.0D,
					(double) packetIn.getSpeedY() / 8000.0D,
					(double) packetIn.getSpeedZ() / 8000.0D);
			packetIn.setData(0);
		} else if (packetIn.getType() == 62) {
			entity = new EntityEgg(this.clientWorldController, d0, d1, d2);
		} else if (packetIn.getType() == 73) {
			entity = new EntityPotion(this.clientWorldController, d0, d1, d2,
					(ItemStack) null);
			packetIn.setData(0);
		} else if (packetIn.getType() == 75) {
			entity = new EntityExpBottle(this.clientWorldController, d0, d1, d2);
			packetIn.setData(0);
		} else if (packetIn.getType() == 1) {
			entity = new EntityBoat(this.clientWorldController, d0, d1, d2);
		} else if (packetIn.getType() == 50) {
			entity = new EntityTNTPrimed(this.clientWorldController, d0, d1,
					d2, (EntityLivingBase) null);
		} else if (packetIn.getType() == 78) {
			entity = new EntityArmorStand(this.clientWorldController, d0, d1,
					d2);
		} else if (packetIn.getType() == 51) {
			entity = new EntityEnderCrystal(this.clientWorldController, d0, d1,
					d2);
		} else if (packetIn.getType() == 2) {
			entity = new EntityItem(this.clientWorldController, d0, d1, d2);
		} else if (packetIn.getType() == 70) {
			entity = new EntityFallingBlock(this.clientWorldController, d0, d1,
					d2, Block.getStateById(packetIn.getData() & 65535));
			packetIn.setData(0);
		} else if (packetIn.getType() == 3) {
			entity = new EntityAreaEffectCloud(this.clientWorldController, d0,
					d1, d2);
		}

		if (entity != null) {
			EntityTracker.func_187254_a(entity, d0, d1, d2);
			entity.rotationPitch = (float) (packetIn.getPitch() * 360) / 256.0F;
			entity.rotationYaw = (float) (packetIn.getYaw() * 360) / 256.0F;
			Entity[] aentity = entity.getParts();

			if (aentity != null) {
				int i = packetIn.getEntityID() - entity.getEntityId();

				for (int j = 0; j < aentity.length; ++j) {
					aentity[j].setEntityId(aentity[j].getEntityId() + i);
				}
			}

			entity.setEntityId(packetIn.getEntityID());
			entity.setUniqueId(packetIn.getUniqueId());
			this.clientWorldController.addEntityToWorld(packetIn.getEntityID(),
					entity);

			if (packetIn.getData() > 0) {
				if (packetIn.getType() == 60 || packetIn.getType() == 91) {
					Entity entity2 = this.clientWorldController
							.getEntityByID(packetIn.getData() - 1);

					if (entity2 instanceof EntityLivingBase
							&& entity instanceof EntityArrow) {
						((EntityArrow) entity).shootingEntity = entity2;
					}
				}

				entity.setVelocity((double) packetIn.getSpeedX() / 8000.0D,
						(double) packetIn.getSpeedY() / 8000.0D,
						(double) packetIn.getSpeedZ() / 8000.0D);
			}
		}
	}

	/**
	 * Spawns an experience orb and sets its value (amount of XP)
	 */
	public void handleSpawnExperienceOrb(SPacketSpawnExperienceOrb packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		double d0 = packetIn.getX();
		double d1 = packetIn.getY();
		double d2 = packetIn.getZ();
		Entity entity = new EntityXPOrb(this.clientWorldController, d0, d1, d2,
				packetIn.getXPValue());
		EntityTracker.func_187254_a(entity, d0, d1, d2);
		entity.rotationYaw = 0.0F;
		entity.rotationPitch = 0.0F;
		entity.setEntityId(packetIn.getEntityID());
		this.clientWorldController.addEntityToWorld(packetIn.getEntityID(),
				entity);
	}

	/**
	 * Handles globally visible entities. Used in vanilla for lightning bolts
	 */
	public void handleSpawnGlobalEntity(SPacketSpawnGlobalEntity packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		double d0 = packetIn.getX();
		double d1 = packetIn.getY();
		double d2 = packetIn.getZ();
		Entity entity = null;

		if (packetIn.getType() == 1) {
			entity = new EntityLightningBolt(this.clientWorldController, d0,
					d1, d2, false);
		}

		if (entity != null) {
			EntityTracker.func_187254_a(entity, d0, d1, d2);
			entity.rotationYaw = 0.0F;
			entity.rotationPitch = 0.0F;
			entity.setEntityId(packetIn.getEntityId());
			this.clientWorldController.addWeatherEffect(entity);
		}
	}

	/**
	 * Handles the spawning of a painting object
	 */
	public void handleSpawnPainting(SPacketSpawnPainting packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		EntityPainting entitypainting = new EntityPainting(
				this.clientWorldController, packetIn.getPosition(),
				packetIn.getFacing(), packetIn.getTitle());
		entitypainting.setUniqueId(packetIn.getUniqueId());
		this.clientWorldController.addEntityToWorld(packetIn.getEntityID(),
				entitypainting);
	}

	/**
	 * Sets the velocity of the specified entity to the specified value
	 */
	public void handleEntityVelocity(SPacketEntityVelocity packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Entity entity = this.clientWorldController.getEntityByID(packetIn
				.getEntityID());

		if (entity != null) {
			entity.setVelocity((double) packetIn.getMotionX() / 8000.0D,
					(double) packetIn.getMotionY() / 8000.0D,
					(double) packetIn.getMotionZ() / 8000.0D);
		}
	}

	/**
	 * Invoked when the server registers new proximate objects in your watchlist
	 * or when objects in your watchlist have changed -> Registers any changes
	 * locally
	 */
	public void handleEntityMetadata(SPacketEntityMetadata packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Entity entity = this.clientWorldController.getEntityByID(packetIn
				.getEntityId());

		if (entity != null && packetIn.getDataManagerEntries() != null) {
			entity.getDataManager().setEntryValues(
					packetIn.getDataManagerEntries());
		}
	}

	/**
	 * Handles the creation of a nearby player entity, sets the position and
	 * held item
	 */
	public void handleSpawnPlayer(SPacketSpawnPlayer packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		double d0 = packetIn.getX();
		double d1 = packetIn.getY();
		double d2 = packetIn.getZ();
		float f = (float) (packetIn.getYaw() * 360) / 256.0F;
		float f1 = (float) (packetIn.getPitch() * 360) / 256.0F;
		EntityOtherPlayerMP entityotherplayermp = new EntityOtherPlayerMP(
				this.gameController.theWorld, this.getPlayerInfo(
						packetIn.getPlayer()).getGameProfile());
		entityotherplayermp.prevPosX = entityotherplayermp.lastTickPosX = d0;
		entityotherplayermp.prevPosY = entityotherplayermp.lastTickPosY = d1;
		entityotherplayermp.prevPosZ = entityotherplayermp.lastTickPosZ = d2;
		EntityTracker.func_187254_a(entityotherplayermp, d0, d1, d2);
		entityotherplayermp.setPositionAndRotation(d0, d1, d2, f, f1);
		this.clientWorldController.addEntityToWorld(packetIn.getEntityID(),
				entityotherplayermp);
		List<EntityDataManager.DataEntry<?>> list = packetIn
				.getDataManagerEntries();

		if (list != null) {
			entityotherplayermp.getDataManager().setEntryValues(list);
		}
	}

	/**
	 * Updates an entity's position and rotation as specified by the packet
	 */
	public void handleEntityTeleport(SPacketEntityTeleport packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Entity entity = this.clientWorldController.getEntityByID(packetIn
				.getEntityId());

		if (entity != null) {
			double d0 = packetIn.getX();
			double d1 = packetIn.getY();
			double d2 = packetIn.getZ();
			EntityTracker.func_187254_a(entity, d0, d1, d2);

			if (!entity.func_184186_bw()) {
				float f = (float) (packetIn.getYaw() * 360) / 256.0F;
				float f1 = (float) (packetIn.getPitch() * 360) / 256.0F;

				if (Math.abs(entity.posX - d0) < 0.03125D
						&& Math.abs(entity.posY - d1) < 0.015625D
						&& Math.abs(entity.posZ - d2) < 0.03125D) {
					entity.setPositionAndRotation2(entity.posX, entity.posY,
							entity.posZ, f, f1, 0, true);
				} else {
					entity.setPositionAndRotation2(d0, d1, d2, f, f1, 3, true);
				}

				entity.onGround = packetIn.getOnGround();
			}
		}
	}

	/**
	 * Updates which hotbar slot of the player is currently selected
	 */
	public void handleHeldItemChange(SPacketHeldItemChange packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);

		if (InventoryPlayer.func_184435_e(packetIn.getHeldItemHotbarIndex())) {
			this.gameController.thePlayer.inventory.currentItem = packetIn
					.getHeldItemHotbarIndex();
		}
	}

	/**
	 * Updates the specified entity's position by the specified relative moment
	 * and absolute rotation. Note that subclassing of the packet allows for the
	 * specification of a subset of this data (e.g. only rel. position, abs.
	 * rotation or both).
	 */
	public void handleEntityMovement(SPacketEntity packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Entity entity = packetIn.getEntity(this.clientWorldController);

		if (entity != null) {
			entity.serverPosX += (long) packetIn.func_186952_a();
			entity.serverPosY += (long) packetIn.func_186953_b();
			entity.serverPosZ += (long) packetIn.func_186951_c();
			double d0 = (double) entity.serverPosX / 4096.0D;
			double d1 = (double) entity.serverPosY / 4096.0D;
			double d2 = (double) entity.serverPosZ / 4096.0D;

			if (!entity.func_184186_bw()) {
				float f = packetIn.func_149060_h() ? (float) (packetIn
						.func_149066_f() * 360) / 256.0F : entity.rotationYaw;
				float f1 = packetIn.func_149060_h() ? (float) (packetIn
						.func_149063_g() * 360) / 256.0F : entity.rotationPitch;
				entity.setPositionAndRotation2(d0, d1, d2, f, f1, 3, false);
				entity.onGround = packetIn.getOnGround();
			}
		}
	}

	/**
	 * Updates the direction in which the specified entity is looking, normally
	 * this head rotation is independent of the rotation of the entity itself
	 */
	public void handleEntityHeadLook(SPacketEntityHeadLook packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Entity entity = packetIn.getEntity(this.clientWorldController);

		if (entity != null) {
			float f = (float) (packetIn.getYaw() * 360) / 256.0F;
			entity.setRotationYawHead(f);
		}
	}

	/**
	 * Locally eliminates the entities. Invoked by the server when the items are
	 * in fact destroyed, or the player is no longer registered as required to
	 * monitor them. The latter happens when distance between the player and
	 * item increases beyond a certain treshold (typically the viewing distance)
	 */
	public void handleDestroyEntities(SPacketDestroyEntities packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);

		for (int i = 0; i < packetIn.getEntityIDs().length; ++i) {
			this.clientWorldController.removeEntityFromWorld(packetIn
					.getEntityIDs()[i]);
		}
	}

	public void handlePlayerPosLook(SPacketPlayerPosLook packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		EntityPlayer entityplayer = this.gameController.thePlayer;
		double d0 = packetIn.getX();
		double d1 = packetIn.getY();
		double d2 = packetIn.getZ();
		float f = packetIn.getYaw();
		float f1 = packetIn.getPitch();

		if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.X)) {
			d0 += entityplayer.posX;
		} else {
			entityplayer.motionX = 0.0D;
		}

		if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Y)) {
			d1 += entityplayer.posY;
		} else {
			entityplayer.motionY = 0.0D;
		}

		if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Z)) {
			d2 += entityplayer.posZ;
		} else {
			entityplayer.motionZ = 0.0D;
		}

		if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.X_ROT)) {
			f1 += entityplayer.rotationPitch;
		}

		if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Y_ROT)) {
			f += entityplayer.rotationYaw;
		}

		entityplayer.setPositionAndRotation(d0, d1, d2, f, f1);
		this.netManager.sendPacket(new CPacketConfirmTeleport(packetIn
				.getTeleportId()));
		this.netManager.sendPacket(new CPacketPlayer.C06PacketPlayerPosLook(
				entityplayer.posX, entityplayer.getEntityBoundingBox().minY,
				entityplayer.posZ, entityplayer.rotationYaw,
				entityplayer.rotationPitch, false));

		if (!this.doneLoadingTerrain) {
			this.gameController.thePlayer.prevPosX = this.gameController.thePlayer.posX;
			this.gameController.thePlayer.prevPosY = this.gameController.thePlayer.posY;
			this.gameController.thePlayer.prevPosZ = this.gameController.thePlayer.posZ;
			this.doneLoadingTerrain = true;
			this.gameController.displayGuiScreen((GuiScreen) null);
		}
	}

	/**
	 * Received from the servers PlayerManager if between 1 and 64 blocks in a
	 * chunk are changed. If only one block requires an update, the server sends
	 * S23PacketBlockChange and if 64 or more blocks are changed, the server
	 * sends S21PacketChunkData
	 */
	public void handleMultiBlockChange(SPacketMultiBlockChange packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);

		for (SPacketMultiBlockChange.BlockUpdateData spacketmultiblockchange$blockupdatedata : packetIn
				.getChangedBlocks()) {
			this.clientWorldController.invalidateRegionAndSetBlock(
					spacketmultiblockchange$blockupdatedata.getPos(),
					spacketmultiblockchange$blockupdatedata.getBlockState());
		}
	}

	/**
	 * Updates the specified chunk with the supplied data, marks it for
	 * re-rendering and lighting recalculation
	 */
	public void handleChunkData(SPacketChunkData packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);

		if (packetIn.func_149274_i()) {
			this.clientWorldController.doPreChunk(packetIn.getChunkX(),
					packetIn.getChunkZ(), true);
		}

		this.clientWorldController.invalidateBlockReceiveRegion(
				packetIn.getChunkX() << 4, 0, packetIn.getChunkZ() << 4,
				(packetIn.getChunkX() << 4) + 15, 256,
				(packetIn.getChunkZ() << 4) + 15);
		Chunk chunk = this.clientWorldController.getChunkFromChunkCoords(
				packetIn.getChunkX(), packetIn.getChunkZ());
		chunk.fillChunk(packetIn.func_186946_a(), packetIn.getExtractedSize(),
				packetIn.func_149274_i());
		this.clientWorldController.markBlockRangeForRenderUpdate(
				packetIn.getChunkX() << 4, 0, packetIn.getChunkZ() << 4,
				(packetIn.getChunkX() << 4) + 15, 256,
				(packetIn.getChunkZ() << 4) + 15);

		if (!packetIn.func_149274_i()
				|| !(this.clientWorldController.provider instanceof WorldProviderSurface)) {
			chunk.resetRelightChecks();
		}
	}

	public void processChunkUnload(SPacketUnloadChunk packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		this.clientWorldController.doPreChunk(packetIn.getX(), packetIn.getZ(),
				false);
	}

	/**
	 * Updates the block and metadata and generates a blockupdate (and notify
	 * the clients)
	 */
	public void handleBlockChange(SPacketBlockChange packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		this.clientWorldController.invalidateRegionAndSetBlock(
				packetIn.getBlockPosition(), packetIn.getBlockState());
	}

	/**
	 * Closes the network channel
	 */
	public void handleDisconnect(SPacketDisconnect packetIn) {
		/* WDL >>> */
		if (wdl.WDL.downloading) {
			wdl.WDL.stopDownload();

			try {
				Thread.sleep(2000L);
			} catch (Exception var3) {
				;
			}
		}

		/* <<< WDL */
		this.netManager.closeChannel(packetIn.getReason());
	}

	/**
	 * Invoked when disconnecting, the parameter is a ChatComponent describing
	 * the reason for termination
	 */
	public void onDisconnect(ITextComponent reason) {
		/* WDL >>> */
		if (wdl.WDL.downloading) {
			wdl.WDL.stopDownload();

			try {
				Thread.sleep(2000L);
			} catch (Exception var3) {
				;
			}
		}

		/* <<< WDL */
		this.gameController.loadWorld((WorldClient) null);

		if (this.guiScreenServer != null) {
			if (this.guiScreenServer instanceof GuiScreenRealmsProxy) {
				this.gameController
						.displayGuiScreen((new DisconnectedRealmsScreen(
								((GuiScreenRealmsProxy) this.guiScreenServer)
										.getProxy(), "disconnect.lost", reason))
								.getProxy());
			} else {
				this.gameController.displayGuiScreen(new GuiDisconnected(
						this.guiScreenServer, "disconnect.lost", reason));
			}
		} else {
			this.gameController.displayGuiScreen(new GuiDisconnected(
					new GuiMultiplayer(new GuiMainMenu()), "disconnect.lost",
					reason));
		}
	}

	public void addToSendQueue(Packet<?> packetIn) {
		this.netManager.sendPacket(packetIn);
	}

	public void handleCollectItem(SPacketCollectItem packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Entity entity = this.clientWorldController.getEntityByID(packetIn
				.getCollectedItemEntityID());
		EntityLivingBase entitylivingbase = (EntityLivingBase) this.clientWorldController
				.getEntityByID(packetIn.getEntityID());

		if (entitylivingbase == null) {
			entitylivingbase = this.gameController.thePlayer;
		}

		if (entity != null) {
			if (entity instanceof EntityXPOrb) {
				this.clientWorldController.func_184134_a(entity.posX,
						entity.posY, entity.posZ,
						SoundEvents.entity_experience_orb_pickup,
						SoundCategory.PLAYERS, 0.2F,
						((this.avRandomizer.nextFloat() - this.avRandomizer
								.nextFloat()) * 0.7F + 1.0F) * 2.0F, false);
			} else {
				this.clientWorldController.func_184134_a(entity.posX,
						entity.posY, entity.posZ,
						SoundEvents.entity_item_pickup, SoundCategory.PLAYERS,
						0.2F,
						((this.avRandomizer.nextFloat() - this.avRandomizer
								.nextFloat()) * 0.7F + 1.0F) * 2.0F, false);
			}

			this.gameController.effectRenderer
					.addEffect(new EntityPickupFX(this.clientWorldController,
							entity, entitylivingbase, 0.5F));
			this.clientWorldController.removeEntityFromWorld(packetIn
					.getCollectedItemEntityID());
		}
	}

	/**
	 * Prints a chatmessage in the chat GUI
	 */
	public void handleChat(SPacketChat packetIn) {
		/* WDL >>> */
		wdl.WDLHooks.onNHPCHandleChat(this, packetIn);
		/* <<< WDL */
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);

		if (packetIn.getType() == 2) {
			this.gameController.ingameGUI.setRecordPlaying(
					packetIn.getChatComponent(), false);
		} else {
			this.gameController.ingameGUI.getChatGUI().printChatMessage(
					packetIn.getChatComponent());
		}
	}

	/**
	 * Renders a specified animation: Waking up a player, a living entity
	 * swinging its currently held item, being hurt or receiving a critical hit
	 * by normal or magical means
	 */
	public void handleAnimation(SPacketAnimation packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Entity entity = this.clientWorldController.getEntityByID(packetIn
				.getEntityID());

		if (entity != null) {
			if (packetIn.getAnimationType() == 0) {
				EntityLivingBase entitylivingbase = (EntityLivingBase) entity;
				entitylivingbase.swingArm(EnumHand.MAIN_HAND);
			} else if (packetIn.getAnimationType() == 3) {
				EntityLivingBase entitylivingbase1 = (EntityLivingBase) entity;
				entitylivingbase1.swingArm(EnumHand.OFF_HAND);
			} else if (packetIn.getAnimationType() == 1) {
				entity.performHurtAnimation();
			} else if (packetIn.getAnimationType() == 2) {
				EntityPlayer entityplayer = (EntityPlayer) entity;
				entityplayer.wakeUpPlayer(false, false, false);
			} else if (packetIn.getAnimationType() == 4) {
				this.gameController.effectRenderer.emitParticleAtEntity(entity,
						EnumParticleTypes.CRIT);
			} else if (packetIn.getAnimationType() == 5) {
				this.gameController.effectRenderer.emitParticleAtEntity(entity,
						EnumParticleTypes.CRIT_MAGIC);
			}
		}
	}

	/**
	 * Retrieves the player identified by the packet, puts him to sleep if
	 * possible (and flags whether all players are asleep)
	 */
	public void handleUseBed(SPacketUseBed packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		packetIn.getPlayer(this.clientWorldController).trySleep(
				packetIn.getBedPosition());
	}

	/**
	 * Spawns the mob entity at the specified location, with the specified
	 * rotation, momentum and type. Updates the entities Datawatchers with the
	 * entity metadata specified in the packet
	 */
	public void handleSpawnMob(SPacketSpawnMob packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		double d0 = packetIn.getX();
		double d1 = packetIn.getY();
		double d2 = packetIn.getZ();
		float f = (float) (packetIn.getYaw() * 360) / 256.0F;
		float f1 = (float) (packetIn.getPitch() * 360) / 256.0F;
		EntityLivingBase entitylivingbase = (EntityLivingBase) EntityList
				.createEntityByID(packetIn.getEntityType(),
						this.gameController.theWorld);
		EntityTracker.func_187254_a(entitylivingbase, d0, d1, d2);
		entitylivingbase.renderYawOffset = entitylivingbase.rotationYawHead = (float) (packetIn
				.getHeadPitch() * 360) / 256.0F;
		Entity[] aentity = entitylivingbase.getParts();

		if (aentity != null) {
			int i = packetIn.getEntityID() - entitylivingbase.getEntityId();

			for (int j = 0; j < aentity.length; ++j) {
				aentity[j].setEntityId(aentity[j].getEntityId() + i);
			}
		}

		entitylivingbase.setEntityId(packetIn.getEntityID());
		entitylivingbase.setUniqueId(packetIn.getUniqueId());
		entitylivingbase.setPositionAndRotation(d0, d1, d2, f, f1);
		entitylivingbase.motionX = (double) ((float) packetIn.getVelocityX() / 8000.0F);
		entitylivingbase.motionY = (double) ((float) packetIn.getVelocityY() / 8000.0F);
		entitylivingbase.motionZ = (double) ((float) packetIn.getVelocityZ() / 8000.0F);
		this.clientWorldController.addEntityToWorld(packetIn.getEntityID(),
				entitylivingbase);
		List<EntityDataManager.DataEntry<?>> list = packetIn.func_149027_c();

		if (list != null) {
			entitylivingbase.getDataManager().setEntryValues(list);
		}
	}

	public void handleTimeUpdate(SPacketTimeUpdate packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		this.gameController.theWorld.setTotalWorldTime(packetIn
				.getTotalWorldTime());
		this.gameController.theWorld.setWorldTime(packetIn.getWorldTime());
	}

	public void handleSpawnPosition(SPacketSpawnPosition packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		this.gameController.thePlayer.setSpawnPoint(packetIn.getSpawnPos(),
				true);
		this.gameController.theWorld.getWorldInfo().setSpawn(
				packetIn.getSpawnPos());
	}

	public void handleSetPassengers(SPacketSetPassengers packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Entity entity = this.clientWorldController.getEntityByID(packetIn
				.getEntityId());

		if (entity == null) {
			logger.warn("Received passengers for unknown entity");
		} else {
			boolean flag = entity
					.isRidingOrBeingRiddenBy(this.gameController.thePlayer);
			entity.removePassengers();

			for (int i : packetIn.getPassengerIds()) {
				Entity entity1 = this.clientWorldController.getEntityByID(i);

				if (entity1 == null) {
					logger.warn("Received unknown passenger for " + entity);
				} else {
					entity1.startRiding(entity, true);

					if (entity1 == this.gameController.thePlayer && !flag) {
						this.gameController.ingameGUI
								.setRecordPlaying(
										I18n.format(
												"mount.onboard",
												new Object[] { GameSettings
														.getKeyDisplayString(this.gameController.gameSettings.keyBindSneak
																.getKeyCode()) }),
										false);
					}
				}
			}
		}
	}

	public void handleEntityAttach(SPacketEntityAttach packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Entity entity = this.clientWorldController.getEntityByID(packetIn
				.getEntityId());
		Entity entity1 = this.clientWorldController.getEntityByID(packetIn
				.getVehicleEntityId());

		if (entity instanceof EntityLiving) {
			if (entity1 != null) {
				((EntityLiving) entity).setLeashedToEntity(entity1, false);
			} else {
				((EntityLiving) entity).clearLeashed(false, false);
			}
		}
	}

	/**
	 * Invokes the entities' handleUpdateHealth method which is implemented in
	 * LivingBase (hurt/death), MinecartMobSpawner (spawn delay), FireworkRocket
	 * & MinecartTNT (explosion), IronGolem (throwing,...), Witch (spawn
	 * particles), Zombie (villager transformation), Animal (breeding mode
	 * particles), Horse (breeding/smoke particles), Sheep (...), Tameable
	 * (...), Villager (particles for breeding mode, angry and happy), Wolf
	 * (...)
	 */
	public void handleEntityStatus(SPacketEntityStatus packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Entity entity = packetIn.getEntity(this.clientWorldController);

		if (entity != null) {
			if (packetIn.getOpCode() == 21) {
				this.gameController.getSoundHandler().playSound(
						new GuardianSound((EntityGuardian) entity));
			} else {
				entity.handleStatusUpdate(packetIn.getOpCode());
			}
		}
	}

	public void handleUpdateHealth(SPacketUpdateHealth packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		this.gameController.thePlayer.setPlayerSPHealth(packetIn.getHealth());
		this.gameController.thePlayer.getFoodStats().setFoodLevel(
				packetIn.getFoodLevel());
		this.gameController.thePlayer.getFoodStats().setFoodSaturationLevel(
				packetIn.getSaturationLevel());
	}

	public void handleSetExperience(SPacketSetExperience packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		this.gameController.thePlayer.setXPStats(packetIn.func_149397_c(),
				packetIn.getTotalExperience(), packetIn.getLevel());
	}

	public void handleRespawn(SPacketRespawn packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);

		if (packetIn.getDimensionID() != this.gameController.thePlayer.dimension) {
			this.doneLoadingTerrain = false;
			Scoreboard scoreboard = this.clientWorldController.getScoreboard();
			this.clientWorldController = new WorldClient(this,
					new WorldSettings(0L, packetIn.getGameType(), false,
							this.gameController.theWorld.getWorldInfo()
									.isHardcoreModeEnabled(), packetIn
									.getWorldType()),
					packetIn.getDimensionID(), packetIn.getDifficulty(),
					this.gameController.mcProfiler);
			this.clientWorldController.setWorldScoreboard(scoreboard);
			this.gameController.loadWorld(this.clientWorldController);
			this.gameController.thePlayer.dimension = packetIn.getDimensionID();
			this.gameController.displayGuiScreen(new GuiDownloadTerrain(this));
		}

		this.gameController.setDimensionAndSpawnPlayer(packetIn
				.getDimensionID());
		this.gameController.playerController
				.setGameType(packetIn.getGameType());
	}

	/**
	 * Initiates a new explosion (sound, particles, drop spawn) for the affected
	 * blocks indicated by the packet.
	 */
	public void handleExplosion(SPacketExplosion packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Explosion explosion = new Explosion(this.gameController.theWorld,
				(Entity) null, packetIn.getX(), packetIn.getY(),
				packetIn.getZ(), packetIn.getStrength(),
				packetIn.getAffectedBlockPositions());
		explosion.doExplosionB(true);
		this.gameController.thePlayer.motionX += (double) packetIn.getMotionX();
		this.gameController.thePlayer.motionY += (double) packetIn.getMotionY();
		this.gameController.thePlayer.motionZ += (double) packetIn.getMotionZ();
	}

	/**
	 * Displays a GUI by ID. In order starting from id 0: Chest, Workbench,
	 * Furnace, Dispenser, Enchanting table, Brewing stand, Villager merchant,
	 * Beacon, Anvil, Hopper, Dropper, Horse
	 */
	public void handleOpenWindow(SPacketOpenWindow packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		EntityPlayerSP entityplayersp = this.gameController.thePlayer;

		if ("minecraft:container".equals(packetIn.getGuiId())) {
			entityplayersp.displayGUIChest(new InventoryBasic(packetIn
					.getWindowTitle(), packetIn.getSlotCount()));
			entityplayersp.openContainer.windowId = packetIn.getWindowId();
		} else if ("minecraft:villager".equals(packetIn.getGuiId())) {
			entityplayersp.displayVillagerTradeGui(new NpcMerchant(
					entityplayersp, packetIn.getWindowTitle()));
			entityplayersp.openContainer.windowId = packetIn.getWindowId();
		} else if ("EntityHorse".equals(packetIn.getGuiId())) {
			Entity entity = this.clientWorldController.getEntityByID(packetIn
					.getEntityId());

			if (entity instanceof EntityHorse) {
				entityplayersp.func_184826_a(
						(EntityHorse) entity,
						new AnimalChest(packetIn.getWindowTitle(), packetIn
								.getSlotCount()));
				entityplayersp.openContainer.windowId = packetIn.getWindowId();
			}
		} else if (!packetIn.hasSlots()) {
			entityplayersp.displayGui(new LocalBlockIntercommunication(packetIn
					.getGuiId(), packetIn.getWindowTitle()));
			entityplayersp.openContainer.windowId = packetIn.getWindowId();
		} else {
			ContainerLocalMenu containerlocalmenu = new ContainerLocalMenu(
					packetIn.getGuiId(), packetIn.getWindowTitle(),
					packetIn.getSlotCount());
			entityplayersp.displayGUIChest(containerlocalmenu);
			entityplayersp.openContainer.windowId = packetIn.getWindowId();
		}
	}

	/**
	 * Handles pickin up an ItemStack or dropping one in your inventory or an
	 * open (non-creative) container
	 */
	public void handleSetSlot(SPacketSetSlot packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		EntityPlayer entityplayer = this.gameController.thePlayer;

		if (packetIn.getWindowId() == -1) {
			entityplayer.inventory.setItemStack(packetIn.getStack());
		} else if (packetIn.getWindowId() == -2) {
			entityplayer.inventory.setInventorySlotContents(packetIn.getSlot(),
					packetIn.getStack());
		} else {
			boolean flag = false;

			if (this.gameController.currentScreen instanceof GuiContainerCreative) {
				GuiContainerCreative guicontainercreative = (GuiContainerCreative) this.gameController.currentScreen;
				flag = guicontainercreative.getSelectedTabIndex() != CreativeTabs.tabInventory
						.getTabIndex();
			}

			if (packetIn.getWindowId() == 0 && packetIn.getSlot() >= 36
					&& packetIn.getSlot() < 45) {
				ItemStack itemstack = entityplayer.inventoryContainer.getSlot(
						packetIn.getSlot()).getStack();

				if (packetIn.getStack() != null
						&& (itemstack == null || itemstack.stackSize < packetIn
								.getStack().stackSize)) {
					packetIn.getStack().animationsToGo = 5;
				}

				entityplayer.inventoryContainer.putStackInSlot(
						packetIn.getSlot(), packetIn.getStack());
			} else if (packetIn.getWindowId() == entityplayer.openContainer.windowId
					&& (packetIn.getWindowId() != 0 || !flag)) {
				entityplayer.openContainer.putStackInSlot(packetIn.getSlot(),
						packetIn.getStack());
			}
		}
	}

	/**
	 * Verifies that the server and client are synchronized with respect to the
	 * inventory/container opened by the player and confirms if it is the case.
	 */
	public void handleConfirmTransaction(SPacketConfirmTransaction packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Container container = null;
		EntityPlayer entityplayer = this.gameController.thePlayer;

		if (packetIn.getWindowId() == 0) {
			container = entityplayer.inventoryContainer;
		} else if (packetIn.getWindowId() == entityplayer.openContainer.windowId) {
			container = entityplayer.openContainer;
		}

		if (container != null && !packetIn.func_148888_e()) {
			this.addToSendQueue(new CPacketConfirmTransaction(packetIn
					.getWindowId(), packetIn.getActionNumber(), true));
		}
	}

	/**
	 * Handles the placement of a specified ItemStack in a specified
	 * container/inventory slot
	 */
	public void handleWindowItems(SPacketWindowItems packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		EntityPlayer entityplayer = this.gameController.thePlayer;

		if (packetIn.getWindowId() == 0) {
			entityplayer.inventoryContainer.putStacksInSlots(packetIn
					.getItemStacks());
		} else if (packetIn.getWindowId() == entityplayer.openContainer.windowId) {
			entityplayer.openContainer.putStacksInSlots(packetIn
					.getItemStacks());
		}
	}

	/**
	 * Creates a sign in the specified location if it didn't exist and opens the
	 * GUI to edit its text
	 */
	public void handleSignEditorOpen(SPacketSignEditorOpen packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		TileEntity tileentity = this.clientWorldController
				.getTileEntity(packetIn.getSignPosition());

		if (!(tileentity instanceof TileEntitySign)) {
			tileentity = new TileEntitySign();
			tileentity.setWorldObj(this.clientWorldController);
			tileentity.setPos(packetIn.getSignPosition());
		}

		this.gameController.thePlayer.openEditSign((TileEntitySign) tileentity);
	}

	/**
	 * Updates a specified sign with the specified text lines
	 */
	public void handleUpdateSign(SPacketUpdateSign packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		boolean flag = false;

		if (this.gameController.theWorld.isBlockLoaded(packetIn.getPos())) {
			TileEntity tileentity = this.gameController.theWorld
					.getTileEntity(packetIn.getPos());

			if (tileentity instanceof TileEntitySign) {
				TileEntitySign tileentitysign = (TileEntitySign) tileentity;

				if (tileentitysign.getIsEditable()) {
					System.arraycopy(packetIn.getLines(), 0,
							tileentitysign.signText, 0, 4);
					tileentitysign.markDirty();
				}

				flag = true;
			}
		}

		if (!flag && this.gameController.thePlayer != null) {
			logger.debug("Unable to locate sign at " + packetIn.getPos().getX()
					+ ", " + packetIn.getPos().getY() + ", "
					+ packetIn.getPos().getZ());
		}
	}

	/**
	 * Updates the NBTTagCompound metadata of instances of the following
	 * entitytypes: Mob spawners, command blocks, beacons, skulls, flowerpot
	 */
	public void handleUpdateTileEntity(SPacketUpdateTileEntity packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);

		if (this.gameController.theWorld.isBlockLoaded(packetIn.getPos())) {
			TileEntity tileentity = this.gameController.theWorld
					.getTileEntity(packetIn.getPos());
			int i = packetIn.getTileEntityType();
			boolean flag = i == 2
					&& tileentity instanceof TileEntityCommandBlock;

			if (i == 1 && tileentity instanceof TileEntityMobSpawner || flag
					|| i == 3 && tileentity instanceof TileEntityBeacon
					|| i == 4 && tileentity instanceof TileEntitySkull
					|| i == 5 && tileentity instanceof TileEntityFlowerPot
					|| i == 6 && tileentity instanceof TileEntityBanner
					|| i == 7 && tileentity instanceof TileEntityStructure
					|| i == 8 && tileentity instanceof TileEntityEndGateway) {
				tileentity.readFromNBT(packetIn.getNbtCompound());
			}

			if (flag
					&& this.gameController.currentScreen instanceof GuiCommandBlock) {
				((GuiCommandBlock) this.gameController.currentScreen)
						.func_184075_a();
			}
		}
	}

	/**
	 * Sets the progressbar of the opened window to the specified value
	 */
	public void handleWindowProperty(SPacketWindowProperty packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		EntityPlayer entityplayer = this.gameController.thePlayer;

		if (entityplayer.openContainer != null
				&& entityplayer.openContainer.windowId == packetIn
						.getWindowId()) {
			entityplayer.openContainer.updateProgressBar(
					packetIn.getVarIndex(), packetIn.getVarValue());
		}
	}

	public void handleEntityEquipment(SPacketEntityEquipment packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Entity entity = this.clientWorldController.getEntityByID(packetIn
				.getEntityID());

		if (entity != null) {
			entity.setItemStackToSlot(packetIn.func_186969_c(),
					packetIn.getItemStack());
		}
	}

	/**
	 * Resets the ItemStack held in hand and closes the window that is opened
	 */
	public void handleCloseWindow(SPacketCloseWindow packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		this.gameController.thePlayer.closeScreenAndDropStack();
	}

	/**
	 * Triggers Block.onBlockEventReceived, which is implemented in
	 * BlockPistonBase for extension/retraction, BlockNote for setting the
	 * instrument (including audiovisual feedback) and in BlockContainer to set
	 * the number of players accessing a (Ender)Chest
	 */
	public void handleBlockAction(SPacketBlockAction packetIn) {
		/* WDL >>> */
		wdl.WDLHooks.onNHPCHandleBlockAction(this, packetIn);
		/* <<< WDL */
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		this.gameController.theWorld.addBlockEvent(packetIn.getBlockPosition(),
				packetIn.getBlockType(), packetIn.getData1(),
				packetIn.getData2());
	}

	/**
	 * Updates all registered IWorldAccess instances with
	 * destroyBlockInWorldPartially
	 */
	public void handleBlockBreakAnim(SPacketBlockBreakAnim packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		this.gameController.theWorld.sendBlockBreakProgress(
				packetIn.getBreakerId(), packetIn.getPosition(),
				packetIn.getProgress());
	}

	public void handleChangeGameState(SPacketChangeGameState packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		EntityPlayer entityplayer = this.gameController.thePlayer;
		int i = packetIn.getGameState();
		float f = packetIn.func_149137_d();
		int j = MathHelper.floor_float(f + 0.5F);

		if (i >= 0 && i < SPacketChangeGameState.MESSAGE_NAMES.length
				&& SPacketChangeGameState.MESSAGE_NAMES[i] != null) {
			entityplayer.addChatComponentMessage(new TextComponentTranslation(
					SPacketChangeGameState.MESSAGE_NAMES[i], new Object[0]));
		}

		if (i == 1) {
			this.clientWorldController.getWorldInfo().setRaining(true);
			this.clientWorldController.setRainStrength(0.0F);
		} else if (i == 2) {
			this.clientWorldController.getWorldInfo().setRaining(false);
			this.clientWorldController.setRainStrength(1.0F);
		} else if (i == 3) {
			this.gameController.playerController
					.setGameType(WorldSettings.GameType.getByID(j));
		} else if (i == 4) {
			if (j == 0) {
				this.gameController.thePlayer.sendQueue
						.addToSendQueue(new CPacketClientStatus(
								CPacketClientStatus.State.PERFORM_RESPAWN));
				this.gameController.displayGuiScreen(new GuiDownloadTerrain(
						this));
			} else if (j == 1) {
				this.gameController.displayGuiScreen(new GuiWinGame());
			}
		} else if (i == 5) {
			GameSettings gamesettings = this.gameController.gameSettings;

			if (f == 0.0F) {
				this.gameController.displayGuiScreen(new GuiScreenDemo());
			} else if (f == 101.0F) {
				this.gameController.ingameGUI
						.getChatGUI()
						.printChatMessage(
								new TextComponentTranslation(
										"demo.help.movement",
										new Object[] {
												GameSettings
														.getKeyDisplayString(gamesettings.keyBindForward
																.getKeyCode()),
												GameSettings
														.getKeyDisplayString(gamesettings.keyBindLeft
																.getKeyCode()),
												GameSettings
														.getKeyDisplayString(gamesettings.keyBindBack
																.getKeyCode()),
												GameSettings
														.getKeyDisplayString(gamesettings.keyBindRight
																.getKeyCode()) }));
			} else if (f == 102.0F) {
				this.gameController.ingameGUI
						.getChatGUI()
						.printChatMessage(
								new TextComponentTranslation(
										"demo.help.jump",
										new Object[] { GameSettings
												.getKeyDisplayString(gamesettings.keyBindJump
														.getKeyCode()) }));
			} else if (f == 103.0F) {
				this.gameController.ingameGUI
						.getChatGUI()
						.printChatMessage(
								new TextComponentTranslation(
										"demo.help.inventory",
										new Object[] { GameSettings
												.getKeyDisplayString(gamesettings.keyBindInventory
														.getKeyCode()) }));
			}
		} else if (i == 6) {
			this.clientWorldController.func_184148_a(entityplayer,
					entityplayer.posX, entityplayer.posY
							+ (double) entityplayer.getEyeHeight(),
					entityplayer.posZ, SoundEvents.entity_arrow_hit_player,
					SoundCategory.PLAYERS, 0.18F, 0.45F);
		} else if (i == 7) {
			this.clientWorldController.setRainStrength(f);
		} else if (i == 8) {
			this.clientWorldController.setThunderStrength(f);
		} else if (i == 10) {
			this.clientWorldController.spawnParticle(
					EnumParticleTypes.MOB_APPEARANCE, entityplayer.posX,
					entityplayer.posY, entityplayer.posZ, 0.0D, 0.0D, 0.0D,
					new int[0]);
			this.clientWorldController.func_184148_a(entityplayer,
					entityplayer.posX, entityplayer.posY, entityplayer.posZ,
					SoundEvents.entity_elder_guardian_curse,
					SoundCategory.HOSTILE, 1.0F, 1.0F);
		}
	}

	/**
	 * Updates the worlds MapStorage with the specified MapData for the
	 * specified map-identifier and invokes a MapItemRenderer for it
	 */
	public void handleMaps(SPacketMaps packetIn) {
		/* WDL >>> */
		wdl.WDLHooks.onNHPCHandleMaps(this, packetIn);
		/* <<< WDL */
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		MapData mapdata = ItemMap.loadMapData(packetIn.getMapId(),
				this.gameController.theWorld);
		packetIn.setMapdataTo(mapdata);
		this.gameController.entityRenderer.getMapItemRenderer()
				.updateMapTexture(mapdata);
	}

	public void handleEffect(SPacketEffect packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);

		if (packetIn.isSoundServerwide()) {
			this.gameController.theWorld.playBroadcastSound(
					packetIn.getSoundType(), packetIn.getSoundPos(),
					packetIn.getSoundData());
		} else {
			this.gameController.theWorld.playAuxSFX(packetIn.getSoundType(),
					packetIn.getSoundPos(), packetIn.getSoundData());
		}
	}

	/**
	 * Updates the players statistics or achievements
	 */
	public void handleStatistics(SPacketStatistics packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		boolean flag = false;

		for (Entry<StatBase, Integer> entry : packetIn.getStatisticMap()
				.entrySet()) {
			StatBase statbase = (StatBase) entry.getKey();
			int i = ((Integer) entry.getValue()).intValue();

			if (statbase.isAchievement() && i > 0) {
				if (this.hasStatistics
						&& this.gameController.thePlayer.getStatFileWriter()
								.readStat(statbase) == 0) {
					Achievement achievement = (Achievement) statbase;
					this.gameController.guiAchievement
							.displayAchievement(achievement);

					if (statbase == AchievementList.field_187982_f) {
						this.gameController.gameSettings.showInventoryAchievementHint = false;
						this.gameController.gameSettings.saveOptions();
					}
				}

				flag = true;
			}

			this.gameController.thePlayer.getStatFileWriter()
					.unlockAchievement(this.gameController.thePlayer, statbase,
							i);
		}

		if (!this.hasStatistics
				&& !flag
				&& this.gameController.gameSettings.showInventoryAchievementHint) {
			this.gameController.guiAchievement
					.displayUnformattedAchievement(AchievementList.field_187982_f);
		}

		this.hasStatistics = true;

		if (this.gameController.currentScreen instanceof IProgressMeter) {
			((IProgressMeter) this.gameController.currentScreen).doneLoading();
		}
	}

	public void handleEntityEffect(SPacketEntityEffect packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Entity entity = this.clientWorldController.getEntityByID(packetIn
				.getEntityId());

		if (entity instanceof EntityLivingBase) {
			Potion potion = Potion.getPotionById(packetIn.getEffectId());

			if (potion != null) {
				PotionEffect potioneffect = new PotionEffect(potion,
						packetIn.getDuration(), packetIn.getAmplifier(),
						packetIn.func_186984_g(), packetIn.func_179707_f());
				potioneffect.setPotionDurationMax(packetIn.func_149429_c());
				((EntityLivingBase) entity).addPotionEffect(potioneffect);
			}
		}
	}

	public void handleCombatEvent(SPacketCombatEvent packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);

		if (packetIn.eventType == SPacketCombatEvent.Event.ENTITY_DIED) {
			Entity entity = this.clientWorldController
					.getEntityByID(packetIn.field_179774_b);

			if (entity == this.gameController.thePlayer) {
				this.gameController.displayGuiScreen(new GuiGameOver(
						packetIn.deathMessage));
			}
		}
	}

	public void handleServerDifficulty(SPacketServerDifficulty packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		this.gameController.theWorld.getWorldInfo().setDifficulty(
				packetIn.getDifficulty());
		this.gameController.theWorld.getWorldInfo().setDifficultyLocked(
				packetIn.isDifficultyLocked());
	}

	public void handleCamera(SPacketCamera packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Entity entity = packetIn.getEntity(this.clientWorldController);

		if (entity != null) {
			this.gameController.setRenderViewEntity(entity);
		}
	}

	public void handleWorldBorder(SPacketWorldBorder packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		packetIn.func_179788_a(this.clientWorldController.getWorldBorder());
	}

	@SuppressWarnings("incomplete-switch")
	public void handleTitle(SPacketTitle packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		SPacketTitle.Type spackettitle$type = packetIn.getType();
		String s = null;
		String s1 = null;
		String s2 = packetIn.getMessage() != null ? packetIn.getMessage()
				.getFormattedText() : "";

		switch (spackettitle$type) {
		case TITLE:
			s = s2;
			break;

		case SUBTITLE:
			s1 = s2;
			break;

		case RESET:
			this.gameController.ingameGUI.displayTitle("", "", -1, -1, -1);
			this.gameController.ingameGUI.setDefaultTitlesTimes();
			return;
		}

		this.gameController.ingameGUI.displayTitle(s, s1,
				packetIn.getFadeInTime(), packetIn.getDisplayTime(),
				packetIn.getFadeOutTime());
	}

	public void handlePlayerListHeaderFooter(
			SPacketPlayerListHeaderFooter packetIn) {
		this.gameController.ingameGUI.getTabList().setHeader(
				packetIn.getHeader().getFormattedText().isEmpty() ? null
						: packetIn.getHeader());
		this.gameController.ingameGUI.getTabList().setFooter(
				packetIn.getFooter().getFormattedText().isEmpty() ? null
						: packetIn.getFooter());
	}

	public void handleRemoveEntityEffect(SPacketRemoveEntityEffect packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Entity entity = packetIn.getEntity(this.clientWorldController);

		if (entity instanceof EntityLivingBase) {
			((EntityLivingBase) entity).func_184596_c(packetIn.getPotion());
		}
	}

	@SuppressWarnings("incomplete-switch")
	public void handlePlayerListItem(SPacketPlayerListItem packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);

		for (SPacketPlayerListItem.AddPlayerData spacketplayerlistitem$addplayerdata : packetIn
				.getEntries()) {
			if (packetIn.getAction() == SPacketPlayerListItem.Action.REMOVE_PLAYER) {
				this.playerInfoMap.remove(spacketplayerlistitem$addplayerdata
						.getProfile().getId());
			} else {
				NetworkPlayerInfo networkplayerinfo = (NetworkPlayerInfo) this.playerInfoMap
						.get(spacketplayerlistitem$addplayerdata.getProfile()
								.getId());

				if (packetIn.getAction() == SPacketPlayerListItem.Action.ADD_PLAYER) {
					networkplayerinfo = new NetworkPlayerInfo(
							spacketplayerlistitem$addplayerdata);
					this.playerInfoMap.put(networkplayerinfo.getGameProfile()
							.getId(), networkplayerinfo);
				}

				if (networkplayerinfo != null) {
					switch (packetIn.getAction()) {
					case ADD_PLAYER:
						networkplayerinfo
								.setGameType(spacketplayerlistitem$addplayerdata
										.getGameMode());
						networkplayerinfo
								.setResponseTime(spacketplayerlistitem$addplayerdata
										.getPing());
						break;

					case UPDATE_GAME_MODE:
						networkplayerinfo
								.setGameType(spacketplayerlistitem$addplayerdata
										.getGameMode());
						break;

					case UPDATE_LATENCY:
						networkplayerinfo
								.setResponseTime(spacketplayerlistitem$addplayerdata
										.getPing());
						break;

					case UPDATE_DISPLAY_NAME:
						networkplayerinfo
								.setDisplayName(spacketplayerlistitem$addplayerdata
										.getDisplayName());
					}
				}
			}
		}
	}

	public void handleKeepAlive(SPacketKeepAlive packetIn) {
		this.addToSendQueue(new CPacketKeepAlive(packetIn.getId()));
	}

	public void handlePlayerAbilities(SPacketPlayerAbilities packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		EntityPlayer entityplayer = this.gameController.thePlayer;
		entityplayer.capabilities.isFlying = packetIn.isFlying();
		entityplayer.capabilities.isCreativeMode = packetIn.isCreativeMode();
		entityplayer.capabilities.disableDamage = packetIn.isInvulnerable();
		entityplayer.capabilities.allowFlying = packetIn.isAllowFlying();
		entityplayer.capabilities.setFlySpeed(packetIn.getFlySpeed());
		entityplayer.capabilities.setPlayerWalkSpeed(packetIn.getWalkSpeed());
	}

	/**
	 * Displays the available command-completion options the server knows of
	 */
	public void handleTabComplete(SPacketTabComplete packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		String[] astring = packetIn.getMatches();

		if (this.gameController.currentScreen instanceof ITabCompleter) {
			((ITabCompleter) this.gameController.currentScreen)
					.func_184072_a(astring);
		}
	}

	public void handleSoundEffect(SPacketSoundEffect packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		this.gameController.theWorld.func_184148_a(
				this.gameController.thePlayer, packetIn.getX(),
				packetIn.getY(), packetIn.getZ(), packetIn.getSound(),
				packetIn.getCategory(), packetIn.getVolume(),
				packetIn.getPitch());
	}

	public void handleCustomSound(SPacketCustomSound packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		this.gameController.getSoundHandler().playSound(
				new PositionedSoundRecord(new ResourceLocation(packetIn
						.getSoundName()), packetIn.getCategory(), packetIn
						.func_186927_f(), packetIn.func_186928_g(), false, 0,
						ISound.AttenuationType.LINEAR, (float) packetIn
								.func_186932_c(), (float) packetIn
								.func_186926_d(), (float) packetIn
								.func_186925_e()));
	}

	public void handleResourcePack(SPacketResourcePackSend packetIn) {
		final String s = packetIn.getURL();
		final String s1 = packetIn.getHash();

		if (this.checkResourcePackURL(s, s1)) {
			if (s.startsWith("level://")) {
				String s2 = s.substring("level://".length());
				File file1 = new File(this.gameController.mcDataDir, "saves");
				File file2 = new File(file1, s2);

				if (file2.isFile()) {
					this.netManager.sendPacket(new CPacketResourcePackStatus(
							s1, CPacketResourcePackStatus.Action.ACCEPTED));
					Futures.addCallback(this.gameController
							.getResourcePackRepository()
							.setResourcePackInstance(file2), this
							.createResourcePackStatusCallback(s1));
				} else {
					this.netManager.sendPacket(new CPacketResourcePackStatus(
							s1,
							CPacketResourcePackStatus.Action.FAILED_DOWNLOAD));
				}
			} else {
				if (this.gameController.getCurrentServerData() != null
						&& this.gameController.getCurrentServerData()
								.getResourceMode() == ServerData.ServerResourceMode.ENABLED) {
					this.netManager.sendPacket(new CPacketResourcePackStatus(
							s1, CPacketResourcePackStatus.Action.ACCEPTED));
					Futures.addCallback(
							this.gameController.getResourcePackRepository()
									.downloadResourcePack(s, s1), this
									.createResourcePackStatusCallback(s1));
				} else if (this.gameController.getCurrentServerData() != null
						&& this.gameController.getCurrentServerData()
								.getResourceMode() != ServerData.ServerResourceMode.PROMPT) {
					this.netManager.sendPacket(new CPacketResourcePackStatus(
							s1, CPacketResourcePackStatus.Action.DECLINED));
				} else {
					this.gameController.addScheduledTask(new Runnable() {
						public void run() {
							NetHandlerPlayClient.this.gameController
									.displayGuiScreen(new GuiYesNo(
											new GuiYesNoCallback() {
												public void confirmClicked(
														boolean result, int id) {
													NetHandlerPlayClient.this.gameController = Minecraft
															.getMinecraft();

													if (result) {
														if (NetHandlerPlayClient.this.gameController
																.getCurrentServerData() != null) {
															NetHandlerPlayClient.this.gameController
																	.getCurrentServerData()
																	.setResourceMode(
																			ServerData.ServerResourceMode.ENABLED);
														}

														NetHandlerPlayClient.this.netManager
																.sendPacket(new CPacketResourcePackStatus(
																		s1,
																		CPacketResourcePackStatus.Action.ACCEPTED));
														Futures.addCallback(
																NetHandlerPlayClient.this.gameController
																		.getResourcePackRepository()
																		.downloadResourcePack(
																				s,
																				s1),
																NetHandlerPlayClient.this
																		.createResourcePackStatusCallback(s1));
													} else {
														if (NetHandlerPlayClient.this.gameController
																.getCurrentServerData() != null) {
															NetHandlerPlayClient.this.gameController
																	.getCurrentServerData()
																	.setResourceMode(
																			ServerData.ServerResourceMode.DISABLED);
														}

														NetHandlerPlayClient.this.netManager
																.sendPacket(new CPacketResourcePackStatus(
																		s1,
																		CPacketResourcePackStatus.Action.DECLINED));
													}

													ServerList
															.func_147414_b(NetHandlerPlayClient.this.gameController
																	.getCurrentServerData());
													NetHandlerPlayClient.this.gameController
															.displayGuiScreen((GuiScreen) null);
												}
											},
											I18n.format(
													"multiplayer.texturePrompt.line1",
													new Object[0]),
											I18n.format(
													"multiplayer.texturePrompt.line2",
													new Object[0]), 0));
						}
					});
				}
			}
		}
	}

	private boolean checkResourcePackURL(String url, String hash) {
		try {
			URI uri = new URI(url.replace(' ', '+'));
			String s = uri.getScheme();
			boolean flag = "level".equals(s);

			if (!"http".equals(s) && !"https".equals(s) && !flag) {
				throw new URISyntaxException(url, "Wrong protocol");
			} else if (!flag || !url.contains("..")
					&& url.endsWith("/resources.zip")) {
				return true;
			} else {
				throw new URISyntaxException(url,
						"Invalid levelstorage resourcepack path");
			}
		} catch (URISyntaxException var6) {
			this.netManager.sendPacket(new CPacketResourcePackStatus(hash,
					CPacketResourcePackStatus.Action.FAILED_DOWNLOAD));
			return false;
		}
	}

	private FutureCallback<Object> createResourcePackStatusCallback(
			final String hash) {
		return new FutureCallback<Object>() {
			public void onSuccess(Object p_onSuccess_1_) {
				NetHandlerPlayClient.this.netManager
						.sendPacket(new CPacketResourcePackStatus(
								hash,
								CPacketResourcePackStatus.Action.SUCCESSFULLY_LOADED));
			}

			public void onFailure(Throwable p_onFailure_1_) {
				NetHandlerPlayClient.this.netManager
						.sendPacket(new CPacketResourcePackStatus(
								hash,
								CPacketResourcePackStatus.Action.FAILED_DOWNLOAD));
			}
		};
	}

	public void handleUpdateEntityNBT(SPacketUpdateEntityNBT packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		this.gameController.ingameGUI.getBossOverlay().func_184055_a(packetIn);
	}

	public void handleCooldown(SPacketCooldown packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);

		if (packetIn.getTicks() == 0) {
			this.gameController.thePlayer.func_184811_cZ().removeCooldown(
					packetIn.getItem());
		} else {
			this.gameController.thePlayer.func_184811_cZ().setCooldown(
					packetIn.getItem(), packetIn.getTicks());
		}
	}

	public void handleMoveVehicle(SPacketMoveVehicle packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Entity entity = this.gameController.thePlayer.getLowestRidingEntity();

		if (entity != this.gameController.thePlayer && entity.func_184186_bw()) {
			entity.setPositionAndRotation(packetIn.getX(), packetIn.getY(),
					packetIn.getZ(), packetIn.getYaw(), packetIn.getPitch());
			this.netManager.sendPacket(new CPacketVehicleMove(entity));
		}
	}

	/**
	 * Handles packets that have room for a channel specification. Vanilla
	 * implemented channels are "MC|TrList" to acquire a MerchantRecipeList
	 * trades for a villager merchant, "MC|Brand" which sets the server brand?
	 * on the player instance and finally "MC|RPack" which the server uses to
	 * communicate the identifier of the default server resourcepack for the
	 * client to load.
	 */
	public void handleCustomPayload(SPacketCustomPayload packetIn) {
		/* WDL >>> */
		wdl.WDLHooks.onNHPCHandleCustomPayload(this, packetIn);
		/* <<< WDL */
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);

		if ("MC|TrList".equals(packetIn.getChannelName())) {
			PacketBuffer packetbuffer = packetIn.getBufferData();

			try {
				int i = packetbuffer.readInt();
				GuiScreen guiscreen = this.gameController.currentScreen;

				if (guiscreen != null
						&& guiscreen instanceof GuiMerchant
						&& i == this.gameController.thePlayer.openContainer.windowId) {
					IMerchant imerchant = ((GuiMerchant) guiscreen)
							.getMerchant();
					MerchantRecipeList merchantrecipelist = MerchantRecipeList
							.readFromBuf(packetbuffer);
					imerchant.setRecipes(merchantrecipelist);
				}
			} catch (IOException ioexception) {
				logger.error((String) "Couldn\'t load trade info",
						(Throwable) ioexception);
			} finally {
				packetbuffer.release();
			}
		} else if ("MC|Brand".equals(packetIn.getChannelName())) {
			this.gameController.thePlayer.setClientBrand(packetIn
					.getBufferData().readStringFromBuffer(32767));
		} else if ("MC|BOpen".equals(packetIn.getChannelName())) {
			EnumHand enumhand = (EnumHand) packetIn.getBufferData()
					.readEnumValue(EnumHand.class);
			ItemStack itemstack = enumhand == EnumHand.OFF_HAND ? this.gameController.thePlayer
					.getHeldItemOffhand() : this.gameController.thePlayer
					.getHeldItemMainhand();

			if (itemstack != null && itemstack.getItem() == Items.written_book) {
				this.gameController.displayGuiScreen(new GuiScreenBook(
						this.gameController.thePlayer, itemstack, false));
			}
		} else if ("MC|DebugPath".equals(packetIn.getChannelName())) {
			PacketBuffer packetbuffer1 = packetIn.getBufferData();
			int j = packetbuffer1.readInt();
			float f = packetbuffer1.readFloat();
			PathEntity pathentity = PathEntity.func_186311_b(packetbuffer1);
			this.gameController.field_184132_p.field_188286_a.func_188289_a(j,
					pathentity, f);
		}
	}

	/**
	 * May create a scoreboard objective, remove an objective from the
	 * scoreboard or update an objectives' displayname
	 */
	public void handleScoreboardObjective(SPacketScoreboardObjective packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Scoreboard scoreboard = this.clientWorldController.getScoreboard();

		if (packetIn.getAction() == 0) {
			ScoreObjective scoreobjective = scoreboard.addScoreObjective(
					packetIn.getObjectiveName(), IScoreCriteria.DUMMY);
			scoreobjective.setDisplayName(packetIn.getObjectiveValue());
			scoreobjective.setRenderType(packetIn.getRenderType());
		} else {
			ScoreObjective scoreobjective1 = scoreboard.getObjective(packetIn
					.getObjectiveName());

			if (packetIn.getAction() == 1) {
				scoreboard.removeObjective(scoreobjective1);
			} else if (packetIn.getAction() == 2) {
				scoreobjective1.setDisplayName(packetIn.getObjectiveValue());
				scoreobjective1.setRenderType(packetIn.getRenderType());
			}
		}
	}

	/**
	 * Either updates the score with a specified value or removes the score for
	 * an objective
	 */
	public void handleUpdateScore(SPacketUpdateScore packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Scoreboard scoreboard = this.clientWorldController.getScoreboard();
		ScoreObjective scoreobjective = scoreboard.getObjective(packetIn
				.getObjectiveName());

		if (packetIn.getScoreAction() == SPacketUpdateScore.Action.CHANGE) {
			Score score = scoreboard.getValueFromObjective(
					packetIn.getPlayerName(), scoreobjective);
			score.setScorePoints(packetIn.getScoreValue());
		} else if (packetIn.getScoreAction() == SPacketUpdateScore.Action.REMOVE) {
			if (StringUtils.isNullOrEmpty(packetIn.getObjectiveName())) {
				scoreboard.removeObjectiveFromEntity(packetIn.getPlayerName(),
						(ScoreObjective) null);
			} else if (scoreobjective != null) {
				scoreboard.removeObjectiveFromEntity(packetIn.getPlayerName(),
						scoreobjective);
			}
		}
	}

	/**
	 * Removes or sets the ScoreObjective to be displayed at a particular
	 * scoreboard position (list, sidebar, below name)
	 */
	public void handleDisplayScoreboard(SPacketDisplayObjective packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Scoreboard scoreboard = this.clientWorldController.getScoreboard();

		if (packetIn.getName().isEmpty()) {
			scoreboard.setObjectiveInDisplaySlot(packetIn.getPosition(),
					(ScoreObjective) null);
		} else {
			ScoreObjective scoreobjective = scoreboard.getObjective(packetIn
					.getName());
			scoreboard.setObjectiveInDisplaySlot(packetIn.getPosition(),
					scoreobjective);
		}
	}

	/**
	 * Updates a team managed by the scoreboard: Create/Remove the team
	 * registration, Register/Remove the player-team- memberships, Set team
	 * displayname/prefix/suffix and/or whether friendly fire is enabled
	 */
	public void handleTeams(SPacketTeams packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Scoreboard scoreboard = this.clientWorldController.getScoreboard();
		ScorePlayerTeam scoreplayerteam;

		if (packetIn.getAction() == 0) {
			scoreplayerteam = scoreboard.createTeam(packetIn.getName());
		} else {
			scoreplayerteam = scoreboard.getTeam(packetIn.getName());
		}

		if (packetIn.getAction() == 0 || packetIn.getAction() == 2) {
			scoreplayerteam.setTeamName(packetIn.getDisplayName());
			scoreplayerteam.setNamePrefix(packetIn.getPrefix());
			scoreplayerteam.setNameSuffix(packetIn.getSuffix());
			scoreplayerteam.setChatFormat(TextFormatting
					.fromColorIndex(packetIn.getColor()));
			scoreplayerteam.setFriendlyFlags(packetIn.getFriendlyFlags());
			Team.EnumVisible team$enumvisible = Team.EnumVisible
					.getByName(packetIn.getNameTagVisibility());

			if (team$enumvisible != null) {
				scoreplayerteam.setNameTagVisibility(team$enumvisible);
			}

			Team.CollisionRule team$collisionrule = Team.CollisionRule
					.getByName(packetIn.getCollisionRule());

			if (team$collisionrule != null) {
				scoreplayerteam.setCollisionRule(team$collisionrule);
			}
		}

		if (packetIn.getAction() == 0 || packetIn.getAction() == 3) {
			for (String s : packetIn.getPlayers()) {
				scoreboard.addPlayerToTeam(s, packetIn.getName());
			}
		}

		if (packetIn.getAction() == 4) {
			for (String s1 : packetIn.getPlayers()) {
				scoreboard.removePlayerFromTeam(s1, scoreplayerteam);
			}
		}

		if (packetIn.getAction() == 1) {
			scoreboard.removeTeam(scoreplayerteam);
		}
	}

	/**
	 * Spawns a specified number of particles at the specified location with a
	 * randomized displacement according to specified bounds
	 */
	public void handleParticles(SPacketParticles packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);

		if (packetIn.getParticleCount() == 0) {
			double d0 = (double) (packetIn.getParticleSpeed() * packetIn
					.getXOffset());
			double d2 = (double) (packetIn.getParticleSpeed() * packetIn
					.getYOffset());
			double d4 = (double) (packetIn.getParticleSpeed() * packetIn
					.getZOffset());

			try {
				this.clientWorldController.spawnParticle(
						packetIn.getParticleType(), packetIn.isLongDistance(),
						packetIn.getXCoordinate(), packetIn.getYCoordinate(),
						packetIn.getZCoordinate(), d0, d2, d4,
						packetIn.getParticleArgs());
			} catch (Throwable var17) {
				logger.warn("Could not spawn particle effect "
						+ packetIn.getParticleType());
			}
		} else {
			for (int i = 0; i < packetIn.getParticleCount(); ++i) {
				double d1 = this.avRandomizer.nextGaussian()
						* (double) packetIn.getXOffset();
				double d3 = this.avRandomizer.nextGaussian()
						* (double) packetIn.getYOffset();
				double d5 = this.avRandomizer.nextGaussian()
						* (double) packetIn.getZOffset();
				double d6 = this.avRandomizer.nextGaussian()
						* (double) packetIn.getParticleSpeed();
				double d7 = this.avRandomizer.nextGaussian()
						* (double) packetIn.getParticleSpeed();
				double d8 = this.avRandomizer.nextGaussian()
						* (double) packetIn.getParticleSpeed();

				try {
					this.clientWorldController.spawnParticle(
							packetIn.getParticleType(),
							packetIn.isLongDistance(),
							packetIn.getXCoordinate() + d1,
							packetIn.getYCoordinate() + d3,
							packetIn.getZCoordinate() + d5, d6, d7, d8,
							packetIn.getParticleArgs());
				} catch (Throwable var16) {
					logger.warn("Could not spawn particle effect "
							+ packetIn.getParticleType());
					return;
				}
			}
		}
	}

	/**
	 * Updates en entity's attributes and their respective modifiers, which are
	 * used for speed bonusses (player sprinting, animals fleeing, baby speed),
	 * weapon/tool attackDamage, hostiles followRange randomization, zombie
	 * maxHealth and knockback resistance as well as reinforcement spawning
	 * chance.
	 */
	public void handleEntityProperties(SPacketEntityProperties packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this,
				this.gameController);
		Entity entity = this.clientWorldController.getEntityByID(packetIn
				.getEntityId());

		if (entity != null) {
			if (!(entity instanceof EntityLivingBase)) {
				throw new IllegalStateException(
						"Server tried to update attributes of a non-living entity (actually: "
								+ entity + ")");
			} else {
				AbstractAttributeMap abstractattributemap = ((EntityLivingBase) entity)
						.getAttributeMap();

				for (SPacketEntityProperties.Snapshot spacketentityproperties$snapshot : packetIn
						.func_149441_d()) {
					IAttributeInstance iattributeinstance = abstractattributemap
							.getAttributeInstanceByName(spacketentityproperties$snapshot
									.func_151409_a());

					if (iattributeinstance == null) {
						iattributeinstance = abstractattributemap
								.registerAttribute(new RangedAttribute(
										(IAttribute) null,
										spacketentityproperties$snapshot
												.func_151409_a(), 0.0D,
										2.2250738585072014E-308D,
										Double.MAX_VALUE));
					}

					iattributeinstance
							.setBaseValue(spacketentityproperties$snapshot
									.func_151410_b());
					iattributeinstance.removeAllModifiers();

					for (AttributeModifier attributemodifier : spacketentityproperties$snapshot
							.func_151408_c()) {
						iattributeinstance.applyModifier(attributemodifier);
					}
				}
			}
		}
	}

	/**
	 * Returns this the NetworkManager instance registered with this
	 * NetworkHandlerPlayClient
	 */
	public NetworkManager getNetworkManager() {
		return this.netManager;
	}

	public Collection<NetworkPlayerInfo> getPlayerInfoMap() {
		return this.playerInfoMap.values();
	}

	public NetworkPlayerInfo getPlayerInfo(UUID uniqueId) {
		return (NetworkPlayerInfo) this.playerInfoMap.get(uniqueId);
	}

	/**
	 * Gets the client's description information about another player on the
	 * server.
	 */
	public NetworkPlayerInfo getPlayerInfo(String name) {
		for (NetworkPlayerInfo networkplayerinfo : this.playerInfoMap.values()) {
			if (networkplayerinfo.getGameProfile().getName().equals(name)) {
				return networkplayerinfo;
			}
		}

		return null;
	}

	public GameProfile getGameProfile() {
		return this.profile;
	}
}
