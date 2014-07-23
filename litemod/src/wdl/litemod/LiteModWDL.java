package wdl.litemod;

import java.io.File;
import java.util.List;

import wdl.GuiWDL;
import wdl.WDL;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.util.IChatComponent;

import com.mumfrey.liteloader.LiteMod;
import com.mumfrey.liteloader.RenderListener;
import com.mumfrey.liteloader.transformers.event.EventInfo;
import com.mumfrey.liteloader.transformers.event.ReturnEventInfo;

public class LiteModWDL implements LiteMod, RenderListener {

	@Override
	public String getName() {
		return "LiteModWDL";
	}

	@Override
	public String getVersion() {
		return "1.7.10";
	}

	@Override
	public void init(File configPath) {
		WDL.init();
	}

	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath) {
		
	}
	
	public static void ingameMenuInit(EventInfo<GuiIngameMenu> eventinfo) {}
	
	public static void ingameMenuActionPerformed(EventInfo<GuiIngameMenu> eventinfo, GuiButton guibutton) {
		//more up here
		switch(guibutton.id){
		    case 50:
		        if (WDL.downloading)
		        {
		            WDL.stop();
		            WDL.mc.displayGuiScreen((GuiScreen)null);
		            WDL.mc.setIngameFocus();
		        }
		        else
		        {
		            WDL.start();
		        }
		        
		        break;
		
		    case 51:
		        Minecraft.getMinecraft().displayGuiScreen(new GuiWDL(eventinfo.getSource()));
		        break;
		}
	}
	
	public static void worldClientTick(EventInfo<WorldClient> eventinfo) {
		//more up here
        if( WDL.downloading )
        {
            if( WDL.tp.openContainer != WDL.windowContainer )
            {
                if( WDL.tp.openContainer == WDL.tp.inventoryContainer )
                    WDL.onItemGuiClosed();
                else
                    WDL.onItemGuiOpened();
                WDL.windowContainer = WDL.tp.openContainer;
            }
        }
	}
	
	public static void worldClientDoPreChunk(EventInfo<WorldClient> eventinfo, int p_73025_1_, int p_73025_2_, boolean p_73025_3_) {
        if (p_73025_3_)
        {
            /* WDL >>> */
            if( eventinfo.getSource() != WDL.wc )
                WDL.onWorldLoad();
            /* <<< WDL */
            
        }
        else
        {
            /* WDL >>> */
            if( WDL.downloading )
                WDL.onChunkNoLongerNeeded( eventinfo.getSource().getChunkProvider().provideChunk(p_73025_1_, p_73025_2_) );
            /* <<< WDL */
        }
        //more down here
	}
	
	public static void worldClientRemoveEntityFromWorld(ReturnEventInfo<WorldClient, Entity> eventinfo, int p_73028_1_) { //return entity
        // If the entity is being removed and it's outside the default tracking range,
        // go ahead and remember it until the chunk is saved.
        if(WDL.downloading)
        {
            Entity entity = eventinfo.getSource().getEntityByID(p_73028_1_);
            if(entity != null)
            {
                int threshold = 0;
                if ((entity instanceof EntityFishHook) ||
                    //(entity instanceof EntityArrow) ||
                    //(entity instanceof EntitySmallFireball) ||
                    //(entity instanceof EntitySnowball) ||
                    (entity instanceof EntityEnderPearl) ||
                    (entity instanceof EntityEnderEye) ||
                    (entity instanceof EntityEgg) ||
                    (entity instanceof EntityPotion) ||
                    (entity instanceof EntityExpBottle) ||
                    (entity instanceof EntityItem) ||
                    (entity instanceof EntitySquid))
                {
                    threshold = 64;
                }
                else if ((entity instanceof EntityMinecart) ||
                         (entity instanceof EntityBoat) ||
                         (entity instanceof IAnimals))
                {
                    threshold = 80;
                }
                else if ((entity instanceof EntityDragon) ||
                         (entity instanceof EntityTNTPrimed) ||
                         (entity instanceof EntityFallingBlock) ||
                         (entity instanceof EntityPainting) ||
                         (entity instanceof EntityXPOrb))
                {
                    threshold = 160;
                }
                double distance = entity.getDistance(WDL.tp.posX, entity.posY, WDL.tp.posZ);
                if( distance > threshold)
                {
                    WDL.chatDebug("removeEntityFromWorld: Refusing to remove " + EntityList.getEntityString(entity) + " at distance " + distance);
                    //return null; //cancel right here
                    eventinfo.setReturnValue(null);
                }
                WDL.chatDebug("removeEntityFromWorld: Removing " + EntityList.getEntityString(entity) + " at distance " + distance);
            }
        }
        //more down here
	}
	
	public static void worldClientAddBlockEvent(EventInfo<WorldClient> eventinfo, int par1, int par2, int par3, Block par4, int par5, int par6) {
		//more up here
        if( WDL.downloading )
            WDL.onBlockEvent( par1, par2, par3, par4, par5, par6 );
	}
	
	public static void netHandlerPlayClientHandleDisconnect(EventInfo<NetHandlerPlayClient> eventinfo, S40PacketDisconnect p_147253_1_) {
        if (WDL.downloading)
        {
            WDL.stop();

            try
            {
                Thread.sleep(2000L);
            }
            catch (Exception var3)
            {

            }
        }
        //more down here
	}
	
	public static void netHandlerPlayClientOnDisconnect(EventInfo<NetHandlerPlayClient> eventinfo, IChatComponent p_147231_1_) {
        if (WDL.downloading)
        {
            WDL.stop();

            try
            {
                Thread.sleep(2000L);
            }
            catch (Exception var3)
            {
 
            }
        }
        //more down here
	}
	
	public static void netHandlerPlayClientHandleChat(EventInfo<NetHandlerPlayClient> eventinfo, S02PacketChat p_147251_1_) {
        String var2 = p_147251_1_.func_148915_c().getFormattedText();
        WDL.handleServerSeedMessage(var2);
        //more down here
	}

	@Override
	public void onRender() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRenderGui(GuiScreen currentScreen) {
		if ((currentScreen instanceof GuiIngameMenu) && !(currentScreen instanceof GuiScreenInject)) {
			WDL.mc.displayGuiScreen(new GuiScreenInject());
		}
		
	}

	@Override
	public void onRenderWorld() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSetupCameraTransform() {
		// TODO Auto-generated method stub
		
	}

}
