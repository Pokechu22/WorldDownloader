package net.minecraft.src;

import java.util.List;
import net.minecraft.client.Minecraft;

public class GuiWorldDLBackup extends GuiScreen
{
	
	private GuiScreen parent;
	
	private GuiTextField commandField;
	private GuiButton backupBtn;
	
	boolean cmdBox = false;

	public GuiWorldDLBackup( GuiScreen parent )
    {
		this.parent = parent;
    }

    public void initGui()
    {
        controlList.clear();
        
        int w = width / 2;
        int h = height / 4;
        
        backupBtn = new GuiButton( 10, w-100, h+105, "Backup: ERROR" );
        controlList.add( backupBtn );
        updateBackup(false);
        
        commandField = new GuiTextField( this, fontRenderer, w-98, h+126, 196, 17, "");
        
        controlList.add( new GuiButton( 100, w-100, h+150, "Done" ) );
    }

    protected void actionPerformed(GuiButton guibutton)
    {
    	if( !guibutton.enabled )
    		return;
    	
    	if( guibutton.id == 10 ) //Backup
    	{
    		updateBackup( true );
    	}
    	else if( guibutton.id == 100 ) //Done
    	{
    		mc.displayGuiScreen( parent );
    	}
    }

    protected void mouseClicked(int i, int j, int k) {
    	super.mouseClicked(i, j, k);
    	if( cmdBox == true )
    		commandField.mouseClicked(i, j, k);
    }
    
    protected void keyTyped(char c, int i) {
    	super.keyTyped(c, i);
    	if( commandField.isFocused )
    		commandField.textboxKeyTyped(c, i);
    }
    
    public void updateScreen()
    {
    	commandField.updateCursorCounter();
        super.updateScreen();
    }

    public void drawScreen(int i, int j, float f)
    {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, "Options for [HostString][:Port]", width / 2, height / 4 - 40, 0xffffff);
        
        drawString(fontRenderer, "Name:", width / 2 - 99, 50, 0xffffff);
        if( cmdBox == true )
        	commandField.drawTextBox();
        super.drawScreen(i, j, f);
    }
    
	public void updateBackup( boolean btnClicked )
	{
		cmdBox = false;
		
		String backup = WorldDL.baseProps.getProperty("Backup");
		if( backup == "off" )
		{
			if( btnClicked )
			{
				WorldDL.baseProps.setProperty("Backup", "folder");
				updateBackup(false);
			}
			else
				backupBtn.displayString = "Backup: Disabled";
		}
		else if( backup == "folder" )
		{
			if( btnClicked )
			{
				WorldDL.baseProps.setProperty("Backup", "zip");
				updateBackup(false);
			}
			else
				backupBtn.displayString = "Backup: Copy World Folder";
		}
		else if( backup == "zip")
		{
			if( btnClicked )
			{
				WorldDL.baseProps.setProperty("Backup", "command");
				updateBackup(false);
			}
			else
	    		backupBtn.displayString = "Backup: Zip World Folder";
		}
		else if( backup == "command")
		{
			if( btnClicked )
			{
				WorldDL.baseProps.setProperty("Backup", "off");
				updateBackup(false);
			}
			else
			{
	    		backupBtn.displayString = "Backup: Run the following command";
	    		cmdBox = true;
			}
		}
		else
		{
			WorldDL.baseProps.setProperty("Backup", "off");
			updateBackup(false);
		}
	}
}
