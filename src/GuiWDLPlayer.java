package net.minecraft.src;

import java.util.List;

public class GuiWDLPlayer extends GuiScreen
{
	private String title = "";
	
	private GuiScreen parent;
	
	private GuiButton healthBtn;
	private GuiButton hungerBtn;
	private GuiButton playerPosBtn;
	private GuiButton pickPosBtn;
	
	private boolean showPosFields = false;
	private GuiTextField posX, posY, posZ;
	private int posTextY;

	public GuiWDLPlayer( GuiScreen parent )
    {
		this.parent = parent;
    }

    public void initGui()
    {
    	this.buttonList.clear();
        
        title = "Player Options for " + WDL.baseFolderName.replace('@', ':');
        
        int w = width / 2;
        int h = height / 4;
        
        int hi = h-15;
        
        healthBtn = new GuiButton( 1, w-100, hi, "Health: ERROR" );
        this.buttonList.add( healthBtn );
        updateHealth(false);
        
        hi += 22;
        hungerBtn = new GuiButton( 2, w-100, hi, "Hunger: ERROR" );
        this.buttonList.add( hungerBtn );
        updateHunger(false);
        
        hi += 22;
        playerPosBtn = new GuiButton( 3, w-100, hi, "Player Position: ERROR" );
        this.buttonList.add( playerPosBtn );
        
        hi += 22;
        posTextY = hi + 4;
        posX = new GuiTextField( fontRenderer, w-87, hi, 50, 16 );
        posY = new GuiTextField( fontRenderer, w-19, hi, 50, 16 );
        posZ = new GuiTextField( fontRenderer, w+48, hi, 50, 16 );
        posX.setMaxStringLength(7);
        posY.setMaxStringLength(7);
        posZ.setMaxStringLength(7);
        
        hi += 18;
        pickPosBtn = new GuiButton(4, w-0, hi, 100, 20, "Current position");
        this.buttonList.add(pickPosBtn);
        
        updatePlayerPos(false);
        updatePosXYZ(false);
        
        this.buttonList.add( new GuiButton( 100, w-100, h+150, "Done" ) );
    }

    protected void actionPerformed(GuiButton guibutton)
    {
    	if( !guibutton.enabled )
    		return;
    	
    	if( guibutton.id == 1 ) //Health
    	{
    		updateHealth(true);
    	}
    	else if( guibutton.id == 2 ) //Hunger
    	{
    		updateHunger(true);
    	}
    	else if( guibutton.id == 3 ) //Player Position
    	{
    		updatePlayerPos(true);
    	}
    	else if( guibutton.id == 4 ) //Pick XYZ
    	{
    		pickPlayerPos();
    	}
    	else if( guibutton.id == 100 ) //Done
    	{
    		if( showPosFields )
    			updatePosXYZ(true);
    		
    		WDL.saveProps();
    		mc.displayGuiScreen( parent );
    	}
    }

    protected void mouseClicked(int i, int j, int k) {
    	super.mouseClicked(i, j, k);
    	
    	if( showPosFields )
    	{
    		posX.mouseClicked(i, j, k);
    		posY.mouseClicked(i, j, k);
    		posZ.mouseClicked(i, j, k);
    	}
    }
    
    protected void keyTyped(char c, int i) {
    	super.keyTyped(c, i);
    	
    	posX.textboxKeyTyped(c, i);
    	posY.textboxKeyTyped(c, i);
    	posZ.textboxKeyTyped(c, i);
    }
    
    public void updateScreen()
    {
    	posX.updateCursorCounter();
    	posY.updateCursorCounter();
    	posZ.updateCursorCounter();
        super.updateScreen();
    }

    public void drawScreen(int i, int j, float f)
    {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, title, width / 2, height / 4 - 40, 0xffffff);

