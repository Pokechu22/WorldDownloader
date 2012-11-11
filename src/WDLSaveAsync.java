package net.minecraft.src;

public class WDLSaveAsync implements Runnable
{
    public void run()
    {
    	// Save everything
        WDL.saveEverything();
        WDL.saving = false;
        WDL.onSaveComplete();
    }
}