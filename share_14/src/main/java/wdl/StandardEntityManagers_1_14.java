/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;


import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.AmbientEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import wdl.EntityUtils.ISpigotEntityManager;
import wdl.EntityUtils.SpigotEntityType;
import wdl.api.IEntityManager;

/**
 * Standard implementations of IEntityManager. These implementations are used on
 * 1.13.
 */
class StandardEntityManagers {
	private StandardEntityManagers() { throw new AssertionError(); }

	// For most entities, we want them to be enabled by default. A few dangerous
	// entities should't be saved, though.
	private static final Set<String> DANGEROUS_ENTITIES = ImmutableSet.of(
			"minecraft:firework_rocket",
			"minecraft:ender_dragon",
			"minecraft:wither",
			"minecraft:tnt");

	public static final ISpigotEntityManager SPIGOT = new ISpigotEntityManager() {
		@Override
		public Set<String> getProvidedEntities() {
			if (WDL.INSTANCE.isSpigot()) {
				return PROVIDED_ENTITIES;
			} else {
				// Don't try to do spigot ranges on non-spigot servers
				return Collections.emptySet();
			}
		}

		@Override
		public String getIdentifierFor(Entity entity) {
			// Handled by default
			return null;
		}

		@Override
		public int getTrackDistance(String identifier, Entity entity) {
			return getSpigotType(identifier).getDefaultRange();
		}

		@Nonnull
		@Override
		public SpigotEntityType getSpigotType(String identifier) {
			Class<? extends Entity> c = entityClassFor(this, identifier);
			if (c == null) {
				return SpigotEntityType.UNKNOWN;
			}

			// Spigot's mapping, which is based off of bukkit inheritance (which
			// doesn't match vanilla)
			if (MobEntity.class.isAssignableFrom(c) ||
					SlimeEntity.class.isAssignableFrom(c)) {
				return SpigotEntityType.MONSTER;
			} else if (CreatureEntity.class.isAssignableFrom(c) ||
					AmbientEntity.class.isAssignableFrom(c)) {
				return SpigotEntityType.ANIMAL;
			} else if (ItemFrameEntity.class.isAssignableFrom(c) ||
					PaintingEntity.class.isAssignableFrom(c) ||
					ItemEntity.class.isAssignableFrom(c) ||
					ExperienceOrbEntity.class.isAssignableFrom(c)) {
				return SpigotEntityType.MISC;
			} else {
				return SpigotEntityType.OTHER;
			}
		}

		@Override
		public boolean enabledByDefault(String identifier) {
			return !DANGEROUS_ENTITIES.contains(identifier);
		}

		// Not intended to be used as a regular extension, so don't worry about
		// these methods
		@Override
		public boolean isValidEnvironment(String version) {
			return true;
		}
		@Override
		public String getEnvironmentErrorMessage(String version) {
			return null;
		}

		@Override
		public String getGroup(String identifier) {
			return null;
		}

		@Override
		public String getDisplayIdentifier(String identifier) {
			return null;
		}

		@Override
		public String getDisplayGroup(String group) {
			return null;
		}
	};

	public static final IEntityManager VANILLA = new IEntityManager() {
		@Override
		public Set<String> getProvidedEntities() {
			return PROVIDED_ENTITIES;
		}

		@Override
		public String getIdentifierFor(Entity entity) {
			ResourceLocation loc = EntityType.getId(entity.getType());
			if (loc == null) {
				// Eg players
				return null;
			} else {
				return loc.toString();
			}
		}

		/**
		 * Gets the entity tracking range used by vanilla Minecraft.
		 */
		@Override
		public int getTrackDistance(String identifier, Entity entity) {
			// Per ChunkManager.func_219210_a; hopefully this is right
			return entity.getType().func_220345_k() * 16;
		}

		@Override
		public String getGroup(String identifier) {
			Class<? extends Entity> c = entityClassFor(this, identifier);
			if (c == null) {
				return null;
			}

			if (IMob.class.isAssignableFrom(c)) {
				return "Hostile";
			} else if (AnimalEntity.class.isAssignableFrom(c)) {
				return "Passive";
			} else {
				return "Other";
			}
		}

		@Override
		public String getDisplayIdentifier(String identifier) {
			String i18nKey = EntityType.func_220327_a(identifier).get().getTranslationKey();
			if (I18n.hasKey(i18nKey)) {
				return I18n.format(i18nKey);
			} else {
				// We want to be clear that there is no result, rather than returning
				// the key (the default for failed formatting)
				// Since MC-68446 has been fixed, this shouldn't be hit normally,
				// but it's best to still be careful.
				return null;
			}
		}

		@Override
		public String getDisplayGroup(String group) {
			// TODO
			return null;
		}

		@Override
		public boolean enabledByDefault(String identifier) {
			return !DANGEROUS_ENTITIES.contains(identifier);
		}

		// Not intended to be used as a regular extension, so don't worry about
		// these methods
		@Override
		public boolean isValidEnvironment(String version) {
			return true;
		}
		@Override
		public String getEnvironmentErrorMessage(String version) {
			return null;
		}
	};

	/**
	 * Gets the entity class for that identifier.
	 */
	@VisibleForTesting
	static Class<? extends Entity> entityClassFor(IEntityManager manager, String identifier) {
		assert manager.getProvidedEntities().contains(identifier);

		ResourceLocation loc = new ResourceLocation(identifier);

		if (!Registry.ENTITY_TYPE.containsKey(loc)) {
			return null;
		}

		// XXX: This is not a good way of doing this
		Class<? extends Entity> c = Registry.ENTITY_TYPE.func_218349_b(loc).get().create(null).getClass();
		assert c != null;

		return c;
	}

	private static final Logger LOGGER = LogManager.getLogger();

	// XXX whatever happens with forge, this doesn't handle it yet

	/**
	 * As returned by {@link #getProvidedEntities()}
	 */
	private static final Set<String> PROVIDED_ENTITIES;
	static {
		try {
			PROVIDED_ENTITIES = Registry.ENTITY_TYPE.stream()
					.filter(EntityType::isSerializable)
					.filter(EntityType::isSummonable)
					.map(EntityType::getId)
					.map(ResourceLocation::toString)
					.collect(ImmutableSet.toImmutableSet());
		} catch (Throwable ex) {
			LOGGER.error("[WDL] Failed to load entity list: ", ex);
			throw new RuntimeException(ex);
		}
	}
}
