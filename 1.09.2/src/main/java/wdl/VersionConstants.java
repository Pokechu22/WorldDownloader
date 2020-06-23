/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl;

import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.realms.RealmsSharedConstants;

/**
 * Contains constants that vary between versions of the mod.
 */
public class VersionConstants {
	/**
	 * Gets the current protocol version number.
	 *
	 * @return A <a href="https://wiki.vg/Protocol_version_numbers">protocol
	 *         version number</a>, eg <samp>316</samp>.
	 */
	public static int getProtocolVersion() {
		return RealmsSharedConstants.NETWORK_PROTOCOL_VERSION;
	}

	/**
	 * Gets the name of the version currently being run, which may change at
	 * runtime.
	 *
	 * @return A version string, eg <samp>1.11</samp>.
	 */
	public static String getMinecraftVersion() {
		return RealmsSharedConstants.VERSION_STRING;
	}

	/**
	 * Gets the current data version, as needed for the <code>DataVersion</code>
	 * tag. Note that unfortunately neither wiki gives a list of these numbers
	 * at the moment.
	 *
	 * @return A version number, eg <samp>819</samp>.
	 */
	public static int getDataVersion() {
		// As per AnvilChunkLoader
		return 176;
	}

	/**
	 * Gets the version this build is expected to run on.
	 *
	 * @return A version string, eg <samp>1.11</samp>.
	 */
	public static String getExpectedVersion() {
		return "1.9.2";
	}

	/**
	 * Gets version info similar to the info in F3.
	 *
	 * @return A version info string, eg <samp>Minecraft 1.9 (1.9/vanilla)</samp>
	 */
	public static String getMinecraftVersionInfo() {
		String version = getMinecraftVersion();
		// Gets the launched version (appears in F3)
		String launchedVersion = Minecraft.getInstance().getVersion();
		String brand = ClientBrandRetriever.getClientModName();
		String versionType = Minecraft.getInstance().getVersionType();

		return String.format("Minecraft %s (%s/%s/%s)", version,
				launchedVersion, brand, versionType);
	}

	/**
	 * Gets the current version of the mod.
	 *
	 * @return A version string, eg <samp>4.0.0.0</samp>
	 */
	public static String getModVersion() {
		// TODO: Automatically generate this somehow
		return "4.0.7.1-SNAPSHOT";
	}

	/**
	 * Checks if this version is one of the older, untested versions.
	 *
	 * @return true if this version is not tested and a disclaimer should be printed on startup
	 */
	public static boolean isUntestedVersion() {
		return true;
	}

	/**
	 * True if this version shades the FastUtil library; false if it's included
	 * with Minecraft directly.
	 *
	 * @return true if FastUtil is shaded
	 */
	public static boolean shadesFastUtil() {
		return true;
	}
}
