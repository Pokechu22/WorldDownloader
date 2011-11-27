// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst 

package net.minecraft.src;

import java.net.ConnectException;
import java.net.UnknownHostException;
import net.minecraft.client.Minecraft;

// Referenced classes of package net.minecraft.src:
//            NetClientHandler, GuiConnecting, Packet2Handshake, Session, 
//            GuiDisconnected

class ThreadConnectToServer extends Thread
{

    final Minecraft mc; /* synthetic field */
    final String hostName; /* synthetic field */
    final int port; /* synthetic field */
    final GuiConnecting connectingGui; /* synthetic field */

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
            if(GuiConnecting.isCancelled(connectingGui))
            {
                return;
            }
            GuiConnecting.getNetClientHandler(connectingGui).addToSendQueue(new Packet2Handshake(mc.session.username));
        }
        catch(UnknownHostException unknownhostexception)
        {
            if(GuiConnecting.isCancelled(connectingGui))
            {
                return;
            }
            mc.displayGuiScreen(new GuiDisconnected("connect.failed", "disconnect.genericReason", new Object[] {
                (new StringBuilder()).append("Unknown host '").append(hostName).append("'").toString()
            }));
        }
        catch(ConnectException connectexception)
        {
            if(GuiConnecting.isCancelled(connectingGui))
            {
                return;
            }
            mc.displayGuiScreen(new GuiDisconnected("connect.failed", "disconnect.genericReason", new Object[] {
                connectexception.getMessage()
            }));
        }
        catch(Exception exception)
        {
            if(GuiConnecting.isCancelled(connectingGui))
            {
                return;
            }
            exception.printStackTrace();
            mc.displayGuiScreen(new GuiDisconnected("connect.failed", "disconnect.genericReason", new Object[] {
                exception.toString()
            }));
        }
    }
}
