package net.minecraft.wdl;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

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
    private GuiTextField spawnX;
    private GuiTextField spawnY;
    private GuiTextField spawnZ;
    private int spawnTextY;

    public GuiWDLWorld(GuiScreen var1)
    {
        this.parent = var1;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        this.buttonList.clear();
        this.title = "World Options for " + WDL.baseFolderName.replace('@', ':');
        int var1 = this.width / 2;
        int var2 = this.height / 4;
        int var3 = var2 - 15;
        this.gameModeBtn = new GuiButton(1, var1 - 100, var3, "Game Mode: ERROR");
        this.buttonList.add(this.gameModeBtn);
        this.updateGameMode(false);
        var3 += 22;
        this.timeBtn = new GuiButton(2, var1 - 100, var3, "Time: ERROR");
        this.buttonList.add(this.timeBtn);
        this.updateTime(false);
        var3 += 22;
        this.weatherBtn = new GuiButton(3, var1 - 100, var3, "Weather: ERROR");
        this.buttonList.add(this.weatherBtn);
        this.updateWeather(false);
        var3 += 22;
        this.spawnBtn = new GuiButton(4, var1 - 100, var3, "Spawn Position: ERROR");
        this.buttonList.add(this.spawnBtn);
        var3 += 22;
        this.spawnTextY = var3 + 4;
        this.spawnX = new GuiTextField(this.fontRendererObj, var1 - 87, var3, 50, 16);
        this.spawnY = new GuiTextField(this.fontRendererObj, var1 - 19, var3, 50, 16);
        this.spawnZ = new GuiTextField(this.fontRendererObj, var1 + 48, var3, 50, 16);
        this.spawnX.func_146203_f(7);
        this.spawnY.func_146203_f(7);
        this.spawnZ.func_146203_f(7);
        var3 += 18;
        this.pickSpawnBtn = new GuiButton(5, var1 - 0, var3, 100, 20, "Current position");
        this.buttonList.add(this.pickSpawnBtn);
        this.updateSpawn(false);
        this.updateSpawnXYZ(false);
        this.buttonList.add(new GuiButton(100, var1 - 100, var2 + 150, "Done"));
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton var1)
    {
        if (var1.enabled)
        {
            if (var1.id == 1)
            {
                this.updateGameMode(true);
            }
            else if (var1.id == 2)
            {
                this.updateTime(true);
            }
            else if (var1.id == 3)
            {
                this.updateWeather(true);
            }
            else if (var1.id == 4)
            {
                this.updateSpawn(true);
            }
            else if (var1.id == 5)
            {
                this.pickSpawn();
            }
            else if (var1.id == 100)
            {
                if (this.showSpawnFields)
                {
                    this.updateSpawnXYZ(true);
                }

                WDL.saveProps();
                this.mc.displayGuiScreen(this.parent);
            }
        }
    }

    /**
     * Called when the mouse is clicked.
     */
    protected void mouseClicked(int var1, int var2, int var3)
    {
        super.mouseClicked(var1, var2, var3);

        if (this.showSpawnFields)
        {
            this.spawnX.mouseClicked(var1, var2, var3);
            this.spawnY.mouseClicked(var1, var2, var3);
            this.spawnZ.mouseClicked(var1, var2, var3);
        }
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char var1, int var2)
    {
        super.keyTyped(var1, var2);
        this.spawnX.textboxKeyTyped(var1, var2);
        this.spawnY.textboxKeyTyped(var1, var2);
        this.spawnZ.textboxKeyTyped(var1, var2);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        this.spawnX.updateCursorCounter();
        this.spawnY.updateCursorCounter();
        this.spawnZ.updateCursorCounter();
        super.updateScreen();
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int var1, int var2, float var3)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, this.height / 4 - 40, 16777215);

        if (this.showSpawnFields)
        {
            this.drawString(this.fontRendererObj, "X:", this.width / 2 - 99, this.spawnTextY, 16777215);
            this.drawString(this.fontRendererObj, "Y:", this.width / 2 - 31, this.spawnTextY, 16777215);
            this.drawString(this.fontRendererObj, "Z:", this.width / 2 + 37, this.spawnTextY, 16777215);
            this.spawnX.drawTextBox();
            this.spawnY.drawTextBox();
            this.spawnZ.drawTextBox();
        }

        super.drawScreen(var1, var2, var3);
    }

    private void updateGameMode(boolean var1)
    {
        String var2 = WDL.baseProps.getProperty("GameType");

        if (var2.equals("keep"))
        {
            if (var1)
            {
                WDL.baseProps.setProperty("GameType", "creative");
                this.updateGameMode(false);
            }
            else
            {
                this.gameModeBtn.displayString = "Game Mode: Don\'t change";
            }
        }
        else if (var2.equals("creative"))
        {
            if (var1)
            {
                WDL.baseProps.setProperty("GameType", "survival");
                this.updateGameMode(false);
            }
            else
            {
                this.gameModeBtn.displayString = "Game Mode: Creative";
            }
        }
        else if (var2.equals("survival"))
        {
            if (var1)
            {
                WDL.baseProps.setProperty("GameType", "hardcore");
                this.updateGameMode(false);
            }
            else
            {
                this.gameModeBtn.displayString = "Game Mode: Survival";
            }
        }
        else if (var2.equals("hardcore"))
        {
            if (var1)
            {
                WDL.baseProps.setProperty("GameType", "keep");
                this.updateGameMode(false);
            }
            else
            {
                this.gameModeBtn.displayString = "Game Mode: Survival Hardcore";
            }
        }
    }

    private void updateTime(boolean var1)
    {
        String var2 = WDL.baseProps.getProperty("Time");

        if (var2.equals("keep"))
        {
            if (var1)
            {
                WDL.baseProps.setProperty("Time", "23000");
                this.updateTime(false);
            }
            else
            {
                this.timeBtn.displayString = "Time: Don\'t change";
            }
        }
        else if (var2.equals("23000"))
        {
            if (var1)
            {
                WDL.baseProps.setProperty("Time", "0");
                this.updateTime(false);
            }
            else
            {
                this.timeBtn.displayString = "Time: Sunrise";
            }
        }
        else if (var2.equals("0"))
        {
            if (var1)
            {
                WDL.baseProps.setProperty("Time", "6000");
                this.updateTime(false);
            }
            else
            {
                this.timeBtn.displayString = "Time: Morning";
            }
        }
        else if (var2.equals("6000"))
        {
            if (var1)
            {
                WDL.baseProps.setProperty("Time", "11500");
                this.updateTime(false);
            }
            else
            {
                this.timeBtn.displayString = "Time: Noon";
            }
        }
        else if (var2.equals("11500"))
        {
            if (var1)
            {
                WDL.baseProps.setProperty("Time", "12500");
                this.updateTime(false);
            }
            else
            {
                this.timeBtn.displayString = "Time: Evening";
            }
        }
        else if (var2.equals("12500"))
        {
            if (var1)
            {
                WDL.baseProps.setProperty("Time", "18000");
                this.updateTime(false);
            }
            else
            {
                this.timeBtn.displayString = "Time: Sunset";
            }
        }
        else if (var2.equals("18000"))
        {
            if (var1)
            {
                WDL.baseProps.setProperty("Time", "keep");
                this.updateTime(false);
            }
            else
            {
                this.timeBtn.displayString = "Time: Midnight";
            }
        }
    }

    private void updateWeather(boolean var1)
    {
        String var2 = WDL.baseProps.getProperty("Weather");

        if (var2.equals("keep"))
        {
            if (var1)
            {
                WDL.baseProps.setProperty("Weather", "sunny");
                this.updateWeather(false);
            }
            else
            {
                this.weatherBtn.displayString = "Weather: Don\'t change";
            }
        }
        else if (var2.equals("sunny"))
        {
            if (var1)
            {
                WDL.baseProps.setProperty("Weather", "rain");
                this.updateWeather(false);
            }
            else
            {
                this.weatherBtn.displayString = "Weather: Sunny";
            }
        }
        else if (var2.equals("rain"))
        {
            if (var1)
            {
                WDL.baseProps.setProperty("Weather", "thunderstorm");
                this.updateWeather(false);
            }
            else
            {
                this.weatherBtn.displayString = "Weather: Rain";
            }
        }
        else if (var2.equals("thunderstorm"))
        {
            if (var1)
            {
                WDL.baseProps.setProperty("Weather", "keep");
                this.updateWeather(false);
            }
            else
            {
                this.weatherBtn.displayString = "Weather: Thunderstorm";
            }
        }
    }

    private void updateSpawn(boolean var1)
    {
        String var2 = WDL.worldProps.getProperty("Spawn");
        this.showSpawnFields = false;
        this.pickSpawnBtn.field_146125_m = false;

        if (var2.equals("auto"))
        {
            if (var1)
            {
                WDL.worldProps.setProperty("Spawn", "player");
                this.updateSpawn(false);
            }
            else
            {
                this.spawnBtn.displayString = "Spawn Position: Automatic";
            }
        }
        else if (var2.equals("player"))
        {
            if (var1)
            {
                WDL.worldProps.setProperty("Spawn", "xyz");
                this.updateSpawn(false);
            }
            else
            {
                this.spawnBtn.displayString = "Spawn Position: Player position";
            }
        }
        else if (var2.equals("xyz"))
        {
            if (var1)
            {
                WDL.worldProps.setProperty("Spawn", "auto");
                this.updateSpawn(false);
            }
            else
            {
                this.spawnBtn.displayString = "Spawn Position:";
                this.showSpawnFields = true;
                this.pickSpawnBtn.field_146125_m = true;
            }
        }
    }

    private void updateSpawnXYZ(boolean var1)
    {
        if (var1)
        {
            try
            {
                int var2 = Integer.parseInt(this.spawnX.getText());
                int var3 = Integer.parseInt(this.spawnY.getText());
                int var4 = Integer.parseInt(this.spawnZ.getText());
                WDL.worldProps.setProperty("SpawnX", String.valueOf(var2));
                WDL.worldProps.setProperty("SpawnY", String.valueOf(var3));
                WDL.worldProps.setProperty("SpawnZ", String.valueOf(var4));
            }
            catch (NumberFormatException var5)
            {
                this.updateSpawn(true);
            }
        }
        else
        {
            this.spawnX.setText(WDL.worldProps.getProperty("SpawnX"));
            this.spawnY.setText(WDL.worldProps.getProperty("SpawnY"));
            this.spawnZ.setText(WDL.worldProps.getProperty("SpawnZ"));
        }
    }

    private void pickSpawn()
    {
        int var1 = (int)Math.floor(WDL.tp.posX);
        int var2 = (int)Math.floor(WDL.tp.posY);
        int var3 = (int)Math.floor(WDL.tp.posZ);
        this.spawnX.setText(String.valueOf(var1));
        this.spawnY.setText(String.valueOf(var2));
        this.spawnZ.setText(String.valueOf(var3));
    }
}
