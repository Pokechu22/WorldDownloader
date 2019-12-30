/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
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
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.IAnimal;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityEvokerFangs;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityLlamaSpit;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.util.ResourceLocation;
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
			if (EntityMob.class.isAssignableFrom(c) ||
					EntitySlime.class.isAssignableFrom(c)) {
				return SpigotEntityType.MONSTER;
			} else if (EntityCreature.class.isAssignableFrom(c) ||
					EntityAmbientCreature.class.isAssignableFrom(c)) {
				return SpigotEntityType.ANIMAL;
			} else if (EntityItemFrame.class.isAssignableFrom(c) ||
					EntityPainting.class.isAssignableFrom(c) ||
					EntityItem.class.isAssignableFrom(c) ||
					EntityXPOrb.class.isAssignableFrom(c)) {
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
			ResourceLocation loc = EntityType.getKey(entity.getType());
			if (loc == null) {
				// Eg players
				return null;
			} else {
				return loc.toString();
			}
		}

		/**
		 * Gets the entity tracking range used by vanilla Minecraft.
		 * <p>
		 * Proper tracking ranges can be found in
		 * {@link EntityTracker#track(Entity)} - it's the 2nd argument given to
		 * addEntityToTracker.  These ranges need to be checked for each new entity.
		 * <p>
		 * This code can be generated via replacing
		 * <pre>\t+\} else if \(entityIn instanceof (.+)\) \{\r?\n\t+this\.track\(entityIn, (\d+), .+, (?:false|true)\);</pre>
		 * with
		 * <pre>\t\t\t} else if \(\1.class.isAssignableFrom\(c\)\) {\r\n\t\t\t\treturn \2;</pre>
		 * (and then doing some manual clean up)
		 */
		@Override
		public int getTrackDistance(String identifier, Entity entity) {
			Class<? extends Entity> c = entityClassFor(this, identifier);
			if (c == null) {
				return -1;
			}

			if (EntityFishHook.class.isAssignableFrom(c)) {
				return 64;
			} else if (EntityArrow.class.isAssignableFrom(c)) {
				return 64;
			} else if (EntitySmallFireball.class.isAssignableFrom(c)) {
				return 64;
			} else if (EntityFireball.class.isAssignableFrom(c)) {
				return 64;
			} else if (EntitySnowball.class.isAssignableFrom(c)) {
				return 64;
			} else if (EntityLlamaSpit.class.isAssignableFrom(c)) {
				return 64;
			} else if (EntityEnderPearl.class.isAssignableFrom(c)) {
				return 64;
			} else if (EntityEnderEye.class.isAssignableFrom(c)) {
				return 64;
			} else if (EntityEgg.class.isAssignableFrom(c)) {
				return 64;
			} else if (EntityPotion.class.isAssignableFrom(c)) {
				return 64;
			} else if (EntityExpBottle.class.isAssignableFrom(c)) {
				return 64;
			} else if (EntityFireworkRocket.class.isAssignableFrom(c)) {
				return 64;
			} else if (EntityItem.class.isAssignableFrom(c)) {
				return 64;
			} else if (EntityMinecart.class.isAssignableFrom(c)) {
				return 80;
			} else if (EntityBoat.class.isAssignableFrom(c)) {
				return 80;
			} else if (EntitySquid.class.isAssignableFrom(c)) {
				return 64;
			} else if (EntityWither.class.isAssignableFrom(c)) {
				return 80;
			} else if (EntityShulkerBullet.class.isAssignableFrom(c)) {
				return 80;
			} else if (EntityBat.class.isAssignableFrom(c)) {
				return 80;
			} else if (EntityDragon.class.isAssignableFrom(c)) {
				return 160;
			} else if (IAnimal.class.isAssignableFrom(c)) {
				return 80;
			} else if (EntityTNTPrimed.class.isAssignableFrom(c)) {
				return 160;
			} else if (EntityFallingBlock.class.isAssignableFrom(c)) {
				return 160;
			} else if (EntityHanging.class.isAssignableFrom(c)) {
				return 160;
			} else if (EntityArmorStand.class.isAssignableFrom(c)) {
				return 160;
			} else if (EntityXPOrb.class.isAssignableFrom(c)) {
				return 160;
			} else if (EntityAreaEffectCloud.class.isAssignableFrom(c)) {
				return 160;
			} else if (EntityEnderCrystal.class.isAssignableFrom(c)) {
				return 256;
			} else if (EntityEvokerFangs.class.isAssignableFrom(c)) {
				return 160;
			} else {
				return -1;
			}
		}

		@Override
		public String getGroup(String identifier) {
			Class<? extends Entity> c = entityClassFor(this, identifier);
			if (c == null) {
				return null;
			}

			if (IMob.class.isAssignableFrom(c)) {
				return "Hostile";
			} else if (IAnimal.class.isAssignableFrom(c)) {
				return "Passive";
			} else {
				return "Other";
			}
		}

		@Override
		public String getDisplayIdentifier(String identifier) {
			String i18nKey = EntityType.byKey(identifier).getTranslationKey();
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

		if (!EntityType.REGISTRY.containsKey(loc)) {
			return null;
		}

		Class<? extends Entity> c = EntityType.REGISTRY.get(loc).getEntityClass();
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
			PROVIDED_ENTITIES = EntityType.REGISTRY.stream()
					.filter(EntityType::isSerializable)
					.filter(EntityType::isSummonable)
					.map(EntityType::getKey)
					.map(ResourceLocation::toString)
					.collect(ImmutableSet.toImmutableSet());
		} catch (Throwable ex) {
			LOGGER.error("[WDL] Failed to load entity list: ", ex);
			throw new RuntimeException(ex);
		}
	}
}
