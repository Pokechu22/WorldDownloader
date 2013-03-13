package net.minecraft.src;

import java.util.List;
import net.minecraft.client.Minecraft;

public class GuiWDL extends GuiScreen
{
	private String title = "";
	
	private GuiScreen parent;
	
	private GuiTextField worldName;
	private GuiButton autoStartBtn;
	private GuiButton backupBtn;
//	private GuiButton multiWorldBtn;
	private GuiButton worldOverrides;
	private GuiButton generatorOverrides;
	private GuiButton playerOverrides;

	public GuiWDL( GuiScreen parent )
    {
		this.parent = parent;
    }

    public void initGui()
    {
		if( WDL.isMultiworld && WDL.worldName.isEmpty() )
		{
			mc.displayGuiScreen( new GuiWDLMultiworldSelect( parent ) );
		}
		
    	if( WDL.propsFound == false )
    	{
    		mc.displayGuiScreen( new GuiWDLMultiworld( parent ) );
    		return;
    	}
		
    	this.buttonList.clear();
        
        title = "Options for " + WDL.baseFolderName.replace('@', ':');
        
        int w = width / 2;
        int h = height / 4;
        
        int hi = h-15;
        
		if( WDL.baseProps.getProperty("ServerName").isEmpty() )
		{
			WDL.baseProps.setProperty( "ServerName", WDL.getServerName() );
		}
        
        worldName = new GuiTextField( fontRenderer, width / 2 - 70, hi, 168, 18 );
        updateServerName(false);
        
        hi += 22;
        autoStartBtn = new GuiButton( 1, w-100, hi, "Start Download: ERROR" );
        this.buttonList.add( autoStartBtn );
        updateAutoStart(false);
        
        hi += 22;
        backupBtn = new GuiButton( 2, w-100, hi, "Backup Options..." );
        backupBtn.enabled = false;
        this.buttonList.add( backupBtn );
        
//        hi += 22;
//        multiWorldBtn = new GuiButton(3, w-100, hi, "Multiworld (DELETE THIS)");
//        controlList.add( multiWorldBtn );
        
        hi += 28;
        worldOverrides = new GuiButton(4, w-100, hi, "World Overrides...");
        this.buttonList.add( worldOverrides );
        
        hi += 22;
        generatorOverrides = new GuiButton(5, w-100, hi, "World Generator Overrides...");
        this.buttonList.add( generatorOverrides );
        
        hi += 22;
        playerOverrides = new GuiButton(6, w-100, hi, "Player Overrides...");
        this.buttonList.add( playerOverrides );
        
        hi += 28;
        this.buttonList.add( new GuiButton( 100, w-100, h+150, "Done" ) );
    }

    protected void actionPerformed(GuiButton guibutton)
    {
    	if( !guibutton.enabled )
    		return;
    	
    	updateServerName(true);
    	
    	if( guibutton.id == 1 ) //Auto start
    		updateAutoStart( true );
    	else if( guibutton.id == 2 ) //Backup
    		mc.displayGuiScreen( new GuiWDLBackup(this) );
//    	else if( guibutton.id == 3 ) //Multiworld
//    		mc.displayGuiScreen( new GuiWDLMultiworld(this) );
    	else if( guibutton.id == 4 ) //World Overrides
    		mc.displayGuiScreen( new GuiWDLWorld(this) );
    	else if( guibutton.id == 5 ) //Generator Overrides
    		mc.displayGuiScreen( new GuiWDLGenerator(this) );
    	else if( guibutton.id == 6 ) //Player Overrides
    		mc.displayGuiScreen( new GuiWDLPlayer(this) );
    	else if( guibutton.id == 100 ) //Done
    	{
    		WDL.saveProps();
    		mc.displayGuiScreen( parent );
    	}
    }

    protected void mouseClicked(int i, int j, int k) {
    	super.mouseClicked(i, j, k);
    	worldName.mouseClicked(i, j, k);
    }
    
    protected void keyTyped(char c, int i) {
    	super.keyTyped(c, i);
    	worldName.textboxKeyTyped(c, i);
    }
    
    public void updateScreen()
    {
    	worldName.updateCursorCounter();
        super.updateScreen();
    }

    public void drawScreen(int i, int j, float f)
    {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, title , width / 2, height / 4 - 40, 0xffffff);
        
        drawString(fontRenderer, "Name:", width / 2 - 99, height / 4 - 10, 0xffffff);
        worldName.drawTextBox();
        super.drawScreen(i, j, f);
    }
    
    public void updateAutoStart( boolean btnClicked )
    {
		String autoStart = WDL.baseProps.getProperty("AutoStart");
		if( autoStart.equals("true") )
		{
			if( btnClicked )
			{
				WDL.baseProps.setProperty("AutoStart", "false");
				updateAutoStart(false);
			}
			else
				autoStartBtn.displayString = "Start Download: Automatically";
		}
		else
		{
			if( btnClicked )
			{
				WDL.baseProps.setProperty("AutoStart", "true");
				updateAutoStart(false);
			}
			else
				autoStartBtn.displayString = "Start Download: Only in menu";
			
		}
    }
    
    private void updateServerName( boolean write )
    {
    	if( write )
    	{
    		WDL.baseProps.setProperty("ServerName", worldName.getText() );
    	}
    	else
    		worldName.setText( WDL.baseProps.getProperty("ServerName") );
    }
}
