package net.minecraft.src;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class GuiBrewingStand extends GuiContainer
{
    private TileEntityBrewingStand field_40217_h;

    public GuiBrewingStand(InventoryPlayer inventoryplayer, TileEntityBrewingStand tileentitybrewingstand)
    {
        super(new ContainerBrewingStand(inventoryplayer, tileentitybrewingstand));
        field_40217_h = tileentitybrewingstand;
    }

    protected void drawGuiContainerForegroundLayer()
    {
        fontRenderer.drawString("Brewing Stand", 56, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
    }

    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        int k = mc.renderEngine.getTexture("/gui/alchemy.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(k);
        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;
        drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
        int j1 = field_40217_h.getBrewTime();
        if (j1 > 0)
        {
            int k1 = (int)(28F * (1.0F - (float)j1 / 400F));
            if (k1 > 0)
            {
                drawTexturedModalRect(l + 97, i1 + 16, 176, 0, 9, k1);
            }
            int l1 = (j1 / 2) % 7;
            switch (l1)
            {
                case 6:
                    k1 = 0;
                    break;

                case 5:
                    k1 = 6;
                    break;

                case 4:
                    k1 = 11;
                    break;

                case 3:
                    k1 = 16;
                    break;

                case 2:
                    k1 = 20;
                    break;

                case 1:
                    k1 = 24;
                    break;

                case 0:
                    k1 = 29;
                    break;
            }
            if (k1 > 0)
            {
                drawTexturedModalRect(l + 65, (i1 + 14 + 29) - k1, 185, 29 - k1, 12, k1);
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
