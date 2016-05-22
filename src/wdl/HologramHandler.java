package wdl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.projectile.EntityWitherSkull;
import wdl.api.ISpecialEntityHandler;
import wdl.api.IWDLMod;
import wdl.api.IWDLModDescripted;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Handles holograms.
 * <br/> 
 * Right now, the definition of a hologram is a Horse riding a skull.
 * <br/>
 * This is also an example of how a {@link IWDLMod} would be implemented.
 */
public class HologramHandler implements ISpecialEntityHandler,
		IWDLModDescripted {
	
	@Override
	public String getDisplayName() {
		return "Hologram support";
	}
	
	@Override
	public Multimap<String, String> getSpecialEntities() {
		Multimap<String, String> returned = HashMultimap.<String, String>create();
		
		returned.put("EntityHorse", "Hologram");
		
		return returned;
	}

	@Override
	public String getSpecialEntityName(Entity entity) {
		if (entity instanceof EntityHorse &&
				entity.ridingEntity != null &&
				entity.ridingEntity instanceof EntityWitherSkull) {
			return "Hologram";
		}
		
		return null;
	}

	@Override
	public String getSpecialEntityCategory(String name) {
		if (name.equals("Hologram")) {
			return "Other";
		}
		return null;
	}

	@Override
	public int getSpecialEntityTrackDistance(String name) {
		return -1;
	}

	@Override
	public String getMainAuthor() {
		return "Pokechu22";
	}

	@Override
	public String[] getAuthors() {
		return null;
	}

	@Override
	public String getURL() {
		return null;
	}

	@Override
	public String getDescription() {
		return "Provides basic support for disabling holograms.";
	}

	@Override
	public boolean isValidEnvironment(String version) {
		return true;
	}

	@Override
	public String getEnvironmentErrorMessage(String version) {
		return null;
	}
}
