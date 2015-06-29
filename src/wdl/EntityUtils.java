package wdl;

import java.lang.reflect.Field;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityArmorStand;

/**
 * Provides utility functions for recognising entities.
 */
public class EntityUtils {
	private static Logger logger = LogManager.getLogger();
	
	/**
	 * Reference to the {@link EntityList#stringToClassMapping} field.
	 */
	public static final Map<String, Class> stringToClassMapping;
	
	static {
		try {
			Map<String, Class> mapping = null;
			
			//Attempt to steal the 'stringToClassMapping' field. 
			for (Field field : EntityList.class.getDeclaredFields()) {
				if (field.getType().equals(Map.class)) {
					field.setAccessible(true);
					Map m = (Map)field.get(null);
					
					Map.Entry e = (Map.Entry)m.entrySet().toArray()[0];
					if (e.getKey() instanceof String && 
							e.getValue() instanceof Class) {
						mapping = (Map<String, Class>)m;
						break;
					}
				}
			}
			
			if (mapping == null) {
				throw new Error("WDL: Failed to find stringToClassMapping!");
			}
			
			stringToClassMapping = mapping;
		} catch (Exception e) {
			throw new Error("WDL: Failed to setup stringToClassMapping!");
		}
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
		return EntityList.getEntityString(e);
	}
}
