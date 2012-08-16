package net.minecraft.src;

import java.net.ConnectException;
import java.net.UnknownHostException;

class ThreadConnectToServer extends Thread
{
    /** The IP address or domain used to connect. */
    final String ip;

    /** The port used to connect. */
    final int port;

    /** A reference to the GuiConnecting object. */
    final GuiConnecting connectingGui;

    ThreadConnectToServer(GuiConnecting par1GuiConnecting, String par2Str, int par3)
    {
        this.connectingGui = par1GuiConnecting;
        this.ip = par2Str;
        this.port = par3;
        /* WORLD DOWNLOADER ---> */
        WorldDL.lastServerHostname = WorldDL.serverHostname;
        WorldDL.serverHostname = par2Str;
        /* <--- WORLD DOWNLOADER */
    }

    public void run()
    {
        try
        {
            GuiConnecting.setNetClientHandler(this.connectingGui, new NetClientHandler(GuiConnecting.func_74256_a(this.connectingGui), this.ip, this.port));

            if (GuiConnecting.isCancelled(this.connectingGui))
            {
                return;
            }

            GuiConnecting.getNetClientHandler(this.connectingGui).addToSendQueue(new Packet2ClientProtocol(39, GuiConnecting.func_74254_c(this.connectingGui).session.username, this.ip, this.port));
        }
        catch (UnknownHostException var2)
        {
            if (GuiConnecting.isCancelled(this.connectingGui))
            {
                return;
            }

            GuiConnecting.func_74249_e(this.connectingGui).displayGuiScreen(new GuiDisconnected("connect.failed", "disconnect.genericReason", new Object[] {"Unknown host \'" + this.ip + "\'"}));
        }
        catch (ConnectException var3)
        {
            if (GuiConnecting.isCancelled(this.connectingGui))
            {
                return;
            }

            GuiConnecting.func_74250_f(this.connectingGui).displayGuiScreen(new GuiDisconnected("connect.failed", "disconnect.genericReason", new Object[] {var3.getMessage()}));
        }
        catch (Exception var4)
        {
            if (GuiConnecting.isCancelled(this.connectingGui))
            {
                return;
            }

            var4.printStackTrace();
            GuiConnecting.func_74251_g(this.connectingGui).displayGuiScreen(new GuiDisconnected("connect.failed", "disconnect.genericReason", new Object[] {var4.toString()}));
        }
    }
}
