package wdl;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

/**
 * Wraps a {@link ClassInheritanceMultiMap}, to help avoid
 * {@link ConcurrentModificationException}s.
 * 
 * The {@link #iterator()} and {@link #getByClass(Class)} methods will return
 * different things based off of the current stack trace -- if it contains
 * {@link AnvilChunkLoader}, the iterator for this instance is returned;
 * otherwise the iterator for the real one is returned.
 * 
 * Thus, this instance can be edited as wanted without causing any crashes (or
 * any entities to be removed from the actual world).
 * 
 * @see https://github.com/Pokechu22/WorldDownloader/issues/13
 */
public class WrappedClassInheratanceMultiMap<T> extends ClassInheritanceMultiMap<T> {
	/**
	 * Creates a copy of a {@link ClassInheritanceMultiMap}.  This is ugly,
	 * but intended to avoid exceptions thrown durring rendering due to skipped
	 * entities.
	 * 
	 * @param original
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> WrappedClassInheratanceMultiMap<T> copyOf(
			ClassInheritanceMultiMap<T> original) {
		Map<Class<?>, List<T>> map = ReflectionUtils.stealAndGetField(original,
				ClassInheritanceMultiMap.class, Map.class);
		Set<T> set = ReflectionUtils.stealAndGetField(original,
				ClassInheritanceMultiMap.class, Set.class);
		Class<T> clazz = ReflectionUtils.stealAndGetField(original,
				ClassInheritanceMultiMap.class, Class.class);
		List<T> values = ReflectionUtils.stealAndGetField(original,
				ClassInheritanceMultiMap.class, List.class);
		
		return new WrappedClassInheratanceMultiMap<T>(clazz, set, map, values,
				original);
	}

	private final ClassInheritanceMultiMap<T> wrapped;
	
	@SuppressWarnings("unchecked")
	private WrappedClassInheratanceMultiMap(Class<T> clazz, Set<T> set,
			Map<Class<?>, List<T>> map, List<T> list,
			ClassInheritanceMultiMap<T> wrapped) {
		super(clazz);
		
		this.wrapped = wrapped;
		
		// Update the private fields.
		Set<T> ownSet = ReflectionUtils.stealAndGetField(this,
				ClassInheritanceMultiMap.class, Set.class);
		Map<Class<?>, List<T>> ownMap = ReflectionUtils.stealAndGetField(this,
				ClassInheritanceMultiMap.class, Map.class);
		List<T> ownList = ReflectionUtils.stealAndGetField(this,
				ClassInheritanceMultiMap.class, List.class);
		
		ownSet.clear();
		ownSet.addAll(set);
		
		ownMap.clear();
		ownMap.putAll(map);
		
		ownList.clear();
		ownList.addAll(list);
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
	
	/**
	 * I have no idea what this does, but it does it based off of the stack
	 * trace as described in {@link #iterator()}.
	 */
	@Override
	public <S> Iterable<S> getByClass(Class<S> p_180215_1_) {
		for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
			if (e.getClassName().equals(AnvilChunkLoader.class.getName())) {
				return super.getByClass(p_180215_1_);
			}
		}
		return wrapped.getByClass(p_180215_1_);
	}
}
