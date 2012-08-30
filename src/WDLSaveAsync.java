package net.minecraft.src;

public class WDLSaveAsync implements Runnable
{
    public void run()
    {
        WDL.chatMsg( "Download stopped" );
        WDL.saveEverything();
        WDL.downloading = false;
        WDL.startOnChange = false;
        WDL.mc.getSaveLoader().flushCache();
        WDL.saveHandler.flush();
    }
}
