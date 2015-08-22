package wdl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
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
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntityWitherSkull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides utility functions for recognising entities.
 */
public class EntityUtils {
	private static Logger logger = LogManager.getLogger();
	
	/**
	 * Reference to the {@link EntityList#stringToClassMapping} field.
	 */
	public static final Map<String, Class> stringToClassMapping;
	/**
	 * Reference to the {@link EntityList#classToStringMapping} field.
	 */
	public static final Map<Class, String> classToStringMapping;
	
	/**
	 * Names of all passive entities.
	 */
	public static final List<String> passiveEntityList;
	/**
	 * Names of all hostile entities.
	 */
	public static final List<String> hostileEntityList;
	/**
	 * Names of all other entities.  Includes hologram. 
	 */
	public static final List<String> otherEntityList;
	
	static {
		try {
			Map<String, Class> mappingSTC = null;
			Map<Class, String> mappingCTS = null;
			
			//Attempt to steal the 'stringToClassMapping' field. 
			for (Field field : EntityList.class.getDeclaredFields()) {
				if (field.getType().equals(Map.class)) {
					field.setAccessible(true);
					Map m = (Map)field.get(null);
					
					Map.Entry e = (Map.Entry)m.entrySet().toArray()[0];
					if (e.getKey() instanceof String && 
							e.getValue() instanceof Class) {
						mappingSTC = (Map<String, Class>)m;
					}
					if (e.getKey() instanceof Class &&
							e.getValue() instanceof String) {
						mappingCTS = (Map<Class, String>)m;
					}
				}
			}
			
			if (mappingSTC == null) {
				throw new Error("WDL: Failed to find stringToClassMapping!");
			}
			if (mappingCTS == null) {
				throw new Error("WDL: Failed to find classToStringMapping!");
			}
			
			stringToClassMapping = mappingSTC;
			classToStringMapping = mappingCTS;
			
			List<String> passiveEntities = new ArrayList<String>();
			List<String> hostileEntities = new ArrayList<String>();
			List<String> otherEntities = new ArrayList<String>();
			
			//Now build an actual list.
			for (Map.Entry<String, Class> e : 
					EntityUtils.stringToClassMapping.entrySet()) {
				String entity = e.getKey();
				Class c = e.getValue();
				
				if (Modifier.isAbstract(c.getModifiers())) {
					//Don't include abstract classes.
					continue;
				}
				
				if (IMob.class.isAssignableFrom(c)) {
					hostileEntities.add(entity);
				} else if (IAnimals.class.isAssignableFrom(c)) {
					passiveEntities.add(entity);
				} else {
					otherEntities.add(entity);
				}
			}
			
			otherEntities.add("Hologram");
			
			Collections.sort(hostileEntities, Collator.getInstance());
			Collections.sort(passiveEntities, Collator.getInstance());
			Collections.sort(otherEntities, Collator.getInstance());
			
			passiveEntityList = passiveEntities;
			hostileEntityList = hostileEntities;
			otherEntityList = otherEntities;
		} catch (Exception e) {
			throw new Error("WDL: Failed to setup entity mappings!");
		}
	}
	
	/**
	 * Gets a list of all types of entities.
	 */
	public static List<String> getEntityTypes() {
		List<String> returned = new ArrayList<String>();
		
		for (Map.Entry<String, Class> e : stringToClassMapping.entrySet()) {
			if (Modifier.isAbstract(e.getValue().getModifiers())) {
				continue;
			}
			
			returned.add(e.getKey());
		}
		
		returned.add("Hologram");
		
		return returned;
	}
	
	/**
	 * Gets the entity tracking range used by vanilla minecraft.
	 * <br/>
	 * Proper tracking ranges can be found in EntityTracker#trackEntity
	 * (the one that takes an Entity as a paremeter) -- it's the 2nd arg
	 * given to addEntityToTracker.
	 * 
	 * @param e
	 * @return 
	 */
	public static int getVanillaEntityRange(Entity e) {
		return getVanillaEntityRange(e.getClass());
	}
	
