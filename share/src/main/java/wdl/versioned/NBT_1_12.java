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

import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

/**
 * 1.15 hides some NBT constructors, which is why most of these functions exist.
 * This file exists in prior versions to deal with formatting of NBT as a string.
 */
class NBTFunctions {
	private NBTFunctions() { throw new AssertionError(); }

	/* (non-javadoc)
	 * @see VersionedFunctions#nbtString
	 */
	static String nbtString(INBTBase tag) {
		// No equivalent of toFormattedComponent or similar, so just try to make a
		// decent multi-line string
		String result = tag.toString();
		result = result.replaceAll("\\{", "\\{\n");
		result = result.replaceAll("\\}", "\n\\}");
		return result;
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#createFloatListTag
	 */
	static NBTTagList createFloatListTag(float... values) {
		NBTTagList result = new NBTTagList();
		for (float value : values) {
			result.add(new NBTTagFloat(value));
		}
		return result;
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#createDoubleListTag
	 */
	static NBTTagList createDoubleListTag(double... values) {
		NBTTagList result = new NBTTagList();
		for (double value : values) {
			result.add(new NBTTagDouble(value));
		}
		return result;
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#createShortListTag
	 */
	static NBTTagList createShortListTag(short... values) {
		NBTTagList result = new NBTTagList();
		for (short value : values) {
			result.add(new NBTTagShort(value));
		}
		return result;
	}

	/* (non-javadoc)
	 * @see VersionedFunctions#createStringTag
	 */
	static NBTTagString createStringTag(String value) {
		return new NBTTagString(value);
	}
}