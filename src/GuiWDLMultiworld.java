package net.minecraft.src;

import java.util.List;
import java.util.Properties;

import net.minecraft.client.Minecraft;

public class GuiWDLMultiworld extends GuiScreen
{
	private GuiScreen parent;
	
	private GuiButton multiworldEnabledBtn;
	boolean newMultiworldState = false;
	
	public GuiWDLMultiworld( GuiScreen parent )
    {
		this.parent = parent;
    }
	
    public void initGui()
    {
    	this.buttonList.clear();
        
        int w = width / 2;
        int h = height / 4;
        
        int hi = h+115;
        
        multiworldEnabledBtn = new GuiButton( 1, w-100, hi, "Multiworld support: ERROR" );
        this.buttonList.add( multiworldEnabledBtn );
        updateMultiworldEnabled(false);
        
        this.buttonList.add( new GuiButton( 100, w-100, h+150, "OK" ) );
    }

    protected void actionPerformed(GuiButton guibutton)
    {
    	if( !guibutton.enabled )
    		return;
    	if( guibutton.id == 1 )
    	{
    		updateMultiworldEnabled( true );
    	}
    	else if( guibutton.id == 100 ) //Done
    	{
    		if( newMultiworldState == true )
    			mc.displayGuiScreen( new GuiWDLMultiworldSelect( parent ) );
    		else
    		{
    			WDL.baseProps.setProperty("LinkedWorlds", "");
    			WDL.saveProps();
    			WDL.propsFound = true;
    			
        		if( parent != null )
    				mc.displayGuiScreen( new GuiWDL( parent ) );
        	    else
        	    {
        	    	WDL.start();
        	    	mc.displayGuiScreen(null);
        	    	mc.setIngameFocus();
        	    }
    		}
    	}
    }

    protected void mouseClicked(int i, int j, int k) {
    	super.mouseClicked(i, j, k);
    }
    
    protected void keyTyped(char c, int i) {
    	super.keyTyped(c, i);
    }
    
    public void updateScreen()
    {
        super.updateScreen();
    }

    public void drawScreen(int i, int j, float f)
    {
        drawDefaultBackground();
        drawRect(width/2-160, height/4-60, width/2+160, height/4+180, 0xb0000000);
        drawCenteredString( fontRenderer, "Multiworld Support", width / 2, height / 4 - 40, 0xff0000 );
        
        drawString( fontRenderer, "Multiworld support is required if at least one of the", width / 2 - 150, height / 4 - 15, 0xffffff );
        drawString( fontRenderer, " following conditions is met:", width / 2 - 150, height / 4 - 5, 0xffffff );
        
        drawString( fontRenderer, "- \"Multiworld\" is mentioned on the server's website", width / 2 - 150, height / 4 + 15, 0xffffff );
        
        drawString( fontRenderer, "- The server has more than 3 dimensions (or worlds)", width / 2 - 150, height / 4 + 35, 0xffffff );
        
        drawString( fontRenderer, "- The server has other dimensions than the official ones", width / 2 - 150, height / 4 + 55, 0xffffff );
        drawString( fontRenderer, "   (Earth, Nether, The End)", width / 2 - 150, height / 4 + 65, 0xffffff );
        
        //drawString( fontRenderer, "- The seeds of the 3 standard dimensions are differing", width / 2 - 150, height / 4 + 85, 0xffffff );
        
        drawRect(width/2-102, height/4+113, width/2+102, height/4+137, 0xffff0000);
        super.drawScreen(i, j, f);
    }
    
    private void updateMultiworldEnabled( boolean btnClicked )
    {
		if( newMultiworldState == false )
		{
			if( btnClicked )
			{
				newMultiworldState = true;
				updateMultiworldEnabled( false );
			}
			else
				multiworldEnabledBtn.displayString = "Multiworld support: Disabled";
		}
		else
		{
			if( btnClicked )
			{
				newMultiworldState = false;
				updateMultiworldEnabled( false );
			}
			else
				multiworldEnabledBtn.displayString = "Multiworld support: Enabled";
		}
    }
}
