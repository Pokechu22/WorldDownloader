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
package wdl.update;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * Hashes classes inside the jar.
 */
public class ClassHasher {
	// https://stackoverflow.com/a/9855338/3991344
	private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	/**
	 * Calculates the hash of the given file.
	 *
	 * @param relativeTo
	 *            Name of class to use {@link Class#getResourceAsStream(String)}
	 *            on.
	 * @param file
	 *            The name of the file (passed to
	 *            {@link Class#getResourceAsStream(String)}).
	 * @return A string version of the hash.
	 * @throws ClassNotFoundException
	 *             When relativeTo does not exist.
	 * @throws FileNotFoundException
	 *             When file cannot be found relative to relativeTo.
	 * @throws Exception
	 *             When any other exception is raised.
	 */
	public static String hash(String relativeTo, String file)
			throws ClassNotFoundException, FileNotFoundException, Exception {
		Class<?> clazz = Class.forName(relativeTo);
		MessageDigest digest = MessageDigest.getInstance("MD5");

		try (InputStream stream = clazz.getResourceAsStream(file)) {
			if (stream == null) {
				throw new FileNotFoundException(file + " relative to "
						+ relativeTo);
			}
			try (DigestInputStream digestStream = new DigestInputStream(stream, digest)) {
				while (digestStream.read() != -1); //Read entire stream
			}
		}

		return bytesToHex(digest.digest());
	}
}
