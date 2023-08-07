/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.versioned;

import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.nbt.StringNBT;

/**
 * 1.15 hides some NBT constructors; therefore, this is used to call
 * the relevant static methods instead in 1.15.
 */
final class NBTFunctions {
	private NBTFunctions() { throw new AssertionError(); }

	/* (non-javadoc)
	 * @see VersionedFunctions#nbtString
	 */
	static String nbtString(INBT tag) {
		return tag.toFormattedComponent("    ", 0).getString();
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#createFloatListTag
	 */
	static ListNBT createFloatListTag(float... values) {
		ListNBT result = new ListNBT();
		for (float value : values) {
			result.add(FloatNBT.valueOf(value));
		}
		return result;
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#createDoubleListTag
	 */
	static ListNBT createDoubleListTag(double... values) {
		ListNBT result = new ListNBT();
		for (double value : values) {
			result.add(DoubleNBT.valueOf(value));
		}
		return result;
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#createShortListTag
	 */
	static ListNBT createShortListTag(short... values) {
		ListNBT result = new ListNBT();
		for (short value : values) {
			result.add(ShortNBT.valueOf(value));
		}
		return result;
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#createStringTag
	 */
	static StringNBT createStringTag(String value) {
		return StringNBT.valueOf(value);
	}
}