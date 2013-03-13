package net.minecraft.src;

import java.util.List;
import net.minecraft.client.Minecraft;

public class GuiWDLWorld extends GuiScreen
{
	private String title = "";
	
	private GuiScreen parent;
	
	private GuiButton gameModeBtn;
	private GuiButton timeBtn;
	private GuiButton weatherBtn;
	private GuiButton spawnBtn;
	private GuiButton pickSpawnBtn;
	
	private boolean showSpawnFields = false;
	private GuiTextField spawnX, spawnY, spawnZ;
	private int spawnTextY;

	public GuiWDLWorld( GuiScreen parent )
    {
		this.parent = parent;
    }

    public void initGui()
    {
    	this.buttonList.clear();
        
        title = "World Options for " + WDL.baseFolderName.replace('@', ':');
        
        int w = width / 2;
        int h = height / 4;
        
        int hi = h-15;
        
        gameModeBtn = new GuiButton( 1, w-100, hi, "Game Mode: ERROR" );
        this.buttonList.add( gameModeBtn );
        updateGameMode(false);
        
        hi += 22;
        timeBtn = new GuiButton( 2, w-100, hi, "Time: ERROR" );
        this.buttonList.add( timeBtn );
        updateTime(false);
        
        hi += 22;
        weatherBtn = new GuiButton( 3, w-100, hi, "Weather: ERROR" );
        this.buttonList.add( weatherBtn );
        updateWeather(false);
        
        hi += 22;
        spawnBtn = new GuiButton( 4, w-100, hi, "Spawn Position: ERROR" );
        this.buttonList.add( spawnBtn );
        
        hi += 22;
        spawnTextY = hi + 4;
        spawnX = new GuiTextField( fontRenderer, w-87, hi, 50, 16 );
        spawnY = new GuiTextField( fontRenderer, w-19, hi, 50, 16 );
        spawnZ = new GuiTextField( fontRenderer, w+48, hi, 50, 16 );
        spawnX.setMaxStringLength(7);
        spawnY.setMaxStringLength(7);
        spawnZ.setMaxStringLength(7);
        
        hi += 18;
        pickSpawnBtn = new GuiButton(5, w-0, hi, 100, 20, "Current position");
        this.buttonList.add(pickSpawnBtn);
        
        updateSpawn(false);
        updateSpawnXYZ(false);
        
        this.buttonList.add( new GuiButton( 100, w-100, h+150, "Done" ) );
    }

    protected void actionPerformed(GuiButton guibutton)
    {
    	if( !guibutton.enabled )
    		return;
    	
    	if( guibutton.id == 1 ) //Game Mode
    	{
    		updateGameMode(true);
    	}
    	else if( guibutton.id == 2 ) //Time
    	{
    		updateTime(true);
    	}
    	else if( guibutton.id == 3 ) //Weather
    	{
    		updateWeather(true);
    	}
    	else if( guibutton.id == 4 ) //Spawn Position
    	{
    		updateSpawn(true);
    	}
    	else if( guibutton.id == 5 ) //Pick XYZ
    	{
    		pickSpawn();
    	}
    	else if( guibutton.id == 100 ) //Done
    	{
    		if( showSpawnFields )
    			updateSpawnXYZ(true);
    		
    		WDL.saveProps();
    		mc.displayGuiScreen( parent );
    	}
    }

    protected void mouseClicked(int i, int j, int k) {
    	super.mouseClicked(i, j, k);
    	
    	if( showSpawnFields )
    	{
    		spawnX.mouseClicked(i, j, k);
    		spawnY.mouseClicked(i, j, k);
    		spawnZ.mouseClicked(i, j, k);
    	}
    }
    
    protected void keyTyped(char c, int i) {
    	super.keyTyped(c, i);
    	
    	spawnX.textboxKeyTyped(c, i);
    	spawnY.textboxKeyTyped(c, i);
    	spawnZ.textboxKeyTyped(c, i);
    }
    
