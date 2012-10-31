package net.minecraft.src;

public class WDLSaveProgressReporter implements Runnable
{
    public void run()
    {
        while (WDL.saving)
        {
            WDL.chatMsg("Saving...");

            try 
            {
                Thread.sleep(5000L);
            } 
            catch (InterruptedException e) 
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void start()
    {
        Thread t = new Thread(this);
        t.start();
    }
}
