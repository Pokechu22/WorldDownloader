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

import java.lang.reflect.Field;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.inventory.container.Container;

/**
 * Reflection utilities, mainly to work with private fields.
 * <p>
 * These methods iterate through the declared fields of a type, looking for a
 * matching one. This is done instead of directly accessing a certain field
 * because the name changes depending on contexts (notch, search, and dev). In
 * the future, this might be replaced with access transformers, though.
 * <p>
 * WARNING: It is undefined behavior to use these methods on a class with
 * multiple fields of the requested type (see the iteration order of
 * {@link Class#getDeclaredFields()}).
 */
public class ReflectionUtils {

	/**
	 * A mapping of containing classes to mappings of field types to fields, used
	 * to (slightly) boost performance.
	 */
	@VisibleForTesting
	static final Map<Class<?>, Map<Class<?>, Field>> CACHE = Maps.newHashMap();

	/**
	 * Uses Java's reflection API to find an inaccessible field of the given
	 * type in the given class.
	 * <p>
	 * This method's result is undefined if the given class has multiple
	 * fields of the same type.
	 *
	 * @param typeOfClass
	 *            Class that the field should be read from
	 * @param typeOfField
	 *            The type of the field
	 * @return The field, with {@link Field#setAccessible(boolean)} already called
	 */
	public static Field findField(Class<?> typeOfClass, Class<?> typeOfField) {
		if (CACHE.containsKey(typeOfClass)) {
			Map<Class<?>, Field> fields = CACHE.get(typeOfClass);
			if (fields.containsKey(typeOfField)) {
				return fields.get(typeOfField);
			}
		}

		Field[] fields = typeOfClass.getDeclaredFields();

		for (Field f : fields) {
			if (f.getType().equals(typeOfField)) {
				try {
					f.setAccessible(true);

					if (!CACHE.containsKey(typeOfClass)) {
						CACHE.put(typeOfClass, Maps.<Class<?>, Field>newHashMap());
					}

					CACHE.get(typeOfClass).put(typeOfField, f);

					return f;
				} catch (Exception e) {
					throw new RuntimeException(
							"WorldDownloader: Couldn't get private Field of type \""
									+ typeOfField + "\" from class \"" + typeOfClass
									+ "\" !", e);
				}
			}
		}

		throw new RuntimeException(
				"WorldDownloader: Couldn't find any Field of type \""
						+ typeOfField + "\" from class \"" + typeOfClass
						+ "\" !");
	}

	/**
	 * Uses Java's reflection API to get access to an inaccessible field
	 *
	 * @param object
	 *            Object that the field should be read from or the type of the
	 *            object if the field is static
	 * @param typeOfField
	 *            The type of the field
	 * @return The value of the field
	 */
	public static <T> T findAndGetPrivateField(Object object, Class<T> typeOfField) {
		Class<?> typeOfObject;

		if (object instanceof Class<?>) { // User asked for static field:
			typeOfObject = (Class<?>) object;
			object = null;
		} else {
			typeOfObject = object.getClass();
		}

		try {
			Field f = findField(typeOfObject, typeOfField);
			return typeOfField.cast(f.get(object));
		} catch (Exception e) {
			throw new RuntimeException(
					"WorldDownloader: Couldn't get Field of type \""
							+ typeOfField + "\" from object \"" + object
							+ "\" !", e);
		}
	}

	/**
	 * Uses Java's reflection API to set the value of an inaccessible field
	 *
	 * @param object
	 *            Object that the field should be set on or the type of the
	 *            object if the field is static
	 * @param typeOfField
	 *            The type of the field
	 * @param value
	 *            The value to set the field to.
	 */
	public static <T> void findAndSetPrivateField(Object object, Class<T> typeOfField,
			T value) {
		Class<?> typeOfObject;

		if (object instanceof Class) { // User asked for static field:
			typeOfObject = (Class<?>) object;
			object = null;
		} else {
			typeOfObject = object.getClass();
		}

		try {
			Field f = findField(typeOfObject, typeOfField);
			f.set(object, value);
		} catch (Exception e) {
			throw new RuntimeException(
					"WorldDownloader: Couldn't set Field of type \""
							+ typeOfField + "\" from object \"" + object
							+ "\" to " + value + "!", e);
		}
	}

