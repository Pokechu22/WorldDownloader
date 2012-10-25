package net.minecraft.src;

import org.lwjgl.opengl.GL11;

public class GuiDispenser extends GuiContainer
{
	/* WORLD DOWNLOADER >>> */
	private TileEntityDispenser ted;
	/* <<< WORLD DOWNLOADER */
	
    public GuiDispenser(InventoryPlayer par1InventoryPlayer, TileEntityDispenser par2TileEntityDispenser)
    {
        super(new ContainerDispenser(par1InventoryPlayer, par2TileEntityDispenser));
        /* WORLD DOWNLOADER ---> */
        ted = par2TileEntityDispenser;
        /* <--- WORLD DOWNLOADER */
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        this.fontRenderer.drawString(StatCollector.translateToLocal("container.dispenser"), 60, 6, 4210752);
        this.fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        int var4 = this.mc.renderEngine.getTexture("/gui/trap.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(var4);
        int var5 = (this.width - this.xSize) / 2;
        int var6 = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(var5, var6, 0, 0, this.xSize, this.ySize);
    }
    
    /* WORLD DOWNLOADER ---> */
    public void onGuiClosed() 
    {
	    if( WorldDL.downloading )
	    {
	            // This adds the TileEntityDispenser to the myChunkTileEntityMap in the appropriate chunk.
	            WorldDL.wc.setMyBlockTileEntity(WorldDL.lastClickedX, WorldDL.lastClickedY, WorldDL.lastClickedZ, ted);
	    }
	    super.onGuiClosed();
    }
    /* <--- WORLD DOWNLOADER */
}
