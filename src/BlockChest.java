// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

package net.minecraft.src;

import java.util.Random;

import javax.swing.text.html.HTMLDocument.BlockElement;

// Referenced classes of package net.minecraft.src:
//            BlockContainer, World, EntityPlayer, EntityItem, 
//            ItemStack, TileEntityChest, Material, IInventory, 
//            InventoryLargeChest, Block, IBlockAccess, TileEntity

public class BlockChest extends BlockContainer
{

    protected BlockChest(int i)
    {
        super(i, Material.wood);
        random = new Random();
        blockIndexInTexture = 26;
    }

    public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l)
    {
        if(l == 1)
        {
            return blockIndexInTexture - 1;
        }
        if(l == 0)
        {
            return blockIndexInTexture - 1;
        }
        int i1 = iblockaccess.getBlockId(i, j, k - 1);
        int j1 = iblockaccess.getBlockId(i, j, k + 1);
        int k1 = iblockaccess.getBlockId(i - 1, j, k);
        int l1 = iblockaccess.getBlockId(i + 1, j, k);
        if(i1 == blockID || j1 == blockID)
        {
            if(l == 2 || l == 3)
            {
                return blockIndexInTexture;
            }
            int i2 = 0;
            if(i1 == blockID)
            {
                i2 = -1;
            }
            int k2 = iblockaccess.getBlockId(i - 1, j, i1 != blockID ? k + 1 : k - 1);
            int i3 = iblockaccess.getBlockId(i + 1, j, i1 != blockID ? k + 1 : k - 1);
            if(l == 4)
            {
                i2 = -1 - i2;
            }
            byte byte1 = 5;
            if((Block.opaqueCubeLookup[k1] || Block.opaqueCubeLookup[k2]) && !Block.opaqueCubeLookup[l1] && !Block.opaqueCubeLookup[i3])
            {
                byte1 = 5;
            }
            if((Block.opaqueCubeLookup[l1] || Block.opaqueCubeLookup[i3]) && !Block.opaqueCubeLookup[k1] && !Block.opaqueCubeLookup[k2])
            {
                byte1 = 4;
            }
            return (l != byte1 ? blockIndexInTexture + 32 : blockIndexInTexture + 16) + i2;
        }
        if(k1 == blockID || l1 == blockID)
        {
            if(l == 4 || l == 5)
            {
                return blockIndexInTexture;
            }
            int j2 = 0;
            if(k1 == blockID)
            {
                j2 = -1;
            }
            int l2 = iblockaccess.getBlockId(k1 != blockID ? i + 1 : i - 1, j, k - 1);
            int j3 = iblockaccess.getBlockId(k1 != blockID ? i + 1 : i - 1, j, k + 1);
            if(l == 3)
            {
                j2 = -1 - j2;
            }
            byte byte2 = 3;
            if((Block.opaqueCubeLookup[i1] || Block.opaqueCubeLookup[l2]) && !Block.opaqueCubeLookup[j1] && !Block.opaqueCubeLookup[j3])
            {
                byte2 = 3;
            }
            if((Block.opaqueCubeLookup[j1] || Block.opaqueCubeLookup[j3]) && !Block.opaqueCubeLookup[i1] && !Block.opaqueCubeLookup[l2])
            {
                byte2 = 2;
            }
            return (l != byte2 ? blockIndexInTexture + 32 : blockIndexInTexture + 16) + j2;
        }
        byte byte0 = 3;
        if(Block.opaqueCubeLookup[i1] && !Block.opaqueCubeLookup[j1])
        {
            byte0 = 3;
        }
        if(Block.opaqueCubeLookup[j1] && !Block.opaqueCubeLookup[i1])
        {
            byte0 = 2;
        }
        if(Block.opaqueCubeLookup[k1] && !Block.opaqueCubeLookup[l1])
        {
            byte0 = 5;
        }
        if(Block.opaqueCubeLookup[l1] && !Block.opaqueCubeLookup[k1])
        {
            byte0 = 4;
        }
        return l != byte0 ? blockIndexInTexture : blockIndexInTexture + 1;
    }

    public int getBlockTextureFromSide(int i)
    {
        if(i == 1)
        {
            return blockIndexInTexture - 1;
        }
        if(i == 0)
        {
            return blockIndexInTexture - 1;
        }
        if(i == 3)
        {
            return blockIndexInTexture + 1;
        } else
        {
            return blockIndexInTexture;
        }
    }

    public boolean canPlaceBlockAt(World world, int i, int j, int k)
    {
        int l = 0;
        if(world.getBlockId(i - 1, j, k) == blockID)
        {
            l++;
        }
        if(world.getBlockId(i + 1, j, k) == blockID)
        {
            l++;
        }
        if(world.getBlockId(i, j, k - 1) == blockID)
        {
            l++;
        }
        if(world.getBlockId(i, j, k + 1) == blockID)
        {
            l++;
        }
        if(l > 1)
        {
            return false;
        }
        if(isThereANeighborChest(world, i - 1, j, k))
        {
            return false;
        }
        if(isThereANeighborChest(world, i + 1, j, k))
        {
            return false;
        }
        if(isThereANeighborChest(world, i, j, k - 1))
        {
            return false;
        }
        return !isThereANeighborChest(world, i, j, k + 1);
    }

    private boolean isThereANeighborChest(World world, int i, int j, int k)
    {
        if(world.getBlockId(i, j, k) != blockID)
        {
            return false;
        }
        if(world.getBlockId(i - 1, j, k) == blockID)
        {
            return true;
        }
        if(world.getBlockId(i + 1, j, k) == blockID)
        {
            return true;
        }
        if(world.getBlockId(i, j, k - 1) == blockID)
        {
            return true;
        }
        return world.getBlockId(i, j, k + 1) == blockID;
    }

    public void onBlockRemoval(World world, int i, int j, int k)
    {
        TileEntityChest tileentitychest = (TileEntityChest)world.getBlockTileEntity(i, j, k);
        //Fixes the bug that crashes MC when the world generator overwrites an empty chest
        if( tileentitychest != null )
        {
label0:
	        for(int l = 0; l < tileentitychest.getSizeInventory(); l++)
	        {
	            ItemStack itemstack = tileentitychest.getStackInSlot(l);
	            if(itemstack == null)
	            {
	                continue;
	            }
	            float f = random.nextFloat() * 0.8F + 0.1F;
	            float f1 = random.nextFloat() * 0.8F + 0.1F;
	            float f2 = random.nextFloat() * 0.8F + 0.1F;
	            do
	            {
	                if(itemstack.stackSize <= 0)
	                {
	                    continue label0;
	                }
	                int i1 = random.nextInt(21) + 10;
	                if(i1 > itemstack.stackSize)
	                {
	                    i1 = itemstack.stackSize;
	                }
	                itemstack.stackSize -= i1;
	                EntityItem entityitem = new EntityItem(world, (float)i + f, (float)j + f1, (float)k + f2, new ItemStack(itemstack.itemID, i1, itemstack.getItemDamage()));
	                float f3 = 0.05F;
	                entityitem.motionX = (float)random.nextGaussian() * f3;
	                entityitem.motionY = (float)random.nextGaussian() * f3 + 0.2F;
	                entityitem.motionZ = (float)random.nextGaussian() * f3;
	                world.entityJoinedWorld(entityitem);
	            } while(true);
	        }
        }

        super.onBlockRemoval(world, i, j, k);
    }

    public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer)
    {
        Object obj = (TileEntityChest)world.getBlockTileEntity(i, j, k);
        if(world.func_28100_h(i, j + 1, k))
        {
            return true;
        }
        if(world.getBlockId(i - 1, j, k) == blockID && world.func_28100_h(i - 1, j + 1, k))
        {
            return true;
        }
        if(world.getBlockId(i + 1, j, k) == blockID && world.func_28100_h(i + 1, j + 1, k))
        {
            return true;
        }
        if(world.getBlockId(i, j, k - 1) == blockID && world.func_28100_h(i, j + 1, k - 1))
        {
            return true;
        }
        if(world.getBlockId(i, j, k + 1) == blockID && world.func_28100_h(i, j + 1, k + 1))
        {
            return true;
        }
        if(world.getBlockId(i - 1, j, k) == blockID)
        {
            obj = new InventoryLargeChest("Large chest", (TileEntityChest)world.getBlockTileEntity(i - 1, j, k), ((IInventory) (obj)));
        }
        if(world.getBlockId(i + 1, j, k) == blockID)
        {
            obj = new InventoryLargeChest("Large chest", ((IInventory) (obj)), (TileEntityChest)world.getBlockTileEntity(i + 1, j, k));
        }
        if(world.getBlockId(i, j, k - 1) == blockID)
        {
            obj = new InventoryLargeChest("Large chest", (TileEntityChest)world.getBlockTileEntity(i, j, k - 1), ((IInventory) (obj)));
        }
        if(world.getBlockId(i, j, k + 1) == blockID)
        {
            obj = new InventoryLargeChest("Large chest", ((IInventory) (obj)), (TileEntityChest)world.getBlockTileEntity(i, j, k + 1));
        }
        if(world.multiplayerWorld)
        {
            return true;
        } else
        {
            entityplayer.displayGUIChest(((IInventory) (obj)));
            return true;
        }
    }

    public static IInventory buildEntity(World world, int i, int j, int k, int size)
    {
        TileEntity te = new TileEntityChest();
        world.setBlockTileEntity(i, j, k, te);
        
        IInventory obj = (IInventory) te;
        if(size <= 27)
        	return obj;
        
        te = new TileEntityChest();
        
        if(world.getBlockId(i - 1, j, k) == 54)
        {
        	world.setBlockTileEntity(i - 1, j, k, te);
            obj = new InventoryLargeChest("Large chest", (IInventory) te, obj);
        }
        if(world.getBlockId(i + 1, j, k) == 54)
        {
        	world.setBlockTileEntity(i + 1, j, k, te);
            obj = new InventoryLargeChest("Large chest", obj, (IInventory) te);
        }
        if(world.getBlockId(i, j, k - 1) == 54)
        {
        	world.setBlockTileEntity(i, j, k - 1, te);
            obj = new InventoryLargeChest("Large chest", (IInventory) te, obj);
        }
        if(world.getBlockId(i, j, k + 1) == 54)
        {
        	world.setBlockTileEntity(i, j, k + 1, te);
            obj = new InventoryLargeChest("Large chest", obj, (IInventory) te);
        }
        return (IInventory) obj;
    }
    
    protected TileEntity getBlockEntity()
    {
        return new TileEntityChest();
    }

    private Random random;
}
