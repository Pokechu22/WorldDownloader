package net.minecraft.src;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class GuiBrewingStand extends GuiContainer
{
    private TileEntityBrewingStand field_40217_h;

    public GuiBrewingStand(InventoryPlayer par1InventoryPlayer, TileEntityBrewingStand par2TileEntityBrewingStand)
    {
        super(new ContainerBrewingStand(par1InventoryPlayer, par2TileEntityBrewingStand));
        field_40217_h = par2TileEntityBrewingStand;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everythin in front of the items)
     */
    protected void drawGuiContainerForegroundLayer()
    {
        fontRenderer.drawString(StatCollector.translateToLocal("container.brewing"), 56, 6, 0x404040);
        fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        int i = mc.renderEngine.getTexture("/gui/alchemy.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(i);
        int j = (width - xSize) / 2;
        int k = (height - ySize) / 2;
        drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
        int l = field_40217_h.getBrewTime();

        if (l > 0)
        {
            int i1 = (int)(28F * (1.0F - (float)l / 400F));

            if (i1 > 0)
            {
                drawTexturedModalRect(j + 97, k + 16, 176, 0, 9, i1);
            }

            int j1 = (l / 2) % 7;

            switch (j1)
            {
                case 6:
                    i1 = 0;
                    break;

                case 5:
                    i1 = 6;
                    break;

                case 4:
                    i1 = 11;
                    break;

                case 3:
                    i1 = 16;
                    break;

                case 2:
                    i1 = 20;
                    break;

                case 1:
                    i1 = 24;
                    break;

                case 0:
                    i1 = 29;
                    break;
            }

            if (i1 > 0)
            {
                drawTexturedModalRect(j + 65, (k + 14 + 29) - i1, 185, 29 - i1, 12, i1);
            }
        }
    }
    
    /* WORLD DOWNLOADER ---> */
    public void onGuiClosed() {
    	if( WorldDL.downloading )
    	{
    		// This adds the TileEntityDispenser to the myChunkTileEntityMap in the appropriate chunk.
    		WorldDL.wc.setMyBlockTileEntity(WorldDL.lastClickedX, WorldDL.lastClickedY, WorldDL.lastClickedZ, field_40217_h);
    	}
    	super.onGuiClosed();
    }
    /* <--- WORLD DOWNLOADER */
}