	/**
	 * Gets the entity tracking range used by vanilla minecraft.
	 * <br/>
	 * Proper tracking ranges can be found in EntityTracker#trackEntity
	 * (the one that takes an Entity as a paremeter) -- it's the 2nd arg
	 * given to addEntityToTracker.
	 * 
	 * @param type The vanilla minecraft entity string.
	 * @return 
	 */
	public static int getVanillaEntityRange(String type) {
		if (type == null) {
			return -1;
		}
		if (type.equals("Hologram")) {
			return getVanillaEntityRange(EntityWitherSkull.class);
		}
		return getVanillaEntityRange(stringToClassMapping.get(type));
	}
	
	/**
	 * Gets the entity tracking range used by vanilla minecraft.
	 * <br/>
	 * Proper tracking ranges can be found in EntityTracker#trackEntity
	 * (the one that takes an Entity as a paremeter) -- it's the 2nd arg
	 * given to addEntityToTracker.
	 * 
	 * @param c The entity class.
	 * @return
	 */
	public static int getVanillaEntityRange(Class<?> c) {
		if (c == null) {
			return -1;
		}
		if (EntityFishHook.class.isAssignableFrom(c)
				|| EntityArrow.class.isAssignableFrom(c)
				|| EntitySmallFireball.class.isAssignableFrom(c)
				|| EntityFireball.class.isAssignableFrom(c)
				|| EntitySnowball.class.isAssignableFrom(c)
				|| EntityEnderPearl.class.isAssignableFrom(c)
				|| EntityEnderEye.class.isAssignableFrom(c)
				|| EntityEgg.class.isAssignableFrom(c)
				|| EntityPotion.class.isAssignableFrom(c)
				|| EntityExpBottle.class.isAssignableFrom(c)
				|| EntityFireworkRocket.class.isAssignableFrom(c)
				|| EntityItem.class.isAssignableFrom(c)
				|| EntitySquid.class.isAssignableFrom(c)) {
			return 64;
		} else if (EntityMinecart.class.isAssignableFrom(c)
				|| EntityBoat.class.isAssignableFrom(c)
				|| EntityWither.class.isAssignableFrom(c)
				|| EntityBat.class.isAssignableFrom(c)
				|| IAnimals.class.isAssignableFrom(c)) {
			return 80; 
		} else if (EntityDragon.class.isAssignableFrom(c)
				|| EntityTNTPrimed.class.isAssignableFrom(c)
				|| EntityFallingBlock.class.isAssignableFrom(c)
				|| EntityHanging.class.isAssignableFrom(c)
				|| EntityXPOrb.class.isAssignableFrom(c)) {
			return 160;
		} else if (EntityEnderCrystal.class.isAssignableFrom(c)) {
			return 256;
		} else {
			return -1;
		}
	}
	
	/**
	 * Gets the track distance for the given entity in the current mode.
	 * 
	 * @param type
	 * @return
	 */
	public static int getEntityTrackDistance(String type) {
		return getEntityTrackDistance(
				WDL.worldProps.getProperty("Entity.TrackDistanceMode"), type);
	}
	
	/**
	 * Gets the track distance for the given entity in the current mode.
	 * 
	 * @param e
	 * @return
	 */
	public static int getEntityTrackDistance(Entity e) {
		return getEntityTrackDistance(
				WDL.worldProps.getProperty("Entity.TrackDistanceMode"), e);
	}
	
	/**
	 * Gets the track distance for the given entity in the current mode.
	 * 
	 * @param c
	 * @return
	 */
	public static int getEntityTrackDistance(Class<?> c) {
		return getEntityTrackDistance(
				WDL.worldProps.getProperty("Entity.TrackDistanceMode"), c);
	}
	
	/**
	 * Gets the track distance for the given entity in the given mode.
	 * 
	 * @param type
	 * @return
	 */
	public static int getEntityTrackDistance(String mode, String type) {
		if ("default".equals(mode)) {
			return getMostLikelyEntityTrackDistance(type);
		} else if ("server".equals(mode)) {
			int serverDistance = WDLPluginChannels.getEntityRange(type);
			
			if (serverDistance < 0) {
				return getMostLikelyEntityTrackDistance(type);
			}
			
			return serverDistance;
		} else if ("user".equals(mode)) {
			String prop = WDL.worldProps.getProperty("Entity." +
					type + ".TrackDistance");
			
			if (prop == null) {
				logger.warn("Entity range property is null for " + type + "!");
				logger.warn("(Tried key 'Entity." + type + ".TrackDistance'.)");
				return -1;
			}
			
			return Integer.valueOf(prop);
		} else {
			throw new IllegalArgumentException("Mode is not a valid mode: " + mode);
		}
	}
	
