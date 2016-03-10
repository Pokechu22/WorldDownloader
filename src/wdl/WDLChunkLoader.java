package wdl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.storage.SaveHandler;

/**
 * Alternative implementation of {@link AnvilChunkLoader} that handles editing
 * WDL-specific properties of chunks as they are being saved.
 */
public class WDLChunkLoader extends AnvilChunkLoader {

	public static WDLChunkLoader create(SaveHandler handler,
			WorldProvider provider) {
		return new WDLChunkLoader(getWorldSaveFolder(handler, provider));
	}
	
	/**
	 * Gets the save folder for the given WorldProvider, respecting Forge's
	 * dimension names if forge is present.
	 */
	private static File getWorldSaveFolder(SaveHandler handler,
			WorldProvider provider) {
		File baseFolder = handler.getWorldDirectory();
		
		// Based off of AnvilSaveHandler.getChunkLoader, but also accounts
		// for forge changes.
		try {
			// See forge changes here:
			// https://github.com/MinecraftForge/MinecraftForge/blob/250a77b35936e7ac68006dfd28a9e93c6def9128/patches/minecraft/net/minecraft/world/WorldProvider.java.patch#L85-L93
			// https://github.com/MinecraftForge/MinecraftForge/blob/250a77b35936e7ac68006dfd28a9e93c6def9128/patches/minecraft/net/minecraft/world/chunk/storage/AnvilSaveHandler.java.patch
			Method forgeGetSaveFolderMethod = provider.getClass().getMethod("getSaveFolder");
			
			String name = (String) forgeGetSaveFolderMethod.invoke(provider);
			if (name != null) {
				File file = new File(baseFolder, name);
				file.mkdirs();
				return file;
			}
			return baseFolder;
		} catch (Exception e) {
			// Not a forge setup - emulate the vanilla method in 
			// AnvilSaveHandler.getChunkLoader.
			
			if (provider instanceof WorldProviderHell) {
				File file = new File(baseFolder, "DIM-1");
				file.mkdirs();
				return file;
			} else if (provider instanceof WorldProviderEnd) {
				File file = new File(baseFolder, "DIM1");
				file.mkdirs();
				return file;
			}
			
			return baseFolder;
		}
	}
	
	public WDLChunkLoader(File file) {
		super(file);
	}

	/**
	 * Saves the given chunk.
	 * 
	 * Note that while the normal implementation swallows Exceptions,
	 * this version does not.
	 */
	@Override
	public void saveChunk(World world, Chunk chunk)
			throws MinecraftException, IOException {
		world.checkSessionLock();
		
		NBTTagCompound levelTag = writeChunkToNBT(chunk, world);
		
		NBTTagCompound rootTag = new NBTTagCompound();
		rootTag.setTag("Level", levelTag);
		
		addChunkToPending(chunk.getChunkCoordIntPair(), rootTag);
	}
	