    public void updateScreen()
    {
    	spawnX.updateCursorCounter();
    	spawnY.updateCursorCounter();
    	spawnZ.updateCursorCounter();
        super.updateScreen();
    }

    public void drawScreen(int i, int j, float f)
    {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, title, width / 2, height / 4 - 40, 0xffffff);

        if( showSpawnFields )
        {
            drawString(fontRenderer, "X:", width / 2 - 99, spawnTextY, 0xffffff);
            drawString(fontRenderer, "Y:", width / 2 - 31, spawnTextY, 0xffffff);
            drawString(fontRenderer, "Z:", width / 2 + 37, spawnTextY, 0xffffff);
        	spawnX.drawTextBox();
        	spawnY.drawTextBox();
        	spawnZ.drawTextBox();
        }

        super.drawScreen(i, j, f);
    }
    
    private void updateGameMode( boolean btnClicked )
    {
    	String gameType = WDL.baseProps.getProperty("GameType");
    	
    	if( gameType.equals("keep") )
    	{
    		if( btnClicked )
    		{
    			WDL.baseProps.setProperty("GameType", "creative");
    			updateGameMode(false);
    		}
    		else
    			gameModeBtn.displayString = "Game Mode: Don't change";
    	}
    	else if( gameType.equals("creative") )
    	{
    		if( btnClicked )
    		{
    			WDL.baseProps.setProperty("GameType", "survival");
    			updateGameMode(false);
    		}
    		else
    			gameModeBtn.displayString = "Game Mode: Creative";
    	}
    	else if( gameType.equals("survival") )
    	{
    		if( btnClicked )
    		{
    			WDL.baseProps.setProperty("GameType", "hardcore");
    			updateGameMode(false);
    		}
    		else
    			gameModeBtn.displayString = "Game Mode: Survival";
    	}
    	else if( gameType.equals("hardcore") )
    	{
    		if( btnClicked )
    		{
    			WDL.baseProps.setProperty("GameType", "keep");
    			updateGameMode(false);
    		}
    		else
    			gameModeBtn.displayString = "Game Mode: Survival Hardcore";
    	}
    }
    
    private void updateTime( boolean btnClicked )
    {
    	String time = WDL.baseProps.getProperty("Time");
    	
    	if( time.equals("keep") )
    	{
    		if( btnClicked )
    		{
    			WDL.baseProps.setProperty("Time", "23000");
    			updateTime(false);
    		}
    		else
    			timeBtn.displayString = "Time: Don't change";
    	}
    	else if( time.equals("23000") )
    	{
    		if( btnClicked )
    		{
    			WDL.baseProps.setProperty("Time", "0");
    			updateTime(false);
    		}
    		else
    			timeBtn.displayString = "Time: Sunrise";
    	}
    	else if( time.equals("0") )
    	{
    		if( btnClicked )
    		{
    			WDL.baseProps.setProperty("Time", "6000");
    			updateTime(false);
    		}
    		else
    			timeBtn.displayString = "Time: Morning";
    	}
    	else if( time.equals("6000") )
    	{
    		if( btnClicked )
    		{
    			WDL.baseProps.setProperty("Time", "11500");
    			updateTime(false);
    		}
    		else
    			timeBtn.displayString = "Time: Noon";
    	}
    	else if( time.equals("11500") )
    	{
    		if( btnClicked )
    		{
    			WDL.baseProps.setProperty("Time", "12500");
    			updateTime(false);
    		}
    		else
    			timeBtn.displayString = "Time: Evening";
    	}
    	else if( time.equals("12500") )
    	{
    		if( btnClicked )
    		{
    			WDL.baseProps.setProperty("Time", "18000");
    			updateTime(false);
    		}
    		else
    			timeBtn.displayString = "Time: Sunset";
    	}
    	else if( time.equals("18000") )
    	{
    		if( btnClicked )
    		{
    			WDL.baseProps.setProperty("Time", "keep");
    			updateTime(false);
    		}
    		else
    			timeBtn.displayString = "Time: Midnight";
    	}
    }
    private void updateWeather( boolean btnClicked )
    {
    	String weather = WDL.baseProps.getProperty("Weather");
    	
    	if( weather.equals("keep") )
    	{
    		if( btnClicked )
    		{
    			WDL.baseProps.setProperty("Weather", "sunny");
    			updateWeather(false);
    		}
    		else
    			weatherBtn.displayString = "Weather: Don't change";
    	}
    	else if( weather.equals("sunny") )
    	{
    		if( btnClicked )
    		{
    			WDL.baseProps.setProperty("Weather", "rain");
    			updateWeather(false);
    		}
    		else
    			weatherBtn.displayString = "Weather: Sunny";
    	}
    	else if( weather.equals("rain") )
    	{
    		if( btnClicked )
    		{
    			WDL.baseProps.setProperty("Weather", "thunderstorm");
    			updateWeather(false);
    		}
    		else
    			weatherBtn.displayString = "Weather: Rain";
    	}
    	else if( weather.equals("thunderstorm") )
    	{
    		if( btnClicked )
    		{
    			WDL.baseProps.setProperty("Weather", "keep");
    			updateWeather(false);
    		}
    		else
    			weatherBtn.displayString = "Weather: Thunderstorm";
    	}
    }
    
    private void updateSpawn( boolean btnClicked )
    {
    	String spawn = WDL.worldProps.getProperty("Spawn");
    	showSpawnFields = false;
    	pickSpawnBtn.drawButton = false;
    	
    	if( spawn.equals("auto") )
    	{
    		if( btnClicked )
    		{
    			WDL.worldProps.setProperty("Spawn", "player");
    			updateSpawn(false);
    		}
    		else
    			spawnBtn.displayString = "Spawn Position: Automatic";
    	}
    	else if( spawn.equals("player") )
    	{
    		if( btnClicked )
    		{
    			WDL.worldProps.setProperty("Spawn", "xyz");
    			updateSpawn(false);
    		}
    		else
    			spawnBtn.displayString = "Spawn Position: Player position";
    	}
    	else if( spawn.equals("xyz") )
    	{
    		if( btnClicked )
    		{
    			WDL.worldProps.setProperty("Spawn", "auto");
    			updateSpawn(false);
    		}
    		else
    		{
    			spawnBtn.displayString = "Spawn Position:";
    			showSpawnFields = true;
    			pickSpawnBtn.drawButton = true;
    		}
    	}
    }
    
    private void updateSpawnXYZ( boolean write )
    {
    	if( write )
    	{
    		try {
	    		int x = Integer.parseInt( spawnX.getText() );
	    		int y = Integer.parseInt( spawnY.getText() );
	    		int z = Integer.parseInt( spawnZ.getText() );
	    		WDL.worldProps.setProperty("SpawnX", String.valueOf(x));
	    		WDL.worldProps.setProperty("SpawnY", String.valueOf(y));
	    		WDL.worldProps.setProperty("SpawnZ", String.valueOf(z));
    		}
    		catch( NumberFormatException e )
    		{
    			updateSpawn(true);
    		}
    	}
    	else
    	{
    		spawnX.setText( WDL.worldProps.getProperty("SpawnX") );
    		spawnY.setText( WDL.worldProps.getProperty("SpawnY") );
    		spawnZ.setText( WDL.worldProps.getProperty("SpawnZ") );
    	}
    }
    
    private void pickSpawn()
    {
    	int x = (int)Math.floor(WDL.tp.posX);
    	int y = (int)Math.floor(WDL.tp.posY);
    	int z = (int)Math.floor(WDL.tp.posZ);
    	spawnX.setText(String.valueOf(x));
    	spawnY.setText(String.valueOf(y));
    	spawnZ.setText(String.valueOf(z));
    }
}
