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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import wdl.versioned.VersionedFunctions;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Performs backup of worlds.
 */
public class WorldBackup {
	/**
	 * Different modes for backups.
	 */
	public static enum WorldBackupType {
		/**
		 * No backup is performed.
		 */
		NONE("wdl.backup.none", ""),
		/**
		 * The world folder is copied.
		 */
		FOLDER("wdl.backup.folder", "wdl.saveProgress.backingUp.title.folder"),
		/**
		 * The world folder is copied to a zip folder.
		 */
		ZIP("wdl.backup.zip", "wdl.saveProgress.backingUp.title.zip");

		/**
		 * I18n key for the description (used on buttons).
		 */
		public final String descriptionKey;
		/**
		 * I18n key for the backup screen title.
		 */
		public final String titleKey;

		private WorldBackupType(String descriptionKey, String titleKey) {
			this.descriptionKey = descriptionKey;
			this.titleKey = titleKey;
		}

		/**
		 * Gets the (translated) description for this backup type.
		 * @return
		 */
		public String getDescription() {
			return I18n.format(descriptionKey);
		}

		/**
		 * Gets the (translated) title for this backup type.
		 * @return
		 */
		public String getTitle() {
			return I18n.format(titleKey);
		}
	}

	/**
	 * Something that listens to backup progress.
	 */
	public static interface IBackupProgressMonitor {
		/**
		 * Sets the initial number of files.
		 */
		public abstract void setNumberOfFiles(int num);
		/**
		 * Called on the next file.
		 * @param name Name of the new file.
		 */
		public abstract void onNextFile(String name);
	}

	/**
	 * The format that is used for world date saving.
	 */
	private static final DateTimeFormatter DATE_FORMAT = new DateTimeFormatterBuilder()
			.appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
			.appendLiteral('-')
			.appendValue(ChronoField.MONTH_OF_YEAR, 2)
			.appendLiteral('-')
			.appendValue(ChronoField.DAY_OF_MONTH, 2)
			.appendLiteral('_')
			.appendValue(ChronoField.HOUR_OF_DAY, 2)
			.appendLiteral('-')
			.appendValue(ChronoField.MINUTE_OF_HOUR, 2)
			.appendLiteral('-')
			.appendValue(ChronoField.SECOND_OF_MINUTE, 2)
			.toFormatter();

	/**
	 * Gets the .minecraft/backups folder.
	 */
	private static File getBackupsFolder() {
		File file = new File(Minecraft.getInstance().gameDir, "backups");
		file.mkdirs();
		return file;
	}

	/**
	 * Backs up the given world with the selected type.
	 *
	 * @param worldFolder The folder that contains the world to backup.
	 * @param worldName The name of the world.
	 * @param type The type to backup with.
	 * @param monitor A monitor.
	 *
	 * @throws IOException
	 */
	public static void backupWorld(File worldFolder, String worldName,
			WorldBackupType type, IBackupProgressMonitor monitor) throws IOException {

		switch (type) {
		case NONE: {
			return;
		}
		case FOLDER: {
			String newWorldName = worldName + "_" + LocalDateTime.now().format(DATE_FORMAT);

			File destination = new File(worldFolder.getParentFile(),
					newWorldName);

			if (destination.exists()) {
				throw new IOException("Backup folder (" + destination +
						") already exists!");
			}

			long size = copyDirectory(worldFolder, destination, monitor);
			VersionedFunctions.makeBackupToast(worldName, size);
			return;
		}
		case ZIP: {
			String archiveName = LocalDateTime.now().format(DATE_FORMAT) + "_" + worldName + ".zip";

			File destination = new File(getBackupsFolder(),
					archiveName);

			if (destination.exists()) {
				throw new IOException("Backup file (" + destination +
						") already exists!");
			}

			long size = zipDirectory(worldFolder, destination, monitor);
			VersionedFunctions.makeBackupToast(worldName, size);
			return;
		}
		}
	}

	/**
	 * Copies a directory.
	 * @return The size of the created copy.
	 */
	public static long copyDirectory(File src, File destination,
			IBackupProgressMonitor monitor) throws IOException {
		monitor.setNumberOfFiles(countFilesInFolder(src));

		return copy(src, destination, src.getPath().length() + 1, monitor);
	}

	/**
	 * Zips a directory.
	 * @return The size of the created file.
	 */
	public static long zipDirectory(File src, File destination,
			IBackupProgressMonitor monitor) throws IOException {
		monitor.setNumberOfFiles(countFilesInFolder(src));

		try (FileOutputStream outStream = new FileOutputStream(destination)) {
			try (ZipOutputStream stream = new ZipOutputStream(outStream)) {
				zipFolder(src, stream, src.getPath().length() + 1, monitor);
			}
		}

		return destination.length();
	}

	/**
	 * Recursively adds a folder to a zip stream.
	 *
	 * @param folder The folder to zip.
	 * @param stream The stream to write to.
	 * @param pathStartIndex The start of the file path.
	 * @param monitor A monitor.
	 *
	 * @throws IOException
	 */
	private static void zipFolder(File folder, ZipOutputStream stream,
			int pathStartIndex, IBackupProgressMonitor monitor) throws IOException {
		for (File file : folder.listFiles()) {
			if (file.isFile()) {
				String name = file.getPath().substring(pathStartIndex);
				monitor.onNextFile(name);
				ZipEntry zipEntry = new ZipEntry(name);
				stream.putNextEntry(zipEntry);
				try (FileInputStream inputStream = new FileInputStream(file)) {
					IOUtils.copy(inputStream, stream);
				}
				stream.closeEntry();
			} else if (file.isDirectory()) {
				zipFolder(file, stream, pathStartIndex, monitor);
			}
		}
	}

	/**
	 * Copies a series of files from one folder to another.
	 *
	 * @param from The file to copy.
	 * @param to The new location for the file.
	 * @param pathStartIndex The start of the file path.
	 * @param monitor A monitor.
	 * @return Copied file size
	 * @throws IOException
	 */
	private static long copy(File from, File to, int pathStartIndex,
			IBackupProgressMonitor monitor) throws IOException {
		if (from.isDirectory()) {
			if (!to.exists()) {
				to.mkdir();
			}

			long size = 0;
			for (String fileName : from.list()) {
				size += copy(new File(from, fileName),
						new File(to, fileName), pathStartIndex, monitor);
			}
			return size;
		} else {
			monitor.onNextFile(to.getPath().substring(pathStartIndex));
			//Yes, FileUtils#copyDirectory exists, but we can't monitor the
			//progress using it.
			FileUtils.copyFile(from, to, true);
			return from.length();
		}
	}

	/**
	 * Recursively counts the number of files in the given folder.
	 * Directories are not included in this count, but the files
	 * contained within are.
	 *
	 * @param folder
	 */
	private static int countFilesInFolder(File folder) {
		if (!folder.isDirectory()) {
			return 0;
		}

		int count = 0;
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				count += countFilesInFolder(file);
			} else {
				count++;
			}
		}

		return count;
	}

	private WorldBackup() { }
}
