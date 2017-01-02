package wdl.update;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * Hashes classes inside the jar.
 */
public class ClassHasher {
	// http://stackoverflow.com/a/9855338/3991344
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
		
		InputStream stream = null;
		try {
			stream = clazz.getResourceAsStream(file);
			if (stream == null) {
				throw new FileNotFoundException(file + " relative to "
						+ relativeTo);
			}
			DigestInputStream digestStream = null;
			try {
				digestStream = new DigestInputStream(stream, digest);
				
				while (digestStream.read() != -1); //Read entire stream
			} finally {
				if (digestStream != null) {
					digestStream.close();
				}
			}
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		
		return bytesToHex(digest.digest());
	}
}
