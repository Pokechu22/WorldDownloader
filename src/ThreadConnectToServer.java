package net.minecraft.src;

import java.net.ConnectException;
import java.net.UnknownHostException;
import net.minecraft.client.Minecraft;

class ThreadConnectToServer extends Thread
{
    final Minecraft mc;
    final String hostName;
    final int port;
    final GuiConnecting connectingGui;

    ThreadConnectToServer(GuiConnecting guiconnecting, Minecraft minecraft, String s, int i)
    {
        connectingGui = guiconnecting;
        mc = minecraft;
        hostName = s;
        port = i;
        /* WORLD DOWNLOADER ---> */
	WorldDL.lastServerHostname = WorldDL.serverHostname;
        WorldDL.serverHostname = s;
        /* <--- WORLD DOWNLOADER */
    }

    public void run()
    {
        try
        {
            GuiConnecting.setNetClientHandler(connectingGui, new NetClientHandler(mc, hostName, port));
            if (GuiConnecting.isCancelled(connectingGui))
            {
                return;
            }
            GuiConnecting.getNetClientHandler(connectingGui).addToSendQueue(new Packet2Handshake(mc.session.username));
        }
        catch (UnknownHostException unknownhostexception)
        {
            if (GuiConnecting.isCancelled(connectingGui))
            {
                return;
            }
            mc.displayGuiScreen(new GuiDisconnected("connect.failed", "disconnect.genericReason", new Object[]
                    {
                        (new StringBuilder()).append("Unknown host '").append(hostName).append("'").toString()
                    }));
        }
        catch (ConnectException connectexception)
        {
            if (GuiConnecting.isCancelled(connectingGui))
            {
                return;
            }
            mc.displayGuiScreen(new GuiDisconnected("connect.failed", "disconnect.genericReason", new Object[]
                    {
                        connectexception.getMessage()
                    }));
        }
        catch (Exception exception)
        {
            if (GuiConnecting.isCancelled(connectingGui))
            {
                return;
            }
            exception.printStackTrace();
            mc.displayGuiScreen(new GuiDisconnected("connect.failed", "disconnect.genericReason", new Object[]
                    {
                        exception.toString()
                    }));
        }
    }
}
