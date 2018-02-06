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

import static org.mockito.AdditionalAnswers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.hamcrest.Matcher;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import wdl.handler.AbstractWorldBehaviorTest;

public abstract class AbstractEntityHandlerTest extends AbstractWorldBehaviorTest {

	/**
	 * An incremental entity ID, or alternatively the number of entities created.
	 */
	private int nextEID;
	protected Int2ObjectMap<Entity> serverEntities;
	protected Int2ObjectMap<Entity> clientEntities;

	@Override
	protected void makeMockWorld() {
		super.makeMockWorld();

		nextEID = 0;
		serverEntities = new Int2ObjectArrayMap<>(4);
		clientEntities = new Int2ObjectArrayMap<>(4);

		when(serverWorld.getEntityByID(any())).thenAnswer(answer(serverEntities::get));
		when(clientWorld.getEntityByID(any())).thenAnswer(answer(clientEntities::get));
	}

	protected void addEntity(Entity serverEntity) {
		try {
			serverEntity.setEntityId(nextEID);
			this.serverEntities.put(serverEntity.getEntityId(), serverEntity);

			// Create the client copy
			Entity clientEntity = serverEntity.getClass().newInstance();
			clientEntity.setEntityId(nextEID);
			// Copy the standard entity metadata
			clientEntity.getDataManager().setEntryValues(serverEntity.getDataManager().getAll());
			// Now add it
			this.clientEntities.put(clientEntity.getEntityId(), clientEntity);

			nextEID++;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
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
