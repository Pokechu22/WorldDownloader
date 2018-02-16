/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.handler.entity;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.player.inventory.ContainerLocalMenu;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.NpcMerchant;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EquineEntity;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerHorseChest;
import net.minecraft.inventory.ContainerHorseInventory;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import wdl.ReflectionUtils;
import wdl.VersionedProperties;
import wdl.handler.AbstractWorldBehaviorTest;
import wdl.handler.HandlerException;

/**
 * Test for entity handlers.
 *
 * @param <E> The type of entity to handle.
 * @param <C> The type of container associated with that block entity.
 * @param <H> The block handler that handles both of those things.
 */
public abstract class AbstractEntityHandlerTest<E extends Entity, C extends Container, H extends EntityHandler<? super E, ? super C>>
		extends AbstractWorldBehaviorTest {

	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Constructor.
	 *
	 * @param blockentityClass
	 *            A strong reference to the entity class that is handled by
	 *            the handler.
	 * @param containerClass
	 *            A strong reference to the container class that is handled by the
	 *            handler.
	 * @param handlerClass
	 *            A strong reference to the handler's class.
	 */
	protected AbstractEntityHandlerTest(Class<E> entityClass, Class<C> containerClass, Class<H> handlerClass) {
		this.entityClass = entityClass;
		this.containerClass = containerClass;
		this.handlerClass = handlerClass;

		try {
			// TODO: may in the future want to have other constructors, which
			// wouldn't work with this
			this.handler = handlerClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	protected final Class<E> entityClass;
	protected final Class<C> containerClass;
	protected final Class<H> handlerClass;

	/**
	 * Handler under test.  Will be a new object, not the handler registered in
	 * {@link VersionedProperties}.
	 */
	protected final H handler;

	/**
	 * Verifies that the handler is registered.
	 *
	 * Note that this does not actually use the {@link #handler} instance.
	 */
	@Test
	public final void testHandlerExists() {
		EntityHandler<? super E, ? super C> handler = EntityHandler.getHandler(entityClass, containerClass);

		assertThat(handler, is(notNullValue()));
		assertThat(handler, is(instanceOf(handlerClass)));
		assertTrue(handler.getEntityClass().isAssignableFrom(entityClass));
		assertThat(handler.getContainerClass(), is(equalTo(containerClass)));
	}

	/**
	 * An incremental entity ID, or alternatively the number of entities created.
	 */
	private int nextEID;
	/**
	 * Backing maps for server and client worlds.
	 */
	private Int2ObjectMap<Entity> serverEntities, clientEntities;

	@Override
	protected void makeMockWorld() {
		super.makeMockWorld();

		nextEID = 0;
		serverEntities = new Int2ObjectArrayMap<>(4);
		clientEntities = new Int2ObjectArrayMap<>(4);

		when(clientWorld.getEntityByID(anyInt())).thenReturn(null);
		when(serverWorld.getEntityByID(anyInt())).thenReturn(null);
	}

	protected void addEntity(Entity serverEntity) {
		try {
			int eid = nextEID++;
			serverEntity.setEntityId(eid);
			serverEntity.setUniqueId(new UUID(0, eid));
			this.serverEntities.put(eid, serverEntity);
			when(this.serverWorld.getEntityByID(eid)).thenReturn(serverEntity);

			// Create the client copy
			Entity clientEntity = serverEntity.getClass().getConstructor(World.class).newInstance((World)clientWorld);
			clientEntity.setEntityId(eid);
			clientEntity.setUniqueId(serverEntity.getUniqueID());
			// Copy the standard entity data
			clientEntity.posX = serverEntity.posX;
			clientEntity.posY = serverEntity.posY;
			clientEntity.posZ = serverEntity.posZ;
			clientEntity.rotationPitch = serverEntity.rotationPitch;
			clientEntity.rotationYaw = serverEntity.rotationYaw;
			if (clientEntity instanceof EntityLivingBase) {
				for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
					ItemStack item = ((EntityLivingBase) serverEntity).getItemStackFromSlot(slot);
					((EntityLivingBase) clientEntity).setItemStackToSlot(slot, item);
				}
			}
			clientEntity.getDataManager().setEntryValues(serverEntity.getDataManager().getAll());
			// Now add it
			this.clientEntities.put(eid, clientEntity);
			when(this.clientWorld.getEntityByID(eid)).thenReturn(clientEntity);

			nextEID++;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Makes a container as the client would have.
	 *
	 * @param serverEntity The entity as present on the server.
	 */
	protected Container createClientContainer(Entity serverEntity) {
		// Precondition for this to make sense
		int eid = serverEntity.getEntityId();
		assertThat("Entity is not known to the server!", serverWorld.getEntityByID(eid), is(serverEntity));
		assertThat("Entity is not known to the client!", clientWorld.getEntityByID(eid), is(notNullValue()));

		// This is a bit of a mess, but entities are a bit of a mess.
		// First, check if this is a villager, because they're a whole different
		// kind of mess.
		if (serverEntity instanceof EntityVillager) {
			return createVillagerContainer((EntityVillager) serverEntity);
		} else if (serverEntity instanceof EquineEntity) {
			return createHorseContainer((EquineEntity) serverEntity);
		} else if (serverEntity instanceof EntityMinecartContainer) {
			return createMinecartContainer((EntityMinecartContainer) serverEntity);
		} else {
			throw new AssertionError("Unexpected entity " + serverEntity);
		}
	}

	/**
	 * Creates a villager container.  This process is ugly.
	 */
	private Container createVillagerContainer(EntityVillager serverVillager) {
		// EntityPlayerMP.displayVillagerTradeGui(IMerchant), part 1 (before recipes)

		// We don't actually need to use the merchant inventory, as it's always blank
		// when opened:
		// ContainerMerchant serverContainer = new ContainerMerchant(serverPlayer.inventory, serverVillager, serverWorld);
		// IInventory serverMerchentInventory = serverContainer.getMerchantInventory();
		ITextComponent serverDispName = serverVillager.getDisplayName();

		// NHPC.handleOpenWindow, and GuiMerchant.<init>
		IMerchant clientMerchant = new NpcMerchant(this.clientPlayer, serverDispName);
		ContainerMerchant clientContainer = new ContainerMerchant(clientPlayer.inventory, clientMerchant, clientWorld);

		// NHPC.handleCustomPayload
		// NOTE: the villager code never actually uses the player
		MerchantRecipeList serverRecipes = serverVillager.getRecipes(this.serverPlayer);
		// Substitute for network stuff
		MerchantRecipeList clientRecipes = new MerchantRecipeList(serverRecipes.getRecipiesAsTags());

		clientMerchant.setRecipes(clientRecipes);

		return clientContainer;
	}

	private Container createHorseContainer(EquineEntity serverHorse) {
		// No getter -_-
		// Also, ContainerHorseChest is not a Container -_-
		ContainerHorseChest serverInv = ReflectionUtils.findAndGetPrivateField(serverHorse, EquineEntity.class, ContainerHorseChest.class);
		// EquineEntity.openGui
		serverInv.setCustomName(serverHorse.getName());

		// NHPC.handleOpenWindow
		IInventory clientInv = new ContainerHorseChest(serverInv.getDisplayName(), serverInv.getSizeInventory());
		// Copy items and fields (this normally happens later, but whatever)
		for (int i = 0; i < serverInv.getSizeInventory(); i++) {
			ItemStack serverItem = serverInv.getStackInSlot(i);
			if (serverItem == null) {
				// In older versions with nullable items
				continue;
			}
			clientInv.setInventorySlotContents(i, serverItem.copy());
		}
		for (int i = 0; i < serverInv.getFieldCount(); i++) {
			clientInv.setField(i, serverInv.getField(i));
		}
		Entity clientHorse_ = clientWorld.getEntityByID(serverHorse.getEntityId());
		assertThat(clientHorse_, is(instanceOf(EquineEntity.class)));
		EquineEntity clientHorse = (EquineEntity) clientHorse_;
		// GuiScreenHorseInventory
		return new ContainerHorseInventory(clientInv, clientPlayer.inventory, clientHorse, clientPlayer);
	}

	private Container createMinecartContainer(EntityMinecartContainer minecart) {
		IInventory serverInv = minecart;
		String guiID = minecart.getGuiID();
		assertThat(guiID, is(not("minecraft:container")));
		IInventory clientInv = new ContainerLocalMenu(guiID, serverInv.getDisplayName(),
				serverInv.getSizeInventory());

		for (int i = 0; i < serverInv.getSizeInventory(); i++) {
			ItemStack serverItem = serverInv.getStackInSlot(i);
			if (serverItem == null) {
				// In older versions with nullable items
				continue;
			}
			clientInv.setInventorySlotContents(i, serverItem.copy());
		}
		for (int i = 0; i < serverInv.getFieldCount(); i++) {
			clientInv.setField(i, serverInv.getField(i));
		}

		Container container = makeContainer(guiID, clientPlayer, clientInv);
		if (container == null) {
			// Unknown -- i.e. minecraft:container
			LOGGER.warn("Unknown container type {} for {}", guiID, minecart);
			return new ContainerChest(clientPlayer.inventory, clientInv, clientPlayer);
		} else {
			return container;
		}
	}

	/**
	 * Runs the handler
	 *
	 * @param entity The entity ID
	 * @param container The container to use
	 * @throws HandlerException when the handler does
	 */
	protected void runHandler(int eid, Container container) throws HandlerException {
		handler.copyDataCasting(container, clientEntities.get(eid), false);
	}

	/**
	 * Checks that the saved world matches the original.
	 */
	protected void checkAllEntities() {
		for (Int2ObjectMap.Entry<Entity> e : serverEntities.int2ObjectEntrySet()) {
			Entity serverEntity = e.getValue();
			Entity clientEntity = clientEntities.get(e.getIntKey());

			assertSameNBT(serverEntity, clientEntity);
		}
	}

	/**
	 * Helper to call {@link #assertSameNBT(NBTTagCompound, NBTTagCompound)
	 *
	 * @param expected Entity with expected NBT (the server entity)
	 * @param actual Entity with the actual NBT (the client entity)
	 */
	protected void assertSameNBT(Entity expected, Entity actual) {
		// Can't call these methods directly because writeToNBT returns void in 1.9
		NBTTagCompound expectedNBT = new NBTTagCompound();
		expected.writeToNBT(expectedNBT);
		NBTTagCompound actualNBT = new NBTTagCompound();
		actual.writeToNBT(actualNBT);
		assertSameNBT(expectedNBT, actualNBT);
	}
}
