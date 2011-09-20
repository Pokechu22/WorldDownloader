// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

package net.minecraft.src;

import java.io.*;

// Referenced classes of package net.minecraft.src:
//            Packet, ItemStack, NetHandler

public class Packet15Place extends Packet
{

    public Packet15Place()
    {
    }

    public Packet15Place(int i, int j, int k, int l, ItemStack itemstack)
    {
        xPosition = i;
        yPosition = j;
        zPosition = k;
        direction = l;
        itemStack = itemstack;
        /* WORLD DOWNLOADER ---> */
        if( WorldDL.downloading == true )
        {
        	WorldDL.lastClickedX = xPosition;
        	WorldDL.lastClickedY = yPosition;
        	WorldDL.lastClickedZ = zPosition;
        }
        /* <--- WORLD DOWNLOADER */
    }

    public void readPacketData(DataInputStream datainputstream)
        throws IOException
    {
        xPosition = datainputstream.readInt();
        yPosition = datainputstream.read();
        zPosition = datainputstream.readInt();
        direction = datainputstream.read();
        short word0 = datainputstream.readShort();
        if(word0 >= 0)
        {
            byte byte0 = datainputstream.readByte();
            short word1 = datainputstream.readShort();
            itemStack = new ItemStack(word0, byte0, word1);
        } else
        {
            itemStack = null;
        }
    }

    public void writePacketData(DataOutputStream dataoutputstream)
        throws IOException
    {
        dataoutputstream.writeInt(xPosition);
        dataoutputstream.write(yPosition);
        dataoutputstream.writeInt(zPosition);
        dataoutputstream.write(direction);
        if(itemStack == null)
        {
            dataoutputstream.writeShort(-1);
        } else
        {
            dataoutputstream.writeShort(itemStack.itemID);
            dataoutputstream.writeByte(itemStack.stackSize);
            dataoutputstream.writeShort(itemStack.getItemDamage());
        }
    }

    public void processPacket(NetHandler nethandler)
    {
        nethandler.handlePlace(this);
    }

    public int getPacketSize()
    {
        return 15;
    }

    public int xPosition;
    public int yPosition;
    public int zPosition;
    public int direction;
    public ItemStack itemStack;
}
