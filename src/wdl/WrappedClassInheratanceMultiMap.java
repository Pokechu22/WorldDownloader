package wdl;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.util.ClassInheratanceMultiMap;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

import com.google.common.collect.Multimap;

/**
 * Wraps a {@link ClassInheratanceMultiMap}, to help avoid
 * {@link ConcurrentModificationException}s.
 * 
 * The {@link #iterator()} and {@link #func_180215_b(Class)} methods will return
 * different things based off of the current stack trace -- if it contains
 * {@link AnvilChunkLoader}, the iterator for this instance is returned;
 * otherwise the iterator for the real one is returned.
 * 
 * Thus, this instance can be edited as wanted without causing any crashes (or
 * any entities to be removed from the actual world).
 * 
 * @see https://github.com/Pokechu22/WorldDownloader/issues/13
 */
public class WrappedClassInheratanceMultiMap<T> extends ClassInheratanceMultiMap {
	/**
	 * Creates a copy of a {@link ClassInheratanceMultiMap}.  This is ugly,
	 * but intended to avoid exceptions thrown durring rendering due to skipped
	 * entities.
	 * 
	 * @param original
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> WrappedClassInheratanceMultiMap<T> copyOf(
			ClassInheratanceMultiMap original) {
		Multimap<Class<?>, T> map = ReflectionUtils.stealAndGetField(original,
				ClassInheratanceMultiMap.class, Multimap.class);
		Set<T> set = ReflectionUtils.stealAndGetField(original,
				ClassInheratanceMultiMap.class, Set.class);
		Class<T> clazz = ReflectionUtils.stealAndGetField(original,
				ClassInheratanceMultiMap.class, Class.class);
		
		return new WrappedClassInheratanceMultiMap<T>(clazz, set, map, original);
	}

	private final ClassInheratanceMultiMap wrapped;
	
	@SuppressWarnings("unchecked")
	private WrappedClassInheratanceMultiMap(Class<T> clazz, Set<T> set,
			Multimap<Class<?>, T> map, ClassInheratanceMultiMap wrapped) {
		super(clazz);
		
		this.wrapped = wrapped;
		
		// Update the private fields.
		Set<T> ownSet = ReflectionUtils.stealAndGetField(this,
				ClassInheratanceMultiMap.class, Set.class);
		Multimap<Class<?>, T> ownMap = ReflectionUtils.stealAndGetField(this,
				ClassInheratanceMultiMap.class, Multimap.class);
		
		ownSet.clear();
		ownSet.addAll(set);
		
		ownMap.clear();
		ownMap.putAll(map);
	}
	
	/**
	 * Adds the given object to this map <b>and the wrapped map</b>.
	 * Assume that this is only going to be called when it is safe to do so.
	 * <hr/>
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(Object obj) {
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
	public boolean addWDL(Object obj) {
		return super.add(obj);
	}
	
	/**
	 * Ugly hack to allow iteration over this properly.
	 */
	@SuppressWarnings("unchecked")
	public Iterable<T> asIterable() {
		return (Iterable<T>) this;
	}
	
	/**
	 * Returns the iterator for the wrapped map unless the current stack trace
	 * includes {@link AnvilChunkLoader}, in which case the modified version
	 * is returned.
	 * <hr/>
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<T> iterator() {
		for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
			if (e.getClassName().equals(AnvilChunkLoader.class.getName())) {
				return super.iterator();
			}
		}
		return wrapped.iterator();
	}
	
	/**
	 * I have no idea what this does, but it does it based off of the stack
	 * trace as described in {@link #iterator()}.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Iterable func_180215_b(Class p_180215_1_) {
		for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
			if (e.getClassName().equals(AnvilChunkLoader.class.getName())) {
				return super.func_180215_b(p_180215_1_);
			}
		}
		return wrapped.func_180215_b(p_180215_1_);
	}
}