	/**
	 * Uses Java's reflection API to get access to an inaccessible field
	 *
	 * @param object
	 *            Object that the field should be read from or null if the field
	 *            is static
	 * @param typeOfObject
	 *            The type of the object to use, for fields on (potentially)
	 *            superclasses
	 * @param typeOfField
	 *            The type of the field
	 * @return The value of the field
	 */
	public static <T, K> T findAndGetPrivateField(K object, Class<K> typeOfObject,
			Class<T> typeOfField) {
		try {
			Field f = findField(typeOfObject, typeOfField);
			return typeOfField.cast(f.get(object));
		} catch (Exception e) {
			throw new RuntimeException(
					"WorldDownloader: Couldn't get Field of type \""
							+ typeOfField + "\" from object \"" + object
							+ "\" !", e);
		}
	}

	/**
	 * Uses Java's reflection API to set the value of an inaccessible field
	 *
	 * @param object
	 *            Object that the field should be set on or null if the field
	 *            is static
	 * @param typeOfObject
	 *            The type of the object to use, for fields on (potentially)
	 *            superclasses
	 * @param typeOfField
	 *            The type of the field
	 * @param value
	 *            The value to set the field to.
	 */
	public static <T, K> void findAndSetPrivateField(K object, Class<K> typeOfObject,
			Class<T> typeOfField, T value) {
		try {
			Field f = findField(typeOfObject, typeOfField);
			f.set(object, value);
		} catch (Exception e) {
			throw new RuntimeException(
					"WorldDownloader: Couldn't set Field of type \""
							+ typeOfField + "\" from object \"" + object
							+ "\" to " + value + "!", e);
		}
	}

	/**
	 * Checks if the given class is
	 * {@link CreativeScreen.ContainerCreative}. In 1.12, this class is
	 * public, but in older versions (e.g. 1.10) it isn't.
	 * <p>
	 * Note that this implementation checks whether it's an inner class of
	 * {@link CreativeScreen}. This implementation works fine for versions
	 * with <code>InnerClasses</code> data, but older versions of Minecraft
	 * (1.8, but not 1.8.9) do not contain this data.  If 1.8 is eventually supported,
	 * this method will not work for it.
	 *
	 * @param containerClass
	 *            The class to check
	 */
	public static boolean isCreativeContainer(Class<? extends Container> containerClass) {
		try {
			return CreativeScreen.class.equals(containerClass.getEnclosingClass());
		} catch (Exception e) {
			// This one really should never happen (unless maybe an
			// external mod does some stupid security manager stuff)
			throw new RuntimeException(
					"WorldDownloader: Couldn't check if \""
							+ containerClass + "\" was the creative inventory!", e);
		}
	}

	// Old names, provided for extensions that may be using them
	@Deprecated
	public static Field stealField(Class<?> typeOfClass, Class<?> typeOfField) {
		return findField(typeOfClass, typeOfField);
	}
	@Deprecated
	public static <T> T stealAndGetField(Object object, Class<T> typeOfField) {
		return findAndGetPrivateField(object, typeOfField);
	}
	@SuppressWarnings("unchecked")
	@Deprecated
	public static <T> void stealAndSetField(Object object, Class<T> typeOfField,
			Object value) {
		findAndSetPrivateField(object, typeOfField, (T) value);
	}
	@SuppressWarnings("unchecked")
	@Deprecated
	public static <T> T stealAndGetField(Object object, Class<?> typeOfObject,
			Class<T> typeOfField) {
		// This is illegal... but needed.  And type erasure makes it fine.  Hopefully.
		Class<Object> oClass = (Class<Object>) typeOfObject;
		// Changed spec
		if (object instanceof Class<?>) {
			object = null;
		}
		return ReflectionUtils.findAndGetPrivateField(object, oClass, typeOfField);
	}
	@SuppressWarnings("unchecked")
	@Deprecated
	public static void stealAndSetField(Object object, Class<?> typeOfObject,
			Class<?> typeOfField, Object value) {
		// This is illegal... but needed.  And type erasure makes it fine.  Hopefully.
		Class<Object> oClass = (Class<Object>) typeOfObject;
		Class<Object> oField = (Class<Object>) typeOfField;
		// Changed spec
		if (object instanceof Class<?>) {
			object = null;
		}
		findAndSetPrivateField(object, oClass, oField, value);
	}
}
