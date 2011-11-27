// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst 

package net.minecraft.src;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

// Referenced classes of package net.minecraft.src:
//            GuiContainer, ContainerDispenser, FontRenderer, RenderEngine, 
//            InventoryPlayer, TileEntityDispenser

public class GuiDispenser extends GuiContainer
{
	/* World Downloader >>> */
    private TileEntityDispenser ted;
    /* <<< World Downloader */
    
    public GuiDispenser(InventoryPlayer inventoryplayer, TileEntityDispenser tileentitydispenser)
    {
        super(new ContainerDispenser(inventoryplayer, tileentitydispenser));
        /* WORLD DOWNLOADER ---> */
        ted = tileentitydispenser;
        /* <--- WORLD DOWNLOADER */
    }

    protected void drawGuiContainerForegroundLayer()
    {
        fontRenderer.drawString("Dispenser", 60, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
    }

    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        int k = mc.renderEngine.getTexture("/gui/trap.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(k);
        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;
        drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
    }
    
    /* WORLD DOWNLOADER ---> */
    public void onGuiClosed() {
    	if( WorldDL.downloading )
    	{
    		// This adds the TileEntityDispenser to the myChunkTileEntityMap in the appropriate chunk.
    		WorldDL.wc.setMyBlockTileEntity(WorldDL.lastClickedX, WorldDL.lastClickedY, WorldDL.lastClickedZ, ted);
    	}
    	super.onGuiClosed();
    }
    /* <--- WORLD DOWNLOADER */

}
