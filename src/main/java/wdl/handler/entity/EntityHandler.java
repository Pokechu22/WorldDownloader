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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;
import wdl.handler.BaseHandler;
import wdl.handler.HandlerException;
import wdl.versioned.VersionedFunctions;

/**
 * A handler for an arbitrary entity.
 *
 * <p>
 * Note that unlike block entity handlers, entity handlers DO accept subclasses
 * for the entity.  Additionally, note that they do not directly write the entity;
 * instead, they modify the information in the entity.
 *
 * @param <E>
 *            The type of block entity to handle.
 * @param <C>
 *            The type of container associated with that entity.
 */
public abstract class EntityHandler<E extends Entity, C extends Container> extends BaseHandler {
	/**
	 * Constructor.
	 *
	 * @param entityClass
	 *            A strong reference to the entity class this handles.
	 * @param containerClass
	 *            A strong reference to the container class this handles.
	 */
	protected EntityHandler(Class<E> entityClass, Class<C> containerClass) {
		this.entityClass = entityClass;
		this.containerClass = containerClass;
	}

	protected final @Nonnull Class<E> entityClass;
	protected final @Nonnull Class<C> containerClass;

	/** Gets the type of entity handled by this. */
	public final Class<E> getEntityClass() {
		return entityClass;
	}
	/** Gets the type of container handled by this. */
	public final Class<C> getContainerClass() {
		return containerClass;
	}

	/**
	 * Checks if the entity being ridden by the player is the owner of the given
	 * container, casting the parameters as necessary.
	 *
	 * <p>
	 * Most entities do not have an inventory that can be opened while riding them,
	 * but horses do; this must be distinguished from the inventory opened by
	 * looking at them, as one can be riding one horse while looking at another.
	 *
	 * @param container
	 *            The container that is open.
	 * @param entity
	 *            The entity that the player is riding (non-null!).
	 *
	 * @return True if the given entity is the owner of that container.
	 * @throws ClassCastException
	 *             If container or entity are not instances of the handled class.
	 */
	public final boolean checkRidingCasting(Container container, Entity entity)
			throws ClassCastException {
		C c = containerClass.cast(container);
		E e = entityClass.cast(entity);
		return checkRiding(c, e);
	}

	/**
	 * Checks if the entity being ridden by the player is the owner of the given
	 * container.
	 *
	 * <p>
	 * Most entities do not have an inventory that can be opened while riding them,
	 * but horses do; this must be distinguished from the inventory opened by
	 * looking at them, as one can be riding one horse while looking at another.
	 *
	 * @param container
	 *            The container that is open.
	 * @param entity
	 *            The entity that the player is riding (non-null!).
	 *
	 * @return True if the given entity is the owner of that container.
	 */
	public boolean checkRiding(C container, E entity) {
		return false;
	}

	/**
	 * Copies data from the given container into the entity, casting the parameters
	 * as necessary.
	 *
	 * @param container
	 *            The container to copy data from.
	 * @param entity
	 *            The entity to copy data into.
	 * @param riding
	 *            True if this is a ridden entity (generally not needed).
	 *
	 * @return A message to put into chat describing what was saved.
	 * @throws HandlerException
	 *             When something is handled wrong.
	 * @throws ClassCastException
	 *             If container or entity are not instances of the handled class.
	 */
	public final ITextComponent copyDataCasting(Container container, Entity entity, boolean riding)
			throws HandlerException, ClassCastException {
		C c = containerClass.cast(container);
		E e = entityClass.cast(entity);
		return copyData(c, e, riding);
	}

	/**
	 * Copies data from the given container into the entity.
	 *
	 * @param container
	 *            The container to copy data from.
	 * @param entity
	 *            The entity to copy data into.
	 * @param riding
	 *            True if this is a ridden entity (generally not needed).
	 *
	 * @return A message to put into chat describing what was saved.
	 * @throws HandlerException
	 *             When something is handled wrong.
	 */
	public abstract ITextComponent copyData(C container, E entity, boolean riding) throws HandlerException;

	/**
	 * Looks up the handler that handles the given block entity/container combo,
	 * from {@link VersionedFunctions#ENTITY_HANDLERS}.
	 *
	 * @param entityClass The type for the entity.
	 * @param containerClass The type for the container.
	 * @return The handler, or null if none is found.
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public static <E extends Entity, C extends Container> EntityHandler<? super E, ? super C> getHandler(Class<E> entityClass, Class<C> containerClass) {
		for (EntityHandler<?, ?> h : VersionedFunctions.ENTITY_HANDLERS) {
			if (h.getEntityClass().isAssignableFrom(entityClass) &&
					h.getContainerClass().isAssignableFrom(containerClass)) {
				return (EntityHandler<? super E, ? super C>) h;
			}
		}

		return null;
	}
}
