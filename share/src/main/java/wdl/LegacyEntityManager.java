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

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import wdl.api.IEntityAdder;
import wdl.api.IEntityManager;
import wdl.api.ISpecialEntityHandler;
import wdl.api.IWDLModDescripted;
import wdl.api.WDLApi;
import wdl.api.WDLApi.ModInfo;

/**
 * {@link IEntityManager} implementation that manages instances of
 * {@link IEntityAdder} or {@link ISpecialEntityHandler}.
 */
@SuppressWarnings("deprecation")
public class LegacyEntityManager implements IEntityManager, IWDLModDescripted {
	public LegacyEntityManager() {
	}
	@Override
	public boolean isValidEnvironment(String version) {
		return true;
	}
	@Override
	public String getEnvironmentErrorMessage(String version) {
		return null;
	}

	@Override
	public String getDisplayName() {
		return "Legacy entity API support";
	}
	@Override
	public String getMainAuthor() {
		return null;
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
		StringBuilder sb = new StringBuilder();
		sb.append("One or more extensions is using the deprecated entity API"
				+ " (IEntityAdder or ISpecialEntityHandler).  This extension"
				+ " is automatically added to provide compatability.");
		sb.append("\nImplementations of ISpecialEntityHandler:");
		for (ModInfo<ISpecialEntityHandler> info : WDLApi
				.getAllImplementingExtensions(ISpecialEntityHandler.class)) {
			sb.append('\n').append(info);
		}
		sb.append("\nImplementations of IEntityAdder:");
		for (ModInfo<IEntityAdder> info : WDLApi
				.getAllImplementingExtensions(IEntityAdder.class)) {
			sb.append('\n').append(info);
		}

		return sb.toString();
	}

	@Override
	public Set<String> getProvidedEntities() {
		Set<String> set = new HashSet<>();
		for (ModInfo<ISpecialEntityHandler> info : WDLApi
				.getImplementingExtensions(ISpecialEntityHandler.class)) {
			set.addAll(info.mod.getSpecialEntities().values());
		}
		for (ModInfo<IEntityAdder> info : WDLApi
				.getImplementingExtensions(IEntityAdder.class)) {
			set.addAll(info.mod.getModEntities());
		}

		return set;
	}

	@Override
	public String getIdentifierFor(Entity entity) {
		// This only provides old names, not minecraft:xxx names, but the old API
		// didn't support the new names anyways.
		String vanillaName = EntityList.getEntityString(entity);

		for (ModInfo<ISpecialEntityHandler> info : WDLApi
				.getImplementingExtensions(ISpecialEntityHandler.class)) {
			if (info.mod.getSpecialEntities().containsKey(vanillaName)) {
				return info.mod.getSpecialEntityName(entity);
			}
		}
		for (ModInfo<IEntityAdder> info : WDLApi
				.getImplementingExtensions(IEntityAdder.class)) {
			if (info.mod.getModEntities().contains(vanillaName)) {
				// Confirmed that one of the extensions uses that name.
				return vanillaName;
			}
		}
		return null;
	}

	@Override
	public int getTrackDistance(String identifier, Entity entity) {
		for (ModInfo<ISpecialEntityHandler> info : WDLApi
				.getImplementingExtensions(ISpecialEntityHandler.class)) {
			if (info.mod.getSpecialEntities().containsKey(identifier)) {
				return info.mod.getSpecialEntityTrackDistance(identifier);
			}
		}
		for (ModInfo<IEntityAdder> info : WDLApi
				.getImplementingExtensions(IEntityAdder.class)) {
			if (info.mod.getModEntities().contains(identifier)) {
				return info.mod.getDefaultEntityTrackDistance(identifier);
			}
		}
		return -1;
	}

	@Override
	public String getGroup(String identifier) {
		for (ModInfo<ISpecialEntityHandler> info : WDLApi
				.getImplementingExtensions(ISpecialEntityHandler.class)) {
			if (info.mod.getSpecialEntities().containsKey(identifier)) {
				return info.mod.getSpecialEntityCategory(identifier);
			}
		}
		for (ModInfo<IEntityAdder> info : WDLApi
				.getImplementingExtensions(IEntityAdder.class)) {
			if (info.mod.getModEntities().contains(identifier)) {
				return info.mod.getEntityCategory(identifier);
			}
		}
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

	@Override
	public boolean enabledByDefault(String identifier) {
		return true;
	}

}