        if( showPosFields )
        {
            drawString(fontRenderer, "X:", width / 2 - 99, posTextY, 0xffffff);
            drawString(fontRenderer, "Y:", width / 2 - 31, posTextY, 0xffffff);
            drawString(fontRenderer, "Z:", width / 2 + 37, posTextY, 0xffffff);
        	posX.drawTextBox();
        	posY.drawTextBox();
        	posZ.drawTextBox();
        }

        super.drawScreen(i, j, f);
    }
    
    private void updateHealth( boolean btnClicked )
    {
    	String playerHealth = WDL.baseProps.getProperty("PlayerHealth");
    	
    	if( playerHealth.equals("keep") )
    	{
    		if( btnClicked )
    		{
    			WDL.baseProps.setProperty("PlayerHealth", "20");
    			updateHealth(false);
    		}
    		else
    			healthBtn.displayString = "Health: Don't change";
    	}
    	else if( playerHealth.equals("20") )
    	{
    		if( btnClicked )
    		{
    			WDL.baseProps.setProperty("PlayerHealth", "keep");
    			updateHealth(false);
    		}
    		else
    			healthBtn.displayString = "Health: Full";
    	}
    }
    
    private void updateHunger( boolean btnClicked )
    {
    	String playerFood = WDL.baseProps.getProperty("PlayerFood");
    	
    	if( playerFood.equals("keep") )
    	{
    		if( btnClicked )
    		{
    			WDL.baseProps.setProperty("PlayerFood", "20");
    			updateHunger(false);
    		}
    		else
    			hungerBtn.displayString = "Hunger: Don't change";
    	}
    	else if( playerFood.equals("20") )
    	{
    		if( btnClicked )
    		{
    			WDL.baseProps.setProperty("PlayerFood", "keep");
    			updateHunger(false);
    		}
    		else
    			hungerBtn.displayString = "Hunger: Full";
    	}
    }
    
    private void updatePlayerPos( boolean btnClicked )
    {
    	String playerPos = WDL.worldProps.getProperty("PlayerPos");
    	showPosFields = false;
    	pickPosBtn.drawButton = false;
    	
    	if( playerPos.equals("keep") )
    	{
    		if( btnClicked )
    		{
    			WDL.worldProps.setProperty("PlayerPos", "xyz");
    			updatePlayerPos(false);
    		}
    		else
    			playerPosBtn.displayString = "Player Position: Don't change";
    	}
    	else if( playerPos.equals("xyz") )
    	{
    		if( btnClicked )
    		{
    			WDL.worldProps.setProperty("PlayerPos", "keep");
    			updatePlayerPos(false);
    		}
    		else
    		{
    			playerPosBtn.displayString = "Player Position:";
    			showPosFields = true;
    			pickPosBtn.drawButton = true;
    		}
    	}
    }
    
    private void updatePosXYZ( boolean write )
    {
    	if( write )
    	{
    		try {
	    		int x = Integer.parseInt( posX.getText() );
	    		int y = Integer.parseInt( posY.getText() );
	    		int z = Integer.parseInt( posZ.getText() );
	    		WDL.worldProps.setProperty("PlayerX", String.valueOf(x));
	    		WDL.worldProps.setProperty("PlayerY", String.valueOf(y));
	    		WDL.worldProps.setProperty("PlayerZ", String.valueOf(z));
    		}
    		catch( NumberFormatException e )
    		{
    			updatePlayerPos(true);
    		}
    	}
    	else
    	{
    		posX.setText( WDL.worldProps.getProperty("PlayerX") );
    		posY.setText( WDL.worldProps.getProperty("PlayerY") );
    		posZ.setText( WDL.worldProps.getProperty("PlayerZ") );
    	}
    }
    
    private void pickPlayerPos()
    {
    	int x = (int)Math.floor(WDL.tp.posX);
    	int y = (int)Math.floor(WDL.tp.posY);
    	int z = (int)Math.floor(WDL.tp.posZ);
    	posX.setText(String.valueOf(x));
    	posY.setText(String.valueOf(y));
    	posZ.setText(String.valueOf(z));
    }
}
