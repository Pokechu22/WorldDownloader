package net.minecraft.src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class GuiWDLMultiworldSelect extends GuiScreen
{

	private GuiButton cancelBtn;
	private GuiTextField newNameField;
	private boolean newWorld = false;
	private int positionID;
	private float yaw;
	private int thirdPersonViewSave;
	private GuiButton[] buttons;
	private String[] worlds;
	private GuiScreen parent;
	
	EntityPlayerSP cam;
	
	public GuiWDLMultiworldSelect( GuiScreen parent )
    {
		this.parent = parent;
		EntityClientPlayerMP player = WDL.tp;
        cam = new EntityPlayerSP(WDL.mc, WDL.wc, new Session("Camera", ""), player.dimension);
        cam.setLocationAndAngles(player.posX, player.posY - player.yOffset, player.posZ, player.rotationYaw, 0.0f );
        yaw = player.rotationYaw;
        thirdPersonViewSave = WDL.mc.gameSettings.thirdPersonView;
        WDL.mc.gameSettings.thirdPersonView = 0;
        WDL.mc.renderViewEntity = cam;
    }

    public void initGui()
    {
    	this.buttonList.clear();
        
        int w = width / 2;
        int h = height / 4;
        
        int columns = width / 150;
        if( columns == 0 )
        	columns = 1;
        int buttonWidth = width / columns - 5;
        
        cancelBtn = new GuiButton( 100, w-100, height-30, "Cancel" );
        this.buttonList.add( cancelBtn );
        
        String linkedWorlds = WDL.baseProps.getProperty("LinkedWorlds");
        String[] tempWorlds = linkedWorlds.split("[|]");
        String[] tempNames = new String[tempWorlds.length];
        
        int validWorlds = 0;
        for( int i = 0; i<tempWorlds.length; i++ )
        {
        	if( tempWorlds[i].isEmpty() )
        	{
        		tempWorlds[i] = null;
        		continue;
        	}
        	Properties worldProps = WDL.loadWorldProps(tempWorlds[i]);
        	if( worldProps == null )
        	{
        		tempWorlds[i] = null;
        		continue;
        	}
        	else
        	{
        		validWorlds++;
        		tempNames[i] = worldProps.getProperty("WorldName");
        	}
        }
        
        if( columns > validWorlds + 1 )
        	columns = validWorlds + 1;
        
        int spaceLeft = (width - columns*buttonWidth)/2;
        
        worlds = new String[validWorlds];
        buttons = new GuiButton[validWorlds + 1];
        
        int wi = 0;
        for( int i = 0; i<tempWorlds.length; i++ )
        {
        	if( tempWorlds[i] != null )
        	{
        		worlds[wi] = tempWorlds[i];
        		buttons[wi] = new GuiButton(wi, (wi%columns)*buttonWidth + spaceLeft, height - 60 - (wi/columns)*21 , buttonWidth, 20, tempNames[i]);
        		this.buttonList.add(buttons[wi]);
        		wi++;
        	}
        }
        
        int newWorldPos = buttons.length-1;
        if( !newWorld ) // Needed to make resizing the window work when the TextBox is visible
        {
        	buttons[newWorldPos] = new GuiButton(newWorldPos, (newWorldPos%columns)*buttonWidth + spaceLeft, height - 60 - (newWorldPos/columns)*21 , buttonWidth, 20, "< New Name >");
        	this.buttonList.add(buttons[newWorldPos]);
        }

        newNameField = new GuiTextField( fontRenderer, (newWorldPos%columns)*buttonWidth + spaceLeft, height - 60 - (newWorldPos/columns)*21 + 1, buttonWidth, 18 );
    }

    protected void actionPerformed(GuiButton guibutton)
    {
    	if( !guibutton.enabled )
    		return;
    	newWorld = false;
    	if( guibutton.id == worlds.length ) //New world
    	{
    		newWorld = true;
    		this.buttonList.remove(buttons[worlds.length]);
    	}
    	else if( guibutton.id == 100 ) //Cancel
    	{
    		mc.displayGuiScreen(null);
    		mc.setIngameFocus();
    	}
    	else
    	{
    		worldSelected(worlds[guibutton.id]);
    	}
    }

    protected void mouseClicked(int par1, int par2, int par3)
    {
    	super.mouseClicked(par1, par2, par3);
    	if( newWorld )
    		newNameField.mouseClicked(par1, par2, par3);
    }
    
    protected void keyTyped(char par1, int par2)
    {
    	super.keyTyped(par1, par2);
    	if( newNameField.isFocused() )
    	{
    		newNameField.textboxKeyTyped(par1, par2);
    		if( par2 == 28 )// Return key
    		{
    			String s = newNameField.getText();
    			if( s != null && !s.isEmpty() )
    				worldSelected( addMultiworld( s ) );
    		}
    	}
    }
    
    public void updateScreen()
    {
    	newNameField.updateCursorCounter();
    	super.updateScreen();
    }
    
    public void drawScreen(int i, int j, float f)
    {
        //drawDefaultBackground();
    	drawRect(width/2-120, 0, width/2+120, height/16+25, 0xc0000000);
        if(parent == null)
        	drawCenteredString(fontRenderer, "World Downloader - Trying To Start Download", width / 2, height/16, 0xffffff);
        else
        	drawCenteredString(fontRenderer, "World Downloader - Trying To Change Options", width / 2, height/16, 0xffffff);
        drawCenteredString(fontRenderer, "Where are you?", width / 2, height/16+10, 0xff0000);
        
        cam.prevRotationPitch = cam.rotationPitch = 0;
        cam.prevRotationYaw = cam.rotationYaw = yaw;
        
        float radius = 0.475f; //Min: 0.475f
        // field_71439_g == thePlayer
        cam.lastTickPosY = cam.prevPosY = cam.posY = WDL.tp.posY;
        cam.lastTickPosX = cam.prevPosX = cam.posX = WDL.tp.posX - radius * Math.sin(yaw/180.0*Math.PI);
        cam.lastTickPosZ = cam.prevPosZ = cam.posZ = WDL.tp.posZ + radius * Math.cos(yaw/180.0*Math.PI);
        
        float baseSpeed = 1.0f;
        yaw += baseSpeed *( 1.0f + 0.7f * Math.cos((yaw+45.0f)/45.0*Math.PI) );
        
        if( newWorld )
        	newNameField.drawTextBox();
        
    	super.drawScreen(i, j, f);
    }
    
    public void onGuiClosed() {
    	super.onGuiClosed();
    	WDL.mc.gameSettings.thirdPersonView = thirdPersonViewSave;
    	mc.renderViewEntity = WDL.tp;
    }
    
    private void worldSelected( String w )
    {
    	WDL.worldName = w;
    	WDL.isMultiworld = true;
    	WDL.propsFound = true;
    	if( parent == null )
    	{
    		WDL.start();
    		mc.displayGuiScreen(null);
    		mc.setIngameFocus();
    	}
    	else
    	{
    		WDL.worldProps = WDL.loadWorldProps( w );
    		mc.displayGuiScreen( new GuiWDL(parent) );
    	}
    }
    
    private String addMultiworld( String name )
    {
    	String world = name;
    	String invalidChars = "\\/:*?\"<>|";
		for( char c : invalidChars.toCharArray())
		{
			world = world.replace(c, '_');
		}

    	new File( mc.mcDataDir, "saves/" + WDL.baseFolderName + " - " + world ).mkdirs();
    	
    	Properties newProps = new Properties( WDL.baseProps );
    	newProps.setProperty("WorldName", name);
    	
    	String[] newWorlds = new String[ worlds.length+1 ];
    	for( int i = 0; i<worlds.length; i++ )
    		newWorlds[i] = worlds[i];
    	newWorlds[ newWorlds.length-1 ] = world;
    	
    	String newLinkedWorlds = "";
    	for( String s : newWorlds )
    		newLinkedWorlds += s + "|";
    	
    	WDL.baseProps.setProperty("LinkedWorlds", newLinkedWorlds);
    	WDL.saveProps(world, newProps);
    	
    	return world;
    }
}
