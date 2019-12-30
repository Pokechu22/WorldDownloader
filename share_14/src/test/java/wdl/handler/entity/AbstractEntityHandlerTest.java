/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.handler.entity;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.world.World;
import wdl.handler.AbstractWorldBehaviorTest;
import wdl.handler.HandlerException;
import wdl.versioned.VersionedFunctions;

/**
 * Test for entity handlers.
 *
 * @param <E> The type of entity to handle.
 * @param <C> The type of container associated with that block entity.
 * @param <H> The block handler that handles both of those things.
 */
public abstract class AbstractEntityHandlerTest<E extends Entity, C extends Container, H extends EntityHandler<? super E, ? super C>>
		extends AbstractWorldBehaviorTest {

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
	 * {@link VersionedFunctions}.
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
	}

	protected void addEntity(Entity serverEntity) {
		try {
			int eid = nextEID++;
			this.serverEntities.put(eid, serverEntity);
			this.serverWorld.addEntity(serverEntity, eid);

			EntityType<?> type = serverEntity.getType();
			// Create the client copy
			Entity clientEntity = serverEntity.getClass().getConstructor(EntityType.class, World.class).newInstance(type, (World)clientWorld);
			// Copy the standard entity data
			double posX = VersionedFunctions.getEntityX(serverEntity);
			double posY = VersionedFunctions.getEntityY(serverEntity);
			double posZ = VersionedFunctions.getEntityZ(serverEntity);
			VersionedFunctions.setEntityPos(clientEntity, posX, posY, posZ);
			clientEntity.rotationPitch = serverEntity.rotationPitch;
			clientEntity.rotationYaw = serverEntity.rotationYaw;
			if (clientEntity instanceof LivingEntity) {
				for (EquipmentSlotType slot : EquipmentSlotType.values()) {
					ItemStack item = ((LivingEntity) serverEntity).getItemStackFromSlot(slot);
					((LivingEntity) clientEntity).setItemStackToSlot(slot, item);
				}
			}
			clientEntity.getDataManager().setEntryValues(serverEntity.getDataManager().getAll());
			// Now add it
			this.clientEntities.put(eid, clientEntity);
			this.clientWorld.addEntity(clientEntity, eid);

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

		serverWorld.setPlayerSneaking(serverPlayer, true);
		serverPlayer.closeScreen();
		assertSame("Should have reset server open container", serverPlayer.openContainer, serverPlayer.container);
		assertSame("Should have reset client open container", clientPlayer.openContainer, clientPlayer.container);
		serverWorld.interactEntity(serverEntity, serverPlayer);
		assertNotSame("Should have updated server open container", serverPlayer.openContainer, serverPlayer.container);
		assertNotSame("Should have updated client open container", clientPlayer.openContainer, clientPlayer.container);
		serverPlayer.sendContainerToPlayer(serverPlayer.openContainer);

		return clientPlayer.openContainer;
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
		CompoundNBT expectedNBT = new CompoundNBT();
		expected.writeWithoutTypeId(expectedNBT);
		CompoundNBT actualNBT = new CompoundNBT();
		actual.writeWithoutTypeId(actualNBT);
		for (String key : getIgnoreTags()) {
			expectedNBT.remove(key);
			actualNBT.remove(key);
		}
		assertSameNBT(expectedNBT, actualNBT);
	}

	/**
	 * A list of tags to ignore for entity NBT.
	 */
	protected List<String> getIgnoreTags() {
		return ImmutableList.of("Age", "Attributes"); // unstable
	}

	/**
	 * Applies the given JSON-NBT data to the entity.
	 */
	protected void applyNBT(Entity entity, String nbt) {
		try {
			applyNBT(entity, JsonToNBT.getTagFromJson(nbt));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Applies the given NBT data to the entity.
	 */
	protected void applyNBT(Entity entity, CompoundNBT update) {
		CompoundNBT tag = new CompoundNBT();
		entity.writeWithoutTypeId(tag);
		tag.merge(update);
		entity.read(tag);
	}

	@Override
	public void resetState() {
		super.resetState();
		nextEID = 0;
		serverEntities = null;
		clientEntities = null;
	}
}
