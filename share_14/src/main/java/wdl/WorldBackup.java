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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import wdl.versioned.VersionedFunctions;

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
		ZIP("wdl.backup.zip", "wdl.saveProgress.backingUp.title.zip"),
		/**
		 * Backup via an external command.
		 */
		CUSTOM("wdl.backup.custom", "wdl.saveProgress.backingUp.title.custom");

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
		/**
		 * Called to check if the backup should be canceled.
		 */
		public abstract boolean shouldCancel();
	}

	/**
	 * A more generalized backup progress monitor, which supports custom backups.
	 */
	public static interface ICustomBackupProgressMonitor extends IBackupProgressMonitor {
		@Override
		public default void setNumberOfFiles(int num) {
			setDenominator(num, true);
		}
		@Override
		public default void onNextFile(String name) {
			incrementNumerator();
			onTextUpdate(I18n.format("wdl.saveProgress.backingUp.file", name));
		}

		/**
		 * Sets the denominator for the progress bar.
		 * Used for e.g. cases where only a percentage is known.
		 */
		public abstract void setDenominator(int value, boolean show);
		public abstract void incrementNumerator();
		/**
		 * Sets the numerator for the progress bar.
		 */
		public abstract void setNumerator(int value);
		/**
		 * A more general function used when there is new information to display.
		 * @param text The text to display.
		 */
		public abstract void onTextUpdate(String text);
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

	/** Exists only so that there is a subclass of IOException that looks more general */
	private static final class BackupFailedException extends IOException {
		private static final long serialVersionUID = 0;
		public BackupFailedException(String message) {
			super(message);
		}
	}

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
		assert type != WorldBackupType.CUSTOM;
		backupWorld0(worldFolder, worldName, type, monitor, null, null);
	}

	/**
	 * Backs up the given world with the selected type.
	 *
	 * @param worldFolder The folder that contains the world to backup.
	 * @param worldName The name of the world.
	 * @param type The type to backup with.
	 * @param monitor A monitor.
	 * @param customCommand The command to run for the custom backup type.
	 * @param customExtension The extension to use for the custom backup type.
	 *
	 * @throws IOException
	 */
	public static void backupWorld(File worldFolder, String worldName,
			WorldBackupType type, ICustomBackupProgressMonitor monitor,
			@Nullable String customCommand, @Nullable String customExtension) throws IOException {
		backupWorld0(worldFolder, worldName, type, monitor, customCommand, customExtension);
	}

	/**
	 * Backs up the world, requiring that for the custom type, monitor is a custom monitor.
	 */
	private static void backupWorld0(File worldFolder, String worldName,
			WorldBackupType type, IBackupProgressMonitor monitor,
			@Nullable String customCommand, @Nullable String customExtension) throws IOException {
		switch (type) {
		case NONE: {
			return;
		}
		case FOLDER: {
			String newWorldName = worldName + "_" + LocalDateTime.now().format(DATE_FORMAT);

			File destination = new File(worldFolder.getParentFile(),
					newWorldName);

			if (destination.exists()) {
				throw new BackupFailedException("Backup folder (" + destination +
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
				throw new BackupFailedException("Backup file (" + destination +
						") already exists!");
			}

			long size = zipDirectory(worldFolder, destination, monitor);
			VersionedFunctions.makeBackupToast(worldName, size);
			return;
		}
		case CUSTOM: {
			ICustomBackupProgressMonitor customMonitor = (ICustomBackupProgressMonitor)monitor;
			if (customCommand == null || customExtension == null) {
				// This should only be hit when backupWorld(File, String, WorldBackupType) is called,
				// not for the user using custom backup without specifying a value.
				throw new BackupFailedException("Cannot use the custom backup type without a command and extension: command=" + customCommand + ", extension=" + customExtension);
			}

			String archiveName = LocalDateTime.now().format(DATE_FORMAT) + "_" + worldName + "." + customExtension;

			File destination = new File(getBackupsFolder(),
					archiveName);

			if (destination.exists()) {
				throw new BackupFailedException("Backup file (" + destination +
						") already exists!");
			}

			long size = runCustomBackup(customCommand, worldFolder, destination, customMonitor);
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

	private static final String REPLACE_SOURCE = "${source}", REPLACE_DESTINATION = "${destination}";
	/**
	 * Runs the user-specified custom backup command.
	 *
	 * @param template The template to use to create the command to run.
	 * @param src The source folder.
	 * @param destination The destination file.
	 * @param monitor Uses to decide to cancel the process.
	 *
	 * @return The size of the created file.
	 */
	public static long runCustomBackup(String template, File src, File destination,
			ICustomBackupProgressMonitor monitor) throws IOException {
		if (!template.contains(REPLACE_SOURCE)) {
			throw new BackupFailedException("Command template must specify " + REPLACE_SOURCE);
		}
		if (!template.contains(REPLACE_DESTINATION)) {
			throw new BackupFailedException("Command template must specify " + REPLACE_DESTINATION);
		}
		// As per Runtime.exec(String, String[], File)
		StringTokenizer tokenizer = new StringTokenizer(template);
		List<String> args = new ArrayList<>();
		while (tokenizer.hasMoreTokens()) {
			String arg = tokenizer.nextToken();
			arg = arg.replace(REPLACE_SOURCE, src.getAbsolutePath());
			arg = arg.replace(REPLACE_DESTINATION, destination.getAbsolutePath());
			args.add(arg);
		}
		Process process = new ProcessBuilder(args)
				.redirectErrorStream(true)
				.start();

		try (InputStream processOutput = process.getInputStream()) {
			while (!process.waitFor(100, TimeUnit.MILLISECONDS)) { // .1 seconds / 2 ticks
				if (monitor.shouldCancel()) {
					process.destroyForcibly();
					throw new BackupFailedException("Backup was canceled");
				}

				updateProcessOutput(processOutput, monitor);
			}
			updateProcessOutput(processOutput, monitor);
		} catch (InterruptedException e) {
			try {
				process.destroyForcibly().waitFor();
			} catch (InterruptedException e2) {}
		}

		int exit = process.exitValue();
		if (exit != 0) {
			throw new BackupFailedException("Exit status " + exit + " from backup program isn't 0");
		}

		if (!destination.exists()) {
			throw new FileNotFoundException("Destination file wasn't created");
		}

		long size = destination.length();
		if (size < 128) {
			// If the file size is too small, it probably didn't actually compress anything
			// (for instance, 7-zip makes an empty archive if it can't find the input)
			// Granted, it _could_ actually have created something, but this almost
			// certainly indicates something went wrong
			throw new IOException(size + " bytes file was created; this is suspiciously small");
		}
		return size;
	}

	/**
	 * A pattern matching 7-Zip percentage progress output.
	 *
	 * This contains several groups in the normal case:
	 * <dl>
	 * <dt>percent</dt>
	 * <dd>The percentage of compression done, which can include sub-file
	 * progress.</dd>
	 * <dt>files</dt>
	 * <dd>The number of files that have finished processing. Might not be
	 * present.</dd>
	 * <dt>action</dt>
	 * <dd>A symbol that indicates what's happening. + means new file, U means
	 * existing file. See https://git.io/fpStn. Note that "Header creation" can also
	 * occur, but that lacks a file name so we don't handle it beyond capturing the
	 * percentage. Additionally, note that WDL doesn't actually do anything with the
	 * action.</dd>
	 * <dt>file</dt>
	 * <dd>The name of the file.</dd>
	 * </dl>
	 *
	 * This is tied directly to 7-zip, and really will only work with it. But 7-zip
	 * is the default choice, so that's fine.
	 */
	private static final Pattern SEVENZIP_PERCENTAGE_OUTPUT = Pattern.compile(
			"^(?<percent>\\d+)%(?: (?<files>\\d+))?(?: (?<action>[+UA=R.D]) (?<file>.+)| Header creation)?$");
	/**
	 * Updates the monitor with the last line of the process' output, without blocking.
	 */
	private static void updateProcessOutput(InputStream processOutput, ICustomBackupProgressMonitor monitor) throws IOException {
		int avail = processOutput.available();
		if (avail <= 0) return;

		byte[] bytes = new byte[avail];
		int read = processOutput.read(bytes);
		if (read <= 0) return;

		// Get the last line, allowing for newlines and backspaces
		// (assume any backspaces are a complete erasure of the current line)
		String message = new String(bytes, StandardCharsets.UTF_8);
		String[] parts = message.split("[\b\r\n]+");
		for (String part : parts) {
			String str = part.trim();
			if (!str.isEmpty()) {
				Matcher matcher = SEVENZIP_PERCENTAGE_OUTPUT.matcher(str);
				if (matcher.matches()) {
					int percent = Integer.parseInt(matcher.group("percent"));
					String file = matcher.group("file");
					if (file != null) {
						monitor.onNextFile(file);
					}
					monitor.setNumerator(percent);
					monitor.setDenominator(100, false);
				} else {
					monitor.onTextUpdate(str);
				}
			}
		}
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
			if (monitor.shouldCancel()) {
				throw new BackupFailedException("Backup was canceled");
			}
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
		if (monitor.shouldCancel()) {
			throw new BackupFailedException("Backup was canceled");
		}
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
