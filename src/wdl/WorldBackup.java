package wdl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class WorldBackup {
	public static enum WorldBackupType {
		NONE("No backup"),
		FOLDER("Copy world folder"),
		ZIP("Zip a copy of a world");

		public final String description;

		private WorldBackupType(String description) {
			this.description = description;
		}
		
		public static WorldBackupType match(String name) {
			for (WorldBackupType type : WorldBackupType.values()) {
				if (type.name().equalsIgnoreCase(name)) {
					return type;
				}
			}
			
			return WorldBackupType.NONE;
		}
	}

	/**
	 * The format that is used for world date saving.
	 * 
	 * TODO: Allow modification ingame? 
	 */
	private static final DateFormat DATE_FORMAT = 
			new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

	/**
	 * Backs up the given world with the selected type.
	 * 
	 * @param worldFolder The folder that contains the world to backup.
	 * @param worldName The name of the world.
	 * @param type The type to backup with.
	 * 
	 * @throws IOException
	 */
	public static void backupWorld(File worldFolder, String worldName,
			WorldBackupType type) throws IOException {
		String newWorldName = worldName + "_" + DATE_FORMAT.format(new Date());
		
		switch (type) {
		case NONE: {
			return;
		}
		case FOLDER: {
			File destination = new File(worldFolder.getParentFile(),
					newWorldName);
			
			if (destination.exists()) {
				throw new IOException("Backup folder (" + destination + 
						") already exists!");
			}
			
			copyDirectory(worldFolder, destination);
			return;
		}
		case ZIP: {
			File destination = new File(worldFolder.getParentFile(), 
					newWorldName + ".zip");
			
			if (destination.exists()) {
				throw new IOException("Backup file (" + destination + 
						") already exists!");
			}
			
			zipDirectory(worldFolder, destination);
			return;
		}
		}
	}

	/**
	 * Copies a directory.
	 */
	public static void copyDirectory(File src, File destination)
			throws IOException {
		FileUtils.copyDirectory(src, destination);
	}
	
	/**
	 * Zips a directory.
	 */
	public static void zipDirectory(File src, File destination)
			throws IOException {
		FileOutputStream outStream = null;
		ZipOutputStream stream = null;
		try {
			outStream = new FileOutputStream(destination);
			try {
				stream = new ZipOutputStream(outStream);
				zipFolder(src, stream, src.getPath().length() + 1);
			} finally {
				stream.close();
			}
		} finally {
			outStream.close();
		}
	}
	
	/**
	 * Recursively adds a folder to a zip stream.
	 * 
	 * @param folder The folder to zip.
	 * @param stream The stream to write to.
	 * @param pathStartIndex The start of the file path.
	 * 
	 * @throws IOException
	 */
	private static void zipFolder(File folder, ZipOutputStream stream,
			int pathStartIndex) throws IOException {
		for (File file : folder.listFiles()) {
			if (file.isFile()) {
				ZipEntry zipEntry = new ZipEntry(file.getPath().substring(
						pathStartIndex));
				stream.putNextEntry(zipEntry);
				FileInputStream inputStream = new FileInputStream(file);
				try {
					IOUtils.copy(inputStream, stream);
				} finally {
					inputStream.close();
				}
				stream.closeEntry();
			} else if (file.isDirectory()) {
				zipFolder(file, stream, pathStartIndex);
			}
		}
	}
	
	private WorldBackup() { }
}
