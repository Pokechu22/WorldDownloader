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

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.HorseInventoryContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import wdl.ReflectionUtils;
import wdl.VersionConstants;
import wdl.handler.HandlerException;

@RunWith(Parameterized.class)
public class HorseTest<T extends AbstractHorseEntity> extends AbstractEntityHandlerTest<T, HorseInventoryContainer, HorseHandler> {
	private static enum HorseType {
		HORSE("minecraft:horse", "net.minecraft.entity.passive.horse.HorseEntity", 0, false),
		DONKEY("minecraft:donkey", "net.minecraft.entity.passive.horse.DonkeyEntity", 1, true),
		MULE("minecraft:mule", "net.minecraft.entity.passive.horse.MuleEntity", 2, true),
		ZOMBIE_HORSE("minecraft:zombie_horse", "net.minecraft.entity.passive.horse.SkeletonHorseEntity", 3, false),
		SKELETON_HORSE("minecraft:skeleton_horse", "net.minecraft.entity.passive.horse.ZombieHorseEntity", 4, false),
		LLAMA("minecraft:llama", "net.minecraft.entity.passive.horse.LlamaEntity", null, true) {
			@Override
			public boolean exists() {
				return HAS_NAMESPACED_ENTITIES;
			}
		};

		/**
		 * True if entity IDs are namespaced, which is the case in 1.11 (snapshot 16w32a
		 * = 800) and later.
		 */
		private static final boolean HAS_NAMESPACED_ENTITIES = (VersionConstants.getDataVersion() >= 800);

		/** The text ID used in 1.11 and above. */
		private final String id;
		/** The name of the class for this entity; may not exist in all versions. */
		private final String className;
		/** Numeric variant ID used in 1.10 and below */
		@Nullable
		private final Integer typeID;
		/** Whether this entity can have chests. */
		private final boolean chestable;

		private HorseType(String id, String className, @Nullable Integer typeID, boolean chestable) {
			this.id = id;
			this.className = className;
			this.typeID = typeID;
			this.chestable = chestable;
		}

		@Override
		public String toString() {
			return this.id;
		}

		/**
		 * Returns true if this entity exists in the current MC version.
		 */
		public boolean exists() {
			return true;
		}

		/**
		 * Gets an NBT blob that can be used to specify the right type for this
		 * horse.
		 * @param addChests true if chests should be present
		 */
		public CompoundNBT getNBT(boolean addChests) {
			CompoundNBT tag = new CompoundNBT();
			if (this.typeID != null && HAS_NAMESPACED_ENTITIES) {
				tag.putInt("Type", typeID);
			}
			if (this.chestable) {
				tag.putBoolean("ChestedHorse", addChests);
			} else {
				assertFalse(addChests);
			}
			return tag;
		}

		/**
		 * Gets the class used to represent this horse.
		 */
		public Class<? extends AbstractHorseEntity> getHorseClass() {
			if (HAS_NAMESPACED_ENTITIES) {
				try {
					return Class.forName(this.className).asSubclass(AbstractHorseEntity.class);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			} else {
				return AbstractHorseEntity.class;
			}
		}
	}

	@Parameters(name="{0} (chested={2})")
	public static List<Object[]> data() {
		List<Object[]> params = new ArrayList<>();
		for (HorseType type : HorseType.values()) {
			if (!type.exists()) {
				continue;
			}
			params.add(new Object[] { type, type.getHorseClass(), false });
			if (type.chestable) {
				params.add(new Object[] { type, type.getHorseClass(), true });
			}
		}
		return params;
	}

	private final HorseType type;
	private final boolean chests;

	public HorseTest(HorseType type, Class<T> horseClass, boolean chests) {
		super(horseClass, HorseInventoryContainer.class, HorseHandler.class);
		this.type = type;
		this.chests = chests;
	}

	private T makeHorse() {
		try {
			EntityType<?> type = EntityType.byKey(this.type.id).get();
			T entity = this.entityClass.getConstructor(EntityType.class, World.class).newInstance(type, this.serverWorld);
			applyNBT(entity, this.type.getNBT(this.chests));
			entity.setHorseTamed(true);
			return entity;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testHorse() throws HandlerException {
		makeMockWorld();
		AbstractHorseEntity horse = makeHorse();
		Inventory inventory = ReflectionUtils.findAndGetPrivateField(horse, AbstractHorseEntity.class, Inventory.class);
		inventory.setInventorySlotContents(0, new ItemStack(Items.SADDLE));
		if (inventory.getSizeInventory() > 2) {
			inventory.setInventorySlotContents(inventory.getSizeInventory() - 1, new ItemStack(Blocks.STONE));
		}
		addEntity(horse);

		runHandler(horse.getEntityId(), createClientContainer(horse));

		checkAllEntities();
	}
}
