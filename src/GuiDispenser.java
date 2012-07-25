package net.minecraft.src;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class GuiDispenser extends GuiContainer
{
    /* World Downloader >>> */
    private TileEntityDispenser ted;
    /* <<< World Downloader */

    public GuiDispenser(InventoryPlayer par1InventoryPlayer, TileEntityDispenser par2TileEntityDispenser)
    {
        super(new ContainerDispenser(par1InventoryPlayer, par2TileEntityDispenser));
        
        /* WORLD DOWNLOADER ---> */
        ted = par2TileEntityDispenser;
        /* <--- WORLD DOWNLOADER */

    }

    /**
     * Draw the foreground layer for the GuiContainer (everythin in front of the items)
     */
    protected void drawGuiContainerForegroundLayer()
    {
        fontRenderer.drawString(StatCollector.translateToLocal("container.dispenser"), 60, 6, 0x404040);
        fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        int i = mc.renderEngine.getTexture("/gui/trap.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(i);
        int j = (width - xSize) / 2;
        int k = (height - ySize) / 2;
        drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
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
