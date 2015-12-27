package wdl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.world.chunk.storage.AnvilChunkLoader;

/**
 * Wraps a {@link List}, to help avoid
 * {@link List}s.
 * 
 * The {@link #iterator()} method will return
 * different things based off of the current stack trace -- if it contains
 * {@link AnvilChunkLoader}, the iterator for this instance is returned;
 * otherwise the iterator for the real one is returned.
 * 
 * Thus, this instance can be edited as wanted without causing any crashes (or
 * any entities to be removed from the actual world).
 * 
 * @see https://github.com/Pokechu22/WorldDownloader/issues/13
 */
public class WrappedEntityList<T> extends ArrayList<T> {
	@SuppressWarnings("unchecked")
	public static <T> WrappedEntityList<T> copyOf(List<?> toWrap) {
		return new WrappedEntityList<T>((List<T>)toWrap);
	}
	
	private final List<T> wrapped;
	
	private WrappedEntityList(List<T> wrapped) {
		super(wrapped);
		
		this.wrapped = wrapped;
	}
	
	/**
	 * Adds the given object to this map <b>and the wrapped map</b>.
	 * Assume that this is only going to be called when it is safe to do so.
	 * <hr/>
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(T obj) {
		super.add(obj);
		return wrapped.add(obj);
	}
	
	/**
	 * Removes the given object from this map <b>and the wrapped map</b>.
	 * Assume that this is only going to be called when it is safe to do so.
	 * <hr/>
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(Object obj) {
		super.remove(obj);
		return wrapped.remove(obj);
	}
	
	/**
	 * Removes the given object from this map <b>but not the wrapped map</b>.
	 * When the map is reverted, these changes will be undone.
	 * 
	 * @param obj
	 * @return
	 */
	public boolean removeWDL(Object obj) {
		return super.remove(obj);
	}
	
	/**
	 * Adds the given object to this map <b>but not the wrapped map</b>.
	 * When the map is reverted, these changes will be undone.
	 * 
	 * @param obj
	 * @return
	 */
	public boolean addWDL(T obj) {
		return super.add(obj);
	}
	
	/**
	 * Returns the iterator for the wrapped map unless the current stack trace
	 * includes {@link AnvilChunkLoader}, in which case the modified version
	 * is returned.
	 * <hr/>
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<T> iterator() {
		for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
			if (e.getClassName().equals(AnvilChunkLoader.class.getName())) {
				return super.iterator();
			}
		}
		return wrapped.iterator();
	}
}