	/**
	 * Gets the track distance for the given entity in the given mode.
	 * 
	 * @param e
	 * @return
	 */
	public static int getEntityTrackDistance(String mode, Entity e) {
		return getEntityTrackDistance(mode, e.getClass());
	}
	
	/**
	 * Gets the track distance for the given entity in the given mode.
	 * 
	 * @param c
	 * @return
	 */
	public static int getEntityTrackDistance(String mode, Class<?> c) {
		return getEntityTrackDistance(mode, classToStringMapping.get(c));
	}
	
	/**
	 * Checks if an entity is enabled.
	 * 
	 * @param e The entity to check.
	 * @return
	 */
	public static boolean isEntityEnabled(Entity e) {
		return isEntityEnabled(getEntityType(e));
	}
	
	/**
	 * Checks if an entity is enabled.
	 * 
	 * @param type The type of the entity (from {@link #getEntityType(Entity)})
	 * @return
	 */
	public static boolean isEntityEnabled(String type) {
		if (!WDLPluginChannels.canSaveEntities()) {
			return false; //Shouldn't get here, but an extra check.
		}
		
		String prop = WDL.worldProps.getProperty("Entity." +
				type + ".Enabled");
		
		if (prop == null) {
			logger.warn("Entity enabled property is null for " + type + "!");
			logger.warn("(Tried key 'Entity." + type + ".Enabled'.)");
			return false;
		}
		
		return prop.equals("true");
	}
	
	/**
	 * Gets the type string for an entity.
	 * 
	 * @param e
	 * @return
	 */
	public static String getEntityType(Entity e) {
		if (isHologram(e)) {
			return "Hologram";
		}
		
		return EntityList.getEntityString(e);
	}
	
	/**
	 * Checks if an entity is a hologram.
	 * 
	 * Currently defined as an invisible horse.  TODO: Is this right?
	 * 
	 * @param e
	 * @return
	 */
	public static boolean isHologram(Entity e) {
		return (e instanceof EntityHorse &&
				e.isInvisible() &&
				((EntityHorse)e).hasCustomNameTag());
	}
	
	/**
	 * Gets the track distance for the given entity, making a guess about
	 * whether to use spigot track distances based off of the server brand.
	 */
	public static int getMostLikelyEntityTrackDistance(String entity) {
		// getClientBrand() returns the server's brand.  Blame MCP.
		if (WDL.thePlayer.getClientBrand().toLowerCase().contains("spigot")) {
			return getSpigotEntityRange(entity);
		} else {
			return getVanillaEntityRange(entity);
		}
	}
	
	/**
	 * Gets the entity range used by Spigot by default.
	 * Mostly a utility method for presets.
	 * 
	 * @param entity
	 * @return
	 */
	public static int getSpigotEntityRange(String entity) {
		if (entity.equals("Hologram")) {
			entity = "EntityHorse";
		}
		Class c = stringToClassMapping.get(entity);
		
		final int monsterRange = 48;
		final int animalRange = 48;
		final int miscRange = 32;
		final int otherRange = 64;
		
		//Spigot's mapping.  It's silly.
		if (EntityMob.class.isAssignableFrom(c) ||
				EntitySlime.class.isAssignableFrom(c)) {
			return monsterRange;
		} else if (EntityCreature.class.isAssignableFrom(c) ||
				EntityAmbientCreature.class.isAssignableFrom(c)) {
			return animalRange;
		} else if (EntityItemFrame.class.isAssignableFrom(c) ||
				EntityPainting.class.isAssignableFrom(c) ||
				EntityItem.class.isAssignableFrom(c) ||
				EntityXPOrb.class.isAssignableFrom(c)) {
			return miscRange;
		} else {
			return otherRange;
		}
	}
}
