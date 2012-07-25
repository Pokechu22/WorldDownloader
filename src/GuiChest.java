package net.minecraft.src;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class GuiChest extends GuiContainer
{
    private IInventory upperChestInventory;
    private IInventory lowerChestInventory;

    /**
     * window height is calculated with this values, the more rows, the heigher
     */
    private int inventoryRows;

    public GuiChest(IInventory par1IInventory, IInventory par2IInventory)
    {
        super(new ContainerChest(par1IInventory, par2IInventory));
        inventoryRows = 0;
        upperChestInventory = par1IInventory;
        lowerChestInventory = par2IInventory;
        allowUserInput = false;
        char c = '\336';
        int i = c - 108;
        inventoryRows = par2IInventory.getSizeInventory() / 9;
        ySize = i + inventoryRows * 18;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everythin in front of the items)
     */
    protected void drawGuiContainerForegroundLayer()
    {
        fontRenderer.drawString(StatCollector.translateToLocal(lowerChestInventory.getInvName()), 8, 6, 0x404040);
        fontRenderer.drawString(StatCollector.translateToLocal(upperChestInventory.getInvName()), 8, (ySize - 96) + 2, 0x404040);
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        int i = mc.renderEngine.getTexture("/gui/container.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(i);
        int j = (width - xSize) / 2;
        int k = (height - ySize) / 2;
        drawTexturedModalRect(j, k, 0, 0, xSize, inventoryRows * 18 + 17);
        drawTexturedModalRect(j, k + inventoryRows * 18 + 17, 0, 126, xSize, 96);
    }
    
    /* WORLD DOWNLOADER ---> */
    public void onGuiClosed() {
    	if( WorldDL.downloading )
    	{
    		// This creates new TileEntityChest(s) for the received chest content
    		//  and adds them to the myChunkTileEntityMap in the appropriate chunk.
    		WorldDL.setChestTileEntitiy( lowerChestInventory );
    	}
    	super.onGuiClosed();
    }
    /* <--- WORLD DOWNLOADER */
}
