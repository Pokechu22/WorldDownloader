package net.minecraft.src;

import java.io.IOException;

public class TaskOnlineConnect extends TaskLongRunning
{
    private NetClientHandler field_96586_a;
    private final McoServer field_96585_c;
    private final GuiScreen field_96584_d;

    public TaskOnlineConnect(GuiScreen par1GuiScreen, McoServer par2McoServer)
    {
        this.field_96584_d = par1GuiScreen;
        this.field_96585_c = par2McoServer;
    }

    public void run()
    {
        this.setMessage(I18n.getString("mco.connect.connecting"));
        McoClient var1 = new McoClient(this.getMinecraft().getSession());
        boolean var2 = false;
        boolean var3 = false;
        int var4 = 5;
        McoServerAddress var5 = null;

        for (int var6 = 0; var6 < 10 && !this.wasScreenClosed(); ++var6)
        {
            try
            {
                var5 = var1.func_96374_a(this.field_96585_c.field_96408_a);
                var2 = true;
            }
            catch (ExceptionRetryCall var8)
            {
                var4 = var8.field_96393_c;
            }
            catch (ExceptionMcoService var9)
            {
                var3 = true;
                this.setFailedMessage(var9.toString());
                Minecraft.getMinecraft().getLogAgent().logSevere(var9.toString());
                break;
            }
            catch (IOException var10)
            {
                Minecraft.getMinecraft().getLogAgent().logWarning("Realms: could not parse response");
            }
            catch (Exception var11)
            {
                var3 = true;
                this.setFailedMessage(var11.getLocalizedMessage());
            }

            if (var2)
            {
                break;
            }

            this.func_111251_a(var4);
        }

        if (!this.wasScreenClosed() && !var3)
        {
            if (var2)
            {
                ServerAddress var12 = ServerAddress.func_78860_a(var5.field_96417_a);
                this.func_96582_a(var12.getIP(), var12.getPort());
            }
            else
            {
                this.getMinecraft().displayGuiScreen(this.field_96584_d);
            }
        }
    }

    private void func_111251_a(int par1)
    {
        try
        {
            Thread.sleep((long)(par1 * 1000));
        }
        catch (InterruptedException var3)
        {
            Minecraft.getMinecraft().getLogAgent().logWarning(var3.getLocalizedMessage());
        }
    }

    private void func_96582_a(String par1Str, int par2)
    {
        /* WDL >>> */
        WDL.mcos = field_96585_c;
        /* <<< WDL */

        (new ThreadOnlineConnect(this, par1Str, par2)).start();
    }

    public void updateScreen()
    {
        if (this.field_96586_a != null)
        {
            this.field_96586_a.processReadPackets();
        }
    }

    static NetClientHandler func_96583_a(TaskOnlineConnect par0TaskOnlineConnect, NetClientHandler par1NetClientHandler)
    {
        return par0TaskOnlineConnect.field_96586_a = par1NetClientHandler;
    }

    static GuiScreen func_98172_a(TaskOnlineConnect par0TaskOnlineConnect)
    {
        return par0TaskOnlineConnect.field_96584_d;
    }

    static NetClientHandler func_96580_a(TaskOnlineConnect par0TaskOnlineConnect)
    {
        return par0TaskOnlineConnect.field_96586_a;
    }
}
