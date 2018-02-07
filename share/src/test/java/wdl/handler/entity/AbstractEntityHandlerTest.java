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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matcher;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.player.inventory.ContainerLocalMenu;
import net.minecraft.entity.Entity;
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
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.village.MerchantRecipeList;
import wdl.ReflectionUtils;
import wdl.handler.AbstractWorldBehaviorTest;
import wdl.handler.HandlerException;

public abstract class AbstractEntityHandlerTest extends AbstractWorldBehaviorTest {

	private static final Logger LOGGER = LogManager.getLogger();

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
	}

	protected void addEntity(Entity serverEntity) {
		try {
			int eid = nextEID++;
			serverEntity.setEntityId(eid);
			this.serverEntities.put(eid, serverEntity);
			when(this.serverWorld.getEntityByID(eid)).thenReturn(serverEntity);

			// Create the client copy
			Entity clientEntity = serverEntity.getClass().newInstance();
			clientEntity.setEntityId(nextEID);
			// Copy the standard entity metadata
			clientEntity.getDataManager().setEntryValues(serverEntity.getDataManager().getAll());
			// Now add it
			this.clientEntities.put(clientEntity.getEntityId(), clientEntity);
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
		assertThat("Entity is not known to the client!", clientWorld.getEntityByID(eid), isNotNull());

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
	 * @param entity The client entity
	 * @param container The container to use
	 * @throws HandlerException when the handler does
	 */
	protected void runHandler(BlockPos pos, Container container) throws HandlerException {
		// TODO
	}

	/**
	 * Checks that the saved world matches the original.
	 */
	protected void checkAllEntities() {
		for (Int2ObjectMap.Entry<Entity> e : serverEntities.int2ObjectEntrySet()) {
			Entity serverEntity = e.getValue();
			Entity clientEntity = clientEntities.get(e.getIntKey());

			assertThat(clientEntity, hasSameNBTAs(serverEntity));
		}
	}

	protected static class HasSameEntityNBT extends HasSameNBT<Entity> {
		public HasSameEntityNBT(Entity e) {
			super(e, "entity");
		}

		@Override
		protected NBTTagCompound getNBT(Entity e) {
			NBTTagCompound tag = new NBTTagCompound();
			e.writeToNBT(tag);
			return tag;
		}
	}

	protected static Matcher<Entity> hasSameNBTAs(Entity serverEntity) {
		return new HasSameEntityNBT(serverEntity);
	}

}
