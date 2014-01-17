package net.minecraft.client.gui;

import io.netty.util.concurrent.GenericFutureListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.mco.ExceptionMcoService;
import net.minecraft.client.mco.ExceptionRetryCall;
import net.minecraft.client.mco.McoClient;
import net.minecraft.client.mco.McoServer;
import net.minecraft.client.mco.McoServerAddress;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* WDL >>> */
import net.minecraft.wdl.WDL;
/* <<< WDL */

public class TaskOnlineConnect extends TaskLongRunning
{
    private static final AtomicInteger field_148439_a = new AtomicInteger(0);
    private static final Logger logger = LogManager.getLogger();
    private NetworkManager field_148436_d;
    private final McoServer field_148437_e;
    private final GuiScreen field_148435_f;
    private static final String __OBFID = "CL_00000790";

    public TaskOnlineConnect(GuiScreen par1GuiScreen, McoServer par2McoServer)
    {
        this.field_148435_f = par1GuiScreen;
        this.field_148437_e = par2McoServer;
    }

    public void run()
    {
        this.func_148417_b(I18n.format("mco.connect.connecting", new Object[0]));
        Session var1 = this.func_148413_b().getSession();
        McoClient var2 = new McoClient(var1.getSessionID(), var1.getUsername(), "1.7.2", Minecraft.getMinecraft().getProxy());
        boolean var3 = false;
        boolean var4 = false;
        int var5 = 5;
        McoServerAddress var6 = null;
        boolean var7 = false;

        for (int var8 = 0; var8 < 10 && !this.func_148418_c(); ++var8)
        {
            try
            {
                var6 = var2.func_148688_b(this.field_148437_e.field_148812_a);
                var3 = true;
            }
            catch (ExceptionRetryCall var10)
            {
                var5 = var10.field_148832_d;
            }
            catch (ExceptionMcoService var11)
            {
                if (var11.field_148830_c == 6002)
                {
                    var7 = true;
                }
                else
                {
                    var4 = true;
                    this.func_148416_a(var11.toString());
                    logger.error("Couldn\'t connect to world", var11);
                }

                break;
            }
            catch (IOException var12)
            {
                logger.error("Couldn\'t parse response connecting to world", var12);
            }
            catch (Exception var13)
            {
                var4 = true;
                logger.error("Couldn\'t connect to world", var13);
                this.func_148416_a(var13.getLocalizedMessage());
            }

            if (var3)
            {
                break;
            }

            this.func_148429_a(var5);
        }

        if (var7)
        {
            this.func_148413_b().displayGuiScreen(new GuiScreenReamlsTOS(this.field_148435_f, this.field_148437_e));
        }
        else if (!this.func_148418_c() && !var4)
        {
            if (var3)
            {
                ServerAddress var14 = ServerAddress.func_78860_a(var6.field_148770_a);
                this.func_148432_a(var14.getIP(), var14.getPort());
            }
            else
            {
                this.func_148413_b().displayGuiScreen(this.field_148435_f);
            }
        }
    }

    private void func_148429_a(int p_148429_1_)
    {
        try
        {
            Thread.sleep((long)(p_148429_1_ * 1000));
        }
        catch (InterruptedException var3)
        {
            logger.warn(var3.getLocalizedMessage());
        }
    }

    private void func_148432_a(final String p_148432_1_, final int p_148432_2_)
    {
        /* WDL >>> */
        WDL.mcos = this.field_148437_e;
        /* <<< WDL */

        (new Thread("MCO Connector #" + field_148439_a.incrementAndGet())
        {
            private static final String __OBFID = "CL_00000791";
            public void run()
            {
                try
                {
                    if (TaskOnlineConnect.this.func_148418_c())
                    {
                        return;
                    }

                    TaskOnlineConnect.this.field_148436_d = NetworkManager.provideLanClient(InetAddress.getByName(p_148432_1_), p_148432_2_);

                    if (TaskOnlineConnect.this.func_148418_c())
                    {
                        return;
                    }

                    TaskOnlineConnect.this.field_148436_d.setNetHandler(new NetHandlerLoginClient(TaskOnlineConnect.this.field_148436_d, TaskOnlineConnect.this.func_148413_b(), TaskOnlineConnect.this.field_148435_f));

                    if (TaskOnlineConnect.this.func_148418_c())
                    {
                        return;
                    }

                    TaskOnlineConnect.this.field_148436_d.scheduleOutboundPacket(new C00Handshake(4, p_148432_1_, p_148432_2_, EnumConnectionState.LOGIN), new GenericFutureListener[0]);

                    if (TaskOnlineConnect.this.func_148418_c())
                    {
                        return;
                    }

                    TaskOnlineConnect.this.field_148436_d.scheduleOutboundPacket(new C00PacketLoginStart(TaskOnlineConnect.this.func_148413_b().getSession().func_148256_e()), new GenericFutureListener[0]);
                    TaskOnlineConnect.this.func_148417_b(I18n.format("mco.connect.authorizing", new Object[0]));
                }
                catch (UnknownHostException var2)
                {
                    if (TaskOnlineConnect.this.func_148418_c())
                    {
                        return;
                    }

                    TaskOnlineConnect.logger.error("Couldn\'t connect to world", var2);
                    TaskOnlineConnect.this.func_148413_b().displayGuiScreen(new GuiScreenDisconnectedOnline(TaskOnlineConnect.this.field_148435_f, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", new Object[] {"Unknown host \'" + p_148432_1_ + "\'"})));
                }
                catch (Exception var3)
                {
                    if (TaskOnlineConnect.this.func_148418_c())
                    {
                        return;
                    }

                    TaskOnlineConnect.logger.error("Couldn\'t connect to world", var3);
                    TaskOnlineConnect.this.func_148413_b().displayGuiScreen(new GuiScreenDisconnectedOnline(TaskOnlineConnect.this.field_148435_f, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", new Object[] {var3.toString()})));
                }
            }
        }).start();
    }

    public void func_148414_a()
    {
        if (this.field_148436_d != null)
        {
            this.field_148436_d.processReceivedPackets();
        }
    }
}
