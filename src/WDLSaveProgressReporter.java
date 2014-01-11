package net.minecraft.wdl;

public class WDLSaveProgressReporter implements Runnable
{
    public void run()
    {
        while (WDL.saving)
        {
            WDL.chatMsg("Saving...");

            try
            {
                Thread.sleep(10000L);
            }
            catch (InterruptedException var2)
            {
                var2.printStackTrace();
            }
        }
    }

    public void start()
    {
        Thread var1 = new Thread(this);
        var1.start();
    }
}
