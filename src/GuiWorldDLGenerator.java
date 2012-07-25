package net.minecraft.src;

import java.util.List;
import net.minecraft.client.Minecraft;

public class GuiWorldDLGenerator extends GuiScreen
{
	private String title = "";
	
	private GuiScreen parent;
	
    private GuiTextField seedField;
	private GuiButton generatorBtn;
	private GuiButton generateStructuresBtn;

	public GuiWorldDLGenerator( GuiScreen parent )
    {
		this.parent = parent;
    }

    public void initGui()
    {
        controlList.clear();
        
        title = "World Generator Options for " + WorldDL.folderName.replace('@', ':');
        
        int w = width / 2;
        int h = height / 4;
        
        int hi = h-15;
        
        seedField = new GuiTextField( this, fontRenderer, width / 2 - 70, hi, 168, 18, "ERROR" );
        updateSeed( false );
        
        hi += 22;
        generatorBtn = new GuiButton(1, w-100, hi, "World Generator: ERROR");
        controlList.add(generatorBtn);
        updateGenerator( false );
        
        hi += 22;
        generateStructuresBtn = new GuiButton( 2, w-100, hi, "Generate Structures: ERROR" );
        controlList.add(generateStructuresBtn);
        updateGenerateStructures( false );
        
        controlList.add( new GuiButton( 100, w-100, h+150, "Done" ) );
    }

    protected void actionPerformed(GuiButton guibutton)
    {
    	if( !guibutton.enabled )
    		return;
    	
    	if( guibutton.id == 1 ) //Generator
    		updateGenerator( true );
    	else if( guibutton.id == 2 ) //Generate structures
    		updateGenerateStructures( true );
    	else if( guibutton.id == 100 ) //Done
    	{
    		updateSeed( true );
    		WorldDL.saveProps();
    		mc.displayGuiScreen( parent );
    	}
    }

    protected void mouseClicked(int i, int j, int k) {
    	super.mouseClicked(i, j, k);
    	seedField.mouseClicked(i, j, k);
    }
    
    protected void keyTyped(char c, int i) {
    	super.keyTyped(c, i);
    	if( seedField.isFocused )
    		seedField.textboxKeyTyped(c, i);
    }
    
    public void updateScreen()
    {
    	seedField.updateCursorCounter();
        super.updateScreen();
    }

    public void drawScreen(int i, int j, float f)
    {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, title, width / 2, height / 4 - 40, 0xffffff);
        
        drawString(fontRenderer, "Seed:", width / 2 - 99, height / 4 - 10, 0xffffff);
        seedField.drawTextBox();
        super.drawScreen(i, j, f);
    }
    
    private void updateGenerator( boolean btnClicked )
    {
		String generatorName = WorldDL.worldProps.getProperty("GeneratorName");
		if( generatorName.equals("default") )
		{
			if( btnClicked )
			{
				WorldDL.worldProps.setProperty("GeneratorName", "flat");
				WorldDL.worldProps.setProperty("GeneratorVersion", "0");
				updateGenerator(false);
			}
			else
				generatorBtn.displayString = "World Generator: Default";
		}
		else
		{
			if( btnClicked )
			{
				WorldDL.worldProps.setProperty("GeneratorName", "default");
				WorldDL.worldProps.setProperty("GeneratorVersion", "1");
				updateGenerator(false);
			}
			else
				generatorBtn.displayString = "World Generator: Flat";
		}
    }
    
    private void updateGenerateStructures( boolean btnClicked )
    {
		String generateStructures = WorldDL.worldProps.getProperty("MapFeatures");
		if( generateStructures.equals("true") )
		{
			if( btnClicked )
			{
				WorldDL.worldProps.setProperty("MapFeatures", "false");
				updateGenerateStructures(false);
			}
			else
				generateStructuresBtn.displayString = "Generate Structures: ON";
		}
		else
		{
			if( btnClicked )
			{
				WorldDL.worldProps.setProperty("MapFeatures", "true");
				updateGenerateStructures(false);
			}
			else
				generateStructuresBtn.displayString = "Generate Structures: OFF";
		}
    }
    
    private void updateSeed( boolean write )
    {
    	if( write )
    	{
    		WorldDL.worldProps.setProperty("RandomSeed", seedField.getText() );
    	}
    	else
    		seedField.setText( WorldDL.worldProps.getProperty("RandomSeed") );
    }
}
