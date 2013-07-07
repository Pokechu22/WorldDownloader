package net.minecraft.src;

import java.util.List;

public class GuiWDLBackup extends GuiScreen
{
	private String title = "";
	
	private GuiScreen parent;
	
	private GuiTextField commandField;
	private GuiButton backupBtn;
	
	boolean cmdBox = false;

	public GuiWDLBackup( GuiScreen parent )
    {
		this.parent = parent;
    }

    public void initGui()
    {
    	this.buttonList.clear();
        
        title = "Backup Options for " + WDL.baseFolderName.replace('@', ':');
        
        int w = width / 2;
        int h = height / 4;
        
        backupBtn = new GuiButton( 10, w-100, h+105, "Backup: ERROR" );
        this.buttonList.add( backupBtn );
        updateBackup(false);
        
        commandField = new GuiTextField( fontRenderer, w-98, h+126, 196, 17);
        
        this.buttonList.add( new GuiButton( 100, w-100, h+150, "Done" ) );
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
        drawCenteredString(fontRenderer, title, width / 2, height / 4 - 40, 0xffffff);
        
        drawString(fontRenderer, "Name:", width / 2 - 99, 50, 0xffffff);
        if( cmdBox == true )
        	commandField.drawTextBox();
        super.drawScreen(i, j, f);
    }
    
	public void updateBackup( boolean btnClicked )
	{
		cmdBox = false;
		
		String backup = WDL.baseProps.getProperty("Backup");
		if( backup == "off" )
		{
			if( btnClicked )
			{
				WDL.baseProps.setProperty("Backup", "folder");
				updateBackup(false);
			}
			else
				backupBtn.displayString = "Backup: Disabled";
		}
		else if( backup == "folder" )
		{
			if( btnClicked )
			{
				WDL.baseProps.setProperty("Backup", "zip");
				updateBackup(false);
			}
			else
				backupBtn.displayString = "Backup: Copy World Folder";
		}
		else if( backup == "zip")
		{
			if( btnClicked )
			{
				WDL.baseProps.setProperty("Backup", "command");
				updateBackup(false);
			}
			else
	    		backupBtn.displayString = "Backup: Zip World Folder";
		}
		else if( backup == "command")
		{
			if( btnClicked )
			{
				WDL.baseProps.setProperty("Backup", "off");
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
			WDL.baseProps.setProperty("Backup", "off");
			updateBackup(false);
		}
	}
}
