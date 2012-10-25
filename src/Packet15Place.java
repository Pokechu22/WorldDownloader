package net.minecraft.src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet15Place extends Packet
{
    private int xPosition;
    private int yPosition;
    private int zPosition;

    /** The offset to use for block/item placement. */
    private int direction;
    private ItemStack itemStack;

    /** The offset from xPosition where the actual click took place */
    private float xOffset;

    /** The offset from yPosition where the actual click took place */
    private float yOffset;

    /** The offset from zPosition where the actual click took place */
    private float zOffset;

    public Packet15Place() {}

    public Packet15Place(int par1, int par2, int par3, int par4, ItemStack par5ItemStack, float par6, float par7, float par8)
    {
        this.xPosition = par1;
        this.yPosition = par2;
        this.zPosition = par3;
        this.direction = par4;
        this.itemStack = par5ItemStack;
        this.xOffset = par6;
        this.yOffset = par7;
        this.zOffset = par8;
        
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
        this.xPosition = par1DataInputStream.readInt();
        this.yPosition = par1DataInputStream.read();
        this.zPosition = par1DataInputStream.readInt();
        this.direction = par1DataInputStream.read();
        this.itemStack = readItemStack(par1DataInputStream);
        this.xOffset = (float)par1DataInputStream.read() / 16.0F;
        this.yOffset = (float)par1DataInputStream.read() / 16.0F;
        this.zOffset = (float)par1DataInputStream.read() / 16.0F;
    }

    /**
     * Abstract. Writes the raw packet data to the data stream.
     */
    public void writePacketData(DataOutputStream par1DataOutputStream) throws IOException
    {
        par1DataOutputStream.writeInt(this.xPosition);
        par1DataOutputStream.write(this.yPosition);
        par1DataOutputStream.writeInt(this.zPosition);
        par1DataOutputStream.write(this.direction);
        writeItemStack(this.itemStack, par1DataOutputStream);
        par1DataOutputStream.write((int)(this.xOffset * 16.0F));
        par1DataOutputStream.write((int)(this.yOffset * 16.0F));
        par1DataOutputStream.write((int)(this.zOffset * 16.0F));
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
        return 19;
    }

    public int getXPosition()
    {
        return this.xPosition;
    }

    public int getYPosition()
    {
        return this.yPosition;
    }

    public int getZPosition()
    {
        return this.zPosition;
    }

    public int getDirection()
    {
        return this.direction;
    }

    public ItemStack getItemStack()
    {
        return this.itemStack;
    }

    /**
     * Returns the offset from xPosition where the actual click took place
     */
    public float getXOffset()
    {
        return this.xOffset;
    }

    /**
     * Returns the offset from yPosition where the actual click took place
     */
    public float getYOffset()
    {
        return this.yOffset;
    }

    /**
     * Returns the offset from zPosition where the actual click took place
     */
    public float getZOffset()
    {
        return this.zOffset;
    }
}
