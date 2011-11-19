// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

package net.minecraft.src;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

// Referenced classes of package net.minecraft.src:
//            GuiContainer, ContainerChest, IInventory, FontRenderer, 
//            RenderEngine

public class GuiChest extends GuiContainer
{

    public GuiChest(IInventory iinventory, IInventory iinventory1)
    {
        super(new ContainerChest(iinventory, iinventory1));
        inventoryRows = 0;
        upperChestInventory = iinventory;
        lowerChestInventory = iinventory1;
        allowUserInput = false;
        char c = '\336';
        int i = c - 108;
        inventoryRows = iinventory1.getSizeInventory() / 9;
        ySize = i + inventoryRows * 18;
    }

    protected void drawGuiContainerForegroundLayer()
    {
        fontRenderer.drawString(lowerChestInventory.getInvName(), 8, 6, 0x404040);
        fontRenderer.drawString(upperChestInventory.getInvName(), 8, (ySize - 96) + 2, 0x404040);
    }

    protected void drawGuiContainerBackgroundLayer(float f)
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
    
    private IInventory upperChestInventory;
    private IInventory lowerChestInventory;
    private int inventoryRows;
}
