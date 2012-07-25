package net.minecraft.src;

import java.io.*;

public class Packet15Place extends Packet
{
    public int xPosition;
    public int yPosition;
    public int zPosition;

    /** The offset to use for block/item placement. */
    public int direction;
    public ItemStack itemStack;

    public Packet15Place()
    {
    }

    public Packet15Place(int par1, int par2, int par3, int par4, ItemStack par5ItemStack)
    {
        xPosition = par1;
        yPosition = par2;
        zPosition = par3;
        direction = par4;
        itemStack = par5ItemStack;
        
        /* WORLD DOWNLOADER ---> */
        if( WorldDL.downloading == true )
        {
        	WorldDL.lastClickedX = xPosition;
        	WorldDL.lastClickedY = yPosition;
        	WorldDL.lastClickedZ = zPosition;
        }
        /* <--- WORLD DOWNLOADER */

    }

    /**
     * Abstract. Reads the raw packet data from the data stream.
     */
    public void readPacketData(DataInputStream par1DataInputStream) throws IOException
    {
        xPosition = par1DataInputStream.readInt();
        yPosition = par1DataInputStream.read();
        zPosition = par1DataInputStream.readInt();
        direction = par1DataInputStream.read();
        itemStack = readItemStack(par1DataInputStream);
    }

    /**
     * Abstract. Writes the raw packet data to the data stream.
     */
    public void writePacketData(DataOutputStream par1DataOutputStream) throws IOException
    {
        par1DataOutputStream.writeInt(xPosition);
        par1DataOutputStream.write(yPosition);
        par1DataOutputStream.writeInt(zPosition);
        par1DataOutputStream.write(direction);
        writeItemStack(itemStack, par1DataOutputStream);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(NetHandler par1NetHandler)
    {
        par1NetHandler.handlePlace(this);
    }

    /**
     * Abstract. Return the size of the packet (not counting the header).
     */
    public int getPacketSize()
    {
        return 15;
    }
}