	/**
	 * Writes the given chunk, creating an NBT compound tag.
	 * 
	 * Note that this does <b>not</b> override the private method
	 * {@link AnvilChunkLoader#writeChunkToNBT(Chunk, World, NBTCompoundTag)}.
	 * That method is private and cannot be overridden; plus, this version
	 * returns a tag rather than modifying the one passed as an argument.
	 * However, that method
	 * 
	 * @param chunk
	 *            The chunk to write
	 * @param world
	 *            The world the chunk is in, used to determine the modified
	 *            time.
	 * @return A new NBTTagCompound
	 */
	private NBTTagCompound writeChunkToNBT(Chunk chunk, World world) {
		NBTTagCompound compound = new NBTTagCompound();
		
		compound.setByte("V", (byte) 1);
		compound.setInteger("xPos", chunk.xPosition);
		compound.setInteger("zPos", chunk.zPosition);
		compound.setLong("LastUpdate", world.getTotalWorldTime());
		compound.setIntArray("HeightMap", chunk.getHeightMap());
		compound.setBoolean("TerrainPopulated",
				chunk.isTerrainPopulated());
		compound.setBoolean("LightPopulated", chunk.isLightPopulated());
		compound.setLong("InhabitedTime", chunk.getInhabitedTime());
		ExtendedBlockStorage[] var4 = chunk.getBlockStorageArray();
		NBTTagList var5 = new NBTTagList();
		boolean var6 = !world.provider.getHasNoSky();
		ExtendedBlockStorage[] var7 = var4;
		int var8 = var4.length;
		NBTTagCompound var11;

		for (int var9 = 0; var9 < var8; ++var9) {
			ExtendedBlockStorage var10 = var7[var9];

			if (var10 != null) {
				var11 = new NBTTagCompound();
				var11.setByte("Y", (byte)(var10.getYLocation() >> 4 & 255));
				byte[] var12 = new byte[var10.getData().length];
				NibbleArray var13 = new NibbleArray();
				NibbleArray var14 = null;

				for (int var15 = 0; var15 < var10.getData().length; ++var15) {
					char var16 = var10.getData()[var15];
					int var17 = var15 & 15;
					int var18 = var15 >> 8 & 15;
					int var19 = var15 >> 4 & 15;

					if (var16 >> 12 != 0) {
						if (var14 == null) {
							var14 = new NibbleArray();
						}

						var14.set(var17, var18, var19, var16 >> 12);
					}

					var12[var15] = (byte)(var16 >> 4 & 255);
					var13.set(var17, var18, var19, var16 & 15);
				}

				var11.setByteArray("Blocks", var12);
				var11.setByteArray("Data", var13.getData());

				if (var14 != null) {
					var11.setByteArray("Add", var14.getData());
				}

				var11.setByteArray("BlockLight", var10.getBlocklightArray()
						.getData());

				if (var6) {
					var11.setByteArray("SkyLight", var10.getSkylightArray()
							.getData());
				} else {
					var11.setByteArray("SkyLight", new byte[var10
							.getBlocklightArray().getData().length]);
				}

				var5.appendTag(var11);
			}
		}

		compound.setTag("Sections", var5);
		compound.setByteArray("Biomes", chunk.getBiomeArray());
		chunk.setHasEntities(false);
		NBTTagList var20 = new NBTTagList();
		Iterator var22;

		for (var8 = 0; var8 < chunk.getEntityLists().length; ++var8) {
			var22 = chunk.getEntityLists()[var8].iterator();

			while (var22.hasNext()) {
				Entity var24 = (Entity) var22.next();
				var11 = new NBTTagCompound();

				if (var24.writeToNBTOptional(var11)) {
					chunk.setHasEntities(true);
					var20.appendTag(var11);
				}
			}
		}

		compound.setTag("Entities", var20);
		NBTTagList var21 = new NBTTagList();
		var22 = chunk.getTileEntityMap().values().iterator();

		while (var22.hasNext()) {
			TileEntity var25 = (TileEntity) var22.next();
			var11 = new NBTTagCompound();
			var25.writeToNBT(var11);
			var21.appendTag(var11);
		}

		compound.setTag("TileEntities", var21);
		List var23 = world.getPendingBlockUpdates(chunk, false);

		if (var23 != null) {
			long var26 = world.getTotalWorldTime();
			NBTTagList var27 = new NBTTagList();
			Iterator var28 = var23.iterator();

			while (var28.hasNext()) {
				NextTickListEntry var29 = (NextTickListEntry) var28.next();
				NBTTagCompound var30 = new NBTTagCompound();
				ResourceLocation var31 = (ResourceLocation) Block.blockRegistry
						.getNameForObject(var29.func_151351_a());
				var30.setString("i", var31 == null ? "" : var31.toString());
				var30.setInteger("x", var29.field_180282_a.getX());
				var30.setInteger("y", var29.field_180282_a.getY());
				var30.setInteger("z", var29.field_180282_a.getZ());
				var30.setInteger("t", (int)(var29.scheduledTime - var26));
				var30.setInteger("p", var29.priority);
				var27.appendTag(var30);
			}

			compound.setTag("TileTicks", var27);
		}
		
		return compound;
	}
}
